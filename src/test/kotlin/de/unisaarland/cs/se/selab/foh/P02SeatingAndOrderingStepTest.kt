package de.unisaarland.cs.se.selab.foh

import de.unisaarland.cs.se.selab.foh.services.DiningService
import de.unisaarland.cs.se.selab.foh.services.EscortingService
import de.unisaarland.cs.se.selab.foh.services.OrderingService
import de.unisaarland.cs.se.selab.foh.services.RatingService
import de.unisaarland.cs.se.selab.foh.services.SeatingService
import de.unisaarland.cs.se.selab.foh.services.ServingService
import de.unisaarland.cs.se.selab.sharedPackage.RestaurantType
import de.unisaarland.cs.se.selab.sharedPackage.TableType
import de.unisaarland.cs.se.selab.simulation.ReservationBook
import de.unisaarland.cs.se.selab.simulation.ratings.RatingBook
import de.unisaarland.cs.se.selab.testsupport.Fixtures.RESTAURANT_ID
import de.unisaarland.cs.se.selab.testsupport.Fixtures.captureLog
import de.unisaarland.cs.se.selab.testsupport.Fixtures.casual
import de.unisaarland.cs.se.selab.testsupport.Fixtures.logLines
import de.unisaarland.cs.se.selab.testsupport.Fixtures.regular
import de.unisaarland.cs.se.selab.testsupport.Fixtures.subUnits
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * P02, the seating-and-ordering step of a tick. Two rules meet here: "This step is performed for
 * both restaurant visits and deliveries", so a delivery orders at its place in the group order,
 * and "A restaurant does not accept new customers in the last 3 ticks of their opening time",
 * which also refuses the retry of a group that arrived earlier (forum 345).
 */
class P02SeatingAndOrderingStepTest {

    private val tables = TableAssignmentService(
        mutableListOf(Table(1, 2, TableType.COMMON), Table(2, 2, TableType.COMMON)),
    )
    private val reservations = ReservationBook(tables)
    private val desk = mock<DeliveryDesk>().also {
        whenever(it.getDrivers()).thenReturn(emptyList())
        whenever(it.getReady()).thenReturn(emptyList())
        whenever(it.getNewOrders()).thenReturn(emptyList())
    }

    private fun frontOfHouse(waiterCount: Int = 2): FrontOfTheHouse {
        val waitstaff = WaiterAssignmentService(MutableList(waiterCount) { Waiter() })
        return FrontOfTheHouse(
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
    }

    @BeforeTest
    fun freshRatings() {
        RatingBook.initializeRatings(RESTAURANT_ID, 0, 0)
    }

    @Test
    fun aDeliveryOrdersBeforeAnInHouseGroupWithAHigherId() {
        val foh = frontOfHouse()
        val placed = mutableListOf<Int>()
        val delivery = casual(1, 2, deliveryDistance = 5)
        val walkIn = casual(5, 2)
        foh.prepareEvening(1, mutableListOf())

        foh.callSeatingAndOrdering(
            arrivals = listOf(walkIn),
            deliveries = listOf(delivery),
        ) { group ->
            placed += group.id()
            group.groupSize()
        }

        assertEquals(listOf(1), placed, "the delivery group has the lower id, so it orders first")
    }

    @Test
    fun aDeliveryWithAHigherIdOrdersAfterEveryInHouseGroup() {
        val foh = frontOfHouse()
        val placed = mutableListOf<Int>()
        val delivery = casual(9, 2, deliveryDistance = 5)
        foh.prepareEvening(1, mutableListOf())

        foh.callSeatingAndOrdering(
            arrivals = listOf(casual(2, 2)),
            deliveries = listOf(delivery),
        ) { group ->
            placed += group.id()
            group.groupSize()
        }

        assertEquals(listOf(9), placed, "it still orders, in the trailing pass after the tables")
    }

    @Test
    fun everyWaitingDeliveryOrdersEvenWithoutAnyArrivals() {
        val foh = frontOfHouse()
        val placed = mutableListOf<Int>()
        foh.prepareEvening(1, mutableListOf())

        foh.callSeatingAndOrdering(
            arrivals = emptyList(),
            deliveries = listOf(casual(4, 1, deliveryDistance = 5), casual(2, 1, deliveryDistance = 5)),
        ) { group ->
            placed += group.id()
            group.groupSize()
        }

        assertEquals(listOf(2, 4), placed, "in ascending group id")
    }

    @Test
    fun deliveriesAndTablesInterleaveByGroupTypeThenId() {
        val foh = frontOfHouse()
        val order = mutableListOf<String>()
        foh.prepareEvening(1, mutableListOf(regular(3, 2)))
        val log = captureLog()

        foh.callSeatingAndOrdering(
            arrivals = listOf(regular(3, 2), casual(8, 2)),
            deliveries = listOf(casual(6, 1, deliveryDistance = 5)),
        ) { group ->
            order += "delivery ${group.id()}"
            group.groupSize()
        }

        val seated = logLines(log).filter { it.contains("seated at table") }
        assertEquals(listOf("delivery 6"), order)
        assertTrue(seated.isNotEmpty(), "the in-house groups were still seated: $seated")
    }

    @Test
    fun inTheLastThreeTicksANewArrivalIsAnnouncedButNotSeated() {
        val foh = frontOfHouse()
        val group = casual(7, 2)
        foh.prepareEvening(1, mutableListOf())
        val log = captureLog()

        foh.callSeatingAndOrdering(listOf(group), acceptsCustomers = false)

        assertTrue(logLines(log).any { it.contains("Group 7 arrived") }, "it still turns up at the door")
        assertTrue(
            logLines(log).none { it.contains("seated at table") },
            "but nobody is seated; the Seating Status summary line is still written",
        )
    }

    @Test
    fun inTheLastThreeTicksAGroupFromAnEarlierTickIsSentAway() {
        val foh = frontOfHouse(waiterCount = 0)
        val group = casual(3, 2)
        foh.prepareEvening(1, mutableListOf())
        foh.callSeatingAndOrdering(listOf(group)) // arrives, no waiter, waits for the next tick

        val log = captureLog()
        foh.callSeatingAndOrdering(emptyList(), acceptsCustomers = false)

        assertTrue(
            logLines(log).none { it.contains("Group 3 arrived") },
            "forum 345: it does not arrive a second time",
        )
        foh.callRatingService()
        assertEquals(1, RatingBook.getById(RESTAURANT_ID).negativeRatings, "being refused rates negative")
    }

    @Test
    fun aGroupCancelledInThePreparationNeverArrives() {
        val foh = frontOfHouse()
        val tooLarge = regular(1, 6)
        foh.prepareEvening(1, mutableListOf(tooLarge))
        val log = captureLog()

        foh.callSeatingAndOrdering(listOf(tooLarge), deliveries = listOf(casual(2, 1, deliveryDistance = 5))) { 1 }

        assertTrue(logLines(log).none { it.contains("Group 1 arrived") })
    }
}
