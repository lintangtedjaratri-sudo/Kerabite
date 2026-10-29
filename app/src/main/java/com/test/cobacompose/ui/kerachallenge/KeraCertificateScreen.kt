package com.test.cobacompose.ui.kerachallenge

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Paint
import android.os.Build
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.FilterQuality
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalResources
import androidx.compose.ui.res.imageResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import com.test.cobacompose.R
import com.test.cobacompose.ui.theme.KeraCertCardShadow
import com.test.cobacompose.ui.theme.KeraCertCardTab
import com.test.cobacompose.ui.theme.KeraCertMaroon
import com.test.cobacompose.ui.theme.KeraCertScore
import com.test.cobacompose.ui.theme.KeraCertScoreLabel
import com.test.cobacompose.ui.theme.KeraCertSubtitle
import com.test.cobacompose.ui.theme.KeraCertTitle
import com.test.cobacompose.ui.theme.KeraCertYellow
import com.test.cobacompose.ui.theme.KeraCertYellowDark
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/** Jumlah hari KeraChallenge yang sudah diselesaikan (3 tantangan x 3 hari). */
private const val COMPLETED_DAYS = 9

/** Skor penghargaan; belum ada sistem poin, jadi memakai nilai dari desain. */
private const val CERTIFICATE_SCORE = 1000

/**
 * Halaman penghargaan setelah Tantangan 3 Hari 2 (teka-teki silang) selesai.
 * Tombol SERTIFIKAT membuat sertifikat bernama [userName] dan menyimpannya ke galeri.
 */
@Composable
fun KeraCertificateScreen(
    userName: String,
    onBackClick: () -> Unit
) {
    val context = LocalContext.current
    val resources = LocalResources.current
    val coroutineScope = rememberCoroutineScope()
    var saving by remember { mutableStateOf(false) }

    fun downloadCertificate() {
        if (saving) return
        saving = true
        coroutineScope.launch {
            val saved = withContext(Dispatchers.IO) {
                val bitmap = createCertificateBitmap(context, userName.ifBlank { resources.getString(R.string.cert_default_name) }, CERTIFICATE_SCORE)
                saveCertificateToGallery(context, bitmap, "sertifikat_kerachallenge_${System.currentTimeMillis()}.png")
                    .also { bitmap.recycle() }
            }
            saving = false
            Toast.makeText(
                context,
                if (saved) resources.getString(R.string.cert_saved, CERTIFICATE_FOLDER) else resources.getString(R.string.cert_save_failed),
                Toast.LENGTH_LONG
            ).show()
        }
    }

    // Android 9 ke bawah butuh izin tulis penyimpanan untuk menyimpan ke galeri
    val permissionLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        if (granted) {
            downloadCertificate()
        } else {
            Toast.makeText(context, resources.getString(R.string.cert_permission), Toast.LENGTH_LONG).show()
        }
    }

    fun onCertificateClick() {
        val needsPermission = Build.VERSION.SDK_INT < Build.VERSION_CODES.Q &&
            ContextCompat.checkSelfPermission(context, Manifest.permission.WRITE_EXTERNAL_STORAGE) !=
            PackageManager.PERMISSION_GRANTED
        if (needsPermission) {
            permissionLauncher.launch(Manifest.permission.WRITE_EXTERNAL_STORAGE)
        } else {
            downloadCertificate()
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Image(
            bitmap = ImageBitmap.imageResource(R.drawable.bg_kerachallenge_certificate),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop,
            filterQuality = FilterQuality.High
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .statusBarsPadding()
                .navigationBarsPadding()
                .padding(horizontal = 20.dp, vertical = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(48.dp))
            StarsRow()
            AwardCard()
            Spacer(modifier = Modifier.height(24.dp))
            CertificateRibbon(saving = saving, onClick = ::onCertificateClick)
            Spacer(modifier = Modifier.height(16.dp))
        }

        KeraChallengeBackButton(onClick = onBackClick, style = KeraBackButtonStyle.PURPLE)
    }
}

@Composable
private fun StarsRow() {
    val star = ImageBitmap.imageResource(R.drawable.img_kerachallenge_star)

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.Top
    ) {
        listOf(92.dp to 30.dp, 124.dp to 0.dp, 92.dp to 30.dp).forEach { (size, drop) ->
            Image(
                bitmap = star,
                contentDescription = null,
                modifier = Modifier
                    .padding(top = drop)
                    .size(size),
                filterQuality = FilterQuality.High
            )
        }
    }
}

