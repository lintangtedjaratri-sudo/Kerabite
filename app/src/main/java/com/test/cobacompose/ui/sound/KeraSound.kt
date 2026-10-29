package com.test.cobacompose.ui.sound

import android.content.Context
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.media.SoundPool
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.test.cobacompose.R
import com.test.cobacompose.data.settings.AppSettingsRepository

/**
 * Suara aplikasi: efek klik (`res/raw/sfx_button_click`) untuk semua tombol, dan musik latar
 * (`res/raw/music_kerachallenge_game`) untuk Susun Game dan Teka-Teki Silang.
 * Semua dipanggil dari main thread.
 */
object KeraSound {

    private const val CLICK_VOLUME = 0.6f
    private const val MUSIC_VOLUME = 0.45f

    private var soundPool: SoundPool? = null
    private var clickId = 0
    private var clickReady = false

    private var music: MediaPlayer? = null
    /** Jumlah halaman musik yang sedang tersusun; pemutar dilepas saat tidak ada lagi. */
    private var musicHolders = 0
    /** Jumlah halaman musik yang sedang terlihat (lifecycle STARTED). */
    private var musicActive = 0
    private var musicEnabled = true

    /** Memuat efek klik di latar belakang; aman dipanggil berkali-kali. */
    fun init(context: Context) {
        if (soundPool != null) return
        val attributes = AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_GAME)
            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
            .build()
        soundPool = SoundPool.Builder()
            .setMaxStreams(3)
            .setAudioAttributes(attributes)
            .build()
            .apply {
                setOnLoadCompleteListener { _, id, status -> if (id == clickId && status == 0) clickReady = true }
                clickId = load(context.applicationContext, R.raw.sfx_button_click, 1)
            }
    }

    fun playClick() {
        if (clickReady) soundPool?.play(clickId, CLICK_VOLUME, CLICK_VOLUME, 1, 0, 1f)
    }

    internal fun holdMusic(context: Context) {
        musicHolders++
        if (music == null) {
            music = MediaPlayer.create(context.applicationContext, R.raw.music_kerachallenge_game)?.apply {
                isLooping = true
                setVolume(MUSIC_VOLUME, MUSIC_VOLUME)
            }
        }
        syncMusic()
    }

    internal fun releaseMusic() {
        musicHolders = (musicHolders - 1).coerceAtLeast(0)
        if (musicHolders == 0) {
            music?.release()
            music = null
        }
    }

    internal fun setMusicActive(active: Boolean) {
        musicActive = (musicActive + if (active) 1 else -1).coerceAtLeast(0)
        syncMusic()
    }

    internal fun setMusicEnabled(enabled: Boolean) {
        musicEnabled = enabled
        syncMusic()
    }

    private fun syncMusic() {
        val player = music ?: return
        if (musicActive > 0 && musicEnabled) {
            if (!player.isPlaying) player.start()
        } else if (player.isPlaying) {
            player.pause()
        }
    }
}

/**
 * Memutar efek klik setiap kali ketukan di dalam layout ini ditangani oleh elemen yang bisa ditekan.
 * Dipasang sekali di akar aplikasi: pass Main berjalan dari anak ke induk, jadi di akar sudah terlihat
 * apakah lepasan jari dikonsumsi (oleh clickable/Button). Geser, gulir, tekan lama, dan multi-jari diabaikan.
 * Dialog punya window sendiri, jadi tidak ikut.
 */
fun Modifier.clickSound(): Modifier = pointerInput(Unit) {
    val slop = viewConfiguration.touchSlop
    val longPress = viewConfiguration.longPressTimeoutMillis

    awaitEachGesture {
        val down = awaitFirstDown(requireUnconsumed = false, pass = PointerEventPass.Main)
        while (true) {
            val event = awaitPointerEvent(PointerEventPass.Main)
            if (event.changes.count { it.pressed } > 1) return@awaitEachGesture
            val change = event.changes.firstOrNull { it.id == down.id } ?: return@awaitEachGesture
            if (!change.pressed) {
                if (change.isConsumed && change.uptimeMillis - down.uptimeMillis < longPress) {
                    KeraSound.playClick()
                }
                return@awaitEachGesture
            }
            if ((change.position - down.position).getDistance() > slop) return@awaitEachGesture
        }
    }
}

/**
 * Memutar musik latar game selama halaman ini terlihat dan musik tidak dimatikan.
 * Pemutarnya dipakai bersama, jadi pindah dari puzzle ke TTS tidak mengulang lagu dari awal.
 */
@Composable
fun GameMusicEffect() {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val enabled by AppSettingsRepository.getInstance(context).gameMusic.collectAsState()

    LaunchedEffect(enabled) { KeraSound.setMusicEnabled(enabled) }

    DisposableEffect(lifecycleOwner) {
        KeraSound.holdMusic(context)
        var started = false
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_START -> if (!started) {
                    started = true
                    KeraSound.setMusicActive(true)
                }
                Lifecycle.Event.ON_STOP -> if (started) {
                    started = false
                    KeraSound.setMusicActive(false)
                }
                else -> Unit
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
            if (started) KeraSound.setMusicActive(false)
            KeraSound.releaseMusic()
        }
    }
}
