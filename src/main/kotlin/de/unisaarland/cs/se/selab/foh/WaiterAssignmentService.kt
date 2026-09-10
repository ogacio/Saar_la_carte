package de.unisaarland.cs.se.selab.foh

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
     * Picks the permanent waiter for a group of [groupSize] customers, or null if none is free.
     *
     * Preference goes to the waiter with the most customers among those that are not busy yet, so
     * that the others keep the capacity to seat larger groups; once everybody is busy the least
     * loaded waiter balances the load. The chosen waiter receives its id if it does not have one.
     */



    /**
     * Spreads [groupSize] customers of an EVENT group over the waiters for [action].
     *
     * Returns how many customers each waiter takes over, or null if the waitstaff cannot cover the
     * whole group in this tick. The allocation is only planned here, the caller books it with
     * [Waiter.consume] once it decided to carry it out.
     */



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
}
