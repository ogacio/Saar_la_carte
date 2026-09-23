package de.unisaarland.cs.se.selab.kitchen

import de.unisaarland.cs.se.selab.sharedPackage.Pantry
import de.unisaarland.cs.se.selab.testsupport.KitchenBoundaryFixtures.dish
import de.unisaarland.cs.se.selab.testsupport.KitchenBoundaryFixtures.kitchen
import de.unisaarland.cs.se.selab.testsupport.KitchenBoundaryFixtures.order
import de.unisaarland.cs.se.selab.testsupport.KitchenBoundaryFixtures.startEvening
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

/** IDs belong to each restaurant independently, even when another restaurant already assigned IDs. */
class F11StaffBoundaryTest {
    @Test
    fun eachRestaurantStartsItsOwnCookIdsAtOne() {
        startEvening()
        val first = kitchen(cooks = 2)
        val second = kitchen(Pantry(restaurantId = 2))
        first.enqueue(order(dish(1, minutes = 30), dish(2, minutes = 30)))
        first.cook()
        assertEquals(listOf(1, 2), first.roaster.cooks.map { it.getId() })
        assertNull(second.roaster.cooks.single().getId())

        second.enqueue(order(dish(1), restaurantId = 2))
        second.cook()

        assertEquals(1, second.roaster.cooks.single().getId())
        assertEquals(listOf(1, 2), first.roaster.cooks.map { it.getId() })
    }
}
