package com.test.cobacompose.ui.kerachallenge

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import com.test.cobacompose.R

/*
 * Teks halaman KeraChallenge yang dulu tergambar di gambar desain. Teksnya sudah dihapus dari gambar
 * (bg_kerachallenge_map, img_kerachallenge_detail_*, img_kerachallenge_maingame*), lalu digambar ulang
 * di sini pada posisi, ukuran, dan warna aslinya (semua angka = piksel gambar 1080 px / 891 px).
 * Judul bergaya logo (Jali, Tala, Main Game, Susun Game) dan nama maskot tetap bagian dari gambar.
 */

private val White = Color.White

/** Pil putih miring "Hari N" di kartu hari. */
@Composable
private fun dayPill(day: Int, left: Int, top: Int, right: Int, bottom: Int, size: Float = 46f) = DesignText(
    text = stringResource(R.string.kc_day_n, day),
    left = left, top = top, right = right, bottom = bottom,
    size = size, color = White, centered = true, align = TextAlign.Center, rotation = -6f
)

/** Tombol kuning "NEXT LEVEL" dengan bayangan oranye. */
@Composable
private fun nextLevel(left: Int, top: Int, right: Int, bottom: Int) = DesignText(
    text = stringResource(R.string.kc_next_level),
    left = left, top = top, right = right, bottom = bottom,
    size = 76f, color = White, weight = FontWeight.ExtraBold, centered = true, align = TextAlign.Center,
    shadowColor = Color(0xFFE9A52F), shadowOffsetY = 5f, letterSpacing = 1f
)

/** Judul dua baris kartu hari, mis. "Jelajahi & Kenali". */
private fun cardTitle(text: String, left: Int, top: Int, right: Int, bottom: Int, color: Color, size: Float = 37f) =
    DesignText(text, left, top, right, bottom, size = size, color = color, lineHeight = size * 1.19f)

/** Penjelasan kecil di bawah judul kartu hari. */
private fun cardDesc(text: String, left: Int, top: Int, right: Int, bottom: Int, color: Color, size: Float, lineHeight: Float) =
    DesignText(text, left, top, right, bottom, size = size, color = color, weight = FontWeight.Medium, lineHeight = lineHeight)

/** Teks peta: tulisan "Tantangan" di tiga papan. */
@Composable
internal fun mapTexts(): List<DesignText> {
    val word = stringResource(R.string.kc_tantangan)
    fun sign(left: Int, top: Int, right: Int, bottom: Int, rotation: Float) = DesignText(
        word, left, top, right, bottom, size = 32f, color = White, centered = true, align = TextAlign.Center, rotation = rotation
    )
    return listOf(
        sign(386, 374, 568, 422, -4f),
        sign(422, 1104, 604, 1152, -3f),
        sign(444, 1738, 626, 1790, -6f)
    )
}

@Composable
internal fun KeraChallenge.detailTexts(): List<DesignText> = when (this) {
    KeraChallenge.JALI -> jaliTexts()
    KeraChallenge.TALA -> talaTexts()
}

@Composable
private fun jaliTexts(): List<DesignText> {
    val title = Color(0xFF20094B)
    return listOf(
        DesignText(
            stringResource(R.string.kc_tantangan_n, 1), 120, 180, 392, 262,
            size = 40f, color = White, centered = true, align = TextAlign.Center, rotation = -4f
        ),
        DesignText(
            stringResource(R.string.kc_days_3), 218, 452, 355, 503,
            size = 38f, color = Color(0xFF330D63), centered = true, align = TextAlign.Center
        ),
        DesignText(
            stringResource(R.string.jali_tagline), 108, 512, 430, 618,
            size = 27f, color = Color(0xFF3E2A6A), weight = FontWeight.Medium, lineHeight = 36f
        ),
        dayPill(1, 52, 660, 214, 742),
        cardTitle(stringResource(R.string.jali_day1_title), 438, 804, 700, 897, title),
        cardDesc(stringResource(R.string.jali_day1_desc), 440, 905, 820, 1020, Color(0xFF635B7C), 23.5f, 34f),
        dayPill(2, 52, 1098, 218, 1178),
        cardTitle(stringResource(R.string.jali_day2_title), 442, 1242, 700, 1342, Color(0xFF3B196E)),
        cardDesc(stringResource(R.string.jali_day2_desc), 445, 1355, 820, 1480, Color(0xFF705B94), 23.5f, 34f),
        dayPill(3, 72, 1548, 234, 1628),
        cardTitle(stringResource(R.string.jali_day3_title), 450, 1680, 700, 1766, Color(0xFF21094D)),
        cardDesc(stringResource(R.string.jali_day3_desc), 452, 1775, 840, 1880, Color(0xFF72568E), 23.5f, 33f),
        nextLevel(240, 2006, 850, 2098)
    )
}

