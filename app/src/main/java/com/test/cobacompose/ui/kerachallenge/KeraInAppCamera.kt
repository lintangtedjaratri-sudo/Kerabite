package com.test.cobacompose.ui.kerachallenge

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.net.Uri
import android.util.Size
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.ImageProxy
import androidx.camera.core.resolutionselector.ResolutionSelector
import androidx.camera.core.resolutionselector.ResolutionStrategy
import androidx.camera.view.LifecycleCameraController
import androidx.camera.view.PreviewView
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalResources
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.test.cobacompose.R
import com.test.cobacompose.ui.theme.KeraBagikanPurple
import java.io.ByteArrayOutputStream
import java.io.File
import java.util.concurrent.Executors
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/** Batas ukuran foto Kera Campaign (dari galeri maupun kamera). */
internal const val CAMPAIGN_PHOTO_MAX_BYTES = 2L * 1024 * 1024

/** Folder foto kamera Kera Campaign di penyimpanan internal. */
private const val CAMPAIGN_PHOTO_DIR = "kerachallenge/campaign"

/** Sisi terpanjang foto kamera setelah diperkecil; cukup tajam untuk dokumentasi dan jauh di bawah 2 MB. */
private const val PHOTO_MAX_SIDE = 1600

private val cameraExecutor by lazy { Executors.newSingleThreadExecutor() }

/**
 * Kamera di dalam aplikasi (CameraX), bukan kamera sistem: pratinjau, tombol jepret, dan tombol ganti
 * kamera depan/belakang. Foto diputar sesuai sensor, diperkecil, dan disimpan sebagai JPEG di bawah
 * [CAMPAIGN_PHOTO_MAX_BYTES] di `filesDir/kerachallenge/campaign/<filePrefix>_<waktu>.jpg`.
 * Kalau izin kamera belum ada, izinnya diminta dulu.
 */
@Composable
internal fun KeraInAppCamera(
    filePrefix: String,
    onPhotoSaved: (File) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val resources = LocalResources.current

    var granted by remember {
        mutableStateOf(ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED)
    }
    var asked by rememberSaveable { mutableStateOf(false) }
    val permission = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) {
        granted = it
        asked = true
    }
    LaunchedEffect(Unit) {
        if (!granted && !asked) permission.launch(Manifest.permission.CAMERA)
    }

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(18.dp))
            .background(Color.Black),
        contentAlignment = Alignment.Center
    ) {
        if (granted) {
            CameraPreview(
                filePrefix = filePrefix,
                onPhotoSaved = onPhotoSaved,
                onError = { Toast.makeText(context, resources.getString(R.string.kc_camera_failed), Toast.LENGTH_SHORT).show() }
            )
        } else {
            Column(
                modifier = Modifier.padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = stringResource(R.string.kc_camera_denied),
                    color = Color.White,
                    fontSize = 13.sp,
                    textAlign = TextAlign.Center
                )
                Text(
                    text = stringResource(R.string.kc_camera_allow),
                    color = Color.White,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier
                        .clip(RoundedCornerShape(50))
                        .background(KeraBagikanPurple)
                        .clickable(role = Role.Button) { permission.launch(Manifest.permission.CAMERA) }
                        .padding(horizontal = 18.dp, vertical = 8.dp)
                )
            }
        }
    }
}

