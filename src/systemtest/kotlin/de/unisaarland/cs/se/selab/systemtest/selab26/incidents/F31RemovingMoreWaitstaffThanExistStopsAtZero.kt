package de.unisaarland.cs.se.selab.systemtest.selab26.incidents

import de.unisaarland.cs.se.selab.systemtest.selab26.utils.LogSkippingSystemTest

private const val INFO = "INFO"
private const val SIX_EVENINGS = 144
private const val SEATED = "[IMPORTANT] FOH Seating (R 1): Group 1 seated at table 1 by waitstaff 1."

/**
 * "All staff numbers remain non-negative, so a reduction beyond 0 results in having 0 members of
 * that staff type." The restaurant employs two waiters. Evening 3 removes five of them, which has
 * to stop at zero rather than run into a negative count, and evening 5 hires one back.
 *
 * The regular visiting every second evening reports the count: seated in evening 1, refused in
 * evening 3 with nobody to seat it, seated again in evening 5. A count left at -3 would swallow the
 * new hire and leave the group unseated in evening 5 as well.
 */
class F31RemovingMoreWaitstaffThanExistStopsAtZero : LogSkippingSystemTest() {
    override val name = "F31RemovingMoreWaitstaffThanExistStopsAtZero"
    override val description =
        "Removing 5 of 2 waiters stops at zero, so hiring one back two evenings later seats the group."
    override val food = "incidents/f31_waitstaff_clamp/food_rice.json"
    override val restaurants = "incidents/f31_waitstaff_clamp/restaurants_two_waiters.json"
    override val scenario = "incidents/f31_waitstaff_clamp/scenario_remove_five_add_one.json"
    override val logLevel = INFO
    override val maxTicks = SIX_EVENINGS

    override suspend fun run() {
        skipToAndAssert("[IMPORTANT] FOH Seating", SEATED)
        skipToAndAssert(
            "[IMPORTANT] Incident: Incident 1 ",
            "[IMPORTANT] Incident: Incident 1 of type STAFF occurred before evening 3.",
        )
        skipToAndAssert(
            "[INFO] FOH No Seating",
            "[INFO] FOH No Seating (R 1): No free waitstaff available for group 1.",
        )
        skipToAndAssert(
            "[IMPORTANT] Incident: Incident 2 ",
            "[IMPORTANT] Incident: Incident 2 of type STAFF occurred before evening 5.",
        )
        // The single re-hired waiter seats the group again, which only happens if the removal
        // stopped at zero instead of going negative.
        skipToAndAssert("[IMPORTANT] FOH Seating", SEATED)
    }
}
