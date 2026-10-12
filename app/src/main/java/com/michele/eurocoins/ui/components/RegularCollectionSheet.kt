package com.michele.eurocoins.ui.components

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import kotlinx.coroutines.flow.first
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.layout.layout
import androidx.compose.runtime.snapshotFlow
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.material3.FilterChip
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.union
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.michele.eurocoins.data.NO_MINT
import com.michele.eurocoins.data.mintLettersFor
import com.michele.eurocoins.data.CoinQuality
import com.michele.eurocoins.data.RegularCollectionEntry
import com.michele.eurocoins.data.RegularCollectionItem
import com.michele.eurocoins.data.RegularIssueImage
import com.michele.eurocoins.data.RegularIssueSeries
import com.michele.eurocoins.data.YearOption
import com.michele.eurocoins.data.chipLabel
import com.michele.eurocoins.data.defaultYearOption
import com.michele.eurocoins.data.regularYearOptions
import com.michele.eurocoins.data.seriesTitle
import com.michele.eurocoins.data.stableKey
import com.michele.eurocoins.data.varietyFor
import java.time.Year

/** Una casella della bozza: un anno (con varietà) in una finitura. Chiave delle mappe di spunte e prezzi. */
internal data class DraftKey(val year: Int, val variety: String, val mint: String, val quality: CoinQuality)

