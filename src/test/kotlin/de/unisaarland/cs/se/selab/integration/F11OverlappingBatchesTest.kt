package de.unisaarland.cs.se.selab.integration

import de.unisaarland.cs.se.selab.sharedPackage.MealStatus
import de.unisaarland.cs.se.selab.simulation.GlobalClock
import de.unisaarland.cs.se.selab.testsupport.KitchenBoundaryFixtures.dish
import de.unisaarland.cs.se.selab.testsupport.KitchenBoundaryFixtures.kitchen
import de.unisaarland.cs.se.selab.testsupport.KitchenBoundaryFixtures.order
import de.unisaarland.cs.se.selab.testsupport.KitchenBoundaryFixtures.startEvening
import kotlin.test.Test
import kotlin.test.assertEquals

/** F10-F12: later arrivals form a new batch without joining or overwriting an occupied cook's pan. */
class F11OverlappingBatchesTest {
    @Test
    fun anotherCookStartsTheSameDishNextTickAndEachBatchKeepsItsOwnFinishTime() {
        startEvening()
        val kitchen = kitchen(cooks = 2)
        val recipe = dish(1, minutes = 30)
        val first = order(recipe, recipe)
        kitchen.enqueue(first)
        assertEquals(0, kitchen.cook())
        assertEquals(listOf(1, null), kitchen.roaster.cooks.map { it.getId() })

        GlobalClock.advanceTick()
        val later = order(recipe)
        kitchen.enqueue(later)
        assertEquals(0, kitchen.cook())
        assertEquals(listOf(1, 2), kitchen.roaster.cooks.map { it.getId() })
        assertEquals(listOf(2, 1), kitchen.roaster.cooks.map { it.batchSize() })

        GlobalClock.advanceTick()
        assertEquals(2, kitchen.cook())
        assertEquals(List(2) { MealStatus.COOKED }, first.getMeals().map { it.status })
        assertEquals(MealStatus.COOKING, later.getMeals().single().status)
        GlobalClock.advanceTick()
        assertEquals(1, kitchen.cook())
        assertEquals(MealStatus.COOKED, later.getMeals().single().status)
    }
}
