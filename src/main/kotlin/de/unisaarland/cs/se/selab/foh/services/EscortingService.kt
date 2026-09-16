package de.unisaarland.cs.se.selab.foh.services

import de.unisaarland.cs.se.selab.foh.ActionType
import de.unisaarland.cs.se.selab.foh.Waiter
import de.unisaarland.cs.se.selab.foh.WaiterAssignmentService
import de.unisaarland.cs.se.selab.foh.visit.ReadyToLeaveState
import de.unisaarland.cs.se.selab.foh.visit.Visit
import de.unisaarland.cs.se.selab.logging.Logger
import de.unisaarland.cs.se.selab.sharedPackage.customers.GroupType
import de.unisaarland.cs.se.selab.simulation.GlobalClock
import de.unisaarland.cs.se.selab.simulation.SubUnits

/**
 * Step 6: walks groups that have finished eating outside (spec, "Escorting customers").
 *
 * Escorting may be partial when the waiters' ESCORTING capacity runs out; the table
 * stays taken until the last customer is out. What happens after a visit ends (statistics,
 * history, releasing the table) is done by the FOH at the end of the tick, because not
 * every visit ends through escorting.
 */
class EscortingService(
    private val waitstaff: WaiterAssignmentService,
) {

    /** Escorts every group that is ready to leave, in group order, and logs the summary. */
    fun escort(visits: List<Visit>, sbu: SubUnits) {
        val tick = GlobalClock.currentTick
        val busyWaiters = mutableSetOf<Waiter>()
        var customers = 0

        for (visit in visits) {
            if (visit.state !is ReadyToLeaveState) continue
            val table = visit.table ?: continue
            for ((waiter, count) in planWaiters(visit, visit.customersInside().size)) {
                val left = visit.escort(count, tick)
                if (left == 0) continue
                waiter.consume(ActionType.ESCORTING, left)
                if (visit.group.groupType() != GroupType.EVENT) waiter.adjustLoad(-left)
                Logger.Foh.escorting(sbu.restaurantId, checkNotNull(waiter.id), left, visit.group.id(), table.id)
                busyWaiters += waiter
                customers += left
            }
        }

        Logger.Foh.escortingStatus(sbu.restaurantId, busyWaiters.size, customers)
    }

    /**
     * REGULAR and CASUAL: the permanent waiter, up to their ESCORTING capacity.
     * EVENT: the manager's plan, which "prioritizes waiters with the lowest current load".
     */
    private fun planWaiters(visit: Visit, customers: Int): Map<Waiter, Int> {
        if (visit.group.groupType() == GroupType.EVENT) {
            val count = minOf(customers, waitstaff.capacity(ActionType.ESCORTING))
            return waitstaff.assignEvent(count, ActionType.ESCORTING).orEmpty()
        }
        val waiter = visit.waiters.firstOrNull() ?: return emptyMap()
        return mapOf(waiter to minOf(customers, waiter.remaining(ActionType.ESCORTING)))
    }
}
