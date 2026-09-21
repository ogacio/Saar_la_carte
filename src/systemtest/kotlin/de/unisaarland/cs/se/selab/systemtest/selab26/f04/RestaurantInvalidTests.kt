package de.unisaarland.cs.se.selab.systemtest.selab26.f04

import de.unisaarland.cs.se.selab.systemtest.selab26.utils.ExampleSystemTestExtension

private const val FOOD = "foh/food_rice.json"
private const val FOOD_PARSED = "[INFO] Initialization Info: food_rice.json successfully parsed and validated."
private const val DEBUG_LEVEL = "DEBUG"
private const val VALID_SCENARIO = "f04/companions/scenario_valid.json"

/**
 * F04: a valid food file with one broken restaurants file. Every subclass names the fixture and
 * asserts that the restaurants file is the one the simulation rejects.
 */
abstract class RestaurantInvalidTest(private val fixture: String) : ExampleSystemTestExtension() {
    override val food = FOOD
    override val restaurants = "f04/invalid/$fixture.json"
    override val scenario = VALID_SCENARIO
    override val logLevel = DEBUG_LEVEL
    override val maxTicks = 0

    override suspend fun run() {
        assertNextLine(FOOD_PARSED)
        assertNextLine("[IMPORTANT] Initialization Info: $fixture.json is invalid.")
    }
}

/** A kitchen with two EXEC cooks violates the maximum of one head cook. */
class ResExecTwo : RestaurantInvalidTest("bad_res_exec_two") {
    override val name = "F04ResExecTwo"
    override val description = "A kitchen with two EXEC cooks violates the maximum of one head cook."
}

/** A restaurant without waitstaff violates the exclusive minimum of zero. */
class ResWaitstaffZero : RestaurantInvalidTest("bad_res_waitstaff_zero") {
    override val name = "F04ResWaitstaffZero"
    override val description = "A restaurant without waitstaff violates the exclusive minimum of zero."
}

/** A restaurant needs at least one table. */
class ResTablesEmpty : RestaurantInvalidTest("bad_res_tables_empty") {
    override val name = "F04ResTablesEmpty"
    override val description = "A restaurant needs at least one table."
}

/** ROUND is not a member of the table type enum. */
class ResTableTypeUnknown : RestaurantInvalidTest("bad_res_table_type_unknown") {
    override val name = "F04ResTableTypeUnknown"
    override val description = "ROUND is not a member of the table type enum."
}

/** The restaurant type enum is case sensitive, so asian is rejected. */
class ResTypeLowercase : RestaurantInvalidTest("bad_res_type_lowercase") {
    override val name = "F04ResTypeLowercase"
    override val description = "The restaurant type enum is case sensitive, so asian is rejected."
}

/** openingTickEnd must be greater than openingTickStart. */
class ResEndBeforeStart : RestaurantInvalidTest("bad_res_end_before_start") {
    override val name = "F04ResEndBeforeStart"
    override val description = "openingTickEnd must be greater than openingTickStart."
}

/** A restaurant recipe id that no recipe in the food file has. */
class ResUnknownRecipe : RestaurantInvalidTest("bad_res_unknown_recipe") {
    override val name = "F04ResUnknownRecipe"
    override val description = "A restaurant recipe id that no recipe in the food file has."
}

/** An unknown key on a restaurant object is rejected. */
class ResExtraKey : RestaurantInvalidTest("bad_res_extra_key") {
    override val name = "F04ResExtraKey"
    override val description = "An unknown key on a restaurant object is rejected."
}

/** The required restaurant name key is absent. */
class ResMissingName : RestaurantInvalidTest("bad_res_missing_name") {
    override val name = "F04ResMissingName"
    override val description = "The required restaurant name key is absent."
}

/** At least one restaurant must exist. */
class ResEmptyArray : RestaurantInvalidTest("bad_res_empty_array") {
    override val name = "F04ResEmptyArray"
    override val description = "At least one restaurant must exist."
}

/** Two restaurants sharing an id violate the unique identifier rule. */
class ResDuplicateId : RestaurantInvalidTest("bad_res_duplicate_id") {
    override val name = "F04ResDuplicateId"
    override val description = "Two restaurants sharing an id violate the unique identifier rule."
}

/** Two restaurants sharing a name violate the unique name rule. */
class ResDuplicateName : RestaurantInvalidTest("bad_res_duplicate_name") {
    override val name = "F04ResDuplicateName"
    override val description = "Two restaurants sharing a name violate the unique name rule."
}

/** Two tables of one restaurant sharing an id are rejected. */
class ResDuplicateTableId : RestaurantInvalidTest("bad_res_duplicate_table_id") {
    override val name = "F04ResDuplicateTableId"
    override val description = "Two tables of one restaurant sharing an id are rejected."
}

/** The kitchen staff object must list every cook type, PASTRY included.
 * Currently fails against our own jar: RestaurantParser does not apply this numeric bound.
 * */
class ResMissingPastry : RestaurantInvalidTest("bad_res_missing_pastry") {
    override val name = "F04ResMissingPastry"
    override val description = "The kitchen staff object must list every cook type, PASTRY included."
}

/** negativeRatings of -1 violates the minimum of zero.
 * Currently fails against our own jar: RestaurantParser does not apply this numeric bound.
 * */
class ResNegativeRatings : RestaurantInvalidTest("bad_res_negative_ratings") {
    override val name = "F04ResNegativeRatings"
    override val description = "negativeRatings of -1 violates the minimum of zero."
}

/** openingTickEnd of 25 is above the range of one to twenty-four.
 * Currently fails against our own jar: RestaurantParser does not apply this numeric bound.
 * */
class ResOpeningEnd25 : RestaurantInvalidTest("bad_res_opening_end_25") {
    override val name = "F04ResOpeningEnd25"
    override val description = "openingTickEnd of 25 is above the range of one to twenty-four."
}

/** openingTickStart of 0 is below the range of one to twenty-four.
 * Currently fails against our own jar: RestaurantParser does not apply this numeric bound.
 * */
class ResOpeningStartZero : RestaurantInvalidTest("bad_res_opening_start_zero") {
    override val name = "F04ResOpeningStartZero"
    override val description = "openingTickStart of 0 is below the range of one to twenty-four."
}

/** A table size of 1 is below the range of two to thirty.
 * Currently fails against our own jar: RestaurantParser does not apply this numeric bound.
 * */
class ResTableSizeOne : RestaurantInvalidTest("bad_res_table_size_one") {
    override val name = "F04ResTableSizeOne"
    override val description = "A table size of 1 is below the range of two to thirty."
}

/** A table size of 31 is above the range of two to thirty.
 * Currently fails against our own jar: RestaurantParser does not apply this numeric bound.
 * */
class ResTableSize31 : RestaurantInvalidTest("bad_res_table_size_31") {
    override val name = "F04ResTableSize31"
    override val description = "A table size of 31 is above the range of two to thirty."
}
