package com.michele.eurocoins.ui.settings

import com.michele.eurocoins.ui.browse.BrowseMode
import com.michele.eurocoins.ui.regular.RegularBrowseMode
import org.junit.Assert.assertEquals
import org.junit.Test

/** Il riepilogo della riga "Catalog options": come sono impostati i cataloghi senza aprire la sottopagina. */
class CatalogsSummaryTest {

    @Test
    fun oneLinePerCatalogWithTheStartingTabWhenNothingIsHidden() {
        assertEquals(
            "Commemorative: Countries\nRegular Issues: Countries",
            catalogsSummary(false, false, BrowseMode.COUNTRIES, RegularBrowseMode.COUNTRIES),
        )
    }

    @Test
    fun theRegularValuesTabIsCalledValuesLikeInTheSelector() {
        assertEquals(
            "Commemorative: Years\nRegular Issues: Values",
            catalogsSummary(false, false, BrowseMode.YEARS, RegularBrowseMode.DENOMINATIONS),
        )
    }

    @Test
    fun hiddenMicrostatesAddAThirdLineNamingTheCatalogs() {
        val tabs = "Commemorative: All\nRegular Issues: All"
        assertEquals("$tabs\nMicrostates hidden in Commemorative", catalogsSummary(true, false, BrowseMode.ALL, RegularBrowseMode.ALL))
        assertEquals("$tabs\nMicrostates hidden in Regular Issues", catalogsSummary(false, true, BrowseMode.ALL, RegularBrowseMode.ALL))
        assertEquals("$tabs\nMicrostates hidden in both", catalogsSummary(true, true, BrowseMode.ALL, RegularBrowseMode.ALL))
    }

    @Test
    fun germanMintsAreNamedOnlyWhenHidden() {
        val tabs = "Commemorative: All\nRegular Issues: All"
        assertEquals(tabs, catalogsSummary(false, false, BrowseMode.ALL, RegularBrowseMode.ALL, germanMintsHidden = false))
        assertEquals("$tabs\nGerman mints hidden", catalogsSummary(false, false, BrowseMode.ALL, RegularBrowseMode.ALL, germanMintsHidden = true))
        assertEquals(
            "$tabs\nMicrostates hidden in both\nGerman mints hidden",
            catalogsSummary(true, true, BrowseMode.ALL, RegularBrowseMode.ALL, germanMintsHidden = true),
        )
    }
}