@Composable
private fun talaTexts(): List<DesignText> = listOf(
    DesignText(
        stringResource(R.string.kc_tantangan_n, 2), 206, 190, 466, 256,
        size = 38f, color = White, centered = true, align = TextAlign.Center, rotation = -3f
    ),
    DesignText(
        stringResource(R.string.kc_days_3), 300, 476, 470, 534,
        size = 40f, color = Color(0xFF144123), centered = true, align = TextAlign.Center
    ),
    dayPill(1, 76, 590, 240, 666),
    cardTitle(stringResource(R.string.tala_day1_title), 630, 654, 960, 762, Color(0xFF1A4429), size = 44f),
    cardDesc(stringResource(R.string.tala_day1_desc), 632, 766, 960, 866, Color(0xFF66736A), 26f, 33f),
    dayPill(2, 76, 1060, 244, 1140),
    cardTitle(stringResource(R.string.tala_day2_title), 630, 1104, 960, 1212, Color(0xFF174228), size = 44f),
    cardDesc(stringResource(R.string.tala_day2_desc), 632, 1218, 960, 1328, Color(0xFF66706A), 26f, 33f),
    DesignText(
        stringResource(R.string.tala_day2_note), 506, 1357, 652, 1402,
        size = 14f, color = Color(0xFFD59B75), weight = FontWeight.Medium, lineHeight = 18f
    ),
    dayPill(3, 80, 1550, 254, 1634),
    cardTitle(stringResource(R.string.tala_day3_title), 636, 1594, 960, 1708, Color(0xFF1F5425), size = 44f),
    cardDesc(stringResource(R.string.tala_day3_desc), 638, 1714, 990, 1818, Color(0xFF6F7B6E), 26f, 33f),
    nextLevel(250, 2026, 860, 2112)
)

@Composable
internal fun mainGameTexts(): List<DesignText> {
    val title = Color(0xFF341D7D)
    val desc = Color(0xFF7A6AA8)
    return listOf(
        DesignText(
            stringResource(R.string.kc_tantangan_n, 3), 200, 130, 450, 190,
            size = 34f, color = White, centered = true, align = TextAlign.Center
        ),
        DesignText(
            stringResource(R.string.kc_days_3), 270, 410, 420, 466,
            size = 40f, color = Color(0xFF351C7A), centered = true, align = TextAlign.Center
        ),
        DesignText(
            stringResource(R.string.mg_tagline), 112, 480, 512, 554,
            size = 23f, color = Color(0xFF463280), weight = FontWeight.Medium, align = TextAlign.Center,
            lineHeight = 30f, italic = true
        ),
        dayPill(1, 60, 586, 220, 660, size = 44f),
        cardTitle(stringResource(R.string.mg_day1_title), 488, 733, 700, 828, title),
        cardDesc(stringResource(R.string.mg_day1_desc), 488, 834, 760, 903, desc, 22f, 30f),
        dayPill(2, 64, 1036, 240, 1110, size = 44f),
        DesignText(
            stringResource(R.string.mg_level_up), 340, 1100, 436, 1160,
            size = 22f, color = White, centered = true, align = TextAlign.Center, lineHeight = 25f, rotation = -5f
        ),
        cardTitle(stringResource(R.string.mg_day2_title), 488, 1163, 700, 1262, title),
        cardDesc(stringResource(R.string.mg_day2_desc), 488, 1266, 760, 1333, desc, 22f, 30f),
        dayPill(3, 64, 1506, 230, 1580, size = 44f),
        cardTitle(stringResource(R.string.mg_day3_title), 488, 1618, 700, 1707, title),
        cardDesc(stringResource(R.string.mg_day3_desc), 488, 1706, 780, 1803, desc, 22f, 30f)
    )
}

/** Header Susun Game (gambar 891 px). */
@Composable
internal fun susunHeaderTexts(): List<DesignText> = listOf(
    DesignText(
        stringResource(R.string.kc_tantangan_n, 3), 158, 128, 392, 195,
        size = 34f, color = White, centered = true, align = TextAlign.Center, rotation = -3f
    ),
    DesignText(
        stringResource(R.string.kc_day_n, 1), 258, 384, 368, 432,
        size = 32f, color = Color(0xFF351C7A), centered = true, align = TextAlign.Center
    ),
    DesignText(
        stringResource(R.string.mg_day1_desc), 112, 448, 384, 527,
        size = 23f, color = Color(0xFF463280), weight = FontWeight.Medium, align = TextAlign.Center,
        lineHeight = 31f, italic = true
    )
)

/** Header Teka-Teki Silang (gambar 891 px). */
@Composable
internal fun ttsHeaderTexts(): List<DesignText> = listOf(
    DesignText(
        stringResource(R.string.kc_tantangan_n, 3), 156, 112, 354, 180,
        size = 32f, color = White, centered = true, align = TextAlign.Center, rotation = -5f
    ),
    DesignText(
        stringResource(R.string.kc_day_n, 2), 224, 334, 344, 386,
        size = 32f, color = Color(0xFF351C7A), centered = true, align = TextAlign.Center
    ),
    DesignText(
        stringResource(R.string.tts_tagline), 90, 388, 438, 466,
        size = 21.5f, color = Color(0xFF463280), weight = FontWeight.Medium, align = TextAlign.Center,
        lineHeight = 27f, italic = true
    ),
    DesignText(
        stringResource(R.string.tts_bubble), 740, 178, 854, 294,
        size = 20f, color = White, weight = FontWeight.SemiBold, centered = true, align = TextAlign.Center,
        lineHeight = 23f, italic = true
    )
)
