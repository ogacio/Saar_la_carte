package de.unisaarland.cs.se.selab.sharedPackage.customers

import de.unisaarland.cs.se.selab.simulation.GlobalClock
import de.unisaarland.cs.se.selab.testsupport.Fixtures.casual
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/** F27: deadline eligibility and an actual give-up are separate delivery states. */
class F27DeliveryWaitingBoundaryTest {
    @Test
    fun onlyAPendingOrderExpiresAfterTheThirdAdditionalTick() {
        GlobalClock.advanceEvening()
        val group = casual(1, 2, deliveryDistance = 5, visitingTick = 5)
        group.orderPlaced()
        repeat(8) {
            GlobalClock.advanceTick()
            assertFalse(group.deliveryGiveUpDue(), "Still within the delivery waiting window")
        }
        GlobalClock.advanceTick()
        assertTrue(group.deliveryGiveUpDue())
        assertFalse(group.deliveryWasGivenUp())
        group.orderResolved()
        assertFalse(group.deliveryGiveUpDue())
        assertFalse(group.deliveryWasGivenUp())
    }

    @Test
    fun actualGiveUpSurvivesResolutionButANewOrderClearsIt() {
        val group = casual(1, 2, deliveryDistance = 5, visitingTick = 5)
        group.orderPlaced()
        group.markDeliveryGivenUp()
        group.orderResolved()

        assertTrue(group.deliveryWasGivenUp())
        assertFalse(group.deliveryGiveUpDue())
        group.orderPlaced()
        assertFalse(group.deliveryWasGivenUp())
    }
}
