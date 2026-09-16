package de.unisaarland.cs.se.selab.simulation

import kotlin.test.Test
import kotlin.test.assertEquals

/** F29: travel time. The delivery takes distance/5 ticks, rounded up to whole ticks. */
class DeliveryServiceTest {

    @Test
    fun travelTicksRoundUpToWholeTicks() {
        assertEquals(1, DeliveryService.calculateTravelTicks(1))
        assertEquals(1, DeliveryService.calculateTravelTicks(5))
        assertEquals(2, DeliveryService.calculateTravelTicks(6))
        assertEquals(2, DeliveryService.calculateTravelTicks(10))
        assertEquals(3, DeliveryService.calculateTravelTicks(11))
    }
}
