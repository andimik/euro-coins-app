package com.michele.eurocoins.ui.components

import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.Velocity

/**
 * Tiene lo scorrimento di una lista DENTRO la lista, dentro un `ModalBottomSheet`.
 *
 * Il pannello di Material ascolta lo scorrimento in eccesso dei figli: quando la lista arriva in cima (o in fondo,
 * o a fine di un lancio) il resto del gesto passa al pannello, che si trascina giù e si chiude. In una tabella di
 * 25 righe in un pannello alto 360 dp succedeva di continuo ("si blocca ed è facile far tornare tutto il pannello
 * giù", 2026-10-11): basta risalire la lista col dito per portarsi dietro il pannello.
 *
 * Questo modificatore sta tra la lista e il pannello e consuma tutto ciò che la lista non ha usato, sia come
 * scorrimento sia come lancio. Il pannello si chiude ancora trascinando l'intestazione o la maniglia (fuori
 * dalla lista), toccando fuori o con "Close". Va messo SULLA lista, non sul pannello.
 */
fun Modifier.keepScrollInside(): Modifier = nestedScroll(ConsumeLeftoverScroll)

private object ConsumeLeftoverScroll : NestedScrollConnection {
    override fun onPostScroll(consumed: Offset, available: Offset, source: NestedScrollSource): Offset = available

    override suspend fun onPostFling(consumed: Velocity, available: Velocity): Velocity = available
}

/**
 * Impedisce che un trascinamento verticale su QUESTA zona trascini il `ModalBottomSheet` che la contiene.
 *
 * Senza, ogni punto del pannello che non è una lista che scorre è una maniglia: nel pannello delle tirature per
 * anno la riga delle intestazioni ("Year", "A D F G J") e il selettore delle qualità chiudevano tutto con un
 * dito appoggiato male (2026-10-11). Messo su un contenitore dal titolo in giù, lascia come zona da cui si chiude
 * trascinando solo la maniglia con titolo e sottotitolo; "Close" e il tocco fuori restano. I tocchi (selettore,
 * pulsanti) e le liste che scorrono dentro non cambiano: la lista usa il trascinamento per prima.
 */
fun Modifier.protectFromSheetDrag(): Modifier = pointerInput(Unit) {
    detectVerticalDragGestures(onVerticalDrag = { change, _ -> change.consume() })
}
