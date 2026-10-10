package com.michele.eurocoins.data

import android.content.Context
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.shareIn
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import java.security.MessageDigest

/**
 * Espone le serie divisionali (1 cent - 2 euro) dal database locale, e si
 * occupa di popolarlo la prima volta leggendo `assets/regular_issues.json`
 * (esportato dalla pipeline dati — vedi repo euro-coins-data-pipeline,
 * `data/processed/ec_national_sides.jsonl`). Stesso meccanismo di
 * [CoinRepository] (hash dell'asset per ripopolare, mutex per evitare
 * doppio seeding), ma dataset separato: nessuna collezione utente qui.
 */
class RegularIssueRepository(
    private val context: Context,
    private val dao: RegularIssueDao,
    private val collectionDao: RegularCollectionDao,
    hideMicrostates: Flow<Boolean>,
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    /**
     * Tutte le serie, senza i microstati quando l'utente li ha nascosti dalle Impostazioni, e senza le
     * immagini che non vanno mostrate ([withUsableImages]): applicato in lettura e non al seeding perché
     * l'asset non cambia, quindi non ripopolerebbe un database già seminato.
     */
    val series: SharedFlow<List<RegularIssueSeries>> = combine(dao.observeAll(), hideMicrostates) { all, hide ->
        (if (hide) all.filterNot { it.paese in MICROSTATE_PAESI } else all).map { it.withUsableImages() }
    }.shareIn(scope, SharingStarted.Eagerly, replay = 1)

    /** Ultimo dataset già letto, o null se Room non ha ancora risposto: come `CoinRepository.coinsNow`. */
    val seriesNow: List<RegularIssueSeries>? get() = series.replayCache.firstOrNull()

    /** Tutte le voci di collezione dell'utente sulle monete circolanti (una per taglio+anno+qualità). */
    val collectionItems: SharedFlow<List<RegularCollectionItem>> = collectionDao.observeAll()
        .shareIn(scope, SharingStarted.Eagerly, replay = 1)

    /** Stesso discorso di [seriesNow] per la collezione. */
    val collectionNow: List<RegularCollectionItem>? get() = collectionItems.replayCache.firstOrNull()

    /**
     * Tagli distinti posseduti (in qualsiasi serie, anno e qualità): quelli che il reset toglie.
     * Non è il conteggio della barra della Home, che conta le righe dentro le finestre delle serie.
     */
    val ownedCount: Flow<Int> = collectionItems.map { items -> items.map { it.seriesKey to it.taglio }.toSet().size }

    /** Svuota la collezione Regular dell'utente; il catalogo `regular_issue_series` non viene toccato. */
    suspend fun resetCollection() = collectionDao.deleteAll()

    /**
     * Salva in blocco le annate/qualità possedute di un taglio ([entries]): quelle non presenti
     * vengono rimosse. Conserva la data di aggiunta delle voci già esistenti (stessa (anno,
     * qualità, varietà)). Stesso pattern di `CoinRepository.saveCollection`, ma qui la chiave include
     * anche l'anno perché più annate dello stesso taglio possono coesistere.
     *
     * [window]: la serie guardata mostra (e quindi può cambiare) solo gli anni della sua finestra di
     * tempo (vedi [denominationsOf]); un taglio rimasto invariato è la stessa moneta in più serie, e
     * salvare dalla serie 2 non deve cancellare le annate della serie 1. Le voci FUORI finestra
     * restano com'erano; senza finestra si sostituisce tutto, come prima.
     */
    suspend fun saveCollection(
        seriesKey: String,
        taglio: String,
        paese: String,
        entries: List<RegularCollectionEntry>,
        window: SeriesDenomination? = null,
    ) {
        // Le voci SENZA ANNO ([NO_YEAR]) appartengono alla serie GUARDATA, non a quella di origine: per un
        // taglio ereditato stanno sotto un'altra chiave e si salvano a parte. Senza finestra (sostituisce
        // tutto) la riga guardata è quella di origine.
        val viewedKey = window?.viewed?.stableKey ?: seriesKey
        val sameKey = viewedKey == seriesKey
        val noYear = entries.filter { it.anno == NO_YEAR }
        val withYear = entries.filter { it.anno != NO_YEAR }
        val all = collectionDao.itemsFor(seriesKey, taglio)
        val kept = if (window == null) {
            emptyList()
        } else {
            // Restano le voci fuori finestra; quelle senza anno si sostituiscono solo se sono della riga guardata.
            all.filterNot { if (it.anno == NO_YEAR) sameKey else window.contains(it.anno) }
        }
        val now = System.currentTimeMillis()
        collectionDao.replaceForDenomination(
            seriesKey,
            taglio,
            kept + toItems(seriesKey, taglio, paese, if (sameKey) withYear + noYear else withYear, all, now),
        )
        if (!sameKey) {
            val atViewed = collectionDao.itemsFor(viewedKey, taglio)
            if (noYear.isNotEmpty() || atViewed.any { it.anno == NO_YEAR }) {
                collectionDao.replaceForDenomination(
                    viewedKey,
                    taglio,
                    atViewed.filter { it.anno != NO_YEAR } + toItems(viewedKey, taglio, paese, noYear, atViewed, now),
                )
            }
        }
    }

    /** Le voci da scrivere per [entries], con la data di aggiunta di quelle già in [existing] (stessa anno, qualità, varietà). */
    private fun toItems(
        seriesKey: String,
        taglio: String,
        paese: String,
        entries: List<RegularCollectionEntry>,
        existing: List<RegularCollectionItem>,
        now: Long,
    ): List<RegularCollectionItem> {
        val before = existing.associateBy { Triple(it.anno, it.quality, it.variety) }
        return entries.map { entry ->
            RegularCollectionItem(
                seriesKey = seriesKey,
                taglio = taglio,
                anno = entry.anno,
                quality = entry.quality,
                variety = entry.variety,
                priceCents = entry.priceCents,
                purchasedOn = entry.purchasedOn,
                paese = paese,
                addedAt = before[Triple(entry.anno, entry.quality, entry.variety)]?.addedAt ?: now,
            )
        }
    }

    /**
     * Tocco con Quick add su una riga ([QuickTap], deciso da [quickTapForRegular] su [items], le voci della riga):
     * aggiunge la voce senza anno o la toglie. [QuickTap.CONFIRM_REMOVE] non fa niente: lo gestisce la schermata
     * mostrando la conferma di rimozione.
     */
    suspend fun quickToggle(window: SeriesDenomination, items: List<RegularCollectionItem>) {
        val entries = when (quickTapForRegular(items)) {
            QuickTap.ADD -> listOf(RegularCollectionEntry(anno = NO_YEAR, quality = CoinQuality.STANDARD, priceCents = null))
            QuickTap.REMOVE -> emptyList()
            QuickTap.CONFIRM_REMOVE -> return
        }
        saveCollection(window.series.stableKey, window.image.taglio, window.series.paese, entries, window)
    }

    private val seedMutex = Mutex()
    private val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    /** Allinea il database a `assets/regular_issues.json`, come `CoinRepository.ensureSeeded()`. */
    suspend fun ensureSeeded() = seedMutex.withLock {
        withContext(Dispatchers.IO) {
            val bytes = context.assets.open(ASSET_FILE_NAME).use { it.readBytes() }
            val hash = MessageDigest.getInstance("SHA-256").digest(bytes).joinToString("") { "%02x".format(it) }
            if (prefs.getString(KEY_ASSET_HASH, null) == hash && dao.count() > 0) return@withContext

            val parsed = jsonFormat.decodeFromString<List<RegularIssueSeriesJson>>(bytes.decodeToString())
            dao.replaceAll(parsed.map { it.toEntity() })
            prefs.edit().putString(KEY_ASSET_HASH, hash).apply()
        }
    }

    companion object {
        private const val ASSET_FILE_NAME = "regular_issues.json"
        private const val PREFS_NAME = "regular_issues_dataset"
        private const val KEY_ASSET_HASH = "regular_issues_asset_sha256"
        private val jsonFormat = Json { ignoreUnknownKeys = true }
    }
}
