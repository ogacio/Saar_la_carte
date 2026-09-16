package de.unisaarland.cs.se.selab.foh
import de.unisaarland.cs.se.selab.sharedPackage.Order
import de.unisaarland.cs.se.selab.simulation.DeliveryService

class DeliveryDesk (private val drivers: MutableList<DeliveryDriver>, private val restaurantId: Int) {
    private val newOrders: MutableList<Order> = mutableListOf()
    private val ready: MutableList<Order> = mutableListOf()
    private var nextDriverId: Int = 1
    fun sendForOrder(o:Order):Order? {
        val id = DeliveryService.chooseDriverForOrder(restaurantId)
        if (id != null) {
            ready.remove(o)
        } else {
            return o
        }
        val driver = drivers.first { it.getId() == id }
        driver.receiveOrder(o)
        return null
    }
    fun enqueue(o: Order): Unit{
        newOrders.add(o)
    }
    fun grantDriverId(): Int{
        return nextDriverId++
    }
    fun resetForEvening(): Unit{
            for (i in drivers) {
                when {
                    i.isDelivering() -> i.abort()
                    i.isWaiting() -> i.resetId()
                    i.isReturning() -> {i.switch()}
                }
            }
        newOrders.clear()
        ready.clear()
        nextDriverId = 1

    }
    fun getNewOrders(): List<Order> = newOrders
    fun readyOrder(o: Order): Unit {
        newOrders.remove(o)
        ready.add(o)
    }
    fun getReady(): List<Order> = ready
    fun getRestaurantId(): Int = restaurantId
    fun getDrivers(): List<DeliveryDriver> = drivers

}