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
                        overview = doc.getString("overview")
                    )
                }
                _watchlist.value = items
            } catch (e: Exception) {
                // handle error
            }
        }
    }

    fun addToWatchlist(show: TmdbShow) {
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
                    "overview" to show.overview
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
