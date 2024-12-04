package com.example.dicodingstory.data.repository

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.example.dicodingstory.data.remote.AddStoryResponse
import com.example.dicodingstory.data.remote.ApiService
import com.example.dicodingstory.data.remote.GetAllStoriesResponse
import com.example.dicodingstory.data.remote.LoginResponse
import com.example.dicodingstory.data.remote.RegisterResponse
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import okhttp3.MultipartBody
import okhttp3.RequestBody
import javax.inject.Inject

class MyRepository @Inject constructor(
    private val apiService: ApiService,
    private val dataStore: DataStore<Preferences>
) {
    companion object {
        private val TOKEN_KEY = stringPreferencesKey("auth_token")
        private val USER_ID_KEY = stringPreferencesKey("user_id")
    }

    suspend fun register(name: String, email: String, password: String): RegisterResponse {
        return apiService.register(name, email, password)
    }

    suspend fun login(email: String, password: String): LoginResponse {
        val response = apiService.login(email, password)

        if (!response.error) {
            saveUserData(
                response.loginResult.token,
                response.loginResult.userId
            )
        }

        return response
    }

    private suspend fun saveUserData(token: String, userId: String) {
        dataStore.edit { preferences ->
            preferences[TOKEN_KEY] = token
            preferences[USER_ID_KEY] = userId
        }
    }

    fun getToken(): Flow<String?> {
        return dataStore.data.map { preferences ->
            preferences[TOKEN_KEY]
        }
    }

    suspend fun clearUserData() {
        dataStore.edit { preferences ->
            preferences.clear()
        }
    }

    suspend fun getStories(): GetAllStoriesResponse {
        return apiService.getStories()
    }

    suspend fun uploadStory(
        description: RequestBody,
        imageFile: MultipartBody.Part
    ): AddStoryResponse {
        return apiService.uploadStory(description, imageFile)
    }
}