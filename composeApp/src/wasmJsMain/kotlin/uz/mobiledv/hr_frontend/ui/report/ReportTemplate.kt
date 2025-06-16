package uz.mobiledv.hr_frontend.ui.report

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

data class ReportTemplate(
    val title: String,
    val description: String
)

val reportTemplates = listOf(
    ReportTemplate("Attendance Summary", "Generate a report summarizing employee attendance over a specified period."),
    ReportTemplate(
        "Salary Distribution",
        "Analyze the distribution of salaries across different departments or roles."
    ),
    ReportTemplate(
        "Project Cost Analysis",
        "Generate a report analyzing project costs, including labor and material expenses."
    ),
    ReportTemplate(
        "Employee Performance Metrics",
        "Generate a report analyzing employee performance metrics, including project completion rates and feedback."
    )
)

@Composable
fun ReportScreen() {
    var selectedTab by remember { mutableStateOf("Templates") }
    Column(modifier = Modifier.fillMaxSize().padding(horizontal = 48.dp, vertical = 24.dp)) {
        Text("Reports",
            style = MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.Bold,
            color =  MaterialTheme.colorScheme.onSurface
        )
        Text(
            "Generate and view detailed reports on various HR aspects.",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
        )
        Spacer(Modifier.height(24.dp))

        TabRow(selectedTabIndex = if (selectedTab == "Templates") 0 else 1) {
            Tab(
                selected = selectedTab == "Templates",
                onClick = { selectedTab = "Templates" },
                text = { Text("Templates") })
            Tab(
                selected = selectedTab == "Generated Reports",
                onClick = { selectedTab = "Generated Reports" },
                text = { Text("Generated Reports") })
        }
        Spacer(Modifier.height(24.dp))

        when (selectedTab) {
            "Templates" -> ReportTemplateGrid()
            else -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("$selectedTab Screen - Not Implemented", style = MaterialTheme.typography.headlineSmall)
            }
        }
    }
}

@Composable
fun ReportTemplateGrid() {
    LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(24.dp),
        horizontalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        items(reportTemplates) { template ->
            ReportTemplateCard(template)
        }
    }
}

@Composable
fun ReportTemplateCard(template: ReportTemplate) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.padding(24.dp)) {
            Text(template.title, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(8.dp))
            Text(
                template.description,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                modifier = Modifier.height(60.dp)
            )
            Spacer(Modifier.height(16.dp))
            Button(onClick = { /* TODO: Generate Report Logic */ }) {
                Text("Generate")
            }
        }
    }
}