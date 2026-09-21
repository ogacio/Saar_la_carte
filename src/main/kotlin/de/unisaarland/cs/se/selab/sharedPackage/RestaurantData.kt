package de.unisaarland.cs.se.selab.sharedPackage
import de.unisaarland.cs.se.selab.sharedPackage.customers.CustomerGroup

/**
 * Contains the static and dynamic restaurant information required
 * by the browsing service, including restaurant properties,
 * available seats, delivery drivers, dishes, and event reservations.
 */
data class RestaurantData(
    private val id: Int,
    private val type: RestaurantType,
    private val openingTick: Int,
    private val closingTick: Int,
    private val dishes: List<Recipe>,
    private var freeSeatsStatic: Map<TableType, Int>,
    private var freeDrivers: Int,
    private val hostsEvents: Boolean,
    private val totalSeats: Int,
    private val eventSeatsBookedStatic: Map<Int, Int>
) {
    private val freeSeats = freeSeatsStatic.toMutableMap()
    private val eventSeatsBooked = eventSeatsBookedStatic.toMutableMap()

    /**
     * updates RestaurantData upon regular customer decision
     */
    fun take(g: CustomerGroup) {
        if (g.isDelivery()) {
            require(freeDrivers > 0) {
                "Available drivers should have been checked before take()"
            }
            freeDrivers--
        } else {
            val availableSeats = freeSeats[g.tableType()] ?: 0
            require(g.groupSize() <= availableSeats) {
                "Available seats should have been checked before take()"
            }
            freeSeats[g.tableType()] = availableSeats - g.groupSize()
        }
    }

    /**
     * updates RestaurantData upon event customer decision
     */
    fun takeForEvent(g: CustomerGroup) {
        val evening = g.getEventEvening()
        eventSeatsBooked[evening] = (eventSeatsBooked[evening] ?: 0) + g.groupSize()
    }

    /**
     * bool
     */
    fun openAt(tick: Int): Boolean {
        return openingTick <= tick && closingTick >= tick
    }

    /**
     * bool
     */
    fun acceptsNewCustomersAt(tick: Int): Boolean {
        return openingTick <= tick &&
            tick <= closingTick - 3
    }

    /**
     * returns number of seats left available for an event
     */
    fun eventSeatsLeft(evening: Int): Int {
        return totalSeats - (eventSeatsBooked[evening] ?: 0)
    }

    /**
     * getter
     */
    fun getFreeSeats(): Map<TableType, Int> = freeSeats.toMap()

    /**
     * getter
     */
    fun getFreeDrivers() = freeDrivers

    /**
     * getter
     */
    fun getId() = id

    /**
     * getter
     */
    fun getType() = type

    /**
     * getter
     */
    fun getOpeningTick() = openingTick

    /**
     * getter
     */
    fun getClosingTick() = closingTick

    /**
     * getter
     */
    fun getHostsEvents() = hostsEvents

    /**
     * getter
     */
    fun getDishes() = dishes

    /**
     * getter
     */
    fun getTotalSeats() = totalSeats
}
