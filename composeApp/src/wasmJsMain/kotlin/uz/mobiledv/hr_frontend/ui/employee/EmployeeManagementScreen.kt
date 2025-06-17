package uz.mobiledv.hr_frontend.ui.employee

import uz.mobiledv.hr_frontend.data.ApiService
import uz.mobiledv.hr_frontend.data.HrRepository
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import org.koin.compose.viewmodel.koinViewModel
import uz.mobiledv.hr_frontend.data.remote.*
import uz.mobiledv.hr_frontend.vm.EmployeeManagementViewModel
import uz.mobiledv.hr_frontend.vm.UserManagementViewModel
import kotlin.math.ceil

@Composable
fun EmployeeManagementScreen(
    token: String,
    viewModel: EmployeeManagementViewModel = koinViewModel()
) {
    var allEmployees by viewModel.allEmployees
    var isLoading by viewModel.isLoading
    var errorMessage by viewModel.errorMessage
    var showCreateDialog by remember { mutableStateOf(false) }
    var employeeToEdit by remember { mutableStateOf<Employee?>(null) }
    var employeeToDelete by remember { mutableStateOf<Employee?>(null) }
    val coroutineScope = rememberCoroutineScope()

    // State for Search and Filter
    var searchQuery by remember { mutableStateOf("") }
    var statusFilter by remember { mutableStateOf("All") }

    // State for Pagination
    val pageSize = 10
    var currentPage by remember { mutableStateOf(1) }

    LaunchedEffect(token) { viewModel.refreshEmployees(token) }


    val filteredEmployees = remember(searchQuery, statusFilter, allEmployees) {
        allEmployees
            .filter { employee ->
                employee.name.contains(searchQuery, ignoreCase = true) ||
                        employee.position.contains(searchQuery, ignoreCase = true)
            }
            .filter { employee ->
                when (statusFilter) {
                    "Active" -> employee.isActive
                    "Inactive" -> !employee.isActive
                    else -> true
                }
            }
    }

    val totalPages = ceil(filteredEmployees.size.toFloat() / pageSize).toInt()
    val paginatedEmployees = filteredEmployees.chunked(pageSize).getOrNull(currentPage - 1) ?: emptyList()

    Column(modifier = Modifier.fillMaxSize().padding(horizontal = 48.dp, vertical = 24.dp)) {
        // Header
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                "Employees",
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.weight(1f),
                color = MaterialTheme.colorScheme.onSurface
            )
            Button(onClick = { showCreateDialog = true }) {
                Icon(Icons.Default.Add, "Add Employee", Modifier.size(18.dp))
                Spacer(Modifier.width(8.dp))
                Text("Add Employee")
            }
        }
        Spacer(Modifier.height(24.dp))

        // Search and Filter Bar
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier.weight(1f),
                label = { Text("Search by name or position...") },
                leadingIcon = { Icon(Icons.Default.Search, "Search") }
            )
            // TODO: Add more filters for Role, Project as in mockup
            FilterDropdown(
                label = "Status",
                options = listOf("All", "Active", "Inactive"),
                selectedOption = statusFilter,
                onOptionSelected = { statusFilter = it }
            )
        }
        Spacer(Modifier.height(24.dp))

        // Content Area
        Card(modifier = Modifier.fillMaxSize()) {
            Box(modifier = Modifier.fillMaxSize()) {
                when {
                    isLoading -> CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                    errorMessage != null -> Text(
                        errorMessage!!,
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.align(Alignment.Center)
                    )

                    allEmployees.isEmpty() -> Text("No employees found.", modifier = Modifier.align(Alignment.Center))
                    else -> {
                        Column {
                            EmployeeList(
                                employees = paginatedEmployees,
                                onEditClick = { employeeToEdit = it },
                                onDeleteClick = { employeeToDelete = it }
                            )
                            Spacer(Modifier.weight(1f))
                            PaginationControls(
                                currentPage = currentPage,
                                totalPages = totalPages,
                                onPageChange = { currentPage = it }
                            )
                        }
                    }
                }
            }
        }
    }

    // --- DIALOGS ---
    if (showCreateDialog) {
        EmployeeDialog(
            onDismiss = { showCreateDialog = false },
            onConfirmCreate = { request ->
                coroutineScope.launch {
                    viewModel.createEmployee(token,request){
                        showCreateDialog = false
                        viewModel.refreshEmployees(token)
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
                    viewModel.updateEmployee(token, employee.id, request){
                        employeeToEdit = null
                        viewModel.refreshEmployees(token)
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
                    viewModel.deleteEmployee(token, employee.id){
                        employeeToDelete = null
                        viewModel.refreshEmployees(token)
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
                OutlinedTextField(
                    value = salaryRate,
                    onValueChange = { salaryRate = it },
                    label = { Text("Salary Rate") })

                ExposedDropdownMenuBox(
                    expanded = isDropdownExpanded,
                    onExpandedChange = { isDropdownExpanded = !isDropdownExpanded }) {
                    OutlinedTextField(
                        value = selectedSalaryType,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Salary Type") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = isDropdownExpanded) },
                        modifier = Modifier.menuAnchor()
                    )
                    ExposedDropdownMenu(
                        expanded = isDropdownExpanded,
                        onDismissRequest = { isDropdownExpanded = false }) {
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
            Button(
                onClick = onConfirm,
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
            ) { Text("Delete") }
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
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
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
                modifier = Modifier.size(56.dp).clip(CircleShape)
                    .background(MaterialTheme.colorScheme.secondaryContainer),
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


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FilterDropdown(label: String, options: List<String>, selectedOption: String, onOptionSelected: (String) -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = !expanded }) {
        OutlinedTextField(
            value = selectedOption,
            onValueChange = {},
            readOnly = true,
            label = { Text(label) },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier.menuAnchor().width(150.dp)
        )
        ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            options.forEach { option ->
                DropdownMenuItem(text = { Text(option) }, onClick = {
                    onOptionSelected(option)
                    expanded = false
                })
            }
        }
    }
}

@Composable
fun PaginationControls(currentPage: Int, totalPages: Int, onPageChange: (Int) -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        IconButton(onClick = { onPageChange(currentPage - 1) }, enabled = currentPage > 1) {
            Icon(Icons.Default.ChevronLeft, "Previous Page")
        }
        Spacer(Modifier.width(16.dp))
        Text("Page $currentPage of $totalPages", style = MaterialTheme.typography.bodyLarge)
        Spacer(Modifier.width(16.dp))
        IconButton(onClick = { onPageChange(currentPage + 1) }, enabled = currentPage < totalPages) {
            Icon(Icons.Default.ChevronRight, "Next Page")
        }
    }
}