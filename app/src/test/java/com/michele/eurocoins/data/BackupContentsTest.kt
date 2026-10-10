package com.michele.eurocoins.data

import com.michele.eurocoins.data.backup.BackupContents
import com.michele.eurocoins.data.backup.BackupFile
import com.michele.eurocoins.data.backup.BackupItem
import com.michele.eurocoins.data.backup.RegularBackupItem
import com.michele.eurocoins.data.backup.contents
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

/** Cosa dice il dialog di ripristino: monete distinte per catalogo e totale, come il Reset. */
class BackupContentsTest {

    private fun item(coinKey: String, quality: String = "STANDARD") = BackupItem(
        coinKey = coinKey, quality = quality, anno = 2012, paese = "Italia", tema = "X", addedAt = 1L,
    )

    private fun regular(seriesKey: String, taglio: String, anno: Int, quality: String = "STANDARD", variety: String = "") =
        RegularBackupItem(seriesKey = seriesKey, taglio = taglio, anno = anno, quality = quality, variety = variety, paese = "Grecia", addedAt = 1L)

    @Test
    fun aCoinWithSeveralFinishesCountsOnce() {
        val file = BackupFile(exportedAt = 1L, items = listOf(item("a"), item("a", "BU"), item("a", "PROOF"), item("b")))
        assertEquals(2, file.contents().commemorative)
    }

    @Test
    fun aRegularDenominationWithSeveralYearsAndVarietiesCountsOnce() {
        val file = BackupFile(
            exportedAt = 1L,
            items = emptyList(),
            regularItems = listOf(
                regular("Grecia|1", "2 euro", 2002),
                regular("Grecia|1", "2 euro", 2002, variety = VARIETY_EFS),
                regular("Grecia|1", "2 euro", 2003, "BU"),
                regular("Grecia|1", "1 euro", 2002),
                regular("Francia|2", "1 euro", 2023),
            ),
        )
        assertEquals(BackupContents(commemorative = 0, regular = 3), file.contents())
    }

    @Test
    fun theTotalAddsBothCatalogs() {
        val c = BackupContents(commemorative = 93, regular = 190)
        assertEquals(283, c.total)
    }

    @Test
    fun anOldBackupWithoutRegularIssuesSaysSoInsteadOfZero() {
        // Versione 1: Regular Issues non esistevano, il ripristino non le tocca.
        val old = BackupFile(schemaVersion = 1, exportedAt = 1L, items = listOf(item("a")))
        val contents = old.contents()
        assertNull(contents.regular)
        assertEquals(1, contents.total)
        // Un backup recente con zero Regular e' un dato vero, non "assente".
        assertEquals(0, BackupFile(exportedAt = 1L, items = listOf(item("a"))).contents().regular)
    }

    @Test
    fun anUnknownQualityIsNotCounted() {
        val file = BackupFile(exportedAt = 1L, items = listOf(item("a"), item("b", "GOLD")))
        assertEquals(1, file.contents().commemorative)
    }

    @Test
    fun theSummaryLineSkipsEmptySectionsAndSaysEmpty() {
        assertEquals("93 Commemorative · 184 Regular Issues", BackupContents(93, 184).summaryLine())
        assertEquals("85 Regular Issues", BackupContents(0, 85).summaryLine())
        assertEquals("12 Commemorative", BackupContents(12, null).summaryLine())
        assertEquals("Empty", BackupContents(0, 0).summaryLine())
    }
}
