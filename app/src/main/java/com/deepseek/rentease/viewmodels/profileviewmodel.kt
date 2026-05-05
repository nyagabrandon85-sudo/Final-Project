package com.deepseek.rentease.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.deepseek.rentease.data.AuthRepository
import com.deepseek.rentease.models.User
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class ProfileViewModel : ViewModel() {
    private val repository = AuthRepository()

    private val _user = MutableStateFlow<User?>(null)
    val user: StateFlow<User?> = _user

    private val _userRole = MutableStateFlow<String?>(null)
    val userRole: StateFlow<String?> = _userRole

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    fun loadUserData() {
        viewModelScope.launch {
            _isLoading.value = true
            _user.value = repository.getUserData()
            _userRole.value = repository.getUserRole()
            _isLoading.value = false
        }
    }

    fun logout() {
        repository.logout()
    }
}

