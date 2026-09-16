package de.unisaarland.cs.se.selab.foh.services

import de.unisaarland.cs.se.selab.foh.ActionType
import de.unisaarland.cs.se.selab.foh.TableAssignmentService
import de.unisaarland.cs.se.selab.foh.Waiter
import de.unisaarland.cs.se.selab.foh.WaiterAssignmentService
import de.unisaarland.cs.se.selab.foh.visit.AwaitingSeatState
import de.unisaarland.cs.se.selab.foh.visit.Visit
import de.unisaarland.cs.se.selab.logging.Logger
import de.unisaarland.cs.se.selab.sharedPackage.customers.GroupType
import de.unisaarland.cs.se.selab.simulation.ReservationBook
import de.unisaarland.cs.se.selab.simulation.SubUnits

/**
 * Step 1, seating: gives an arriving group a waiter and a table (spec, "Front of house").
 *
 * REGULAR and CASUAL groups get one permanent waiter first, then a table: the one
 * reserved in preparation, or for a CASUAL group one assigned now by rules 1 to 4.
 * EVENT groups are seated by as many waiters as needed at their reserved table, all
 * within this tick or not at all.
 *
 * The FOH calls [seat] once per group, because arrival, seating and ordering run group
 * by group. The numbers for the status line are collected until [logStatus].
 */
class SeatingService(
    private val tables: TableAssignmentService,
    private val waitstaff: WaiterAssignmentService,
    private val reservations: ReservationBook,
) {
    private val busyWaiters = mutableSetOf<Waiter>()
    private var customers = 0
    private var tablesUsed = 0

    /** Tries to seat [visit] if it still waits for a table, and counts a success for the status line. */
    fun seat(visit: Visit, sbu: SubUnits, tick: Int) {
        if (visit.state !is AwaitingSeatState) return
        val waiters = when (visit.group.groupType()) {
            GroupType.EVENT -> seatEventCustomers(visit, sbu, tick)
            else -> seatOtherCustomers(visit, sbu, tick)
        }
        if (waiters.isEmpty()) return
        busyWaiters += waiters
        customers += visit.customersInside().size
        tablesUsed++
    }

    /** Writes the seating summary of this tick and starts counting afresh. */
    fun logStatus(sbu: SubUnits) {
        // "Status logs like this one only log successful attempts."
        Logger.Foh.seatingStatus(sbu.restaurantId, busyWaiters.size, customers, tablesUsed)
        busyWaiters.clear()
        customers = 0
        tablesUsed = 0
    }

    /**
     * REGULAR and CASUAL: waiter first, then table. Returns the waiter who seated the
     * group, or an empty list if seating failed.
     */
    private fun seatOtherCustomers(visit: Visit, sbu: SubUnits, tick: Int): List<Waiter> {
        val size = visit.customersInside().size
        val waiter = waitstaff.assignPermanent(size)
        if (waiter == null) {
            Logger.Foh.noSeatingNoWaiter(sbu.restaurantId, visit.group.id())
            visit.noWaiterFree(tick)
            return emptyList()
        }
        val table = reservations.claim(visit.group.id())
            ?: tables.assign(size, visit.group.tableType(), liftRule = false)
        if (table == null) {
            // "the customers are sent away and the waiter does not register this as a SEATING action"
            Logger.Foh.noSeatingNoTable(sbu.restaurantId, checkNotNull(waiter.id), visit.group.id())
            visit.sentAway(tick)
            return emptyList()
        }
        waiter.consume(ActionType.SEATING, size)
        waiter.adjustLoad(size)

        if (table.isMerged) {
            Logger.Foh.mergingTables(sbu.restaurantId, visit.group.id(), table.originals().map { it.id }, table.id)
        }
        Logger.Foh.seating(sbu.restaurantId, visit.group.id(), table.id, listOf(checkNotNull(waiter.id)))

        visit.seated(table, listOf(waiter), tick)
        return listOf(waiter)
    }

    /**
     * EVENT: the manager's plan decides which waiters seat how many customers, at the
     * table reserved in preparation. "An EVENT SEATING only counts as fulfilled if all
     * customers in the group could be seated within the tick." Event customers do not
     * count toward a waiter's current load.
     */
    private fun seatEventCustomers(visit: Visit, sbu: SubUnits, tick: Int): List<Waiter> {
        val plan = waitstaff.assignEvent(visit.customersInside().size, ActionType.SEATING)
        val table = if (plan != null) reservations.claim(visit.group.id()) else null
        if (plan == null || table == null) {
            Logger.Foh.noSeatingNoWaiter(sbu.restaurantId, visit.group.id())
            visit.sentAway(tick)
            return emptyList()
        }
        plan.forEach { (waiter, customers) -> waiter.consume(ActionType.SEATING, customers) }
        val waiters = plan.keys.toList()

        if (table.isMerged) {
            Logger.Foh.mergingTables(sbu.restaurantId, visit.group.id(), table.originals().map { it.id }, table.id)
        }
        Logger.Foh.seating(sbu.restaurantId, visit.group.id(), table.id, waiters.map { checkNotNull(it.id) })

        visit.seated(table, emptyList(), tick)
        return waiters
    }
}
