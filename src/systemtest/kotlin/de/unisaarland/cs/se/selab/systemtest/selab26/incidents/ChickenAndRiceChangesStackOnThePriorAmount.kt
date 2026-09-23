package de.unisaarland.cs.se.selab.systemtest.selab26.incidents

import de.unisaarland.cs.se.selab.systemtest.selab26.utils.LogSkippingSystemTest

/** The line that starts the preparation phase of [evening]. */
private fun preparation(evening: Int) = "[IMPORTANT] Preparation: Preparation for evening $evening starts."

/**
 * F32, Incident - Recipe Change: a second +10% change applies to the already-adapted amount, not
 * to the original one. Two +10% changes apply to the prior amount each time: 11 -> 12 -> 13 and
 * 21 -> 23 -> 25, so chicken is 32, then 35, then 38 g. Run against the reference first.
 */
class ChickenAndRiceChangesStackOnThePriorAmount : LogSkippingSystemTest() {
    override val name = "F32_ChickenAndRiceChangesStackOnThePriorAmount"
    override val description = "A second recipe change adapts the already adapted amounts."
    override val food = "gvalidation/biborka_mutants/food_chicken_eleven_and_twenty_one.json"
    override val restaurants = "gvalidation/biborka_mutants/restaurants_ten_seats_two_recipes.json"
    override val scenario = "gvalidation/biborka_mutants/scenario_two_recipe_changes.json"
    override val logLevel = "DEBUG"
    override val maxTicks = 72

    override suspend fun run() {
        skipToAndAssert(preparation(2), preparation(2))
        skipToAndAssert(
            "[DEBUG] Pantry (R 1): Procured",
            "[DEBUG] Pantry (R 1): Procured 35 g of chicken from the supplier."
        )
        skipToAndAssert(preparation(3), preparation(3))
        skipToAndAssert(
            "[DEBUG] Pantry (R 1): Procured",
            "[DEBUG] Pantry (R 1): Procured 38 g of chicken from the supplier."
        )
    }
}
