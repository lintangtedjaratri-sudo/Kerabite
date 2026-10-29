package com.test.cobacompose.data.challenge

import android.content.Context
import android.net.Uri
import androidx.core.content.FileProvider
import androidx.core.content.edit
import androidx.core.net.toUri
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.Blob
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.io.File
import java.util.concurrent.Executors

/** Jenis file dokumentasi Tala Hari 3. */
enum class TalaDocKind { FOTO, VIDEO, DOKUMEN }

/**
 * Satu file dokumentasi Tala (foto kamera Hari 2 atau file yang dipilih di Hari 3).
 * [remoteId] terisi untuk foto yang tersimpan di Firestore; [uri] lalu menunjuk salinan lokalnya.
 */
data class TalaDoc(
    val kind: TalaDocKind,
    val uri: String,
    val name: String,
    val remoteId: String? = null
) {
    internal fun encode() = "${kind.name}|$uri|$name"

    internal companion object {
        fun decode(value: String): TalaDoc? {
            val parts = value.split('|', limit = 3)
            if (parts.size != 3) return null
            val kind = TalaDocKind.entries.firstOrNull { it.name == parts[0] } ?: return null
            return TalaDoc(kind, parts[1], parts[2])
        }
    }
}

/**
 * Isi tugas harian KeraChallenge yang perlu diingat di antara layar:
 * pangan KeraFYI yang sudah dibuka (Jali Hari 1), jurnal (Jali Hari 3),
 * menu pilihan (Tala Hari 1) dan file dokumentasi (Tala Hari 2-3).
 */
data class ChallengeTasks(
    val openedFoods: Set<String> = emptySet(),
    val journal: String = "",
    val talaRecipe: String? = null,
    val talaDocs: List<TalaDoc> = emptyList()
)

/**
 * Menyimpan [ChallengeTasks] per akun (uid).
 *
 * - Akun yang login disinkronkan ke Cloud Firestore:
 *   `users/{uid}/progress/kerachallenge_tugas` (jurnal, menu Tala, pangan yang dibuka) dan satu dokumen
 *   per foto di `users/{uid}/kerachallenge_foto/{id}`. Foto dikompres jadi JPEG < [MAX_PHOTO_BYTES] dan
 *   disimpan sebagai Blob, karena satu dokumen Firestore maksimal 1 MB.
 * - SharedPreferences menjadi cache lokal; jurnal dan menu memakai waktu ubah terakhir, jadi yang
 *   paling baru menang, dan pangan yang dibuka digabung.
 * - Video dan PDF terlalu besar untuk Firestore, jadi tetap tersimpan di perangkat saja.
 *   Tanpa login, semuanya hanya disimpan lokal.
 */
class ChallengeTaskRepository private constructor(context: Context) {

    private val appContext = context.applicationContext
    private val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    private val firestore by lazy { FirebaseFirestore.getInstance() }

    /** Kompres/unggah foto tetap berjalan walau layarnya sudah ditutup. */
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val listenerExecutor = Executors.newSingleThreadExecutor()

    private val _tasks = MutableStateFlow(ChallengeTasks())
    val tasks: StateFlow<ChallengeTasks> = _tasks.asStateFlow()

    private var userId: String? = null
    private var journalUpdatedAt = 0L
    private var recipeUpdatedAt = 0L
    /** File yang hanya ada di perangkat (video, PDF, foto tamu atau foto yang gagal diunggah). */
    private var localDocs: List<TalaDoc> = emptyList()
    /** Foto dari Firestore, termasuk yang masih menunggu terkirim. */
    private var remoteDocs: List<TalaDoc> = emptyList()
    private var taskListener: ListenerRegistration? = null
    private var photoListener: ListenerRegistration? = null

