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
import de.unisaarland.cs.se.selab.testsupport.Fixtures.captureLog
import de.unisaarland.cs.se.selab.testsupport.Fixtures.event
import de.unisaarland.cs.se.selab.testsupport.Fixtures.logLines
import de.unisaarland.cs.se.selab.testsupport.Fixtures.recipe
import de.unisaarland.cs.se.selab.testsupport.Fixtures.subUnits
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * P03: an EVENT group's orders are taken by the waiters who seated it, each the block of customers
 * it seated (forum thread 266). A waiter whose whole block failed to order takes no order, so it is
 * neither named in the ordering line nor counted in the ordering status, and keeps its actions.
 */
class P03EventOrderingWaiterWithoutOrdersTest {

    private val menu = mock<Menu>()
    private val pantry = mock<Pantry>()
    private val sbu = subUnits(menu = menu, pantry = pantry)

    @Test
    fun aWaiterWhoseSeatedBlockAllFailedTakesNoOrder() {
        val dish = recipe(1)
        whenever(menu.getOrderables()).thenReturn(listOf(dish))
        // The first ten customers get their dish, the pantry runs dry for the last two.
        whenever(pantry.reserve(dish)).thenReturn(true, *Array(FIRST_BLOCK - 1) { true }, false)
        val group = event(3, FIRST_BLOCK + SECOND_BLOCK)
        val first = Waiter().also { it.id = 1 }
        val second = Waiter().also { it.id = 2 }
        val visit = Visit(group)
        visit.seated(Table(4, group.groupSize(), TableType.COMMON), emptyList(), 1)
        visit.eventSeatingPlan = linkedMapOf(first to FIRST_BLOCK, second to SECOND_BLOCK)
        val service = OrderingService(WaiterAssignmentService(mutableListOf(first, second)), RestaurantType.EUROPEAN)
        val log = captureLog()

        service.takeOrder(visit, sbu, 2)
        service.logStatus(sbu)

        val lines = logLines(log)
        assertEquals(FIRST_BLOCK, visit.order?.getMeals()?.size)
        assertTrue(lines.any { it.contains("FOH Ordering (") && it.endsWith("with waitstaff 1.") }, "$lines")
        assertTrue(lines.any { it.contains("FOH No Ordering") && it.contains("for $SECOND_BLOCK customers") })
        assertTrue(
            lines.any { it.endsWith("received orders from $FIRST_BLOCK customers, 1 waitstaff took orders.") },
            "$lines",
        )
        assertEquals(Waiter.ACTION_LIMIT - FIRST_BLOCK, first.remaining(ActionType.ORDERING))
        assertEquals(Waiter.ACTION_LIMIT, second.remaining(ActionType.ORDERING), "the second waiter took nothing")
    }

    private companion object {
        const val FIRST_BLOCK = 10
        const val SECOND_BLOCK = 2
    }
}
