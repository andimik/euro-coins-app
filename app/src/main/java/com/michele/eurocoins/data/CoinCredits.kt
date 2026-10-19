package com.michele.eurocoins.data

/**
 * Dove è stata coniata la moneta, come paese della zecca ("Italy", "Netherlands"; vedi
 * [mintCountry] in MintNames.kt), non il paese emittente (`displayCountry()`).
 * Fonte: campo 'mints' di Numista, più valori uniti da "; " quando la moneta è stata coniata in
 * più zecche (anche identiche: es. "Royal Dutch Mint; Royal Dutch Mint" nel dataset grezzo).
 * Le 5 zecche regionali tedesche (mintmark A/D/F/G/J, su ogni moneta tedesca) collassano in un
 * solo "Germany" dopo la mappatura, quindi non serve più il conteggio "5 mints" che serviva
 * quando si mostravano i nomi per esteso. Il valore non contiene "Mint": c'è già l'etichetta.
 */
fun Coin.displayMint(): String? = formatMints(zeccaFisicaRaw)

/** Stesso criterio di [Coin.displayMint], per la zecca di un taglio delle Regular Issues. */
fun RegularIssueImage.displayMint(): String? = formatMints(zeccaFisicaRaw)

/** Designer del lato nazionale del taglio; l'incisore non si mostra (vedi [Coin.displayDesigner]). */
fun RegularIssueImage.displayDesigner(): String? = disegnatoreRaw?.takeIf { it.isNotBlank() }

/** Un nome di designer e se è solo probabile (corsivo + nota nell'interfaccia). */
data class DesignerCredit(val name: String, val probable: Boolean)

/** Come [displayDesigner], con il livello: null se non c'è nessun nome. */
fun RegularIssueImage.designerCredit(): DesignerCredit? =
    displayDesigner()?.let { DesignerCredit(it, disegnatoreProbabile) }

/** Come [Coin.displayDesigner], con il livello: null se non c'è nessun nome. */
fun Coin.designerCredit(): DesignerCredit? =
    displayDesigner()?.let { DesignerCredit(it, autoreRetroLivello == LEVEL_PROBABLE) }

private fun formatMints(raw: String?): String? {
    // distinct() DOPO la mappatura: Berlin/Munich/... sono cinque stringhe e un solo "Germany"
    val mints = raw?.split("; ")?.map { it.trim() }?.filter { it.isNotEmpty() }
        ?.map(::mintCountry)?.distinct().orEmpty()
    return mints.takeIf { it.isNotEmpty() }?.joinToString(", ")
}

/**
 * Chi ha ideato il disegno del lato nazionale (2026-10-18: è l'UNICO ruolo che l'app espone). Il valore è
 * l'`autore_retro` della pipeline (al livello "certo", o "probabile" se Numista non ha un designer, con il
 * livello in [Coin.autoreRetroLivello]), altrimenti il designer grezzo di Numista (vedi
 * `CoinJson.toEntity`). L'incisore resta nel database (`incisoreRetroRaw`) ma non si mostra: nel dato è quasi
 * sempre la stessa persona del designer (251 commemorative su 328 con entrambi i nomi) e per il
 * collezionista conta chi ha creato il disegno.
 */
fun Coin.displayDesigner(): String? = disegnatoreRetroRaw?.takeIf { it.isNotBlank() }