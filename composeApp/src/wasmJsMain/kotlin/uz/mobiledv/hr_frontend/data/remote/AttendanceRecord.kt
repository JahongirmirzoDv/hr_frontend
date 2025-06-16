// composeApp/src/wasmJsMain/kotlin/uz/mobiledv/hr_frontend/data/remote/Attendance.kt
package uz.mobiledv.hr_frontend.data.remote

import kotlinx.serialization.Serializable

@Serializable
data class AttendanceRecord(
    val id: String,
    val employeeId: String,
    val projectId: String,
    val checkInTime: String,
    val checkOutTime: String?,
    val selfieUrl: String?
)