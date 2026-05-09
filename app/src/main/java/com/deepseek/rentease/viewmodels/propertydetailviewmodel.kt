package com.deepseek.rentease.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.deepseek.rentease.data.AuthRepository
import com.deepseek.rentease.data.BookingRepository
import com.deepseek.rentease.data.FavoritesRepository
import com.deepseek.rentease.data.PropertyRepository
import com.deepseek.rentease.models.Booking
import com.deepseek.rentease.models.Property
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class PropertyDetailViewModel : ViewModel() {
    private val propertyRepository = PropertyRepository()
    private val favoritesRepository = FavoritesRepository()
    private val bookingRepository = BookingRepository()
    private val authRepository = AuthRepository()

    private val _property = MutableStateFlow<Property?>(null)
    val property: StateFlow<Property?> = _property

    private val _isFavorite = MutableStateFlow(false)
    val isFavorite: StateFlow<Boolean> = _isFavorite

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _currentUserId = MutableStateFlow<String?>(null)
    val currentUserId: StateFlow<String?> = _currentUserId

    private val _bookingState = MutableStateFlow<BookingState>(BookingState.Idle)
    val bookingState: StateFlow<BookingState> = _bookingState

    private val _deleteState = MutableStateFlow<DeleteState>(DeleteState.Idle)
    val deleteState: StateFlow<DeleteState> = _deleteState

    fun loadProperty(propertyId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _property.value = propertyRepository.getPropertyById(propertyId)
            _isFavorite.value = favoritesRepository.isFavorite(propertyId)
            _currentUserId.value = authRepository.currentUser?.uid
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

    fun bookViewing(property: Property, message: String) {
        viewModelScope.launch {
            _bookingState.value = BookingState.Loading
            val user = authRepository.getUserData()
            if (user == null) {
                _bookingState.value = BookingState.Error("You must be logged in to book a viewing")
                return@launch
            }

            if (property.ownerId.isBlank()) {
                android.util.Log.e("PropertyDetailVM", "Property ownerId is blank for property ${property.id}")
                _bookingState.value = BookingState.Error("This property has no owner information. Cannot book.")
                return@launch
            }

            val booking = Booking(
                propertyId = property.id,
                propertyTitle = property.title,
                tenantId = user.uid,
                tenantName = user.name,
                tenantPhone = user.phone,
                landlordId = property.ownerId,
                landlordName = property.ownerName,
                landlordPhone = property.ownerPhone,
                message = message,
                status = "pending"
            )

            android.util.Log.d("PropertyDetailVM", "Creating booking: $booking")
            val result = bookingRepository.createBooking(booking)
            if (result.isSuccess) {
                _bookingState.value = BookingState.Success
            } else {
                _bookingState.value = BookingState.Error(result.exceptionOrNull()?.message ?: "Booking failed")
            }
        }
    }

    fun deleteProperty(propertyId: String) {
        viewModelScope.launch {
            _deleteState.value = DeleteState.Loading
            val result = propertyRepository.deleteProperty(propertyId)
            if (result.isSuccess) {
                _deleteState.value = DeleteState.Success
            } else {
                _deleteState.value = DeleteState.Error(result.exceptionOrNull()?.message ?: "Delete failed")
            }
        }
    }

    fun resetBookingState() {
        _bookingState.value = BookingState.Idle
    }

    sealed class BookingState {
        object Idle : BookingState()
        object Loading : BookingState()
        object Success : BookingState()
        data class Error(val message: String) : BookingState()
    }

    sealed class DeleteState {
        object Idle : DeleteState()
        object Loading : DeleteState()
        object Success : DeleteState()
        data class Error(val message: String) : DeleteState()
    }
}
