package uz.mobiledv.hr_frontend.ui.project

import uz.mobiledv.hr_frontend.data.ApiService
import uz.mobiledv.hr_frontend.data.HrRepository
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import uz.mobiledv.hr_frontend.data.remote.Project
import uz.mobiledv.hr_frontend.ui.employee.PaginationControls
import kotlin.math.ceil

@Composable
fun ProjectManagementScreen(repository: HrRepository, token: String) {
    var allProjects by remember { mutableStateOf<List<Project>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var showCreateDialog by remember { mutableStateOf(false) }
    var projectToEdit by remember { mutableStateOf<Project?>(null) }
    var projectToDelete by remember { mutableStateOf<Project?>(null) }
    val coroutineScope = rememberCoroutineScope()

    // State for UI controls
    var searchQuery by remember { mutableStateOf("") }
    var viewMode by remember { mutableStateOf("List") } // "List" or "Card"
    val pageSize = 9
    var currentPage by remember { mutableStateOf(1) }

    fun refreshProjects() {
        isLoading = true
        errorMessage = null
        coroutineScope.launch {
            try {
                allProjects = repository.getProjects(token) ?: emptyList()
            } catch (e: Exception) {
                errorMessage = "Failed to load projects: ${e.message}"
            } finally {
                isLoading = false
            }
        }
    }

    LaunchedEffect(Unit) {
        refreshProjects()
    }

    val filteredProjects = remember(searchQuery, allProjects) {
        allProjects.filter {
            it.name.contains(searchQuery, ignoreCase = true) ||
                    it.location.contains(searchQuery, ignoreCase = true)
        }
    }

    val totalPages = ceil(filteredProjects.size.toFloat() / pageSize).toInt()
    val paginatedProjects = filteredProjects.chunked(pageSize).getOrNull(currentPage - 1) ?: emptyList()


    Column(modifier = Modifier.fillMaxSize().padding(horizontal = 48.dp, vertical = 24.dp)) {
        // Header
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                "Projects",
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.weight(1f),
                color = MaterialTheme.colorScheme.onSurface
            )
            Button(onClick = { showCreateDialog = true }) {
                Icon(Icons.Default.Add, "Add Project", Modifier.size(18.dp))
                Spacer(Modifier.width(8.dp))
                Text("New Project")
            }
        }
        Spacer(Modifier.height(24.dp))

        // Search and View Toggle Bar
        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier.weight(1f),
                label = { Text("Search projects...") },
                leadingIcon = { Icon(Icons.Default.Search, "Search") }
            )
            Spacer(Modifier.width(16.dp))
            SingleChoiceSegmentedButtonRow {
                SegmentedButton(
                    selected = viewMode == "List",
                    onClick = { viewMode = "List" },
                    shape = SegmentedButtonDefaults.itemShape(count = 2, index = 0)
                ) { Text("List") }
                SegmentedButton(
                    selected = viewMode == "Card",
                    onClick = { viewMode = "Card" },
                    shape = SegmentedButtonDefaults.itemShape(count = 2, index = 1)
                ) { Text("Card") }
            }
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

                    paginatedProjects.isEmpty() -> Text(
                        "No projects match your search.",
                        modifier = Modifier.align(Alignment.Center)
                    )

                    else -> Column {
                        if (viewMode == "List") {
                            ProjectList(
                                projects = paginatedProjects,
                                onEditClick = { projectToEdit = it },
                                onDeleteClick = { projectToDelete = it }
                            )
                        } else {
                            ProjectCardGrid(
                                projects = paginatedProjects,
                                onEditClick = { projectToEdit = it },
                                onDeleteClick = { projectToDelete = it }
                            )
                        }
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
    // --- DIALOGS ---

    // Dialog for Creating a new project
    if (showCreateDialog) {
        ProjectDialog(
            onDismiss = { showCreateDialog = false },
            onConfirm = { newProject ->
                coroutineScope.launch {
                    repository.createProject(token, newProject)?.let {
                        showCreateDialog = false
                        refreshProjects()
                    }
                }
            }
        )
    }

    // Dialog for Editing an existing project
    projectToEdit?.let { project ->
        ProjectDialog(
            project = project,
            onDismiss = { projectToEdit = null },
            onConfirm = { updatedProject ->
                coroutineScope.launch {
                    repository.updateProject(token, project.id, updatedProject)?.let {
                        projectToEdit = null
                        refreshProjects()
                    }
                }
            }
        )
    }

    // Confirmation Dialog for Deleting a project
    projectToDelete?.let { project ->
        ConfirmDeleteDialog(
            projectName = project.name,
            onDismiss = { projectToDelete = null },
            onConfirm = {
                coroutineScope.launch {
                    if (repository.deleteProject(token, project.id)) {
                        projectToDelete = null
                        refreshProjects()
                    }
                }
            }
        )
    }
}

