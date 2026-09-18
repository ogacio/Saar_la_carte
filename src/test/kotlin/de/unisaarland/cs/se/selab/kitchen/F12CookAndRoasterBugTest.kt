package de.unisaarland.cs.se.selab.kitchen

/* Cook and CookRoaster: defects that make the kitchen lose what it cooked. */

import de.unisaarland.cs.se.selab.sharedPackage.Meal
import de.unisaarland.cs.se.selab.sharedPackage.MealStatus
import de.unisaarland.cs.se.selab.sharedPackage.Recipe
import de.unisaarland.cs.se.selab.sharedPackage.customers.Customer
import de.unisaarland.cs.se.selab.simulation.GlobalClock
import de.unisaarland.cs.se.selab.testsupport.Fixtures.RESTAURANT_ID
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class F12CookAndRoasterBugTest {

    /** A dish of [minutes] minutes; 10 minutes is one tick. */
    private fun dish(id: Int, minutes: Int = 10) =
        Recipe(id, "dish$id", minutes, setOf(CookType.EXEC), mutableListOf(), null)

    private fun meals(recipe: Recipe, count: Int): MutableList<Meal> =
        MutableList(count) { Meal(1, Customer(null), recipe) }

    private fun roaster(execCooks: Int) =
        CookRoaster(mapOf(CookType.EXEC to execCooks), restaurantId = RESTAURANT_ID).also { it.initialiseCooks() }

    /** Starts a fresh evening at tick 1. */
    private fun startEvening() {
        GlobalClock.advanceEvening()
        GlobalClock.advanceTick()
    }

    /** A cook hands the finished meals back to the kitchen. */
    @Test
    fun aFinishedBatchIsHandedBackToTheKitchen() {
        startEvening()
        val cook = Cook(CookType.EXEC)
        cook.startCooking(meals(dish(1), 2))

        GlobalClock.advanceTick()
        val finished = cook.cookingFinished()

        assertEquals(2, finished?.size, "the cook must hand back the two meals it just finished")
        assertTrue(finished.orEmpty().all { it.status == MealStatus.COOKED })
    }

    /** A cook with an empty pan reports nothing. */
    @Test
    fun anIdleCookReportsNothing() {
        startEvening()
        val cook = Cook(CookType.EXEC)

        assertNull(cook.cookingFinished(), "a cook with an empty pan has nothing to report")
    }

    /** No meal is left cooking with nobody cooking it when a COOK incident fires a cook. */
    @Test
    fun firingACookMustNotLoseTheMealsInThePan() {
        startEvening()
        val roaster = roaster(execCooks = 2)
        val batch = meals(dish(1), 2)
        roaster.startCooking(batch)

        roaster.changeStaff(CookType.EXEC, -1)
        repeat(5) {
            GlobalClock.advanceTick()
            roaster.finished()
        }

        assertTrue(
            batch.none { it.status == MealStatus.COOKING },
            "meals of a fired cook are stuck in COOKING forever",
        )
    }

    /** Control: a 30 minute dish is not ready before its time. */
    @Test
    fun aCookIsBusyUntilTheDishIsDone() {
        startEvening()
        val cook = Cook(CookType.EXEC)
        cook.startCooking(meals(dish(1, minutes = 30), 1))

        GlobalClock.advanceTick()
        assertNull(cook.cookingFinished(), "still cooking in tick 2")
        GlobalClock.advanceTick()
        assertNull(cook.cookingFinished(), "still cooking in tick 3")
        GlobalClock.advanceTick()

        // only "is it done", not "what came out" - the second question is the failing test above
        assertNotNull(cook.cookingFinished(), "ready in tick 4")
    }
}
