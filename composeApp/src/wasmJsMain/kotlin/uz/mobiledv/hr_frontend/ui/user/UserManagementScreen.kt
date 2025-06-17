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
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import org.koin.compose.viewmodel.koinViewModel
import uz.mobiledv.hr_frontend.data.remote.CreateUserRequest
import uz.mobiledv.hr_frontend.data.remote.UpdateUserRequest
import uz.mobiledv.hr_frontend.data.remote.UserResponse
import uz.mobiledv.hr_frontend.ui.employee.FilterDropdown
import uz.mobiledv.hr_frontend.ui.employee.PaginationControls
import uz.mobiledv.hr_frontend.vm.UserManagementViewModel
import kotlin.math.ceil

@Composable
fun UserManagementScreen(
    viewModel: UserManagementViewModel = koinViewModel(),
    token: String
) {
    val allUsers by viewModel.allUsers
    var isLoading by viewModel.isLoading
    var errorMessage by viewModel.errorMessage
    var showCreateDialog by remember { mutableStateOf(false) }
    var userToEdit by remember { mutableStateOf<UserResponse?>(null) }
    var userToDelete by remember { mutableStateOf<UserResponse?>(null) }

    LaunchedEffect(token) { viewModel.refreshUsers(token) }

    // State for UI controls
    var searchQuery by remember { mutableStateOf("") }
    var roleFilter by remember { mutableStateOf("All") }
    val pageSize = 10
    var currentPage by remember { mutableStateOf(1) }


    val filteredUsers = remember(searchQuery, roleFilter, allUsers) {
        allUsers.filter {
            it.fullName.contains(searchQuery, ignoreCase = true) || it.email.contains(searchQuery, ignoreCase = true)
        }.filter {
            if (roleFilter == "All") true else it.role.equals(roleFilter, ignoreCase = true)
        }
    }

    val totalPages = ceil(filteredUsers.size.toFloat() / pageSize).toInt()
    val paginatedUsers = filteredUsers.chunked(pageSize).getOrNull(currentPage - 1) ?: emptyList()


    Column(modifier = Modifier.fillMaxSize().padding(horizontal = 48.dp, vertical = 24.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                "User Management",
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.weight(1f)
            )
            Button(onClick = { showCreateDialog = true }) {
                Icon(Icons.Default.Add, "Add User", Modifier.size(18.dp))
                Spacer(Modifier.width(8.dp))
                Text("Add User")
            }
        }
        Spacer(Modifier.height(24.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier.weight(1f),
                label = { Text("Search by name or email...") },
                leadingIcon = { Icon(Icons.Default.Search, "Search") })
            FilterDropdown(
                label = "Role",
                options = listOf("All", "Admin", "Manager", "User"),
                selectedOption = roleFilter,
                onOptionSelected = { roleFilter = it })
        }
        Spacer(Modifier.height(24.dp))

        Card(modifier = Modifier.fillMaxSize()) {
            Box(modifier = Modifier.fillMaxSize()) {
                when {
                    isLoading -> CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                    errorMessage != null -> Text(
                        errorMessage!!,
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.align(Alignment.Center)
                    )

                    paginatedUsers.isEmpty() -> Text(
                        "No users match your search.",
                        modifier = Modifier.align(Alignment.Center)
                    )

                    else -> Column {
                        UserList(
                            users = paginatedUsers,
                            onEditClick = { userToEdit = it },
                            onDeleteClick = { userToDelete = it })
                        Spacer(Modifier.weight(1f))
                        PaginationControls(
                            currentPage = currentPage,
                            totalPages = totalPages,
                            onPageChange = { currentPage = it })
                    }
                }
            }
        }
    }

    val coroutineScope = rememberCoroutineScope()

    // --- DIALOGS ---
    if (showCreateDialog) {
        UserDialog(
            onDismiss = { showCreateDialog = false },
            onConfirmCreate = { newUserRequest ->
                coroutineScope.launch {
                    viewModel.createUser(token,newUserRequest){
                        showCreateDialog = false
                        viewModel.refreshUsers(token)
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
                    viewModel.updateUser(token,user.id,updatedUserRequest){
                        userToEdit = null
                        viewModel.refreshUsers(token)
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
                    viewModel.deleteUser(token, user.id) {
                        userToDelete = null
                        viewModel.refreshUsers(token)
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
                ExposedDropdownMenuBox(
                    expanded = isDropdownExpanded,
                    onExpandedChange = { isDropdownExpanded = !isDropdownExpanded }) {
                    OutlinedTextField(
                        value = selectedRole,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Role") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = isDropdownExpanded) },
                        modifier = Modifier.menuAnchor()
                    )
                    ExposedDropdownMenu(
                        expanded = isDropdownExpanded,
                        onDismissRequest = { isDropdownExpanded = false }) {
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
                            onConfirmCreate?.invoke(
                                CreateUserRequest(
                                    fullName,
                                    email,
                                    selectedRole.uppercase(),
                                    password
                                )
                            )
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
            Button(
                onClick = onConfirm,
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
            ) { Text("Delete") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}

// UserList and UserListItem functions remain the same as the previous step, but are included here for completeness.
@Composable
fun UserList(users: List<UserResponse>, onEditClick: (UserResponse) -> Unit, onDeleteClick: (UserResponse) -> Unit) {
    LazyColumn(modifier = Modifier.fillMaxSize()) {
        item {
            Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp)) {
                Text(
                    "FULL NAME",
                    modifier = Modifier.weight(1f),
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    "EMAIL",
                    modifier = Modifier.weight(1f),
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    "ROLE",
                    modifier = Modifier.weight(0.5f),
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    "ACTIONS",
                    modifier = Modifier.width(100.dp),
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
            }
            Divider()
        }
        items(users) { user ->
            UserListItem(user = user, onEditClick = { onEditClick(user) }, onDeleteClick = { onDeleteClick(user) })
            Divider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f))
        }
    }
}

@Composable
fun UserListItem(user: UserResponse, onEditClick: () -> Unit, onDeleteClick: () -> Unit) {
    Row(
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(user.fullName, modifier = Modifier.weight(1f), style = MaterialTheme.typography.bodyLarge)
        Text(user.email, modifier = Modifier.weight(1f), style = MaterialTheme.typography.bodyLarge)
        Text(user.role, modifier = Modifier.weight(0.5f), style = MaterialTheme.typography.bodyLarge)
        Row(modifier = Modifier.width(100.dp), horizontalArrangement = Arrangement.Center) {
            IconButton(onClick = onEditClick) { Icon(Icons.Default.Edit, "Edit User") }
            IconButton(onClick = onDeleteClick) { Icon(Icons.Default.Delete, "Delete User") }
        }
    }
}