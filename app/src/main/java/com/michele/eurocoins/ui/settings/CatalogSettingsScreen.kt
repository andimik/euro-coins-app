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
import com.michele.eurocoins.ui.browse.BrowseMode
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
 * Riepilogo della riga "Catalog options" nelle Impostazioni: dice come sono impostati i cataloghi senza aprire la
 * sottopagina. Una riga per catalogo con la scheda iniziale (due righe: "Commemorative: Countries" / "Regular Issues: Countries", perché in una sola "Regular Issues" andava a capo a metà); una terza solo se si nascondono dei microstati, perché cambia
 * ciò che si vede (e i totali).
 */
internal fun catalogsSummary(
    hideCommemorative: Boolean,
    hideRegular: Boolean,
    defaultTab: BrowseMode,
    defaultRegularTab: RegularBrowseMode,
): String {
    val tabs = "Commemorative: ${defaultTab.settingsLabel()}\nRegular Issues: ${defaultRegularTab.settingsLabel()}"
    val hidden = when {
        hideCommemorative && hideRegular -> "Microstates hidden in both"
        hideCommemorative -> "Microstates hidden in Commemorative"
        hideRegular -> "Microstates hidden in Regular Issues"
        else -> null
    }
    return if (hidden == null) tabs else "$tabs\n$hidden"
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
    onBack: () -> Unit,
) {
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
            Spacer(Modifier.height(24.dp))
        }
    }
}
