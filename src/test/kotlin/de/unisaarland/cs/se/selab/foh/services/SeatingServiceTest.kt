package de.unisaarland.cs.se.selab.foh.services

import de.unisaarland.cs.se.selab.foh.ActionType
import de.unisaarland.cs.se.selab.foh.Table
import de.unisaarland.cs.se.selab.foh.TableAssignmentService
import de.unisaarland.cs.se.selab.foh.Waiter
import de.unisaarland.cs.se.selab.foh.WaiterAssignmentService
import de.unisaarland.cs.se.selab.foh.visit.AwaitingSeatState
import de.unisaarland.cs.se.selab.foh.visit.SeatedState
import de.unisaarland.cs.se.selab.foh.visit.Visit
import de.unisaarland.cs.se.selab.sharedPackage.TableType
import de.unisaarland.cs.se.selab.simulation.ReservationBook
import de.unisaarland.cs.se.selab.testsupport.Fixtures.captureLog
import de.unisaarland.cs.se.selab.testsupport.Fixtures.casual
import de.unisaarland.cs.se.selab.testsupport.Fixtures.event
import de.unisaarland.cs.se.selab.testsupport.Fixtures.logLines
import de.unisaarland.cs.se.selab.testsupport.Fixtures.regular
import de.unisaarland.cs.se.selab.testsupport.Fixtures.subUnits
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertTrue

/** F16: seating, including the EVENT seating of P02/P03. */
class SeatingServiceTest {

    private val sbu = subUnits()

    private class Floor(tables: List<Table>, waiters: List<Waiter>) {
        val tables = TableAssignmentService(tables.toMutableList())
        val waitstaff = WaiterAssignmentService(waiters.toMutableList())
        val reservations = ReservationBook(this.tables)
        val seating = SeatingService(this.tables, waitstaff, reservations)
    }

    @Test
    fun visitThatIsNotWaitingForASeatIsIgnored() {
        val floor = Floor(listOf(Table(1, 2, TableType.COMMON)), listOf(Waiter()))
        val visit = Visit(casual(3, 2))
        visit.seated(Table(9, 2, TableType.COMMON), emptyList(), 1)
        val log = captureLog()

        floor.seating.seat(visit, sbu, 1)
        floor.seating.logStatus(sbu)

        assertEquals(
            listOf("[DEBUG] FOH Seating Status (R 1): 0 waitstaff seated 0 customers on 0 tables."),
            logLines(log),
        )
    }

    @Test
    fun noFreeWaiterLetsTheGroupTryAgain() {
        val floor = Floor(listOf(Table(1, 2, TableType.COMMON)), emptyList())
        val visit = Visit(regular(3, 2))
        val log = captureLog()

        floor.seating.seat(visit, sbu, 1)

        assertEquals(listOf("[INFO] FOH No Seating (R 1): No free waitstaff available for group 3."), logLines(log))
        assertEquals(1, visit.seatingAttempts)
        assertIs<AwaitingSeatState>(visit.state)
    }

    @Test
    fun waiterButNoTableSendsTheGroupAway() {
        val waiter = Waiter()
        val floor = Floor(emptyList(), listOf(waiter))
        val visit = Visit(casual(3, 2))
        val log = captureLog()

        floor.seating.seat(visit, sbu, 1)

        assertEquals(
            listOf("[INFO] FOH No Seating (R 1): Assigned waitstaff 1 but no table available, group 3 is sent away."),
            logLines(log),
        )
        assertTrue(visit.isFinished())
        assertTrue(visit.failedAttempt)
        assertEquals(Waiter.ACTION_LIMIT, waiter.remaining(ActionType.SEATING))
    }

    @Test
    fun regularGroupIsSeatedAtItsReservedTable() {
        val waiter = Waiter()
        val floor = Floor(listOf(Table(1, 2, TableType.COMMON), Table(2, 3, TableType.COMMON)), listOf(waiter))
        val group = regular(3, 3)
        floor.reservations.openEvening(1, listOf(group))
        val visit = Visit(group)
        val log = captureLog()

        floor.seating.seat(visit, sbu, 1)

        assertEquals(listOf("[IMPORTANT] FOH Seating (R 1): Group 3 seated at table 2 by waitstaff 1."), logLines(log))
        assertIs<SeatedState>(visit.state)
        assertEquals(2, visit.table?.id)
        assertEquals(listOf(waiter), visit.waiters)
        assertEquals(3, waiter.currentLoad)
        assertEquals(Waiter.ACTION_LIMIT - 3, waiter.remaining(ActionType.SEATING))
    }

