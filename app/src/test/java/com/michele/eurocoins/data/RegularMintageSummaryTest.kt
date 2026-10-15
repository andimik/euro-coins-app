package com.michele.eurocoins.data

import org.junit.Assert.assertEquals
import org.junit.Test

class RegularMintageSummaryTest {

    @Test
    fun noDataReturnsNullSummary() {
        assertEquals(MintageSummary(null, null), summarizeMintages(emptyList(), CoinQuality.STANDARD))
    }

    @Test
    fun singleYearCaptionIsTheYearNotSummed() {
        val tirature = listOf(RegularIssueMintage(anno = 2020, quality = CoinQuality.PROOF, tiratura = 12_000))
        assertEquals(MintageSummary(12_000, "2020"), summarizeMintages(tirature, CoinQuality.PROOF))
    }

    @Test
    fun multipleYearsAreSummedWithAnAllYearsCaption() {
        val tirature = listOf(
            RegularIssueMintage(anno = 2002, quality = CoinQuality.STANDARD, tiratura = 1_200_000),
            RegularIssueMintage(anno = 2003, quality = CoinQuality.STANDARD, tiratura = 980_000),
        )
        assertEquals(MintageSummary(2_180_000, "all years"), summarizeMintages(tirature, CoinQuality.STANDARD))
    }

    @Test
    fun otherQualitiesDoNotLeakIntoTheSum() {
        val tirature = listOf(
            RegularIssueMintage(anno = 2020, quality = CoinQuality.STANDARD, tiratura = 1_000_000),
            RegularIssueMintage(anno = 2020, quality = CoinQuality.PROOF, tiratura = 12_000),
        )
        assertEquals(MintageSummary(1_000_000, "2020"), summarizeMintages(tirature, CoinQuality.STANDARD))
    }

    @Test
    fun groupingByYearIsAscendingAndOnlyIncludesYearsWithData() {
        val tirature = listOf(
            RegularIssueMintage(anno = 2020, quality = CoinQuality.PROOF, tiratura = 12_000),
            RegularIssueMintage(anno = 2002, quality = CoinQuality.STANDARD, tiratura = 1_200_000),
            RegularIssueMintage(anno = 2015, quality = CoinQuality.BU, tiratura = 25_000),
        )
        val rows = groupMintagesByYear(tirature)
        assertEquals(listOf(2002, 2015, 2020), rows.map { it.first })
        assertEquals(mapOf(CoinQuality.STANDARD to 1_200_000L), rows[0].second)
        assertEquals(mapOf(CoinQuality.BU to 25_000L), rows[1].second)
        assertEquals(mapOf(CoinQuality.PROOF to 12_000L), rows[2].second)
    }

    @Test
    fun sameYearMultipleQualitiesMergeIntoOneRow() {
        val tirature = listOf(
            RegularIssueMintage(anno = 2020, quality = CoinQuality.STANDARD, tiratura = 1_000_000),
            RegularIssueMintage(anno = 2020, quality = CoinQuality.PROOF, tiratura = 12_000),
        )
        val rows = groupMintagesByYear(tirature)
        assertEquals(1, rows.size)
        assertEquals(mapOf(CoinQuality.STANDARD to 1_000_000L, CoinQuality.PROOF to 12_000L), rows[0].second)
    }

    @Test
    fun sumsBeyondIntRangeDoNotOverflow() {
        // Germania 1 cent: 4 miliardi nel solo 2002, oltre Int.MAX_VALUE (2,147,483,647).
        val tirature = listOf(
            RegularIssueMintage(anno = 2002, quality = CoinQuality.STANDARD, tiratura = 4_000_000_000),
            RegularIssueMintage(anno = 2004, quality = CoinQuality.STANDARD, tiratura = 1_400_000_000),
        )
        assertEquals(MintageSummary(5_400_000_000, "all years"), summarizeMintages(tirature, CoinQuality.STANDARD))
    }

    @Test
    fun captionAdmitsMissingYearsInsteadOfClaimingAllYears() {
        // Lo standard del 2003 manca (Numista non lo ha): "all years" sarebbe un totale falso.
        val tirature = listOf(
            RegularIssueMintage(anno = 2002, quality = CoinQuality.STANDARD, tiratura = 1_000),
            RegularIssueMintage(anno = 2002, quality = CoinQuality.BU, tiratura = 10),
            RegularIssueMintage(anno = 2003, quality = CoinQuality.BU, tiratura = 20),
            RegularIssueMintage(anno = 2004, quality = CoinQuality.STANDARD, tiratura = 3_000),
            RegularIssueMintage(anno = 2004, quality = CoinQuality.BU, tiratura = 30),
        )
        assertEquals(MintageSummary(4_000, "2 of 3 years"), summarizeMintages(tirature, CoinQuality.STANDARD))
        assertEquals(MintageSummary(60, "all years"), summarizeMintages(tirature, CoinQuality.BU))
    }

    @Test
    fun compactCardValuesAlwaysHaveOneDecimal() {
        // Un taglio con più anni: tre colonne dello stesso aspetto, senza "≈" e senza numeri a 5 cifre accanto ai miliardi.
        assertEquals("14.2 B", formatCardMintage(14_216_389_000, compact = true))
        assertEquals("10.1 M", formatCardMintage(10_126_000, compact = true))
        assertEquals("19.0 k", formatCardMintage(19_000, compact = true))
        assertEquals("523", formatCardMintage(523, compact = true))
    }

    @Test
    fun singleYearCardValuesStayExact() {
        // Un anno solo: non c'è "View by year", quindi l'esatto si legge solo qui.
        assertEquals("12,345,678", formatCardMintage(12_345_678, compact = false))
        assertEquals("19,000", formatCardMintage(19_000, compact = false))
    }
}
