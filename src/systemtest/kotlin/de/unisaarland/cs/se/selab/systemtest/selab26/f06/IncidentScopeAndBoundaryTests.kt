package de.unisaarland.cs.se.selab.systemtest.selab26.f06

import de.unisaarland.cs.se.selab.systemtest.selab26.utils.LogSkippingSystemTest

private const val FOOD = "f03/root/valid/ok_baseline.json"
private const val RESTAURANTS = "f03/companions/restaurants_valid.json"
private const val DEBUG_LEVEL = "DEBUG"
private const val INVALID_PREFIX = "[IMPORTANT] Initialization Info"

/** F06 additions that isolate incident schema, cross-file and boundary validation rules. */
abstract class AddedIncidentTest(
    private val fixture: String,
    private val valid: Boolean,
) : LogSkippingSystemTest() {
    override val food = FOOD
    override val restaurants = RESTAURANTS
    override val scenario = "f06/${if (valid) "valid" else "invalid"}/$fixture.json"
    override val logLevel = DEBUG_LEVEL
    override val maxTicks = 0

    override suspend fun run() {
        if (valid) {
            assertNextLine(parsedMessage("ok_baseline.json"))
            assertNextLine(parsedMessage("restaurants_valid.json"))
            assertNextLine(parsedMessage("$fixture.json"))
        } else {
            skipToAndAssert(
                INVALID_PREFIX,
                "[IMPORTANT] Initialization Info: $fixture.json is invalid.",
            )
        }
    }

    private fun parsedMessage(file: String): String =
        "[INFO] Initialization Info: $file successfully parsed and validated."
}

/** Consecutive unavailability windows do not overlap. */
class F06AdjacentUnavailabilityPeriods : AddedIncidentTest("ok_incident_adjacent_unavailability", true) {
    override val name = "F06AdjacentUnavailabilityPeriods"
    override val description = "Unavailability during evenings 2-3 may be followed directly by evenings 4-5."
}

/** A staff incident must change the staff count by a non-zero number. */
class F06StaffNumberZero : AddedIncidentTest("bad_incident_staff_number_zero", false) {
    override val name = "F06StaffNumberZero"
    override val description = "A staff incident with number zero is invalid."
}

/** A negative staff number removes staff and is valid. */
class F06StaffNegativeOne : AddedIncidentTest("ok_incident_staff_negative_one", true) {
    override val name = "F06StaffNegativeOne"
    override val description = "A staff incident with number minus one is valid."
}

/** A COOK staff incident must identify the affected cook type. */
class F06CookWithoutCookType : AddedIncidentTest("bad_incident_cook_without_cook_type", false) {
    override val name = "F06CookWithoutCookType"
    override val description = "A cook staff incident without a cook type is invalid."
}

/** A WAITSTAFF incident must not contain a cook type. */
class F06WaitstaffWithCookType : AddedIncidentTest("bad_incident_waitstaff_with_cook_type", false) {
    override val name = "F06WaitstaffWithCookType"
    override val description = "A waitstaff incident with a cook type is invalid."
}

/** Incident evenings are one-based. */
class F06EveningZero : AddedIncidentTest("bad_incident_evening_zero", false) {
    override val name = "F06EveningZero"
    override val description = "An incident on evening zero is invalid."
}

/** Evening one is the smallest valid incident evening. */
class F06EveningOne : AddedIncidentTest("ok_incident_evening_one", true) {
    override val name = "F06EveningOne"
    override val description = "An incident on evening one is valid."
}

/** A recipe change can only refer to a known ingredient. */
class F06RecipeUnknownIngredient : AddedIncidentTest("bad_incident_recipe_unknown_ingredient", false) {
    override val name = "F06RecipeUnknownIngredient"
    override val description = "A recipe incident for an unknown ingredient is invalid."
}

/** A packaging change can only refer to a known ingredient. */
class F06PackagingUnknownIngredient : AddedIncidentTest("bad_incident_packaging_unknown_ingredient", false) {
    override val name = "F06PackagingUnknownIngredient"
    override val description = "A packaging incident for an unknown ingredient is invalid."
}
