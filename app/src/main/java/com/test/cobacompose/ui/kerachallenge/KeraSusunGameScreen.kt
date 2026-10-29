package com.test.cobacompose.ui.kerachallenge

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.relocation.BringIntoViewRequester
import androidx.compose.foundation.relocation.bringIntoViewRequester
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.test.cobacompose.R
import com.test.cobacompose.ui.theme.InputError
import com.test.cobacompose.ui.theme.KeraChallengeSkyLight
import com.test.cobacompose.ui.theme.KeraGameActiveWord
import com.test.cobacompose.ui.theme.KeraGameClueCard
import com.test.cobacompose.ui.theme.KeraGamePanel
import com.test.cobacompose.ui.theme.KeraGamePurple
import com.test.cobacompose.ui.theme.KeraGameText
import com.test.cobacompose.ui.theme.KeraGreen
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/** Batas waktu menyusun puzzle. */
private const val PUZZLE_TIME_SECONDS = 240

private fun formatTime(seconds: Int) = "%02d:%02d".format(seconds / 60, seconds % 60)

/** Main Game Hari 1 - Susun Game: puzzle jigsaw 4x4 yang bisa dimainkan, dengan batas waktu. */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun KeraSusunGameScreen(
    onPuzzleCompleted: () -> Unit,
    onNextDayClick: () -> Unit,
    onBackClick: () -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    val puzzleViewRequester = remember { BringIntoViewRequester() }

    var status by rememberSaveable { mutableStateOf(PuzzleStatus.SIAP) }
    var round by rememberSaveable { mutableIntStateOf(0) }
    var remaining by rememberSaveable { mutableIntStateOf(PUZZLE_TIME_SECONDS) }
    // Potongan dibuat ulang tiap ronde, jadi hitungannya juga ikut ronde
    var placedCount by remember(round) { mutableIntStateOf(0) }

    LaunchedEffect(status, round) {
        if (status == PuzzleStatus.MAIN) {
            while (remaining > 0) {
                delay(1000)
                remaining--
            }
            status = PuzzleStatus.WAKTU_HABIS
        }
    }

    fun startGame() {
        round++
        remaining = PUZZLE_TIME_SECONDS
        status = PuzzleStatus.MAIN
        // Geser layar supaya papan dan nampan terlihat
        coroutineScope.launch { puzzleViewRequester.bringIntoView() }
    }

    Box(modifier = Modifier.fillMaxSize()) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(KeraGamePanel)
            .verticalScroll(rememberScrollState())
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(KeraChallengeSkyLight)
                .statusBarsPadding()
        )

        HotspotImage(
            imageRes = R.drawable.img_kerachallenge_maingame_susun_header,
            contentDescription = stringResource(R.string.susun_description),
            hotspots = emptyList(),
            texts = susunHeaderTexts()
        )

        Column(
            modifier = Modifier.padding(horizontal = 12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            PetunjukCard()

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .bringIntoViewRequester(puzzleViewRequester)
                    .clip(RoundedCornerShape(24.dp))
                    .background(Color.White)
                    .padding(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    SectionPill(text = stringResource(R.string.susun_pill))
                    Spacer(modifier = Modifier.weight(1f))
                    InfoChip(text = "$placedCount/$PUZZLE_PIECE_COUNT")
                    InfoChip(
                        text = "⏱ ${formatTime(remaining)}",
                        textColor = if (status == PuzzleStatus.MAIN && remaining <= 30) InputError else KeraGameText
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                JigsawPuzzle(
                    imageRes = R.drawable.img_kerachallenge_maingame_puzzle,
                    status = status,
                    round = round,
                    onPiecePlaced = { count ->
                        placedCount = count
                        if (count == PUZZLE_PIECE_COUNT) {
                            status = PuzzleStatus.MENANG
                            // Hari 1 Tantangan 3 selesai (+1 bintang)
                            onPuzzleCompleted()
                        }
                    }
                )
            }

            val message = when (status) {
                PuzzleStatus.SIAP -> null
                PuzzleStatus.MAIN -> stringResource(R.string.susun_status_main)
                PuzzleStatus.MENANG ->
                    stringResource(R.string.susun_status_won, formatTime(PUZZLE_TIME_SECONDS - remaining))
                PuzzleStatus.WAKTU_HABIS ->
                    stringResource(R.string.susun_status_timeout, placedCount, PUZZLE_PIECE_COUNT)
            }
            if (message != null) {
                Text(
                    text = message,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center,
                    fontSize = 14.sp,
                    lineHeight = 19.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = when (status) {
                        PuzzleStatus.MENANG -> KeraGreen
                        PuzzleStatus.WAKTU_HABIS -> InputError
                        else -> KeraGameText
                    }
                )
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
                    .padding(bottom = 20.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                when (status) {
                    PuzzleStatus.SIAP -> GameButton(
                        text = stringResource(R.string.susun_start),
                        icon = Icons.Default.PlayArrow,
                        containerColor = KeraGamePurple,
                        contentColor = Color.White,
                        modifier = Modifier.weight(1f),
                        onClick = ::startGame
                    )

                    PuzzleStatus.MAIN -> GameButton(
                        text = stringResource(R.string.susun_shuffle),
                        icon = Icons.Default.Refresh,
                        containerColor = Color.White,
                        contentColor = KeraGamePurple,
                        modifier = Modifier.weight(1f),
                        onClick = ::startGame
                    )

                    PuzzleStatus.MENANG -> {
                        GameButton(
                            text = stringResource(R.string.susun_play_again),
                            icon = Icons.Default.Refresh,
                            containerColor = Color.White,
                            contentColor = KeraGamePurple,
                            modifier = Modifier.weight(1f),
                            onClick = ::startGame
                        )
                        GameButton(
                            text = stringResource(R.string.susun_next_day),
                            icon = Icons.AutoMirrored.Filled.ArrowForward,
                            containerColor = KeraGamePurple,
                            contentColor = Color.White,
                            modifier = Modifier.weight(1.2f),
                            onClick = onNextDayClick
                        )
                    }

                    PuzzleStatus.WAKTU_HABIS -> GameButton(
                        text = stringResource(R.string.susun_try_again),
                        icon = Icons.Default.Refresh,
                        containerColor = KeraGamePurple,
                        contentColor = Color.White,
                        modifier = Modifier.weight(1f),
                        onClick = ::startGame
                    )
                }
            }
        }
    }

        // Ukuran dan posisi sama dengan tombol kembali di semua halaman KeraChallenge
        KeraChallengeBackButton(onClick = onBackClick, style = KeraBackButtonStyle.PURPLE)
        GameMusicButton(modifier = Modifier.align(Alignment.TopEnd))
    }
}

