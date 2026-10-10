package com.michele.eurocoins.ui.settings

import org.junit.Assert.assertEquals
import org.junit.Test

/** Le righe del riquadro "THIS REMOVES" del dialog di Reset: i nomi dei cataloghi come nel resto dell'app, la sezione vuota saltata. */
class OwnedCountsTest {

    @Test
    fun oneRowPerSectionNamedLikeTheCatalogs() {
        assertEquals(listOf("Commemorative" to "2", "Regular Issues" to "4"), OwnedCounts(2, 4).rows())
    }

    @Test
    fun emptySectionIsSkipped() {
        assertEquals(listOf("Commemorative" to "85"), OwnedCounts(85, 0).rows())
        assertEquals(listOf("Regular Issues" to "4"), OwnedCounts(0, 4).rows())
        assertEquals(emptyList<Pair<String, String>>(), OwnedCounts(0, 0).rows())
    }

    @Test
    fun theTotalAddsBothSections() {
        assertEquals(89, OwnedCounts(85, 4).total)
    }
}
