package com.michele.eurocoins.ui.detail

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.michele.eurocoins.data.Coin
import com.michele.eurocoins.data.CoinQuality
import com.michele.eurocoins.data.MintLetterShare
import com.michele.eurocoins.data.displayCountry
import com.michele.eurocoins.data.displayTema
import com.michele.eurocoins.data.mintLetterCity
import com.michele.eurocoins.ui.components.LocalMintLetters
import com.michele.eurocoins.ui.components.LocalProAccess
import com.michele.eurocoins.ui.components.LocalShowMints
import com.michele.eurocoins.ui.components.MintLettersProPrompt
import java.text.NumberFormat
import java.util.Locale

/**
 * Sotto i tre numeri di MINTAGES di una commemorativa: "View by mint" (Pro), che apre le tirature per lettera di
 * zecca (oggi le 5 tedesche). Compare solo se il dato esiste per questa moneta. Senza Pro lo stesso posto ha un
 * pulsante lilla con il lucchetto che apre l'invito (2026-10-11, mockup approvato). Stessa posizione e stessa
 * grammatica del "View by year" delle Regular Issues.
 */
@Composable
fun CoinMintLettersEntry(coin: Coin) {
    // "German mints" spento nelle Impostazioni: nessun pulsante, nemmeno quello bloccato con l'invito al Pro.
    if (!LocalShowMints.current) return
    val shares = LocalMintLetters.current.forCommemorative(coin.numistaId, coin.anno)
    if (shares.isEmpty()) return
    val access = LocalProAccess.current
    var open by remember { mutableStateOf(false) }

    if (open) {
        if (access.isPro) {
            CoinMintLettersSheet(coin = coin, shares = shares, onDismiss = { open = false })
        } else {
            MintLettersProPrompt(onDismiss = { open = false }, onGoPro = access.onGoPro)
        }
    }
    Row(modifier = Modifier.fillMaxWidth().padding(top = 4.dp), horizontalArrangement = Arrangement.Center) {
        if (access.isPro) {
            TextButton(onClick = { open = true }) {
                Text("View by mint")
                Icon(Icons.Filled.KeyboardArrowDown, contentDescription = null, modifier = Modifier.padding(start = 4.dp).size(18.dp))
            }
        } else {
            FilledTonalButton(onClick = { open = true }) {
                Icon(Icons.Filled.Lock, contentDescription = null, modifier = Modifier.padding(end = 6.dp).size(16.dp))
                Text("View by mint")
            }
        }
    }
}

/**
 * Bottom sheet di sola lettura (come `RegularMintageHistorySheet`): una riga per lettera con Standard / BU / Proof
 * e il totale in fondo, che coincide con i tre numeri della card (la somma delle lettere è controllata contro
 * `coins.json` in `MintLettersAssetTest`). "—" dove Numista non ha il dato, mai zero.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CoinMintLettersSheet(coin: Coin, shares: List<MintLetterShare>, onDismiss: () -> Unit) {
    val numberFormat = remember { NumberFormat.getIntegerInstance(Locale.ENGLISH) }
    fun format(value: Long?) = value?.let(numberFormat::format) ?: NO_VALUE
    fun total(quality: CoinQuality): Long? =
        shares.mapNotNull { it.mintageFor(quality) }.takeIf { it.isNotEmpty() }?.sum()

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = MaterialTheme.colorScheme.background,
    ) {
        Column(
            modifier = Modifier
                .padding(horizontal = 16.dp)
                .padding(bottom = 16.dp)
                .windowInsetsPadding(WindowInsets.navigationBars),
        ) {
            Text(
                text = "MINTAGES BY MINT",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.primary,
            )
            Text(
                text = coin.displayTema(),
                style = sansTitleMedium(),
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(top = 2.dp),
            )
            Text(
                text = "${coin.displayCountry()} · ${coin.anno}",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 4.dp, bottom = 12.dp),
            )
            Row(modifier = Modifier.fillMaxWidth().padding(bottom = 6.dp)) {
                HeaderCell("Mint", Modifier.width(MINT_COLUMN_WIDTH), center = false)
                CoinQuality.entries.forEach { HeaderCell(it.label, Modifier.weight(it.columnWeight()), center = true) }
            }
            HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.4f))
            shares.forEach { share ->
                ValuesRow(
                    label = {
                        Text(share.lettera, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                        mintLetterCity(coin.paese, share.lettera)?.let {
                            Text(
                                text = "  $it",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 1,
                            )
                        }
                    },
                    values = CoinQuality.entries.map { format(share.mintageFor(it)) },
                    bold = false,
                )
                HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
            }
            ValuesRow(
                label = { Text("Total", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold) },
                values = CoinQuality.entries.map { format(total(it)) },
                bold = true,
            )
            TextButton(onClick = onDismiss, modifier = Modifier.align(Alignment.End).padding(top = 8.dp)) {
                Text("Close")
            }
        }
    }
}

private val MINT_COLUMN_WIDTH = 104.dp

private fun CoinQuality.columnWeight(): Float = if (this == CoinQuality.STANDARD) 1.3f else 1f

@Composable
private fun HeaderCell(text: String, modifier: Modifier, center: Boolean) {
    Text(
        text = text,
        style = MaterialTheme.typography.labelSmall,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        textAlign = if (center) TextAlign.Center else TextAlign.Start,
        modifier = modifier,
    )
}

@Composable
private fun ValuesRow(label: @Composable () -> Unit, values: List<String>, bold: Boolean) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 9.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Row(modifier = Modifier.width(MINT_COLUMN_WIDTH), verticalAlignment = Alignment.CenterVertically) { label() }
        CoinQuality.entries.forEachIndexed { index, quality ->
            Text(
                text = values[index],
                style = MaterialTheme.typography.bodyMedium.copy(fontFeatureSettings = "tnum"),
                fontWeight = if (bold) FontWeight.SemiBold else FontWeight.Normal,
                textAlign = TextAlign.Center,
                maxLines = 1,
                softWrap = false,
                modifier = Modifier.weight(quality.columnWeight()),
            )
        }
    }
}
