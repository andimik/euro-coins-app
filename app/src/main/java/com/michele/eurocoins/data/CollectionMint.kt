package com.michele.eurocoins.data

/**
 * "Zecca non indicata" nella collezione: la voce di sempre (ce l'ho, di che zecca non lo dico), e anche quella
 * che crea il tocco semplice del Quick add. Sta in [CollectionItem.mint] e [RegularCollectionItem.mint] come
 * stringa vuota; le lettere vere ("A", "D", "F", "G", "J" per la Germania) sono quelle di [mintLettersFor].
 * Parallela a [NO_YEAR]: "il dato c'è ma non lo so dire", mai "nessuna zecca".
 */
const val NO_MINT = ""

/**
 * Una finitura di una moneta in una zecca: la chiave delle voci di UNA commemorativa (oggi, senza zecca, solo
 * la qualità). Ogni zecca è una moneta diversa, con prezzo e data propri.
 */
data class MintFinish(val mint: String = NO_MINT, val quality: CoinQuality)
