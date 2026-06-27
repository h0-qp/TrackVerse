package com.example.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.network.ApiClient
import com.example.network.TmdbShow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class DiscoverViewModel : ViewModel() {
    private val _discoverResults = MutableStateFlow<List<TmdbShow>>(emptyList())
    val discoverResults: StateFlow<List<TmdbShow>> = _discoverResults

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    var currentType = MutableStateFlow("tv") // "tv" or "movie"
    var selectedGenre = MutableStateFlow<String?>(null)
    var selectedYear = MutableStateFlow<String?>(null)
    var selectedRating = MutableStateFlow<Float?>(null)
    var selectedSortBy = MutableStateFlow("popularity.desc")

    init {
        applyFilters()
    }

    fun applyFilters() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                if (currentType.value == "tv") {
                    val response = ApiClient.tmdbService.discoverTv(
                        withGenres = selectedGenre.value,
                        firstAirDateYear = selectedYear.value,
                        voteAverageGte = selectedRating.value,
                        sortBy = selectedSortBy.value
                    )
                    _discoverResults.value = response.results
                } else {
                    val response = ApiClient.tmdbService.discoverMovie(
                        withGenres = selectedGenre.value,
                        primaryReleaseYear = selectedYear.value,
                        voteAverageGte = selectedRating.value,
                        sortBy = selectedSortBy.value
                    )
                    _discoverResults.value = response.results
                }
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                _isLoading.value = false
            }
        }
    }
}

