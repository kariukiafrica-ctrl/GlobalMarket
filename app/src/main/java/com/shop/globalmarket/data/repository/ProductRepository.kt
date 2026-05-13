package com.shop.globalmarket.data.repository

import com.google.firebase.database.FirebaseDatabase
import com.shop.globalmarket.data.model.Product
import com.shop.globalmarket.data.util.SampleData
import kotlinx.coroutines.tasks.await

class ProductRepository {
    private val database = FirebaseDatabase.getInstance().getReference("products")

    suspend fun getProducts(): List<Product> {
        return try {
            val snapshot = database.get().await()
            val products = snapshot.children.mapNotNull { it.getValue(Product::class.java) }
            if (products.isEmpty()) {
                SampleData.products
            } else {
                products
            }
        } catch (e: Exception) {
            SampleData.products
        }
    }

    suspend fun getApprovedProducts(): List<Product> {
        val products = getProducts()
        return products.filter { it.isApproved }
    }

    suspend fun getUnapprovedProducts(): List<Product> {
        val products = getProducts()
        return products.filter { !it.isApproved }
    }

    suspend fun approveProduct(productId: String) {
        database.child(productId).child("approved").setValue(true).await()
    }

    suspend fun getProductById(id: String): Product? {
        return try {
            val snapshot = database.child(id).get().await()
            snapshot.getValue(Product::class.java) ?: SampleData.products.find { it.id == id }
        } catch (e: Exception) {
            SampleData.products.find { it.id == id }
        }
    }

    suspend fun addProduct(product: Product) {
        val key = database.push().key ?: return
        val productWithId = product.copy(id = key, isApproved = false) // New products need moderation
        database.child(key).setValue(productWithId).await()
    }

    suspend fun updateProduct(product: Product) {
        try {
            database.child(product.id).setValue(product).await()
        } catch (e: Exception) {
            // Handle error
        }
    }

    suspend fun deleteProduct(productId: String) {
        try {
            database.child(productId).removeValue().await()
        } catch (e: Exception) {
            // Handle error
        }
    }
}
