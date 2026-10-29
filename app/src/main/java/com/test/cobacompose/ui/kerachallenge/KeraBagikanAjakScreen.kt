package com.test.cobacompose.ui.kerachallenge

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import android.util.Patterns
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.DrawableRes
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.FilterQuality
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalResources
import androidx.compose.ui.res.imageResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.test.cobacompose.R
import com.test.cobacompose.data.challenge.CampaignSubmission
import com.test.cobacompose.data.challenge.SubmissionFile
import com.test.cobacompose.data.challenge.SubmissionFileKind
import com.test.cobacompose.ui.theme.KeraBagikanCard
import com.test.cobacompose.ui.theme.KeraBagikanCountPill
import com.test.cobacompose.ui.theme.KeraBagikanCountText
import com.test.cobacompose.ui.theme.KeraBagikanDash
import com.test.cobacompose.ui.theme.KeraBagikanDone
import com.test.cobacompose.ui.theme.KeraBagikanDoneText
import com.test.cobacompose.ui.theme.KeraBagikanPanel
import com.test.cobacompose.ui.theme.KeraBagikanPurple
import com.test.cobacompose.ui.theme.KeraBagikanPurpleDark
import com.test.cobacompose.ui.theme.KeraBagikanPurpleLight
import com.test.cobacompose.ui.theme.KeraBagikanRow
import com.test.cobacompose.ui.theme.KeraBagikanTaglineInk
import com.test.cobacompose.ui.theme.KeraBagikanText
import com.test.cobacompose.ui.theme.KeraBagikanTitleInk
import com.test.cobacompose.ui.theme.KeraGameLocked
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlinx.coroutines.launch

/** Batas dokumentasi Kera Campaign. */
private const val MAX_PHOTOS = 3
private const val MAX_VIDEO_BYTES = 10L * 1024 * 1024
/** Batas karakter caption dan link. */
private const val MAX_TEXT_CHARS = 500

/** Jenis file dokumentasi: foto masuk ke "Foto kegiatan", video ke "Video kegiatan". */
private enum class DocKind(@DrawableRes val icon: Int) {
    FOTO(R.drawable.ic_image),
    VIDEO(R.drawable.ic_movie)
}

/** Akordeon di Checklist Dokumentasi. */
private enum class CampaignSection { CAPTION, INVITE, LINK }

/** File yang dipilih. Disimpan sebagai "jenis|uri|nama" supaya bisa masuk rememberSaveable. */
private data class PickedDoc(val kind: DocKind, val uri: String, val name: String) {
    fun encode() = "${kind.name}|$uri|$name"

    companion object {
        fun decode(value: String): PickedDoc {
            val (kind, uri, name) = value.split('|', limit = 3)
            return PickedDoc(DocKind.valueOf(kind), uri, name)
        }
    }
}

/** Nama dan ukuran file dari galeri; jenisnya null kalau bukan foto/video. */
private class PickedInfo(val kind: DocKind?, val name: String, val size: Long)

private fun Context.pickedInfo(uri: Uri): PickedInfo {
    val mime = contentResolver.getType(uri).orEmpty()
    val kind = when {
        mime.startsWith("image/") -> DocKind.FOTO
        mime.startsWith("video/") -> DocKind.VIDEO
        else -> null
    }
    var name: String? = null
    var size = -1L
    contentResolver.query(uri, arrayOf(OpenableColumns.DISPLAY_NAME, OpenableColumns.SIZE), null, null, null)
        ?.use { cursor ->
            if (cursor.moveToFirst()) {
                name = cursor.getString(0)
                if (!cursor.isNull(1)) size = cursor.getLong(1)
            }
        }
    // Kalau penyedia tidak memberi ukuran, hitung dari isi file
    if (size < 0) {
        size = runCatching { contentResolver.openAssetFileDescriptor(uri, "r")?.use { it.length } }.getOrNull() ?: -1L
    }
    return PickedInfo(kind, name ?: uri.lastPathSegment.orEmpty(), size)
}

/** Foto dari kamera aplikasi disimpan di filesDir; hapus filenya kalau sudah tidak dipakai. */
private fun deleteLocalPhoto(uri: String?) {
    val parsed = uri?.let(Uri::parse) ?: return
    if (parsed.scheme == "file") parsed.path?.let { File(it).delete() }
}

/** Link postingan: URL web (http/https, boleh tanpa skema) tanpa spasi. */
private fun isValidLink(text: String): Boolean {
    val link = text.trim()
    if (link.isEmpty() || link.any(Char::isWhitespace)) return false
    val scheme = Uri.parse(link).scheme?.lowercase()
    if (scheme != null && link.contains("://") && scheme != "http" && scheme != "https") return false
    return Patterns.WEB_URL.matcher(link).matches()
}

