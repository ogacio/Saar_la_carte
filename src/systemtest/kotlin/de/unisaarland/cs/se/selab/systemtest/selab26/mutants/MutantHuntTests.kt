package de.unisaarland.cs.se.selab.systemtest.selab26.mutants

import de.unisaarland.cs.se.selab.systemtest.api.SystemTestAssertionError
import de.unisaarland.cs.se.selab.systemtest.selab26.utils.LogSkippingSystemTest

private const val DIR = "mutanthunt"
private const val SAFFRON_FOOD = "$DIR/food_saffron_chicken_egg.json"
private const val ONE_ASIAN = "$DIR/restaurants_one_asian.json"
private const val MEMBER_CANNOT_ORDER = "$DIR/scenario_one_member_cannot_order.json"

/**
 * Tests aimed at the mutants still not found on d4ef9e0 (Arbeitszeitbetrug, FreeForAll, Backlash,
 * DinnerForOne, KingOfTheHill, ShortStaffed). Each pins one rule the earlier tests left open.
 * [trace] collects the messages that start with one of the prefixes, each tagged "evening/tick".
 */
abstract class MutantHuntTest : LogSkippingSystemTest() {
    override val logLevel = "DEBUG"

    protected suspend fun trace(prefixes: List<String>): List<String> {
        val clock = Regex("Simulation: Tick (\\d+) \\((\\d+)\\)")
        val lines = mutableListOf<String>()
        var time = "before ticks"
        while (true) {
            val line = getNextLine() ?: break
            clock.find(line)?.let { time = "${it.groupValues[2]}/${it.groupValues[1]}" }
            val message = line.substringAfter("] ")
            if (prefixes.any { message.startsWith(it) }) lines += "$time $message"
        }
        return lines
    }

    protected suspend fun assertTrace(prefixes: List<String>, expected: List<String>) {
        val actual = trace(prefixes)
        if (actual != expected) throw SystemTestAssertionError("Expected trace $expected but got $actual")
    }
}

/**
 * Arbeitszeitbetrug (F30): the end of the evening aborts a delivery that is still on its way to the
 * customer, "without rating or other consequences", and the driver goes home (forum 121, 286). The
 * three dishes are cooked one after the other, so the order leaves in tick 22 on a 5-tick trip; the
 * group gives up in tick 24 = visitingTick + 3. Nothing about the delivery may follow in evening 2.
 */
class F30DeliveryStillOnTheRoadAtTheEndOfTheEveningIsAborted : MutantHuntTest() {
    override val name = "F30DeliveryStillOnTheRoadAtTheEndOfTheEveningIsAborted"
    override val description = "A driver still 15 km out when evening 1 ends never arrives, not even in evening 2."
    override val food = "$DIR/food_slow_and_quick_bowl.json"
    override val restaurants = "$DIR/restaurants_one_driver.json"
    override val scenario = "$DIR/scenario_delivery_out_at_evening_end.json"
    override val maxTicks = 48

    override suspend fun run() {
        assertTrace(
            listOf("Delivery ", "Rating ("),
            listOf(
                "1/22 Delivery Preparation (R 1): Driver 1 prepares driving order 1 to group 1, " +
                    "which will take 5 ticks.",
                "1/23 Delivery Driving (R 1): Driver 1 drove 5 km and needs 4 more ticks.",
                "1/24 Delivery Driving (R 1): Driver 1 drove 10 km and needs 3 more ticks.",
                "1/24 Delivery Given Up (R 1): Group 1 gave up on waiting for delivery of order 1.",
                "1/24 Rating (R 1): Group 1 rates the restaurant 1 with NEGATIVE rating, " +
                    "leading to 0 positive ratings and 1 negative ratings.",
            ),
        )
    }
}

/**
 * FreeForAll (F28): the browsing service knows "whether they can deliver food or host events". An
 * EVENT group only chooses a restaurant that hosts events: restaurant 1 has the lower id and the
 * same ratings, but "event": false.
 */
class F28EventGroupOnlyChoosesARestaurantThatHostsEvents : MutantHuntTest() {
    override val name = "F28EventGroupOnlyChoosesARestaurantThatHostsEvents"
    override val description = "Restaurant 1 does not host events, so the EVENT group decides on restaurant 2."
    override val food = SAFFRON_FOOD
    override val restaurants = "$DIR/restaurants_only_second_hosts_events.json"
    override val scenario = "$DIR/scenario_event_group.json"
    override val maxTicks = 1

    override suspend fun run() {
        skipToAndAssert(
            "[DEBUG] Restaurant",
            "[DEBUG] Restaurant Decision: Group 1 decided on restaurant 2.",
        )
    }
}

