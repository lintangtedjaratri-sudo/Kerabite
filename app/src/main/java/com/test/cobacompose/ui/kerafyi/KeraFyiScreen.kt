package com.test.cobacompose.ui.kerafyi

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
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.FilterQuality
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.imageResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.test.cobacompose.R
import com.test.cobacompose.ui.kerachallenge.DesignText
import com.test.cobacompose.ui.kerachallenge.DesignTextImage

/**
 * Halaman utama KeraFYI: maskot dan logo di atas, definisi keragaman pangan lokal di atas foto
 * hidangan yang diburamkan, lalu kartu tiap pangan.
 */
@Composable
fun KeraFyiScreen(
    onFoodClick: (LocalFood) -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        // Hiasan daun di pojok bawah, tetap di dasar layar di belakang konten
        Image(
            bitmap = ImageBitmap.imageResource(R.drawable.bg_kerafyi_bottom_deco),
            contentDescription = null,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth(),
            contentScale = ContentScale.FillWidth,
            filterQuality = FilterQuality.High
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .navigationBarsPadding(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            MascotHeader()
            DefinitionPanel()

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                LocalFood.entries.forEach { food ->
                    DesignTextImage(
                        bitmap = ImageBitmap.imageResource(food.fyiCardRes),
                        contentDescription = stringResource(food.titleRes),
                        texts = food.cardTexts(),
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(20.dp))
                            .clickable { onFoodClick(food) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(64.dp))
        }
    }
}

/**
 * Nama dan penjelasan singkat di kartu maskot (dulu tergambar di img_kerafyi_card_*, ~302 x 556 px).
 * Angka = piksel gambar. Kotak penjelasan berhenti sebelum tombol panah di kanan bawah.
 */
@Composable
private fun LocalFood.cardTexts(): List<DesignText> {
    val (title, desc) = when (this) {
        LocalFood.JALAR -> R.string.fyi_card_title_jalar to R.string.fyi_card_desc_jalar
        LocalFood.SINGKONG -> R.string.fyi_card_title_singkong to R.string.fyi_card_desc_singkong
        LocalFood.TALAS -> R.string.fyi_card_title_talas to R.string.fyi_card_desc_talas
    }
    return listOf(
        DesignText(stringResource(title), 34, 346, 292, 394, size = 33f, color = Color(0xFF103D27)),
        DesignText(
            stringResource(desc), 34, 398, 266, 500,
            size = 22f, color = Color(0xFF34443A), weight = FontWeight.Normal, lineHeight = 31f
        )
    )
}

/** Tiga maskot di atas gelombang kuning, dengan logo KERABITE miring di kanan atas. */
@Composable
private fun MascotHeader() {
    val mascots = ImageBitmap.imageResource(R.drawable.img_kerafyi_main_mascots)

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(mascots.width.toFloat() / mascots.height)
    ) {
        Image(
            bitmap = mascots,
            contentDescription = stringResource(R.string.fyi_mascot),
            modifier = Modifier.fillMaxSize(),
            filterQuality = FilterQuality.High
        )

        Image(
            bitmap = ImageBitmap.imageResource(R.drawable.logo_kerabite_text),
            contentDescription = "Kerabite",
            modifier = Modifier
                .align(Alignment.TopEnd)
                .statusBarsPadding()
                .padding(top = 6.dp, end = 10.dp)
                .fillMaxWidth(0.46f)
                .rotate(16f),
            filterQuality = FilterQuality.High
        )
    }
}

/** Definisi keragaman pangan lokal, teks putih di atas foto hidangan yang memudar ke putih. */
@Composable
private fun DefinitionPanel() {
    val food = ImageBitmap.imageResource(R.drawable.bg_kerafyi_food_spread)

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(food.width.toFloat() / food.height),
        contentAlignment = Alignment.Center
    ) {
        Image(
            bitmap = food,
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.FillBounds,
            filterQuality = FilterQuality.High
        )

        // Bayangan gelap di tengah supaya teks putih terbaca, lalu pudar ke putih di atas dan bawah
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Brush.radialGradient(listOf(Color.Black.copy(alpha = 0.42f), Color.Transparent)))
        )
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        0f to Color.White,
                        0.22f to Color.Transparent,
                        0.78f to Color.Transparent,
                        1f to Color.White
                    )
                )
        )

        Text(
            text = stringResource(R.string.fyi_definition),
            modifier = Modifier.padding(horizontal = 18.dp),
            style = TextStyle(
                fontSize = 17.sp,
                lineHeight = 25.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color.White,
                textAlign = TextAlign.Center,
                shadow = Shadow(Color.Black.copy(alpha = 0.35f), offset = Offset(0f, 2f), blurRadius = 8f)
            )
        )
    }
}

