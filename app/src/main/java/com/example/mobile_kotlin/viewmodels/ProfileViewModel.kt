package com.example.mobile_kotlin.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mobile_kotlin.data.model.User
import com.example.mobile_kotlin.data.remote.FirestoreService
import com.example.mobile_kotlin.data.repository.AuthRepository
import com.example.mobile_kotlin.ui.utils.UiState
import com.google.firebase.firestore.SetOptions
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val authRepo: AuthRepository
) : ViewModel() {
    // Состояние пользователя
    private val _userState = MutableStateFlow<UiState<User>>(UiState.Loading)
    val userState: StateFlow<UiState<User>> = _userState

    init {
        loadUser()
    }

    /** Загружает текущего пользователя и обновляет _userState */
    fun loadUser() {
        viewModelScope.launch {
            authRepo.getCurrentUser()
                .catch { e -> _userState.value = UiState.Error(e.message ?: "Ошибка загрузки профиля") }
                .collect { user ->
                    if (user != null) _userState.value = UiState.Success(user)
                    else _userState.value = UiState.Error("Пользователь не найден")
                }
        }
    }

    /** Обновляет профиль пользователя */
    fun updateProfile(updated: User) {
        viewModelScope.launch {
            try {
                authRepo.updateUser(updated)
                // После успешного обновления перезагружаем данные
                loadUser()
            } catch (e: Exception) {
                _userState.value = UiState.Error("Ошибка обновления: ${e.message}")
            }
        }
    }

    /** Выходит из аккаунта */
    fun logout(onComplete: (() -> Unit)? = null) {
        viewModelScope.launch {
            authRepo.logout()
            onComplete?.invoke()
        }
    }

    /** Удаляет аккаунт пользователя */
    fun deleteAccount(onComplete: (() -> Unit)? = null) {
        viewModelScope.launch {
            authRepo.deleteUser()
            onComplete?.invoke()
        }
    }
}