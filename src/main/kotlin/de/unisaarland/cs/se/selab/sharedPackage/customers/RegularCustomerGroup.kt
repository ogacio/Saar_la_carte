package de.unisaarland.cs.se.selab.sharedPackage.customers

import de.unisaarland.cs.se.selab.sharedPackage.Recipe
import de.unisaarland.cs.se.selab.sharedPackage.RestaurantType
import de.unisaarland.cs.se.selab.sharedPackage.TableType
import de.unisaarland.cs.se.selab.simulation.ratings.Experience
import de.unisaarland.cs.se.selab.simulation.ratings.Rating

/** A group that visits one fixed restaurant on a fixed schedule. */
class RegularCustomerGroup(
    id: Int,
    groupSize: Int,
    tableType: TableType,
    visitingTick: Int,
    members: List<Customer>,
    preferences: List<FoodPreference>,
    private val visitingStart: Int,
    private val visitingPeriod: Int,
    private val restaurantId: Int,
) : CustomerGroup(id, groupSize, GroupType.REGULAR, tableType, visitingTick, null, members, preferences) {

    private var failedAttempts = 0

    /** The first evening this group visits the restaurant. */
    fun visitingStart(): Int = visitingStart

    /** The number of evenings between two visits of this group. */
    fun visitingPeriod(): Int = visitingPeriod

    /** The id of the restaurant this group visits periodically. */
    fun restaurantId(): Int = restaurantId

    /** The number of consecutive failed attempts this group has had so far. */
    fun failedAttempts(): Int = failedAttempts

    override fun visitsOn(evening: Int): Boolean =
        !hasGivenUp() &&
            evening >= visitingStart &&
            (evening - visitingStart) % visitingPeriod == 0

    /** REGULAR groups are bound to one fixed restaurant and never browse by type. */
    override fun restaurantTypes(): Set<RestaurantType> = emptySet()

    override fun homeRestaurant(): Int = restaurantId

    /** Every regular group rates every visit; a negative experience gives a negative rating. */
    override fun ratingFor(experience: Experience): Rating =
        if (experience == Experience.NEGATIVE) Rating.NEGATIVE else Rating.POSITIVE

    /** The dishes of the group's last three visits, counted once per member that ordered it. */
    override fun expectedDishes(menu: List<Recipe>): Map<Recipe, Int> {
        val counts = mutableMapOf<Recipe, Int>()
        for (visit in history().getLastThree()) {
            for (dish in visit) {
                if (menu.contains(dish)) {
                    counts[dish] = (counts[dish] ?: 0) + 1
                }
            }
        }
        return counts
    }

    override fun isDelivery(): Boolean = false

    override fun hasGivenUp(): Boolean = failedAttempts >= FAILURE_LIMIT

    /** Records a failed reservation, seating failure, or the whole group leaving unserved. */
    fun recordFailedAttempt() {
        failedAttempts++
    }

    /** Resets the failure counter after a successful visit. */
    fun recordSuccessfulVisit() {
        failedAttempts = 0
    }

    /** A negative experience counts as a failed attempt; anything else resets the streak. */
    override fun recordOutcome(e: Experience) {
        if (e == Experience.NEGATIVE) recordFailedAttempt() else recordSuccessfulVisit()
    }

    private companion object {
        const val FAILURE_LIMIT = 2
    }
}
