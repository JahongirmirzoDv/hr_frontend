package uz.mobiledv.hr_frontend.vm

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import uz.mobiledv.hr_frontend.data.HrRepository
import uz.mobiledv.hr_frontend.data.remote.DashboardSummary

class ReportingDashboardViewModel(
    private val repository: HrRepository
) : ViewModel() {
    var summary = mutableStateOf<DashboardSummary?>(null)
    var isLoading = mutableStateOf(true)
    var errorMessage = mutableStateOf<String?>(null)

    fun refreshSummary(token: String) {
        isLoading.value = true
        errorMessage.value = null
        viewModelScope.launch {
            try {
                summary.value = repository.getDashboardSummary(token)
            } catch (e: Exception) {
                errorMessage.value = "Failed to load summary: ${e.message}"
            } finally {
                isLoading.value = false
            }
        }
    }
}