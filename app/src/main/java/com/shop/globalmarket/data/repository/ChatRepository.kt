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
        chatCollection.add(message).await()
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
                    trySend(messages)
                }
            }
        awaitClose { subscription.remove() }
    }
}
