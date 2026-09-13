package de.unisaarland.cs.se.selab.simulation
import de.unisaarland.cs.se.selab.foh.DriverState
import de.unisaarland.cs.se.selab.sharedPackage.customers.CustomerGroup
import de.unisaarland.cs.se.selab.simulation.Simulator

class DeliveryService {
    private val allDrivers = mutableListOf<DeliveryDriver>()
    fun placeOrder(g: CustomerGroup, restaurant:Restaurant, tick: Int): Order?{
        //TODO
    }
    fun chooseDriverForOrder(restaurantId: Int): Int{
        val driver = allDrivers.filter{it.getRestaurantId() == restaurantId}.first{it.isFree()}
        if (driver.getId()==null){
            driver.setId(Simulator.restaurantsById(restaurantId).getFoh().getDeliveryDesk().grantDriverId())
        }
        return driver.getId()!!
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
        allDrivers.add(DeliveryDriver(null, null, null, restaurantId,null, DriverState.WAITING))
    }
}