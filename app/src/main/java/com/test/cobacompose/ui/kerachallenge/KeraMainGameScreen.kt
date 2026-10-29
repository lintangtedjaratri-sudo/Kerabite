package com.test.cobacompose.ui.kerachallenge

import androidx.compose.ui.res.stringResource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.test.cobacompose.R
import com.test.cobacompose.data.challenge.ChallengeProgress
import com.test.cobacompose.ui.theme.KeraChallengeSky

/** Tombol status di kartu Hari 1-3 pada gambar Main Game. */
private val mainGamePillAreas = listOf(
    ImageArea(0.652f, 0.291f, 0.948f, 0.345f),
    ImageArea(0.652f, 0.4775f, 0.948f, 0.533f),
    ImageArea(0.661f, 0.666f, 0.954f, 0.726f)
)

/**
 * Tantangan 3 - Main Game: tiga kartu hari.
 * Hari 1 = Susun Game, Hari 2 = Teka-Teki Silang, Hari 3 = Bagikan & Ajak (lanjut ke Kera Campaign).
 * Tiap hari selesai saat permainannya diselesaikan, dan kartunya berubah jadi "Selesai".
 */
@Composable
fun KeraMainGameScreen(
    progress: ChallengeProgress,
    onSusunGameClick: () -> Unit,
    onTekaTekiSilangClick: () -> Unit,
    onBagikanAjakClick: () -> Unit,
    onBackClick: () -> Unit
) {
    val overlays = mainGamePillAreas.mapIndexed { index, area ->
        val done = progress.isDayDone(MAIN_GAME_CHALLENGE, index + 1)
        ImageOverlay(area.inflate()) { DayStatusPill(done = done, theme = DayPillTheme.PURPLE) }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        HotspotImagePage(
            imageRes = R.drawable.img_kerachallenge_maingame,
            contentDescription = stringResource(R.string.kc_title_main_game),
            topColor = KeraChallengeSky,
            hotspots = listOf(
                // Seluruh kartu hari bisa ditekan, termasuk tombol "Mulai Tantangan"
                ImageHotspot(0.03f, 0.25f, 0.97f, 0.40f, stringResource(R.string.mg_day1_label), onSusunGameClick),
                ImageHotspot(0.03f, 0.435f, 0.97f, 0.59f, stringResource(R.string.mg_day2_label), onTekaTekiSilangClick),
                ImageHotspot(0.03f, 0.635f, 0.97f, 0.78f, stringResource(R.string.mg_day3_label), onBagikanAjakClick)
            ),
            overlays = overlays,
            texts = mainGameTexts()
        )

        // Ukuran dan posisi sama dengan tombol kembali di semua halaman KeraChallenge
        KeraChallengeBackButton(onClick = onBackClick, style = KeraBackButtonStyle.PURPLE)
    }
}
