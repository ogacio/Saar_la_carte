package de.unisaarland.cs.se.selab.kitchen

import de.unisaarland.cs.se.selab.sharedPackage.MealStatus
import de.unisaarland.cs.se.selab.simulation.GlobalClock
import de.unisaarland.cs.se.selab.testsupport.KitchenBoundaryFixtures.dish
import de.unisaarland.cs.se.selab.testsupport.KitchenBoundaryFixtures.order
import de.unisaarland.cs.se.selab.testsupport.KitchenBoundaryFixtures.startEvening
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull

/** Eleven minutes rounds up to two cooking ticks; ten-minute completion is covered separately. */
class F12DurationBoundaryTest {
    @Test
    fun elevenMinutesFinishesTheWholeBatchInTheFollowingTickOnlyOnce() {
        startEvening()
        val cook = Cook(CookType.SOUS)
        val recipe = dish(1, minutes = 11)
        val meals = order(recipe, recipe).getMeals()
        cook.startCooking(meals.toMutableList())

        assertNull(cook.cookingFinished())
        assertEquals(List(2) { MealStatus.COOKING }, meals.map { it.status })
        GlobalClock.advanceTick()
        assertEquals(meals, requireNotNull(cook.cookingFinished()).toList())
        assertEquals(List(2) { MealStatus.COOKED }, meals.map { it.status })
        assertFalse(cook.isFree())
        assertNull(cook.cookingFinished(), "A batch must never be emitted twice")
    }
}
