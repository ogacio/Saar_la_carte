package de.unisaarland.cs.se.selab.simulation

/* F28: choosing between restaurants when several of them would do. */

import de.unisaarland.cs.se.selab.sharedPackage.RestaurantData
import de.unisaarland.cs.se.selab.sharedPackage.RestaurantType
import de.unisaarland.cs.se.selab.sharedPackage.TableType
import de.unisaarland.cs.se.selab.simulation.ratings.RatingBook
import de.unisaarland.cs.se.selab.testsupport.Fixtures.casual
import de.unisaarland.cs.se.selab.testsupport.Fixtures.recipe
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class F28BrowsingChoiceTest {

    /** The browsing service reads the clock, so every test browses in the first tick of an evening. */
    @BeforeTest
    fun firstTickOfAFreshEvening() {
        GlobalClock.advanceEvening()
        GlobalClock.advanceTick()
    }

    /** A restaurant of [type] that is open all evening and has [seats] common seats. */
    private fun snapshot(id: Int, type: RestaurantType, seats: Int = 10, drivers: Int = 0) = RestaurantData(
        id,
        type,
        1,
        24,
        mutableListOf(recipe(1)),
        mutableMapOf(TableType.COMMON to seats),
        drivers,
        false,
        seats,
        mutableMapOf(),
    )

    private fun ratedWith(id: Int, positive: Int, negative: Int) =
        RatingBook.initializeRatings(id, positive, negative)

    /** A group that accepts several types compares the restaurants of all of them. */
    @Test
    fun aGroupComparesAcrossAllTypesItAccepts() {
        ratedWith(8281, positive = 1, negative = 0)
        ratedWith(8282, positive = 5, negative = 0)
        val browsing = BrowsingService(
            mutableListOf(snapshot(8281, RestaurantType.EUROPEAN), snapshot(8282, RestaurantType.ASIAN)),
            RatingBook,
        )
        val group = casual(1, 2, types = setOf(RestaurantType.EUROPEAN, RestaurantType.ASIAN))

        assertEquals(8282, browsing.choose(group), "the better rated restaurant wins across types")
    }

    /** Ratings can be worse than nothing; the least bad restaurant is still chosen. */
    @Test
    fun theLeastBadRestaurantIsStillChosen() {
        ratedWith(8283, positive = 0, negative = 4)
        ratedWith(8284, positive = 1, negative = 2)
        val browsing = BrowsingService(
            mutableListOf(snapshot(8283, RestaurantType.EUROPEAN), snapshot(8284, RestaurantType.EUROPEAN)),
            RatingBook,
        )

        assertEquals(8284, browsing.choose(casual(2, 2)), "a difference of -1 beats one of -4")
    }

    /** A delivery group needs a free driver, whatever the ratings say. */
    @Test
    fun aDeliveryGroupPrefersTheOnlyRestaurantWithADriver() {
        ratedWith(8285, positive = 9, negative = 0)
        ratedWith(8286, positive = 0, negative = 0)
        val browsing = BrowsingService(
            mutableListOf(
                snapshot(8285, RestaurantType.EUROPEAN, drivers = 0),
                snapshot(8286, RestaurantType.EUROPEAN, drivers = 1),
            ),
            RatingBook,
        )

        assertEquals(8286, browsing.choose(casual(3, 2, deliveryDistance = 5)))
    }

    /** Once the last driver is booked, the next delivery group finds nothing. */
    @Test
    fun theSecondDeliveryGroupFindsNoDriverLeft() {
        ratedWith(8287, positive = 1, negative = 0)
        val browsing = BrowsingService(mutableListOf(snapshot(8287, RestaurantType.EUROPEAN, drivers = 1)), RatingBook)

        assertEquals(8287, browsing.choose(casual(4, 2, deliveryDistance = 5)))
        assertNull(browsing.choose(casual(5, 2, deliveryDistance = 5)), "the only driver is taken")
    }

    /** An eat-in group ignores the drivers and takes the seats instead. */
    @Test
    fun anEatInGroupBooksSeatsAndLeavesTheDriversAlone() {
        ratedWith(8288, positive = 1, negative = 0)
        val snapshot = snapshot(8288, RestaurantType.EUROPEAN, seats = 4, drivers = 1)
        val browsing = BrowsingService(mutableListOf(snapshot), RatingBook)

        assertEquals(8288, browsing.choose(casual(6, 4)))

        assertEquals(1, snapshot.getFreeDrivers(), "an eat-in group does not take a driver")
        assertNull(browsing.choose(casual(7, 4)), "the seats are gone")
    }
}