/**
 * Pannello per registrare una moneta circolante posseduta. **Stesso pannello delle commemorative**
 * ([CollectionSheet]: tre card Standard / BU / Proof con il prezzo a destra, riuso di
 * [FinishCard]) più UN selettore dell'anno in cima: nelle commemorative l'anno è nel dataset, qui
 * (una serie copre più anni con lo stesso disegno) lo sceglie l'utente. Prima c'era una lista di
 * righe anno + prezzo da digitare, con "Add year" e rimozione: troppo diversa dall'altro pannello,
 * lenta per chi vuole solo registrare una moneta, e l'anno scritto a mano permetteva anni
 * impossibili o doppi.
 *
 * - **L'anno si sceglie da una lista** ([regularYearOptions]: dal primo anno della serie fino
 *   all'ultimo o a oggi), mai scritto. Il 2002 greco ha due voci (normale ed EFS): sono due monete.
 * - **Default = il primo anno della serie** (scelta dell'utente; non prima del 2002) come chip
 *   selezionato, ma **senza nessuna finitura spuntata**: una spunta preselezionata (come nelle
 *   commemorative, dove la moneta è una sola) qui è un'ipotesi sull'anno e non si distingue da una
 *   scelta dell'utente, quindi o si sposta da sola, o sparisce, o resta e va tolta a mano per chi
 *   vuole un altro anno (tre versioni provate il 2026-10-08/09). Registrare un anno costa tre tocchi
 *   (apri, spunta, Save); più anni si spuntano uno per uno senza toccare niente due volte. Se
 *   qualcosa è già in collezione si apre sulla prima voce posseduta.
 * - **Bozza per anno + Save**: cambiare anno non perde le spunte e i prezzi inseriti, "Save" scrive
 *   tutti gli anni insieme, chiudere senza salvare non cambia nulla (come [CollectionSheet]). Gli
 *   anni con almeno una finitura in bozza hanno un puntino verde sul chip ([YearStrip]).
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegularCollectionSheet(
    countryName: String,
    series: RegularIssueSeries,
    /** Posizione (1-based) della serie nella lista del paese: vedi `seriesTitle` in `RegularIssueText.kt`. */
    seriesNumber: Int,
    denomination: RegularIssueImage,
    currentItems: List<RegularCollectionItem>,
    /**
     * Cosa salvare e le zecche che il pannello può cambiare (`null` = tutte): come in [CollectionSheet], senza Pro
     * il pannello vede e cambia solo "No mint" e il salvataggio lascia intatte le voci con zecca.
     */
    onSave: (List<RegularCollectionEntry>, Set<String>?) -> Unit,
    onDismiss: () -> Unit,
    /** Anno su cui aprire il pannello (es. quello scelto nella card COLLECTION del dettaglio); null = il predefinito. */
    initialYear: YearOption? = null,
) {
    val access = LocalProAccess.current
    // Le lettere di zecca del paese (oggi la Germania): senza, nessuna riga Mint e il pannello è quello di sempre.
    val showMints = LocalShowMints.current
    val letters = remember(series.paese, showMints) { if (showMints) mintLettersFor(series.paese) else emptyList() }
    // Il pannello cambia le zecche solo con il Pro E con la funzione accesa (vedi [LocalShowMints]).
    val canEditMints = access.isPro && showMints
    var showProPrompt by remember { mutableStateOf(false) }
    val editableMints: Set<String>? = if (canEditMints) null else setOf(NO_MINT)
    // Con Pro la bozza ha TUTTE le voci; senza, solo quelle senza zecca (le altre restano intatte al salvataggio).
    val items = if (canEditMints) currentItems else currentItems.filter { it.mint == NO_MINT }
    val stateKey = "${series.stableKey}|${denomination.taglio}"
    var confirmRemove by remember(stateKey) { mutableStateOf(false) }
    val currentYear = remember { Year.now().value }
    // Gli anni da offrire e da cui aprire contano anche le voci con zecca che questo pannello non può cambiare.
    val owned = remember(stateKey) { currentItems.map { YearOption(it.anno, it.variety) }.distinct() }
    val options = remember(stateKey) { regularYearOptions(series, denomination, owned, currentYear) }
    val initial = remember(stateKey) { initialYear?.takeIf { it in options } ?: defaultYearOption(options, owned) }
    var selected by remember(stateKey) { mutableStateOf(initial) }
    // Si apre sulla zecca che ha qualcosa nell'anno iniziale: "No mint" se c'è la voce semplice (o niente),
    // altrimenti la prima lettera. Senza Pro sempre "No mint" (le altre sono bloccate).
    var selectedMint by remember(stateKey) {
        val atInitial = items.filter { YearOption(it.anno, it.variety) == initial }
        mutableStateOf(
            if (!canEditMints || atInitial.isEmpty() || atInitial.any { it.mint == NO_MINT }) NO_MINT
            else atInitial.map { it.mint }.min(),
        )
    }
    // La riga di aiuto sotto i chip (solo con la varietà EFS scelta) apre la spiegazione di cosa significa.
    var showVarietyInfo by remember(stateKey) { mutableStateOf(false) }
    val checked = remember(stateKey) {
        mutableStateMapOf<DraftKey, Boolean>().apply {
            // Nessuna spunta preselezionata: l'anno di partenza è solo un'ipotesi (il primo della serie) e una spunta
            // già messa lì non si distingue da una scelta dell'utente. Vedi la nota sul default qui sopra.
            items.forEach { this[DraftKey(it.anno, it.variety, it.mint, it.quality)] = true }
        }
    }
    val prices = remember(stateKey) {
        mutableStateMapOf<DraftKey, String>().apply {
            items.forEach { this[DraftKey(it.anno, it.variety, it.mint, it.quality)] = formatPrice(it.priceCents) }
        }
    }
    // Una data per FINITURA di ogni annata (e varietà e zecca), facoltativa e vuota di default: una finitura
    // aggiunta dopo non eredita la data di un'altra (prima era una per annata, valida per tutte le finiture).
    val dates = remember(stateKey) {
        mutableStateMapOf<DraftKey, Long?>().apply {
            items.forEach { this[DraftKey(it.anno, it.variety, it.mint, it.quality)] = it.purchasedOn }
        }
    }
    // Voci con zecca che questo pannello non vede (senza Pro): contano solo per i puntini.
    val hidden = if (canEditMints) emptyList() else currentItems.filter { it.mint != NO_MINT }
    val withData: Set<YearOption> = checked.filterValues { it }.keys.map { YearOption(it.year, it.variety) }.toSet() +
        hidden.map { YearOption(it.anno, it.variety) }
    // Puntino sul chip della zecca: finiture spuntate (o già registrate) dell'anno scelto.
    val mintsWithData: Set<String> = checked.filterValues { it }.keys
        .filter { it.year == selected.year && it.variety == selected.variety }.map { it.mint }.toSet() +
        hidden.filter { it.anno == selected.year && it.variety == selected.variety }.map { it.mint }

    if (showProPrompt) {
        MintRecordProPrompt(onDismiss = { showProPrompt = false }, onGoPro = access.onGoPro)
    }
    if (confirmRemove) {
        RemoveConfirmDialog(
            message = regularRemovalMessage(
                denomination = denomination.taglio,
                where = "$countryName · ${seriesTitle(seriesNumber)}",
                years = items.map { YearOption(it.anno, it.variety) }.distinct(),
                mints = items.map { it.mint },
            ),
            onConfirm = { confirmRemove = false; onSave(emptyList(), editableMints) },
            onDismiss = { confirmRemove = false },
        )
    }

    if (showVarietyInfo) EfsInfoDialog(taglio = denomination.taglio, onDismiss = { showVarietyInfo = false })

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = MaterialTheme.colorScheme.background,
    ) {
        Column(
            modifier = Modifier
                .padding(horizontal = 16.dp)
                .padding(bottom = 16.dp)
                .windowInsetsPadding(WindowInsets.navigationBars.union(WindowInsets.ime))
        ) {
            Text(
                text = "MY COLLECTION",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.primary,
            )
            Text(
                text = denomination.taglio,
                style = MaterialTheme.typography.titleMedium,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.padding(top = 2.dp),
            )
            Text(
                text = "$countryName · ${seriesTitle(seriesNumber)}",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 4.dp, bottom = 12.dp),
            )

            YearStrip(
                options = options,
                selected = selected,
                withData = withData,
                varietyDetail = { series.varietyFor(denomination.taglio, it.year)?.detail },
                onSelect = { selected = it },
            )

            // Solo con la varietà EFS scelta: la lettera da cercare su questo taglio, senza aprire niente. L'altezza
            // si anima, così passare da "2002" a "2002 EFS" non fa saltare le card sotto.
            Box(Modifier.fillMaxWidth().animateContentSize()) {
                if (selected.variety.isNotEmpty()) {
                    EfsHint(taglio = denomination.taglio, onClick = { showVarietyInfo = true })
                }
            }

            // La zecca (oggi la Germania) sta SOTTO l'anno e sempre visibile: l'anno è preselezionato dall'apertura,
            // farla comparire dopo un tocco avrebbe cambiato l'altezza del pannello e nascosto la funzione.
            if (letters.isNotEmpty()) {
                Spacer(Modifier.height(4.dp))
                MintStrip(
                    paese = series.paese,
                    mints = letters,
                    selected = selectedMint,
                    withData = mintsWithData,
                    locked = !access.isPro,
                    onSelect = { selectedMint = it },
                    onLockedTap = { showProPrompt = true },
                )
            }

            Spacer(Modifier.height(8.dp))
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                CoinQuality.entries.forEach { quality ->
                    val key = DraftKey(selected.year, selected.variety, selectedMint, quality)
                    FinishCard(
                        quality = quality,
                        checked = checked[key] == true,
                        price = prices[key].orEmpty(),
                        onCheckedChange = { checked[key] = it },
                        onPriceChange = { prices[key] = it },
                        date = dates[key],
                        onDateChange = { dates[key] = it },
                    )
                }
            }

            Spacer(Modifier.height(16.dp))
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.End),
                modifier = Modifier.fillMaxWidth(),
            ) {
                // Solo se il taglio ha già qualcosa di salvato (nella finestra di questa serie): vedi RemoveFromCollection.kt.
                if (items.isNotEmpty()) {
                    RemoveButton(onClick = { confirmRemove = true })
                    Spacer(Modifier.weight(1f))
                }
                TextButton(onClick = onDismiss) { Text("Cancel") }
                Button(
                    onClick = {
                        onSave(
                            checked.filterValues { it }.keys.map { key ->
                                RegularCollectionEntry(
                                    anno = key.year,
                                    quality = key.quality,
                                    priceCents = parsePriceCents(prices[key].orEmpty()),
                                    variety = key.variety,
                                    purchasedOn = dates[key],
                                    mint = key.mint,
                                )
                            },
                            editableMints,
                        )
                    },
                ) { Text("Save") }
            }
        }
    }
}

