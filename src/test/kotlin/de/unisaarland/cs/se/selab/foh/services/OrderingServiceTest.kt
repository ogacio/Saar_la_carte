package de.unisaarland.cs.se.selab.foh.services

import de.unisaarland.cs.se.selab.foh.Table
import de.unisaarland.cs.se.selab.foh.Waiter
import de.unisaarland.cs.se.selab.foh.WaiterAssignmentService
import de.unisaarland.cs.se.selab.foh.visit.Visit
import de.unisaarland.cs.se.selab.sharedPackage.Ingredient
import de.unisaarland.cs.se.selab.sharedPackage.Menu
import de.unisaarland.cs.se.selab.sharedPackage.Pantry
import de.unisaarland.cs.se.selab.sharedPackage.RecipeIngredient
import de.unisaarland.cs.se.selab.sharedPackage.RestaurantType
import de.unisaarland.cs.se.selab.sharedPackage.TableType
import de.unisaarland.cs.se.selab.sharedPackage.UnitType
import de.unisaarland.cs.se.selab.sharedPackage.customers.CustomerStatus
import de.unisaarland.cs.se.selab.sharedPackage.customers.FoodPreference
import de.unisaarland.cs.se.selab.testsupport.Fixtures.captureLog
import de.unisaarland.cs.se.selab.testsupport.Fixtures.casual
import de.unisaarland.cs.se.selab.testsupport.Fixtures.logLines
import de.unisaarland.cs.se.selab.testsupport.Fixtures.recipe
import de.unisaarland.cs.se.selab.testsupport.Fixtures.regular
import de.unisaarland.cs.se.selab.testsupport.Fixtures.subUnits
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * F18: ordering. Only the paths that end without an order are tested here: creating an order
 * still stops at the TODO in OrderingService.buildOrder.
 */
class OrderingServiceTest {

    private val menu = mock<Menu>()
    private val pantry = mock<Pantry>()
    private val sbu = subUnits(menu = menu, pantry = pantry)
    private val service = OrderingService(WaiterAssignmentService(mutableListOf()), RestaurantType.EUROPEAN)

    private fun seatedVisit(visit: Visit, waiter: Waiter): Visit {
        waiter.id = 1
        waiter.adjustLoad(visit.customersInside().size)
        visit.seated(Table(4, visit.group.groupSize(), TableType.COMMON), listOf(waiter), 1)
        return visit
    }

    @Test
    fun groupThatIsNotSeatedDoesNotOrder() {
        val visit = Visit(regular(3, 2))
        val log = captureLog()

        service.takeOrder(visit, sbu, 1)

        assertTrue(logLines(log).isEmpty())
        verify(menu, never()).refresh()
    }

    @Test
    fun emptyMenuSendsTheWholeGroupAway() {
        whenever(menu.getOrderables()).thenReturn(emptyList())
        val waiter = Waiter()
        val visit = seatedVisit(Visit(regular(3, 2)), waiter)
        val log = captureLog()

        service.takeOrder(visit, sbu, 2)

        assertEquals(
            listOf(
                "[IMPORTANT] FOH No Ordering (R 1): Group 3 could not place an order for 2 customers, " +
                    "they leave the restaurant.",
            ),
            logLines(log),
        )
        assertTrue(visit.isFinished())
        assertTrue(visit.group.members().all { it.status() == CustomerStatus.LEFT })
        assertEquals(0, waiter.currentLoad)
    }

    @Test
    fun customerWhosePreferenceExcludesEveryDishLeaves() {
        val fish = Ingredient("fish", UnitType.G, 100, 1)
        whenever(menu.getOrderables()).thenReturn(listOf(recipe(1, listOf(RecipeIngredient(fish, 50)))))
        val noFish = FoodPreference(1, setOf(fish), emptySet(), emptyList())
        val visit = seatedVisit(Visit(casual(3, 1, preference = noFish)), Waiter())
        val log = captureLog()

        service.takeOrder(visit, sbu, 2)

        assertEquals(1, logLines(log).size)
        assertTrue(visit.isFinished())
        verify(pantry, never()).reserve(any())
    }

    @Test
    fun customersLeaveWhenThePantryCannotReserveTheirDish() {
        val dish = recipe(1)
        whenever(menu.getOrderables()).thenReturn(listOf(dish))
        whenever(pantry.reserve(dish)).thenReturn(false)
        val visit = seatedVisit(Visit(casual(3, 2)), Waiter())
        val log = captureLog()

        service.takeOrder(visit, sbu, 2)

        assertEquals(
            listOf(
                "[IMPORTANT] FOH No Ordering (R 1): Group 3 could not place an order for 2 customers, " +
                    "they leave the restaurant.",
            ),
            logLines(log),
        )
        assertTrue(visit.isFinished())
    }

    @Test
    fun statusCountsNothingWhenNobodyOrdered() {
        whenever(menu.getOrderables()).thenReturn(emptyList())
        service.takeOrder(seatedVisit(Visit(regular(3, 2)), Waiter()), sbu, 2)
        val log = captureLog()

        service.logStatus(sbu)

        assertEquals(
            listOf(
                "[DEBUG] FOH Ordering Status (R 1): The restaurant received orders from 0 customers, " +
                    "0 waitstaff took orders.",
            ),
            logLines(log),
        )
    }
}
