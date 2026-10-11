package com.michele.eurocoins.data

import java.math.BigDecimal
import java.math.RoundingMode

/** Una riga della tabella a zecche affiancate: un anno e, per ogni lettera di [MintLetterTable.letters], il valore. */
data class MintLetterYearRow(
    val anno: Int,
    /** Nello stesso ordine di [MintLetterTable.letters]; `null` = Numista non ha il dato per quella lettera e qualità. */
    val values: List<Long?>,
)

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
 * Tiratura compatta per una colonna stretta: "960 M", "119.4 M", "4 B", "85 k". Fino a due decimali, senza zeri
 * in coda. Perde le ultime cifre (è fatta per confrontare le zecche tra loro, non per leggere il numero esatto:
 * quello sta nella card e nella tabella dei totali). Niente "≈" davanti, a differenza di [formatApproxTotal]:
 * ripetuto in ogni cella sarebbe rumore.
 */
fun formatCompactMintage(value: Long): String = when {
    value >= 1_000_000_000L -> scaled(value, 9) + " B"
    value >= 1_000_000L -> scaled(value, 6) + " M"
    value >= 10_000L -> scaled(value, 3, decimals = 1) + " k"
    else -> "%,d".format(java.util.Locale.ENGLISH, value)
}

private fun scaled(value: Long, exponent: Int, decimals: Int = 2): String =
    BigDecimal(value).movePointLeft(exponent).setScale(decimals, RoundingMode.HALF_UP).stripTrailingZeros().toPlainString()
