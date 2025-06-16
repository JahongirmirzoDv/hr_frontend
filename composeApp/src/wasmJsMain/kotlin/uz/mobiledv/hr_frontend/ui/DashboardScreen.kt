// In composeApp/src/wasmJsMain/kotlin/uz/mobiledv/hr_frontend/ui/DashboardScreen.kt
package uz.mobiledv.hr_frontend.ui

import HrRepository
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import uz.mobiledv.hr_frontend.data.remote.LoginResponse
import uz.mobiledv.hr_frontend.ui.attendance.AttendanceManagementScreen
import uz.mobiledv.hr_frontend.ui.employee.EmployeeManagementScreen
import uz.mobiledv.hr_frontend.ui.project.ProjectManagementScreen
import uz.mobiledv.hr_frontend.ui.reports.ReportingDashboardScreen
import uz.mobiledv.hr_frontend.ui.user.UserManagementScreen

sealed class Screen(val title: String) {
    object Projects : Screen("Projects")
    object Users : Screen("Users")
    object Employees : Screen("Employees")
    object Attendance : Screen("Attendance")
    // New Screen
    object Reporting : Screen("Reporting")
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    user: LoginResponse,
    repository: HrRepository,
    onLogout: () -> Unit
) {
    // Let's make Reporting the default screen
    var currentScreen by remember { mutableStateOf<Screen>(Screen.Reporting) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("HR Admin - ${currentScreen.title}") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                ),
                actions = {
                    IconButton(onClick = onLogout) {
                        Icon(Icons.Default.ExitToApp, "Logout")
                    }
                }
            )
        },
        bottomBar = {
            NavigationBar {
                NavigationBarItem(
                    selected = currentScreen is Screen.Reporting,
                    onClick = { currentScreen = Screen.Reporting },
                    icon = { Icon(Icons.Default.Analytics, "Reporting") },
                    label = { Text("Reporting") }
                )
                NavigationBarItem(
                    selected = currentScreen is Screen.Projects,
                    onClick = { currentScreen = Screen.Projects },
                    icon = { Icon(Icons.Default.Business, "Projects") },
                    label = { Text("Projects") }
                )
                NavigationBarItem(
                    selected = currentScreen is Screen.Users,
                    onClick = { currentScreen = Screen.Users },
                    icon = { Icon(Icons.Default.People, "Users") },
                    label = { Text("Users") }
                )
                NavigationBarItem(
                    selected = currentScreen is Screen.Employees,
                    onClick = { currentScreen = Screen.Employees },
                    icon = { Icon(Icons.Default.Person, "Employees") },
                    label = { Text("Employees") }
                )
                NavigationBarItem(
                    selected = currentScreen is Screen.Attendance,
                    onClick = { currentScreen = Screen.Attendance },
                    icon = { Icon(Icons.Default.AccessTime, "Attendance") },
                    label = { Text("Attendance") }
                )
            }
        }
    ) { paddingValues ->
        Box(modifier = Modifier.padding(paddingValues)) {
            when (currentScreen) {
                is Screen.Projects -> ProjectManagementScreen(repository, user.token)
                is Screen.Users -> UserManagementScreen(repository, user.token)
                is Screen.Employees -> EmployeeManagementScreen(repository, user.token)
                is Screen.Attendance -> AttendanceManagementScreen(repository, user.token)
                is Screen.Reporting -> ReportingDashboardScreen(repository, user.token)
            }
        }
    }
}