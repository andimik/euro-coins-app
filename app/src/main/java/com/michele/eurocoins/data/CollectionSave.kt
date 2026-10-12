package com.michele.eurocoins.data

/**
 * Cosa scrivere in `collection_items` salvando una commemorativa: logica pura, separata dal database per
 * poterla provare.
 *
 * [editableMints] è l'insieme delle zecche che il pannello può cambiare (`null` = tutte). Le voci di una zecca
 * fuori da quell'insieme restano com'erano e le [entries] che la riguardano si ignorano. Serve a un pannello che
 * vede solo una parte delle voci: senza Pro i pannelli modificano solo "No mint", e un salvataggio che SOSTITUISCE
 * tutte le voci della moneta cancellerebbe quelle con zecca che l'utente aveva registrato (e un Pro scaduto non
 * deve far perdere dati).
 *
 * La data di aggiunta delle voci già esistenti (stessa zecca e finitura) si conserva.
 */
fun planCommemorativeSave(
    existing: List<CollectionItem>,
    entries: Map<MintFinish, FinishEntry>,
    editableMints: Set<String>?,
    coinKey: String,
    anno: Int,
    paese: String,
    tema: String,
    now: Long,
): List<CollectionItem> {
    fun editable(mint: String) = editableMints == null || mint in editableMints
    val before = existing.associateBy { MintFinish(it.mint, it.quality) }
    val kept = existing.filterNot { editable(it.mint) }
    val written = entries.filterKeys { editable(it.mint) }.map { (key, entry) ->
        CollectionItem(
            coinKey = coinKey,
            quality = key.quality,
            mint = key.mint,
            priceCents = entry.priceCents,
            purchasedOn = entry.purchasedOn,
            anno = anno,
            paese = paese,
            tema = tema,
            addedAt = before[key]?.addedAt ?: now,
        )
    }
    return kept + written
}

/** Una scrittura di [RegularIssueRepository.saveCollection]: tutte le voci di (serie, taglio) dopo il salvataggio. */
data class RegularSaveWrite(val seriesKey: String, val items: List<RegularCollectionItem>)

/**
 * Cosa scrivere in `regular_collection_items` salvando un taglio, come [planCommemorativeSave] ma per le Regular
 * (logica che stava dentro il repository, spostata qui per provarla).
 *
 * - [inWindow]: gli anni che il pannello vede e può cambiare (la finestra della serie guardata, vedi
 *   `denominationsOf`); `null` = senza finestra, si sostituisce tutto. Le voci FUORI finestra restano.
 * - Le voci SENZA ANNO ([NO_YEAR]) appartengono alla serie GUARDATA ([viewedKey]), non a quella di origine
 *   ([seriesKey]): per un taglio ereditato stanno sotto un'altra chiave e si scrivono a parte.
 * - [editableMints]: come in [planCommemorativeSave].
 *
 * @param atOrigin le voci già salvate per (seriesKey, taglio).
 * @param atViewed le voci già salvate per (viewedKey, taglio); vuota se le due chiavi coincidono.
 */
fun planRegularSave(
    seriesKey: String,
    viewedKey: String,
    taglio: String,
    paese: String,
    entries: List<RegularCollectionEntry>,
    atOrigin: List<RegularCollectionItem>,
    atViewed: List<RegularCollectionItem>,
    inWindow: ((Int) -> Boolean)?,
    editableMints: Set<String>?,
    now: Long,
): List<RegularSaveWrite> {
    fun editable(mint: String) = editableMints == null || mint in editableMints
    val sameKey = viewedKey == seriesKey
    val mine = entries.filter { editable(it.mint) }
    val noYear = mine.filter { it.anno == NO_YEAR }
    val withYear = mine.filter { it.anno != NO_YEAR }

    fun replacedAtOrigin(item: RegularCollectionItem): Boolean = when {
        inWindow == null -> true
        item.anno == NO_YEAR -> sameKey // quelle senza anno si sostituiscono solo se sono della riga guardata
        else -> inWindow(item.anno)
    }
    val kept = atOrigin.filterNot { replacedAtOrigin(it) && editable(it.mint) }
    val writes = mutableListOf(
        RegularSaveWrite(
            seriesKey,
            kept + regularItems(seriesKey, taglio, paese, if (sameKey) withYear + noYear else withYear, atOrigin, now),
        ),
    )
    if (!sameKey && (noYear.isNotEmpty() || atViewed.any { it.anno == NO_YEAR && editable(it.mint) })) {
        writes += RegularSaveWrite(
            viewedKey,
            atViewed.filterNot { it.anno == NO_YEAR && editable(it.mint) } +
                regularItems(viewedKey, taglio, paese, noYear, atViewed, now),
        )
    }
    return writes
}

/** Le voci da scrivere per [entries], con la data di aggiunta di quelle già in [existing] (stessa anno, qualità, varietà, zecca). */
private fun regularItems(
    seriesKey: String,
    taglio: String,
    paese: String,
    entries: List<RegularCollectionEntry>,
    existing: List<RegularCollectionItem>,
    now: Long,
): List<RegularCollectionItem> {
    val before = existing.associateBy { listOf(it.anno, it.quality, it.variety, it.mint) }
    return entries.map { entry ->
        RegularCollectionItem(
            seriesKey = seriesKey,
            taglio = taglio,
            anno = entry.anno,
            quality = entry.quality,
            variety = entry.variety,
            mint = entry.mint,
            priceCents = entry.priceCents,
            purchasedOn = entry.purchasedOn,
            paese = paese,
            addedAt = before[listOf(entry.anno, entry.quality, entry.variety, entry.mint)]?.addedAt ?: now,
        )
    }
}

/**
 * Alla PRIMA apertura di una moneta ("Standard" senza zecca già spuntata, il caso di quasi tutte: un tocco e Save)
 * quella spunta è una preselezione, non una scelta dell'utente. Se l'utente passa a una lettera e spunta lì le sue
 * finiture, la preselezione deve sparire da sola: altrimenti registrare solo "Standard · D" salverebbe anche la voce
 * senza zecca, un doppione (2026-10-12, visto sul telefono). La preselezione resta finché l'utente non tocca una
 * card di "No mint" ([noMintTouched]) e finché non ha spuntato niente in una zecca con lettera.
 */
fun isSoftDefaultActive(firstOpen: Boolean, noMintTouched: Boolean, checkedFinishes: Collection<MintFinish>): Boolean =
    firstOpen && !noMintTouched && checkedFinishes.none { it.mint != NO_MINT }
