package com.test.cobacompose.data.challenge

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import androidx.core.net.toUri
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.Blob
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout

/** Jenis file kiriman Kera Campaign; [key] disimpan di Firestore. */
enum class SubmissionFileKind(val key: String) {
    FOTO("foto"),
    VIDEO("video"),
    DOKUMEN("dokumen"),
    BUKTI_AJAK("bukti_ajak")
}

/**
 * Satu file yang dikirim. [remotePhotoId] terisi untuk foto Tala yang sudah ada di
 * `kerachallenge_foto`; foto itu cukup dirujuk dan tidak diunggah ulang.
 */
data class SubmissionFile(
    val kind: SubmissionFileKind,
    val uri: String,
    val name: String,
    val remotePhotoId: String? = null
)

/** Isi kiriman Kera Campaign (Tantangan 2 Hari 3 atau Tantangan 3 Hari 3). */
data class CampaignSubmission(
    val files: List<SubmissionFile>,
    val caption: String? = null,
    val link: String? = null
)

sealed interface SubmitResult {
    data object Success : SubmitResult
    data object NotLoggedIn : SubmitResult
    data object NoConnection : SubmitResult
    data object TooManyFiles : SubmitResult
    data class FileTooLarge(val name: String) : SubmitResult
    data class Unreadable(val name: String) : SubmitResult
    data object Failed : SubmitResult
}

/**
 * Mengirim dokumentasi Kera Campaign ke Cloud Firestore (proyek ini tidak memakai Firebase Storage).
 *
 * `users/{uid}/kerachallenge_kirim/{kirimId}` berisi caption, link, dan daftar file. Isi tiap file
 * dipotong maks. [CHUNK_BYTES] dan disimpan sebagai Blob di subkoleksi `potongan/{fileId}_{n}`
 * (satu dokumen Firestore maks. 1 MB). Dokumen utama ditulis paling akhir, jadi hanya ada kalau
 * semua potongan sudah tersimpan. ID-nya tetap per hari, jadi kirim ulang menimpa kiriman lama
 * dan potongan sisa kiriman lama dihapus.
 */
class CampaignSubmissionRepository private constructor(private val appContext: Context) {

    private val firestore = FirebaseFirestore.getInstance()

    /** [onProgress] menerima 0..1 sesuai bytes yang sudah terunggah. */
    suspend fun submit(
        kirimId: String,
        submission: CampaignSubmission,
        onProgress: (Float) -> Unit
    ): SubmitResult = withContext(Dispatchers.IO) {
        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return@withContext SubmitResult.NotLoggedIn
        if (submission.files.size > MAX_FILES) return@withContext SubmitResult.TooManyFiles
        if (!isOnline()) return@withContext SubmitResult.NoConnection

        // Siapkan isi semua file dulu supaya kesalahan (terlalu besar, tidak terbaca) ketahuan sebelum mengunggah
        val prepared = submission.files.mapIndexed { index, file ->
            val bytes = if (file.remotePhotoId != null) {
                ByteArray(0)
            } else {
                when (val read = readFile(file)) {
                    is ReadResult.Ok -> read.bytes
                    ReadResult.TooLarge -> return@withContext SubmitResult.FileTooLarge(file.name)
                    ReadResult.Unreadable -> return@withContext SubmitResult.Unreadable(file.name)
                }
            }
            PreparedFile("f$index", file, bytes)
        }

        val submissionRef = firestore.collection("users").document(uid).collection(COLLECTION).document(kirimId)
        val chunksRef = submissionRef.collection(CHUNKS)
        val totalBytes = prepared.sumOf { it.bytes.size }.coerceAtLeast(1)
        var sentBytes = 0
        val chunkIds = mutableSetOf<String>()

        try {
            onProgress(0f)
            for (file in prepared) {
                file.chunks().forEachIndexed { order, chunk ->
                    val id = "${file.id}_$order"
                    chunkIds += id
                    chunksRef.document(id).setAwait(
                        mapOf(F_FILE to file.id, F_ORDER to order, F_DATA to Blob.fromBytes(chunk))
                    )
                    sentBytes += chunk.size
                    onProgress(sentBytes.toFloat() / totalBytes)
                }
            }

            submissionRef.setAwait(
                buildMap {
                    submission.caption?.let { put(F_CAPTION, it) }
                    submission.link?.let { put(F_LINK, it) }
                    put(F_FILES, prepared.map { it.describe() })
                    put(F_SENT_AT, FieldValue.serverTimestamp())
                    put(F_UPDATED_AT, FieldValue.serverTimestamp())
                }
            )
            onProgress(1f)

            // Bersihkan potongan dari kiriman sebelumnya yang tidak dipakai lagi (gagal pun tidak masalah)
            runCatching {
                chunksRef.get().await().documents
                    .filter { it.id !in chunkIds }
                    .forEach { it.reference.delete() }
            }
            SubmitResult.Success
        } catch (e: CancellationException) {
            if (e is TimeoutCancellationException) SubmitResult.NoConnection else throw e
        } catch (_: Exception) {
            // Mis. aturan Firestore belum dipasang (PERMISSION_DENIED)
            SubmitResult.Failed
        }
    }

