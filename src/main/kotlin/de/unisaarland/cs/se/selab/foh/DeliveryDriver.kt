package de.unisaarland.cs.se.selab.foh

import de.unisaarland.cs.se.selab.logging.Logger
import de.unisaarland.cs.se.selab.sharedPackage.Meal
import de.unisaarland.cs.se.selab.sharedPackage.Order
import de.unisaarland.cs.se.selab.simulation.DeliveryService
import de.unisaarland.cs.se.selab.simulation.GlobalClock

/**
 * Represents a delivery driver and manages the driver's current
 * delivery state, assigned order, travel progress, delivery
 * completion, return journey, and availability for future deliveries.
 */
class DeliveryDriver(private val restaurantId: Int) {
    private var id: Int? = null
    private var departureTick: Int? = null
    private var order: Order? = null
    private var ticksLeft: Int? = null
    private var state: DriverState = DriverState.WAITING
    private var travelTicks: Int? = null
    private var distance: Int? = null
    private var resolvedOrder: Order? = null
    private var resolvedAsGivenUp: Boolean = false
    private var switch: Boolean = false

    /** Forum #6: "delivered meals being stored with the delivery driver" until the order is whole. */
    private val loaded: MutableList<Meal> = mutableListOf()

    private companion object {
        const val TICK_DISTANCE = 5
    }

    /** Captured by prepare(), before any driver delivers during this tick. */
    private var returningAtStartOfTick = false

    /** First phase: capture the return state and log newly loaded orders. */
    fun prepare() {
        returningAtStartOfTick = state == DriverState.RETURNING
        if (state == DriverState.DELIVERING &&
            departureTick == GlobalClock.getTickInEvening()
        ) {
            logPreparation()
        }
    }

    /** Advance outbound travel once. A newly loaded driver starts next tick. */
    fun drive() {
        if (state != DriverState.DELIVERING) return
        if (departureTick == GlobalClock.getTickInEvening()) return

        ticksLeft = checkNotNull(ticksLeft) - 1
        Logger.Delivery.deliveryDriving(
            restaurantId,
            checkNotNull(id),
            drivenDistance(),
            checkNotNull(ticksLeft),
        )
    }

    private fun atDestination(): Boolean =
        state == DriverState.DELIVERING && ticksLeft == 0

    /** Emit arrivals after every driver's Driving message has been emitted. */
    fun arrive() {
        if (!atDestination()) return
        val current = checkNotNull(order)
        Logger.Delivery.deliveryArrival(
            restaurantId,
            checkNotNull(id),
            current.getCustomerGroup().id(),
            current.getId(),
        )
    }

    /** Resolve successful arrivals before the rejected-delivery phase. */
    fun deliverAccepted() {
        if (!atDestination()) return
        val current = checkNotNull(order)
        val group = current.getCustomerGroup()
        if (group.deliveryWasGivenUp() || group.hasGivenUp()) return

        for (meal in current.getMeals()) {
            meal.customer.receive(meal, GlobalClock.getTickInEvening())
        }
        Logger.Delivery.deliveryFinished(
            restaurantId,
            checkNotNull(id),
            current.getId(),
            current.getCustomerGroup().id(),
        )
        resolvedOrder = current
        resolvedAsGivenUp = false
        startReturn(checkNotNull(travelTicks))
    }

    /** Successful arrivals are already RETURNING and cannot be rejected here. */
    fun deliverRejected() {
        if (!atDestination()) return
        val current = checkNotNull(order)
        val group = current.getCustomerGroup()
        if (!group.deliveryWasGivenUp() && !group.hasGivenUp()) return

        Logger.Delivery.deliveryFailed(
            restaurantId,
            checkNotNull(id),
            current.getId(),
            current.getCustomerGroup().id(),
        )
        resolvedOrder = current
        resolvedAsGivenUp = true
        startReturn(checkNotNull(travelTicks))
    }

    /** Advance only drivers who were already returning when prepare() ran. */
    fun returnHome() {
        if (!returningAtStartOfTick || state != DriverState.RETURNING) return

        ticksLeft = checkNotNull(ticksLeft) - 1
        if (ticksLeft != 0) return

        Logger.Delivery.deliveryReturned(restaurantId, checkNotNull(id))
        if (switch) abort() else clearDelivery()
    }

