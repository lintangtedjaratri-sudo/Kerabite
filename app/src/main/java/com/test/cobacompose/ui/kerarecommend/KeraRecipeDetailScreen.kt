package com.test.cobacompose.ui.kerarecommend

import android.net.Uri
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.imageResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.test.cobacompose.R
import com.test.cobacompose.ui.components.pinchToZoom
import com.test.cobacompose.ui.kerachallenge.DesignText
import com.test.cobacompose.ui.kerachallenge.DesignTextImage
import com.test.cobacompose.ui.components.rememberPinchZoomState
import com.test.cobacompose.ui.kerafyi.FyiBullet
import com.test.cobacompose.ui.kerafyi.FyiNextButton
import com.test.cobacompose.ui.kerafyi.FyiNutrition
import com.test.cobacompose.ui.kerafyi.GiziNutritionSection
import com.test.cobacompose.ui.kerafyi.Nutrient
import com.test.cobacompose.ui.theme.KeraFyiShadow
import com.test.cobacompose.ui.theme.KeraRecipeBackground
import com.test.cobacompose.ui.theme.KeraRecipeCream
import com.test.cobacompose.ui.theme.KeraRecipeGiziBackground
import com.test.cobacompose.ui.theme.KeraRecipePanel
import com.test.cobacompose.ui.theme.KeraRecipeTitle
import kotlinx.coroutines.launch

private const val RecipePageCount = 3

/**
 * Detail resep KeraRecommend dalam satu pager berisi 3 halaman (rcpdtl_<key>_1..3):
 * bahan & alat, langkah-langkah, lalu informasi nilai gizi.
 */
@Composable
fun KeraRecipeDetailScreen(
    recipe: KeraRecipe,
    onBackToHome: () -> Unit
) {
    val content = recipeContent(recipe)
    val pagerState = rememberPagerState(pageCount = { RecipePageCount })
    val coroutineScope = rememberCoroutineScope()
    val zoomState = rememberPinchZoomState()

    // Zoom kembali normal setiap pindah halaman
    LaunchedEffect(pagerState.currentPage) { zoomState.reset() }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(KeraRecipeBackground)
    ) {
        HorizontalPager(
            state = pagerState,
            // Saat halaman di-zoom, swipe pager dinonaktifkan supaya tidak pindah halaman tanpa sengaja
            userScrollEnabled = !zoomState.isZoomed,
            modifier = Modifier
                .fillMaxSize()
                .pinchToZoom(zoomState)
        ) { page ->
            val onNextClick: () -> Unit = {
                coroutineScope.launch { pagerState.animateScrollToPage(page + 1) }
            }
            when (page) {
                0 -> RecipeIngredientsPage(content, onNextClick)
                1 -> RecipeStepsPage(content, onNextClick)
                else -> RecipeNutritionPage(content, onNextClick = onBackToHome)
            }
        }

        // Tombol kembali melayang di atas foto
        Box(
            modifier = Modifier
                .statusBarsPadding()
                .padding(start = 16.dp, top = 12.dp)
                .size(44.dp)
                .clip(CircleShape)
                .background(Color.White.copy(alpha = 0.85f))
                .clickable {
                    if (pagerState.currentPage > 0) {
                        coroutineScope.launch {
                            pagerState.animateScrollToPage(pagerState.currentPage - 1)
                        }
                    } else {
                        onBackToHome()
                    }
                },
            contentAlignment = Alignment.Center
        ) {
            Image(
                painter = painterResource(R.drawable.ic_arrow_back),
                contentDescription = stringResource(R.string.common_back),
                modifier = Modifier.size(26.dp)
            )
        }
    }
}

/** Isi satu resep: teks bahan/alat, gambar langkah, dan nilai gizi per sajian. */
private class RecipeContent(
    @DrawableRes val heroRes: Int,
    val name: String,
    @StringRes val tagline: Int,
    val ingredientsLeft: List<Int>,
    val ingredientsRight: List<Int>,
    val tools: List<Int>,
    @DrawableRes val stepsRes: Int,
    /** Teks langkah yang dulu tergambar di [stepsRes], digambar ulang di atasnya. */
    val stepTexts: List<StepText>,
    val stepsBackground: Color,
    val nutrition: FyiNutrition
)

/**
 * Satu teks di gambar langkah. Teksnya sudah dihapus dari gambar; kotak [left]..[bottom], [size] dan
 * [lineHeight] dalam piksel gambar. Teks diletakkan di tengah kotak secara vertikal.
 */
