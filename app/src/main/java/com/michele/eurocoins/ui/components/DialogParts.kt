package com.michele.eurocoins.ui.components

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Warning
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Pezzi comuni ai dialog che riguardano la collezione intera: "Replace your collection?" (ripristino) e "Reset
 * collection?". Stesso riquadro e stesso avviso, così i due dialog hanno la stessa grammatica e non possono
 * divergere (2026-10-10: il Reset era testo con un elenco puntato, il Replace un riquadro; l'utente ha chiesto di
 * adeguare il Reset al Replace).
 */

/** "1 coin" / "283 coins". */
fun coinsText(count: Int): String = "$count ${if (count == 1) "coin" else "coins"}"

/**
 * Riquadro con bordo: un titolo piccolo in maiuscolo ("THE BACKUP CONTAINS", "THIS REMOVES"), una riga per catalogo
 * con il numero allineato a destra e il totale sotto un filetto. [footnote] è una riga piccola grigia al posto di una
 * riga assente (un backup vecchio senza Regular Issues).
 */
@Composable
fun CountsBox(
    heading: String,
    rows: List<Pair<String, String>>,
    total: String,
    footnote: String? = null,
) {
    val shape = RoundedCornerShape(12.dp)
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.6f), shape)
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Text(
            text = heading,
            style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 0.6.sp),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        rows.forEach { (label, value) -> CountRow(label, value) }
        footnote?.let {
            Text(
                text = it,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        HorizontalDivider(
            modifier = Modifier.padding(vertical = 4.dp),
            color = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f),
        )
        CountRow("Total", total, bold = true)
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
 * Avviso di perdita di dati: testo rosso (il colore del distruttivo nell'app, lo stesso di "Replace", "Reset" e
 * "Remove") con un'icona davanti. Va usato solo dove qualcosa si perde davvero: una rassicurazione ("il backup ce
 * l'ha") è testo normale.
 */
@Composable
fun DialogWarning(text: String) {
    val error = MaterialTheme.colorScheme.error
    Row(verticalAlignment = Alignment.Top) {
        Icon(Icons.Outlined.Warning, contentDescription = null, tint = error, modifier = Modifier.padding(top = 2.dp).size(16.dp))
        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
            color = error,
            modifier = Modifier.padding(start = 8.dp),
        )
    }
}
