package com.test.cobacompose.ui.kerachallenge

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class TekaTekiSilangTest {

    /** Kunci jawaban resmi: nomor + arah -> jawaban. */
    private val answerKey = mapOf(
        (1 to TtsDirection.MENDATAR) to "Antosianin",
        (3 to TtsDirection.MENDATAR) to "Pengolahan",
        (5 to TtsDirection.MENDATAR) to "Singkong",
        (6 to TtsDirection.MENDATAR) to "Protein",
        (8 to TtsDirection.MENDATAR) to "Kampanye",
        (9 to TtsDirection.MENDATAR) to "Donomulyo",
        (10 to TtsDirection.MENDATAR) to "Serat",
        (2 to TtsDirection.MENURUN) to "Vitamin C",
        (4 to TtsDirection.MENURUN) to "Ubi Ungu",
        (7 to TtsDirection.MENURUN) to "Pangan Alternatif"
    )

    @Test
    fun jawabanSesuaiKunci() {
        val actual = TekaTekiSilang.words.associate { (it.number to it.direction) to it.displayAnswer }
        assertEquals(answerKey, actual)
    }

    @Test
    fun tiapKataPunyaKotakAwalSendiriDanMuatDiGrid() {
        val starts = TekaTekiSilang.words.map { TtsCell(it.row, it.col) }
        assertEquals("Kotak awal tidak boleh dipakai dua kata", starts.size, starts.toSet().size)

        TekaTekiSilang.words.flatMap { it.cells }.forEach { cell ->
            assertTrue("$cell di luar grid", cell.row in 0 until TekaTekiSilang.ROWS && cell.col in 0 until TekaTekiSilang.COLS)
        }
    }

    @Test
    fun persilanganHurufnyaCocok() {
        // solution memakai check() untuk tiap persilangan; mengaksesnya cukup untuk menguji
        val solution = TekaTekiSilang.solution
        TekaTekiSilang.words.forEach { word ->
            assertEquals(word.answer, word.cells.map { solution.getValue(it) }.joinToString(""))
        }
    }

    @Test
    fun validasiPerKata() {
        val vitaminC = TekaTekiSilang.words.first { it.number == 2 }

        assertEquals(TtsWordStatus.KOSONG, TekaTekiSilang.wordStatus(vitaminC, List(8) { null }))
        assertEquals(
            TtsWordStatus.BELUM_LENGKAP,
            TekaTekiSilang.wordStatus(vitaminC, "VITAMIN".toList() + null)
        )
        assertEquals(TtsWordStatus.BENAR, TekaTekiSilang.wordStatus(vitaminC, "vitaminc".toList()))
        assertEquals(TtsWordStatus.SALAH, TekaTekiSilang.wordStatus(vitaminC, "VITAMINE".toList()))
    }
}
