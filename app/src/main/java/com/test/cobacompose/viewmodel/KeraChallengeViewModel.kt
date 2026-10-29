package com.test.cobacompose.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.test.cobacompose.data.challenge.CampaignSubmission
import com.test.cobacompose.data.challenge.CampaignSubmissionRepository
import com.test.cobacompose.data.challenge.ChallengeProgress
import com.test.cobacompose.data.challenge.ChallengeTaskRepository
import com.test.cobacompose.data.challenge.ChallengeTasks
import com.test.cobacompose.data.challenge.KeraChallengeRepository
import com.test.cobacompose.data.challenge.SubmitResult
import com.test.cobacompose.data.challenge.TalaDoc
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * Progres KeraChallenge (bintang dan tantangan yang terbuka) dan isi tugas hariannya.
 * Tiap fungsi `on...`/`submit...` menyelesaikan hari yang bersangkutan dan mengembalikan
 * true kalau hari itu baru saja selesai (untuk toast +1 bintang).
 */
class KeraChallengeViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = KeraChallengeRepository.get(application)
    private val taskRepository = ChallengeTaskRepository.get(application)
    private val submissionRepository = CampaignSubmissionRepository.get(application)

    val progress: StateFlow<ChallengeProgress> = repository.progress
    val tasks: StateFlow<ChallengeTasks> = taskRepository.tasks

    init {
        repository.load()
        taskRepository.load()
    }

    /** Tandai hari selesai; false kalau hari itu sudah selesai sebelumnya. */
    fun completeDay(challenge: Int, day: Int): Boolean = repository.completeDay(challenge, day)

    /**
     * Jali Hari 1: satu pangan KeraFYI dibuka. Hari selesai setelah [total] pangan dibuka.
     * Mengembalikan jumlah pangan yang sudah dibuka.
     */
    fun onFyiFoodOpened(foodKey: String, total: Int): Int {
        val opened = taskRepository.markFoodOpened(foodKey)
        if (opened >= total) completeDay(JALI, 1)
        return opened
    }

    /** Jali Hari 2: peta KeraFYI dibuka. */
    fun onFyiMapOpened(): Boolean = completeDay(JALI, 2)

    private var journalUpload: Job? = null

    /** Draf jurnal: disimpan di perangkat tiap berubah, dikirim ke Firestore setelah berhenti mengetik. */
    fun saveJournal(text: String) {
        taskRepository.saveJournal(text, upload = false)
        journalUpload?.cancel()
        journalUpload = viewModelScope.launch {
            delay(JOURNAL_UPLOAD_DELAY_MS)
            taskRepository.uploadJournal()
        }
    }

    /** Jali Hari 3: jurnal dikumpulkan (langsung dikirim ke Firestore). */
    fun submitJournal(text: String): Boolean {
        journalUpload?.cancel()
        taskRepository.saveJournal(text, upload = true)
        return completeDay(JALI, 3)
    }

    override fun onCleared() {
        // Draf yang belum terkirim karena layar ditutup sebelum jeda selesai
        if (journalUpload?.isActive == true) taskRepository.uploadJournal()
    }

    /** Tala Hari 1: menu dari Kera Recommend dipilih. */
    fun chooseTalaRecipe(recipeKey: String): Boolean {
        taskRepository.setTalaRecipe(recipeKey)
        return completeDay(TALA, 1)
    }

    /** Tala Hari 2: foto masakan dari kamera ikut masuk dokumentasi Hari 3 (dan diunggah ke Firestore). */
    fun addCookingPhoto(photo: TalaDoc): Boolean {
        taskRepository.addTalaDocs(listOf(photo))
        return completeDay(TALA, 2)
    }

    fun addTalaDocs(docs: List<TalaDoc>) = taskRepository.addTalaDocs(docs)

    fun removeTalaDoc(doc: TalaDoc) = taskRepository.removeTalaDoc(doc)

    private val _sendProgress = MutableStateFlow<Float?>(null)

    /** Progres pengiriman Kera Campaign (0..1), atau null kalau sedang tidak mengirim. */
    val sendProgress: StateFlow<Float?> = _sendProgress.asStateFlow()

    /**
     * Kirim dokumentasi Kera Campaign ([kirimId] dari [CampaignSubmissionRepository]) ke Firestore.
     * [onResult] dipanggil di main thread; hari diselesaikan oleh pemanggil kalau hasilnya Success.
     * Diabaikan kalau masih ada kiriman yang berjalan.
     */
    fun sendCampaign(kirimId: String, submission: CampaignSubmission, onResult: (SubmitResult) -> Unit) {
        if (_sendProgress.value != null) return
        _sendProgress.value = 0f
        viewModelScope.launch {
            val result = try {
                submissionRepository.submit(kirimId, submission) { _sendProgress.value = it }
            } finally {
                _sendProgress.value = null
            }
            onResult(result)
        }
    }

    /** Tala Hari 3: dokumentasi sudah terkirim ke Kera Campaign. */
    fun submitTalaCampaign(): Boolean = completeDay(TALA, 3)

    private companion object {
        const val JALI = 1
        const val TALA = 2
        const val JOURNAL_UPLOAD_DELAY_MS = 1500L
    }
}
