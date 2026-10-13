package com.michele.eurocoins.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.michele.eurocoins.data.mintLetterCity

/**
 * Le zecche di una moneta con lettera (oggi la Germania) come cerchi: pieni dove l'utente la possiede, vuoti dove
 * manca, con "3 of 5 mints" accanto. Sta nella card COLLECTION dei dettagli (mockup del 2026-10-11). È un di più
 * sopra il "posseduto": non cambia nessun totale. [owned] sono le lettere con almeno una finitura in collezione.
 *
 * **Toccabili** (2026-10-13, dopo un parere UX): i cerchi stanno sotto i chip dell'anno e hanno la stessa forma, ma
 * prima non rispondevano al tocco (un controllo finto). Le zecche sono cinque posti fissi, una lista di controllo
 * (non una sequenza da sfogliare come gli anni): il tocco AGISCE sul posto, [onLetterClick] apre il pannello di
 * collezione già su quella zecca (e sull'anno scelto). Scartato il filtro delle righe per zecca (uno stato in più
 * per 1-5 righe, e righe che sembrano sparite). Cerchi da 36 dp (erano 30), senza area di tocco più grande dei
 * cerchi: a 48 dp i bersagli si sovrapporrebbero con 6 dp di spazio.
 */
@Composable
fun MintLetterBadges(
    paese: String,
    letters: List<String>,
    owned: Set<String>,
    modifier: Modifier = Modifier,
    onLetterClick: ((String) -> Unit)? = null,
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
                    .size(36.dp)
                    .clip(CircleShape)
                    .then(
                        if (has) Modifier.background(colors.primary, CircleShape)
                        else Modifier.border(1.5.dp, colors.outline, CircleShape),
                    )
                    .then(
                        if (onLetterClick != null) Modifier.clickable(role = Role.Button) { onLetterClick(letter) }
                        else Modifier,
                    )
                    .semantics {
                        val city = mintLetterCity(paese, letter)
                        contentDescription = "Mint $letter" + (city?.let { ", $it" } ?: "") +
                            if (has) ", owned" else ", not owned"
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
