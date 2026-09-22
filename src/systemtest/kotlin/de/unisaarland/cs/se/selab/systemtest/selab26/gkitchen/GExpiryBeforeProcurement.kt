package de.unisaarland.cs.se.selab.systemtest.selab26.gkitchen

import de.unisaarland.cs.se.selab.systemtest.selab26.utils.LogSkippingSystemTest

private const val DEBUG_LEVEL = "DEBUG"

/**
 * From tests/giant/gaps/g2_bestbefore: rice has bestBefore 2 and a 20g package. Evening 1 buys a
 * full package; evening 2 needs none (leftovers still in date); evening 3 the batch expires and is
 * thrown out BEFORE that evening's procurement runs, in the same preparation block.
 */
class GExpiryBeforeProcurement : LogSkippingSystemTest() {
    override val name = "GExpiryBeforeProcurement"
    override val description = "An expired ingredient batch is removed before the same evening's " +
        "procurement, not after."
    override val food = "gkitchen/g2_bestbefore/food.json"
    override val restaurants = "gkitchen/g2_bestbefore/restaurants.json"
    override val scenario = "gkitchen/g2_bestbefore/scenario.json"
    override val logLevel = DEBUG_LEVEL
    override val maxTicks = 120

    override suspend fun run() {
        skipToAndAssert(
            "[IMPORTANT] Preparation: Preparation for evening 3 starts.",
            "[IMPORTANT] Preparation: Preparation for evening 3 starts.",
        )
        assertNextLine("[DEBUG] Pantry (R 1): Removed 6 g of rice from the pantry.")
        assertNextLine("[DEBUG] Pantry (R 1): Procured 20 g of rice from the supplier.")
    }
}
