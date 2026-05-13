package com.shop.globalmarket.ui.admin

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.shop.globalmarket.data.model.Order
import com.shop.globalmarket.data.model.User
import com.shop.globalmarket.data.repository.OrderRepository
import com.shop.globalmarket.data.repository.ProductRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class AdminDashboardViewModel : ViewModel() {
    private val orderRepository = OrderRepository()
    private val productRepository = ProductRepository()
    
    private val _totalRevenue = MutableStateFlow(0.0)
    val totalRevenue: StateFlow<Double> = _totalRevenue

    private val _totalOrders = MutableStateFlow(0)
    val totalOrders: StateFlow<Int> = _totalOrders

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    init {
        loadStats()
    }

    private fun loadStats() {
        viewModelScope.launch {
            _isLoading.value = true
            // In a real app, you'd have a specific admin stats endpoint/query
            // Here we aggregate from orders for demo
            val orders = orderRepository.getOrdersBySeller("") // Get all orders logic (repo might need update)
            _totalOrders.value = orders.size
            _totalRevenue.value = orders.sumOf { it.totalAmount }
            _isLoading.value = false
        }
    }
}
