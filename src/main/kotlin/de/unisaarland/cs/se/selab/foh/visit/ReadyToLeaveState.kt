package de.unisaarland.cs.se.selab.foh.visit

/**
 * Everyone has finished eating and waits to be escorted outside; escorting may take several ticks.
 */
class ReadyToLeaveState : VisitState() {

    /** Up to [n] customers leave; once the table is empty, the visit is over. */
    override fun onEscorted(visit: Visit, n: Int, tick: Int) {
        visit.customersInside().take(n).forEach { it.leave() }
        if (visit.customersInside().isEmpty()) visit.state = GoneState()
    }
}