    @Test
    fun casualGroupGetsAMergedTableAndTheMergeIsLogged() {
        val floor = Floor(listOf(Table(1, 2, TableType.COMMON), Table(2, 2, TableType.COMMON)), listOf(Waiter()))
        val visit = Visit(casual(3, 4))
        val log = captureLog()

        floor.seating.seat(visit, sbu, 1)

        assertEquals(
            listOf(
                "[INFO] FOH Merging Tables (R 1): For group 3 the tables 1,2 were merged into 1.",
                "[IMPORTANT] FOH Seating (R 1): Group 3 seated at table 1 by waitstaff 1.",
            ),
            logLines(log),
        )
    }

    @Test
    fun eventGroupWithoutEnoughWaitersIsSentAway() {
        val floor = Floor(listOf(Table(1, 4, TableType.COMMON)), emptyList())
        val group = event(3, 4)
        floor.reservations.bookAhead(group, 4)
        floor.reservations.openEvening(4, emptyList())
        val visit = Visit(group)
        val log = captureLog()

        floor.seating.seat(visit, sbu, 1)

        assertEquals(listOf("[INFO] FOH No Seating (R 1): No free waitstaff available for group 3."), logLines(log))
        assertTrue(visit.isFinished())
    }

    @Test
    fun eventGroupWithoutReservedTableIsSentAway() {
        val floor = Floor(listOf(Table(1, 4, TableType.COMMON)), listOf(Waiter()))
        val visit = Visit(event(3, 4))
        val log = captureLog()

        floor.seating.seat(visit, sbu, 1)

        assertEquals(listOf("[INFO] FOH No Seating (R 1): No free waitstaff available for group 3."), logLines(log))
        assertTrue(visit.isFinished())
    }

    @Test
    fun eventGroupIsSeatedByOneWaiterAtAMergedTableWithoutLoad() {
        val waiter = Waiter()
        val floor = Floor(listOf(Table(1, 2, TableType.COMMON), Table(2, 2, TableType.COMMON)), listOf(waiter))
        val group = event(3, 4)
        floor.reservations.bookAhead(group, 4)
        floor.reservations.openEvening(4, emptyList())
        val visit = Visit(group)
        val log = captureLog()

        floor.seating.seat(visit, sbu, 1)

        assertEquals(
            listOf(
                "[INFO] FOH Merging Tables (R 1): For group 3 the tables 1,2 were merged into 1.",
                "[IMPORTANT] FOH Seating (R 1): Group 3 seated at table 1 by waitstaff 1.",
            ),
            logLines(log),
        )
        assertTrue(visit.waiters.isEmpty())
        assertEquals(0, waiter.currentLoad)
        assertEquals(Waiter.ACTION_LIMIT - 4, waiter.remaining(ActionType.SEATING))
    }

    @Test
    fun largeEventGroupIsSplitOverSeveralWaiters() {
        val first = Waiter()
        val second = Waiter()
        val floor = Floor(listOf(Table(1, 12, TableType.COMMON)), listOf(first, second))
        val group = event(3, 12)
        floor.reservations.bookAhead(group, 4)
        floor.reservations.openEvening(4, emptyList())
        val log = captureLog()

        floor.seating.seat(Visit(group), sbu, 1)
        floor.seating.logStatus(sbu)

        assertEquals(
            listOf(
                "[IMPORTANT] FOH Seating (R 1): Group 3 seated at table 1 by waitstaff 1,2.",
                "[DEBUG] FOH Seating Status (R 1): 2 waitstaff seated 12 customers on 1 tables.",
            ),
            logLines(log),
        )
        assertEquals(0, first.remaining(ActionType.SEATING))
        assertEquals(Waiter.ACTION_LIMIT - 2, second.remaining(ActionType.SEATING))
    }

    @Test
    fun statusCountsSuccessfulSeatingsAndStartsAfresh() {
        val floor = Floor(listOf(Table(1, 2, TableType.COMMON), Table(2, 3, TableType.COMMON)), listOf(Waiter()))
        floor.seating.seat(Visit(casual(1, 2)), sbu, 1)
        floor.seating.seat(Visit(casual(2, 3)), sbu, 1)
        val log = captureLog()

        floor.seating.logStatus(sbu)
        floor.seating.logStatus(sbu)

        assertEquals(
            listOf(
                "[DEBUG] FOH Seating Status (R 1): 1 waitstaff seated 5 customers on 2 tables.",
                "[DEBUG] FOH Seating Status (R 1): 0 waitstaff seated 0 customers on 0 tables.",
            ),
            logLines(log),
        )
    }
}
