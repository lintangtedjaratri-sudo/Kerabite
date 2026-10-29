package com.test.cobacompose.ui.profile

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.annotation.DrawableRes
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.net.toUri
import com.test.cobacompose.R
import com.test.cobacompose.data.settings.AppLanguage
import com.test.cobacompose.ui.components.AuthTextField
import com.test.cobacompose.viewmodel.AuthState
import com.test.cobacompose.ui.theme.KeraLavender
import com.test.cobacompose.ui.theme.KeraLavenderDeep
import com.test.cobacompose.ui.theme.KeraLavenderLight
import com.test.cobacompose.ui.theme.KeraPlum
import com.test.cobacompose.ui.theme.KeraProfileBackground
import com.test.cobacompose.ui.theme.KeraProfileCard
import com.test.cobacompose.ui.theme.KeraProfileDarkBackground
import com.test.cobacompose.ui.theme.KeraProfileDarkCard
import com.test.cobacompose.ui.theme.KeraProfileDarkDivider
import com.test.cobacompose.ui.theme.KeraProfileDarkHeader
import com.test.cobacompose.ui.theme.KeraProfileDarkLogoutContainer
import com.test.cobacompose.ui.theme.KeraProfileDarkSubtext
import com.test.cobacompose.ui.theme.KeraProfileDarkText
import com.test.cobacompose.ui.theme.KeraProfileDivider
import com.test.cobacompose.ui.theme.KeraProfileLogout
import com.test.cobacompose.ui.theme.KeraProfileLogoutContainer
import com.test.cobacompose.ui.theme.KeraProfileSubtext
import com.test.cobacompose.ui.theme.KeraProfileText
import com.test.cobacompose.ui.theme.KeraPurple

/** Nomor WhatsApp Pusat Bantuan (0812-4679-2343) dalam format internasional untuk wa.me. */
private const val HELP_WHATSAPP_NUMBER = "6281246792343"

/** Warna drawer; versi gelap dipakai saat Mode Gelap aktif. */
private data class ProfileColors(
    val background: Color,
    val headerTop: Color,
    val headerBottom: Color,
    val card: Color,
    val divider: Color,
    val text: Color,
    val subtext: Color,
    val accent: Color,
    val iconTile: Color,
    val logoutContainer: Color
)

private val LightProfileColors = ProfileColors(
    background = KeraProfileBackground,
    headerTop = KeraLavender,
    headerBottom = KeraLavenderLight,
    card = KeraProfileCard,
    divider = KeraProfileDivider,
    text = KeraProfileText,
    subtext = KeraProfileSubtext,
    accent = KeraPurple,
    iconTile = KeraLavenderLight,
    logoutContainer = KeraProfileLogoutContainer
)

private val DarkProfileColors = ProfileColors(
    background = KeraProfileDarkBackground,
    headerTop = KeraProfileDarkHeader,
    headerBottom = KeraProfileDarkBackground,
    card = KeraProfileDarkCard,
    divider = KeraProfileDarkDivider,
    text = KeraProfileDarkText,
    subtext = KeraProfileDarkSubtext,
    accent = KeraLavender,
    iconTile = KeraProfileDarkHeader,
    logoutContainer = KeraProfileDarkLogoutContainer
)

/** Teks drawer dan dialognya dalam bahasa yang dipilih. */
private data class ProfileStrings(
    val guest: String,
    val personalization: String,
    val app: String,
    val darkMode: String,
    val darkModeOn: String,
    val darkModeOff: String,
    val language: String,
    val changePassword: String,
    val changePasswordHint: String,
    val about: String,
    val aboutHint: String,
    val aboutDescription: String,
    val help: String,
    val helpHint: String,
    val helpMessage: String,
    val noWhatsApp: String,
    val logout: String,
    val logoutConfirmTitle: String,
    val logoutConfirmText: String,
    val cancel: String,
    val save: String,
    val close: String,
    val chooseLanguage: String,
    val version: String,
    val googleAccountInfo: String,
    val currentPassword: String,
    val newPassword: String,
    val confirmPassword: String,
    val errorFillAll: String,
    val errorMinLength: String,
    val errorMismatch: String,
    val errorSame: String,
    val passwordChanged: String
)

