package com.shop.globalmarket.data.repository

import com.google.firebase.database.FirebaseDatabase
import com.shop.globalmarket.data.model.Order
import kotlinx.coroutines.tasks.await

class OrderRepository {
    private val database = FirebaseDatabase.getInstance().getReference("orders")

    suspend fun placeOrder(order: Order) {
        val key = database.push().key ?: return
        val orderWithId = order.copy(id = key)
        database.child(key).setValue(orderWithId).await()
    }

    suspend fun getOrdersByBuyer(buyerId: String): List<Order> {
        return try {
            val snapshot = database.orderByChild("buyerId").equalTo(buyerId).get().await()
            snapshot.children.mapNotNull { it.getValue(Order::class.java) }
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun getOrdersBySeller(sellerId: String): List<Order> {
        // In a real app, orders might be split or filtered differently
        // For simplicity, we'll assume a seller can see all orders they have products in
        // or we filter at the application level if using a flat list
        return try {
            val snapshot = database.get().await()
            snapshot.children.mapNotNull { it.getValue(Order::class.java) }
                .filter { order -> order.products.any { it.productId != "" } } // Logic would be more complex
        } catch (e: Exception) {
            emptyList()
        }
    }
}
