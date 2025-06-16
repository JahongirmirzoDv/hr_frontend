package uz.mobiledv.hr_frontend.ui.attendance

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import uz.mobiledv.hr_frontend.data.ApiService
import uz.mobiledv.hr_frontend.data.HrRepository
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
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
    var showCreateDialog by remember { mutableStateOf(false) }
    var recordToEdit by remember { mutableStateOf<AttendanceRecord?>(null) }
    var recordToDelete by remember { mutableStateOf<AttendanceRecord?>(null) }
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        isLoading = true
        errorMessage = null
        coroutineScope.launch {
            try {
                projects = repository.getProjects(token) ?: emptyList()
                attendanceRecords = repository.getAttendance(token, null, null, null) ?: emptyList()
            } catch (e: Exception) {
                errorMessage = "Failed to load data: ${e.message}"
            } finally {
                isLoading = false
            }
        }
    }

    fun fetchAttendanceWithFilters(projectId: String?, startDate: String?, endDate: String?) {
        isLoading = true
        errorMessage = null
        coroutineScope.launch {
            try {
                attendanceRecords = repository.getAttendance(token, projectId, startDate, endDate) ?: emptyList()
            } catch (e: Exception) {
                errorMessage = "Failed to load attendance: ${e.message}"
            } finally {
                isLoading = false
            }
        }
    }

    Column(modifier = Modifier.fillMaxSize().padding(horizontal = 48.dp, vertical = 24.dp)) {
        Text("Attendance Management",
            style = MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )
        Text(
            "Manage employee attendance records, track hours, and generate reports.",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
        )
        Spacer(Modifier.height(24.dp))

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            // Calendar View
            Card(modifier = Modifier.weight(1.5f)) {
                CalendarView()
            }
            // Attendance List
            Card(modifier = Modifier.weight(1f).fillMaxHeight()) {
                Column(Modifier.padding(16.dp)) {
                    Text("Attendance Records", style = MaterialTheme.typography.titleLarge)
                    Spacer(Modifier.height(16.dp))
                    when {
                        isLoading -> Box(
                            Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) { CircularProgressIndicator() }

                        errorMessage != null -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text(
                                errorMessage!!
                            )
                        }

                        attendanceRecords.isEmpty() -> Box(
                            Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) { Text("No records found.") }

                        else -> AttendanceList(
                            records = attendanceRecords,
                            onEditClick = { recordToEdit = it },
                            onDeleteClick = { recordToDelete = it }
                        )
                    }
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
//                        fetchAttendance()
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
//                        fetchAttendance()
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
//                        fetchAttendance()
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
    var selectedProject by remember { mutableStateOf(projects.find { it.id == record?.projectId }) }
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
                    OutlinedTextField(
                        value = employeeId,
                        onValueChange = { employeeId = it },
                        label = { Text("Employee ID") })
                    ExposedDropdownMenuBox(
                        expanded = isDropdownExpanded,
                        onExpandedChange = { isDropdownExpanded = !isDropdownExpanded }) {
                        OutlinedTextField(
                            value = selectedProject?.name ?: "Select Project",
                            onValueChange = {},
                            readOnly = true,
                            modifier = Modifier.menuAnchor(),
                            label = { Text("Project") })
                        ExposedDropdownMenu(
                            expanded = isDropdownExpanded,
                            onDismissRequest = { isDropdownExpanded = false }) {
                            projects.forEach { project ->
                                DropdownMenuItem(text = { Text(project.name) }, onClick = {
                                    selectedProject = project
                                    isDropdownExpanded = false
                                })
                            }
                        }
                    }
                }
                OutlinedTextField(
                    value = checkInTime,
                    onValueChange = { checkInTime = it },
                    label = { Text("Check-in Time (ISO Format)") })
                OutlinedTextField(
                    value = checkOutTime,
                    onValueChange = { checkOutTime = it },
                    label = { Text("Check-out Time (Optional)") })
                if (isError) Text("Validation failed. Check fields.", color = MaterialTheme.colorScheme.error)
            }
        },
        confirmButton = {
            Button(onClick = {
                if (checkInTime.isBlank() || (record == null && (employeeId.isBlank() || selectedProject == null))) {
                    isError = true
                } else {
                    if (record == null) { // Create
                        onConfirmCreate?.invoke(
                            CreateAttendanceRequest(
                                employeeId,
                                selectedProject!!.id,
                                checkInTime,
                                checkOutTime.ifBlank { null })
                        )
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
fun CalendarView() {
    // NOTE: This is a static representation. A real implementation would require a date library
    // like kotlinx-datetime to manage state, month transitions, and day selection.
    val daysOfWeek = listOf("S", "M", "T", "W", "T", "F", "S")
    val daysInMonth = (1..31).toList()
    val today = 15 // Mocking "today"

    Column(Modifier.padding(24.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = { /* TODO: Previous Month */ }) { Icon(Icons.Default.ChevronLeft, null) }
            Text(
                "July 2024",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.weight(1f)
            )
            IconButton(onClick = { /* TODO: Next Month */ }) { Icon(Icons.Default.ChevronRight, null) }
        }
        Spacer(Modifier.height(16.dp))
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceAround) {
            daysOfWeek.forEach { day ->
                Text(day, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
            }
        }
        Spacer(Modifier.height(16.dp))
        LazyVerticalGrid(
            columns = GridCells.Fixed(7),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(daysInMonth) { day ->
                val isToday = day == today
                Box(
                    modifier = Modifier
                        .aspectRatio(1f)
                        .clip(CircleShape)
                        .background(if (isToday) MaterialTheme.colorScheme.primary else Color.Transparent)
                        .border(
                            width = if (isToday) 0.dp else 1.dp,
                            color = if (isToday) Color.Transparent else MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                            shape = CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = day.toString(),
                        color = if (isToday) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }
        Spacer(Modifier.height(16.dp))
        Button(modifier = Modifier.fillMaxWidth(), onClick = { /* TODO: Add new record */ }) {
            Icon(Icons.Default.Add, null, modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(8.dp))
            Text("Add New Record")
        }
    }
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
    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(vertical = 8.dp)) {
        // Mock Photo
        Box(
            modifier = Modifier.size(40.dp).clip(CircleShape).background(MaterialTheme.colorScheme.secondaryContainer),
            contentAlignment = Alignment.Center
        ) {
            // In a real app, you'd fetch the employee name
            Text(
                record.employeeId.firstOrNull()?.toString() ?: "E",
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Bold
            )
        }
        Spacer(Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            // We only have IDs, so we display them. A real app would resolve these to names.
            Text(
                "Employee: ${record.employeeId.take(8)}...",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                "Project: ${record.projectId.take(8)}...",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
            )
        }
        Text(
            text = if (record.checkOutTime != null) "Present" else "Checked-In",
            color = if (record.checkOutTime != null) Color(0xFF22C55E) else Color(0xFFFACC15),
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Bold
        )
        Row {
            IconButton(onClick = onEditClick, modifier = Modifier.size(36.dp)) {
                Icon(
                    Icons.Default.Edit,
                    "Edit",
                    modifier = Modifier.size(18.dp)
                )
            }
            IconButton(onClick = onDeleteClick, modifier = Modifier.size(36.dp)) {
                Icon(
                    Icons.Default.Delete,
                    "Delete",
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}