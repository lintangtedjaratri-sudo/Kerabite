package com.test.cobacompose.ui.kerafyi

import androidx.activity.compose.BackHandler
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.TextAutoSize
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.FilterQuality
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.imageResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.test.cobacompose.R
import com.test.cobacompose.ui.components.pinchToZoom
import com.test.cobacompose.ui.components.rememberPinchZoomState
import com.test.cobacompose.ui.theme.KeraFyiBenefitCard
import com.test.cobacompose.ui.theme.KeraFyiBenefitPanel
import com.test.cobacompose.ui.theme.KeraFyiBerry
import com.test.cobacompose.ui.theme.KeraFyiGreen
import com.test.cobacompose.ui.theme.KeraFyiGreenDark
import com.test.cobacompose.ui.theme.KeraFyiGreenTitle
import com.test.cobacompose.ui.theme.KeraFyiKenaliPanel
import com.test.cobacompose.ui.theme.KeraFyiKenaliTitle
import com.test.cobacompose.ui.theme.KeraFyiLavenderPanel
import com.test.cobacompose.ui.theme.KeraFyiOlive
import com.test.cobacompose.ui.theme.KeraFyiPanel
import com.test.cobacompose.ui.theme.KeraFyiPlumText
import com.test.cobacompose.ui.theme.KeraFyiShadow
import com.test.cobacompose.ui.theme.KeraFyiText
import com.test.cobacompose.ui.theme.KeraGiziBerry
import com.test.cobacompose.ui.theme.KeraGiziDivider
import com.test.cobacompose.ui.theme.KeraGiziDot
import com.test.cobacompose.ui.theme.KeraGiziLavenderPill
import com.test.cobacompose.ui.theme.KeraGiziLavenderText
import com.test.cobacompose.ui.theme.KeraGiziMagenta
import com.test.cobacompose.ui.theme.KeraGiziNote
import com.test.cobacompose.ui.theme.KeraGiziNoteText
import com.test.cobacompose.ui.theme.KeraGiziPanelEnd
import com.test.cobacompose.ui.theme.KeraGiziPanelStart
import com.test.cobacompose.ui.theme.KeraGiziPink
import com.test.cobacompose.ui.theme.KeraGiziPinkDeep
import com.test.cobacompose.ui.theme.KeraGiziPinkPill
import com.test.cobacompose.ui.theme.KeraGiziPinkText
import com.test.cobacompose.ui.theme.KeraGiziPlum
import com.test.cobacompose.ui.theme.KeraGiziPurple
import com.test.cobacompose.ui.theme.KeraGiziRowDivider
import com.test.cobacompose.ui.theme.KeraGiziTable
import com.test.cobacompose.ui.theme.KeraPurple
import kotlinx.coroutines.launch

private const val ARTICLE_PAGE = 0
private const val MAP_PAGE = 1

/**
 * Detail KeraFYI untuk satu pangan lokal: halaman 1 adalah artikel lengkap yang digulir
 * (deskripsi, karakteristik, manfaat, cara aman mengonsumsi, nilai gizi), halaman 2 adalah Map.
 * Geser atau tombol NEXT di akhir artikel membuka Map; tombol kembali di Map balik ke artikel.
 * Dengan [startOnMap] (dari KeraChallenge Jali Hari 2) halaman langsung dibuka di Map,
 * dan kembali dari Map keluar lewat [onBackToHome].
 */
@Composable
fun KeraFyiDetailScreen(
    food: LocalFood,
    onBackToHome: () -> Unit,
    startOnMap: Boolean = false,
    onMapOpened: () -> Unit = {}
) {
    val content = food.article
    val pagerState = rememberPagerState(initialPage = if (startOnMap) MAP_PAGE else ARTICLE_PAGE, pageCount = { 2 })
    val coroutineScope = rememberCoroutineScope()

    val isMapPage = pagerState.currentPage == MAP_PAGE
    val zoomState = rememberPinchZoomState()

    fun openPage(page: Int) {
        coroutineScope.launch { pagerState.animateScrollToPage(page) }
    }

    // Zoom kembali normal setiap pindah halaman; membuka Map dicatat untuk KeraChallenge
    LaunchedEffect(pagerState.currentPage) {
        zoomState.reset()
        if (pagerState.currentPage == MAP_PAGE) onMapOpened()
    }

    fun leaveMap() = if (startOnMap) onBackToHome() else openPage(ARTICLE_PAGE)

    // Tombol kembali sistem di Map kembali ke artikel, bukan keluar dari KeraFYI
    BackHandler(enabled = isMapPage) { leaveMap() }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        if (!isMapPage) {
            Image(
                painter = painterResource(R.drawable.bg_kerafyi_detail),
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
        }

        HorizontalPager(
            state = pagerState,
            // Dari artikel bisa digeser ke Map. Di Map geser dipakai peta, jadi pager dikunci.
            userScrollEnabled = !isMapPage && !zoomState.isZoomed,
            // Map punya zoom sendiri, jadi pinch zoom hanya untuk artikel
            modifier = Modifier
                .fillMaxSize()
                .pinchToZoom(zoomState, enabled = !isMapPage)
        ) { page ->
            when (page) {
                ARTICLE_PAGE -> KeraFyiArticle(
                    content = content,
                    onBackClick = onBackToHome,
                    onNextClick = { openPage(MAP_PAGE) }
                )

                else -> Box(modifier = Modifier.fillMaxSize()) {
                    KeraFyiMapContent(locations = food.mapLocations(), note = food.mapNoteRes?.let { stringResource(it) })
                    KeraFyiTopBar(
                        onBackClick = { leaveMap() },
                        backgroundColor = Color.White
                    )
                }
            }
        }
    }
}

