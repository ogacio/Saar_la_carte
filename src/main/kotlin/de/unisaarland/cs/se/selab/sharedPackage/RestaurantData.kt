package de.unisaarland.cs.se.selab.sharedPackage
import de.unisaarland.cs.se.selab.sharedPackage.CustomerGroup
import de.unisaarland.cs.se.selab.sharedPackage.RegularCustomerGroup
import de.unisaarland.cs.se.selab.sharedPackage.CasualCustomerGroup
import de.unisaarland.cs.se.selab.sharedPackage.EventCustomerGroup
data class RestaurantData(private val id: Int, private val type: RestaurantType, private val openingTick: Int, private val closingTick: Int,
                          private val dishes: MutableList<Recipe>, private var freeSeats: MutableMap<TableType, Int>, private var freeDrivers: Int,
                          private val hostsEvents: Boolean, private val totalSeats:Int, private val eventSeatsBooked: MutableMap<Int, Int>) {

    fun take(g: CustomerGroup): Unit {
        if (g.isDelivery()){
            require(freeDrivers > 0) {
                "Available drivers should have been checked before take()"
            }
            freeDrivers--
        }else {
            val availableSeats = freeSeats[g.getTableType()] ?: 0
            require(g.getGroupSize() <= availableSeats) {
                "Available seats should have been checked before take()"
            }
            freeSeats[g.getTableType()] = availableSeats - g.getGroupSize()
        }
    }
    fun takeForEvent(g: CustomerGroup, evening: Int):Unit{
        eventSeatsBooked[evening] = (eventSeatsBooked[evening] ?: 0) + g.getGroupSize()
    }
    fun openAt(tick: Int): Boolean {
        return openingTick <= tick && closingTick >= tick
    }
    fun eventSeatsLeft(evening: Int): Int {
        return totalSeats-(eventSeatsBooked[evening]?:0)
    }
    fun getFreeSeats(): Map<TableType, Int> = freeSeats
    fun getFreeDrivers() = freeDrivers
    fun getId() = id
    fun getType() = type
    fun getOpeningTick() = openingTick
    fun getClosingTick() = closingTick
    fun getHostsEvents() = hostsEvents
    fun getDishes() = dishes
}