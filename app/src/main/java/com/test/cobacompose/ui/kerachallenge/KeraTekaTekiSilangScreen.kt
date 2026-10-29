package com.test.cobacompose.ui.kerachallenge

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
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
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.test.cobacompose.R
import com.test.cobacompose.ui.theme.InputError
import com.test.cobacompose.ui.theme.KeraChallengeSkyLight
import com.test.cobacompose.ui.theme.KeraGameActiveWord
import com.test.cobacompose.ui.theme.KeraGameClueCard
import com.test.cobacompose.ui.theme.KeraGameCorrect
import com.test.cobacompose.ui.theme.KeraGameGrid
import com.test.cobacompose.ui.theme.KeraGameLocked
import com.test.cobacompose.ui.theme.KeraGamePanel
import com.test.cobacompose.ui.theme.KeraGamePurple
import com.test.cobacompose.ui.theme.KeraGameText
import com.test.cobacompose.ui.theme.KeraGameWrong
import com.test.cobacompose.ui.theme.KeraGreen
import kotlinx.coroutines.delay

/** Isian disimpan sebagai satu String (satu karakter per kotak, spasi = kosong) supaya bisa di-save. */
private val EMPTY_ANSWERS = " ".repeat(TekaTekiSilang.ROWS * TekaTekiSilang.COLS)

private fun TtsCell.index() = row * TekaTekiSilang.COLS + col

private fun String.letterAt(cell: TtsCell): Char? = this[cell.index()].takeIf { it != ' ' }

private fun String.withLetter(cell: TtsCell, letter: Char?): String =
    replaceRange(cell.index(), cell.index() + 1, (letter ?: ' ').toString())

private fun TtsDirection.other() =
    if (this == TtsDirection.MENDATAR) TtsDirection.MENURUN else TtsDirection.MENDATAR

/**
 * Main Game Hari 2: teka-teki silang yang bisa diisi, dicek, dan di-reset.
 * Kalau semua jawaban benar, [onCompleted] dipanggil (membuka halaman penghargaan).
 */
