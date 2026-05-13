package com.shop.globalmarket.ui.chat

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.shop.globalmarket.data.model.ChatMessage
import com.shop.globalmarket.data.model.MessageType
import com.shop.globalmarket.data.repository.ChatRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class ChatViewModel : ViewModel() {
    private val repository = ChatRepository()
    private val auth = FirebaseAuth.getInstance()

    private val _messages = MutableStateFlow<List<ChatMessage>>(emptyList())
    val messages: StateFlow<List<ChatMessage>> = _messages

    fun loadMessages(receiverId: String) {
        val senderId = auth.currentUser?.uid ?: return
        viewModelScope.launch {
            repository.getMessages(senderId, receiverId).collect {
                _messages.value = it
            }
        }
    }

    fun sendMessage(receiverId: String, text: String) {
        val senderId = auth.currentUser?.uid ?: return
        if (text.isBlank()) return
        
        val message = ChatMessage(
            senderId = senderId,
            receiverId = receiverId,
            message = text,
            type = MessageType.TEXT
        )
        viewModelScope.launch {
            repository.sendMessage(message)
        }
    }

    fun sendOffer(receiverId: String, productId: String, productName: String, amount: Double) {
        val senderId = auth.currentUser?.uid ?: return
        
        val message = ChatMessage(
            senderId = senderId,
            receiverId = receiverId,
            message = "Sent an offer for $productName",
            type = MessageType.OFFER,
            offerAmount = amount,
            productId = productId,
            productName = productName,
            status = "PENDING"
        )
        viewModelScope.launch {
            repository.sendMessage(message)
        }
    }

    fun updateOfferStatus(messageId: String, status: String) {
        viewModelScope.launch {
            repository.updateMessageStatus(messageId, status)
        }
    }
}
