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

    private val _unreadCount = MutableStateFlow(0)
    val unreadCount: StateFlow<Int> = _unreadCount

    fun loadBookings() {
        viewModelScope.launch {
            _isLoading.value = true
            
            // 1. Fetch data in parallel for speed
            val roleDeferred = viewModelScope.launch {
                val role = authRepository.getUserRole()?.lowercase()?.trim()
                android.util.Log.d("BookingsViewModel", "Detected role: $role")
                _userRole.value = role
            }
            
            _myBookings.value = bookingRepository.getMyBookings()
            
            // 2. Fetch received bookings. 
            // We fetch them anyway and let the UI decide whether to show the tab.
            // This is safer in case the role check is delayed.
            val received = bookingRepository.getReceivedBookings()
            _receivedBookings.value = received
            
            // Update unread count
            _unreadCount.value = received.count { !it.viewedByLandlord }

            _isLoading.value = false
        }
    }

    fun markAllReceivedAsViewed() {
        viewModelScope.launch {
            bookingRepository.markReceivedBookingsAsViewed()
            _unreadCount.value = 0
            // Optional: update the list locally to show they are read if UI depends on it
            _receivedBookings.value = _receivedBookings.value.map { it.copy(viewedByLandlord = true) }
        }
    }

    fun loadUserRole() {
        // Already handled in loadBookings for efficiency
    }

    fun updateStatus(bookingId: String, status: String) {
        viewModelScope.launch {
            bookingRepository.updateBookingStatus(bookingId, status)
            loadBookings()
        }
    }
}


