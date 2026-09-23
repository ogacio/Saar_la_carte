package de.unisaarland.cs.se.selab.kitchen

import de.unisaarland.cs.se.selab.sharedPackage.MealStatus
import de.unisaarland.cs.se.selab.simulation.GlobalClock
import de.unisaarland.cs.se.selab.testsupport.KitchenBoundaryFixtures.dish
import de.unisaarland.cs.se.selab.testsupport.KitchenBoundaryFixtures.kitchen
import de.unisaarland.cs.se.selab.testsupport.KitchenBoundaryFixtures.order
import de.unisaarland.cs.se.selab.testsupport.KitchenBoundaryFixtures.startEvening
import kotlin.test.Test
import kotlin.test.assertEquals

/** Priority is tested within one order, including the recipe-id tie breaker missing in older tests. */
class F10QueueBoundaryTest {
    @Test
    fun basicThenAscendingRecipeIdOverridesTheMealInputOrder() {
        startEvening()
        val kitchen = kitchen()
        val order = order(dish(5), dish(2), dish(9, basic = true))
        kitchen.enqueue(order)
        val meals = order.getMeals()

        kitchen.cook()
        assertEquals(listOf(MealStatus.QUEUED, MealStatus.QUEUED, MealStatus.COOKED), meals.map { it.status })
        GlobalClock.advanceTick()
        kitchen.cook()
        assertEquals(listOf(MealStatus.QUEUED, MealStatus.COOKED, MealStatus.COOKED), meals.map { it.status })
        GlobalClock.advanceTick()
        kitchen.cook()
        assertEquals(List(3) { MealStatus.COOKED }, meals.map { it.status })
    }
}
