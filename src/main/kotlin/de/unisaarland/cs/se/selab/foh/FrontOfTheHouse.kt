package de.unisaarland.cs.se.selab.foh

import de.unisaarland.cs.se.selab.foh.visit.AwaitingSeatState
import de.unisaarland.cs.se.selab.foh.visit.Visit
import de.unisaarland.cs.se.selab.logging.Logger
import de.unisaarland.cs.se.selab.sharedPackage.MealStatus
import de.unisaarland.cs.se.selab.sharedPackage.customers.CustomerGroup
import de.unisaarland.cs.se.selab.sharedPackage.customers.CustomerStatus
import de.unisaarland.cs.se.selab.sharedPackage.customers.EventCustomerGroup
import de.unisaarland.cs.se.selab.sharedPackage.customers.GroupType
import de.unisaarland.cs.se.selab.simulation.GlobalClock
import de.unisaarland.cs.se.selab.simulation.ReservationBook
import de.unisaarland.cs.se.selab.simulation.Statistics
import de.unisaarland.cs.se.selab.simulation.SubUnits
import de.unisaarland.cs.se.selab.simulation.ratings.Experience

/**
 * Facade for the front of the house.
 *
 * The restaurant talks only to this class and never learns that tables, waiters,
 * drivers or visits exist. Each tick step hands [visits] plus [sbu] to the service
 * that owns the step. [visits] is always kept in the spec's group order: group type
 * (REGULAR, EVENT, CASUAL), then ascending group id.
 */
