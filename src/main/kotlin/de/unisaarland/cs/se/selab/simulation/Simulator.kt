package de.unisaarland.cs.se.selab.simulation
import de.unisaarland.cs.se.selab.incident.Incident
import de.unisaarland.cs.se.selab.logging.Logger
import de.unisaarland.cs.se.selab.sharedPackage.customers.CustomerGroup
import de.unisaarland.cs.se.selab.sharedPackage.customers.EventCustomerGroup

private const val TICKS_PER_EVENING = 24

/**
 * Runs the whole simulation: evening by evening (incidents, preparation, serving ticks, end of the
 * evening) until [maxTicks] is reached, then logs the statistics. It only keeps time and order;
 * what happens inside a restaurant during a tick is up to [Restaurant.runRestaurantTick].
 */
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
        for (restaurant in restaurants.sortedBy { it.getId() }) {
            restaurant.prepare(customers.regularsFor(restaurant.getId(), evening))
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
        Logger.tickStarted(tick, GlobalClock.getEvening())

        browsingService.refresh(restaurants.map { it.snapshot() }.toMutableList())
        bookEventsThreeEveningsAhead(tick)
        val walkIns = decideWalkIns(tick)

        for (restaurant in restaurants.sortedBy { it.getId() }) {
            val arrivals = customers.arriving(restaurant.getId(), GlobalClock.getEvening(), tick) +
                walkIns[restaurant.getId()].orEmpty()
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
            Logger.Customer.restaurantDecision(group.id(), restaurant.getId())
            if (restaurant.bookEvent(group, group.getEventEvening())) group.book(restaurant.getId())
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
            Logger.Customer.restaurantDecision(group.id(), restaurant.getId())
            if (group.isDelivery()) {
                DeliveryService.placeOrder(group, restaurant)
            } else {
                walkIns.getOrPut(restaurant.getId()) { mutableListOf() } += group
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

    /** The restaurant with [id], or null if there is none; used by incidents and the delivery service. */
    fun restaurantsById(id: Int): Restaurant? {
        return restaurants.firstOrNull { it.getId() == id }
    }

    /**
     * Starts the simulation: "Simulation started", evenings until [maxTicks] ticks have run, then the
     * simulation statistics per restaurant.
     */
    fun run() {
        Logger.simulationStarted()
        while (GlobalClock.currentTick < maxTicks) {
            runEvening()
        }
        Statistics.report(restaurants.map { it.getId() })
    }

    /** The browsing service with every restaurant's current snapshot; used by the recipe change incident. */
    fun getBrowsingService(): BrowsingService {
        return browsingService
    }
}
