package com.michele.eurocoins.data

import java.io.File
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * La migrazione 10 -> 11 ricostruisce le due tabelle di collezione: se lo SQL differisce da quello che Room genera
 * dalle entity, Room rifiuta il database all'apertura e l'app non parte per NESSUNO. Non si può provare la
 * migrazione vera in JVM (serve SQLite di Android), ma si può confrontare lo schema con quello generato.
 */
class CollectionMigrationTest {

    // Gradle esegue gli unit test con la cartella del modulo (app/) come directory corrente.
    private val generated: String =
        File("build/generated/ksp/debug/kotlin/com/michele/eurocoins/data/CoinDatabase_Impl.kt").readText()

    private fun roomCreateSql(table: String): String {
        val match = Regex("CREATE TABLE IF NOT EXISTS `$table` \\((.*?)\\)\"\\)").find(generated)
            ?: error("Room non ha generato la tabella $table")
        return "CREATE TABLE `${table}_new` (${match.groupValues[1]})"
    }

    @Test
    fun theNewTablesAreExactlyWhatRoomExpects() {
        assertEquals(roomCreateSql("collection_items"), COLLECTION_ITEMS_CREATE_NEW_SQL)
        assertEquals(roomCreateSql("regular_collection_items"), REGULAR_COLLECTION_ITEMS_CREATE_NEW_SQL)
    }

    private fun columnsOf(createSql: String): List<String> =
        Regex("`(\\w+)` (?:TEXT|INTEGER)").findAll(createSql.substringBefore("PRIMARY KEY")).map { it.groupValues[1] }.toList()

    private fun checkCopy(table: String, createSql: String) {
        val insert = MIGRATION_10_11_STATEMENTS.single { it.startsWith("INSERT INTO `${table}_new`") }
        val target = Regex("\\((.*?)\\) SELECT").find(insert)!!.groupValues[1].split(", ").map { it.trim('`') }
        val source = Regex("SELECT (.*?) FROM").find(insert)!!.groupValues[1].split(", ")
        val columns = columnsOf(createSql)
        // Tutte le colonne della tabella nuova si copiano, nessuna in più, nello stesso ordine.
        assertEquals(columns, target)
        // La zecca parte vuota; le altre si copiano da sé: nessun dato dell'utente perso (purchasedOn compreso).
        assertEquals(columns.map { if (it == "mint") "''" else "`$it`" }, source)
    }

    @Test
    fun everyColumnIsCopiedAndTheMintStartsEmpty() {
        checkCopy("collection_items", COLLECTION_ITEMS_CREATE_NEW_SQL)
        checkCopy("regular_collection_items", REGULAR_COLLECTION_ITEMS_CREATE_NEW_SQL)
        assertTrue("purchasedOn" in columnsOf(COLLECTION_ITEMS_CREATE_NEW_SQL))
        assertTrue("purchasedOn" in columnsOf(REGULAR_COLLECTION_ITEMS_CREATE_NEW_SQL))
    }

    @Test
    fun eachTableIsRebuiltInTheRightOrder() {
        val sql = MIGRATION_10_11_STATEMENTS
        for (table in listOf("collection_items", "regular_collection_items")) {
            val create = sql.indexOfFirst { it.startsWith("CREATE TABLE `${table}_new`") }
            val insert = sql.indexOfFirst { it.startsWith("INSERT INTO `${table}_new`") }
            val drop = sql.indexOf("DROP TABLE `$table`")
            val rename = sql.indexOf("ALTER TABLE `${table}_new` RENAME TO `$table`")
            assertTrue("$table: $create $insert $drop $rename", create in 0 until insert && insert < drop && drop < rename)
        }
    }
}
