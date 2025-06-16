// In src/wasmJsMain/kotlin/org/example/project/data/remote/ApiService.kt

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.json.Json
import uz.mobiledv.hr_frontend.data.remote.AttendanceRecord
import uz.mobiledv.hr_frontend.data.remote.CreateAttendanceRequest
import uz.mobiledv.hr_frontend.data.remote.CreateEmployeeRequest
import uz.mobiledv.hr_frontend.data.remote.CreateUserRequest
import uz.mobiledv.hr_frontend.data.remote.DashboardSummary
import uz.mobiledv.hr_frontend.data.remote.Employee
import uz.mobiledv.hr_frontend.data.remote.ErrorResponse
import uz.mobiledv.hr_frontend.data.remote.LoginRequest
import uz.mobiledv.hr_frontend.data.remote.LoginResponse
import uz.mobiledv.hr_frontend.data.remote.Project
import uz.mobiledv.hr_frontend.data.remote.UpdateAttendanceRequest
import uz.mobiledv.hr_frontend.data.remote.UpdateEmployeeRequest
import uz.mobiledv.hr_frontend.data.remote.UpdateUserRequest
import uz.mobiledv.hr_frontend.data.remote.UserResponse

class ApiService {

    private val client = HttpClient {
        // Configure JSON serialization
        install(ContentNegotiation) {
            json(Json {
                prettyPrint = true
                isLenient = true
                ignoreUnknownKeys = true // Important for API evolution
            })
        }

        install(HttpTimeout) {
            requestTimeoutMillis = 30000 // 30 seconds
            connectTimeoutMillis = 15000 // 15 seconds
        }
    }

    // We assume your backend is running locally for now
    private val baseUrl = "http://0.0.0.0:8080"

    /**
     * Performs a login request to the hr-ktorBackend.
     * @return LoginResponse on success.
     * @throws Exception on failure (e.g., 401 Unauthorized, network error).
     */
    suspend fun login(request: LoginRequest): LoginResponse {
        try {
            println("Attempting login for user: ${request.email}")
            val response = client.post("$baseUrl/auth/login") {
                contentType(ContentType.Application.Json)
                setBody(request)
                headers {
                    append("Accept", "application/json")
                }
            }
            println("Login response status: ${response.status}")
            return response.body()
        } catch (e: Exception) {
            println("Login error: ${e.message}")
            throw e
        }
    }

    suspend fun getEmployees(token: String): List<Employee> {
        try {
            println("Fetching employees with token: ${token.take(10)}...")
            val response = client.get("$baseUrl/admin/employees") {
                bearerAuth(token) // Add the JWT as a Bearer token
                headers {
                    append("Accept", "application/json")
                }
            }
            println("Employees response status: ${response.status}")
            return response.body()
        } catch (e: Exception) {
            println("Fetch employees error: ${e.message}")
            throw e
        }
    }

    // --- Project Management (NEW) ---
    suspend fun getProjects(token: String): List<Project> {
        return client.get("$baseUrl/admin/projects") {
            bearerAuth(token)
        }.body()
    }

    suspend fun createProject(token: String, project: Project): Project {
        return client.post("$baseUrl/admin/projects") {
            bearerAuth(token)
            contentType(ContentType.Application.Json)
            setBody(project)
        }.body()
    }

    // --- User Management (NEW) ---
    suspend fun getUsers(token: String): List<UserResponse> {
        return client.get("$baseUrl/admin/users") {
            bearerAuth(token)
        }.body()
    }

    // --- Attendance Management (NEW) ---
    suspend fun getAttendanceForProject(token: String, projectId: String): List<AttendanceRecord> {
        return client.get("$baseUrl/admin/attendance") {
            bearerAuth(token)
            parameter("projectId", projectId)
        }.body()
    }

    suspend fun updateProject(token: String, projectId: String, project: Project): Project {
        return client.put("$baseUrl/admin/projects/$projectId") {
            bearerAuth(token)
            contentType(ContentType.Application.Json)
            setBody(project)
        }.body()
    }

    suspend fun deleteProject(token: String, projectId: String) {
        client.delete("$baseUrl/admin/projects/$projectId") {
            bearerAuth(token)
        }
    }

    suspend fun createUser(token: String, userRequest: CreateUserRequest): UserResponse {
        return client.post("$baseUrl/admin/users") {
            bearerAuth(token)
            contentType(ContentType.Application.Json)
            setBody(userRequest)
        }.body()
    }

    suspend fun updateUser(token: String, userId: String, userRequest: UpdateUserRequest): UserResponse {
        return client.put("$baseUrl/admin/users/$userId") {
            bearerAuth(token)
            contentType(ContentType.Application.Json)
            setBody(userRequest)
        }.body()
    }

    suspend fun deleteUser(token: String, userId: String) {
        client.delete("$baseUrl/admin/users/$userId") {
            bearerAuth(token)
        }
    }

    suspend fun createEmployee(token: String, request: CreateEmployeeRequest): Employee {
        return client.post("$baseUrl/admin/employees") {
            bearerAuth(token)
            contentType(ContentType.Application.Json)
            setBody(request)
        }.body()
    }

    suspend fun updateEmployee(token: String, employeeId: String, request: UpdateEmployeeRequest): Employee {
        return client.put("$baseUrl/admin/employees/$employeeId") {
            bearerAuth(token)
            contentType(ContentType.Application.Json)
            setBody(request)
        }.body()
    }

    suspend fun deleteEmployee(token: String, employeeId: String) {
        client.delete("$baseUrl/admin/employees/$employeeId") {
            bearerAuth(token)
        }
    }

    suspend fun getAttendance(token: String, projectId: String?, startDate: String?, endDate: String?): List<AttendanceRecord> {
        return client.get("$baseUrl/admin/attendance") {
            bearerAuth(token)
            // Add parameters only if they are not null or blank
            if (!projectId.isNullOrBlank()) parameter("projectId", projectId)
            if (!startDate.isNullOrBlank()) parameter("startDate", startDate)
            if (!endDate.isNullOrBlank()) parameter("endDate", endDate)
        }.body()
    }


    suspend fun createAttendance(token: String, request: CreateAttendanceRequest): AttendanceRecord {
        return client.post("$baseUrl/admin/attendance") {
            bearerAuth(token)
            contentType(ContentType.Application.Json)
            setBody(request)
        }.body()
    }

    suspend fun updateAttendance(token: String, recordId: String, request: UpdateAttendanceRequest): AttendanceRecord {
        return client.put("$baseUrl/admin/attendance/$recordId") {
            bearerAuth(token)
            contentType(ContentType.Application.Json)
            setBody(request)
        }.body()
    }

    suspend fun deleteAttendance(token: String, recordId: String) {
        client.delete("$baseUrl/admin/attendance/$recordId") {
            bearerAuth(token)
        }
    }

    suspend fun getDashboardSummary(token: String): DashboardSummary {
        return client.get("$baseUrl/admin/reports/summary") {
            bearerAuth(token)
        }.body()
    }

    fun close() {
        client.close()
    }
}