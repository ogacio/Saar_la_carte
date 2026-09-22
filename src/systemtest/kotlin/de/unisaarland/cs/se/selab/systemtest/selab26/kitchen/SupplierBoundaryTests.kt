package de.unisaarland.cs.se.selab.systemtest.selab26.kitchen

import de.unisaarland.cs.se.selab.systemtest.selab26.utils.LogSkippingSystemTest

private const val DEBUG = "DEBUG"
private const val PREPARATION_PREFIX = "[IMPORTANT] Preparation"
private const val PANTRY_PREFIX = "[DEBUG] Pantry"
private const val TEN_SEAT_RESTAURANT = "f08/restaurants_ten_seats.json"
private const val EMPTY_SCENARIO = "f08/scenario_empty.json"
private const val FIRST_PREPARATION = "[IMPORTANT] Preparation: Preparation for evening 1 starts."
private const val SECOND_PREPARATION = "[IMPORTANT] Preparation: Preparation for evening 2 starts."
private const val RICE = "rice"

/** F08 additions that isolate package rounding, stock deficits and procurement order. */
abstract class AddedSupplierTest(
    foodFixture: String,
    restaurantFixture: String = TEN_SEAT_RESTAURANT,
    scenarioFixture: String = EMPTY_SCENARIO,
    ticks: Int = 1,
) : LogSkippingSystemTest() {
    override val food = foodFixture
    override val restaurants = restaurantFixture
    override val scenario = scenarioFixture
    override val logLevel = DEBUG
    override val maxTicks = ticks

    protected fun procured(amount: Int, ingredient: String): String =
        "[DEBUG] Pantry (R 1): Procured $amount g of $ingredient from the supplier."
}

/** An exact multiple of the package volume needs no additional package. */
class F08ExactPackageMultiple : AddedSupplierTest("f08/food_exact_package_multiple.json") {
    override val name = "F08ExactPackageMultiple"
    override val description = "A requirement of 40 g is supplied as exactly two 20 g packages."

    override suspend fun run() {
        skipToAndAssert(PREPARATION_PREFIX, FIRST_PREPARATION)
        skipToAndAssert(PANTRY_PREFIX, procured(40, RICE))
    }
}

/** Only the gap between required ingredients and pantry stock is procured. */
class F08PartialStockDeficit : AddedSupplierTest(
    "f08/food_partial_stock.json",
    "f08/restaurants_two_recipes_ten_seats.json",
    "f08/scenario_eat_twenty_on_first_evening.json",
    25,
) {
    override val name = "F08PartialStockDeficit"
    override val description = "With 40 of 50 g left in stock, only one 20 g package is procured."

    override suspend fun run() {
        skipToAndAssert(PREPARATION_PREFIX, FIRST_PREPARATION)
        skipToAndAssert(PANTRY_PREFIX, procured(60, RICE))
        skipToAndAssert(PREPARATION_PREFIX, SECOND_PREPARATION)
        skipToAndAssert(PANTRY_PREFIX, procured(20, RICE))
    }
}

/** Ingredient procurement logs are ordered by ingredient name. */
class F08TwoIngredientsAlphabetical : AddedSupplierTest("f08/food_two_ingredients_reversed.json") {
    override val name = "F08TwoIngredientsAlphabetical"
    override val description = "Apple is procured before zucchini despite their reversed input order."

    override suspend fun run() {
        skipToAndAssert(PREPARATION_PREFIX, FIRST_PREPARATION)
        skipToAndAssert(PANTRY_PREFIX, procured(20, "apple"))
        assertNextLine(procured(20, "zucchini"))
    }
}

/** Planning includes a restaurant recipe even when no cook can prepare it. */
class F08IngredientWithoutEligibleCookIsProcured : AddedSupplierTest(
    "f08/food_without_eligible_cook.json",
) {
    override val name = "F08IngredientWithoutEligibleCookIsProcured"
    override val description = "Ingredients are procured for a recipe without an eligible cook."

    override suspend fun run() {
        skipToAndAssert(PREPARATION_PREFIX, FIRST_PREPARATION)
        skipToAndAssert(PANTRY_PREFIX, procured(40, RICE))
    }
}
