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
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.michele.eurocoins.data.mintLetterCity
import com.michele.eurocoins.ui.theme.PurpleFieldDark
import com.michele.eurocoins.ui.theme.PurpleFieldLight

/**
 * Le zecche di una moneta con lettera (oggi la Germania) come cerchi: pieni dove l'utente la possiede, vuoti dove
 * manca, con "3 of 5 mints" accanto. Sta nella card COLLECTION dei dettagli (mockup del 2026-10-11). È un di più
 * sopra il "posseduto": non cambia nessun totale. [owned] sono le lettere con almeno una finitura in collezione.
 *
 * **Filtro** (2026-10-14, richiesta del proprietario, "come gli anni"): con [onSelect] i cerchi si toccano e la card
 * mostra solo le righe di quella zecca; un secondo tocco sulla stessa lettera toglie il filtro (nessuna scelta =
 * righe di tutte, com'era). La lettera scelta ha un anello viola, lo stesso viola dei bordi delle righe, che si
 * somma al riempimento: una zecca può essere posseduta e scelta insieme. Storia: il 2026-10-13 il tocco apriva il
 * pannello My collection su quella zecca (parere UX: le zecche sono una lista di controllo), scartato dal proprietario
 * perché duplicava "Edit collection". Cerchi da 36 dp (erano 30) dentro un riquadro da 40 dp che ospita l'anello.
 */
@Composable
fun MintLetterBadges(
    paese: String,
    letters: List<String>,
    owned: Set<String>,
    modifier: Modifier = Modifier,
    selected: String? = null,
    onSelect: ((String?) -> Unit)? = null,
) {
    val colors = MaterialTheme.colorScheme
    val ring = if (colors.surface.luminance() < 0.5f) PurpleFieldDark else PurpleFieldLight
    val count = letters.count { it in owned }
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(2.dp),
        modifier = modifier,
    ) {
        letters.forEach { letter ->
            val has = letter in owned
            val isSelected = letter == selected
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .then(if (isSelected) Modifier.border(2.5.dp, ring, CircleShape) else Modifier)
                    .then(
                        if (onSelect != null) {
                            Modifier.clickable(role = Role.Button) { onSelect(if (isSelected) null else letter) }
                        } else Modifier,
                    )
                    .semantics {
                        val city = mintLetterCity(paese, letter)
                        contentDescription = "Mint $letter" + (city?.let { ", $it" } ?: "") +
                            (if (has) ", owned" else ", not owned") + if (isSelected) ", showing only this mint" else ""
                    },
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(34.dp)
                        .then(
                            if (has) Modifier.background(colors.primary, CircleShape)
                            else Modifier.border(1.5.dp, colors.outline, CircleShape),
                        ),
                ) {
                    Text(
                        text = letter,
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Medium,
                        color = if (has) colors.onPrimary else colors.onSurfaceVariant,
                    )
                }
            }
        }
        Text(
            text = "$count of ${letters.size} mints",
            style = MaterialTheme.typography.bodySmall,
            color = colors.onSurfaceVariant,
            modifier = Modifier.padding(start = 6.dp),
        )
    }
}
