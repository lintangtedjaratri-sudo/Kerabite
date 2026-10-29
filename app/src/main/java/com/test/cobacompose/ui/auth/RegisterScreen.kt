package com.test.cobacompose.ui.auth

import android.util.Patterns
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.test.cobacompose.R
import com.test.cobacompose.ui.components.AuthFooterPrompt
import com.test.cobacompose.ui.components.AuthPrimaryButton
import com.test.cobacompose.ui.components.AuthScaffold
import com.test.cobacompose.ui.components.AuthTextField
import com.test.cobacompose.ui.components.GoogleAuthButton
import com.test.cobacompose.viewmodel.AuthState
import com.test.cobacompose.viewmodel.AuthViewModel

@Composable
fun RegisterScreen(
    onRegisterSuccess: () -> Unit,
    onLoginClick: () -> Unit,
    viewModel: AuthViewModel = viewModel()
) {
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }

    val authState by viewModel.authState.collectAsState()
    val context = LocalContext.current
    val isLoading = authState is AuthState.Loading

    var emailTouched by remember { mutableStateOf(false) }
    var confirmPasswordTouched by remember { mutableStateOf(false) }
    var submitAttempted by remember { mutableStateOf(false) }

    val isEmailValid = Patterns.EMAIL_ADDRESS
        .matcher(email.trim())
        .matches()

    val isPasswordValid =
        password.length >= 8 &&
                password.any { it.isUpperCase() } &&
                password.any { !it.isLetterOrDigit() }

    val isPasswordMatch = password == confirmPassword

    val passwordError = password.isNotBlank() && !isPasswordValid

    val confirmPasswordError = (confirmPasswordTouched || submitAttempted) &&
            confirmPassword.isNotBlank() &&
            !isPasswordMatch

    val emailError = (emailTouched || submitAttempted) &&
            email.isNotBlank() &&
            !isEmailValid

    LaunchedEffect(authState) {
        if (authState is AuthState.Success) {
            onRegisterSuccess()
            viewModel.resetState()
        }
    }

    AuthScaffold {
        Spacer(modifier = Modifier.height(8.dp))

        AuthTextField(
            value = name,
            onValueChange = { name = it },
            placeholder = stringResource(R.string.auth_username),
            enabled = !isLoading,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text)
        )

        Spacer(modifier = Modifier.height(12.dp))

        AuthTextField(
            value = email,
            onValueChange = {
                email = it
                emailTouched = true
            },
            placeholder = stringResource(R.string.auth_email),
            enabled = !isLoading,
            isError = emailError,
            supportingText = if (emailError) stringResource(R.string.auth_email_invalid) else null,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email)
        )

        Spacer(modifier = Modifier.height(12.dp))

        AuthTextField(
            value = password,
            onValueChange = { password = it },
            placeholder = stringResource(R.string.auth_password),
            enabled = !isLoading,
            isPassword = true,
            isError = passwordError,
            supportingText = if (passwordError) {
                stringResource(R.string.auth_password_rule)
            } else {
                null
            },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password)
        )

        Spacer(modifier = Modifier.height(12.dp))

        AuthTextField(
            value = confirmPassword,
            onValueChange = {
                confirmPassword = it
                confirmPasswordTouched = true
            },
            placeholder = stringResource(R.string.auth_confirm_password),
            enabled = !isLoading,
            isPassword = true,
            isError = confirmPasswordError,
            supportingText = if (confirmPasswordError) {
                stringResource(R.string.auth_password_mismatch)
            } else {
                null
            },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password)
        )

        Spacer(modifier = Modifier.height(16.dp))

        (authState as? AuthState.Error)?.let { error ->
            Text(
                text = stringResource(error.messageRes),
                color = MaterialTheme.colorScheme.error,
                fontSize = 12.sp,
                modifier = Modifier.padding(bottom = 8.dp)
            )
        }

        AuthPrimaryButton(
            onClick = {
                submitAttempted = true

                if (isEmailValid && isPasswordValid && isPasswordMatch) {
                    viewModel.register(
                        name.trim(),
                        email.trim(),
                        password
                    )
                }
            },
            enabled = !isLoading &&
                    name.isNotBlank() &&
                    email.isNotBlank() &&
                    password.isNotBlank() &&
                    confirmPassword.isNotBlank()
        ) {
            Text(
                text = stringResource(if (isLoading) R.string.auth_registering else R.string.auth_register_button),
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        GoogleAuthButton(
            text = stringResource(R.string.auth_register_google),
            onClick = { viewModel.authenticateWithGoogle(context) },
            enabled = !isLoading
        )

        Spacer(modifier = Modifier.weight(1f))

        AuthFooterPrompt(
            question = stringResource(R.string.auth_have_account),
            actionText = stringResource(R.string.auth_login),
            onActionClick = onLoginClick,
            enabled = !isLoading
        )
    }
}