/** Kartu putih berbayang ungu dengan lencana "9", judul melengkung, logo dan skor. */
@Composable
private fun AwardCard() {
    val badgeSize = 112.dp
    val cardShape = RoundedCornerShape(22.dp)

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = badgeSize / 2 - 8.dp)
    ) {
        // Tab lavender di belakang bagian atas kartu
        Box(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .offset(y = (-26).dp)
                .fillMaxWidth(0.84f)
                .height(80.dp)
                .clip(RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp))
                .background(KeraCertCardTab)
        )

        // Bayangan ungu di kanan bawah + kartu putih
        Box(modifier = Modifier.padding(end = 8.dp, bottom = 8.dp)) {
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .offset(x = 8.dp, y = 8.dp)
                    .clip(cardShape)
                    .background(KeraCertCardShadow)
            )

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(cardShape)
                    .background(Color.White)
                    .padding(start = 16.dp, end = 16.dp, top = badgeSize / 2 + 14.dp, bottom = 22.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                ArcText(text = stringResource(R.string.cert_wow), color = KeraCertTitle, fontSize = 21.sp, fontRes = R.font.poppins_extrabold, height = 46.dp)
                ArcText(text = stringResource(R.string.cert_take), color = KeraCertSubtitle, fontSize = 16.sp, fontRes = R.font.poppins_bold, height = 34.dp)

                Image(
                    painter = painterResource(R.drawable.logo_kerabite_header),
                    contentDescription = "Kerabite",
                    modifier = Modifier
                        .padding(top = 6.dp)
                        .fillMaxWidth(0.72f),
                    contentScale = ContentScale.FillWidth
                )

                ScoreFrame(score = CERTIFICATE_SCORE)
            }
        }

        // Lencana jumlah hari, separuh di atas kartu
        Box(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .offset(y = -(badgeSize / 2))
                .size(badgeSize)
                .clip(CircleShape)
                .background(Color.White)
                .padding(6.dp)
                .clip(CircleShape)
                .background(KeraCertMaroon),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "$COMPLETED_DAYS",
                style = TextStyle(
                    fontSize = 62.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color.White,
                    shadow = Shadow(KeraCertSubtitle, offset = androidx.compose.ui.geometry.Offset(6f, 6f))
                )
            )
        }
    }
}

/** Teks melengkung ke atas seperti di desain, digambar di sepanjang kurva. */
@Composable
private fun ArcText(
    text: String,
    color: Color,
    fontSize: TextUnit,
    fontRes: Int,
    height: Dp
) {
    val context = LocalContext.current
    val typeface = remember(fontRes) { ResourcesCompat.getFont(context, fontRes) }

    Canvas(
        modifier = Modifier
            .fillMaxWidth()
            .height(height)
    ) {
        val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            this.typeface = typeface
            textSize = fontSize.toPx()
            this.color = color.toArgb()
            textAlign = Paint.Align.CENTER
        }
        val curve = android.graphics.Path().apply {
            moveTo(0f, size.height)
            quadTo(size.width / 2, size.height * 0.05f - paint.textSize * 0.4f, size.width, size.height)
        }
        drawIntoCanvas { it.nativeCanvas.drawTextOnPath(text, curve, 0f, -paint.descent(), paint) }
    }
}

@Composable
private fun ScoreFrame(score: Int) {
    val frame = ImageBitmap.imageResource(R.drawable.img_kerachallenge_score_frame)

    Box(
        modifier = Modifier
            .padding(top = 8.dp)
            .fillMaxWidth()
            .aspectRatio(frame.width.toFloat() / frame.height)
    ) {
        Image(
            bitmap = frame,
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            filterQuality = FilterQuality.High
        )

        Column(
            modifier = Modifier.align(Alignment.Center),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = stringResource(R.string.cert_score_label),
                fontSize = 22.sp,
                lineHeight = 24.sp,
                fontWeight = FontWeight.ExtraBold,
                color = KeraCertScoreLabel
            )
            Text(
                text = "$score",
                fontSize = 44.sp,
                lineHeight = 46.sp,
                fontWeight = FontWeight.ExtraBold,
                color = KeraCertScore
            )
        }

        Image(
            bitmap = ImageBitmap.imageResource(R.drawable.img_kerachallenge_trophy),
            contentDescription = null,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .offset(x = 10.dp, y = 14.dp)
                .size(64.dp),
            filterQuality = FilterQuality.High
        )
    }
}

/** Pita putih berbingkai marun dengan ujung bertakik, berisi tombol SERTIFIKAT. */
@Composable
private fun CertificateRibbon(
    saving: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(150.dp),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val notch = size.width * 0.09f
            val ribbon = Path().apply {
                moveTo(0f, 0f)
                lineTo(size.width, 0f)
                lineTo(size.width - notch, size.height / 2)
                lineTo(size.width, size.height)
                lineTo(0f, size.height)
                lineTo(notch, size.height / 2)
                close()
            }
            translate(left = 7.dp.toPx(), top = 7.dp.toPx()) {
                drawPath(ribbon, KeraCertMaroon.copy(alpha = 0.35f))
            }
            drawPath(ribbon, Color.White)
            drawPath(ribbon, KeraCertMaroon, style = Stroke(width = 5.dp.toPx()))
        }

        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = stringResource(R.string.cert_download_hint),
                fontSize = 18.sp,
                fontWeight = FontWeight.Medium,
                color = KeraCertTitle
            )
            Spacer(modifier = Modifier.height(8.dp))
            Box(
                modifier = Modifier
                    .width(220.dp)
                    .height(56.dp)
                    .clip(RoundedCornerShape(18.dp))
                    .background(Brush.verticalGradient(listOf(KeraCertYellow, KeraCertYellow, KeraCertYellowDark)))
                    .border(2.dp, KeraCertYellowDark, RoundedCornerShape(18.dp))
                    .clickable(enabled = !saving, onClickLabel = stringResource(R.string.cert_download_label), onClick = onClick),
                contentAlignment = Alignment.Center
            ) {
                if (saving) {
                    CircularProgressIndicator(color = Color.White, strokeWidth = 3.dp, modifier = Modifier.size(28.dp))
                } else {
                    Text(
                        text = stringResource(R.string.cert_button),
                        style = TextStyle(
                            fontSize = 26.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color.White,
                            letterSpacing = 1.sp,
                            shadow = Shadow(KeraCertYellowDark, offset = androidx.compose.ui.geometry.Offset(3f, 4f))
                        )
                    )
                }
            }
        }
    }
}
