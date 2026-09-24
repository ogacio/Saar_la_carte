package de.unisaarland.cs.se.selab.sharedPackage.customers

import de.unisaarland.cs.se.selab.sharedPackage.Ingredient
import de.unisaarland.cs.se.selab.sharedPackage.RecipeIngredient
import de.unisaarland.cs.se.selab.sharedPackage.UnitType
import de.unisaarland.cs.se.selab.testsupport.Fixtures.event
import de.unisaarland.cs.se.selab.testsupport.Fixtures.recipe
import de.unisaarland.cs.se.selab.testsupport.Fixtures.regular
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/**
 * The delivery lifecycle every group type shares. Only CASUAL groups can request a delivery, so
 * REGULAR and EVENT groups fall back to the defaults on [CustomerGroup]: never a delivery, never
 * a pending order, never given up. Also covers [CustomerGroup.acceptsAny], which the browsing
 * service uses to drop a restaurant whose menu one member cannot eat.
 */
class P05NonDeliveryGroupDefaultsTest {

    private val onion = Ingredient("onion", UnitType.G, 10, 5)
    private val rice = Ingredient("rice", UnitType.G, 10, 5)

    private fun dish(vararg parts: Ingredient) =
        recipe(1, parts.map { RecipeIngredient(it, 1) })

    private fun preference(excluded: Set<Ingredient>) =
        FoodPreference(2, excluded, emptySet(), emptyList())

    @Test
    fun aRegularGroupNeverOrdersDelivery() {
        val group = regular(1, 2)

        assertFalse(group.isDelivery(), "REGULAR groups always come in person")
        assertFalse(group.deliveryGiveUpDue())
        assertFalse(group.deliveryWasGivenUp())
    }

    @Test
    fun anEventGroupNeverOrdersDelivery() {
        val group = event(2, 4)

        assertFalse(group.isDelivery())
        assertFalse(group.deliveryGiveUpDue())
        assertFalse(group.deliveryWasGivenUp())
    }

    @Test
    fun theDeliveryLifecycleIsANoOpForAGroupThatCannotOrderOne() {
        val group = event(3, 2)

        group.orderPlaced()
        group.markDeliveryGivenUp()
        group.orderResolved()

        assertFalse(group.deliveryGiveUpDue(), "nothing was ever pending")
        assertFalse(group.deliveryWasGivenUp())
        assertFalse(group.hasGivenUp(), "an EVENT group does not churn")
    }

    @Test
    fun aGroupWithoutPreferencesEatsAnything() {
        val group = regular(4, 2)

        assertTrue(group.acceptsAny(listOf(dish(onion))), "no preference means nothing is refused")
        assertTrue(group.acceptsAny(emptyList()), "not even an empty menu, since nobody objects")
    }

    @Test
    fun aMenuWithNoEdibleDishIsRefused() {
        val group = event(5, 2, preference = preference(setOf(onion)))

        assertFalse(
            group.acceptsAny(listOf(dish(onion))),
            "every dish contains the excluded ingredient",
        )
    }

    @Test
    fun oneEdibleDishIsEnoughForTheWholeGroup() {
        val group = event(6, 2, preference = preference(setOf(onion)))

        assertTrue(group.acceptsAny(listOf(dish(onion), dish(rice))), "the rice dish is edible")
    }
}
