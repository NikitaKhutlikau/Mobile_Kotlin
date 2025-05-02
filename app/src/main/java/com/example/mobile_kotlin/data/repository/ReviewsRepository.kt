package com.example.mobile_kotlin.data.repository

import com.example.mobile_kotlin.data.model.Review
import com.example.mobile_kotlin.data.remote.FirestoreService
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class ReviewsRepository @Inject constructor(
    private val firestoreService: FirestoreService
) {
    fun observeReviews(actorId: String): Flow<List<Review>> {
        return firestoreService.observeReviews(actorId)
    }

    suspend fun addReview(review: Review) {
        firestoreService.addDocument("reviews", review)
    }

    suspend fun deleteReview(reviewId: String) {
        firestoreService.deleteDocument("reviews", reviewId)
    }

    suspend fun updateReview(reviewId: String, newText: String, newRating: Float) {
        firestoreService.updateDocument(
            "reviews",
            reviewId,
            mapOf(
                "text" to newText,
                "rating" to newRating,
                "timestamp" to Timestamp.now()
            )
        )
    }
}