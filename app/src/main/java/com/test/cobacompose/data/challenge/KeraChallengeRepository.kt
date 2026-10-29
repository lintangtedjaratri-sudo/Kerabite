package com.test.cobacompose.data.challenge

import android.content.Context
import androidx.core.content.edit
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Progres KeraChallenge satu pengguna: hari yang sudah selesai per tantangan,
 * disimpan sebagai bitmask (bit 0 = Hari 1). Satu hari selesai = satu bintang.
 */
data class ChallengeProgress(
    private val completedDays: Map<Int, Int> = emptyMap()
) {
    fun isDayDone(challenge: Int, day: Int): Boolean =
        (completedDays[challenge] ?: 0) and (1 shl (day - 1)) != 0

    fun stars(challenge: Int): Int = Integer.bitCount(completedDays[challenge] ?: 0)

    /** Tantangan 1 selalu terbuka; tantangan berikutnya butuh 3 bintang dari tantangan sebelumnya. */
    fun isUnlocked(challenge: Int): Boolean = challenge == 1 || stars(challenge - 1) >= MAX_STARS

    internal fun mask(challenge: Int): Int = completedDays[challenge] ?: 0

    internal fun withMask(challenge: Int, mask: Int) = copy(completedDays = completedDays + (challenge to mask))

    /** Gabungan dua progres: hari yang selesai di salah satunya dianggap selesai. */
    internal fun union(other: ChallengeProgress): ChallengeProgress {
        var merged = this
        for (challenge in 1..CHALLENGE_COUNT) {
            merged = merged.withMask(challenge, mask(challenge) or other.mask(challenge))
        }
        return merged
    }

    /** Semua (tantangan, hari) yang sudah selesai. */
    internal fun completedPairs(): List<Pair<Int, Int>> =
        (1..CHALLENGE_COUNT).flatMap { challenge ->
            (1..DAYS_PER_CHALLENGE).filter { isDayDone(challenge, it) }.map { challenge to it }
        }

    companion object {
        const val MAX_STARS = 3
        const val CHALLENGE_COUNT = 3
        const val DAYS_PER_CHALLENGE = 3
    }
}

/**
 * Menyimpan progres KeraChallenge per akun Firebase (uid), jadi pengguna baru mulai dari
 * Tantangan 1 dengan 0 bintang dan progres ikut pindah saat login di HP lain.
 *
 * - Cloud Firestore `users/{uid}/progress/kerachallenge` adalah sumber utama. Tiap hari
 *   disimpan sebagai field boolean sendiri (mis. `tantangan1_hari2: true`) dan hanya pernah
 *   ditulis `true` dengan merge, jadi dua perangkat tidak saling menimpa dan progres tidak hilang.
 * - SharedPreferences menjadi cache lokal: layar langsung tampil walau offline, dan Firestore
 *   mengirim tulisan yang tertunda saat online kembali. Progres lokal yang belum ada di server
 *   (mis. dari versi aplikasi sebelum sinkron) ikut diunggah saat pertama tersinkron.
 * - Tanpa login, progres hanya disimpan lokal.
 *
 * Satu instance untuk seluruh aplikasi supaya semua layar melihat progres yang sama.
 */
class KeraChallengeRepository private constructor(context: Context) {

    private val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    private val firestore by lazy { FirebaseFirestore.getInstance() }

    private val _progress = MutableStateFlow(ChallengeProgress())
    val progress: StateFlow<ChallengeProgress> = _progress.asStateFlow()

    private var userId: String? = null
    private var remoteListener: ListenerRegistration? = null

    /** Muat progres akun yang sedang login dan mulai sinkron dengan Firestore. */
    fun load() {
        val uid = currentUid()
        if (uid == userId) return

        remoteListener?.remove()
        remoteListener = null
        userId = uid
        _progress.value = readLocal(uid)

        if (uid != GUEST_ID) listenRemote(uid)
    }

