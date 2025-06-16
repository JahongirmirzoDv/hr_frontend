// composeApp/src/wasmJsMain/kotlin/uz/mobiledv/hr_frontend/data/remote/Employee.kt
package uz.mobiledv.hr_frontend.data.remote

import kotlinx.serialization.Serializable

@Serializable
enum class SalaryType {
    HOURLY,
    FIXED_MONTHLY,
    DAILY_THRESHOLD
}

//@Serializable
//data class Employee(
//    val id: String,
//    val name: String,
//    val position: String,
//    val photoUrl: String?,
//    val salaryType: SalaryType,
//    val salaryRate: Double // Hourly rate or monthly salary amount
//)