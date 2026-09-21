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
    private var resolvedOrder: Order? = null
    private var resolvedAsGivenUp: Boolean = false

    private companion object {
        const val TICK_DISTANCE = 5
    }

    /**
     * simulates one tick of the driver
     */
    fun plusTick() {
        if (state == DriverState.WAITING || departureTick == GlobalClock.getTickInEvening())return
        ticksLeft = checkNotNull(ticksLeft) - 1
        if (state == DriverState.DELIVERING) {
            Logger.Delivery.deliveryDriving(
                restaurantId,
                checkNotNull(id),
                drivenDistance(),
                checkNotNull(ticksLeft)
            )
        }
        if (ticksLeft != 0) {
            return
        }
        if (state == DriverState.DELIVERING) {
            Logger.Delivery.deliveryArrival(
                restaurantId,
                checkNotNull(id),
                checkNotNull(order).getCustomerGroup().id(),
                checkNotNull(order).getId()
            )
            if (checkNotNull(order).getCustomerGroup().hasGivenUp()) {
                Logger.Delivery.deliveryFailed(
                    restaurantId,
                    checkNotNull(id),
                    checkNotNull(order).getId(),
                    checkNotNull(order).getCustomerGroup().id()
                )
                resolvedAsGivenUp = true
            } else {
                for (i in checkNotNull(order).getMeals()) {
                    i.customer.receive(i, GlobalClock.getTickInEvening())
                }
                Logger.Delivery.deliveryFinished(
                    restaurantId,
                    checkNotNull(id),
                    checkNotNull(order).getId(),
                    checkNotNull(order).getCustomerGroup().id()
                )
                resolvedAsGivenUp = false
            }
            resolvedOrder = order
            startReturn(checkNotNull(travelTicks))
            return
        }
        Logger.Delivery.deliveryReturned(restaurantId, checkNotNull(id))
        if (switch) { abort() } else { clearDelivery() }
    }

    /**
     * after getting passed an order by a waiter the driver prepares
     */
    fun receiveOrder(o: Order) {
        order = o
        departureTick = GlobalClock.getTickInEvening()
        state = DriverState.DELIVERING
        distance = o.getCustomerGroup().getDeliveryDistance()
        ticksLeft = DeliveryService.calculateTravelTicks(checkNotNull(distance))
        travelTicks = checkNotNull(ticksLeft)
        o.getCustomerGroup().orderPlaced()
    }

    /**
     * "Based on the meals received by the waitstaff, the drivers that have a full order prepare
     * driving": the waitstaff hands the meals over first, so this line is written after those.
     */
    fun logPreparation() {
        val o = order ?: return
        Logger.Delivery.deliveryPreparation(
            restaurantId,
            checkNotNull(id),
            o.getId(),
            o.getCustomerGroup().id(),
            checkNotNull(travelTicks)
        )
    }

    /** The order this driver is carrying, null while it is waiting or on its way back. */
    fun currentOrder(): Order? = order

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
        resolvedOrder = null
        resolvedAsGivenUp = false
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
        return if (ticksLeft == 0 && checkNotNull(distance) % TICK_DISTANCE != 0) {
            checkNotNull(distance) % TICK_DISTANCE
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
     * the order this driver resolved (delivered or given up on) this tick, consumed on read
     */
    fun takeResolvedOrder(): Pair<Order, Boolean>? {
        val order = resolvedOrder ?: return null
        val gaveUp = resolvedAsGivenUp
        resolvedOrder = null
        return order to gaveUp
    }

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