private val PillShape = RoundedCornerShape(50)
private val PanelShape = RoundedCornerShape(28.dp)

/**
 * Main Game Hari 3 (desain 46-47): halaman 1 Bagikan & Ajak (upload dokumentasi + checklist),
 * geser ke kiri untuk halaman 2 Kera Campaign. Teks dan panel digambar dengan Compose
 * supaya tajam; hanya maskot, judul, dan contoh postingan yang berupa gambar.
 *
 * Dokumentasi disimpan di rememberSaveable sampai dikirim. "Kirim ke Kera Campaign" mengecek semua item
 * checklist sudah lengkap, lalu [onKirimCampaignClick] mengunggahnya ke Firestore (progres lewat [sendProgress]):
 * - foto (galeri atau kamera aplikasi) maks. 3 dan ≤ 2 MB; foto ke-4 menggeser foto paling lama,
 * - video maks. 1 dan ≤ 10 MB; video baru menggantikan yang lama,
 * - caption dan link diisi lewat akordeon (maks. 500 karakter, link divalidasi),
 * - bukti mengajak difoto lewat kamera di dalam akordeon dan tidak ikut dihitung sebagai foto kegiatan.
 */
@Composable
fun KeraBagikanAjakScreen(
    sendProgress: Float?,
    onKirimCampaignClick: (CampaignSubmission) -> Unit,
    onBackClick: () -> Unit
) {
    val context = LocalContext.current
    val resources = LocalResources.current
    val coroutineScope = rememberCoroutineScope()
    val pagerState = rememberPagerState(pageCount = { 2 })

    var photos by rememberSaveable { mutableStateOf(listOf<String>()) }
    var video by rememberSaveable { mutableStateOf<String?>(null) }
    var caption by rememberSaveable { mutableStateOf("") }
    var link by rememberSaveable { mutableStateOf("") }
    var inviteProof by rememberSaveable { mutableStateOf<String?>(null) }
    var cameraOpen by rememberSaveable { mutableStateOf(false) }

    fun toast(message: String) = Toast.makeText(context, message, Toast.LENGTH_LONG).show()

    /** Menambah foto; kalau lebih dari 3, foto paling lama dibuang. Mengembalikan true kalau ada yang tergeser. */
    fun addPhotos(added: List<PickedDoc>): Boolean {
        val all = photos + added.map(PickedDoc::encode)
        val dropped = all.dropLast(MAX_PHOTOS)
        dropped.forEach { deleteLocalPhoto(PickedDoc.decode(it).uri) }
        photos = all.takeLast(MAX_PHOTOS)
        return dropped.isNotEmpty()
    }

    val gallery = rememberLauncherForActivityResult(ActivityResultContracts.PickMultipleVisualMedia(MAX_PHOTOS + 1)) { uris ->
        val messages = mutableListOf<String>()
        val newPhotos = mutableListOf<PickedDoc>()
        var newVideo: PickedDoc? = null
        var videoCount = 0
        uris.forEach { uri ->
            val info = context.pickedInfo(uri)
            when (info.kind) {
                DocKind.FOTO ->
                    if (info.size > CAMPAIGN_PHOTO_MAX_BYTES) {
                        messages += resources.getString(R.string.bagikan_photo_too_big, info.name)
                    } else {
                        newPhotos += PickedDoc(DocKind.FOTO, uri.toString(), info.name)
                    }
                DocKind.VIDEO ->
                    if (info.size > MAX_VIDEO_BYTES) {
                        messages += resources.getString(R.string.bagikan_video_too_big, info.name)
                    } else {
                        videoCount++
                        newVideo = PickedDoc(DocKind.VIDEO, uri.toString(), info.name)
                    }
                null -> messages += resources.getString(R.string.bagikan_unsupported, info.name)
            }
        }
        if (newPhotos.isNotEmpty() && addPhotos(newPhotos)) {
            messages += resources.getString(R.string.bagikan_photo_replaced)
        }
        newVideo?.let {
            if (video != null || videoCount > 1) {
                messages += resources.getString(R.string.bagikan_video_replaced)
            }
            video = it.encode()
        }
        if (messages.isNotEmpty()) toast(messages.joinToString("\n"))
    }

    Box(modifier = Modifier.fillMaxSize()) {
        // Latar pemandangan tetap di belakang kedua halaman
        Image(
            bitmap = ImageBitmap.imageResource(R.drawable.bg_kerachallenge_bagikan),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )

        HorizontalPager(
            state = pagerState,
            modifier = Modifier.fillMaxSize()
        ) { page ->
            when (page) {
                0 -> BagikanAjakPage(
                    photos = photos.map(PickedDoc::decode),
                    video = video?.let(PickedDoc::decode),
                    caption = caption,
                    link = link,
                    inviteProof = inviteProof,
                    onGalleryClick = {
                        gallery.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageAndVideo))
                    },
                    onCameraClick = { cameraOpen = true },
                    onRemove = { doc ->
                        deleteLocalPhoto(doc.uri)
                        if (doc.kind == DocKind.VIDEO) video = null else photos = photos - doc.encode()
                    },
                    onCaptionChange = { caption = it.take(MAX_TEXT_CHARS) },
                    onLinkChange = { link = it.take(MAX_TEXT_CHARS) },
                    onInviteProofChange = { uri ->
                        deleteLocalPhoto(inviteProof)
                        inviteProof = uri
                    }
                )
                else -> KeraCampaignPage(
                    onKirimClick = {
                        // Semua item checklist wajib lengkap sebelum dikirim
                        val missing = buildList {
                            if (photos.size < MAX_PHOTOS) add(R.string.bagikan_item_photo)
                            if (video == null) add(R.string.bagikan_item_video)
                            if (caption.isBlank()) add(R.string.bagikan_item_caption)
                            if (inviteProof == null) add(R.string.bagikan_item_invite)
                            if (!isValidLink(link)) add(R.string.bagikan_item_link)
                        }
                        if (missing.isNotEmpty()) {
                            val items = missing.joinToString(", ") { resources.getString(it) }
                            val linkHint = if (link.isNotBlank() && !isValidLink(link)) {
                                "\n" + resources.getString(R.string.bagikan_link_invalid)
                            } else {
                                ""
                            }
                            toast(resources.getString(R.string.bagikan_incomplete, items) + linkHint)
                            coroutineScope.launch { pagerState.animateScrollToPage(0) }
                        } else {
                            val files = photos.map(PickedDoc::decode).map { SubmissionFile(SubmissionFileKind.FOTO, it.uri, it.name) } +
                                listOfNotNull(video?.let(PickedDoc::decode)).map { SubmissionFile(SubmissionFileKind.VIDEO, it.uri, it.name) } +
                                listOfNotNull(inviteProof).map {
                                    SubmissionFile(SubmissionFileKind.BUKTI_AJAK, it, resources.getString(R.string.bagikan_invite_photo_desc))
                                }
                            onKirimCampaignClick(CampaignSubmission(files = files, caption = caption.trim(), link = link.trim()))
                        }
                    }
                )
            }
        }

        KeraChallengeBackButton(onClick = onBackClick, style = KeraBackButtonStyle.PURPLE)

        // Penanda halaman supaya pengguna tahu bisa digeser
        Row(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .navigationBarsPadding()
                .padding(bottom = 12.dp)
                .clip(PillShape)
                .background(Color.White.copy(alpha = 0.85f))
                .padding(horizontal = 10.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            repeat(pagerState.pageCount) { index ->
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(if (pagerState.currentPage == index) KeraBagikanPurple else KeraGameLocked)
                )
            }
        }

        sendProgress?.let { CampaignSendingOverlay(progress = it, accent = KeraBagikanPurple) }

        // Kamera aplikasi layar penuh untuk "Ambil Foto" di Upload Dokumentasi
        if (cameraOpen) {
            BackHandler { cameraOpen = false }
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black)
                    // Menahan sentuhan supaya tidak tembus ke halaman di belakang
                    .pointerInput(Unit) {}
            ) {
                KeraInAppCamera(
                    filePrefix = "foto_kegiatan",
                    onPhotoSaved = { file ->
                        val date = SimpleDateFormat("d MMM yyyy, HH.mm.ss", Locale.getDefault()).format(Date())
                        val name = resources.getString(R.string.bagikan_camera_photo_name, date)
                        if (addPhotos(listOf(PickedDoc(DocKind.FOTO, Uri.fromFile(file).toString(), name)))) {
                            toast(resources.getString(R.string.bagikan_photo_replaced))
                        }
                        cameraOpen = false
                    },
                    modifier = Modifier
                        .fillMaxSize()
                        .statusBarsPadding()
                        .navigationBarsPadding()
                )
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .statusBarsPadding()
                        .padding(12.dp)
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(Color.Black.copy(alpha = 0.45f))
                        .clickable(role = Role.Button, onClickLabel = stringResource(R.string.bagikan_camera_close)) {
                            cameraOpen = false
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(imageVector = Icons.Default.Close, contentDescription = null, tint = Color.White)
                }
            }
        }
    }
}

