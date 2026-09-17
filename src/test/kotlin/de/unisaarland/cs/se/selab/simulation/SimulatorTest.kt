package de.unisaarland.cs.se.selab.simulation

import de.unisaarland.cs.se.selab.incident.Incident
import de.unisaarland.cs.se.selab.incident.IncidentType
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
import org.mockito.kotlin.never
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * F01: [Simulator]'s own orchestration - evening/tick sequencing, the mid-evening [maxTicks] stop,
 * incident dispatch, and EVENT-before-CASUAL restaurant decisions - with its collaborators
 * ([Restaurant], [CustomerRegistry], [BrowsingService]) mocked so only Simulator's logic is under
 * test, following the same mocked-[Restaurant] pattern already used in `IncidentsTest`.
 */
class SimulatorTest {

    private fun restaurantData(
        id: Int,
        hostsEvents: Boolean = false,
        freeSeats: Map<TableType, Int> = mapOf(TableType.COMMON to 10),
        freeDrivers: Int = 1,
        totalSeats: Int = 10,
    ) = RestaurantData(
        id, RestaurantType.EUROPEAN, 1, 24, emptyList(), freeSeats, freeDrivers, hostsEvents, totalSeats, mapOf(),
    )

    private fun mockRestaurant(id: Int, hostsEvents: Boolean = false): Restaurant {
        val restaurant = mock<Restaurant>()
        whenever(restaurant.id).thenReturn(id)
        whenever(restaurant.snapshot()).thenReturn(restaurantData(id, hostsEvents))
        val menu = mock<Menu>()
        whenever(menu.getOrderables()).thenReturn(emptyList())
        whenever(restaurant.getMenu()).thenReturn(menu)
        whenever(restaurant.getPantry()).thenReturn(mock<Pantry>())
        RatingBook.initializeRatings(id, 0, 0)
        return restaurant
    }

    private fun simulator(
        maxTicks: Int,
        restaurants: MutableList<Restaurant> = mutableListOf(),
        groups: MutableList<CustomerGroup> = mutableListOf(),
        incidents: MutableList<Incident> = mutableListOf(),
    ) = Simulator(
        maxTicks,
        restaurants,
        CustomerRegistry(groups),
        incidents,
        BrowsingService(mutableListOf(), RatingBook),
    )

    @Test
    fun runStopsAfterMaxTicksAndReportsStatisticsOncePerRestaurant() {
        val startTick = GlobalClock.currentTick
        val restaurant = mockRestaurant(101)
        val log = captureLog(LogLevel.INFO)

        simulator(startTick + 2, mutableListOf(restaurant)).run()

        assertEquals(startTick + 2, GlobalClock.currentTick)
        assertTrue(logLines(log).any { it.contains("Simulation started") })
        assertEquals(1, logLines(log).count { it.contains("statistics are calculated") })
    }

    @Test
    fun runEveningStopsMidEveningWithoutServingEndedOrCloseEvening() {
        val startTick = GlobalClock.currentTick
        val restaurant = mockRestaurant(102)
        val log = captureLog(LogLevel.INFO)

        simulator(startTick + 3, mutableListOf(restaurant)).run()

        verify(restaurant, never()).closeEvening()
        assertTrue(logLines(log).none { it.contains("Serving") && it.contains("ends") })
    }

    @Test
    fun runEveningCompletesAllTwentyFourTicksAndClosesWhenMaxTicksAllowsIt() {
        val startTick = GlobalClock.currentTick
        val restaurant = mockRestaurant(103)
        val log = captureLog(LogLevel.INFO)

        simulator(startTick + TICKS_PER_EVENING, mutableListOf(restaurant)).run()

        verify(restaurant, times(1)).closeEvening()
        assertTrue(logLines(log).any { it.contains("Serving") && it.contains("ends") })
    }

    @Test
    fun tickStartedLogsTheEveningNumberNotTheTickWithinEvening() {
        val startTick = GlobalClock.currentTick
        val restaurant = mockRestaurant(107)
        val log = captureLog(LogLevel.IMPORTANT)

        simulator(startTick + 1, mutableListOf(restaurant)).run()

        val evening = GlobalClock.getEvening()
        assertTrue(logLines(log).any { it.contains("Tick") && it.contains("($evening) started") })
    }

    @Test
    fun everyTickCallsRunRestaurantTickOnEachRestaurantInAscendingId() {
        val startTick = GlobalClock.currentTick
        val first = mockRestaurant(5)
        val second = mockRestaurant(2)
        val calls = mutableListOf<Int>()
        whenever(first.runRestaurantTick(any(), any())).then { calls.add(5) }
        whenever(second.runRestaurantTick(any(), any())).then { calls.add(2) }

        simulator(startTick + 1, mutableListOf(first, second)).run()

        assertEquals(listOf(2, 5), calls)
    }

    @Test
    fun prepareIsCalledOnceForEveryRestaurantAtTheStartOfTheEvening() {
        val startTick = GlobalClock.currentTick
        val restaurant = mockRestaurant(104)

        simulator(startTick + 1, mutableListOf(restaurant)).run()

        verify(restaurant, times(1)).prepare(any())
    }

