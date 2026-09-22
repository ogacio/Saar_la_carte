package de.unisaarland.cs.se.selab.systemtest.selab26.foh

import de.unisaarland.cs.se.selab.systemtest.selab26.utils.LogSkippingSystemTest

private const val DEBUG = "DEBUG"
private const val THREE_EVENINGS = 72
private const val PREPARATION_PREFIX = "[IMPORTANT] Preparation"

/** F28: a DRIVER incident that removes the only driver blocks deliveries every evening after. */
class F28DriverRemovalPersistsAcrossEvenings : LogSkippingSystemTest() {
    override val name = "F28DriverRemovalPersistsAcrossEvenings"
    override val description =
        "Evening 1 delivers successfully, then a DRIVER incident makes evenings 2 and 3 both fail to decide."
    override val food = "foh/f28b_fluseason/food_rice.json"
    override val restaurants = "foh/f28b_fluseason/restaurants_one_driver.json"
    override val scenario = "foh/f28b_fluseason/scenario_driver_removed.json"
    override val logLevel = DEBUG
    override val maxTicks = THREE_EVENINGS

    override suspend fun run() {
        skipToAndAssert(
            "[DEBUG] Restaurant Decision",
            "[DEBUG] Restaurant Decision: Group 1 decided on restaurant 1.",
        )
        skipToAndAssert(
            "[IMPORTANT] Incident",
            "[IMPORTANT] Incident: Incident 1 of type STAFF occurred before evening 2.",
        )
        skipToAndAssert(PREPARATION_PREFIX, "[IMPORTANT] Preparation: Preparation for evening 2 starts.")
        skipToAndAssert(
            "[DEBUG] Restaurant No Decision",
            "[DEBUG] Restaurant No Decision: Group 2 could not decide for a restaurant.",
        )
        skipToAndAssert(PREPARATION_PREFIX, "[IMPORTANT] Preparation: Preparation for evening 3 starts.")
        skipToAndAssert(
            "[DEBUG] Restaurant No Decision",
            "[DEBUG] Restaurant No Decision: Group 3 could not decide for a restaurant.",
        )
    }
}