/** Seluruh isi artikel satu pangan dalam satu kolom yang digulir. */
@Composable
private fun KeraFyiArticle(
    content: FyiArticle,
    onBackClick: () -> Unit,
    onNextClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .navigationBarsPadding()
            .padding(bottom = 32.dp)
    ) {
        KeraFyiTopBar(onBackClick = onBackClick)
        val shortName = stringResource(content.shortName)
        FyiFoodIntro(content = content)

        // Banner tiga maskot hanya sekali, sebagai pembatas setelah Karakteristik
        FyiMascotBanner()
        FyiBenefitSection(title = stringResource(R.string.fyi_benefits_of, shortName), benefits = content.benefits)

        content.kenali?.let { kenali ->
            Spacer(modifier = Modifier.height(20.dp))
            FyiKenaliSection(shortName = shortName, kenali = kenali)
        }

        Spacer(modifier = Modifier.height(28.dp))
        GiziNutritionSection(
            nutrition = content.nutrition,
            title = stringResource(R.string.fyi_nutrition_of, shortName)
        )

        FyiNextButton(onClick = onNextClick)
    }
}

@Composable
private fun KeraFyiTopBar(
    onBackClick: () -> Unit,
    backgroundColor: Color = Color.Transparent
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(backgroundColor)
            .statusBarsPadding()
            .height(64.dp)
    ) {
        Image(
            painter = painterResource(R.drawable.ic_arrow_back),
            contentDescription = stringResource(R.string.common_back),
            modifier = Modifier
                .align(Alignment.CenterStart)
                .padding(start = 20.dp)
                .size(28.dp)
                .clickable(onClick = onBackClick)
        )

        // Judul hijau dengan outline ungu: teks outline digambar di bawah teks isi
        val titleStyle = TextStyle(fontSize = 30.sp, fontWeight = FontWeight.ExtraBold)
        val outlineWidth = with(LocalDensity.current) { 2.dp.toPx() }
        Box(modifier = Modifier.align(Alignment.Center)) {
            Text(
                text = "KERA FYI",
                style = titleStyle.copy(
                    color = KeraPurple,
                    drawStyle = Stroke(width = outlineWidth, join = StrokeJoin.Round)
                )
            )
            Text(
                text = "KERA FYI",
                style = titleStyle.copy(color = KeraFyiGreenTitle)
            )
        }
    }
}

/** Satu varietas di bagian Karakteristik. */
private data class FyiVariety(
    @StringRes val title: Int,
    @DrawableRes val imageRes: Int,
    @StringRes val caption: Int,
    @StringRes val source: Int,
    /** Pasangan (label, isi) resource string. */
    val traits: List<Pair<Int, Int>>
)

/** Satu manfaat: judul, penjelasan, dan ilustrasi di kartu hijau. */
private data class FyiBenefit(
    @StringRes val title: Int,
    @StringRes val description: Int,
    @DrawableRes val imageRes: Int
)

/** Bagian "Kenali", "Cara Aman Mengonsumsi", dan "... Enak, Tapi Jangan Asal Olah!". */
private class FyiKenali(
    @StringRes val intro: Int,
    val safeSteps: List<Int>,
    @StringRes val tastyTitle: Int
)

/** Teks deskripsi beserta gaya panelnya (desain jalar memakai teks plum, lainnya hijau tua). */
private class FyiDescription(
    /** Teks deskripsi; %1$s diganti [italicName] yang ditulis miring. */
    @StringRes val text: Int,
    val italicName: String,
    val textColor: Color,
    val textAlign: TextAlign
)

/**
 * Isi artikel satu pangan. [mascotBesideTitle] = false untuk nama yang panjang:
 * nama memakai lebar penuh dan maskot pindah ke samping judul Karakteristik, seperti di desain.
 */
private class FyiArticle(
    @DrawableRes val heroRes: Int,
    @StringRes val name: Int,
    val latinName: String,
    /** Nama pendek untuk judul bagian, mis. "Ubi Kayu". */
    @StringRes val shortName: Int,
    @DrawableRes val mascotRes: Int,
    val mascotBesideTitle: Boolean,
    val description: FyiDescription,
    val varieties: List<FyiVariety>,
    val benefits: List<FyiBenefit>,
    val kenali: FyiKenali?,
    val nutrition: FyiNutrition
)

/** Isi artikel tiap pangan. */
private val LocalFood.article: FyiArticle
    get() = when (this) {
        LocalFood.JALAR -> jalarArticle
        LocalFood.SINGKONG -> singkongArticle
        LocalFood.TALAS -> talasArticle
    }

/** Teks [res] dengan %1$s diganti [italicName] yang ditulis miring (posisinya beda tiap bahasa). */
@Composable
private fun italicNameText(@StringRes res: Int, italicName: String): AnnotatedString {
    val marker = "\u0001"
    val (before, after) = stringResource(res, marker).split(marker, limit = 2).let { it[0] to it.getOrElse(1) { "" } }
    return buildAnnotatedString {
        append(before)
        withStyle(SpanStyle(fontStyle = FontStyle.Italic)) { append(italicName) }
        append(after)
    }
}

// ---------- Nilai gizi (juga dipakai KeraRecommend) ----------

/** Satu baris tabel gizi; [isSub] untuk baris turunan (mis. serat di bawah karbohidrat). */
internal data class Nutrient(
    @StringRes val name: Int,
    val amount: String,
    val percent: String,
    @DrawableRes val iconRes: Int?,
    val isSub: Boolean = false
)

/** Isi halaman nilai gizi satu pangan; pangan lokal per 100 gram, resep KeraRecommend per sajian. */
internal class FyiNutrition(
    @DrawableRes val servingIconRes: Int,
    val energy: String,
    val energyFromFat: String,
    val nutrients: List<Nutrient>,
    val showAkgLabel: Boolean = false,
    val servingSize: String = "100 gram"
)

