package com.shop.globalmarket.data.model

data class Product(
    val id: String = "",
    val name: String = "",
    val description: String = "",
    val price: Double = 0.0,
    val imageUrl: String = "",
    val category: String = "",
    val sellerId: String = "",
    val rating: Float = 0f,
    val reviewCount: Int = 0,
    val stock: Int = 0,
    val discountPercentage: Double = 0.0,
    val isNegotiable: Boolean = true,
    val isApproved: Boolean = false // Admin moderation flag
) {
    val formattedPrice: String
        get() = "Ksh ${String.format("%,.0f", price)}"
    
    val discountedPrice: Double
        get() = price * (1 - discountPercentage / 100)
    
    val formattedDiscountedPrice: String
        get() = "Ksh ${String.format("%,.0f", discountedPrice)}"
}
