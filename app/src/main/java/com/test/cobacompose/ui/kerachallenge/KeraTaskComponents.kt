package com.test.cobacompose.ui.kerachallenge

import android.content.Context
import android.widget.Toast
import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.TextAutoSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.FilterQuality
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.imageResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.test.cobacompose.R

/*
 * Komponen bersama halaman tugas harian Jali dan Tala (desain 41-47).
 */

/** Latar pemandangan yang sama dengan halaman Bagikan & Ajak, memenuhi layar. */
@Composable
internal fun TaskLandscapeBackground() {
    Image(
        bitmap = ImageBitmap.imageResource(R.drawable.bg_kerachallenge_bagikan),
        contentDescription = null,
        modifier = Modifier.fillMaxSize(),
        contentScale = ContentScale.Crop
    )
}

/** Gambar desain (nodpi) dengan rasio aslinya; lebarnya diatur pemanggil. */
@Composable
internal fun TaskImage(
    @DrawableRes res: Int,
    modifier: Modifier = Modifier,
    contentDescription: String? = null
) {
    val bitmap = ImageBitmap.imageResource(res)

    Image(
        bitmap = bitmap,
        contentDescription = contentDescription,
        modifier = modifier.aspectRatio(bitmap.width.toFloat() / bitmap.height),
        contentScale = ContentScale.FillBounds,
        filterQuality = FilterQuality.High
    )
}

/** Tombol pil "Mari Kita Mulai!" dengan bulatan di kanan, seperti di desain 41-45. */
@Composable
internal fun LetsStartButton(
    text: String,
    color: Color,
    dotColor: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(64.dp)
            .shadow(4.dp, RoundedCornerShape(50))
            .clip(RoundedCornerShape(50))
            .background(color)
            .clickable(role = Role.Button, onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(start = 20.dp, end = 62.dp),
            color = Color.White,
            textAlign = TextAlign.Center,
            maxLines = 1,
            // Satu baris seperti di desain; terjemahan panjang mengecil sendiri
            autoSize = TextAutoSize.StepBased(minFontSize = 13.sp, maxFontSize = 21.sp, stepSize = 0.5.sp)
        )
        Box(
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .padding(end = 12.dp)
                .size(42.dp)
                .clip(CircleShape)
                .background(dotColor)
        )
    }
}

/** Toast hari selesai yang menyebut tantangannya, untuk hari yang selesai di luar halaman tantangan (KeraFYI). */
internal fun showChallengeDayToast(context: Context, challenge: Int, day: Int, stars: Int) {
    Toast.makeText(
        context,
        context.getString(R.string.kc_toast_challenge_day_done, challenge, day, stars),
        Toast.LENGTH_SHORT
    ).show()
}
