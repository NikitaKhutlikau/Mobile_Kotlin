package com.example.mobile_kotlin.viewmodels

import androidx.lifecycle.viewModelScope
import com.example.mobile_kotlin.data.model.Actor
import com.example.mobile_kotlin.data.repository.ActorsRepository
import com.example.mobile_kotlin.data.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.scan
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class FavoritesViewModel @Inject constructor(
    actorsRepo: ActorsRepository,
    authRepo: AuthRepository
) : BaseActorViewModel(actorsRepo, authRepo) {
    @OptIn(ExperimentalCoroutinesApi::class)
    override val sourceData: StateFlow<List<Actor>> =
        authRepo.getCurrentUser()
            .filterNotNull()
            .flatMapLatest { user ->
                val ids = user?.favoriteActorIds.orEmpty()
                if (ids.isEmpty()) {
                    flowOf(emptyList())
                } else {
                    observeActorsByIdsAccumulating(ids)
                }
            }

            .stateIn(
                viewModelScope,
                SharingStarted.Eagerly,
                emptyList()
            )
    init{
        setupActorsStream()
    }

    fun observeActorsByIdsAccumulating(ids: List<String>): Flow<List<Actor>> {
        return actorsRepo.observeActorsByIds(ids)
            .scan(emptyList<Actor>()) { accumulated, newBatch ->
                // Добавляем новые элементы, избегая дубликатов по id
                val merged = (accumulated + newBatch)
                    .distinctBy { it.id }
                merged
            }
    }
}

