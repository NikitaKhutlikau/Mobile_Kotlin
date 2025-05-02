package com.example.mobile_kotlin.data.model

import com.google.firebase.Timestamp

data class Actor(
    val id: String = "",                     // Firebase‑ID актёра
    val name: String = "",                   // Имя
    val birthDate: Timestamp = Timestamp.now(), // Дата рождения
    val images: List<String> = emptyList(),  // Ссылки на изображения
    val description: String = "",            // Описание
    val movies: List<String> = emptyList(),  // Список фильмов (если нужно)
    val genreIds: List<String> = emptyList(),// ID жанров для фильтров
    val countryId: String? = null,           // ID страны для фильтра
    val awardIds: List<String> = emptyList(),// ID наград для фильтра
    val rating: Float = 0f                 // Средний рейтинг
) {
    constructor() : this(
        id = "",
        name = "",
        birthDate = Timestamp.now(),
        images = emptyList(),
        description = "",
        movies = emptyList(),
        genreIds = emptyList(),
        countryId = null,
        awardIds = emptyList(),
        rating = 0f
    )
}