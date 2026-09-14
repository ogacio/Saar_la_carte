package de.unisaarland.cs.se.selab.foh


class SeatingService(
    private val tables: TableAssignmentService,
    private val waitstaff: WaiterAssignmentService,
    private val reservations: ReservationBook,
) {

    public fun seatAll(visit: Visit, sbu: SubUnits): Boolean {
        
        val tick = GlobalClock.currentTick
        val busyWaiters = mutableSetOf<Waiter>()
        var customers = 0
        var tablesUsed = 0

        // REGULARS -> EVENT -> CASUAL order for seating
        val groupOrder: List<Visit> = visits.sortedWith(compareBy({ it.group.groupType.ordinal }, { it.group.id }))
        for (v in groupOrder) {
            if (v.state !is AwaitingSeatState) {
                continue
            }
            val waiters = when (v.group.groupType) {
                GroupType.EVENT -> seatEventCustomers(visit, sbu, tick)
                else -> seatOtherCustomers(visit, sbu, tick)
            }
            if (waiters.isEmpty()) {
                continue
            }
            busyWaiters += waiters
            customers += v.customersInside().size
            tablesUsed++
        }

        Logger.Foh.seatingStatus(sbu.restaurantId, busyWaiters.size, customers, tablesUsed)
        
    }


    private fun seatEventCustomers(visit: Visit, sbu: Subunits, tick: Int) {
        
        val size = visit.customersInside().size
        val waiter = waitstaff.assignPermanent(size)
        if (waiter == null) {
            Logger.Foh.noSeatingNoWaiter(sbu.restaurantId, visit.group.id)
            visit.seatingFailed(mayRetry = true, tick)
            return emptyList()
        }
        val table = reservations.claim(visit.group.id) ?: tables.assign(size, visit.group.tableType, liftRule = false)
        if (table == null) {
            // "the customers are sent away and the waiter does not register this as a SEATING action"
            Logger.Foh.noSeatingNoTable(sbu.restaurantId, checkNotNull(waiter.id), v.group.id)
            v.seatingFailed(mayRetry = false, tick)
            return emptyList()
        }
        waiter.consume(ActionType.SEATING, size)
        waiter.adjustLoad(size)
        logSeating(visit, table, listOf(waiter), sbu)
        visit.seated(table, listOf(waiter), tick)
        return listOf(waiter)

    }

    private fun seatOtherCustomers(visit: Visit, sbu: Subunits, tick: Int) {
        
        val plan = waitstaff.assignEvent(visit.customersInside().size, ActionType.SEATING)
        val table = if (plan != null) {
            reservations.claim(visit.group.id)
        } else null
        if (plan == null || table == null) {
            Logger.Foh.noSeatingNoWaiter(sbu.restaurantId, visit.group.id)
            visit.seatingFailed(mayRetry = false, tick)
            return emptyList()
        }
        plan.forEach { 
            (waiter, customers) -> waiter.consume(ActionType.SEATING, customers) 
        }
        val waiters = plan.keys.toList()

        if (table.isMerged) {
            Logger.Foh.mergingTables(sbu.restaurantId, visit.group.id, table.originals().map { it.id }, table.id)
        }
        Logger.Foh.seating(sbu.restaurantId, visit.group.id, table.id, waiters.map { checkNotNull(it.id) })

        v.seated(table, emptyList(), tick)
        return waiters

    }

    
}