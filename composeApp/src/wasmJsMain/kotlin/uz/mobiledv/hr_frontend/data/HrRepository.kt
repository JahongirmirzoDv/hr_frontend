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
            println("Repository: Attempting login for $username")
            val request = LoginRequest(username, password)
            val response = apiService.login(request)
            println("Repository: Login successful for user ${response.userId}")
            response
        } catch (e: Exception) {
            // Log the exception, handle different error types, etc.
            println("Repository: Login failed: ${e.message}")
            e.printStackTrace()
            null
        }
    }

    suspend fun getEmployees(token: String): List<Employee>? {
        return try {
            println("Repository: Fetching employees")
            val employees = apiService.getEmployees(token)
            println("Repository: Fetched ${employees.size} employees")
            employees
        } catch (e: Exception) {
            println("Repository: Failed to fetch employees: ${e.message}")
            e.printStackTrace()
            null
        }
    }
}