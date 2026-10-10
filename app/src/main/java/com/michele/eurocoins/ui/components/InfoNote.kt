package com.michele.eurocoins.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Nota informativa: icona "i" piccola in `primary` davanti a una riga di testo grigio, senza riquadro né fondo.
 * Dice un fatto ("dati in arrivo", "tirature non disponibili"), non un errore: per questo niente colori d'errore e
 * niente contenitore, che in una lista di card sembrerebbe un'altra card o un banner. Testo corto, una riga.
 * [horizontalArrangement] centra la nota dentro una card o la lascia a sinistra sotto un titolo.
 */
@Composable
fun InfoNote(
    text: String,
    modifier: Modifier = Modifier,
    horizontalArrangement: Arrangement.Horizontal = Arrangement.Start,
) = InfoNote(AnnotatedString(text), modifier, horizontalArrangement)

/**
 * Come sopra, con testo formattato (una parte in evidenza) e, per le note su più righe, l'icona allineata alla
 * PRIMA riga ([verticalAlignment] = [Alignment.Top]) invece che al centro del blocco.
 */
@Composable
fun InfoNote(
    text: AnnotatedString,
    modifier: Modifier = Modifier,
    horizontalArrangement: Arrangement.Horizontal = Arrangement.Start,
    verticalAlignment: Alignment.Vertical = Alignment.CenterVertically,
) {
    Row(
        modifier = modifier,
        horizontalArrangement = horizontalArrangement,
        verticalAlignment = verticalAlignment,
    ) {
        Icon(
            Icons.Outlined.Info,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(top = if (verticalAlignment == Alignment.Top) 2.dp else 0.dp).size(16.dp),
        )
        Text(
            text = text,
            style = MaterialTheme.typography.bodySmall.copy(fontSize = 13.sp),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(start = 7.dp),
        )
    }
}
