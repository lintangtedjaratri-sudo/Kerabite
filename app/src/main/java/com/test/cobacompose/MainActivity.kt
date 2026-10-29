package com.test.cobacompose

import android.app.Activity
import android.content.Context
import android.content.res.Configuration
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalResources
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.navigation.compose.rememberNavController
import com.test.cobacompose.data.settings.AppLanguage
import com.test.cobacompose.data.settings.AppSettingsRepository
import com.test.cobacompose.navigation.AppNavGraph
import com.test.cobacompose.ui.sound.KeraSound
import com.test.cobacompose.ui.sound.clickSound
import com.test.cobacompose.ui.splash.SplashScreen
import com.test.cobacompose.ui.theme.CobacomposeTheme
import java.util.Locale
import kotlinx.coroutines.delay

class MainActivity : ComponentActivity() {
    override fun attachBaseContext(newBase: Context) {
        super.attachBaseContext(AppSettingsRepository.localizedContext(newBase))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            val language by AppSettingsRepository.getInstance(this).language.collectAsState()

            LocalizedContent(language) {
                // Layar didesain untuk mode terang dengan background putih hard-coded,
                // jadi dark theme dan dynamic color (Android 12+) sengaja dimatikan.
                CobacomposeTheme(
                    darkTheme = false,
                    dynamicColor = false
                ) {
                    KeraApp()
                }
            }
        }
    }
}

/**
 * Ganti bahasa di tempat tanpa membuat ulang activity (recreate() membuat layar berkedip hitam).
 * Resource activity ikut diperbarui supaya context.getString (toast, sertifikat) juga memakai bahasa
 * baru, lalu Configuration/Resources baru diberikan ke Compose supaya semua stringResource diperbarui.
 * Arah tata letak tetap kiri-ke-kanan, sama seperti [AppSettingsRepository.localizedContext].
 */
@Composable
private fun LocalizedContent(language: AppLanguage, content: @Composable () -> Unit) {
    val context = LocalContext.current
    val baseConfiguration = LocalConfiguration.current
    val configuration = remember(baseConfiguration, language) {
        Configuration(baseConfiguration).apply {
            setLocale(Locale.forLanguageTag(language.code))
            setLayoutDirection(Locale.ROOT)
        }
    }
    val resources = remember(configuration) {
        @Suppress("DEPRECATION")
        context.resources.updateConfiguration(configuration, context.resources.displayMetrics)
        context.createConfigurationContext(configuration).resources
    }

    CompositionLocalProvider(
        LocalConfiguration provides configuration,
        LocalResources provides resources,
        content = content
    )
}

@Composable
fun KeraApp() {
    val context = LocalContext.current
    // Disimpan supaya splash tidak muncul lagi saat activity dibuat ulang (mis. ganti bahasa)
    var showSplash by rememberSaveable { mutableStateOf(true) }

    LaunchedEffect(showSplash) {
        val window = (context as Activity).window

        WindowCompat.setDecorFitsSystemWindows(
            window,
            !showSplash
        )

        if (!showSplash) {
            WindowInsetsControllerCompat(
                window,
                window.decorView
            ).apply {
                show(WindowInsetsCompat.Type.systemBars())
                isAppearanceLightStatusBars = true
                isAppearanceLightNavigationBars = true
            }
        }

        if (showSplash) {
            delay(2000)
            showSplash = false
        }
    }

    if (showSplash) {
        SplashScreen()
    } else {
        LaunchedEffect(Unit) { KeraSound.init(context) }
        val navController = rememberNavController()
        // Efek klik untuk semua tombol di aplikasi (lihat Modifier.clickSound)
        Box(modifier = Modifier.fillMaxSize().clickSound()) {
            AppNavGraph(navController)
        }
    }
}