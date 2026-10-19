package com.michele.eurocoins.data

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Una moneta da 2€ commemorativa, così come vive nel database locale.
 *
 * `tema` e `noteStoriche` sono oggi un unico blob di testo nella lingua della
 * fonte (inglese, per la BCE — vedi CLAUDE.md § Lingua). Non dare per
 * scontato altrove nel codice che sia l'unica rappresentazione testuale
 * possibile: un'eventuale fonte futura in italiano arriverà come dato
 * aggiuntivo dalla pipeline (nuovi record/campi), non come traduzione
 * automatica che sovrascrive questi campi in-place.
 */
@Entity(tableName = "coins")
data class Coin(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val paese: String,
    val anno: Int,
    val tema: String,
    val tiratura: Int?,
    val zeccaEmittente: String,
    val zeccaRaw: String,
    val zeccaFisicaRaw: String?,
    /** Incisore del disegno commemorativo (chi ha inciso il conio); non si mostra (vedi [displayDesigner]). */
    val incisoreRetroRaw: String?,
    /**
     * Disegnatore del disegno commemorativo (chi ha ideato il soggetto). Ruolo distinto da
     * [incisoreRetroRaw], non un ripiego. Nonostante il nome è il valore CONSOLIDATO dalla pipeline
     * quando c'è (`CoinJson.autoreRetro`), altrimenti il grezzo Numista: il nome della colonna
     * resta per non fare una migrazione Room per un cambio di significato.
     */
    val disegnatoreRetroRaw: String?,
    val noteStoriche: String?,
    val urlImmagineFonte: String?,
    val licenzaImmagine: String,
    val attribuzioneImmagineRaw: String?,
    val immaginePlaceholder: Boolean,
    val fonteDati: String,
    /** True per le monete delle 5 emissioni commemorative congiunte dell'Eurozona (vedi CLAUDE.md). */
    val emissioneComune: Boolean,
    /**
     * Tirature per finitura da Numista (fonte indipendente da [tiratura], mai sovrascritta
     * né fusa con essa — vedi NOTES.md della pipeline). Null se Numista non riporta quella
     * finitura per questa moneta: non implica che la moneta non l'abbia mai avuta.
     */
    val tiraturaNumistaStandard: Int?,
    val tiraturaNumistaBu: Int?,
    val tiraturaNumistaProof: Int?,
    /** N# del type Numista da cui vengono tirature, zecca e incisore: attribuzione obbligatoria nei crediti (Termini API, par. 4). Null per le poche monete senza abbinamento. */
    val numistaId: Int? = null,
    /**
     * Livello di [disegnatoreRetroRaw] quando il nome viene da `autore_retro` della pipeline: "certo" o
     * "probabile" (l'incisore Numista usato come ipotesi: l'interfaccia lo mostra in corsivo con la nota
     * «ruolo approssimativo»). Null = valore del grezzo Numista o nessun nome. Colonna dalla v12.
     */
    val autoreRetroLivello: String? = null,
)
