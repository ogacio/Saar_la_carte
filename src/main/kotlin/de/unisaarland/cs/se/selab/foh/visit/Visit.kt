package de.unisaarland.cs.se.selab.foh.visit

import de.unisaarland.cs.se.selab.foh.Table
import de.unisaarland.cs.se.selab.foh.Waiter
import de.unisaarland.cs.se.selab.sharedPackage.Meal
import de.unisaarland.cs.se.selab.sharedPackage.MealStatus
import de.unisaarland.cs.se.selab.sharedPackage.Order
import de.unisaarland.cs.se.selab.sharedPackage.customers.Customer
import de.unisaarland.cs.se.selab.sharedPackage.customers.CustomerGroup
import de.unisaarland.cs.se.selab.sharedPackage.customers.CustomerStatus

class Visit(
    private val group: CustomerGroup,
) {
    var state: VisitState = AwaitingSeatState()
    var table: Table? = null
    var waiters: List<Waiter> = emptyList()
    var wasSeated: Boolean = false
    var order: Order? = null

    var seatingAttempts: Int = 0
    var orderedTick: Int? = null
    var firstMealTick: Int? = null
    var rated: Boolean = false

    var leftUnservedThisTick: Int = 0
    var finishedEatingThisTick: Int = 0


    public fun seated(table: Table, waiter: List<Waiter>, tick: Int): Unit {
        state.onSeated(this, table, waiter, tick)
    }

    /** No waiter was free this tick; the group waits and tries once more. */
    public fun noWaiterFree(tick: Int) = state.onNoWaiterFree(this, tick)

    /** The group is sent away: no table, or an EVENT group that could not be seated completely. */
    public fun sentAway(tick: Int) = state.onSentAway(this, tick)

    /** The group placed [order]. */
    public fun ordered(order: Order, tick: Int) = state.onOrdered(this, order, tick)

    /** Nobody in the group found a dish. */
    public fun orderingFailed(tick: Int) = state.onOrderingFailed(this, tick)

    /** The kitchen finished [meal] of this group's order. */
    public fun mealCooked(meal: Meal, tick: Int) = state.onMealCooked(this, meal, tick)

    /** [meals] are served to their customers. */
    public fun serve(meals: List<Meal>, tick: Int) = state.onServed(this, meals, tick)

    /**
     * The eating step of a tick. Resets the two per-tick counters, then lets the
     * state react: waiting deadlines, and who finished eating.
     */
    public fun advance(tick: Int) {
        leftUnservedThisTick = 0
        finishedEatingThisTick = 0
        state.onTickElapsed(this, tick)

    }

    /** Escorts up to [n] customers outside and returns how many actually left. */
    public fun escort(n: Int, tick: Int): Int {
        val before = customersInside().size
        state.onEscorted(this, n, tick)
        return before - customersInside().size
    }

    /**
     * Opening time is over: everyone still inside is escorted out at once, whatever
     * phase the visit is in. Called by the FOH in closeOpeningTime.
     */
    public fun sendOut() {
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