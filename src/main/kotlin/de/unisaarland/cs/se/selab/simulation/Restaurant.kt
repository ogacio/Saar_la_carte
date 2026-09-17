package de.unisaarland.cs.se.selab.simulation
import de.unisaarland.cs.se.selab.foh.FrontOfTheHouse
import de.unisaarland.cs.se.selab.kitchen.CookType
import de.unisaarland.cs.se.selab.kitchen.Kitchen
import de.unisaarland.cs.se.selab.logging.Logger
import de.unisaarland.cs.se.selab.sharedPackage.Menu
import de.unisaarland.cs.se.selab.sharedPackage.Pantry
import de.unisaarland.cs.se.selab.sharedPackage.RestaurantData
import de.unisaarland.cs.se.selab.sharedPackage.StaffType
import de.unisaarland.cs.se.selab.sharedPackage.customers.CustomerGroup

/**
 * the restaurant class controls the foh, kitchen, pantry... and it is responsible to prepare everything
 * before the evening starts, run every tick, then to close the evening, take care of staff change incident
 */
class Restaurant(
    val name: String,
    val hostsEvents: Boolean,
    val initialPositiveRatings: Int,
    val initialNegativeRatings: Int,
    private val foh: FrontOfTheHouse,
    private val kitchen: Kitchen,
    private val pantry: Pantry,
    private val menu: Menu,
    private val data: RestaurantData
) {
    val clock = GlobalClock
    val openingTick = data.getOpeningTick()
    val closingTick = data.getClosingTick()
    val type = data.getType()
    val id = data.getId()

    /**
     prepares the kitchen and the front of the house at the preparation phase
     */
    fun prepare(regulars: MutableList<CustomerGroup>) {
        var regularsSeats = 0
        for (r in regulars) {
            regularsSeats += r.getGroupSize()
        }
        kitchen.planEvening(regulars, data.getTotalSeats() - regularsSeats, menu)
        foh.prepareEvening(clock.getEvening(), regulars)
    }

    /**
     creates the subunits that are going to be passed on
     */
    fun subunit(): SubUnits {
        return SubUnits(id, menu, pantry, kitchen)
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
    fun snapshot(): RestaurantData {
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

    /**
     * tries to book an event, returns if it was successful or not
     */
    fun bookEvent(g: CustomerGroup, evening: Int): Boolean {
        return foh.bookEvent(g, evening)
    }

    /**
     implements the staff change incident IF it is cook or waitstaff
     */
    fun changeStaff(type: StaffType, cook: CookType?, delta: Int) {
        if (type == StaffType.COOK) kitchen.changeStaff(cook!!, delta)
        if (type == StaffType.WAITSTAFF) foh.getWaitstaff().changeStaff(delta)
    }

    /**
     * returns if the restaurant is open or not
     */
    fun isOpen(): Boolean {
        return clock.getTickInEvening() in openingTick..closingTick
    }

    /**
     * One tick of this restaurant, steps 1 to 7 in the spec's order, between "Restaurant Start" and "Restaurant End".
     * [arrivals] are the groups arriving now. At the closing tick everyone still inside is sent out before rating.
     */
    fun runRestaurantTick(arrivals: List<CustomerGroup>, tick: Int) {
        Logger.restaurantStart(id)
        foh.beginTick()
        foh.callSeatingAndOrdering(arrivals)
        kitchen.cook()
        foh.callServingService()
        foh.callDeliveryDesk()
        foh.callDiningService()
        foh.callEscortingService()
        if (tick == closingTick) foh.closeOpeningTime()
        foh.callRatingService()
        Logger.restaurantEnd(id)
    }

    /**
     * returns the front of the house
     */
    fun getFoh(): FrontOfTheHouse = foh

    /**
     * returns the kitchen
     */
    fun getKitchen(): Kitchen = kitchen

    /**
     * returns the pantry
     */
    fun getPantry(): Pantry = pantry

    /**
     * returns the restaurant id
     */
    fun getId(): Int = id

    /**
     * returns the menu
     */
    fun getMenu(): Menu = menu
}
