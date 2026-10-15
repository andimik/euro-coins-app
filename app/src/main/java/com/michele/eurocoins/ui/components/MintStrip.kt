package com.michele.eurocoins.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.michele.eurocoins.data.NO_MINT
import com.michele.eurocoins.data.mintLetterCity

/** Etichetta del chip: "No mint" per la zecca non indicata, altrimenti la lettera. */
fun mintChipLabel(mint: String): String = if (mint == NO_MINT) "No mint" else mint

/**
 * Scelta della zecca nei pannelli di collezione (fase 3 delle zecche tedesche, mockup del 2026-10-11): una riga
 * di chip sotto il sottotitolo (commemorative) o sotto la striscia degli anni (Regular): **"No mint"** (la voce
 * semplice di sempre, primo e predefinito, parallelo a "No year") e le lettere del paese ([mints]). Stessi chip
 * dell'anno (`FilterChip`, scelto in lilla). Un puntino verde sul chip dice che quella zecca ha già finiture
 * spuntate (la bozza non salvata conta): il pallino sta ANGOLATO sul chip e non davanti all'etichetta, perché con
 * sei chip su un telefono da 375 dp l'icona davanti non ci starebbe.
 *
 * [locked] = senza Pro: la riga resta visibile con il lucchetto (le lettere già registrate si vedono, vedi i
 * puntini) ma un tocco apre l'invito al Pro ([onLockedTap]) invece di scegliere la zecca.
 */
@Composable
fun MintStrip(
    paese: String,
    mints: List<String>,
    selected: String,
    withData: Set<String>,
    locked: Boolean,
    onSelect: (String) -> Unit,
    onLockedTap: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = MaterialTheme.colorScheme
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        modifier = modifier.fillMaxWidth(),
    ) {
        Text(text = "Mint", style = MaterialTheme.typography.bodyMedium, color = colors.onSurfaceVariant)
        if (locked) {
            Icon(
                imageVector = Icons.Filled.Lock,
                contentDescription = "Part of Pro",
                tint = colors.onSurfaceVariant,
                modifier = Modifier.size(14.dp),
            )
            Text(text = "Pro", style = MaterialTheme.typography.labelMedium, color = colors.onSurfaceVariant)
        }
    }
    // Larghezze a peso (non scorrevole): "No mint" ha più spazio delle lettere e tutte e sei entrano su un telefono
    // da 375 dp senza che l ultima esca di lato (la J era tagliata).
    Row(
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 4.dp, bottom = 4.dp),
    ) {
        (listOf(NO_MINT) + mints).forEach { mint ->
            val hasData = mint in withData
            val isSelected = mint == selected
            val city = if (mint == NO_MINT) null else mintLetterCity(paese, mint)
            Box(modifier = Modifier.weight(if (mint == NO_MINT) 2.2f else 1f).alpha(if (locked) 0.6f else 1f)) {
                FilterChip(
                    selected = isSelected,
                    onClick = { if (locked) onLockedTap() else onSelect(mint) },
                    label = {
                        Text(
                            text = mintChipLabel(mint),
                            maxLines = 1,
                            softWrap = false,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth(),
                        )
                    },
                    modifier = Modifier.fillMaxWidth().semantics {
                        contentDescription = buildString {
                            append(if (mint == NO_MINT) "No mint recorded" else "Mint $mint")
                            city?.let { append(", $it") }
                            if (hasData) append(", marked as owned")
                            if (isSelected) append(", selected")
                            if (locked) append(", part of Pro")
                        }
                    },
                )
                if (hasData) {
                    Box(
                        Modifier
                            .align(Alignment.TopEnd)
                            .offset(x = (-3).dp, y = 3.dp)
                            .size(8.dp)
                            .background(colors.primary, CircleShape),
                    )
                }
            }
        }
    }
}
