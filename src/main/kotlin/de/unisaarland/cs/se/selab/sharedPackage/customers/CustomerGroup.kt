package de.unisaarland.cs.se.selab.sharedPackage.customers

import de.unisaarland.cs.se.selab.sharedPackage.Order
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
    private val preferences: List<FoodPreference>
) {
    private val history: History = History()

    /** The unique identifier of this customer group. */
    fun id(): Int = id

    /** The number of customers in this group. */
    fun groupSize(): Int = groupSize

    /** Whether this group is REGULAR, CASUAL or an EVENT. */
    fun groupType(): GroupType = groupType

    /** The type of table this group wants. */
    fun tableType(): TableType = tableType

    /** The tick within an evening when this group wants to get food from a restaurant. */
    fun visitingTick(): Int = visitingTick

    /** The delivery distance in kilometers, or null if the group never orders delivery. */
    fun deliveryDistance(): Int? = deliveryDistance

    /** Customers in group */
    fun members(): List<Customer> = members

    /** The food preferences declared for this group. */
    fun preferences(): List<FoodPreference> = preferences

    /** The last three visits of this group, if it has visited before. */
    protected fun history(): History = history

    /** Whether a remembered visit of this group produced dishes the kitchen can plan from. */
    fun hasOrderedBefore(): Boolean = history.hasOrdered()

    /** The evening of this group's event, or 0 if it has none. */
    abstract fun getEventEvening(): Int

    /** The delivery distance in kilometers, or 0 if the group never orders delivery. */
    abstract fun getDeliveryDistance(): Int

    /** Whether the group visits a restaurant on [evening]. */
    abstract fun visitsOn(evening: Int): Boolean

    /** The restaurant types this group is willing to visit. */
    abstract fun restaurantTypes(): Set<RestaurantType>

    /** The id of the restaurant the group is bound to, or null if it browses for one. */
    abstract fun homeRestaurant(): Int?

    /** The rating the group leaves after [experience], or null if it does not rate. */
    abstract fun ratingFor(experience: Experience): Rating?

    /** The dishes and amounts the kitchen should prepare for this group, given [menu] at a [type] restaurant. */
    abstract fun expectedDishes(menu: List<Recipe>, type: RestaurantType): Map<Recipe, Int>

    /** The dish the group forces its members to order at a restaurant of [type], or null. */
    open fun dishOverride(type: RestaurantType): String? = null

    /** Whether every member of the group finds at least one dish it can eat among [dishes]. */
    open fun acceptsAny(dishes: List<Recipe>): Boolean =
        members.all { customer -> customer.preference()?.let { pref -> dishes.any(pref::accepts) } ?: true }

    /** Whether the group orders a delivery instead of visiting in person. */
    open fun isDelivery(): Boolean = (deliveryDistance ?: 0) > 0

    /** Whether the group has stopped visiting restaurants for good. */
    open fun hasGivenUp(): Boolean = false

    /** Marks that a delivery order was placed this evening and is now awaited. */
    open fun orderPlaced() = Unit

    /** Marks a placed delivery order as resolved, whether delivered or given up on. */
    open fun orderResolved() = Unit

    /** Whether an unresolved delivery has exceeded the group's waiting deadline this tick. */
    open fun deliveryGiveUpDue(): Boolean = false

    /** Records that this delivery group has actually given up waiting for its current order. */
    open fun markDeliveryGivenUp() = Unit

    /** Whether the current delivery was previously given up on, even after its rating was handled. */
    open fun deliveryWasGivenUp(): Boolean = false

    /** Records the dishes of a completed order as this group's latest visit. */
    open fun recordVisit(evening: Int, o: Order) {
        history.shiftAndPutNew(o.getMeals().map { it.recipe })
    }

    /** Records whether a visit succeeded or failed, for groups that track consecutive failures. */
    open fun recordOutcome(e: Experience) = Unit

    /** The number of customers in this group, for callers that use the getter style. */
    fun getGroupSize() = groupSize
}
