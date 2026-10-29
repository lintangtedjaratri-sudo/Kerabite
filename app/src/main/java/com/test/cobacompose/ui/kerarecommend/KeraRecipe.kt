package com.test.cobacompose.ui.kerarecommend

import androidx.annotation.DrawableRes
import com.test.cobacompose.R

/** Resep olahan pangan lokal di bagian KeraRecommend (card di Home dan halaman detail resep). */
enum class KeraRecipe(
    val key: String,
    val cardTitle: String,
    @DrawableRes val cardImageRes: Int,
    @DrawableRes val badgeRes: Int
) {
    UBEE_CHEESE_BREAD(
        key = "ubee",
        cardTitle = "ubee cheese bread",
        cardImageRes = R.drawable.img_kerarecommend_ubee_hero,
        badgeRes = R.drawable.img_kerarecommend_badge_1
    ),
    GOGUMAPPANG(
        key = "gogumappang",
        cardTitle = "gogumappang",
        cardImageRes = R.drawable.img_kerarecommend_food_2,
        badgeRes = R.drawable.img_kerarecommend_badge_2
    ),
    TARO_CROQUETTE(
        key = "taro",
        cardTitle = "Taro Cheese Melt Croquette",
        cardImageRes = R.drawable.img_kerarecommend_food_4,
        badgeRes = R.drawable.img_kerarecommend_badge_1
    )
}
