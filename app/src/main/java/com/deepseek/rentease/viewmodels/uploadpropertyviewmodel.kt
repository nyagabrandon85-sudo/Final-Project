package com.deepseek.rentease.viewmodels

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.deepseek.rentease.data.AuthRepository
import com.deepseek.rentease.data.PropertyRepository
import com.deepseek.rentease.models.Property
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class UploadPropertyViewModel : ViewModel() {
    private val propertyRepository = PropertyRepository()
    private val authRepository = AuthRepository()

    private val _uploadState = MutableStateFlow<UploadState>(UploadState.Idle)
    val uploadState: StateFlow<UploadState> = _uploadState

    fun uploadProperty(
        title: String,
        description: String,
        price: Double,
        location: String,
        bedrooms: Int,
        bathrooms: Int,
        propertyType: String,
        imageUris: List<Uri>
    ) {
        if (title.isBlank() || description.isBlank() || location.isBlank() || imageUris.isEmpty()) {
            _uploadState.value = UploadState.Error("Please fill all required fields and select images")
            return
        }

        viewModelScope.launch {
            _uploadState.value = UploadState.Loading

            val user = authRepository.getUserData()
            if (user == null) {
                _uploadState.value = UploadState.Error("User not authenticated")
                return@launch
            }

            val property = Property(
                title = title,
                description = description,
                price = price,
                location = location,
                propertyType = propertyType,
                bedrooms = bedrooms,
                bathrooms = bathrooms,
                ownerId = user.uid,
                ownerName = user.name,
                ownerPhone = user.phone
            )

            val result = propertyRepository.addProperty(property, imageUris)
            _uploadState.value = if (result.isSuccess) {
                UploadState.Success
            } else {
                UploadState.Error(result.exceptionOrNull()?.message ?: "Upload failed")
            }
        }
    }

    sealed class UploadState {
        object Idle : UploadState()
        object Loading : UploadState()
        object Success : UploadState()
        data class Error(val message: String) : UploadState()
    }
}
