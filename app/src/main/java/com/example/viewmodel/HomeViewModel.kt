package com.example.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.BuildConfig
import com.example.network.ApiClient
import com.example.network.TmdbShow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class HomeViewModel : ViewModel() {

    private val _trendingShows = MutableStateFlow<List<TmdbShow>>(emptyList())
    val trendingShows: StateFlow<List<TmdbShow>> = _trendingShows

    private val _topRatedShows = MutableStateFlow<List<TmdbShow>>(emptyList())
    val topRatedShows: StateFlow<List<TmdbShow>> = _topRatedShows

    private val _topRatedMovies = MutableStateFlow<List<TmdbShow>>(emptyList())
    val topRatedMovies: StateFlow<List<TmdbShow>> = _topRatedMovies

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    private val apiKey = BuildConfig.TMDB_API_KEY

    init {
        fetchHomeData()
    }

    fun fetchHomeData() {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            try {
                if (apiKey.isEmpty() || apiKey == "YOUR_TMDB_API_KEY") {
                    _error.value = "TMDB API Key is missing. Please add it in project secrets."
                    _isLoading.value = false
                    return@launch
                }

                val trendingResponse = ApiClient.tmdbService.getTrendingShows()
                _trendingShows.value = trendingResponse.results

                val topRatedResponse = ApiClient.tmdbService.getTopRatedShows()
                _topRatedShows.value = topRatedResponse.results

                val topRatedMoviesResponse = ApiClient.tmdbService.getTopRatedMovies()
                _topRatedMovies.value = topRatedMoviesResponse.results

            } catch (e: Exception) {
                _error.value = e.localizedMessage ?: "An error occurred while fetching data"
            } finally {
                _isLoading.value = false
            }
        }
    }
}
