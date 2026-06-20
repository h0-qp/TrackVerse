package com.example.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.BuildConfig
import com.example.network.ApiClient
import com.example.network.TmdbShow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class DiscoverViewModel : ViewModel() {
    private val _trendingMovies = MutableStateFlow<List<TmdbShow>>(emptyList())
    val trendingMovies: StateFlow<List<TmdbShow>> = _trendingMovies

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading
    
    private val apiKey = BuildConfig.TMDB_API_KEY

    init {
        loadDiscover()
    }

    private fun loadDiscover() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                if (apiKey.isEmpty() || apiKey == "YOUR_TMDB_API_KEY") return@launch
                
                // You could add "Popular", "Airing Today" etc. We reuse the endpoints we have
                val moviesResponse = ApiClient.tmdbService.getTrendingMovies(apiKey)
                _trendingMovies.value = moviesResponse.results
            } catch (e: Exception) {
                // handle error
            } finally {
                _isLoading.value = false
            }
        }
    }
}
