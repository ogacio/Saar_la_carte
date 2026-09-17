package de.unisaarland.cs.se.selab.foh.visit

import de.unisaarland.cs.se.selab.foh.Table
import de.unisaarland.cs.se.selab.foh.Waiter
import de.unisaarland.cs.se.selab.sharedPackage.Meal
import de.unisaarland.cs.se.selab.sharedPackage.MealStatus
import de.unisaarland.cs.se.selab.sharedPackage.Order
import de.unisaarland.cs.se.selab.sharedPackage.customers.Customer
import de.unisaarland.cs.se.selab.sharedPackage.customers.CustomerGroup
import de.unisaarland.cs.se.selab.sharedPackage.customers.CustomerStatus
import de.unisaarland.cs.se.selab.sharedPackage.customers.GroupType
import de.unisaarland.cs.se.selab.simulation.ratings.Experience

/**
 * One customer group's stay in one restaurant, from arrival to leaving.
 *
 * Services report what happened by calling the methods below. Each of them forwards
 * to the current [VisitState], which applies the event and decides when the phase is over.
 */
class Visit(val group: CustomerGroup) {

    /** The phase this visit is in. Services read it; the states assign the next one. */
    internal var state: VisitState = AwaitingSeatState()

    /** The table the group sits at; null until it is seated. */
    var table: Table? = null

    /** REGULAR and CASUAL: the permanent waiter. EVENT: empty, the manager picks waiters per action. */
    var waiters: List<Waiter> = emptyList()

    /** Whether the group ever sat down. */
    var wasSeated: Boolean = false

    /** The group's order; null until it has ordered. */
    var order: Order? = null

    /** Failed seatings because no waiter was free. The group tries once more, then leaves. */
    var seatingAttempts: Int = 0

    /** The tick the order was taken. Every waiting deadline counts from here. */
    var orderedTick: Int? = null

    /** The tick the first meal of the order was cooked, set by serving. The hold-back rule counts from here. */
    var firstMealTick: Int? = null

    /** Whether the table already waited one tick because the waitstaff lacked SERVING capacity. */
    var waitedForCapacity: Boolean = false

    /** Whether the visit ended as a failed attempt for a REGULAR group's two-strikes rule. */
    var failedAttempt: Boolean = false

    /** Whether the rating step has already handled this visit. */
    var rated: Boolean = false
        private set

    /** Customers who left this tick without being served. Reset by [advance]. */
    var leftUnservedThisTick: Int = 0

    /** Customers who finished eating this tick. Reset by [advance]. */
    var finishedEatingThisTick: Int = 0

    /** Set by [sendOut]: whether everyone still inside had finished eating when opening time ended. */
    private var sentOutFinished: Boolean? = null

    /** The group sits down at [table]; [waiters] is empty for an EVENT group. */
    fun seated(table: Table, waiters: List<Waiter>, tick: Int) = state.onSeated(this, table, waiters, tick)

    /** No waiter was free this tick; the group waits and tries once more. */
    fun noWaiterFree(tick: Int) = state.onNoWaiterFree(this, tick)

    /** The group is sent away: no table, or an EVENT group that could not be seated completely. */
    fun sentAway(tick: Int) = state.onSentAway(this, tick)

    /** The group placed [order]. */
    fun ordered(order: Order, tick: Int) = state.onOrdered(this, order, tick)

    /** Nobody in the group found a dish. */
    fun orderingFailed(tick: Int) = state.onOrderingFailed(this, tick)

    /** [meals] are served to their customers. */
    fun serve(meals: List<Meal>, tick: Int) = state.onServed(this, meals, tick)

    /**
     * The eating step of a tick. Resets the two per-tick counters, then lets the
     * state react: waiting deadlines, and who finished eating.
     */
    fun advance(tick: Int) {
        leftUnservedThisTick = 0
        finishedEatingThisTick = 0
        state.onTickElapsed(this, tick)
    }

    /** Escorts up to [n] customers outside and returns how many actually left. */
    fun escort(n: Int, tick: Int): Int {
        val before = customersInside().size
        state.onEscorted(this, n, tick)
        return before - customersInside().size
    }

