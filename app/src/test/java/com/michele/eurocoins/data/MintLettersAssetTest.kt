package com.michele.eurocoins.data

import java.io.File
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Controlla `assets/mint_letters.json` (prodotto da `scripts/export-mint-letters.ps1`) con le stesse
 * classi dell'app e lo incrocia con gli altri due asset: la somma delle lettere deve dare i totali
 * già mostrati, altrimenti l'app direbbe due numeri diversi per la stessa moneta.
 */
class MintLettersAssetTest {

    private val json = Json { ignoreUnknownKeys = true }
    private val letters = MintLetters.parse(File("src/main/assets/mint_letters.json").readText())

    private val series: List<RegularIssueSeries> = json
        .decodeFromString<List<RegularIssueSeriesJson>>(File("src/main/assets/regular_issues.json").readText())
        .map { it.toEntity() }

    private val coins: List<CoinJson> = json.decodeFromString(File("src/main/assets/coins.json").readText())

    private val germanLetters = listOf("A", "D", "F", "G", "J")

    @Test
    fun everyGermanYearHasAllFiveLettersInOrder() {
        val raw = json.decodeFromString<MintLettersJson>(File("src/main/assets/mint_letters.json").readText())
        assertEquals(191, raw.circolanti.size)
        assertEquals(33, raw.commemorative.size)
        assertTrue(raw.circolanti.all { it.paese == "Germania" && it.zecche.map { z -> z.lettera } == germanLetters })
        assertTrue(raw.commemorative.all { it.zecche.map { z -> z.lettera } == germanLetters })
    }

    @Test
    fun circulationLookupUsesPaeseTaglioAnno() {
        val shares = letters.forCirculation("Germania", "1 cent", 2005)
        assertEquals(germanLetters, shares.map { it.lettera })
        assertEquals(120_000_000L, shares.first { it.lettera == "A" }.standard)
        assertEquals(84_000_000L, shares.first { it.lettera == "G" }.standard)
        // Anni senza lettere nel dato (2 euro 2007 non è su Numista): vuoto, non un'eccezione.
        assertTrue(letters.forCirculation("Germania", "2 euro", 2007).isEmpty())
        assertTrue(letters.forCirculation("Francia", "1 cent", 2005).isEmpty())
    }

    @Test
    fun commemorativeLookupUsesNumistaIdAndYear() {
        val shares = letters.forCommemorative(2169, 2006) // Schleswig-Holstein
        assertEquals(30_110_000L, shares.sumOf { it.standard ?: 0L })
        assertEquals(7_250_000L, shares.first { it.lettera == "F" }.standard)
        assertEquals(85_000L, shares.first { it.lettera == "A" }.bu)
        assertEquals(75_000L, shares.first { it.lettera == "A" }.proof)
        assertTrue(letters.forCommemorative(null, 2006).isEmpty())
        assertTrue(letters.forCommemorative(2169, 2007).isEmpty())
    }

    @Test
    fun commemorativeLettersAddUpToTheCoinTotals() {
        val german = coins.filter { it.paese == "Germania" && it.numistaId != null }
        assertEquals(33, german.size)
        for (coin in german) {
            val shares = letters.forCommemorative(coin.numistaId, coin.anno)
            assertEquals("${coin.anno} ${coin.tema}", germanLetters, shares.map { it.lettera })
            fun total(q: CoinQuality) = shares.sumOf { it.mintageFor(q) ?: 0L }
            assertEquals(coin.tema, (coin.tiraturaNumistaStandard ?: 0).toLong(), total(CoinQuality.STANDARD))
            assertEquals(coin.tema, (coin.tiraturaNumistaBu ?: 0).toLong(), total(CoinQuality.BU))
            assertEquals(coin.tema, (coin.tiraturaNumistaProof ?: 0).toLong(), total(CoinQuality.PROOF))
        }
    }

    @Test
    fun circulationLettersAddUpToTheYearTotalsShownByTheRegularCatalog() {
        val images = series.first { it.paese == "Germania" }.immagini
        var checked = 0
        for (image in images) {
            for (year in image.tirature.map { it.anno }.distinct()) {
                val shares = letters.forCirculation("Germania", image.taglio, year)
                if (shares.isEmpty()) continue
                for (quality in CoinQuality.values()) {
                    val shown = image.tirature.filter { it.anno == year && it.quality == quality }.sumOf { it.tiratura }
                    val sum = shares.sumOf { it.mintageFor(quality) ?: 0L }
                    assertEquals("${image.taglio} $year $quality", shown, sum)
                }
                checked++
            }
        }
        assertEquals(191, checked)
    }
}