class FrontOfTheHouse(
    private val sbu: SubUnits,
    private val tables: TableAssignmentService,
    private val reservations: ReservationBook,
    private val waitstaff: WaiterAssignmentService,
    private val services: FohServices,
    private val deliveryDesk: DeliveryDesk,
) {
    private var openingTimeOver = false

    /** Every group of this evening that has not left yet, in group order. */
    private val visits: MutableList<Visit> = mutableListOf()

    /** Groups whose reservation failed today; they are rated in the next rating step. */
    private var turnedAway: List<CustomerGroup> = emptyList()

    /** Ids of the groups turned away tonight; they never get a visit, even if they are handed in as arrivals. */
    private val cancelledTonight = mutableSetOf<Int>()

    /** Delivery groups whose meal has arrived and are eating it; rated once every member is done. */
    private val eatingDeliveries: MutableList<CustomerGroup> = mutableListOf()

    /** Delivery groups that gave up waiting this tick; rated in the next rating step. */
    private var gaveUpDeliveries: List<CustomerGroup> = emptyList()

    /**
     * Preparation, before the doors open. The floor is reset first, then tables are
     * reserved for EVENT and then REGULAR groups; the groups that got no table are sent away.
     */
    fun prepareEvening(evening: Int, regulars: MutableList<CustomerGroup>) {
        visits.clear()
        turnedAway = emptyList()
        cancelledTonight.clear()
        tables.splitAllMerged()
        waitstaff.resetEvening()
        deliveryDesk.resetForEvening()
        reservations.clearTonight()
        sendCustomersAway(reservations.openEvening(evening, regulars))
    }

    /**
     * [groups] could not get a reserved table. They are told before they travel, so
     * they never show up; they are logged now and rated in the first tick of the evening.
     */
    fun sendCustomersAway(groups: List<CustomerGroup>) {
        for (group in groups) {
            Logger.Foh.noReserving(sbu.restaurantId, group.id())
            if (group is EventCustomerGroup) group.cancelBooking()
            cancelledTonight += group.id()
        }
        turnedAway = turnedAway + groups
    }

    /** An EVENT group reserving three evenings ahead; the table itself is chosen in the evening. */
    fun bookEvent(group: CustomerGroup, evening: Int): Boolean = reservations.bookAhead(group, evening)

    /** The delivery desk of this restaurant, used by the delivery service. */
    fun getDeliveryDesk(): DeliveryDesk = deliveryDesk

    /** Start of a tick: every waiter gets a fresh action budget. */
    fun beginTick() = waitstaff.beginTick()

    /**
     * Step 1: arrivals, seating and ordering. The spec runs these per group: arrival,
     * then seating, then ordering, before the next group. Groups that found no waiter
     * last tick try again here without arriving again. The two status lines come last.
     */
    fun callSeatingAndOrdering(arrivals: List<CustomerGroup>) {
        val tick = GlobalClock.currentTick
        val arrived = arrivals.filter { it.id() !in cancelledTonight }.map { Visit(it) }
        visits += arrived
        visits.sortWith(compareBy<Visit> { it.group.groupType().ordinal }.thenBy { it.group.id() })

        for (visit in visits) {
            if (visit.state !is AwaitingSeatState) continue
            if (visit in arrived) Logger.Customer.arrival(sbu.restaurantId, visit.group.id())
            services.seating.seat(visit, sbu, tick)
            services.ordering.takeOrder(visit, sbu, tick)
        }
        services.seating.logStatus(sbu)
        services.ordering.logStatus(sbu)
    }

    /** Step 3: serving, including the hand-over of complete delivery orders to drivers. */
    fun callServingService() = services.serving.serve(visits, sbu)

    /** Step 4: the drivers prepare, drive, deliver and return; resolved orders join the eating/rating flow. */
    fun callDeliveryDesk() {
        dropGivenUpOrders()
        for (driver in deliveryDesk.getDrivers()) {
            driver.plusTick()
            val resolved = driver.takeResolvedOrder() ?: continue
            val (order, gaveUp) = resolved
            if (gaveUp) {
                gaveUpDeliveries = gaveUpDeliveries + order.getCustomerGroup()
            } else {
                eatingDeliveries += order.getCustomerGroup()
            }
        }
    }

    /** Step 5: eating; waiting deadlines and finished eaters. */
    fun callDiningService() = services.dining.eat(visits, sbu)

    /** Step 6: escorting groups that have finished eating. */
    fun callEscortingService() = services.escorting.escort(visits, sbu)

    /**
     * Step 7: rating, then the sweep. Every visit that ended this tick is booked into the
     * statistics and the group's history, a CASUAL table is freed, and the visit is dropped.
     * Delivery groups that finished eating or gave up this tick rate alongside them.
     */
    fun callRatingService() {
        val finished = visits.filter { it.isFinished() }
        val visitOf = finished.associateBy { it.group.id() }
        val tick = GlobalClock.getTickInEvening()
        for (group in eatingDeliveries) {
            for (customer in group.members()) {
                if (customer.status() == CustomerStatus.SERVED && customer.isDoneEating(tick)) customer.doneEating()
            }
        }
        val doneEatingDeliveries = eatingDeliveries.filter { group ->
            group.members().all { it.status() == CustomerStatus.DONE_EATING }
        }
        val allRaters = finished.map { it.group } + turnedAway + doneEatingDeliveries + gaveUpDeliveries
        val raters: List<CustomerGroup> = allRaters.sortedWith(compareBy({ it.groupType().ordinal }, { it.id() }))
        for (group in raters) {
            val visit = visitOf[group.id()]
            when {
                visit != null -> {
                    services.rating.rate(visit, sbu)
                }
                group in gaveUpDeliveries -> {
                    group.orderResolved()
                    services.rating.rateDelivery(group, Experience.NEGATIVE, sbu)
                }
                group in doneEatingDeliveries -> {
                    group.orderResolved()
                    Logger.Delivery.deliveryFinishedEating(sbu.restaurantId, group.id())
                    Statistics.record(sbu.restaurantId, group.groupSize(), delivered = true)
                    services.rating.rateDelivery(group, deliveryExperience(group), sbu)
                }
                else -> {
                    services.rating.rateFailedReservation(group, sbu)
                }
            }
        }
        services.rating.logStatus(sbu)
        turnedAway = emptyList()
        gaveUpDeliveries = emptyList()
        eatingDeliveries.removeAll(doneEatingDeliveries)
        finished.forEach { finishVisit(it) }
        visits.removeAll(finished)

        if (openingTimeOver) {
            reservations.clearTonight() // "reservations of this evening are discarded"
            tables.splitAllMerged() // "tables are separated"
            openingTimeOver = false
        }
    }

    /**
     * Opening time is over: everyone still inside is escorted out at once. Their visits
     * end here, so the Simulator must run [callRatingService] afterward in the same tick.
     */
    fun closeOpeningTime() {
        visits.forEach { it.sendOut() }
        openingTimeOver = true
    }

    /** After the last rating step of the evening: drop everything that belongs to tonight. */
    fun closeEvening() {
        visits.clear()
        turnedAway = emptyList()
        cancelledTonight.clear()
        eatingDeliveries.clear()
        gaveUpDeliveries = emptyList()
        reservations.clearTonight()
        reservations.dropBookings()
        tables.splitAllMerged()
        waitstaff.resetEvening()
        deliveryDesk.resetForEvening()
    }

    /** Books a finished visit: served customers, the group's history, and a CASUAL table becomes free. */
    private fun finishVisit(visit: Visit) {
        Statistics.record(sbu.restaurantId, visit.servedCustomers(), delivered = false)
        visit.order?.let { visit.group.recordVisit(GlobalClock.getEvening(), it) }
        val table = visit.table
        // REGULAR and EVENT tables stay reserved for the whole evening, "even after the group has left".
        if (table != null && visit.group.groupType() == GroupType.CASUAL) tables.release(table)
    }

    /**
     * A delivery's experience (spec, "Rating"): positive if the meal arrived before the group's
     * [CustomerGroup.visitingTick], neutral at it, negative after.
     */
    private fun deliveryExperience(group: CustomerGroup): Experience {
        val arrivedTick = group.members().maxOf { checkNotNull(it.servedTick()) }
        return when {
            arrivedTick < group.visitingTick() -> Experience.POSITIVE
            arrivedTick == group.visitingTick() -> Experience.NEUTRAL
            else -> Experience.NEGATIVE
        }
    }

    /** The waiter assignment of this restaurant; the restaurant applies staff changes to it. */
    fun getWaitstaff(): WaiterAssignmentService = waitstaff

    /** The six services that run the tick steps of this front of house. */
    fun getServices(): FohServices = services

    /** The tables of this restaurant; the restaurant's snapshot reads free and total seats from them. */
    fun getTables(): TableAssignmentService = tables

    /** The reservations of this restaurant; the restaurant's snapshot reads the booked event seats from it. */
    fun getReservationBook(): ReservationBook = reservations

    /**
     * "in case they didn't get food at the end of the 3rd tick after the tick where their food should
     * have arrived": an order still waiting at the desk is dropped, its meals are aborted so the kitchen
     * stops cooking them, and the group rates in this tick.
     */
    private fun dropGivenUpOrders() {
        val waiting = (deliveryDesk.getNewOrders() + deliveryDesk.getReady()).sortedBy { it.getId() }
        for (order in waiting.filter { it.getCustomerGroup().hasGivenUp() }) {
            Logger.Delivery.deliveryGivenUp(sbu.restaurantId, order.getCustomerGroup().id(), order.getId())
            order.getMeals().forEach { if (it.status != MealStatus.SERVED) it.status = MealStatus.ABORTED }
            deliveryDesk.drop(order)
            gaveUpDeliveries = gaveUpDeliveries + order.getCustomerGroup()
        }
    }
}
