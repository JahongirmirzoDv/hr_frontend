package uz.mobiledv.hr_frontend.vm

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import uz.mobiledv.hr_frontend.data.HrRepository
import uz.mobiledv.hr_frontend.data.remote.Project

class ProjectManagementViewModel(
    private val repository: HrRepository
) : ViewModel() {
    var allProjects = mutableStateOf<List<Project>>(emptyList())
    var isLoading = mutableStateOf(true)
    var errorMessage = mutableStateOf<String?>(null)

    fun refreshProjects(token: String) {
        isLoading.value = true
        errorMessage.value = null
        viewModelScope.launch {
            try {
                allProjects.value = repository.getProjects(token) ?: emptyList()
            } catch (e: Exception) {
                errorMessage.value = "Failed to load projects: ${e.message}"
            } finally {
                isLoading.value = false
            }
        }
    }

    fun createProject(token: String, project: Project, onSuccess: () -> Unit) {
        viewModelScope.launch {
            repository.createProject(token, project)?.let { onSuccess() }
            refreshProjects(token)
        }
    }

    fun updateProject(token: String, projectId: String, project: Project, onSuccess: () -> Unit) {
        viewModelScope.launch {
            repository.updateProject(token, projectId, project)?.let { onSuccess() }
            refreshProjects(token)
        }
    }

    fun deleteProject(token: String, projectId: String, onSuccess: () -> Unit) {
        viewModelScope.launch {
            if (repository.deleteProject(token, projectId)) {
                onSuccess()
                refreshProjects(token)
            }
        }
    }
}