package de.unisaarland.cs.se.selab.systemtest.selab26.foh

import de.unisaarland.cs.se.selab.systemtest.api.SystemTestAssertionError
import de.unisaarland.cs.se.selab.systemtest.selab26.utils.LogSkippingSystemTest

private const val PREFERENCE_FOOD = "f26/food_preference_amounts.json"
private const val PREFERENCE_RESTAURANTS = "f26/restaurants_preference_boundary.json"
private const val WAITING_RESTAURANTS = "f27/restaurants_waiting_pair.json"

/** F26/F27: collect the complete relevant trace, including unexpected early or repeated events. */
abstract class PreferenceAndWaitingBoundaryTest : LogSkippingSystemTest() {
    override val logLevel = "INFO"
    override val maxTicks = 14

    protected suspend fun assertTrace(expected: List<String>, deliveryOnly: Boolean = false) {
        val patterns = if (deliveryOnly) {
            listOf(
                "Delivery Arrival",
                "Delivery Finished",
                "Delivery Failed",
                "Delivery Given Up",
                "Delivery Finished Eating",
            )
        } else {
            listOf("FOH Ordering", "FOH Serving", "Restaurant No Eating", "FOH Finished Eating")
        }
        val actual = mutableListOf<String>()
        val clock = Regex("Simulation: Tick (\\d+) \\((\\d+)\\)")
        var time = "before ticks"
        while (true) {
            val line = getNextLine() ?: break
            clock.find(line)?.let { time = "${it.groupValues[2]}/${it.groupValues[1]}" }
            val message = line.substringAfter("] ")
            if (patterns.any { message.startsWith("$it (R ") }) actual += "$time $message"
        }
        if (actual != expected) throw SystemTestAssertionError("Expected trace $expected but got $actual")
    }
}

/** F26: two kinds beat a large amount; equal kind counts choose the highest id per subgroup. */
class F26IngredientAmountsAndTiesRespectSubgroups : PreferenceAndWaitingBoundaryTest() {
    override val name = "F26IngredientAmountsAndTiesRespectSubgroups"
    override val description = "Two subgroups independently choose Egg Rice and Large Rice from an unsorted menu."
    override val food = PREFERENCE_FOOD
    override val restaurants = PREFERENCE_RESTAURANTS
    override val scenario = "f26/scenario_kind_count_and_tie.json"
    override val maxTicks = 1

    override suspend fun run() {
        assertTrace(
            listOf("1/1 FOH Ordering (R 1): Group 1 placed order 1 of Egg Rice:1,Large Rice:1 with waitstaff 1."),
        )
    }
}

/** F26: the event's basic dish wins over both a personal favourite and more preferred ingredients. */
class F26EventFavouriteOverridesPersonalRanking : PreferenceAndWaitingBoundaryTest() {
    override val name = "F26EventFavouriteOverridesPersonalRanking"
    override val description = "All four event members order Plain Rice despite personally favouring Egg Rice."
    override val food = PREFERENCE_FOOD
    override val restaurants = PREFERENCE_RESTAURANTS
    override val scenario = "f26/scenario_event_precedence.json"
    override val maxTicks = 73

    override suspend fun run() {
        assertTrace(
            listOf(
                "4/1 FOH Ordering (R 1): Group 1 placed order 1 of Plain Rice:4 with waitstaff 1.",
                "4/1 FOH Serving (R 1): Waitstaff 1 serves Plain Rice:4 to table 1 0 ticks after ordering.",
            ),
        )
    }
}

/** F27: serving in the fifth waiting tick beats the timeout later in the same tick. */
class F27LastWaitingTickStillAllowsServing : PreferenceAndWaitingBoundaryTest() {
    override val name = "F27LastWaitingTickStillAllowsServing"
    override val description = "A queued dish reaches its group four ticks after ordering; nobody leaves hungry."
    override val food = "f27/food_last_serving_tick.json"
    override val restaurants = WAITING_RESTAURANTS
    override val scenario = "f27/scenario_last_serving_tick.json"

