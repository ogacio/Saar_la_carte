package de.unisaarland.cs.se.selab.simulation
import de.unisaarland.cs.se.selab.foh.FrontOfTheHouse
import de.unisaarland.cs.se.selab.foh.TableStatus
import de.unisaarland.cs.se.selab.kitchen.CookType
import de.unisaarland.cs.se.selab.kitchen.Kitchen
import de.unisaarland.cs.se.selab.logging.Logger
import de.unisaarland.cs.se.selab.sharedPackage.Menu
import de.unisaarland.cs.se.selab.sharedPackage.Pantry
import de.unisaarland.cs.se.selab.sharedPackage.RestaurantData
import de.unisaarland.cs.se.selab.sharedPackage.StaffType
import de.unisaarland.cs.se.selab.sharedPackage.customers.CustomerGroup
import de.unisaarland.cs.se.selab.sharedPackage.customers.GroupType

/** "A restaurant does not accept new customers in the last 3 ticks of their opening time." */
private const val LAST_TICKS_CLOSED = 3

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
    private val id = data.getId()

    /**
     prepares the kitchen and the front of the house at the preparation phase
     */
    fun prepare(regulars: MutableList<CustomerGroup>) {
        foh.prepareEvening(clock.getEvening(), regulars)
        val book = foh.getReservationBook()
        val reserved = book.reservedGroupsTonight(clock.getEvening(), regulars)
        // Forum 328: only groups the kitchen can predict are planned for. A regular that has never
        // ordered here yet is guessed at like a free table instead.
        val planned = reserved.filter { it.groupType() == GroupType.EVENT || it.hasOrderedBefore() }
        val plannedIds = planned.map { it.id() }.toSet()
        val reservedButUnplanned = book.tablesTonight()
            .filterKeys { it !in plannedIds }.values.sumOf { it.size }
        val freeSeats = foh.getTables().getTables()
            .filter { it.status == TableStatus.FREE }.sumOf { it.size }
        kitchen.planEvening(planned.toMutableList(), reservedButUnplanned + freeSeats, menu)
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
        if (type == StaffType.COOK) kitchen.changeStaff(checkNotNull(cook), delta)
        if (type == StaffType.WAITSTAFF) foh.getWaitstaff().changeStaff(delta)
        if (type == StaffType.DRIVER) DeliveryService.changeStaff(this.getId(), delta)
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
     *
     * After the opening time "the kitchen stops working" and the front of house is cleaned, so only the
     * drivers keep working: "only deliveries already given to a driver continue after the opening time" —
     * so delivery hand-off (step 4) and rating (step 7, which a delivery resolving this tick needs) both
     * run regardless of [open]; dine-in eating/escorting do not, since no dine-in visit survives past closing.
     */
    fun runRestaurantTick(arrivals: List<CustomerGroup>, tick: Int) {
        Logger.restaurantStart(id)
        // "In the ticks before a restaurant's openingTickStart, the simulation will not log any
        // action or action status messages." Afterwards the deliveries, the eating and the ratings
        // keep being logged every tick, even when the doors are already closed.
        if (tick >= openingTick) {
            val open = isOpen()
            if (open) {
                foh.beginTick()
                foh.callSeatingAndOrdering(arrivals, acceptsCustomers = tick <= closingTick - LAST_TICKS_CLOSED)
                val cookedThisTick = kitchen.cook()
                if (cookedThisTick > 0) Statistics.recordCooked(id, cookedThisTick)
                foh.callServingService()
            }
            foh.callDeliveryDesk()
            foh.callDiningService()
            if (open) {
                foh.callEscortingService()
                if (tick == closingTick) foh.closeOpeningTime()
            }
            foh.callRatingService()
        }
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
