package de.unisaarland.cs.se.selab.systemtest.selab26.kitchen

import de.unisaarland.cs.se.selab.systemtest.selab26.utils.LogSkippingSystemTest

private const val DEBUG = "DEBUG"
private const val FOUR_EVENINGS = 96
private const val PROCURED = "[DEBUG] Pantry (R 1): Procured 50 g of rice from the supplier."
private const val PREPARATION = "[IMPORTANT] Preparation: Preparation for evening "

/**
 * The four-evening pantry cycle, with one regular group of two eating 2 g of rice per evening out
 * of 50 g packages whose bestBefore is 2.
 *
 *  * evening 1: nothing in stock, so one whole package is bought (F08, whole packages only)
 *  * evening 2: 48 g left over covers the evening, so the supplier is not called at all
 *    ("the supplier buys only what is missing")
 *  * evening 3: the evening-1 delivery has now lasted its two evenings and is thrown out. The
 *    removal reports the 46 g actually left, not the 50 g bought, and a fresh package is bought
 *  * evening 4: the new package covers it again, so no procurement
 *
 * A Pantry mutant that ages a delivery on the wrong counter, that reports the delivered amount
 * instead of the remaining one, or that re-buys whole packages while stock is still good, moves or
 * changes one of the four lines below.
 */
class F08LeftoversSuppressProcurementUntilTheBatchExpires : LogSkippingSystemTest() {
    override val name = "F08LeftoversSuppressProcurementUntilTheBatchExpires"
    override val description =
        "Leftovers cover evening 2, the evening-1 batch expires before evening 3 and 46 g are removed."
    override val food = "delivery/browsing/food_expiring_rice.json"
    override val restaurants = "delivery/browsing/restaurants_small_pantry.json"
    override val scenario = "delivery/browsing/scenario_regular_three_evenings.json"
    override val logLevel = DEBUG
    override val maxTicks = FOUR_EVENINGS

    override suspend fun run() {
        skipToAndAssert(PREPARATION, PREPARATION + "1 starts.")
        assertNextLine(PROCURED)
        // Evening 2 buys nothing: the next Procured line is the one after the expiry in evening 3.
        skipToAndAssert(PREPARATION, PREPARATION + "2 starts.")
        skipToAndAssert(PREPARATION, PREPARATION + "3 starts.")
        assertNextLine("[DEBUG] Pantry (R 1): Removed 46 g of rice from the pantry.")
        assertNextLine(PROCURED)
        // Evening 4 is covered by the package bought in evening 3.
        skipToAndAssert(PREPARATION, PREPARATION + "4 starts.")
        assertNextLine("[INFO] Pantry (R 1): Restocked ingredients.")
    }
}
