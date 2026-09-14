package de.unisaarland.cs.se.selab.foh.visit


class SeatedState : VisitState() {

    /**
     * The order is in; the waiting clock starts. Customers who found nothing have
     * already left inside OrderingService, so everyone still here has ordered.
     */
    override fun onOrdered(visit: Visit, order: Order, tick: Int) {
        visit.order = order
        visit.orderedTick = tick
        visit.state = AwaitingMealState()
    }

    /**
     * Nobody found a dish, so the whole group leaves. Whether this counts as a
     * failed attempt for a REGULAR group is still open (FOH services PDF, 8.3), so
     * it is not marked as one here.
     */
    override fun onOrderingFailed(visit: Visit, tick: Int) {
        visit.leaveUnserved(visit.customersInside())
        visit.state = GoneState()
    }
    
}