/** Kolom halaman yang bisa digulir, mulai di bawah tombol kembali dan berakhir di atas penanda halaman. */
@Composable
private fun HariTigaPage(content: @Composable () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .statusBarsPadding()
            .padding(top = 56.dp)
            .navigationBarsPadding()
            .padding(bottom = 48.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        content()
    }
}

// ---------------------------------------------------------------------------
// Halaman 1 - Bagikan & Ajak
// ---------------------------------------------------------------------------

@Composable
private fun BagikanAjakPage(
    photos: List<PickedDoc>,
    video: PickedDoc?,
    caption: String,
    link: String,
    inviteProof: String?,
    onGalleryClick: () -> Unit,
    onCameraClick: () -> Unit,
    onRemove: (PickedDoc) -> Unit,
    onCaptionChange: (String) -> Unit,
    onLinkChange: (String) -> Unit,
    onInviteProofChange: (String?) -> Unit
) {
    // Hanya satu akordeon yang terbuka; membuka yang lain menutup yang sedang terbuka
    var openSection by rememberSaveable { mutableStateOf<CampaignSection?>(null) }
    fun toggle(section: CampaignSection) {
        openSection = if (openSection == section) null else section
    }
    val linkValid = isValidLink(link)

    HariTigaPage {
        HariTigaHeader(
            titleRes = R.drawable.img_kerachallenge_bagikan_title,
            mascotRes = R.drawable.img_kerachallenge_bagikan_mascot,
            title = stringResource(R.string.bagikan_title_desc),
            titleTexts = hariTigaTitleTexts(
                pill = intArrayOf(140, 36, 414, 104),
                days = intArrayOf(300, 368, 452, 426),
                tagline = intArrayOf(98, 440, 608, 580),
                taglineText = stringResource(R.string.bagikan_tagline),
                taglineRotation = -3f
            )
        )

        HariTigaPanel {
            HariTigaRibbon(text = stringResource(R.string.bagikan_upload), icon = R.drawable.ic_photo_camera)

            Text(
                text = stringResource(R.string.bagikan_upload_desc),
                fontSize = 13.sp,
                lineHeight = 19.sp,
                color = KeraBagikanText
            )

            UploadArea(
                docs = photos + listOfNotNull(video),
                onGalleryClick = onGalleryClick,
                onCameraClick = onCameraClick,
                onRemove = onRemove
            )
        }

        HariTigaPanel {
            HariTigaRibbon(text = stringResource(R.string.bagikan_checklist), icon = R.drawable.ic_leaf)

            Row(
                modifier = Modifier
                    .align(Alignment.End)
                    .clip(PillShape)
                    .background(Color.White)
                    .border(1.dp, KeraBagikanRow, PillShape)
                    .padding(horizontal = 12.dp, vertical = 5.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(R.string.bagikan_check_hint),
                    fontSize = 11.sp,
                    fontStyle = FontStyle.Italic,
                    color = KeraBagikanText
                )
                Spacer(modifier = Modifier.width(4.dp))
                Icon(
                    imageVector = Icons.Default.Favorite,
                    contentDescription = null,
                    tint = KeraBagikanPurpleLight,
                    modifier = Modifier.size(12.dp)
                )
            }

            ChecklistRow(R.drawable.ic_photo_camera, stringResource(R.string.bagikan_item_photo), photos.size, MAX_PHOTOS)
            ChecklistRow(R.drawable.ic_videocam, stringResource(R.string.bagikan_item_video), if (video != null) 1 else 0, 1)

            ChecklistAccordion(
                icon = R.drawable.ic_description,
                label = stringResource(R.string.bagikan_item_caption),
                done = caption.isNotBlank(),
                expanded = openSection == CampaignSection.CAPTION,
                onToggle = { toggle(CampaignSection.CAPTION) }
            ) {
                CampaignTextField(
                    value = caption,
                    onValueChange = onCaptionChange,
                    placeholder = stringResource(R.string.bagikan_caption_hint),
                    singleLine = false
                )
            }

            ChecklistAccordion(
                icon = R.drawable.ic_groups,
                label = stringResource(R.string.bagikan_item_invite),
                done = inviteProof != null,
                expanded = openSection == CampaignSection.INVITE,
                onToggle = { toggle(CampaignSection.INVITE) }
            ) {
                InviteProofCamera(photo = inviteProof, onPhotoChange = onInviteProofChange)
            }

            ChecklistAccordion(
                icon = R.drawable.ic_link,
                label = stringResource(R.string.bagikan_item_link),
                done = linkValid,
                expanded = openSection == CampaignSection.LINK,
                onToggle = { toggle(CampaignSection.LINK) }
            ) {
                CampaignTextField(
                    value = link,
                    onValueChange = onLinkChange,
                    placeholder = stringResource(R.string.bagikan_link_hint),
                    singleLine = true,
                    keyboardType = KeyboardType.Uri,
                    error = if (link.isNotBlank() && !linkValid) stringResource(R.string.bagikan_link_invalid) else null
                )
            }
        }
    }
}

