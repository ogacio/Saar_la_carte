package de.unisaarland.cs.se.selab.systemtest.selab26.incidents

import de.unisaarland.cs.se.selab.systemtest.api.SystemTestAssertionError
import de.unisaarland.cs.se.selab.systemtest.selab26.utils.LogSkippingSystemTest

private const val ONE_EVENING = 24
private const val PROCURED_RICE = "[DEBUG] Pantry (R 1): Procured 10 g of rice from the supplier."

/**
 * F02: `--logLevel DEBUG` includes DEBUG-level lines. Paired with
 * [F02LogLevelImportantSuppressesDebugLines], which runs the identical scenario at IMPORTANT
 * and asserts the same line is absent, proving the CLI level filter actually filters.
 */
class F02LogLevelDebugIncludesDebugLines : LogSkippingSystemTest() {
    override val name = "F02LogLevelDebugIncludesDebugLines"
    override val description = "At --logLevel DEBUG a DEBUG-level Pantry procurement line is present."
    override val food = "incidents/food_rice_ten.json"
    override val restaurants = "incidents/restaurants_ten_seats.json"
    override val scenario = "incidents/scenario_recipe_change.json"
    override val logLevel = "DEBUG"
    override val maxTicks = ONE_EVENING

    override suspend fun run() {
        skipToAndAssert(PROCURED_RICE, PROCURED_RICE)
    }
}

/**
 * F02: the identical scenario at `--logLevel IMPORTANT` must never emit the DEBUG-level line
 * [F02LogLevelDebugIncludesDebugLines] proved present at DEBUG.
 */
class F02LogLevelImportantSuppressesDebugLines : LogSkippingSystemTest() {
    override val name = "F02LogLevelImportantSuppressesDebugLines"
    override val description = "At --logLevel IMPORTANT the same DEBUG-level Pantry procurement line never appears."
    override val food = "incidents/food_rice_ten.json"
    override val restaurants = "incidents/restaurants_ten_seats.json"
    override val scenario = "incidents/scenario_recipe_change.json"
    override val logLevel = "IMPORTANT"
    override val maxTicks = ONE_EVENING

    override suspend fun run() {
        while (true) {
            val line = getNextLine() ?: return
            if (line == PROCURED_RICE) {
                throw SystemTestAssertionError("DEBUG-level line leaked through at --logLevel IMPORTANT: '$line'.")
            }
        }
    }
}
