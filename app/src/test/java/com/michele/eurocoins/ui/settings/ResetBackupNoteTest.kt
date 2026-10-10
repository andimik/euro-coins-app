package com.michele.eurocoins.ui.settings

import com.michele.eurocoins.data.backup.BackupStatus
import com.michele.eurocoins.data.backup.GoogleAccount
import com.michele.eurocoins.ui.backup.BackupUiState
import org.junit.Assert.assertEquals
import org.junit.Test

/** Il testo sul backup nel dialog di Reset: i quattro casi. */
class ResetBackupNoteTest {

    private val account = GoogleAccount("a@b.c", null)

    /** Spazi non separabili dentro la data, come nel testo (a capo non deve restare "07:44" da solo). */
    private fun nbsp(date: String) = date.replace(' ', Char(0x00A0))

    @Test
    fun notSignedInHasNoBackupToRestoreFrom() {
        assertEquals(
            "You're not signed in, so there's no backup to restore from.",
            resetBackupNote(BackupUiState(configured = true)),
        )
    }

    @Test
    fun upToDateBackupCanBeRestored() {
        val note = resetBackupNote(
            BackupUiState(configured = true, account = account, localStatus = BackupStatus.UpToDate, lastBackup = "6 Oct 2026, 07:44"),
            coins = 6,
        )
        assertEquals("Your Google Drive backup from " + nbsp("6 Oct 2026, 07:44") + " has them, so you can restore them.", note)
    }

    @Test
    fun singleCoinUsesTheSingularPronoun() {
        val note = resetBackupNote(
            BackupUiState(configured = true, account = account, localStatus = BackupStatus.UpToDate),
            coins = 1,
        )
        assertEquals("Your Google Drive backup has it, so you can restore it.", note)
    }

    @Test
    fun changesAfterTheLastBackupCannotBeRestored() {
        val note = resetBackupNote(
            BackupUiState(configured = true, account = account, localStatus = BackupStatus.Unknown, lastBackupLocal = "5 Oct 2026, 10:00"),
        )
        assertEquals("Anything added since your last backup on " + nbsp("5 Oct 2026, 10:00") + " can't be restored.", note)
    }

    @Test
    fun noBackupAtAllCannotBeUndone() {
        val note = resetBackupNote(BackupUiState(configured = true, account = account))
        assertEquals("There's no backup on Google Drive, so this can't be undone.", note)
    }

    @Test
    fun theNoteIsAWarningOnlyWhenSomethingWouldBeLost() {
        // Backup aggiornato: rassicurazione, testo normale.
        assertEquals(
            false,
            resetBackupNoteIsWarning(BackupUiState(configured = true, account = account, localStatus = BackupStatus.UpToDate)),
        )
        // Non collegato, modifiche non salvate, nessun backup: avviso rosso con icona.
        assertEquals(true, resetBackupNoteIsWarning(BackupUiState(configured = true)))
        assertEquals(
            true,
            resetBackupNoteIsWarning(BackupUiState(configured = true, account = account, localStatus = BackupStatus.Pending(3), lastBackup = "6 Oct 2026, 07:44")),
        )
        assertEquals(
            true,
            resetBackupNoteIsWarning(BackupUiState(configured = true, account = account, localStatus = BackupStatus.Unknown)),
        )
    }
}
