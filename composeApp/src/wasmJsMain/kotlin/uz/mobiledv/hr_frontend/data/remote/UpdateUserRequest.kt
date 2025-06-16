// In composeApp/src/wasmJsMain/kotlin/uz/mobiledv/hr_frontend/data/remote/UpdateUserRequest.kt
package uz.mobiledv.hr_frontend.data.remote

import kotlinx.serialization.Serializable

@Serializable
data class UpdateUserRequest(
    val fullName: String,
    val email: String,
    val role: String
)