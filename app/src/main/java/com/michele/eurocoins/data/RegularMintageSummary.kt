package com.michele.eurocoins.data

import java.text.NumberFormat
import java.util.Locale

/**
 * Tiratura riassunta di una qualità nella card compatta: total e caption null se non c'è alcun dato.
 * [total] è la somma su tutti gli anni con un dato (o il valore dell'anno, se uno solo): vedi [formatCardMintage].
 */
data class MintageSummary(val total: Long?, val caption: String?)

/**
 * Somma le tirature di [quality] su tutti gli anni. Con una sola annata la didascalia è l'anno
 * stesso (niente da sommare, coerente con le commemorative che mostrano solo l'anno); con più
 * annate è "all years" — senza, la somma si legge come se fosse la tiratura di un solo anno,
 * un ordine di grandezza fuorviante per chi guarda la card.
 *
 * **"all years" solo se quella qualità ha un dato in OGNI anno in cui il taglio ne ha uno** (per
 * qualunque qualità): Numista spesso non ha lo standard di un anno (o la qualità non è mai stata
 * coniata quell'anno: BU/Proof esistono solo in alcuni), e una somma su 12 anni su 25 spacciata
 * per "all years" sarebbe un totale sbagliato scritto come esatto. Con buchi la didascalia
 * diventa "12 of 25 years" — il numero resta quello che si può sommare, ma dichiara su quanti
 * anni poggia.
 */
fun summarizeMintages(tirature: List<RegularIssueMintage>, quality: CoinQuality): MintageSummary {
    val forQuality = tirature.filter { it.quality == quality }
    return when (forQuality.size) {
        0 -> MintageSummary(null, null)
        1 -> MintageSummary(forQuality[0].tiratura, forQuality[0].anno.toString())
        else -> {
            val yearsWithData = forQuality.map { it.anno }.distinct().size
            val allYears = tirature.map { it.anno }.distinct().size
            val caption = if (yearsWithData == allYears) "all years" else "$yearsWithData of $allYears years"
            MintageSummary(forQuality.sumOf { it.tiratura }, caption)
        }
    }
}

/**
 * Righe della tabella "by year": un anno per riga (ordine crescente), valore per qualità o
 * assente se quella qualità non ha un dato per quell'anno. Include solo gli anni per cui esiste
 * almeno una tiratura: una serie uscita nel 2009 e terminata nel 2015 non genera righe fuori da
 * quell'intervallo, perché semplicemente non c'è nessuna tiratura da mostrare per quegli anni —
 * non serve un campo separato "anno di inizio/fine serie" (che tra l'altro non è affidabile nel
 * dataset, vedi `RegularIssueSeries.anniCitati`).
 */
fun groupMintagesByYear(tirature: List<RegularIssueMintage>): List<Pair<Int, Map<CoinQuality, Long>>> =
    tirature.groupBy { it.anno }
        .toSortedMap()
        .map { (year, entries) -> year to entries.associate { it.quality to it.tiratura } }

/**
 * Valore di una colonna della card MINTAGES. [compact] = formato compatto a UNA cifra decimale ("14.2 B", "10.1 M",
 * "19.0 k", come le tabelle per zecca), altrimenti il numero esatto con le virgole ("12,345,678").
 *
 * La card è compatta quando il taglio ha almeno due anni (2026-10-13, scelta del proprietario): c'è "View by year"
 * con i numeri esatti, e le tre colonne non si mescolano più tra "≈ 14.2 B", "≈ 10.1 M" e "19,000" (somme di
 * decine di anni accanto a un anno solo). Con un anno solo resta esatto: non c'è nessun altro posto dove leggerlo e
 * tutti i numeri sono dello stesso ordine di grandezza. Prima le somme erano a 3 cifre significative con "≈"; ora
 * niente "≈" (in un formato compatto per costruzione ripetuto su ogni colonna sarebbe rumore) e la somma esatta non
 * è scritta da nessuna parte (una riga Total nella tabella per anno è stata proposta e scartata).
 */
fun formatCardMintage(value: Long, compact: Boolean): String =
    if (compact) formatCompactMintage(value) else NumberFormat.getIntegerInstance(Locale.ENGLISH).format(value)
