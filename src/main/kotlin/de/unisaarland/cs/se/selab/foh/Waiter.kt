package de.unisaarland.cs.se.selab.foh

/**
 * One member of the waitstaff.
 *
 * Waiters are interchangeable and only receive an id when they perform their first action of an
 * evening, which is why [id] is null until then and is dropped again by [resetEvening]. The current
 * load counts the customers the waiter is waiting on, the tick load counts the actions of one type
 * performed in the running tick.
 */
class Waiter {
    /** The id granted at the first action of the evening, null before that. */
    var id: Int? = null

    /** The number of customers this waiter is currently waiting on; EVENT groups do not count. */
    var currentLoad: Int = 0
        private set

    private val tickLoad: MutableMap<ActionType, Int> = mutableMapOf()

    /**
     * How many further actions of type [action] the waiter may perform in this tick.
     */
    fun remaining(action: ActionType): Int = ACTION_LIMIT - tickLoad.getOrDefault(action, 0)

    /**
     * Books [count] actions of type [action] against the limit of this tick.
     */
    fun consume(action: ActionType, count: Int) {
        tickLoad[action] = tickLoad.getOrDefault(action, 0) + count
    }

    /**
     * Adds [delta] customers to the current load, which never drops below zero.
     */
    fun adjustLoad(delta: Int) {
        currentLoad = (currentLoad + delta).coerceAtLeast(0)
    }

    /**
     * Clears the per tick action counters at the start of a new tick.
     */
    fun beginTick() = tickLoad.clear()

    /**
     * Drops the id, the current load and the tick load at the end of an evening.
     */
    fun resetEvening() {
        id = null
        currentLoad = 0
        tickLoad.clear()
    }

    /** The action limit every waiter has per action type and tick. */
    companion object {
        const val ACTION_LIMIT = 10
    }
}
