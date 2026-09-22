package de.unisaarland.cs.se.selab.foh.services

import de.unisaarland.cs.se.selab.foh.Table
import de.unisaarland.cs.se.selab.foh.Waiter
import de.unisaarland.cs.se.selab.foh.visit.Visit
import de.unisaarland.cs.se.selab.sharedPackage.Meal
import de.unisaarland.cs.se.selab.sharedPackage.MealStatus
import de.unisaarland.cs.se.selab.sharedPackage.Order
import de.unisaarland.cs.se.selab.sharedPackage.TableType
import de.unisaarland.cs.se.selab.sharedPackage.customers.CustomerGroup
import de.unisaarland.cs.se.selab.simulation.GlobalClock
import de.unisaarland.cs.se.selab.testsupport.Fixtures.RESTAURANT_ID
import de.unisaarland.cs.se.selab.testsupport.Fixtures.captureLog
import de.unisaarland.cs.se.selab.testsupport.Fixtures.logLines
import de.unisaarland.cs.se.selab.testsupport.Fixtures.recipe
import de.unisaarland.cs.se.selab.testsupport.Fixtures.regular
import de.unisaarland.cs.se.selab.testsupport.Fixtures.subUnits
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * The eating step of the specification: "customers ... wait for up to 5 ticks for their food to be
 * SERVED. If they haven't received food until then, the group leaves immediately. If at least one
 * person on the table has received their meal, they wait for 2 more ticks", and "once a customer has
 * received their meal, they take 2 full ticks to eat it". The log order of the step is: all No Eating
 * lines, then all Finished Eating lines, then the status line.
 */
class DiningServiceTest {

    private val sbu = subUnits()
    private val dining = DiningService()

    private fun nextTick(): Int {
        GlobalClock.advanceTick()
        return GlobalClock.currentTick
    }

    private fun wait(ticks: Int) = repeat(ticks) { GlobalClock.advanceTick() }

    /** A seated group that ordered at [tick]; every meal is cooked but nothing has been served yet. */
    private fun waitingVisit(group: CustomerGroup, tick: Int, tableId: Int = 2): Visit {
        val visit = Visit(group)
        visit.seated(Table(tableId, group.groupSize(), TableType.COMMON), listOf(Waiter()), tick)
        val meals = group.members().map { Meal(null, it, recipe(1)) }.toMutableList()
        val order = Order(group, RESTAURANT_ID, group.id(), tick, false, meals)
        meals.forEach {
            it.orderId = order.getId()
            it.status = MealStatus.COOKED
        }
        visit.ordered(order, tick)
        return visit
    }

    @Test
    fun groupWithoutAnyFoodLeavesAfterFiveTicks() {
        val ordered = nextTick()
        val visit = waitingVisit(regular(3, 2), ordered)
        wait(PATIENCE)
        val log = captureLog()

        dining.eat(listOf(visit), sbu)

        assertEquals(
            listOf(
                "[INFO] Restaurant No Eating (R 1): 2 customers of group 3 leave table 2 due to not being served.",
                "[DEBUG] FOH Eating Status (R 1): 0 customers are eating and " +
                    "0 customers have finished eating this tick.",
            ),
            logLines(log),
        )
        assertTrue(visit.isFinished())
    }

    @Test
    fun groupWithoutFoodStaysUntilTheFifthTick() {
        val ordered = nextTick()
        val visit = waitingVisit(regular(3, 2), ordered)
        wait(PATIENCE - 2)
        val log = captureLog()

        dining.eat(listOf(visit), sbu)

        assertEquals(
            listOf(
                "[DEBUG] FOH Eating Status (R 1): 0 customers are eating and " +
                    "0 customers have finished eating this tick.",
            ),
            logLines(log),
        )
        assertTrue(!visit.isFinished())
    }

    @Test
    fun customersStillWithoutFoodLeaveTwoTicksAfterTheFirstMealWasServed() {
        val ordered = nextTick()
        val group = regular(3, 3)
        val visit = waitingVisit(group, ordered)
        val served = visit.cookedMeals().take(1)
        visit.serve(served, ordered)
        wait(PATIENCE + EXTRA)
        val log = captureLog()

        dining.eat(listOf(visit), sbu)

        assertTrue(
            logLines(log).any {
                it == "[INFO] Restaurant No Eating (R 1): 2 customers of group 3 leave table 2 due to not being served."
            },
        )
        assertEquals(1, visit.customersInside().size)
    }

    @Test
    fun customersNeedTwoFullTicksToEatAndAreLoggedThen() {
        val ordered = nextTick()
        val visit = waitingVisit(regular(3, 2), ordered)
        visit.serve(visit.cookedMeals(), ordered)
        val duringEating = captureLog()

        dining.eat(listOf(visit), sbu)
        assertEquals(
            listOf(
                "[DEBUG] FOH Eating Status (R 1): 2 customers are eating and " +
                    "0 customers have finished eating this tick.",
            ),
            logLines(duringEating),
        )

        wait(EATING_TICKS)
        val afterEating = captureLog()
        dining.eat(listOf(visit), sbu)

        assertEquals(
            listOf(
                "[INFO] FOH Finished Eating (R 1): 2 customers of group 3 have finished eating at table 2.",
                "[DEBUG] FOH Eating Status (R 1): 0 customers are eating and " +
                    "2 customers have finished eating this tick.",
            ),
            logLines(afterEating),
        )
    }

    @Test
    fun allLeavingLinesComeBeforeAllFinishedEatingLines() {
        val ordered = nextTick()
        val leaving = waitingVisit(regular(1, 2), ordered, tableId = 1)
        val eating = waitingVisit(regular(2, 2), ordered, tableId = 2)
        eating.serve(eating.cookedMeals(), ordered)
        wait(PATIENCE)
        val log = captureLog()

        dining.eat(listOf(leaving, eating), sbu)

        assertEquals(
            listOf(
                "[INFO] Restaurant No Eating (R 1): 2 customers of group 1 leave table 1 due to not being served.",
                "[INFO] FOH Finished Eating (R 1): 2 customers of group 2 have finished eating at table 2.",
                "[DEBUG] FOH Eating Status (R 1): 0 customers are eating and " +
                    "2 customers have finished eating this tick.",
            ),
            logLines(log),
        )
    }

    private companion object {
        /** "customers ... wait for up to 5 ticks for their food to be SERVED." */
        const val PATIENCE = 5

        /** "If at least one person on the table has received their meal, they wait for 2 more ticks." */
        const val EXTRA = 2

        /** "Once a customer has received their meal, they take 2 full ticks to eat it." */
        const val EATING_TICKS = 2
    }
}
