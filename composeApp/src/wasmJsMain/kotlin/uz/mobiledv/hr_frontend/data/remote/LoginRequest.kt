package uz.mobiledv.hr_frontend.data.remote// In src/wasmJsMain/kotlin/org/example/project/data/remote/Dtos.kt

import kotlinx.serialization.Serializable

// Data we send TO the backend in the request body
@Serializable
data class LoginRequest(
    val username: String,
    val password: String
)

// Data we expect FROM the backend on successful login
@Serializable
data class LoginResponse(
    val userId: String,
    val token: String // e.g., a JWT for authenticating future requests
)