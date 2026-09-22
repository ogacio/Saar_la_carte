package de.unisaarland.cs.se.selab.foh

/** "all waiter with a current load below 10, as they do not have much to do yet" */
private const val BUSY_LOAD = 10

/**
 * Decides which waiter serves which customer group.
 *
 * REGULAR and CASUAL groups get one permanent waiter who stays responsible for every further action
 * of that group. EVENT groups have no permanent waiter: the event manager spreads the work over as
 * many waiters as the action needs, and the attempt only counts if the whole group is covered
 * within one tick.
 */
class WaiterAssignmentService(
    private val waitstaff: MutableList<Waiter>,
) {
    private var nextId = 1

    /**
     * How many actions of type [action] the whole waitstaff can still perform in this tick.
     */
    fun capacity(action: ActionType): Int = waitstaff.sumOf { it.remaining(action) }

    /**
     * Picks the permanent waiter for a group of [groupSize] customers, or null if none is free.
     *
     * Preference goes to the waiter with the most customers among those that are not busy yet, so
     * that the others keep the capacity to seat larger groups; once everybody is busy the least
     * loaded waiter balances the load. The chosen waiter receives its id if it does not have one.
     */
    fun assignPermanent(groupSize: Int): Waiter? {
        // Rule 1: only waiters whose SEATING tick load stays within the limit with this group.
        val eligible = waitstaff.filter { it.remaining(ActionType.SEATING) >= groupSize }
        // Rule 2: among the waiters with a current load below 10, the one with the most customers.
        val notBusy = eligible.filter { it.currentLoad < BUSY_LOAD }
        val chosen = if (notBusy.isNotEmpty()) {
            notBusy.minWithOrNull(compareByDescending<Waiter> { it.currentLoad }.thenBy { idOrLast(it) })
        } else {
            // Rule 3: everybody is busy, so the one with the least customers balances the load.
            eligible.minWithOrNull(compareBy<Waiter> { it.currentLoad }.thenBy { idOrLast(it) })
        }
        chosen?.let { grantId(it) }
        return chosen
    }

    /**
     * Spreads [groupSize] customers of an EVENT group over the waiters for [action].
     *
     * Returns how many customers each waiter takes over, or null if the waitstaff cannot cover the
     * whole group in this tick. The allocation is only planned here, the caller books it with
     * [Waiter.consume] once it decided to carry it out.
     */
    fun assignEvent(
        groupSize: Int,
        action: ActionType,
        cookedMeals: Map<Waiter, Int> = emptyMap(),
    ): Map<Waiter, Int>? {
        val candidates = waitstaff
            .filter { it.remaining(action) > 0 }
            .sortedWith(eventPriority(action, cookedMeals))
        val plan = linkedMapOf<Waiter, Int>()
        var left = groupSize
        for (waiter in candidates) {
            if (left == 0) break
            val share = minOf(left, waiter.remaining(action))
            plan[waiter] = share
            left -= share
        }
        if (left > 0) return null
        plan.keys.forEach { grantId(it) }
        return plan
    }

    /**
     * Hands the orders of an EVENT group to the waiters who seated it (forum thread 266).
     *
     * Customers are seated and order in the same fixed sequence, so every waiter owns the next block
     * of that sequence, as large as the number of customers it seated in [seatingPlan] (in seating
     * order). [placed] tells for each position of the ordering sequence whether that customer placed
     * an order. A customer who found no dish leaves an empty slot; orders never move to another
     * waiter. Returns the orders each waiter takes, in seating order; a waiter whose customers all
     * failed keeps an entry of 0. Nothing is booked here, the caller does that with [Waiter.consume].
     */
    fun assignEventOrders(seatingPlan: Map<Waiter, Int>, placed: List<Boolean>): Map<Waiter, Int> {
        val orders = linkedMapOf<Waiter, Int>()
        var position = 0
        for ((waiter, seated) in seatingPlan) {
            val block = placed.subList(minOf(position, placed.size), minOf(position + seated, placed.size))
            orders[waiter] = block.count { it }
            position += seated
        }
        return orders
    }

    /**
     * The waiter the event manager would pick first for [action], ignoring capacity. Nothing is
     * planned, but the waiter receives its id: the specification demands that the No Serving line of
     * an EVENT table names "the first that would have served", which is only possible with an id.
     */
    fun currentEventWaiter(action: ActionType, cookedMeals: Map<Waiter, Int> = emptyMap()): Waiter? {
        val waiter = waitstaff.sortedWith(eventPriority(action, cookedMeals)).firstOrNull()
        waiter?.let { grantId(it) }
        return waiter
    }

    /**
     * The waiter who carries the next meals to a delivery driver: "The waiters with the lowest id
     * starts". Only waiters with SERVING actions left count; those without an id come last and
     * receive one now. Null if nobody can serve anymore this tick.
     */
    fun nextServingWaiter(): Waiter? {
        val waiter = waitstaff
            .filter { it.remaining(ActionType.SERVING) > 0 }
            .minByOrNull { idOrLast(it) }
        waiter?.let { grantId(waiter) }
        return waiter
    }

    /**
     * Starts a new tick for every waiter.
     */
    fun beginTick() = waitstaff.forEach { it.beginTick() }

    /**
     * Applies a staff change of [delta] members; the waitstaff never drops below zero members.
     *
     * New members join without an id and receive one at their first action. When members leave, the
     * ones that have not acted this evening go first, then the highest ids.
     */
    fun changeStaff(delta: Int) {
        if (delta > 0) {
            repeat(delta) { waitstaff.add(Waiter()) }
            return
        }
        repeat(minOf(-delta, waitstaff.size)) {
            val leaving = waitstaff
                .sortedWith(compareByDescending { it.id ?: Int.MAX_VALUE })
                .first()
            waitstaff.remove(leaving)
        }
    }

    /**
     * Drops the ids and loads of every waiter at the end of an evening.
     */
    fun resetEvening() {
        waitstaff.forEach { it.resetEvening() }
        nextId = 1
    }

    /**
     * Gives [waiter] its id for this evening if it is about to act for the first time.
     */
    private fun grantId(waiter: Waiter) {
        if (waiter.id == null) {
            waiter.id = nextId
            nextId++
        }
    }

    /**
     * The waiter's id for tie-breaking. Waiters without an id sort last: they would receive a higher
     * id than everybody who already acted.
     */
    private fun idOrLast(waiter: Waiter): Int = waiter.id ?: Int.MAX_VALUE

    /**
     * The order in which the event manager uses the waiters for [action]; ties go to the lowest id.
     */
    private fun eventPriority(action: ActionType, cookedMeals: Map<Waiter, Int>): Comparator<Waiter> {
        val first = when (action) {
            // "the manager prioritizes the waiters in descending order of the current load to perform the SEATING"
            // ORDERING uses the same order: the waiters who seated an EVENT group take its orders, in the
            // order they seated it (forum thread 266).
            ActionType.SEATING, ActionType.ORDERING -> compareByDescending { it.currentLoad }
            // "the manager prioritizes the waiters in descending number of cooked meals in the kitchen
            // that belong to their assigned tables"
            ActionType.SERVING -> compareByDescending { cookedMeals[it] ?: 0 }
            // "For EVENT groups, the waitstaff manager prioritizes waiters with the lowest current load."
            ActionType.ESCORTING -> compareBy<Waiter> { it.currentLoad }
        }
        return first.thenBy { idOrLast(it) }
    }
}