@Composable
private fun UploadArea(
    docs: List<PickedDoc>,
    onGalleryClick: () -> Unit,
    onCameraClick: () -> Unit,
    onRemove: (PickedDoc) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .drawBehind {
                drawRoundRect(
                    color = KeraBagikanDash,
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
        Row(verticalAlignment = Alignment.CenterVertically) {
            TiltedTile(icon = R.drawable.ic_image, rotation = -10f)
            Box(
                modifier = Modifier
                    .padding(horizontal = 10.dp)
                    .size(92.dp)
                    .shadow(6.dp, CircleShape)
                    .clip(CircleShape)
                    .background(Brush.verticalGradient(listOf(KeraBagikanPurpleLight, KeraBagikanPurpleDark))),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_cloud_upload),
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(54.dp)
                )
            }
            TiltedTile(icon = R.drawable.ic_movie, rotation = 10f)
        }

        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = stringResource(R.string.bagikan_add_media),
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold,
                color = KeraBagikanPurple
            )
            Text(
                text = stringResource(R.string.bagikan_limits),
                fontSize = 12.sp,
                textAlign = TextAlign.Center,
                color = KeraBagikanText
            )
        }

        // Pilih sumber: galeri (foto/video) atau kamera aplikasi (foto)
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            SourceButton(R.drawable.ic_photo_library, stringResource(R.string.bagikan_from_gallery), onGalleryClick)
            SourceButton(R.drawable.ic_photo_camera, stringResource(R.string.bagikan_take_photo), onCameraClick)
        }

        docs.forEach { doc ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color.White)
                    .padding(start = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                DocThumbnail(doc)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = doc.name,
                    fontSize = 12.sp,
                    color = KeraBagikanText,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clickable(onClickLabel = stringResource(R.string.bagikan_remove, doc.name)) { onRemove(doc) },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = null,
                        tint = KeraBagikanCountText,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun SourceButton(
    @DrawableRes icon: Int,
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .shadow(3.dp, PillShape)
            .clip(PillShape)
            .background(Brush.verticalGradient(listOf(KeraBagikanPurpleLight, KeraBagikanPurple)))
            .clickable(role = Role.Button, onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 9.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(painter = painterResource(icon), contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
        Spacer(modifier = Modifier.width(6.dp))
        Text(text = label, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = Color.White)
    }
}

/** Gambar mini foto; video cukup memakai ikonnya. */
@Composable
private fun DocThumbnail(doc: PickedDoc) {
    val thumbnail = if (doc.kind == DocKind.FOTO) rememberThumbnail(doc.uri) else null
    Box(
        modifier = Modifier
            .padding(vertical = 4.dp)
            .size(36.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(KeraBagikanRow),
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
                tint = KeraBagikanPurple,
                modifier = Modifier.size(18.dp)
            )
        }
    }
}

@Composable
private fun TiltedTile(@DrawableRes icon: Int, rotation: Float) {
    Box(
        modifier = Modifier
            .graphicsLayer { rotationZ = rotation }
            .size(48.dp)
            .shadow(4.dp, RoundedCornerShape(12.dp))
            .clip(RoundedCornerShape(12.dp))
            .background(Brush.verticalGradient(listOf(KeraBagikanPurpleLight, KeraBagikanPurple)))
            .border(2.dp, Color.White, RoundedCornerShape(12.dp)),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            painter = painterResource(icon),
            contentDescription = null,
            tint = Color.White,
            modifier = Modifier.size(26.dp)
        )
    }
}

