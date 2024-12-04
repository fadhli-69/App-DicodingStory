package com.example.dicodingstory.data.remote

data class RegisterResponse(
    val error: Boolean,
    val message: String
)

data class LoginResponse(
    val error: Boolean,
    val message: String,
    val loginResult: LoginResult
)

data class LoginResult(
    val userId: String,
    val name: String,
    val token: String
)

data class ErrorResponse(
    val error: Boolean? = null,
    val message: String? = null
)