private val singkongNutrition = FyiNutrition(
    servingIconRes = R.drawable.ic_kerafyi_gizi_bowl,
    energy = "160 KKal",
    energyFromFat = "3 Kkal",
    nutrients = listOf(
        Nutrient(R.string.fyi_lemak_total, "0.28 g", "0%", R.drawable.ic_kerafyi_gizi_fat),
        Nutrient(R.string.fyi_protein, "1.36 g", "2%", R.drawable.ic_kerafyi_gizi_protein),
        Nutrient(R.string.fyi_karbohidrat_total, "38.1 g", "12%", R.drawable.ic_kerafyi_gizi_carb),
        Nutrient(R.string.fyi_serat_kasar, "1.8 g", "6%", null, isSub = true),
        Nutrient(R.string.fyi_gula, "1.7 g", "3%", R.drawable.ic_kerafyi_gizi_sugar),
        Nutrient(R.string.fyi_natrium, "14 mg", "1%", R.drawable.ic_kerafyi_gizi_sodium)
    )
)

private val jalarNutrition = FyiNutrition(
    servingIconRes = R.drawable.ic_kerafyi_gizi_scale,
    energy = "81 KKal",
    energyFromFat = "1 Kkal",
    nutrients = listOf(
        Nutrient(R.string.fyi_lemak_total, "0.1 g", "0%", R.drawable.ic_kerafyi_gizi_leaf),
        Nutrient(R.string.fyi_protein, "1.6 g", "3%", R.drawable.ic_kerafyi_gizi_protein),
        Nutrient(R.string.fyi_karbohidrat_total, "20 g", "6%", R.drawable.ic_kerafyi_gizi_carb),
        Nutrient(R.string.fyi_serat_kasar, "4 g", "13%", null, isSub = true),
        Nutrient(R.string.fyi_gula, "5 g", "10%", R.drawable.ic_kerafyi_gizi_drop),
        Nutrient(R.string.fyi_natrium, "27 mg", "2%", R.drawable.ic_kerafyi_gizi_sodium)
    ),
    showAkgLabel = true
)

private val talasNutrition = FyiNutrition(
    servingIconRes = R.drawable.ic_kerafyi_gizi_scale,
    energy = "112 KKal",
    energyFromFat = "4 Kkal",
    nutrients = listOf(
        Nutrient(R.string.fyi_lemak_total, "0.4 g", "0%", R.drawable.ic_kerafyi_gizi_leaf_purple),
        Nutrient(R.string.fyi_protein, "1.5 g", "2%", R.drawable.ic_kerafyi_gizi_protein_talas),
        Nutrient(R.string.fyi_karbohidrat_total, "26 g", "8%", R.drawable.ic_kerafyi_gizi_carb),
        Nutrient(R.string.fyi_serat_kasar, "5 g", "17%", null, isSub = true),
        Nutrient(R.string.fyi_gula, "0.5 g", "1%", R.drawable.ic_kerafyi_gizi_drop),
        Nutrient(R.string.fyi_natrium, "11 mg", "0%", R.drawable.ic_kerafyi_gizi_sodium)
    ),
    showAkgLabel = true
)

/** Judul [title], takaran saji, energi, tabel gizi, dan catatan AKG. */
@Composable
internal fun GiziNutritionSection(
    nutrition: FyiNutrition,
    modifier: Modifier = Modifier,
    title: String = stringResource(R.string.fyi_nutrition_info)
) {
    Column(
        modifier = modifier.padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Row(
            modifier = Modifier.padding(start = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Image(
                painter = painterResource(R.drawable.ic_kerafyi_cutlery_plum),
                contentDescription = null,
                modifier = Modifier.size(34.dp)
            )
            Spacer(modifier = Modifier.width(10.dp))
            Text(
                text = title,
                fontSize = 21.sp,
                fontWeight = FontWeight.Bold,
                color = KeraFyiBerry
            )
        }

        GiziServingPanel(
            servingIconRes = nutrition.servingIconRes,
            servingSize = nutrition.servingSize
        )

        Text(
            text = stringResource(R.string.fyi_amount_per_serving),
            modifier = Modifier
                .background(KeraGiziPurple, RoundedCornerShape(50))
                .padding(horizontal = 24.dp, vertical = 8.dp),
            fontSize = 18.sp,
            fontWeight = FontWeight.ExtraBold,
            color = Color.White
        )

        GiziEnergyRow(energy = nutrition.energy, energyFromFat = nutrition.energyFromFat)
        GiziNutrientTable(nutrients = nutrition.nutrients, showAkgLabel = nutrition.showAkgLabel)

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(KeraGiziNote, RoundedCornerShape(24.dp))
                .padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Image(
                painter = painterResource(R.drawable.ic_kerafyi_gizi_bulb),
                contentDescription = null,
                modifier = Modifier.size(40.dp)
            )
            Spacer(modifier = Modifier.width(10.dp))
            Text(
                text = stringResource(R.string.fyi_akg_note),
                fontSize = 11.sp,
                lineHeight = 15.sp,
                color = KeraGiziNoteText
            )
        }
    }
}

/** Panel takaran saji: [servingSize] | 1 sajian per porsi. */
@Composable
private fun GiziServingPanel(@DrawableRes servingIconRes: Int, servingSize: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                Brush.horizontalGradient(listOf(KeraGiziPanelStart, KeraGiziPanelEnd)),
                RoundedCornerShape(40.dp)
            )
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            modifier = Modifier.weight(1f),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Image(
                painter = painterResource(servingIconRes),
                contentDescription = null,
                modifier = Modifier.size(50.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Column {
                Text(text = stringResource(R.string.fyi_serving_size), fontSize = 13.sp, lineHeight = 16.sp, color = KeraGiziPlum)
                Text(
                    text = servingSize,
                    fontSize = 21.sp,
                    lineHeight = 26.sp,
                    fontWeight = FontWeight.Bold,
                    color = KeraGiziPlum
                )
            }
        }

        Box(
            modifier = Modifier
                .padding(horizontal = 8.dp)
                .width(1.5.dp)
                .height(40.dp)
                .background(KeraGiziDivider)
        )

        Row(
            modifier = Modifier.weight(1f),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Image(
                painter = painterResource(R.drawable.ic_kerafyi_gizi_cutlery_light),
                contentDescription = null,
                modifier = Modifier.size(50.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = stringResource(R.string.fyi_one_serving),
                fontSize = 15.sp,
                lineHeight = 20.sp,
                color = KeraGiziPlum
            )
        }
    }
}

