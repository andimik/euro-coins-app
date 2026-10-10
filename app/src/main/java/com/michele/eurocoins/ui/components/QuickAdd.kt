package com.michele.eurocoins.ui.components

import androidx.compose.runtime.compositionLocalOf

/**
 * Quick add acceso (Impostazioni, sezione Collection): il tocco sulla casella di una moneta aggiunge o toglie la
 * voce più semplice senza aprire il pannello. Fornito da `MainActivity`. Una moneta con dettagli apre comunque il
 * pannello (`quickTapFor`, `quickTapForRegular`).
 */
val LocalQuickAdd = compositionLocalOf { false }
