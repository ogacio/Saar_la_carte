package de.unisaarland.cs.se.selab.kitchen

import de.unisaarland.cs.se.selab.sharedPackage.Meal
import de.unisaarland.cs.se.selab.sharedPackage.MealStatus
import de.unisaarland.cs.se.selab.sharedPackage.customers.Customer
import de.unisaarland.cs.se.selab.simulation.GlobalClock
import de.unisaarland.cs.se.selab.testsupport.Fixtures.RESTAURANT_ID
import de.unisaarland.cs.se.selab.testsupport.Fixtures.captureLog
import de.unisaarland.cs.se.selab.testsupport.Fixtures.logLines
import de.unisaarland.cs.se.selab.testsupport.Fixtures.recipe
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

/** F11: the roster's guards, its reporting order, and what a STAFF reduction does to a busy cook. */
class F11CookRoasterGuardTest {

    private fun roster(vararg staff: Pair<CookType, Int>) =
        CookRoaster(staff.toMap(), restaurantId = RESTAURANT_ID).also { it.initialiseCooks() }

    private fun meals(count: Int, orderId: Int? = 1) =
        List(count) { Meal(orderId, Customer(null), recipe(1)) }.toMutableList()

    @BeforeTest
    fun freshEvening() {
        GlobalClock.advanceEvening()
        GlobalClock.advanceTick()
    }

    @Test
    fun anEmptyBatchIsNeverAssigned() {
        val r = roster(CookType.EXEC to 1)
        val log = captureLog()

        assertNull(r.startCooking(mutableListOf()), "there is nothing to cook")

        assertTrue(logLines(log).isEmpty(), "and nothing is logged")
    }

    @Test
    fun mealsWithoutAnOrderAreNeverAssigned() {
        val r = roster(CookType.EXEC to 1)
        val log = captureLog()

        assertNull(r.startCooking(meals(2, orderId = null)), "a meal belongs to an order or to nobody")

        assertTrue(logLines(log).isEmpty())
    }

    @Test
    fun noEligibleCookLeavesTheDishInTheQueue() {
        val r = roster(CookType.SOUS to 1) // the fixture recipe is EXEC only

        assertNull(r.startCooking(meals(1)))
    }

    @Test
    fun finishedSkipsCooksWithNothingInThePan() {
        val r = roster(CookType.EXEC to 1)
        assertTrue(r.finished().isEmpty(), "an idle kitchen reports no batches")

        r.startCooking(meals(2))

        assertEquals(1, r.finished().size, "the one busy cook reports its batch")
        assertTrue(r.finished().isEmpty(), "and only once")
    }

    @Test
    fun theStatusCountersFollowThePans() {
        val r = roster(CookType.EXEC to 1)
        assertEquals(0, r.cooksWithAFullPan())
        assertEquals(0, r.mealsInPans())

        r.startCooking(meals(3))

        assertEquals(1, r.cooksWithAFullPan())
        assertEquals(3, r.mealsInPans())
    }

    @Test
    fun aStaffReductionFiresAFreeCookBeforeABusyOne() {
        val r = roster(CookType.EXEC to 2)
        r.startCooking(meals(1))
        assertEquals(1, r.cooksWithAFullPan())

        r.changeStaff(CookType.EXEC, -1)

        assertEquals(1, r.cooks.size)
        assertEquals(1, r.cooksWithAFullPan(), "the cook still holding a batch was kept")
    }

    @Test
    fun firingABusyCookHandsItsMealsBackToTheQueue() {
        val r = roster(CookType.EXEC to 1)
        val batch = meals(2)
        val held = batch.toList()
        r.startCooking(batch)
        assertEquals(listOf(MealStatus.COOKING, MealStatus.COOKING), held.map { it.status })

        r.changeStaff(CookType.EXEC, -1)

        assertTrue(r.cooks.isEmpty())
        assertEquals(
            listOf(MealStatus.QUEUED, MealStatus.QUEUED),
            held.map { it.status },
            "ISSUE 6: the meals are not lost with the cook",
        )
    }

    @Test
    fun addedCooksAreEligibleImmediatelyAndIdsRestartEachEvening() {
        val r = roster(CookType.EXEC to 0)
        assertNull(r.startCooking(meals(1)), "no cook at all yet")

        r.changeStaff(CookType.EXEC, 1)
        val cook = r.startCooking(meals(1))

        assertNotNull(cook)
        assertEquals(1, cook.getId(), "ids start at one")

        r.resetEvening()
        assertNull(cook.getId(), "and are granted again next evening")
        assertEquals(1, r.nextId)
    }
}