    /** Muat tugas akun yang sedang login (dipanggil lagi otomatis kalau akun berganti). */
    @Synchronized
    fun load() {
        val uid = currentUid()
        if (uid == userId) return

        taskListener?.remove()
        photoListener?.remove()
        taskListener = null
        photoListener = null
        userId = uid

        journalUpdatedAt = prefs.getLong(key(uid, KEY_JOURNAL_AT), 0L)
        recipeUpdatedAt = prefs.getLong(key(uid, KEY_RECIPE_AT), 0L)
        localDocs = prefs.getString(key(uid, KEY_DOCS), "").orEmpty()
            .split('\n')
            .filter { it.isNotBlank() }
            .mapNotNull(TalaDoc::decode)
        remoteDocs = emptyList()
        _tasks.value = ChallengeTasks(
            openedFoods = prefs.getStringSet(key(uid, KEY_FOODS), emptySet()).orEmpty().toSet(),
            journal = prefs.getString(key(uid, KEY_JOURNAL), "").orEmpty(),
            talaRecipe = prefs.getString(key(uid, KEY_RECIPE), null),
            talaDocs = localDocs
        )

        if (uid != GUEST_ID) {
            listenTasks(uid)
            listenPhotos(uid)
        }
    }

    /** Catat satu pangan KeraFYI yang sudah dibuka; mengembalikan jumlah pangan yang sudah dibuka. */
    fun markFoodOpened(foodKey: String): Int {
        val before = ensureLoaded().openedFoods
        val updated = update { it.copy(openedFoods = it.openedFoods + foodKey) }
        if (foodKey !in before) pushTasks(mapOf(F_FOODS to FieldValue.arrayUnion(foodKey)))
        return updated.openedFoods.size
    }

    /** Simpan jurnal di perangkat; [upload] = kirim juga ke Firestore. */
    fun saveJournal(text: String, upload: Boolean) {
        ensureLoaded()
        journalUpdatedAt = System.currentTimeMillis()
        update { it.copy(journal = text) }
        if (upload) uploadJournal()
    }

    /** Kirim jurnal terakhir yang tersimpan ke Firestore. */
    fun uploadJournal() {
        pushTasks(mapOf(F_JOURNAL to _tasks.value.journal, F_JOURNAL_AT to journalUpdatedAt))
    }

    fun setTalaRecipe(recipeKey: String) {
        ensureLoaded()
        recipeUpdatedAt = System.currentTimeMillis()
        update { it.copy(talaRecipe = recipeKey) }
        pushTasks(mapOf(F_RECIPE to recipeKey, F_RECIPE_AT to recipeUpdatedAt))
    }

    /**
     * Tambah file dokumentasi. Foto dari akun yang login dikompres lalu diunggah ke Firestore;
     * file lain (dan semua file tamu) disimpan di perangkat.
     */
    fun addTalaDocs(docs: List<TalaDoc>) {
        ensureLoaded()
        val uid = userId ?: return
        val (photos, others) = docs.partition { it.kind == TalaDocKind.FOTO && uid != GUEST_ID }
        if (others.isNotEmpty()) setLocalDocs((localDocs + others).distinctBy(TalaDoc::uri))
        photos.forEach { photo -> scope.launch { uploadPhoto(uid, photo) } }
    }

    fun removeTalaDoc(doc: TalaDoc) {
        ensureLoaded()
        val uid = userId ?: return
        if (doc.remoteId != null && uid != GUEST_ID) {
            remoteDocs = remoteDocs.filterNot { it.remoteId == doc.remoteId }
            publishDocs()
            photoDoc(uid, doc.remoteId).delete()
            cacheFile(doc.remoteId).delete()
        } else {
            setLocalDocs(localDocs - doc)
        }
    }

    // -----------------------------------------------------------------------
    // Firestore
    // -----------------------------------------------------------------------

    private fun tasksDoc(uid: String): DocumentReference =
        firestore.collection("users").document(uid).collection("progress").document("kerachallenge_tugas")

    private fun photoDoc(uid: String, id: String): DocumentReference =
        firestore.collection("users").document(uid).collection(PHOTO_COLLECTION).document(id)

    /** Tulis sebagian field dokumen tugas (merge); tersimpan di antrean offline kalau sedang tidak ada koneksi. */
    private fun pushTasks(fields: Map<String, Any>) {
        val uid = userId ?: return
        if (uid == GUEST_ID) return
        tasksDoc(uid).set(fields + (F_UPDATED_AT to FieldValue.serverTimestamp()), SetOptions.merge())
    }

