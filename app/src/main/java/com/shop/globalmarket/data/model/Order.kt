package com.shop.globalmarket.data.model

import java.util.Date

data class Order(
    val id: String = "",
    val buyerId: String = "",
    val products: List<CartItem> = emptyList(),
    val totalAmount: Double = 0.0,
    val status: OrderStatus = OrderStatus.PENDING,
    val timestamp: Long = System.currentTimeMillis(),
    val paymentMethod: String = "Mpesa"
)

enum class OrderStatus {
    PENDING, PROCESSING, SHIPPED, DELIVERED, CANCELLED
}

data class CartItem(
    val productId: String = "",
    val productName: String = "",
    val productPrice: Double = 0.0,
    val quantity: Int = 1,
    val imageUrl: String = ""
)
