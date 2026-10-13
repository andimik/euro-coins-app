package com.michele.eurocoins.data

import java.math.BigDecimal
import java.math.RoundingMode

/** Una riga della tabella a zecche affiancate: un anno e, per ogni lettera di [MintLetterTable.letters], il valore. */
data class MintLetterYearRow(
    val anno: Int,
    /** Nello stesso ordine di [MintLetterTable.letters]; `null` = Numista non ha il dato per quella lettera e qualità. */
    val values: List<Long?>,
) {
    /** Somma delle lettere che hanno il dato; `null` se nessuna (mai 0: "non c'è il dato" non è "zero pezzi"). */
    val total: Long? get() = values.filterNotNull().takeIf { it.isNotEmpty() }?.sum()
}

/** Le zecche di un taglio affiancate, una colonna per lettera, per UNA qualità (variante C del mockup, 2026-10-11). */
data class MintLetterTable(
    val letters: List<String>,
    val rows: List<MintLetterYearRow>,
)

/**
 * La tabella "zecche affiancate" di un taglio circolante, sugli [years] dati (quelli della tabella "by year",
 * in ordine): una colonna per lettera, i valori della [quality] scelta. Gli anni senza lettere nel dato (Numista
 * non li ha, o ha un solo totale) restano nella tabella con tutti i valori `null`: spariti, la tabella avrebbe
 * buchi tra gli anni che la tabella dei totali ha. `null` se in nessun anno ci sono lettere (tutti i paesi tranne
 * la Germania): allora non c'è nulla da affiancare e vale la tabella dei totali.
 */
fun mintLetterTable(
    source: MintLetters,
    paese: String,
    taglio: String,
    years: List<Int>,
    quality: CoinQuality,
): MintLetterTable? {
    val perYear = years.associateWith { source.forCirculation(paese, taglio, it) }
    val letters = perYear.values.flatten().map { it.lettera }.distinct().sorted()
    if (letters.isEmpty()) return null
    val rows = years.map { year ->
        val byLetter = perYear.getValue(year).associateBy { it.lettera }
        MintLetterYearRow(year, letters.map { byLetter[it]?.mintageFor(quality) })
    }
    return MintLetterTable(letters, rows)
}

/**
 * Tiratura compatta per una colonna stretta, SEMPRE con una cifra decimale: "960.0 M", "119.4 M", "4.0 B", "85.0 k"
 * (scelta del proprietario, 2026-10-11: coerenza tra le colonne e più spazio). Perde le ultime cifre ("125.37 M"
 * diventa "125.4 M"): è fatta per confrontare le zecche tra loro, il numero esatto sta nella card e nella tabella
 * dei totali. Sotto 1.000 è il numero intero. Niente "≈" davanti: ripetuto in ogni cella sarebbe rumore. Lo
 * usa anche la card MINTAGES dei tagli con più anni ([formatCardMintage]).
 */
fun formatCompactMintage(value: Long): String {
    if (value < 1_000L) return value.toString()
    val units = listOf("k" to 3, "M" to 6, "B" to 9)
    var index = units.indexOfLast { value >= BigDecimal.TEN.pow(it.second).toLong() }
    var scaled = scaled(value, units[index].second)
    // 999.950 arrotonda a "1000.0 k": in realtà è "1.0 M".
    if (scaled >= BigDecimal(1000) && index < units.lastIndex) {
        index++
        scaled = scaled(value, units[index].second)
    }
    return scaled.toPlainString() + " " + units[index].first
}

private fun scaled(value: Long, exponent: Int): BigDecimal =
    BigDecimal(value).movePointLeft(exponent).setScale(1, RoundingMode.HALF_UP)
