package com.example.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.network.ApiClient
import com.example.network.TmdbShow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class DetailsViewModel : ViewModel() {
    private val _show = MutableStateFlow<TmdbShow?>(null)
    val show: StateFlow<TmdbShow?> = _show

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

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
            } catch (e: Exception) {
                _error.value = e.localizedMessage
            } finally {
                _isLoading.value = false
            }
        }
    }
}
