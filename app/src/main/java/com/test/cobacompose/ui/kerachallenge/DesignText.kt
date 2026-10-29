package com.test.cobacompose.ui.kerachallenge

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.BoxWithConstraintsScope
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.BasicText
import androidx.compose.foundation.text.TextAutoSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.FilterQuality
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.PlatformTextStyle
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.LineHeightStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDirection
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp

/**
 * Teks yang dulu tergambar di dalam gambar desain, sekarang digambar Compose supaya tajam dan bisa
 * diterjemahkan. Posisi ([left]..[bottom]), [size], [lineHeight], dan bayangan memakai piksel gambar
 * asli, jadi ikut mengecil/membesar bersama gambarnya dan tidak terpengaruh ukuran font sistem.
 * Kalau terjemahan lebih panjang dari kotaknya, ukuran huruf diperkecil otomatis.
 */
internal class DesignText(
    val text: String,
    val left: Int,
    val top: Int,
    val right: Int,
    val bottom: Int,
    val size: Float,
    val color: Color,
    val weight: FontWeight = FontWeight.Bold,
    val align: TextAlign = TextAlign.Start,
    /** Jarak antarbaris dalam piksel gambar; null = 1.25 x [size]. */
    val lineHeight: Float? = null,
    /** Kemiringan dalam derajat (pil dan papan yang miring di desain). */
    val rotation: Float = 0f,
    /** true untuk teks satu baris di tengah pil/tombol. */
    val centered: Boolean = false,
    val italic: Boolean = false,
    /** Bayangan teks (mis. NEXT LEVEL), offset dalam piksel gambar. */
    val shadowColor: Color? = null,
    val shadowOffsetY: Float = 0f,
    val letterSpacing: Float = 0f
)

/** Menggambar [texts] di atas gambar berukuran [imageWidth] x [imageHeight] piksel. */
@Composable
internal fun BoxWithConstraintsScope.DesignTexts(
    texts: List<DesignText>,
    imageWidth: Int,
    imageHeight: Int
) {
    val density = LocalDensity.current
    // Piksel layar per piksel gambar
    val scale = constraints.maxWidth.toFloat() / imageWidth

    texts.forEach { t ->
        val fontSize = with(density) { (t.size * scale).toSp() }
        val style = MaterialTheme.typography.bodyMedium.copy(
            fontSize = fontSize,
            lineHeight = ((t.lineHeight ?: (t.size * 1.25f)) / t.size).em,
            letterSpacing = (t.letterSpacing / t.size).em,
            color = t.color,
            fontWeight = t.weight,
            fontStyle = if (t.italic) FontStyle.Italic else FontStyle.Normal,
            textAlign = t.align,
            // Tata letak dipaksa LTR; arah dari isi teks supaya teks Arab rata kanan di kotaknya
            textDirection = TextDirection.Content,
            shadow = t.shadowColor?.let { Shadow(it, Offset(0f, t.shadowOffsetY * scale), blurRadius = 0f) },
            platformStyle = PlatformTextStyle(includeFontPadding = false),
            lineHeightStyle = LineHeightStyle(LineHeightStyle.Alignment.Center, LineHeightStyle.Trim.Both)
        )

        Box(
            modifier = Modifier
                .offset(x = maxWidth * (t.left.toFloat() / imageWidth), y = maxHeight * (t.top.toFloat() / imageHeight))
                .size(
                    width = maxWidth * ((t.right - t.left).toFloat() / imageWidth),
                    height = maxHeight * ((t.bottom - t.top).toFloat() / imageHeight)
                )
                .graphicsLayer { rotationZ = t.rotation },
            contentAlignment = if (t.centered) Alignment.Center else Alignment.TopStart
        ) {
            BasicText(
                text = t.text,
                modifier = Modifier.fillMaxWidth(),
                style = style,
                autoSize = TextAutoSize.StepBased(
                    minFontSize = fontSize * 0.5f,
                    maxFontSize = fontSize,
                    stepSize = 0.25.sp
                )
            )
        }
    }
}

/**
 * Gambar desain (nodpi) dengan rasio aslinya dan [texts] di atasnya. Lebarnya diatur pemanggil
 * (fillMaxWidth, weight, ...); dipakai untuk kartu yang teksnya sudah dihapus dari gambar.
 */
@Composable
internal fun DesignTextImage(
    bitmap: ImageBitmap,
    contentDescription: String?,
    texts: List<DesignText>,
    modifier: Modifier = Modifier
) {
    BoxWithConstraints(modifier = modifier.aspectRatio(bitmap.width.toFloat() / bitmap.height)) {
        Image(
            bitmap = bitmap,
            contentDescription = contentDescription,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.FillBounds,
            filterQuality = FilterQuality.High
        )
        DesignTexts(texts, bitmap.width, bitmap.height)
    }
}
