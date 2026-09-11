package de.unisaarland.cs.se.selab.sharedPackage

import de.unisaarland.cs.se.selab.simulation.ratings.Experience
import de.unisaarland.cs.se.selab.simulation.ratings.Rating

/** A group of customers that visits or orders from restaurants. */
abstract class CustomerGroup(
    val id: Int,
    val groupSize: Int,
    val groupType: GroupType,
    val tableType: TableType,
    val visitingTick: Int,
    val deliveryDistance: Int?,
    val members: List<Customer>,
    val preferences: List<FoodPreference>,
) {
    val history: History = History()

    /** Whether the group visits a restaurant on [evening]. */
    abstract fun visitsOn(evening: Int): Boolean

    /** The id of the restaurant the group is bound to, or null if it browses for one. */
    abstract fun homeRestaurant(): Int?

    /** The rating the group leaves after [experience], or null if it does not rate. */
    abstract fun ratingFor(experience: Experience): Rating?

    /** The dishes and amounts the kitchen should prepare for this group, given [menu]. */
    abstract fun expectedDishes(menu: List<Recipe>): Map<Recipe, Int>

    /** The dish the group forces its members to order at a restaurant of [type], or null. */
    open fun dishOverride(type: RestaurantType): String? = null

    /** Whether every member of the group finds at least one dish it can eat among [dishes]. */
    open fun acceptsAny(dishes: List<Recipe>): Boolean =
        members.all { customer -> customer.preference?.let { pref -> dishes.any(pref::accepts) } ?: true }

    /** Whether the group orders a delivery instead of visiting in person. */
    open fun isDelivery(): Boolean = (deliveryDistance ?: 0) > 0

    /** Whether the group has stopped visiting restaurants for good. */
    open fun hasGivenUp(): Boolean = false
}
