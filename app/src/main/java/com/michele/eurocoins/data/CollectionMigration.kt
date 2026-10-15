package com.michele.eurocoins.data

/**
 * Migrazione 10 -> 11 del database: la zecca ([NO_MINT] = non indicata) entra nella CHIAVE delle due tabelle
 * di collezione. SQLite non cambia una chiave primaria con `ALTER`, quindi si ricostruiscono entrambe: tabella
 * `_new` con la chiave nuova, copia con `mint = ''`, `DROP`, `RENAME` (come la 8 -> 9).
 *
 * Stanno qui, e non dentro la migrazione, per poterle confrontare con lo schema che Room genera
 * ([CollectionMigrationTest]): se differiscono Room rifiuta il database all'apertura e l'app non parte per
 * nessuno. Si copiano TUTTE le colonne, `purchasedOn` compreso (la 8 -> 9 non lo conosceva: è arrivato con la
 * 9 -> 10).
 */
internal const val COLLECTION_ITEMS_CREATE_NEW_SQL =
    "CREATE TABLE `collection_items_new` (" +
        "`coinKey` TEXT NOT NULL, `quality` TEXT NOT NULL, `mint` TEXT NOT NULL, `priceCents` INTEGER, " +
        "`purchasedOn` INTEGER, `anno` INTEGER NOT NULL, `paese` TEXT NOT NULL, `tema` TEXT NOT NULL, " +
        "`addedAt` INTEGER NOT NULL, PRIMARY KEY(`coinKey`, `quality`, `mint`))"

internal const val REGULAR_COLLECTION_ITEMS_CREATE_NEW_SQL =
    "CREATE TABLE `regular_collection_items_new` (" +
        "`seriesKey` TEXT NOT NULL, `taglio` TEXT NOT NULL, `anno` INTEGER NOT NULL, `quality` TEXT NOT NULL, " +
        "`variety` TEXT NOT NULL, `mint` TEXT NOT NULL, `priceCents` INTEGER, `purchasedOn` INTEGER, " +
        "`paese` TEXT NOT NULL, `addedAt` INTEGER NOT NULL, " +
        "PRIMARY KEY(`seriesKey`, `taglio`, `anno`, `quality`, `variety`, `mint`))"

internal val MIGRATION_10_11_STATEMENTS: List<String> = listOf(
    COLLECTION_ITEMS_CREATE_NEW_SQL,
    "INSERT INTO `collection_items_new` " +
        "(`coinKey`, `quality`, `mint`, `priceCents`, `purchasedOn`, `anno`, `paese`, `tema`, `addedAt`) " +
        "SELECT `coinKey`, `quality`, '', `priceCents`, `purchasedOn`, `anno`, `paese`, `tema`, `addedAt` " +
        "FROM `collection_items`",
    "DROP TABLE `collection_items`",
    "ALTER TABLE `collection_items_new` RENAME TO `collection_items`",
    REGULAR_COLLECTION_ITEMS_CREATE_NEW_SQL,
    "INSERT INTO `regular_collection_items_new` " +
        "(`seriesKey`, `taglio`, `anno`, `quality`, `variety`, `mint`, `priceCents`, `purchasedOn`, `paese`, `addedAt`) " +
        "SELECT `seriesKey`, `taglio`, `anno`, `quality`, `variety`, '', `priceCents`, `purchasedOn`, `paese`, `addedAt` " +
        "FROM `regular_collection_items`",
    "DROP TABLE `regular_collection_items`",
    "ALTER TABLE `regular_collection_items_new` RENAME TO `regular_collection_items`",
)
