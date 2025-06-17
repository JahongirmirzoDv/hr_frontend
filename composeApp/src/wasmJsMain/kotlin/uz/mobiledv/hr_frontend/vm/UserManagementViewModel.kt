package uz.mobiledv.hr_frontend.vm

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import uz.mobiledv.hr_frontend.data.HrRepository
import uz.mobiledv.hr_frontend.data.remote.UserResponse
import uz.mobiledv.hr_frontend.data.remote.CreateUserRequest
import uz.mobiledv.hr_frontend.data.remote.UpdateUserRequest

class UserManagementViewModel(
    private val repository: HrRepository
) : ViewModel() {
    var allUsers = mutableStateOf<List<UserResponse>>(emptyList())
    var isLoading = mutableStateOf(true)
    var errorMessage = mutableStateOf<String?>(null)

    fun refreshUsers(token: String) {
        isLoading.value = true
        errorMessage.value = null
        viewModelScope.launch {
            try {
                allUsers.value = repository.getUsers(token) ?: emptyList()
            } catch (e: Exception) {
                errorMessage.value = "Failed to load users: ${e.message}"
            } finally {
                isLoading.value = false
            }
        }
    }

    fun createUser(token: String, req: CreateUserRequest, onSuccess: () -> Unit) {
        viewModelScope.launch {
            repository.createUser(token, req)?.let { onSuccess() }
            refreshUsers(token)
        }
    }

    fun updateUser(token: String, userId: String, req: UpdateUserRequest, onSuccess: () -> Unit) {
        viewModelScope.launch {
            repository.updateUser(token, userId, req)?.let { onSuccess() }
            refreshUsers(token)
        }
    }

    fun deleteUser(token: String, userId: String, onSuccess: () -> Unit) {
        viewModelScope.launch {
            if (repository.deleteUser(token, userId)) {
                onSuccess()
                refreshUsers(token)
            }
        }
    }
}