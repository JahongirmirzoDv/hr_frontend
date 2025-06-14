// In src/wasmJsMain/kotlin/org/example/project/App.kt

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import kotlinx.coroutines.launch
import uz.mobiledv.hr_frontend.data.remote.LoginResponse
import uz.mobiledv.hr_frontend.ui.LoginScreen

@Composable
fun App() {
    MaterialTheme {
        // Instantiate our services (in a real app, use dependency injection)
        val repository = remember { HrRepository(ApiService()) }
        val coroutineScope = rememberCoroutineScope()

        var activeUser by remember { mutableStateOf<LoginResponse?>(null) }
        var isLoading by remember { mutableStateOf(false) }
        var errorMessage by remember { mutableStateOf<String?>(null) }

        if (activeUser == null) {
            // Pass a lambda to LoginScreen that calls our repository
            LoginScreen(
                // This is a simplified version. The state should be passed down.
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
            // You would also pass isLoading and errorMessage to the LoginScreen
            // to display them, as we designed in Step 1.
        } else {
            // If login is successful, show the dashboard
            // We will create this screen next.
            // DashboardScreen(user = activeUser!!)
            Text("Welcome, ${activeUser?.userId}! You are logged in.")
        }
    }
}