// In composeApp/src/wasmJsMain/kotlin/uz/mobiledv/hr_frontend/data/remote/DashboardSummary.kt
package uz.mobiledv.hr_frontend.data.remote

import kotlinx.serialization.Serializable

@Serializable
data class ProjectAttendanceSummary(
    val projectName: String,
    val totalPresent: Int,
    val totalAbsent: Int
)

@Serializable
data class DashboardSummary(
    val checkedInToday: Int,
    val absentToday: Int,
    val monthlySalaryExpense: Double,
    val attendancePerProject: List<ProjectAttendanceSummary>
)