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
        return try {
            val snapshot = database.get().await()
            val allOrders = snapshot.children.mapNotNull { it.getValue(Order::class.java) }
            if (sellerId.isEmpty()) {
                allOrders
            } else {
                allOrders.filter { order -> 
                    order.products.any { it.sellerId == sellerId } 
                }
            }
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun updateOrderStatus(orderId: String, status: String) {
        try {
            database.child(orderId).child("status").setValue(status).await()
        } catch (e: Exception) {
            // Handle error
        }
    }

    suspend fun cancelOrder(orderId: String) {
        updateOrderStatus(orderId, "CANCELLED")
    }
}
