package de.unisaarland.cs.se.selab.integration

/*
 * F21 "FOH - Escorting: escorting customers, **resetting tables**".
 */

import de.unisaarland.cs.se.selab.foh.DeliveryDesk
import de.unisaarland.cs.se.selab.foh.FohServices
import de.unisaarland.cs.se.selab.foh.FrontOfTheHouse
import de.unisaarland.cs.se.selab.foh.Table
import de.unisaarland.cs.se.selab.foh.TableAssignmentService
import de.unisaarland.cs.se.selab.foh.TableStatus
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
import de.unisaarland.cs.se.selab.sharedPackage.Recipe
import de.unisaarland.cs.se.selab.sharedPackage.RestaurantType
import de.unisaarland.cs.se.selab.sharedPackage.TableType
import de.unisaarland.cs.se.selab.sharedPackage.customers.CustomerGroup
import de.unisaarland.cs.se.selab.simulation.GlobalClock
import de.unisaarland.cs.se.selab.simulation.ReservationBook
import de.unisaarland.cs.se.selab.simulation.SubUnits
import de.unisaarland.cs.se.selab.simulation.ratings.RatingBook
import de.unisaarland.cs.se.selab.testsupport.Fixtures.RESTAURANT_ID
import de.unisaarland.cs.se.selab.testsupport.Fixtures.casual
import de.unisaarland.cs.se.selab.testsupport.Fixtures.regular
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals

class F21TableResetTest {

    /** A dish without ingredients, so the pantry always covers it. */
    private val soup = Recipe(1, "soup", 10, setOf(CookType.EXEC), mutableListOf(), RestaurantType.EUROPEAN)

    private lateinit var table: Table
    private lateinit var kitchen: Kitchen
    private lateinit var foh: FrontOfTheHouse

    private fun buildRestaurant() {
        table = Table(1, 2, TableType.COMMON)
        val tables = TableAssignmentService(mutableListOf(table))
        val waitstaff = WaiterAssignmentService(mutableListOf(Waiter()))
        val reservations = ReservationBook(tables)
        val pantry = Pantry(restaurantId = RESTAURANT_ID)
        val roaster = CookRoaster(mapOf(CookType.EXEC to 1), restaurantId = RESTAURANT_ID)
        roaster.initialiseCooks()
        kitchen = Kitchen(roaster, pantry, mutableListOf(), reservations, RestaurantType.EUROPEAN)
        val menu = Menu(mutableListOf(soup), pantry, kitchen)
        val desk = DeliveryDesk(mutableListOf(), RESTAURANT_ID)
        val services = FohServices(
            SeatingService(tables, waitstaff, reservations),
            OrderingService(waitstaff, RestaurantType.EUROPEAN),
            ServingService(waitstaff, desk, RestaurantType.EUROPEAN),
            DiningService(),
            EscortingService(waitstaff),
            RatingService(),
        )
        val sbu = SubUnits(RESTAURANT_ID, menu, pantry, kitchen)
        foh = FrontOfTheHouse(sbu, tables, reservations, waitstaff, services, desk)
    }

    /** One tick of the restaurant, in the order of Restaurant.runRestaurantTick. */
    private fun tick(arrivals: List<CustomerGroup> = emptyList()) {
        GlobalClock.advanceTick()
        foh.beginTick()
        foh.callSeatingAndOrdering(arrivals)
        kitchen.cook()
        foh.callServingService()
        foh.callDeliveryDesk()
        foh.callDiningService()
        foh.callEscortingService()
        foh.callRatingService()
    }

    /** Runs a whole visit: the group arrives, eats and is escorted out. */
    private fun runVisitOf(group: CustomerGroup, regulars: MutableList<CustomerGroup> = mutableListOf()) {
        GlobalClock.advanceEvening()
        RatingBook.initializeRatings(RESTAURANT_ID, 0, 0)
        buildRestaurant()
        foh.prepareEvening(GlobalClock.getEvening(), regulars)
        tick(listOf(group))
        repeat(TICKS_TO_FINISH_A_VISIT) { tick() }
    }

    /** A CASUAL table is cleaned and can take the next group of the evening. */
    @Test
    fun theTableOfACasualGroupIsFreeAgainAfterTheyLeave() {
        runVisitOf(casual(1, 2))

        assertEquals(TableStatus.FREE, table.status, "the table was never cleaned")
    }

    /**
     * A table reserved in the preparation belongs to that group for the whole evening, "even after
     * the group has left the restaurant" - so it must not become free when they go. Whether it
     * stays RESERVED or stays OCCUPIED does not matter; both keep the next group off it, and only
     * FREE would hand it away.
     */
    @Test
    fun theTableOfARegularGroupIsNotGivenAwayWhenTheyLeave() {
        val group = regular(1, 2)
        runVisitOf(group, regulars = mutableListOf(group))

        assertNotEquals(TableStatus.FREE, table.status, "the reserved table was given away too early")
    }

    /** The end of the evening clears everything: the reserved table is free for the next evening. */
    @Test
    fun theEndOfTheEveningReleasesTheReservedTable() {
        val group = regular(1, 2)
        runVisitOf(group, regulars = mutableListOf(group))

        foh.closeEvening()

        assertEquals(TableStatus.FREE, table.status, "the table is still blocked tomorrow")
    }

    private companion object {
        /** Cooking, serving, two ticks of eating, escorting and the rating. */
        const val TICKS_TO_FINISH_A_VISIT = 6
    }
}
