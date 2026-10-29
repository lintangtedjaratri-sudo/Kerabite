package com.test.cobacompose.ui.auth

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.test.cobacompose.R
import com.test.cobacompose.ui.components.AuthFooterPrompt
import com.test.cobacompose.ui.components.AuthPrimaryButton
import com.test.cobacompose.ui.components.AuthScaffold
import com.test.cobacompose.ui.components.AuthTextField
import com.test.cobacompose.ui.components.AuthTextFieldDefaults
import com.test.cobacompose.ui.components.GoogleAuthButton
import com.test.cobacompose.viewmodel.AuthState
import com.test.cobacompose.viewmodel.AuthViewModel

@Composable
fun LoginScreen(
    onLoginSuccess: () -> Unit,
    onRegisterClick: () -> Unit,
    onForgotPasswordClick: () -> Unit,
    viewModel: AuthViewModel = viewModel()
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    val authState by viewModel.authState.collectAsState()
    val context = LocalContext.current
    val isLoading = authState is AuthState.Loading

    LaunchedEffect(authState) {
        if (authState is AuthState.Success) {
            viewModel.resetState()
            onLoginSuccess()
        }
    }

    AuthScaffold {
        Spacer(modifier = Modifier.height(16.dp))

        AuthTextField(
            value = email,
            onValueChange = { email = it },
            placeholder = stringResource(R.string.auth_email),
            enabled = !isLoading,
            colors = AuthTextFieldDefaults.plainColors()
        )

        Spacer(modifier = Modifier.height(12.dp))

        AuthTextField(
            value = password,
            onValueChange = { password = it },
            placeholder = stringResource(R.string.auth_password),
            enabled = !isLoading,
            isPassword = true,
            colors = AuthTextFieldDefaults.plainColors()
        )

        // Lupa password
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End
        ) {
            TextButton(
                onClick = onForgotPasswordClick,
                enabled = !isLoading
            ) {
                Text(
                    text = stringResource(R.string.auth_forgot_link),
                    color = Color.White,
                    fontSize = 12.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        AuthPrimaryButton(
            onClick = {
                viewModel.login(
                    email.trim(),
                    password
                )
            },
            enabled = email.isNotBlank() &&
                    password.isNotBlank() &&
                    !isLoading
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(20.dp),
                    color = Color.White,
                    strokeWidth = 2.dp
                )
            } else {
                Text(
                    text = stringResource(R.string.auth_login_button),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        (authState as? AuthState.Error)?.let { error ->
            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = stringResource(error.messageRes),
                color = Color.Red,
                fontSize = 12.sp
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        GoogleAuthButton(
            text = stringResource(R.string.auth_login_google),
            onClick = { viewModel.authenticateWithGoogle(context) },
            enabled = !isLoading
        )

        Spacer(modifier = Modifier.weight(1f))

        AuthFooterPrompt(
            question = stringResource(R.string.auth_no_account),
            actionText = stringResource(R.string.auth_register),
            onActionClick = onRegisterClick,
            enabled = !isLoading
        )
    }
}
