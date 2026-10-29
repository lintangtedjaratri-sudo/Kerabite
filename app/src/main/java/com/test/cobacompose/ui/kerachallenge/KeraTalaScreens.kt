package com.test.cobacompose.ui.kerachallenge

import android.Manifest
import android.content.ActivityNotFoundException
import android.content.pm.PackageManager
import android.graphics.Paint
import android.graphics.PathMeasure
import android.graphics.Typeface
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.DrawableRes
import androidx.annotation.FontRes
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.GenericShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.TextAutoSize
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.FilterQuality
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalResources
import androidx.compose.ui.res.imageResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.core.content.res.ResourcesCompat
import com.test.cobacompose.R
import com.test.cobacompose.data.challenge.TalaDoc
import com.test.cobacompose.data.challenge.TalaDocKind
import com.test.cobacompose.ui.kerarecommend.KeraRecipe
import com.test.cobacompose.ui.theme.KeraTalaButton
import com.test.cobacompose.ui.theme.KeraTalaButtonDot
import com.test.cobacompose.ui.theme.KeraTalaCookBackground
import com.test.cobacompose.ui.theme.KeraTalaCookTitle
import com.test.cobacompose.ui.theme.KeraTalaCream
import com.test.cobacompose.ui.theme.KeraTalaGreen
import com.test.cobacompose.ui.theme.KeraTalaInk
import com.test.cobacompose.ui.theme.KeraTalaPanduanText
import com.test.cobacompose.ui.theme.KeraTalaTitleFill
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/** Foto Kera Recommend untuk kartu menu Tala Hari 1. */
private val KeraRecipe.menuPhotoRes: Int
    get() = when (this) {
        KeraRecipe.UBEE_CHEESE_BREAD -> R.drawable.img_kerarecommend_ubee_hero
        KeraRecipe.GOGUMAPPANG -> R.drawable.img_kerarecommend_gogumappang_hero
        KeraRecipe.TARO_CROQUETTE -> R.drawable.img_kerarecommend_taro_hero
    }

/**
 * Tala Hari 1 - Rancang & Pilih (desain 44): pilih menu dari Kera Recommend.
 * Menekan kartu menu menyelesaikan hari ini dan membuka resepnya; "Mari Kita Mulai!" lanjut ke Hari 2.
 */
