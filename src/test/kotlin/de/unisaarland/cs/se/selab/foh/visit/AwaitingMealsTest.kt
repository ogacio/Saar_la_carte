package de.unisaarland.cs.se.selab.foh.visit

import de.unisaarland.cs.se.selab.sharedPackage.customers.Customer
import de.unisaarland.cs.se.selab.sharedPackage.customers.CustomerGroup
import de.unisaarland.cs.se.selab.sharedPackage.customers.CustomerStatus
import de.unisaarland.cs.se.selab.sharedPackage.customers.GroupType
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

/**
 * F27: tests for the waiting/patience state machine
 */
class AwaitingMealsTest {

    private lateinit var group: CustomerGroup
    private lateinit var customerA: Customer
    private lateinit var customerB: Customer
    private lateinit var visit: Visit

    @BeforeEach
    fun setUp() {
        customerA = mock()
        customerB = mock()
        whenever(customerA.status()).thenReturn(CustomerStatus.ORDERED)
        whenever(customerB.status()).thenReturn(CustomerStatus.ORDERED)

        group = mock()
        whenever(group.members()).thenReturn(mutableListOf(customerA, customerB))
        whenever(group.groupType()).thenReturn(GroupType.CASUAL)

        visit = Visit(group)
        visit.state = AwaitingMealState()
        visit.orderedTick = 1
    }

    @Test
    fun nobodyServedYetPatienceNotExceededTableKeepsWaiting() {
        visit.advance(tick = 5) // waited = 4, PATIENCE_TICKS = 5
        assertTrue(visit.state is AwaitingMealState)
        verify(customerA, never()).leave()
    }

    /*@Test
    fun nobodyServedAfter5TicksWholeGroupLeavesAsFailedAttempt() {
        visit.advance(tick = 6) // waited = 5 >= PATIENCE_TICKS
        assertTrue(visit.state is GoneState)
        assertTrue(visit.failedAttempt)
        verify(customerA, times(1)).leave()
        verify(customerB, times(1)).leave()
    }
*/
    @Test
    fun OnceSomeoneIsSServedUnservedCustomers2ExtraTickes() {
        whenever(customerA.status()).thenReturn(CustomerStatus.SERVED)
        whenever(customerA.servedTick()).thenReturn(3)
        // customerB stays ORDERED, never served

        visit.advance(tick = 6) // waited since order = 5, still < 4 + 2
        verify(customerB, never()).leave()
        assertTrue(visit.state is AwaitingMealState)

        visit.advance(tick = 7) // waited = 6 >= 4 + 2
        verify(customerB, times(1)).leave()
        verify(customerA, never()).leave() // only the unserved one is dropped
    }

    @Test
    fun allServedAndAllDoneReadyToLeave() {
        whenever(customerA.status()).thenReturn(CustomerStatus.DONE_EATING)
        whenever(customerB.status()).thenReturn(CustomerStatus.DONE_EATING)
        whenever(customerA.servedTick()).thenReturn(2)
        whenever(customerB.servedTick()).thenReturn(2)

        visit.advance(tick = 3)
        assertTrue(visit.state is ReadyToLeaveState)
    }

    @Test
    fun allServedSomebodyStillEatingEatingUp() {
        whenever(customerA.status()).thenReturn(CustomerStatus.SERVED)
        whenever(customerB.status()).thenReturn(CustomerStatus.DONE_EATING)
        whenever(customerA.servedTick()).thenReturn(2)
        whenever(customerB.servedTick()).thenReturn(2)

        visit.advance(tick = 3)
        assertTrue(visit.state is EatingUpState)
    }
}
