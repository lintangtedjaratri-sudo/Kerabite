package com.test.cobacompose.ui.kerarecommend

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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.FilterQuality
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.imageResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.test.cobacompose.R
import com.test.cobacompose.ui.kerachallenge.DesignText
import com.test.cobacompose.ui.kerachallenge.DesignTextImage
import com.test.cobacompose.ui.theme.KeraFyiBerry
import com.test.cobacompose.ui.theme.KeraSppgGiziText
import com.test.cobacompose.ui.theme.KeraSppgGreenPill
import com.test.cobacompose.ui.theme.KeraSppgGreenRow
import com.test.cobacompose.ui.theme.KeraSppgGreenText
import com.test.cobacompose.ui.theme.KeraSppgInk
import com.test.cobacompose.ui.theme.KeraSppgLabelInk
import com.test.cobacompose.ui.theme.KeraSppgPill
import com.test.cobacompose.ui.theme.KeraSppgPinkPill
import com.test.cobacompose.ui.theme.KeraSppgPinkRow
import com.test.cobacompose.ui.theme.KeraSppgPinkText
import com.test.cobacompose.ui.theme.KeraSppgPurplePill
import com.test.cobacompose.ui.theme.KeraSppgPurpleRow
import com.test.cobacompose.ui.theme.KeraSppgPurpleText
import com.test.cobacompose.ui.theme.KeraSppgYellowPill
import com.test.cobacompose.ui.theme.KeraSppgYellowRow
import com.test.cobacompose.ui.theme.KeraSppgYellowText

/**
 * Detail kartu terakhir KeraRecommend (desain 2): Menu SPPG berbasis pangan lokal. Satu halaman
 * yang bisa di-scroll: judul, nampan menu dengan label tiap isi, lalu informasi nilai gizi per porsi.
 */
