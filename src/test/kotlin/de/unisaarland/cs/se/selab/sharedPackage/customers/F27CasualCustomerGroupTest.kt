package de.unisaarland.cs.se.selab.sharedPackage.customers

import de.unisaarland.cs.se.selab.sharedPackage.RestaurantType
import de.unisaarland.cs.se.selab.sharedPackage.TableType
import de.unisaarland.cs.se.selab.simulation.GlobalClock
import de.unisaarland.cs.se.selab.simulation.ratings.RatingLikelihood
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

/** Tests CasualCustomerGroup's delivery give-up timing: hasGivenUp/orderPlaced/orderResolved.
 * Covers F27, no browsing/ranking logic. */
class F27CasualCustomerGroupTest {

    private fun group(visitingTick: Int) = CasualCustomerGroup(
        1, 2, TableType.COMMON, visitingTick, emptyList(), emptyList(),
        setOf(RestaurantType.EUROPEAN), listOf(1), DeliveryPreference(10, RatingLikelihood.NEVER),
    )

    @BeforeEach
    fun resetClock() {
        // GlobalClock has no test reset, so every test starts its own evening to be independent
        // of what earlier tests (in this or other classes) left tickInEvening at.
        GlobalClock.advanceEvening()
    }

    @Test
    fun casualGroupHasNotGivenUpWithNoPendingOrder() {
        val g = group(visitingTick = 1)
        assertTrue(!g.hasGivenUp())
    }

    @Test
    fun casualGroupHasNotGivenUpBeforeDeadline() {
        val g = group(visitingTick = 1)
        g.orderPlaced()
        repeat(3) { GlobalClock.advanceTick() } // tickInEvening = 3, deadline is visitingTick + 3 = 4
        assertTrue(!g.hasGivenUp())
    }

    @Test
    fun casualGroupHasNotGivenUpAtExactDeadlineTick() {
        val g = group(visitingTick = 1)
        g.orderPlaced()
        repeat(4) { GlobalClock.advanceTick() } // tickInEvening = 4, exactly the deadline, not exceeded
        assertTrue(!g.hasGivenUp())
    }

    @Test
    fun casualGroupHasGivenUpAfterDeadline() {
        val g = group(visitingTick = 1)
        g.orderPlaced()
        repeat(5) { GlobalClock.advanceTick() } // tickInEvening = 5, exceeds deadline of 4
        assertTrue(g.hasGivenUp())
    }

    @Test
    fun casualGroupOrderResolvedClearsPendingSoNoLongerGivenUp() {
        val g = group(visitingTick = 1)
        g.orderPlaced()
        repeat(5) { GlobalClock.advanceTick() }
        assertTrue(g.hasGivenUp())
        g.orderResolved()
        assertTrue(!g.hasGivenUp())
    }
}