/**
 * Backlash (P05): "you rate always for the full group" and if part of it could not order, "the rating
 * of the group will be negative" (forum 268). Saffron is unavailable, so the member who excludes
 * chicken has nothing to order and leaves; the other one is served at once and still rates NEGATIVE.
 */
class P05GroupWithAMemberWhoCouldNotOrderRatesNegative : MutantHuntTest() {
    override val name = "P05GroupWithAMemberWhoCouldNotOrderRatesNegative"
    override val description = "One member finds no dish, the other is served on time: the group rates NEGATIVE."
    override val food = SAFFRON_FOOD
    override val restaurants = ONE_ASIAN
    override val scenario = MEMBER_CANNOT_ORDER
    override val maxTicks = 12

    override suspend fun run() {
        assertTrace(
            listOf("Rating ("),
            listOf(
                "1/3 Rating (R 1): Group 1 rates the restaurant 1 with NEGATIVE rating, " +
                    "leading to 0 positive ratings and 1 negative ratings.",
            ),
        )
    }
}

/**
 * DinnerForOne (F07): only the customer who ordered is cooked for, served, eats and is counted. The
 * member who could not order leaves at once and appears in no eating line and no statistic.
 */
class F07OnlyTheMemberWhoOrderedEatsAndIsCounted : MutantHuntTest() {
    override val name = "F07OnlyTheMemberWhoOrderedEatsAndIsCounted"
    override val description = "Of a group of 2 only 1 orders: 1 finishes eating, 1 meal cooked, 1 customer served."
    override val food = SAFFRON_FOOD
    override val restaurants = ONE_ASIAN
    override val scenario = MEMBER_CANNOT_ORDER
    override val maxTicks = 12

    override suspend fun run() {
        skipToAndAssert(
            "[INFO] FOH Finished Eating",
            "[INFO] FOH Finished Eating (R 1): 1 customers of group 1 have finished eating at table 1.",
        )
        assertStatistics(1, cooked = 1, served = 1, delivered = 0, ratings = 1)
    }
}

/**
 * KingOfTheHill (F18): customers order "starting with the customer(s) that have the most excluded
 * ingredients and then the least favorite dishes". Both exclude one ingredient; the second one has
 * fewer favourites, orders first and takes the only portion of saffron, so the first one falls back
 * to their second favourite and nobody leaves. In list order the second one would find nothing.
 */
class F18FewerFavouritesOrderFirstAtEqualExclusions : MutantHuntTest() {
    override val name = "F18FewerFavouritesOrderFirstAtEqualExclusions"
    override val description = "At equal exclusions the customer with fewer favourites orders first; both get a dish."
    override val food = SAFFRON_FOOD
    override val restaurants = ONE_ASIAN
    override val scenario = "$DIR/scenario_fewer_favourites_order_first.json"
    override val maxTicks = 1

    override suspend fun run() {
        val lines = trace(listOf("FOH Ordering (R", "FOH No Ordering (R"))
        val order = lines.singleOrNull()
            ?: throw SystemTestAssertionError("Expected exactly one ordering line but got $lines")
        val expectedStart = "1/1 FOH Ordering (R 1): Group 1 placed order 1 of "
        val dishes = order.removePrefix(expectedStart).removeSuffix(" with waitstaff 1.").split(",").toSet()
        if (!order.startsWith(expectedStart) || dishes != setOf("Saffron Rice:1", "Chicken Rice:1")) {
            throw SystemTestAssertionError("Expected Saffron Rice:1 and Chicken Rice:1 but got '$order'")
        }
    }
}

/**
 * ShortStaffed (F16): a group that found no free waiter "tries again the next tick" without arriving
 * again. Group 2 arrives once in tick 3, finds no waiter and is seated in tick 4.
 */
class F16RetryingGroupIsSeatedWithoutArrivingAgain : MutantHuntTest() {
    override val name = "F16RetryingGroupIsSeatedWithoutArrivingAgain"
    override val description = "Group 2 arrives only in tick 3, has no waiter there and is seated in tick 4."
    override val food = "foh/food_rice.json"
    override val restaurants = "foh/f16/restaurants_ten_and_two.json"
    override val scenario = "foh/f16/scenario_ten_then_two.json"
    override val maxTicks = 5

    override suspend fun run() {
        val group2 = trace(listOf("Restaurant Arrival", "FOH No Seating", "FOH Seating"))
            .filter { "group 2" in it.lowercase() }
        val expected = listOf(
            "1/3 Restaurant Arrival (R 1): Group 2 arrived at restaurant 1.",
            "1/3 FOH No Seating (R 1): No free waitstaff available for group 2.",
            "1/4 FOH Seating (R 1): Group 2 seated at table 2 by waitstaff 1.",
        )
        if (group2 != expected) throw SystemTestAssertionError("Expected trace $expected but got $group2")
    }
}
