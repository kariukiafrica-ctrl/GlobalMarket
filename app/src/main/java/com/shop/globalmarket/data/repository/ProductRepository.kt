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
                // Return sample data if database is empty for demo purposes
                SampleData.products
            } else {
                products
            }
        } catch (e: Exception) {
            SampleData.products
        }
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
        val productWithId = product.copy(id = key)
        database.child(key).setValue(productWithId).await()
    }
}