@Composable
private fun PetunjukCard() {
    val steps = listOf(
        stringResource(R.string.susun_tip_1),
        stringResource(R.string.susun_tip_2, PUZZLE_TIME_SECONDS / 60),
        stringResource(R.string.susun_tip_3)
    )

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(KeraGameClueCard)
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            SectionPill(text = stringResource(R.string.kc_tips), icon = Icons.Default.Info)
            Spacer(modifier = Modifier.weight(1f))
            Text(
                text = stringResource(R.string.susun_tips_title),
                fontSize = 13.sp,
                fontStyle = FontStyle.Italic,
                fontWeight = FontWeight.SemiBold,
                color = KeraGamePurple
            )
        }

        steps.forEachIndexed { index, step ->
            Row(verticalAlignment = Alignment.Top) {
                Box(
                    modifier = Modifier
                        .size(24.dp)
                        .clip(CircleShape)
                        .background(KeraGamePurple),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "${index + 1}",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = step,
                    fontSize = 13.sp,
                    lineHeight = 18.sp,
                    color = KeraGameText
                )
            }
        }
    }
}

@Composable
private fun InfoChip(
    text: String,
    textColor: Color = KeraGameText
) {
    Text(
        text = text,
        modifier = Modifier
            .clip(RoundedCornerShape(50))
            .background(KeraGameActiveWord)
            .padding(horizontal = 10.dp, vertical = 5.dp),
        fontSize = 13.sp,
        fontWeight = FontWeight.Bold,
        color = textColor
    )
}
