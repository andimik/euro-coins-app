package com.michele.eurocoins.data

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class MintLetterTableTest {

    private val letters = MintLetters.parse(
        """
        {"schema":1,"circolanti":[
          {"paese":"Germania","taglio":"1 cent","anno":2004,"zecche":[
            {"lettera":"A","standard":280000000,"bu":135000,"proof":106000},
            {"lettera":"D","standard":294000000,"bu":135000,"proof":106000}]},
          {"paese":"Germania","taglio":"1 cent","anno":2006,"zecche":[
            {"lettera":"A","proof":75000},
            {"lettera":"D","bu":85000,"proof":75000}]}
        ],"commemorative":[]}
        """.trimIndent(),
    )

    @Test
    fun oneColumnPerLetterAndOneRowPerGivenYear() {
        val table = mintLetterTable(letters, "Germania", "1 cent", listOf(2004, 2005, 2006), CoinQuality.STANDARD)!!
        assertEquals(listOf("A", "D"), table.letters)
        assertEquals(listOf(2004, 2005, 2006), table.rows.map { it.anno })
        assertEquals(listOf<Long?>(280_000_000L, 294_000_000L), table.rows[0].values)
        // 2005 non ha lettere nel dato: la riga c'è, vuota (la tabella dei totali ha quell'anno).
        assertEquals(listOf<Long?>(null, null), table.rows[1].values)
        // 2006 non ha lo standard: "non c'è il dato", mai zero.
        assertEquals(listOf<Long?>(null, null), table.rows[2].values)
    }

    @Test
    fun theQualityPicksTheValues() {
        val bu = mintLetterTable(letters, "Germania", "1 cent", listOf(2004, 2006), CoinQuality.BU)!!
        assertEquals(listOf<Long?>(135_000L, 135_000L), bu.rows[0].values)
        assertEquals(listOf<Long?>(null, 85_000L), bu.rows[1].values)
    }

    @Test
    fun noTableWhereThereAreNoLetters() {
        assertNull(mintLetterTable(letters, "Francia", "1 cent", listOf(2004), CoinQuality.STANDARD))
        assertNull(mintLetterTable(letters, "Germania", "2 euro", listOf(2004), CoinQuality.STANDARD))
        assertNull(mintLetterTable(MintLetters.EMPTY, "Germania", "1 cent", listOf(2004), CoinQuality.STANDARD))
    }

    @Test
    fun compactFormatKeepsUpToTwoDecimalsWithoutTrailingZeros() {
        assertEquals("960 M", formatCompactMintage(960_000_000))
        assertEquals("119.4 M", formatCompactMintage(119_400_000))
        assertEquals("125.37 M", formatCompactMintage(125_370_000))
        assertEquals("83.58 M", formatCompactMintage(83_580_000))
        assertEquals("4.26 M", formatCompactMintage(4_260_000))
        assertEquals("4 B", formatCompactMintage(4_000_000_000))
        assertEquals("1.4 B", formatCompactMintage(1_400_000_000))
        assertEquals("85 k", formatCompactMintage(85_000))
        assertEquals("12.5 k", formatCompactMintage(12_500))
        assertEquals("9,000", formatCompactMintage(9_000))
        assertEquals("0", formatCompactMintage(0))
    }
}
