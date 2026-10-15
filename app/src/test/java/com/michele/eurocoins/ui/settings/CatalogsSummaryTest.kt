package com.michele.eurocoins.ui.settings

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

/**
 * La riga "Microstates, start tab and German mints" delle Impostazioni: il titolo dice cosa c'è dentro e, SOLO se
 * qualcosa è nascosto, una riga sotto lo dice (cambia ciò che si vede e i totali).
 */
class CatalogsSummaryTest {

    @Test
    fun theTitleNamesWhatIsInsideAndIsTheSameForFreeAndPro() {
        // Le zecche si nominano anche da Free (bloccate): è l'unico posto dove si scopre che esistono.
        assertEquals("Microstates, start tab and German mints", CATALOGS_ROW_TITLE)
    }

    @Test
    fun nothingIsSaidWhenNothingIsHidden() {
        assertNull(catalogsHiddenNote(hideCommemorative = false, hideRegular = false, germanMintsHidden = false))
    }

    @Test
    fun hiddenMicrostatesNameTheCatalogs() {
        assertEquals("Microstates hidden in Commemorative", catalogsHiddenNote(true, false, false))
        assertEquals("Microstates hidden in Regular Issues", catalogsHiddenNote(false, true, false))
        assertEquals("Microstates hidden in both", catalogsHiddenNote(true, true, false))
    }

    @Test
    fun hiddenGermanMintsAreNamedAfterTheMicrostates() {
        assertEquals("German mints hidden", catalogsHiddenNote(false, false, true))
        assertEquals("Microstates hidden in both\nGerman mints hidden", catalogsHiddenNote(true, true, true))
    }
}
