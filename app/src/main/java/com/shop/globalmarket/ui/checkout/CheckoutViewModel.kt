package com.shop.globalmarket.ui.checkout

import androidx.lifecycle.ViewModel
import com.shop.globalmarket.data.model.CartItem
import com.shop.globalmarket.data.model.Order
import com.shop.globalmarket.data.repository.OrderRepository
import com.google.firebase.auth.FirebaseAuth

class CheckoutViewModel : ViewModel() {
    private val orderRepository = OrderRepository()
    private val auth = FirebaseAuth.getInstance()

    suspend fun placeOrder(
        cartItems: List<CartItem>,
        totalAmount: Double,
        address: String,
        city: String,
        phone: String
    ): Boolean {
        val userId = auth.currentUser?.uid ?: return false
        val order = Order(
            buyerId = userId,
            products = cartItems,
            totalAmount = totalAmount,
            deliveryAddress = address,
            deliveryCity = city,
            phoneNumber = phone
        )
        return try {
            orderRepository.placeOrder(order)
            true
        } catch (e: Exception) {
            false
        }
    }
}