@Composable
fun KeraTekaTekiSilangScreen(
    onCompleted: () -> Unit,
    onBackClick: () -> Unit
) {
    val focusManager = LocalFocusManager.current
    val solution = TekaTekiSilang.solution

    var answers by rememberSaveable { mutableStateOf(EMPTY_ANSWERS) }
    var direction by rememberSaveable { mutableStateOf(TtsDirection.MENDATAR) }
    var checked by rememberSaveable { mutableStateOf(false) }
    var activeCell by remember { mutableStateOf<TtsCell?>(null) }

    val focusRequesters = remember { solution.keys.associateWith { FocusRequester() } }

    val activeWord = activeCell?.let { TekaTekiSilang.wordAt(it, direction) }
    val allFilled = solution.keys.all { answers.letterAt(it) != null }
    val wrongCount = solution.count { (cell, letter) -> answers.letterAt(cell) != letter }

    // Validasi per jawaban: kata yang sudah lengkap langsung dicek benar/salah
    val wordStatuses = TekaTekiSilang.words.associateWith { word ->
        TekaTekiSilang.wordStatus(word, word.cells.map { answers.letterAt(it) })
    }
    val correctCells = wordStatuses.filterValues { it == TtsWordStatus.BENAR }.keys.flatMap { it.cells }.toSet()
    val wrongWords = wordStatuses.filterValues { it == TtsWordStatus.SALAH }.keys
        .sortedBy { it.number }

    // Beri waktu melihat semua kotak hijau sebelum pindah ke halaman penghargaan
    LaunchedEffect(checked, wrongCount) {
        if (checked && wrongCount == 0) {
            delay(1200)
            onCompleted()
        }
    }

    // Kotak berikut/sebelumnya di kata yang sedang aktif
    fun neighbour(cell: TtsCell, step: Int): TtsCell? {
        val word = TekaTekiSilang.wordAt(cell, direction)
            ?: TekaTekiSilang.wordAt(cell, direction.other())
            ?: return null
        return word.cells.getOrNull(word.cells.indexOf(cell) + step)
    }

    fun focusWord(word: TtsWord) {
        direction = word.direction
        val target = word.cells.firstOrNull { answers.letterAt(it) == null } ?: word.cells.first()
        focusRequesters[target]?.requestFocus()
    }

    Box(modifier = Modifier.fillMaxSize()) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(KeraGamePanel)
            .verticalScroll(rememberScrollState())
            .imePadding()
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(KeraChallengeSkyLight)
                .statusBarsPadding()
        )

        HotspotImage(
            imageRes = R.drawable.img_kerachallenge_maingame_tts_header,
            contentDescription = stringResource(R.string.tts_description),
            hotspots = emptyList(),
            texts = ttsHeaderTexts()
        )

        Column(
            modifier = Modifier.padding(horizontal = 12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Grid teka-teki silang
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(24.dp))
                    .background(Color.White)
                    .padding(12.dp)
            ) {
                SectionPill(text = stringResource(R.string.tts_pill))

                Spacer(modifier = Modifier.height(10.dp))

                BoxWithConstraints(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(KeraGameGrid)
                        .padding(8.dp)
                ) {
                    val cellSize = maxWidth / TekaTekiSilang.COLS

                    Column {
                        for (row in 0 until TekaTekiSilang.ROWS) {
                            Row {
                                for (col in 0 until TekaTekiSilang.COLS) {
                                    val cell = TtsCell(row, col)
                                    val correctLetter = solution[cell]

                                    if (correctLetter == null) {
                                        Spacer(modifier = Modifier.size(cellSize))
                                    } else {
                                        val letter = answers.letterAt(cell)
                                        val background = when {
                                            checked && letter == correctLetter -> KeraGameCorrect
                                            checked && letter != null -> KeraGameWrong
                                            cell in correctCells -> KeraGameCorrect
                                            activeWord != null && cell in activeWord.cells -> KeraGameActiveWord
                                            else -> Color.White
                                        }

                                        TtsCellField(
                                            letter = letter,
                                            number = TekaTekiSilang.numbers[cell],
                                            background = background,
                                            isActive = cell == activeCell,
                                            size = cellSize,
                                            focusRequester = focusRequesters.getValue(cell),
                                            onFocused = {
                                                activeCell = cell
                                                if (TekaTekiSilang.wordAt(cell, direction) == null) {
                                                    direction = direction.other()
                                                }
                                            },
                                            onLetterChange = { newLetter ->
                                                answers = answers.withLetter(cell, newLetter)
                                                checked = false
                                                if (newLetter != null) {
                                                    neighbour(cell, 1)?.let { focusRequesters[it]?.requestFocus() }
                                                }
                                            },
                                            onBackspaceWhenEmpty = {
                                                neighbour(cell, -1)?.let { previous ->
                                                    answers = answers.withLetter(previous, null)
                                                    checked = false
                                                    focusRequesters[previous]?.requestFocus()
                                                }
                                            }
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = stringResource(R.string.tts_help),
                    fontSize = 12.sp,
                    lineHeight = 16.sp,
                    color = KeraGameText
                )
            }

            TtsDirection.entries.forEach { dir ->
                ClueCard(
                    direction = dir,
                    words = TekaTekiSilang.words.filter { it.direction == dir },
                    activeWord = activeWord,
                    statuses = wordStatuses,
                    onClueClick = ::focusWord
                )
            }

            if (checked) {
                Text(
                    text = if (wrongCount == 0) {
                        stringResource(R.string.tts_all_correct)
                    } else {
                        stringResource(
                            R.string.tts_wrong,
                            wrongWords.map { "${it.number} ${stringResource(it.direction.labelRes)}" }.joinToString()
                        )
                    },
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = if (wrongCount == 0) KeraGreen else InputError
                )
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
                    .padding(top = 4.dp, bottom = 20.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                GameButton(
                    text = stringResource(R.string.tts_reset),
                    icon = Icons.Default.Refresh,
                    containerColor = Color.White,
                    contentColor = KeraGamePurple,
                    modifier = Modifier.weight(1f),
                    onClick = {
                        answers = EMPTY_ANSWERS
                        checked = false
                        activeCell = null
                        focusManager.clearFocus()
                    }
                )

                // Terkunci sampai semua kotak terisi
                GameButton(
                    text = stringResource(R.string.tts_check),
                    icon = if (allFilled) Icons.Default.Check else Icons.Default.Lock,
                    containerColor = if (allFilled) KeraGamePurple else KeraGameLocked,
                    contentColor = Color.White,
                    enabled = allFilled,
                    modifier = Modifier.weight(1.4f),
                    onClick = {
                        checked = true
                        focusManager.clearFocus()
                    }
                )
            }
        }
    }

        // Ukuran dan posisi sama dengan tombol kembali di semua halaman KeraChallenge
        KeraChallengeBackButton(onClick = onBackClick, style = KeraBackButtonStyle.PURPLE)
        GameMusicButton(modifier = Modifier.align(Alignment.TopEnd))
    }
}

/** Satu kotak huruf. Mengetik mengganti huruf yang ada, backspace di kotak kosong mundur ke kotak sebelumnya. */
@Composable
private fun TtsCellField(
    letter: Char?,
    number: Int?,
    background: Color,
    isActive: Boolean,
    size: Dp,
    focusRequester: FocusRequester,
    onFocused: () -> Unit,
    onLetterChange: (Char?) -> Unit,
    onBackspaceWhenEmpty: () -> Unit
) {
    val density = LocalDensity.current
    val letterSize = with(density) { (size * 0.5f).toSp() }
    val numberSize = with(density) { (size * 0.26f).toSp() }
    val shape = RoundedCornerShape(3.dp)
    val text = letter?.toString().orEmpty()

    Box(
        modifier = Modifier
            .size(size)
            .padding(1.dp)
            .clip(shape)
            .background(background)
            .then(if (isActive) Modifier.border(2.dp, KeraGamePurple, shape) else Modifier)
    ) {
        BasicTextField(
            // Kursor selalu di akhir, jadi huruf baru selalu jadi karakter terakhir
            value = TextFieldValue(text = text, selection = TextRange(text.length)),
            onValueChange = { value ->
                if (value.text.isEmpty()) {
                    onLetterChange(null)
                } else {
                    value.text.lastOrNull { it.isLetter() }
                        ?.uppercaseChar()
                        ?.let(onLetterChange)
                }
            },
            modifier = Modifier
                .fillMaxSize()
                .focusRequester(focusRequester)
                .onFocusChanged { if (it.isFocused) onFocused() }
                .onPreviewKeyEvent { event ->
                    if (event.type == KeyEventType.KeyDown && event.key == Key.Backspace && letter == null) {
                        onBackspaceWhenEmpty()
                        true
                    } else {
                        false
                    }
                },
            textStyle = TextStyle(
                fontSize = letterSize,
                fontWeight = FontWeight.Bold,
                color = KeraGameText,
                textAlign = TextAlign.Center
            ),
            singleLine = true,
            keyboardOptions = KeyboardOptions(
                capitalization = KeyboardCapitalization.Characters,
                autoCorrectEnabled = false,
                imeAction = ImeAction.Done
            ),
            cursorBrush = SolidColor(Color.Transparent),
            decorationBox = { innerTextField ->
                Box(contentAlignment = Alignment.Center) { innerTextField() }
            }
        )

        if (number != null) {
            Text(
                text = number.toString(),
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(start = 2.dp),
                fontSize = numberSize,
                lineHeight = numberSize,
                fontWeight = FontWeight.SemiBold,
                color = KeraGamePurple
            )
        }
    }
}

/** Daftar petunjuk satu arah. Menekan petunjuk langsung memilih kotak kata tersebut. */
@Composable
private fun ClueCard(
    direction: TtsDirection,
    words: List<TtsWord>,
    activeWord: TtsWord?,
    statuses: Map<TtsWord, TtsWordStatus>,
    onClueClick: (TtsWord) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(KeraGameClueCard)
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        SectionPill(
            text = stringResource(direction.labelRes),
            icon = if (direction == TtsDirection.MENDATAR) {
                Icons.AutoMirrored.Filled.KeyboardArrowRight
            } else {
                Icons.Default.KeyboardArrowDown
            }
        )

        words.forEach { word ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(if (word == activeWord) KeraGameActiveWord else Color.Transparent)
                    .clickable { onClueClick(word) }
                    .padding(horizontal = 6.dp, vertical = 6.dp),
                verticalAlignment = Alignment.Top
            ) {
                Box(
                    modifier = Modifier
                        .size(26.dp)
                        .clip(CircleShape)
                        .background(KeraGamePurple),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = word.number.toString(),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }

                Spacer(modifier = Modifier.width(10.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = stringResource(word.clueRes),
                        fontSize = 13.sp,
                        lineHeight = 17.sp,
                        color = KeraGameText
                    )
                    Text(
                        text = if (word.displayAnswer.contains(' ')) {
                            stringResource(R.string.tts_letters_nospace, word.answer.length)
                        } else {
                            stringResource(R.string.tts_letters, word.answer.length)
                        },
                        fontSize = 11.sp,
                        color = KeraGamePurple
                    )
                }

                // Tanda hasil validasi begitu semua kotak kata terisi
                when (statuses[word]) {
                    TtsWordStatus.BENAR -> ClueMark(icon = Icons.Default.Check, color = KeraGreen, description = stringResource(R.string.tts_correct))
                    TtsWordStatus.SALAH -> ClueMark(icon = Icons.Default.Close, color = InputError, description = stringResource(R.string.tts_incorrect))
                    else -> Unit
                }
            }
        }
    }
}

/** Tanda kecil ✓/✗ di sebelah petunjuk. */
@Composable
private fun ClueMark(icon: ImageVector, color: Color, description: String) {
    Box(
        modifier = Modifier
            .padding(start = 8.dp)
            .size(24.dp)
            .clip(CircleShape)
            .background(color),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = description,
            tint = Color.White,
            modifier = Modifier.size(16.dp)
        )
    }
}
