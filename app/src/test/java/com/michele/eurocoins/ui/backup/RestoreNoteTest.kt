package com.michele.eurocoins.ui.backup

import com.michele.eurocoins.data.backup.BackupStatus
import org.junit.Assert.assertEquals
import org.junit.Test

/** La nota del dialog di ripristino: rossa solo se qualcosa va perso, e detta come la card di stato. */
class RestoreNoteTest {

    @Test
    fun upToDateHasNothingToLoseSoItIsAPlainNote() {
        val note = restoreNote(BackupStatus.UpToDate)
        assertEquals("Your collection is already up to date with this backup.", note.text)
        assertEquals(false, note.isWarning)
    }

    @Test
    fun unsavedChangesAreCountedInTheWordsOfTheStatusCard() {
        assertEquals(RestoreNote("3 changes not backed up will be lost.", isWarning = true), restoreNote(BackupStatus.Pending(3)))
        assertEquals(RestoreNote("1 change not backed up will be lost.", isWarning = true), restoreNote(BackupStatus.Pending(1)))
    }

    @Test
    fun anUnknownStateKeepsTheGenericWarning() {
        assertEquals(RestoreNote("Anything added since will be lost.", isWarning = true), restoreNote(BackupStatus.Unknown))
    }
}
