package de.unisaarland.cs.se.selab.systemtest.selab26.foh

import de.unisaarland.cs.se.selab.systemtest.selab26.utils.LogSkippingSystemTest

private const val INFO = "INFO"
private const val ONE_EVENING = 24

/**
 * Probe for the open "+2 more ticks" question (forum 227 vs 340), deliberately not registered for
 * the mutants: only the reference implementation run can answer it.
 *
 * Group 2 orders in tick 3. One member wants Quick Bowl (2 min, EXEC) and is served in tick 5; the
 * other wants Stew Two (40 min, SOUS), and the SOUS cook is busy all evening with group 1's Stew
 * One, so that member is never served. The two readings of "the group waits 2 more ticks" then
 * disagree by two ticks:
 *
 *  * thread 227, "a one-time extension of the patience": the unserved member leaves at
 *    order + PATIENCE_TICKS + EXTRA_PATIENCE_TICKS = 3 + 5 + ... , i.e. tick 9. This is what we do.
 *  * thread 340, "the 2 ticks count from the first serve": the unserved member leaves at
 *    firstServed + 2 = 5 + 2 = tick 7.
 *
 * The assertions below pin our reading. The point of the test is the report: if it fails, the
 * reference uses thread 340's reading and [de.unisaarland.cs.se.selab.foh.visit.AwaitingMealState]
 * has to measure the extension from the first serve instead of from the order.
 */
class F21ExtraPatienceAfterPartialServe : LogSkippingSystemTest() {
    override val name = "F21ExtraPatienceAfterPartialServe"
    override val description =
        "A partially served group: does the unserved member leave 2 ticks after the serve, or 2 ticks " +
            "after the normal patience runs out?"
    override val food = "foh/f21_extra_patience/food_quick_and_two_stews.json"
    override val restaurants = "foh/f21_extra_patience/restaurants_exec_and_sous.json"
    override val scenario = "foh/f21_extra_patience/scenario_partial_serve.json"
    override val logLevel = INFO
    override val maxTicks = ONE_EVENING

    override suspend fun run() {
        skipToAndAssert(
            "[IMPORTANT] FOH Ordering (R 1): Group 2",
            "[IMPORTANT] FOH Ordering (R 1): Group 2 placed order 2 of Quick Bowl:1,Stew Two:1 with waitstaff 1.",
        )
        // The Quick Bowl is served in tick 5, two ticks after the order. This is the partial serve
        // that grants the group its extra patience.
        skipToAndAssert(
            "[IMPORTANT] FOH Serving (R 1): Waitstaff 1 serves Quick Bowl",
            "[IMPORTANT] FOH Serving (R 1): Waitstaff 1 serves Quick Bowl:1 to table 2 2 ticks after ordering.",
        )
        // Nothing leaves in tick 7, where "2 ticks after the first serve" would put it. Skipping
        // past tick 9 consumes such a line, so the assertion after this one would run out of log.
        skipToAndAssert(
            "[IMPORTANT] Simulation: Tick 9 ",
            "[IMPORTANT] Simulation: Tick 9 (1) started.",
        )
        skipToAndAssert(
            "[INFO] Restaurant No Eating",
            "[INFO] Restaurant No Eating (R 1): 1 customers of group 2 leave table 2 due to not being served.",
        )
        skipToAndAssert(
            "[INFO] Rating (R 1): Group 2",
            "[INFO] Rating (R 1): Group 2 rates the restaurant 1 with NEGATIVE rating, " +
                "leading to 1 positive ratings and 1 negative ratings.",
        )
    }
}