/** Baris energi total dengan nilai kkal di pil pink sebelah kanan. */
@Composable
private fun GiziEnergyRow(energy: String, energyFromFat: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(KeraGiziPink, RoundedCornerShape(24.dp))
            .padding(10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Image(
            painter = painterResource(R.drawable.ic_kerafyi_gizi_fire),
            contentDescription = null,
            modifier = Modifier.size(48.dp)
        )
        Spacer(modifier = Modifier.width(10.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = stringResource(R.string.fyi_total_energy),
                fontSize = 19.sp,
                lineHeight = 24.sp,
                fontWeight = FontWeight.Bold,
                color = KeraGiziBerry
            )
            Text(
                text = stringResource(R.string.fyi_energy_from_fat),
                fontSize = 14.sp,
                lineHeight = 18.sp,
                color = KeraGiziMagenta
            )
        }
        Column(
            modifier = Modifier
                .background(KeraGiziPinkDeep, RoundedCornerShape(20.dp))
                .padding(horizontal = 14.dp, vertical = 6.dp),
            horizontalAlignment = Alignment.End
        ) {
            Text(
                text = energy,
                fontSize = 21.sp,
                lineHeight = 26.sp,
                fontWeight = FontWeight.Bold,
                color = KeraGiziPlum
            )
            Text(text = energyFromFat, fontSize = 14.sp, lineHeight = 18.sp, color = KeraGiziPlum)
        }
    }
}

private val NutrientIconSize = 40.dp

@Composable
private fun GiziNutrientTable(nutrients: List<Nutrient>, showAkgLabel: Boolean = false) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(KeraGiziTable, RoundedCornerShape(20.dp))
            .padding(horizontal = 8.dp, vertical = 6.dp)
    ) {
        // Judul kolom persen, lebarnya sama dengan pil persen di tiap baris
        if (showAkgLabel) {
            Text(
                text = stringResource(R.string.fyi_akg),
                modifier = Modifier
                    .align(Alignment.End)
                    .width(56.dp),
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                color = KeraGiziPlum
            )
        }
        nutrients.forEachIndexed { index, nutrient ->
            if (index > 0) {
                Box(
                    modifier = Modifier
                        .padding(start = NutrientIconSize + 10.dp)
                        .fillMaxWidth()
                        .height(1.dp)
                        .background(KeraGiziRowDivider)
                )
            }
            GiziNutrientRow(nutrient = nutrient, isLavender = index % 2 == 0)
        }
    }
}

@Composable
private fun GiziNutrientRow(nutrient: Nutrient, isLavender: Boolean) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = if (nutrient.isSub) 4.dp else 5.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (nutrient.iconRes != null) {
            Image(
                painter = painterResource(nutrient.iconRes),
                contentDescription = null,
                modifier = Modifier.size(NutrientIconSize)
            )
        } else {
            Spacer(modifier = Modifier.width(NutrientIconSize))
        }
        Spacer(modifier = Modifier.width(10.dp))

        Row(
            modifier = Modifier.weight(1f),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (nutrient.isSub) {
                Box(
                    modifier = Modifier
                        .padding(start = 8.dp, end = 8.dp)
                        .size(7.dp)
                        .background(KeraGiziDot, CircleShape)
                )
            }
            Text(
                text = stringResource(nutrient.name),
                fontSize = 14.sp,
                lineHeight = 18.sp,
                fontWeight = if (nutrient.isSub) FontWeight.Medium else FontWeight.Bold,
                color = KeraGiziPlum
            )
        }

        Text(
            text = nutrient.amount,
            modifier = Modifier.width(64.dp),
            fontSize = 15.sp,
            fontWeight = FontWeight.SemiBold,
            textAlign = TextAlign.End,
            color = KeraGiziPlum
        )
        Spacer(modifier = Modifier.width(10.dp))
        Text(
            text = nutrient.percent,
            modifier = Modifier
                .width(56.dp)
                .background(
                    if (isLavender) KeraGiziLavenderPill else KeraGiziPinkPill,
                    RoundedCornerShape(50)
                )
                .padding(vertical = 4.dp),
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold,
            textAlign = TextAlign.Center,
            color = if (isLavender) KeraGiziLavenderText else KeraGiziPinkText
        )
    }
}

// ---------- Isi artikel ----------

/** Isi bagian "... Enak, Tapi Jangan Asal Olah!", sama untuk semua pangan. */
private val notCookedEffects = listOf(
    R.string.fyi_mulut_gatel_perih,
    R.string.fyi_tenggorokan_nggak_nyaman,
    R.string.fyi_perut_bisa_nggak_enak
)

