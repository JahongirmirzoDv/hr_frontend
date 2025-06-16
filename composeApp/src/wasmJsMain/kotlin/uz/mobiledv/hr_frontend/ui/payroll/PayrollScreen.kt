package uz.mobiledv.hr_frontend.ui.payroll

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@Composable
fun PayrollScreen() {
    var selectedTab by remember { mutableStateOf("Salary Rules") }

    Column(modifier = Modifier.fillMaxSize().padding(horizontal = 48.dp, vertical = 24.dp)) {
        Text("Payroll Management",
            style = MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )
        Text(
            "Configure and manage employee compensation with complete CRUD functionality.",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
        )
        Spacer(Modifier.height(24.dp))

        TabRow(selectedTabIndex = if (selectedTab == "Salary Rules") 1 else 0) {
            Tab(
                selected = selectedTab == "Current Payroll",
                onClick = { selectedTab = "Current Payroll" },
                text = { Text("Current Payroll") }
            )
            Tab(
                selected = selectedTab == "Salary Rules",
                onClick = { selectedTab = "Salary Rules" },
                text = { Text("Salary Rules") }
            )
            Tab(
                selected = selectedTab == "Salary History",
                onClick = { selectedTab = "Salary History" },
                text = { Text("Salary History") }
            )
        }
        Spacer(Modifier.height(24.dp))

        when (selectedTab) {
            "Salary Rules" -> SalaryRuleConfiguration()
            else -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("$selectedTab Screen - Not Implemented", style = MaterialTheme.typography.headlineSmall)
            }
        }
    }
}

@Composable
fun SalaryRuleConfiguration() {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(32.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Text("Salary Rule Configuration", style = MaterialTheme.typography.headlineSmall)
            // This would be a dropdown/searchable field in a real app
            OutlinedTextField(
                value = "",
                onValueChange = {},
                label = { Text("Employee") },
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = "",
                onValueChange = {},
                label = { Text("Salary Type") },
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = "",
                onValueChange = {},
                label = { Text("Base Rate") },
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = "",
                onValueChange = {},
                label = { Text("Bonus") },
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = "",
                onValueChange = {},
                label = { Text("Deductions") },
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = "",
                onValueChange = {},
                label = { Text("Daily Threshold") },
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = "",
                onValueChange = {},
                label = { Text("Monthly Threshold") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(16.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextButton(onClick = { /* TODO */ }) { Text("Cancel") }
                Spacer(Modifier.width(16.dp))
                Button(onClick = { /* TODO: Save logic */ }) {
                    Text("Save Changes")
                }
            }
        }
    }
}