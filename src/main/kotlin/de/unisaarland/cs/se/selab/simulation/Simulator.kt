package de.unisaarland.cs.se.selab.simulation
import de.unisaarland.cs.se.selab.incident.Incident
import de.unisaarland.cs.se.selab.simulation.ratings.RatingBook
class Simulator(
    private val maxTicks: Int,
    private val restaurants: MutableList<Restaurant>,
    private val customers: CustomerRegistry,
    private val incidents: MutableList<Incident>,
    private val browsingService: BrowsingService,
    private val ratingBook: RatingBook,
    private val statistics: Statistics
) {

    private fun runOnlineOrders() {
        //TODO: implement the online order processing here
    }

    private fun runEvening() {
        //TODO: implement the evening processing here
    }

    private fun runTick() {
        //TODO: implement the tick processing here
    }

    private fun bookEventsThreeEveningsAhead() {
        //TODO: implement the event booking logic here
    }

    fun restaurantsById(id: Int): Restaurant? {
        return restaurants.first{it.id == id}
    }

    public fun run() {
        //TODO: implement the simulation loop here
    }





}