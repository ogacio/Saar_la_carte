package de.unisaarland.cs.se.selab.foh.visit

import de.unisaarland.cs.se.selab.sharedPackage.Order

/**
 * The group sits at its table and orders in the same tick; the visit leaves this phase
 * either with an order or with nobody finding a dish.
 */
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
     * Nobody found a dish, so the whole group leaves. This is NOT one of a REGULAR group's three
     * failed attempts: the spec counts "failed reservations, failures to be seated by the waitstaff
     * or the whole group leaving the restaurant because no one was served food", and that last kind
     * is a group that ordered and was never served, which AwaitingMealState handles. A group that
     * could not order at all never got that far.
     */
    override fun onOrderingFailed(visit: Visit, tick: Int) {
        visit.leaveUnserved(visit.customersInside())
        visit.state = GoneState()
    }
}
