package com.michele.eurocoins.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.michele.eurocoins.data.MintLetters

/**
 * Se l'utente ha il Pro e dove portarlo se non ce l'ha. Fornito da `EuroCoinsNavHost` (che conosce il
 * `NavController`): le schermate non sanno niente di Play Billing, chiedono solo "è Pro?" e, per l'invito,
 * [onGoPro] porta alle Impostazioni, dove sta la card con il prezzo (resta l'unico punto che avvia l'acquisto).
 */
@Immutable
class ProAccess(val isPro: Boolean, val onGoPro: () -> Unit)

val LocalProAccess = compositionLocalOf { ProAccess(isPro = false, onGoPro = {}) }

/** Tirature per lettera di zecca, fornite da `MainActivity` (asset opzionale: vuoto = nessuna schermata ha lettere). */
val LocalMintLetters = staticCompositionLocalOf { MintLetters.EMPTY }

/**
 * Invito al Pro per il dettaglio per zecca (2026-10-11, mockup approvato): dice cosa si ottiene in una frase,
 * "Not now" a sinistra e "Go Pro" pieno a pillola, come il pulsante della card Pro delle Impostazioni.
 */
@Composable
fun MintLettersProPrompt(onDismiss: () -> Unit, onGoPro: () -> Unit) {
    AlertDialog(
        containerColor = MaterialTheme.colorScheme.surface,
        onDismissRequest = onDismiss,
        title = { DialogTitle("Mintage by mint") },
        text = {
            Text(
                "See how many coins each German mint struck, for every coin and year. " +
                    "Part of Euro Coins Pro, with no ads.",
            )
        },
        confirmButton = {
            Button(
                onClick = {
                    onDismiss()
                    onGoPro()
                },
                shape = RoundedCornerShape(50),
            ) { Text("Go Pro") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Not now") } },
    )
}

/**
 * Riga lilla toccabile che dice che una vista è del Pro (lucchetto, testo, freccia): sta dove il Pro vedrebbe
 * qualcosa in più, solo se quel dato esiste. Lilla perché è un controllo azionabile, non un'avvertenza.
 */
@Composable
fun ProLockedRow(text: String, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.secondaryContainer,
        modifier = modifier.fillMaxWidth(),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Icon(Icons.Filled.Lock, contentDescription = null)
            Text(
                text = text,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.weight(1f),
            )
            Icon(Icons.Filled.ChevronRight, contentDescription = null)
        }
    }
}
