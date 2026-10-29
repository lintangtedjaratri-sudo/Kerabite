package com.test.cobacompose.ui.kerachallenge

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.media.ExifInterface
import android.net.Uri
import android.provider.OpenableColumns
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.TextAutoSize
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalResources
import androidx.compose.ui.res.imageResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import androidx.core.net.toUri
import com.test.cobacompose.R
import com.test.cobacompose.data.challenge.TalaDoc
import com.test.cobacompose.data.challenge.TalaDocKind
import com.test.cobacompose.ui.theme.KeraTalaChip
import com.test.cobacompose.ui.theme.KeraTalaDash
import com.test.cobacompose.ui.theme.KeraTalaGreen
import com.test.cobacompose.ui.theme.KeraTalaGreenDark
import com.test.cobacompose.ui.theme.KeraTalaGreenLight
import com.test.cobacompose.ui.theme.KeraTalaPanel
import com.test.cobacompose.ui.theme.KeraTalaSticky
import com.test.cobacompose.ui.theme.KeraTalaStickyTape
import com.test.cobacompose.ui.theme.KeraTalaText
import com.test.cobacompose.ui.theme.KeraTalaUpload
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

private val TalaPanelShape = RoundedCornerShape(28.dp)

/** Letak tombol "Lihat Panduan" di gambar kartu Bagikan & Ajak (pecahan lebar/tinggi). */
private val guideButtonArea = ImageArea(0.6f, 0.68f, 0.957f, 0.885f)

private val TalaDocKind.icon: Int
    get() = when (this) {
        TalaDocKind.FOTO -> R.drawable.ic_image
        TalaDocKind.VIDEO -> R.drawable.ic_movie
        TalaDocKind.DOKUMEN -> R.drawable.ic_description
    }

/** Jumlah foto/video yang bisa dipilih sekaligus dari galeri. */
private const val GALLERY_MAX_ITEMS = 10

/** File yang dipilih dari picker; izinnya disimpan supaya file tetap bisa dibuka setelah aplikasi ditutup. */
internal fun Context.talaDoc(uri: Uri): TalaDoc {
    runCatching { contentResolver.takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION) }
    val mime = contentResolver.getType(uri).orEmpty()
    val kind = when {
        mime.startsWith("image/") -> TalaDocKind.FOTO
        mime.startsWith("video/") -> TalaDocKind.VIDEO
        else -> TalaDocKind.DOKUMEN
    }
    val name = runCatching {
        contentResolver.query(uri, arrayOf(OpenableColumns.DISPLAY_NAME), null, null, null)
            ?.use { cursor -> if (cursor.moveToFirst()) cursor.getString(0) else null }
    }.getOrNull() ?: uri.lastPathSegment.orEmpty()
    return TalaDoc(kind, uri.toString(), name)
}

/**
 * Tala Hari 3 - Bagikan & Ajak (desain 46). Foto masakan dari Hari 2 sudah ada di daftar
 * dokumentasi; file lain bisa ditambah dari galeri, kamera, atau dokumen. "Kirim ke Kera Campaign" (minimal 1 file)
 * mengunggah semuanya ke Firestore lewat [onKirimClick]; hari ini selesai setelah berhasil.
 */
