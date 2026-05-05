package com.deepseek.rentease.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.deepseek.rentease.data.AuthRepository
import com.deepseek.rentease.data.PropertyRepository
import com.deepseek.rentease.models.Property
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class HomeViewModel : ViewModel() {
    private val propertyRepository = PropertyRepository()
    private val authRepository = AuthRepository()

    private val _properties = MutableStateFlow<List<Property>>(emptyList())
    val properties: StateFlow<List<Property>> = _properties

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _userRole = MutableStateFlow<String?>(null)
    val userRole: StateFlow<String?> = _userRole

    fun loadProperties() {
        viewModelScope.launch {
            _isLoading.value = true
            _properties.value = propertyRepository.getProperties()
            _isLoading.value = false
        }
    }

    fun loadUserRole() {
        viewModelScope.launch {
            _userRole.value = authRepository.getUserRole()
        }
    }
}
