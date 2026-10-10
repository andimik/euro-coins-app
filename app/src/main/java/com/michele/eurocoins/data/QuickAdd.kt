package com.michele.eurocoins.data

/**
 * "Anno non indicato" delle Regular Issues: la voce creata dal tocco semplice (Quick add) su una riga,
 * dove l'utente ha detto solo "ce l'ho" senza dire di che anno. Sta in [RegularCollectionItem.anno] come
 * `0` (nessuna annata vera lo è). A differenza delle annate, che appartengono alla serie di ORIGINE del
 * taglio e a una sua finestra di tempo, una voce senza anno appartiene alla riga GUARDATA: la chiave
 * della serie è quella guardata ([SeriesDenomination.viewed]), perché un taglio rimasto invariato è la
 * stessa moneta in più serie e senza anno non c'è modo di dire in quale finestra cada.
 */
const val NO_YEAR = 0

/** Cosa fa un tocco sulla casella con Quick add acceso. */
enum class QuickTap {
    /** Niente in collezione: si aggiunge la voce più semplice. */
    ADD,

    /** Solo la voce semplice: si toglie, senza perdere nulla. */
    REMOVE,

    /** Ci sono dettagli (BU, Proof, prezzo, data, anno): si apre il pannello invece di cancellarli. */
    OPEN_FORM,
}

/**
 * Tocco su una commemorativa: nulla → si aggiunge Standard; solo Standard senza prezzo né data → si toglie;
 * altrimenti si apre il pannello, perché togliere cancellerebbe in silenzio ciò che l'utente ha scritto.
 */
fun quickTapFor(items: List<CollectionItem>): QuickTap = when {
    items.isEmpty() -> QuickTap.ADD
    items.all { it.quality == CoinQuality.STANDARD && it.priceCents == null && it.purchasedOn == null } -> QuickTap.REMOVE
    else -> QuickTap.OPEN_FORM
}

/**
 * Come [quickTapFor] per una riga Regular: [items] sono quelle della riga ([rowItems]). La voce semplice è
 * Standard senza anno, prezzo né data; un anno vero, la varietà EFS, un'altra finitura, un prezzo o una data
 * sono dettagli.
 */
fun quickTapForRegular(items: List<RegularCollectionItem>): QuickTap = when {
    items.isEmpty() -> QuickTap.ADD
    items.all {
        it.anno == NO_YEAR && it.quality == CoinQuality.STANDARD && it.variety.isEmpty() &&
            it.priceCents == null && it.purchasedOn == null
    } -> QuickTap.REMOVE
    else -> QuickTap.OPEN_FORM
}

/**
 * Le voci in collezione di UNA riga (un taglio in una serie guardata): le annate della serie di origine
 * dentro la finestra della riga, più le voci senza anno della serie guardata. [byKey] è la collezione
 * raggruppata per (serie, taglio).
 */
fun SeriesDenomination.rowItemsFrom(byKey: Map<Pair<String, String>, List<RegularCollectionItem>>): List<RegularCollectionItem> =
    byKey[series.stableKey to image.taglio].orEmpty().filter { it.anno != NO_YEAR && contains(it.anno) } +
        byKey[viewed.stableKey to image.taglio].orEmpty().filter { it.anno == NO_YEAR }

/** Come [rowItemsFrom], partendo dall'elenco intero della collezione. */
fun SeriesDenomination.rowItems(collection: List<RegularCollectionItem>): List<RegularCollectionItem> {
    val taglio = image.taglio
    return collection.filter {
        it.taglio == taglio &&
            if (it.anno == NO_YEAR) it.seriesKey == viewed.stableKey else it.seriesKey == series.stableKey && contains(it.anno)
    }
}

/** Annate vere distinte tra [items] (la voce senza anno non è un anno: "Owned", non "0 years owned"). */
fun ownedYearsOf(items: List<RegularCollectionItem>): Int = items.filter { it.anno != NO_YEAR }.map { it.anno }.distinct().size