    private fun listenTasks(uid: String) {
        taskListener = tasksDoc(uid).addSnapshotListener(listenerExecutor) { snapshot, error ->
            if (error != null || snapshot == null || uid != userId) return@addSnapshotListener
            if (snapshot.metadata.hasPendingWrites()) return@addSnapshotListener
            val data = snapshot.data.orEmpty()
            val current = _tasks.value
            val upload = mutableMapOf<String, Any>()

            // Jurnal dan menu: yang paling baru menang
            val remoteJournalAt = (data[F_JOURNAL_AT] as? Number)?.toLong() ?: 0L
            var journal = current.journal
            if (remoteJournalAt > journalUpdatedAt) {
                journal = data[F_JOURNAL] as? String ?: ""
                journalUpdatedAt = remoteJournalAt
            } else if (journalUpdatedAt > remoteJournalAt) {
                upload[F_JOURNAL] = current.journal
                upload[F_JOURNAL_AT] = journalUpdatedAt
            }

            val remoteRecipeAt = (data[F_RECIPE_AT] as? Number)?.toLong() ?: 0L
            var recipe = current.talaRecipe
            if (remoteRecipeAt > recipeUpdatedAt) {
                recipe = data[F_RECIPE] as? String
                recipeUpdatedAt = remoteRecipeAt
            } else if (recipeUpdatedAt > remoteRecipeAt && current.talaRecipe != null) {
                upload[F_RECIPE] = current.talaRecipe
                upload[F_RECIPE_AT] = recipeUpdatedAt
            }

            // Pangan yang dibuka: digabung
            val remoteFoods = (data[F_FOODS] as? List<*>)?.filterIsInstance<String>()?.toSet().orEmpty()
            val missing = current.openedFoods - remoteFoods
            if (missing.isNotEmpty()) upload[F_FOODS] = FieldValue.arrayUnion(*missing.toTypedArray())

            update { it.copy(journal = journal, talaRecipe = recipe, openedFoods = it.openedFoods + remoteFoods) }
            if (upload.isNotEmpty()) pushTasks(upload)
        }
    }

    private fun listenPhotos(uid: String) {
        photoListener = firestore.collection("users").document(uid).collection(PHOTO_COLLECTION)
            .addSnapshotListener(listenerExecutor) { snapshot, error ->
                if (error != null || snapshot == null || uid != userId) return@addSnapshotListener
                remoteDocs = snapshot.documents
                    .sortedBy { (it.get(F_CREATED_AT) as? Number)?.toLong() ?: 0L }
                    .mapNotNull { doc ->
                        val file = cacheFile(doc.id)
                        // Foto dari perangkat lain: simpan salinannya supaya bisa ditampilkan
                        if (!file.exists()) {
                            val blob = doc.getBlob(F_DATA) ?: return@mapNotNull null
                            runCatching { file.parentFile?.mkdirs(); file.writeBytes(blob.toBytes()) }
                                .onFailure { return@mapNotNull null }
                        }
                        TalaDoc(TalaDocKind.FOTO, fileUri(file).toString(), doc.getString(F_NAME).orEmpty(), doc.id)
                    }
                publishDocs()
            }
    }

    /** Kompres foto, simpan salinannya, lalu tulis ke Firestore. Kalau gagal, foto tetap disimpan lokal. */
    private fun uploadPhoto(uid: String, photo: TalaDoc) {
        val bytes = runCatching { PhotoCompressor.compress(appContext, photo.uri.toUri(), MAX_PHOTO_SIDE, MAX_PHOTO_BYTES) }.getOrNull()
        if (bytes == null) {
            setLocalDocs((localDocs + photo).distinctBy(TalaDoc::uri))
            return
        }

        val ref = firestore.collection("users").document(uid).collection(PHOTO_COLLECTION).document()
        val file = cacheFile(ref.id)
        runCatching { file.parentFile?.mkdirs(); file.writeBytes(bytes) }
        val saved = TalaDoc(TalaDocKind.FOTO, fileUri(file).toString(), photo.name, ref.id)

        ref.set(
            mapOf(
                F_NAME to photo.name.take(MAX_NAME),
                F_DATA to Blob.fromBytes(bytes),
                F_CREATED_AT to System.currentTimeMillis(),
                F_UPDATED_AT to Timestamp.now()
            )
        ).addOnFailureListener {
            // Mis. aturan Firestore belum dipasang: simpan di perangkat supaya foto tidak hilang
            if (uid == userId) setLocalDocs((localDocs + saved.copy(remoteId = null)).distinctBy(TalaDoc::uri))
        }

        // Foto kamera asli (ukuran penuh) tidak diperlukan lagi setelah dikompres
        photo.uri.toUri().takeIf { it.authority == fileAuthority() }?.lastPathSegment?.let { name ->
            File(File(appContext.filesDir, PHOTO_DIR), name).takeIf { it.exists() && it != file }?.delete()
        }
    }

