package com.michele.eurocoins.data

import java.io.File
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

/** La spiegazione dell'EFS: la tabella "dove cercare" raggruppata per lettera, derivata dalla stessa tabella della varietà. */
class EfsLetterGroupsTest {

    @Test
    fun threeRowsGroupedByLetterInDenominationOrder() {
        val groups = greekEfsLetterGroups()
        assertEquals(listOf("F", "E", "S"), groups.map { it.letter })
        assertEquals(listOf("1, 2, 5, 10, 50 cent", "20 cent", "1 and 2 euro"), groups.map { it.label })
        assertEquals(listOf("Paris", "Madrid", "Finland"), groups.map { it.mint })
    }

    @Test
    fun everyDenominationIsInExactlyOneGroup() {
        val all = greekEfsLetterGroups().flatMap { it.taglios }
        assertEquals(REGULAR_DENOMINATIONS.sorted(), all.sorted())
    }

    @Test
    fun theLetterForADenominationIsTheOneToLookFor() {
        assertEquals("S", greekEfsLetterFor("2 euro")?.letter)
        assertEquals("S", greekEfsLetterFor("1 euro")?.letter)
        assertEquals("E", greekEfsLetterFor("20 cent")?.letter)
        assertEquals("F", greekEfsLetterFor("50 cent")?.letter)
        assertNull(greekEfsLetterFor("3 euro"))
    }

    @Test
    fun theExplanationAgreesWithTheVarietyTheSheetOffers() {
        // Stessa tabella: la lettera che la spiegazione indica è quella del segno che la varietà dichiara.
        val greece = Json { ignoreUnknownKeys = true }
            .decodeFromString<List<RegularIssueSeriesJson>>(File("src/main/assets/regular_issues.json").readText())
            .map { it.toEntity() }
            .first { it.paese == "Grecia" && it.ordineCronologico == 1 }
        for (taglio in REGULAR_DENOMINATIONS) {
            val variety = greece.varietyFor(taglio, 2002)
            assertEquals("letter ${greekEfsLetterFor(taglio)?.letter} in the star", variety?.detail)
        }
    }
}
