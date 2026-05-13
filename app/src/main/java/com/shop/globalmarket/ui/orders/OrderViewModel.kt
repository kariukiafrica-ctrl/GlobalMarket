package com.shop.globalmarket.ui.orders

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.shop.globalmarket.data.model.Order
import com.shop.globalmarket.data.repository.OrderRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class OrderViewModel : ViewModel() {
    private val repository = OrderRepository()
    private val auth = FirebaseAuth.getInstance()

    private val _orders = MutableStateFlow<List<Order>>(emptyList())
    val orders: StateFlow<List<Order>> = _orders

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    init {
        fetchOrders()
    }

    fun fetchOrders() {
        val userId = auth.currentUser?.uid ?: return
        viewModelScope.launch {
            _isLoading.value = true
            _orders.value = repository.getOrdersByBuyer(userId).sortedByDescending { it.timestamp }
            _isLoading.value = false
        }
    }

    fun cancelOrder(orderId: String) {
        viewModelScope.launch {
            repository.cancelOrder(orderId)
            fetchOrders() // Refresh list
        }
    }
}