/** Teks drawer dari resource string, jadi mengikuti bahasa aplikasi. */
@Composable
private fun profileStrings() = ProfileStrings(
    guest = stringResource(R.string.profile_guest),
    personalization = stringResource(R.string.profile_personalization),
    app = stringResource(R.string.profile_app),
    darkMode = stringResource(R.string.profile_dark_mode),
    darkModeOn = stringResource(R.string.profile_dark_mode_on),
    darkModeOff = stringResource(R.string.profile_dark_mode_off),
    language = stringResource(R.string.profile_language),
    changePassword = stringResource(R.string.profile_change_password),
    changePasswordHint = stringResource(R.string.profile_change_password_hint),
    about = stringResource(R.string.profile_about),
    aboutHint = stringResource(R.string.profile_about_hint),
    aboutDescription = stringResource(R.string.profile_about_description),
    help = stringResource(R.string.profile_help),
    helpHint = stringResource(R.string.profile_help_hint),
    helpMessage = stringResource(R.string.profile_help_message),
    noWhatsApp = stringResource(R.string.profile_no_whats_app),
    logout = stringResource(R.string.profile_logout),
    logoutConfirmTitle = stringResource(R.string.profile_logout_confirm_title),
    logoutConfirmText = stringResource(R.string.profile_logout_confirm_text),
    cancel = stringResource(R.string.profile_cancel),
    save = stringResource(R.string.profile_save),
    close = stringResource(R.string.profile_close),
    chooseLanguage = stringResource(R.string.profile_choose_language),
    version = stringResource(R.string.profile_version),
    googleAccountInfo = stringResource(R.string.profile_google_account_info),
    currentPassword = stringResource(R.string.profile_current_password),
    newPassword = stringResource(R.string.profile_new_password),
    confirmPassword = stringResource(R.string.profile_confirm_password),
    errorFillAll = stringResource(R.string.profile_error_fill_all),
    errorMinLength = stringResource(R.string.profile_error_min_length),
    errorMismatch = stringResource(R.string.profile_error_mismatch),
    errorSame = stringResource(R.string.profile_error_same),
    passwordChanged = stringResource(R.string.profile_password_changed)
)

/**
 * Isi drawer Profil yang terbuka dari ikon hamburger di Home.
 * Bagian Personalisasi: Mode Gelap, Ubah Bahasa, Ubah Kata Sandi.
 * Bagian Aplikasi: Tentang Aplikasi, Pusat Bantuan (WhatsApp), dan tombol Keluar.
 */
