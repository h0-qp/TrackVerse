package com.example.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.BuildConfig
import com.example.network.Candidate
import com.example.network.Content
import com.example.network.GenerateContentRequest
import com.example.network.GenerateContentResponse
import com.example.network.GeminiClient
import com.example.network.Part
import com.example.network.ApiClient
import com.example.network.TmdbShow
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class RecommendationsViewModel : ViewModel() {
    private val _recommendations = MutableStateFlow<List<TmdbShow>>(emptyList())
    val recommendations: StateFlow<List<TmdbShow>> = _recommendations.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    fun fetchRecommendations(watchedShows: List<TmdbShow>, watchlist: List<TmdbShow>) {
        if (_isLoading.value) return
        
        // If we don't have enough data, skip
        if (watchedShows.isEmpty() && watchlist.isEmpty()) return

        _isLoading.value = true
        _error.value = null

        viewModelScope.launch {
            try {
                val apiKey = BuildConfig.GEMINI_API_KEY
                
                val showTitles = (watchedShows + watchlist).mapNotNull { it.title ?: it.name }.distinct().take(15)
                
                if (showTitles.isEmpty()) {
                    _isLoading.value = false
                    return@launch
                }

                val prompt = "Based on these movies/TV shows: ${showTitles.joinToString(", ")}, recommend 5 similar popular movies or TV shows that are available on TMDB. ONLY return a comma-separated list of EXACT titles, nothing else. You are a TV/Movie recommendation system. Always reply with just a comma-separated list of titles."

                val request = GenerateContentRequest(
                    contents = listOf(
                        Content(parts = listOf(Part(text = prompt)), role = "user")
                    )
                )

                val response = withContext(Dispatchers.IO) {
                    GeminiClient.service.generateContent(apiKey, request)
                }

                val responseText = response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text ?: ""
                
                if (responseText.isNotEmpty()) {
                    val titles = responseText.split(",").map { it.trim() }.filter { it.isNotEmpty() }
                    
                    val recommendedShows = mutableListOf<TmdbShow>()
                    
                    withContext(Dispatchers.IO) {
                        for (title in titles) {
                            try {
                                val searchResponse = ApiClient.tmdbService.search(title)
                                val show = searchResponse.results.firstOrNull { 
                                    it.title.equals(title, ignoreCase = true) || it.name.equals(title, ignoreCase = true)
                                }
                                if (show != null) {
                                    recommendedShows.add(show)
                                }
                            } catch (e: Exception) {
                                e.printStackTrace()
                            }
                        }
                    }
                    _recommendations.value = recommendedShows
                }
            } catch (e: retrofit2.HttpException) {
                val errorBody = try { e.response()?.errorBody()?.string() } catch(t: Throwable) { null }
                if (e.code() == 400 && errorBody?.contains("API_KEY_INVALID") == true) {
                    _error.value = "مفتاح API الخاص بـ Gemini غير صالح."
                } else {
                    _error.value = "حدث خطأ في الاتصال: HTTP ${e.code()}"
                }
            } catch (e: Exception) {
                _error.value = e.message
            } finally {
                _isLoading.value = false
            }
        }
    }
}
