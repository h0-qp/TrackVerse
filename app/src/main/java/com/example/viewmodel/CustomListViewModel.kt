package com.example.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.network.TmdbShow
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.util.UUID

data class CustomListMinShow(
    val id: Int = 0,
    val title: String = "",
    val posterPath: String? = null,
    val isMovie: Boolean = false
)

data class CustomList(
    val id: String = "",
    val name: String = "",
    val shows: List<CustomListMinShow> = emptyList(),
    val isPublic: Boolean = false,
    val ownerId: String = "",
    val ownerName: String = "",
    val createdAt: Long = 0L
)

class CustomListViewModel : ViewModel() {
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    private val _myLists = MutableStateFlow<List<CustomList>>(emptyList())
    val myLists: StateFlow<List<CustomList>> = _myLists.asStateFlow()

    private val _publicLists = MutableStateFlow<List<CustomList>>(emptyList())
    val publicLists: StateFlow<List<CustomList>> = _publicLists.asStateFlow()

    init {
        loadMyLists()
        loadPublicLists()
    }

    fun loadMyLists() {
        val user = auth.currentUser ?: return
        db.collection("users").document(user.uid).collection("custom_lists")
            .addSnapshotListener { snapshot, _ ->
                if (snapshot != null) {
                    val lists = snapshot.documents.mapNotNull { it.toObject(CustomList::class.java) }
                    _myLists.value = lists
                }
            }
    }

    fun loadPublicLists() {
        db.collectionGroup("custom_lists")
            .whereEqualTo("public", true)
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, _ ->
                if (snapshot != null) {
                    val lists = snapshot.documents.mapNotNull { it.toObject(CustomList::class.java) }
                    _publicLists.value = lists
                }
            }
    }

    fun createList(name: String, isPublic: Boolean) {
        val user = auth.currentUser ?: return
        viewModelScope.launch {
            val listId = UUID.randomUUID().toString()
            val newList = CustomList(
                id = listId,
                name = name,
                shows = emptyList(),
                isPublic = isPublic,
                ownerId = user.uid,
                ownerName = user.displayName ?: "User",
                createdAt = System.currentTimeMillis()
            )
            db.collection("users").document(user.uid)
                .collection("custom_lists").document(listId)
                .set(newList).await()
        }
    }

    fun addShowToList(listId: String, show: TmdbShow, isMovie: Boolean) {
        val user = auth.currentUser ?: return
        viewModelScope.launch {
            val listDoc = db.collection("users").document(user.uid)
                .collection("custom_lists").document(listId).get().await()
            val customList = listDoc.toObject(CustomList::class.java) ?: return@launch
            
            val minShow = CustomListMinShow(
                id = show.id,
                title = show.displayTitle,
                posterPath = show.posterPath,
                isMovie = isMovie
            )
            
            if (!customList.shows.any { it.id == show.id }) {
                val updatedShows = customList.shows + minShow
                db.collection("users").document(user.uid)
                    .collection("custom_lists").document(listId)
                    .update("shows", updatedShows).await()
            }
        }
    }

    fun removeShowFromList(listId: String, showId: Int) {
        val user = auth.currentUser ?: return
        viewModelScope.launch {
            val listDoc = db.collection("users").document(user.uid)
                .collection("custom_lists").document(listId).get().await()
            val customList = listDoc.toObject(CustomList::class.java) ?: return@launch
            
            val updatedShows = customList.shows.filter { it.id != showId }
            db.collection("users").document(user.uid)
                .collection("custom_lists").document(listId)
                .update("shows", updatedShows).await()
        }
    }
}
