package com.michele.eurocoins.ui.components

import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
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
