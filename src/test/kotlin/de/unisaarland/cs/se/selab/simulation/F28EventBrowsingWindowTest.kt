package de.unisaarland.cs.se.selab.simulation

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
import kotlin.test.assertNull

/**
 * F28: the two browsing windows are not the same one. A walk-in is refused in the last three ticks
 * of the opening time ("A restaurant does not accept new customers in the last 3 ticks"), but an
 * EVENT group reserves three evenings ahead and only needs the restaurant to be open at its
 * visitingTick. Event seats are booked per event evening, so two events on different evenings do
 * not compete for the same capacity.
 */
class F28EventBrowsingWindowTest {

    private val ids = generateSequence(9100) { it + 1 }.iterator()

    private fun restaurant(
        id: Int,
        hostsEvents: Boolean = true,
        totalSeats: Int = 20,
        booked: MutableMap<Int, Int> = mutableMapOf(),
    ) = RestaurantData(
        id,
        RestaurantType.EUROPEAN,
        1,
        24,
        mutableListOf(recipe(1)),
        mutableMapOf(TableType.COMMON to 20),
        1,
        hostsEvents,
        totalSeats,
        booked,
    )

    private fun serviceFor(vararg data: RestaurantData): BrowsingService {
        data.forEach { RatingBook.initializeRatings(it.getId(), 0, 0) }
        return BrowsingService(data.toMutableList(), RatingBook)
    }

    @BeforeTest
    fun freshEvening() {
        GlobalClock.advanceEvening()
        GlobalClock.advanceTick()
    }

    @Test
    fun anEventMayBookATickAWalkInWouldBeRefusedIn() {
        val id = ids.next()
        val data = restaurant(id)
        val service = serviceFor(data)
        val lateTick = 23 // inside the last three ticks of a 1..24 opening time

        assertEquals(
            id,
            service.chooseForEvent(event(1, 4, visitingTick = lateTick), 4),
            "an event only needs the restaurant to be open at its visitingTick",
        )
    }

    @Test
    fun anEventIsRefusedWhenTheRestaurantIsClosedAtItsVisitingTick() {
        val id = ids.next()
        val data = RestaurantData(
            id, RestaurantType.EUROPEAN, 5, 10, mutableListOf(recipe(1)),
            mutableMapOf(TableType.COMMON to 20), 1, true, 20, mutableMapOf(),
        )
        val service = serviceFor(data)

        assertNull(service.chooseForEvent(event(2, 4, visitingTick = 12), 4), "the doors are shut by then")
    }

    @Test
    fun aRestaurantThatDoesNotCaterEventsIsNeverChosenForOne() {
        val id = ids.next()
        val service = serviceFor(restaurant(id, hostsEvents = false))

        assertNull(service.chooseForEvent(event(3, 4), 4))
    }

    @Test
    fun anEventIsRefusedWhenTooFewSeatsAreLeftOnItsEvening() {
        val id = ids.next()
        val service = serviceFor(restaurant(id, totalSeats = 10, booked = mutableMapOf(4 to 6)))

        assertNull(service.chooseForEvent(event(4, 6), 4), "only four seats are left on evening 4")
        assertEquals(id, service.chooseForEvent(event(5, 4), 4), "four still fit")
    }

    @Test
    fun bookingAnEventReducesTheSeatsLeftOnThatEveningOnly() {
        val id = ids.next()
        val data = restaurant(id, totalSeats = 20)
        val service = serviceFor(data)

        service.chooseForEvent(event(6, 8), 4)

        assertEquals(12, data.eventSeatsLeft(4), "eight seats are now booked for evening 4")
        assertEquals(20, data.eventSeatsLeft(5), "another evening is untouched")
    }

    @Test
    fun aWalkInIsStillRefusedInTheLastThreeTicks() {
        val id = ids.next()
        val service = serviceFor(restaurant(id))
        repeat(22) { GlobalClock.advanceTick() } // tickInEvening = 23

        assertNull(service.choose(casual(7, 2)), "no new customers in the last three ticks")
    }
}
