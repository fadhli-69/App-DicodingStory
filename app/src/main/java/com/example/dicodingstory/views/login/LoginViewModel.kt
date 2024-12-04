package com.example.dicodingstory.views.login

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.dicodingstory.data.remote.ErrorResponse
import com.example.dicodingstory.data.repository.MyRepository
import com.google.gson.Gson
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import retrofit2.HttpException
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val repository: MyRepository
) : ViewModel() {

    private val _loginResult = MutableLiveData<Result<String>>()
    val loginResult: LiveData<Result<String>> = _loginResult

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    fun login(email: String, password: String) {
        _isLoading.value = true
        viewModelScope.launch {
            try {
                val response = repository.login(email, password)
                if (!response.error) {
                    _loginResult.value = Result.success(response.message)
                } else {
                    _loginResult.value = Result.failure(Exception(response.message))
                }
            } catch (e: HttpException) {
                val jsonInString = e.response()?.errorBody()?.string()
                val errorBody = jsonInString?.let { Gson().fromJson(it, ErrorResponse::class.java) }
                _loginResult.value =
                    Result.failure(Exception(errorBody?.message ?: "Unknown error"))
            } catch (e: Exception) {
                _loginResult.value = Result.failure(e)
            } finally {
                _isLoading.value = false
            }
        }
    }
}
