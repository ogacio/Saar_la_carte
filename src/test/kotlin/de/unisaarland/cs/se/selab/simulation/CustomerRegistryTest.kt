package de.unisaarland.cs.se.selab.simulation

import de.unisaarland.cs.se.selab.testsupport.Fixtures.casual
import de.unisaarland.cs.se.selab.testsupport.Fixtures.event
import de.unisaarland.cs.se.selab.testsupport.Fixtures.regular
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/** F24: when casual (delivery) and event groups decide on a restaurant. */
class CustomerRegistryTest {

    @Test
    fun eatInGroupDecidesInItsVisitingTickOnItsEvenings() {
        val group = casual(1, 2, evenings = listOf(2), visitingTick = 6)
        val registry = CustomerRegistry(mutableListOf(group))

        assertEquals(listOf(group.id()), registry.deciding(2, 6).map { it.id() })
        assertTrue(registry.deciding(2, 5).isEmpty())
        assertTrue(registry.deciding(3, 6).isEmpty())
    }

    @Test
    fun deliveryGroupDecidesEarlyByTravelTimeAndThreeCookingTicks() {
        val group = casual(1, 2, deliveryDistance = 7, evenings = listOf(2), visitingTick = 6)
        val registry = CustomerRegistry(mutableListOf(group))

        assertEquals(listOf(group.id()), registry.deciding(2, 1).map { it.id() })
        assertTrue(registry.deciding(2, 6).isEmpty())
    }

    @Test
    fun eventGroupDecidesInTheFirstTickThreeEveningsBeforeItsEvent() {
        val group = event(1, 4, eventEvening = 7)
        val registry = CustomerRegistry(mutableListOf(group))

        assertEquals(listOf(group.id()), registry.deciding(4, 1).map { it.id() })
        assertTrue(registry.deciding(4, 2).isEmpty())
        assertTrue(registry.deciding(5, 1).isEmpty())
    }

    @Test
    fun regularGroupsNeverBrowse() {
        val registry = CustomerRegistry(mutableListOf(regular(1, 2)))

        assertTrue(registry.deciding(1, 1).isEmpty())
    }

    @Test
    fun decidingGroupsAreOrderedEventsFirstThenById() {
        val casualLow = casual(1, 2, evenings = listOf(4), visitingTick = 1)
        val casualHigh = casual(5, 2, evenings = listOf(4), visitingTick = 1)
        val eventGroup = event(9, 4, eventEvening = 7)
        val registry = CustomerRegistry(mutableListOf(casualHigh, eventGroup, casualLow))

        assertEquals(listOf(9, 1, 5), registry.deciding(4, 1).map { it.id() })
    }

    @Test
    fun regularsForReturnsOnlyTheHomeRestaurantsVisitorsOfTheEvening() {
        val visiting = regular(1, 2, restaurantId = 1, visitingStart = 2, visitingPeriod = 2)
        val otherRestaurant = regular(2, 2, restaurantId = 2, visitingStart = 2, visitingPeriod = 2)
        val registry = CustomerRegistry(mutableListOf(visiting, otherRestaurant, casual(3, 2, evenings = listOf(4))))

        assertEquals(listOf(visiting.id()), registry.regularsFor(1, 4).map { it.id() })
        assertTrue(registry.regularsFor(1, 3).isEmpty())
    }

    @Test
    fun regularArrivesAtItsHomeRestaurantInItsVisitingTickOnly() {
        val group = regular(1, 2, restaurantId = 1, visitingStart = 2, visitingPeriod = 2)
        val registry = CustomerRegistry(mutableListOf(group))

        assertEquals(listOf(group.id()), registry.arriving(1, 4, group.visitingTick()).map { it.id() })
        assertTrue(registry.arriving(1, 4, group.visitingTick() + 1).isEmpty())
        assertTrue(registry.arriving(1, 3, group.visitingTick()).isEmpty())
        assertTrue(registry.arriving(2, 4, group.visitingTick()).isEmpty())
    }

    @Test
    fun bookedEventGroupArrivesAtItsBookedRestaurantOnItsEventEvening() {
        val booked = event(1, 4, eventEvening = 7, visitingTick = 3).also { it.book(2) }
        val unbooked = event(2, 4, eventEvening = 7, visitingTick = 3)
        val registry = CustomerRegistry(mutableListOf(booked, unbooked))

        assertEquals(listOf(booked.id()), registry.arriving(2, 7, 3).map { it.id() })
        assertTrue(registry.arriving(2, 6, 3).isEmpty())
        assertTrue(registry.arriving(1, 7, 3).isEmpty())
    }

    @Test
    fun casualGroupsNeverArriveOnTheirOwnBecauseTheyDecideFirst() {
        val registry = CustomerRegistry(mutableListOf(casual(1, 2, evenings = listOf(4), visitingTick = 1)))

        assertTrue(registry.arriving(1, 4, 1).isEmpty())
    }
}
