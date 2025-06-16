// In composeApp/src/wasmJsMain/kotlin/uz/mobiledv/hr_frontend/ui/reporting/ReportingDashboardScreen.kt
package uz.mobiledv.hr_frontend.ui.reports

import uz.mobiledv.hr_frontend.data.ApiService
import uz.mobiledv.hr_frontend.data.HrRepository
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Download
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import uz.mobiledv.hr_frontend.data.remote.DashboardSummary

@Composable
fun ReportingDashboardScreen(repository: HrRepository, token: String) {
    var summary by remember { mutableStateOf<DashboardSummary?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        isLoading = true
        errorMessage = null
        try {
            summary = repository.getDashboardSummary(token)
        } catch (e: Exception) {
            errorMessage = "Failed to load dashboard data: ${e.message}"
        } finally {
            isLoading = false
        }
    }

    Scaffold(
        floatingActionButton = {
            ExtendedFloatingActionButton(
                icon = { Icon(Icons.Default.Download, contentDescription = "Export") },
                text = { Text("Export Reports") },
                onClick = { /* TODO: Implement CSV/Excel export logic */ }
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier.fillMaxSize().padding(paddingValues),
            contentAlignment = Alignment.Center
        ) {
            when {
                isLoading -> CircularProgressIndicator()
                errorMessage != null -> Text(errorMessage!!, color = MaterialTheme.colorScheme.error)
                summary == null -> Text("No summary data available.")
                else -> DashboardContent(summary!!)
            }
        }
    }
}

@Composable
fun DashboardContent(summary: DashboardSummary) {
    LazyColumn(
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text("Analytics Dashboard", style = MaterialTheme.typography.headlineMedium)
        }

        item {
            // Daily Summary Card
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Daily Summary", style = MaterialTheme.typography.titleLarge)
                    Spacer(Modifier.height(8.dp))
                    Row(horizontalArrangement = Arrangement.SpaceAround, modifier = Modifier.fillMaxWidth()) {
                        StatItem("Checked-in Today", summary.checkedInToday.toString())
                        StatItem("Absent Today", summary.absentToday.toString())
                    }
                }
            }
        }

        item {
            // Monthly Expenses Card
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Financials", style = MaterialTheme.typography.titleLarge)
                    Spacer(Modifier.height(8.dp))
                    StatItem("Total Salary Expenses (Month)","")//"$" + "%.2f".format(summary.monthlySalaryExpense)
                    Spacer(Modifier.height(8.dp))
                    Text("// TODO: Implement Salary Chart (e.g., Bar Chart)", style = MaterialTheme.typography.bodySmall)
                }
            }
        }

        item {
            Text("Monthly Attendance Per Project", style = MaterialTheme.typography.titleLarge)
        }

        items(summary.attendancePerProject) { projectSummary ->
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(projectSummary.projectName, style = MaterialTheme.typography.titleMedium)
                    Spacer(Modifier.height(8.dp))
                    Row(horizontalArrangement = Arrangement.SpaceAround, modifier = Modifier.fillMaxWidth()) {
                        StatItem("Present", projectSummary.totalPresent.toString())
                        StatItem("Absent", projectSummary.totalAbsent.toString())
                    }
                    Spacer(Modifier.height(8.dp))
                    Text("// TODO: Implement Attendance Chart (e.g., Pie Chart)", style = MaterialTheme.typography.bodySmall)
                }
            }
        }
    }
}

@Composable
fun StatItem(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = value, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
        Text(text = label, style = MaterialTheme.typography.bodyMedium)
    }
}