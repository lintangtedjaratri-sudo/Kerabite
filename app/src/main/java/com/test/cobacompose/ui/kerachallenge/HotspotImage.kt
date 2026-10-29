package com.test.cobacompose.ui.kerachallenge

import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.FilterQuality
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.res.imageResource
import androidx.compose.ui.unit.dp

/**
 * Area yang bisa ditekan di atas gambar desain, dalam pecahan lebar/tinggi gambar (0..1),
 * supaya posisinya tetap pas di semua ukuran layar.
 */
internal data class ImageHotspot(
    val left: Float,
    val top: Float,
    val right: Float,
    val bottom: Float,
    val label: String,
    val onClick: () -> Unit
)

/**
 * Konten yang digambar di atas gambar desain pada [area] (pecahan lebar/tinggi gambar),
 * mis. bintang, tombol berwarna, atau pil status hari yang berubah sesuai progres.
 */
internal class ImageOverlay(
    val area: ImageArea,
    val content: @Composable () -> Unit
)

/** Overlay berupa gambar drawable yang direntangkan memenuhi [area]. */
internal fun drawableOverlay(@DrawableRes res: Int, area: ImageArea) = ImageOverlay(area) {
    Image(
        bitmap = ImageBitmap.imageResource(res),
        contentDescription = null,
        modifier = Modifier.fillMaxSize(),
        contentScale = ContentScale.FillBounds,
        filterQuality = FilterQuality.High
    )
}

/** Gambar selebar layar (rasio asli) dengan [overlays] lalu area tekan tak terlihat di atasnya. */
@Composable
internal fun HotspotImage(
    @DrawableRes imageRes: Int,
    contentDescription: String?,
    hotspots: List<ImageHotspot>,
    modifier: Modifier = Modifier,
    overlays: List<ImageOverlay> = emptyList(),
    texts: List<DesignText> = emptyList()
) {
    val bitmap = ImageBitmap.imageResource(imageRes)
    val aspectRatio = bitmap.width.toFloat() / bitmap.height

    BoxWithConstraints(
        modifier = modifier
            .fillMaxWidth()
            .aspectRatio(aspectRatio)
    ) {
        Image(
            bitmap = bitmap,
            contentDescription = contentDescription,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.FillBounds,
            // Gambar besar diperkecil ke layar; High supaya teks di gambar tetap tajam
            filterQuality = FilterQuality.High
        )

        // Teks di bawah overlay, supaya kunci (LockedVeil) tetap menutupi tombol yang terkunci
        DesignTexts(texts, bitmap.width, bitmap.height)

        overlays.forEach { overlay ->
            Box(
                modifier = Modifier
                    .offset(x = maxWidth * overlay.area.left, y = maxHeight * overlay.area.top)
                    .size(
                        width = maxWidth * (overlay.area.right - overlay.area.left),
                        height = maxHeight * (overlay.area.bottom - overlay.area.top)
                    )
            ) {
                overlay.content()
            }
        }

        hotspots.forEach { spot ->
            Box(
                modifier = Modifier
                    .offset(x = maxWidth * spot.left, y = maxHeight * spot.top)
                    .size(
                        width = maxWidth * (spot.right - spot.left),
                        height = maxHeight * (spot.bottom - spot.top)
                    )
                    .clip(RoundedCornerShape(24.dp))
                    .clickable(onClickLabel = spot.label, onClick = spot.onClick)
            )
        }
    }
}

/**
 * Halaman berisi satu gambar desain yang bisa digulir. Gambar mulai di bawah status bar,
 * dan area status bar diwarnai [topColor] (warna langit di tepi atas gambar).
 */
@Composable
internal fun HotspotImagePage(
    @DrawableRes imageRes: Int,
    contentDescription: String?,
    topColor: Color,
    hotspots: List<ImageHotspot>,
    modifier: Modifier = Modifier,
    overlays: List<ImageOverlay> = emptyList(),
    texts: List<DesignText> = emptyList()
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color.White)
            .verticalScroll(rememberScrollState())
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(topColor)
                .statusBarsPadding()
        )

        HotspotImage(
            imageRes = imageRes,
            contentDescription = contentDescription,
            hotspots = hotspots,
            overlays = overlays,
            texts = texts
        )
    }
}