private val singkongArticle = FyiArticle(
    heroRes = R.drawable.img_kerafyi_singkong_hero,
    name = R.string.fyi_ubi_kayu_singkong,
    latinName = "Manihot esculenta",
    shortName = R.string.fyi_ubi_kayu,
    mascotRes = R.drawable.img_mascot_kerafyi_singkong,
    mascotBesideTitle = false,
    description = FyiDescription(
        text = R.string.fyi_ubi_kayu_adalah_tanaman,
        italicName = "Euphorbiaceae",
        textColor = KeraFyiText,
        textAlign = TextAlign.Justify
    ),
    varieties = listOf(
        FyiVariety(
            title = R.string.fyi_a_ubi_kayu_putih,
            imageRes = R.drawable.img_kerafyi_singkong_putih,
            caption = R.string.fyi_gambar_1_ubi_kayu,
            source = R.string.fyi_sumber_alodokter_2024,
            traits = listOf(
                R.string.fyi_senyawa_fungsional to R.string.fyi_karotenoid_sangat_rendah_pati,
                R.string.fyi_warna to R.string.fyi_putih_putih_kekuningan,
                R.string.fyi_serat to R.string.fyi_selulosa_hemiselulosa_dan_pektin,
                R.string.fyi_tekstur to R.string.fyi_empuk_pulen_setelah_dimasak,
                R.string.fyi_rasa to R.string.fyi_gurih_dengan_manis_ringan
            )
        ),
        FyiVariety(
            title = R.string.fyi_b_ubi_kayu_kuning,
            imageRes = R.drawable.img_kerafyi_singkong_kuning,
            caption = R.string.fyi_gambar_2_ubi_kayu,
            source = R.string.fyi_sumber_artikula_id_t,
            traits = listOf(
                R.string.fyi_senyawa_fungsional to R.string.fyi_karotenoid_pati_resisten_yang,
                R.string.fyi_warna to R.string.fyi_kuning_oranye,
                R.string.fyi_sifat_fungsional to R.string.fyi_karotenoid_berpotensi_sebagai_antioksidan,
                R.string.fyi_serat to R.string.fyi_selulosa_hemiselulosa_dan_pektin,
                R.string.fyi_tekstur to R.string.fyi_empuk_pulen_setelah_dimasak,
                R.string.fyi_rasa to R.string.fyi_gurih_manis
            )
        )
    ),
    benefits = listOf(
        FyiBenefit(R.string.fyi_sumber_energi, R.string.fyi_karbohidrat_dan_pati_menjadi, R.drawable.img_kerafyi_benefit_singkong_1),
        FyiBenefit(
            R.string.fyi_mendukung_pencernaan,
            R.string.fyi_dapat_menghasilkan_asam_lemak,
            R.drawable.img_kerafyi_benefit_singkong_2
        ),
        FyiBenefit(
            R.string.fyi_mendukung_fungsi_tubuh,
            R.string.fyi_kalium_membantu_fungsi_otot,
            R.drawable.img_kerafyi_benefit_singkong_3
        )
    ),
    kenali = FyiKenali(
        intro = R.string.fyi_ubi_kayu_memiliki_senyawa,
        safeSteps = listOf(
            R.string.fyi_rebus_bersama_kulit_selama,
            R.string.fyi_pastikan_benar_benar_matang,
            R.string.fyi_tambahkan_sedikit_lemak_sehat
        ),
        tastyTitle = R.string.fyi_singkongnya_enak_tapi_njangan
    ),
    nutrition = singkongNutrition
)

private val jalarArticle = FyiArticle(
    heroRes = R.drawable.img_kerafyi_jalar_hero,
    name = R.string.fyi_ubi_jalar,
    latinName = "Ipomoea batatas",
    shortName = R.string.fyi_ubi_jalar_2,
    mascotRes = R.drawable.img_mascot_kerafyi_jalar,
    mascotBesideTitle = true,
    description = FyiDescription(
        text = R.string.fyi_ubi_jalar_adalah_tanaman,
        italicName = "Convolvulaceae",
        textColor = KeraFyiPlumText,
        textAlign = TextAlign.Start
    ),
    varieties = listOf(
        FyiVariety(
            title = R.string.fyi_a_ubi_jalar_putih,
            imageRes = R.drawable.img_kerafyi_jalar_putih,
            caption = R.string.fyi_gambar_1_ubi_jalar,
            source = R.string.fyi_sumber_sukan_b_v,
            traits = listOf(
                R.string.fyi_senyawa_fungsional to R.string.fyi_kandungan_pigmen_relatif_rendah,
                R.string.fyi_warna to R.string.fyi_putih_hingga_putih_kekuningan,
                R.string.fyi_serat to R.string.fyi_selulosa_hemiselulosa_dan_pektin,
                R.string.fyi_tekstur to R.string.fyi_cenderung_padat_dan_agak,
                R.string.fyi_rasa to R.string.fyi_manis_ringan_dan_cenderung
            )
        ),
        FyiVariety(
            title = R.string.fyi_b_ubi_jalar_kuning,
            imageRes = R.drawable.img_kerafyi_jalar_kuning,
            caption = R.string.fyi_gambar_2_ubi_jalar,
            source = R.string.fyi_sumber_idnjurnal_com_2022,
            traits = listOf(
                R.string.fyi_senyawa_fungsional to R.string.fyi_karotenoid,
                R.string.fyi_warna to R.string.fyi_kuning_oranye,
                R.string.fyi_sifat_fungsional to R.string.fyi_karotenoid_berpotensi_sebagai_antioksidan,
                R.string.fyi_serat to R.string.fyi_selulosa_hemiselulosa_dan_pektin,
                R.string.fyi_tekstur to R.string.fyi_empuk_pulen_setelah_dimasak,
                R.string.fyi_rasa to R.string.fyi_gurih_manis
            )
        ),
        FyiVariety(
            title = R.string.fyi_c_ubi_jalar_oranye,
            imageRes = R.drawable.img_kerafyi_jalar_oranye,
            caption = R.string.fyi_gambar_3_ubi_jalar,
            source = R.string.fyi_sumber_puspapertiwi_2024,
            traits = listOf(
                R.string.fyi_senyawa_fungsional to R.string.fyi_karotenoid_terutama_karoten,
                R.string.fyi_warna to R.string.fyi_oranye_hingga_oranye_kemerahan,
                R.string.fyi_serat to R.string.fyi_selulosa_hemiselulosa_dan_pektin,
                R.string.fyi_tekstur to R.string.fyi_lembut_dan_cenderung_lebih,
                R.string.fyi_rasa to R.string.fyi_manis_terutama_pada_varietas
            )
        ),
        FyiVariety(
            title = R.string.fyi_d_ubi_jalar_ungu,
            imageRes = R.drawable.img_kerafyi_jalar_ungu,
            caption = R.string.fyi_gambar_4_ubi_jalar,
            source = R.string.fyi_sumber_siti_m_t,
            traits = listOf(
                R.string.fyi_senyawa_fungsional to R.string.fyi_antosianin,
                R.string.fyi_warna to R.string.fyi_ungu_hingga_ungu_pekat,
                R.string.fyi_serat to R.string.fyi_selulosa_hemiselulosa_dan_pektin,
                R.string.fyi_tekstur to R.string.fyi_cenderung_padat_lembut_setelah,
                R.string.fyi_rasa to R.string.fyi_manis_dengan_cita_rasa
            )
        )
    ),
    benefits = listOf(
        FyiBenefit(
            R.string.fyi_menjaga_pencernaan,
            R.string.fyi_serat_membantu_melancarkan_pencernaan,
            R.drawable.img_kerafyi_benefit_jalar_1
        ),
        FyiBenefit(R.string.fyi_menjaga_kesehatan_mata, R.string.fyi_beta_karoten_mendukung_fungsi, R.drawable.img_kerafyi_benefit_jalar_2),
        FyiBenefit(
            R.string.fyi_mendukung_fungsi_tubuh,
            R.string.fyi_vitamin_c_dan_antioksidan,
            R.drawable.img_kerafyi_benefit_jalar_3
        )
    ),
    kenali = null,
    nutrition = jalarNutrition
)

