package de.unisaarland.cs.se.selab.simulation

import de.unisaarland.cs.se.selab.logging.LogLevel
import de.unisaarland.cs.se.selab.sharedPackage.Menu
import de.unisaarland.cs.se.selab.sharedPackage.Pantry
import de.unisaarland.cs.se.selab.sharedPackage.RestaurantData
import de.unisaarland.cs.se.selab.sharedPackage.RestaurantType
import de.unisaarland.cs.se.selab.sharedPackage.TableType
import de.unisaarland.cs.se.selab.sharedPackage.customers.CustomerGroup
import de.unisaarland.cs.se.selab.simulation.ratings.RatingBook
import de.unisaarland.cs.se.selab.testsupport.Fixtures.captureLog
import de.unisaarland.cs.se.selab.testsupport.Fixtures.casual
import de.unisaarland.cs.se.selab.testsupport.Fixtures.event
import de.unisaarland.cs.se.selab.testsupport.Fixtures.logLines
import org.mockito.kotlin.any
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.eq
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertSame
import kotlin.test.assertTrue

/**
 * F01: the restaurant decisions the [Simulator] makes in a tick when they do not work out - a CASUAL group
 * that finds no restaurant, and an EVENT group whose booking the chosen restaurant refuses. The restaurants
 * are mocked; the ids 401+ are used by no other test.
 */
class SimulatorDecisionTest {

    private fun mockRestaurant(id: Int, hostsEvents: Boolean = false): Restaurant {
        val restaurant = mock<Restaurant>()
        whenever(restaurant.getId()).thenReturn(id)
        whenever(restaurant.snapshot()).thenReturn(
            RestaurantData(
                id,
                RestaurantType.EUROPEAN,
                1,
                TICKS_PER_EVENING,
                emptyList(),
                mapOf(TableType.COMMON to SEATS),
                1,
                hostsEvents,
                SEATS,
                mapOf(),
            ),
        )
        val menu = mock<Menu>()
        whenever(menu.getOrderables()).thenReturn(emptyList())
        whenever(restaurant.getMenu()).thenReturn(menu)
        whenever(restaurant.getPantry()).thenReturn(mock<Pantry>())
        RatingBook.initializeRatings(id, 0, 0)
        return restaurant
    }

    private fun simulator(
        maxTicks: Int,
        restaurants: MutableList<Restaurant>,
        groups: MutableList<CustomerGroup>,
        browsing: BrowsingService = BrowsingService(mutableListOf(), RatingBook),
    ) = Simulator(maxTicks, restaurants, CustomerRegistry(groups), mutableListOf(), browsing)

    @Test
    fun casualGroupWithoutAMatchingRestaurantDecidesForNoneAndArrivesNowhere() {
        val startTick = GlobalClock.currentTick
        val restaurant = mockRestaurant(401)
        val arrivals = argumentCaptor<List<CustomerGroup>>()
        val nextEvening = GlobalClock.getEvening() + 1
        val wantsAsian = casual(1, 2, types = setOf(RestaurantType.ASIAN), evenings = listOf(nextEvening))
        val log = captureLog(LogLevel.DEBUG)

        simulator(startTick + 1, mutableListOf(restaurant), mutableListOf(wantsAsian)).run()

        assertTrue(logLines(log).contains("[DEBUG] Restaurant No Decision: Group 1 could not decide for a restaurant."))
        assertTrue(logLines(log).none { it.contains("Group 1 decided") })
        verify(restaurant).runRestaurantTick(arrivals.capture(), any())
        assertTrue(arrivals.firstValue.isEmpty())
    }

    @Test
    fun eventGroupWhoseBookingIsRefusedStaysUnbooked() {
        val startTick = GlobalClock.currentTick
        val restaurant = mockRestaurant(402, hostsEvents = true)
        whenever(restaurant.bookEvent(any(), any())).thenReturn(false)
        val eventGroup = event(1, 2, eventEvening = GlobalClock.getEvening() + 1 + BOOKING_LEAD)
        val log = captureLog(LogLevel.DEBUG)

        simulator(startTick + 1, mutableListOf(restaurant), mutableListOf(eventGroup)).run()

        assertTrue(logLines(log).contains("[DEBUG] Restaurant Decision: Group 1 decided on restaurant 402."))
        verify(restaurant).bookEvent(eq(eventGroup), eq(eventGroup.getEventEvening()))
        assertNull(eventGroup.bookedRestaurant())
    }

    @Test
    fun theBrowsingServiceHandedInIsTheOneIncidentsSee() {
        val browsing = BrowsingService(mutableListOf(), RatingBook)

        assertSame(browsing, simulator(0, mutableListOf(), mutableListOf(), browsing).getBrowsingService())
    }

    @Test
    fun eventGroupThatIsNotDueYetMakesNoDecision() {
        val startTick = GlobalClock.currentTick
        val restaurant = mockRestaurant(403, hostsEvents = true)
        val eventGroup = event(1, 2, eventEvening = GlobalClock.getEvening() + 2 + BOOKING_LEAD)
        val log = captureLog(LogLevel.DEBUG)

        simulator(startTick + 1, mutableListOf(restaurant), mutableListOf(eventGroup)).run()

        assertEquals(0, logLines(log).count { it.contains("Group 1") })
        assertNull(eventGroup.bookedRestaurant())
    }

    private companion object {
        const val TICKS_PER_EVENING = 24
        const val SEATS = 10
        const val BOOKING_LEAD = 3
    }
}
