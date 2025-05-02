package com.example.mobile_kotlin.viewmodels

import com.example.mobile_kotlin.data.model.Actor
import com.example.mobile_kotlin.data.repository.ActorsRepository
import com.example.mobile_kotlin.data.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import javax.inject.Inject

@HiltViewModel
class FavoritesViewModel @Inject constructor(
    actorsRepo: ActorsRepository,
    authRepo: AuthRepository
) : BaseActorViewModel(actorsRepo, authRepo) {
    override val sourceData: Flow<List<Actor>> = authRepo.getCurrentUser()
        .flatMapLatest { user ->
            val ids = user?.favoriteActorIds.orEmpty()
            if (ids.isEmpty()) {
                flowOf(emptyList())
            } else {
                actorsRepo.observeActorsByIds(ids)
                    .catch { e ->
                        emit(emptyList())
                    }
            }
        }
}