private class StepText(
    @StringRes val res: Int,
    val left: Int,
    val top: Int,
    val right: Int,
    val bottom: Int,
    val color: Color,
    val lineHeight: Float,
    val size: Float = 21f,
    val weight: FontWeight = FontWeight.Medium,
    /** true untuk tulisan di balon (rata tengah). */
    val center: Boolean = false,
    val rotation: Float = 0f
)

/** Enam langkah di kolom teks [left]..[right]; [rows] = rentang vertikal tiap baris kartu langkah. */
private fun stepColumn(
    steps: List<Int>,
    left: Int,
    right: Int,
    rows: List<Pair<Int, Int>>,
    color: Color,
    lineHeight: Float
) = steps.zip(rows) { res, (top, bottom) -> StepText(res, left, top, right, bottom, color, lineHeight) }

@Composable
private fun StepText.toDesignText() = DesignText(
    stringResource(res), left, top, right, bottom,
    size = size, color = color, weight = weight, lineHeight = lineHeight, rotation = rotation,
    align = if (center) TextAlign.Center else TextAlign.Start, centered = true
)

private fun recipeContent(recipe: KeraRecipe): RecipeContent = when (recipe) {
    KeraRecipe.UBEE_CHEESE_BREAD -> ubeeContent
    KeraRecipe.GOGUMAPPANG -> gogumappangContent
    KeraRecipe.TARO_CROQUETTE -> taroContent
}

/** Ikon gizi resep mengikuti desain: daun ungu, telur pink, gandum, tetes, dan garam. */
private fun recipeNutrients(
    fat: Pair<String, String>,
    protein: Pair<String, String>,
    carb: Pair<String, String>,
    fiber: Pair<String, String>,
    sugar: Pair<String, String>,
    sodium: Pair<String, String>
) = listOf(
    Nutrient(R.string.fyi_lemak_total, fat.first, fat.second, R.drawable.ic_kerafyi_gizi_leaf_purple),
    Nutrient(R.string.fyi_protein, protein.first, protein.second, R.drawable.ic_kerafyi_gizi_protein_talas),
    Nutrient(R.string.fyi_karbohidrat_total, carb.first, carb.second, R.drawable.ic_kerafyi_gizi_carb),
    Nutrient(R.string.fyi_serat_kasar, fiber.first, fiber.second, null, isSub = true),
    Nutrient(R.string.fyi_gula, sugar.first, sugar.second, R.drawable.ic_kerafyi_gizi_drop),
    Nutrient(R.string.fyi_natrium, sodium.first, sodium.second, R.drawable.ic_kerafyi_gizi_sodium)
)

private val ubeeContent = RecipeContent(
    heroRes = R.drawable.img_kerarecommend_ubee_hero,
    name = "Ube Cheese Bread",
    tagline = R.string.rcp_roti_ubi_ungu_dengan_isian,
    ingredientsLeft = listOf(
        R.string.rcp_90_g_tepung_terigu_protein,
        R.string.rcp_55_g_susu_cair_uht,
        R.string.rcp_10_g_butter_lelehkan,
        R.string.rcp_sdt_garam
    ),
    ingredientsRight = listOf(
        R.string.rcp_200_g_ubi_ungu_kukus,
        R.string.rcp_2_sdm_susu_kental_manis,
        R.string.rcp_keju_mozzarella_secukupnya,
        R.string.rcp_sedikit_butter_untuk_memanggang
    ),
    tools = listOf(
        R.string.rcp_mangkuk,
        R.string.rcp_rolling_pin,
        R.string.rcp_teflon_wajan_anti_lengket,
        R.string.rcp_spatula,
        R.string.rcp_plastik_wrap_atau_kain_bersih
    ),
    stepsRes = R.drawable.img_kerarecommend_ubee_steps,
    // Gambar 800 x 810 px
    stepTexts = stepColumn(
        steps = listOf(
            R.string.rcp_ubee_step_1, R.string.rcp_ubee_step_2, R.string.rcp_ubee_step_3,
            R.string.rcp_ubee_step_4, R.string.rcp_ubee_step_5, R.string.rcp_ubee_step_6
        ),
        left = 121,
        right = 470,
        rows = listOf(18 to 131, 159 to 249, 277 to 391, 419 to 535, 563 to 651, 679 to 795),
        color = Color(0xFF34335A),
        lineHeight = 29.5f
    ),
    stepsBackground = KeraRecipeBackground,
    nutrition = FyiNutrition(
        servingIconRes = R.drawable.ic_kerafyi_gizi_scale,
        energy = "710 KKal",
        energyFromFat = "162 Kkal",
        nutrients = recipeNutrients(
            fat = "18 g" to "30%",
            protein = "22 g" to "34%",
            carb = "115 g" to "35%",
            fiber = "8 g" to "27%",
            sugar = "20 g" to "40%",
            sodium = "600 mg" to "46%"
        ),
        showAkgLabel = true,
        servingSize = "436 gram"
    )
)

