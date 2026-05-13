package com.shop.globalmarket.ui.seller

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.shop.globalmarket.data.model.Order
import com.shop.globalmarket.data.model.Product
import com.shop.globalmarket.data.repository.OrderRepository
import com.shop.globalmarket.data.repository.ProductRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class SellerDashboardViewModel : ViewModel() {
    private val orderRepository = OrderRepository()
    private val productRepository = ProductRepository()
    private val auth = FirebaseAuth.getInstance()

    private val _orders = MutableStateFlow<List<Order>>(emptyList())
    val orders: StateFlow<List<Order>> = _orders

    private val _myProducts = MutableStateFlow<List<Product>>(emptyList())
    val myProducts: StateFlow<List<Product>> = _myProducts

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    init {
        loadDashboardData()
    }

    private fun loadDashboardData() {
        val sellerId = auth.currentUser?.uid ?: return
        viewModelScope.launch {
            _isLoading.value = true
            // Load seller products
            val allProducts = productRepository.getProducts()
            _myProducts.value = allProducts.filter { it.sellerId == sellerId }
            
            // Load relevant orders
            _orders.value = orderRepository.getOrdersBySeller(sellerId)
            _isLoading.value = false
        }
    }

    val totalSales: Double
        get() = _orders.value.sumOf { it.totalAmount }
}
