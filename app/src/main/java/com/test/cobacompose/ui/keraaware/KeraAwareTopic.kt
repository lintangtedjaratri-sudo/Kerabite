package com.test.cobacompose.ui.keraaware

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import com.test.cobacompose.R

/**
 * Topik KeraAware: satu keluhan/penyakit dan pangan lokal yang membantu mencegahnya.
 * Dipakai untuk kartu di KeraAwareScreen dan isi KeraAwareDetailScreen.
 */
enum class KeraAwareTopic(
    val key: String,
    @StringRes val cardTitleRes: Int,
    @DrawableRes val cardImageRes: Int,
    /** true = foto kartu gelap, judul memakai panel gelap dan teks putih. */
    val darkCard: Boolean,
    @DrawableRes val heroRes: Int,
    /** Nama pangan yang ditebalkan di awal judul detail. */
    @StringRes val foodNameRes: Int,
    @StringRes val detailTitleRes: Int,
    @StringRes val detailSubtitleRes: Int?,
    @DrawableRes val infographicRes: Int
) {
    DIABETES(
        key = "diabetes",
        cardTitleRes = R.string.aware_diabetes_card,
        cardImageRes = R.drawable.img_keraaware_diabetes,
        darkCard = false,
        heroRes = R.drawable.img_keraaware_diabetes,
        foodNameRes = R.string.aware_diabetes_food,
        detailTitleRes = R.string.aware_diabetes_title,
        detailSubtitleRes = R.string.aware_diabetes_subtitle,
        infographicRes = R.drawable.img_keraaware_diabetes_info
    ),
    HEART(
        key = "jantung",
        cardTitleRes = R.string.aware_heart_card,
        cardImageRes = R.drawable.img_keraaware_heart,
        darkCard = true,
        heroRes = R.drawable.img_keraaware_heart,
        foodNameRes = R.string.aware_heart_food,
        detailTitleRes = R.string.aware_heart_title,
        detailSubtitleRes = R.string.aware_heart_subtitle,
        infographicRes = R.drawable.img_keraaware_heart_info
    ),
    DIGESTION(
        key = "pencernaan",
        cardTitleRes = R.string.aware_digestion_card,
        cardImageRes = R.drawable.img_keraaware_stomach_card,
        darkCard = false,
        heroRes = R.drawable.img_keraaware_stomach,
        foodNameRes = R.string.aware_digestion_food,
        detailTitleRes = R.string.aware_digestion_title,
        detailSubtitleRes = null,
        infographicRes = R.drawable.img_keraaware_stomach_info
    ),
    WEIGHT(
        key = "beratbadan",
        cardTitleRes = R.string.aware_weight_card,
        cardImageRes = R.drawable.img_keraaware_weight_card,
        darkCard = true,
        heroRes = R.drawable.img_keraaware_weight,
        foodNameRes = R.string.aware_weight_food,
        detailTitleRes = R.string.aware_weight_title,
        detailSubtitleRes = null,
        infographicRes = R.drawable.img_keraaware_weight_info
    )
}