    /** Tandai satu hari selesai. Mengembalikan false kalau hari itu sudah selesai sebelumnya. */
    fun completeDay(challenge: Int, day: Int): Boolean {
        // Akun berganti (logout lalu login akun lain) -> muat progres akun yang baru dulu
        if (userId != currentUid()) load()
        val uid = userId ?: return false

        val current = _progress.value
        if (current.isDayDone(challenge, day)) return false

        val updated = current.withMask(challenge, current.mask(challenge) or (1 shl (day - 1)))
        _progress.value = updated
        writeLocal(uid, updated)

        if (uid != GUEST_ID) {
            // Tersimpan di antrean offline Firestore kalau sedang tidak ada koneksi
            progressDoc(uid).set(
                mapOf(dayField(challenge, day) to true, FIELD_UPDATED_AT to FieldValue.serverTimestamp()),
                SetOptions.merge()
            )
        }
        return true
    }

    /** Dengarkan dokumen progres; perubahan dari perangkat lain langsung tampil. */
    private fun listenRemote(uid: String) {
        val doc = progressDoc(uid)
        remoteListener = doc.addSnapshotListener { snapshot, error ->
            // Abaikan hasil untuk akun lama setelah ganti akun, dan error (mis. rules belum dipasang)
            if (error != null || snapshot == null || uid != userId) return@addSnapshotListener

            val remote = progressFrom(snapshot.data)
            val merged = _progress.value.union(remote)
            if (merged != _progress.value) {
                _progress.value = merged
                writeLocal(uid, merged)
            }

            // Unggah hari yang hanya ada di lokal
            val missing = merged.completedPairs().filterNot { (c, d) -> remote.isDayDone(c, d) }
            if (missing.isNotEmpty() && !snapshot.metadata.hasPendingWrites()) {
                val fields: Map<String, Any> =
                    missing.associate { (c, d) -> dayField(c, d) to true } +
                        (FIELD_UPDATED_AT to FieldValue.serverTimestamp())
                doc.set(fields, SetOptions.merge())
            }
        }
    }

    private fun progressDoc(uid: String): DocumentReference =
        firestore.collection("users").document(uid).collection("progress").document("kerachallenge")

    private fun progressFrom(data: Map<String, Any>?): ChallengeProgress {
        var progress = ChallengeProgress()
        if (data == null) return progress
        for (challenge in 1..ChallengeProgress.CHALLENGE_COUNT) {
            var mask = 0
            for (day in 1..ChallengeProgress.DAYS_PER_CHALLENGE) {
                if (data[dayField(challenge, day)] == true) mask = mask or (1 shl (day - 1))
            }
            progress = progress.withMask(challenge, mask)
        }
        return progress
    }

    private fun readLocal(uid: String): ChallengeProgress {
        var loaded = ChallengeProgress()
        for (challenge in 1..ChallengeProgress.CHALLENGE_COUNT) {
            loaded = loaded.withMask(challenge, prefs.getInt(localKey(uid, challenge), 0))
        }
        return loaded
    }

    private fun writeLocal(uid: String, progress: ChallengeProgress) {
        prefs.edit {
            for (challenge in 1..ChallengeProgress.CHALLENGE_COUNT) {
                putInt(localKey(uid, challenge), progress.mask(challenge))
            }
        }
    }

    private fun currentUid(): String = FirebaseAuth.getInstance().currentUser?.uid ?: GUEST_ID

    private fun localKey(uid: String, challenge: Int) = "${uid}_tantangan_$challenge"

    private fun dayField(challenge: Int, day: Int) = "tantangan${challenge}_hari$day"

    companion object {
        private const val PREFS_NAME = "kerachallenge_progress"
        private const val GUEST_ID = "tamu"
        private const val FIELD_UPDATED_AT = "updatedAt"

        @Volatile
        private var instance: KeraChallengeRepository? = null

        fun get(context: Context): KeraChallengeRepository =
            instance ?: synchronized(this) {
                instance ?: KeraChallengeRepository(context.applicationContext).also { instance = it }
            }
    }
}
