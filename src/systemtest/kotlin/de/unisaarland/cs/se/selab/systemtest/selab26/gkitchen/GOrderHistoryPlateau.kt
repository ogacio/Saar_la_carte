package de.unisaarland.cs.se.selab.systemtest.selab26.gkitchen

import de.unisaarland.cs.se.selab.systemtest.selab26.utils.LogSkippingSystemTest

private const val DEBUG_LEVEL = "DEBUG"
private const val PROCURED_RICE = "[DEBUG] Pantry (R 1): Procured"

/**
 * From tests/giant/gaps/g3_history: a small ingredient package (3g) makes REGULAR order-history
 * planning visible one package at a time. Procurement ramps by one package per remembered visit
 * (3, 6, 9, 12g over evenings 1-4) until the 3-visit history window is full, then plateaus at 15g
 * from evening 5 onward. Asserts the transition (evening 4 to 5) and that the plateau holds at
 * evening 8, not every evening in between.
 */
class GOrderHistoryPlateau : LogSkippingSystemTest() {
    override val name = "GOrderHistoryPlateau"
    override val description = "Rice procurement ramps 3g per evening while REGULAR order history " +
        "fills, then plateaus once the 3-visit window is full."
    override val food = "gkitchen/g3_history/food.json"
    override val restaurants = "gkitchen/g3_history/restaurants.json"
    override val scenario = "gkitchen/g3_history/scenario.json"
    override val logLevel = DEBUG_LEVEL
    override val maxTicks = 192

    override suspend fun run() {
        skipToAndAssert(
            "[IMPORTANT] Preparation: Preparation for evening 4 starts.",
            "[IMPORTANT] Preparation: Preparation for evening 4 starts.",
        )
        skipToAndAssert(PROCURED_RICE, "$PROCURED_RICE 12 g of rice from the supplier.")
        skipToAndAssert(
            "[IMPORTANT] Preparation: Preparation for evening 5 starts.",
            "[IMPORTANT] Preparation: Preparation for evening 5 starts.",
        )
        skipToAndAssert(PROCURED_RICE, "$PROCURED_RICE 15 g of rice from the supplier.")
        skipToAndAssert(
            "[IMPORTANT] Preparation: Preparation for evening 8 starts.",
            "[IMPORTANT] Preparation: Preparation for evening 8 starts.",
        )
        skipToAndAssert(PROCURED_RICE, "$PROCURED_RICE 15 g of rice from the supplier.")
    }
}
