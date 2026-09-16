package de.unisaarland.cs.se.selab.simulation
import de.unisaarland.cs.se.selab.incident.Incident
import de.unisaarland.cs.se.selab.logging.Logger
import de.unisaarland.cs.se.selab.sharedPackage.customers.CustomerGroup
import de.unisaarland.cs.se.selab.sharedPackage.customers.EventCustomerGroup


private const val TICKS_PER_EVENING = 24

class Simulator(
    private val maxTicks: Int,
    private val restaurants: MutableList<Restaurant>,
    private val customers: CustomerRegistry,
    incidents: MutableList<Incident>,
    private val browsingService: BrowsingService,
) {
    private val upcomingIncidents = incidents.sortedBy { it.id }.toMutableList()

    /**
     * One evening: incidents, preparation, up to 24 ticks, end of the evening. If maxTicks is reached in the middle
     * of the evening, it stops right after the last Restaurant End, without "Serving ... ends".
     */
    private fun runEvening() {
        GlobalClock.advanceEvening()
        val evening = GlobalClock.getEvening()
        applyIncidents(evening)

        Logger.preparationStarted(evening)
        for (restaurant in restaurants.sortedBy { it.id }) {
            restaurant.prepare(customers.regularsFor(restaurant.id, evening))
        }

        Logger.servingStarted(evening)
        while (GlobalClock.getTickInEvening() < TICKS_PER_EVENING) {
            if (GlobalClock.currentTick >= maxTicks) return
            runTick()
        }
        restaurants.forEach { it.closeEvening() }
        Logger.servingEnded(evening)
    }


    /** One tick: tick log, restaurant decisions, then every restaurant in ascending id. */
    private fun runTick() {
        GlobalClock.advanceTick()
        val tick = GlobalClock.getTickInEvening()
        Logger.tickStarted(GlobalClock.currentTick, tick)

        browsingService.refresh(restaurants.map { it.snapshot() }.toMutableList())
        bookEventsThreeEveningsAhead(tick)
        val walkIns = decideWalkIns(tick)

        for (restaurant in restaurants.sortedBy { it.id }) {
            val arrivals = customers.arriving(restaurant.id, GlobalClock.getEvening(), tick) +
                    walkIns[restaurant.id].orEmpty()
            restaurant.runRestaurantTick(arrivals, tick)
        }
    }

    /**
     * "Restaurant Decision", EVENT part: groups whose event is three evenings away choose a restaurant
     * and reserve there for their event evening. Runs before [decideWalkIns], so EVENT decisions are
     * logged before CASUAL ones, each in ascending group id.
     */
    private fun bookEventsThreeEveningsAhead(tick: Int) {
        val eventGroups = customers.deciding(GlobalClock.getEvening(), tick).filterIsInstance<EventCustomerGroup>()
        for (group in eventGroups) {
            val restaurant = browsingService.chooseForEvent(group, group.getEventEvening())?.let { restaurantsById(it) }
            if (restaurant == null) {
                Logger.Customer.noRestaurantDecision(group.id())
                continue
            }
            Logger.Customer.restaurantDecision(group.id(), restaurant.id)
            if (restaurant.bookEvent(group, group.getEventEvening())) group.book(restaurant.id)
        }
    }

    /**
     * "Restaurant Decision", CASUAL part: deliveries place their order, the other groups walk in.
     * Returns the walk-ins per restaurant id.
     */
    private fun decideWalkIns(tick: Int): Map<Int, List<CustomerGroup>> {
        val walkIns = mutableMapOf<Int, MutableList<CustomerGroup>>()
        val casualGroups = customers.deciding(GlobalClock.getEvening(), tick).filter { it !is EventCustomerGroup }
        for (group in casualGroups) {
            val restaurant = browsingService.choose(group)?.let { restaurantsById(it) }
            if (restaurant == null) {
                Logger.Customer.noRestaurantDecision(group.id())
                continue
            }
            Logger.Customer.restaurantDecision(group.id(), restaurant.id)
            if (group.isDelivery()) {
                DeliveryService.placeOrder(group, restaurant, tick)
            } else {
                walkIns.getOrPut(restaurant.id) { mutableListOf() } += group
            }
        }
        return walkIns
    }

    /** Logs and applies the incidents of this evening, in ascending id. */
    private fun applyIncidents(evening: Int) {
        val incidentsThisEvening = upcomingIncidents.filter { it.evening <= evening }
        for (incident in incidentsThisEvening) {
            Logger.incidentOccurred(incident.id, incident.type, evening)
            incident.apply(this)
        }
        upcomingIncidents.removeAll(incidentsThisEvening)
    }


    fun restaurantsById(id: Int): Restaurant? {
        return restaurants.firstOrNull{it.id == id}
    }

    fun run() {
        Logger.simulationStarted()
        while (GlobalClock.currentTick < maxTicks) {
            runEvening()
        }
        Statistics.report(restaurants.map { it.id })
    }

    fun getBrowsingService(): BrowsingService {
        return browsingService
    }


}