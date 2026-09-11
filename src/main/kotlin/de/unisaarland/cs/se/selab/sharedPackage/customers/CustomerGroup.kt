package de.unisaarland.cs.se.selab.sharedPackage.customers

import de.unisaarland.cs.se.selab.sharedPackage.Recipe
import de.unisaarland.cs.se.selab.sharedPackage.RestaurantType
import de.unisaarland.cs.se.selab.sharedPackage.TableType
import de.unisaarland.cs.se.selab.simulation.ratings.Experience
import de.unisaarland.cs.se.selab.simulation.ratings.Rating

/** A group of customers that visits or orders from restaurants. */
abstract class CustomerGroup(
    private val id: Int,
    private val groupSize: Int,
    private val groupType: GroupType,
    private val tableType: TableType,
    private val visitingTick: Int,
    private val deliveryDistance: Int?,
    private val members: List<Customer>,
    private val preferences: List<FoodPreference>,
) {
    private val history: History = History()

    /** The unique identifier of this customer group. */
    fun id(): Int = id

    /** The number of customers in this group. */
    protected fun groupSize(): Int = groupSize

    /** Whether this group is REGULAR, CASUAL or an EVENT. */
    fun groupType(): GroupType = groupType

    /** The type of table this group wants. */
    fun tableType(): TableType = tableType

    /** The tick within an evening when this group wants to get food from a restaurant. */
    fun visitingTick(): Int = visitingTick

    /** The food preferences declared for this group. */
    fun preferences(): List<FoodPreference> = preferences

    /** The last three visits of this group, if it has visited before. */
    protected fun history(): History = history

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
        members.all { customer -> customer.preference()?.let { pref -> dishes.any(pref::accepts) } ?: true }

    /** Whether the group orders a delivery instead of visiting in person. */
    open fun isDelivery(): Boolean = (deliveryDistance ?: 0) > 0

    /** Whether the group has stopped visiting restaurants for good. */
    open fun hasGivenUp(): Boolean = false
}
