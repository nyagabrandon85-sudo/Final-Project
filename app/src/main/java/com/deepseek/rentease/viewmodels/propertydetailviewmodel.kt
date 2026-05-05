package com.deepseek.rentease.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.deepseek.rentease.data.FavoritesRepository
import com.deepseek.rentease.data.PropertyRepository
import com.deepseek.rentease.models.Property
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class PropertyDetailViewModel : ViewModel() {
    private val propertyRepository = PropertyRepository()
    private val favoritesRepository = FavoritesRepository()

    private val _property = MutableStateFlow<Property?>(null)
    val property: StateFlow<Property?> = _property

    private val _isFavorite = MutableStateFlow(false)
    val isFavorite: StateFlow<Boolean> = _isFavorite

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    fun loadProperty(propertyId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _property.value = propertyRepository.getPropertyById(propertyId)
            _isFavorite.value = favoritesRepository.isFavorite(propertyId)
            _isLoading.value = false
        }
    }

    fun toggleFavorite(property: Property) {
        viewModelScope.launch {
            if (_isFavorite.value) {
                favoritesRepository.removeFromFavorites(property.id)
                _isFavorite.value = false
            } else {
                favoritesRepository.addToFavorites(property)
                _isFavorite.value = true
            }
        }
    }
}
