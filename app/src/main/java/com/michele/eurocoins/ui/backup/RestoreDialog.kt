package com.michele.eurocoins.ui.backup

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.michele.eurocoins.data.backup.BackupContents
import com.michele.eurocoins.data.backup.BackupStatus
import com.michele.eurocoins.ui.components.CountsBox
import com.michele.eurocoins.ui.components.DialogTitle
import com.michele.eurocoins.ui.components.DialogWarning
import com.michele.eurocoins.ui.components.coinsText

/**
 * Conferma del ripristino (tocco su "Restore"): dice COSA si ripristina, non solo da quando. Il riquadro "The backup
 * contains" elenca commemorative, Regular Issues e totale, con i nomi e il conteggio del dialog di Reset. "Anything
 * added since will be lost" è in rosso con un'icona: è l'unico avviso di perdita dati, e il rosso è già il colore del
 * distruttivo nell'app ("Replace", "Remove"). La versione precedente è una sezione a parte, separata da un filetto,
 * con la sua data e il suo contenuto: è la rete di sicurezza contro un backup sovrascritto da una collezione sbagliata
 * e prima era un link senza spiegazione (e, come bottone a pillola con il testo a capo, con i bordi tagliati).
 * (2026-10-10, mockup approvato.)
 */
@Composable
fun RestoreDialog(
    prompt: RestorePrompt,
    /** Rapporto tra la collezione del telefono e l'ultimo backup: decide se l'avviso c'è e quanto dice (vedi [restoreNote]). */
    localStatus: BackupStatus,
    onReplace: () -> Unit,
    onUsePrevious: () -> Unit,
    onDismiss: () -> Unit,
) {
    val error = MaterialTheme.colorScheme.error
    AlertDialog(
        containerColor = MaterialTheme.colorScheme.surface,
        onDismissRequest = onDismiss,
        title = { DialogTitle("Replace your collection?") },
        text = {
            // Con il font ingrandito il dialog può non stare nello schermo: il testo scorre.
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Text(
                    "Your collection on this phone will be replaced by the backup saved on Google Drive" +
                        (prompt.latestDate?.let { " ($it)" } ?: "") + ".",
                )
                CountsBox(
                    heading = "THE BACKUP CONTAINS",
                    rows = listOfNotNull(
                        "Commemorative" to prompt.latest.commemorative.toString(),
                        prompt.latest.regular?.let { "Regular Issues" to it.toString() },
                    ),
                    total = coinsText(prompt.latest.total),
                    // Un backup precedente a Regular Issues non le porta: il ripristino non le tocca, e "0" farebbe credere il contrario.
                    footnote = if (prompt.latest.regular == null) "Regular Issues aren't in this backup: yours stay as they are." else null,
                )
                restoreNote(localStatus).let { note -> if (note.isWarning) DialogWarning(note.text) else Text(note.text) }
                prompt.previous?.let { previous ->
                    HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f))
                    OlderCopy(date = prompt.previousDate, contents = previous, onUse = onUsePrevious)
                }
            }
        },
        confirmButton = { TextButton(onClick = onReplace) { Text("Replace", color = error) } },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } },
    )
}

/**
 * La copia di sicurezza: titolo piccolo, data, contenuto e un'azione in testo semplice. Non un `TextButton`: la sua forma
 * a pillola ritagliava i bordi di un testo che va a capo (visto sul telefono, "Restore the previous version…").
 */
@Composable
private fun OlderCopy(date: String?, contents: BackupContents, onUse: () -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
        Text(
            text = "OLDER COPY ON DRIVE",
            style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 0.6.sp),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Text(date ?: "Date unknown", style = MaterialTheme.typography.bodyMedium)
        Text(
            text = contents.summaryLine(),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Text(
            text = "Restore this one instead",
            style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Medium),
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 44.dp)
                .clickable(role = Role.Button, onClick = onUse)
                .wrapContentHeight(Alignment.CenterVertically),
        )
    }
}

/** La nota sotto il riquadro del ripristino: [isWarning] = rossa con icona (qualcosa andrebbe perso), altrimenti testo normale. */
internal data class RestoreNote(val text: String, val isWarning: Boolean)

/**
 * Cosa dire su ciò che si perde, secondo lo stato del backup (lo stesso criterio del Reset: il rosso solo dove
 * qualcosa va perso davvero). Collezione aggiornata: niente da perdere, nota normale; modifiche non salvate: rosso e
 * quantificato con le parole della card di stato ("N changes not backed up"); stato non verificabile (telefono nuovo,
 * dopo il ripristino della versione precedente): l'avviso generico, perché non si sa cosa c'è oltre il backup.
 */
internal fun restoreNote(status: BackupStatus): RestoreNote = when (status) {
    BackupStatus.UpToDate -> RestoreNote("Your collection is already up to date with this backup.", isWarning = false)
    is BackupStatus.Pending ->
        RestoreNote("${status.changes} ${if (status.changes == 1) "change" else "changes"} not backed up will be lost.", isWarning = true)
    BackupStatus.Unknown -> RestoreNote("Anything added since will be lost.", isWarning = true)
}