/** Baris checklist yang bisa dibuka seperti akordeon untuk mengisi item itu langsung di bawahnya. */
@Composable
private fun ChecklistAccordion(
    @DrawableRes icon: Int,
    label: String,
    done: Boolean,
    expanded: Boolean,
    onToggle: () -> Unit,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(if (expanded) Color.White else Color.Transparent)
    ) {
        ChecklistRow(icon, label, if (done) 1 else 0, 1, expanded = expanded, onClick = onToggle)
        AnimatedVisibility(visible = expanded) {
            Column(
                modifier = Modifier.padding(start = 10.dp, end = 10.dp, top = 10.dp, bottom = 12.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                content = content
            )
        }
    }
}

@Composable
private fun ChecklistRow(
    @DrawableRes icon: Int,
    label: String,
    count: Int,
    target: Int,
    expanded: Boolean? = null,
    onClick: (() -> Unit)? = null
) {
    val done = count >= target

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(PillShape)
            .background(KeraBagikanRow)
            .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier)
            .padding(end = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(46.dp)
                .clip(CircleShape)
                .background(Brush.verticalGradient(listOf(KeraBagikanPurpleLight, KeraBagikanPurpleDark))),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                painter = painterResource(icon),
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(22.dp)
            )
        }
        Spacer(modifier = Modifier.width(12.dp))
        Text(
            text = label,
            fontSize = 13.sp,
            fontWeight = FontWeight.Medium,
            color = KeraBagikanText,
            modifier = Modifier.weight(1f)
        )
        Row(
            modifier = Modifier
                .clip(PillShape)
                .background(if (done) KeraBagikanDone else KeraBagikanCountPill)
                .padding(horizontal = 12.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (done) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = stringResource(R.string.kc_done),
                    tint = KeraBagikanDoneText,
                    modifier = Modifier.size(14.dp)
                )
                Spacer(modifier = Modifier.width(2.dp))
            }
            Text(
                text = "${count.coerceAtMost(target)}/$target",
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = if (done) KeraBagikanDoneText else KeraBagikanCountText
            )
        }
        if (expanded != null) {
            Icon(
                imageVector = if (expanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                contentDescription = null,
                tint = KeraBagikanPurple,
                modifier = Modifier
                    .padding(start = 4.dp)
                    .size(22.dp)
            )
        }
    }
}

