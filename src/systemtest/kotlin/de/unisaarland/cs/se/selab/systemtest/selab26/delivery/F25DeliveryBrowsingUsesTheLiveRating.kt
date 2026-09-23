package de.unisaarland.cs.se.selab.systemtest.selab26.delivery

import de.unisaarland.cs.se.selab.systemtest.selab26.utils.LogSkippingSystemTest

private const val INFO = "INFO"
private const val ONE_EVENING = 24

/**
 * The browsing service ranks on the rating as it stands in the deciding tick, not on the rating the
 * restaurant started the simulation with.
 *
 * Beta (restaurant 2) opens one positive rating ahead of Alpha (restaurant 1). A regular group
 * bound to Alpha eats and rates it positive in tick 3, which levels the score at 1 each. The
 * delivery group decides in tick 20 - 1 - 3 = 16 and now sees a tie, which "prefer the lowest id as
 * final tie-break" resolves to Alpha.
 *
 * A mutant that ranks on the initial ratings still sees 1 against 0 and sends the delivery to Beta,
 * so the order is logged under the wrong restaurant.
 */
class F25DeliveryBrowsingUsesTheLiveRating : LogSkippingSystemTest() {
    override val name = "F25DeliveryBrowsingUsesTheLiveRating"
    override val description =
        "A regular's rating levels the two restaurants, so the later delivery goes to the lower id."
    override val food = "delivery/browsing/food_two_dishes.json"
    override val restaurants = "delivery/browsing/restaurants_beta_one_ahead.json"
    override val scenario = "delivery/browsing/scenario_regular_lifts_alpha.json"
    override val logLevel = INFO
    override val maxTicks = ONE_EVENING

    override suspend fun run() {
        // The regular lifts Alpha from 0 to 1, levelling it with Beta's opening rating.
        skipToAndAssert(
            "[INFO] Rating (R 1): Group 1",
            "[INFO] Rating (R 1): Group 1 rates the restaurant 1 with POSITIVE rating, " +
                "leading to 1 positive ratings and 0 negative ratings.",
        )
        skipToAndAssert(
            "[IMPORTANT] Simulation: Tick 16 ",
            "[IMPORTANT] Simulation: Tick 16 (1) started.",
        )
        skipToAndAssert(
            "[IMPORTANT] FOH Ordering",
            "[IMPORTANT] FOH Ordering (R 1): Group 2 placed order 2 of Rice Bowl:2.",
        )
    }
}
