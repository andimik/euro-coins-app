package com.michele.eurocoins.data

import java.io.File
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/** Quick add: la regola del tocco sulla casella e la voce "senza anno" delle Regular Issues. */
class QuickAddTest {

    private fun commemorative(
        quality: CoinQuality = CoinQuality.STANDARD,
        priceCents: Int? = null,
        purchasedOn: Long? = null,
    ) = CollectionItem(
        coinKey = "k", quality = quality, priceCents = priceCents, purchasedOn = purchasedOn,
        anno = 2012, paese = "Italia", tema = "X", addedAt = 0L,
    )

    private fun regular(
        series: RegularIssueSeries,
        taglio: String,
        anno: Int,
        quality: CoinQuality = CoinQuality.STANDARD,
        variety: String = "",
        priceCents: Int? = null,
        purchasedOn: Long? = null,
    ) = RegularCollectionItem(
        seriesKey = series.stableKey, taglio = taglio, anno = anno, quality = quality, variety = variety,
        priceCents = priceCents, purchasedOn = purchasedOn, paese = series.paese, addedAt = 0L,
    )

    private val all: List<RegularIssueSeries> = Json { ignoreUnknownKeys = true }
        .decodeFromString<List<RegularIssueSeriesJson>>(File("src/main/assets/regular_issues.json").readText())
        .map { it.toEntity() }

    private fun forCountry(paese: String) = all.filter { it.paese == paese }

    // ---- la regola del tocco -------------------------------------------------------------------------

    @Test
    fun anEmptyCoinIsAdded() {
        assertEquals(QuickTap.ADD, quickTapFor(emptyList()))
    }

    @Test
    fun aPlainStandardIsRemovedWithoutLosingAnything() {
        assertEquals(QuickTap.REMOVE, quickTapFor(listOf(commemorative())))
    }

    @Test
    fun anythingWithDetailsAsksBeforeDeleting() {
        assertEquals(QuickTap.CONFIRM_REMOVE, quickTapFor(listOf(commemorative(priceCents = 350))))
        assertEquals(QuickTap.CONFIRM_REMOVE, quickTapFor(listOf(commemorative(purchasedOn = 20_000L))))
        assertEquals(QuickTap.CONFIRM_REMOVE, quickTapFor(listOf(commemorative(CoinQuality.BU))))
        assertEquals(QuickTap.CONFIRM_REMOVE, quickTapFor(listOf(commemorative(), commemorative(CoinQuality.PROOF))))
    }

    @Test
    fun aRegularRowWithOnlyTheNoYearEntryIsRemoved() {
        val s = forCountry("Grecia").first()
        assertEquals(QuickTap.ADD, quickTapForRegular(emptyList()))
        assertEquals(QuickTap.REMOVE, quickTapForRegular(listOf(regular(s, "2 euro", NO_YEAR))))
    }

    @Test
    fun aRegularRowWithAYearOrDetailsAsksBeforeRemoving() {
        val s = forCountry("Grecia").first()
        assertEquals(QuickTap.CONFIRM_REMOVE, quickTapForRegular(listOf(regular(s, "2 euro", 2002))))
        assertEquals(QuickTap.CONFIRM_REMOVE, quickTapForRegular(listOf(regular(s, "2 euro", NO_YEAR, priceCents = 100))))
        assertEquals(QuickTap.CONFIRM_REMOVE, quickTapForRegular(listOf(regular(s, "2 euro", NO_YEAR, CoinQuality.BU))))
        assertEquals(QuickTap.CONFIRM_REMOVE, quickTapForRegular(listOf(regular(s, "2 euro", NO_YEAR, variety = VARIETY_EFS))))
        assertEquals(QuickTap.CONFIRM_REMOVE, quickTapForRegular(listOf(regular(s, "2 euro", NO_YEAR), regular(s, "2 euro", 2003))))
    }

    // ---- la voce senza anno appartiene alla riga guardata ---------------------------------------------

    @Test
    fun noYearIsNeverInsideAnyWindow() {
        val s = forCountry("Germania").first()
        val row = denominationsOf(forCountry("Germania"), s).first()
        assertTrue(!row.contains(NO_YEAR))
    }

    @Test
    fun aNoYearEntryFillsExactlyTheRowItWasTappedOn() {
        val france = forCountry("Francia")
        val s1 = france.first { it.ordineCronologico == 1 }
        val s2 = france.first { it.ordineCronologico == 2 }
        val s3 = france.first { it.ordineCronologico == 3 }
        // Il 5 cent francese e' lo stesso disegno in tre serie (origine: la 1). Toccare la casella della
        // riga della serie 2 salva la voce con la chiave della serie 2: riempie solo quella riga.
        val onSeries2 = listOf(regular(s2, "5 cent", NO_YEAR))
        assertEquals(Progress(1, 24), france.regularProgress(onSeries2))
        val rows = france.associateWith { denominationsOf(france, it).first { d -> d.image.taglio == "5 cent" } }
        assertTrue(rows.getValue(s1).rowItems(onSeries2).isEmpty())
        assertEquals(1, rows.getValue(s2).rowItems(onSeries2).size)
        assertTrue(rows.getValue(s3).rowItems(onSeries2).isEmpty())
    }

    @Test
    fun aNoYearEntryOnTheOriginSeriesFillsOnlyTheOriginRow() {
        val france = forCountry("Francia")
        val s1 = france.first { it.ordineCronologico == 1 }
        val s2 = france.first { it.ordineCronologico == 2 }
        val onSeries1 = listOf(regular(s1, "5 cent", NO_YEAR))
        assertEquals(Progress(1, 24), france.regularProgress(onSeries1))
        val row2 = denominationsOf(france, s2).first { it.image.taglio == "5 cent" }
        assertTrue(row2.rowItems(onSeries1).isEmpty())
        // E insieme a un anno vero dentro la finestra della serie 2 le righe restano due, non una.
        val both = onSeries1 + regular(s1, "5 cent", 2023)
        assertEquals(Progress(2, 24), france.regularProgress(both))
    }

    @Test
    fun theNoYearEntryIsOwnedButIsNotAYear() {
        val s = forCountry("Germania").first()
        val items = listOf(regular(s, "2 euro", NO_YEAR))
        assertEquals(0, ownedYearsOf(items))
        assertEquals(1, ownedYearsOf(items + regular(s, "2 euro", 2015)))
        val rows = denominationRows(forCountry("Germania"), items)
        val row = rows.getValue("2 euro").single()
        assertTrue(row.owned)
        assertEquals(0, row.ownedYears)
    }

    @Test
    fun theChipTextNamesTheNoYearEntryAndTheVariety() {
        assertEquals("No year", YearOption(NO_YEAR).chipLabel())
        assertEquals("2008", YearOption(2008).chipLabel())
        assertEquals("2002 EFS", YearOption(2002, VARIETY_EFS).chipLabel())
    }

    @Test
    fun theNoYearOptionIsListedFirstOnlyWhenTheRowHasIt() {
        val s = forCountry("Grecia").first()
        val image = s.immagini.first { it.taglio == "2 euro" }
        val without = regularYearOptions(s, image, emptyList(), 2026)
        assertTrue(without.none { it.year == NO_YEAR })
        val with = regularYearOptions(s, image, listOf(YearOption(NO_YEAR)), 2026)
        assertEquals(YearOption(NO_YEAR), with.first())
        assertEquals("No year", with.first().label)
        // Se la riga ha la voce senza anno, il pannello si apre su di essa.
        assertEquals(YearOption(NO_YEAR), defaultYearOption(with, listOf(YearOption(NO_YEAR))))
    }
}