/** Kotak isian caption/link: maks. [MAX_TEXT_CHARS] karakter dengan penghitung, dan pesan error kalau ada. */
@Composable
private fun CampaignTextField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    singleLine: Boolean,
    keyboardType: KeyboardType = KeyboardType.Text,
    error: String? = null
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = Modifier.fillMaxWidth(),
        textStyle = TextStyle(fontSize = 13.sp, color = KeraBagikanText),
        placeholder = { Text(text = placeholder, fontSize = 13.sp) },
        singleLine = singleLine,
        minLines = if (singleLine) 1 else 3,
        maxLines = if (singleLine) 1 else 8,
        isError = error != null,
        keyboardOptions = KeyboardOptions(
            keyboardType = keyboardType,
            imeAction = if (singleLine) ImeAction.Done else ImeAction.Default
        ),
        supportingText = {
            Row(modifier = Modifier.fillMaxWidth()) {
                Text(text = error.orEmpty(), fontSize = 11.sp, modifier = Modifier.weight(1f))
                Text(
                    text = stringResource(R.string.bagikan_char_count, value.length, MAX_TEXT_CHARS),
                    fontSize = 11.sp
                )
            }
        },
        shape = RoundedCornerShape(16.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = KeraBagikanPurple,
            unfocusedBorderColor = KeraBagikanRow,
            cursorColor = KeraBagikanPurple,
            focusedContainerColor = Color.White,
            unfocusedContainerColor = Color.White
        )
    )
}

/**
 * Isi akordeon bukti mengajak: kamera aplikasi atau foto dari galeri (≤ 2 MB), lalu fotonya
 * dengan tombol Foto Ulang / Galeri untuk menggantinya. Foto ini terpisah dari foto kegiatan.
 */
@Composable
private fun ColumnScope.InviteProofCamera(photo: String?, onPhotoChange: (String?) -> Unit) {
    val context = LocalContext.current
    val resources = LocalResources.current
    val gallery = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        if (uri == null) return@rememberLauncherForActivityResult
        val info = context.pickedInfo(uri)
        when {
            info.kind != DocKind.FOTO ->
                Toast.makeText(context, resources.getString(R.string.bagikan_unsupported, info.name), Toast.LENGTH_LONG).show()
            info.size > CAMPAIGN_PHOTO_MAX_BYTES ->
                Toast.makeText(context, resources.getString(R.string.bagikan_photo_too_big, info.name), Toast.LENGTH_LONG).show()
            else -> onPhotoChange(uri.toString())
        }
    }
    val openGallery = { gallery.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)) }

    Text(
        text = stringResource(R.string.bagikan_invite_hint),
        fontSize = 12.sp,
        lineHeight = 17.sp,
        color = KeraBagikanText
    )

    val frame = Modifier
        .fillMaxWidth()
        .aspectRatio(3f / 4f)

    if (photo == null) {
        KeraInAppCamera(
            filePrefix = "bukti_ajak",
            onPhotoSaved = { file -> onPhotoChange(Uri.fromFile(file).toString()) },
            modifier = frame
        )
        SourceButton(
            icon = R.drawable.ic_photo_library,
            label = stringResource(R.string.bagikan_from_gallery),
            onClick = openGallery,
            modifier = Modifier.align(Alignment.CenterHorizontally)
        )
    } else {
        val bitmap = rememberThumbnail(photo, maxSide = 1080)
        Box(
            modifier = frame
                .clip(RoundedCornerShape(18.dp))
                .background(KeraBagikanRow)
        ) {
            if (bitmap != null) {
                Image(
                    bitmap = bitmap,
                    contentDescription = stringResource(R.string.bagikan_invite_photo_desc),
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop,
                    filterQuality = FilterQuality.High
                )
            }
            Row(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 14.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                SourceButton(
                    icon = R.drawable.ic_photo_camera,
                    label = stringResource(R.string.bagikan_retake),
                    onClick = { onPhotoChange(null) }
                )
                SourceButton(
                    icon = R.drawable.ic_photo_library,
                    label = stringResource(R.string.bagikan_from_gallery),
                    onClick = openGallery
                )
            }
        }
    }
}

