package com.example.mobile_kotlin.data.repository

import android.content.Context
import com.example.mobile_kotlin.data.model.User
import com.example.mobile_kotlin.data.remote.FirebaseAuthService
import com.example.mobile_kotlin.data.remote.FirestoreService
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class AuthRepository @Inject constructor(
    private val authService: FirebaseAuthService,
    private val firestoreService: FirestoreService
) {

    // Вход
    suspend fun login(email: String, password: String): Result<Unit> = try {
        authService.login(email, password)
        Result.success(Unit)
    } catch (e: Exception) {
        Result.failure(e)
    }

    // Выход
    fun logout() {
        authService.logout()
    }

    suspend fun register(email: String, password: String, userData: User): Result<Unit> = try {
        val authResult = authService.register(email, password)
        val user = userData.copy(id = authResult.user?.uid ?: "")
        firestoreService.addDocument("users", user)
        Result.success(Unit)
    } catch (e: Exception) {
        Result.failure(e)
    }

    suspend fun deleteUser() {
        authService.currentUser?.uid?.let { userId ->
            firestoreService.deleteDocument("users", userId)
            authService.deleteUser()
        }
    }

    fun getCurrentUser(): Flow<User?> = authService.currentUser?.uid?.let { userId ->
        firestoreService.observeUser(userId)
    } ?: emptyFlow()

    suspend fun updateUser(user: User) {
        firestoreService.updateUser(user)
    }

    fun getUserUpdates(userId: String): Flow<User?> {
        return firestoreService.observeUser(userId)
    }

    fun observeUser(userId: String): Flow<User?> {
        return firestoreService.observeUser(userId)
    }
}