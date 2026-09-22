package de.unisaarland.cs.se.selab.systemtest.selab26.gvalidation

import de.unisaarland.cs.se.selab.systemtest.selab26.utils.LogSkippingSystemTest

private const val DEBUG_LEVEL = "DEBUG"

/**
 * DISABLED (2026-09-22, Stefan): asserts an EVENT favourite must be the basic dish of its
 * restaurant type, which is directly contradicted by staff (forum topic 352, `Xiyang`,
 * 2026-09-22): "there is no rule that the dish must be a basic dish of that same type." Fixture
 * JSON (`gvalidation/s30_event_africa_nonbasic_dish/`) was removed with the original delete and
 * is not recreated here — kept commented per "commented code > deleted code," not registered.
 * `GEventMissingTypeFavourite` in this same package covers the rule that IS still correct
 * (every considered type needs *a* favourite, just not necessarily that type's basic dish).
 */
class GEventAfricaNonbasicDish : LogSkippingSystemTest() {
    override val name = "GEventAfricaNonbasicDish"
    override val description = "An EVENT group naming an AFRICAN favourite that is not the AFRICAN " +
        "basic dish is rejected."
    override val food = "gvalidation/s30_event_africa_nonbasic_dish/food.json"
    override val restaurants = "gvalidation/s30_event_africa_nonbasic_dish/restaurants.json"
    override val scenario = "gvalidation/s30_event_africa_nonbasic_dish/scenario.json"
    override val logLevel = DEBUG_LEVEL
    override val maxTicks = 0

    override suspend fun run() {
        skipToAndAssert(
            "[IMPORTANT] Initialization Info:",
            "[IMPORTANT] Initialization Info: scenario.json is invalid.",
        )
    }
}
