package com.example.mobile_kotlin.data.remote

import android.util.Log
import com.example.mobile_kotlin.data.model.Actor
import com.example.mobile_kotlin.data.model.User
import com.example.mobile_kotlin.data.model.Award
import com.example.mobile_kotlin.data.model.Country
import com.example.mobile_kotlin.data.model.Favorite
import com.example.mobile_kotlin.data.model.Genre
import com.example.mobile_kotlin.data.model.Review
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreSettings
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.SetOptions
import com.google.firebase.firestore.snapshots
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.buffer
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flatMapMerge
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class FirestoreService @Inject constructor(
    val firestore: FirebaseFirestore
) {
    /*init {
        firestore.firestoreSettings = FirebaseFirestoreSettings.Builder()
            .setPersistenceEnabled(true)
            .build()
    }*/

    // Для актеров
    fun getActors(): Flow<List<Actor>> = callbackFlow {
        val listener = firestore.collection("actors")
            .addSnapshotListener { snapshot, _ ->
                val actors = snapshot?.toObjects(Actor::class.java) ?: emptyList()
                trySend(actors)
            }
        awaitClose { listener.remove() }
    }

    suspend fun getActorById(actorId: String): Actor? {
        return firestore.collection("actors")
            .document(actorId)
            .get()
            .await()
            .toObject(Actor::class.java)
    }

    // Фильтры
    suspend fun getGenres(): List<Genre> =
        firestore.collection("genres").get().await().toObjects(Genre::class.java)

    suspend fun getCountries(): List<Country> =
        firestore.collection("countries").get().await().toObjects(Country::class.java)

    suspend fun getAwards(): List<Award> =
        firestore.collection("awards").get().await().toObjects(Award::class.java)

    fun observeGenres(): Flow<List<Genre>> =
        firestore.collection("genres").snapshots().map { it.toObjects(Genre::class.java) }

    fun observeCountries(): Flow<List<Country>> =
        firestore.collection("countries").snapshots().map { it.toObjects(Country::class.java) }

    fun observeAwards(): Flow<List<Award>> =
        firestore.collection("awards").snapshots().map { it.toObjects(Award::class.java) }

    fun observeActorsByIds(ids: List<String>): Flow<List<Actor>> = callbackFlow {
        val listeners = mutableListOf<ListenerRegistration>()

        ids.chunked(10).forEach { chunk ->
            if (chunk.size > 10) {
                throw IllegalArgumentException("Chunk size exceeds Firestore's whereIn limit of 10")
            }
            val listener = firestore.collection("actors")
                .whereIn("id", chunk)
                .addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        Log.e("Firestore", "Error fetching actors", error)
                        trySend(emptyList())
                        return@addSnapshotListener
                    }
                    val actors = try {
                        snapshot?.toObjects(Actor::class.java) ?: emptyList()
                    } catch (e: Exception) {
                        Log.e("Firestore", "Conversion error", e)
                        emptyList()
                    }
                    trySend(actors)
                }
            listeners.add(listener)
        }

        awaitClose {
            listeners.forEach { it.remove() }
        }
    }.buffer(Channel.UNLIMITED)

    // Для избранного
    suspend fun toggleFavorite(actorId: String, userId: String) {
        val favoritesDocRef = firestore.collection("favorites").document("$userId-$actorId")
        val userDocRef = firestore.collection("users").document(userId)

        firestore.runTransaction { transaction ->
            val favoritesSnapshot = transaction.get(favoritesDocRef)

            if (favoritesSnapshot.exists()) {
                // Удаляем из favorites и удаляем actorId из списка пользователя
                transaction.delete(favoritesDocRef)
                transaction.update(userDocRef, "favoriteActorIds", FieldValue.arrayRemove(actorId))
            } else {
                // Добавляем в favorites и добавляем actorId в список пользователя
                transaction.set(favoritesDocRef, Favorite(userId = userId, actorId = actorId))
                transaction.update(userDocRef, "favoriteActorIds", FieldValue.arrayUnion(actorId))
            }
        }.await()
    }


    fun observeFavorites(userId: String): Flow<List<String>> = callbackFlow {
        val listener = firestore.collection("favorites")
            .whereEqualTo("userId", userId)
            .addSnapshotListener { snapshot, _ ->
                val favorites = snapshot?.toObjects(Favorite::class.java)?.map { it.actorId } ?: emptyList()
                trySend(favorites)
            }
        awaitClose { listener.remove() }
    }

    // Для пользователей
    suspend fun updateUser(user: User) {
        firestore.collection("users")
            .document(user.id)
            .set(user, SetOptions.merge())
            .await()
    }

    suspend fun <T : Any> addDocument(collection: String, data: T) {
        firestore.collection(collection).add(data).await()
    }


    suspend fun deleteDocument(collection: String, docId: String) {
        firestore.collection(collection).document(docId).delete().await()
    }

    fun observeUser(userId: String): Flow<User?> = callbackFlow {
        val listener = firestore.collection("users")
            .document(userId)
            .addSnapshotListener { snapshot, _ ->
                trySend(snapshot?.toObject(User::class.java))
            }
        awaitClose { listener.remove() }
    }

    suspend fun getReviews(actorId: String): List<Review> {
        return firestore.collection("reviews")
            .whereEqualTo("actorId", actorId)
            .get()
            .await()
            .toObjects(Review::class.java)
    }

    suspend fun updateDocument(collection: String, docId: String, data: Map<String, Any>) {
        firestore.collection(collection)
            .document(docId)
            .update(data)
            .await()
    }

    fun observeReviews(actorId: String): Flow<List<Review>> = callbackFlow {
        val listener = firestore.collection("reviews")
            .whereEqualTo("actorId", actorId)
            .addSnapshotListener { snapshot, _ ->
                trySend(snapshot?.toObjects(Review::class.java) ?: emptyList())
            }
        awaitClose { listener.remove() }
    }
}