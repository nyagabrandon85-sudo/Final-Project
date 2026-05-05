package com.deepseek.rentease.viewmodels


import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.deepseek.rentease.data.PropertyRepository
import com.deepseek.rentease.models.Property
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class SearchViewModel : ViewModel() {
    private val repository = PropertyRepository()

    private val _searchResults = MutableStateFlow<List<Property>>(emptyList())
    val searchResults: StateFlow<List<Property>> = _searchResults

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    fun search(query: String, minPrice: Double?, maxPrice: Double?, type: String?) {
        viewModelScope.launch {
            _isLoading.value = true
            _searchResults.value = repository.searchProperties(query, minPrice, maxPrice, type)
            _isLoading.value = false
        }
    }
}

