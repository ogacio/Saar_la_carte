package de.unisaarland.cs.se.selab.foh.services

import de.unisaarland.cs.se.selab.foh.ActionType
import de.unisaarland.cs.se.selab.foh.DeliveryDesk
import de.unisaarland.cs.se.selab.foh.Table
import de.unisaarland.cs.se.selab.foh.Waiter
import de.unisaarland.cs.se.selab.foh.WaiterAssignmentService
import de.unisaarland.cs.se.selab.foh.visit.AwaitingMealState
import de.unisaarland.cs.se.selab.foh.visit.Visit
import de.unisaarland.cs.se.selab.logging.Logger
import de.unisaarland.cs.se.selab.sharedPackage.Meal
import de.unisaarland.cs.se.selab.sharedPackage.RestaurantType
import de.unisaarland.cs.se.selab.sharedPackage.customers.GroupType
import de.unisaarland.cs.se.selab.simulation.GlobalClock
import de.unisaarland.cs.se.selab.simulation.Statistics
import de.unisaarland.cs.se.selab.simulation.SubUnits

/**
 * Step 3: carries cooked meals to the tables, then brings complete delivery orders
 * to the drivers (spec, "Serving").
 *
 * Whether a table may be served at all is the hold-back rule in [Visit.servableMeals].
 * Which meals go out first is decided here: basic dishes, then ascending recipe id.
 * For EVENT tables the manager ranks the waiters by the cooked meals waiting for their
 * own tables (P03). Handing a meal for a driver is a SERVING action like serving it at a table;
 * which driver takes the order is up to the [DeliveryDesk].
 */
