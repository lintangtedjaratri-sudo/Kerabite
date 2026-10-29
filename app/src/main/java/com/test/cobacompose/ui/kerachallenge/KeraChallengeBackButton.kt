package com.test.cobacompose.ui.kerachallenge

import com.test.cobacompose.R
import androidx.compose.ui.res.stringResource
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import com.test.cobacompose.ui.theme.KeraBackGreen
import com.test.cobacompose.ui.theme.KeraBackGreenDark
import com.test.cobacompose.ui.theme.KeraBackGreenLight
import com.test.cobacompose.ui.theme.KeraBackPink
import com.test.cobacompose.ui.theme.KeraBackPinkDark
import com.test.cobacompose.ui.theme.KeraBackPinkLight
import com.test.cobacompose.ui.theme.KeraBackPurple
import com.test.cobacompose.ui.theme.KeraBackPurpleDark
import com.test.cobacompose.ui.theme.KeraBackPurpleLight
import com.test.cobacompose.ui.theme.KeraBackWhite
import com.test.cobacompose.ui.theme.KeraBackWhiteDark

/** Warna tombol kembali, mengikuti warna dasar tiap tantangan. */
enum class KeraBackButtonStyle(
    val light: Color,
    val base: Color,
    val dark: Color,
    val arrow: Color
) {
    /** Tantangan 3 - Main Game. */
    PURPLE(KeraBackPurpleLight, KeraBackPurple, KeraBackPurpleDark, Color.White),

    /** Tantangan 2 - Tala. */
    GREEN(KeraBackGreenLight, KeraBackGreen, KeraBackGreenDark, Color.White),

    /** Tantangan 1 - Jali. */
    PINK(KeraBackPinkLight, KeraBackPink, KeraBackPinkDark, Color.White),

    /** Peta KeraChallenge. */
    WHITE(Color.White, KeraBackWhite, KeraBackWhiteDark, KeraBackPurple)
}

/**
 * Tombol kembali bulat mengkilap seperti di desain Tantangan 3 (panah putih di lingkaran ungu),
 * digambar ulang supaya warnanya bisa diganti per tantangan.
 */
@Composable
internal fun KeraChallengeBackButton(
    onClick: () -> Unit,
    style: KeraBackButtonStyle,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .statusBarsPadding()
            .padding(start = 16.dp, top = 12.dp)
            .size(48.dp)
            .shadow(elevation = 6.dp, shape = CircleShape)
            .clip(CircleShape)
            .background(Brush.verticalGradient(listOf(style.light, style.base, style.dark)))
            .border(1.5.dp, style.dark.copy(alpha = 0.5f), CircleShape)
            .clickable(onClickLabel = stringResource(R.string.common_back), role = Role.Button, onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        // Kilau di bagian atas tombol
        Box(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 4.dp)
                .size(width = 28.dp, height = 10.dp)
                .clip(RoundedCornerShape(50))
                .background(Color.White.copy(alpha = 0.28f))
        )

        Canvas(modifier = Modifier.size(22.dp)) {
            val w = size.width
            val h = size.height
            val arrow = Path().apply {
                moveTo(w * 0.47f, h * 0.18f)
                lineTo(w * 0.15f, h * 0.5f)
                lineTo(w * 0.47f, h * 0.82f)
                moveTo(w * 0.15f, h * 0.5f)
                lineTo(w * 0.87f, h * 0.5f)
            }
            drawPath(
                path = arrow,
                color = style.arrow,
                style = Stroke(width = w * 0.19f, cap = StrokeCap.Round, join = StrokeJoin.Round)
            )
        }
    }
}
