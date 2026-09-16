package de.unisaarland.cs.se.selab.integration

import de.unisaarland.cs.se.selab.foh.Table
import de.unisaarland.cs.se.selab.foh.TableAssignmentService
import de.unisaarland.cs.se.selab.foh.TableStatus
import de.unisaarland.cs.se.selab.foh.Waiter
import de.unisaarland.cs.se.selab.foh.WaiterAssignmentService
import de.unisaarland.cs.se.selab.foh.services.DiningService
import de.unisaarland.cs.se.selab.foh.services.EscortingService
import de.unisaarland.cs.se.selab.foh.services.RatingService
import de.unisaarland.cs.se.selab.foh.services.SeatingService
import de.unisaarland.cs.se.selab.foh.visit.Visit
import de.unisaarland.cs.se.selab.logging.LogLevel
import de.unisaarland.cs.se.selab.sharedPackage.Meal
import de.unisaarland.cs.se.selab.sharedPackage.MealStatus
import de.unisaarland.cs.se.selab.sharedPackage.Order
import de.unisaarland.cs.se.selab.sharedPackage.TableType
import de.unisaarland.cs.se.selab.sharedPackage.customers.CustomerGroup
import de.unisaarland.cs.se.selab.simulation.CustomerRegistry
import de.unisaarland.cs.se.selab.simulation.GlobalClock
import de.unisaarland.cs.se.selab.simulation.ReservationBook
import de.unisaarland.cs.se.selab.simulation.Statistics
import de.unisaarland.cs.se.selab.simulation.SubUnits
import de.unisaarland.cs.se.selab.simulation.ratings.RatingBook
import de.unisaarland.cs.se.selab.testsupport.Fixtures.captureLog
import de.unisaarland.cs.se.selab.testsupport.Fixtures.logLines
import de.unisaarland.cs.se.selab.testsupport.Fixtures.recipe
import de.unisaarland.cs.se.selab.testsupport.Fixtures.regular
import org.mockito.kotlin.mock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * Integration of the front of the house over one visit: reservation (F14), seating (F16), eating,
 * escorting (F21), rating (P05) and statistics (F07), with the real tables, waiters, visit states,
 * customers and rating book.
 *
 * Ordering and serving are driven directly on the visit, because OrderingService.buildOrder and
 * ServingService are not usable yet. Menu, pantry and kitchen are placeholders: none of the
 * services used here touches them.
 */
class FohVisitIntegrationTest {

    private class Restaurant(id: Int, vararg tableSizes: Int) {
        val tables = TableAssignmentService(
            tableSizes.mapIndexed { index, size -> Table(index + 1, size, TableType.COMMON) }.toMutableList(),
        )
        val waiter = Waiter()
        val waitstaff = WaiterAssignmentService(mutableListOf(waiter))
        val reservations = ReservationBook(tables)
        val ratings = RatingBook.also { it.initializeRatings(id, 0, 0) }
        val sbu = SubUnits(id, mock(), mock(), mock())
        val seating = SeatingService(tables, waitstaff, reservations)
        val dining = DiningService()
        val escorting = EscortingService(waitstaff)
        val rating = RatingService()
    }

    /** Advances the clock to the start of the next evening and returns its first global tick. */
    private fun newEvening(): Int {
        GlobalClock.advanceEvening()
        GlobalClock.advanceTick()
        return GlobalClock.currentTick
    }

    /** Advances the clock by [ticks] ticks. */
    private fun wait(ticks: Int) = repeat(ticks) { GlobalClock.advanceTick() }

    /** Places an order for every member at [tick] and lets the kitchen finish it at once. */
    private fun orderAndCook(visit: Visit, group: CustomerGroup, restaurantId: Int, tick: Int): List<Meal> {
        val order = Order(1, group, restaurantId, group.id(), tick, false, mutableListOf())
        group.members().forEach { order.meals.add(Meal(order.id, it, recipe(1))) }
        visit.ordered(order, tick)
        order.meals.forEach {
            it.status = MealStatus.COOKED
            visit.mealCooked(it, tick)
        }
        return order.meals
    }

