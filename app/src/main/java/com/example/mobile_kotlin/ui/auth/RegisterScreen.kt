import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.mobile_kotlin.data.model.User
import com.example.mobile_kotlin.ui.components.FullScreenLoader
import com.example.mobile_kotlin.ui.utils.ErrorMessage
import com.example.mobile_kotlin.ui.utils.UiState
import com.example.mobile_kotlin.viewmodels.AuthViewModel

@Composable
fun RegisterScreen(
    onRegisterSuccess: () -> Unit,
    onLoginClick: () -> Unit,
    viewModel: AuthViewModel
) {

    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var username by remember { mutableStateOf("") }
    val state by viewModel.registrationState.collectAsState()

    LaunchedEffect(state) {
        if (state is UiState.Success) {
            onRegisterSuccess()
        }
    }

    Column(modifier = Modifier.padding(16.dp)) {
        when (state) {
            UiState.Loading -> FullScreenLoader()
            is UiState.Error -> ErrorMessage((state as UiState.Error).message)
            else -> {
                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("Email") },
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = { Text("Пароль") },
                    visualTransformation = PasswordVisualTransformation(),
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = confirmPassword,
                    onValueChange = { confirmPassword = it },
                    label = { Text("Подтвердите пароль") },
                    visualTransformation = PasswordVisualTransformation(),
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = username,
                    onValueChange = { username = it },
                    label = { Text("Имя пользователя") },
                    modifier = Modifier.fillMaxWidth()
                )

                Button(
                    onClick = {
                        if (validateInput(email, password, confirmPassword, username)) {
                            viewModel.register(
                                email = email,
                                password = password,
                                user = User(username = username, email = email)
                            )
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Зарегистрироваться")
                }

                TextButton(
                    onClick = onLoginClick,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Уже есть аккаунт? Войдите")
                }
            }
        }
    }
}

private fun validateInput(
    email: String,
    password: String,
    confirmPassword: String,
    username: String
): Boolean {
    return when {
        email.isBlank() -> false
        password != confirmPassword -> false
        password.length < 6 -> false
        username.isBlank() -> false
        else -> true
    }
}