package de.unisaarland.cs.se.selab.integration

import de.unisaarland.cs.se.selab.foh.Table
import de.unisaarland.cs.se.selab.foh.Waiter
import de.unisaarland.cs.se.selab.foh.WaiterAssignmentService
import de.unisaarland.cs.se.selab.foh.services.OrderingService
import de.unisaarland.cs.se.selab.foh.visit.Visit
import de.unisaarland.cs.se.selab.sharedPackage.Ingredient
import de.unisaarland.cs.se.selab.sharedPackage.Menu
import de.unisaarland.cs.se.selab.sharedPackage.Pantry
import de.unisaarland.cs.se.selab.sharedPackage.RecipeIngredient
import de.unisaarland.cs.se.selab.sharedPackage.RestaurantType
import de.unisaarland.cs.se.selab.sharedPackage.TableType
import de.unisaarland.cs.se.selab.sharedPackage.UnitType
import de.unisaarland.cs.se.selab.sharedPackage.customers.Customer
import de.unisaarland.cs.se.selab.sharedPackage.customers.CustomerGroup
import de.unisaarland.cs.se.selab.sharedPackage.customers.EventCustomerGroup
import de.unisaarland.cs.se.selab.sharedPackage.customers.FoodPreference
import de.unisaarland.cs.se.selab.sharedPackage.customers.RegularCustomerGroup
import de.unisaarland.cs.se.selab.simulation.GlobalClock
import de.unisaarland.cs.se.selab.simulation.SubUnits
import de.unisaarland.cs.se.selab.testsupport.KitchenBoundaryFixtures.dish
import de.unisaarland.cs.se.selab.testsupport.KitchenBoundaryFixtures.kitchen
import de.unisaarland.cs.se.selab.testsupport.KitchenBoundaryFixtures.startEvening
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertSame

/** F26: independent preferences reach the real kitchen with the matching stock reservations. */
class F26PreferenceOrderingTest {
    private val rice = Ingredient("rice", UnitType.G, 1000, 5)
    private val egg = Ingredient("egg", UnitType.X, 1000, 5)
    private val basic = dish(1, basic = true).also { it.ingredients += RecipeIngredient(rice, 1) }
    private val varied = dish(2).also {
        it.ingredients += RecipeIngredient(rice, 1)
        it.ingredients += RecipeIngredient(egg, 1)
    }
    private val large = dish(3).also { it.ingredients += RecipeIngredient(rice, 100) }

    @Test
    fun subgroupsChooseByKindCountAndIdIndependentlyOfAmounts() {
        val variedPreference = FoodPreference(1, emptySet(), setOf(rice, egg), emptyList())
        val tiedPreference = FoodPreference(1, emptySet(), setOf(rice), emptyList())
        val preferences = listOf(variedPreference, tiedPreference)
        val group = RegularCustomerGroup(
            1, 2, TableType.COMMON, 1, preferences.map(::Customer), preferences, 1, 1, 1,
        )
        val (visit, pantry) = order(group)

        assertEquals(listOf(varied, large), group.members().map { it.chosenDish() })
        assertEquals(listOf(varied, large), requireNotNull(visit.order).getMeals().map { it.recipe })
        assertEquals(899, pantry.getTotalIngredients(rice))
        assertEquals(999, pantry.getTotalIngredients(egg))
    }

    @Test
    fun eventBasicDishOverridesPersonalFavouriteAndHigherPreferredCount() {
        val preference = FoodPreference(4, emptySet(), setOf(rice, egg), listOf(varied.getDishName()))
        val group = EventCustomerGroup(
            1, 4, TableType.COMMON, 1, List(4) { Customer(preference) }, listOf(preference),
            setOf(RestaurantType.ASIAN), 4, mapOf(RestaurantType.ASIAN to basic.getDishName()),
        )
        val (visit, pantry) = order(group)

        assertEquals(List(4) { basic }, group.members().map { it.chosenDish() })
        assertEquals(4, requireNotNull(visit.order).getMeals().size)
        assertEquals(996, pantry.getTotalIngredients(rice))
        assertEquals(1000, pantry.getTotalIngredients(egg))
    }

    private fun order(group: CustomerGroup): Pair<Visit, Pantry> {
        startEvening()
        val pantry = Pantry(restaurantId = 1)
        pantry.restock(rice, 1000)
        pantry.restock(egg, 1000)
        val kitchen = kitchen(pantry)
        val menu = Menu(mutableListOf(large, basic, varied), pantry, kitchen)
        val waitstaff = WaiterAssignmentService(mutableListOf(Waiter()))
        val waiter = requireNotNull(waitstaff.assignPermanent(group.groupSize()))
        val visit = Visit(group)
        visit.seated(Table(1, group.groupSize(), TableType.COMMON), listOf(waiter), GlobalClock.currentTick)
        visit.eventSeatingPlan = mapOf(waiter to group.groupSize())
        OrderingService(waitstaff, RestaurantType.ASIAN).takeOrder(
            visit,
            SubUnits(1, menu, pantry, kitchen),
            GlobalClock.currentTick,
        )
        assertSame(visit.order, kitchen.queue.single())
        return visit to pantry
    }
}