private val talasArticle = FyiArticle(
    heroRes = R.drawable.img_kerafyi_talas_hero,
    name = R.string.fyi_ubi_talas_bentul,
    latinName = "Colocasia esculenta",
    shortName = R.string.fyi_ubi_talas,
    mascotRes = R.drawable.img_mascot_kerafyi_jalar,
    mascotBesideTitle = false,
    description = FyiDescription(
        text = R.string.fyi_talas_adalah_tanaman_umbi,
        italicName = "Araceae",
        textColor = KeraFyiText,
        textAlign = TextAlign.Justify
    ),
    varieties = listOf(
        FyiVariety(
            title = R.string.fyi_a_talas_putih_krem,
            imageRes = R.drawable.img_kerafyi_talas_putih,
            caption = R.string.fyi_gambar_1_ubi_talas,
            source = R.string.fyi_sumber_bella_2022_alodokter,
            traits = listOf(
                R.string.fyi_senyawa_fungsional to R.string.fyi_kandungan_pigmen_relatif_rendah,
                R.string.fyi_warna to R.string.fyi_putih_hingga_krem_pucat,
                R.string.fyi_serat to R.string.fyi_pektin_hemiselulosa_dan_selulosa,
                R.string.fyi_tekstur to R.string.fyi_empuk_dan_lembut_setelah,
                R.string.fyi_rasa to R.string.fyi_gurih_dengan_rasa_yang
            )
        ),
        FyiVariety(
            title = R.string.fyi_b_talas_mentega,
            imageRes = R.drawable.img_kerafyi_talas_kuning,
            caption = R.string.fyi_gambar_2_ubi_talas,
            source = R.string.fyi_sumber_choudhary_2025_stylecraze,
            traits = listOf(
                R.string.fyi_senyawa_fungsional to R.string.fyi_karotenoid,
                R.string.fyi_warna to R.string.fyi_kuning_muda_hingga_kuning,
                R.string.fyi_serat to R.string.fyi_pektin_hemiselulosa_dan_selulosa,
                R.string.fyi_tekstur to R.string.fyi_cenderung_padat_dan_pulen,
                R.string.fyi_rasa to R.string.fyi_gurih_dengan_sedikit_rasa
            )
        ),
        FyiVariety(
            title = R.string.fyi_c_talas_ungu_taro,
            imageRes = R.drawable.img_kerafyi_talas_ungu,
            caption = R.string.fyi_gambar_3_talas_ungu,
            source = R.string.fyi_sumber_kompas_com_2023,
            traits = listOf(
                R.string.fyi_senyawa_fungsional to R.string.fyi_antosianin,
                R.string.fyi_warna to R.string.fyi_putih_keunguan_hingga_ungu,
                R.string.fyi_serat to R.string.fyi_pektin_hemiselulosa_dan_selulosa,
                R.string.fyi_tekstur to R.string.fyi_pulen_dan_lembut_setelah,
                R.string.fyi_rasa to R.string.fyi_gurih_dengan_aroma_khas
            )
        )
    ),
    benefits = listOf(
        FyiBenefit(
            R.string.fyi_membantu_mengontrol_gula_darah,
            R.string.fyi_serat_dan_indeks_glikemik,
            R.drawable.img_kerafyi_benefit_talas_1
        ),
        FyiBenefit(
            R.string.fyi_menjaga_kesehatan_jantung,
            R.string.fyi_kalium_magnesium_dan_serat,
            R.drawable.img_kerafyi_benefit_talas_2
        ),
        FyiBenefit(
            R.string.fyi_melancarkan_pencernaan,
            R.string.fyi_kandungan_serat_membantu_menjaga,
            R.drawable.img_kerafyi_benefit_talas_3
        )
    ),
    kenali = FyiKenali(
        intro = R.string.fyi_ubi_talas_memiliki_kalsium,
        safeSteps = listOf(
            R.string.fyi_potong_dan_rendam_talas,
            R.string.fyi_rebus_atau_kukus_selama
        ),
        tastyTitle = R.string.fyi_talasnya_enak_tapi_njangan
    ),
    nutrition = talasNutrition
)

// ---------- Bagian artikel ----------

/**
 * Pembuka artikel: foto, nama, deskripsi, dan kartu karakteristik varietas.
 * Maskot ada di samping nama ([FyiArticle.mascotBesideTitle]) atau di samping judul Karakteristik.
 */
