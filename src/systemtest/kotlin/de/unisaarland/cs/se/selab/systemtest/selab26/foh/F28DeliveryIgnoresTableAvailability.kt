package de.unisaarland.cs.se.selab.systemtest.selab26.foh

import de.unisaarland.cs.se.selab.systemtest.selab26.utils.LogSkippingSystemTest

private const val DEBUG = "DEBUG"
private const val ONE_EVENING = 24

/** F28: a delivery group must still decide on a restaurant with zero free tables, as long as a driver is free. */
class F28DeliveryIgnoresTableAvailability : LogSkippingSystemTest() {
    override val name = "F28DeliveryIgnoresTableAvailability"
    override val description =
        "The only table stays reserved for group 1 all evening; group 2's delivery still decides on restaurant 1."
    override val food = "foh/food_rice.json"
    override val restaurants = "foh/f28e_notmytype/restaurants_one_table_one_driver.json"
    override val scenario = "foh/f28e_notmytype/scenario_delivery_despite_full_tables.json"
    override val logLevel = DEBUG
    override val maxTicks = ONE_EVENING

    override suspend fun run() {
        skipToAndAssert(
            "[IMPORTANT] FOH Seating",
            "[IMPORTANT] FOH Seating (R 1): Group 1 seated at table 1 by waitstaff 1.",
        )
        skipToAndAssert(
            "[DEBUG] Restaurant Decision",
            "[DEBUG] Restaurant Decision: Group 2 decided on restaurant 1.",
        )
    }
}
