package com.michele.eurocoins.data.backup

/**
 * Cosa contiene un backup, per il dialog di ripristino: monete distinte per catalogo, contate come il Reset (una
 * commemorativa con più finiture conta una volta, un taglio Regular con più annate conta una volta).
 *
 * [regular] è `null` se il backup non porta Regular Issues (un file della versione 1): ripristinarlo lascia la
 * collezione Regular com'è (vedi [BackupFile.includesRegularIssues]), e mostrare "0" farebbe credere che venga svuotata.
 */
data class BackupContents(val commemorative: Int, val regular: Int?) {
    val total: Int get() = commemorative + (regular ?: 0)

    /** "93 Commemorative · 184 Regular Issues": una riga, le sezioni vuote o assenti saltate; "Empty" se non c'è niente. */
    fun summaryLine(): String =
        listOfNotNull(
            commemorative.takeIf { it > 0 }?.let { "$it Commemorative" },
            regular?.takeIf { it > 0 }?.let { "$it Regular Issues" },
        ).joinToString(" · ").ifEmpty { "Empty" }
}

/** Le monete che un ripristino metterebbe in collezione (le voci con una qualità non riconosciuta si saltano, come nel ripristino). */
fun BackupFile.contents(): BackupContents = BackupContents(
    commemorative = items.mapNotNull { it.toCollectionItem() }.map { it.coinKey }.distinct().size,
    regular = if (includesRegularIssues) {
        regularItems.mapNotNull { it.toCollectionItem() }.map { it.seriesKey to it.taglio }.distinct().size
    } else {
        null
    },
)

/** Una copia su Drive: quando è stata salvata (ISO-8601, come la dà Drive) e cosa contiene. */
data class RestoreCopy(val modifiedTime: String?, val contents: BackupContents)

/** Le due copie ripristinabili: l'ultimo backup e, se esiste, la versione precedente (la copia di almeno un giorno prima). */
data class RestorePreview(val latest: RestoreCopy, val previous: RestoreCopy?)
