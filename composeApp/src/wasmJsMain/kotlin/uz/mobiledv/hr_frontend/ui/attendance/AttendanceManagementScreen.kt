package uz.mobiledv.hr_frontend.ui.attendance

import HrRepository
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import uz.mobiledv.hr_frontend.data.remote.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AttendanceManagementScreen(repository: HrRepository, token: String) {
    var attendanceRecords by remember { mutableStateOf<List<AttendanceRecord>>(emptyList()) }
    var projects by remember { mutableStateOf<List<Project>>(emptyList()) }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    // --- State for Dialogs ---
    var showCreateDialog by remember { mutableStateOf(false) }
    var recordToEdit by remember { mutableStateOf<AttendanceRecord?>(null) }
    var recordToDelete by remember { mutableStateOf<AttendanceRecord?>(null) }

    // --- State for Filters ---
    var selectedProject by remember { mutableStateOf<Project?>(null) }
    var isProjectDropdownExpanded by remember { mutableStateOf(false) }
    var startDate by remember { mutableStateOf("") }
    var endDate by remember { mutableStateOf("") }

    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        projects = repository.getProjects(token) ?: emptyList()
    }

    fun fetchAttendance() {
        isLoading = true
        errorMessage = null
        coroutineScope.launch {
            try {
                attendanceRecords = repository.getAttendance(token, selectedProject?.id, startDate, endDate) ?: emptyList()
            } catch (e: Exception) {
                errorMessage = "Failed to load attendance: ${e.message}"
            } finally {
                isLoading = false
            }
        }
    }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(onClick = { showCreateDialog = true }) {
                Icon(Icons.Default.Add, "Add Attendance Record")
            }
        }
    ) { paddingValues ->
        Column(modifier = Modifier.fillMaxSize().padding(paddingValues).padding(16.dp)) {
            Text("Attendance Management", style = MaterialTheme.typography.headlineMedium)
            Spacer(Modifier.height(16.dp))

            Card(modifier = Modifier.fillMaxWidth()) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    ExposedDropdownMenuBox(expanded = isProjectDropdownExpanded, onExpandedChange = { isProjectDropdownExpanded = !isProjectDropdownExpanded }) {
                        OutlinedTextField(value = selectedProject?.name ?: "All Projects", onValueChange = {}, readOnly = true, label = { Text("Project") }, trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = isProjectDropdownExpanded) }, modifier = Modifier.menuAnchor().fillMaxWidth())
                        ExposedDropdownMenu(expanded = isProjectDropdownExpanded, onDismissRequest = { isProjectDropdownExpanded = false }) {
                            DropdownMenuItem(text = { Text("All Projects") }, onClick = { selectedProject = null; isProjectDropdownExpanded = false })
                            projects.forEach { project ->
                                DropdownMenuItem(text = { Text(project.name) }, onClick = { selectedProject = project; isProjectDropdownExpanded = false })
                            }
                        }
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(value = startDate, onValueChange = { startDate = it }, label = { Text("Start Date") }, modifier = Modifier.weight(1f))
                        OutlinedTextField(value = endDate, onValueChange = { endDate = it }, label = { Text("End Date") }, modifier = Modifier.weight(1f))
                    }
                    Button(onClick = { fetchAttendance() }, modifier = Modifier.align(Alignment.End)) { Text("Apply Filters") }
                }
            }
            Spacer(Modifier.height(16.dp))

            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.TopCenter) {
                when {
                    isLoading -> CircularProgressIndicator()
                    errorMessage != null -> Text(errorMessage!!, color = MaterialTheme.colorScheme.error)
                    attendanceRecords.isEmpty() -> Text("No attendance records found.")
                    else -> AttendanceList(
                        records = attendanceRecords,
                        onEditClick = { recordToEdit = it },
                        onDeleteClick = { recordToDelete = it }
                    )
                }
            }
        }
    }

    // --- DIALOGS ---
    if (showCreateDialog) {
        AttendanceDialog(
            projects = projects,
            onDismiss = { showCreateDialog = false },
            onConfirmCreate = { request ->
                coroutineScope.launch {
                    repository.createAttendance(token, request)?.let {
                        showCreateDialog = false
                        fetchAttendance()
                    }
                }
            }
        )
    }

    recordToEdit?.let { record ->
        AttendanceDialog(
            record = record,
            projects = projects,
            onDismiss = { recordToEdit = null },
            onConfirmUpdate = { request ->
                coroutineScope.launch {
                    repository.updateAttendance(token, record.id, request)?.let {
                        recordToEdit = null
                        fetchAttendance()
                    }
                }
            }
        )
    }

    recordToDelete?.let { record ->
        ConfirmDeleteDialog(
            recordId = record.id,
            onDismiss = { recordToDelete = null },
            onConfirm = {
                coroutineScope.launch {
                    if (repository.deleteAttendance(token, record.id)) {
                        recordToDelete = null
                        fetchAttendance()
                    }
                }
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AttendanceDialog(
    record: AttendanceRecord? = null,
    projects: List<Project>,
    onDismiss: () -> Unit,
    onConfirmCreate: ((CreateAttendanceRequest) -> Unit)? = null,
    onConfirmUpdate: ((UpdateAttendanceRequest) -> Unit)? = null
) {
    var employeeId by remember { mutableStateOf(record?.employeeId ?: "") }
    var selectedProject by remember { mutableStateOf(projects.find { it.id == record?.projectId } ) }
    var isDropdownExpanded by remember { mutableStateOf(false) }
    var checkInTime by remember { mutableStateOf(record?.checkInTime ?: "") }
    var checkOutTime by remember { mutableStateOf(record?.checkOutTime ?: "") }
    var isError by remember { mutableStateOf(false) }

    val title = if (record == null) "Add Attendance Record" else "Edit Attendance Record"

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                if (record == null) { // Only allow setting these on create
                    OutlinedTextField(value = employeeId, onValueChange = { employeeId = it }, label = { Text("Employee ID") })
                    ExposedDropdownMenuBox(expanded = isDropdownExpanded, onExpandedChange = { isDropdownExpanded = !isDropdownExpanded }) {
                        OutlinedTextField(value = selectedProject?.name ?: "Select Project", onValueChange = {}, readOnly = true, modifier = Modifier.menuAnchor(), label = {Text("Project")})
                        ExposedDropdownMenu(expanded = isDropdownExpanded, onDismissRequest = { isDropdownExpanded = false }) {
                            projects.forEach { project ->
                                DropdownMenuItem(text = { Text(project.name) }, onClick = {
                                    selectedProject = project
                                    isDropdownExpanded = false
                                })
                            }
                        }
                    }
                }
                OutlinedTextField(value = checkInTime, onValueChange = { checkInTime = it }, label = { Text("Check-in Time (ISO Format)") })
                OutlinedTextField(value = checkOutTime, onValueChange = { checkOutTime = it }, label = { Text("Check-out Time (Optional)") })
                if (isError) Text("Validation failed. Check fields.", color = MaterialTheme.colorScheme.error)
            }
        },
        confirmButton = {
            Button(onClick = {
                if (checkInTime.isBlank() || (record == null && (employeeId.isBlank() || selectedProject == null))) {
                    isError = true
                } else {
                    if (record == null) { // Create
                        onConfirmCreate?.invoke(CreateAttendanceRequest(employeeId, selectedProject!!.id, checkInTime, checkOutTime.ifBlank { null }))
                    } else { // Update
                        onConfirmUpdate?.invoke(UpdateAttendanceRequest(checkInTime, checkOutTime.ifBlank { null }))
                    }
                }
            }) { Text("Save") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}

@Composable
fun ConfirmDeleteDialog(recordId: String, onDismiss: () -> Unit, onConfirm: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Confirm Deletion") },
        text = { Text("Are you sure you want to delete attendance record '$recordId'?") },
        confirmButton = { Button(onClick = onConfirm, colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)) { Text("Delete") } },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}

@Composable
fun AttendanceList(
    records: List<AttendanceRecord>,
    onEditClick: (AttendanceRecord) -> Unit,
    onDeleteClick: (AttendanceRecord) -> Unit
) {
    LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        items(records) { record ->
            AttendanceListItem(
                record = record,
                onEditClick = { onEditClick(record) },
                onDeleteClick = { onDeleteClick(record) }
            )
        }
    }
}

@Composable
fun AttendanceListItem(
    record: AttendanceRecord,
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Record ID: ${record.id}", style = MaterialTheme.typography.titleSmall, modifier = Modifier.weight(1f))
                IconButton(onClick = onEditClick, modifier = Modifier.size(24.dp)) { Icon(Icons.Default.Edit, "Edit") }
                IconButton(onClick = onDeleteClick, modifier = Modifier.size(24.dp)) { Icon(Icons.Default.Delete, "Delete") }
            }
            Divider(modifier = Modifier.padding(vertical = 4.dp))
            Text("Employee ID: ${record.employeeId}")
            Text("Project ID: ${record.projectId}")
            Text("Check-in: ${record.checkInTime}")
            Text("Check-out: ${record.checkOutTime ?: "N/A"}")
        }
    }
}