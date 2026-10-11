package com.michele.eurocoins.ui.components

import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import com.michele.eurocoins.data.greekEfsLetterFor
import com.michele.eurocoins.data.greekEfsLetterGroups

/**
 * La riga di aiuto che compare SOLO con la varietà EFS scelta (Grecia 2002), sotto i chip dell'anno: dice già la
 * lettera da cercare su QUESTO taglio ("Look for the letter S in a star · What's EFS?") e al tocco apre
 * [EfsInfoDialog]. Prima era una "i" accanto a "Year", visibile su ogni anno (2026-10-11, su segnalazione
 * dell'utente: serve solo dove c'entra l'EFS). **"What's EFS?" nel colore dei link** (variante C dopo mockup, scartati
 * "Learn more" in fondo, che mandava la riga a capo, e una pillola con freccia, che sul fondo dei chip sembra una
 * scelta d'anno): è la domanda a far capire che si tocca. L'icona è decorativa, il tocco è su tutta la riga (alta
 * almeno 40 dp).
 */
@Composable
fun EfsHint(taglio: String?, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val letter = taglio?.let(::greekEfsLetterFor)?.letter
    Row(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = 40.dp)
            .clickable(role = Role.Button, onClickLabel = "What is the EFS variety?", onClick = onClick),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Icon(
            imageVector = Icons.Outlined.Info,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(18.dp),
        )
        Text(
            text = buildAnnotatedString {
                if (letter != null) {
                    append("Look for the letter ")
                    withStyle(SpanStyle(fontWeight = FontWeight.Bold)) { append(letter) }
                    append(" in a star")
                } else {
                    append("Look for a letter in a star")
                }
                // Il segnale che si tocca: la domanda nel colore dei link dell'app (verdigris), come "Edit collection" e
                // "Show more". Dice da sola che c'è una risposta, e "Minted abroad" sta nel dialog.
                append(" · ")
                withStyle(SpanStyle(color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Medium)) {
                    append("What's EFS?")
                }
            },
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

/**
 * Cosa significa "EFS" e dove cercare la lettera su ogni taglio (2026-10-11, accorciato dopo il primo giro:
 * "troppo testo, deve capirsi più rapidamente"). Una frase con E, F, S in grassetto e la tabella raggruppata per
 * lettera (cinque tagli su otto portano la F), con la riga del taglio che si ha davanti ([taglio]) evidenziata:
 * la risposta a "quale lettera guardo" sta già nella riga [EfsHint], qui non si ripete. Non dice su quale stella
 * cade la lettera: il dato non c'è e non si inventa.
 */
@Composable
fun EfsInfoDialog(taglio: String?, onDismiss: () -> Unit) {
    val own = taglio?.let(::greekEfsLetterFor)
    val bold = SpanStyle(fontWeight = FontWeight.Bold)
    AlertDialog(
        containerColor = MaterialTheme.colorScheme.surface,
        onDismissRequest = onDismiss,
        title = { DialogTitle("EFS variety") },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Text(
                    buildAnnotatedString {
                        append("Greek 2002 coins minted abroad carry a letter in a star: ")
                        withStyle(bold) { append("E") }
                        append(" Spain, ")
                        withStyle(bold) { append("F") }
                        append(" France, ")
                        withStyle(bold) { append("S") }
                        append(" Suomi (Finland). Athens coins have none.")
                    },
                )
                val shape = RoundedCornerShape(12.dp)
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.6f), shape)
                        .padding(horizontal = 12.dp, vertical = 10.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    greekEfsLetterGroups().forEach { group ->
                        val current = group == own
                        val weight = if (current) FontWeight.Medium else FontWeight.Normal
                        val color = if (current) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text(
                                text = group.label,
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = weight),
                                color = if (current) color else MaterialTheme.colorScheme.onSurface,
                            )
                            Text(
                                text = AnnotatedString.Builder().apply {
                                    pushStyle(bold)
                                    append(group.letter)
                                    pop()
                                    append(" · ${group.mint}")
                                }.toAnnotatedString(),
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = weight),
                                color = color,
                            )
                        }
                    }
                }
            }
        },
        confirmButton = { TextButton(onClick = onDismiss) { Text("Got it") } },
    )
}
