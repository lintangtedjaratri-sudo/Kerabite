package com.test.cobacompose.ui.home

import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.Text
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.imageResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.test.cobacompose.R
import com.test.cobacompose.data.challenge.ChallengeProgress
import com.test.cobacompose.ui.kerachallenge.DesignText
import com.test.cobacompose.ui.kerachallenge.DesignTextImage
import com.test.cobacompose.ui.kerachallenge.HotspotImage
import com.test.cobacompose.ui.kerachallenge.ImageArea
import com.test.cobacompose.ui.kerachallenge.ImageOverlay
import com.test.cobacompose.ui.kerafyi.LocalFood
import com.test.cobacompose.ui.kerarecommend.KeraRecipe
import com.test.cobacompose.ui.theme.KeraAksiLink
import com.test.cobacompose.ui.theme.KeraAksiPanelBottom
import com.test.cobacompose.ui.theme.KeraAksiPanelTop
import com.test.cobacompose.ui.theme.KeraAksiSubtitle
import com.test.cobacompose.ui.theme.KeraAksiTitle
import com.test.cobacompose.ui.theme.KeraLavender
import com.test.cobacompose.ui.theme.KeraLavenderDeep
import com.test.cobacompose.ui.theme.KeraLavenderLight
import com.test.cobacompose.ui.theme.KeraLocalBiteBarBorder
import com.test.cobacompose.ui.theme.KeraLocalBiteBarFill
import com.test.cobacompose.ui.theme.KeraLocalBiteBarTrack
import com.test.cobacompose.ui.theme.KeraPlum
import com.test.cobacompose.ui.theme.KeraPlumLight
import kotlinx.coroutines.launch

@Composable
fun HomeScreen(
    userName: String,
    challengeProgress: ChallengeProgress,
    profileDrawer: @Composable (closeDrawer: () -> Unit) -> Unit,
    onKeraFyiClick: () -> Unit,
    onKeraAwareClick: () -> Unit,
    onKeraChallengeClick: () -> Unit,
    onBagikanAksiClick: () -> Unit,
    onFoodClick: (LocalFood) -> Unit,
    onRecipeClick: (KeraRecipe) -> Unit,
    onMenuSppgClick: () -> Unit
) {
    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()
    val drawerState = rememberDrawerState(DrawerValue.Closed)

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            // Drawer Profil digambar sampai ke balik status bar
            ModalDrawerSheet(windowInsets = WindowInsets(0)) {
                profileDrawer {
                    coroutineScope.launch {
                        drawerState.close()
                    }
                }
            }
        }
    ) {
        val density = LocalDensity.current
        val statusBarHeight = with(density) {
            WindowInsets.statusBars.getTop(this).toDp()
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.White)
        ) {
            // Background di belakang notification/status bar
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(statusBarHeight)
                    .background(KeraLavenderLight)
                    .align(Alignment.TopCenter)
            )

            LazyColumn(
                state = listState,
                modifier = Modifier
                    .fillMaxSize()
                    .windowInsetsPadding(WindowInsets.systemBars),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item {
                    HomeHeader(
                        onProfileClick = {
                            coroutineScope.launch {
                                drawerState.open()
                            }
                        }
                    )
                }

                item {
                    KeraFyiSection(
                        userName = userName,
                        onKeraFyiClick = onKeraFyiClick,
                        onFoodClick = onFoodClick
                    )
                }

                item {
                    KeraAwareSection(onClick = onKeraAwareClick)
                }

                item {
                    KeraRecommendSection(onRecipeClick = onRecipeClick, onMenuSppgClick = onMenuSppgClick)
                }

                item {
                    AksiHariIniSection(
                        progress = challengeProgress,
                        onChallengeClick = onKeraChallengeClick,
                        onCampaignClick = onBagikanAksiClick
                    )
                }

                item {
                    Spacer(
                        modifier = Modifier.height(16.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun HomeHeader(
    onProfileClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(KeraLavenderLight)
            .padding(
                horizontal = 16.dp,
                vertical = 4.dp
            )
    ) {
        IconButton(
            onClick = onProfileClick,
            modifier = Modifier.align(Alignment.CenterStart)
        ) {
            Icon(
                painter = painterResource(R.drawable.ic_menu),
                contentDescription = stringResource(R.string.home_menu)
            )
        }

        Image(
            painter = painterResource(R.drawable.logo_kerabite_header),
            contentDescription = "KeraBite",
            modifier = Modifier
                .size(100.dp)
                .graphicsLayer {
                    scaleX = 1.8f
                    scaleY = 1.8f
                }
                .align(Alignment.Center),
            contentScale = ContentScale.Fit
        )
    }
}

@Composable
private fun KeraFyiSection(
    userName: String,
    onKeraFyiClick: () -> Unit,
    onFoodClick: (LocalFood) -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp)
    ) {
        // Main card
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp)
                .background(
                    color = KeraLavender,
                    shape = RoundedCornerShape(12.dp)
                )
        ) {
            // Pattern background
            Image(
                painter = painterResource(R.drawable.bg_kerafyi_pattern),
                contentDescription = null,
                modifier = Modifier
                    .matchParentSize()
                    .clip(RoundedCornerShape(12.dp)),
                contentScale = ContentScale.Crop,
                alpha = 0.5f
            )

            // Content
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(
                        start = 16.dp,
                        top = 14.dp,
                        end = 16.dp,
                        bottom = 14.dp
                    )
            ) {
                // Beri ruang untuk mascot di sebelah kanan
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(86.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .align(Alignment.TopStart)
                            .padding(end = 90.dp)
                    ) {
                        Text(
                            text = stringResource(R.string.home_hi, userName),
                            fontSize = 13.sp,
                            color = KeraPlum
                        )

                        Spacer(
                            modifier = Modifier.height(2.dp)
                        )

                        Text(
                            text = stringResource(R.string.home_fyi_title),
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold,
                            lineHeight = 20.sp,
                            color = Color.White
                        )
                    }
                }

                // Tiga card pangan lokal
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    LocalFood.entries.forEach { food ->
                        FoodCard(
                            imageRes = food.imageRes,
                            title = stringResource(food.titleRes),
                            modifier = Modifier.weight(1f),
                            onClick = { onFoodClick(food) }
                        )
                    }
                }
            }
        }

        // Mascot KeraFYI - overlay / keluar dari main card
        Image(
            painter = painterResource(R.drawable.img_mascot_kerafyi),
            contentDescription = "KeraFYI",
            modifier = Modifier
                .size(115.dp)
                .align(Alignment.TopEnd)
                .offset(
                    x = (-2).dp,
                    y = (-8).dp
                )
                .clickable(onClick = onKeraFyiClick),
            contentScale = ContentScale.Fit
        )
    }
}

