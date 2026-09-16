package de.unisaarland.cs.se.selab.foh.visit

import de.unisaarland.cs.se.selab.foh.Table
import de.unisaarland.cs.se.selab.foh.Waiter
import de.unisaarland.cs.se.selab.sharedPackage.Meal
import de.unisaarland.cs.se.selab.sharedPackage.Order

// sealed instead of abstract because the set of states is fixed and all of them live in this package.
sealed class VisitState {

    open fun onSeated(visit: Visit, table: Table, waiters: List<Waiter>, tick: Int) = Unit

    open fun onNoWaiterFree(visit: Visit, tick: Int) = Unit

    open fun onSentAway(visit: Visit, tick: Int) = Unit

    open fun onOrdered(visit: Visit, order: Order, tick: Int) = Unit

    open fun onOrderingFailed(visit: Visit, tick: Int) = Unit

    open fun onServed(visit: Visit, meals: List<Meal>, tick: Int) = Unit

    open fun onTickElapsed(visit: Visit, tick: Int) = Unit

    open fun onEscorted(visit: Visit, n: Int, tick: Int) = Unit
}