@Composable
private fun CameraPreview(
    filePrefix: String,
    onPhotoSaved: (File) -> Unit,
    onError: () -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    var busy by remember { mutableStateOf(false) }
    var useFront by rememberSaveable { mutableStateOf(false) }

    val controller = remember {
        LifecycleCameraController(context).apply {
            setEnabledUseCases(LifecycleCameraController.IMAGE_CAPTURE)
            imageCaptureMode = ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY
            // Ambil resolusi sedang saja; foto tetap diperkecil lagi sebelum disimpan
            imageCaptureResolutionSelector = ResolutionSelector.Builder()
                .setResolutionStrategy(
                    ResolutionStrategy(Size(1920, 1440), ResolutionStrategy.FALLBACK_RULE_CLOSEST_LOWER_THEN_HIGHER)
                )
                .build()
        }
    }

    DisposableEffect(lifecycleOwner) {
        controller.bindToLifecycle(lifecycleOwner)
        onDispose { controller.unbind() }
    }

    LaunchedEffect(useFront) {
        val selector = if (useFront) CameraSelector.DEFAULT_FRONT_CAMERA else CameraSelector.DEFAULT_BACK_CAMERA
        // hasCamera melempar exception sebelum CameraX selesai disiapkan, jadi tunggu dulu
        controller.initializationFuture.addListener({
            if (runCatching { controller.hasCamera(selector) }.getOrDefault(false)) controller.cameraSelector = selector
        }, ContextCompat.getMainExecutor(context))
    }

    Box(modifier = Modifier.fillMaxSize()) {
        AndroidView(
            factory = { viewContext ->
                PreviewView(viewContext).apply {
                    // TextureView supaya pratinjau ikut bergulir dengan halaman tanpa berkedip
                    implementationMode = PreviewView.ImplementationMode.COMPATIBLE
                    scaleType = PreviewView.ScaleType.FILL_CENTER
                    this.controller = controller
                }
            },
            modifier = Modifier.fillMaxSize()
        )

        // Tombol jepret
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 16.dp)
                .size(68.dp)
                .clip(CircleShape)
                .border(4.dp, Color.White, CircleShape)
                .padding(6.dp)
                .clip(CircleShape)
                .background(if (busy) Color.White.copy(alpha = 0.5f) else Color.White)
                .clickable(
                    enabled = !busy,
                    role = Role.Button,
                    onClickLabel = stringResource(R.string.kc_camera_shutter)
                ) {
                    busy = true
                    capturePhoto(context, controller, filePrefix) { file ->
                        busy = false
                        if (file != null) onPhotoSaved(file) else onError()
                    }
                },
            contentAlignment = Alignment.Center
        ) {
            if (busy) CircularProgressIndicator(color = KeraBagikanPurple, strokeWidth = 3.dp, modifier = Modifier.size(28.dp))
        }

        // Ganti kamera depan/belakang (misalnya untuk swafoto bersama teman)
        Box(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 20.dp, bottom = 26.dp)
                .size(44.dp)
                .clip(CircleShape)
                .background(Color.Black.copy(alpha = 0.45f))
                .clickable(role = Role.Button, onClickLabel = stringResource(R.string.kc_camera_switch)) {
                    useFront = !useFront
                },
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Refresh,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(24.dp)
            )
        }
    }
}

/** Menjepret, lalu memutar, memperkecil, dan menyimpan foto di thread kamera; hasilnya dikirim ke main thread. */
private fun capturePhoto(
    context: Context,
    controller: LifecycleCameraController,
    filePrefix: String,
    onDone: (File?) -> Unit
) {
    val mainExecutor = ContextCompat.getMainExecutor(context)
    controller.takePicture(cameraExecutor, object : ImageCapture.OnImageCapturedCallback() {
        override fun onCaptureSuccess(image: ImageProxy) {
            val file = runCatching {
                val bitmap = image.use { it.toBitmap().rotated(it.imageInfo.rotationDegrees) }
                val dir = File(context.filesDir, CAMPAIGN_PHOTO_DIR).apply { mkdirs() }
                File(dir, "${filePrefix}_${System.currentTimeMillis()}.jpg").also { target ->
                    target.writeBytes(compressPhoto(bitmap))
                }
            }.getOrNull()
            mainExecutor.execute { onDone(file) }
        }

        override fun onError(exception: ImageCaptureException) {
            mainExecutor.execute { onDone(null) }
        }
    })
}

private fun Bitmap.rotated(degrees: Int): Bitmap {
    val scale = minOf(1f, PHOTO_MAX_SIDE.toFloat() / maxOf(width, height))
    if (degrees == 0 && scale == 1f) return this
    val matrix = Matrix().apply {
        postScale(scale, scale)
        postRotate(degrees.toFloat())
    }
    return Bitmap.createBitmap(this, 0, 0, width, height, matrix, true)
}

/** JPEG dengan kualitas setinggi mungkin yang masih di bawah batas 2 MB. */
private fun compressPhoto(bitmap: Bitmap): ByteArray {
    var quality = 88
    while (true) {
        val bytes = ByteArrayOutputStream().use { out ->
            bitmap.compress(Bitmap.CompressFormat.JPEG, quality, out)
            out.toByteArray()
        }
        if (bytes.size <= CAMPAIGN_PHOTO_MAX_BYTES || quality <= 40) return bytes
        quality -= 12
    }
}

/** Gambar kecil dari URI foto (galeri atau kamera) untuk daftar dokumentasi; null selama dimuat atau gagal. */
@Composable
internal fun rememberThumbnail(uri: String, maxSide: Int = 256): ImageBitmap? {
    val context = LocalContext.current
    val thumbnail by produceState<ImageBitmap?>(null, uri) {
        value = withContext(Dispatchers.IO) {
            runCatching {
                val parsed = Uri.parse(uri)
                val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
                context.contentResolver.openInputStream(parsed)?.use { BitmapFactory.decodeStream(it, null, bounds) }
                var sample = 1
                while (maxOf(bounds.outWidth, bounds.outHeight) / (sample * 2) >= maxSide) sample *= 2
                val options = BitmapFactory.Options().apply { inSampleSize = sample }
                context.contentResolver.openInputStream(parsed)?.use { BitmapFactory.decodeStream(it, null, options) }
                    ?.asImageBitmap()
            }.getOrNull()
        }
    }
    return thumbnail
}

