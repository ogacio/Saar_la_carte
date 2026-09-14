

class Visit(
    private val group: CustomerGroup,
) {
    private var state: VisitState = AwaitingSeatState
    var table: Table? = null
    var waiters: List<Waiter> = emptyList()
    var wasSeated: Boolean = false
    var order: Order? = null

    val seatingAttempts: Int
    val orderedTick: Int?
    val firstMealTick: Int?
    val rated: Boolean

    val leftUnservedThisTick: Int
    val finishedEatingThisTick: Int


    public fun seated(table: Table, waiter: Waiter, tick: Int): VisitState {
        state.onSeated(this, table, waiter, tick)
    }

    /** No waiter was free this tick; the group waits and tries once more. */
    public noWaiterFree(tick: Int) = state.onNoWaiterFree(this, tick)

    /** The group is sent away: no table, or an EVENT group that could not be seated completely. */
    public sentAway(tick: Int) = state.onSentAway(this, tick)

    /** The group placed [order]. */
    public ordered(order: Order, tick: Int) = state.onOrdered(this, order, tick)

    /** Nobody in the group found a dish. */
    public orderingFailed(tick: Int) = state.onOrderingFailed(this, tick)

    /** The kitchen finished [meal] of this group's order. */
    public mealCooked(meal: Meal, tick: Int) = state.onMealCooked(this, meal, tick)

    /** [meals] are served to their customers. */
    public serve(meals: List<Meal>, tick: Int) = state.onServed(this, meals, tick)

    /**
     * The eating step of a tick. Resets the two per-tick counters, then lets the
     * state react: waiting deadlines, and who finished eating.
     */
    public advance(tick: Int) {
        leftUnservedThisTick = 0
        finishedEatingThisTick = 0
        state.onTickElapsed(this, tick)

    }

    /** Escorts up to [n] customers outside and returns how many actually left. */
    public escort(n: Int, tick: Int): Int {
        val before = customersInside().size
        state.onEscorted(this, n, tick)
        return before - customersInside().size
    }

    /**
     * Opening time is over: everyone still inside is escorted out at once, whatever
     * phase the visit is in. Called by the FOH in closeOpeningTime.
     */
    public sendOut() {
        if (state is GoneState) return
        sentOutFinished = customersInside().all { it.status == CustomerStatus.DONE_EATING }
        leaveUnserved(customersWaitingForFood())
        customersInside().forEach { it.leave() }
        state = GoneState()
    }

    /** Customers still in the restaurant: how many a waiter has to seat, order for, or escort. */
    fun customersInside(): List<Customer> = group.members.filter { it.status != CustomerStatus.LEFT }

    /** Cooked meals that have not been served yet, whether or not they may go out now. */
    fun cookedMeals(): List<Meal> = order?.meals.orEmpty().filter { it.status == MealStatus.COOKED }

    /** Customers who ordered and have not been served yet. */
    fun customersWaitingForFood(): List<Customer> =
        group.members.filter { it.status == CustomerStatus.ORDERED }

    /** Marks served customers who are done eating now, and counts them for this tick. */
    fun recordFinishedEaters(tick: Int) {
        for (customer in group.members()) {
            if (customer.status == CustomerStatus.SERVED && customer.isDoneEating(tick)) {
                customer.finishEating()
                finishedEatingThisTick++
            }
        }
    }

    /** Customers leave without food; their meals are aborted and status changed. */
    fun leaveUnserved(customers: List<Customer>) {
        // Abort meals, this is kitchen side
        order?.meals.orEmpty().filter { it.customer in customers }.forEach { it.abort() }
        // Customer status cahgnes to LEFT
        customers.forEach { it.leave() }
    }


    public fun isFinished(): Boolean {
        // TODO
        return false
    }

    public fun experience(tick: Int): Int {
        // TODO
    }

    public fun isRated(): Boolean {
        // TODO
        return false
    }

    public fun markRated(): VisitState {
        // TODO
        return state
    }



    
}