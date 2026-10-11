package com.michele.eurocoins.ui.components

import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.michele.eurocoins.data.greekEfsLetterFor
import com.michele.eurocoins.data.greekEfsLetterGroups

/**
 * La "i" accanto alla scelta dell'anno quando c'è la varietà EFS (Grecia 2002): apre [EfsInfoDialog]. Un'icona a parte
 * e non dentro il chip, il cui tocco serve a sceglierlo; l'area di tocco è da 40 dp, l'icona da 20.
 */
@Composable
fun VarietyInfoButton(onClick: () -> Unit, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .size(40.dp)
            .clip(CircleShape)
            .clickable(role = Role.Button, onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            imageVector = Icons.Outlined.Info,
            contentDescription = "What is the EFS variety?",
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(20.dp),
        )
    }
}

/**
 * Cosa significa "EFS" e dove cercare la lettera su ogni taglio (2026-10-11, mockup approvato; l'utente ha chiesto
 * anche "quale lettera guardare": in alto la riga per la moneta che si ha davanti, [taglio], e nella tabella la riga
 * del taglio evidenziata). La tabella è raggruppata per lettera: cinque tagli su otto portano la F. Non dice su quale
 * stella cade la lettera: il dato non c'è e non si inventa.
 */
@Composable
fun EfsInfoDialog(taglio: String?, onDismiss: () -> Unit) {
    val own = taglio?.let(::greekEfsLetterFor)
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
                    "Some Greek coins of 2002 were minted abroad. They carry a small letter inside a star on the Greek " +
                        "side: E for Spain, F for France and S for Suomi (Finland), hence EFS. Coins minted in Athens " +
                        "have no letter.",
                )
                if (own != null && taglio != null) {
                    Text(
                        text = buildAnnotatedString {
                            append("On this $taglio, look for the letter ")
                            withStyle(SpanStyle(fontWeight = FontWeight.Bold)) { append(own.letter) }
                            append(" (minted in ${own.mint}).")
                        },
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                        color = MaterialTheme.colorScheme.primary,
                    )
                }
                val shape = RoundedCornerShape(12.dp)
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.6f), shape)
                        .padding(horizontal = 12.dp, vertical = 10.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    Text(
                        text = "WHERE TO LOOK",
                        style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 0.6.sp),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    greekEfsLetterGroups().forEach { group ->
                        val current = group == own
                        val weight = if (current) FontWeight.Medium else FontWeight.Normal
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text(
                                text = group.label,
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = weight),
                                color = if (current) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
                            )
                            Text(
                                text = "${group.letter} · ${group.mint}",
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = weight),
                                color = if (current) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                }
                Text(
                    "They are separate coins, so you can register both the normal 2002 and the 2002 EFS.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        },
        confirmButton = { TextButton(onClick = onDismiss) { Text("Got it") } },
    )
}
