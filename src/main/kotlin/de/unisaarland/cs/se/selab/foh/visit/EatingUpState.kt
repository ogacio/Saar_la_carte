package de.unisaarland.cs.se.selab.foh.visit

import de.unisaarland.cs.se.selab.sharedPackage.customers.CustomerStatus

/**
 * Everyone still inside has their meal; the table waits until the last customer has finished eating.
 */
class EatingUpState : VisitState() {

    /** Customers who are done eating are counted; once all are done, the table waits to be escorted. */
    override fun onTickElapsed(visit: Visit, tick: Int) {
        visit.recordFinishedEaters(tick)
        if (visit.customersInside().all { it.status() == CustomerStatus.DONE_EATING }) {
            visit.state = ReadyToLeaveState()
        }
    }
}
