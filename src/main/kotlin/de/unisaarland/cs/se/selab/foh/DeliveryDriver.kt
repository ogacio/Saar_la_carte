package de.unisaarland.cs.se.selab.foh

import de.unisaarland.cs.se.selab.simulation.DeliveryService
import de.unisaarland.cs.se.selab.sharedPackage.Order
import de.unisaarland.cs.se.selab.logging.Logger
import de.unisaarland.cs.se.selab.simulation.GlobalClock

class DeliveryDriver(private val restaurantId: Int) {
    private var id: Int? = null
    private var departureTick: Int? = null
    private var order: Order? = null
    private var ticksLeft: Int? = null
    private var state: DriverState = DriverState.WAITING
    private var travelTicks: Int? = null
    private var distance: Int? = null
    private var switch: Boolean = false

    fun plusTick(): Unit{
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
            if(state == DriverState.DELIVERING) {
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
                if (switch) {abort()}else{clearDelivery()}
            }
        }
    }

    fun receiveOrder(o:Order): Unit{
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
    fun isFree(): Boolean {
        return state == DriverState.WAITING
    }
    fun abort(): Unit {
        clearDelivery()
        id = null
    }
    private fun clearDelivery(){
        departureTick = null
        order = null
        ticksLeft = null
        state = DriverState.WAITING
        travelTicks = null
        distance = null
        switch = false
    }
    private fun startReturn(travelTicks:Int): Unit{
        ticksLeft = travelTicks
        state = DriverState.RETURNING
    }
    private fun drivenDistance(): Int {
        return if (ticksLeft == 0 && distance!! % 5 != 0) {
            distance!! % 5
        } else 5
    }
    fun switch(): Unit {switch=!switch}
    fun resetId(): Unit {id = null}
    fun isDelivering():Boolean{return state==DriverState.DELIVERING}
    fun isWaiting():Boolean{return state==DriverState.WAITING}
    fun isReturning():Boolean{return state==DriverState.RETURNING}
    fun getRestaurantId() = restaurantId
    fun getDepartureTick() = departureTick
    fun getId() = id
    fun setId(id: Int){
        this.id = id
    }
}
