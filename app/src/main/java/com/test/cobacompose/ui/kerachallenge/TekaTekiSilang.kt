package com.test.cobacompose.ui.kerachallenge

import androidx.annotation.StringRes
import com.test.cobacompose.R

/** Arah kata di teka-teki silang. */
internal enum class TtsDirection(@StringRes val labelRes: Int) {
    MENDATAR(R.string.tts_across),
    MENURUN(R.string.tts_down)
}

/** Posisi satu kotak di grid. */
internal data class TtsCell(val row: Int, val col: Int)

/**
 * Satu jawaban: nomor, arah, kotak awal dan petunjuknya.
 * [displayAnswer] ditulis seperti biasa (boleh berspasi, mis. "Vitamin C"); di grid spasinya
 * dihapus dan semua huruf kapital, lihat [answer].
 */
internal data class TtsWord(
    val number: Int,
    val direction: TtsDirection,
    val row: Int,
    val col: Int,
    val displayAnswer: String,
    @StringRes val clueRes: Int
) {
    /** Jawaban di grid: tanpa spasi, huruf kapital. */
    val answer: String = displayAnswer.filter { it.isLetter() }.uppercase()

    val cells: List<TtsCell> = answer.indices.map { i ->
        if (direction == TtsDirection.MENDATAR) TtsCell(row, col + i) else TtsCell(row + i, col)
    }
}

/**
 * Teka-teki silang Main Game Hari 2.
 * Nomor mengikuti kunci jawaban (Mendatar 1, 3, 5, 6, 8, 9, 10; Menurun 2, 4, 7) sebagai label
 * di kotak awal tiap kata. Urutannya tidak bisa mengikuti urutan baca grid: huruf awal semua
 * jawaban menurun (V, U, P) tidak ada di ANTOSIANIN, jadi nomor 1 tidak mungkin jadi kotak pertama.
 */
internal object TekaTekiSilang {
    const val ROWS = 17
    const val COLS = 12

    val words = listOf(
        TtsWord(
            1, TtsDirection.MENDATAR, 14, 0, "Antosianin",
            R.string.tts_clue_1
        ),
        TtsWord(
            2, TtsDirection.MENURUN, 9, 5, "Vitamin C",
            R.string.tts_clue_2
        ),
        TtsWord(
            3, TtsDirection.MENDATAR, 1, 2, "Pengolahan",
            R.string.tts_clue_3
        ),
        TtsWord(
            4, TtsDirection.MENURUN, 10, 1, "Ubi Ungu",
            R.string.tts_clue_4
        ),
        TtsWord(
            5, TtsDirection.MENDATAR, 3, 1, "Singkong",
            R.string.tts_clue_5
        ),
        TtsWord(
            6, TtsDirection.MENDATAR, 5, 2, "Protein",
            R.string.tts_clue_6
        ),
        TtsWord(
            7, TtsDirection.MENURUN, 0, 8, "Pangan Alternatif",
            R.string.tts_clue_7
        ),
        TtsWord(
            8, TtsDirection.MENDATAR, 12, 4, "Kampanye",
            R.string.tts_clue_8
        ),
        TtsWord(
            9, TtsDirection.MENDATAR, 7, 2, "Donomulyo",
            R.string.tts_clue_9
        ),
        TtsWord(
            10, TtsDirection.MENDATAR, 9, 7, "Serat",
            R.string.tts_clue_10
        )
    )

    /** Huruf yang benar untuk tiap kotak terisi. */
    val solution: Map<TtsCell, Char> = buildMap {
        words.forEach { word ->
            word.cells.forEachIndexed { i, cell ->
                val existing = get(cell)
                // Penjaga tata letak: huruf di persilangan harus sama untuk kedua kata
                check(existing == null || existing == word.answer[i]) {
                    "Persilangan tidak cocok di $cell untuk ${word.number} ${word.direction}"
                }
                put(cell, word.answer[i])
            }
        }
    }

    /** Nomor kecil di pojok kotak awal kata. */
    val numbers: Map<TtsCell, Int> = words.associate { TtsCell(it.row, it.col) to it.number }

    fun wordAt(cell: TtsCell, direction: TtsDirection): TtsWord? =
        words.firstOrNull { it.direction == direction && cell in it.cells }

    /**
     * Cek satu jawaban dari huruf-huruf yang diisi di kotaknya (null = kosong).
     * Huruf besar/kecil diabaikan; spasi tidak dihitung karena tiap kotak satu huruf.
     */
    fun wordStatus(word: TtsWord, letters: List<Char?>): TtsWordStatus = when {
        letters.all { it == null } -> TtsWordStatus.KOSONG
        letters.any { it == null } -> TtsWordStatus.BELUM_LENGKAP
        letters.joinToString("").uppercase() == word.answer -> TtsWordStatus.BENAR
        else -> TtsWordStatus.SALAH
    }
}

/** Hasil pengecekan satu jawaban. */
internal enum class TtsWordStatus { KOSONG, BELUM_LENGKAP, BENAR, SALAH }