private val gogumappangContent = RecipeContent(
    heroRes = R.drawable.img_kerarecommend_gogumappang_hero,
    name = "Goguma Ppang",
    tagline = R.string.rcp_roti_korea_bertekstur_chewy_dengan,
    ingredientsLeft = listOf(
        R.string.rcp_150_g_ubi_jalar_kukus,
        R.string.rcp_100_g_tepung_tapioka,
        R.string.rcp_30_g_tepung_terigu,
        R.string.rcp_30_g_gula_pasir,
        R.string.rcp_30_g_butter_lelehkan
    ),
    ingredientsRight = listOf(
        R.string.rcp_1_butir_telur,
        R.string.rcp_50_ml_susu_cair,
        R.string.rcp_20_g_susu_kental_manis,
        R.string.rcp_sdt_garam_2
    ),
    tools = listOf(
        R.string.rcp_mangkuk,
        R.string.rcp_garpu_alat_penumbuk,
        R.string.rcp_spatula,
        R.string.rcp_loyang,
        R.string.rcp_baking_paper,
        R.string.rcp_oven
    ),
    stepsRes = R.drawable.img_kerarecommend_gogumappang_steps,
    // Gambar 966 x 1062 px; langkah 6 paling panjang, jadi barisnya lebih rapat
    stepTexts = stepColumn(
        steps = listOf(
            R.string.rcp_goguma_step_1, R.string.rcp_goguma_step_2, R.string.rcp_goguma_step_3,
            R.string.rcp_goguma_step_4, R.string.rcp_goguma_step_5
        ),
        left = 144,
        right = 488,
        rows = listOf(24 to 147, 175 to 284, 312 to 474, 502 to 637, 665 to 762),
        color = Color(0xFF473479),
        lineHeight = 32.5f
    ) + listOf(
        StepText(R.string.rcp_goguma_step_6, 144, 790, 488, 966, Color(0xFF473479), lineHeight = 28f),
        // Balon ungu miring dan balon lavender di bawah kartu
        StepText(
            R.string.rcp_goguma_note_1, 156, 982, 326, 1044, Color.White, lineHeight = 26f,
            size = 22f, weight = FontWeight.SemiBold, center = true, rotation = -8f
        ),
        StepText(R.string.rcp_goguma_note_2, 560, 988, 852, 1032, Color(0xFF94509A), lineHeight = 25f, center = true)
    ),
    stepsBackground = KeraRecipeBackground,
    nutrition = FyiNutrition(
        servingIconRes = R.drawable.ic_kerafyi_gizi_scale,
        energy = "210 KKal",
        energyFromFat = "54 Kkal",
        nutrients = recipeNutrients(
            fat = "6 g" to "10%",
            protein = "4 g" to "6%",
            carb = "35 g" to "10%",
            fiber = "2 g" to "7%",
            sugar = "8 g" to "16%",
            sodium = "200 mg" to "15%"
        ),
        showAkgLabel = true,
        servingSize = "77 gram"
    )
)

