package de.unisaarland.cs.se.selab.simulation

import de.unisaarland.cs.se.selab.sharedPackage.Recipe
import de.unisaarland.cs.se.selab.sharedPackage.RestaurantData
import de.unisaarland.cs.se.selab.sharedPackage.RestaurantType
import de.unisaarland.cs.se.selab.sharedPackage.TableType
import de.unisaarland.cs.se.selab.simulation.ratings.RatingBook
import de.unisaarland.cs.se.selab.testsupport.Fixtures.casual
import de.unisaarland.cs.se.selab.testsupport.Fixtures.event
import de.unisaarland.cs.se.selab.testsupport.Fixtures.recipe
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertSame

/**
 * F28: the best-rated restaurant wins even when it is not the first entry, for casual groups and for
 * events alike, and its capacity is booked. [RatingBook] is a singleton, so the restaurant ids are
 * this test's own.
 */
class BrowsingServiceWinnerTest {

    private fun restaurant(id: Int, hostsEvents: Boolean = false, dishes: List<Recipe> = listOf(recipe(1))) =
        RestaurantData(
            id,
            RestaurantType.EUROPEAN,
            2,
            20,
            dishes.toMutableList(),
            mutableMapOf(TableType.COMMON to 10),
            1,
            hostsEvents,
            10,
            mutableMapOf(),
        )

    @BeforeTest
    fun openAtTickFive() {
        GlobalClock.advanceEvening()
        repeat(5) { GlobalClock.advanceTick() }
    }

    @Test
    fun aBetterRatedRestaurantFurtherDownTheListIsChosenAndBooked() {
        RatingBook.initializeRatings(LOW, 0, 0)
        RatingBook.initializeRatings(HIGH, 3, 0)
        val better = restaurant(HIGH)
        val service = BrowsingService(mutableListOf(restaurant(LOW), better), RatingBook)

        assertEquals(HIGH, service.choose(casual(1, 2)))
        assertEquals(8, better.getFreeSeats()[TableType.COMMON])
    }

    @Test
    fun aBetterRatedEventRestaurantFurtherDownTheListIsChosen() {
        RatingBook.initializeRatings(EVENT_LOW, 0, 0)
        RatingBook.initializeRatings(EVENT_HIGH, 3, 0)
        val service = BrowsingService(
            mutableListOf(restaurant(EVENT_LOW, hostsEvents = true), restaurant(EVENT_HIGH, hostsEvents = true)),
            RatingBook,
        )

        assertEquals(EVENT_HIGH, service.chooseForEvent(event(1, 4, eventEvening = 7, visitingTick = 5), 7))
    }

    @Test
    fun theEntriesAndTheRatingBookCanBeReplaced() {
        val service = BrowsingService(mutableListOf(), RatingBook)
        val snapshot = mutableListOf(restaurant(LOW))

        service.refresh(snapshot)
        service.ratings = RatingBook

        assertSame(snapshot, service.entries)
        assertSame(RatingBook, service.ratings)
    }

    private companion object {
        const val LOW = 1181
        const val HIGH = 1182
        const val EVENT_LOW = 1183
        const val EVENT_HIGH = 1184
    }
}