    // -----------------------------------------------------------------------
    // Lokal
    // -----------------------------------------------------------------------

    private fun ensureLoaded(): ChallengeTasks {
        if (userId != currentUid()) load()
        return _tasks.value
    }

    private fun setLocalDocs(docs: List<TalaDoc>) {
        localDocs = docs
        publishDocs()
    }

    private fun publishDocs() {
        update { it.copy(talaDocs = localDocs + remoteDocs) }
    }

    @Synchronized
    private fun update(change: (ChallengeTasks) -> ChallengeTasks): ChallengeTasks {
        val uid = userId ?: currentUid()
        val updated = change(_tasks.value)
        _tasks.value = updated
        prefs.edit {
            putStringSet(key(uid, KEY_FOODS), updated.openedFoods)
            putString(key(uid, KEY_JOURNAL), updated.journal)
            putLong(key(uid, KEY_JOURNAL_AT), journalUpdatedAt)
            if (updated.talaRecipe != null) putString(key(uid, KEY_RECIPE), updated.talaRecipe) else remove(key(uid, KEY_RECIPE))
            putLong(key(uid, KEY_RECIPE_AT), recipeUpdatedAt)
            putString(key(uid, KEY_DOCS), localDocs.joinToString("\n") { it.encode() })
        }
        return updated
    }

    private fun cacheFile(id: String) = File(File(appContext.filesDir, "$PHOTO_DIR/firestore"), "$id.jpg")

    private fun fileAuthority() = "${appContext.packageName}.fileprovider"

    private fun fileUri(file: File): Uri = FileProvider.getUriForFile(appContext, fileAuthority(), file)

    private fun currentUid(): String = FirebaseAuth.getInstance().currentUser?.uid ?: GUEST_ID

    private fun key(uid: String, name: String) = "${uid}_$name"

    companion object {
        /** Folder foto KeraChallenge di filesDir (dibagikan lewat FileProvider). */
        const val PHOTO_DIR = "kerachallenge"

        private const val PREFS_NAME = "kerachallenge_tasks"
        private const val GUEST_ID = "tamu"
        private const val KEY_FOODS = "jali_pangan_dibuka"
        private const val KEY_JOURNAL = "jali_jurnal"
        private const val KEY_JOURNAL_AT = "jali_jurnal_diubah"
        private const val KEY_RECIPE = "tala_menu"
        private const val KEY_RECIPE_AT = "tala_menu_diubah"
        private const val KEY_DOCS = "tala_dokumentasi"

        private const val PHOTO_COLLECTION = "kerachallenge_foto"
        private const val F_JOURNAL = "jurnal"
        private const val F_JOURNAL_AT = "jurnalDiubah"
        private const val F_RECIPE = "menuTala"
        private const val F_RECIPE_AT = "menuTalaDiubah"
        private const val F_FOODS = "panganDibuka"
        private const val F_UPDATED_AT = "updatedAt"
        private const val F_NAME = "nama"
        private const val F_DATA = "data"
        private const val F_CREATED_AT = "dibuat"

        private const val MAX_PHOTO_SIDE = 1280
        private const val MAX_PHOTO_BYTES = 750_000
        private const val MAX_NAME = 200

        @Volatile
        private var instance: ChallengeTaskRepository? = null

        fun get(context: Context): ChallengeTaskRepository =
            instance ?: synchronized(this) {
                instance ?: ChallengeTaskRepository(context.applicationContext).also { instance = it }
            }
    }
}
