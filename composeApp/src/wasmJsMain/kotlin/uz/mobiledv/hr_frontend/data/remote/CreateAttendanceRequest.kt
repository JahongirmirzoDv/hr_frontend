// In composeApp/src/wasmJsMain/kotlin/uz/mobiledv/hr_frontend/data/remote/AttendanceRequest.kt
package uz.mobiledv.hr_frontend.data.remote

import kotlinx.serialization.Serializable

@Serializable
data class CreateAttendanceRequest(
    val employeeId: String,
    val projectId: String,
    val checkInTime: String, // Expecting ISO 8601 format, e.g., "2023-10-27T09:00:00"
    val checkOutTime: String? = null
)

@Serializable
data class UpdateAttendanceRequest(
    val checkInTime: String,
    val checkOutTime: String?
)