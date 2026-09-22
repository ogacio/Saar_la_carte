package de.unisaarland.cs.se.selab.sharedPackage.customers

import de.unisaarland.cs.se.selab.sharedPackage.Recipe

/** The last three visits of a regular customer group. */
class History {
    private val visits = ArrayDeque<List<Recipe>>()

    /** Records the dishes ordered on the latest visit, dropping the oldest if now over three. */
    fun shiftAndPutNew(dishes: List<Recipe>) {
        visits.addLast(dishes)
        if (visits.size > MAX_VISITS) {
            visits.removeFirst()
        }
    }

    /** The dishes of the last three visits, oldest first, at most three entries. */
    fun getLastThree(): List<List<Recipe>> = visits.toList()

    /** Whether the group has visited the restaurant at least once. */
    fun isKnown(): Boolean = visits.isNotEmpty()

    /** Whether any remembered visit produced at least one dish (forum 328). */
    fun hasOrdered(): Boolean = visits.any { it.isNotEmpty() }

    private companion object {
        const val MAX_VISITS = 3
    }
}