private val taroContent = RecipeContent(
    heroRes = R.drawable.img_kerarecommend_taro_hero,
    name = "Taro Cheese Melt Croquette",
    tagline = R.string.rcp_kroket_talas_renyah_dengan_isian,
    ingredientsLeft = listOf(
        R.string.rcp_500_g_talas_kukus_haluskan,
        R.string.rcp_2_sdm_butter_margarin,
        R.string.rcp_2_sdm_susu_bubuk_full,
        R.string.rcp_1_butir_kuning_telur,
        R.string.rcp_sdt_garam_2,
        R.string.rcp_sdt_merica_bubuk
    ),
    ingredientsRight = listOf(
        R.string.rcp_sdt_pala_bubuk_opsional,
        R.string.rcp_100_120_g_keju_mozzarella,
        R.string.rcp_3_4_sdm_tepung_terigu,
        R.string.rcp_1_butir_telur_kocok_lepas,
        R.string.rcp_100_g_tepung_panir,
        R.string.rcp_minyak_goreng_secukupnya
    ),
    tools = listOf(
        R.string.rcp_kukusan,
        R.string.rcp_mangkuk,
        R.string.rcp_garpu_alat_penghalus,
        R.string.rcp_wajan,
        R.string.rcp_spatula,
        R.string.rcp_3_wadah_untuk_bahan_pelapis
    ),
    stepsRes = R.drawable.img_kerarecommend_taro_steps,
    // Gambar 984 x 976 px
    stepTexts = stepColumn(
        steps = listOf(
            R.string.rcp_taro_step_1, R.string.rcp_taro_step_2, R.string.rcp_taro_step_3,
            R.string.rcp_taro_step_4, R.string.rcp_taro_step_5, R.string.rcp_taro_step_6
        ),
        left = 136,
        right = 484,
        rows = listOf(17 to 127, 155 to 283, 311 to 444, 472 to 586, 614 to 746, 774 to 872),
        color = Color(0xFF7A3A26),
        lineHeight = 30f
    ) + listOf(
        // Balon oranye miring dan balon peach di bawah kartu
        StepText(
            R.string.rcp_taro_note_1, 198, 902, 386, 938, Color.White, lineHeight = 26f,
            size = 22f, weight = FontWeight.SemiBold, center = true, rotation = -6f
        ),
        StepText(
            R.string.rcp_taro_note_2, 586, 886, 808, 954, Color(0xFFA65A38), lineHeight = 26f,
            size = 19f, center = true, rotation = -3f
        )
    ),
    stepsBackground = KeraRecipeCream,
    nutrition = FyiNutrition(
        servingIconRes = R.drawable.ic_kerafyi_gizi_scale,
        energy = "128 KKal",
        energyFromFat = "54 Kkal",
        nutrients = recipeNutrients(
            fat = "6 g" to "10%",
            protein = "4 g" to "6%",
            carb = "15 g" to "4%",
            fiber = "2 g" to "7%",
            sugar = "1 g" to "2%",
            sodium = "200 mg" to "15%"
        ),
        showAkgLabel = true,
        servingSize = "85 gram"
    )
)

/** Halaman 1 (rcpdtl_<key>_1): nama resep, kartu bahan-bahan, kartu alat, lalu NEXT dan banner maskot. */
@Composable
private fun RecipeIngredientsPage(content: RecipeContent, onNextClick: () -> Unit) {
    RecipeScrollPage(heroRes = content.heroRes, background = KeraRecipeBackground) {
        Column(modifier = Modifier.padding(horizontal = 16.dp)) {
            Spacer(modifier = Modifier.height(20.dp))
            Text(
                text = content.name,
                fontSize = 28.sp,
                lineHeight = 34.sp,
                fontWeight = FontWeight.Bold,
                color = KeraRecipeTitle
            )
            Text(
                text = stringResource(content.tagline),
                fontSize = 15.sp,
                lineHeight = 21.sp,
                color = KeraRecipeTitle
            )
            Spacer(modifier = Modifier.height(24.dp))

            RecipeCard {
                RecipeCardHeading(title = stringResource(R.string.rcp_ingredients))
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    RecipeBulletList(content.ingredientsLeft, Modifier.weight(1f))
                    RecipeBulletList(content.ingredientsRight, Modifier.weight(1f))
                }
            }
            Spacer(modifier = Modifier.height(24.dp))
            RecipeCard {
                RecipeCardHeading(title = stringResource(R.string.rcp_tools))
                RecipeBulletList(content.tools)
            }
        }

        RecipePageFooter(onNextClick)
    }
}

