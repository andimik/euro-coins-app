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

    /**
     * Ci sono dettagli (BU, Proof, prezzo, data, anno): si chiede conferma prima di toglierli, con il dialog "Remove"
     * dei pannelli. Il tocco NON apre il pannello: la casella vuol dire sempre "ce l'ho / non ce l'ho", e un gesto
     * che a volte alterna e a volte apre un pannello (secondo uno stato che non si vede) era incoerente.
     */
    CONFIRM_REMOVE,
}

/**
 * Tocco su una commemorativa: nulla → si aggiunge Standard; solo Standard senza zecca, prezzo né data → si
 * toglie; altrimenti si chiede conferma, perché togliere cancellerebbe ciò che l'utente ha scritto (una zecca
 * registrata è un dettaglio come il prezzo).
 */
fun quickTapFor(items: List<CollectionItem>): QuickTap = when {
    items.isEmpty() -> QuickTap.ADD
    items.all {
        it.quality == CoinQuality.STANDARD && it.mint == NO_MINT && it.priceCents == null && it.purchasedOn == null
    } -> QuickTap.REMOVE
    else -> QuickTap.CONFIRM_REMOVE
}

/**
 * Come [quickTapFor] per una riga Regular: [items] sono quelle della riga ([rowItems]). La voce semplice è
 * Standard senza anno, zecca, prezzo né data; un anno vero, la varietà EFS, una zecca, un'altra finitura, un
 * prezzo o una data sono dettagli.
 */
fun quickTapForRegular(items: List<RegularCollectionItem>): QuickTap = when {
    items.isEmpty() -> QuickTap.ADD
    items.all {
        it.anno == NO_YEAR && it.quality == CoinQuality.STANDARD && it.variety.isEmpty() && it.mint == NO_MINT &&
            it.priceCents == null && it.purchasedOn == null
    } -> QuickTap.REMOVE
    else -> QuickTap.CONFIRM_REMOVE
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
