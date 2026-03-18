package com.zixuan_wang.swapnest.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zixuan_wang.swapnest.model.User
import com.zixuan_wang.swapnest.repository.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

sealed interface AuthUiState {
    data object Idle : AuthUiState
    data object Loading : AuthUiState
    data object Success : AuthUiState
    data class Error(val message: String) : AuthUiState
}

class AuthViewModel(private val repository: AuthRepository) : ViewModel() {
    val currentUser: StateFlow<User?> = repository.currentUser
    private val _authUiState = MutableStateFlow<AuthUiState>(AuthUiState.Idle)
    val authUiState: StateFlow<AuthUiState> = _authUiState

    fun login(email: String, password: String) {
        viewModelScope.launch {
            _authUiState.value = AuthUiState.Loading
            runCatching {
                repository.login(email, password)
            }.onSuccess {
                _authUiState.value = AuthUiState.Success
            }.onFailure { t ->
                _authUiState.value = AuthUiState.Error(t.message ?: "登录失败")
            }
        }
    }

    fun register(name: String, email: String, password: String) {
        viewModelScope.launch {
            _authUiState.value = AuthUiState.Loading
            runCatching {
                repository.register(name, email, password)
            }.onSuccess {
                _authUiState.value = AuthUiState.Success
            }.onFailure { t ->
                _authUiState.value = AuthUiState.Error(t.message ?: "注册失败")
            }
        }
    }

    fun logout() {
        viewModelScope.launch {
            repository.logout()
        }
    }

    fun resetAuthUiState() {
        _authUiState.value = AuthUiState.Idle
    }
}
