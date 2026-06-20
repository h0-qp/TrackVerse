package com.example.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

data class Review(
    val id: String = "",
    val userId: String = "",
    val userName: String = "",
    val showId: Int = 0,
    val isMovie: Boolean = false,
    val rating: Float = 0f,
    val text: String = "",
    val timestamp: Long = System.currentTimeMillis()
)

class ReviewViewModel : ViewModel() {
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    private val _reviews = MutableStateFlow<List<Review>>(emptyList())
    val reviews: StateFlow<List<Review>> = _reviews.asStateFlow()

    private val _averageRating = MutableStateFlow(0f)
    val averageRating: StateFlow<Float> = _averageRating.asStateFlow()

    fun loadReviews(showId: Int, isMovie: Boolean) {
        viewModelScope.launch {
            try {
                val snapshot = db.collection("reviews")
                    .whereEqualTo("showId", showId)
                    .whereEqualTo("isMovie", isMovie)
                    .orderBy("timestamp", Query.Direction.DESCENDING)
                    .get()
                    .await()

                val items = snapshot.documents.mapNotNull { doc ->
                    doc.toObject(Review::class.java)?.copy(id = doc.id)
                }
                _reviews.value = items
                
                if (items.isNotEmpty()) {
                    _averageRating.value = items.map { it.rating }.average().toFloat()
                } else {
                    _averageRating.value = 0f
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun submitReview(showId: Int, isMovie: Boolean, rating: Float, text: String) {
        val user = auth.currentUser ?: return
        viewModelScope.launch {
            try {
                val reviewRef = db.collection("reviews").document()
                val review = Review(
                    id = reviewRef.id,
                    userId = user.uid,
                    userName = user.email?.substringBefore("@") ?: "Anonymous",
                    showId = showId,
                    isMovie = isMovie,
                    rating = rating,
                    text = text,
                    timestamp = System.currentTimeMillis()
                )
                reviewRef.set(review).await()
                loadReviews(showId, isMovie)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}
