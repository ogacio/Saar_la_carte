package de.unisaarland.cs.se.selab.foh

import de.unisaarland.cs.se.selab.logging.Logger
import de.unisaarland.cs.se.selab.sharedPackage.Order
import de.unisaarland.cs.se.selab.simulation.DeliveryService
import de.unisaarland.cs.se.selab.simulation.GlobalClock

/**
 * Represents a delivery driver and manages the driver's current
 * delivery state, assigned order, travel progress, delivery
 * completion, return journey, and availability for future deliveries.
 */
class DeliveryDriver(private val restaurantId: Int) {
    private var id: Int? = null
    private var departureTick: Int? = null
    private var order: Order? = null
    private var ticksLeft: Int? = null
    private var state: DriverState = DriverState.WAITING
    private var travelTicks: Int? = null
    private var distance: Int? = null
    private var switch: Boolean = false

    private companion object {
        const val TICK_DISTANCE = 5
    }

    /**
     * simulates one tick of the driver
     */
    fun plusTick() {
        if (state == DriverState.WAITING || departureTick == GlobalClock.getTickInEvening())return
        ticksLeft = ticksLeft!! - 1
        if (state == DriverState.DELIVERING) {
            Logger.Delivery.deliveryDriving(
                restaurantId,
                id!!,
                drivenDistance(),
                ticksLeft!!
            )
        }
        if (ticksLeft != 0) {
            return
        }
        if (state == DriverState.DELIVERING) {
            Logger.Delivery.deliveryArrival(
                restaurantId,
                id!!,
                order!!.getCustomerGroup().id(),
                order!!.getId()
            )
            if (order!!.getCustomerGroup().hasGivenUp()) {
                Logger.Delivery.deliveryFailed(
                    restaurantId,
                    id!!,
                    order!!.getId(),
                    order!!.getCustomerGroup().id()
                )
            } else {
                for (i in order!!.getMeals()) {
                    i.customer.receive(i, GlobalClock.getTickInEvening())
                }
                Logger.Delivery.deliveryFinished(
                    restaurantId,
                    id!!,
                    order!!.getId(),
                    order!!.getCustomerGroup().id()
                )
            }
            startReturn(travelTicks!!)
            return
        }
        Logger.Delivery.deliveryReturned(restaurantId, id!!)
        if (switch) { abort() } else { clearDelivery() }
    }

    /**
     * after getting passed an order by a waiter the driver prepares
     */
    fun receiveOrder(o: Order) {
        order = o
        departureTick = GlobalClock.getTickInEvening()
        state = DriverState.DELIVERING
        distance = order!!.getCustomerGroup().getDeliveryDistance()
        ticksLeft = DeliveryService.calculateTravelTicks(distance!!)
        travelTicks = ticksLeft!!
        Logger.Delivery.deliveryPreparation(
            restaurantId,
            id!!,
            o.getId(),
            o.getCustomerGroup().id(),
            travelTicks!!
        )
    }

    /**
     * is the driver state waiting?
     */
    fun isFree(): Boolean {
        return state == DriverState.WAITING
    }

    /**
     * resets driver
     */
    fun abort() {
        clearDelivery()
        id = null
    }

    /**
     * resets everything except for the id
     */
    private fun clearDelivery() {
        departureTick = null
        order = null
        ticksLeft = null
        state = DriverState.WAITING
        travelTicks = null
        distance = null
        switch = false
    }

    /**
     * transition to return
     */
    private fun startReturn(travelTicks: Int) {
        ticksLeft = travelTicks
        state = DriverState.RETURNING
    }

    /**
     * calculates the drivenDistance for logging
     */
    private fun drivenDistance(): Int {
        return if (ticksLeft == 0 && distance!! % TICK_DISTANCE != 0) {
            distance!! % TICK_DISTANCE
        } else {
            TICK_DISTANCE
        }
    }

    /**
     * switch to choose to reset id as well or not
     */
    fun switch() { switch = !switch }

    /**
     * id = null
     */
    fun resetId() { id = null }

    /**
     * bool
     */
    fun isDelivering(): Boolean { return state == DriverState.DELIVERING }

    /**
     * bool
     */
    fun isWaiting(): Boolean { return state == DriverState.WAITING }

    /**
     * bool
     */
    fun isReturning(): Boolean { return state == DriverState.RETURNING }

    /**
     * getter
     */
    fun getRestaurantId() = restaurantId

    /**
     * getter
     */
    fun getDepartureTick() = departureTick

    /**
     * getter
     */
    fun getId() = id

    /**
     * id = input
     */
    fun setId(id: Int) {
        this.id = id
    }
}
