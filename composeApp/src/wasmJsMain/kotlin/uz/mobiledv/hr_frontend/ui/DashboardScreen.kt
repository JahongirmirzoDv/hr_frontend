// composeApp/src/wasmJsMain/kotlin/uz/mobiledv/hr_frontend/ui/DashboardScreen.kt

package uz.mobiledv.hr_frontend.ui

import HrRepository
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.Refresh
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
    repository: HrRepository,
    onLogout: () -> Unit
) {
    // State for the employee list, loading, and error
    var employees by remember { mutableStateOf<List<Employee>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    val coroutineScope = rememberCoroutineScope()

    // Function to load employees
    fun loadEmployees() {
        isLoading = true
        errorMessage = null
        coroutineScope.launch {
            try {
                println("Dashboard: Loading employees")
                val result = repository.getEmployees(user.token)
                if (result != null) {
                    employees = result
                    println("Dashboard: Loaded ${result.size} employees")
                } else {
                    errorMessage = "Failed to load employee data."
                    println("Dashboard: Failed to load employees")
                }
            } catch (e: Exception) {
                errorMessage = "Error loading employees: ${e.message}"
                println("Dashboard: Exception loading employees: ${e.message}")
            } finally {
                isLoading = false
            }
        }
    }

    // LaunchedEffect will run once when the screen is first displayed.
    LaunchedEffect(Unit) {
        loadEmployees()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("HR Dashboard - Welcome ${user.user.id}") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                ),
                actions = {
                    IconButton(
                        onClick = { loadEmployees() },
                        enabled = !isLoading
                    ) {
                        Icon(Icons.Default.Refresh, "Refresh")
                    }
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
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        CircularProgressIndicator()
                        Text("Loading employees...")
                    }
                }
                errorMessage != null -> {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Text(
                            errorMessage!!,
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodyLarge
                        )
                        Button(onClick = { loadEmployees() }) {
                            Text("Retry")
                        }
                    }
                }
                employees.isEmpty() -> {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Text("No employees found")
                        Button(onClick = { loadEmployees() }) {
                            Text("Refresh")
                        }
                    }
                }
                else -> {
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
                "Employee Roster (${employees.size} employees)",
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
    Card(
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    employee.name,
                    style = MaterialTheme.typography.bodyLarge
                )
                Text(
                    employee.position,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    "ID: ${employee.id}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}