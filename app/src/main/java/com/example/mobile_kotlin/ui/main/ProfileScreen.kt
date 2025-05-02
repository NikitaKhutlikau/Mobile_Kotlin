package com.example.mobile_kotlin.ui.main

import android.icu.text.SimpleDateFormat
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.PersonOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.mobile_kotlin.R
import com.example.mobile_kotlin.data.model.User
import com.example.mobile_kotlin.ui.components.FullScreenLoader
import com.example.mobile_kotlin.ui.components.NavBar
import com.example.mobile_kotlin.ui.utils.ErrorMessage
import com.example.mobile_kotlin.ui.utils.UiState
import com.example.mobile_kotlin.ui.utils.toFormattedDate
import com.example.mobile_kotlin.viewmodels.ProfileViewModel
import com.google.firebase.Timestamp
import com.google.type.Date

@Composable
fun ProfileScreen(navController: NavController) {
    val profileViewModel: ProfileViewModel = hiltViewModel()
    val userState by profileViewModel.userState.collectAsState()
    var showEditDialog by remember { mutableStateOf(false) }
    var showDeleteConfirm by remember { mutableStateOf(false) }

    Scaffold(
        bottomBar = { NavBar(navController) }
    ) { padding ->
        Box(Modifier.padding(padding)) {
            when (val state = userState) {
                is UiState.Loading -> FullScreenLoader()
                is UiState.Success -> {
                    if (state.data.id.isEmpty()) EmptyProfile()
                    else ProfileContent(
                        user = state.data,
                        onEdit = { showEditDialog = true },
                        onLogout = { profileViewModel.logout { navController.navigate("login") } },
                        onDelete = { showDeleteConfirm = true }
                    )
                }
                is UiState.Empty -> EmptyProfile()
                is UiState.Error -> ErrorMessage(state.message)
            }
        }
    }

    if (showEditDialog) {
        EditProfileDialog(
            user = (userState as? UiState.Success)?.data ?: User(),
            onDismiss = { showEditDialog = false },
            onSave = { updatedUser ->
                profileViewModel.updateProfile(updatedUser)
                showEditDialog = false
            }
        )
    }

    if (showDeleteConfirm) {
        DeleteConfirmationDialog(
            onConfirm = {
                profileViewModel.deleteAccount { navController.navigate("login") }
                showDeleteConfirm = false
            },
            onDismiss = { showDeleteConfirm = false }
        )
    }
}

@Composable
private fun EmptyProfile() {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = Icons.Outlined.PersonOff,
            contentDescription = null,
            modifier = Modifier.size(64.dp)
        )
        Text("Профиль не найден", style = MaterialTheme.typography.titleMedium)
    }
}


@Composable
private fun ProfileContent(
    user: User,
    onEdit: () -> Unit,
    onLogout: () -> Unit,
    onDelete: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        AsyncImage(
            model = user.avatarUrl,
            contentDescription = "Аватар",
            modifier = Modifier
                .size(120.dp)
                .clip(CircleShape)
        )

        Spacer(Modifier.height(16.dp))

        UserInfo(user)

        Spacer(Modifier.height(24.dp))

        ActionButtons(
            onEditClick = onEdit,
            onLogoutClick = onLogout,
            onDeleteClick = onDelete
        )
    }
}

@Composable
private fun UserInfoSection(user: User) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 24.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        InfoRow("Имя пользователя", user.username)
        InfoRow("Email", user.email)
        InfoRow("Дата регистрации", user.registrationDate.toFormattedDate())
        InfoRow("Страна", user.country)
        InfoRow("О себе", user.bio.ifEmpty { "Не указано" })
    }
}

@Composable
fun InfoRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, style = MaterialTheme.typography.bodyLarge)
        Text(value, style = MaterialTheme.typography.bodyMedium)
    }
}

@Composable
private fun ActionButtons(
    onEditClick: () -> Unit,
    onLogoutClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Button(
            onClick = onEditClick,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Редактировать профиль")
        }

        OutlinedButton(
            onClick = onLogoutClick,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Выйти из аккаунта")
        }

        TextButton(
            onClick = onDeleteClick,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Удалить аккаунт", color = MaterialTheme.colorScheme.error)
        }
    }
}

@Composable
private fun EditProfileDialog(
    user: User,
    onDismiss: () -> Unit,
    onSave: (User) -> Unit
) {
    var editedUser by remember { mutableStateOf(user) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Редактирование профиля") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = editedUser.username,
                    onValueChange = { editedUser = editedUser.copy(username = it) },
                    label = { Text("Имя пользователя") },
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = editedUser.bio,
                    onValueChange = { editedUser = editedUser.copy(bio = it) },
                    label = { Text("О себе") },
                    modifier = Modifier.fillMaxWidth(),
                    maxLines = 3
                )

                // Добавьте остальные поля по аналогии
            }
        },
        confirmButton = {
            Button(onClick = { onSave(editedUser) }) {
                Text("Сохранить")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Отмена")
            }
        }
    )
}

@Composable
private fun DeleteConfirmationDialog(
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Удаление аккаунта") },
        text = { Text("Вы уверены, что хотите удалить аккаунт без возможности восстановления?") },
        confirmButton = {
            TextButton(
                onClick = onConfirm,
                colors = ButtonDefaults.textButtonColors(contentColor = Color.Red)
            ) {
                Text("Удалить")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Отмена")
            }
        }
    )
}

@Composable
private fun UserInfo(user: User) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        InfoRow("Имя пользователя", user.username)
        InfoRow("Email", user.email)
        InfoRow("Дата рождения", user.birthDate.toFormattedDate())
        InfoRow("Страна", user.country)
        InfoRow("О себе", user.bio.ifEmpty { "Не указано" })
    }
}