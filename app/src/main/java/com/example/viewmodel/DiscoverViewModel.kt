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
    private val _trendingShows = MutableStateFlow<List<TmdbShow>>(emptyList())
    val trendingShows: StateFlow<List<TmdbShow>> = _trendingShows

    private val _popularShows = MutableStateFlow<List<TmdbShow>>(emptyList())
    val popularShows: StateFlow<List<TmdbShow>> = _popularShows

    private val _topRatedShows = MutableStateFlow<List<TmdbShow>>(emptyList())
    val topRatedShows: StateFlow<List<TmdbShow>> = _topRatedShows

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading
    
    init {
        loadDiscover()
    }

    private fun loadDiscover() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val trending = ApiClient.tmdbService.getTrendingShows()
                _trendingShows.value = trending.results

                val popular = ApiClient.tmdbService.getPopularShows()
                _popularShows.value = popular.results

                val topRated = ApiClient.tmdbService.getTopRatedShows()
                _topRatedShows.value = topRated.results
            } catch (e: Exception) {
            } finally {
                _isLoading.value = false
            }
        }
    }
}
