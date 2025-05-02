package com.example.mobile_kotlin.data.remote

import android.net.Uri
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.ktx.storage
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

// remote/FirebaseStorageService.kt
class FirebaseStorageService @Inject constructor(
    private val storage: FirebaseStorage
) {

    suspend fun uploadImage(path: String, imageUri: Uri): String {
        val ref = storage.reference.child(path)
        ref.putFile(imageUri).await()
        return ref.downloadUrl.await().toString()
    }

    suspend fun deleteImage(url: String) {
        storage.getReferenceFromUrl(url).delete().await()
    }
}