package com.example.mobile_kotlin.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mobile_kotlin.data.model.Actor
import com.example.mobile_kotlin.data.model.Favorite
import com.example.mobile_kotlin.data.repository.ActorsRepository
import com.example.mobile_kotlin.data.repository.AuthRepository
import com.example.mobile_kotlin.ui.utils.UiState
import com.example.mobile_kotlin.viewmodels.BaseActorViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ActorsViewModel @Inject constructor(
    actorsRepo: ActorsRepository,
    authRepo: AuthRepository
) : BaseActorViewModel(actorsRepo, authRepo) {
    override val sourceData: Flow<List<Actor>> = actorsRepo.getActors()
}
