package de.unisaarland.cs.se.selab.foh.services

import de.unisaarland.cs.se.selab.foh.ActionType
import de.unisaarland.cs.se.selab.foh.Table
import de.unisaarland.cs.se.selab.foh.Waiter
import de.unisaarland.cs.se.selab.foh.WaiterAssignmentService
import de.unisaarland.cs.se.selab.foh.visit.Visit
import de.unisaarland.cs.se.selab.sharedPackage.Menu
import de.unisaarland.cs.se.selab.sharedPackage.Pantry
import de.unisaarland.cs.se.selab.sharedPackage.RestaurantType
import de.unisaarland.cs.se.selab.sharedPackage.TableType
import de.unisaarland.cs.se.selab.sharedPackage.customers.EventCustomerGroup
import de.unisaarland.cs.se.selab.sharedPackage.customers.FoodPreference
import de.unisaarland.cs.se.selab.testsupport.Fixtures.captureLog
import de.unisaarland.cs.se.selab.testsupport.Fixtures.casual
import de.unisaarland.cs.se.selab.testsupport.Fixtures.logLines
import de.unisaarland.cs.se.selab.testsupport.Fixtures.members
import de.unisaarland.cs.se.selab.testsupport.Fixtures.recipe
import de.unisaarland.cs.se.selab.testsupport.Fixtures.subUnits
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * F18: ordering - the success path. [OrderingServiceTest] covers the paths that end without an
 * order; this file covers dish precedence, order construction and waiter booking once an order
 * is actually built. Kept separate rather than extending Teodor's file directly.
 */
class OrderingServiceOrderedTest {

    private val menu = mock<Menu>()
    private val pantry = mock<Pantry>()
    private val sbu = subUnits(menu = menu, pantry = pantry)

    private fun seatedVisit(visit: Visit, vararg waiters: Waiter): Visit {
        waiters.forEachIndexed { index, waiter -> waiter.id = index + 1 }
        waiters.forEach { it.adjustLoad(visit.customersInside().size) }
        visit.seated(Table(4, visit.group.groupSize(), TableType.COMMON), waiters.toList(), 1)
        return visit
    }

    @Test
    fun customerOrdersTheirFirstFavouriteDishOverTheHigherIdFallback() {
        val favourite = recipe(1)
        val higherId = recipe(2)
        whenever(pantry.reserve(any())).thenReturn(true)
        whenever(menu.getOrderables()).thenReturn(listOf(favourite, higherId))
        val preference = FoodPreference(1, emptySet(), emptySet(), listOf(favourite.getDishName()))
        val waiter = Waiter()
        val visit = seatedVisit(Visit(casual(3, 1, preference = preference)), waiter)
        val service = OrderingService(WaiterAssignmentService(mutableListOf(waiter)), RestaurantType.EUROPEAN)

        service.takeOrder(visit, sbu, 2)

        assertEquals(favourite, visit.order?.getMeals()?.get(0)?.recipe)
    }

    @Test
    fun noPreferenceFallsBackToTheHighestRecipeId() {
        val low = recipe(1)
        val high = recipe(2)
        whenever(pantry.reserve(any())).thenReturn(true)
        whenever(menu.getOrderables()).thenReturn(listOf(low, high))
        val waiter = Waiter()
        val visit = seatedVisit(Visit(casual(3, 1)), waiter)
        val service = OrderingService(WaiterAssignmentService(mutableListOf(waiter)), RestaurantType.EUROPEAN)

        service.takeOrder(visit, sbu, 2)

        assertEquals(high, visit.order?.getMeals()?.get(0)?.recipe)
    }

    @Test
    fun eventOverrideDishTakesPrecedenceOverThePersonalFavourite() {
        val personalFavourite = recipe(1)
        val eventDish = recipe(2)
        whenever(pantry.reserve(any())).thenReturn(true)
        whenever(menu.getOrderables()).thenReturn(listOf(personalFavourite, eventDish))
        val preference = FoodPreference(1, emptySet(), emptySet(), listOf(personalFavourite.getDishName()))
        val group = EventCustomerGroup(
            3,
            1,
            TableType.COMMON,
            1,
            members(1, preference),
            listOf(preference),
            setOf(RestaurantType.EUROPEAN),
            4,
            mapOf(RestaurantType.EUROPEAN to eventDish.getDishName()),
        )
        val waiter = Waiter()
        val visit = seatedVisit(Visit(group), waiter)
        val service = OrderingService(WaiterAssignmentService(mutableListOf(waiter)), RestaurantType.EUROPEAN)

        service.takeOrder(visit, sbu, 2)

        assertEquals(eventDish, visit.order?.getMeals()?.get(0)?.recipe)
    }

