package de.unisaarland.cs.se.selab.simulation
import de.unisaarland.cs.se.selab.foh.FrontOfTheHouse
import de.unisaarland.cs.se.selab.kitchen.CookType
import de.unisaarland.cs.se.selab.kitchen.Kitchen
import de.unisaarland.cs.se.selab.sharedPackage.Menu
import de.unisaarland.cs.se.selab.sharedPackage.Pantry
import de.unisaarland.cs.se.selab.sharedPackage.RestaurantData
import de.unisaarland.cs.se.selab.sharedPackage.RestaurantType
import de.unisaarland.cs.se.selab.sharedPackage.StaffType
import de.unisaarland.cs.se.selab.sharedPackage.customers.CustomerGroup

class Restaurant (
    val id : Int,
    val name : String,
    val type : RestaurantType,
    val openingTick : Int,
    val closingTick : Int,
    val hostsEvents : Boolean,
    val initialPositiveRatings : Int,
    val initialNegativeRatings: Int,
    val foh : FrontOfTheHouse,
    val kitchen : Kitchen,
    val pantry : Pantry,
    val menu : Menu,
    val clock : GlobalClock,
    val data : RestaurantData
)
{
    /**
    prepares the kitchen and the front of the house at the preparation phase
     */
    fun prepare(regulars : MutableList<CustomerGroup>, supplier : Supplier) {
        var regularsSeats : Int = 0
        for (r in regulars) {
            regularsSeats += r.getGroupSize()
        }
        kitchen.planEvening(regulars, data.getTotalSeats()-regularsSeats,supplier)
        foh.prepareEvening(clock.getEvening(), regulars)
    }

    /**
    creates the subunits that are going to be passed on
     */
    fun subunit() : SubUnits {
        return SubUnits(id,menu, pantry,kitchen)
    }

    /**
    closes evening -> kitchen, foh, pantry
     */
    fun closeEvening() {
        kitchen.closeEvening()
        foh.closeEvening()
        pantry.discardEvening()
    }

    /**
    returns the RestaurantData of the restaurant
-     */
    fun snapshot() : RestaurantData {
        return RestaurantData(
            id,
            type,
            openingTick,
            closingTick,
            menu.getRecipes(),
            foh.getTables().freeSeats(),
            foh.getDeliveryDesk().amountFreeDrivers(),
            hostsEvents,
            foh.getTables().totalSeats(),
            foh.getReservationBook().getEventSeatsBooked()
            )
    }

    fun bookEvent(g : CustomerGroup, evening : Int) : Boolean {
        return foh.bookEvent(g, evening)
    }

    /**
    implements the staff change incident IF it is cook or waitstaff
     */
    fun changeStaff(type : StaffType, cook : CookType?, delta : Int) {
        if (type == StaffType.COOK) kitchen.changeStaff(cook!!, delta)
        if (type == StaffType.WAITSTAFF) foh.getWaitstaff().changeStaff(delta)
    }

    fun isOpen(tick: Int) : Boolean {
        return tick in openingTick..closingTick
    }
    fun getFoh(): FrontOfTheHouse = foh
    fun getKitchen() : Kitchen = kitchen
    fun getPantry() : Pantry = pantry
    fun getId(): Int = id
    fun getMenu(): Menu = menu
}