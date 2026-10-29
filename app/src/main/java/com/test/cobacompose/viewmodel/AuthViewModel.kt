package com.test.cobacompose.viewmodel

import android.content.Context
import androidx.annotation.StringRes
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.test.cobacompose.R
import com.test.cobacompose.data.auth.AuthRepository
import com.test.cobacompose.data.auth.getFirebaseAuthErrorMessage
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed interface AuthState {
    data object Idle : AuthState
    data object Loading : AuthState
    data class Success(@StringRes val messageRes: Int) : AuthState
    data class Error(@StringRes val messageRes: Int) : AuthState
}

class AuthViewModel(
    private val repository: AuthRepository = AuthRepository()
) : ViewModel() {

    private val _authState = MutableStateFlow<AuthState>(AuthState.Idle)
    val authState: StateFlow<AuthState> = _authState.asStateFlow()

    private val _userName = MutableStateFlow("")
    val userName: StateFlow<String> = _userName.asStateFlow()

    private val _userEmail = MutableStateFlow("")
    val userEmail: StateFlow<String> = _userEmail.asStateFlow()

    /** Akun Google saja tidak punya kata sandi untuk diubah. */
    val canChangePassword: Boolean
        get() = repository.hasPasswordLogin

    init {
        loadUser()
    }

    private fun loadUser() {
        _userName.value = repository.currentUser?.displayName.orEmpty()
        _userEmail.value = repository.currentUser?.email.orEmpty()
    }

    fun register(
        name: String,
        email: String,
        password: String
    ) = runAuthAction(
        successMessage = R.string.auth_msg_register_ok,
        reloadUser = true
    ) {
        repository.register(name, email, password)
    }

    fun login(
        email: String,
        password: String
    ) = runAuthAction(
        successMessage = R.string.auth_msg_login_ok,
        reloadUser = true
    ) {
        repository.login(email, password)
    }

    fun sendPasswordResetEmail(email: String) = runAuthAction(
        successMessage = R.string.auth_msg_reset_sent,
        reloadUser = false
    ) {
        repository.sendPasswordResetEmail(email)
    }

    fun authenticateWithGoogle(context: Context) = runAuthAction(
        successMessage = R.string.auth_msg_google_ok,
        reloadUser = true
    ) {
        repository.loginWithGoogle(context)
    }

    fun changePassword(
        currentPassword: String,
        newPassword: String
    ) = runAuthAction(
        successMessage = R.string.auth_msg_password_changed,
        reloadUser = false
    ) {
        repository.changePassword(currentPassword, newPassword)
    }

    fun deleteAccount(context: Context, currentPassword: String?) = runAuthAction(
        successMessage = R.string.auth_msg_account_deleted,
        reloadUser = false
    ) {
        repository.deleteAccount(context, currentPassword)
    }

    fun logout() {
        repository.logout()
        _userName.value = ""
        _userEmail.value = ""
        resetState()
    }

    fun resetState() {
        _authState.value = AuthState.Idle
    }

    /** Loading -> jalankan [action] -> Success/Error dengan pesan yang sudah dilokalkan. */
    private fun runAuthAction(
        @StringRes successMessage: Int,
        reloadUser: Boolean,
        action: suspend () -> Result<*>
    ) {
        viewModelScope.launch {
            _authState.value = AuthState.Loading

            action()
                .onSuccess {
                    if (reloadUser) loadUser()
                    _authState.value = AuthState.Success(successMessage)
                }
                .onFailure {
                    _authState.value = AuthState.Error(
                        getFirebaseAuthErrorMessage(it)
                    )
                }
        }
    }
}
