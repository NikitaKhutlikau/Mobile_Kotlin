package com.example.mobile_kotlin.data.model

data class Favorite(
    val id: String = "",       // ID в Firebase
    val userId: String = "",   // ID пользователя
    val actorId: String = ""   // ID актёра
){
    constructor() : this("", "", "")
}