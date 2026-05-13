package com.shop.globalmarket.data.repository

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.shop.globalmarket.data.model.ChatMessage
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class ChatRepository {
    private val firestore = FirebaseFirestore.getInstance()
    private val chatCollection = firestore.collection("chats")

    suspend fun sendMessage(message: ChatMessage) {
        val docRef = chatCollection.document()
        val messageWithId = message.copy(id = docRef.id)
        docRef.set(messageWithId).await()
    }

    suspend fun updateMessageStatus(messageId: String, status: String) {
        chatCollection.document(messageId).update("status", status).await()
    }

    fun getMessages(senderId: String, receiverId: String): Flow<List<ChatMessage>> = callbackFlow {
        val subscription = chatCollection
            .whereIn("senderId", listOf(senderId, receiverId))
            .whereIn("receiverId", listOf(senderId, receiverId))
            .orderBy("timestamp", Query.Direction.ASCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                if (snapshot != null) {
                    val messages = snapshot.toObjects(ChatMessage::class.java)
                    // Ensure the list is sorted by timestamp correctly because whereIn can sometimes mess with order if not careful
                    trySend(messages.sortedBy { it.timestamp })
                }
            }
        awaitClose { subscription.remove() }
    }
}
