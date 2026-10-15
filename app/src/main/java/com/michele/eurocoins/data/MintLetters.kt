package com.michele.eurocoins.data

import android.content.res.AssetManager
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

/**
 * Tirature PER LETTERA DI ZECCA (oggi solo la Germania: A, D, F, G, J), da `assets/mint_letters.json`
 * (prodotto da `scripts/export-mint-letters.ps1`).
 *
 * Asset separato da `coins.json` e `regular_issues.json` di proposito: sono dati Numista, quindi il
 * piano B (CLAUDE.md § Backlog, "Gate da ricontrollare") è togliere questo solo file. Per questo il
 * file è OPZIONALE: se manca o non si legge, [MintLetters.EMPTY] e nessuna schermata ha lettere da
 * mostrare, senza errori. Non finisce in Room (è statico, ~75 KB, e non ha dati utente): si legge
 * una volta e si tiene in memoria.
 *
 * Si unisce al catalogo per CHIAVE NATURALE, mai per id di riga (rigenerati a ogni ripopolamento):
 * - circolanti: `(paese, taglio, anno)`, la stessa chiave di [RegularIssueMintage];
 * - commemorative: `(numistaId, anno)`, dove `numistaId` è [Coin.numistaId].
 */
@Serializable
data class MintLetterShare(
    /** "A", "D", "F", "G", "J". */
    val lettera: String,
    val standard: Long? = null,
    val bu: Long? = null,
    val proof: Long? = null,
    /** Il bucket che l'app non mostra (set, rotoli): c'è per un uso futuro, vedi CLAUDE.md § dataset. */
    val altro: Long? = null,
) {
    /** `null` = Numista non ha il dato (non "zero pezzi"). */
    fun mintageFor(quality: CoinQuality): Long? = when (quality) {
        CoinQuality.STANDARD -> standard
        CoinQuality.BU -> bu
        CoinQuality.PROOF -> proof
    }
}

@Serializable
data class CirculationMintLettersJson(
    val paese: String,
    val taglio: String,
    val anno: Int,
    val zecche: List<MintLetterShare>,
)

@Serializable
data class CommemorativeMintLettersJson(
    @SerialName("numista_id") val numistaId: Int,
    val anno: Int,
    val zecche: List<MintLetterShare>,
)

@Serializable
data class MintLettersJson(
    val schema: Int = 1,
    val fonte: String = "numista",
    val circolanti: List<CirculationMintLettersJson> = emptyList(),
    val commemorative: List<CommemorativeMintLettersJson> = emptyList(),
)

class MintLetters private constructor(json: MintLettersJson) {

    private val circulation: Map<Triple<String, String, Int>, List<MintLetterShare>> =
        json.circolanti.associate { Triple(it.paese, it.taglio, it.anno) to it.zecche }

    private val commemorative: Map<Pair<Int, Int>, List<MintLetterShare>> =
        json.commemorative.associate { (it.numistaId to it.anno) to it.zecche }

    /** Le lettere di un taglio circolante in un anno, in ordine di lettera; vuota = nessun dato per lettera. */
    fun forCirculation(paese: String, taglio: String, anno: Int): List<MintLetterShare> =
        circulation[Triple(paese, taglio, anno)].orEmpty()

    /** Le lettere di una commemorativa; [numistaId] nullo (monete senza abbinamento Numista) = vuota. */
    fun forCommemorative(numistaId: Int?, anno: Int): List<MintLetterShare> =
        if (numistaId == null) emptyList() else commemorative[numistaId to anno].orEmpty()

    val isEmpty: Boolean get() = circulation.isEmpty() && commemorative.isEmpty()

    companion object {
        const val ASSET_NAME = "mint_letters.json"

        val EMPTY = MintLetters(MintLettersJson())

        private val jsonFormat = Json { ignoreUnknownKeys = true }

        fun parse(text: String): MintLetters = MintLetters(jsonFormat.decodeFromString(MintLettersJson.serializer(), text))

        /** Asset opzionale (piano B Numista): assente o illeggibile = [EMPTY], mai un'eccezione. */
        fun load(assets: AssetManager): MintLetters = try {
            assets.open(ASSET_NAME).bufferedReader(Charsets.UTF_8).use { parse(it.readText()) }
        } catch (_: Exception) {
            EMPTY
        }
    }
}

/**
 * Città della zecca per lettera, per paese (chiave = `paese` stabile del dataset). Solo per mostrare
 * "A · Berlin": il dato porta la lettera, la città è un fatto noto e fisso.
 */
private val MINT_LETTER_CITIES: Map<String, Map<String, String>> = mapOf(
    "Germania" to mapOf(
        "A" to "Berlin",
        "D" to "Munich",
        "F" to "Stuttgart",
        "G" to "Karlsruhe",
        "J" to "Hamburg",
    ),
)

/** "Berlin" per ("Germania", "A"); `null` se il paese o la lettera non sono in tabella. */
fun mintLetterCity(paese: String, lettera: String): String? = MINT_LETTER_CITIES[paese]?.get(lettera)

/**
 * Le lettere di zecca di un paese, in ordine ("A", "D", "F", "G", "J" per la Germania); vuota per i paesi
 * che non hanno più zecche a lettera in contemporanea. Decide dove i pannelli di collezione mostrano la
 * riga "Mint": dipende dal paese, non dal dato delle tirature (la lettera c'è su ogni moneta tedesca anche dove
 * Numista non ha il numero).
 */
fun mintLettersFor(paese: String): List<String> = MINT_LETTER_CITIES[paese]?.keys?.sorted().orEmpty()
