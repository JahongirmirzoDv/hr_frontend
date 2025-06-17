package uz.mobiledv.hr_frontend.vm

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import uz.mobiledv.hr_frontend.data.HrRepository
import uz.mobiledv.hr_frontend.data.remote.Employee
import uz.mobiledv.hr_frontend.data.remote.CreateEmployeeRequest
import uz.mobiledv.hr_frontend.data.remote.UpdateEmployeeRequest

class EmployeeManagementViewModel(
    private val repository: HrRepository
) : ViewModel() {
    var allEmployees = mutableStateOf<List<Employee>>(emptyList())
    var isLoading = mutableStateOf(true)
    var errorMessage = mutableStateOf<String?>(null)

    fun refreshEmployees(token: String) {
        isLoading.value = true
        errorMessage.value = null
        viewModelScope.launch {
            try {
                allEmployees.value = repository.getEmployees(token) ?: emptyList()
            } catch (e: Exception) {
                errorMessage.value = "Failed to load employees: ${e.message}"
            } finally {
                isLoading.value = false
            }
        }
    }

    fun createEmployee(token: String, req: CreateEmployeeRequest, onSuccess: () -> Unit) {
        viewModelScope.launch {
            repository.createEmployee(token, req)?.let { onSuccess() }
            refreshEmployees(token)
        }
    }

    fun updateEmployee(token: String, empId: String, req: UpdateEmployeeRequest, onSuccess: () -> Unit) {
        viewModelScope.launch {
            repository.updateEmployee(token, empId, req)?.let { onSuccess() }
            refreshEmployees(token)
        }
    }

    fun deleteEmployee(token: String, empId: String, onSuccess: () -> Unit) {
        viewModelScope.launch {
            if (repository.deleteEmployee(token, empId)) {
                onSuccess()
                refreshEmployees(token)
            }
        }
    }
}