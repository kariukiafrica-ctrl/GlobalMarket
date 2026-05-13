package com.shop.globalmarket.ui.seller

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.shop.globalmarket.data.model.ChatMessage
import com.shop.globalmarket.data.model.MessageType
import com.shop.globalmarket.data.model.Order
import com.shop.globalmarket.data.model.OrderStatus
import com.shop.globalmarket.data.model.Product
import com.shop.globalmarket.data.repository.OrderRepository
import com.shop.globalmarket.data.repository.ProductRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class SellerDashboardViewModel : ViewModel() {
    private val orderRepository = OrderRepository()
    private val productRepository = ProductRepository()
    private val auth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()

    private val _orders = MutableStateFlow<List<Order>>(emptyList())
    val orders: StateFlow<List<Order>> = _orders

    private val _myProducts = MutableStateFlow<List<Product>>(emptyList())
    val myProducts: StateFlow<List<Product>> = _myProducts
    
    private val _pendingOffers = MutableStateFlow<List<ChatMessage>>(emptyList())
    val pendingOffers: StateFlow<List<ChatMessage>> = _pendingOffers

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    init {
        loadDashboardData()
    }

    fun loadDashboardData() {
        val sellerId = auth.currentUser?.uid ?: return
        viewModelScope.launch {
            _isLoading.value = true
            try {
                // Load seller products
                val allProducts = productRepository.getProducts()
                _myProducts.value = allProducts.filter { it.sellerId == sellerId }
                
                // Load relevant orders
                _orders.value = orderRepository.getOrdersBySeller(sellerId)
                
                // Load pending offers (hybrid feature: direct negotiation)
                val offersSnapshot = firestore.collection("chats")
                    .whereEqualTo("receiverId", sellerId)
                    .whereEqualTo("type", MessageType.OFFER.name)
                    .whereEqualTo("status", "PENDING")
                    .get()
                    .await()
                
                _pendingOffers.value = offersSnapshot.toObjects(ChatMessage::class.java)
            } catch (e: Exception) {
                // Handle error
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun deleteProduct(productId: String) {
        viewModelScope.launch {
            productRepository.deleteProduct(productId)
            loadDashboardData()
        }
    }

    fun updateOrderStatus(orderId: String, status: OrderStatus) {
        viewModelScope.launch {
            orderRepository.updateOrderStatus(orderId, status.name)
            loadDashboardData()
        }
    }
    
    fun updateOfferStatus(messageId: String, status: String) {
        viewModelScope.launch {
            firestore.collection("chats").document(messageId).update("status", status).await()
            loadDashboardData()
        }
    }

    val totalSales: Double
        get() {
            val sellerId = auth.currentUser?.uid ?: return 0.0
            return _orders.value.sumOf { order ->
                order.products
                    .filter { it.sellerId == sellerId }
                    .sumOf { it.productPrice * it.quantity }
            }
        }
}