@Composable
fun KeraTalaMenuScreen(
    chosenRecipe: KeraRecipe?,
    onRecipeClick: (KeraRecipe) -> Unit,
    onStartClick: () -> Unit,
    onBackClick: () -> Unit
) {
    val context = LocalContext.current
    val resources = LocalResources.current

    Box(modifier = Modifier.fillMaxSize()) {
        TaskLandscapeBackground()

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .statusBarsPadding()
                .navigationBarsPadding()
                .padding(top = 56.dp, bottom = 32.dp)
        ) {
            // Judul di gumpalan hijau, maskot kaca pembesar menempel di kanan (di depan kartu pertama)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .zIndex(1f)
            ) {
                CurvedTitleImage(
                    res = R.drawable.img_kerachallenge_tala_title_blob,
                    lines = menuTitleLines(),
                    modifier = Modifier
                        .padding(start = 8.dp)
                        .fillMaxWidth(0.72f)
                )
                TaskImage(
                    res = R.drawable.img_kerachallenge_tala_sparkle,
                    modifier = Modifier
                        .padding(start = 18.dp, top = 2.dp)
                        .width(34.dp)
                )
                TaskImage(
                    res = R.drawable.img_kerachallenge_tala_mascot_search,
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .offset(y = 28.dp)
                        .fillMaxWidth(0.4f)
                )
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                TaskImage(
                    res = R.drawable.img_kerachallenge_tala_bagikan_mascot,
                    modifier = Modifier.weight(0.3f)
                )
                Column(
                    modifier = Modifier
                        .weight(0.7f)
                        .padding(start = 4.dp, end = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    KeraRecipe.entries.forEach { recipe ->
                        MenuCard(
                            recipe = recipe,
                            chosen = recipe == chosenRecipe,
                            onClick = { onRecipeClick(recipe) }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            LetsStartButton(
                text = stringResource(R.string.kc_lets_start),
                color = KeraTalaButton,
                dotColor = KeraTalaButtonDot,
                onClick = {
                    if (chosenRecipe == null) {
                        Toast.makeText(context, resources.getString(R.string.tala_menu_pick_first), Toast.LENGTH_SHORT).show()
                    } else {
                        onStartClick()
                    }
                },
                modifier = Modifier.padding(horizontal = 48.dp)
            )
        }

        KeraChallengeBackButton(onClick = onBackClick, style = KeraBackButtonStyle.GREEN)
    }
}

/**
 * Satu baris teks yang mengikuti lengkung (kurva Bezier kuadrat lewat [start], [middle], [end]).
 * Semua angka = piksel gambar, jadi ikut mengecil bersama gambarnya.
 */
private class ArcLine(
    val text: String,
    val start: Offset,
    val middle: Offset,
    val end: Offset,
    val size: Float,
    @param:FontRes val font: Int,
    val fill: Color,
    val outline: Color? = null,
    val outlineWidth: Float = 0f
)

/** "Apa yang mau / kamu masak" melengkung di gumpalan hijau (gambar 900 x 529 px), seperti di desain 44. */
@Composable
private fun menuTitleLines() = listOf(
    ArcLine(
        stringResource(R.string.tala_menu_title_1),
        start = Offset(110f, 268f), middle = Offset(410f, 182f), end = Offset(790f, 212f),
        size = 82f, font = R.font.poppins_semibold,
        fill = KeraTalaTitleFill, outline = KeraTalaInk, outlineWidth = 7f
    ),
    ArcLine(
        stringResource(R.string.tala_menu_title_2),
        start = Offset(140f, 372f), middle = Offset(450f, 300f), end = Offset(770f, 340f),
        size = 100f, font = R.font.poppins_extrabold, fill = KeraTalaInk
    )
)

/** Gambar desain dengan [lines] digambar melengkung di atasnya (drawTextOnPath). */
@Composable
private fun CurvedTitleImage(
    @DrawableRes res: Int,
    lines: List<ArcLine>,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val bitmap = ImageBitmap.imageResource(res)
    val typefaces = remember(lines.map { it.font }) {
        lines.map { ResourcesCompat.getFont(context, it.font) ?: Typeface.DEFAULT }
    }

    Box(
        modifier = modifier
            .aspectRatio(bitmap.width.toFloat() / bitmap.height)
            .semantics { contentDescription = lines.joinToString(" ") { it.text } }
    ) {
        Image(
            bitmap = bitmap,
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.FillBounds,
            filterQuality = FilterQuality.High
        )
        Canvas(modifier = Modifier.fillMaxSize()) {
            val scale = size.width / bitmap.width
            drawIntoCanvas { canvas ->
                lines.forEachIndexed { index, line ->
                    // Titik kontrol dipilih supaya kurva melewati [middle]
                    val control = Offset(
                        2f * line.middle.x - (line.start.x + line.end.x) / 2f,
                        2f * line.middle.y - (line.start.y + line.end.y) / 2f
                    )
                    val path = android.graphics.Path().apply {
                        moveTo(line.start.x * scale, line.start.y * scale)
                        quadTo(control.x * scale, control.y * scale, line.end.x * scale, line.end.y * scale)
                    }
                    val length = PathMeasure(path, false).length
                    val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                        typeface = typefaces[index]
                        textSize = line.size * scale
                    }
                    // Terjemahan yang lebih panjang dari lengkungnya diperkecil
                    val fullWidth = paint.measureText(line.text)
                    if (fullWidth > length) paint.textSize *= length / fullWidth
                    // Rata tengah di lengkung (Align.CENTER tidak berlaku untuk drawTextOnPath di semua versi)
                    val startOffset = ((length - paint.measureText(line.text)) / 2f).coerceAtLeast(0f)

                    line.outline?.let { outline ->
                        paint.style = Paint.Style.STROKE
                        paint.strokeJoin = Paint.Join.ROUND
                        paint.strokeWidth = line.outlineWidth * scale
                        paint.color = outline.toArgb()
                        canvas.nativeCanvas.drawTextOnPath(line.text, path, startOffset, 0f, paint)
                    }
                    paint.style = Paint.Style.FILL
                    paint.color = line.fill.toArgb()
                    canvas.nativeCanvas.drawTextOnPath(line.text, path, startOffset, 0f, paint)
                }
            }
        }
    }
}

@Composable
private fun MenuCard(recipe: KeraRecipe, chosen: Boolean, onClick: () -> Unit) {
    val shape = RoundedCornerShape(22.dp)
    val name = recipe.cardTitle.replaceFirstChar { it.uppercase() }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(1.55f)
            .shadow(6.dp, shape)
            .clip(shape)
            .then(if (chosen) Modifier.border(3.dp, KeraTalaButtonDot, shape) else Modifier)
            .clickable(onClickLabel = name, onClick = onClick)
    ) {
        Image(
            bitmap = ImageBitmap.imageResource(recipe.menuPhotoRes),
            contentDescription = name,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop,
            filterQuality = FilterQuality.High
        )
        Text(
            text = name,
            modifier = Modifier
                .align(Alignment.Center)
                .padding(horizontal = 12.dp)
                .clip(RoundedCornerShape(14.dp))
                .background(Color.White.copy(alpha = 0.3f))
                .padding(horizontal = 8.dp, vertical = 2.dp),
            fontSize = 22.sp,
            lineHeight = 28.sp,
            fontStyle = FontStyle.Italic,
            color = Color.White,
            textAlign = TextAlign.Center
        )
        if (chosen) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(8.dp)
                    .size(30.dp)
                    .clip(CircleShape)
                    .background(KeraTalaButtonDot),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = stringResource(R.string.tala_menu_chosen),
                    tint = Color.White,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

/** Folder foto kamera Tala di penyimpanan internal (dibagikan ke kamera lewat FileProvider). */
private const val PHOTO_DIR = "kerachallenge"

/** Bagian bawah krem dengan tepi atas melengkung ke atas di tengah, seperti di desain 45. */
private val CreamHillShape = GenericShape { size, _ ->
    val dip = size.height * 0.7f
    moveTo(0f, dip)
    quadraticTo(size.width / 2f, -dip, size.width, dip)
    lineTo(size.width, size.height)
    lineTo(0f, size.height)
    close()
}

/**
 * Kamera sistem untuk foto Tala (Hari 2 dan Hari 3). Foto ditulis ke filesDir lewat FileProvider dan
 * dikirim sebagai [TalaDoc]. Mengembalikan fungsi yang membuka kamera (meminta izin CAMERA dulu kalau perlu).
 */
@Composable
internal fun rememberTalaCamera(onPhotoTaken: (TalaDoc) -> Unit): () -> Unit {
    val context = LocalContext.current
    val resources = LocalResources.current
    // Disimpan supaya foto tetap tercatat kalau aplikasi dimatikan selama kamera terbuka
    var pendingPhoto by rememberSaveable { mutableStateOf<String?>(null) }

    val camera = rememberLauncherForActivityResult(ActivityResultContracts.TakePicture()) { saved ->
        val uri = pendingPhoto
        pendingPhoto = null
        if (saved && uri != null) {
            val date = SimpleDateFormat("d MMM yyyy, HH.mm", Locale.getDefault()).format(Date())
            onPhotoTaken(TalaDoc(TalaDocKind.FOTO, uri, resources.getString(R.string.tala_cook_photo_name, date)))
        }
    }

    fun launchCamera() {
        val dir = File(context.filesDir, PHOTO_DIR).apply { mkdirs() }
        val file = File(dir, "tala_masak_${System.currentTimeMillis()}.jpg")
        val uri: Uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
        pendingPhoto = uri.toString()
        try {
            camera.launch(uri)
        } catch (_: ActivityNotFoundException) {
            pendingPhoto = null
            Toast.makeText(context, resources.getString(R.string.tala_cook_no_camera), Toast.LENGTH_SHORT).show()
        }
    }

    // Izin CAMERA dideklarasikan di manifest (untuk kamera Kera Campaign), jadi intent kamera sistem juga
    // butuh izin itu diberikan dulu; tanpanya ACTION_IMAGE_CAPTURE melempar SecurityException.
    val cameraPermission = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        if (granted) {
            launchCamera()
        } else {
            Toast.makeText(context, resources.getString(R.string.kc_camera_denied), Toast.LENGTH_SHORT).show()
        }
    }

    fun openCamera() {
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            launchCamera()
        } else {
            cameraPermission.launch(Manifest.permission.CAMERA)
        }
    }

    return ::openCamera
}

/**
 * Tala Hari 2 - Masak & Kreasi (desain 45): "Mari Kita Mulai!" menawarkan kamera atau galeri.
 * Fotonya disimpan di aplikasi, masuk ke dokumentasi Hari 3, dan menyelesaikan hari ini.
 */
@Composable
fun KeraTalaCookScreen(
    onPhotoTaken: (TalaDoc) -> Unit,
    onBackClick: () -> Unit
) {
    val context = LocalContext.current
    val openCamera = rememberTalaCamera(onPhotoTaken)
    var chooseSource by rememberSaveable { mutableStateOf(false) }

    val gallery = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        if (uri != null) onPhotoTaken(context.talaDoc(uri))
    }

    // "Mari Kita Mulai!" menanyakan sumber fotonya: kamera atau galeri
    if (chooseSource) {
        MediaSourceDialog(
            title = stringResource(R.string.tala_cook_source_title),
            accent = KeraTalaGreen,
            onCameraClick = {
                chooseSource = false
                openCamera()
            },
            onGalleryClick = {
                chooseSource = false
                gallery.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
            },
            onDismiss = { chooseSource = false }
        )
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(KeraTalaCream)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
        ) {
            // Pita hijau tua di atas (termasuk status bar)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(KeraTalaButtonDot)
                    .statusBarsPadding()
                    .height(24.dp)
            )

            Box(modifier = Modifier.fillMaxWidth()) {
                Image(
                    bitmap = ImageBitmap.imageResource(R.drawable.bg_kerachallenge_tala_cook),
                    contentDescription = null,
                    modifier = Modifier.matchParentSize(),
                    contentScale = ContentScale.Crop,
                    filterQuality = FilterQuality.High
                )
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    TaskImage(
                        res = R.drawable.img_kerachallenge_tala_chef,
                        modifier = Modifier
                            .padding(top = 64.dp)
                            .fillMaxWidth(0.94f)
                    )
                    Spacer(modifier = Modifier.height(36.dp))
                }
                // Krem melengkung menutupi bawah ilustrasi
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .fillMaxWidth()
                        .height(64.dp)
                        .background(KeraTalaCream, CreamHillShape)
                )
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
                    .padding(bottom = 32.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = stringResource(R.string.tala_cook_title),
                    modifier = Modifier.padding(horizontal = 24.dp),
                    fontSize = 46.sp,
                    lineHeight = 58.sp,
                    color = KeraTalaCookTitle,
                    textAlign = TextAlign.Center
                )
                Text(
                    text = stringResource(R.string.tala_cook_desc),
                    modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp),
                    fontSize = 17.sp,
                    lineHeight = 24.sp,
                    color = Color.Black,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(40.dp))

                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.CenterStart
                ) {
                    LetsStartButton(
                        text = stringResource(R.string.kc_lets_start),
                        color = KeraTalaButton,
                        dotColor = KeraTalaButtonDot,
                        onClick = { chooseSource = true },
                        modifier = Modifier.padding(start = 56.dp, end = 24.dp)
                    )
                    TaskImage(
                        res = R.drawable.img_kerachallenge_tala_mascot_leaf,
                        modifier = Modifier
                            .padding(start = 4.dp)
                            .width(104.dp)
                    )
                }
            }
        }

        KeraChallengeBackButton(onClick = onBackClick, style = KeraBackButtonStyle.GREEN)
    }
}

