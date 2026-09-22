package de.unisaarland.cs.se.selab.systemtest.selab26.delivery

import de.unisaarland.cs.se.selab.systemtest.api.SystemTestAssertionError
import de.unisaarland.cs.se.selab.systemtest.selab26.utils.LogSkippingSystemTest

private const val DEBUG = "DEBUG"
private const val DRIVING_PREFIX = "[DEBUG] Delivery Driving"
private const val RETURNED_PREFIX = "[INFO] Delivery Returned"
private const val ONE_EVENING = 24

/** F29: a 12 km delivery drives 5/5/2 km per tick and the return leg logs no driving. */
class F29ThreeTickDistanceAndSilentReturn : LogSkippingSystemTest() {
    override val name = "F29ThreeTickDistanceAndSilentReturn"
    override val description =
        "A 12 km delivery takes 3 ticks out (5/5/2 km per tick), then returns silently in 3 more."
    override val food = "foh/food_rice.json"
    override val restaurants = "foh/f20/restaurants_one_driver.json"
    override val scenario = "delivery/f29/scenario_delivery_twelve_km.json"
    override val logLevel = DEBUG
    override val maxTicks = ONE_EVENING

    override suspend fun run() {
        skipToAndAssert(
            "[INFO] Delivery Preparation",
            "[INFO] Delivery Preparation (R 1): Driver 1 prepares driving order 1 to group 1, " +
                "which will take 3 ticks.",
        )
        skipToAndAssert(
            DRIVING_PREFIX,
            "[DEBUG] Delivery Driving (R 1): Driver 1 drove 5 km and needs 2 more ticks.",
        )
        skipToAndAssert(
            DRIVING_PREFIX,
            "[DEBUG] Delivery Driving (R 1): Driver 1 drove 5 km and needs 1 more ticks.",
        )
        skipToAndAssert(
            DRIVING_PREFIX,
            "[DEBUG] Delivery Driving (R 1): Driver 1 drove 2 km and needs 0 more ticks.",
        )
        assertNextLine("[INFO] Delivery Arrival (R 1): Driver 1 arrived at group 1 with order 1.")
        assertNextLine("[IMPORTANT] Delivery Finished (R 1): Driver 1 gave delivery of order 1 to group 1.")
        skipToReturnWithoutDriving()
        assertCurrentLine("[INFO] Delivery Returned (R 1): Driver 1 has returned.")
    }

    private suspend fun skipToReturnWithoutDriving() {
        while (true) {
            val line = getNextLine() ?: throw SystemTestAssertionError("End of log reached before '$RETURNED_PREFIX'.")
            if (line.startsWith(DRIVING_PREFIX)) {
                throw SystemTestAssertionError("Unexpected driving log on the return leg: '$line'.")
            }
            if (line.startsWith(RETURNED_PREFIX)) return
        }
    }
}
