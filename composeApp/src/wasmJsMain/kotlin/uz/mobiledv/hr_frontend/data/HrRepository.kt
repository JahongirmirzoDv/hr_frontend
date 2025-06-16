import uz.mobiledv.hr_frontend.data.remote.AttendanceRecord
import uz.mobiledv.hr_frontend.data.remote.CreateAttendanceRequest
import uz.mobiledv.hr_frontend.data.remote.CreateEmployeeRequest
import uz.mobiledv.hr_frontend.data.remote.CreateUserRequest
import uz.mobiledv.hr_frontend.data.remote.DashboardSummary
import uz.mobiledv.hr_frontend.data.remote.Employee
import uz.mobiledv.hr_frontend.data.remote.LoginRequest
import uz.mobiledv.hr_frontend.data.remote.LoginResponse
import uz.mobiledv.hr_frontend.data.remote.Project
import uz.mobiledv.hr_frontend.data.remote.UpdateAttendanceRequest
import uz.mobiledv.hr_frontend.data.remote.UpdateEmployeeRequest
import uz.mobiledv.hr_frontend.data.remote.UpdateUserRequest
import uz.mobiledv.hr_frontend.data.remote.UserResponse

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
            println("Repository: Login successful for user ${response.user.id}")
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

    // --- Projects (NEW) ---
    suspend fun getProjects(token: String): List<Project>? {
        return try {
            apiService.getProjects(token)
        } catch (e: Exception) {
            println("Repository: Failed to fetch projects: ${e.message}")
            null
        }
    }

    suspend fun createProject(token: String, project: Project): Project? {
        return try {
            apiService.createProject(token, project)
        } catch (e: Exception) {
            println("Repository: Failed to create project: ${e.message}")
            null
        }
    }

    // --- Users (NEW) ---
    suspend fun getUsers(token: String): List<UserResponse>? {
        return try {
            apiService.getUsers(token)
        } catch (e: Exception) {
            println("Repository: Failed to fetch users: ${e.message}")
            null
        }
    }

    // --- Attendance (NEW) ---
    suspend fun getAttendanceForProject(token: String, projectId: String): List<AttendanceRecord>? {
        return try {
            apiService.getAttendanceForProject(token, projectId)
        } catch (e: Exception) {
            println("Repository: Failed to fetch attendance for project $projectId: ${e.message}")
            null
        }
    }

    suspend fun updateProject(token: String, projectId: String, project: Project): Project? {
        return try {
            apiService.updateProject(token, projectId, project)
        } catch (e: Exception) {
            println("Repository: Failed to update project: ${e.message}")
            null
        }
    }

    suspend fun deleteProject(token: String, projectId: String): Boolean {
        return try {
            apiService.deleteProject(token, projectId)
            true
        } catch (e: Exception) {
            println("Repository: Failed to delete project: ${e.message}")
            false
        }
    }

    // Add this function inside your HrRepository class
    suspend fun createUser(token: String, userRequest: CreateUserRequest): UserResponse? {
        return try {
            apiService.createUser(token, userRequest)
        } catch (e: Exception) {
            println("Repository: Failed to create user: ${e.message}")
            null
        }
    }
    suspend fun updateUser(token: String, userId: String, userRequest: UpdateUserRequest): UserResponse? {
        return try {
            apiService.updateUser(token, userId, userRequest)
        } catch (e: Exception) {
            println("Repository: Failed to update user: ${e.message}")
            null
        }
    }

    suspend fun deleteUser(token: String, userId: String): Boolean {
        return try {
            apiService.deleteUser(token, userId)
            true
        } catch (e: Exception) {
            println("Repository: Failed to delete user: ${e.message}")
            false
        }
    }
    suspend fun createEmployee(token: String, request: CreateEmployeeRequest): Employee? {
        return try {
            apiService.createEmployee(token, request)
        } catch (e: Exception) {
            println("Repository: Failed to create employee: ${e.message}")
            null
        }
    }

    suspend fun updateEmployee(token: String, employeeId: String, request: UpdateEmployeeRequest): Employee? {
        return try {
            apiService.updateEmployee(token, employeeId, request)
        } catch (e: Exception) {
            println("Repository: Failed to update employee: ${e.message}")
            null
        }
    }

    suspend fun deleteEmployee(token: String, employeeId: String): Boolean {
        return try {
            apiService.deleteEmployee(token, employeeId)
            true
        } catch (e: Exception) {
            println("Repository: Failed to delete employee: ${e.message}")
            false
        }
    }

    suspend fun getAttendance(token: String, projectId: String?, startDate: String?, endDate: String?): List<AttendanceRecord>? {
        return try {
            apiService.getAttendance(token, projectId, startDate, endDate)
        } catch (e: Exception) {
            println("Repository: Failed to fetch attendance: ${e.message}")
            null
        }
    }

    suspend fun createAttendance(token: String, request: CreateAttendanceRequest): AttendanceRecord? {
        return try {
            apiService.createAttendance(token, request)
        } catch (e: Exception) {
            println("Repository: Failed to create attendance record: ${e.message}")
            null
        }
    }

    suspend fun updateAttendance(token: String, recordId: String, request: UpdateAttendanceRequest): AttendanceRecord? {
        return try {
            apiService.updateAttendance(token, recordId, request)
        } catch (e: Exception) {
            println("Repository: Failed to update attendance record: ${e.message}")
            null
        }
    }

    suspend fun deleteAttendance(token: String, recordId: String): Boolean {
        return try {
            apiService.deleteAttendance(token, recordId)
            true
        } catch (e: Exception) {
            println("Repository: Failed to delete attendance record: ${e.message}")
            false
        }
    }

    suspend fun getDashboardSummary(token: String): DashboardSummary? {
        return try {
            apiService.getDashboardSummary(token)
        } catch (e: Exception) {
            println("Repository: Failed to fetch dashboard summary: ${e.message}")
            null
        }
    }

}