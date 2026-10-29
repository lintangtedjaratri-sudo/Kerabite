package com.test.cobacompose.ui.kerachallenge

import com.test.cobacompose.R
import androidx.compose.ui.res.stringResource
import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.text.TextAutoSize
import androidx.compose.ui.unit.dp
import com.test.cobacompose.ui.theme.KeraBackGreen
import com.test.cobacompose.ui.theme.KeraBackGreenDark
import com.test.cobacompose.ui.theme.KeraBackGreenLight
import com.test.cobacompose.ui.theme.KeraBackPurple
import com.test.cobacompose.ui.theme.KeraBackPurpleDark
import com.test.cobacompose.ui.theme.KeraBackPurpleLight
import com.test.cobacompose.ui.theme.KeraGameLocked

/** Warna pil "Mulai Tantangan" sesuai tema tantangan. */
internal enum class DayPillTheme(val light: Color, val base: Color, val dark: Color) {
    GREEN(KeraBackGreenLight, KeraBackGreen, KeraBackGreenDark),
    PURPLE(KeraBackPurpleLight, KeraBackPurple, KeraBackPurpleDark)
}

/** Perbesar area sedikit supaya pil Compose menutupi penuh pil yang tergambar di desain. */
internal fun ImageArea.inflate(horizontal: Float = 0.006f, vertical: Float = 0.004f) =
    ImageArea(left - horizontal, top - vertical, right + horizontal, bottom + vertical)

/**
 * Pil status satu hari, menggantikan tombol yang tergambar di kartu hari:
 * belum selesai = "Mulai Tantangan →" sesuai tema, sudah selesai = "✓ Selesai" hijau.
 * Ukuran teks mengikuti tinggi pil supaya pas di semua lebar layar.
 */
@Composable
internal fun DayStatusPill(done: Boolean, theme: DayPillTheme) {
    val colors = if (done) DayPillTheme.GREEN else theme

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .shadow(elevation = 3.dp, shape = RoundedCornerShape(50))
            .clip(RoundedCornerShape(50))
            .background(Brush.verticalGradient(listOf(colors.light, colors.base, colors.dark)))
            .border(1.dp, colors.dark.copy(alpha = 0.6f), RoundedCornerShape(50)),
        contentAlignment = Alignment.Center
    ) {
        val density = LocalDensity.current
        val textSize = with(density) { (maxHeight * if (done) 0.42f else 0.33f).toSp() }
        val iconSize = maxHeight * 0.42f
        val gap = maxHeight * 0.12f

        Row(
            modifier = Modifier.padding(horizontal = maxHeight * 0.3f),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            if (done) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(iconSize)
                )
                Spacer(modifier = Modifier.width(gap))
            }
            // Teks mengecil otomatis supaya muat di pil sempit. "Mulai Tantangan" boleh 2 baris
            // seperti di desain Main Game; "Selesai" selalu satu baris di semua tantangan.
            Text(
                text = stringResource(if (done) R.string.kc_done else R.string.kc_start),
                modifier = Modifier.weight(1f, fill = false),
                lineHeight = textSize * 1.05f,
                fontWeight = FontWeight.SemiBold,
                color = Color.White,
                textAlign = TextAlign.Center,
                maxLines = if (done) 1 else 2,
                softWrap = !done,
                autoSize = TextAutoSize.StepBased(minFontSize = 6.sp, maxFontSize = textSize, stepSize = 0.5.sp)
            )
            if (!done) {
                Spacer(modifier = Modifier.width(gap))
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(iconSize * 0.85f)
                )
            }
        }
    }
}

/** Gembok putih bulat di atas tombol tantangan yang masih terkunci. */
@Composable
internal fun LockBadge() {
    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .shadow(elevation = 4.dp, shape = CircleShape)
            .clip(CircleShape)
            .background(Color.White)
            .border(2.dp, KeraGameLocked, CircleShape),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = Icons.Default.Lock,
            contentDescription = stringResource(R.string.kc_locked),
            tint = KeraBackPurpleDark,
            modifier = Modifier.size(maxWidth * 0.55f)
        )
    }
}

/** Selubung abu dengan gembok di atas tombol NEXT LEVEL selama bintang belum 3. */
@Composable
internal fun LockedVeil() {
    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .clip(RoundedCornerShape(30))
            .background(Color.White.copy(alpha = 0.55f)),
        contentAlignment = Alignment.CenterEnd
    ) {
        Box(
            modifier = Modifier
                .padding(end = maxHeight * 0.18f)
                .size(maxHeight * 0.62f)
        ) {
            LockBadge()
        }
    }
}

/** Pesan singkat saat hari diselesaikan atau sudah selesai sebelumnya. */
internal fun showDayToast(context: Context, day: Int, newlyCompleted: Boolean, stars: Int) {
    val message = if (newlyCompleted) {
        context.getString(R.string.kc_toast_day_done, day, stars)
    } else {
        context.getString(R.string.kc_toast_day_already, day)
    }
    Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
}