@Composable
fun KeraTalaBagikanScreen(
    docs: List<TalaDoc>,
    sendProgress: Float?,
    onAddDocs: (List<TalaDoc>) -> Unit,
    onRemoveDoc: (TalaDoc) -> Unit,
    onGuideClick: () -> Unit,
    onKirimClick: () -> Unit,
    onBackClick: () -> Unit
) {
    val context = LocalContext.current
    val resources = LocalResources.current
    // Tiga sumber: galeri (foto/video), kamera (foto), dan dokumen (PDF)
    val gallery = rememberLauncherForActivityResult(ActivityResultContracts.PickMultipleVisualMedia(GALLERY_MAX_ITEMS)) { uris ->
        if (uris.isNotEmpty()) onAddDocs(uris.map { context.talaDoc(it) })
    }
    val openCamera = rememberTalaCamera { photo -> onAddDocs(listOf(photo)) }
    val picker = rememberLauncherForActivityResult(ActivityResultContracts.OpenMultipleDocuments()) { uris ->
        if (uris.isNotEmpty()) onAddDocs(uris.map { context.talaDoc(it) })
    }

    Box(modifier = Modifier.fillMaxSize()) {
        TaskLandscapeBackground()

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .statusBarsPadding()
                .navigationBarsPadding()
                .padding(top = 40.dp, bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Judul (kiri) dan maskot (kanan), sejajar di bawah
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp),
                verticalAlignment = Alignment.Bottom
            ) {
                DesignTextImage(
                    bitmap = ImageBitmap.imageResource(R.drawable.img_kerachallenge_tala_bagikan_title),
                    contentDescription = stringResource(R.string.tala_bagikan_title_desc),
                    texts = titleTexts(),
                    modifier = Modifier.weight(0.6f)
                )
                TaskImage(
                    res = R.drawable.img_kerachallenge_tala_bagikan_mascot,
                    modifier = Modifier.weight(0.4f)
                )
            }

            HotspotImage(
                imageRes = R.drawable.img_kerachallenge_tala_bagikan_card,
                contentDescription = stringResource(R.string.tala_bagikan_desc),
                hotspots = listOf(
                    ImageHotspot(
                        guideButtonArea.left, guideButtonArea.top, guideButtonArea.right, guideButtonArea.bottom,
                        resources.getString(R.string.tala_bagikan_guide), onGuideClick
                    )
                ),
                texts = cardTexts(),
                modifier = Modifier.padding(horizontal = 6.dp)
            )

            UploadPanel(
                docs = docs,
                onGalleryClick = {
                    gallery.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageAndVideo))
                },
                onCameraClick = openCamera,
                onDocumentClick = { picker.launch(arrayOf("application/pdf")) },
                onRemoveDoc = onRemoveDoc
            )

            TotalFileBar(
                count = docs.size,
                onKirimClick = {
                    if (docs.isEmpty()) {
                        Toast.makeText(context, resources.getString(R.string.tala_kirim_empty), Toast.LENGTH_SHORT).show()
                    } else {
                        onKirimClick()
                    }
                }
            )
        }

        KeraChallengeBackButton(onClick = onBackClick, style = KeraBackButtonStyle.GREEN)

        sendProgress?.let { CampaignSendingOverlay(progress = it, accent = KeraTalaGreen) }
    }
}

/** Pil "Tantangan 2" dan "3 Hari" di gambar judul (730 px). */
@Composable
private fun titleTexts() = listOf(
    DesignText(
        stringResource(R.string.kc_tantangan_n, 2), 226, 70, 566, 132,
        size = 46f, color = Color.White, centered = true, align = TextAlign.Center
    ),
    DesignText(
        stringResource(R.string.kc_days_3), 312, 472, 500, 530,
        size = 48f, color = KeraTalaGreenDark, centered = true, align = TextAlign.Center
    )
)

/** Judul, penjelasan, dan tombol panduan di gambar kartu (1080 px). */
@Composable
private fun cardTexts() = listOf(
    DesignText(
        stringResource(R.string.tala_day3_title), 590, 44, 900, 168,
        size = 48f, color = KeraTalaGreenDark, lineHeight = 56f
    ),
    DesignText(
        stringResource(R.string.tala_bagikan_desc), 592, 176, 990, 284,
        size = 23f, color = KeraTalaText, weight = FontWeight.Normal, lineHeight = 33f
    ),
    DesignText(
        stringResource(R.string.tala_bagikan_guide), 748, 320, 975, 368,
        size = 29f, color = Color.White, weight = FontWeight.SemiBold, centered = true, align = TextAlign.Center
    )
)

@Composable
private fun UploadPanel(
    docs: List<TalaDoc>,
    onGalleryClick: () -> Unit,
    onCameraClick: () -> Unit,
    onDocumentClick: () -> Unit,
    onRemoveDoc: (TalaDoc) -> Unit
) {
    Box(modifier = Modifier.padding(horizontal = 12.dp)) {
        Column(
            modifier = Modifier
                .padding(top = 14.dp)
                .fillMaxWidth()
                .shadow(6.dp, TalaPanelShape)
                .clip(TalaPanelShape)
                .background(KeraTalaPanel)
                .border(2.dp, Color.White, TalaPanelShape)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            UploadRibbon()

            Text(
                text = stringResource(R.string.bagikan_upload_desc),
                modifier = Modifier.padding(end = 92.dp),
                fontSize = 12.sp,
                lineHeight = 17.sp,
                color = KeraTalaText
            )

            UploadArea(
                docs = docs,
                onGalleryClick = onGalleryClick,
                onCameraClick = onCameraClick,
                onDocumentClick = onDocumentClick,
                onRemoveDoc = onRemoveDoc
            )
        }

        StickyNote(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .offset(x = 2.dp, y = 30.dp)
        )
    }
}

