package de.unisaarland.cs.se.selab.data

data class RestaurantData(private val id: Int, private val type: RestaurantType, private val openingTick: Int, private val closingTick: Int,
                          private val dishes: MutableList<Recipe>, private var freeSeats: MutableMap<TableType, Int>, private var freeDrivers: Int,
                          private val hostsEvents: Boolean, private  val eventSeatsBooked: MutableMap<Int, Int>) {

    fun take(g: CustomerGroup): Unit {

        require(g.getGroupSize()<= freeSeats.tableType){
            "Seats have to be checked somewhere else"
        }
        freeSeats.tableType -= g.getGroupSize()
    }
    fun openAt(tick: Int): Boolean {
        return openingTick<=tick && closingTick>tick
    }

    fun eventSeatsLeft(evening: Int): Int {
        //TODO
    }
    fun decreaseDrivers1(){
        freeDrivers--
    }
    fun increaseDrivers1(){
        freeDrivers++
    }
}