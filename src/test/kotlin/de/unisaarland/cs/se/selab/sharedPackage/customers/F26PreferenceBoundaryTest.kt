package de.unisaarland.cs.se.selab.sharedPackage.customers

import de.unisaarland.cs.se.selab.sharedPackage.Ingredient
import de.unisaarland.cs.se.selab.sharedPackage.RecipeIngredient
import de.unisaarland.cs.se.selab.sharedPackage.UnitType
import de.unisaarland.cs.se.selab.testsupport.Fixtures.recipe
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse

/** F26: ingredient amounts affect neither exclusions nor the count of preferred kinds. */
class F26PreferenceBoundaryTest {
    private val rice = Ingredient("rice", UnitType.G, 100, 5)
    private val egg = Ingredient("egg", UnitType.X, 10, 5)

    @Test
    fun increasingOnePreferredAmountCannotOutrankTwoPreferredKinds() {
        val preference = FoodPreference(1, emptySet(), setOf(rice, egg), emptyList())
        val large = recipe(9, listOf(RecipeIngredient(rice, 100)))
        val varied = recipe(1, listOf(RecipeIngredient(rice, 1), RecipeIngredient(egg, 1)))

        assertEquals(1, preference.rank(large))
        assertEquals(2, preference.rank(varied))
    }

    @Test
    fun evenOneUnitOfAnExcludedIngredientRejectsAnOtherwisePreferredFavourite() {
        val dish = recipe(9, listOf(RecipeIngredient(rice, 100), RecipeIngredient(egg, 1)))
        val preference = FoodPreference(1, setOf(egg), setOf(rice), listOf(dish.getDishName()))

        assertFalse(preference.accepts(dish))
        assertEquals(null, preference.firstFavourite(listOf(dish).filter(preference::accepts)))
    }
}
