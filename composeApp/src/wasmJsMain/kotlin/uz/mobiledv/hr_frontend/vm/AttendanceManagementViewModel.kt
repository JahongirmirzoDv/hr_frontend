package uz.mobiledv.hr_frontend.vm

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import uz.mobiledv.hr_frontend.data.HrRepository
import uz.mobiledv.hr_frontend.data.remote.AttendanceRecord
import uz.mobiledv.hr_frontend.data.remote.Project
import uz.mobiledv.hr_frontend.data.remote.CreateAttendanceRequest
import uz.mobiledv.hr_frontend.data.remote.UpdateAttendanceRequest

class AttendanceManagementViewModel(
    private val repository: HrRepository
) : ViewModel() {
    var attendanceRecords = mutableStateOf<List<AttendanceRecord>>(emptyList())
    var projects = mutableStateOf<List<Project>>(emptyList())
    var isLoading = mutableStateOf(false)
    var errorMessage = mutableStateOf<String?>(null)

    fun refreshAttendance(token: String) {
        isLoading.value = true
        errorMessage.value = null
        viewModelScope.launch {
            try {
                projects.value = repository.getProjects(token) ?: emptyList()
                attendanceRecords.value = repository.getAttendance(token, null, null, null) ?: emptyList()
            } catch (e: Exception) {
                errorMessage.value = "Failed to load attendance: ${e.message}"
            } finally {
                isLoading.value = false
            }
        }
    }

    fun createAttendance(token: String, req: CreateAttendanceRequest, onSuccess: () -> Unit) {
        viewModelScope.launch {
            repository.createAttendance(token, req)?.let { onSuccess() }
            refreshAttendance(token)
        }
    }

    fun updateAttendance(token: String, attendanceId: String, req: UpdateAttendanceRequest, onSuccess: () -> Unit) {
        viewModelScope.launch {
            repository.updateAttendance(token, attendanceId, req)?.let { onSuccess() }
            refreshAttendance(token)
        }
    }

    fun deleteAttendance(token: String, attendanceId: String, onSuccess: () -> Unit) {
        viewModelScope.launch {
            if (repository.deleteAttendance(token, attendanceId)) {
                onSuccess()
                refreshAttendance(token)
            }
        }
    }
}