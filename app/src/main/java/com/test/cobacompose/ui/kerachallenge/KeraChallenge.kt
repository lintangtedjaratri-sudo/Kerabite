package com.test.cobacompose.ui.kerachallenge

import androidx.annotation.StringRes
import androidx.annotation.DrawableRes
import com.test.cobacompose.R

/** Area di gambar desain, dalam pecahan lebar/tinggi gambar (0..1). */
internal data class ImageArea(val left: Float, val top: Float, val right: Float, val bottom: Float)

/**
 * Tantangan KeraChallenge yang detailnya berupa satu gambar rencana 3 hari.
 * [number] = nomor tantangan di peta KeraChallengeScreen (juga kunci progres bintangnya).
 * Menekan kartu hari membuka halaman tugasnya (diatur AppNavGraph): Jali = KeraFYI, peta harta karun,
 * jurnal; Tala = pilih menu, masak + foto, Bagikan & Ajak.
 * Tantangan 3 (Main Game) punya alur sendiri, lihat KeraMainGameScreen.
 */
enum class KeraChallenge(
    val key: String,
    val number: Int,
    @StringRes val titleRes: Int,
    @DrawableRes val detailImageRes: Int,
    /** Warna tombol kembali = warna dasar tantangan. */
    val backButtonStyle: KeraBackButtonStyle,
    /** Letak tombol "NEXT LEVEL" di gambar detail. */
    internal val nextLevelArea: ImageArea,
    /** Kartu Hari 1-3 (seluruh kartu bisa ditekan). */
    internal val dayCardAreas: List<ImageArea>,
    /** Tombol status di tiap kartu hari, ditimpa [DayStatusPill]. */
    internal val dayPillAreas: List<ImageArea>
) {
    JALI(
        key = "jali",
        number = 1,
        titleRes = R.string.kc_title_jali,
        detailImageRes = R.drawable.img_kerachallenge_detail_jali,
        backButtonStyle = KeraBackButtonStyle.PINK,
        nextLevelArea = ImageArea(0.22f, 0.84f, 0.79f, 0.92f),
        dayCardAreas = listOf(
            ImageArea(0.016f, 0.2725f, 0.985f, 0.445f),
            ImageArea(0.016f, 0.46f, 0.985f, 0.6325f),
            ImageArea(0.016f, 0.6525f, 0.985f, 0.82f)
        ),
        dayPillAreas = listOf(
            ImageArea(0.682f, 0.3025f, 0.963f, 0.3525f),
            ImageArea(0.684f, 0.49f, 0.963f, 0.54f),
            ImageArea(0.676f, 0.68f, 0.942f, 0.73f)
        )
    ),
    TALA(
        key = "tala",
        number = 2,
        titleRes = R.string.kc_title_tala,
        detailImageRes = R.drawable.img_kerachallenge_detail_tala,
        backButtonStyle = KeraBackButtonStyle.GREEN,
        nextLevelArea = ImageArea(0.23f, 0.845f, 0.81f, 0.92f),
        dayCardAreas = listOf(
            ImageArea(0.02f, 0.245f, 0.97f, 0.44f),
            ImageArea(0.02f, 0.4525f, 0.97f, 0.6475f),
            ImageArea(0.02f, 0.655f, 0.97f, 0.845f)
        ),
        dayPillAreas = listOf(
            ImageArea(0.584f, 0.3775f, 0.933f, 0.42f),
            ImageArea(0.628f, 0.59f, 0.933f, 0.63f),
            ImageArea(0.593f, 0.786f, 0.933f, 0.8275f)
        )
    )
}

/** Nomor tantangan Main Game (punya halaman hari sendiri). */
internal const val MAIN_GAME_CHALLENGE = 3
