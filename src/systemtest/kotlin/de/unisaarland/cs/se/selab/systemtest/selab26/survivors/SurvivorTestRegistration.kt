package de.unisaarland.cs.se.selab.systemtest.selab26.survivors

import de.unisaarland.cs.se.selab.systemtest.selab26.SELab26TestSuite

/**
 * Registers the tests for the six mutants no earlier round found. They start out as reference-only
 * probes: a test registered for the mutants that fails against the reference skips the whole
 * mutant run, so each one moves to the mutant lists once the reference report shows it green.
 */
object SurvivorTestRegistration {

    fun register(testSuite: SELab26TestSuite) {
        registerShortStaffed(testSuite)
        registerBacklash(testSuite)
        registerDinnerForOne(testSuite)
        registerKingOfTheHill(testSuite)
        registerFreeForAll(testSuite)
        registerArbeitszeitbetrug(testSuite)
    }

    private fun registerShortStaffed(testSuite: SELab26TestSuite) {
        testSuite.registerTest(P03EventOfTwentyIsSeatedByBothWaitersAndBlocksTheNextGroup())
        testSuite.registerTest(F16RegularRetriesAtItsReservedTable())
        testSuite.registerTest(F22RegularStopsAfterTwoEveningsWithoutAFreeWaiter())
        testSuite.registerTest(F16NewArrivalWithLowerIdGoesBeforeTheRetry())
        testSuite.registerTest(F17SixAndFourFillOneWaiterExactly())
        testSuite.registerTest(F16RetryJustBeforeTheLastThreeTicksIsSeated())
        testSuite.registerTest(F16RetryInTheLastThreeTicksOfAnEarlyClosingIsRefused())
        testSuite.registerTest(P05LikelihoodsAfterTwoFailedSeatingAttempts())
        testSuite.registerTest(F25NoReconsiderationAfterTwoFailedSeatingAttempts())
        testSuite.registerTest(F16TheSeatingRetryLimitIsTwoAttemptsNotThree())
    }

    private fun registerBacklash(testSuite: SELab26TestSuite) {
        testSuite.registerTest(P05FailedRegularReservationRatesInTheOpeningTick())
        testSuite.registerTest(P05FailedEventReservationRatesInTheOpeningTick())
        testSuite.registerTest(F28NegativeRatingsSendTheNextGroupElsewhere())
        testSuite.registerTest(P05GroupWithAnUnservedMemberRatesNegativeOnce())
        testSuite.registerTest(P05GivenUpDeliveryRatesOnceWithSome())
        testSuite.registerTest(P05LateDeliveryRatesNegative())
        testSuite.registerTest(P05OnTimeDeliveryIsNeutralForSome())
        testSuite.registerTest(P05OnTimeDeliveryIsPositiveForAlways())
        testSuite.registerTest(P05RegularRatesEveryVisitOnTopOfTheSeeds())
    }

    private fun registerDinnerForOne(testSuite: SELab26TestSuite) {
        testSuite.registerTest(F21MembersServedApartFinishEatingApart())
        testSuite.registerTest(F19ServingLoadCountsMealsNotTables())
        testSuite.registerTest(F27TwoUnservedMembersLeaveTogether())
        testSuite.registerTest(P03EventMembersOrderOneByOne())
        testSuite.registerTest(F21EscortingLoadCountsCustomers())
        testSuite.registerTest(F22NonConsecutiveFailuresDoNotStopARegular())
        testSuite.registerTest(F22RegularWithStartOneAndPeriodThreeVisitsEveryThirdEvening())
    }

    private fun registerKingOfTheHill(testSuite: SELab26TestSuite) {
        testSuite.registerTest(F18CustomerWithoutFavouritesOrdersFirst())
        testSuite.registerTest(F18JsonOrderBreaksTheOrderingTie())
        testSuite.registerTest(F26SecondFavouriteWhenTheFirstIsUnavailable())
        testSuite.registerTest(F18RegularOrdersBeforeACasualWithALowerId())
        testSuite.registerTest(F26PreferredIngredientTieGoesToTheHighestId())
        testSuite.registerTest(F26PreferredIngredientsCountByKindNotAmount())
        testSuite.registerTest(F26HighestRecipeIdEvenWhenListedOutOfOrder())
    }

    private fun registerFreeForAll(testSuite: SELab26TestSuite) {
        testSuite.registerTest(F28BrowsingCountsSeatsPerTableType())
        testSuite.registerTest(F28AReservedTableIsNotAFreeSeat())
        testSuite.registerTest(F28BarSeatsShrinkAsBarGroupsDecide())
        testSuite.registerTest(F28AnOccupiedTableIsNotOfferedUntilItIsFree())
        testSuite.registerTest(P04EventSeatsCountPriorEventReservations())
        testSuite.registerTest(F28SeatsShrinkByTheGroupSize())
    }

    private fun registerArbeitszeitbetrug(testSuite: SELab26TestSuite) {
        testSuite.registerTest(F29DriverIsBusyUntilTheTickAfterAFailedTripReturns())
        testSuite.registerTest(F20HandOverUsesTheWaitersServingLoad())
        testSuite.registerTest(F20TablesAreServedBeforeTheDriverIsLoaded())
        testSuite.registerTest(F30NoHandOverAfterClosing())
        testSuite.registerTest(F29DrivingDistanceRestartsForEachOrder())
    }
}
