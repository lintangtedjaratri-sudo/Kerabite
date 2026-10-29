package com.test.cobacompose.data.settings

import android.content.Context
import android.content.res.Configuration
import androidx.compose.ui.unit.LayoutDirection
import androidx.core.content.edit
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.Locale

/** Bahasa tampilan yang bisa dipilih di Profil. */
enum class AppLanguage(
    val code: String,
    /** Nama bahasa dalam bahasanya sendiri. */
    val label: String,
    val layoutDirection: LayoutDirection = LayoutDirection.Ltr
) {
    INDONESIA("id", "Bahasa Indonesia"),
    ENGLISH("en", "English"),
    MANDARIN("zh", "中文 (简体)"),
    ARABIC("ar", "العربية", LayoutDirection.Rtl);

    companion object {
        fun fromCode(code: String?) = entries.firstOrNull { it.code == code } ?: INDONESIA
    }
}

/**
 * Pengaturan aplikasi (mode gelap, bahasa) yang disimpan di SharedPreferences.
 * Singleton supaya semua layar membaca flow yang sama.
 */
class AppSettingsRepository private constructor(context: Context) {

    private val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    private val _darkMode = MutableStateFlow(prefs.getBoolean(KEY_DARK_MODE, false))
    val darkMode: StateFlow<Boolean> = _darkMode.asStateFlow()

    private val _gameMusic = MutableStateFlow(prefs.getBoolean(KEY_GAME_MUSIC, true))
    /** Musik latar Susun Game dan TTS; dimatikan lewat tombol kecil di kedua halaman itu. */
    val gameMusic: StateFlow<Boolean> = _gameMusic.asStateFlow()

    private val _language = MutableStateFlow(AppLanguage.fromCode(prefs.getString(KEY_LANGUAGE, null)))
    val language: StateFlow<AppLanguage> = _language.asStateFlow()

    fun setDarkMode(enabled: Boolean) {
        prefs.edit { putBoolean(KEY_DARK_MODE, enabled) }
        _darkMode.value = enabled
    }

    fun setGameMusic(enabled: Boolean) {
        prefs.edit { putBoolean(KEY_GAME_MUSIC, enabled) }
        _gameMusic.value = enabled
    }

    fun setLanguage(language: AppLanguage) {
        prefs.edit { putString(KEY_LANGUAGE, language.code) }
        _language.value = language
    }

    companion object {
        private const val PREFS_NAME = "kera_settings"
        private const val KEY_DARK_MODE = "dark_mode"
        private const val KEY_LANGUAGE = "language"
        private const val KEY_GAME_MUSIC = "game_music"

        /**
         * Context dengan bahasa pilihan pengguna, dipasang di MainActivity.attachBaseContext
         * supaya semua stringResource/getString memakai bahasa itu.
         * Arah tata letak tetap kiri-ke-kanan untuk semua bahasa, supaya gambar desain dan area tekannya
         * tidak tercermin; teks Arab tetap ditulis kanan-ke-kiri oleh sistem teks.
         */
        fun localizedContext(base: Context): Context {
            val code = base.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).getString(KEY_LANGUAGE, null)
            val config = Configuration(base.resources.configuration)
            config.setLocale(Locale.forLanguageTag(AppLanguage.fromCode(code).code))
            config.setLayoutDirection(Locale.ROOT)
            return base.createConfigurationContext(config)
        }

        @Volatile
        private var instance: AppSettingsRepository? = null

        fun getInstance(context: Context): AppSettingsRepository =
            instance ?: synchronized(this) {
                instance ?: AppSettingsRepository(context.applicationContext).also { instance = it }
            }
    }
}
