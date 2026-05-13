package com.shop.globalmarket.data.model

data class User(
    val uid: String = "",
    val name: String = "",
    val email: String = "",
    val profilePicture: String = "",
    val userType: UserType = UserType.BUYER,
    val phoneNumber: String = "",
    val isApprovedSeller: Boolean = false // Admin approval for sellers
)

enum class UserType {
    BUYER, SELLER, ADMIN
}
