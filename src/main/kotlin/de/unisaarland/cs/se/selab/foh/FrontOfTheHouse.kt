package de.unisaarland.cs.se.selab.foh

import de.unisaarland.cs.se.selab.simulation.ratings.ReservationBook


class FrontOfTheHouse(
    private val ctx: SubUnits,
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
        rating.rateFailedReservation(group, ctx)

    }

    public fun beginTick() {
        waitstaff.beginTick()
    }

    /** Call each service in order. Order corresponds top-down in the definition of the methods */
    public fun callSeatingService(arrivals: MutableList<CustomerGroup>) {
        // TODO
    }

    public fun callOrderingService() {
        // TODO
    }

    public fun callServingService() {
        // TODO
    }

    public fun mealsReady(meals: MutableList<Meal>) {
        // TODO
    }

    public fun callDeliveryDesk() {
        // TODO
    }

    public fun callDiningService() {
        // TODO
    }

    public fun callEscortingService() {
        // TODO
    }

    public fun callRatingService() {
        // TODO
    }




}