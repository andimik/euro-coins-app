package com.michele.eurocoins.ui.settings

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.Spacer
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.michele.eurocoins.ui.backup.SettingsCard
import com.michele.eurocoins.ui.browse.BrowseMode
import com.michele.eurocoins.ui.components.InfoNote
import com.michele.eurocoins.ui.regular.RegularBrowseMode
import com.michele.eurocoins.ui.theme.appBarColors

/** Etichetta della scheda iniziale di Commemorative, come nel selettore e nel riepilogo della riga. */
internal fun BrowseMode.settingsLabel(): String = when (this) {
    BrowseMode.YEARS -> "Years"
    BrowseMode.COUNTRIES -> "Countries"
    BrowseMode.ALL -> "All"
}

/** Etichetta della scheda iniziale di Regular Issues ("Values" è la scheda Denominations, vedi il selettore). */
internal fun RegularBrowseMode.settingsLabel(): String = when (this) {
    RegularBrowseMode.COUNTRIES -> "Countries"
    RegularBrowseMode.DENOMINATIONS -> "Values"
    RegularBrowseMode.ALL -> "All"
}

/**
 * Titolo della riga che apre la sottopagina dei cataloghi nelle Impostazioni (variante A del mockup del
 * 2026-10-13): una frase che dice COSA c'è dentro, uguale per Free e Pro (le zecche si nominano anche se sono
 * bloccate: da Free è l'unico posto dove si scopre che esistono). Prima il titolo era "Catalog options", che
 * ripeteva la sezione "Catalogs", e il riepilogo elencava le schede iniziali ("Commemorative: Countries / Regular
 * Issues: Countries"): prolisso e poco utile, la scheda iniziale si cambia una volta sola.
 * Tra "German" e "mints" c'è uno spazio NON separabile (U+00A0): sul telefono la frase va a capo dopo "and" e
 * senza, a capo dopo "German", "mints" restava da solo sulla seconda riga.
 */
internal const val CATALOGS_ROW_TITLE = "Microstates, start tab and German mints"

/**
 * Riga di avviso sotto il titolo: SOLO ciò che è nascosto (microstati, zecche tedesche), perché cambia ciò che si
 * vede e i totali. `null` se non è nascosto niente: allora la riga è il solo titolo.
 */
internal fun catalogsHiddenNote(
    hideCommemorative: Boolean,
    hideRegular: Boolean,
    germanMintsHidden: Boolean,
): String? {
    val microstates = when {
        hideCommemorative && hideRegular -> "Microstates hidden in both"
        hideCommemorative -> "Microstates hidden in Commemorative"
        hideRegular -> "Microstates hidden in Regular Issues"
        else -> null
    }
    return listOfNotNull(microstates, "German mints hidden".takeIf { germanMintsHidden })
        .takeIf { it.isNotEmpty() }?.joinToString("\n")
}

/**
 * Sottopagina "Catalogs" delle Impostazioni: per ciascun catalogo i microstati nascosti e la scheda che si apre per
 * prima. Spostata fuori dalla schermata principale (2026-10-11) perché sono cinque controlli che si toccano una volta
 * sola e allungavano la pagina di una schermata; le card sono quelle di prima ([CatalogCard]).
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CatalogSettingsScreen(
    settingsViewModel: SettingsViewModel,
    /** "German mints" è del Pro: senza, l'interruttore è bloccato. */
    isPro: Boolean,
    onBack: () -> Unit,
) {
    val showMints by settingsViewModel.showMints.collectAsState()
    val hideCommemorativeMicrostates by settingsViewModel.hideCommemorativeMicrostates.collectAsState()
    val hideRegularMicrostates by settingsViewModel.hideRegularMicrostates.collectAsState()
    val defaultTab by settingsViewModel.defaultTab.collectAsState()
    val defaultRegularTab by settingsViewModel.defaultRegularTab.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                colors = appBarColors(),
                title = { Text("Catalogs") },
                navigationIcon = {
                    IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back") }
                },
            )
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 8.dp),
        ) {
            // Una sezione per catalogo, ciascuna con le sue due impostazioni (interruttore, poi selettore):
            // i microstati si nascondono in modo indipendente e la scheda iniziale è quella del catalogo.
            SectionHeader("Commemorative", first = true)
            CatalogCard(
                catalogName = "Commemorative",
                hideMicrostates = hideCommemorativeMicrostates,
                onHideMicrostatesChange = settingsViewModel::setHideCommemorativeMicrostates,
                options = BrowseMode.entries,
                selected = defaultTab,
                label = { it.settingsLabel() },
                onSelect = settingsViewModel::setDefaultTab,
            )

            SectionHeader("Regular Issues")
            CatalogCard(
                catalogName = "Regular Issues",
                hideMicrostates = hideRegularMicrostates,
                onHideMicrostatesChange = settingsViewModel::setHideRegularMicrostates,
                options = RegularBrowseMode.entries,
                selected = defaultRegularTab,
                label = { it.settingsLabel() },
                onSelect = settingsViewModel::setDefaultRegularTab,
            )

            // Le zecche tedesche valgono per ENTRAMBI i cataloghi, quindi una card a sé e non dentro una delle due.
            // Sta qui e non in "Collection" (2026-10-12): come "Hide microstates" mostra o nasconde una parte dei
            // cataloghi, non è un comportamento come Quick add. Opzione del Pro: senza, bloccata.
            SectionHeader("German mints")
            SettingsCard {
                SwitchRow(
                    title = "Show mints",
                    subtitle = "Mint letters on German coins",
                    // Senza Pro il valore effettivo è sempre "acceso" (la funzione si vede, bloccata, con gli inviti).
                    checked = showMints || !isPro,
                    onCheckedChange = settingsViewModel::setShowMints,
                    enabled = isPro,
                )
                InfoNote(
                    text = if (isPro) {
                        "Record which mint (A, D, F, G, J) each coin comes from and see mintages by mint. " +
                            "Hiding it keeps what you saved."
                    } else {
                        "Record which mint (A, D, F, G, J) each coin comes from and see mintages by mint. " +
                            "Go Pro to unlock."
                    },
                    modifier = Modifier.padding(top = 8.dp),
                )
            }
            Spacer(Modifier.height(24.dp))
        }
    }
}
