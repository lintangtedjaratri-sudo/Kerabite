package com.test.cobacompose.ui.components

import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.calculateCentroid
import androidx.compose.foundation.gestures.calculatePan
import androidx.compose.foundation.gestures.calculateZoom
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.positionChanged
import androidx.compose.ui.unit.IntSize

private const val MaxZoom = 4f

/** Status zoom halaman: skala 1x–4x dan geseran konten saat diperbesar. */
@Stable
class PinchZoomState {
    var scale by mutableFloatStateOf(1f)
        private set
    var offset by mutableStateOf(Offset.Zero)
        private set

    val isZoomed: Boolean get() = scale > 1.01f

    fun reset() {
        scale = 1f
        offset = Offset.Zero
    }

    /** Zoom di sekitar titik tengah dua jari, lalu geser; konten tidak boleh keluar dari layar. */
    internal fun transform(centroid: Offset, pan: Offset, zoom: Float, size: IntSize) {
        val newScale = (scale * zoom).coerceIn(1f, MaxZoom)
        val zoomed = centroid - (centroid - offset) * (newScale / scale) + pan
        offset = Offset(
            x = zoomed.x.coerceIn(size.width * (1 - newScale), 0f),
            y = zoomed.y.coerceIn(size.height * (1 - newScale), 0f)
        )
        scale = newScale
    }
}

@Composable
fun rememberPinchZoomState(): PinchZoomState = remember { PinchZoomState() }

/**
 * Perbesar konten dengan cubit dua jari (geser dengan dua jari saat diperbesar).
 * Gestur satu jari tetap diteruskan ke scroll dan pager di dalamnya.
 */
fun Modifier.pinchToZoom(state: PinchZoomState, enabled: Boolean = true): Modifier =
    this
        .clipToBounds()
        .pointerInput(state, enabled) {
            if (!enabled) return@pointerInput
            awaitEachGesture {
                do {
                    // Pass Initial: dua jari ditangani di sini sebelum scroll/pager anak ikut bergerak
                    val event = awaitPointerEvent(PointerEventPass.Initial)
                    if (event.changes.count { it.pressed } >= 2) {
                        state.transform(
                            centroid = event.calculateCentroid(useCurrent = true),
                            pan = event.calculatePan(),
                            zoom = event.calculateZoom(),
                            size = size
                        )
                        event.changes.forEach { if (it.positionChanged()) it.consume() }
                    }
                } while (event.changes.any { it.pressed })
            }
        }
        .graphicsLayer {
            scaleX = state.scale
            scaleY = state.scale
            translationX = state.offset.x
            translationY = state.offset.y
            transformOrigin = TransformOrigin(0f, 0f)
        }
