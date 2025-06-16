// In a new file: composeApp/src/wasmJsMain/kotlin/uz/mobiledv/hr_frontend/data/remote/Employee.kt

package uz.mobiledv.hr_frontend.data.remote

import kotlinx.serialization.Serializable

@Serializable
data class Employee(
    val id: String,
    val name: String,
    val position: String,
    val salaryType: String,
    val salaryAmount: Double,
)