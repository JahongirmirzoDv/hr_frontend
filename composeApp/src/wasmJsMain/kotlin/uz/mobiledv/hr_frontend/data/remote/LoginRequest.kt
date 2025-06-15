package uz.mobiledv.hr_frontend.data.remote// In src/wasmJsMain/kotlin/org/example/project/data/remote/Dtos.kt

import kotlinx.serialization.Serializable

// Data we send TO the backend in the request body
@Serializable
data class LoginRequest(
    val email: String,
    val password: String
)
