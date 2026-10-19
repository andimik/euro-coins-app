package com.michele.eurocoins.ui.settings

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp

/**
 * Primo avvio: la scheda di benvenuto sulla Home e il breve percorso nelle Impostazioni che evidenzia prima
 * l'accesso con Google (backup e ripristino) e poi Quick add. Scelto dopo mockup (2026-10-19): niente schermate
 * finte da mantenere allineate a quelle vere, l'utente vede i controlli reali nel posto dove li ritroverà.
 *
 * Lo stato che si salva è solo "da fare / fatto": il passo in cui si è arrivati vive nella schermata. Toccare
 * "Set up" o "Later" lo chiude subito, quindi se il processo muore a metà percorso non ricompare (meglio che
 * insistere con chi ha già mostrato di voler andare avanti).
 */
enum class SetupTour { PENDING, DONE }

/**
 * Stato iniziale del percorso. Un valore già salvato vince. Senza valore, un telefono che ha già seminato il
 * catalogo ([hasPriorData], l'hash dell'asset in SharedPreferences `dataset`) è un'installazione esistente (te, i
 * tester del test interno, chiunque aggiorni l'app): il percorso non gli si mostra. Va calcolato PRIMA del primo
 * seeding, e salvato subito, altrimenti alla seconda apertura anche un'installazione nuova risulterebbe "esistente".
 */
internal fun resolveSetupTour(stored: String?, hasPriorData: Boolean): SetupTour =
    SetupTour.entries.firstOrNull { it.name == stored } ?: if (hasPriorData) SetupTour.DONE else SetupTour.PENDING

/** Passo del percorso nelle Impostazioni; [OFF] = nessun percorso. */
internal const val TOUR_OFF = -1
internal const val TOUR_ACCOUNT = 0
internal const val TOUR_COLLECTION = 1
internal const val TOUR_STEPS = 2

/** Come appare una zona delle Impostazioni durante il percorso. */
internal enum class TourSpot { NONE, LIT, DIM }

/** [lit] = il passo che evidenzia questa zona; le altre zone si attenuano finché il percorso è acceso. */
internal fun tourSpotFor(step: Int, lit: Int): TourSpot = when {
    step == TOUR_OFF -> TourSpot.NONE
    step == lit -> TourSpot.LIT
    else -> TourSpot.DIM
}

/** L'intestazione di una zona evidenziata resta com'è: attenuata e cornice sono per la card sotto. */
internal fun headerSpotFor(spot: TourSpot): TourSpot = if (spot == TourSpot.LIT) TourSpot.NONE else spot

/**
 * Zona delle Impostazioni durante il percorso. Evidenziata = bordo del colore primario con un alone, disegnati
 * FUORI dai limiti (4 dp) così il contenuto non si sposta; attenuata = trasparenza e tocchi assorbiti (non si
 * esce dal percorso per sbaglio aprendo un'altra riga). La struttura non cambia tra i tre stati: il contenuto non
 * perde il proprio stato quando si passa da un passo all'altro. L'assorbimento è un `clickable` e non un
 * `pointerInput` che consuma il tocco: quest'ultimo impedirebbe di far scorrere la pagina partendo da lì.
 */
@Composable
internal fun TourSpotBox(spot: TourSpot, modifier: Modifier = Modifier, content: @Composable () -> Unit) {
    val dimAlpha by animateFloatAsState(if (spot == TourSpot.DIM) DIM_ALPHA else 1f, label = "tourDim")
    val frame by animateFloatAsState(if (spot == TourSpot.LIT) 1f else 0f, label = "tourFrame")
    val primary = MaterialTheme.colorScheme.primary
    Box(
        modifier = modifier
            .alpha(dimAlpha)
            .drawBehind {
                if (frame <= 0f) return@drawBehind
                val grow = 4.dp.toPx()
                val topLeft = Offset(-grow, -grow)
                val size = Size(this.size.width + 2 * grow, this.size.height + 2 * grow)
                val corner = CornerRadius(18.dp.toPx())
                drawRoundRect(primary.copy(alpha = 0.22f * frame), topLeft, size, corner, style = Stroke(width = 8.dp.toPx()))
                drawRoundRect(primary.copy(alpha = frame), topLeft, size, corner, style = Stroke(width = 2.5.dp.toPx()))
            },
    ) {
        content()
        if (spot == TourSpot.DIM) {
            Box(
                Modifier
                    .matchParentSize()
                    .clickable(interactionSource = remember { MutableInteractionSource() }, indication = null) {},
            )
        }
    }
}

private const val DIM_ALPHA = 0.38f

/**
 * Scheda di un passo, ancorata in basso sopra la barra di navigazione: "N of 2", la frase, una riga facoltativa e
 * due azioni. Sinistra = uscire o tornare indietro, destra = andare avanti (pulsante pieno, l'unico).
 */
@Composable
internal fun SetupTourCard(
    step: Int,
    text: String,
    hint: String?,
    leftLabel: String,
    onLeft: () -> Unit,
    rightLabel: String,
    onRight: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(12.dp),
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
        shadowElevation = 8.dp,
    ) {
        Column(modifier = Modifier.padding(start = 18.dp, end = 12.dp, top = 14.dp, bottom = 8.dp)) {
            Text(
                "${step + 1} of $TOUR_STEPS",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(Modifier.height(4.dp))
            Text(text, style = MaterialTheme.typography.bodyLarge)
            if (hint != null) {
                Spacer(Modifier.height(4.dp))
                Text(hint, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Spacer(Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = androidx.compose.ui.Alignment.CenterVertically,
            ) {
                TextButton(onClick = onLeft) { Text(leftLabel) }
                Button(onClick = onRight) { Text(rightLabel) }
            }
        }
    }
}

/**
 * Scheda di benvenuto sulla Home. Chiuderla (trascinando o toccando fuori) vale "Later": niente percorso e niente
 * insistenza, le Impostazioni restano dove sono.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WelcomeSheet(onSetUp: () -> Unit, onLater: () -> Unit) {
    ModalBottomSheet(
        onDismissRequest = onLater,
        containerColor = MaterialTheme.colorScheme.surface,
    ) {
        Column(modifier = Modifier.padding(start = 20.dp, end = 20.dp, bottom = 16.dp)) {
            Text("Welcome to Euro Coins", style = MaterialTheme.typography.titleLarge.copy(fontFamily = androidx.compose.ui.text.font.FontFamily.Default))
            Spacer(Modifier.height(6.dp))
            Text(
                "Two quick settings and you're ready to start your collection.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(Modifier.height(20.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = androidx.compose.ui.Alignment.CenterVertically,
            ) {
                TextButton(onClick = onLater) { Text("Later") }
                Button(onClick = onSetUp) { Text("Set up") }
            }
        }
    }
}

/** Testi del passo Quick add: dicono cosa succede nello stato in cui l'interruttore è adesso. */
internal fun quickAddTourText(quickAdd: Boolean): String =
    if (quickAdd) "Quick add is on: one tap marks a coin as owned."
    else "Quick add is off: tapping a coin opens finish, year, price and date."

internal const val TOUR_ACCOUNT_TEXT = "Sign in to back up your collection and restore it on a new phone."
internal const val TOUR_QUICK_ADD_HINT = "You can change this any time in Settings."

/** Altezza lasciata in fondo alle Impostazioni durante il percorso: più della scheda del passo (circa 190 dp con il testo di Quick add). */
internal val TourCardClearance = 220.dp
