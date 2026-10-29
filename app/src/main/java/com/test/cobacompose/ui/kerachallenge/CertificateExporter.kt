package com.test.cobacompose.ui.kerachallenge

import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Typeface
import android.media.MediaScannerConnection
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import androidx.compose.ui.graphics.toArgb
import androidx.core.content.res.ResourcesCompat
import androidx.core.graphics.createBitmap
import com.test.cobacompose.R
import com.test.cobacompose.ui.theme.KeraCertCardTab
import com.test.cobacompose.ui.theme.KeraCertMaroon
import com.test.cobacompose.ui.theme.KeraCertSubtitle
import com.test.cobacompose.ui.theme.KeraCertTitle
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/** Folder di galeri tempat sertifikat disimpan. */
internal const val CERTIFICATE_FOLDER = "Kerabite"

private const val CERT_WIDTH = 1600
private const val CERT_HEIGHT = 1131

/**
 * Menggambar sertifikat KeraChallenge (landscape, kira-kira rasio A4) sebagai bitmap.
 * Dipanggil di background thread.
 */
internal fun createCertificateBitmap(context: Context, userName: String, score: Int): Bitmap {
    val bitmap = createBitmap(CERT_WIDTH, CERT_HEIGHT)
    val canvas = Canvas(bitmap)
    val w = CERT_WIDTH.toFloat()
    val h = CERT_HEIGHT.toFloat()

    fun font(id: Int): Typeface = ResourcesCompat.getFont(context, id) ?: Typeface.DEFAULT
    fun textPaint(id: Int, size: Float, color: Int) = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        typeface = font(id)
        textSize = size
        this.color = color
        textAlign = Paint.Align.CENTER
    }

    // Latar putih dengan bingkai ganda
    canvas.drawColor(android.graphics.Color.WHITE)
    val framePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { style = Paint.Style.STROKE }
    framePaint.color = KeraCertMaroon.toArgb()
    framePaint.strokeWidth = 22f
    canvas.drawRoundRect(RectF(30f, 30f, w - 30f, h - 30f), 36f, 36f, framePaint)
    framePaint.color = KeraCertCardTab.toArgb()
    framePaint.strokeWidth = 10f
    canvas.drawRoundRect(RectF(62f, 62f, w - 62f, h - 62f), 26f, 26f, framePaint)

    // Logo
    BitmapFactory.decodeResource(context.resources, R.drawable.logo_kerabite_header)?.let { logo ->
        val logoWidth = 420f
        val logoHeight = logoWidth * logo.height / logo.width
        val left = (w - logoWidth) / 2
        canvas.drawBitmap(logo, null, RectF(left, 95f, left + logoWidth, 95f + logoHeight), Paint(Paint.FILTER_BITMAP_FLAG))
        logo.recycle()
    }

    val center = w / 2
    // Teks mengikuti bahasa aplikasi; huruf yang tidak ada di Poppins (Mandarin, Arab) memakai font sistem
    val title = context.getString(R.string.cert_title)
    canvas.drawText(title, center, 395f, textPaint(R.font.poppins_extrabold, 76f, KeraCertMaroon.toArgb()).fitTo(title, w - 200f))
    canvas.drawText(context.getString(R.string.cert_given_to), center, 485f, textPaint(R.font.poppins_medium, 38f, KeraCertSubtitle.toArgb()))

    // Nama, diperkecil kalau terlalu panjang
    val namePaint = textPaint(R.font.poppins_bold, 92f, KeraCertTitle.toArgb())
    while (namePaint.measureText(userName) > w - 360f && namePaint.textSize > 40f) {
        namePaint.textSize -= 4f
    }
    canvas.drawText(userName, center, 615f, namePaint)
    val linePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = KeraCertMaroon.toArgb()
        strokeWidth = 4f
    }
    canvas.drawLine(center - 420f, 650f, center + 420f, 650f, linePaint)

    val bodyPaint = textPaint(R.font.poppins_regular, 36f, KeraCertTitle.toArgb())
    val body1 = context.getString(R.string.cert_body_1)
    val body2 = context.getString(R.string.cert_body_2)
    bodyPaint.fitTo(body1, w - 200f)
    canvas.drawText(body1, center, 730f, bodyPaint)
    canvas.drawText(body2, center, 782f, bodyPaint)

    canvas.drawText(context.getString(R.string.cert_score_n, score), center, 880f, textPaint(R.font.poppins_bold, 48f, KeraCertMaroon.toArgb()))

    val locale = context.resources.configuration.locales[0] ?: Locale.forLanguageTag("id-ID")
    val date = SimpleDateFormat("d MMMM yyyy", locale).format(Date())
    canvas.drawText(context.getString(R.string.cert_place_date, date), center, 985f, textPaint(R.font.poppins_medium, 32f, KeraCertSubtitle.toArgb()))

    return bitmap
}

/** Perkecil ukuran huruf sampai [text] muat di [maxWidth] (untuk terjemahan yang lebih panjang). */
private fun Paint.fitTo(text: String, maxWidth: Float): Paint = apply {
    while (measureText(text) > maxWidth && textSize > 20f) textSize -= 2f
}

/**
 * Menyimpan [bitmap] sebagai PNG ke galeri (Pictures/Kerabite). Android 10+ memakai MediaStore
 * tanpa izin; versi lama butuh izin WRITE_EXTERNAL_STORAGE yang sudah diberikan.
 * Dipanggil di background thread. Mengembalikan true kalau berhasil.
 */
internal fun saveCertificateToGallery(context: Context, bitmap: Bitmap, fileName: String): Boolean =
    runCatching {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val resolver = context.contentResolver
            val values = ContentValues().apply {
                put(MediaStore.Images.Media.DISPLAY_NAME, fileName)
                put(MediaStore.Images.Media.MIME_TYPE, "image/png")
                put(MediaStore.Images.Media.RELATIVE_PATH, "${Environment.DIRECTORY_PICTURES}/$CERTIFICATE_FOLDER")
                put(MediaStore.Images.Media.IS_PENDING, 1)
            }
            val uri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)
                ?: return@runCatching false
            resolver.openOutputStream(uri)?.use { bitmap.compress(Bitmap.CompressFormat.PNG, 100, it) }
                ?: return@runCatching false
            values.clear()
            values.put(MediaStore.Images.Media.IS_PENDING, 0)
            resolver.update(uri, values, null, null)
            true
        } else {
            @Suppress("DEPRECATION")
            val folder = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), CERTIFICATE_FOLDER)
            folder.mkdirs()
            val file = File(folder, fileName)
            FileOutputStream(file).use { bitmap.compress(Bitmap.CompressFormat.PNG, 100, it) }
            MediaScannerConnection.scanFile(context, arrayOf(file.absolutePath), arrayOf("image/png"), null)
            true
        }
    }.getOrDefault(false)
