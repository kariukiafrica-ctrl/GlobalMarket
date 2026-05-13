package com.shop.globalmarket.ui.wishlist

import androidx.lifecycle.ViewModel
import com.shop.globalmarket.data.model.Product
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class WishlistViewModel : ViewModel() {
    private val _wishlistItems = MutableStateFlow<List<Product>>(emptyList())
    val wishlistItems: StateFlow<List<Product>> = _wishlistItems.asStateFlow()

    fun toggleWishlist(product: Product) {
        val current = _wishlistItems.value.toMutableList()
        if (current.any { it.id == product.id }) {
            current.removeAll { it.id == product.id }
        } else {
            current.add(product)
        }
        _wishlistItems.value = current
    }

    fun isWishlisted(productId: String): Boolean {
        return _wishlistItems.value.any { it.id == productId }
    }
}
