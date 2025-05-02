package com.example.mobile_kotlin.data.repository

import com.example.mobile_kotlin.data.model.Actor
import com.example.mobile_kotlin.data.model.Award
import com.example.mobile_kotlin.data.model.Country
import com.example.mobile_kotlin.data.model.Genre
import com.example.mobile_kotlin.data.remote.FirestoreService
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import javax.inject.Inject

class ActorsRepository @Inject constructor(
    private val firestoreService: FirestoreService,
    private val authRepo: AuthRepository
) {

    fun getActors(): Flow<List<Actor>> = firestoreService.getActors()

    /*fun getFavorites(userId: String): Flow<List<Actor>> {
        return firestoreService.observeFavorites(userId)
    }*/

    fun getGenres(): Flow<List<Genre>> =
        firestoreService.observeGenres()

    fun getCountries(): Flow<List<Country>> =
        firestoreService.observeCountries()

    fun getAwards(): Flow<List<Award>> =
        firestoreService.observeAwards()

    suspend fun getActorDetails(actorId: String): Actor? =
        firestoreService.getActorById(actorId)

    fun observeActorsByIds(ids: List<String>): Flow<List<Actor>> {
        return firestoreService.observeActorsByIds(ids)
    }

    fun getActorsWithFavorites(): Flow<Pair<List<Actor>, Set<String>>> {
        return authRepo.getCurrentUser().flatMapLatest { user ->
            user?.id?.let { userId ->
                firestoreService.getActors().combine(firestoreService.observeFavorites(userId)) { actors, favorites ->
                    actors to favorites.toSet()
                }
            } ?: flowOf(emptyList<Actor>() to emptySet())
        }
    }

    suspend fun toggleFavorite(actorId: String) {
        authRepo.getCurrentUser().first()?.id?.let { userId ->
            firestoreService.toggleFavorite(actorId, userId)
        }
    }

    suspend fun getAvailableFilters(): Triple<List<Genre>, List<Country>, List<Award>> {
        return Triple(
            firestoreService.getGenres(),
            firestoreService.getCountries(),
            firestoreService.getAwards()
        )
    }
}