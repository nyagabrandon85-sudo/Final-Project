package com.deepseek.rentease.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.deepseek.rentease.data.AuthRepository
import com.deepseek.rentease.data.BookingRepository
import com.deepseek.rentease.models.Booking
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class BookingsViewModel : ViewModel() {
    private val bookingRepository = BookingRepository()
    private val authRepository = AuthRepository()

    private val _myBookings = MutableStateFlow<List<Booking>>(emptyList())
    val myBookings: StateFlow<List<Booking>> = _myBookings

    private val _receivedBookings = MutableStateFlow<List<Booking>>(emptyList())
    val receivedBookings: StateFlow<List<Booking>> = _receivedBookings

    private val _userRole = MutableStateFlow<String?>(null)
    val userRole: StateFlow<String?> = _userRole

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    fun loadBookings() {
        viewModelScope.launch {
            _isLoading.value = true
            _myBookings.value = bookingRepository.getMyBookings()
            _receivedBookings.value = bookingRepository.getReceivedBookings()
            _isLoading.value = false
        }
    }

    fun loadUserRole() {
        viewModelScope.launch {
            _userRole.value = authRepository.getUserRole()
        }
    }

    fun updateStatus(bookingId: String, status: String) {
        viewModelScope.launch {
            bookingRepository.updateBookingStatus(bookingId, status)
            loadBookings()
        }
    }
}


