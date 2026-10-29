package com.test.cobacompose.data.auth

import android.content.Context
import androidx.credentials.CredentialManager
import androidx.credentials.GetCredentialRequest
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Source
import com.test.cobacompose.R
import kotlinx.coroutines.tasks.await
import java.io.File

class AuthRepository {

    private val auth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()

    val currentUser: FirebaseUser?
        get() = auth.currentUser

    suspend fun login(
        email: String,
        password: String
    ): Result<FirebaseUser> {
        return try {
            val result = auth
                .signInWithEmailAndPassword(email, password)
                .await()

            Result.success(result.user!!)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun register(
        name: String,
        email: String,
        password: String
    ): Result<FirebaseUser> {
        return try {
            val result = auth
                .createUserWithEmailAndPassword(email, password)
                .await()

            val user = result.user!!

            val profileUpdates = UserProfileChangeRequest.Builder()
                .setDisplayName(name)
                .build()

            user.updateProfile(profileUpdates).await()

            Result.success(user)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun sendPasswordResetEmail(
        email: String
    ): Result<Unit> {
        return try {
            auth.sendPasswordResetEmail(email).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun loginWithGoogle(
        context: Context
    ): Result<FirebaseUser> {
        return try {
            val authResult = auth
                .signInWithCredential(googleCredential(context))
                .await()

            Result.success(authResult.user!!)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /** True kalau akun bisa masuk dengan email + kata sandi (bukan hanya Google). */
    val hasPasswordLogin: Boolean
        get() = auth.currentUser?.providerData.orEmpty()
            .any { it.providerId == EmailAuthProvider.PROVIDER_ID }

    /** Login ulang dengan kata sandi lama (wajib oleh Firebase), lalu ganti ke kata sandi baru. */
    suspend fun changePassword(
        currentPassword: String,
        newPassword: String
    ): Result<Unit> {
        return try {
            val user = auth.currentUser
                ?: return Result.failure(IllegalStateException("Belum masuk"))
            val email = user.email
                ?: return Result.failure(IllegalStateException("Akun tanpa email"))

            user.reauthenticate(EmailAuthProvider.getCredential(email, currentPassword)).await()
            user.updatePassword(newPassword).await()

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Hapus seluruh data Kerabite yang diketahui, lalu hapus akun Firebase Auth.
     * Pengguna diautentikasi ulang lebih dahulu karena penghapusan akun adalah tindakan sensitif.
     */
    suspend fun deleteAccount(context: Context, currentPassword: String?): Result<Unit> {
        return try {
            val user = auth.currentUser
                ?: return Result.failure(IllegalStateException("Belum masuk"))

            if (hasPasswordLogin) {
                val email = user.email
                    ?: return Result.failure(IllegalStateException("Akun tanpa email"))
                val password = currentPassword.orEmpty()
                require(password.isNotBlank()) { "Kata sandi wajib diisi" }
                user.reauthenticate(EmailAuthProvider.getCredential(email, password)).await()
            } else {
                user.reauthenticate(googleCredential(context)).await()
            }

            deleteUserData(user.uid)
            clearLocalUserData(context)
            user.delete().await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /** Hapus koleksi dan subkoleksi yang dibuat aplikasi untuk satu pengguna. */
    private suspend fun deleteUserData(uid: String) {
        val user = firestore.collection("users").document(uid)

        user.collection("kerachallenge_kirim").get(Source.SERVER).await().documents.forEach { submission ->
            submission.reference.collection("potongan").get(Source.SERVER).await().documents.forEach { chunk ->
                chunk.reference.delete().await()
            }
            submission.reference.delete().await()
        }

        listOf("progress", "kerachallenge_foto").forEach { collection ->
            user.collection(collection).get(Source.SERVER).await().documents.forEach { document ->
                document.reference.delete().await()
            }
        }

    }

    private fun clearLocalUserData(context: Context) {
        context.getSharedPreferences("kerachallenge_progress", Context.MODE_PRIVATE)
            .edit().clear().apply()
        context.getSharedPreferences("kerachallenge_tasks", Context.MODE_PRIVATE)
            .edit().clear().apply()
        File(context.filesDir, "kerachallenge").deleteRecursively()
    }

    private suspend fun googleCredential(context: Context) =
        GoogleAuthProvider.getCredential(
            GoogleIdTokenCredential.createFrom(
                CredentialManager.create(context).getCredential(
                    context,
                    GetCredentialRequest.Builder()
                        .addCredentialOption(
                            GetGoogleIdOption.Builder()
                                .setServerClientId(context.getString(R.string.default_web_client_id))
                                .setFilterByAuthorizedAccounts(false)
                                .build()
                        )
                        .build()
                ).credential.data
            ).idToken,
            null
        )

    fun logout() {
        auth.signOut()
    }
}
