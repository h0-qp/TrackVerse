package com.example.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.network.TmdbShow
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.tasks.await
import androidx.lifecycle.viewModelScope

class WatchlistViewModel(application: Application) : AndroidViewModel(application) {
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    
    private val _watchlist = MutableStateFlow<List<TmdbShow>>(emptyList())
    val watchlist: StateFlow<List<TmdbShow>> = _watchlist.asStateFlow()

    private val _favorites = MutableStateFlow<List<TmdbShow>>(emptyList())
    val favorites: StateFlow<List<TmdbShow>> = _favorites.asStateFlow()

    private val _favoriteIds = MutableStateFlow<Set<Int>>(emptySet())
    val favoriteIds: StateFlow<Set<Int>> = _favoriteIds.asStateFlow()

    private val _watchedEpisodesCount = MutableStateFlow<Map<Int, Int>>(emptyMap())
    val watchedEpisodesCount: StateFlow<Map<Int, Int>> = _watchedEpisodesCount.asStateFlow()

    private val _watchedEpisodesList = MutableStateFlow<Map<Int, List<Int>>>(emptyMap())
    val watchedEpisodesList: StateFlow<Map<Int, List<Int>>> = _watchedEpisodesList.asStateFlow()

    private val _unwatchedEpisodes = MutableStateFlow<List<Pair<com.example.network.TmdbShow, com.example.network.TmdbEpisode>>>(emptyList())
    val unwatchedEpisodes: StateFlow<List<Pair<com.example.network.TmdbShow, com.example.network.TmdbEpisode>>> = _unwatchedEpisodes.asStateFlow()

    private var authListener: FirebaseAuth.AuthStateListener? = null
    private var listenerRegistration: com.google.firebase.firestore.ListenerRegistration? = null
    private var episodesFetchJob: kotlinx.coroutines.Job? = null

    fun loadAllUnwatchedEpisodes() {
        val currentWatchlist = _watchlist.value.filter { it.title == null }
        val watched = _watchedEpisodesList.value

        episodesFetchJob?.cancel()
        episodesFetchJob = viewModelScope.launch(kotlinx.coroutines.Dispatchers.IO) {
            val scope = this
            val allEps = java.util.concurrent.ConcurrentLinkedQueue<Pair<com.example.network.TmdbShow, com.example.network.TmdbEpisode>>()
            val semaphore = kotlinx.coroutines.sync.Semaphore(10)
            
            val deferreds = currentWatchlist.flatMap { show ->
                val numSeasons = show.numberOfSeasons ?: 1
                (1..numSeasons).map { s ->
                    scope.async {
                        semaphore.acquire()
                        try {
                            val seasonResp = com.example.network.ApiClient.tmdbService.getSeasonDetails(show.id, s)
                            seasonResp.episodes?.forEach { ep ->
                                if (watched[show.id]?.contains(ep.id) != true) {
                                    if (!ep.airDate.isNullOrEmpty()) {
                                        allEps.add(Pair(show, ep))
                                    }
                                }
                            }
                        } catch (e: Exception) {
                            // ignore
                        } finally {
                            semaphore.release()
                        }
                    }
                }
            }
            deferreds.awaitAll()
            
            _unwatchedEpisodes.value = allEps.toList()
        }
    }

    init {
        // مراقبة الدخول لكي نجلب مسلسلات المستخدم من السيرفر مباشرة
        authListener = FirebaseAuth.AuthStateListener { firebaseAuth ->
            val user = firebaseAuth.currentUser
            if (user != null) {
                getWatchedEpisodes(user.uid) // الجلب السحابي
            } else {
                clearWatchlist()
            }
        }
        auth.addAuthStateListener(authListener!!)
    }

