package com.example.mobile_kotlin.data.model

import com.google.firebase.Timestamp

data class Review(
    val id: String = "",
    val userId: String = "",          // ID пользователя, оставившего отзыв
    val actorId: String = "",         // ID актёра
    val text: String = "",
    val rating: Float = 0f,          // Оценка от 0 до 10
    val timestamp: Timestamp = Timestamp.now()
) {
    constructor() : this("", "", "", "", 0f, Timestamp.now())
}

