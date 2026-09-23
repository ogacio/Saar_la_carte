package de.unisaarland.cs.se.selab.simulation

import de.unisaarland.cs.se.selab.foh.FrontOfTheHouse
import de.unisaarland.cs.se.selab.kitchen.Kitchen
import de.unisaarland.cs.se.selab.sharedPackage.Menu
import de.unisaarland.cs.se.selab.sharedPackage.Pantry
import de.unisaarland.cs.se.selab.sharedPackage.RestaurantData
import de.unisaarland.cs.se.selab.sharedPackage.RestaurantType
import de.unisaarland.cs.se.selab.sharedPackage.TableType
import org.mockito.kotlin.any
import org.mockito.kotlin.eq
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

    private fun restaurant(
        foh: FrontOfTheHouse,
        kitchen: Kitchen,
        closingTick: Int,
        openingTick: Int = 1,
    ): Restaurant {
        val data = RestaurantData(
            1,
            RestaurantType.EUROPEAN,
            openingTick,
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
        verify(foh).callSeatingAndOrdering(any(), any(), any(), any())
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

    /**
     * "In the ticks after a restaurant's openingTickEnd, the simulation logs only some action or
     * action status messages": the deliveries, the eating and the ratings keep running, the rest
     * of the front of house does not.
     */
    @Test
    fun afterTheOpeningTimeDeliveriesEatingAndRatingsKeepWorking() {
        val foh = mock<FrontOfTheHouse>()
        val kitchen = mock<Kitchen>()
        val restaurant = restaurant(foh, kitchen, closingTick = 2)
        clockAt(5)

        restaurant.runRestaurantTick(emptyList(), 5)

        verify(foh).callDeliveryDesk()
        verify(foh).callDiningService()
        verify(foh).callRatingService()
        verify(foh, never()).beginTick()
        verify(foh, never()).callSeatingAndOrdering(any(), any(), any(), any())
        verify(kitchen, never()).cook()
        verify(foh, never()).callServingService()
        verify(foh, never()).callEscortingService()
    }

    /**
     * "In the ticks before a restaurant's openingTickStart, the simulation will not log any action
     * or action status messages", so nothing but the start and end of the tick happens.
     */
    @Test
    fun beforeTheOpeningTimeNothingHappensAtAll() {
        val foh = mock<FrontOfTheHouse>()
        val kitchen = mock<Kitchen>()
        val restaurant = restaurant(foh, kitchen, openingTick = 5, closingTick = 10)
        clockAt(2)

        restaurant.runRestaurantTick(emptyList(), 2)

        verify(foh, never()).callDeliveryDesk()
        verify(foh, never()).callDiningService()
        verify(foh, never()).callRatingService()
        verify(kitchen, never()).cook()
    }

    /**
     * "A restaurant does not accept new customers in the last 3 ticks of their opening time. This
     * includes customers that arrived in the previous tick but could not be seated."
     */
    @Test
    fun inTheLastThreeTicksNobodyIsSeatedAnyMore() {
        val foh = mock<FrontOfTheHouse>()
        val restaurant = restaurant(foh, mock<Kitchen>(), closingTick = 10)
        clockAt(8)

        restaurant.runRestaurantTick(emptyList(), 8)

        verify(foh).callSeatingAndOrdering(any(), eq(false), any(), any())
    }
}
