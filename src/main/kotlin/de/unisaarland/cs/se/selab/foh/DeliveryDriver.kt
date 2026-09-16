package de.unisaarland.cs.se.selab.foh

import de.unisaarland.cs.se.selab.logging.Logger
import de.unisaarland.cs.se.selab.sharedPackage.Order
import de.unisaarland.cs.se.selab.simulation.DeliveryService
import de.unisaarland.cs.se.selab.simulation.GlobalClock

/**does deliveries**/
class DeliveryDriver(private val restaurantId: Int) {
    private var id: Int? = null
    private var departureTick: Int? = null
    private var order: Order? = null
    private var ticksLeft: Int? = null
    private var state: DriverState = DriverState.WAITING
    private var travelTicks: Int? = null
    private var distance: Int? = null
    private var switch: Boolean = false

    /**simulates one tick of the driver**/
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
        if (ticksLeft == 0) {
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
            } else {
                Logger.Delivery.deliveryReturned(restaurantId, id!!)
                if (switch) { abort() } else { clearDelivery() }
            }
        }
    }

    /** after getting passed an order by a waiter the driver prepares**/
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

    /**waiting?**/
    fun isFree(): Boolean {
        return state == DriverState.WAITING
    }

    /**resets driver**/
    fun abort() {
        clearDelivery()
        id = null
    }

    /**resets everything except for the id**/
    private fun clearDelivery() {
        departureTick = null
        order = null
        ticksLeft = null
        state = DriverState.WAITING
        travelTicks = null
        distance = null
        switch = false
    }

    /**transition to return**/
    private fun startReturn(travelTicks: Int) {
        ticksLeft = travelTicks
        state = DriverState.RETURNING
    }

    /**calculates the drivenDistance for logging**/
    private fun drivenDistance(): Int {
        val tickDistance = 5
        return if (ticksLeft == 0 && distance!! % tickDistance != 0) {
            distance!! % tickDistance
        } else {
            tickDistance
        }
    }

    /**switch to choose wether to reset id too**/
    fun switch() { switch = !switch }

    /**id = null**/
    fun resetId() { id = null }

    /****/
    fun isDelivering(): Boolean { return state == DriverState.DELIVERING }

    /****/
    fun isWaiting(): Boolean { return state == DriverState.WAITING }

    /****/
    fun isReturning(): Boolean { return state == DriverState.RETURNING }

    /****/
    fun getRestaurantId() = restaurantId

    /****/
    fun getDepartureTick() = departureTick

    /****/
    fun getId() = id

    /**id = input**/
    fun setId(id: Int) {
        this.id = id
    }
}
