package de.unisaarland.cs.se.selab.foh

import de.unisaarland.cs.se.selab.simulation.SubUnits
import de.unisaarland.cs.se.selab.simulation.ratings.ReservationBook
import de.unisaarland.cs.se.selab.foh.visit.Visit


class FrontOfTheHouse(
    private val sbu: SubUnits,
    private val tables: TableAssignmentService,
    private val reservations: ReservationBook,
    private val waitstaff: WaiterAssignmentService,
    private val seating: SeatingService,
    private val ordering: OrderingService,
    private val serving: ServingService,
    private val dining: DiningService,
    private val escorting: EscortingService,
    private val rating: RatingService,
    private val deliveryDesk: DeliveryDesk,
) {

    private val visits: MutableList<Visit> = mutableListOf()
    private var regularsForTonight: MutableList<CustomerGroup> = mutableListOf()
    private var turnedAway: List<CustomerGroup> = emptyList()
    


    public fun prepareEvening(evening: Int, regulars: MutableList<CustomerGroup>) {
        visits.clear()
        tables.splitAllMerged()
        waitstaff.resetEvening()
        deliveryDesk.resetForEvening()
        reservations.clearTonight()

        regularsForTonight = reservations.openEvening(evening, regulars)
    }

    public fun closeEvening() {
        /**TODO: From the spec we have that 
        customers still inside are escorted outside immediately. 
        Those groups that have finished eating see this as a non-negative experience, 
        for all other groups this counts as a negative experience.*/
        deliveryDesk.resetForEvening()
        tables.splitAllMerged()
        waitstaff.resetEvening()

        visits.clear()
        regularsForTonight.clear()
        //TODO
    }


    public fun bookEvent(group: CustomerGroup, evening: Int): Boolean {
        val booked = reservations.bookAhead(group, evening)
        // Probably logging here, but double check
        return booked
    }

    public fun sendCustomersAway(group: CustomerGroup) {
        rating.rateFailedReservation(group, sbu)

    }

    public fun beginTick() {
        waitstaff.beginTick()
    }

    /** Call each service in order. Order corresponds top-down in the definition of the methods */
    public fun callSeatingService(arrivals: MutableList<CustomerGroup>) {    
        for (group in arrivals) {
            Logger.Customer.arrival(sbu.restaurantId, group.id())
            visits.add(Visit(group))
        }
        // Insert the visit in oerder needed REGULARS -> EVENT -> CASUAL
        visits.sortWith(
            compareBy<Visit> { it.group.groupType().ordinal }
                .thenBy { it.group.id() },
        )
        seating.seatAll(visits, sbu)
    }

    public fun callOrderingService() {
        ordering.takeOrders(visits, sbu)
    }

    public fun callServingService() {
        serving.serve(visits, sbu)
    }

    public fun mealsReady(meals: MutableList<Meal>) {
        // TODO
    }

    public fun callDeliveryDesk() {
        // TODO
    }

    public fun callDiningService() {
        dining.eat(visits, sbu)
    }

    public fun callEscortingService() {
        escorting.escort(visits, sbu)
    }

    public fun callRatingService() {
        rating.rate(visits, turnedAway, sbu)
        turnedAway = emptyList()
        visits.removeAll { it.state is GoneState }
    }




}