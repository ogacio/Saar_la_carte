package de.unisaarland.cs.se.selab.simulation

import de.unisaarland.cs.se.selab.foh.FrontOfTheHouse
import de.unisaarland.cs.se.selab.kitchen.Kitchen
import de.unisaarland.cs.se.selab.sharedPackage.Menu
import de.unisaarland.cs.se.selab.sharedPackage.Pantry
import de.unisaarland.cs.se.selab.sharedPackage.RestaurantData
import de.unisaarland.cs.se.selab.sharedPackage.RestaurantType
import de.unisaarland.cs.se.selab.sharedPackage.TableType
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import kotlin.test.Test

/**
 * One tick of a restaurant. While it is open, the seven steps of the specification run in order. After
 * the opening time "the kitchen stops working" and the front of house is cleaned, so only the drivers
 * keep working: "only deliveries already given to a driver continue after the opening time".
 */
class RestaurantTickTest {

    private fun restaurant(foh: FrontOfTheHouse, kitchen: Kitchen, closingTick: Int): Restaurant {
        val data = RestaurantData(
            1,
            RestaurantType.EUROPEAN,
            1,
            closingTick,
            emptyList(),
            mapOf(TableType.COMMON to 4),
            0,
            false,
            4,
            mapOf(),
        )
        return Restaurant("Test", false, 0, 0, foh, kitchen, mock<Pantry>(), mock<Menu>(), data)
    }

    /** Starts a fresh evening and moves the clock to tick [tickInEvening] of it. */
    private fun clockAt(tickInEvening: Int) {
        GlobalClock.advanceEvening()
        repeat(tickInEvening) { GlobalClock.advanceTick() }
    }

    @Test
    fun whileTheRestaurantIsOpenEveryStepRuns() {
        val foh = mock<FrontOfTheHouse>()
        val kitchen = mock<Kitchen>()
        val restaurant = restaurant(foh, kitchen, closingTick = 10)
        clockAt(3)

        restaurant.runRestaurantTick(emptyList(), 3)

        verify(foh).beginTick()
        verify(foh).callSeatingAndOrdering(any())
        verify(kitchen).cook()
        verify(foh).callServingService()
        verify(foh).callDeliveryDesk()
        verify(foh).callDiningService()
        verify(foh).callEscortingService()
        verify(foh).callRatingService()
        verify(foh, never()).closeOpeningTime()
    }

    @Test
    fun atTheClosingTickEveryoneIsSentOutBeforeTheRating() {
        val foh = mock<FrontOfTheHouse>()
        val restaurant = restaurant(foh, mock<Kitchen>(), closingTick = 4)
        clockAt(4)

        restaurant.runRestaurantTick(emptyList(), 4)

        verify(foh).closeOpeningTime()
        verify(foh).callRatingService()
    }

    @Test
    fun afterTheOpeningTimeOnlyTheDriversAndRatingKeepWorking() {
        val foh = mock<FrontOfTheHouse>()
        val kitchen = mock<Kitchen>()
        val restaurant = restaurant(foh, kitchen, closingTick = 2)
        clockAt(5)

        restaurant.runRestaurantTick(emptyList(), 5)

        verify(foh).callDeliveryDesk()
        verify(foh).callRatingService()
        verify(foh, never()).beginTick()
        verify(foh, never()).callSeatingAndOrdering(any())
        verify(kitchen, never()).cook()
        verify(foh, never()).callServingService()
        verify(foh, never()).callDiningService()
        verify(foh, never()).callEscortingService()
    }
}
