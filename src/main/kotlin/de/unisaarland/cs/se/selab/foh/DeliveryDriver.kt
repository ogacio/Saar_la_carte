package de.unisaarland.cs.se.selab.foh

import de.unisaarland.cs.se.selab.simulation.DeliveryService
import de.unisaarland.cs.se.selab.sharedPackage.Order

class DeliveryDriver(private val restaurantId:Int) {
    private var id: Int? = null
    private var departureTick:Int? = null
    private var order: Order? = null
    private var ticksLeft: Int? = null
    private var state: DriverState = DriverState.WAITING
    private var travelTicks: Int? = null

    fun plusTick(): Unit{
        if (state == DriverState.WAITING)return
        ticksLeft = ticksLeft!!-1
        if (ticksLeft == 0){
            if(state == DriverState.DELIVERING){
                //TODO call something to try take order
                startReturn(travelTicks!!)
            }else{
                clearDelivery()
            }
        }
    }

    fun receiveOrder(o:Order, tick:Int): Unit{
        order = o
        departureTick = tick
        state = DriverState.DELIVERING
        ticksLeft = DeliveryService.calculateTravelTicks(order!!.getCustomerGroup().getDeliveryDistance()) + 1
        travelTicks = ticksLeft!!-1
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
    }
    private fun startReturn(travelTicks:Int): Unit{
        ticksLeft = travelTicks
        state = DriverState.RETURNING
    }
    fun getRestaurantId() = restaurantId
    fun getDepartureTick() = departureTick
    fun getId() = id
    fun setId(id: Int){
        this.id = id
    }
}
