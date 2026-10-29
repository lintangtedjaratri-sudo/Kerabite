package com.test.cobacompose.data.auth

import androidx.annotation.StringRes
import androidx.credentials.exceptions.GetCredentialCancellationException
import androidx.credentials.exceptions.NoCredentialException
import com.google.firebase.auth.FirebaseAuthException
import com.test.cobacompose.R

/** Resource pesan error untuk [throwable]; teksnya mengikuti bahasa aplikasi. */
@StringRes
fun getFirebaseAuthErrorMessage(throwable: Throwable): Int {
    // Error Credential Manager saat login Google (bukan FirebaseAuthException)
    when (throwable) {
        is NoCredentialException -> return R.string.auth_err_no_google
        is GetCredentialCancellationException -> return R.string.auth_err_google_cancelled
    }

    return when ((throwable as? FirebaseAuthException)?.errorCode) {
        "ERROR_INVALID_EMAIL" -> R.string.auth_err_invalid_email
        "ERROR_INVALID_CREDENTIAL",
        "ERROR_WRONG_PASSWORD",
        "ERROR_USER_NOT_FOUND" -> R.string.auth_err_wrong_credentials
        "ERROR_USER_DISABLED" -> R.string.auth_err_disabled
        "ERROR_EMAIL_ALREADY_IN_USE" -> R.string.auth_err_email_used
        "ERROR_ACCOUNT_EXISTS_WITH_DIFFERENT_CREDENTIAL" -> R.string.auth_err_other_method
        "ERROR_WEAK_PASSWORD" -> R.string.auth_err_weak_password
        "ERROR_OPERATION_NOT_ALLOWED" -> R.string.auth_err_not_allowed
        "ERROR_TOO_MANY_REQUESTS" -> R.string.auth_err_too_many
        "ERROR_NETWORK_REQUEST_FAILED" -> R.string.auth_err_network
        "ERROR_USER_TOKEN_EXPIRED" -> R.string.auth_err_session_expired
        "ERROR_INVALID_USER_TOKEN" -> R.string.auth_err_session_invalid
        "ERROR_REQUIRES_RECENT_LOGIN" -> R.string.auth_err_recent_login
        else -> R.string.auth_err_generic
    }
}
