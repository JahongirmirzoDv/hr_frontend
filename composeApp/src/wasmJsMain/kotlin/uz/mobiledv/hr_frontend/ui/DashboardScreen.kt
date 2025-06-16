// In composeApp/src/wasmJsMain/kotlin/uz/mobiledv/hr_frontend/ui/DashboardScreen.kt
package uz.mobiledv.hr_frontend.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import uz.mobiledv.hr_frontend.data.HrRepository
import uz.mobiledv.hr_frontend.data.remote.LoginResponse
import uz.mobiledv.hr_frontend.ui.attendance.AttendanceManagementScreen
import uz.mobiledv.hr_frontend.ui.employee.EmployeeManagementScreen
import uz.mobiledv.hr_frontend.ui.payroll.PayrollScreen
import uz.mobiledv.hr_frontend.ui.project.ProjectManagementScreen
import uz.mobiledv.hr_frontend.ui.report.ReportScreen
import uz.mobiledv.hr_frontend.ui.reports.ReportingDashboardScreen

// Updated Screen sealed class to include all new screens
sealed class Screen(val title: String) {
    object Reporting : Screen("Dashboard")
    object Employees : Screen("Employees")
    object Projects : Screen("Projects")
    object Attendance : Screen("Attendance")
    object Payroll : Screen("Payroll")
    object Reports : Screen("Reports")
}

@Composable
fun DashboardScreen(
    user: LoginResponse,
    repository: HrRepository,
    onLogout: () -> Unit
) {
    var currentScreen by remember { mutableStateOf<Screen>(Screen.Reporting) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        TopHeader(
            currentScreen = currentScreen,
            onScreenSelect = { currentScreen = it },
            userName = user.user.fullName,
            onLogout = onLogout
        )

        // The content of the selected screen
        Box(modifier = Modifier.weight(1f)) {
            when (currentScreen) {
                is Screen.Reporting -> ReportingDashboardScreen(repository, user.token)
                is Screen.Projects -> ProjectManagementScreen(repository, user.token)
                is Screen.Employees -> EmployeeManagementScreen(repository, user.token)
                is Screen.Attendance -> AttendanceManagementScreen(repository, user.token)
                is Screen.Payroll -> PayrollScreen() // New Screen
                is Screen.Reports -> ReportScreen() // New Screen
            }
        }
    }
}

@Composable
fun TopHeader(
    currentScreen: Screen,
    onScreenSelect: (Screen) -> Unit,
    userName: String,
    onLogout: () -> Unit
) {
    // Correct order from mockups
    val screens =
        listOf(Screen.Reporting, Screen.Employees, Screen.Projects, Screen.Attendance, Screen.Payroll, Screen.Reports)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface)
            .padding(horizontal = 24.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "ConstructHR",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )

        Spacer(Modifier.width(64.dp))

        // Navigation links
        screens.forEach { screen ->
            HeaderButton(
                text = screen.title,
                isSelected = screen.title == currentScreen.title,
                onClick = { onScreenSelect(screen) }
            )
        }

        Spacer(Modifier.weight(1f))

        // User profile and actions
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            IconButton(onClick = { /* TODO: Notification logic */ }) {
                Icon(
                    Icons.Default.Notifications,
                    contentDescription = "Notifications",
                    tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )
            }

            var showLogoutMenu by remember { mutableStateOf(false) }
            Box {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.clip(CircleShape).clickable { showLogoutMenu = true }.padding(4.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primary),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            userName.firstOrNull()?.toString()?.uppercase() ?: "U",
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Spacer(Modifier.width(12.dp))
                    Text(userName, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.SemiBold)
                }

                DropdownMenu(
                    expanded = showLogoutMenu,
                    onDismissRequest = { showLogoutMenu = false }
                ) {
                    DropdownMenuItem(
                        text = { Text("Log Out") },
                        onClick = {
                            showLogoutMenu = false
                            onLogout()
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun HeaderButton(
    text: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val color =
        if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
    val weight = if (isSelected) FontWeight.Bold else FontWeight.Normal
    TextButton(
        onClick = onClick,
        colors = ButtonDefaults.textButtonColors(contentColor = color)
    ) {
        Text(text, fontWeight = weight, fontSize = MaterialTheme.typography.bodyLarge.fontSize)
    }
}