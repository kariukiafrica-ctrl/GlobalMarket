package com.shop.globalmarket.ui.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.shop.globalmarket.data.model.User
import com.shop.globalmarket.data.repository.UserRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class ProfileViewModel : ViewModel() {
    private val userRepository = UserRepository()
    private val auth = FirebaseAuth.getInstance()

    private val _userData = MutableStateFlow<User?>(null)
    val userData: StateFlow<User?> = _userData

    init {
        fetchUserData()
    }

    private fun fetchUserData() {
        val uid = auth.currentUser?.uid ?: return
        viewModelScope.launch {
            _userData.value = userRepository.getUser(uid)
        }
    }

    fun updateProfile(name: String, phoneNumber: String) {
        val uid = auth.currentUser?.uid ?: return
        viewModelScope.launch {
            userRepository.updateProfile(uid, name, phoneNumber)
            fetchUserData() // Refresh
        }
    }
}
