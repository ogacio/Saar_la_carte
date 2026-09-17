package de.unisaarland.cs.se.selab.foh.visit

import de.unisaarland.cs.se.selab.foh.Table
import de.unisaarland.cs.se.selab.foh.Waiter
import de.unisaarland.cs.se.selab.sharedPackage.TableType
import de.unisaarland.cs.se.selab.sharedPackage.customers.CustomerGroup
import de.unisaarland.cs.se.selab.sharedPackage.customers.CustomerStatus
import de.unisaarland.cs.se.selab.testsupport.Fixtures.event
import de.unisaarland.cs.se.selab.testsupport.Fixtures.regular
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertTrue

/**
 * Escorting, from the specification: "Once all customers sitting on a table have finished eating, the
 * waitstaff can escort them outside." Escorting may be partial, because a waiter has only 10 actions
 * per tick; the visit ends when the last customer is outside.
 */
class ReadyToLeaveStateTest {

    /** A group that has finished eating and waits to be escorted out. */
    private fun readyVisit(group: CustomerGroup): Visit {
        val visit = Visit(group)
        visit.seated(Table(1, group.groupSize(), TableType.COMMON), listOf(Waiter()), 1)
        visit.state = ReadyToLeaveState()
        return visit
    }

    @Test
    fun escortingEveryoneEndsTheVisit() {
        val visit = readyVisit(regular(1, 3))

        val left = visit.escort(3, 5)

        assertEquals(3, left)
        assertIs<GoneState>(visit.state)
        assertTrue(visit.isFinished())
        assertTrue(visit.group.members().all { it.status() == CustomerStatus.LEFT })
    }

    @Test
    fun partialEscortingKeepsTheRestAtTheTable() {
        val visit = readyVisit(regular(1, 5))

        val left = visit.escort(2, 5)

        assertEquals(2, left)
        assertIs<ReadyToLeaveState>(visit.state)
        assertEquals(3, visit.customersInside().size)
    }

    @Test
    fun escortingMoreCustomersThanAreLeftIsHarmless() {
        val visit = readyVisit(event(2, 2))

        val left = visit.escort(10, 5)

        assertEquals(2, left)
        assertIs<GoneState>(visit.state)
    }

    @Test
    fun aFinishedVisitReactsToNothing() {
        val visit = readyVisit(regular(1, 2))
        visit.escort(2, 5)

        val left = visit.escort(2, 6)
        visit.advance(7)

        assertEquals(0, left)
        assertIs<GoneState>(visit.state)
    }
}
