package de.unisaarland.cs.se.selab.systemtest.selab26.incidents

import de.unisaarland.cs.se.selab.systemtest.selab26.utils.LogSkippingSystemTest

private const val DEBUG = "DEBUG"
private const val FIVE_EVENINGS = 120

/**
 * F33: a PACKAGING incident applies from its own evening even when the pantry already holds
 * stock bought under the old package size. The old stock is removed on expiry (not repackaged
 * retroactively) and the very next purchase already uses the new size.
 */
class F33PackagingIncidentAppliesFromItsOwnEveningWithExistingStock : LogSkippingSystemTest() {
    override val name = "F33PackagingIncidentAppliesFromItsOwnEveningWithExistingStock"
    override val description =
        "A 20 g package of rice bought in evening 1 expires in evening 3, the same evening a packaging " +
            "change to 5 g packages occurs; the next purchase is already sized 10 g (two new packages)."
    override val food = "incidents/food_rice_long_shelf_life.json"
    override val restaurants = "incidents/restaurants_ten_seats.json"
    override val scenario = "incidents/scenario_packaging_change_with_existing_stock.json"
    override val logLevel = DEBUG
    override val maxTicks = FIVE_EVENINGS

    override suspend fun run() {
        skipToAndAssert(
            "[IMPORTANT] Preparation",
            "[IMPORTANT] Preparation: Preparation for evening 1 starts.",
        )
        assertNextLine("[DEBUG] Pantry (R 1): Procured 20 g of rice from the supplier.")
        skipToAndAssert(
            "[IMPORTANT] Incident",
            "[IMPORTANT] Incident: Incident 1 of type PACKAGING occurred before evening 3.",
        )
        skipToAndAssert(
            "[IMPORTANT] Preparation",
            "[IMPORTANT] Preparation: Preparation for evening 3 starts.",
        )
        assertNextLine("[DEBUG] Pantry (R 1): Removed 20 g of rice from the pantry.")
        assertNextLine("[DEBUG] Pantry (R 1): Procured 10 g of rice from the supplier.")
    }
}
