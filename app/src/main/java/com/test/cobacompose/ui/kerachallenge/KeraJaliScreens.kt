package com.test.cobacompose.ui.kerachallenge

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.test.cobacompose.R
import com.test.cobacompose.ui.kerafyi.LocalFood
import com.test.cobacompose.ui.theme.KeraJaliButton
import com.test.cobacompose.ui.theme.KeraJaliButtonDot
import com.test.cobacompose.ui.theme.KeraJaliHint
import com.test.cobacompose.ui.theme.KeraJaliInk
import com.test.cobacompose.ui.theme.KeraJaliPaperInk

/** Kolom halaman tugas Jali: maskot di atas (di belakang status bar), isi, lalu ruang untuk navigasi. */
@Composable
private fun JaliTaskPage(
    onBackClick: () -> Unit,
    content: @Composable () -> Unit
) {
    Box(modifier = Modifier.fillMaxSize()) {
        TaskLandscapeBackground()

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .imePadding()
                .navigationBarsPadding()
                .padding(bottom = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            TaskImage(res = R.drawable.img_kerachallenge_jali_mascots, modifier = Modifier.fillMaxWidth())
            content()
        }

        KeraChallengeBackButton(onClick = onBackClick, style = KeraBackButtonStyle.PINK)
    }
}

/**
 * Jali Hari 2 - Temukan & Pilih (desain 41): peta harta karun, lalu "Mari Kita Mulai!"
 * membuka pilihan pangan dan langsung ke Map KeraFYI-nya. Hari selesai saat Map dibuka.
 */
@Composable
fun KeraJaliMapScreen(
    onFoodSelected: (LocalFood) -> Unit,
    onBackClick: () -> Unit
) {
    var picking by rememberSaveable { mutableStateOf(false) }

    JaliTaskPage(onBackClick = onBackClick) {
        TaskImage(
            res = R.drawable.img_kerachallenge_jali_treasure_map,
            contentDescription = stringResource(R.string.jali_map_title),
            modifier = Modifier
                .padding(top = 16.dp)
                .fillMaxWidth(0.88f)
        )

        Text(
            text = stringResource(R.string.jali_map_title),
            modifier = Modifier
                .padding(horizontal = 20.dp, vertical = 24.dp)
                .clip(RoundedCornerShape(18.dp))
                .background(Color.White.copy(alpha = 0.7f))
                .padding(horizontal = 14.dp, vertical = 8.dp),
            fontSize = 25.sp,
            lineHeight = 34.sp,
            fontWeight = FontWeight.Bold,
            color = KeraJaliInk,
            textAlign = TextAlign.Center
        )

        LetsStartButton(
            text = stringResource(R.string.kc_lets_start),
            color = KeraJaliButton,
            dotColor = KeraJaliButtonDot,
            onClick = { picking = true },
            modifier = Modifier.padding(horizontal = 36.dp)
        )
    }

    if (picking) {
        FoodPickerDialog(
            onDismiss = { picking = false },
            onPick = { food ->
                picking = false
                onFoodSelected(food)
            }
        )
    }
}

@Composable
private fun FoodPickerDialog(
    onDismiss: () -> Unit,
    onPick: (LocalFood) -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .clip(RoundedCornerShape(28.dp))
                .background(Color.White)
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(
                text = stringResource(R.string.jali_map_pick),
                fontSize = 18.sp,
                lineHeight = 24.sp,
                fontWeight = FontWeight.Bold,
                color = KeraJaliInk
            )
            LocalFood.entries.forEach { food ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(50))
                        .background(KeraJaliButtonDot.copy(alpha = 0.25f))
                        .clickable { onPick(food) }
                        .padding(start = 18.dp, end = 8.dp, top = 12.dp, bottom = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        painter = painterResource(R.drawable.ic_kerafyi_pin_purple),
                        contentDescription = null,
                        tint = Color.Unspecified,
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = stringResource(food.titleRes),
                        modifier = Modifier.weight(1f),
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = KeraJaliInk
                    )
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                        contentDescription = null,
                        tint = KeraJaliButton
                    )
                }
            }
        }
    }
}

/**
 * Jali Hari 3 - Tulis & Refleksi (desain 42): jurnal ditulis langsung di kertas buku.
 * Draf disimpan setiap berubah; "yuk kumpulkan karyamu" menyelesaikan hari ini.
 */
@Composable
fun KeraJaliJournalScreen(
    initialText: String,
    onTextChange: (String) -> Unit,
    onSubmit: (String) -> Unit,
    onBackClick: () -> Unit
) {
    var text by rememberSaveable { mutableStateOf(initialText) }

    // Jurnal dari Firestore (mis. ditulis di HP lain) bisa datang setelah layar terbuka
    LaunchedEffect(initialText) {
        if (text.isEmpty() && initialText.isNotEmpty()) text = initialText
    }

    JaliTaskPage(onBackClick = onBackClick) {
        Box(
            modifier = Modifier
                .padding(top = 8.dp)
                .fillMaxWidth(0.92f)
                .graphicsLayer { rotationZ = -1.2f }
        ) {
            TaskImage(res = R.drawable.img_kerachallenge_jali_notebook, modifier = Modifier.fillMaxWidth())

            Column(
                modifier = Modifier
                    .matchParentSize()
                    // Kiri = lebar ring spiral di gambar
                    .padding(start = 44.dp, end = 16.dp, top = 22.dp, bottom = 16.dp)
            ) {
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    TaskImage(res = R.drawable.img_kerachallenge_jali_cloud, modifier = Modifier.fillMaxWidth(0.95f))
                    Text(
                        text = stringResource(R.string.jali_journal_label),
                        modifier = Modifier.padding(horizontal = 28.dp),
                        fontSize = 26.sp,
                        lineHeight = 30.sp,
                        color = Color.White,
                        textAlign = TextAlign.Center,
                        maxLines = 2
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                val textStyle = MaterialTheme.typography.bodyLarge.copy(
                    fontSize = 16.sp,
                    lineHeight = 26.sp,
                    color = KeraJaliPaperInk
                )
                BasicTextField(
                    value = text,
                    onValueChange = {
                        text = it
                        onTextChange(it)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .padding(horizontal = 8.dp),
                    textStyle = textStyle,
                    cursorBrush = SolidColor(KeraJaliButton),
                    keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences),
                    decorationBox = { inner ->
                        Box {
                            if (text.isEmpty()) {
                                Text(text = stringResource(R.string.jali_journal_hint), style = textStyle.copy(color = KeraJaliHint))
                            }
                            inner()
                        }
                    }
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        LetsStartButton(
            text = stringResource(R.string.jali_journal_submit),
            color = KeraJaliButton,
            dotColor = KeraJaliButtonDot,
            onClick = { onSubmit(text.trim()) },
            modifier = Modifier.padding(horizontal = 36.dp)
        )
    }
}
