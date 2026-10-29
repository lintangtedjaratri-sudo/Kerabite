package com.test.cobacompose

import android.content.res.Configuration
import android.graphics.Bitmap
import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalResources
import androidx.compose.ui.test.captureToImage
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onRoot
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.test.cobacompose.ui.kerachallenge.HotspotImage
import com.test.cobacompose.ui.kerachallenge.KeraChallenge
import com.test.cobacompose.ui.kerachallenge.detailTexts
import com.test.cobacompose.ui.kerachallenge.mainGameTexts
import com.test.cobacompose.ui.kerachallenge.mapTexts
import com.test.cobacompose.ui.kerachallenge.susunHeaderTexts
import com.test.cobacompose.ui.kerachallenge.ttsHeaderTexts
import com.test.cobacompose.ui.theme.CobacomposeTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.io.File
import java.util.Locale

/** Sementara: render gambar desain + teks Compose ke PNG untuk dicek posisinya. */
@RunWith(AndroidJUnit4::class)
class DesignTextScreenshotTest {
    @get:Rule
    val rule = createComposeRule()

    private fun shot(name: String, lang: String, content: @Composable () -> Unit) {
        val base = InstrumentationRegistry.getInstrumentation().targetContext
        val config = Configuration(base.resources.configuration).apply {
            setLocale(Locale.forLanguageTag(lang))
            setLayoutDirection(Locale.ROOT)
        }
        val ctx = base.createConfigurationContext(config)
        rule.setContent {
            CompositionLocalProvider(
                LocalContext provides ctx,
                LocalResources provides ctx.resources,
                LocalConfiguration provides config
            ) {
                CobacomposeTheme(darkTheme = false, dynamicColor = false) { Column { content() } }
            }
        }
        val bmp = rule.onRoot().captureToImage().asAndroidBitmap()
        val dir = File(base.getExternalFilesDir(null), "shots").apply { mkdirs() }
        File(dir, "${name}_$lang.png").outputStream().use { bmp.compress(Bitmap.CompressFormat.PNG, 100, it) }
    }

    private fun page(name: String, lang: String, res: Int, texts: @Composable () -> List<com.test.cobacompose.ui.kerachallenge.DesignText>) =
        shot(name, lang) { HotspotImage(imageRes = res, contentDescription = null, hotspots = emptyList(), texts = texts()) }

    @Test fun jali() = page("jali", argLang(), R.drawable.img_kerachallenge_detail_jali) { KeraChallenge.JALI.detailTexts() }
    @Test fun tala() = page("tala", argLang(), R.drawable.img_kerachallenge_detail_tala) { KeraChallenge.TALA.detailTexts() }
    @Test fun mainGame() = page("maingame", argLang(), R.drawable.img_kerachallenge_maingame) { mainGameTexts() }
    @Test fun map() = page("map", argLang(), R.drawable.bg_kerachallenge_map) { mapTexts() }
    @Test fun susun() = page("susun", argLang(), R.drawable.img_kerachallenge_maingame_susun_header) { susunHeaderTexts() }
    @Test fun tts() = page("tts", argLang(), R.drawable.img_kerachallenge_maingame_tts_header) { ttsHeaderTexts() }

    @Test fun jaliDone() = shot("jali_done", argLang()) {
        com.test.cobacompose.ui.kerachallenge.KeraChallengeDetailScreen(KeraChallenge.JALI, com.test.cobacompose.data.challenge.ChallengeProgress(mapOf(1 to 7)), { false }, {}, {})
    }
    @Test fun mainGameDone() = shot("maingame_done", argLang()) {
        com.test.cobacompose.ui.kerachallenge.KeraMainGameScreen(com.test.cobacompose.data.challenge.ChallengeProgress(mapOf(3 to 7)), {}, {}, {}, {})
    }

    private fun argLang(): String = InstrumentationRegistry.getArguments().getString("lang") ?: "id"
}
