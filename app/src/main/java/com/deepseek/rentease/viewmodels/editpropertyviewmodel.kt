package com.deepseek.rentease.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.deepseek.rentease.data.PropertyRepository
import com.deepseek.rentease.models.Property
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class EditPropertyViewModel : ViewModel() {
    private val propertyRepository = PropertyRepository()

    private val _property = MutableStateFlow<Property?>(null)
    val property: StateFlow<Property?> = _property

    private val _updateState = MutableStateFlow<UpdateState>(UpdateState.Idle)
    val updateState: StateFlow<UpdateState> = _updateState

    fun loadProperty(propertyId: String) {
        viewModelScope.launch {
            _property.value = propertyRepository.getPropertyById(propertyId)
        }
    }

    fun updateProperty(
        title: String,
        description: String,
        price: Double,
        location: String,
        bedrooms: Int,
        bathrooms: Int,
        propertyType: String
    ) {
        val currentProperty = _property.value ?: return

        viewModelScope.launch {
            _updateState.value = UpdateState.Loading
            val updatedProperty = currentProperty.copy(
                title = title,
                description = description,
                price = price,
                location = location,
                bedrooms = bedrooms,
                bathrooms = bathrooms,
                propertyType = propertyType
            )
            val result = propertyRepository.updateProperty(updatedProperty)
            _updateState.value = if (result.isSuccess) {
                UpdateState.Success
            } else {
                UpdateState.Error(result.exceptionOrNull()?.message ?: "Update failed")
            }
        }
    }

    sealed class UpdateState {
        object Idle : UpdateState()
        object Loading : UpdateState()
        object Success : UpdateState()
        data class Error(val message: String) : UpdateState()
    }
}
