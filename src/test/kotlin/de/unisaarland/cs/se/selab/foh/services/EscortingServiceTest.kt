package de.unisaarland.cs.se.selab.foh.services

import de.unisaarland.cs.se.selab.foh.ActionType
import de.unisaarland.cs.se.selab.foh.Table
import de.unisaarland.cs.se.selab.foh.Waiter
import de.unisaarland.cs.se.selab.foh.WaiterAssignmentService
import de.unisaarland.cs.se.selab.foh.visit.ReadyToLeaveState
import de.unisaarland.cs.se.selab.foh.visit.Visit
import de.unisaarland.cs.se.selab.sharedPackage.TableType
import de.unisaarland.cs.se.selab.sharedPackage.customers.CustomerGroup
import de.unisaarland.cs.se.selab.testsupport.Fixtures.captureLog
import de.unisaarland.cs.se.selab.testsupport.Fixtures.casual
import de.unisaarland.cs.se.selab.testsupport.Fixtures.event
import de.unisaarland.cs.se.selab.testsupport.Fixtures.logLines
import de.unisaarland.cs.se.selab.testsupport.Fixtures.regular
import de.unisaarland.cs.se.selab.testsupport.Fixtures.subUnits
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/** F21: escorting, including the EVENT escorting of P03. */
class EscortingServiceTest {

    private val sbu = subUnits()

    private fun waiter(id: Int, load: Int = 0) = Waiter().also {
        it.id = id
        it.adjustLoad(load)
    }

    /** A visit that finished eating, sitting at table [tableId] and served by [waiters]. */
    private fun readyVisit(group: CustomerGroup, tableId: Int = 4, waiters: List<Waiter> = emptyList()) =
        Visit(group).also {
            it.table = Table(tableId, group.groupSize(), TableType.COMMON)
            it.waiters = waiters
            it.state = ReadyToLeaveState()
        }

    @Test
    fun visitsThatAreNotReadyOrHaveNoTableAreSkipped() {
        val waiter = waiter(1, load = 2)
        val notReady = Visit(regular(1, 2)).also { it.waiters = listOf(waiter) }
        val noTable = Visit(regular(2, 2)).also {
            it.waiters = listOf(waiter)
            it.state = ReadyToLeaveState()
        }
        val service = EscortingService(WaiterAssignmentService(mutableListOf(waiter)))
        val log = captureLog()

        service.escort(listOf(notReady, noTable), sbu)

        assertEquals(
            listOf("[DEBUG] FOH Escorting Status (R 1): 0 waitstaff escorted 0 customers this tick."),
            logLines(log),
        )
    }

    @Test
    fun permanentWaiterEscortsTheWholeGroupAndLosesTheLoad() {
        val waiter = waiter(1, load = 3)
        val visit = readyVisit(regular(3, 3), waiters = listOf(waiter))
        val service = EscortingService(WaiterAssignmentService(mutableListOf(waiter)))
        val log = captureLog()

        service.escort(listOf(visit), sbu)

        assertEquals(
            listOf(
                "[IMPORTANT] FOH Escorting (R 1): Waitstaff 1 escorts 3 customers of group 3 from table 4 outside.",
                "[DEBUG] FOH Escorting Status (R 1): 1 waitstaff escorted 3 customers this tick.",
            ),
            logLines(log),
        )
        assertTrue(visit.isFinished())
        assertEquals(0, waiter.currentLoad)
        assertEquals(Waiter.ACTION_LIMIT - 3, waiter.remaining(ActionType.ESCORTING))
    }

    @Test
    fun permanentWaiterEscortsOnlyUpToTheActionLimit() {
        val waiter = waiter(1, load = 5).also { it.consume(ActionType.ESCORTING, 8) }
        val visit = readyVisit(casual(3, 5), waiters = listOf(waiter))
        val service = EscortingService(WaiterAssignmentService(mutableListOf(waiter)))
        val log = captureLog()

        service.escort(listOf(visit), sbu)

        assertEquals(
            "[IMPORTANT] FOH Escorting (R 1): Waitstaff 1 escorts 2 customers of group 3 from table 4 outside.",
            logLines(log).first(),
        )
        assertFalse(visit.isFinished())
        assertEquals(3, visit.customersInside().size)
        assertEquals(3, waiter.currentLoad)
    }

    @Test
    fun exhaustedWaiterEscortsNobodyAndLogsNothingButTheStatus() {
        val waiter = waiter(1, load = 2).also { it.consume(ActionType.ESCORTING, Waiter.ACTION_LIMIT) }
        val visit = readyVisit(regular(3, 2), waiters = listOf(waiter))
        val service = EscortingService(WaiterAssignmentService(mutableListOf(waiter)))
        val log = captureLog()

        service.escort(listOf(visit), sbu)

        assertEquals(
            listOf("[DEBUG] FOH Escorting Status (R 1): 0 waitstaff escorted 0 customers this tick."),
            logLines(log),
        )
        assertEquals(2, visit.customersInside().size)
    }

    @Test
    fun nonEventVisitWithoutWaiterIsNotEscorted() {
        val visit = readyVisit(regular(3, 2))
        val service = EscortingService(WaiterAssignmentService(mutableListOf(waiter(1))))
        captureLog()

        service.escort(listOf(visit), sbu)

        assertEquals(2, visit.customersInside().size)
    }

    @Test
    fun eventGroupIsEscortedByTheLeastLoadedWaitersFirstWithoutChangingLoads() {
        val busy = waiter(1, load = 5)
        val idle = waiter(2)
        val visit = readyVisit(event(3, 12))
        val service = EscortingService(WaiterAssignmentService(mutableListOf(busy, idle)))
        val log = captureLog()

        service.escort(listOf(visit), sbu)

        assertEquals(
            listOf(
                "[IMPORTANT] FOH Escorting (R 1): Waitstaff 2 escorts 10 customers of group 3 from table 4 outside.",
                "[IMPORTANT] FOH Escorting (R 1): Waitstaff 1 escorts 2 customers of group 3 from table 4 outside.",
                "[DEBUG] FOH Escorting Status (R 1): 2 waitstaff escorted 12 customers this tick.",
            ),
            logLines(log),
        )
        assertTrue(visit.isFinished())
        assertEquals(5, busy.currentLoad)
        assertEquals(0, idle.currentLoad)
    }

    @Test
    fun eventGroupLargerThanTheCapacityIsEscortedPartially() {
        val only = waiter(1)
        val visit = readyVisit(event(3, 12))
        val service = EscortingService(WaiterAssignmentService(mutableListOf(only)))
        captureLog()

        service.escort(listOf(visit), sbu)

        assertEquals(2, visit.customersInside().size)
        assertFalse(visit.isFinished())
    }

    @Test
    fun statusSumsOverAllVisitsOfTheTick() {
        val waiter = waiter(1, load = 4)
        val first = readyVisit(regular(1, 2), tableId = 1, waiters = listOf(waiter))
        val second = readyVisit(regular(2, 2), tableId = 2, waiters = listOf(waiter))
        val service = EscortingService(WaiterAssignmentService(mutableListOf(waiter)))
        val log = captureLog()

        service.escort(listOf(first, second), sbu)

        assertEquals(
            "[DEBUG] FOH Escorting Status (R 1): 1 waitstaff escorted 4 customers this tick.",
            logLines(log).last(),
        )
    }
}
