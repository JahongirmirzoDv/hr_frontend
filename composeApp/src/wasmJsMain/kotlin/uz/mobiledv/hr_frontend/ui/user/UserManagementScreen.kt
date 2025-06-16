package uz.mobiledv.hr_frontend.ui.user

import uz.mobiledv.hr_frontend.data.ApiService
import uz.mobiledv.hr_frontend.data.HrRepository
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import uz.mobiledv.hr_frontend.data.remote.CreateUserRequest
import uz.mobiledv.hr_frontend.data.remote.UpdateUserRequest
import uz.mobiledv.hr_frontend.data.remote.UserResponse

@Composable
fun UserManagementScreen(repository: HrRepository, token: String) {
    var users by remember { mutableStateOf<List<UserResponse>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    // Dialog states
    var showCreateDialog by remember { mutableStateOf(false) }
    var userToEdit by remember { mutableStateOf<UserResponse?>(null) }
    var userToDelete by remember { mutableStateOf<UserResponse?>(null) }

    val coroutineScope = rememberCoroutineScope()

    fun refreshUsers() {
        isLoading = true
        errorMessage = null
        coroutineScope.launch {
            try {
                users = repository.getUsers(token) ?: emptyList()
            } catch (e: Exception) {
                errorMessage = "Failed to load users: ${e.message}"
            } finally {
                isLoading = false
            }
        }
    }

    LaunchedEffect(Unit) { refreshUsers() }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(onClick = { showCreateDialog = true }) {
                Icon(Icons.Default.Add, contentDescription = "Add User")
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier.fillMaxSize().padding(paddingValues),
            contentAlignment = Alignment.Center
        ) {
            when {
                isLoading -> CircularProgressIndicator()
                errorMessage != null -> Text(errorMessage!!, color = MaterialTheme.colorScheme.error)
                users.isEmpty() -> Text("No users found.")
                else -> UserList(
                    users = users,
                    onEditClick = { userToEdit = it },
                    onDeleteClick = { userToDelete = it }
                )
            }
        }
    }

    // --- DIALOGS ---
    if (showCreateDialog) {
        UserDialog(
            onDismiss = { showCreateDialog = false },
            onConfirmCreate = { newUserRequest ->
                coroutineScope.launch {
                    repository.createUser(token, newUserRequest)?.let {
                        showCreateDialog = false
                        refreshUsers()
                    }
                }
            }
        )
    }

    userToEdit?.let { user ->
        UserDialog(
            user = user,
            onDismiss = { userToEdit = null },
            onConfirmUpdate = { updatedUserRequest ->
                coroutineScope.launch {
                    repository.updateUser(token, user.id, updatedUserRequest)?.let {
                        userToEdit = null
                        refreshUsers()
                    }
                }
            }
        )
    }

    userToDelete?.let { user ->
        ConfirmDeleteDialog(
            userName = user.fullName,
            onDismiss = { userToDelete = null },
            onConfirm = {
                coroutineScope.launch {
                    if (repository.deleteUser(token, user.id)) {
                        userToDelete = null
                        refreshUsers()
                    }
                }
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserDialog(
    user: UserResponse? = null,
    onDismiss: () -> Unit,
    onConfirmCreate: ((CreateUserRequest) -> Unit)? = null,
    onConfirmUpdate: ((UpdateUserRequest) -> Unit)? = null
) {
    var fullName by remember { mutableStateOf(user?.fullName ?: "") }
    var email by remember { mutableStateOf(user?.email ?: "") }
    var password by remember { mutableStateOf("") }
    var selectedRole by remember { mutableStateOf(user?.role ?: "User") }

    val roles = listOf("Admin", "Manager", "User")
    var isDropdownExpanded by remember { mutableStateOf(false) }
    var isError by remember { mutableStateOf(false) }

    val title = if (user == null) "Add New User" else "Edit User"

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(value = fullName, onValueChange = { fullName = it }, label = { Text("Full Name") })
                OutlinedTextField(value = email, onValueChange = { email = it }, label = { Text("Email") })
                if (user == null) { // Only show password field for new users
                    OutlinedTextField(
                        value = password,
                        onValueChange = { password = it },
                        label = { Text("Password") },
                        visualTransformation = PasswordVisualTransformation()
                    )
                }
                ExposedDropdownMenuBox(expanded = isDropdownExpanded, onExpandedChange = { isDropdownExpanded = !isDropdownExpanded }) {
                    OutlinedTextField(
                        value = selectedRole,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Role") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = isDropdownExpanded) },
                        modifier = Modifier.menuAnchor()
                    )
                    ExposedDropdownMenu(expanded = isDropdownExpanded, onDismissRequest = { isDropdownExpanded = false }) {
                        roles.forEach { role ->
                            DropdownMenuItem(text = { Text(role) }, onClick = {
                                selectedRole = role
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
                if (fullName.isBlank() || email.isBlank()) {
                    isError = true
                } else {
                    if (user == null) { // Create Mode
                        if (password.isBlank()) {
                            isError = true
                        } else {
                            onConfirmCreate?.invoke(CreateUserRequest(fullName, email, selectedRole.uppercase(), password))
                        }
                    } else { // Edit Mode
                        onConfirmUpdate?.invoke(UpdateUserRequest(fullName, email, selectedRole.uppercase()))
                    }
                }
            }) { Text("Save") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}

@Composable
fun ConfirmDeleteDialog(userName: String, onDismiss: () -> Unit, onConfirm: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Confirm Deletion") },
        text = { Text("Are you sure you want to delete the user '$userName'?") },
        confirmButton = {
            Button(onClick = onConfirm, colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)) { Text("Delete") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}

// UserList and UserListItem functions remain the same as the previous step, but are included here for completeness.

@Composable
fun UserList(users: List<UserResponse>, onEditClick: (UserResponse) -> Unit, onDeleteClick: (UserResponse) -> Unit) {
    LazyColumn(modifier = Modifier.fillMaxSize(), contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        items(users) { user ->
            UserListItem(user = user, onEditClick = { onEditClick(user) }, onDeleteClick = { onDeleteClick(user) })
        }
    }
}

@Composable
fun UserListItem(user: UserResponse, onEditClick: () -> Unit, onDeleteClick: () -> Unit) {
    Card(modifier = Modifier.fillMaxWidth(), elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(user.fullName, style = MaterialTheme.typography.titleMedium)
                Text(user.email, style = MaterialTheme.typography.bodyMedium)
                Text("Role: ${user.role}", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
            }
            Row {
                IconButton(onClick = onEditClick) { Icon(Icons.Default.Edit, "Edit User") }
                IconButton(onClick = onDeleteClick) { Icon(Icons.Default.Delete, "Delete User") }
            }
        }
    }
}