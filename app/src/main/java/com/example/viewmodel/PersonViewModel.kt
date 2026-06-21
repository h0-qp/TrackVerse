package com.example.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.network.ApiClient
import com.example.network.TmdbPerson
import com.example.network.TmdbPersonCredits
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class PersonViewModel : ViewModel() {
    private val _person = MutableStateFlow<TmdbPerson?>(null)
    val person: StateFlow<TmdbPerson?> = _person.asStateFlow()

    private val _credits = MutableStateFlow<TmdbPersonCredits?>(null)
    val credits: StateFlow<TmdbPersonCredits?> = _credits.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    fun loadPerson(personId: Int) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                _person.value = ApiClient.tmdbService.getPersonDetails(personId)
                _credits.value = ApiClient.tmdbService.getPersonCredits(personId)
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                _isLoading.value = false
            }
        }
    }
}
