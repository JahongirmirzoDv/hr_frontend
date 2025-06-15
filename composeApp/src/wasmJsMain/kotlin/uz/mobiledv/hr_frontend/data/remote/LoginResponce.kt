package uz.mobiledv.hr_frontend.data.remote

import kotlinx.serialization.Serializable

@Serializable
data class UserResponse(
    val id: String,
    val fullName: String,
    val email: String,
    val role: String
)

@Serializable
data class LoginResponse(
    val token: String,
    val user: UserResponse
)

@Serializable
data class ErrorResponse(
    val error: String? = null
)

@Serializable
data class RegisterResponse(
    val userId: String
)