// composeApp/src/wasmJsMain/kotlin/uz/mobiledv/hr_frontend/data/remote/Project.kt
package uz.mobiledv.hr_frontend.data.remote

import kotlinx.serialization.Serializable

@Serializable
data class Project(
    val id: String,
    val name: String,
    val description: String,
    val location: String,
    val startDate: String,
    val endDate: String,
    val managerId: String,
    val employeeIds: List<String>,
    val budget: Double,
    val status: String,
    val createdAt: String,
    val updatedAt: String
)