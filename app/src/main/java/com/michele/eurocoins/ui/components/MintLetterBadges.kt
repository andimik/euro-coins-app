package com.michele.eurocoins.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.michele.eurocoins.data.mintLetterCity

/**
 * Le zecche di una moneta con lettera (oggi la Germania) come cerchi: pieni dove l'utente la possiede, vuoti dove
 * manca, con "3 of 5 mints" accanto. Sta nella card COLLECTION dei dettagli (mockup del 2026-10-11). È un di più
 * sopra il "posseduto": non cambia nessun totale. [owned] sono le lettere con almeno una finitura in collezione.
 */
@Composable
fun MintLetterBadges(
    paese: String,
    letters: List<String>,
    owned: Set<String>,
    modifier: Modifier = Modifier,
) {
    val colors = MaterialTheme.colorScheme
    val count = letters.count { it in owned }
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        modifier = modifier,
    ) {
        letters.forEach { letter ->
            val has = letter in owned
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(30.dp)
                    .then(
                        if (has) Modifier.background(colors.primary, CircleShape)
                        else Modifier.border(1.5.dp, colors.outline, CircleShape),
                    )
                    .semantics {
                        val city = mintLetterCity(paese, letter)
                        contentDescription = "Mint $letter" + (city?.let { ", $it" } ?: "") + if (has) ", owned" else ", not owned"
                    },
            ) {
                Text(
                    text = letter,
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Medium,
                    color = if (has) colors.onPrimary else colors.onSurfaceVariant,
                )
            }
        }
        Text(
            text = "$count of ${letters.size} mints",
            style = MaterialTheme.typography.bodySmall,
            color = colors.onSurfaceVariant,
            modifier = Modifier.padding(start = 4.dp),
        )
    }
}
