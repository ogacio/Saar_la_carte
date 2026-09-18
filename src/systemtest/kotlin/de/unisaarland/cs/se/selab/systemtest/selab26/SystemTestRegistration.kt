package de.unisaarland.cs.se.selab.systemtest.selab26

import de.unisaarland.cs.se.selab.systemtest.selab26.basictests.ExampleSystemTest
import de.unisaarland.cs.se.selab.systemtest.selab26.f03.IngredientBestBeforeFractional
import de.unisaarland.cs.se.selab.systemtest.selab26.f03.IngredientBestBeforeNegative
import de.unisaarland.cs.se.selab.systemtest.selab26.f03.IngredientBestBeforeOne
import de.unisaarland.cs.se.selab.systemtest.selab26.f03.IngredientBestBeforeZero
import de.unisaarland.cs.se.selab.systemtest.selab26.f03.IngredientCaseDistinctNames
import de.unisaarland.cs.se.selab.systemtest.selab26.f03.IngredientDuplicateIdenticalObject
import de.unisaarland.cs.se.selab.systemtest.selab26.f03.IngredientDuplicateName
import de.unisaarland.cs.se.selab.systemtest.selab26.f03.IngredientExtraKey
import de.unisaarland.cs.se.selab.systemtest.selab26.f03.IngredientIntOverflow
import de.unisaarland.cs.se.selab.systemtest.selab26.f03.IngredientLargeValues
import de.unisaarland.cs.se.selab.systemtest.selab26.f03.IngredientLongName
import de.unisaarland.cs.se.selab.systemtest.selab26.f03.IngredientNameEmpty
import de.unisaarland.cs.se.selab.systemtest.selab26.f03.IngredientNameEmptyStringFixture
import de.unisaarland.cs.se.selab.systemtest.selab26.f03.IngredientNameMissing
import de.unisaarland.cs.se.selab.systemtest.selab26.f03.IngredientNameNull
import de.unisaarland.cs.se.selab.systemtest.selab26.f03.IngredientNameNullFixture
import de.unisaarland.cs.se.selab.systemtest.selab26.f03.IngredientPackagingFractional
import de.unisaarland.cs.se.selab.systemtest.selab26.f03.IngredientPackagingMissing
import de.unisaarland.cs.se.selab.systemtest.selab26.f03.IngredientPackagingNegative
import de.unisaarland.cs.se.selab.systemtest.selab26.f03.IngredientPackagingOne
import de.unisaarland.cs.se.selab.systemtest.selab26.f03.IngredientPackagingZero
import de.unisaarland.cs.se.selab.systemtest.selab26.f03.IngredientUnitEmpty
import de.unisaarland.cs.se.selab.systemtest.selab26.f03.IngredientUnitG
import de.unisaarland.cs.se.selab.systemtest.selab26.f03.IngredientUnitLowercaseX
import de.unisaarland.cs.se.selab.systemtest.selab26.f03.IngredientUnitMissing
import de.unisaarland.cs.se.selab.systemtest.selab26.f03.IngredientUnitMl
import de.unisaarland.cs.se.selab.systemtest.selab26.f03.IngredientUnitUnknown
import de.unisaarland.cs.se.selab.systemtest.selab26.f03.IngredientUnitUppercaseG
import de.unisaarland.cs.se.selab.systemtest.selab26.f03.IngredientUnitUppercaseMl
import de.unisaarland.cs.se.selab.systemtest.selab26.f03.IngredientUnitX
import de.unisaarland.cs.se.selab.systemtest.selab26.f03.IngredientUnused
import de.unisaarland.cs.se.selab.systemtest.selab26.f03.IngredientsAndRecipesBothEmpty
import de.unisaarland.cs.se.selab.systemtest.selab26.f03.IngredientsEmptyArray
import de.unisaarland.cs.se.selab.systemtest.selab26.f03.IngredientsEmptyArrayDuplicateFixture
import de.unisaarland.cs.se.selab.systemtest.selab26.f03.RootBaseline
import de.unisaarland.cs.se.selab.systemtest.selab26.f03.RootExtraKey
import de.unisaarland.cs.se.selab.systemtest.selab26.f03.RootIngredientsNotArray
import de.unisaarland.cs.se.selab.systemtest.selab26.f03.RootIngredientsNull
import de.unisaarland.cs.se.selab.systemtest.selab26.f03.RootMissingIngredients
import de.unisaarland.cs.se.selab.systemtest.selab26.f03.RootMissingRecipes
import de.unisaarland.cs.se.selab.systemtest.selab26.f03.RootNotObject
import de.unisaarland.cs.se.selab.systemtest.selab26.f03.RootRecipesNull
import de.unisaarland.cs.se.selab.systemtest.selab26.f03.RootSingleIngredientSingleRecipe
import de.unisaarland.cs.se.selab.systemtest.selab26.foh.F01FirstTickLogOrder
import de.unisaarland.cs.se.selab.systemtest.selab26.foh.F01SecondEveningStartsAtTick25
import de.unisaarland.cs.se.selab.systemtest.selab26.foh.F01StopsAfterFullEvening
import de.unisaarland.cs.se.selab.systemtest.selab26.foh.F07ServedAndDeliveredCountedSeparately
import de.unisaarland.cs.se.selab.systemtest.selab26.foh.F07StatisticsInAscendingRestaurantId
import de.unisaarland.cs.se.selab.systemtest.selab26.foh.F13DishWithoutEligibleCookIsNotOrdered
import de.unisaarland.cs.se.selab.systemtest.selab26.foh.F14EventReservedBeforeRegular
import de.unisaarland.cs.se.selab.systemtest.selab26.foh.F14RegularWithoutTableIsNotReserved
import de.unisaarland.cs.se.selab.systemtest.selab26.foh.F16BarGroupSentAwayRatesNegative
import de.unisaarland.cs.se.selab.systemtest.selab26.foh.F16CasualGroupSeatedAtMergedTable
import de.unisaarland.cs.se.selab.systemtest.selab26.foh.F16NoFreeWaiterThenSeatedNextTick
import de.unisaarland.cs.se.selab.systemtest.selab26.foh.F18FavouriteDishAndHighestRecipeId
import de.unisaarland.cs.se.selab.systemtest.selab26.foh.F18SecondCustomerFindsNoDishAndLeaves
import de.unisaarland.cs.se.selab.systemtest.selab26.foh.F19TableHeldBackUntilAllMealsAreCooked
import de.unisaarland.cs.se.selab.systemtest.selab26.foh.F20DeliveryOverSevenKilometres
import de.unisaarland.cs.se.selab.systemtest.selab26.foh.F21VisitServedEscortedAndRated
import de.unisaarland.cs.se.selab.systemtest.selab26.foh.F28HighestRatingDifferenceWins
import de.unisaarland.cs.se.selab.systemtest.selab26.foh.F28NoNewCustomersInLastThreeTicks
import de.unisaarland.cs.se.selab.systemtest.selab26.foh.F28TieGoesToLowestId
import de.unisaarland.cs.se.selab.systemtest.selab26.foh.F30GroupStillEatingAtClosingRatesNegative
import de.unisaarland.cs.se.selab.systemtest.selab26.foh.P03EventOrdersFavouriteDish
import de.unisaarland.cs.se.selab.systemtest.selab26.foh.P03EventSeatedByTwoWaiters
import de.unisaarland.cs.se.selab.systemtest.selab26.foh.P05AlwaysLikelihoodRatesNeutralPositive
import de.unisaarland.cs.se.selab.systemtest.selab26.foh.P05NeverLikelihoodLeavesNoRating
import de.unisaarland.cs.se.selab.systemtest.selab26.foh.P05SomeLikelihoodSkipsNeutralExperience
// RootEmptyFile and RootMalformedJson unregistered below, see the note there — imports removed too.

