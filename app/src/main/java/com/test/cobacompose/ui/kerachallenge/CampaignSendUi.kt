package com.test.cobacompose.ui.kerachallenge

import android.content.Context
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalResources
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.test.cobacompose.R
import com.test.cobacompose.data.challenge.CampaignSubmissionRepository
import com.test.cobacompose.data.challenge.SubmitResult

/**
 * Lapisan selama dokumentasi dikirim ke Kera Campaign: menahan sentuhan dan tombol kembali
 * (keluar dari halaman membatalkan pengiriman) dan menampilkan progres unggahan.
 */
@Composable
internal fun CampaignSendingOverlay(progress: Float, accent: Color) {
    val context = LocalContext.current
    val resources = LocalResources.current
    BackHandler {
        Toast.makeText(context, resources.getString(R.string.campaign_sending_wait), Toast.LENGTH_SHORT).show()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.45f))
            .pointerInput(Unit) {},
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .padding(horizontal = 40.dp)
                .fillMaxWidth()
                .clip(RoundedCornerShape(24.dp))
                .background(Color.White)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            CircularProgressIndicator(color = accent, modifier = Modifier.size(40.dp))
            Text(
                text = stringResource(R.string.campaign_sending, (progress * 100).toInt()),
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold,
                textAlign = TextAlign.Center
            )
            LinearProgressIndicator(
                progress = { progress },
                color = accent,
                trackColor = accent.copy(alpha = 0.2f),
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

/** Toast kiriman berhasil, digabung dengan pesan hari selesai (seperti [showDayToast]). */
internal fun showCampaignSentToast(context: Context, day: Int, newlyCompleted: Boolean, stars: Int) {
    val dayMessage = if (newlyCompleted) {
        context.getString(R.string.kc_toast_day_done, day, stars)
    } else {
        context.getString(R.string.kc_toast_day_already, day)
    }
    Toast.makeText(context, context.getString(R.string.campaign_sent) + "\n" + dayMessage, Toast.LENGTH_LONG).show()
}

/** Toast alasan kiriman gagal. */
internal fun showCampaignErrorToast(context: Context, result: SubmitResult) {
    val message = when (result) {
        SubmitResult.Success -> return
        SubmitResult.NotLoggedIn -> context.getString(R.string.campaign_err_login)
        SubmitResult.NoConnection -> context.getString(R.string.campaign_err_offline)
        SubmitResult.TooManyFiles -> context.getString(R.string.campaign_err_too_many, CampaignSubmissionRepository.MAX_FILES)
        is SubmitResult.FileTooLarge -> context.getString(R.string.campaign_err_too_large, result.name)
        is SubmitResult.Unreadable -> context.getString(R.string.campaign_err_unreadable, result.name)
        SubmitResult.Failed -> context.getString(R.string.campaign_err_failed)
    }
    Toast.makeText(context, message, Toast.LENGTH_LONG).show()
}