/** Panduan Kera Campaign (desain 47), dibuka dari "Lihat Panduan" di Tala Hari 3. */
@Composable
fun KeraTalaPanduanScreen(onBackClick: () -> Unit) {
    Box(modifier = Modifier.fillMaxSize()) {
        TaskLandscapeBackground()

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .statusBarsPadding()
                .navigationBarsPadding()
                .padding(top = 64.dp, bottom = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = stringResource(R.string.tala_panduan_title),
                modifier = Modifier.padding(start = 64.dp, end = 24.dp),
                fontWeight = FontWeight.Medium,
                color = Color.White,
                textAlign = TextAlign.Center,
                maxLines = 1,
                autoSize = TextAutoSize.StepBased(minFontSize = 16.sp, maxFontSize = 30.sp, stepSize = 0.5.sp)
            )
            // Garis bawah berujung panah
            Canvas(
                modifier = Modifier
                    .padding(top = 6.dp)
                    .fillMaxWidth(0.82f)
                    .height(10.dp)
            ) {
                val y = size.height / 2f
                val stroke = 2.dp.toPx()
                drawLine(Color.White, Offset(0f, y), Offset(size.width, y), stroke, StrokeCap.Round)
                drawLine(Color.White, Offset(size.width, y), Offset(size.width - 5.dp.toPx(), y - 4.dp.toPx()), stroke, StrokeCap.Round)
                drawLine(Color.White, Offset(size.width, y), Offset(size.width - 5.dp.toPx(), y + 4.dp.toPx()), stroke, StrokeCap.Round)
            }

            Box(
                modifier = Modifier
                    .padding(start = 14.dp, end = 14.dp, top = 24.dp)
                    .fillMaxWidth()
            ) {
                Image(
                    bitmap = ImageBitmap.imageResource(R.drawable.img_kerachallenge_panduan_photo),
                    contentDescription = stringResource(R.string.tala_panduan_title),
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(1f),
                    contentScale = ContentScale.Crop,
                    filterQuality = FilterQuality.High
                )
                TaskImage(
                    res = R.drawable.img_kerachallenge_tala_mascot_thumb,
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .offset(x = (-6).dp, y = 20.dp)
                        .fillMaxWidth(0.3f)
                )
            }

            Text(
                text = stringResource(R.string.tala_panduan_text),
                modifier = Modifier
                    .padding(start = 16.dp, end = 16.dp, top = 36.dp)
                    .clip(RoundedCornerShape(24.dp))
                    .background(Color.White.copy(alpha = 0.55f))
                    .padding(horizontal = 12.dp, vertical = 10.dp),
                fontSize = 22.sp,
                lineHeight = 32.sp,
                color = KeraTalaPanduanText,
                textAlign = TextAlign.Center
            )
        }

        KeraChallengeBackButton(onClick = onBackClick, style = KeraBackButtonStyle.GREEN)
    }
}
