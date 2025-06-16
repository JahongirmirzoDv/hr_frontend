// composeApp/src/wasmJsMain/kotlin/uz/mobiledv/hr_frontend/App.kt

package uz.mobiledv.hr_frontend

import ApiService
import HrRepository
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import kotlinx.coroutines.launch
import uz.mobiledv.hr_frontend.data.remote.LoginResponse
import uz.mobiledv.hr_frontend.data.storage.AuthStorage
import uz.mobiledv.hr_frontend.ui.DashboardScreen
import uz.mobiledv.hr_frontend.ui.LoginScreen

@Composable
fun App() {
    MaterialTheme {
        val repository = remember { HrRepository(ApiService(), AuthStorage()) }
        val coroutineScope = rememberCoroutineScope()

        var activeUser by remember { mutableStateOf(repository.getInitialUser()) }
        var isLoading by remember { mutableStateOf(false) }
        var errorMessage by remember { mutableStateOf<String?>(null) }

        if (activeUser == null) {
            LoginScreen(
                isLoading = isLoading,
                errorMessage = errorMessage,
                onLoginClicked = { username, password ->
                    // Clear previous error
                    errorMessage = null
                    isLoading = true

                    coroutineScope.launch {
                        try {
                            println("App: Starting login process")
                            val user = repository.login(username, password)
                            if (user != null) {
                                println("App: Login successful")
                                activeUser = user
                                errorMessage = null
                            } else {
                                println("App: Login failed - invalid credentials")
                                errorMessage = "Invalid credentials. Please try again."
                            }
                        } catch (e: Exception) {
                            println("App: Login exception: ${e.message}")
                            errorMessage = "Login failed: ${e.message ?: "Unknown error"}"
                        } finally {
                            isLoading = false
                        }
                    }
                }
            )
        } else {
            // If login is successful, show the DashboardScreen
            DashboardScreen(
                user = activeUser!!,
                repository = repository,
                onLogout = {
                    // To log out, simply clear the active user
                    println("App: Logging out user")
                    repository.logout()
                    errorMessage = null
                    activeUser = null
                }
            )
        }
    }
}