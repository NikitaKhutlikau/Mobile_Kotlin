package com.example.mobile_kotlin.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mobile_kotlin.data.model.User
import com.example.mobile_kotlin.data.repository.AuthRepository
import com.example.mobile_kotlin.ui.utils.UiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _registrationState = MutableStateFlow<UiState<Unit>>(UiState.Empty)
    val registrationState: StateFlow<UiState<Unit>> = _registrationState

    private val _loginState = MutableStateFlow<UiState<Unit>>(UiState.Empty)
    val loginState: StateFlow<UiState<Unit>> = _loginState

    private val _deleteState = MutableStateFlow<UiState<Unit>>(UiState.Empty)
    val deleteState: StateFlow<UiState<Unit>> = _deleteState

    private val _isLoggedIn = MutableStateFlow(false)
    val isLoggedIn: StateFlow<Boolean> = _isLoggedIn.asStateFlow()

    private val _currentUser = MutableStateFlow<User?>(null)
    val currentUser: StateFlow<User?> = _currentUser.asStateFlow()

    init {
        viewModelScope.launch {
            authRepository.getCurrentUser().collect { user ->
                _isLoggedIn.value = user != null
                _currentUser.value = user
            }
        }
    }

    fun register(email: String, password: String, user: User) {
        when {
            email.isBlank() -> _registrationState.value = UiState.Error("Email обязателен")
            password.length < 6 -> _registrationState.value = UiState.Error("Пароль слишком короткий")
            user.username.isBlank() -> _registrationState.value = UiState.Error("Введите имя пользователя")
            else -> viewModelScope.launch {
                _registrationState.value = UiState.Loading
                try {
                    authRepository.register(email, password, user)
                    _registrationState.value = UiState.Success(Unit)
                    _isLoggedIn.value = true
                } catch (e: Exception) {
                    _registrationState.value = UiState.Error(e.message ?: "Ошибка регистрации")
                }
            }
        }
    }

    fun login(email: String, password: String) {
        viewModelScope.launch {
            _registrationState.value = UiState.Loading
            try {
                authRepository.login(email, password)
                _registrationState.value = UiState.Success(Unit)
                _isLoggedIn.value = true
            } catch (e: Exception) {
                _registrationState.value = UiState.Error(e.message ?: "Ошибка входа")
            }
        }
    }

    fun logout() {
        viewModelScope.launch {
            authRepository.logout()
            _isLoggedIn.value = false
        }
    }

    fun deleteAccount() {
        viewModelScope.launch {
            try {
                authRepository.deleteUser()
                _isLoggedIn.value = false
            } catch (e: Exception) {
                // Обработка ошибок удаления
            }
        }
    }
}