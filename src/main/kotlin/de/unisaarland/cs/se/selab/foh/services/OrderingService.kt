package de.unisaarland.cs.se.selab.foh.services

import de.unisaarland.cs.se.selab.foh.ActionType
import de.unisaarland.cs.se.selab.foh.Waiter
import de.unisaarland.cs.se.selab.foh.WaiterAssignmentService
import de.unisaarland.cs.se.selab.foh.visit.SeatedState
import de.unisaarland.cs.se.selab.foh.visit.Visit
import de.unisaarland.cs.se.selab.logging.Logger
import de.unisaarland.cs.se.selab.sharedPackage.Meal
import de.unisaarland.cs.se.selab.sharedPackage.Order
import de.unisaarland.cs.se.selab.sharedPackage.Recipe
import de.unisaarland.cs.se.selab.sharedPackage.RestaurantType
import de.unisaarland.cs.se.selab.sharedPackage.customers.Customer
import de.unisaarland.cs.se.selab.sharedPackage.customers.GroupType
import de.unisaarland.cs.se.selab.simulation.SubUnits

/**
 * Step 1b, ordering: takes the order of a group right after it was seated (spec, "Ordering").
 *
 * Customers order one by one; each picks a dish by the spec's preference rules, and
 * its ingredients are reserved before the next customer chooses. Customers who find
 * nothing leave. The [Order] is created only once at least one customer ordered,
 * because creating one takes the next id from Order's own static counter.
 *
 * The FOH calls [takeOrder] once per group, right after seating it. The numbers for
 * the status line are collected until [logStatus].
 */
class OrderingService(
    private val waitstaff: WaiterAssignmentService,
    private val restaurantType: RestaurantType,
) {
    private val busyWaiters = mutableSetOf<Waiter>()
    private var customers = 0

    /** Takes the order of [visit] if it was just seated, logs it, and hands it to the kitchen. */
    fun takeOrder(visit: Visit, sbu: SubUnits, tick: Int) {
        if (visit.state !is SeatedState) return
        sbu.menu.refresh()
        val atTable = visit.customersInside().size
        val choices = chooseDishes(visit, sbu)
        if (choices.isEmpty()) {
            Logger.Foh.noOrdering(sbu.restaurantId, visit.group.id(), atTable)
            visit.orderingFailed(tick)
            return
        }
        val waiters = bookWaiters(visit, choices.size)
        val order = buildOrder(visit, sbu, tick, choices)
        Logger.Foh.ordering(
            sbu.restaurantId,
            visit.group.id(),
            order.getId(),
            order.dishCounts(),
            // "the orders are logged ... based on the id of the group, the waiter (one or more) and the order"
            waiters.map { checkNotNull(it.id) },
        )
        if (choices.size < atTable) {
            Logger.Foh.noOrdering(sbu.restaurantId, visit.group.id(), atTable - choices.size)
        }
        visit.ordered(order, tick)
        sbu.kitchen.enqueue(order)
        busyWaiters += waiters
        customers += choices.size
    }

    /** Writes the ordering summary of this tick and starts counting afresh. */
    fun logStatus(sbu: SubUnits) {
        Logger.Foh.orderingStatus(sbu.restaurantId, customers, busyWaiters.size)
        busyWaiters.clear()
        customers = 0
    }

    /**
     * The customers at the table choose one by one: most excluded ingredients first,
     * then fewest favourite dishes; ties keep the order of the group's food preferences
     * (the sort is stable). Returns who ordered what; customers who found nothing have left.
     */
    private fun chooseDishes(visit: Visit, sbu: SubUnits): List<Pair<Customer, Recipe>> {
        val choices = mutableListOf<Pair<Customer, Recipe>>()
        val customers = visit.customersInside().sortedWith(
            compareByDescending<Customer> { it.preference()?.excluded()?.size ?: 0 }
                .thenBy { it.preference()?.favouriteDishNames()?.size ?: 0 },
        )
        for (customer in customers) {
            val dish = chooseFor(customer, visit, sbu)
            if (dish == null || !sbu.pantry.reserve(dish)) {
                visit.leaveUnserved(listOf(customer))
                continue
            }
            customer.choose(dish)
            choices += customer to dish
            sbu.menu.refresh()
        }
        return choices
    }

    /**
     * The dish rules: only dishes the customer fully eats; among those the event's own
     * favourite dish first, then the customer's first favourite, then the most preferred
     * ingredients (counted, not weighed). Ties, and customers without preferences, go to
     * the highest recipe id.
     */
    private fun chooseFor(customer: Customer, visit: Visit, sbu: SubUnits): Recipe? {
        val preference = customer.preference()
        val edible = sbu.menu.getOrderables().filter { preference == null || preference.accepts(it) }
        val eventDish = visit.group.dishOverride(restaurantType)
        return edible.firstOrNull { it.getDishName() == eventDish }
            ?: preference?.firstFavourite(edible)
            ?: edible.maxWithOrNull(compareBy({ preference?.rank(it) ?: 0 }, { it.getId() }))
    }

    /**
     * Creates the order with one queued meal per customer who chose a dish. The meals are built first,
     * because the order only exposes them read-only; they get the order's id once Order has assigned it.
     */
    private fun buildOrder(visit: Visit, sbu: SubUnits, tick: Int, choices: List<Pair<Customer, Recipe>>): Order {
        val meals = choices.map { (customer, recipe) -> Meal(null, customer, recipe) }.toMutableList()
        val order = Order(
            visit.group,
            sbu.restaurantId,
            visit.group.id(),
            tick,
            isDelivery = false,
            meals = meals,
        )
        meals.forEach { it.orderId = order.getId() }
        return order
    }

    /**
     * REGULAR and CASUAL: "the waiter who seated them also TAKE THEIR ORDERS".
     * EVENT: the manager's plan for [customers] orders.
     */
    private fun bookWaiters(visit: Visit, customers: Int): List<Waiter> {
        if (visit.group.groupType() != GroupType.EVENT) {
            visit.waiters.forEach { it.consume(ActionType.ORDERING, customers) }
            return visit.waiters
        }
        val plan = waitstaff.assignEvent(customers, ActionType.ORDERING).orEmpty()
        plan.forEach { (waiter, count) -> waiter.consume(ActionType.ORDERING, count) }
        return plan.keys.toList()
    }
}
