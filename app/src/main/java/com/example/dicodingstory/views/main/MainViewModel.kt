package com.example.dicodingstory.views.main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.dicodingstory.data.repository.MyRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val repository: MyRepository
) : ViewModel() {

    private val _logoutState = MutableStateFlow<LogoutState>(LogoutState.Idle)
    val logoutState = _logoutState.asStateFlow()

    fun isUserLoggedIn() = repository.getToken().map { token ->
        !token.isNullOrBlank()
    }

    fun logout() {
        viewModelScope.launch {
            try {
                _logoutState.value = LogoutState.Loading

                repository.clearUserData()

                val token = repository.getToken().first()

                if (token.isNullOrBlank()) {
                    _logoutState.value = LogoutState.Success
                } else {
                    _logoutState.value = LogoutState.Error("Gagal menghapus sesi login")
                }
            } catch (e: Exception) {
                _logoutState.value = LogoutState.Error(e.localizedMessage ?: "Logout gagal")
            }
        }
    }

    sealed class LogoutState {
        data object Idle : LogoutState()
        data object Loading : LogoutState()
        data object Success : LogoutState()
        data class Error(val errorMessage: String) : LogoutState()
    }
}