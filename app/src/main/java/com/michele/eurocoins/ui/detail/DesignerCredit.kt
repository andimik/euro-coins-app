package com.michele.eurocoins.ui.detail

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ErrorOutline
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.michele.eurocoins.ui.components.DialogTitle

/**
 * Il designer nella scheda DETAILS (2026-10-19, variante E del quarto mockup, scelta dal proprietario): un nome
 * certo è un [ValueLabel] come gli altri; un nome solo probabile (le fonti nominano l'incisore e non dicono se ha
 * anche ideato il disegno) ha lo STESSO aspetto, con in più un cerchio con il punto esclamativo in BRONZO
 * (`secondary`) accanto all'etichetta "Designer": è l'unico segnale, e il tocco sulla riga apre
 * [DesignerCreditInfoDialog]. Il nome resta centrato e intero, la scheda è identica a quella delle monete certe.
 *
 * Storia (tutto su mockup): icona ⓘ attaccata al NOME (spostava il nome dal centro), poi nome in corsivo con
 * "Designer ⓘ" tutto verdigris (troppi segnali), poi solo l'ⓘ verde accanto all'etichetta: il proprietario ha
 * osservato che chi guarda la legge come "maggiori informazioni sul designer" e non nota che alcuni nomi ce l'hanno e
 * altri no, cioè l'icona non diceva "approssimativo". Da qui un segnale di cautela. **Il bronzo è già nella palette**
 * (`secondary`: euro del segnaposto senza foto, icone `€` delle righe senza immagine) ma finora solo decorativo: qui
 * prende il significato "attenzione, controlla", l'unico uso, non il rosso del distruttivo. Scartati: il triangolo
 * (dice "errore", ma il nome non è sbagliato, solo il ruolo è incerto), il punto interrogativo verde (si confonde con
 * un aiuto generico), l'icona tonale in un cerchio, l'etichetta con una domanda e il corsivo.
 */
@Composable
fun DesignerValue(name: String, probable: Boolean, label: String, modifier: Modifier = Modifier) {
    if (!probable) {
        ValueLabel(value = name, label = label, modifier = modifier)
        return
    }
    var showInfo by remember { mutableStateOf(false) }
    Column(modifier = modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = name,
            style = sansTitleMedium(),
            fontWeight = FontWeight.SemiBold,
            textAlign = TextAlign.Center,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.fillMaxWidth().semantics { contentDescription = "$name, role may be approximate" },
        )
        Row(
            modifier = Modifier
                .heightIn(min = 40.dp)
                .clickable(role = Role.Button, onClickLabel = "About this credit") { showInfo = true },
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(5.dp),
        ) {
            // spazio vuoto grande come l'icona: l'etichetta resta centrata sotto il nome, non spostata a sinistra
            Spacer(Modifier.size(18.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Icon(
                imageVector = Icons.Outlined.ErrorOutline,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.secondary,
                modifier = Modifier.size(18.dp),
            )
        }
    }
    if (showInfo) DesignerCreditInfoDialog(onDismiss = { showInfo = false })
}

/** Spiega l'icona: il nome è quello accreditato, il ruolo (designer o incisore) può essere approssimativo. */
@Composable
fun DesignerCreditInfoDialog(onDismiss: () -> Unit) {
    AlertDialog(
        containerColor = MaterialTheme.colorScheme.surface,
        onDismissRequest = onDismiss,
        title = {
            // la stessa icona del segnale, perché chi tocca riconosca che è la stessa cosa
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Icon(
                    imageVector = Icons.Outlined.ErrorOutline,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.secondary,
                    modifier = Modifier.size(24.dp),
                )
                DialogTitle("About this credit")
            }
        },
        text = {
            Text(
                // spazi non separabili (U+00A0): "may be approximate." resta unito e non lascia una parola sola a capo
                "The sources credit this artist, but the role may be approximate.",
                style = MaterialTheme.typography.bodyMedium,
            )
        },
        confirmButton = { TextButton(onClick = onDismiss) { Text("Got it") } },
    )
}
