package uz.mobiledv.hr_frontend.ui.reports

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.koin.compose.viewmodel.koinViewModel
import uz.mobiledv.hr_frontend.data.HrRepository
import uz.mobiledv.hr_frontend.data.remote.DashboardSummary
import uz.mobiledv.hr_frontend.data.remote.ProjectAttendanceSummary
import uz.mobiledv.hr_frontend.vm.ReportingDashboardViewModel

@Composable
fun ReportingDashboardScreen(
    token: String,
    viewModel : ReportingDashboardViewModel = koinViewModel()
) {
    var summary by viewModel.summary
    var isLoading by viewModel.isLoading
    var errorMessage by viewModel.errorMessage

    LaunchedEffect(token) {
       viewModel.refreshSummary(token)
    }

    Box(
        modifier = Modifier.fillMaxSize().padding(horizontal = 48.dp, vertical = 24.dp),
        contentAlignment = Alignment.TopCenter
    ) {
        when {
            isLoading -> CircularProgressIndicator()
            errorMessage != null -> Text(errorMessage!!, color = MaterialTheme.colorScheme.error)
            summary == null -> Text("No summary data available.", color = MaterialTheme.colorScheme.onSurface)
            else -> DashboardContent(summary!!)
        }
    }
}

@Composable
fun DashboardContent(summary: DashboardSummary) {
    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        item {
            Text(
                "Dashboard",
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold
            )
            Text(
                "Welcome back, here is the summary of your business.",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
            )
        }

        item {
            // Key Metrics Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                StatCard("Checked-in Today", summary.checkedInToday.toString(), Modifier.weight(1f))
                StatCard("Absent Today", summary.absentToday.toString(), Modifier.weight(1f))
                StatCard(
                    "Monthly Salary Expenses",
                    "$",//+ "%.2f".format(summary.monthlySalaryExpense) // Mock data, use summary.monthlySalaryExpense
                    Modifier.weight(1f)
                )
            }
        }

        item {
            Text(
                "Analytics",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(top = 16.dp)
            )
        }

        item {
            // Analytics Cards Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                AnalyticsChartCard("Attendance Trends", Modifier.weight(1f))
                AnalyticsChartCard("Salary Analytics", Modifier.weight(1f))
                AnalyticsChartCard("Overtime Statistics", Modifier.weight(1f))
            }
        }

        item {
            Text(
                "Attendance Per Project",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(top = 16.dp)
            )
        }

        items(summary.attendancePerProject) { projectSummary ->
            ProjectAttendanceCard(projectSummary)
        }
    }
}

@Composable
fun StatCard(title: String, value: String, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(24.dp)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
fun AnalyticsChartCard(title: String, modifier: Modifier = Modifier) {
    Card(modifier = modifier.height(300.dp)) {
        Column(Modifier.padding(24.dp)) {
            Text(title, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("// TODO: Implement Chart", color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
            }
        }
    }
}

@Composable
fun ProjectAttendanceCard(projectSummary: ProjectAttendanceSummary) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Text(
                projectSummary.projectName,
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.weight(1f),
                fontWeight = FontWeight.Bold
            )
            StatItem("Present", projectSummary.totalPresent.toString())
            Spacer(Modifier.width(24.dp))
            StatItem("Absent", projectSummary.totalAbsent.toString())
        }
    }
}

@Composable
fun StatItem(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = value, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
        )
    }
}