@Composable
private fun KeraAwareSection(
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp)
            .height(82.dp)
            // Seluruh area kartu (termasuk maskot) bisa diklik
            .clickable(onClick = onClick)
    ) {
        // Main card
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(64.dp)
                .align(Alignment.Center)
                .clip(RoundedCornerShape(12.dp))
                .background(KeraLavender)
        ) {
            // Background ungu gelap + text
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(
                        start = 52.dp,
                        end = 8.dp
                    )
                    .height(48.dp)
                    .align(Alignment.Center)
                    .clip(RoundedCornerShape(10.dp))
                    .background(KeraLavenderDeep)
            ) {
                Text(
                    text = stringResource(R.string.home_aware_title),
                    modifier = Modifier.align(Alignment.Center),
                    color = Color.White,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    lineHeight = 17.sp,
                    textAlign = TextAlign.Center
                )
            }

            // Arrow kanan atas
            Image(
                painter = painterResource(R.drawable.ic_arrow_right),
                contentDescription = stringResource(R.string.home_aware_open),
                modifier = Modifier
                    .size(28.dp)
                    .align(Alignment.TopEnd)
                    .padding(
                        top = 6.dp,
                        end = 6.dp
                    ),
                contentScale = ContentScale.Fit
            )
        }

        // KeraAware mascot
        // Sengaja berada di luar main card
        Image(
            painter = painterResource(R.drawable.img_mascot_keraaware),
            contentDescription = "KeraAware",
            modifier = Modifier
                .size(92.dp)
                .align(Alignment.CenterStart)
                .offset(
                    x = 0.dp,
                    y = (-8).dp
                ),
            contentScale = ContentScale.Fit
        )
    }
}

@Composable
private fun KeraRecommendSection(onRecipeClick: (KeraRecipe) -> Unit, onMenuSppgClick: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(KeraLavender)
    ) {
        // Pattern background
        Image(
            painter = painterResource(R.drawable.bg_kerarecommend_pattern),
            contentDescription = null,
            modifier = Modifier
                .matchParentSize()
                .clip(RoundedCornerShape(12.dp)),
            contentScale = ContentScale.Crop,
            alpha = 0.5f
        )

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    top = 10.dp,
                    bottom = 8.dp
                )
        ) {
            Text(
                text = stringResource(R.string.home_recommend_title),
                modifier = Modifier.padding(
                    start = 12.dp,
                    bottom = 6.dp
                ),
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = KeraPlumLight
            )

            LazyRow(
                modifier = Modifier.fillMaxWidth(),
                contentPadding = PaddingValues(
                    horizontal = 8.dp
                ),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(KeraRecipe.entries) { recipe ->
                    KeraRecommendCard(
                        imageRes = recipe.cardImageRes,
                        recommendIconRes = recipe.badgeRes,
                        title = recipe.cardTitle,
                        onClick = { onRecipeClick(recipe) }
                    )
                }

                item {
                    KeraRecommendCard(
                        imageRes = R.drawable.img_kerarecommend_food_3,
                        recommendIconRes = R.drawable.img_kerarecommend_badge_3,
                        title = "",
                        // Kartu MBG membuka halaman Menu SPPG
                        onClick = onMenuSppgClick
                    )
                }
            }
        }
    }
}

