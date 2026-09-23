package de.unisaarland.cs.se.selab.systemtest.selab26.foh

import de.unisaarland.cs.se.selab.systemtest.selab26.utils.LogSkippingSystemTest

/**
 * F22, Customer - Regulars: visitingStart 2, visitingPeriod 3 means the regular pair visits on
 * evenings 2 and 5 of five, so 4 meals are cooked and served and 2 ratings given. Filed here rather
 * than under a customer-specific folder because the assertion is entirely statistics-based — move
 * it if your project keeps a dedicated "regulars"/"customers" folder instead. Run against the
 * reference first.
 */
class RegularStartTwoPeriodThreeVisitsOnlyOnScheduledEvenings : LogSkippingSystemTest() {
    override val name = "F22_RegularStartTwoPeriodThreeVisitsOnlyOnScheduledEvenings"
    override val description = "A regular with start 2 and period 3 visits exactly on evenings 2 and 5."
    override val food = "gvalidation/biborka_mutants/food_asian_only.json"
    override val restaurants = "gvalidation/biborka_mutants/restaurants_common_only.json"
    override val scenario = "gvalidation/biborka_mutants/scenario_regular_start_two_period_three.json"
    override val logLevel = "IMPORTANT"
    override val maxTicks = 120

    override suspend fun run() {
        assertStatistics(1, cooked = 4, served = 4, delivered = 0, ratings = 2)
    }
}