class ServingService(
    private val waitstaff: WaiterAssignmentService,
    private val deliveryDesk: DeliveryDesk,
    private val restaurantType: RestaurantType,
) {

    /** "They prioritize basic dishes, and in a tie sort by ascending recipe id." Tables and drivers alike. */
    private val mealPriority = compareBy<Meal>({ !it.recipe.isBasicFor(restaurantType) }, { it.recipe.getId() })

    /** Serves every table with meals ready, in group order, then the delivery orders, then the summary. */
    fun serve(visits: List<Visit>, sbu: SubUnits) {
        val tick = GlobalClock.currentTick
        val carried = mutableMapOf<Waiter, Int>()

        for (visit in visits) {
            if (visit.state !is AwaitingMealState) continue
            // The kitchen has just cooked (step 2): the first cooked meal starts the hold-back window.
            if (visit.firstMealTick == null && visit.cookedMeals().isNotEmpty()) visit.firstMealTick = tick
            val table = visit.table ?: continue
            serveTable(visit, table, sbu, tick, visits).forEach { (waiter, meals) ->
                carried[waiter] = (carried[waiter] ?: 0) + meals
            }
        }
        promoteCookedDeliveryOrders()
        // "the total number of meals served or delivered to a driver"
        serveDeliveryDesk(carried, sbu)

        Logger.Foh.servingStatus(sbu.restaurantId, carried.size, carried.values.sum())
    }

    /**
     * Serves one table and returns how many meals each waiter carried to it. [visits] are
     * all tables of the restaurant; an EVENT table needs them to rank the waiters.
     */
    private fun serveTable(
        visit: Visit,
        table: Table,
        sbu: SubUnits,
        tick: Int,
        visits: List<Visit>,
    ): Map<Waiter, Int> {
        val queue = visit.servableMeals(tick).sortedWith(mealPriority)
        // EVENT tables: the manager ranks the waiters by the cooked meals waiting for their own tables.
        val cookedMeals = if (visit.group.groupType() == GroupType.EVENT) cookedMealsPerWaiter(visits) else emptyMap()
        val plan = if (queue.isEmpty()) emptyMap() else planWaiters(visit, queue.size, tick, cookedMeals)

        val carried = mutableMapOf<Waiter, Int>()
        var next = 0
        for ((waiter, count) in plan) {
            val meals = queue.drop(next).take(count)
            if (meals.isEmpty()) continue
            next += meals.size
            visit.serve(meals, tick)
            Statistics.record(sbu.restaurantId, meals.size, delivered = false)
            waiter.consume(ActionType.SERVING, meals.size)
            Logger.Foh.serving(
                sbu.restaurantId,
                checkNotNull(waiter.id),
                dishCounts(meals),
                table.id,
                tick - checkNotNull(visit.orderedTick),
            )
            carried[waiter] = meals.size
        }
        // Cooked but not carried: held back for the rest of the table, or the action limit was reached.
        val notServed = visit.cookedMeals().size
        if (notServed > 0) {
            val reporter = plan.keys.lastOrNull() ?: firstWaiterFor(visit, cookedMeals)
            reporter?.id?.let { Logger.Foh.noServing(sbu.restaurantId, it, notServed, table.id) }
        }

        return carried
    }

    /**
     * Who carries how many of the [meals] servable meals.
     *
     * REGULAR and CASUAL: the permanent waiter up to their SERVING capacity. EVENT: the
     * manager's plan over the whole waitstaff, ranked by [cookedMeals]. If the capacity does
     * not cover a complete table, the table waits one tick and is then served one by one.
     */
    private fun planWaiters(visit: Visit, meals: Int, tick: Int, cookedMeals: Map<Waiter, Int>): Map<Waiter, Int> {
        val isEvent = visit.group.groupType() == GroupType.EVENT
        val capacity = if (isEvent) {
            waitstaff.capacity(ActionType.SERVING)
        } else {
            visit.waiters.sumOf { it.remaining(ActionType.SERVING) }
        }
        if (meals > capacity && visit.needsCompleteServing(tick)) {
            visit.waitedForCapacity = true
            return if (isEvent) emptyMap() else visit.waiters.associateWith { 0 }
        }
        val count = minOf(meals, capacity)
        if (isEvent) return waitstaff.assignEvent(count, ActionType.SERVING, cookedMeals).orEmpty()
        val waiter = visit.waiters.firstOrNull() ?: return emptyMap()
        return mapOf(waiter to count)
    }

    /** Moves every delivery order whose meals have all finished cooking from new to ready. */
    private fun promoteCookedDeliveryOrders() {
        for (order in deliveryDesk.getNewOrders().toList()) {
            if (order.allCooked()) deliveryDesk.readyOrder(order)
        }
    }

    /**
     * "For deliveries, the meals queue until all meals of an order are ready and a driver is free.
     * Then, all waiters with a SERVING tick load below the action limit deliver meals to the drivers
     * ... The waiters with the lowest id starts, and the order with the lowest id takes precedence."
     *
     * The delivery desk holds the complete orders and picks the driver. An order is only handed over
     * if the waitstaff can still carry all of its meals this tick; every meal is one SERVING action.
     */
    private fun serveDeliveryDesk(carried: MutableMap<Waiter, Int>, sbu: SubUnits) {
        for (order in deliveryDesk.getReady().sortedBy { it.getId() }) {
            val meals = order.getMeals().sortedWith(mealPriority)
            // Several waiters may carry one order out together. An order that does not fit into
            // this tick waits, but it does not block the smaller orders behind it.
            if (waitstaff.capacity(ActionType.SERVING) < meals.size) continue
            // Without a free driver no order can go out this tick at all.
            val driverId = deliveryDesk.sendForOrder(order) ?: return
            var next = 0
            while (next < meals.size) {
                // The capacity check above guarantees a waiter with SERVING actions left.
                val waiter = checkNotNull(waitstaff.nextServingWaiter())
                val batch = meals.drop(next).take(waiter.remaining(ActionType.SERVING))
                waiter.consume(ActionType.SERVING, batch.size)
                Logger.Foh.deliveryHandover(
                    sbu.restaurantId,
                    checkNotNull(waiter.id),
                    dishCounts(batch),
                    driverId,
                    order.getId(),
                )
                carried[waiter] = (carried[waiter] ?: 0) + batch.size
                next += batch.size
            }
            // deliveryDesk.logPreparationFor(driverId)
        }
    }

    /** "For tables that serve event customer groups, they log ... the first that would have served." */
    private fun firstWaiterFor(visit: Visit, cookedMeals: Map<Waiter, Int>): Waiter? =
        if (visit.group.groupType() == GroupType.EVENT) {
            waitstaff.currentEventWaiter(ActionType.SERVING, cookedMeals)
        } else {
            visit.waiters.firstOrNull()
        }

    /**
     * Cooked meals waiting in the kitchen per waiter, over the tables he is responsible for:
     * "the manager prioritizes the waiters in descending number of cooked meals in the kitchen
     * that belong to their assigned tables".
     */
    private fun cookedMealsPerWaiter(visits: List<Visit>): Map<Waiter, Int> {
        val counts = mutableMapOf<Waiter, Int>()
        for (visit in visits) {
            val waiter = visit.waiters.singleOrNull() ?: continue
            counts[waiter] = (counts[waiter] ?: 0) + visit.cookedMeals().size
        }
        return counts
    }

    /** Dish name to number of meals, as the serving log wants it. */
    private fun dishCounts(meals: List<Meal>): Map<String, Int> =
        meals.groupingBy { it.recipe.getDishName() }.eachCount()
}
