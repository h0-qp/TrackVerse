package com.example.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.network.ApiClient
import com.example.network.TmdbShow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import com.example.network.TmdbEpisode

class DetailsViewModel : ViewModel() {
    private val _show = MutableStateFlow<TmdbShow?>(null)
    val show: StateFlow<TmdbShow?> = _show

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    private val _seasonDetails = MutableStateFlow<Map<Int, List<TmdbEpisode>>>(emptyMap())
    val seasonDetails: StateFlow<Map<Int, List<TmdbEpisode>>> = _seasonDetails

    private val _watchProviders = MutableStateFlow<com.example.network.WatchProvidersResponse?>(null)
    val watchProviders: StateFlow<com.example.network.WatchProvidersResponse?> = _watchProviders

    private val _similarContent = MutableStateFlow<List<TmdbShow>>(emptyList())
    val similarContent: StateFlow<List<TmdbShow>> = _similarContent

    fun loadDetails(id: Int, isMovie: Boolean) {
        _isLoading.value = true
        _error.value = null
        viewModelScope.launch {
            try {
                val details = if (isMovie) {
                    ApiClient.tmdbService.getMovieDetails(id)
                } else {
                    ApiClient.tmdbService.getTvDetails(id)
                }
                _show.value = details

                try {
                    val similarResponse = if (isMovie) {
                        ApiClient.tmdbService.getSimilarMovies(id)
                    } else {
                        ApiClient.tmdbService.getSimilarShows(id)
                    }
                    _similarContent.value = similarResponse.results?.take(10) ?: emptyList()
                } catch (e: Exception) {
                    _similarContent.value = emptyList()
                }

                try {
                    val providers = if (isMovie) {
                        ApiClient.tmdbService.getMovieWatchProviders(id)
                    } else {
                        ApiClient.tmdbService.getTvWatchProviders(id)
                    }
                    _watchProviders.value = providers
                } catch (e: Exception) {
                    // silently ignore watch provider errors
                }
            } catch (e: Exception) {
                _error.value = e.localizedMessage
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun loadSeasonDetails(tvId: Int, seasonNumber: Int) {
        if (_seasonDetails.value.containsKey(seasonNumber)) return
        
        viewModelScope.launch {
            try {
                val response = ApiClient.tmdbService.getSeasonDetails(tvId, seasonNumber)
                val current = _seasonDetails.value.toMutableMap()
                current[seasonNumber] = response.episodes ?: emptyList()
                _seasonDetails.value = current
            } catch (e: Exception) {
                // handle error silently for season episodes
                val current = _seasonDetails.value.toMutableMap()
                current[seasonNumber] = emptyList()
                _seasonDetails.value = current
            }
        }
    }
}