@Composable
fun ProfileDrawer(
    userName: String,
    userEmail: String,
    darkMode: Boolean,
    onDarkModeChange: (Boolean) -> Unit,
    language: AppLanguage,
    onLanguageChange: (AppLanguage) -> Unit,
    canChangePassword: Boolean,
    passwordState: AuthState,
    onChangePassword: (currentPassword: String, newPassword: String) -> Unit,
    onPasswordDialogClosed: () -> Unit,
    onLogoutClick: () -> Unit
) {
    val context = LocalContext.current
    val colors = if (darkMode) DarkProfileColors else LightProfileColors
    val strings = profileStrings()

    var showLanguageDialog by rememberSaveable { mutableStateOf(false) }
    var showPasswordDialog by rememberSaveable { mutableStateOf(false) }
    var showAboutDialog by rememberSaveable { mutableStateOf(false) }
    var showLogoutDialog by rememberSaveable { mutableStateOf(false) }

    // Bahasa Arab dibaca dari kanan ke kiri, termasuk dialognya
    CompositionLocalProvider(LocalLayoutDirection provides language.layoutDirection) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.background)
            .verticalScroll(rememberScrollState())
            .navigationBarsPadding()
    ) {
        ProfileHeader(
            name = userName.ifBlank { strings.guest },
            email = userEmail,
            colors = colors
        )

        Column(
            modifier = Modifier.padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            SectionTitle(strings.personalization, colors)
            SettingsCard(colors) {
                SettingsRow(
                    icon = painterIcon(R.drawable.ic_dark_mode),
                    title = strings.darkMode,
                    subtitle = if (darkMode) strings.darkModeOn else strings.darkModeOff,
                    colors = colors,
                    onClick = { onDarkModeChange(!darkMode) },
                    trailing = {
                        Switch(
                            checked = darkMode,
                            onCheckedChange = onDarkModeChange,
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.White,
                                checkedTrackColor = KeraPurple,
                                uncheckedThumbColor = KeraLavenderDeep,
                                uncheckedTrackColor = KeraLavenderLight,
                                uncheckedBorderColor = KeraLavender
                            )
                        )
                    }
                )
                RowDivider(colors)
                SettingsRow(
                    icon = painterIcon(R.drawable.ic_translate),
                    title = strings.language,
                    subtitle = language.label,
                    colors = colors,
                    onClick = { showLanguageDialog = true }
                )
                RowDivider(colors)
                SettingsRow(
                    icon = vectorIcon(Icons.Default.Lock),
                    title = strings.changePassword,
                    subtitle = strings.changePasswordHint,
                    colors = colors,
                    onClick = { showPasswordDialog = true }
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            SectionTitle(strings.app, colors)
            SettingsCard(colors) {
                SettingsRow(
                    icon = vectorIcon(Icons.Default.Info),
                    title = strings.about,
                    subtitle = strings.aboutHint,
                    colors = colors,
                    onClick = { showAboutDialog = true }
                )
                RowDivider(colors)
                SettingsRow(
                    icon = painterIcon(R.drawable.ic_help),
                    title = strings.help,
                    subtitle = strings.helpHint,
                    colors = colors,
                    onClick = { context.openHelpWhatsApp(strings) }
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            LogoutButton(text = strings.logout, colors = colors, onClick = { showLogoutDialog = true })

            Text(
                text = "Kerabite ${strings.version} ${context.appVersionName()}",
                fontSize = 12.sp,
                color = colors.subtext,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp)
            )
        }
    }

    if (showLanguageDialog) {
        LanguageDialog(
            current = language,
            strings = strings,
            onSelect = {
                onLanguageChange(it)
                showLanguageDialog = false
            },
            onDismiss = { showLanguageDialog = false }
        )
    }

    if (showPasswordDialog) {
        ChangePasswordDialog(
            strings = strings,
            canChangePassword = canChangePassword,
            state = passwordState,
            onSubmit = onChangePassword,
            onDismiss = {
                showPasswordDialog = false
                onPasswordDialogClosed()
            }
        )
    }

    if (showAboutDialog) {
        AboutDialog(
            strings = strings,
            versionName = context.appVersionName(),
            onDismiss = { showAboutDialog = false }
        )
    }

    if (showLogoutDialog) {
        AlertDialog(
            onDismissRequest = { showLogoutDialog = false },
            title = { Text(strings.logoutConfirmTitle, fontWeight = FontWeight.Bold) },
            text = { Text(strings.logoutConfirmText) },
            confirmButton = {
                TextButton(onClick = {
                    showLogoutDialog = false
                    onLogoutClick()
                }) {
                    Text(strings.logout, color = KeraProfileLogout, fontWeight = FontWeight.SemiBold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showLogoutDialog = false }) {
                    Text(strings.cancel, color = KeraPurple)
                }
            }
        )
    }
    }
}

/** Buka chat WhatsApp Pusat Bantuan; wa.me tetap jalan di browser kalau WhatsApp tidak terpasang. */
private fun Context.openHelpWhatsApp(strings: ProfileStrings) {
    val message = Uri.encode(strings.helpMessage)
    val intent = Intent(Intent.ACTION_VIEW, "https://wa.me/$HELP_WHATSAPP_NUMBER?text=$message".toUri())
    try {
        startActivity(intent)
    } catch (e: ActivityNotFoundException) {
        Toast.makeText(this, strings.noWhatsApp, Toast.LENGTH_SHORT).show()
    }
}

