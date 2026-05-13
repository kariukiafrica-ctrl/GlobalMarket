package com.shop.globalmarket.data.repository

import android.net.Uri
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.tasks.await

class ImageStorageRepository {
    private val storage = FirebaseStorage.getInstance()
    private val storageRef = storage.reference

    suspend fun uploadProductImage(uri: Uri, productId: String): String {
        val imageRef = storageRef.child("products/$productId/${uri.lastPathSegment}")
        val uploadTask = imageRef.putFile(uri).await()
        return imageRef.downloadUrl.await().toString()
    }

    suspend fun uploadProfileImage(uri: Uri, userId: String): String {
        val imageRef = storageRef.child("users/$userId/profile.jpg")
        imageRef.putFile(uri).await()
        return imageRef.downloadUrl.await().toString()
    }
}