    @Test
    fun regularGroupVisitFromReservationToPositiveRating() {
        val restaurant = Restaurant(801, 2)
        val group = regular(1, 2, restaurantId = 801)
        restaurant.reservations.openEvening(1, listOf(group))
        val visit = Visit(group)
        val log = captureLog(LogLevel.INFO)

        val start = newEvening()
        restaurant.seating.seat(visit, restaurant.sbu, start)
        val meals = orderAndCook(visit, group, 801, tick = start)
        visit.serve(meals, start + 1)
        wait(2)
        restaurant.dining.eat(listOf(visit), restaurant.sbu)
        wait(1)
        restaurant.dining.eat(listOf(visit), restaurant.sbu)
        restaurant.escorting.escort(listOf(visit), restaurant.sbu)
        restaurant.rating.rate(visit, restaurant.sbu)
        Statistics.record(801, visit.servedCustomers(), delivered = false)

        assertEquals(
            listOf(
                "[IMPORTANT] FOH Seating (R 801): Group 1 seated at table 1 by waitstaff 1.",
                "[INFO] FOH Finished Eating (R 801): 2 customers of group 1 have finished eating at table 1.",
                "[IMPORTANT] FOH Escorting (R 801): Waitstaff 1 escorts 2 customers of group 1 from table 1 outside.",
                "[INFO] Rating (R 801): Group 1 rates the restaurant 801 with POSITIVE rating, " +
                    "leading to 1 positive ratings and 0 negative ratings.",
            ),
            logLines(log),
        )
        assertTrue(visit.isFinished())
        assertEquals(0, restaurant.waiter.currentLoad)
        assertEquals(TableStatus.OCCUPIED, restaurant.reservations.claim(1)?.status)

        val stats = captureLog(LogLevel.IMPORTANT)
        Statistics.report(listOf(801))
        assertEquals(
            listOf(
                "[IMPORTANT] Simulation Statistics: Restaurant 801 served 2 customers.",
                "[IMPORTANT] Simulation Statistics: Restaurant 801 received 1 ratings.",
            ),
            logLines(stats).filter { it.contains("served") || it.contains("received") },
        )
    }

    @Test
    fun unservedGroupLeavesAfterFiveTicksAndRatesNegative() {
        val restaurant = Restaurant(802, 2)
        val group = regular(1, 2, restaurantId = 802)
        val visit = Visit(group)
        val start = newEvening()
        restaurant.seating.seat(visit, restaurant.sbu, start)
        val order = Order(2, group, 802, group.id(), start, false, mutableListOf())
        group.members().forEach { order.meals.add(Meal(order.id, it, recipe(1))) }
        visit.ordered(order, start)
        val log = captureLog(LogLevel.INFO)

        wait(5)
        restaurant.dining.eat(listOf(visit), restaurant.sbu)
        restaurant.rating.rate(visit, restaurant.sbu)

        assertEquals(
            listOf(
                "[INFO] Restaurant No Eating (R 802): 2 customers of group 1 leave table 1 due to not being served.",
                "[INFO] Rating (R 802): Group 1 rates the restaurant 802 with NEGATIVE rating, " +
                    "leading to 0 positive ratings and 1 negative ratings.",
            ),
            logLines(log),
        )
        assertTrue(visit.isFinished())
        assertTrue(order.meals.all { it.status == MealStatus.ABORTED })
        assertEquals(1, group.failedAttempts())
    }

    @Test
    fun regularGroupWithoutTableTwiceStopsVisiting() {
        val restaurant = Restaurant(803, 2)
        val owner = regular(1, 2, restaurantId = 803)
        val unlucky = regular(2, 2, restaurantId = 803)
        val registry = CustomerRegistry(mutableListOf(owner, unlucky))
        captureLog()

        for (evening in 1..2) {
            restaurant.reservations.clearTonight()
            val failed = restaurant.reservations.openEvening(evening, registry.regularsFor(803, evening))
            failed.forEach { restaurant.rating.rateFailedReservation(it, restaurant.sbu) }
        }

        assertTrue(unlucky.hasGivenUp())
        assertEquals(listOf(1), registry.regularsFor(803, 3).map { it.id() })
        assertEquals(2, restaurant.ratings.getById(803).negativeRatings)
    }
}
