package com.deepseek.rentease.viewmodels


import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope

import com.deepseek.rentease.data.FavoritesRepository
import com.deepseek.rentease.models.Property
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class FavoritesViewModel : ViewModel() {
    private val repository = FavoritesRepository()

    private val _favorites = MutableStateFlow<List<Property>>(emptyList())
    val favorites: StateFlow<List<Property>> = _favorites

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    fun loadFavorites() {
        viewModelScope.launch {
            _isLoading.value = true
            _favorites.value = repository.getFavorites()
            _isLoading.value = false
        }
    }
}
