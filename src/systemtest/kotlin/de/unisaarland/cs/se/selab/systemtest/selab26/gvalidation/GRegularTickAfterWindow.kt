package de.unisaarland.cs.se.selab.systemtest.selab26.gvalidation

/* Parser-rule rejections from tests/giant/cluster_g, targeting the validation mutants
   FoodScarcity, Gourmand, Indie, NormThis: each rejects a scenario.json for a rule enforced in
   CustomerGroupSerialiser.kt, not the JSON schema, so a mutant that removes the rule needs a
   fixture like these to be caught. */

import de.unisaarland.cs.se.selab.systemtest.selab26.utils.LogSkippingSystemTest

private const val DEBUG_LEVEL = "DEBUG"

/** A REGULAR group's visitingTick must leave 3 ticks before closing: visitingTick <= openingTickEnd - 3. */
class GRegularTickAfterWindow : LogSkippingSystemTest() {
    override val name = "GRegularTickAfterWindow"
    override val description = "A REGULAR group's visitingTick 8 is rejected when the bound restaurant closes at 10."
    override val food = "gvalidation/s01_regular_tick_after_window/food.json"
    override val restaurants = "gvalidation/s01_regular_tick_after_window/restaurants.json"
    override val scenario = "gvalidation/s01_regular_tick_after_window/scenario.json"
    override val logLevel = DEBUG_LEVEL
    override val maxTicks = 0

    override suspend fun run() {
        skipToAndAssert(
            "[IMPORTANT] Initialization Info:",
            "[IMPORTANT] Initialization Info: scenario.json is invalid.",
        )
    }
}