/**
 * Used for test registration
 */
object SystemTestRegistration {
    /**
     * Register your tests to run against the reference implementation!
     * This can also be used to debug our system test, or to see if we
     * understood something correctly or not (everything should work
     * the same as their reference implementation)
     */
    fun registerSystemTestsForReferenceImplementation(testSuite: SELab26TestSuite) {
        testSuite.registerTest(ExampleSystemTest())
        registerFoodParserTests(testSuite)
        registerFrontOfHouseTests(testSuite)
    }

    /**
     * Register the tests you want to run against the validation mutants here!
     * The test only check validation, so they log messages will only possibly
     * be incorrect during the parsing/validation.
     * Everything after 'Simulation start' works correctly
     */
    fun registerSystemTestsMutantValidation(testSuite: SELab26TestSuite) {
        testSuite.registerTest(ExampleSystemTest())
        registerFoodParserTests(testSuite)
    }

    /**
     * The same as above, but the log message only (possibly) become incorrect
     * from the 'Simulation start' log onwards
     */
    fun registerSystemTestsMutantSimulation(testSuite: SELab26TestSuite) {
        testSuite.registerTest(ExampleSystemTest())
        registerFrontOfHouseTests(testSuite)
    }

    /**
     * Registers the simulation scenario tests of F01, F07, F13, F14, F16, F18-F21, F28, F30, P03 and P05.
     */
    private fun registerFrontOfHouseTests(testSuite: SELab26TestSuite) {
        testSuite.registerTest(F01FirstTickLogOrder())
        testSuite.registerTest(F01StopsAfterFullEvening())
        testSuite.registerTest(F01SecondEveningStartsAtTick25())
        testSuite.registerTest(F07StatisticsInAscendingRestaurantId())
        testSuite.registerTest(F14RegularWithoutTableIsNotReserved())
        testSuite.registerTest(F14EventReservedBeforeRegular())
        testSuite.registerTest(F16CasualGroupSeatedAtMergedTable())
        testSuite.registerTest(F16NoFreeWaiterThenSeatedNextTick())
        testSuite.registerTest(F16BarGroupSentAwayRatesNegative())
        testSuite.registerTest(P05NeverLikelihoodLeavesNoRating())
        testSuite.registerTest(F18FavouriteDishAndHighestRecipeId())
        testSuite.registerTest(F13DishWithoutEligibleCookIsNotOrdered())
        testSuite.registerTest(F21VisitServedEscortedAndRated())
        testSuite.registerTest(P03EventOrdersFavouriteDish())
        testSuite.registerTest(F28TieGoesToLowestId())
        testSuite.registerTest(F28HighestRatingDifferenceWins())
        testSuite.registerTest(F28NoNewCustomersInLastThreeTicks())
        testSuite.registerTest(F20DeliveryOverSevenKilometres())
        testSuite.registerTest(F18SecondCustomerFindsNoDishAndLeaves())
        testSuite.registerTest(F19TableHeldBackUntilAllMealsAreCooked())
        testSuite.registerTest(P05SomeLikelihoodSkipsNeutralExperience())
        testSuite.registerTest(P05AlwaysLikelihoodRatesNeutralPositive())
        testSuite.registerTest(F30GroupStillEatingAtClosingRatesNegative())
        testSuite.registerTest(F07ServedAndDeliveredCountedSeparately())
        testSuite.registerTest(P03EventSeatedByTwoWaiters())
    }

