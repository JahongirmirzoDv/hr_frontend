package uz.mobiledv.hr_frontend.ui.employee

import HrRepository
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import uz.mobiledv.hr_frontend.data.remote.*

@Composable
fun EmployeeManagementScreen(repository: HrRepository, token: String) {
    var employees by remember { mutableStateOf<List<Employee>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    // Dialog states
    var showCreateDialog by remember { mutableStateOf(false) }
    var employeeToEdit by remember { mutableStateOf<Employee?>(null) }
    var employeeToDelete by remember { mutableStateOf<Employee?>(null) }

    val coroutineScope = rememberCoroutineScope()

    fun refreshEmployees() {
        isLoading = true
        errorMessage = null
        coroutineScope.launch {
            try {
                employees = repository.getEmployees(token) ?: emptyList()
            } catch (e: Exception) {
                errorMessage = "Failed to load employees: ${e.message}"
            } finally {
                isLoading = false
            }
        }
    }

    LaunchedEffect(Unit) { refreshEmployees() }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(onClick = { showCreateDialog = true }) {
                Icon(Icons.Default.Add, "Add Employee")
            }
        }
    ) { paddingValues ->
        Box(modifier = Modifier.fillMaxSize().padding(paddingValues), contentAlignment = Alignment.Center) {
            when {
                isLoading -> CircularProgressIndicator()
                errorMessage != null -> Text(errorMessage!!, color = MaterialTheme.colorScheme.error)
                employees.isEmpty() -> Text("No employees found.")
                else -> EmployeeList(
                    employees = employees,
                    onEditClick = { employeeToEdit = it },
                    onDeleteClick = { employeeToDelete = it }
                )
            }
        }
    }

    // --- DIALOGS ---
    if (showCreateDialog) {
        EmployeeDialog(
            onDismiss = { showCreateDialog = false },
            onConfirmCreate = { request ->
                coroutineScope.launch {
                    repository.createEmployee(token, request)?.let {
                        showCreateDialog = false
                        refreshEmployees()
                    }
                }
            }
        )
    }

    employeeToEdit?.let { employee ->
        EmployeeDialog(
            employee = employee,
            onDismiss = { employeeToEdit = null },
            onConfirmUpdate = { request ->
                coroutineScope.launch {
                    repository.updateEmployee(token, employee.id, request)?.let {
                        employeeToEdit = null
                        refreshEmployees()
                    }
                }
            }
        )
    }

    employeeToDelete?.let { employee ->
        ConfirmDeleteDialog(
            employeeName = employee.name,
            onDismiss = { employeeToDelete = null },
            onConfirm = {
                coroutineScope.launch {
                    if (repository.deleteEmployee(token, employee.id)) {
                        employeeToDelete = null
                        refreshEmployees()
                    }
                }
            }
        )
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EmployeeDialog(
    employee: Employee? = null,
    onDismiss: () -> Unit,
    onConfirmCreate: ((CreateEmployeeRequest) -> Unit)? = null,
    onConfirmUpdate: ((UpdateEmployeeRequest) -> Unit)? = null
) {
    var name by remember { mutableStateOf(employee?.name ?: "") }
    var position by remember { mutableStateOf(employee?.position ?: "") }
    var salaryRate by remember { mutableStateOf(employee?.salaryAmount?.toString() ?: "") }
    var selectedSalaryType by remember { mutableStateOf(employee?.salaryType ?: SalaryType.FIXED_MONTHLY.name) }

    val salaryTypes = SalaryType.entries.toTypedArray()
    var isDropdownExpanded by remember { mutableStateOf(false) }
    var isError by remember { mutableStateOf(false) }


    val title = if (employee == null) "Add New Employee" else "Edit Employee"

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Full Name") })
                OutlinedTextField(value = position, onValueChange = { position = it }, label = { Text("Position") })
                OutlinedTextField(value = salaryRate, onValueChange = { salaryRate = it }, label = { Text("Salary Rate") })
                ExposedDropdownMenuBox(expanded = isDropdownExpanded, onExpandedChange = { isDropdownExpanded = !isDropdownExpanded }) {
                    OutlinedTextField(
                        value = selectedSalaryType,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Salary Type") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = isDropdownExpanded) },
                        modifier = Modifier.menuAnchor()
                    )
                    ExposedDropdownMenu(expanded = isDropdownExpanded, onDismissRequest = { isDropdownExpanded = false }) {
                        salaryTypes.forEach { type ->
                            DropdownMenuItem(text = { Text(type.name) }, onClick = {
                                selectedSalaryType = type.name
                                isDropdownExpanded = false
                            })
                        }
                    }
                }
                if (isError) Text("Validation failed. Check fields.", color = MaterialTheme.colorScheme.error)
            }
        },
        confirmButton = {
            Button(onClick = {
                val rate = salaryRate.toDoubleOrNull()
                if (name.isBlank() || position.isBlank() || rate == null) {
                    isError = true
                } else {
                    val selectedSalaryType = SalaryType.valueOf(selectedSalaryType)
                    if (employee == null) {
                        onConfirmCreate?.invoke(CreateEmployeeRequest(name, position, selectedSalaryType, rate))
                    } else {
                        onConfirmUpdate?.invoke(UpdateEmployeeRequest(name, position, selectedSalaryType, rate))
                    }
                }
            }) { Text("Save") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}

@Composable
fun ConfirmDeleteDialog(employeeName: String, onDismiss: () -> Unit, onConfirm: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Confirm Deletion") },
        text = { Text("Are you sure you want to delete employee '$employeeName'?") },
        confirmButton = {
            Button(onClick = onConfirm, colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)) { Text("Delete") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}

@Composable
fun EmployeeList(
    employees: List<Employee>,
    onEditClick: (Employee) -> Unit,
    onDeleteClick: (Employee) -> Unit
) {
    LazyColumn(modifier = Modifier.fillMaxSize(), contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        items(employees) { employee ->
            EmployeeListItem(
                employee = employee,
                onEditClick = { onEditClick(employee) },
                onDeleteClick = { onDeleteClick(employee) }
            )
        }
    }
}

@Composable
fun EmployeeListItem(
    employee: Employee,
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
    Card(modifier = Modifier.fillMaxWidth(), elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier.size(56.dp).clip(CircleShape).background(MaterialTheme.colorScheme.secondaryContainer),
                contentAlignment = Alignment.Center
            ) {
                Text(employee.name.firstOrNull()?.toString() ?: "", style = MaterialTheme.typography.titleLarge)
            }
            Spacer(Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(employee.name, style = MaterialTheme.typography.titleMedium)
                Text(employee.position, style = MaterialTheme.typography.bodyMedium)
                Text(
                    "Salary: ${employee.salaryType} ($${employee.salaryAmount})",
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Bold
                )
            }
            Row {
                IconButton(onClick = onEditClick) { Icon(Icons.Default.Edit, "Edit Employee") }
                IconButton(onClick = onDeleteClick) { Icon(Icons.Default.Delete, "Delete Employee") }
            }
        }
    }
}