/**
 * Scelta dell'anno: una STRISCIA di chip scorrevole di lato, direttamente nel pannello (prima una pillola
 * "Year" che apriva una finestra con una griglia a 5 colonne: una finestra sopra un pannello, un tocco in
 * più per cambiare anno e celle piccole; scelta dell'utente dopo quattro mockup). Stessi chip della card
 * COLLECTION del dettaglio (`FilterChip`: rettangoli con angoli morbidi, scelto in lilla). Un puntino
 * verde davanti all'anno dice che ha già finiture spuntate (la bozza non salvata conta, come nella
 * griglia). La varietà EFS del 2002 greco è un chip a parte subito dopo il 2002 ("2002 EFS"): sono due
 * monete diverse. La striscia si centra sull'anno scelto e arriva fino ai bordi del pannello (i chip
 * escono di lato invece di fermarsi sul margine), così si capisce che scorre.
 */
@Composable
private fun YearStrip(
    options: List<YearOption>,
    selected: YearOption,
    withData: Set<YearOption>,
    varietyDetail: (YearOption) -> String?,
    onSelect: (YearOption) -> Unit,
) {
    val colors = MaterialTheme.colorScheme
    val listState = rememberLazyListState()
    val selectedIndex = options.indexOf(selected).coerceAtLeast(0)
    val density = LocalDensity.current
    // Centra l'anno scelto quando cambia (e alla prima apertura, appena il layout ha una larghezza).
    LaunchedEffect(selected) {
        snapshotFlow { listState.layoutInfo.viewportEndOffset }.first { it > 0 }
        val info = listState.layoutInfo
        val viewport = info.viewportEndOffset - info.viewportStartOffset
        val itemWidth = info.visibleItemsInfo.firstOrNull { it.index == selectedIndex }?.size
            ?: with(density) { 72.dp.roundToPx() }
        listState.animateScrollToItem(selectedIndex, scrollOffset = -(viewport / 2 - itemWidth / 2))
    }
    Text(text = "Year", style = MaterialTheme.typography.bodyMedium, color = colors.onSurfaceVariant)
    LazyRow(
        state = listState,
        contentPadding = PaddingValues(horizontal = StripEdge),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 4.dp)
            // Esce di StripEdge per lato: il pannello ha 16 dp di margine e i chip devono arrivare al bordo.
            .layout { measurable, constraints ->
                val edge = StripEdge.roundToPx()
                val width = constraints.maxWidth + 2 * edge
                val placeable = measurable.measure(constraints.copy(minWidth = width, maxWidth = width))
                layout(constraints.maxWidth, placeable.height) { placeable.place(-edge, 0) }
            },
    ) {
        items(options, key = { "${it.year}|${it.variety}" }) { option ->
            val hasData = option in withData
            val isSelected = option == selected
            val description = varietyDetail(option)?.takeIf { option.variety.isNotEmpty() }
            FilterChip(
                selected = isSelected,
                onClick = { onSelect(option) },
                label = { Text(option.chipLabel()) },
                leadingIcon = if (hasData) {
                    { Box(Modifier.size(8.dp).background(colors.primary, CircleShape)) }
                } else null,
                modifier = Modifier.semantics {
                    contentDescription = buildString {
                        append(option.label)
                        description?.let { append(", $it") }
                        if (hasData) append(", marked as owned")
                        if (isSelected) append(", selected")
                    }
                },
            )
        }
    }
}

/** Margine laterale del pannello che la striscia dei chip attraversa. */
private val StripEdge = 16.dp
