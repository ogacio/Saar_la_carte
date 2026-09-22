package de.unisaarland.cs.se.selab.foh

import de.unisaarland.cs.se.selab.foh.visit.AwaitingSeatState
import de.unisaarland.cs.se.selab.foh.visit.Visit
import de.unisaarland.cs.se.selab.logging.Logger
import de.unisaarland.cs.se.selab.sharedPackage.customers.CustomerGroup
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
    private val gaveUpDeliveries: MutableSet<CustomerGroup> = mutableSetOf()

    /** Delivery groups that finished eating in this tick; produced by the eating step, consumed by rating. */
    private var finishedEatingDeliveriesThisTick: List<CustomerGroup> = emptyList()

    /** Orders whose group has already been reported as having given up, so the line is written once. */
    private val reportedGiveUps: MutableSet<Int> = mutableSetOf()

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
    fun callSeatingAndOrdering(arrivals: List<CustomerGroup>, acceptsCustomers: Boolean = true) {
        val tick = GlobalClock.currentTick
        val arrived = arrivals.filter { it.id() !in cancelledTonight }.map { Visit(it) }
        visits += arrived
        visits.sortWith(compareBy<Visit> { it.group.groupType().ordinal }.thenBy { it.group.id() })

        for (visit in visits) {
            if (visit.state !is AwaitingSeatState) continue
            if (visit in arrived) Logger.Customer.arrival(sbu.restaurantId, visit.group.id())
            // The last-three-ticks rule applies only to new arrivals. A group that already arrived
            // last tick but found no free waiter still gets its one retry in this tick.
            if (!acceptsCustomers && visit in arrived) continue
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
        // A returning driver still holds its order until it is back, so it sorts by that group too.
        // Typed on purpose: inferred, detektMain wrongly reports the loop below as unreachable.
        val drivers: List<DeliveryDriver> = deliveryDesk.getDrivers()
            .filter { it.currentOrder() != null }
            .sortedBy { checkNotNull(it.currentOrder()).getCustomerGroupId() }

        // Forum #18: each of these runs across every driver before the next one starts. prepare()
        // also captures whether a driver was already returning before this tick's travel runs,
        // which returnHome() needs to tell "just started back" from "back this tick".
        drivers.forEach { it.prepare() } // Preparation
        drivers.forEach { it.drive() } // Driving
        drivers.forEach { it.arrive() } // Arrival
        drivers.forEach { it.deliverAccepted() } // Finished
        drivers.forEach { it.deliverRejected() } // Failed
        dropGivenUpOrders() // Given Up
        drivers.forEach { it.returnHome() } // Returned

        for (driver in drivers) {
            val (order, gaveUp) = driver.takeResolvedOrder() ?: continue
            if (!gaveUp) {
                val group = order.getCustomerGroup()
                group.orderResolved()
                Statistics.record(sbu.restaurantId, order.getMeals().size, delivered = true)
                if (group !in eatingDeliveries) eatingDeliveries += group
            }
        }
    }

    /** Step 5: eating; waiting deadlines and finished eaters, including delivered food. */
    fun callDiningService() {
        finishedEatingDeliveriesThisTick = services.dining.eat(visits, eatingDeliveries, sbu)
        eatingDeliveries.removeAll(finishedEatingDeliveriesThisTick.toSet())
    }

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
        val doneEatingDeliveries = finishedEatingDeliveriesThisTick
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
                    services.rating.rateDelivery(group, deliveryExperience(group), sbu)
                }
                else -> {
                    services.rating.rateFailedReservation(group, sbu)
                }
            }
        }
        services.rating.logStatus(sbu)
        turnedAway = emptyList()
        gaveUpDeliveries.clear()
        finishedEatingDeliveriesThisTick = emptyList()
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
        reportedGiveUps.clear()
        visits.clear()
        turnedAway = emptyList()
        cancelledTonight.clear()
        eatingDeliveries.clear()
        gaveUpDeliveries.clear()
        finishedEatingDeliveriesThisTick = emptyList()
        reservations.clearTonight()
        reservations.dropBookings()
        tables.splitAllMerged()
        waitstaff.resetEvening()
        deliveryDesk.resetForEvening()
    }

    /** Books a finished visit: served customers, the group's history, and a CASUAL table becomes free. */
    private fun finishVisit(visit: Visit) {
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
     *
     * Called from [callDeliveryDesk] after [DeliveryDriver.deliverAccepted] / [DeliveryDriver.deliverRejected],
     * so an order that gives up the same tick a driver reaches it logs "Given Up" after "Delivery Failed",
     * matching the section order in the spec ("Delivering") and forum update #18. `libs/selab.jar` logs
     * these the other way round for that case; it does not implement #18's per-phase ordering at all
     * (see BUGS-FOUND.md #4), so it is not a reason to move this call back above the delivery phases.
     */
    private fun dropGivenUpOrders() {
        val atTheDesk = deliveryDesk.getNewOrders() + deliveryDesk.getReady()
        val onTheRoad = deliveryDesk.getDrivers().mapNotNull { it.currentOrder() }
        // "This is logged using group and order id in ascending order of group id": an order that
        // is already with a driver counts too, and the line is written once, in the tick the group
        // ran out of patience. The driver then fails to deliver it on arrival.
        val due = (atTheDesk + onTheRoad)
            .distinctBy { it.getId() }
            .filter { it.getCustomerGroup().deliveryGiveUpDue() }
            .sortedBy { it.getCustomerGroupId() }
        for (order in due) {
            if (reportedGiveUps.add(order.getId())) {
                val group = order.getCustomerGroup()
                group.markDeliveryGivenUp()
                Logger.Delivery.deliveryGivenUp(sbu.restaurantId, group.id(), order.getId())
                gaveUpDeliveries += group
            }
        }
        for (order in atTheDesk.filter { it.getCustomerGroup().deliveryWasGivenUp() }) {
            // "It can happen that a customer leaves the restaurant or aborts a delivery. However,
            // for this there is no synchronization to the kitchen": the meals keep being cooked,
            // only no driver takes them out anymore.
            deliveryDesk.drop(order)
        }
    }
}
