package de.unisaarland.cs.se.selab.foh.visit


class ReadyToLeaveState : VisitState() {

    override fun onEscorted(visit: Visit, n: Int, tick: Int) {
        visit.customersInside().take(n).forEach { it.leave() }
        if (visit.customersInside().isEmpty()) visit.state = GoneState()
    }
    
}