@Composable
private fun FyiFoodIntro(content: FyiArticle) {
    val panelShape = RoundedCornerShape(24.dp)

    Column(modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 4.dp, bottom = 16.dp)) {
        Image(
            painter = painterResource(content.heroRes),
            contentDescription = stringResource(content.name),
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(2.3f)
                .clip(RoundedCornerShape(16.dp)),
            contentScale = ContentScale.Crop
        )

        Row(verticalAlignment = Alignment.Top) {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(top = 8.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Ukuran nama mengecil otomatis supaya tetap satu baris di layar sempit
                Text(
                    text = stringResource(content.name),
                    lineHeight = 40.sp,
                    fontWeight = FontWeight.ExtraBold,
                    maxLines = 1,
                    autoSize = TextAutoSize.StepBased(minFontSize = 20.sp, maxFontSize = 36.sp),
                    color = KeraFyiGreen
                )
                Text(
                    text = content.latinName,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    fontStyle = FontStyle.Italic,
                    color = KeraFyiGreenDark
                )
            }

            // Maskot sedikit menumpuk ke bawah foto, seperti di desain
            if (content.mascotBesideTitle) {
                Image(
                    painter = painterResource(content.mascotRes),
                    contentDescription = null,
                    modifier = Modifier
                        .width(88.dp)
                        .offset(y = (-28).dp),
                    contentScale = ContentScale.FillWidth
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
        FyiSectionTitle(text = stringResource(R.string.fyi_description))
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = italicNameText(content.description.text, content.description.italicName),
            modifier = Modifier
                .fillMaxWidth()
                .background(KeraFyiLavenderPanel.copy(alpha = 0.9f), panelShape)
                .padding(horizontal = 14.dp, vertical = 12.dp),
            fontSize = 13.sp,
            lineHeight = 20.sp,
            fontWeight = FontWeight.SemiBold,
            textAlign = content.description.textAlign,
            color = content.description.textColor
        )
        Spacer(modifier = Modifier.height(24.dp))

        Box(modifier = Modifier.fillMaxWidth()) {
            FyiSectionTitle(text = stringResource(R.string.fyi_characteristics))

            // Maskot di kanan judul, menjulur ke atas tanpa menambah tinggi baris judul
            if (!content.mascotBesideTitle) {
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .height(34.dp)
                ) {
                    Image(
                        painter = painterResource(content.mascotRes),
                        contentDescription = null,
                        modifier = Modifier
                            .wrapContentHeight(align = Alignment.Bottom, unbounded = true)
                            .width(76.dp),
                        contentScale = ContentScale.FillWidth
                    )
                }
            }
        }
        Spacer(modifier = Modifier.height(10.dp))

        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
            content.varieties.forEach { variety ->
                FyiVarietyCard(
                    variety = variety,
                    modifier = Modifier.background(KeraFyiPanel.copy(alpha = 0.85f), panelShape)
                )
            }
        }
    }
}

@Composable
private fun FyiSectionTitle(
    text: String,
    fontSize: TextUnit = 22.sp
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Image(
            painter = painterResource(R.drawable.ic_leaf),
            contentDescription = null,
            modifier = Modifier.size(width = 15.dp, height = 23.dp)
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(
            text = text,
            fontSize = fontSize,
            fontWeight = FontWeight.Bold,
            color = KeraFyiOlive
        )
    }
}

@Composable
private fun FyiVarietyCard(
    variety: FyiVariety,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(
            modifier = Modifier.weight(0.4f),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Image(
                painter = painterResource(variety.imageRes),
                contentDescription = stringResource(variety.title),
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1.15f)
                    .clip(RoundedCornerShape(12.dp)),
                contentScale = ContentScale.Crop
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = stringResource(variety.caption),
                fontSize = 8.sp,
                lineHeight = 11.sp,
                textAlign = TextAlign.Center,
                color = KeraFyiText
            )
            Text(
                text = stringResource(variety.source),
                fontSize = 8.sp,
                lineHeight = 11.sp,
                fontStyle = FontStyle.Italic,
                textAlign = TextAlign.Center,
                color = KeraFyiText
            )
        }

        Spacer(modifier = Modifier.width(12.dp))

        Column(modifier = Modifier.weight(0.6f)) {
            Text(
                text = stringResource(variety.title),
                fontSize = 15.sp,
                lineHeight = 20.sp,
                fontWeight = FontWeight.Bold,
                color = KeraFyiText
            )
            Spacer(modifier = Modifier.height(4.dp))
            variety.traits.forEach { (labelRes, valueRes) ->
                val label = stringResource(labelRes)
                val value = stringResource(valueRes)
                FyiBullet(
                    text = buildAnnotatedString {
                        withStyle(SpanStyle(fontWeight = FontWeight.Bold)) { append("$label: ") }
                        append(value)
                    }
                )
            }
        }
    }
}

/** Banner tiga maskot dengan gelombang kuning, tampil sekali setelah bagian Karakteristik. */
@Composable
private fun FyiMascotBanner() {
    Image(
        painter = painterResource(R.drawable.img_kerafyi_mascot_banner),
        contentDescription = null,
        modifier = Modifier
            .padding(top = 16.dp)
            .fillMaxWidth()
            .aspectRatio(1080f / 620f)
    )
}

/**
 * Bagian manfaat di panel hijau muda: tiap manfaat berisi judul dan penjelasan di kiri,
 * ilustrasi di kartu hijau berbayang yang menempel ke tepi kanan layar.
 */