    @Test
    fun orderIsBuiltWithOneMealPerCustomerAndTheOrderingLineIsLogged() {
        val dish = recipe(1)
        whenever(pantry.reserve(dish)).thenReturn(true)
        whenever(menu.getOrderables()).thenReturn(listOf(dish))
        val waiter = Waiter()
        val visit = seatedVisit(Visit(casual(3, 2)), waiter)
        val service = OrderingService(WaiterAssignmentService(mutableListOf(waiter)), RestaurantType.EUROPEAN)
        val log = captureLog()

        service.takeOrder(visit, sbu, 2)

        assertEquals(2, visit.order?.getMeals()?.size)
        assertTrue(logLines(log).any { it.contains("FOH Ordering") && it.contains("waitstaff 1") })
    }

    @Test
    fun waiterConsumesOrderingActionsForNonEventGroups() {
        val dish = recipe(1)
        whenever(pantry.reserve(dish)).thenReturn(true)
        whenever(menu.getOrderables()).thenReturn(listOf(dish))
        val waiter = Waiter()
        val visit = seatedVisit(Visit(casual(3, 3)), waiter)
        val service = OrderingService(WaiterAssignmentService(mutableListOf(waiter)), RestaurantType.EUROPEAN)

        service.takeOrder(visit, sbu, 2)

        assertEquals(Waiter.ACTION_LIMIT - 3, waiter.remaining(ActionType.ORDERING))
    }

    @Test
    fun eventGroupOrderNamesEveryWaiterThatTookIt() {
        val dish = recipe(1)
        whenever(pantry.reserve(dish)).thenReturn(true)
        whenever(menu.getOrderables()).thenReturn(listOf(dish))
        val group = EventCustomerGroup(
            3,
            12,
            TableType.COMMON,
            1,
            members(12),
            emptyList(),
            setOf(RestaurantType.EUROPEAN),
            4,
            emptyMap(),
        )
        val first = Waiter()
        val second = Waiter()
        val visit = Visit(group)
        visit.seated(Table(4, 12, TableType.COMMON), emptyList(), 1)
        val service = OrderingService(WaiterAssignmentService(mutableListOf(first, second)), RestaurantType.EUROPEAN)
        val log = captureLog()

        service.takeOrder(visit, sbu, 2)

        assertEquals(12, visit.order?.getMeals()?.size)
        // "the orders are logged ... based on the id of the group, the waiter (one or more) and the order":
        // twelve customers need two waiters, so both of them appear, in ascending id
        assertTrue(logLines(log).any { it.contains("FOH Ordering") && it.endsWith("with waitstaff 1,2.") })
    }

    @Test
    fun menuRefreshesOnceMoreForEachSuccessfulChoiceSoAvailabilityUpdatesPerCustomer() {
        val dish = recipe(1)
        whenever(pantry.reserve(dish)).thenReturn(true)
        whenever(menu.getOrderables()).thenReturn(listOf(dish))
        val waiter = Waiter()
        val visit = seatedVisit(Visit(casual(3, 2)), waiter)
        val service = OrderingService(WaiterAssignmentService(mutableListOf(waiter)), RestaurantType.EUROPEAN)

        service.takeOrder(visit, sbu, 2)

        verify(menu, times(3)).refresh()
    }

    @Test
    fun oneCustomerFailingStillLetsTheRestOrderInTheSameTick() {
        val dish = recipe(1)
        whenever(pantry.reserve(dish)).thenReturn(true).thenReturn(false)
        whenever(menu.getOrderables()).thenReturn(listOf(dish))
        val waiter = Waiter()
        val visit = seatedVisit(Visit(casual(3, 2)), waiter)
        val service = OrderingService(WaiterAssignmentService(mutableListOf(waiter)), RestaurantType.EUROPEAN)
        val log = captureLog()

        service.takeOrder(visit, sbu, 2)

        assertTrue(logLines(log).any { it.contains("FOH Ordering") })
        assertTrue(logLines(log).any { it.contains("FOH No Ordering") && it.contains(" 1 ") })
        assertEquals(1, visit.order?.getMeals()?.size)
    }

    @Test
    fun orderIdsAreUniqueAcrossSeparateCalls() {
        val dish = recipe(1)
        whenever(pantry.reserve(dish)).thenReturn(true)
        whenever(menu.getOrderables()).thenReturn(listOf(dish))
        val waiterA = Waiter()
        val visitA = seatedVisit(Visit(casual(3, 1)), waiterA)
        val serviceA = OrderingService(WaiterAssignmentService(mutableListOf(waiterA)), RestaurantType.EUROPEAN)
        serviceA.takeOrder(visitA, sbu, 2)
        val firstId = checkNotNull(visitA.order).getId()

        val waiterB = Waiter()
        val visitB = seatedVisit(Visit(casual(4, 1)), waiterB)
        val serviceB = OrderingService(WaiterAssignmentService(mutableListOf(waiterB)), RestaurantType.EUROPEAN)
        serviceB.takeOrder(visitB, sbu, 2)
        val secondId = checkNotNull(visitB.order).getId()

        assertTrue(secondId > firstId)
    }
}