/** Halaman 2 (rcpdtl_<key>_2): label Langkah-Langkah, gambar langkah, link video tutorial, lalu NEXT dan banner maskot. */
@Composable
private fun RecipeStepsPage(content: RecipeContent, onNextClick: () -> Unit) {
    val uriHandler = LocalUriHandler.current

    RecipeScrollPage(heroRes = content.heroRes, background = content.stepsBackground) {
        // Tulisan di coretan ungu digambar ulang (gambar 798 x 271 px), topi koki tetap bagian dari gambar
        val stepsLabel = stringResource(R.string.rcp_steps)
        DesignTextImage(
            bitmap = ImageBitmap.imageResource(R.drawable.img_kerarecommend_label_steps),
            contentDescription = stepsLabel,
            texts = listOf(
                DesignText(
                    stepsLabel, 192, 96, 668, 184,
                    size = 56f, color = Color.White, centered = true, align = TextAlign.Center, rotation = -2f
                )
            ),
            modifier = Modifier
                .padding(top = 16.dp, start = 4.dp)
                .fillMaxWidth(0.68f)
        )
        Spacer(modifier = Modifier.height(8.dp))
        DesignTextImage(
            bitmap = ImageBitmap.imageResource(content.stepsRes),
            contentDescription = stringResource(R.string.rcp_steps_of, content.name),
            texts = content.stepTexts.map { it.toDesignText() },
            modifier = Modifier
                .padding(horizontal = 8.dp)
                .fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(12.dp))

        val searchQuery = stringResource(R.string.rcp_search_query, content.name)
        RecipeCard(modifier = Modifier.padding(horizontal = 16.dp)) {
            Text(
                text = stringResource(R.string.rcp_tutorial),
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black
            )
            Text(
                text = stringResource(R.string.rcp_watch_video),
                modifier = Modifier.clickable {
                    // Belum ada video resmi, jadi buka hasil pencarian YouTube untuk resep ini
                    val query = Uri.encode(searchQuery)
                    runCatching {
                        uriHandler.openUri("https://www.youtube.com/results?search_query=$query")
                    }
                },
                fontSize = 15.sp,
                textDecoration = TextDecoration.Underline,
                color = KeraRecipeTitle
            )
        }

        RecipePageFooter(onNextClick)
    }
}

/** Halaman 3 (rcpdtl_<key>_3): informasi nilai gizi per sajian, lalu NEXT (kembali ke Home) dan banner maskot. */
@Composable
private fun RecipeNutritionPage(content: RecipeContent, onNextClick: () -> Unit) {
    RecipeScrollPage(heroRes = content.heroRes, background = KeraRecipeGiziBackground) {
        Spacer(modifier = Modifier.height(20.dp))
        GiziNutritionSection(nutrition = content.nutrition)
        RecipePageFooter(onNextClick)
    }
}

/** Penutup setiap halaman resep: tombol NEXT tepat di atas banner tiga maskot. */
@Composable
private fun RecipePageFooter(onNextClick: () -> Unit) {
    FyiNextButton(onClick = onNextClick, showMascots = false)
    Image(
        painter = painterResource(R.drawable.img_kerafyi_mascot_banner),
        contentDescription = null,
        modifier = Modifier
            .padding(top = 8.dp)
            .fillMaxWidth()
            .aspectRatio(1080f / 620f)
    )
}

/** Halaman yang bisa di-scroll: foto resep di paling atas (di belakang status bar) lalu isi halaman. */
@Composable
private fun RecipeScrollPage(
    @DrawableRes heroRes: Int,
    background: Color,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(background)
            .verticalScroll(rememberScrollState())
            .navigationBarsPadding()
            .padding(bottom = 24.dp)
    ) {
        Image(
            painter = painterResource(heroRes),
            contentDescription = null,
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1.49f)
                .clip(RoundedCornerShape(bottomStart = 24.dp, bottomEnd = 24.dp)),
            contentScale = ContentScale.Crop
        )
        content()
    }
}

/** Kartu pink dengan bayangan abu di kanan bawah, dipakai untuk bahan, alat, dan video tutorial. */
@Composable
private fun RecipeCard(
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    val shape = RoundedCornerShape(24.dp)
    Box(modifier = modifier.fillMaxWidth()) {
        Box(
            modifier = Modifier
                .matchParentSize()
                .offset(x = 6.dp, y = 8.dp)
                .background(KeraFyiShadow, shape)
        )
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(KeraRecipePanel, shape)
                .padding(start = 14.dp, end = 14.dp, top = 14.dp, bottom = 20.dp),
            content = content
        )
    }
}

@Composable
private fun RecipeCardHeading(title: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Image(
            painter = painterResource(R.drawable.ic_kerarecommend_ingredients),
            contentDescription = null,
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(
            text = title,
            fontSize = 15.sp,
            fontWeight = FontWeight.Bold,
            color = Color.Black
        )
    }
}

@Composable
private fun RecipeBulletList(items: List<Int>, modifier: Modifier = Modifier) {
    Column(modifier = modifier.padding(start = 12.dp, top = 2.dp)) {
        items.forEach { item ->
            FyiBullet(text = AnnotatedString(stringResource(item)), fontSize = 14.sp, color = Color.Black)
        }
    }
}
