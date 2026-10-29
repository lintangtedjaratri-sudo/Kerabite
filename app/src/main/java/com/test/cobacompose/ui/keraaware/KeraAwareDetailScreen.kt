package com.test.cobacompose.ui.keraaware

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.test.cobacompose.R
import com.test.cobacompose.ui.components.pinchToZoom
import com.test.cobacompose.ui.components.rememberPinchZoomState
import com.test.cobacompose.ui.theme.KeraAwareTitlePanel

/** Detail KeraAware: foto dengan judul di panel gelap, lalu infografis manfaat. Bisa di-zoom dengan cubit. */
@Composable
fun KeraAwareDetailScreen(
    topic: KeraAwareTopic,
    onBackClick: () -> Unit
) {
    val zoomState = rememberPinchZoomState()
    val foodName = stringResource(topic.foodNameRes)
    val detailTitle = stringResource(topic.detailTitleRes)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .pinchToZoom(zoomState)
                .verticalScroll(rememberScrollState())
                .navigationBarsPadding()
                .padding(bottom = 24.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1.2f)
            ) {
                Image(
                    painter = painterResource(topic.heroRes),
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )

                Column(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .fillMaxWidth()
                        .background(KeraAwareTitlePanel)
                        .padding(horizontal = 16.dp, vertical = 14.dp)
                ) {
                    Text(
                        text = buildAnnotatedString {
                            withStyle(SpanStyle(fontWeight = FontWeight.Bold)) { append(foodName) }
                            append(" $detailTitle")
                        },
                        fontSize = 25.sp,
                        lineHeight = 32.sp,
                        color = Color.White
                    )
                    topic.detailSubtitleRes?.let { stringResource(it) }?.let {
                        Text(
                            text = it,
                            fontSize = 14.sp,
                            lineHeight = 20.sp,
                            color = Color.White
                        )
                    }
                }
            }

            Image(
                painter = painterResource(topic.infographicRes),
                contentDescription = "$foodName $detailTitle",
                modifier = Modifier
                    .padding(horizontal = 6.dp, vertical = 12.dp)
                    .fillMaxWidth(),
                contentScale = ContentScale.FillWidth
            )
        }

        // Tombol kembali di kiri atas, di luar area zoom supaya selalu terlihat
        Box(
            modifier = Modifier
                .statusBarsPadding()
                .padding(start = 16.dp, top = 12.dp)
                .size(44.dp)
                .clip(CircleShape)
                .background(Color.White.copy(alpha = 0.85f))
                .clickable(onClick = onBackClick),
            contentAlignment = Alignment.Center
        ) {
            Image(
                painter = painterResource(R.drawable.ic_arrow_back),
                contentDescription = stringResource(R.string.common_back),
                modifier = Modifier.size(26.dp)
            )
        }
    }
}