/** Pita hijau "Upload Dokumentasi", sedikit miring seperti di desain. */
@Composable
private fun UploadRibbon() {
    val shape = RoundedCornerShape(16.dp)

    Row(
        modifier = Modifier
            .graphicsLayer { rotationZ = -2f }
            .shadow(4.dp, shape)
            .clip(shape)
            .background(Brush.verticalGradient(listOf(KeraTalaGreenLight, KeraTalaGreen)))
            .padding(horizontal = 18.dp, vertical = 9.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            painter = painterResource(R.drawable.ic_photo_camera),
            contentDescription = null,
            tint = Color.White,
            modifier = Modifier.size(26.dp)
        )
        Spacer(modifier = Modifier.width(12.dp))
        Text(
            text = stringResource(R.string.bagikan_upload),
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )
    }
}

/** Catatan tempel kuning "Yuk, tunjukin aksi kamu!" dengan selotip. */
@Composable
private fun StickyNote(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .graphicsLayer { rotationZ = -6f }
            .width(96.dp),
        contentAlignment = Alignment.TopCenter
    ) {
        Text(
            text = stringResource(R.string.tala_bagikan_sticky),
            modifier = Modifier
                .padding(top = 8.dp)
                .fillMaxWidth()
                .shadow(3.dp, RoundedCornerShape(4.dp))
                .background(KeraTalaSticky, RoundedCornerShape(4.dp))
                .padding(horizontal = 8.dp, vertical = 12.dp),
            fontSize = 12.sp,
            lineHeight = 16.sp,
            fontWeight = FontWeight.SemiBold,
            fontStyle = FontStyle.Italic,
            color = KeraTalaGreenDark,
            textAlign = TextAlign.Center
        )
        Box(
            modifier = Modifier
                .size(width = 30.dp, height = 14.dp)
                .background(KeraTalaStickyTape.copy(alpha = 0.85f), RoundedCornerShape(3.dp))
        )
    }
}

@Composable
private fun UploadArea(
    docs: List<TalaDoc>,
    onGalleryClick: () -> Unit,
    onCameraClick: () -> Unit,
    onDocumentClick: () -> Unit,
    onRemoveDoc: (TalaDoc) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(KeraTalaUpload)
            .drawBehind {
                drawRoundRect(
                    color = KeraTalaDash,
                    style = Stroke(
                        width = 2.dp.toPx(),
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(10.dp.toPx(), 6.dp.toPx()))
                    ),
                    cornerRadius = CornerRadius(20.dp.toPx())
                )
            }
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        TaskImage(res = R.drawable.img_kerachallenge_tala_upload, modifier = Modifier.fillMaxWidth(0.55f))

        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = stringResource(R.string.bagikan_add_label),
                fontSize = 15.sp,
                fontWeight = FontWeight.Medium,
                color = KeraTalaText,
                textAlign = TextAlign.Center
            )
            Text(
                text = stringResource(R.string.bagikan_types),
                fontSize = 13.sp,
                color = KeraTalaText,
                textAlign = TextAlign.Center
            )
        }

        Row(horizontalArrangement = Arrangement.spacedBy(14.dp)) {
            FormatChip(R.drawable.ic_image, "JPG/PNG")
            FormatChip(R.drawable.ic_movie, "MP4")
            FormatChip(R.drawable.ic_description, "PDF")
        }

        // Pilih sumber file
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            MediaSourceButton(R.drawable.ic_photo_library, stringResource(R.string.bagikan_from_gallery), KeraTalaGreen, onGalleryClick)
            MediaSourceButton(R.drawable.ic_photo_camera, stringResource(R.string.bagikan_take_photo), KeraTalaGreen, onCameraClick)
            MediaSourceButton(R.drawable.ic_description, stringResource(R.string.bagikan_from_files), KeraTalaGreen, onDocumentClick)
        }

        docs.forEach { doc -> DocRow(doc = doc, onRemove = { onRemoveDoc(doc) }) }
    }
}

@Composable
private fun FormatChip(icon: Int, label: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier
                .size(width = 58.dp, height = 40.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(KeraTalaChip),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                painter = painterResource(icon),
                contentDescription = null,
                tint = KeraTalaGreenDark,
                modifier = Modifier.size(24.dp)
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(text = label, fontSize = 11.sp, color = KeraTalaText)
    }
}

