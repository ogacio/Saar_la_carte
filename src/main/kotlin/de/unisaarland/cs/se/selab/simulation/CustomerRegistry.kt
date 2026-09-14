package de.unisaarland.cs.se.selab.simulation

import de.unisaarland.cs.se.selab.sharedPackage.customers.CustomerGroup
import de.unisaarland.cs.se.selab.sharedPackage.customers.EventCustomerGroup
import de.unisaarland.cs.se.selab.sharedPackage.customers.GroupType

/** Holds every customer group and answers who is due to act on a given evening and tick. */
class CustomerRegistry(
    private val groups: MutableList<CustomerGroup>,
) {

    /** The REGULAR groups that visit restaurant [restaurantId] on [evening]. */
    fun regularsFor(restaurantId: Int, evening: Int): MutableList<CustomerGroup> =
        groups.filter {
            it.groupType() == GroupType.REGULAR && it.homeRestaurant() == restaurantId && it.visitsOn(evening)
        }.toMutableList()

    /** The groups due to decide on a restaurant this [evening]/[tick], EVENT first, then CASUAL, then by id. */
    fun deciding(evening: Int, tick: Int): MutableList<CustomerGroup> =
        groups.filter { isDeciding(it, evening, tick) }
            .sortedWith(compareBy({ it.groupType() != GroupType.EVENT }, CustomerGroup::id))
            .toMutableList()

    private fun isDeciding(group: CustomerGroup, evening: Int, tick: Int): Boolean = when (group.groupType()) {
        GroupType.REGULAR -> false
        GroupType.EVENT -> tick == 1 && group is EventCustomerGroup && group.booksOn(evening)
        GroupType.CASUAL -> isCasualDeciding(group, evening, tick)
    }

    private fun isCasualDeciding(group: CustomerGroup, evening: Int, tick: Int): Boolean {
        if (!group.visitsOn(evening)) return false
        val distance = group.deliveryDistance() ?: 0
        val decisionTick = if (distance > 0) {
            group.visitingTick() - DeliveryService().calculateTravelTicks(distance) - DELIVERY_COOKING_TICKS
        } else {
            group.visitingTick()
        }
        return tick == decisionTick
    }

    private companion object {
        const val DELIVERY_COOKING_TICKS = 3
    }
}
