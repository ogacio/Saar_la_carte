package de.unisaarland.cs.se.selab.systemtest.selab26.delivery

import de.unisaarland.cs.se.selab.systemtest.selab26.utils.LogSkippingSystemTest

private const val DEBUG = "DEBUG"
private const val TEN_EVENINGS = 240

/**
 * A 10-evening, 2-restaurant fixture stressing delivery: multiple simultaneous delivery
 * decisions against a limited driver pool, a driver staff change mid-run, cross-restaurant
 * driver-id independence, incidents (RECIPE/PACKAGING/UNAVAILABLE/STAFF) landing while
 * deliveries are active, and the full 3x3 delivery-experience x rating-likelihood matrix.
 * A coarse regression guard: the final statistics are the one thing every group's outcome
 * (finished, given up, failed, or never decided) is folded into, so any silent order drop or
 * rating-count corruption shows up here even without asserting every one of the run's ~4700
 * log lines individually.
 */
class GiantDeliveryStress : LogSkippingSystemTest() {
    override val name = "GiantDeliveryStress"
    override val description =
        "10-evening delivery/rating stress across two restaurants; asserts the final statistics stay internally " +
            "consistent with what every customer group actually experienced."
    override val food = "giant/delivery_stress/food.json"
    override val restaurants = "giant/delivery_stress/restaurants.json"
    override val scenario = "giant/delivery_stress/scenario.json"
    override val logLevel = DEBUG
    override val maxTicks = TEN_EVENINGS

    override suspend fun run() {
        assertStatistics(1, cooked = 28, served = 18, delivered = 10, ratings = 17)
        assertStatistics(2, cooked = 13, served = 8, delivered = 5, ratings = 8)
    }
}