    /**
     * Opening time is over: everyone still inside is escorted out at once, whatever
     * phase the visit is in. Called by the FOH in closeOpeningTime.
     */
    fun sendOut() {
        if (state is GoneState) return
        sentOutFinished = customersInside().all { it.status() == CustomerStatus.DONE_EATING }
        val inside = customersInside()
        leaveUnserved(customersWaitingForFood()) // aborts their meals and reduces the load
        val remaining = inside.filter { it.status() != CustomerStatus.LEFT }
        remaining.forEach { it.leave() }
        if (group.groupType() != GroupType.EVENT) {
            waiters.forEach { it.adjustLoad(-remaining.size) }
        }
        state = GoneState()
    }

    /** Customers still in the restaurant: how many a waiter has to seat, order for, or escort. */
    fun customersInside(): List<Customer> = group.members().filter { it.status() != CustomerStatus.LEFT }

    /** Customers who ordered and have not been served yet. */
    fun customersWaitingForFood(): List<Customer> =
        group.members().filter { it.status() == CustomerStatus.ORDERED }

    /** How many customers of the group received their food. */
    fun servedCustomers(): Int = group.members().count { it.servedTick() != null }

    /** Whether at least one customer of the group has received their food. */
    fun anyoneServed(): Boolean = group.members().any { it.servedTick() != null }

    /** Cooked meals that have not been served yet, whether they may go out now or not. */
    fun cookedMeals(): List<Meal> = order?.getMeals().orEmpty().filter { it.status == MealStatus.COOKED }

    /**
     * Cooked meals that may go out this tick. Empty while the table is held back:
     * after the first meal is cooked the table is not served "for this and the
     * following tick unless all other meals have been cooked as well".
     */
    fun servableMeals(tick: Int): List<Meal> {
        val placed = order ?: return emptyList()
        if (firstMealTick == null) return emptyList()
        val allCooked = placed.getMeals().none { it.status == MealStatus.QUEUED || it.status == MealStatus.COOKING }
        val heldBack = !allCooked && inHoldBackWindow(tick)
        return if (heldBack) emptyList() else cookedMeals()
    }

    /** Whether [tick] is the tick of the first cooked meal or the one after it. */
    fun inHoldBackWindow(tick: Int): Boolean {
        val firstMeal = firstMealTick ?: return false
        return tick <= firstMeal + 1
    }

    /**
     * Whether the table still has to be served in one go: still inside the hold-back
     * window, nobody served yet, and the one tick of waiting for capacity not used up.
     */
    fun needsCompleteServing(tick: Int): Boolean =
        inHoldBackWindow(tick) && !anyoneServed() && !waitedForCapacity

    /** Whether the visit is over. */
    fun isFinished(): Boolean = state is GoneState

    /** Records that the rating step has handled this visit. */
    fun markRated() {
        rated = true
    }

    /**
     * How the group experienced the visit (spec, "Rating"). Never seated or ordered, or
     * food missing for anyone: negative. Otherwise, the last meal decides: before the
     * end of the 4-tick expectation window positive, exactly at its end neutral, later
     * negative. A group sent out at the end of opening time that had finished eating
     * sees at least a neutral experience; one that had not, a negative one.
     */
    fun experience(): Experience {
        val placed = orderedTick
        if (placed == null || sentOutFinished == false) return Experience.NEGATIVE
        val servedTicks = group.members().map { it.servedTick() }
        if (servedTicks.any { it == null }) return Experience.NEGATIVE
        val waited = servedTicks.filterNotNull().max() - placed
        val onTime = when {
            waited < EXPECTED_TICKS -> Experience.POSITIVE
            waited == EXPECTED_TICKS -> Experience.NEUTRAL
            else -> Experience.NEGATIVE
        }
        return if (sentOutFinished == true) maxOf(onTime, Experience.NEUTRAL) else onTime
    }

    /** Marks served customers who are done eating now, and counts them for this tick. */
    fun recordFinishedEaters(tick: Int) {
        for (customer in group.members()) {
            if (customer.status() == CustomerStatus.SERVED && customer.isDoneEating(tick)) {
                customer.doneEating()
                finishedEatingThisTick++
            }
        }
    }

    /**
     * [customers] leave without food: their meals are aborted, they leave, and the
     * permanent waiter no longer waits on them.
     */
    fun leaveUnserved(customers: List<Customer>) {
        order?.getMeals().orEmpty().filter { it.customer in customers }.forEach { it.status = MealStatus.ABORTED }
        customers.forEach { it.leave() }
        if (group.groupType() != GroupType.EVENT) {
            waiters.forEach { it.adjustLoad(-customers.size) }
        }
    }

    private companion object {
        /** "After ordering in a restaurant, customers expect food within 4 ticks." */
        const val EXPECTED_TICKS = 4
    }
}
