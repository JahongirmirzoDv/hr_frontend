// In composeApp/src/wasmJsMain/kotlin/uz/mobiledv/hr_frontend/App.kt

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import kotlinx.coroutines.launch
import uz.mobiledv.hr_frontend.data.remote.LoginResponse
import uz.mobiledv.hr_frontend.ui.DashboardScreen // <-- Import DashboardScreen
import uz.mobiledv.hr_frontend.ui.LoginScreen

@Composable
fun App() {
    MaterialTheme {
        val repository = remember { HrRepository(ApiService()) }
        val coroutineScope = rememberCoroutineScope()

        var activeUser by remember { mutableStateOf<LoginResponse?>(null) }
        var isLoading by remember { mutableStateOf(false) }
        var errorMessage by remember { mutableStateOf<String?>(null) }

        if (activeUser == null) {
            LoginScreen(
                isLoading = isLoading,
                errorMessage = errorMessage,
                onLoginClicked = { username, password ->
                    isLoading = true
                    errorMessage = null
                    coroutineScope.launch {
                        val user = repository.login(username, password)
                        if (user != null) {
                            activeUser = user
                        } else {
                            errorMessage = "Invalid credentials. Please try again."
                        }
                        isLoading = false
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
                    activeUser = null
                }
            )
        }
    }
}