/** Section "Aksi Hari Ini": kartu 9 Days Local Bite (ke KeraChallenge) dan kartu Kera Campaign. */
@Composable
private fun AksiHariIniSection(
    progress: ChallengeProgress,
    onChallengeClick: () -> Unit,
    onCampaignClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(Brush.verticalGradient(listOf(KeraAksiPanelTop, KeraAksiPanelBottom)))
            .border(1.dp, Color.White, RoundedCornerShape(16.dp))
            .padding(12.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = "🔥",
                fontSize = 26.sp
            )

            Spacer(modifier = Modifier.width(8.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = stringResource(R.string.home_action_title),
                    fontSize = 18.sp,
                    lineHeight = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = KeraAksiTitle
                )
                Text(
                    text = stringResource(R.string.home_action_subtitle),
                    fontSize = 12.sp,
                    lineHeight = 16.sp,
                    color = KeraAksiSubtitle
                )
            }

            Text(
                text = stringResource(R.string.home_see_all),
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .clickable(onClick = onChallengeClick)
                    .padding(horizontal = 4.dp, vertical = 2.dp),
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                color = KeraAksiLink
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Dua kartu setinggi sama: lebar mengikuti rasio gambarnya masing-masing
        val campaign = ImageBitmap.imageResource(R.drawable.img_kerachallenge_home_campaign)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            LocalBiteCard(
                progress = progress,
                modifier = Modifier.weight(LOCAL_BITE_WIDTH.toFloat() / LOCAL_BITE_HEIGHT),
                onClick = onChallengeClick
            )
            AksiCard(
                image = campaign,
                description = stringResource(R.string.home_campaign_desc),
                texts = campaignTexts(),
                modifier = Modifier.weight(campaign.width.toFloat() / campaign.height),
                onClick = onCampaignClick
            )
        }
    }
}

@Composable
private fun AksiCard(
    image: ImageBitmap,
    description: String,
    texts: List<DesignText>,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    DesignTextImage(
        bitmap = image,
        contentDescription = description,
        texts = texts,
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .clickable(onClick = onClick)
    )
}

/** Jumlah hari KeraChallenge (3 tantangan x 3 hari). */
private const val TOTAL_CHALLENGE_DAYS = ChallengeProgress.CHALLENGE_COUNT * ChallengeProgress.DAYS_PER_CHALLENGE

/** Ukuran img_kerachallenge_home_localbite dalam piksel. */
private const val LOCAL_BITE_WIDTH = 716
private const val LOCAL_BITE_HEIGHT = 500

/** Bilah progres yang tergambar di kartu 9 Days Local Bite (pecahan lebar/tinggi gambar). */
private val localBiteBarArea = ImageArea(335f / 716, 323f / 500, 656f / 716, 351f / 500)

/** Hari berikutnya yang belum selesai, urut Tantangan 1-3 lalu Hari 1-3; null kalau semua selesai. */
private fun ChallengeProgress.nextDay(): Pair<Int, Int>? =
    (1..ChallengeProgress.CHALLENGE_COUNT).firstNotNullOfOrNull { challenge ->
        (1..ChallengeProgress.DAYS_PER_CHALLENGE).firstOrNull { !isDayDone(challenge, it) }?.let { challenge to it }
    }

private fun ChallengeProgress.completedDays(): Int = (1..ChallengeProgress.CHALLENGE_COUNT).sumOf { stars(it) }

/**
 * Kartu 9 Days Local Bite yang mengikuti progres KeraChallenge: pil jumlah hari selesai, bilah progres,
 * hari berikutnya, dan tombol Ikuti / Lanjutkan / Lihat.
 */
@Composable
private fun LocalBiteCard(
    progress: ChallengeProgress,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val done = progress.completedDays()
    val fraction = done.toFloat() / TOTAL_CHALLENGE_DAYS

    HotspotImage(
        imageRes = R.drawable.img_kerachallenge_home_localbite,
        contentDescription = stringResource(R.string.home_localbite_desc),
        hotspots = emptyList(),
        overlays = listOf(ImageOverlay(localBiteBarArea) { LocalBiteBar(fraction) }),
        texts = localBiteTexts(progress, done),
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .clickable(onClick = onClick)
    )
}

