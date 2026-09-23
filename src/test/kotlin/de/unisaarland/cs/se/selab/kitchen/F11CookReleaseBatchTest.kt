package de.unisaarland.cs.se.selab.kitchen

import de.unisaarland.cs.se.selab.sharedPackage.Meal
import de.unisaarland.cs.se.selab.sharedPackage.MealStatus
import de.unisaarland.cs.se.selab.sharedPackage.customers.Customer
import de.unisaarland.cs.se.selab.simulation.GlobalClock
import de.unisaarland.cs.se.selab.testsupport.Fixtures.recipe
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

/**
 * F11/F12, the pan of a single cook: the one-tick gap before it is free again, and ISSUE 6 —
 * a cook taken off the roster mid-batch hands its meals back to the queue instead of losing them.
 */
class F11CookReleaseBatchTest {

    private fun meals(count: Int) =
        List(count) { Meal(1, Customer(null), recipe(1)) }.toMutableList()

    /**
     * Cook.startCooking keeps the caller's list and clears it when the batch ends, so a test has to
     * hold its own references to the meals if it wants to inspect them afterwards.
     */
    private fun refs(batch: MutableList<Meal>) = batch.toList()

    @BeforeTest
    fun freshEvening() {
        GlobalClock.advanceEvening()
        GlobalClock.advanceTick()
    }

    @Test
    fun releasingABatchPutsTheMealsBackInTheQueue() {
        val cook = Cook(CookType.SOUS)
        val batch = meals(2)
        val held = refs(batch)
        cook.startCooking(batch)
        assertEquals(listOf(MealStatus.COOKING, MealStatus.COOKING), held.map { it.status })

        cook.releaseBatch()

        assertEquals(
            listOf(MealStatus.QUEUED, MealStatus.QUEUED),
            held.map { it.status },
            "ISSUE 6: a cook leaving mid-batch returns its meals to the kitchen queue",
        )
        assertEquals(0, cook.batchSize())
        assertTrue(cook.isFree(), "the pan is empty, so the slot is free again")
    }

    @Test
    fun releasingLeavesAlreadyCookedMealsAlone() {
        val cook = Cook(CookType.SOUS)
        val batch = meals(2)
        val held = refs(batch)
        cook.startCooking(batch)
        held[0].status = MealStatus.COOKED

        cook.releaseBatch()

        assertEquals(MealStatus.COOKED, held[0].status, "a finished meal is not re-queued")
        assertEquals(MealStatus.QUEUED, held[1].status)
    }

    @Test
    fun releasingAnIdleCookIsHarmless() {
        val cook = Cook(CookType.EXEC)

        cook.releaseBatch()

        assertTrue(cook.isFree())
        assertEquals(0, cook.batchSize())
    }

    @Test
    fun anIdleCookHandsBackNullRatherThanAnEmptyBatch() {
        val cook = Cook(CookType.EXEC)

        assertNull(cook.cookingFinished(), "an empty pan is not an empty delivery")
    }

    @Test
    fun aBatchIsHandedOverOnlyOnceAndTheCookIsFreeTheTickAfter() {
        val cook = Cook(CookType.EXEC)
        val batch = meals(1)
        val held = refs(batch)
        cook.startCooking(batch)

        val done = cook.cookingFinished()
        assertEquals(1, done?.size, "a 10 minute dish is finished in the same tick")
        assertEquals(MealStatus.COOKED, held[0].status)
        assertFalse(cook.isFree(), "still busy for the rest of this tick")

        assertNull(cook.cookingFinished(), "the same batch is not handed over twice")

        GlobalClock.advanceTick()
        assertTrue(cook.isFree(), "free again in the tick after the batch finished")
    }

    @Test
    fun resetClearsTheIdAndThePanForTheNextEvening() {
        val cook = Cook(CookType.ROAST)
        cook.setId(4)
        cook.startCooking(meals(2))

        cook.reset()

        assertNull(cook.getId(), "ids are granted afresh each evening")
        assertEquals(0, cook.batchSize())
        assertTrue(cook.isFree())
        assertEquals(CookType.ROAST, cook.getType(), "the type is not an evening property")
    }
}
