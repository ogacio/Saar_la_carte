package de.unisaarland.cs.se.selab.foh

import de.unisaarland.cs.se.selab.foh.visit.AwaitingSeatState
import de.unisaarland.cs.se.selab.foh.visit.Visit
import de.unisaarland.cs.se.selab.logging.Logger
import de.unisaarland.cs.se.selab.sharedPackage.Meal
import de.unisaarland.cs.se.selab.sharedPackage.customers.CustomerGroup
import de.unisaarland.cs.se.selab.sharedPackage.customers.EventCustomerGroup
import de.unisaarland.cs.se.selab.sharedPackage.customers.GroupType
import de.unisaarland.cs.se.selab.simulation.GlobalClock
import de.unisaarland.cs.se.selab.simulation.ReservationBook
import de.unisaarland.cs.se.selab.simulation.Statistics
import de.unisaarland.cs.se.selab.simulation.SubUnits

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

    /** Every group of this evening that has not left yet, in group order. */
    private val visits: MutableList<Visit> = mutableListOf()

    /** Groups whose reservation failed today; they are rated in the next rating step. */
    private var turnedAway: List<CustomerGroup> = emptyList()

    /** Ids of the groups turned away tonight; they never get a visit, even if they are handed in as arrivals. */
    private val cancelledTonight = mutableSetOf<Int>()

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

    /** An EVENT group reserving three evenings ahead; the table itself is chosen on the evening. */
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

    /**
     * The kitchen's hand-back after step 2: every cooked meal goes to the visit that
     * ordered it. Meals of delivery orders have no visit and go to the delivery desk.
     * Must run before callServingService, so the hold-back rule sees this tick's meals.
     */
    fun mealsReady(meals: List<Meal>) {
        val tick = GlobalClock.currentTick
        for (meal in meals) {
            val visit = visits.firstOrNull { it.order === meal.order }
            if (visit != null) visit.mealCooked(meal, tick) else deliveryDesk.mealCooked(meal)
        }
    }

    /** Step 3: serving, including the hand-over of complete delivery orders to drivers. */
    fun callServingService() = services.serving.serve(visits, sbu)

    /** Step 4: the drivers prepare, drive, deliver and return. */
    fun callDeliveryDesk() = deliveryDesk.send()

    /** Step 5: eating; waiting deadlines and finished eaters. */
    fun callDiningService() = services.dining.eat(visits, sbu)

    /** Step 6: escorting groups that have finished eating. */
    fun callEscortingService() = services.escorting.escort(visits, sbu)

    /**
     * Step 7: rating, then the sweep. Every visit that ended this tick is booked into the
     * statistics and the group's history, a CASUAL table is freed, and the visit is dropped.
     */
    fun callRatingService() {
        val finished = visits.filter { it.isFinished() }
        val visitOf = finished.associateBy { it.group.id() }
        val raters: List<CustomerGroup> = (finished.map { it.group } + turnedAway)
            .sortedWith(compareBy({ it.groupType().ordinal }, { it.id() }))
        for (group in raters) {
            val visit = visitOf[group.id()]
            if (visit != null) services.rating.rate(visit, sbu) else services.rating.rateFailedReservation(group, sbu)
        }
        services.rating.logStatus(sbu)
        turnedAway = emptyList()
        finished.forEach { finishVisit(it) }
        visits.removeAll(finished)
    }

    /**
     * Opening time is over: everyone still inside is escorted out at once. Their visits
     * end here, so the Simulator must run [callRatingService] afterwards in the same tick.
     */
    fun closeOpeningTime() = visits.forEach { it.sendOut() }

    /** After the last rating step of the evening: drop everything that belongs to tonight. */
    fun closeEvening() {
        visits.clear()
        turnedAway = emptyList()
        cancelledTonight.clear()
        reservations.clearTonight()
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
}
