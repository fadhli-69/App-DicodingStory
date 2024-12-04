package com.example.dicodingstory.views.welcome

import androidx.lifecycle.ViewModel
import com.example.dicodingstory.data.repository.MyRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

@HiltViewModel
class WelcomeViewModel @Inject constructor(
    private val repository: MyRepository
) : ViewModel() {
    fun isUserLoggedIn(): Flow<Boolean> {
        return repository.getToken().map { token ->
            !token.isNullOrBlank()
        }
    }
}