@Composable
private fun FyiBenefitSection(title: String, benefits: List<FyiBenefit>) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(KeraFyiBenefitPanel)
            .padding(top = 8.dp, bottom = 28.dp),
        verticalArrangement = Arrangement.spacedBy(28.dp)
    ) {
        Box(modifier = Modifier.padding(start = 16.dp)) {
            FyiSectionTitle(text = title, fontSize = 24.sp)
        }

        benefits.forEach { benefit ->
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .padding(start = 12.dp, end = 10.dp)
                ) {
                    FyiBullet(
                        text = AnnotatedString(stringResource(benefit.title)),
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = stringResource(benefit.description),
                        fontSize = 13.sp,
                        lineHeight = 18.sp,
                        color = KeraFyiText
                    )
                }

                BenefitImageCard(
                    imageRes = benefit.imageRes,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

/** Kartu hijau rata kiri-membulat dengan bayangan abu di bawah, berisi ilustrasi manfaat. */
@Composable
private fun BenefitImageCard(@DrawableRes imageRes: Int, modifier: Modifier = Modifier) {
    val shape = RoundedCornerShape(topStart = 22.dp, bottomStart = 22.dp)
    val image = ImageBitmap.imageResource(imageRes)

    Box(modifier = modifier.padding(bottom = 10.dp)) {
        Box(
            modifier = Modifier
                .matchParentSize()
                .offset(x = 6.dp, y = 10.dp)
                .background(KeraFyiShadow.copy(alpha = 0.7f), shape)
        )
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(KeraFyiBenefitCard, shape)
                .padding(vertical = 6.dp, horizontal = 10.dp),
            contentAlignment = Alignment.Center
        ) {
            Image(
                bitmap = image,
                contentDescription = null,
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(image.width.toFloat() / image.height)
                    .clip(RoundedCornerShape(8.dp)),
                contentScale = ContentScale.Fit,
                filterQuality = FilterQuality.High
            )
        }
    }
}

/** Kenali (pengantar), Cara Aman Mengonsumsi, dan "... Enak, Tapi Jangan Asal Olah!". */
@Composable
private fun FyiKenaliSection(shortName: String, kenali: FyiKenali) {
    Column(
        modifier = Modifier.padding(start = 20.dp, end = 20.dp, top = 8.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        KenaliTitle(text = stringResource(R.string.fyi_kenali_of, shortName))
        KenaliCard {
            Text(
                text = stringResource(kenali.intro),
                fontSize = 14.sp,
                lineHeight = 20.sp,
                textAlign = TextAlign.Justify,
                color = KeraFyiKenaliTitle
            )
        }

        Spacer(modifier = Modifier.height(4.dp))
        KenaliTitle(text = stringResource(R.string.fyi_safe_ways))
        KenaliCard {
            kenali.safeSteps.forEach { step ->
                FyiBullet(text = AnnotatedString(stringResource(step)), fontSize = 14.sp, color = KeraFyiKenaliTitle)
            }
        }

        Spacer(modifier = Modifier.height(4.dp))
        KenaliTitle(text = stringResource(kenali.tastyTitle))
        KenaliCard {
            Text(
                text = stringResource(R.string.fyi_not_cooked),
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = KeraFyiKenaliTitle
            )
            Spacer(modifier = Modifier.height(8.dp))
            notCookedEffects.forEach { effect ->
                FyiBullet(text = AnnotatedString(stringResource(effect)), fontSize = 14.sp, color = KeraFyiKenaliTitle)
            }
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = buildAnnotatedString {
                    withStyle(SpanStyle(fontWeight = FontWeight.Bold)) { append(stringResource(R.string.fyi_solution) + " ") }
                    append(stringResource(R.string.fyi_solution_text))
                },
                fontSize = 14.sp,
                lineHeight = 20.sp,
                color = KeraFyiKenaliTitle
            )
        }
    }
}

@Composable
private fun KenaliTitle(text: String) {
    Text(
        text = text,
        modifier = Modifier.padding(start = 4.dp),
        fontSize = 24.sp,
        lineHeight = 32.sp,
        fontWeight = FontWeight.Bold,
        color = KeraFyiKenaliTitle
    )
}

/** Kartu pink dengan bayangan abu di kanan bawah. */
@Composable
private fun KenaliCard(content: @Composable () -> Unit) {
    val shape = RoundedCornerShape(22.dp)
    Box(modifier = Modifier.fillMaxWidth()) {
        Box(
            modifier = Modifier
                .matchParentSize()
                .offset(x = 6.dp, y = 8.dp)
                .background(KeraFyiShadow, shape)
        )
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(KeraFyiKenaliPanel, shape)
                .padding(horizontal = 18.dp, vertical = 16.dp)
        ) {
            content()
        }
    }
}

/** Tombol NEXT; [showMascots] = dua maskot kecil menumpuk sedikit di atasnya. */
@Composable
internal fun FyiNextButton(onClick: () -> Unit, showMascots: Boolean = true) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        if (showMascots) {
            Image(
                painter = painterResource(R.drawable.img_mascot_kerafyi_duo),
                contentDescription = null,
                modifier = Modifier.width(96.dp),
                contentScale = ContentScale.FillWidth
            )
        }
        Image(
            painter = painterResource(R.drawable.img_kerafyi_button_next),
            contentDescription = stringResource(R.string.fyi_next),
            modifier = Modifier
                .offset(y = (-6).dp)
                .width(200.dp)
                .clip(RoundedCornerShape(50))
                .clickable(onClick = onClick),
            contentScale = ContentScale.FillWidth
        )
    }
}


@Composable
internal fun FyiBullet(
    text: AnnotatedString,
    fontSize: TextUnit = 13.sp,
    fontWeight: FontWeight = FontWeight.Normal,
    color: Color = KeraFyiText
) {
    val lineHeight = fontSize * 1.4f
    Row {
        Text(
            text = "•",
            modifier = Modifier.width(14.dp),
            fontSize = fontSize,
            lineHeight = lineHeight,
            color = color
        )
        Text(
            text = text,
            fontSize = fontSize,
            lineHeight = lineHeight,
            fontWeight = fontWeight,
            color = color
        )
    }
}
