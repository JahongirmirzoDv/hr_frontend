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
import uz.mobiledv.hr_frontend.ui.project.ProjectManagementScreen
import uz.mobiledv.hr_frontend.ui.reports.ReportingDashboardScreen
import uz.mobiledv.hr_frontend.ui.user.UserManagementScreen

sealed class Screen(val title: String) {
    object Reporting : Screen("Dashboard") // Renamed for consistency with mockups
    object Employees : Screen("Employees")
    object Projects : Screen("Projects")
    object Attendance : Screen("Attendance")
    object Reports : Screen("Reports")
    object Payroll : Screen("Payroll")
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
        // Our new Top Header Navigation
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
                // Add placeholders for other screens from mockups
                is Screen.Payroll -> CenterText("Payroll Management")
                is Screen.Reports -> CenterText("Report Generation")
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
    val screens = listOf(Screen.Reporting, Screen.Employees, Screen.Projects, Screen.Payroll, Screen.Reports)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface)
            .padding(horizontal = 24.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "ConstructHR",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )

        Spacer(Modifier.width(48.dp))

        // Navigation links
        screens.forEach { screen ->
            HeaderButton(
                text = screen.title,
                isSelected = screen == currentScreen,
                onClick = { onScreenSelect(screen) }
            )
        }

        Spacer(Modifier.weight(1f))

        // Right side icons
        IconButton(onClick = { /* TODO */ }) {
            Icon(Icons.Default.Notifications, contentDescription = "Notifications", tint = MaterialTheme.colorScheme.onSurface)
        }
        Spacer(Modifier.width(8.dp))
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.clickable { onLogout() }) {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary),
                contentAlignment = Alignment.Center
            ) {
                Text(userName.first().toString(), color = Color.White, fontWeight = FontWeight.Bold)
            }
            Spacer(Modifier.width(8.dp))
            // This is just a placeholder, you can add a dropdown on click
            Text(userName, style = MaterialTheme.typography.bodyMedium)
        }
    }
}

@Composable
fun HeaderButton(
    text: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
    TextButton(onClick = onClick) {
        Text(text, color = color, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal)
    }
}

@Composable
fun CenterText(text: String) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text(text, style = MaterialTheme.typography.headlineSmall)
    }
}