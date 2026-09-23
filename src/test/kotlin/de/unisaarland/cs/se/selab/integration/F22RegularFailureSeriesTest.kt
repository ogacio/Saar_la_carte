package de.unisaarland.cs.se.selab.integration

import de.unisaarland.cs.se.selab.foh.DeliveryDesk
import de.unisaarland.cs.se.selab.foh.FohServices
import de.unisaarland.cs.se.selab.foh.FrontOfTheHouse
import de.unisaarland.cs.se.selab.foh.Table
import de.unisaarland.cs.se.selab.foh.TableAssignmentService
import de.unisaarland.cs.se.selab.foh.Waiter
import de.unisaarland.cs.se.selab.foh.WaiterAssignmentService
import de.unisaarland.cs.se.selab.foh.services.DiningService
import de.unisaarland.cs.se.selab.foh.services.EscortingService
import de.unisaarland.cs.se.selab.foh.services.OrderingService
import de.unisaarland.cs.se.selab.foh.services.RatingService
import de.unisaarland.cs.se.selab.foh.services.SeatingService
import de.unisaarland.cs.se.selab.foh.services.ServingService
import de.unisaarland.cs.se.selab.kitchen.CookRoaster
import de.unisaarland.cs.se.selab.kitchen.CookType
import de.unisaarland.cs.se.selab.kitchen.Kitchen
import de.unisaarland.cs.se.selab.sharedPackage.Menu
import de.unisaarland.cs.se.selab.sharedPackage.Pantry
import de.unisaarland.cs.se.selab.sharedPackage.RestaurantType
import de.unisaarland.cs.se.selab.sharedPackage.TableType
import de.unisaarland.cs.se.selab.simulation.CustomerRegistry
import de.unisaarland.cs.se.selab.simulation.GlobalClock
import de.unisaarland.cs.se.selab.simulation.ReservationBook
import de.unisaarland.cs.se.selab.simulation.SubUnits
import de.unisaarland.cs.se.selab.simulation.ratings.RatingBook
import de.unisaarland.cs.se.selab.testsupport.Fixtures.captureLog
import de.unisaarland.cs.se.selab.testsupport.Fixtures.recipe
import de.unisaarland.cs.se.selab.testsupport.Fixtures.regular
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/** Real reservation, seating, ordering and rating paths feed the same regular's failure streak. */
class F22RegularFailureSeriesTest {
    private val group = regular(1, 2)
    private val registry = CustomerRegistry(mutableListOf(group))

    @BeforeTest
    fun freshRatings() {
        captureLog()
        RatingBook.initializeRatings(1, 0, 0)
    }

    private fun evening(waiters: Int = 1, food: Boolean = true, tableType: TableType = TableType.COMMON) {
        GlobalClock.advanceEvening()
        val tables = TableAssignmentService(mutableListOf(Table(1, 2, tableType)))
        val reservations = ReservationBook(tables)
        val staff = WaiterAssignmentService(MutableList(waiters) { Waiter() })
        val pantry = Pantry(restaurantId = 1)
        val cooks = CookRoaster(mapOf(CookType.EXEC to 1), restaurantId = 1).also { it.initialiseCooks() }
        val kitchen = Kitchen(cooks, pantry, mutableListOf(), reservations, RestaurantType.EUROPEAN)
        val menu = Menu(if (food) mutableListOf(recipe(1)) else mutableListOf(), pantry, kitchen)
        val desk = DeliveryDesk(mutableListOf(), 1)
        val services = FohServices(
            SeatingService(tables, staff, reservations),
            OrderingService(staff, RestaurantType.EUROPEAN),
            ServingService(staff, desk, RestaurantType.EUROPEAN),
            DiningService(),
            EscortingService(staff),
            RatingService(),
        )
        val foh = FrontOfTheHouse(SubUnits(1, menu, pantry, kitchen), tables, reservations, staff, services, desk)
        val evening = GlobalClock.getEvening()
        foh.prepareEvening(evening, registry.regularsFor(1, evening))
        repeat(8) {
            GlobalClock.advanceTick()
            foh.beginTick()
            foh.callSeatingAndOrdering(registry.arriving(1, evening, GlobalClock.getTickInEvening()))
            kitchen.cook()
            foh.callServingService()
            foh.callDiningService()
            foh.callEscortingService()
            foh.callRatingService()
        }
        foh.closeEvening()
        kitchen.closeEvening()
    }

    private fun assertGoneAfterTwoFailures() {
        assertEquals(2, group.failedAttempts())
        assertTrue(group.hasGivenUp())
        evening()
        assertTrue(registry.regularsFor(1, GlobalClock.getEvening() + 1).isEmpty())
        assertEquals(2, RatingBook.getById(1).negativeRatings)
        assertEquals(0, RatingBook.getById(1).positiveRatings)
    }

    @Test
    fun twoFailedReservationsStopFutureVisitsEvenWhenATableBecomesAvailable() {
        repeat(2) { evening(tableType = TableType.BAR) }
        assertGoneAfterTwoFailures()
    }

    @Test
    fun twoEveningsWithoutAWaiterStopFutureVisitsEvenAfterStaffReturns() {
        repeat(2) { evening(waiters = 0) }
        assertGoneAfterTwoFailures()
    }

    @Test
    fun twoWholeGroupsLeavingWithoutFoodStopFutureVisitsEvenWhenFoodReturns() {
        repeat(2) { evening(food = false) }
        assertGoneAfterTwoFailures()
    }

    @Test
    fun successfulVisitBetweenDifferentFailureCausesResetsTheSeriesAndEveryVisitRates() {
        evening(waiters = 0)
        assertEquals(1, group.failedAttempts())
        evening()
        assertEquals(0, group.failedAttempts())
        evening(food = false)
        assertEquals(1, group.failedAttempts())
        assertFalse(group.hasGivenUp())
        evening()
        assertEquals(0, group.failedAttempts())
        assertEquals(2, RatingBook.getById(1).negativeRatings)
        assertEquals(2, RatingBook.getById(1).positiveRatings)
    }
}
