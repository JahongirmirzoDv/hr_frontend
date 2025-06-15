// In src/wasmJsMain/kotlin/org/example/project/data/remote/ApiService.kt

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.json.Json
import uz.mobiledv.hr_frontend.data.remote.Employee
import uz.mobiledv.hr_frontend.data.remote.LoginRequest
import uz.mobiledv.hr_frontend.data.remote.LoginResponse

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
    }

    // We assume your backend is running locally for now
    private val baseUrl = "http://0.0.0.0:8080"

    /**
     * Performs a login request to the hr-ktorBackend.
     * @return LoginResponse on success.
     * @throws Exception on failure (e.g., 401 Unauthorized, network error).
     */
    suspend fun login(request: LoginRequest): LoginResponse {
        return client.post("$baseUrl/login") {
            contentType(ContentType.Application.Json)
            setBody(request)
        }.body()
    }

    suspend fun getEmployees(token: String): List<Employee> {
        return client.get("$baseUrl/employees") {
            bearerAuth(token) // Add the JWT as a Bearer token
        }.body()
    }
}