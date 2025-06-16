// In composeApp/src/wasmJsMain/kotlin/uz/mobiledv/hr_frontend/data/remote/CreateUserRequest.kt
package uz.mobiledv.hr_frontend.data.remote

import kotlinx.serialization.Serializable

@Serializable
data class CreateUserRequest(
    val fullName: String,
    val email: String,
    val role: String,
    val password: String
)