// ---------------------------------------------------------------------------
// Halaman 2 - Kera Campaign
// ---------------------------------------------------------------------------

@Composable
private fun KeraCampaignPage(onKirimClick: () -> Unit) {
    HariTigaPage {
        HariTigaHeader(
            titleRes = R.drawable.img_kerachallenge_campaign_title,
            mascotRes = R.drawable.img_kerachallenge_campaign_mascot,
            title = stringResource(R.string.campaign_title_desc),
            titleTexts = hariTigaTitleTexts(
                pill = intArrayOf(104, 30, 378, 104),
                days = intArrayOf(248, 358, 428, 418),
                tagline = intArrayOf(92, 442, 600, 568),
                taglineText = stringResource(R.string.campaign_tagline),
                taglineRotation = -2f
            ),
            // Gelembung ucapan maskot
            mascotTexts = listOf(
                DesignText(
                    stringResource(R.string.campaign_bubble), 366, 142, 518, 282,
                    size = 26f, color = Color.White, weight = FontWeight.SemiBold, centered = true,
                    align = TextAlign.Center, lineHeight = 31f, rotation = -8f, italic = true
                )
            )
        )

        HariTigaPanel {
            HariTigaRibbon(text = stringResource(R.string.campaign_collect))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(IntrinsicSize.Max),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                CollectCard(R.drawable.img_kerachallenge_campaign_ic_story, stringResource(R.string.campaign_c1))
                CollectCard(R.drawable.img_kerachallenge_campaign_ic_post, stringResource(R.string.campaign_c2))
                CollectCard(R.drawable.img_kerachallenge_campaign_ic_bukti, stringResource(R.string.campaign_c3))
                CollectCard(R.drawable.img_kerachallenge_campaign_ic_dokumen, stringResource(R.string.campaign_c4))
            }

            // Lebar tiap contoh sebanding rasio gambarnya, jadi tingginya sama
            val examples = listOf(
                R.drawable.img_kerachallenge_campaign_story to stringResource(R.string.campaign_ex1),
                R.drawable.img_kerachallenge_campaign_post to stringResource(R.string.campaign_ex2),
                R.drawable.img_kerachallenge_campaign_bukti to stringResource(R.string.campaign_ex3)
            ).map { (res, label) -> ImageBitmap.imageResource(res) to label }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                examples.forEach { (bitmap, label) ->
                    ExampleCard(bitmap = bitmap, label = label, ratio = bitmap.width.toFloat() / bitmap.height)
                }
            }
        }

        KirimButton(onClick = onKirimClick)
    }
}

@Composable
private fun RowScope.CollectCard(@DrawableRes icon: Int, label: String) {
    Column(
        modifier = Modifier
            .weight(1f)
            .fillMaxHeight()
            .shadow(3.dp, RoundedCornerShape(18.dp))
            .clip(RoundedCornerShape(18.dp))
            .background(KeraBagikanCard)
            .border(2.dp, Color.White, RoundedCornerShape(18.dp))
            .padding(horizontal = 4.dp, vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        DesignImage(res = icon, modifier = Modifier.fillMaxWidth(0.8f))
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = label,
            fontSize = 10.sp,
            lineHeight = 13.sp,
            fontWeight = FontWeight.Medium,
            textAlign = TextAlign.Center,
            color = KeraBagikanText
        )
    }
}

@Composable
private fun RowScope.ExampleCard(bitmap: ImageBitmap, label: String, ratio: Float) {
    Column(
        modifier = Modifier.weight(ratio),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Image(
            bitmap = bitmap,
            contentDescription = label,
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(ratio),
            contentScale = ContentScale.Fit,
            filterQuality = FilterQuality.High
        )
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = label,
            fontSize = 10.sp,
            lineHeight = 12.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            color = Color.White,
            modifier = Modifier
                .fillMaxWidth()
                .clip(PillShape)
                .background(Brush.verticalGradient(listOf(KeraBagikanPurpleLight, KeraBagikanPurple)))
                .padding(horizontal = 6.dp, vertical = 5.dp)
        )
    }
}

