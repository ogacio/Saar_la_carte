package de.unisaarland.cs.se.selab.foh.visit

import de.unisaarland.cs.se.selab.foh.Table
import de.unisaarland.cs.se.selab.foh.Waiter
import de.unisaarland.cs.se.selab.sharedPackage.Meal
import de.unisaarland.cs.se.selab.sharedPackage.Order

/**
 * One phase of a [Visit] (State pattern).
 *
 * Only [Visit] calls these methods: a service calls the visit, and the visit forwards
 * to its current state. Every method has a harmless default, so a state overrides only
 * what it reacts to. A state moves the visit on by assigning [Visit.state].
 *
 * `sealed` instead of abstract because the set of phases is fixed and all of them live in this package.
 */
sealed class VisitState {

    /** The group sat down at [table]; [waiters] is empty for an EVENT group. */
    open fun onSeated(visit: Visit, table: Table, waiters: List<Waiter>, tick: Int) = Unit

    /** No waiter was free to seat the group this tick. */
    open fun onNoWaiterFree(visit: Visit, tick: Int) = Unit

    /** The group is sent away: no table, or an EVENT group that could not be seated completely. */
    open fun onSentAway(visit: Visit, tick: Int) = Unit

    /** The group placed [order]. */
    open fun onOrdered(visit: Visit, order: Order, tick: Int) = Unit

    /** Nobody in the group found a dish. */
    open fun onOrderingFailed(visit: Visit, tick: Int) = Unit

    /** [meals] were served to their customers. */
    open fun onServed(visit: Visit, meals: List<Meal>, tick: Int) = Unit

    /** The eating step of a tick has come round. */
    open fun onTickElapsed(visit: Visit, tick: Int) = Unit

    /** A waiter escorts up to [n] customers outside. */
    open fun onEscorted(visit: Visit, n: Int, tick: Int) = Unit
}
