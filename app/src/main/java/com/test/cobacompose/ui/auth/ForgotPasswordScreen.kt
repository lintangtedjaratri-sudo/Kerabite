package com.test.cobacompose.ui.auth

import android.util.Patterns
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.test.cobacompose.R
import com.test.cobacompose.ui.components.AuthPrimaryButton
import com.test.cobacompose.ui.components.AuthScaffold
import com.test.cobacompose.ui.components.AuthTextField
import com.test.cobacompose.viewmodel.AuthState
import com.test.cobacompose.viewmodel.AuthViewModel

@Composable
fun ForgotPasswordScreen(
    viewModel: AuthViewModel = viewModel()
) {
    var email by remember { mutableStateOf("") }
    var emailTouched by remember { mutableStateOf(false) }

    val authState by viewModel.authState.collectAsState()
    val isLoading = authState is AuthState.Loading

    val isEmailValid = Patterns.EMAIL_ADDRESS
        .matcher(email.trim())
        .matches()

    val emailError = emailTouched &&
            email.isNotBlank() &&
            !isEmailValid

    LaunchedEffect(authState) {
        if (authState is AuthState.Success) {
            viewModel.resetState()
        }
    }

    AuthScaffold {
        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = stringResource(R.string.auth_forgot_title),
            color = Color.Black,
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(20.dp))

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

        Spacer(modifier = Modifier.height(20.dp))

        AuthPrimaryButton(
            onClick = { viewModel.sendPasswordResetEmail(email.trim()) },
            enabled = isEmailValid && !isLoading
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(20.dp),
                    color = Color.White,
                    strokeWidth = 2.dp
                )
            } else {
                Text(
                    text = stringResource(R.string.auth_send_reset),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        (authState as? AuthState.Success)?.let { success ->
            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = stringResource(success.messageRes),
                color = Color.White,
                fontSize = 12.sp
            )
        }

        (authState as? AuthState.Error)?.let { error ->
            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = stringResource(error.messageRes),
                color = Color(0xFFFFCDD2),
                fontSize = 12.sp
            )
        }
    }
}
