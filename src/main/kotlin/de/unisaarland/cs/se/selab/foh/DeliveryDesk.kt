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
     * The driver that carries [o]: the one already collecting its meals, or a newly reserved free
     * driver. Forum #6 lets the waitstaff fill a driver over several ticks, so the driver is picked
     * once, on the first meal, and keeps the order until it is complete.
     */
    fun driverFor(o: Order): DeliveryDriver? {
        val alreadyLoading = drivers.firstOrNull { it.currentOrder() === o }
        if (alreadyLoading != null) return alreadyLoading

        val id = DeliveryService.chooseDriverForOrder(restaurantId) ?: return null
        val driver = drivers.first { it.getId() == id }
        driver.assignOrder(o)
        return driver
    }

    /** The driver holds every meal of [o], so the desk stops offering it to the waitstaff. */
    fun departed(o: Order) {
        ready.remove(o)
    }

    /**
     * Reserves a driver and loads [o] in one go, the way the waitstaff did before forum #6.
     * Only the unit tests and DeliveryIntegrationTest still use it; the serving step goes through
     * driverFor + DeliveryDriver.loadMeals so a partial order can be carried over several ticks.
     */
    fun sendForOrder(o: Order): Int? {
        val driver = driverFor(o) ?: return null
        driver.loadMeals(driver.pendingMeals())
        departed(o)
        return driver.getId()
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
     * Resets drivers and desk after an evening. "Drivers immediately go home and return the next
     * evening": neither an aborted outbound delivery nor a return trip carries into the new
     * evening, and the ids start at 1 again because they are granted on receiving meals.
     */
    fun resetForEvening() {
        for (i in drivers) {
            when {
                i.isDelivering() || i.isLoading() -> i.abort()
                i.isWaiting() -> i.resetId()
                i.isReturning() -> i.switch()
            }
        }
        newOrders.clear()
        ready.clear()
        nextDriverId = 1
    }

    /** Lets the driver announce its route, once the waitstaff has handed the meals over. */
    fun logPreparationFor(driverId: Int) {
        drivers.firstOrNull { it.getId() == driverId }?.logPreparation()
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
    /**
     * For browsing (forum 304): "a driver counts as busy only once they have the complete
     * order and start the delivery", so a driver still being loaded is still free. Picking a
     * driver for a new order stays on isFree() alone.
     */
    fun amountFreeDrivers(): Int = drivers.count { it.isFree() || it.isLoading() }

    /**
     * Removes an order the group gave up on, from whichever queue it is still in. A driver that was
     * only collecting its meals is released again; one that already drove off keeps it and reports
     * the failed delivery on arrival.
     */
    fun drop(order: Order) {
        newOrders.remove(order)
        ready.remove(order)
        drivers.firstOrNull { it.currentOrder() === order && it.isLoading() }?.releaseLoad()
    }
}
