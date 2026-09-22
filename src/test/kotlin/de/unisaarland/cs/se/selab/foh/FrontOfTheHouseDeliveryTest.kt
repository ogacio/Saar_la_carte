package de.unisaarland.cs.se.selab.foh

import de.unisaarland.cs.se.selab.foh.services.DiningService
import de.unisaarland.cs.se.selab.foh.services.EscortingService
import de.unisaarland.cs.se.selab.foh.services.OrderingService
import de.unisaarland.cs.se.selab.foh.services.RatingService
import de.unisaarland.cs.se.selab.foh.services.SeatingService
import de.unisaarland.cs.se.selab.foh.services.ServingService
import de.unisaarland.cs.se.selab.sharedPackage.Meal
import de.unisaarland.cs.se.selab.sharedPackage.Order
import de.unisaarland.cs.se.selab.sharedPackage.RestaurantType
import de.unisaarland.cs.se.selab.sharedPackage.TableType
import de.unisaarland.cs.se.selab.sharedPackage.customers.CasualCustomerGroup
import de.unisaarland.cs.se.selab.simulation.GlobalClock
import de.unisaarland.cs.se.selab.simulation.ReservationBook
import de.unisaarland.cs.se.selab.simulation.ratings.RatingBook
import de.unisaarland.cs.se.selab.simulation.ratings.RatingLikelihood
import de.unisaarland.cs.se.selab.testsupport.Fixtures.RESTAURANT_ID
import de.unisaarland.cs.se.selab.testsupport.Fixtures.captureLog
import de.unisaarland.cs.se.selab.testsupport.Fixtures.casual
import de.unisaarland.cs.se.selab.testsupport.Fixtures.logLines
import de.unisaarland.cs.se.selab.testsupport.Fixtures.recipe
import de.unisaarland.cs.se.selab.testsupport.Fixtures.subUnits
import java.io.StringWriter
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/**
 * F20, F30: a delivery after the driver reached the group. A delivered group eats and then rates by when the
 * meal arrived compared to its visiting tick; a group that gave up rates negatively without eating; and the
 * end of the evening forgets groups still eating. A real driver runs through the front of house's delivery
 * and rating steps, one tick at a time.
 */
class FrontOfTheHouseDeliveryTest {

    private val tables = TableAssignmentService(mutableListOf(Table(1, 2, TableType.COMMON)))
    private val reservations = ReservationBook(tables)
    private val waitstaff = WaiterAssignmentService(mutableListOf(Waiter()))
    private val driver = DeliveryDriver(RESTAURANT_ID).also { it.setId(1) }
    private val desk = DeliveryDesk(mutableListOf(driver), RESTAURANT_ID)
    private val foh = FrontOfTheHouse(
        subUnits(),
        tables,
        reservations,
        waitstaff,
        FohServices(
            SeatingService(tables, waitstaff, reservations),
            OrderingService(waitstaff, RestaurantType.EUROPEAN),
            ServingService(waitstaff, desk, RestaurantType.EUROPEAN),
            DiningService(),
            EscortingService(waitstaff),
            RatingService(),
        ),
        desk,
    )

    @BeforeTest
    fun freshEveningAndRatings() {
        GlobalClock.advanceEvening()
        GlobalClock.advanceTick()
        RatingBook.initializeRatings(RESTAURANT_ID, 0, 0)
    }

    /** A one-person delivery group that rates only clearly positive or negative experiences. */
    private fun deliveryGroup(distance: Int, visitingTick: Int) = casual(
        GROUP,
        1,
        deliveryDistance = distance,
        likelihood = RatingLikelihood.SOME,
        visitingTick = visitingTick,
    )

    /** The driver leaves in the current tick with the group's order. */
    private fun sendOut(group: CasualCustomerGroup) {
        val meals = mutableListOf(Meal(null, group.members()[0], recipe(1)))
        group.orderPlaced()
        driver.receiveOrder(Order(group, RESTAURANT_ID, group.id(), 1, true, meals))
    }