private fun Context.appVersionName(): String =
    runCatching { packageManager.getPackageInfo(packageName, 0).versionName }.getOrNull().orEmpty()

@Composable
private fun ProfileHeader(name: String, email: String, colors: ProfileColors) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(bottomStart = 32.dp, bottomEnd = 32.dp))
            .background(Brush.verticalGradient(listOf(colors.headerTop, colors.headerBottom)))
            .statusBarsPadding()
            .padding(horizontal = 24.dp, vertical = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Avatar berisi inisial nama
        Box(
            modifier = Modifier
                .size(84.dp)
                .clip(CircleShape)
                .background(Brush.verticalGradient(listOf(KeraLavenderDeep, KeraPurple)))
                .border(3.dp, Color.White, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = name.trim().firstOrNull()?.uppercase() ?: "K",
                fontSize = 34.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = name,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = colors.text,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )

        if (email.isNotBlank()) {
            Text(
                text = email,
                fontSize = 13.sp,
                color = colors.subtext,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun SectionTitle(text: String, colors: ProfileColors) {
    Text(
        text = text,
        fontSize = 14.sp,
        fontWeight = FontWeight.Bold,
        color = colors.accent,
        modifier = Modifier.padding(start = 4.dp, top = 16.dp)
    )
}

@Composable
private fun SettingsCard(colors: ProfileColors, content: @Composable ColumnScope.() -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(colors.card)
            .border(1.dp, colors.divider, RoundedCornerShape(20.dp)),
        content = content
    )
}

@Composable
private fun RowDivider(colors: ProfileColors) {
    HorizontalDivider(
        modifier = Modifier.padding(start = 68.dp),
        thickness = 1.dp,
        color = colors.divider
    )
}

/** Ikon baris pengaturan: vector Material core atau drawable sendiri. */
private sealed interface RowIcon
private data class VectorRowIcon(val vector: ImageVector) : RowIcon
private data class PainterRowIcon(@DrawableRes val res: Int) : RowIcon

private fun vectorIcon(vector: ImageVector): RowIcon = VectorRowIcon(vector)
private fun painterIcon(@DrawableRes res: Int): RowIcon = PainterRowIcon(res)

@Composable
private fun SettingsRow(
    icon: RowIcon,
    title: String,
    subtitle: String,
    colors: ProfileColors,
    onClick: () -> Unit,
    trailing: (@Composable () -> Unit)? = null
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(colors.iconTile),
            contentAlignment = Alignment.Center
        ) {
            when (icon) {
                is VectorRowIcon -> Icon(icon.vector, null, tint = colors.accent, modifier = Modifier.size(22.dp))
                is PainterRowIcon -> Icon(painterResource(icon.res), null, tint = colors.accent, modifier = Modifier.size(22.dp))
            }
        }

        Spacer(modifier = Modifier.width(14.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(text = title, fontSize = 15.sp, fontWeight = FontWeight.SemiBold, color = colors.text)
            Text(
                text = subtitle,
                fontSize = 12.sp,
                color = colors.subtext,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }

        if (trailing != null) {
            trailing()
        } else {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = null,
                tint = colors.subtext
            )
        }
    }
}

@Composable
private fun LogoutButton(text: String, colors: ProfileColors, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(52.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(colors.logoutContainer)
            .clickable(onClick = onClick),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            painter = painterResource(R.drawable.ic_logout),
            contentDescription = null,
            tint = KeraProfileLogout,
            modifier = Modifier.size(22.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(text = text, fontSize = 15.sp, fontWeight = FontWeight.Bold, color = KeraProfileLogout)
    }
}

@Composable
private fun LanguageDialog(
    current: AppLanguage,
    strings: ProfileStrings,
    onSelect: (AppLanguage) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(strings.chooseLanguage, fontWeight = FontWeight.Bold) },
        text = {
            Column {
                AppLanguage.entries.forEach { option ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .selectable(
                                selected = option == current,
                                onClick = { onSelect(option) },
                                role = Role.RadioButton
                            )
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = option == current,
                            onClick = null,
                            colors = RadioButtonDefaults.colors(selectedColor = KeraPurple)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(option.label, fontSize = 15.sp)
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(strings.close, color = KeraPurple)
            }
        }
    )
}

@Composable
private fun ChangePasswordDialog(
    strings: ProfileStrings,
    canChangePassword: Boolean,
    state: AuthState,
    onSubmit: (String, String) -> Unit,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current

    // Akun Google tidak punya kata sandi Kerabite yang bisa diubah
    if (!canChangePassword) {
        AlertDialog(
            onDismissRequest = onDismiss,
            title = { Text(strings.changePassword, fontWeight = FontWeight.Bold) },
            text = {
                Text(strings.googleAccountInfo)
            },
            confirmButton = {
                TextButton(onClick = onDismiss) { Text(strings.close, color = KeraPurple) }
            }
        )
        return
    }

    var current by remember { mutableStateOf("") }
    var new by remember { mutableStateOf("") }
    var confirm by remember { mutableStateOf("") }
    var localError by remember { mutableStateOf<String?>(null) }
    val loading = state is AuthState.Loading

    LaunchedEffect(state) {
        if (state is AuthState.Success) {
            Toast.makeText(context, strings.passwordChanged, Toast.LENGTH_SHORT).show()
            onDismiss()
        }
    }

    AlertDialog(
        onDismissRequest = { if (!loading) onDismiss() },
        containerColor = KeraLavenderLight,
        title = { Text(strings.changePassword, fontWeight = FontWeight.Bold, color = KeraPlum) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                val passwordKeyboard = KeyboardOptions(keyboardType = KeyboardType.Password)
                AuthTextField(
                    value = current,
                    onValueChange = { current = it; localError = null },
                    placeholder = strings.currentPassword,
                    enabled = !loading,
                    isPassword = true,
                    keyboardOptions = passwordKeyboard
                )
                AuthTextField(
                    value = new,
                    onValueChange = { new = it; localError = null },
                    placeholder = strings.newPassword,
                    enabled = !loading,
                    isPassword = true,
                    keyboardOptions = passwordKeyboard
                )
                AuthTextField(
                    value = confirm,
                    onValueChange = { confirm = it; localError = null },
                    placeholder = strings.confirmPassword,
                    enabled = !loading,
                    isPassword = true,
                    keyboardOptions = passwordKeyboard
                )

                val error = localError ?: (state as? AuthState.Error)?.let { stringResource(it.messageRes) }
                if (error != null) {
                    Text(error, color = KeraProfileLogout, fontSize = 12.sp)
                }
            }
        },
        confirmButton = {
            TextButton(
                enabled = !loading,
                onClick = {
                    localError = when {
                        current.isBlank() || new.isBlank() || confirm.isBlank() -> strings.errorFillAll
                        new.length < 6 -> strings.errorMinLength
                        new != confirm -> strings.errorMismatch
                        new == current -> strings.errorSame
                        else -> null
                    }
                    if (localError == null) onSubmit(current, new)
                }
            ) {
                if (loading) {
                    CircularProgressIndicator(color = KeraPurple, strokeWidth = 2.dp, modifier = Modifier.size(18.dp))
                } else {
                    Text(strings.save, color = KeraPurple, fontWeight = FontWeight.SemiBold)
                }
            }
        },
        dismissButton = {
            TextButton(enabled = !loading, onClick = onDismiss) {
                Text(strings.cancel, color = KeraPurple)
            }
        }
    )
}

@Composable
private fun AboutDialog(
    strings: ProfileStrings,
    versionName: String,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(strings.about, fontWeight = FontWeight.Bold) },
        text = {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "Kerabite ${strings.version} $versionName",
                    fontWeight = FontWeight.SemiBold,
                    color = KeraPurple
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(text = strings.aboutDescription, textAlign = TextAlign.Center, fontSize = 14.sp)
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text(strings.close, color = KeraPurple) }
        }
    )
}
