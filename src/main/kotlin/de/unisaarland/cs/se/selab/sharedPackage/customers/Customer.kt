package de.unisaarland.cs.se.selab.sharedPackage.customers

import de.unisaarland.cs.se.selab.sharedPackage.Meal
import de.unisaarland.cs.se.selab.sharedPackage.Recipe

private const val EATING_TICKS = 2

/** One customer inside a group, following its subgroup's food preference if it has one. */
class Customer(private val preference: FoodPreference?) {

    // set by OrderingService once F18 exists; no writer today
    private var chosenDish: Recipe? = null
    private var meal: Meal? = null
    private var servedTick: Int? = null
    private var status: CustomerStatus = CustomerStatus.ORDERED

    /** The food preference this customer follows, or null if it has none. */
    fun preference(): FoodPreference? = preference

    /** The dish this customer decided on, null until it has ordered. */
    fun chosenDish(): Recipe? = chosenDish

    /** The meal this customer was served, null until it has received one. */
    fun meal(): Meal? = meal

    /** The tick this customer was served its meal, null until then. */
    fun servedTick(): Int? = servedTick

    /** The stage of this customer's visit. */
    fun status(): CustomerStatus = status

    /** Records that this customer received [m] at [tick]. */
    fun receive(m: Meal, tick: Int) {
        meal = m
        servedTick = tick
        status = CustomerStatus.SERVED
    }

    /** Whether this customer has finished eating by [tick] (2 full ticks after being served). */
    fun isDoneEating(tick: Int): Boolean {
        val served = servedTick ?: return false
        return tick - served >= EATING_TICKS
    }

    /** Records that this customer has left the restaurant. */
    fun leave() {
        status = CustomerStatus.LEFT
    }

    /** Set the status to DONE_EATING */
    fun doneEating() {
        status = CustomerStatus.DONE_EATING
    }

    /** */
}