    /**
     * Registers the F03 food file validation tests covering the root and ingredient fixtures.
     */
    private fun registerFoodParserTests(testSuite: SELab26TestSuite) {
        registerRootTests(testSuite)
        registerIngredientValidTests(testSuite)
        registerIngredientFieldTests(testSuite)
        registerIngredientRangeTests(testSuite)
    }

    /**
     * Registers the food file tests that mutate the root object itself.
     */
    private fun registerRootTests(testSuite: SELab26TestSuite) {
        testSuite.registerTest(RootBaseline())
        testSuite.registerTest(RootSingleIngredientSingleRecipe())
        testSuite.registerTest(RootMissingIngredients())
        testSuite.registerTest(RootMissingRecipes())
        testSuite.registerTest(RootExtraKey())
        testSuite.registerTest(RootIngredientsNull())
        testSuite.registerTest(RootRecipesNull())
        testSuite.registerTest(RootIngredientsNotArray())
        testSuite.registerTest(RootNotObject())
        // RootEmptyFile and RootMalformedJson unregistered: they assert `is invalid.` for input
        // that is not JSON at all, a case the spec never states a contract for. Both fail against
        // the grading server (pass locally), blocking all 5 validation mutants. See
        // misc/implementation/tests/qa-findings-2026-09-18.md sections 3-4. Uncomment once
        // replaced with fixtures that break a rule the spec actually defines.
        // testSuite.registerTest(RootEmptyFile())
        // testSuite.registerTest(RootMalformedJson())
    }

    /**
     * Registers the ingredient fixtures that must parse successfully.
     */
    private fun registerIngredientValidTests(testSuite: SELab26TestSuite) {
        testSuite.registerTest(IngredientUnitG())
        testSuite.registerTest(IngredientUnitMl())
        testSuite.registerTest(IngredientUnitX())
        testSuite.registerTest(IngredientPackagingOne())
        testSuite.registerTest(IngredientBestBeforeOne())
        testSuite.registerTest(IngredientLargeValues())
        testSuite.registerTest(IngredientLongName())
        testSuite.registerTest(IngredientCaseDistinctNames())
        testSuite.registerTest(IngredientUnused())
    }

    /**
     * Registers the ingredient fixtures that mutate a field's presence, nullability or spelling.
     */
    private fun registerIngredientFieldTests(testSuite: SELab26TestSuite) {
        testSuite.registerTest(IngredientNameMissing())
        testSuite.registerTest(IngredientNameNull())
        testSuite.registerTest(IngredientNameEmpty())
        testSuite.registerTest(IngredientUnitMissing())
        testSuite.registerTest(IngredientUnitEmpty())
        testSuite.registerTest(IngredientUnitUnknown())
        testSuite.registerTest(IngredientUnitUppercaseG())
        testSuite.registerTest(IngredientUnitUppercaseMl())
        testSuite.registerTest(IngredientUnitLowercaseX())
        testSuite.registerTest(IngredientExtraKey())
    }

    /**
     * Registers the ingredient fixtures that mutate a numeric range or a uniqueness rule.
     */
    private fun registerIngredientRangeTests(testSuite: SELab26TestSuite) {
        testSuite.registerTest(IngredientPackagingZero())
        testSuite.registerTest(IngredientPackagingNegative())
        testSuite.registerTest(IngredientPackagingFractional())
        testSuite.registerTest(IngredientPackagingMissing())
        testSuite.registerTest(IngredientIntOverflow())
        testSuite.registerTest(IngredientBestBeforeZero())
        testSuite.registerTest(IngredientBestBeforeNegative())
        testSuite.registerTest(IngredientBestBeforeFractional())
        testSuite.registerTest(IngredientDuplicateName())
        testSuite.registerTest(IngredientDuplicateIdenticalObject())
        testSuite.registerTest(IngredientsEmptyArray())
        testSuite.registerTest(IngredientsEmptyArrayDuplicateFixture())
        testSuite.registerTest(IngredientsAndRecipesBothEmpty())
        testSuite.registerTest(IngredientNameEmptyStringFixture())
        testSuite.registerTest(IngredientNameNullFixture())
    }
}
