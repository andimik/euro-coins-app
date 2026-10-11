package com.michele.eurocoins.data

/** Codice della varietà EFS (stringa salvata in `RegularCollectionItem.variety`; `""` = moneta normale). */
const val VARIETY_EFS = "EFS"

/**
 * Una varietà collezionabile di un'annata: [code] è il valore salvato nella collezione, [label] il
 * nome mostrato nel pannello, [detail] il segno che la distingue.
 */
data class RegularVariety(val code: String, val label: String, val detail: String)

/**
 * Lettera nella stella delle monete greche del 2002 coniate all'estero: la serie "EFS" (E = Madrid,
 * F = Parigi, S = Finlandia, "Suomi"), diversa dalle stesse monete coniate ad Atene, senza lettera.
 * Fatti pubblici e documentati (tabella "Identifying marks on euro coins"): TENUTI QUI e non
 * derivati da `RegularIssueImage.zecchePerAnno`, che viene da Numista ed è escluso dall'asset
 * committato (`-ExcludeNumista`) — la funzione sparirebbe proprio nell'app pubblica. Un test sul
 * dataset completo controlla che le lettere corrispondano alla zecca estera del dato.
 */
private val GREEK_EFS_LETTER = mapOf(
    "1 cent" to "F", "2 cent" to "F", "5 cent" to "F", "10 cent" to "F", "50 cent" to "F",
    "20 cent" to "E",
    "1 euro" to "S", "2 euro" to "S",
)

private const val GREEK_EFS_YEAR = 2002

/**
 * La varietà offerta dal pannello di collezione per [taglio] nell'anno [year] di questa serie, o
 * null se l'annata non ne ha. Oggi solo Grecia, serie 1, anno 2002 (EFS).
 */
fun RegularIssueSeries.varietyFor(taglio: String, year: Int): RegularVariety? {
    if (paese != "Grecia" || ordineCronologico != 1 || year != GREEK_EFS_YEAR) return null
    val letter = GREEK_EFS_LETTER[taglio] ?: return null
    return RegularVariety(VARIETY_EFS, "EFS variety", "letter $letter in the star")
}

/** Zecca estera che ha coniato i pezzi con una certa lettera, come la si nomina nella spiegazione. */
private val GREEK_EFS_MINT = mapOf("F" to "Paris", "E" to "Madrid", "S" to "Finland")

/**
 * I tagli che portano la stessa lettera dell'EFS: "1, 2, 5, 10, 50 cent" → F, Parigi. Raggruppati perché cinque tagli
 * su otto hanno la stessa lettera e una riga per taglio ripeterebbe la F cinque volte. Derivati da [GREEK_EFS_LETTER],
 * la stessa tabella che offre la varietà nel pannello, così la spiegazione non può divergere.
 */
data class EfsLetterGroup(val taglios: List<String>, val letter: String, val mint: String) {
    /** "1, 2, 5, 10, 50 cent", "20 cent", "1 and 2 euro". */
    val label: String
        get() {
            val unit = taglios.first().substringAfter(' ')
            val numbers = taglios.map { it.substringBefore(' ') }
            val list = if (numbers.size == 2) "${numbers[0]} and ${numbers[1]}" else numbers.joinToString(", ")
            return "$list $unit"
        }
}

/** Le tre righe di "dove cercare" (F, E, S), nell'ordine in cui compaiono tra i tagli. */
fun greekEfsLetterGroups(): List<EfsLetterGroup> =
    GREEK_EFS_LETTER.entries.groupBy({ it.value }, { it.key }).map { (letter, taglios) ->
        EfsLetterGroup(taglios, letter, GREEK_EFS_MINT.getValue(letter))
    }

/** Il gruppo di [taglio] (la lettera da cercare sulla sua moneta), o null se non è un taglio dell'EFS. */
fun greekEfsLetterFor(taglio: String): EfsLetterGroup? = greekEfsLetterGroups().firstOrNull { taglio in it.taglios }
