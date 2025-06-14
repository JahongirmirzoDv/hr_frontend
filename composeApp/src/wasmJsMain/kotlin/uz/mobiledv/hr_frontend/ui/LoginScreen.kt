package uz.mobiledv.hr_frontend.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp

/**
 * A simple login screen Composable.
 *
 * @param onLoginClicked A callback function that is invoked when the user clicks the login button.
 * It provides the entered username and password.
 */
@Composable
fun LoginScreen(onLoginClicked: (username: String, password: String) -> Unit) {
    // 1. State holders for the input fields
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) } // To show a loading indicator
    var errorMessage by remember { mutableStateOf<String?>(null) } // To show an error

    // This would be triggered by the result of the onLoginClicked call
    // For now, we'll just build the UI.

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

                // 2. Username Input Field
                OutlinedTextField(
                    value = username,
                    onValueChange = { username = it },
                    label = { Text("Username") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                // 3. Password Input Field
                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = { Text("Password") },
                    modifier = Modifier.fillMaxWidth(),
                    visualTransformation = PasswordVisualTransformation(),
                    singleLine = true
                )

                // 4. Show an error message if it exists
                errorMessage?.let {
                    Text(
                        text = it,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall
                    )
                }

                // 5. Login Button
                Button(
                    onClick = {

                        // When clicked, we trigger the callback function
                        onLoginClicked(username, password)
                    },
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