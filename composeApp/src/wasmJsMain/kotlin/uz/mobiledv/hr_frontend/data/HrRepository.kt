import uz.mobiledv.hr_frontend.data.remote.Employee
import uz.mobiledv.hr_frontend.data.remote.LoginRequest
import uz.mobiledv.hr_frontend.data.remote.LoginResponse

// In src/wasmJsMain/kotlin/org/example/project/data/repository/HrRepository.kt


class HrRepository(private val apiService: ApiService) {

    /**
     * Tries to log in the user.
     * @return LoginResponse on success, null on failure.
     */
    suspend fun login(username: String, password: String): LoginResponse? {
        return try {
            val request = LoginRequest(username, password)
            apiService.login(request)
        } catch (e: Exception) {
            // Log the exception, handle different error types, etc.
            println("Login failed: ${e.message}")
            null
        }
    }

    suspend fun getEmployees(token: String): List<Employee>? {
        return try {
            apiService.getEmployees(token)
        } catch (e: Exception) {
            println("Failed to fetch employees: ${e.message}")
            null
        }
    }
}