// In composeApp/src/wasmJsMain/kotlin/uz/mobiledv/hr_frontend/ui/LoginScreen.kt

package uz.mobiledv.hr_frontend.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp

/**
 * A simple login screen Composable. It is now stateless.
 *
 * @param isLoading Whether the login process is in progress.
 * @param errorMessage An optional error message to display.
 * @param onLoginClicked A callback function that is invoked when the user clicks the login button.
 * It provides the entered username and password.
 */
@Composable
fun LoginScreen(
    isLoading: Boolean,
    errorMessage: String?,
    onLoginClicked: (username: String, password: String) -> Unit
) {
    // State for input fields is okay to keep here as it's specific to this screen's input boxes.
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier.width(350.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(
                modifier = Modifier.padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "HR Platform Login",
                    style = MaterialTheme.typography.headlineSmall
                )

                OutlinedTextField(
                    value = username,
                    onValueChange = { username = it },
                    label = { Text("Username") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    enabled = !isLoading // Also disable fields when loading
                )

                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = { Text("Password") },
                    modifier = Modifier.fillMaxWidth(),
                    visualTransformation = PasswordVisualTransformation(),
                    singleLine = true,
                    enabled = !isLoading // Also disable fields when loading
                )

                // Show an error message if it exists
                errorMessage?.let {
                    Text(
                        text = it,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall
                    )
                }

                // Login Button now reads the isLoading state passed as a parameter
                Button(
                    onClick = { onLoginClicked(username, password) },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !isLoading // Disable button when loading
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(modifier = Modifier.size(20.dp))
                    } else {
                        Text("Login")
                    }
                }
            }
        }
    }
}