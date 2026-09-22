package de.unisaarland.cs.se.selab.systemtest.selab26.delivery

import de.unisaarland.cs.se.selab.systemtest.selab26.utils.LogSkippingSystemTest

private const val DEBUG = "DEBUG"
private const val ONE_EVENING = 24

/** F24: two deliveries queue on one cook, the second gives up at visitingTick + 3. */
class F24GivesUpThreeTicksAfterVisitingTick : LogSkippingSystemTest() {
    override val name = "F24GivesUpThreeTicksAfterVisitingTick"
    override val description =
        "Group 2's delivery queues behind group 1's, arrives after visitingTick + 3 and is rejected."
    override val food = "delivery/f24b_impatience/food_slow_dish.json"
    override val restaurants = "delivery/f24b_impatience/restaurants_one_driver.json"
    override val scenario = "delivery/f24b_impatience/scenario_late_delivery.json"
    override val logLevel = DEBUG
    override val maxTicks = ONE_EVENING

    override suspend fun run() {
        skipToAndAssert(
            "[IMPORTANT] Delivery Failed",
            "[IMPORTANT] Delivery Failed (R 1): Driver 1 failed to deliver order 2 to group 2.",
        )
        assertNextLine("[INFO] Delivery Given Up (R 1): Group 2 gave up on waiting for delivery of order 2.")
        skipToAndAssert(
            "[INFO] Rating (R 1): Group 2",
            "[INFO] Rating (R 1): Group 2 rates the restaurant 1 with NEGATIVE rating, " +
                "leading to 1 positive ratings and 1 negative ratings.",
        )
    }
}
