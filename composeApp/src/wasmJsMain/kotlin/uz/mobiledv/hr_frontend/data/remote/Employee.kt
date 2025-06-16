// In a new file: composeApp/src/wasmJsMain/kotlin/uz/mobiledv/hr_frontend/data/remote/Employee.kt

package uz.mobiledv.hr_frontend.data.remote

import kotlinx.serialization.Serializable

@Serializable
data class Employee(
    val id: String,
    val name: String,
    val photoUrl: String,
    val position: String,
    val salaryRate: Double,
    val salaryType: String,
    val salaryAmount: Double,
    val userId: String,
    val department: String,
    val hireDate: String,
    val createdAt: String,
    val updatedAt: String,
    val isActive: Boolean
)