    override suspend fun run() {
        assertTrace(
            listOf(
                "1/1 FOH Ordering (R 1): Group 1 placed order 1 of First Bowl:2 with waitstaff 1.",
                "1/1 FOH Ordering (R 1): Group 2 placed order 2 of Last Bowl:2 with waitstaff 1.",
                "1/2 FOH Serving (R 1): Waitstaff 1 serves First Bowl:2 to table 1 1 ticks after ordering.",
                "1/4 FOH Finished Eating (R 1): 2 customers of group 1 have finished eating at table 1.",
                "1/5 FOH Serving (R 1): Waitstaff 1 serves Last Bowl:2 to table 2 4 ticks after ordering.",
                "1/7 FOH Finished Eating (R 1): 2 customers of group 2 have finished eating at table 2.",
            ),
        )
    }
}

/** F27: the extension ends while the served members finish their two full eating ticks. */
class F27MixedGroupFinishesEatingAndLosesUnservedMember : PreferenceAndWaitingBoundaryTest() {
    override val name = "F27MixedGroupFinishesEatingAndLosesUnservedMember"
    override val description = "Two eaters finish in tick seven; only the third customer leaves without food."
    override val food = "f27/food_mixed_waiting.json"
    override val restaurants = "f27/restaurants_mixed_waiting.json"
    override val scenario = "f27/scenario_mixed_waiting.json"

    override suspend fun run() {
        assertTrace(
            listOf(
                "1/1 FOH Ordering (R 1): Group 1 placed order 1 of " +
                    "First Bowl:1,Last Bowl:1,Middle Bowl:1 with waitstaff 1.",
                "1/5 FOH Serving (R 1): Waitstaff 1 serves First Bowl:1,Middle Bowl:1 " +
                    "to table 1 4 ticks after ordering.",
                "1/7 Restaurant No Eating (R 1): 1 customers of group 1 leave table 1 due to not being served.",
                "1/7 FOH Finished Eating (R 1): 2 customers of group 1 have finished eating at table 1.",
            ),
        )
    }
}

/** F27: wanted tick six plus three additional ticks still permits delivery. */
class F27DeliveryAcceptedAtLastWaitingTick : PreferenceAndWaitingBoundaryTest() {
    override val name = "F27DeliveryAcceptedAtLastWaitingTick"
    override val description = "Arrival at tick nine is accepted, and the group finishes eating exactly at tick eleven."
    override val food = "f27/food_delivery_deadline.json"
    override val restaurants = WAITING_RESTAURANTS
    override val scenario = "f27/scenario_delivery_at_deadline.json"

    override suspend fun run() {
        assertTrace(
            listOf(
                "1/9 Delivery Arrival (R 1): Driver 1 arrived at group 2 with order 2.",
                "1/9 Delivery Finished (R 1): Driver 1 gave delivery of order 2 to group 2.",
                "1/11 Delivery Finished Eating (R 1): Group 2 has finished eating.",
            ),
            deliveryOnly = true,
        )
    }
}

/**
 * F27: the group gives up in tick nine = visitingTick + 3 while its order is still at the desk. The
 * order still goes out once cooked: "the delivery still continues normally until the driver arrives
 * at the group", which then logs Delivery Failed (forum 353, staff). The reference rejected the
 * version of this test that expected the desk to drop the order.
 */
class F27DeliveryRejectedAfterActualGiveUp : PreferenceAndWaitingBoundaryTest() {
    override val name = "F27AnOrderAtTheDeskStillGoesOutAfterGiveUp"
    override val description =
        "The group gives up in tick nine; the order still leaves in tick ten and fails on arrival in eleven."
    override val food = "f27/food_delivery_after_give_up.json"
    override val restaurants = "f27/restaurants_delivery_after_give_up.json"
    override val scenario = "f27/scenario_delivery_after_give_up.json"

    override suspend fun run() {
        assertTrace(
            listOf(
                "1/9 Delivery Given Up (R 1): Group 2 gave up on waiting for delivery of order 2.",
                "1/11 Delivery Arrival (R 1): Driver 1 arrived at group 2 with order 2.",
                "1/11 Delivery Failed (R 1): Driver 1 failed to deliver order 2 to group 2.",
            ),
            deliveryOnly = true,
        )
    }
}
