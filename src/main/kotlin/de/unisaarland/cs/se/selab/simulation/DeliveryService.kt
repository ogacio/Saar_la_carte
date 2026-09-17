package de.unisaarland.cs.se.selab.simulation
import de.unisaarland.cs.se.selab.foh.DeliveryDriver
import de.unisaarland.cs.se.selab.logging.Logger
import de.unisaarland.cs.se.selab.sharedPackage.Meal
import de.unisaarland.cs.se.selab.sharedPackage.Order
import de.unisaarland.cs.se.selab.sharedPackage.Recipe
import de.unisaarland.cs.se.selab.sharedPackage.customers.Customer
import de.unisaarland.cs.se.selab.sharedPackage.customers.CustomerGroup

/**
 * Handles delivery-related operations such as placing delivery
 * orders, assigning available delivery drivers, calculating travel
 * times, and managing the number of drivers for restaurants.
 */
object DeliveryService {
    private val allDrivers = mutableListOf<DeliveryDriver>()
    private var simulator: Simulator? = null
    private const val DISTANCE_PER_TICK = 5
    private const val BUFFER = 4

    /**
     * choose dishes according to customer preferences
     */
    fun placeOrder(g: CustomerGroup, restaurant: Restaurant, tick: Int): Order? {
        var failed = 0
        val found = mutableListOf<Meal>()
        val sortedCustomers = sortCustomers(g)
        for (i in sortedCustomers) {
            val tmpMenu: MutableList<Recipe> = restaurant.getMenu().getOrderables().toMutableList()
            tmpMenu.removeAll {
                it.getIngredients().any {
                        ingredient ->
                    ingredient.getIngredient() in i.preference()?.excluded().orEmpty()
                }
            }
            tmpMenu.sortWith(
                compareByDescending<Recipe> {
                    it.getIngredients().map { it.getIngredient() }.intersect(i.preference()?.preferred().orEmpty()).size
                }.thenByDescending {
                    it.getId()
                }
            )
            var chosen = i.preference()?.favouriteDishNames().orEmpty()
                .firstNotNullOfOrNull { favourite ->
                    tmpMenu.firstOrNull { it.getDishName() == favourite }
                }
                ?: tmpMenu.firstOrNull()
            if (chosen == null) {
                failed++
            } else {
                found.add(Meal(null, i, chosen))
                restaurant.getPantry().reserve(chosen)
                restaurant.getMenu().refresh()
            }
        }
        var order: Order? = null
        if (found.isNotEmpty()) {
            order = Order(g, restaurant.getId(), g.id(), tick, true, found)
            for (i in order.getMeals()) {
                i.orderId = order.getId()
            }
            Logger.Foh.ordering(
                restaurant.getId(),
                g.id(),
                order.getId(),
                order.dishCounts(),
                null
            )
            restaurant.getKitchen().enqueue(order)
            restaurant.getFoh().getDeliveryDesk().enqueue(order)
        }
        if (failed > 0) {
            Logger.Foh.noOrdering(
                restaurant.getId(),
                g.id(),
                failed
            )
        }
        return order
    }

    private fun sortCustomers(g: CustomerGroup): List<Customer> {
        return g.members().sortedWith(
            compareByDescending<Customer> {
                it.preference()?.excluded()?.size ?: 0
            }.thenBy {
                it.preference()?.favouriteDishNames()?.size ?: 0
            }
        )
    }

    /**
     * chooses driver for order and sets id if it is null
     */
    fun chooseDriverForOrder(restaurantId: Int): Int? {
        val driver = allDrivers.filter { it.getRestaurantId() == restaurantId }.firstOrNull { it.isFree() }
        if (driver == null) { return null }
        if (driver.getId() == null) {
            driver.setId(simulator!!.restaurantsById(restaurantId)!!.getFoh().getDeliveryDesk().grantDriverId())
        }
        return driver.getId()
    }

    /**
     * returns travel ticks
     */
    fun calculateTravelTicks(distance: Int): Int {
        return (distance + BUFFER) / DISTANCE_PER_TICK
    }

    /**
     * removes driver from simulation
     */
    fun rmDriver(restaurantId: Int) {
        val driver = allDrivers.firstOrNull { it.getRestaurantId() == restaurantId }

        if (driver != null) {
            allDrivers.remove(driver)
            simulator!!.restaurantsById(restaurantId)!!.getFoh().getDeliveryDesk().removeDriver(driver)
        }
    }

    /**
     * adds driver to simulation
     */
    fun addDriver(restaurantId: Int) {
        val driver = DeliveryDriver(restaurantId)
        allDrivers.add(driver)
        simulator!!.restaurantsById(restaurantId)!!.getFoh().getDeliveryDesk().addDriver(driver)
    }

    /**
     * sets simulator: necessary
     */
    fun setSimulator(s: Simulator) { simulator = s }
}