/** Bilah progres hari selesai, menutupi bilah contoh yang tergambar di kartu. */
@Composable
private fun LocalBiteBar(fraction: Float) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .clip(RoundedCornerShape(50))
            .background(KeraLocalBiteBarBorder)
            .padding(1.5.dp)
            .clip(RoundedCornerShape(50))
            .background(KeraLocalBiteBarTrack)
    ) {
        if (fraction > 0f) {
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .fillMaxWidth(fraction.coerceIn(0f, 1f))
                    .clip(RoundedCornerShape(50))
                    .background(KeraLocalBiteBarFill)
            )
        }
    }
}

/*
 * Teks kartu Aksi Hari Ini yang dulu tergambar di img_kerachallenge_home_localbite (716 x 500 px)
 * dan img_kerachallenge_home_campaign (422 x 511 px). Angka = piksel gambar.
 * Judul "9 DAYS LOCAL BITE" dan "Kera CAMPAIGN" tetap bagian dari gambar.
 */
@Composable
private fun localBiteTexts(progress: ChallengeProgress, done: Int): List<DesignText> {
    val next = progress.nextDay()
    val days = if (done == 0) stringResource(R.string.home_localbite_days) else stringResource(R.string.home_localbite_progress, done, TOTAL_CHALLENGE_DAYS)
    val tagline = when {
        done == 0 -> stringResource(R.string.home_localbite_tagline)
        next == null -> stringResource(R.string.home_localbite_done_tagline)
        else -> stringResource(R.string.home_localbite_next, next.first, next.second)
    }
    val cta = when {
        done == 0 -> stringResource(R.string.home_localbite_cta)
        next == null -> stringResource(R.string.home_localbite_done_cta)
        else -> stringResource(R.string.home_localbite_continue)
    }
    return localBiteTexts(days, tagline, cta)
}

private fun localBiteTexts(days: String, tagline: String, cta: String) = listOf(
    // Pil kuning jumlah hari ("1–9 hari" atau "3/9 hari")
    DesignText(
        days, 412, 56, 586, 98,
        size = 32f, color = Color(0xFF2D1A0E), weight = FontWeight.ExtraBold, centered = true, align = TextAlign.Center
    ),
    DesignText(
        tagline, 370, 238, 704, 312,
        size = 26f, color = Color(0xFFFDEEFF), weight = FontWeight.Medium, lineHeight = 36f
    ),
    // Tombol kuning; panah di kanannya tetap bagian dari gambar
    DesignText(
        cta, 300, 412, 590, 460,
        size = 30f, color = Color(0xFF2E1A0A), weight = FontWeight.SemiBold, centered = true, align = TextAlign.Center
    )
)

@Composable
private fun campaignTexts() = listOf(
    DesignText(
        stringResource(R.string.home_campaign_text), 30, 278, 408, 396,
        size = 26f, color = Color(0xFF18293D), weight = FontWeight.Medium, lineHeight = 39f
    ),
    // Tombol tosca; ikon pesawat kertas dan panah tetap bagian dari gambar
    DesignText(
        stringResource(R.string.home_campaign_cta), 116, 434, 328, 486,
        size = 26f, color = Color.White, weight = FontWeight.SemiBold, centered = true, align = TextAlign.Center
    )
)

@Composable
private fun FoodCard(
    @DrawableRes imageRes: Int,
    title: String,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Box(
        modifier = modifier
            .height(78.dp)
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
    ) {
        Image(
            painter = painterResource(imageRes),
            contentDescription = title,
            modifier = Modifier
                .fillMaxSize()
                .scale(1.55f),
            contentScale = ContentScale.Crop
        )

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                .background(
                    Color.Black.copy(alpha = 0.35f)
                )
                .padding(
                    horizontal = 4.dp,
                    vertical = 5.dp
                )
        ) {
            Text(
                text = title,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        }
    }
}

@Composable
private fun KeraRecommendCard(
    @DrawableRes imageRes: Int,
    @DrawableRes recommendIconRes: Int,
    title: String,
    onClick: (() -> Unit)? = null
) {
    Box(
        modifier = Modifier
            .width(115.dp)
            .height(130.dp)
            .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier)
    ) {
        // Food card
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(100.dp)
                .align(Alignment.TopCenter)
                .clip(RoundedCornerShape(14.dp))
        ) {
            Image(
                painter = painterResource(imageRes),
                contentDescription = title,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )

            Text(
                text = title,
                modifier = Modifier
                    .align(Alignment.Center)
                    .padding(horizontal = 8.dp),
                color = Color.White,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
        }

        // KeraRecomend overlay
        Image(
            painter = painterResource(recommendIconRes),
            contentDescription = "KeraRecomend",
            modifier = Modifier
                .width(130.dp)
                .height(58.dp)
                .align(Alignment.BottomCenter)
                .offset(y = 2.dp),
            contentScale = ContentScale.Fit
        )
    }
}

