package de.unisaarland.cs.se.selab.simulation
import de.unisaarland.cs.se.selab.foh.DeliveryDriver
import de.unisaarland.cs.se.selab.logging.Logger
import de.unisaarland.cs.se.selab.sharedPackage.Meal
import de.unisaarland.cs.se.selab.sharedPackage.Order
import de.unisaarland.cs.se.selab.sharedPackage.Menu
import de.unisaarland.cs.se.selab.sharedPackage.Recipe
import de.unisaarland.cs.se.selab.sharedPackage.customers.Customer
import de.unisaarland.cs.se.selab.sharedPackage.customers.CustomerGroup

object DeliveryService {
    private val allDrivers = mutableListOf<DeliveryDriver>()
    private var simulator: Simulator? = null
    fun placeOrder(g: CustomerGroup, restaurant: Restaurant, tick: Int): Order?{
        var failed = 0
        val found = mutableListOf<Meal>()
        val sortedCustomers = g.members().sortedWith(
            compareByDescending<Customer> {
                it.preference()?.excluded()?.size ?: 0
            }.thenBy {
                it.preference()?.favouriteDishNames()?.size ?: 0
            }
        )
        for (i in sortedCustomers) {
            val tmpMenu:MutableList<Recipe> = restaurant.getMenu().getOrderables().toMutableList()
            tmpMenu.removeAll{it.getIngredients().any {
                    ingredient -> ingredient.getIngredient() in i.preference()?.excluded().orEmpty()
            }}
            tmpMenu.sortWith(
                compareByDescending<Recipe> {
                    it.getIngredients().map{it.getIngredient()}.intersect(i.preference()?.preferred().orEmpty()).size
                }.thenByDescending {
                    it.getId()
                }
            )
            var chosen: Recipe? = null
            for (p in i.preference()?.favouriteDishNames().orEmpty()) {
                chosen = tmpMenu.firstOrNull{it.getDishName() == p}
                if (chosen != null) {break}
            }
            if (chosen == null) {
                chosen = tmpMenu.firstOrNull()
            }
            if (chosen == null){
                failed++
            }else{
                found.add(Meal(null,i,chosen))
                restaurant.getPantry().reserve(chosen)
                restaurant.getMenu().refresh()
            }
        }
        var order:Order? = null
        if (found.isNotEmpty()){
            order = Order(g,restaurant.getId(),g.id(),tick,true,found)
            for (i in order.getMeals()) {
                i.orderId = order.getId()
            }
            Logger.Foh.ordering(
                restaurant.getId(), g.id(),
                order.getId(),
                order.dishCounts(),
                null
            )
            restaurant.getSubunit().getKitchen().enqueue(order)
            restaurant.getFoh().getDeliveryDesk().enqueue(order)
        }
        if (failed>0){
            Logger.Foh.noOrdering(
                restaurant.getId(),
                g.id(),
                failed
            )
        }
        return order
    }
    fun chooseDriverForOrder(restaurantId: Int): Int?{
        val driver = allDrivers.filter{it.getRestaurantId() == restaurantId}.firstOrNull{it.isFree()}
        if (driver == null){return null}
        if (driver.getId()==null){
            driver.setId(simulator?.restaurantsById(restaurantId).getFoh().getDeliveryDesk().grantDriverId())
        }
        return driver.getId()
    }
    fun calculateTravelTicks(distance: Int):Int{
        return (distance+4)/5
    }
    fun rmDriver(restaurantId: Int): Unit{
        val driver= allDrivers.firstOrNull{it.getRestaurantId() == restaurantId }

        if (driver != null) {
            allDrivers.remove(driver)
        }
    }
    fun addDriver(restaurantId: Int): Unit{
        allDrivers.add(DeliveryDriver(restaurantId))
    }
    fun setSimulator(s:Simulator): Unit {simulator = s}
}