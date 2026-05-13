package com.shop.globalmarket.ui.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.shop.globalmarket.data.repository.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class AuthViewModel : ViewModel() {
    private val repository = AuthRepository()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _authState = MutableStateFlow<AuthResult?>(null)
    val authState: StateFlow<AuthResult?> = _authState

    fun signUp(email: String, pass: String) {
        viewModelScope.launch {
            _isLoading.value = true
            val result = repository.signUp(email, pass)
            _authState.value = if (result.isSuccess) AuthResult.Success else AuthResult.Error(result.exceptionOrNull()?.message ?: "Unknown error")
            _isLoading.value = false
        }
    }

    fun signIn(email: String, pass: String) {
        viewModelScope.launch {
            _isLoading.value = true
            val result = repository.signIn(email, pass)
            _authState.value = if (result.isSuccess) AuthResult.Success else AuthResult.Error(result.exceptionOrNull()?.message ?: "Unknown error")
            _isLoading.value = false
        }
    }
    
    fun resetAuthState() {
        _authState.value = null
    }
}

sealed class AuthResult {
    object Success : AuthResult()
    data class Error(val message: String) : AuthResult()
}