@Composable
fun ProjectDialog(
    project: Project? = null, // Null for Create, non-null for Edit
    onDismiss: () -> Unit,
    onConfirm: (Project) -> Unit
) {
    var name by remember { mutableStateOf(project?.name ?: "") }
    var description by remember { mutableStateOf(project?.description ?: "") }
    var location by remember { mutableStateOf(project?.location ?: "") }
    var startDate by remember { mutableStateOf(project?.startDate ?: "") }
    var endDate by remember { mutableStateOf(project?.endDate ?: "") }
    var isError by remember { mutableStateOf(false) }

    val title = if (project == null) "Add New Project" else "Edit Project"

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Name") })
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Description") })
                OutlinedTextField(value = location, onValueChange = { location = it }, label = { Text("Location") })
                OutlinedTextField(value = startDate, onValueChange = { startDate = it }, label = { Text("Start Date") })
                OutlinedTextField(value = endDate, onValueChange = { endDate = it }, label = { Text("End Date") })
                if (isError) Text("All fields are required.", color = MaterialTheme.colorScheme.error)
            }
        },
        confirmButton = {
            Button(onClick = {
                if (name.isBlank() || description.isBlank() || location.isBlank()) {
                    isError = true
                } else {
                    onConfirm(
                        Project(
                            id = project?.id ?: "",
                            name = name,
                            description = description,
                            location = location,
                            startDate = startDate,
                            endDate = endDate,
                            managerId = project?.managerId ?: "",
                            employeeIds = project?.employeeIds ?: emptyList(),
                            budget = project?.budget ?: 0.0,
                            status = project?.status ?: "",
                            createdAt = project?.createdAt ?: "",
                            updatedAt = project?.updatedAt ?: ""
                        )
                    )
                }
            }) { Text("Save") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}

@Composable
fun ConfirmDeleteDialog(projectName: String, onDismiss: () -> Unit, onConfirm: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Confirm Deletion") },
        text = { Text("Are you sure you want to delete the project '$projectName'? This action cannot be undone.") },
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
fun ProjectList(
    projects: List<Project>,
    onEditClick: (Project) -> Unit,
    onDeleteClick: (Project) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(projects) { project ->
            ProjectListItem(
                project = project,
                onEditClick = { onEditClick(project) },
                onDeleteClick = { onDeleteClick(project) }
            )
        }
    }
}

@Composable
fun ProjectListItem(
    project: Project,
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
    Card(modifier = Modifier.fillMaxWidth(), elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(text = project.name, style = MaterialTheme.typography.titleLarge, modifier = Modifier.weight(1f))
                IconButton(onClick = onEditClick) { Icon(Icons.Default.Edit, "Edit Project") }
                IconButton(onClick = onDeleteClick) { Icon(Icons.Default.Delete, "Delete Project") }
            }
            Text(text = project.description, style = MaterialTheme.typography.bodyMedium)
            Divider(modifier = Modifier.padding(vertical = 8.dp))
            // ... (rest of the item content is the same)
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                Text("Location:", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
                Text(project.location, style = MaterialTheme.typography.bodySmall)
            }
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                Text("Duration:", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
                Text("${project.startDate} to ${project.endDate}", style = MaterialTheme.typography.bodySmall)
            }
        }
    }
}

@Composable
fun ProjectCardGrid(projects: List<Project>, onEditClick: (Project) -> Unit, onDeleteClick: (Project) -> Unit) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(3),
        modifier = Modifier.padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        items(projects) { project ->
            ProjectListItemCard(
                project = project,
                onEditClick = { onEditClick(project) },
                onDeleteClick = { onDeleteClick(project) }
            )
        }
    }
}

@Composable
fun ProjectListItemCard(project: Project, onEditClick: () -> Unit, onDeleteClick: () -> Unit) {
    Card(modifier = Modifier.fillMaxWidth(), elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(text = project.name, style = MaterialTheme.typography.titleLarge, modifier = Modifier.weight(1f))
                IconButton(onClick = onEditClick) { Icon(Icons.Default.Edit, "Edit Project") }
                IconButton(onClick = onDeleteClick) { Icon(Icons.Default.Delete, "Delete Project") }
            }
            Text(text = project.description, style = MaterialTheme.typography.bodyMedium, maxLines = 2)
            Divider(modifier = Modifier.padding(vertical = 8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                Text("Location:", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
                Text(project.location, style = MaterialTheme.typography.bodySmall)
            }
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                Text("Duration:", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
                Text("${project.startDate} to ${project.endDate}", style = MaterialTheme.typography.bodySmall)
            }
        }
    }
}