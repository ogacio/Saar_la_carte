package de.unisaarland.cs.se.selab.foh.visit

import de.unisaarland.cs.se.selab.foh.Table
import de.unisaarland.cs.se.selab.foh.Waiter

class AwaitingSeatState: VisitState() {

    override fun onSeated(visit: Visit, table: Table, waiters: List<Waiter>, tick: Int) { 
        visit.table = table
        visit.waiters = waiters
        visit.state = SeatedState()
    }


    /** "The customers try again the next tick and then leave, they do not reconsider." */
    override fun onNoWaiterFree(visit: Visit, tick: Int) {
        if (visit.noWaiterAttempts == 0) {
            visit.noWaiterAttempts = 1
            return
        }
        giveUp(visit)
    }

    /** No table, or an EVENT group that could not be seated completely: the group leaves at once. */
    override fun onSentAway(visit: Visit, tick: Int) = giveUp(visit)

    /** The group never sat down: everyone leaves, and it counts as a failed attempt. */
    private fun giveUp(visit: Visit) {
        visit.leaveUnserved(visit.customersInside())
        visit.failedAttempt = true
        visit.state = GoneState()
    }


}