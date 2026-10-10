package com.michele.eurocoins.ui.backup

import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
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
import com.michele.eurocoins.ui.components.DialogTitle

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
                ContentsBox(prompt.latest)
                Row(verticalAlignment = Alignment.Top) {
                    Icon(Icons.Outlined.Warning, contentDescription = null, tint = error, modifier = Modifier.padding(top = 2.dp).size(16.dp))
                    Text(
                        text = "Anything added since will be lost.",
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                        color = error,
                        modifier = Modifier.padding(start = 8.dp),
                    )
                }
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

/** Riquadro "The backup contains": una riga per catalogo e il totale sotto un filetto. */
@Composable
private fun ContentsBox(contents: BackupContents) {
    val shape = RoundedCornerShape(12.dp)
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.6f), shape)
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Text(
            text = "THE BACKUP CONTAINS",
            style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 0.6.sp),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        CountRow("Commemorative", contents.commemorative.toString())
        if (contents.regular != null) {
            CountRow("Regular Issues", contents.regular.toString())
        } else {
            // Un backup precedente a Regular Issues non le porta: il ripristino non le tocca, e "0" farebbe credere il contrario.
            Text(
                text = "Regular Issues aren't in this backup: yours stay as they are.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        HorizontalDivider(
            modifier = Modifier.padding(vertical = 4.dp),
            color = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f),
        )
        CountRow("Total", "${contents.total} ${if (contents.total == 1) "coin" else "coins"}", bold = true)
    }
}

@Composable
private fun CountRow(label: String, value: String, bold: Boolean = false) {
    val style = MaterialTheme.typography.bodyMedium.copy(fontWeight = if (bold) FontWeight.Medium else FontWeight.Normal)
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, style = style)
        Text(value, style = style)
    }
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
                .heightIn(min = 48.dp)
                .clickable(role = Role.Button, onClick = onUse)
                .wrapContentHeight(Alignment.CenterVertically),
        )
    }
}
