package com.shop.globalmarket.ui.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.shop.globalmarket.data.model.User
import com.shop.globalmarket.data.model.UserType
import com.shop.globalmarket.data.repository.AuthRepository
import com.shop.globalmarket.data.repository.UserRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class AuthViewModel : ViewModel() {
    private val authRepository = AuthRepository()
    private val userRepository = UserRepository()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _authState = MutableStateFlow<AuthResult?>(null)
    val authState: StateFlow<AuthResult?> = _authState

    fun signUp(email: String, pass: String) {
        viewModelScope.launch {
            _isLoading.value = true
            val result = authRepository.signUp(email, pass)
            if (result.isSuccess) {
                val firebaseUser = result.getOrNull()
                if (firebaseUser != null) {
                    val newUser = User(
                        uid = firebaseUser.uid,
                        email = email,
                        userType = UserType.BUYER // Default type
                    )
                    userRepository.createUser(newUser)
                }
                _authState.value = AuthResult.Success
            } else {
                _authState.value = AuthResult.Error(result.exceptionOrNull()?.message ?: "Unknown error")
            }
            _isLoading.value = false
        }
    }

    fun signIn(email: String, pass: String) {
        viewModelScope.launch {
            _isLoading.value = true
            val result = authRepository.signIn(email, pass)
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