    /** Runs the delivery and rating steps of the next [ticks] ticks and returns what they logged. */
    private fun runTicks(ticks: Int): List<String> {
        val log: StringWriter = captureLog()
        repeat(ticks) {
            GlobalClock.advanceTick()
            foh.callDeliveryDesk()
            foh.callDiningService()
            foh.callRatingService()
        }
        return logLines(log)
    }

    private fun finishedEating(log: List<String>) =
        log.count { it == "[INFO] Delivery Finished Eating (R 1): Group $GROUP has finished eating." }

    private fun ratings(log: List<String>) = log.filter { it.contains("Group $GROUP rates the restaurant") }

    @Test
    fun mealArrivingBeforeTheVisitingTickIsEatenAndRatedPositive() {
        sendOut(deliveryGroup(distance = 5, visitingTick = 10))

        val log = runTicks(4)

        assertEquals(1, finishedEating(log))
        assertEquals(1, ratings(log).size)
        assertTrue(ratings(log).single().contains("with POSITIVE rating"))
    }

    @Test
    fun theGroupOnlyRatesOnceEveryMemberHasFinishedEating() {
        sendOut(deliveryGroup(distance = 5, visitingTick = 10))

        // Arrives in the first tick, eats in the second, is done in the third.
        val whileEating = runTicks(2)
        val done = runTicks(1)

        assertEquals(0, finishedEating(whileEating))
        assertTrue(ratings(whileEating).isEmpty())
        assertEquals(1, finishedEating(done))
    }

    @Test
    fun mealArrivingExactlyAtTheVisitingTickIsANeutralExperience() {
        val arrival = GlobalClock.getTickInEvening() + 1
        sendOut(deliveryGroup(distance = 5, visitingTick = arrival))

        val log = runTicks(4)

        // A neutral experience leaves no rating for a group that rates only SOME experiences.
        assertEquals(1, finishedEating(log))
        assertTrue(ratings(log).isEmpty())
    }

    @Test
    fun mealArrivingAfterTheVisitingTickButBeforeGivingUpIsRatedNegative() {
        val visitingTick = GlobalClock.getTickInEvening()
        // Two ticks of driving: the meal arrives after the visiting tick, but within the three ticks of patience.
        sendOut(deliveryGroup(distance = 10, visitingTick = visitingTick))

        val log = runTicks(5)

        assertEquals(1, finishedEating(log))
        assertTrue(ratings(log).single().contains("with NEGATIVE rating"))
    }

    @Test
    fun groupThatGaveUpRatesNegativeWithoutEating() {
        val group = deliveryGroup(distance = 30, visitingTick = GlobalClock.getTickInEvening())
        sendOut(group)

        val log = runTicks(8)

        assertTrue(log.any { it.startsWith("[IMPORTANT] Delivery Failed (R 1): Driver 1 failed to deliver order") })
        assertEquals(0, finishedEating(log))
        assertTrue(ratings(log).single().contains("with NEGATIVE rating"))
        assertFalse(group.hasGivenUp())
    }

    @Test
    fun aGroupThatGivesUpWhileTheDriverIsStillOnTheRoadRatesImmediately() {
        val visitingTick = GlobalClock.getTickInEvening()
        sendOut(deliveryGroup(distance = 30, visitingTick = visitingTick))

        val beforeDeadline = runTicks(3)
        assertTrue(beforeDeadline.none { it.contains("Delivery Given Up") })
        assertTrue(ratings(beforeDeadline).isEmpty())

        val giveUpTick = runTicks(1)
        assertTrue(giveUpTick.any { it.contains("Delivery Given Up") })
        assertTrue(giveUpTick.none { it.contains("Delivery Failed") })
        assertEquals(1, ratings(giveUpTick).size)
        assertTrue(ratings(giveUpTick).single().contains("with NEGATIVE rating"))
    }

    @Test
    fun theEndOfTheEveningForgetsDeliveryGroupsThatAreStillEating() {
        sendOut(deliveryGroup(distance = 5, visitingTick = 10))
        runTicks(1)

        foh.closeEvening()
        val log = runTicks(3)

        assertEquals(0, finishedEating(log))
        assertTrue(ratings(log).isEmpty())
    }

    private companion object {
        const val GROUP = 7
    }
}
