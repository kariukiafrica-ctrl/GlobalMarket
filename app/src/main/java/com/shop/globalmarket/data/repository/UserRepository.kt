package com.shop.globalmarket.data.repository

import com.google.firebase.firestore.FirebaseFirestore
import com.shop.globalmarket.data.model.User
import com.shop.globalmarket.data.model.UserType
import kotlinx.coroutines.tasks.await

class UserRepository {
    private val firestore = FirebaseFirestore.getInstance()
    private val usersCollection = firestore.collection("users")

    suspend fun createUser(user: User) {
        usersCollection.document(user.uid).set(user).await()
    }

    suspend fun getUser(uid: String): User? {
        return try {
            val snapshot = usersCollection.document(uid).get().await()
            snapshot.toObject(User::class.java)
        } catch (e: Exception) {
            null
        }
    }

    suspend fun updateProfile(uid: String, name: String, phoneNumber: String) {
        usersCollection.document(uid).update(
            mapOf(
                "name" to name,
                "phoneNumber" to phoneNumber
            )
        ).await()
    }

    suspend fun getAllUsers(): List<User> {
        return try {
            val snapshot = usersCollection.get().await()
            snapshot.toObjects(User::class.java)
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun updateSellerApprovalStatus(uid: String, isApproved: Boolean) {
        usersCollection.document(uid).update("isApprovedSeller", isApproved).await()
    }

    suspend fun getUnapprovedSellers(): List<User> {
        return try {
            val snapshot = usersCollection
                .whereEqualTo("userType", UserType.SELLER.name)
                .whereEqualTo("isApprovedSeller", false)
                .get()
                .await()
            snapshot.toObjects(User::class.java)
        } catch (e: Exception) {
            emptyList()
        }
    }
}
