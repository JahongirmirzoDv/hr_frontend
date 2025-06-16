// In composeApp/src/wasmJsMain/kotlin/uz/mobiledv/hr_frontend/data/remote/EmployeeRequest.kt
package uz.mobiledv.hr_frontend.data.remote

import kotlinx.serialization.Serializable

@Serializable
data class CreateEmployeeRequest(
    val name: String,
    val position: String,
    val salaryType: SalaryType,
    val salaryRate: Double,
    val userId: String? = null // Optional: to link to an existing user account
)

@Serializable
data class UpdateEmployeeRequest(
    val name: String,
    val position: String,
    val salaryType: SalaryType,
    val salaryRate: Double
)