// composeApp/src/wasmJsMain/kotlin/uz/mobiledv/hr_frontend/data/remote/Project.kt
package uz.mobiledv.hr_frontend.data.remote

import kotlinx.serialization.Serializable

@Serializable
data class Project(
    val id: String,
    val name: String,
    val location: String,
    val description: String,
    val startDate: String, // Use String for simplicity, consider kotlinx-datetime for real projects
    val endDate: String,
    val managerId: String,
    val employeeIds: List<String>
)