/** Satu file dokumentasi; foto ditampilkan sebagai gambar kecil. */
@Composable
private fun DocRow(doc: TalaDoc, onRemove: () -> Unit) {
    val thumbnail = if (doc.kind == TalaDocKind.FOTO) rememberThumbnail(doc.uri) else null

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(Color.White)
            .padding(start = 6.dp, top = 6.dp, bottom = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(KeraTalaChip),
            contentAlignment = Alignment.Center
        ) {
            if (thumbnail != null) {
                Image(
                    bitmap = thumbnail,
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            } else {
                Icon(
                    painter = painterResource(doc.kind.icon),
                    contentDescription = null,
                    tint = KeraTalaGreenDark,
                    modifier = Modifier.size(22.dp)
                )
            }
        }
        Spacer(modifier = Modifier.width(10.dp))
        Text(
            text = doc.name,
            modifier = Modifier.weight(1f),
            fontSize = 12.sp,
            color = KeraTalaText,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis
        )
        Box(
            modifier = Modifier
                .size(44.dp)
                .clickable(onClickLabel = stringResource(R.string.bagikan_remove, doc.name), onClick = onRemove),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Close,
                contentDescription = null,
                tint = KeraTalaGreenDark,
                modifier = Modifier.size(18.dp)
            )
        }
    }
}

/** Gambar kecil dari [uri] (dikecilkan dan diputar sesuai EXIF), dimuat di luar main thread. */
@Composable
private fun rememberThumbnail(uri: String): ImageBitmap? {
    val context = LocalContext.current
    val thumbnail by produceState<ImageBitmap?>(initialValue = null, uri) {
        value = withContext(Dispatchers.IO) { runCatching { context.loadThumbnail(uri.toUri()) }.getOrNull() }
    }
    return thumbnail
}

private fun Context.loadThumbnail(uri: Uri, targetPx: Int = 160): ImageBitmap? {
    val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
    contentResolver.openInputStream(uri)?.use { BitmapFactory.decodeStream(it, null, bounds) }
    if (bounds.outWidth <= 0 || bounds.outHeight <= 0) return null

    var sample = 1
    while (minOf(bounds.outWidth, bounds.outHeight) / (sample * 2) >= targetPx) sample *= 2
    val bitmap = contentResolver.openInputStream(uri)?.use {
        BitmapFactory.decodeStream(it, null, BitmapFactory.Options().apply { inSampleSize = sample })
    } ?: return null

    val rotation = contentResolver.openInputStream(uri)?.use {
        when (ExifInterface(it).getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL)) {
            ExifInterface.ORIENTATION_ROTATE_90 -> 90f
            ExifInterface.ORIENTATION_ROTATE_180 -> 180f
            ExifInterface.ORIENTATION_ROTATE_270 -> 270f
            else -> 0f
        }
    } ?: 0f
    val upright = if (rotation == 0f) bitmap else {
        Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, Matrix().apply { postRotate(rotation) }, true)
    }
    return upright.asImageBitmap()
}

/** Bar bawah: jumlah file dan tombol "Kirim ke Kera Campaign". */
@Composable
private fun TotalFileBar(count: Int, onKirimClick: () -> Unit) {
    Row(
        modifier = Modifier
            .padding(horizontal = 12.dp)
            .fillMaxWidth()
            .shadow(6.dp, TalaPanelShape)
            .clip(TalaPanelShape)
            .background(KeraTalaPanel)
            .border(2.dp, Color.White, TalaPanelShape)
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(52.dp)
                .clip(CircleShape)
                .background(KeraTalaChip),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                painter = painterResource(R.drawable.ic_folder),
                contentDescription = null,
                tint = KeraTalaGreen,
                modifier = Modifier.size(28.dp)
            )
        }
        Spacer(modifier = Modifier.width(10.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(text = stringResource(R.string.tala_total_file), fontSize = 13.sp, color = KeraTalaText)
            Text(
                text = pluralStringResource(R.plurals.tala_file_count, count, count),
                fontSize = 17.sp,
                fontWeight = FontWeight.Bold,
                color = KeraTalaGreenDark
            )
        }
        Row(
            modifier = Modifier
                .weight(2f)
                .height(60.dp)
                .shadow(4.dp, RoundedCornerShape(50))
                .clip(RoundedCornerShape(50))
                .background(Brush.verticalGradient(listOf(KeraTalaGreenLight, KeraTalaGreen, KeraTalaGreenDark)))
                .clickable(onClickLabel = stringResource(R.string.campaign_send), onClick = onKirimClick)
                .padding(horizontal = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.Send,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = stringResource(R.string.campaign_send),
                modifier = Modifier.weight(1f),
                lineHeight = 1.15.em,
                fontWeight = FontWeight.SemiBold,
                color = Color.White,
                maxLines = 2,
                autoSize = TextAutoSize.StepBased(minFontSize = 9.sp, maxFontSize = 15.sp, stepSize = 0.5.sp)
            )
            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(24.dp)
            )
        }
    }
}