    @Test
    fun applyIncidentsRunsOnlyDueIncidentsInAscendingIdOrderAndConsumesThem() {
        val startTick = GlobalClock.currentTick
        val applied = mutableListOf<Int>()
        val dueLate = mock<Incident>()
        whenever(dueLate.id).thenReturn(2)
        whenever(dueLate.evening).thenReturn(GlobalClock.getEvening() + 100)
        val dueSecond = mock<Incident>()
        whenever(dueSecond.id).thenReturn(3)
        whenever(dueSecond.evening).thenReturn(GlobalClock.getEvening() + 1)
        whenever(dueSecond.type).thenReturn(IncidentType.STAFF)
        whenever(dueSecond.apply(any())).then { applied.add(3) }
        val dueFirst = mock<Incident>()
        whenever(dueFirst.id).thenReturn(1)
        whenever(dueFirst.evening).thenReturn(GlobalClock.getEvening() + 1)
        whenever(dueFirst.type).thenReturn(IncidentType.RECIPE)
        whenever(dueFirst.apply(any())).then { applied.add(1) }

        simulator(
            startTick + 1,
            mutableListOf(mockRestaurant(105)),
            incidents = mutableListOf(dueLate, dueSecond, dueFirst),
        ).run()

        assertEquals(listOf(1, 3), applied)
        verify(dueLate, never()).apply(any())
    }

    @Test
    fun incidentsAreNotAppliedAgainOnceTheirEveningHasPassed() {
        val startTick = GlobalClock.currentTick
        val incident = mock<Incident>()
        whenever(incident.id).thenReturn(1)
        whenever(incident.evening).thenReturn(GlobalClock.getEvening() + 1)
        whenever(incident.type).thenReturn(IncidentType.STAFF)

        simulator(
            startTick + TICKS_PER_EVENING + 1,
            mutableListOf(mockRestaurant(106)),
            incidents = mutableListOf(incident),
        ).run()

        verify(incident, times(1)).apply(any())
    }

    @Test
    fun restaurantsByIdFindsTheMatchingRestaurantOrNull() {
        val restaurant = mockRestaurant(9)
        val sim = simulator(0, mutableListOf(restaurant))

        assertEquals(restaurant, sim.restaurantsById(9))
        assertEquals(null, sim.restaurantsById(999))
    }

    @Test
    fun eventGroupThatBooksSuccessfullyIsRecordedAndLoggedBeforeCasualDecisions() {
        val startTick = GlobalClock.currentTick
        val restaurant = mockRestaurant(201, hostsEvents = true)
        whenever(restaurant.bookEvent(any(), any())).thenReturn(true)
        val nextEvening = GlobalClock.getEvening() + 1
        val eventGroup = event(1, 2, eventEvening = nextEvening + EventLead.DAYS)
        val casualGroup = casual(2, 2, visitingTick = 1, evenings = listOf(nextEvening))
        val log = captureLog(LogLevel.DEBUG)

        simulator(
            startTick + 1,
            mutableListOf(restaurant),
            groups = mutableListOf(eventGroup, casualGroup),
        ).run()

        verify(restaurant, times(1)).bookEvent(eq(eventGroup), any())
        val decisionLines = logLines(log).filter { it.contains("Restaurant Decision") }
        val eventLineIndex = decisionLines.indexOfFirst { it.contains("Group 1 decided") }
        val casualLineIndex = decisionLines.indexOfFirst { it.contains("Group 2 decided") }
        assertTrue(eventLineIndex in decisionLines.indices)
        assertTrue(casualLineIndex in decisionLines.indices)
        assertTrue(eventLineIndex < casualLineIndex)
    }

    @Test
    fun eventGroupIsNotBookedWhenNoRestaurantMatchesAndLogsNoDecision() {
        val startTick = GlobalClock.currentTick
        val eventGroup = event(1, 2, eventEvening = GlobalClock.getEvening() + 1 + EventLead.DAYS)
        val log = captureLog(LogLevel.DEBUG)

        simulator(
            startTick + 1,
            mutableListOf(),
            groups = mutableListOf(eventGroup),
        ).run()

        assertTrue(logLines(log).any { it.contains("could not decide for a restaurant") })
    }

    @Test
    fun nonDeliveryCasualGroupBecomesAWalkInPassedToItsChosenRestaurant() {
        val startTick = GlobalClock.currentTick
        val restaurant = mockRestaurant(301)
        val arrivals = argumentCaptor<List<CustomerGroup>>()
        val nextEvening = GlobalClock.getEvening() + 1
        val walkIn = casual(1, 2, deliveryDistance = 0, visitingTick = 1, evenings = listOf(nextEvening))

        simulator(
            startTick + 1,
            mutableListOf(restaurant),
            groups = mutableListOf(walkIn),
        ).run()

        verify(restaurant).runRestaurantTick(arrivals.capture(), any())
        assertTrue(arrivals.firstValue.contains(walkIn))
    }

    @Test
    fun deliveryCasualGroupIsRoutedToDeliveryServiceInsteadOfBeingSeated() {
        val startTick = GlobalClock.currentTick
        val restaurant = mockRestaurant(302)
        val arrivals = argumentCaptor<List<CustomerGroup>>()
        val nextEvening = GlobalClock.getEvening() + 1
        val deliveryGroup = casual(1, 2, deliveryDistance = 5, visitingTick = 5, evenings = listOf(nextEvening))
        val log = captureLog(LogLevel.DEBUG)

        simulator(
            startTick + 5,
            mutableListOf(restaurant),
            groups = mutableListOf(deliveryGroup),
        ).run()

        verify(restaurant, times(5)).runRestaurantTick(arrivals.capture(), any())
        assertTrue(arrivals.allValues.none { it.contains(deliveryGroup) })
        assertTrue(logLines(log).any { it.contains("Restaurant Decision") })
    }

    private object EventLead {
        const val DAYS = 3
    }

    private companion object {
        const val TICKS_PER_EVENING = 24
    }
}
