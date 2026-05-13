package com.shop.globalmarket.data.model

import com.google.firebase.Timestamp

enum class MessageType {
    TEXT, OFFER
}

data class ChatMessage(
    val id: String = "",
    val senderId: String = "",
    val receiverId: String = "",
    val message: String = "",
    val timestamp: Timestamp = Timestamp.now(),
    val type: MessageType = MessageType.TEXT,
    val offerAmount: Double? = null,
    val productId: String? = null,
    val productName: String? = null,
    val status: String? = "PENDING" // PENDING, ACCEPTED, REJECTED for offers
)
