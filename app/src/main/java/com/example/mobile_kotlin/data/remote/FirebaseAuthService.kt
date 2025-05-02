package com.example.mobile_kotlin.data.remote

import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class FirebaseAuthService @Inject constructor(
    private val firebaseAuth: FirebaseAuth
) {

    val currentUser get() = firebaseAuth.currentUser

    // Регистрация
    suspend fun register(email: String, password: String): AuthResult {
        return firebaseAuth.createUserWithEmailAndPassword(email, password).await()
    }

    // Вход
    suspend fun login(email: String, password: String): AuthResult {
        return firebaseAuth.signInWithEmailAndPassword(email, password).await()
    }

    // Выход
    fun logout() {
        firebaseAuth.signOut()
    }

    // Удаление пользователя
    suspend fun deleteUser() {
        firebaseAuth.currentUser?.delete()?.await()
    }
}