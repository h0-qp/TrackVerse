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

data class UserProfile(
    val uid: String = "",
    val email: String = "",
    val displayName: String = ""
)

data class SocialComment(
    val userId: String = "",
    val userName: String = "",
    val text: String = "",
    val timestamp: Long = System.currentTimeMillis()
)

data class SocialActivity(
    val id: String = "",
    val userId: String = "",
    val userName: String = "",
    val actionType: String = "", // "WATCHED", "ADDED_WATCHLIST", "SUGGESTED"
    val showId: Int = 0,
    val showName: String = "",
    val details: String = "", // S01E03
    val timestamp: Long = System.currentTimeMillis(),
    val likes: List<String> = emptyList(),
    val comments: List<SocialComment> = emptyList(),
    val targetUserId: String = "" // For suggestions
)

class SocialViewModel : ViewModel() {
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    private val _searchResults = MutableStateFlow<List<UserProfile>>(emptyList())
    val searchResults: StateFlow<List<UserProfile>> = _searchResults.asStateFlow()

    private val _following = MutableStateFlow<List<String>>(emptyList())
    val following: StateFlow<List<String>> = _following.asStateFlow()

    private val _followingProfiles = MutableStateFlow<List<UserProfile>>(emptyList())
    val followingProfiles: StateFlow<List<UserProfile>> = _followingProfiles.asStateFlow()
    
    private val _feed = MutableStateFlow<List<SocialActivity>>(emptyList())
    val feed: StateFlow<List<SocialActivity>> = _feed.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    val currentUserId: String? get() = auth.currentUser?.uid

    init {
        saveUserProfile()
        loadFollowing()
        loadFeed()
    }

    private fun saveUserProfile() {
        val user = auth.currentUser ?: return
        viewModelScope.launch {
            try {
                val profile = UserProfile(
                    uid = user.uid,
                    email = user.email ?: "",
                    displayName = user.displayName ?: user.email?.substringBefore("@") ?: "User"
                )
                db.collection("users").document(user.uid).set(profile).await()
            } catch (e: Exception) { e.printStackTrace() }
        }
    }

    fun searchUsers(query: String) {
        if (query.isBlank()) {
            _searchResults.value = emptyList()
            return
        }
        viewModelScope.launch {
            try {
                // simple search by prefix in email or displayName is hard in firestore without extra tools.
                // We will just fetch all users and filter locally to make it simple (as this is a prototype).
                val snap = db.collection("users").get().await()
                val users = snap.documents.mapNotNull { it.toObject(UserProfile::class.java) }
                _searchResults.value = users.filter { 
                    it.email.contains(query, ignoreCase = true) || it.displayName.contains(query, ignoreCase = true)
                }.filter { it.uid != auth.currentUser?.uid }
            } catch (e: Exception) {}
        }
    }

    fun loadFollowing() {
        val uid = auth.currentUser?.uid ?: return
        viewModelScope.launch {
            try {
                val snap = db.collection("users").document(uid).collection("following").get().await()
                val followingIds = snap.documents.map { it.id }
                _following.value = followingIds
                
                if (followingIds.isNotEmpty()) {
                    val profiles = mutableListOf<UserProfile>()
                    for (id in followingIds) {
                        val doc = db.collection("users").document(id).get().await()
                        doc.toObject(UserProfile::class.java)?.let { profiles.add(it) }
                    }
                    _followingProfiles.value = profiles
                } else {
                    _followingProfiles.value = emptyList()
                }
            } catch (e: Exception) {}
        }
    }

    fun toggleFollow(targetUid: String) {
        val uid = auth.currentUser?.uid ?: return
        viewModelScope.launch {
            try {
                val ref = db.collection("users").document(uid).collection("following").document(targetUid)
                if (_following.value.contains(targetUid)) {
                    ref.delete().await()
                    _following.value = _following.value - targetUid
                } else {
                    ref.set(mapOf("timestamp" to System.currentTimeMillis())).await()
                    _following.value = _following.value + targetUid
                }
            } catch (e: Exception) {}
        }
    }

    fun loadFeed() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val snap = db.collection("activity")
                    .orderBy("timestamp", Query.Direction.DESCENDING)
                    .limit(50)
                    .get().await()

                val activities = snap.documents.mapNotNull {
                    it.toObject(SocialActivity::class.java)?.copy(id = it.id)
                }
                _feed.value = activities
            } catch (e: Exception) { e.printStackTrace() }
            finally {
                _isLoading.value = false
            }
        }
    }

    fun publishActivity(actionType: String, showId: Int, showName: String, details: String, targetUserId: String = "") {
        val user = auth.currentUser ?: return
        viewModelScope.launch {
            try {
                val act = SocialActivity(
                    userId = user.uid,
                    userName = user.displayName ?: user.email?.substringBefore("@") ?: "User",
                    actionType = actionType,
                    showId = showId,
                    showName = showName,
                    details = details,
                    targetUserId = targetUserId
                )
                db.collection("activity").add(act).await()
                loadFeed()
            } catch (e: Exception) {}
        }
    }

    fun suggestShow(showId: Int, showName: String, targetUserId: String, targetUserName: String) {
        publishActivity("SUGGESTED", showId, showName, "Suggested this to $targetUserName", targetUserId)
    }

    fun toggleLike(activityId: String) {
        val user = auth.currentUser ?: return
        viewModelScope.launch {
            try {
                val docRef = db.collection("activity").document(activityId)
                val doc = docRef.get().await()
                val activity = doc.toObject(SocialActivity::class.java) ?: return@launch
                
                val newLikes = if (activity.likes.contains(user.uid)) {
                    activity.likes - user.uid
                } else {
                    activity.likes + user.uid
                }
                docRef.update("likes", newLikes).await()
                loadFeed()
            } catch (e: Exception) { e.printStackTrace() }
        }
    }

    fun addComment(activityId: String, text: String) {
        val user = auth.currentUser ?: return
        viewModelScope.launch {
            try {
                val docRef = db.collection("activity").document(activityId)
                val doc = docRef.get().await()
                val activity = doc.toObject(SocialActivity::class.java) ?: return@launch
                
                val comment = SocialComment(
                    userId = user.uid,
                    userName = user.displayName ?: user.email?.substringBefore("@") ?: "User",
                    text = text
                )
                val newComments = activity.comments + comment
                docRef.update("comments", newComments).await()
                loadFeed()
            } catch (e: Exception) { e.printStackTrace() }
        }
    }
}
