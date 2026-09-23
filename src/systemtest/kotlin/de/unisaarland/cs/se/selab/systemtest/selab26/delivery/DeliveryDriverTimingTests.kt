package de.unisaarland.cs.se.selab.systemtest.selab26.delivery

import de.unisaarland.cs.se.selab.systemtest.api.SystemTestAssertionError
import de.unisaarland.cs.se.selab.systemtest.selab26.utils.LogSkippingSystemTest

private const val FOOD = "delivery/teodor/food_rice_and_slow_bowls.json"
private const val RESTAURANTS = "delivery/teodor/restaurants_one_driver.json"
private const val GIVE_UP_ON_THE_ROAD = "delivery/teodor/scenario_give_up_on_the_road.json"

/**
 * One restaurant, one EXEC cook, one driver. Collects the log lines that start with one of [prefixes]
 * (after the log level), each prefixed with "evening/tick", so the test pins the tick of every line.
 */
abstract class DeliveryTimingTest : LogSkippingSystemTest() {
    override val food = FOOD
    override val restaurants = RESTAURANTS
    override val logLevel = "DEBUG"
    override val maxTicks = 20

    protected suspend fun assertTrace(prefixes: List<String>, expected: List<String>) {
        val clock = Regex("Simulation: Tick (\\d+) \\((\\d+)\\)")
        val actual = mutableListOf<String>()
        var time = "before ticks"
        while (true) {
            val line = getNextLine() ?: break
            clock.find(line)?.let { time = "${it.groupValues[2]}/${it.groupValues[1]}" }
            val message = line.substringAfter("] ")
            if (prefixes.any { message.startsWith(it) }) actual += "$time $message"
        }
        if (actual != expected) throw SystemTestAssertionError("Expected trace $expected but got $actual")
    }
}

/**
 * F20/F29: meals handed to a driver are no longer waiting in the kitchen. The order is cooked and
 * handed over in tick 5, so the Kitchen Status of tick 6 has nothing left to serve.
 */
class F20KitchenStatusForgetsMealsHandedToTheDriver : DeliveryTimingTest() {
    override val name = "F20KitchenStatusForgetsMealsHandedToTheDriver"
    override val description = "After the hand-over in tick 5, tick 6's Kitchen Status has 0 servable meals."
    override val scenario = "delivery/teodor/scenario_one_delivery_ten_km.json"

    override suspend fun run() {
        skipToAndAssert(
            "[IMPORTANT] FOH Delivery",
            "[IMPORTANT] FOH Delivery (R 1): Waitstaff 1 serves Rice Bowl:2 meals to driver 1 for order 1.",
        )
        skipToAndAssert("[IMPORTANT] Simulation: Tick 6 ", "[IMPORTANT] Simulation: Tick 6 (1) started.")
        skipToAndAssert(
            "[DEBUG] Kitchen Status",
            "[DEBUG] Kitchen Status (R 1): 0 cooks were active cooking 0 and finishing 0 meals. " +
                "0 meals can be served by the waitstaff.",
        )
    }
}

/**
 * F29: the driving log names the distance driven so far (forum 126, staff): 5, 10 and then all 15 km.
 * The return trip is not logged; the driver is back after as many ticks as the way out took.
 */
class F29DrivingLogShowsTheDistanceSoFar : DeliveryTimingTest() {
    override val name = "F29DrivingLogShowsTheDistanceSoFar"
    override val description = "A 15 km delivery logs 5, 10 and 15 km, arrives in tick 12 and is back in 15."
    override val scenario = GIVE_UP_ON_THE_ROAD

    override suspend fun run() {
        assertTrace(
            listOf("Delivery Preparation", "Delivery Driving", "Delivery Arrival", "Delivery Returned"),
            listOf(
                "1/9 Delivery Preparation (R 1): Driver 1 prepares driving order 1 to group 1, " +
                    "which will take 3 ticks.",
                "1/10 Delivery Driving (R 1): Driver 1 drove 5 km and needs 2 more ticks.",
                "1/11 Delivery Driving (R 1): Driver 1 drove 10 km and needs 1 more ticks.",
                "1/12 Delivery Driving (R 1): Driver 1 drove 15 km and needs 0 more ticks.",
                "1/12 Delivery Arrival (R 1): Driver 1 arrived at group 1 with order 1.",
                "1/15 Delivery Returned (R 1): Driver 1 has returned.",
            ),
        )
    }
}

/**
 * F24/F27: visitingTick 8, so the group gives up at the end of tick 11 = visitingTick + 3 and rates
 * NEGATIVE in that tick. The two dishes are cooked one after the other (ticks 2-5 and 6-9), the driver
 * leaves in tick 9 and needs 3 ticks: it is still on the road, keeps driving and fails in tick 12.
 */
class F24GroupGivesUpWhileTheDriverIsOnTheRoad : DeliveryTimingTest() {
    override val name = "F24GroupGivesUpWhileTheDriverIsOnTheRoad"
    override val description = "Give-up and NEGATIVE rating in tick 11; the driver still arrives in 12 and fails."
    override val scenario = GIVE_UP_ON_THE_ROAD

    override suspend fun run() {
        assertTrace(
            listOf("Delivery Finished", "Delivery Failed", "Delivery Given Up", "Rating (R"),
            listOf(
                "1/11 Delivery Given Up (R 1): Group 1 gave up on waiting for delivery of order 1.",
                "1/11 Rating (R 1): Group 1 rates the restaurant 1 with NEGATIVE rating, " +
                    "leading to 0 positive ratings and 1 negative ratings.",
                "1/12 Delivery Failed (R 1): Driver 1 failed to deliver order 1 to group 1.",
            ),
        )
    }
}

/**
 * F28/F29: a driver on the way back is not free (forum 174, staff). Group 1 (7 km, decides in tick 5)
 * gets the only driver, who arrives in tick 7 and is back in tick 9. Group 2 decides in tick 8 and
 * finds no driver; group 3 decides in tick 16 and gets it.
 */
class F29ReturningDriverIsNotFreeForTheNextGroup : DeliveryTimingTest() {
    override val name = "F29ReturningDriverIsNotFreeForTheNextGroup"
    override val description = "Group 2 finds no driver in tick 8 while it drives back; group 3 gets it in 16."
    override val scenario = "delivery/teodor/scenario_driver_busy_until_back.json"

    override suspend fun run() {
        assertTrace(
            listOf("Restaurant Decision", "Restaurant No Decision", "Delivery Returned"),
            listOf(
                "1/5 Restaurant Decision: Group 1 decided on restaurant 1.",
                "1/8 Restaurant No Decision: Group 2 could not decide for a restaurant.",
                "1/9 Delivery Returned (R 1): Driver 1 has returned.",
                "1/16 Restaurant Decision: Group 3 decided on restaurant 1.",
                "1/18 Delivery Returned (R 1): Driver 1 has returned.",
            ),
        )
    }
}
