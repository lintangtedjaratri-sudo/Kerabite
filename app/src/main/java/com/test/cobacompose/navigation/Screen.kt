package com.test.cobacompose.navigation

import com.test.cobacompose.ui.keraaware.KeraAwareTopic
import com.test.cobacompose.ui.kerachallenge.KeraChallenge as KeraChallengeItem
import com.test.cobacompose.ui.kerafyi.LocalFood
import com.test.cobacompose.ui.kerarecommend.KeraRecipe

sealed class Screen(val route: String) {
    data object Landing : Screen("landing")
    data object Login : Screen("login")
    data object Register : Screen("register")
    data object ForgotPassword : Screen("forgot_password")
    data object Home : Screen("home")
    data object KeraFyi : Screen("fyidtl_main")
    data object KeraAware : Screen("keraaware_main")
    data object KeraChallenge : Screen("kerachallenge_main")

    /** Tantangan 3 - Main Game dan tiga halaman harinya. */
    data object KeraMainGame : Screen("kerachallenge_maingame")
    data object KeraSusunGame : Screen("kerachallenge_maingame_susun")
    data object KeraTekaTekiSilang : Screen("kerachallenge_maingame_tts")
    data object KeraBagikanAjak : Screen("kerachallenge_maingame_bagikan")

    /** Halaman tugas Jali (Hari 1 = KeraFYI) dan Tala, dibuka dari kartu harinya. */
    data object KeraJaliMap : Screen("kerachallenge_jali_hari2")
    data object KeraJaliJournal : Screen("kerachallenge_jali_hari3")
    data object KeraTalaMenu : Screen("kerachallenge_tala_hari1")
    data object KeraTalaCook : Screen("kerachallenge_tala_hari2")
    data object KeraTalaBagikan : Screen("kerachallenge_tala_hari3")
    data object KeraTalaPanduan : Screen("kerachallenge_tala_panduan")

    /** Halaman penghargaan + unduh sertifikat setelah teka-teki silang selesai. */
    data object KeraCertificate : Screen("kerachallenge_sertifikat")

    /** Satu destinasi per pangan lokal; halaman 1-3 + Map ada di dalam pager-nya. */
    data class KeraFyiDetail(val food: LocalFood) : Screen("fyidtl_${food.key}_1")

    /** Detail KeraFYI yang langsung dibuka di halaman Map (Jali Hari 2). */
    data class KeraFyiMap(val food: LocalFood) : Screen("fyidtl_${food.key}_2")

    /** Satu destinasi per resep KeraRecommend; halaman 1-3 ada di dalam pager-nya. */
    data class KeraRecipeDetail(val recipe: KeraRecipe) : Screen("rcpdtl_${recipe.key}_1")

    /** Detail kartu terakhir KeraRecommend: Menu SPPG berbasis pangan lokal. */
    data object KeraMenuSppg : Screen("rcpdtl_sppg")

    /** Satu destinasi per topik KeraAware (detail infografis). */
    data class KeraAwareDetail(val topic: KeraAwareTopic) : Screen("keraaware_${topic.key}")

    /** Satu destinasi per tantangan KeraChallenge yang sudah punya detail. */
    data class KeraChallengeDetail(val challenge: KeraChallengeItem) :
        Screen("kerachallenge_${challenge.key}")
}
