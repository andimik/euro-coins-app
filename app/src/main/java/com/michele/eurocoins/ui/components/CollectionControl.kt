package com.michele.eurocoins.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp

/**
 * Il controllo a destra di ogni moneta degli elenchi (commemorative e Regular Issues). **Cambia forma con la
 * modalità** (2026-10-15, parere UX, variante A dopo mockup):
 *
 * - **Quick add acceso: casella quadrata con la spunta**, com'era. È un interruttore vero: un tocco la accende o
 *   la spegne, e il suo aspetto lo promette.
 * - **Modalità dettaglio: pulsante rotondo**, "+" a contorno se non posseduta ("aggiungi") e cerchio verde pieno con
 *   la matita se posseduta ("modifica"). Il tocco apre il pannello My collection in entrambi i casi, quindi una
 *   casella con la spunta era sbagliata: prometteva "tocco = spengo" e su una moneta già spuntata apriva un modulo.
 *   + e matita sono gli stessi glifi di "Add collection" / "Edit collection" della card COLLECTION. Lo stato
 *   "posseduta" non dipende dal solo colore: cambia anche il glifo (+ contro matita).
 *
 * Area di tocco 44 dp, disegno 26 dp (casella) o 32 dp (pulsante).
 */
@Composable
fun CollectionControl(owned: Boolean, onClick: () -> Unit) {
    val primary = MaterialTheme.colorScheme.primary
    val quickAdd = LocalQuickAdd.current
    Box(
        modifier = Modifier
            .size(44.dp)
            .clip(CircleShape)
            .clickable(role = Role.Button, onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        if (quickAdd) {
            Box(
                modifier = Modifier
                    .size(26.dp)
                    .clip(RoundedCornerShape(7.dp))
                    .background(if (owned) primary else Color.Transparent)
                    .border(
                        2.dp,
                        if (owned) primary else MaterialTheme.colorScheme.onSurfaceVariant,
                        RoundedCornerShape(7.dp),
                    ),
                contentAlignment = Alignment.Center,
            ) {
                if (owned) {
                    Icon(
                        imageVector = Icons.Filled.Check,
                        contentDescription = "In your collection, tap to remove",
                        tint = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.size(18.dp),
                    )
                }
            }
        } else {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(if (owned) primary else Color.Transparent)
                    .border(2.dp, primary, CircleShape),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = if (owned) Icons.Filled.Edit else Icons.Filled.Add,
                    contentDescription = if (owned) "In your collection, tap to edit" else "Add to your collection",
                    tint = if (owned) MaterialTheme.colorScheme.onPrimary else primary,
                    modifier = Modifier.size(if (owned) 16.dp else 20.dp),
                )
            }
        }
    }
}
