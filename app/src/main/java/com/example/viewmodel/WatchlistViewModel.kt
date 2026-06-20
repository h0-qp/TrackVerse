package com.example.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.network.TmdbShow
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class WatchlistViewModel : ViewModel() {
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    
    private val _watchlist = MutableStateFlow<List<TmdbShow>>(emptyList())
    val watchlist: StateFlow<List<TmdbShow>> = _watchlist
    
    // Local tracking map for watched episodes
    private val _watchedEpisodesCount = MutableStateFlow<Map<Int, Int>>(emptyMap())
    val watchedEpisodesCount: StateFlow<Map<Int, Int>> = _watchedEpisodesCount

    fun loadWatchlist() {
        val user = auth.currentUser ?: return
        viewModelScope.launch {
            try {
                val snapshot = db.collection("users").document(user.uid).collection("watchlist").get().await()
                val items = snapshot.documents.mapNotNull { doc ->
                    TmdbShow(
                        id = doc.getLong("id")?.toInt() ?: 0,
                        name = doc.getString("name"),
                        title = doc.getString("title"),
                        posterPath = doc.getString("posterPath"),
                        backdropPath = doc.getString("backdropPath"),
                        voteAverage = doc.getDouble("voteAverage"),
                        overview = doc.getString("overview"),
                        numberOfEpisodes = doc.getLong("numberOfEpisodes")?.toInt(),
                        numberOfSeasons = doc.getLong("numberOfSeasons")?.toInt()
                    )
                }
                
                val counts = snapshot.documents.associate { doc ->
                    val showId = doc.getLong("id")?.toInt() ?: 0
                    val count = doc.getLong("watchedEpisodes")?.toInt() ?: 0
                    showId to count
                }
                _watchedEpisodesCount.value = counts
                _watchlist.value = items
            } catch (e: Exception) {
                // handle error
            }
        }
    }

    fun addToWatchlist(show: TmdbShow, isTracking: Boolean = true) {
        val user = auth.currentUser ?: return
        viewModelScope.launch {
            try {
                val showMap = hashMapOf(
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
                    "watchedEpisodes" to (_watchedEpisodesCount.value[show.id] ?: 0)
                )
                db.collection("users").document(user.uid)
                    .collection("watchlist").document(show.id.toString())
                    .set(showMap).await()
                loadWatchlist() // Refresh
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
                loadWatchlist()
            } catch (e: Exception) {
                // handle error
            }
        }
    }
}
