
// sealed insted of abstract because the set of states is fixed and all of them live in this package.
sealed class VisitState {

    open fun onSeated(v: Visit, table: Table, waiters: List<Waiter>, tick: Int) = Unit

    open fun onNoWaiterFree(v: Visit, tick: Int) = Unit

    open fun onSentAway(v: Visit, tick: Int) = Unit

    open fun onOrdered(v: Visit, order: Order, tick: Int) = Unit

    open fun onOrderingFailed(v: Visit, tick: Int) = Unit

    open fun onMealCooked(v: Visit, meal: Meal, tick: Int) = Unit

    open fun onServed(v: Visit, meals: List<Meal>, tick: Int) = Unit

    open fun onTickElapsed(v: Visit, tick: Int) = Unit

    open fun onEscorted(v: Visit, n: Int, tick: Int) = Unit


}