@Composable
fun KeraMenuSppgScreen(onBackClick: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        // Motif ubi dipudarkan dengan lapisan putih seperti di desain
        Image(
            bitmap = ImageBitmap.imageResource(R.drawable.bg_kerarecommend_sppg_texture),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.White.copy(alpha = 0.72f))
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .statusBarsPadding()
                .navigationBarsPadding()
                .padding(top = 56.dp, bottom = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            SppgTitle()
            Spacer(modifier = Modifier.height(12.dp))
            DesignTextImage(
                bitmap = ImageBitmap.imageResource(R.drawable.img_kerarecommend_sppg_tray),
                contentDescription = stringResource(R.string.rcp_sppg_tray_desc),
                texts = trayTexts(),
                modifier = Modifier
                    .padding(horizontal = 4.dp)
                    .fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(20.dp))
            SppgNutritionSection()
        }

        // Tombol kembali melayang, sama seperti halaman resep
        Box(
            modifier = Modifier
                .statusBarsPadding()
                .padding(start = 16.dp, top = 12.dp)
                .size(44.dp)
                .clip(CircleShape)
                .background(Color.White.copy(alpha = 0.85f))
                .clickable(onClick = onBackClick),
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

/** Logo "Menu SPPG" tetap gambar; pil kuning dan tagline di bawahnya teks asli supaya diterjemahkan. */
@Composable
private fun SppgTitle() {
    Image(
        bitmap = ImageBitmap.imageResource(R.drawable.img_kerarecommend_sppg_logo),
        contentDescription = "Menu SPPG",
        modifier = Modifier
            .fillMaxWidth(0.74f)
            .aspectRatio(670f / 124f),
        filterQuality = FilterQuality.High
    )
    Text(
        text = stringResource(R.string.rcp_sppg_subtitle),
        modifier = Modifier
            .padding(top = 4.dp, start = 32.dp, end = 32.dp)
            .rotate(-3f)
            .background(KeraSppgPill, RoundedCornerShape(50))
            .padding(horizontal = 20.dp, vertical = 4.dp),
        fontSize = 20.sp,
        lineHeight = 26.sp,
        fontWeight = FontWeight.Bold,
        color = KeraSppgInk,
        textAlign = TextAlign.Center
    )
    Row(
        modifier = Modifier.padding(top = 6.dp, start = 24.dp, end = 24.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        TitleDash()
        Text(
            text = stringResource(R.string.rcp_sppg_tagline),
            modifier = Modifier
                .weight(1f, fill = false)
                .padding(horizontal = 10.dp),
            fontSize = 16.sp,
            fontWeight = FontWeight.SemiBold,
            color = KeraSppgInk,
            textAlign = TextAlign.Center
        )
        TitleDash()
    }
}

@Composable
private fun TitleDash() {
    Box(
        modifier = Modifier
            .size(width = 18.dp, height = 3.dp)
            .background(KeraSppgInk, RoundedCornerShape(50))
    )
}

/** Gambar nampan 1080 px; koordinat di bawah dalam piksel desain asli (lebar 1280), jadi diskalakan. */
private const val TrayScale = 1080f / 1280f

@Composable
private fun trayLabel(
    @StringRes res: Int?,
    text: String? = null,
    left: Int,
    top: Int,
    right: Int,
    bottom: Int,
    isTitle: Boolean,
    rotation: Float = 0f,
    lineHeight: Float = 28f
) = DesignText(
    text ?: stringResource(res!!),
    (left * TrayScale).toInt(), (top * TrayScale).toInt(),
    (right * TrayScale).toInt(), (bottom * TrayScale).toInt(),
    size = (if (isTitle) 29f else 22f) * TrayScale,
    color = if (isTitle) KeraSppgInk else KeraSppgLabelInk,
    weight = if (isTitle) FontWeight.SemiBold else FontWeight.Normal,
    lineHeight = lineHeight * TrayScale,
    rotation = rotation,
    centered = true,
    align = if (isTitle) TextAlign.Start else TextAlign.Center
)

/** Label isi nampan yang tulisannya sudah dihapus dari gambar. Mashed/Perkedel Kimpul = nama hidangan. */
@Composable
private fun trayTexts(): List<DesignText> = listOf(
    trayLabel(R.string.rcp_sppg_salad, left = 118, top = 18, right = 372, bottom = 66, isTitle = true),
    trayLabel(R.string.rcp_sppg_salad_desc, left = 90, top = 70, right = 342, bottom = 164, isTitle = false),
    trayLabel(R.string.rcp_sppg_milk, left = 600, top = 40, right = 744, bottom = 84, isTitle = true),
    trayLabel(R.string.rcp_sppg_milk_desc, left = 598, top = 80, right = 774, bottom = 144, isTitle = false),
    trayLabel(R.string.rcp_sppg_fruit, left = 1026, top = 50, right = 1186, bottom = 100, isTitle = true, rotation = -3f),
    trayLabel(R.string.rcp_sppg_fruit_desc, left = 1000, top = 108, right = 1170, bottom = 188, isTitle = false, rotation = -5f),
    trayLabel(null, "Mashed Kimpul", left = 132, top = 840, right = 372, bottom = 884, isTitle = true),
    trayLabel(R.string.rcp_sppg_mashed_desc, left = 138, top = 895, right = 318, bottom = 960, isTitle = false),
    trayLabel(null, "Perkedel Kimpul", left = 588, top = 856, right = 812, bottom = 910, isTitle = true, rotation = -3f),
    trayLabel(R.string.rcp_sppg_perkedel_desc, left = 573, top = 919, right = 778, bottom = 992, isTitle = false, rotation = -3f),
    trayLabel(
        R.string.rcp_sppg_tumis, left = 1004, top = 850, right = 1228, bottom = 927,
        isTitle = true, rotation = -4f, lineHeight = 31f
    ),
    trayLabel(R.string.rcp_sppg_tumis_desc, left = 974, top = 922, right = 1208, bottom = 998, isTitle = false, rotation = -4f)
)

/** Satu baris gizi: warna baris, pil, dan teks persen mengikuti desain. */
private class SppgNutrient(
    @StringRes val nameRes: Int,
    val amount: String,
    val percent: String,
    @DrawableRes val iconRes: Int,
    val rowColor: Color,
    val pillColor: Color,
    val percentColor: Color
)

private val sppgNutrients = listOf(
    SppgNutrient(
        R.string.rcp_sppg_energi_total, "530 kkal", "25%", R.drawable.ic_kerarecommend_sppg_energy,
        KeraSppgPinkRow, KeraSppgPinkPill, KeraSppgPinkText
    ),
    SppgNutrient(
        R.string.fyi_lemak_total, "20 g", "33%", R.drawable.ic_kerarecommend_sppg_fat,
        KeraSppgYellowRow, KeraSppgYellowPill, KeraSppgYellowText
    ),
    SppgNutrient(
        R.string.fyi_protein, "16 g", "24%", R.drawable.ic_kerarecommend_sppg_protein,
        KeraSppgPurpleRow, KeraSppgPurplePill, KeraSppgPurpleText
    ),
    SppgNutrient(
        R.string.fyi_karbohidrat_total, "72 g", "21%", R.drawable.ic_kerarecommend_sppg_carb,
        KeraSppgGreenRow, KeraSppgGreenPill, KeraSppgGreenText
    ),
    SppgNutrient(
        R.string.rcp_sppg_serat_pangan, "12 g", "13%", R.drawable.ic_kerarecommend_sppg_fiber,
        KeraSppgPurpleRow, KeraSppgPurplePill, KeraSppgPurpleText
    ),
    SppgNutrient(
        R.string.rcp_sppg_gula_total, "20 g", "40%", R.drawable.ic_kerarecommend_sppg_sugar,
        KeraSppgPinkRow, KeraSppgPinkPill, KeraSppgPinkText
    ),
    SppgNutrient(
        R.string.fyi_natrium, "500 mg", "38%", R.drawable.ic_kerarecommend_sppg_sodium,
        KeraSppgPurpleRow, KeraSppgPurplePill, KeraSppgPurpleText
    )
)

@Composable
private fun SppgNutritionSection() {
    Column(modifier = Modifier.padding(horizontal = 8.dp)) {
        Row(
            modifier = Modifier.padding(start = 8.dp, bottom = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Image(
                painter = painterResource(R.drawable.ic_kerarecommend_sppg_cutlery),
                contentDescription = null,
                modifier = Modifier.size(38.dp)
            )
            Spacer(modifier = Modifier.width(10.dp))
            Text(
                text = stringResource(R.string.fyi_nutrition_info),
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = KeraFyiBerry
            )
        }
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            sppgNutrients.forEach { SppgNutrientRow(it) }
        }
    }
}

@Composable
private fun SppgNutrientRow(nutrient: SppgNutrient) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(
                Brush.horizontalGradient(listOf(nutrient.rowColor, nutrient.rowColor.copy(alpha = 0.55f)))
            )
            .padding(horizontal = 8.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Image(
            painter = painterResource(nutrient.iconRes),
            contentDescription = null,
            modifier = Modifier.size(48.dp)
        )
        Text(
            text = stringResource(nutrient.nameRes),
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 12.dp),
            fontSize = 16.sp,
            lineHeight = 20.sp,
            fontWeight = FontWeight.Bold,
            color = KeraSppgGiziText
        )
        Text(
            text = nutrient.amount,
            fontSize = 17.sp,
            fontWeight = FontWeight.Bold,
            color = KeraSppgGiziText,
            textAlign = TextAlign.End
        )
        Spacer(modifier = Modifier.width(12.dp))
        Box(
            modifier = Modifier
                .width(72.dp)
                .background(nutrient.pillColor, RoundedCornerShape(50))
                .padding(vertical = 6.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = nutrient.percent,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = nutrient.percentColor
            )
        }
    }
}