@Composable
private fun KirimButton(onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .padding(horizontal = 24.dp)
            .fillMaxWidth()
            .height(64.dp)
            .shadow(8.dp, PillShape)
            .clip(PillShape)
            .background(Brush.verticalGradient(listOf(KeraBagikanPurpleLight, KeraBagikanPurple, KeraBagikanPurpleDark)))
            .border(3.dp, Color.White.copy(alpha = 0.8f), PillShape)
            .clickable(onClickLabel = stringResource(R.string.campaign_send), onClick = onClick),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.AutoMirrored.Filled.Send,
            contentDescription = null,
            tint = Color.White,
            modifier = Modifier.size(26.dp)
        )
        Spacer(modifier = Modifier.width(12.dp))
        Text(
            text = stringResource(R.string.campaign_send),
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )
        Spacer(modifier = Modifier.width(8.dp))
        Icon(
            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
            contentDescription = null,
            tint = Color.White,
            modifier = Modifier.size(22.dp)
        )
    }
}

// ---------------------------------------------------------------------------
// Komponen bersama kedua halaman
// ---------------------------------------------------------------------------

/** Judul (kiri) dan maskot (kanan) dari desain, sejajar di bawah. */
@Composable
private fun HariTigaHeader(
    @DrawableRes titleRes: Int,
    @DrawableRes mascotRes: Int,
    title: String,
    titleTexts: List<DesignText>,
    mascotTexts: List<DesignText> = emptyList()
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp),
        verticalAlignment = Alignment.Bottom
    ) {
        HotspotImage(
            imageRes = titleRes,
            contentDescription = title,
            hotspots = emptyList(),
            modifier = Modifier.weight(0.56f),
            texts = titleTexts
        )
        HotspotImage(
            imageRes = mascotRes,
            contentDescription = null,
            hotspots = emptyList(),
            modifier = Modifier.weight(0.44f),
            texts = mascotTexts
        )
    }
}

/** Pil "Tantangan 3", pil "3 Hari", dan ajakan miring di gambar judul Hari 3 (piksel gambar asli). */
@Composable
private fun hariTigaTitleTexts(
    pill: IntArray,
    days: IntArray,
    tagline: IntArray,
    taglineText: String,
    taglineRotation: Float
) = listOf(
    DesignText(
        stringResource(R.string.kc_tantangan_n, 3), pill[0], pill[1], pill[2], pill[3],
        size = 38f, color = Color.White, centered = true, align = TextAlign.Center, rotation = -5f
    ),
    DesignText(
        stringResource(R.string.kc_days_3), days[0], days[1], days[2], days[3],
        size = 40f, color = KeraBagikanTitleInk, centered = true, align = TextAlign.Center
    ),
    DesignText(
        taglineText, tagline[0], tagline[1], tagline[2], tagline[3],
        size = 27f, color = KeraBagikanTaglineInk, weight = FontWeight.Medium, align = TextAlign.Center,
        lineHeight = 40f, rotation = taglineRotation, italic = true, centered = true
    )
)

/** Panel putih tembus pandang tempat isi tiap bagian. */
@Composable
private fun HariTigaPanel(content: @Composable ColumnScope.() -> Unit) {
    Column(
        modifier = Modifier
            .padding(horizontal = 14.dp)
            .fillMaxWidth()
            .shadow(6.dp, PanelShape, ambientColor = KeraBagikanPurple, spotColor = KeraBagikanPurple)
            .clip(PanelShape)
            .background(KeraBagikanPanel)
            .border(2.dp, Color.White, PanelShape)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        content = content
    )
}

/** Pita ungu judul bagian, sedikit miring seperti di desain. */
@Composable
private fun HariTigaRibbon(text: String, @DrawableRes icon: Int? = null) {
    val shape = RoundedCornerShape(14.dp)

    Row(
        modifier = Modifier
            .graphicsLayer { rotationZ = -2f }
            .shadow(4.dp, shape)
            .clip(shape)
            .background(Brush.verticalGradient(listOf(KeraBagikanPurpleLight, KeraBagikanPurple)))
            .border(2.dp, Color.White.copy(alpha = 0.7f), shape)
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (icon != null) {
            Icon(
                painter = painterResource(icon),
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(10.dp))
        }
        Text(
            text = text,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )
    }
}

/** Gambar desain (nodpi) dengan rasio aslinya, diperkecil dengan filter High supaya tajam. */
@Composable
private fun DesignImage(
    @DrawableRes res: Int,
    modifier: Modifier = Modifier,
    contentDescription: String? = null
) {
    val bitmap = ImageBitmap.imageResource(res)

    Image(
        bitmap = bitmap,
        contentDescription = contentDescription,
        modifier = modifier.aspectRatio(bitmap.width.toFloat() / bitmap.height),
        contentScale = ContentScale.Fit,
        filterQuality = FilterQuality.High
    )
}
