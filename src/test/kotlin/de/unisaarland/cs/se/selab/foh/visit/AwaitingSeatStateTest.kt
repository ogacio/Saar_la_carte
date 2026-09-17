package de.unisaarland.cs.se.selab.foh.visit

import de.unisaarland.cs.se.selab.foh.Table
import de.unisaarland.cs.se.selab.foh.Waiter
import de.unisaarland.cs.se.selab.sharedPackage.TableType
import de.unisaarland.cs.se.selab.sharedPackage.customers.CustomerStatus
import de.unisaarland.cs.se.selab.testsupport.Fixtures.casual
import de.unisaarland.cs.se.selab.testsupport.Fixtures.regular
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertIs
import kotlin.test.assertTrue

/**
 * The first phase of a visit, as the specification describes it: "In case there is no free waiter,
 * the customers try again the next tick and then leave, they do not reconsider", and a group that
 * finds no table "is sent away". Both count as a failed attempt for a REGULAR group.
 */
class AwaitingSeatStateTest {

    private fun table(size: Int) = Table(1, size, TableType.COMMON)

    @Test
    fun aNewVisitWaitsForASeat() {
        val visit = Visit(regular(1, 2))

        assertIs<AwaitingSeatState>(visit.state)
        assertFalse(visit.wasSeated)
        assertFalse(visit.isFinished())
    }

    @Test
    fun seatingStoresTableAndWaitersAndMovesOn() {
        val visit = Visit(regular(1, 2))
        val waiter = Waiter()

        visit.seated(table(2), listOf(waiter), 1)

        assertIs<SeatedState>(visit.state)
        assertEquals(1, visit.table?.id)
        assertEquals(listOf(waiter), visit.waiters)
        assertTrue(visit.wasSeated)
    }

    @Test
    fun withoutAFreeWaiterTheGroupTriesAgainInTheNextTick() {
        val visit = Visit(regular(1, 2))

        visit.noWaiterFree(1)

        assertIs<AwaitingSeatState>(visit.state)
        assertFalse(visit.isFinished())
        assertEquals(2, visit.customersInside().size)
    }

    @Test
    fun theSecondTickWithoutAFreeWaiterEndsTheVisit() {
        val visit = Visit(regular(1, 2))

        visit.noWaiterFree(1)
        visit.noWaiterFree(2)

        assertIs<GoneState>(visit.state)
        assertTrue(visit.isFinished())
        assertTrue(visit.failedAttempt)
        assertTrue(visit.customersInside().isEmpty())
        assertTrue(visit.group.members().all { it.status() == CustomerStatus.LEFT })
    }

    @Test
    fun withoutATableTheGroupIsSentAwayAtOnce() {
        val visit = Visit(casual(2, 3))

        visit.sentAway(1)

        assertIs<GoneState>(visit.state)
        assertTrue(visit.failedAttempt)
        assertTrue(visit.customersInside().isEmpty())
    }

    @Test
    fun aSeatedGroupNoLongerReactsToSeatingEvents() {
        val visit = Visit(regular(1, 2))
        visit.seated(table(2), listOf(Waiter()), 1)

        visit.noWaiterFree(2)
        visit.sentAway(2)

        assertIs<SeatedState>(visit.state)
        assertFalse(visit.isFinished())
    }
}
