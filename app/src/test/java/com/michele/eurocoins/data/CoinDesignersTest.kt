package com.michele.eurocoins.data

import java.io.File
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Autore del lato nazionale delle commemorative: `autore_retro` della pipeline vince sul grezzo Numista al
 * livello "certo"; il "probabile" (l'incisore usato come ipotesi) si mostra, dal 2026-10-18, solo se Numista non
 * ha un designer suo e con il livello a bordo (corsivo + ⓘ nell'interfaccia). Fonti: ufficiali, banche centrali e
 * Numista, mai zwei-euro.com.
 */
class CoinDesignersTest {

    private val json = Json { ignoreUnknownKeys = true }

    private val records: List<CoinJson> = json
        .decodeFromString<List<CoinJson>>(File("src/main/assets/coins.json").readText())

    private fun record(extra: String) = json.decodeFromString<CoinJson>(
        """{"paese":"X","anno":2020,"tema":"t","zecca_emittente":"X","zecca_raw":"X",""" +
            """"licenza_immagine":"l","fonte_dati":"f"$extra""",
    )

    @Test
    fun certainAuthorWinsOverRawNumista() {
        val coin = record(""","disegnatore_retro_raw":"Raw","autore_retro":"Author","autore_retro_livello":"certo"}""")
        val credit = coin.toEntity().designerCredit()
        assertEquals(DesignerCredit("Author", probable = false), credit)
    }

    @Test
    fun probableAuthorIsShownButMarked() {
        val coin = record(""","autore_retro":"Engraver","autore_retro_livello":"probabile"}""")
        assertEquals(DesignerCredit("Engraver", probable = true), coin.toEntity().designerCredit())
    }

    @Test
    fun probableAuthorNeverCancelsANumistaDesigner() {
        val withRaw = record(""","disegnatore_retro_raw":"Raw","autore_retro":"Engraver","autore_retro_livello":"probabile"}""")
        assertEquals(DesignerCredit("Raw", probable = false), withRaw.toEntity().designerCredit())
    }

    @Test
    fun withoutAuthorFieldsTheRawNumistaValueStays() {
        assertEquals(DesignerCredit("Raw", false), record(""","disegnatore_retro_raw":"Raw"}""").toEntity().designerCredit())
        assertNull(record("""}""").toEntity().designerCredit())
        assertEquals(
            DesignerCredit("Raw", false),
            record(""","disegnatore_retro_raw":"Raw","autore_retro":"  ","autore_retro_livello":"certo"}""").toEntity().designerCredit(),
        )
    }

    @Test
    fun theBundledCatalogCarriesOnlyOfficialAndNumistaAuthors() {
        val coins = records.map { it.toEntity() }
        val shown = coins.count { !it.displayDesigner().isNullOrBlank() }
        val probable = coins.count { it.designerCredit()?.probable == true }
        // 2026-10-18: 377 certi (169 Numista designers + 135 testo BCE/Gazzetta UE + 70 banche centrali + 3 testo
        // Numista) e 148 probabili = 525 di 613. Erano 566 con zwei-euro.com, tolte per la licenza.
        assertEquals(525, shown)
        assertEquals(148, probable)
        assertTrue(records.none { it.autoreRetroLivello == "certo" && it.autoreRetro.isNullOrBlank() })
        // il livello sta nel database solo dove c'è un nome
        assertTrue(coins.none { it.autoreRetroLivello != null && it.displayDesigner() == null })
        assertFalse(coins.any { it.autoreRetroLivello == LEVEL_PROBABLE && it.designerCredit()?.probable != true })
    }
}
