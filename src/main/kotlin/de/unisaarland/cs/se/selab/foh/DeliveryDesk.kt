package de.unisaarland.cs.se.selab.foh
import de.unisaarland.cs.se.selab.sharedPackage.Order
import de.unisaarland.cs.se.selab.simulation.DeliveryService

/**
 * Manages delivery orders for a restaurant, including queued and
 * ready orders, available delivery drivers, driver identifiers,
 * order handoff, and the reset of delivery state between evenings.
 */
class DeliveryDesk(private val drivers: MutableList<DeliveryDriver>, private val restaurantId: Int) {
    private val newOrders: MutableList<Order> = mutableListOf()
    private val ready: MutableList<Order> = mutableListOf()
    private var nextDriverId: Int = 1

    /**
     * sends a driver for a given order
     */
    fun sendForOrder(o: Order): Int? {
        val id = DeliveryService.chooseDriverForOrder(restaurantId)
        if (id != null) {
            ready.remove(o)
        } else {
            return null
        }
        val driver = drivers.first { it.getId() == id }
        driver.receiveOrder(o)
        return id
    }

    /**
     * delivery service enqueues an order
     */
    fun enqueue(o: Order) {
        newOrders.add(o)
    }

    /**
     * returns next higher driver id
     */
    fun grantDriverId(): Int {
        return nextDriverId++
    }

    /**
     * resets drivers and desk after an evening (tick>24)
     */
    fun resetForEvening() {
        for (i in drivers) {
            when {
                i.isDelivering() -> i.abort()
                i.isWaiting() -> i.resetId()
                i.isReturning() -> i.switch()
            }
        }
        newOrders.clear()
        ready.clear()
        nextDriverId = 1
    }

    /**
     * getter
     */
    fun getNewOrders(): List<Order> = newOrders

    /**
     * take from new, put to ready
     */
    fun readyOrder(o: Order) {
        newOrders.remove(o)
        ready.add(o)
    }

    /**
     * getter
     */
    fun getReady(): List<Order> = ready

    /**
     * getter
     */
    fun getRestaurantId(): Int = restaurantId

    /**
     * getter
     */
    fun getDrivers(): List<DeliveryDriver> = drivers

    /**
     * remove driver from desk-tracking
     */
    fun removeDriver(driver: DeliveryDriver) { drivers.remove(driver) }

    /**
     * add driver to desk-tracking
     */
    fun addDriver(driver: DeliveryDriver) { drivers.add(driver) }

    /**
     * returns the amount of currently free available drivers
     */
    fun amountFreeDrivers(): Int = drivers.count { it.isFree() }
}
