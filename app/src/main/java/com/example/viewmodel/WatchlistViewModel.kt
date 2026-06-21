package com.example.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.AppDatabase
import com.example.data.WatchlistEntity
import com.example.network.TmdbShow
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class WatchlistViewModel(application: Application) : AndroidViewModel(application) {
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private val appDb = AppDatabase.getDatabase(application)
    
    private val _watchlist = MutableStateFlow<List<TmdbShow>>(emptyList())
    val watchlist: StateFlow<List<TmdbShow>> = _watchlist.asStateFlow()
    
    private val _watchedEpisodesCount = MutableStateFlow<Map<Int, Int>>(emptyMap())
    val watchedEpisodesCount: StateFlow<Map<Int, Int>> = _watchedEpisodesCount.asStateFlow()

    private val _watchedEpisodesList = MutableStateFlow<Map<Int, List<Int>>>(emptyMap())
    val watchedEpisodesList: StateFlow<Map<Int, List<Int>>> = _watchedEpisodesList.asStateFlow()

    init {
        // Collect from local DB for offline access
        viewModelScope.launch {
            appDb.watchlistDao().getAllWatchlistItems().collect { entities ->
                _watchlist.value = entities.map { it.toTmdbShow() }
            }
        }
    }

    fun loadWatchlist() {
        val user = auth.currentUser ?: return
        viewModelScope.launch {
            try {
                val snapshot = db.collection("users").document(user.uid).collection("watchlist").get().await()
                val items = snapshot.documents.mapNotNull { doc ->
                    val nextEpisodeMap = doc.get("nextEpisodeToAir") as? Map<String, Any>
                    val nextEpisode = if (nextEpisodeMap != null) {
                        try {
                            com.example.network.TmdbEpisode(
                                id = (nextEpisodeMap["id"] as? Long)?.toInt() ?: 0,
                                name = nextEpisodeMap["name"] as? String,
                                airDate = nextEpisodeMap["airDate"] as? String,
                                episodeNumber = (nextEpisodeMap["episodeNumber"] as? Long)?.toInt(),
                                seasonNumber = (nextEpisodeMap["seasonNumber"] as? Long)?.toInt()
                            )
                        } catch (e: Exception) { null }
                    } else null

                    TmdbShow(
                        id = doc.getLong("id")?.toInt() ?: 0,
                        name = doc.getString("name"),
                        title = doc.getString("title"),
                        posterPath = doc.getString("posterPath"),
                        backdropPath = doc.getString("backdropPath"),
                        voteAverage = doc.getDouble("voteAverage"),
                        overview = doc.getString("overview"),
                        numberOfEpisodes = doc.getLong("numberOfEpisodes")?.toInt(),
                        numberOfSeasons = doc.getLong("numberOfSeasons")?.toInt(),
                        nextEpisodeToAir = nextEpisode
                    )
                }
                
                val counts = snapshot.documents.associate { doc ->
                    val showId = doc.getLong("id")?.toInt() ?: 0
                    val count = doc.getLong("watchedEpisodes")?.toInt() ?: 0
                    showId to count
                }
                val lists = snapshot.documents.associate { doc ->
                    val showId = doc.getLong("id")?.toInt() ?: 0
                    val watchedList = (doc.get("watchedEpisodeIds") as? List<*>)?.mapNotNull { (it as? Long)?.toInt() } ?: emptyList()
                    showId to watchedList
                }
                
                _watchedEpisodesCount.value = counts
                _watchedEpisodesList.value = lists
                
                // Cache locally
                appDb.watchlistDao().insertItems(items.map { WatchlistEntity.fromTmdbShow(it, it.title != null) })
            } catch (e: Exception) {
                // Ignore error, local DB collection provides offline data
            }
        }
    }

    fun addToWatchlist(show: TmdbShow, isTracking: Boolean = true) {
        val user = auth.currentUser ?: return
        viewModelScope.launch {
            try {
                val showMap = hashMapOf<String, Any?>(
                    "id" to show.id,
                    "name" to show.name,
                    "title" to show.title,
                    "posterPath" to show.posterPath,
                    "backdropPath" to show.backdropPath,
                    "voteAverage" to show.voteAverage,
                    "overview" to show.overview,
                    "numberOfEpisodes" to show.numberOfEpisodes,
                    "numberOfSeasons" to show.numberOfSeasons,
                    "isTracking" to isTracking,
                    "watchedEpisodes" to (_watchedEpisodesCount.value[show.id] ?: 0),
                    "watchedEpisodeIds" to (_watchedEpisodesList.value[show.id] ?: emptyList<Int>()),
                    "nextEpisodeToAir" to show.nextEpisodeToAir?.let {
                        mapOf(
                            "id" to it.id,
                            "name" to it.name,
                            "airDate" to it.airDate,
                            "episodeNumber" to it.episodeNumber,
                            "seasonNumber" to it.seasonNumber
                        )
                    }
                )
                db.collection("users").document(user.uid)
                    .collection("watchlist").document(show.id.toString())
                    .set(showMap).await()
                    
                appDb.watchlistDao().insertItem(WatchlistEntity.fromTmdbShow(show, show.title != null))

                // Log activity
                val act = hashMapOf(
                    "userId" to user.uid,
                    "userName" to (user.displayName ?: user.email?.substringBefore("@") ?: "User"),
                    "actionType" to if (isTracking) "ADDED_WATCHLIST" else "REMOVED_WATCHLIST",
                    "showId" to show.id,
                    "showName" to show.displayTitle,
                    "details" to "",
                    "timestamp" to System.currentTimeMillis()
                )
                db.collection("activity").add(act)

                loadWatchlist() // Refresh
            } catch (e: Exception) {
                // handle error
            }
        }
    }

    fun toggleEpisodeWatched(showId: Int, episodeId: Int) {
        val user = auth.currentUser ?: return
        viewModelScope.launch {
            try {
                val currentList = _watchedEpisodesList.value[showId]?.toMutableList() ?: mutableListOf()
                if (currentList.contains(episodeId)) {
                    currentList.removeAll { it == episodeId }
                } else {
                    currentList.add(episodeId)
                }
                
                val updatedLists = _watchedEpisodesList.value.toMutableMap()
                updatedLists[showId] = currentList
                _watchedEpisodesList.value = updatedLists

                val updatedCounts = _watchedEpisodesCount.value.toMutableMap()
                updatedCounts[showId] = currentList.size
                _watchedEpisodesCount.value = updatedCounts
                
                db.collection("users").document(user.uid)
                  .collection("watchlist").document(showId.toString())
                  .update(
                      mapOf(
                          "watchedEpisodeIds" to currentList,
                          "watchedEpisodes" to currentList.size
                      )
                  ).await()
                  
                if (currentList.contains(episodeId)) {
                    val act = hashMapOf(
                        "userId" to user.uid,
                        "userName" to (user.displayName ?: user.email?.substringBefore("@") ?: "User"),
                        "actionType" to "WATCHED_EPISODE",
                        "showId" to showId,
                        "showName" to "Show $showId", // Will just put ID since name isn't passed
                        "details" to "Episode ID: $episodeId",
                        "timestamp" to System.currentTimeMillis()
                    )
                    db.collection("activity").add(act)
                }
                
            } catch (e: Exception) {
                // handle error
            }
        }
    }

    fun updateWatchedEpisodes(showId: Int, count: Int) {
        val user = auth.currentUser ?: return
        viewModelScope.launch {
            try {
                db.collection("users").document(user.uid)
                  .collection("watchlist").document(showId.toString())
                  .update("watchedEpisodes", count).await()
                loadWatchlist()
            } catch (e: Exception) {
                // handle error
            }
        }
    }

    fun removeFromWatchlist(showId: Int) {
        val user = auth.currentUser ?: return
        viewModelScope.launch {
            try {
                db.collection("users").document(user.uid)
                  .collection("watchlist").document(showId.toString())
                  .delete().await()
                appDb.watchlistDao().deleteItem(showId)
                loadWatchlist()
            } catch (e: Exception) {
                // handle error
            }
        }
    }
}
