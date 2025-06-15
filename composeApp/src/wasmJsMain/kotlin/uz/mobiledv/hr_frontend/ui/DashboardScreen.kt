// In composeApp/src/wasmJsMain/kotlin/uz/mobiledv/hr_frontend/ui/DashboardScreen.kt

package uz.mobiledv.hr_frontend.ui

import HrRepository
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import uz.mobiledv.hr_frontend.data.remote.Employee
import uz.mobiledv.hr_frontend.data.remote.LoginResponse

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    user: LoginResponse,
    repository: HrRepository, // Pass the repository to the dashboard
    onLogout: () -> Unit
) {
    // State for the employee list, loading, and error
    var employees by remember { mutableStateOf<List<Employee>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    val coroutineScope = rememberCoroutineScope()

    // LaunchedEffect will run once when the screen is first displayed.
    // It's the perfect place to make our initial data-fetching call.
    LaunchedEffect(Unit) {
        coroutineScope.launch {
            val result = repository.getEmployees(user.token)
            if (result != null) {
                employees = result
            } else {
                errorMessage = "Failed to load employee data."
            }
            isLoading = false
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Dashboard") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                ),
                actions = {
                    IconButton(onClick = onLogout) {
                        Icon(Icons.Default.ExitToApp, "Logout")
                    }
                }
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier.fillMaxSize().padding(paddingValues),
            contentAlignment = Alignment.Center
        ) {
            when {
                isLoading -> {
                    // Show a loading spinner
                    CircularProgressIndicator()
                }
                errorMessage != null -> {
                    // Show an error message
                    Text(errorMessage!!, color = MaterialTheme.colorScheme.error)
                }
                else -> {
                    // Show the list of employees
                    EmployeeList(employees)
                }
            }
        }
    }
}

@Composable
fun EmployeeList(employees: List<Employee>) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        item {
            Text(
                "Employee Roster",
                style = MaterialTheme.typography.headlineSmall,
                modifier = Modifier.padding(bottom = 8.dp)
            )
        }
        items(employees) { employee ->
            EmployeeListItem(employee)
        }
    }
}

@Composable
fun EmployeeListItem(employee: Employee) {
    Card(elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(employee.name, style = MaterialTheme.typography.bodyLarge)
                Text(employee.position, style = MaterialTheme.typography.bodySmall)
            }
        }
    }
}