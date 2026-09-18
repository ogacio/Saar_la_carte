package de.unisaarland.cs.se.selab.kitchen

/* One cook: what it does to the meals of its batch, and the reset. */

import de.unisaarland.cs.se.selab.sharedPackage.Meal
import de.unisaarland.cs.se.selab.sharedPackage.MealStatus
import de.unisaarland.cs.se.selab.sharedPackage.Recipe
import de.unisaarland.cs.se.selab.sharedPackage.customers.Customer
import de.unisaarland.cs.se.selab.simulation.GlobalClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class F11CookLifecycleTest {

    private fun dish(minutes: Int) = Recipe(1, "dish1", minutes, setOf(CookType.SOUS), mutableListOf(), null)

    private fun meals(recipe: Recipe, count: Int = 1): MutableList<Meal> =
        MutableList(count) { Meal(1, Customer(null), recipe) }

    private fun startEvening() {
        GlobalClock.advanceEvening()
        GlobalClock.advanceTick()
    }

    /** Every meal of a batch is COOKING while the cook is busy and COOKED afterwards. */
    @Test
    fun theWholeBatchChangesItsStatusTogether() {
        startEvening()
        val cook = Cook(CookType.SOUS)
        val batch = meals(dish(30), count = 3)

        cook.startCooking(batch)

        assertEquals(List(3) { MealStatus.COOKING }, batch.map { it.status })
        assertFalse(cook.isFree(), "the cook is busy while the batch is in the pan")

        // far beyond any reading of the duration, so this test says nothing about the timing
        repeat(5) { GlobalClock.advanceTick() }
        cook.cookingFinished()

        assertEquals(List(3) { MealStatus.COOKED }, batch.map { it.status })
        assertTrue(cook.isFree(), "the cook is free again")
    }

    /** A cook that has not started anything is free and nameless. */
    @Test
    fun aNewCookIsFreeAndHasNoId() {
        val cook = Cook(CookType.PASTRY)

        assertTrue(cook.isFree())
        assertNull(cook.getId())
        assertEquals(CookType.PASTRY, cook.getType())
    }

    /** The evening reset frees the cook, drops its id and empties the pan. */
    @Test
    fun resetClearsTheCookCompletely() {
        startEvening()
        val cook = Cook(CookType.SOUS)
        cook.setId(7)
        cook.startCooking(meals(dish(60), count = 2))

        cook.reset()

        assertTrue(cook.isFree())
        assertNull(cook.getId())
        // an unfinished batch is dropped, so nothing of yesterday can come out of the pan tonight
        startEvening()
        assertTrue(cook.cookingFinished().isNullOrEmpty(), "nothing left in the pan")
    }
}
