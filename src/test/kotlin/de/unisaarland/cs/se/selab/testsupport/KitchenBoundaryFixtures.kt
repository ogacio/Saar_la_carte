package de.unisaarland.cs.se.selab.testsupport

import de.unisaarland.cs.se.selab.foh.TableAssignmentService
import de.unisaarland.cs.se.selab.kitchen.CookRoaster
import de.unisaarland.cs.se.selab.kitchen.CookType
import de.unisaarland.cs.se.selab.kitchen.Kitchen
import de.unisaarland.cs.se.selab.sharedPackage.Meal
import de.unisaarland.cs.se.selab.sharedPackage.Order
import de.unisaarland.cs.se.selab.sharedPackage.Pantry
import de.unisaarland.cs.se.selab.sharedPackage.Recipe
import de.unisaarland.cs.se.selab.sharedPackage.RestaurantType
import de.unisaarland.cs.se.selab.simulation.GlobalClock
import de.unisaarland.cs.se.selab.simulation.ReservationBook

/** Real kitchen objects shared by the additional F10-F12 boundary tests. */
object KitchenBoundaryFixtures {
    fun kitchen(pantry: Pantry = Pantry(restaurantId = 1), cooks: Int = 1): Kitchen {
        val roaster = CookRoaster(mapOf(CookType.SOUS to cooks), restaurantId = pantry.getRestaurantId())
        roaster.initialiseCooks()
        return Kitchen(
            roaster,
            pantry,
            mutableListOf(),
            ReservationBook(TableAssignmentService(mutableListOf())),
            RestaurantType.ASIAN,
        )
    }

    fun dish(id: Int, minutes: Int = 10, basic: Boolean = false) = Recipe(
        id,
        "dish$id",
        minutes,
        setOf(CookType.SOUS),
        mutableListOf(),
        if (basic) RestaurantType.ASIAN else null,
    )

    fun order(vararg recipes: Recipe, restaurantId: Int = 1): Order {
        val group = Fixtures.casual(1, recipes.size)
        val meals = recipes.mapIndexed { index, recipe -> Meal(null, group.members()[index], recipe) }.toMutableList()
        val order = Order(group, restaurantId, group.id(), GlobalClock.currentTick, false, meals)
        meals.forEach { it.orderId = order.getId() }
        return order
    }

    fun startEvening() {
        GlobalClock.advanceEvening()
        GlobalClock.advanceTick()
        Fixtures.captureLog()
    }
}
