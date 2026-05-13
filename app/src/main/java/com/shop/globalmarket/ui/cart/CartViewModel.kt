package com.shop.globalmarket.ui.cart

import androidx.lifecycle.ViewModel
import com.shop.globalmarket.data.model.CartItem
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class CartViewModel : ViewModel() {
    private val _cartItems = MutableStateFlow<List<CartItem>>(emptyList())
    val cartItems: StateFlow<List<CartItem>> = _cartItems.asStateFlow()

    fun addToCart(item: CartItem) {
        val currentItems = _cartItems.value.toMutableList()
        val existingItem = currentItems.find { it.productId == item.productId }
        if (existingItem != null) {
            val index = currentItems.indexOf(existingItem)
            currentItems[index] = existingItem.copy(quantity = existingItem.quantity + item.quantity)
        } else {
            currentItems.add(item)
        }
        _cartItems.value = currentItems
    }

    fun removeFromCart(productId: String) {
        _cartItems.value = _cartItems.value.filter { it.productId != productId }
    }

    fun updateQuantity(productId: String, quantity: Int) {
        if (quantity <= 0) {
            removeFromCart(productId)
            return
        }
        val currentItems = _cartItems.value.toMutableList()
        val index = currentItems.indexOfFirst { it.productId == productId }
        if (index != -1) {
            currentItems[index] = currentItems[index].copy(quantity = quantity)
            _cartItems.value = currentItems
        }
    }

    fun clearCart() {
        _cartItems.value = emptyList()
    }

    val subtotal: Double
        get() = _cartItems.value.sumOf { it.productPrice * it.quantity }

    val formattedSubtotal: String
        get() = "Ksh ${String.format("%,.0f", subtotal)}"
}
