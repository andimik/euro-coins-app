package com.michele.eurocoins.data

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Forma esatta di un record in `assets/coins.json`, esportato da
 * `data/processed/ecb_coins.jsonl` della pipeline dati (repo
 * euro-coins-data-pipeline). I nomi dei campi ricalcano lo schema pydantic
 * `MonetaCommemorativa` — vedi quel repo per il significato di ciascuno.
 */
@Serializable
data class CoinJson(
    val paese: String,
    val anno: Int,
    val tema: String,
    val tiratura: Int? = null,
    @SerialName("zecca_emittente") val zeccaEmittente: String,
    @SerialName("zecca_raw") val zeccaRaw: String,
    @SerialName("zecca_fisica_raw") val zeccaFisicaRaw: String? = null,
    @SerialName("incisore_retro_raw") val incisoreRetroRaw: String? = null,
    @SerialName("disegnatore_retro_raw") val disegnatoreRetroRaw: String? = null,
    /**
     * Autore del lato nazionale consolidato dalla pipeline: il designer se una fonte ufficiale o
     * Numista lo dichiara, altrimenti l'incisore Numista (livello "probabile"). Scritto da
     * `scripts/merge-commemorative-designers.ps1`. In app si mostra solo il livello "certo".
     */
    @SerialName("autore_retro") val autoreRetro: String? = null,
    /** "certo" | "probabile" | null (se [autoreRetro] è null). */
    @SerialName("autore_retro_livello") val autoreRetroLivello: String? = null,
    @SerialName("note_storiche") val noteStoriche: String? = null,
    @SerialName("url_immagine_fonte") val urlImmagineFonte: String? = null,
    @SerialName("licenza_immagine") val licenzaImmagine: String,
    @SerialName("attribuzione_immagine_raw") val attribuzioneImmagineRaw: String? = null,
    @SerialName("immagine_placeholder") val immaginePlaceholder: Boolean = false,
    @SerialName("fonte_dati") val fonteDati: String,
    @SerialName("emissione_comune") val emissioneComune: Boolean = false,
    @SerialName("tiratura_numista_standard") val tiraturaNumistaStandard: Int? = null,
    @SerialName("tiratura_numista_bu") val tiraturaNumistaBu: Int? = null,
    @SerialName("tiratura_numista_proof") val tiraturaNumistaProof: Int? = null,
    @SerialName("numista_id") val numistaId: Int? = null,
)

fun CoinJson.toEntity(): Coin = Coin(
    paese = paese,
    anno = anno,
    tema = tema,
    tiratura = tiratura,
    zeccaEmittente = zeccaEmittente,
    zeccaRaw = zeccaRaw,
    zeccaFisicaRaw = zeccaFisicaRaw,
    incisoreRetroRaw = incisoreRetroRaw,
    disegnatoreRetroRaw = shownAuthor() ?: disegnatoreRetroRaw,
    noteStoriche = noteStoriche,
    urlImmagineFonte = urlImmagineFonte,
    licenzaImmagine = licenzaImmagine,
    attribuzioneImmagineRaw = attribuzioneImmagineRaw,
    immaginePlaceholder = immaginePlaceholder,
    fonteDati = fonteDati,
    emissioneComune = emissioneComune,
    tiraturaNumistaStandard = tiraturaNumistaStandard,
    tiraturaNumistaBu = tiraturaNumistaBu,
    tiraturaNumistaProof = tiraturaNumistaProof,
    numistaId = numistaId,
    autoreRetroLivello = if (shownAuthor() != null) autoreRetroLivello else null,
)

const val LEVEL_CERTAIN = "certo"
const val LEVEL_PROBABLE = "probabile"

/**
 * Il nome di `autore_retro` che l'app mostra: il "certo" vince sempre sul grezzo Numista; il "probabile"
 * (l'incisore come ipotesi) solo se Numista non ha un designer suo, e il livello lo marca (`Coin.autoreRetroLivello`).
 */
private fun CoinJson.shownAuthor(): String? {
    val name = autoreRetro?.trim()?.takeIf { it.isNotEmpty() } ?: return null
    return when (autoreRetroLivello) {
        LEVEL_CERTAIN -> name
        LEVEL_PROBABLE -> name.takeIf { disegnatoreRetroRaw.isNullOrBlank() }
        else -> null
    }
}