    /**
     * Forum #6: the desk reserves this driver for [o] as soon as the first meal is loaded. The
     * driver holds the order and is no longer free, but only drives once every meal has arrived.
     */
    fun assignOrder(o: Order) {
        order = o
        loaded.clear()
        state = DriverState.LOADING
        distance = o.getCustomerGroup().getDeliveryDistance()
        travelTicks = DeliveryService.calculateTravelTicks(checkNotNull(distance))
    }

    /** The meals of the assigned order the waitstaff has not handed over yet. */
    fun pendingMeals(): List<Meal> {
        val current = order ?: return emptyList()
        val outstanding = current.getMeals().toMutableList()
        for (meal in loaded) {
            outstanding.remove(meal)
        }
        return outstanding
    }

    /** Stores [batch] with the driver; the last batch starts the journey ("in the tick after"). */
    fun loadMeals(batch: List<Meal>) {
        val current = order ?: return
        loaded.addAll(batch)
        if (loaded.size < current.getMeals().size) return

        departureTick = GlobalClock.getTickInEvening()
        state = DriverState.DELIVERING
        ticksLeft = checkNotNull(travelTicks)
    }

    /** Whether the whole order is on board, so the desk can stop offering it to the waitstaff. */
    fun hasDeparted(): Boolean = state == DriverState.DELIVERING

    /** Gives up a partly loaded order (the group rejected it) without touching the driver's id. */
    fun releaseLoad() {
        if (state != DriverState.LOADING) return
        clearDelivery()
    }

    /**
     * After getting passed a complete order by a waiter the driver prepares. Kept so the existing
     * unit tests and the integration tests still compile: it is assignOrder plus one full load.
     */
    fun receiveOrder(o: Order) {
        assignOrder(o)
        loadMeals(o.getMeals())
    }

    /**
     * "Based on the meals received by the waitstaff, the drivers that have a full order prepare
     * driving": the waitstaff hands the meals over first, so this line is written after those.
     */
    fun logPreparation() {
        val o = order ?: return
        Logger.Delivery.deliveryPreparation(
            restaurantId,
            checkNotNull(id),
            o.getId(),
            o.getCustomerGroup().id(),
            checkNotNull(travelTicks)
        )
    }

    /** Retain the order during the return trip so phases can still sort by group id. */
    fun currentOrder(): Order? = order

    /**
     * is the driver state waiting?
     */
    fun isFree(): Boolean {
        return state == DriverState.WAITING
    }

    /**
     * resets driver
     */
    fun abort() {
        clearDelivery()
        id = null
    }

    /**
     * resets everything except for the id
     */
    private fun clearDelivery() {
        returningAtStartOfTick = false
        departureTick = null
        order = null
        loaded.clear()
        ticksLeft = null
        state = DriverState.WAITING
        travelTicks = null
        distance = null
        resolvedOrder = null
        resolvedAsGivenUp = false
        switch = false
    }

    /**
     * transition to return
     */
    private fun startReturn(travelTicks: Int) {
        ticksLeft = travelTicks
        state = DriverState.RETURNING
    }

    /**
     * calculates the drivenDistance for logging
     */
    private fun drivenDistance(): Int {
        return if (ticksLeft == 0 && checkNotNull(distance) % TICK_DISTANCE != 0) {
            checkNotNull(distance) % TICK_DISTANCE
        } else {
            TICK_DISTANCE
        }
    }

    /**
     * switch to choose to reset id as well or not
     */
    fun switch() { switch = !switch }

    /**
     * id = null
     */
    fun resetId() { id = null }

    /**
     * bool - the driver holds a complete order and is on the road
     */
    fun isDelivering(): Boolean { return state == DriverState.DELIVERING }

    /**
     * bool - the driver is reserved for an order but is still collecting its meals
     */
    fun isLoading(): Boolean { return state == DriverState.LOADING }

    /**
     * bool
     */
    fun isWaiting(): Boolean { return state == DriverState.WAITING }

    /**
     * bool
     */
    fun isReturning(): Boolean { return state == DriverState.RETURNING }

    /**
     * getter
     */
    fun getRestaurantId() = restaurantId

    /**
     * getter
     */
    fun getDepartureTick() = departureTick

    /**
     * the order this driver resolved (delivered or given up on) this tick, consumed on read
     */
    fun takeResolvedOrder(): Pair<Order, Boolean>? {
        val order = resolvedOrder ?: return null
        val gaveUp = resolvedAsGivenUp
        resolvedOrder = null
        return order to gaveUp
    }

    /**
     * getter
     */
    fun getId() = id

    /**
     * id = input
     */
    fun setId(id: Int) {
        this.id = id
    }
}
