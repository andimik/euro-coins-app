package com.michele.eurocoins.ui.settings

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Test

class SetupTourTest {

    @Test
    fun aNewInstallStartsThePendingTour() {
        assertEquals(SetupTour.PENDING, resolveSetupTour(stored = null, hasPriorData = false))
    }

    @Test
    fun anExistingInstallNeverSeesTheTour() {
        // Te, i tester del test interno, chiunque aggiorni l'app: il catalogo è già stato seminato.
        assertEquals(SetupTour.DONE, resolveSetupTour(stored = null, hasPriorData = true))
    }

    @Test
    fun aStoredValueWinsOverTheHeuristic() {
        // Seconda apertura di un'installazione nuova: il seeding ha lasciato l'hash, ma il valore salvato è quello giusto.
        assertEquals(SetupTour.PENDING, resolveSetupTour(stored = "PENDING", hasPriorData = true))
        assertEquals(SetupTour.DONE, resolveSetupTour(stored = "DONE", hasPriorData = false))
    }

    @Test
    fun anUnknownStoredValueFallsBackToTheHeuristic() {
        assertEquals(SetupTour.PENDING, resolveSetupTour(stored = "boh", hasPriorData = false))
        assertEquals(SetupTour.DONE, resolveSetupTour(stored = "", hasPriorData = true))
    }

    @Test
    fun noTourLeavesEverythingAsItIs() {
        assertEquals(TourSpot.NONE, tourSpotFor(TOUR_OFF, TOUR_ACCOUNT))
        assertEquals(TourSpot.NONE, tourSpotFor(TOUR_OFF, TOUR_COLLECTION))
    }

    @Test
    fun theTourLightsOneZoneAndDimsTheOther() {
        assertEquals(TourSpot.LIT, tourSpotFor(TOUR_ACCOUNT, TOUR_ACCOUNT))
        assertEquals(TourSpot.DIM, tourSpotFor(TOUR_ACCOUNT, TOUR_COLLECTION))
        assertEquals(TourSpot.DIM, tourSpotFor(TOUR_COLLECTION, TOUR_ACCOUNT))
        assertEquals(TourSpot.LIT, tourSpotFor(TOUR_COLLECTION, TOUR_COLLECTION))
    }

    @Test
    fun aLitZonesHeaderIsNotFramed() {
        // La cornice è della card; il titolo sopra non si attenua e non si incornicia.
        assertEquals(TourSpot.NONE, headerSpotFor(TourSpot.LIT))
        assertEquals(TourSpot.DIM, headerSpotFor(TourSpot.DIM))
        assertEquals(TourSpot.NONE, headerSpotFor(TourSpot.NONE))
    }

    @Test
    fun theQuickAddStepNamesWhatTheSwitchDoesNow() {
        assertNotEquals(quickAddTourText(true), quickAddTourText(false))
        assertEquals("Quick add is on: one tap marks a coin as owned.", quickAddTourText(true))
    }
}
