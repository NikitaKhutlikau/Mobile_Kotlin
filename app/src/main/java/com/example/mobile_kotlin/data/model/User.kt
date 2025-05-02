package com.example.mobile_kotlin.data.model

import com.google.firebase.Timestamp

data class User(
    val id: String = "",
    val login: String = "",
    val email: String = "",
    val username: String = "",
    val birthDate: Timestamp = Timestamp.now(),
    val phone: String = "",
    val country: String = "",
    val address: String = "",
    val bio: String = "",
    val avatarUrl: String = "",
    val registrationDate: Timestamp = Timestamp.now(),
    val favoriteActorIds: List<String> = emptyList() // Добавлено для избранного
) {
    constructor() : this("", "", "", "", Timestamp(0, 0))
}