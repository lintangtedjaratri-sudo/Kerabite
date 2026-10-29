package com.test.cobacompose.ui.kerachallenge

import com.test.cobacompose.R
import androidx.compose.ui.res.stringResource
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalResources
import com.test.cobacompose.data.challenge.ChallengeProgress
import com.test.cobacompose.ui.components.pinchToZoom
import com.test.cobacompose.ui.components.rememberPinchZoomState

/**
 * Detail satu tantangan: rencana 3 hari dalam satu gambar penuh, bisa di-zoom dengan cubit.
 * Menekan kartu hari membuka halaman tugas hari itu; hari selesai (+1 bintang) dari halaman tugasnya.
 * "NEXT LEVEL" terkunci sampai 3 bintang.
 */
@Composable
fun KeraChallengeDetailScreen(
    challenge: KeraChallenge,
    progress: ChallengeProgress,
    onDayClick: (day: Int) -> Unit,
    onNextLevelClick: () -> Unit,
    onBackClick: () -> Unit
) {
    val context = LocalContext.current
    val resources = LocalResources.current
    val zoomState = rememberPinchZoomState()
    val stars = progress.stars(challenge.number)
    val completed = stars >= ChallengeProgress.MAX_STARS
    val next = challenge.nextLevelArea
    // Jali bernuansa ungu-pink, Tala hijau; pil "Selesai" selalu hijau
    val pillTheme = if (challenge == KeraChallenge.TALA) DayPillTheme.GREEN else DayPillTheme.PURPLE

    val overlays = buildList {
        challenge.dayPillAreas.forEachIndexed { index, area ->
            val done = progress.isDayDone(challenge.number, index + 1)
            add(ImageOverlay(area.inflate()) { DayStatusPill(done = done, theme = pillTheme) })
        }
        if (!completed) add(ImageOverlay(next) { LockedVeil() })
    }

    val hotspots = buildList {
        challenge.dayCardAreas.forEachIndexed { index, area ->
            val day = index + 1
            add(ImageHotspot(area.left, area.top, area.right, area.bottom, resources.getString(R.string.kc_open_day_n, day)) {
                onDayClick(day)
            })
        }
        add(ImageHotspot(next.left, next.top, next.right, next.bottom, resources.getString(R.string.kc_next_level)) {
            if (completed) {
                onNextLevelClick()
            } else {
                Toast.makeText(
                    context,
                    resources.getString(R.string.kc_locked_next, stars),
                    Toast.LENGTH_SHORT
                ).show()
            }
        })
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .pinchToZoom(zoomState)
                .verticalScroll(rememberScrollState())
        ) {
            HotspotImage(
                imageRes = challenge.detailImageRes,
                contentDescription = stringResource(challenge.titleRes),
                hotspots = hotspots,
                overlays = overlays,
                texts = challenge.detailTexts()
            )
        }

        // Di luar area zoom supaya selalu terlihat
        KeraChallengeBackButton(onClick = onBackClick, style = challenge.backButtonStyle)
    }
}