    /** Tulis lalu tunggu konfirmasi server; tanpa koneksi Firestore hanya mengantre, jadi diberi batas waktu. */
    private suspend fun DocumentReference.setAwait(data: Map<String, Any>) {
        withTimeout(WRITE_TIMEOUT_MS) { set(data).await() }
    }

    private sealed interface ReadResult {
        class Ok(val bytes: ByteArray) : ReadResult
        data object TooLarge : ReadResult
        data object Unreadable : ReadResult
    }

    private fun readFile(file: SubmissionFile): ReadResult {
        val uri = file.uri.toUri()
        return when (file.kind) {
            SubmissionFileKind.FOTO, SubmissionFileKind.BUKTI_AJAK ->
                runCatching { PhotoCompressor.compress(appContext, uri, MAX_PHOTO_SIDE, MAX_PHOTO_BYTES) }.getOrNull()
                    ?.let { ReadResult.Ok(it) }
                    ?: ReadResult.Unreadable
            SubmissionFileKind.VIDEO, SubmissionFileKind.DOKUMEN -> runCatching {
                appContext.contentResolver.openInputStream(uri)?.use { input ->
                    // Baca paling banyak batas + 1 byte supaya file besar tidak dimuat seluruhnya
                    val buffer = java.io.ByteArrayOutputStream()
                    val chunk = ByteArray(64 * 1024)
                    var total = 0L
                    while (true) {
                        val read = input.read(chunk)
                        if (read < 0) break
                        total += read
                        if (total > MAX_FILE_BYTES) return@use ReadResult.TooLarge
                        buffer.write(chunk, 0, read)
                    }
                    ReadResult.Ok(buffer.toByteArray())
                }
            }.getOrNull() ?: ReadResult.Unreadable
        }
    }

    private fun isOnline(): Boolean {
        val manager = appContext.getSystemService(ConnectivityManager::class.java) ?: return true
        val capabilities = manager.getNetworkCapabilities(manager.activeNetwork) ?: return false
        return capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
    }

    private class PreparedFile(val id: String, val file: SubmissionFile, val bytes: ByteArray) {
        fun chunks(): List<ByteArray> =
            (bytes.indices step CHUNK_BYTES).map { start -> bytes.copyOfRange(start, minOf(start + CHUNK_BYTES, bytes.size)) }

        fun describe(): Map<String, Any> = buildMap {
            put(F_ID, id)
            put(F_KIND, file.kind.key)
            put(F_NAME, file.name.take(MAX_NAME))
            put(F_SIZE, bytes.size)
            put(F_CHUNK_COUNT, chunks().size)
            file.remotePhotoId?.let { put(F_PHOTO_ID, it) }
        }
    }

    companion object {
        const val KIRIM_TALA = "tantangan2_hari3"
        const val KIRIM_MAIN_GAME = "tantangan3_hari3"

        /** Batas video/dokumen (sama dengan batas video Tantangan 3). */
        const val MAX_FILE_BYTES = 10L * 1024 * 1024
        /** Batas jumlah file per kiriman (juga dicek di firestore.rules). */
        const val MAX_FILES = 12

        private const val COLLECTION = "kerachallenge_kirim"
        private const val CHUNKS = "potongan"
        private const val CHUNK_BYTES = 800_000
        private const val MAX_PHOTO_SIDE = 1280
        private const val MAX_PHOTO_BYTES = 750_000
        private const val MAX_NAME = 200
        private const val WRITE_TIMEOUT_MS = 90_000L

        private const val F_FILE = "file"
        private const val F_ORDER = "urutan"
        private const val F_DATA = "data"
        private const val F_CAPTION = "caption"
        private const val F_LINK = "link"
        private const val F_FILES = "daftarFile"
        private const val F_SENT_AT = "dikirim"
        private const val F_UPDATED_AT = "updatedAt"
        private const val F_ID = "id"
        private const val F_KIND = "jenis"
        private const val F_NAME = "nama"
        private const val F_SIZE = "ukuran"
        private const val F_CHUNK_COUNT = "jumlahPotongan"
        private const val F_PHOTO_ID = "fotoId"

        @Volatile
        private var instance: CampaignSubmissionRepository? = null

        fun get(context: Context): CampaignSubmissionRepository =
            instance ?: synchronized(this) {
                instance ?: CampaignSubmissionRepository(context.applicationContext).also { instance = it }
            }
    }
}
