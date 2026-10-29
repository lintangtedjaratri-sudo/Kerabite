package com.test.cobacompose.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextFieldColors
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.test.cobacompose.R
import com.test.cobacompose.ui.theme.InputDisabled
import com.test.cobacompose.ui.theme.InputError
import com.test.cobacompose.ui.theme.InputIcon
import com.test.cobacompose.ui.theme.InputPlaceholder
import com.test.cobacompose.ui.theme.InputText
import com.test.cobacompose.ui.theme.KeraPurple
import com.test.cobacompose.ui.theme.KeraPurpleDark

/**
 * Kerangka layar auth (login, register, lupa password): background, overlay ungu,
 * area konten di luar navigation bar, dan logo KeraBite di atas.
 */
@Composable
fun AuthScaffold(
    content: @Composable ColumnScope.() -> Unit
) {
    // Background putih untuk system bar
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        Image(
            painter = painterResource(R.drawable.bg_main),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )

        // Overlay ungu 65%
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(KeraPurpleDark.copy(alpha = 0.65f))
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .windowInsetsPadding(WindowInsets.navigationBars)
                .padding(
                    horizontal = 64.dp,
                    vertical = 24.dp
                ),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Image(
                painter = painterResource(R.drawable.logo_kerabite),
                contentDescription = "KeraBite",
                modifier = Modifier.size(150.dp),
                contentScale = ContentScale.Fit
            )

            content()
        }
    }
}

object AuthTextFieldDefaults {

    /** Field dengan border saat fokus/error (register, lupa password). */
    @Composable
    fun formColors(): TextFieldColors = OutlinedTextFieldDefaults.colors(
        focusedContainerColor = Color.White,
        unfocusedContainerColor = Color.White,
        disabledContainerColor = InputDisabled,
        errorContainerColor = Color.White,

        focusedBorderColor = KeraPurple,
        unfocusedBorderColor = Color.Transparent,
        disabledBorderColor = Color.Transparent,
        errorBorderColor = InputError,

        focusedTextColor = InputText,
        unfocusedTextColor = InputText,
        errorTextColor = InputText,

        cursorColor = KeraPurple,
        errorCursorColor = InputError
    )

    /** Field polos tanpa border (login). */
    @Composable
    fun plainColors(): TextFieldColors = OutlinedTextFieldDefaults.colors(
        focusedContainerColor = Color.White,
        unfocusedContainerColor = Color.White,
        disabledContainerColor = Color.White,
        focusedBorderColor = Color.Transparent,
        unfocusedBorderColor = Color.Transparent,
        disabledBorderColor = Color.Transparent
    )
}

@Composable
fun AuthTextField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    enabled: Boolean,
    isPassword: Boolean = false,
    isError: Boolean = false,
    supportingText: String? = null,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    colors: TextFieldColors = AuthTextFieldDefaults.formColors()
) {
    var passwordVisible by remember { mutableStateOf(false) }

    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        placeholder = {
            Text(
                text = placeholder,
                color = InputPlaceholder,
                fontSize = 14.sp
            )
        },
        // Tinggi tidak dipatok: teks bantuan panjang bisa turun ke baris kedua (layar sempit
        // atau ukuran font sistem besar) dan tinggi tetap membuat isian terpotong
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 58.dp),
        shape = RoundedCornerShape(10.dp),
        singleLine = true,
        visualTransformation = if (isPassword && !passwordVisible) {
            PasswordVisualTransformation()
        } else {
            VisualTransformation.None
        },
        trailingIcon = if (isPassword) {
            {
                IconButton(
                    onClick = {
                        passwordVisible = !passwordVisible
                    }
                ) {
                    // Ikon mengikuti keadaan: mata terbuka ungu = password terlihat,
                    // mata dicoret abu-abu = password tersembunyi
                    Icon(
                        painter = if (passwordVisible) {
                            painterResource(R.drawable.ic_visibility)
                        } else {
                            painterResource(R.drawable.ic_visibility_off)
                        },
                        contentDescription = if (passwordVisible) {
                            stringResource(R.string.auth_hide_password)
                        } else {
                            stringResource(R.string.auth_show_password)
                        },
                        tint = if (passwordVisible) KeraPurple else InputIcon
                    )
                }
            }
        } else {
            null
        },
        enabled = enabled,
        isError = isError,
        supportingText = supportingText?.let {
            {
                Text(
                    text = it,
                    fontSize = 11.sp
                )
            }
        },
        keyboardOptions = keyboardOptions,
        colors = colors
    )
}

@Composable
fun AuthPrimaryButton(
    onClick: () -> Unit,
    enabled: Boolean,
    content: @Composable RowScope.() -> Unit
) {
    Button(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = KeraPurple,
            contentColor = Color.White,
            disabledContainerColor = KeraPurple,
            disabledContentColor = Color.White
        ),
        enabled = enabled,
        content = content
    )
}

@Composable
fun GoogleAuthButton(
    text: String,
    onClick: () -> Unit,
    enabled: Boolean
) {
    OutlinedButton(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(10.dp),
        enabled = enabled,
        colors = ButtonDefaults.outlinedButtonColors(
            containerColor = Color.White,
            contentColor = KeraPurple
        ),
        border = BorderStroke(
            width = 1.dp,
            color = KeraPurple
        )
    ) {
        Text(
            text = text,
            fontSize = 13.sp
        )
    }
}

/** Baris "Belum punya akun? Daftar" / "Sudah punya akun? Masuk". */
@Composable
fun AuthFooterPrompt(
    question: String,
    actionText: String,
    onActionClick: () -> Unit,
    enabled: Boolean
) {
    Row(
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = question,
            fontSize = 12.sp,
            color = Color.White
        )

        TextButton(
            onClick = onActionClick,
            enabled = enabled
        ) {
            Text(
                text = actionText,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        }
    }
}
