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
import de.unisaarland.cs.se.selab.systemtest.selab26.f03.RootEmptyFile
import de.unisaarland.cs.se.selab.systemtest.selab26.f03.RootExtraKey
import de.unisaarland.cs.se.selab.systemtest.selab26.f03.RootIngredientsNotArray
import de.unisaarland.cs.se.selab.systemtest.selab26.f03.RootIngredientsNull
import de.unisaarland.cs.se.selab.systemtest.selab26.f03.RootMalformedJson
import de.unisaarland.cs.se.selab.systemtest.selab26.f03.RootMissingIngredients
import de.unisaarland.cs.se.selab.systemtest.selab26.f03.RootMissingRecipes
import de.unisaarland.cs.se.selab.systemtest.selab26.f03.RootNotObject
import de.unisaarland.cs.se.selab.systemtest.selab26.f03.RootRecipesNull
import de.unisaarland.cs.se.selab.systemtest.selab26.f03.RootSingleIngredientSingleRecipe

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
        testSuite.registerTest(RootEmptyFile())
        testSuite.registerTest(RootMalformedJson())
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
