package com.test.cobacompose.ui.kerachallenge

import androidx.compose.ui.res.stringResource
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalResources
import com.test.cobacompose.R
import com.test.cobacompose.data.challenge.ChallengeProgress

/** Gambar bintang & tombol per tantangan untuk peta (posisi dalam pecahan gambar peta). */
private class MapChallengeArt(
    /** Bintang yang digambar saat keadaannya berbeda dari yang tergambar di peta. */
    val stars: List<Pair<Int, ImageArea>>,
    /** true = bintang di peta emas (ditimpa perak kalau belum didapat), false = perak (ditimpa emas). */
    val bakedStarsGold: Boolean,
    /** Tombol berwarna saat terbuka; null = tombol di peta sudah berwarna. */
    val unlockedButton: Pair<Int, ImageArea>?,
    /** Letak gembok saat terkunci. */
    val lockArea: ImageArea?,
    val hotspot: ImageArea
)

// Gambar bintang dan tombol dibuat dari peta itu sendiri (diwarnai ulang), jadi posisinya pas
private val mapArt = mapOf(
    1 to MapChallengeArt(
        stars = listOf(
            R.drawable.img_kerachallenge_star_off_1_1 to ImageArea(0.4815f, 0.7903f, 0.5417f, 0.8263f),
            R.drawable.img_kerachallenge_star_off_1_2 to ImageArea(0.5398f, 0.7886f, 0.6000f, 0.8169f),
            R.drawable.img_kerachallenge_star_off_1_3 to ImageArea(0.5981f, 0.7963f, 0.6583f, 0.8258f)
        ),
        bakedStarsGold = true,
        unlockedButton = null,
        lockArea = null,
        hotspot = ImageArea(0.36f, 0.71f, 0.70f, 0.885f)
    ),
    2 to MapChallengeArt(
        stars = listOf(
            R.drawable.img_kerachallenge_star_on_2_1 to ImageArea(0.2565f, 0.5092f, 0.3259f, 0.5421f),
            R.drawable.img_kerachallenge_star_on_2_2 to ImageArea(0.3241f, 0.4994f, 0.3981f, 0.5344f),
            R.drawable.img_kerachallenge_star_on_2_3 to ImageArea(0.3935f, 0.5092f, 0.4611f, 0.5421f)
        ),
        bakedStarsGold = false,
        unlockedButton = R.drawable.img_kerachallenge_button_on_2 to ImageArea(0.2120f, 0.5302f, 0.5278f, 0.6213f),
        lockArea = ImageArea(0.435f, 0.566f, 0.515f, 0.6028f),
        hotspot = ImageArea(0.21f, 0.445f, 0.59f, 0.63f)
    ),
    3 to MapChallengeArt(
        stars = listOf(
            R.drawable.img_kerachallenge_star_on_3_1 to ImageArea(0.4287f, 0.2088f, 0.4917f, 0.2396f),
            R.drawable.img_kerachallenge_star_on_3_2 to ImageArea(0.4880f, 0.2041f, 0.5565f, 0.2311f),
            R.drawable.img_kerachallenge_star_on_3_3 to ImageArea(0.5528f, 0.2075f, 0.6176f, 0.2383f)
        ),
        bakedStarsGold = false,
        unlockedButton = R.drawable.img_kerachallenge_button_on_3 to ImageArea(0.3269f, 0.2285f, 0.6583f, 0.3119f),
        lockArea = ImageArea(0.565f, 0.258f, 0.645f, 0.2948f),
        hotspot = ImageArea(0.32f, 0.13f, 0.67f, 0.31f)
    )
)

/**
 * Peta KeraChallenge. Tiap tantangan menampilkan bintangnya (1 bintang per hari selesai);
 * tantangan berikutnya terbuka setelah tantangan sebelumnya mendapat 3 bintang.
 * Dibuka dari bawah (Tantangan 1).
 */
@Composable
fun KeraChallengeScreen(
    progress: ChallengeProgress,
    onChallengeClick: (KeraChallenge) -> Unit,
    onMainGameClick: () -> Unit,
    onBackClick: () -> Unit
) {
    val context = LocalContext.current
    val resources = LocalResources.current
    val scrollState = rememberScrollState()

    // Mulai dari bawah peta, tempat Tantangan 1 berada
    LaunchedEffect(scrollState.maxValue) {
        scrollState.scrollTo(scrollState.maxValue)
    }

    val overlays = buildList {
        mapArt.forEach { (number, art) ->
            val unlocked = progress.isUnlocked(number)
            val stars = progress.stars(number)

            art.unlockedButton?.let { (res, area) -> if (unlocked) add(drawableOverlay(res, area)) }
            art.stars.forEachIndexed { index, (res, area) ->
                val earned = index < stars
                // Tumpuk gambar hanya kalau keadaan bintang berbeda dari yang tergambar di peta
                if (earned != art.bakedStarsGold) add(drawableOverlay(res, area))
            }
            if (!unlocked) art.lockArea?.let { add(ImageOverlay(it) { LockBadge() }) }
        }
    }

    val hotspots = mapArt.map { (number, art) ->
        val area = art.hotspot
        ImageHotspot(area.left, area.top, area.right, area.bottom, resources.getString(R.string.kc_open_challenge_n, number)) {
            when {
                !progress.isUnlocked(number) -> Toast.makeText(
                    context,
                    resources.getString(R.string.kc_locked_challenge, number - 1, number),
                    Toast.LENGTH_SHORT
                ).show()

                number == MAIN_GAME_CHALLENGE -> onMainGameClick()
                else -> onChallengeClick(KeraChallenge.entries.first { it.number == number })
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
        ) {
            HotspotImage(
                imageRes = R.drawable.bg_kerachallenge_map,
                contentDescription = stringResource(R.string.kc_map_description),
                hotspots = hotspots,
                overlays = overlays,
                texts = mapTexts()
            )
        }

        // Tetap terlihat saat peta digulir
        KeraChallengeBackButton(onClick = onBackClick, style = KeraBackButtonStyle.WHITE)
    }
}