    // دالة لحفظ وتحديث الحلقات المشاهدة بالسيرفر
    fun saveWatchedEpisode(showId: Int, episodeId: Int) {
        val user = auth.currentUser ?: return
        viewModelScope.launch {
            try {
                val currentList = _watchedEpisodesList.value[showId] ?: emptyList()
                val isWatched = currentList.contains(episodeId)

                val newList = if (isWatched) {
                    currentList - episodeId
                } else {
                    currentList + episodeId
                }

                // نستخدم merge() للتأكد من تحديث الحقل بشكل آمن دون مسح البيانات السابقة للمسلسل
                val data = hashMapOf(
                    "watchedEpisodeIds" to newList,
                    "watchedEpisodes" to newList.size
                )
                db.collection("users").document(user.uid)
                  .collection("watchlist").document(showId.toString())
                  .set(data, SetOptions.merge())
                  .addOnSuccessListener {
                      android.util.Log.d("Watchlist", "saveWatchedEpisode success")
                  }
                  .addOnFailureListener {
                      android.util.Log.e("Watchlist", "saveWatchedEpisode failed", it)
                  }
                  
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun markSeasonWatched(showId: Int, episodeIds: List<Int>) {
        val user = auth.currentUser ?: return
        viewModelScope.launch {
            try {
                val currentList = _watchedEpisodesList.value[showId] ?: emptyList()
                val currentSet = currentList.toMutableSet()
                currentSet.addAll(episodeIds)
                val newList = currentSet.toList()

                val data = hashMapOf(
                    "watchedEpisodeIds" to newList,
                    "watchedEpisodes" to newList.size
                )
                db.collection("users").document(user.uid)
                  .collection("watchlist").document(showId.toString())
                  .set(data, SetOptions.merge())
                  .addOnSuccessListener {
                      android.util.Log.d("Watchlist", "markSeasonWatched success")
                  }
                  .addOnFailureListener {
                      android.util.Log.e("Watchlist", "markSeasonWatched failed", it)
                  }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    // إعادة تعبئة القوائم وجلب البيانات سحابياً
    private fun getWatchedEpisodes(userId: String) {
        listenerRegistration?.remove()
        
        // جلبها بشكل سريع من السيرفر كإجراء مبدئي (لضمان وصول أحدث البيانات دائماً)
        db.collection("users").document(userId).collection("watchlist")
            .get(com.google.firebase.firestore.Source.SERVER)
            .addOnSuccessListener { snapshot ->
                parseAndUpdateLists(snapshot)
            }
            .addOnFailureListener {
                android.util.Log.e("Watchlist", "Failed initial server fetch", it)
            }
            
        // وضع مستمع للرد الفوري على التغييرات الجديدة
        listenerRegistration = db.collection("users").document(userId).collection("watchlist")
            .addSnapshotListener { snapshot, e ->
                if (e != null) {
                    android.util.Log.e("Watchlist", "Listen failed.", e)
                    return@addSnapshotListener
                }
                if (snapshot != null) {
                    parseAndUpdateLists(snapshot)
                }
            }
    }

    private fun parseAndUpdateLists(snapshot: com.google.firebase.firestore.QuerySnapshot) {
        val items = snapshot.documents.mapNotNull { doc ->
            // تحويل تفاصيل الحلقة القادمة إن وجدت
            val nextEpisodeMap = doc.get("nextEpisodeToAir") as? Map<String, Any>
            val nextEpisode = if (nextEpisodeMap != null) {
                try {
                    com.example.network.TmdbEpisode(
                        id = (nextEpisodeMap["id"] as? Number)?.toInt() ?: 0,
                        name = nextEpisodeMap["name"] as? String,
                        airDate = nextEpisodeMap["airDate"] as? String,
                        episodeNumber = (nextEpisodeMap["episodeNumber"] as? Number)?.toInt(),
                        seasonNumber = (nextEpisodeMap["seasonNumber"] as? Number)?.toInt(),
                        stillPath = null,
                        overview = null,
                        voteAverage = null,
                        runtime = null
                    )
                } catch (e: Exception) { 
                    null 
                }
            } else null

            val lastEpisodeMap = doc.get("lastEpisodeToAir") as? Map<String, Any>
            val lastEpisode = if (lastEpisodeMap != null) {
                try {
                    com.example.network.TmdbEpisode(
                        id = (lastEpisodeMap["id"] as? Number)?.toInt() ?: 0,
                        name = lastEpisodeMap["name"] as? String,
                        airDate = lastEpisodeMap["airDate"] as? String,
                        episodeNumber = (lastEpisodeMap["episodeNumber"] as? Number)?.toInt(),
                        seasonNumber = (lastEpisodeMap["seasonNumber"] as? Number)?.toInt(),
                        stillPath = null,
                        overview = null,
                        voteAverage = null,
                        runtime = null
                    )
                } catch (e: Exception) { 
                    null 
                }
            } else null

            try {
                TmdbShow(
                    id = doc.getLong("id")?.toInt() ?: 0,
                    name = doc.getString("name"),
                    originalName = doc.getString("originalName"),
                    title = doc.getString("title"),
                    originalTitle = doc.getString("originalTitle"),
                    posterPath = doc.getString("posterPath"),
                    backdropPath = doc.getString("backdropPath"),
                    voteAverage = (doc.get("voteAverage") as? Number)?.toDouble(),
                    overview = doc.getString("overview"),
                    numberOfEpisodes = doc.getLong("numberOfEpisodes")?.toInt(),
                    numberOfSeasons = doc.getLong("numberOfSeasons")?.toInt(),
                    nextEpisodeToAir = nextEpisode,
                    lastEpisodeToAir = lastEpisode
                )
            } catch (e: Exception) {
                null
            }
        }
        
        val counts = snapshot.documents.associate { doc ->
            val showId = doc.getLong("id")?.toInt() ?: 0
            val listCount = (doc.get("watchedEpisodeIds") as? List<*>)?.size ?: 0
            val fieldCount = doc.getLong("watchedEpisodes")?.toInt() ?: 0
            val count = maxOf(listCount, fieldCount)
            showId to count
        }
        
        val lists = snapshot.documents.associate { doc ->
            val showId = doc.getLong("id")?.toInt() ?: 0
            val watchedList = (doc.get("watchedEpisodeIds") as? List<*>)?.mapNotNull { (it as? Number)?.toInt() } ?: emptyList()
            showId to watchedList
        }

        val favIds = snapshot.documents.filter { it.getBoolean("isFavorite") == true }.mapNotNull { it.getLong("id")?.toInt() }.toSet()
        val favItems = items.filter { it.id in favIds }
        
        // تحديث القوائم الحية
        _watchlist.value = items
        _favorites.value = favItems
        _favoriteIds.value = favIds
        _watchedEpisodesCount.value = counts
        _watchedEpisodesList.value = lists
    }

    // إضافة المسلسل بالكامل للسيرفر
    fun addToWatchlist(show: TmdbShow, isTracking: Boolean = true) {
        val user = auth.currentUser
        if (user == null) {
            android.util.Log.e("Watchlist", "Cannot add to watchlist: User is null")
            return
        }
        android.util.Log.d("Watchlist", "Adding ${show.name} to watchlist for user ${user.uid}")
        viewModelScope.launch {
            try {
                val nextEpisodeMap = show.nextEpisodeToAir?.let {
                    hashMapOf(
                        "id" to it.id,
                        "name" to it.name,
                        "airDate" to it.airDate,
                        "episodeNumber" to it.episodeNumber,
                        "seasonNumber" to it.seasonNumber
                    )
                }
                
                val lastEpisodeMap = show.lastEpisodeToAir?.let {
                    hashMapOf(
                        "id" to it.id,
                        "name" to it.name,
                        "airDate" to it.airDate,
                        "episodeNumber" to it.episodeNumber,
                        "seasonNumber" to it.seasonNumber
                    )
                }

                val showMap = hashMapOf(
                    "id" to show.id,
                    "name" to show.name,
                    "originalName" to show.originalName,
                    "title" to show.title,
                    "originalTitle" to show.originalTitle,
                    "posterPath" to show.posterPath,
                    "backdropPath" to show.backdropPath,
                    "voteAverage" to show.voteAverage,
                    "overview" to show.overview,
                    "numberOfEpisodes" to show.numberOfEpisodes,
                    "numberOfSeasons" to show.numberOfSeasons,
                    "nextEpisodeToAir" to nextEpisodeMap,
                    "lastEpisodeToAir" to lastEpisodeMap,
                    "isTracking" to isTracking,
                    "watchedEpisodes" to 0, // القيمة المبدئية 0
                    "addedAt" to System.currentTimeMillis()
                )
                
                db.collection("users").document(user.uid)
                    .collection("watchlist").document(show.id.toString())
                    .set(showMap, SetOptions.merge())
                    .addOnSuccessListener {
                        android.util.Log.d("Watchlist", "addToWatchlist success!")
                    }
                    .addOnFailureListener {
                        android.util.Log.e("Watchlist", "addToWatchlist failed", it)
                    }

                // سجل المزامنة (Log activity)
                val act = hashMapOf(
                    "userId" to user.uid,
                    "action" to "added_to_watchlist",
                    "showId" to show.id,
                    "showName" to (show.name ?: show.title),
                    "timestamp" to System.currentTimeMillis()
                )
                db.collection("activity").add(act)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun updateWatchedEpisodes(showId: Int, count: Int) {
        val user = auth.currentUser ?: return
        viewModelScope.launch {
            try {
                val data = hashMapOf("watchedEpisodes" to count)
                db.collection("users").document(user.uid)
                  .collection("watchlist").document(showId.toString())
                  .set(data, SetOptions.merge())
                  .addOnFailureListener {
                      android.util.Log.e("Watchlist", "updateWatchedEpisodes failed", it)
                  }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun toggleEpisodeWatched(showId: Int, episodeId: Int) {
        saveWatchedEpisode(showId, episodeId)
    }

    fun toggleFavorite(show: TmdbShow) {
        val user = auth.currentUser ?: return
        viewModelScope.launch {
            try {
                val currentIsFav = _favoriteIds.value.contains(show.id)
                val newIsFav = !currentIsFav

                // Ensure it exists in the watchlist collection first
                if (!_watchlist.value.any { it.id == show.id }) {
                    addToWatchlist(show, isTracking = false) // Add with default tracking
                }

                val data = hashMapOf("isFavorite" to newIsFav)
                db.collection("users").document(user.uid)
                  .collection("watchlist").document(show.id.toString())
                  .set(data, SetOptions.merge())
                  .addOnFailureListener {
                      android.util.Log.e("Watchlist", "toggleFavorite failed", it)
                  }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun toggleWatchedEpisode(showId: Int, episodeId: Int) {
        saveWatchedEpisode(showId, episodeId)
    }

    fun removeFromWatchlist(showId: Int) {
        val user = auth.currentUser ?: return
        viewModelScope.launch {
            try {
                db.collection("users").document(user.uid)
                  .collection("watchlist").document(showId.toString())
                  .delete()
                  .addOnFailureListener {
                      android.util.Log.e("Watchlist", "removeFromWatchlist failed", it)
                  }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun loadWatchlist() {
        val user = auth.currentUser ?: return
        getWatchedEpisodes(user.uid)
    }

    fun clearWatchlist() {
        listenerRegistration?.remove()
        listenerRegistration = null
        _watchlist.value = emptyList()
        _watchedEpisodesCount.value = emptyMap()
        _watchedEpisodesList.value = emptyMap()
    }

    override fun onCleared() {
        super.onCleared()
        listenerRegistration?.remove()
        authListener?.let { auth.removeAuthStateListener(it) }
    }
}
