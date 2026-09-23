package de.unisaarland.cs.se.selab.simulation

import de.unisaarland.cs.se.selab.foh.DeliveryDesk
import de.unisaarland.cs.se.selab.foh.FrontOfTheHouse
import de.unisaarland.cs.se.selab.foh.Table
import de.unisaarland.cs.se.selab.foh.TableAssignmentService
import de.unisaarland.cs.se.selab.kitchen.Kitchen
import de.unisaarland.cs.se.selab.sharedPackage.Meal
import de.unisaarland.cs.se.selab.sharedPackage.Menu
import de.unisaarland.cs.se.selab.sharedPackage.Order
import de.unisaarland.cs.se.selab.sharedPackage.Pantry
import de.unisaarland.cs.se.selab.sharedPackage.RestaurantData
import de.unisaarland.cs.se.selab.sharedPackage.RestaurantType
import de.unisaarland.cs.se.selab.sharedPackage.TableType
import de.unisaarland.cs.se.selab.sharedPackage.customers.CustomerGroup
import de.unisaarland.cs.se.selab.testsupport.Fixtures.RESTAURANT_ID
import de.unisaarland.cs.se.selab.testsupport.Fixtures.recipe
import de.unisaarland.cs.se.selab.testsupport.Fixtures.regular
import org.mockito.kotlin.any
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * P02/F09: what the restaurant hands the kitchen at preparation time.
 *
 * "The first planning step is for the known REGULAR groups, so those REGULAR groups that already
 * have visited the restaurant at least once." Everyone else is guessed at from seats: the tables
 * still free plus the tables held by regulars the kitchen cannot predict yet (forum 328).
 */
class P02PrepareSeatEstimateTest {

    private val kitchen = mock<Kitchen>()
    private val pantry = mock<Pantry>()
    private val menu = mock<Menu>()
    private val foh = mock<FrontOfTheHouse>()
    private val tables = TableAssignmentService(
        mutableListOf(Table(1, 4, TableType.COMMON), Table(2, 4, TableType.COMMON), Table(3, 2, TableType.COMMON)),
    )
    private val reservations = ReservationBook(tables)

    init {
        whenever(foh.getTables()).thenReturn(tables)
        whenever(foh.getReservationBook()).thenReturn(reservations)
        whenever(foh.getDeliveryDesk()).thenReturn(mock<DeliveryDesk>())
        whenever(menu.getRecipes()).thenReturn(emptyList())
    }

    private fun restaurant() = Restaurant(
        "Seat Estimate", true, 0, 0, foh, kitchen, pantry, menu,
        RestaurantData(
            RESTAURANT_ID, RestaurantType.ASIAN, 1, 24, emptyList(), mapOf(), 0, true, 10, mapOf(),
        ),
    )

    /**
     * Gives [group] a remembered visit it actually ordered on. An empty order would make it known
     * but not "has ordered before", which is the forum 328 distinction the planning filter uses.
     */
    private fun withAVisitBehindIt(group: CustomerGroup): CustomerGroup {
        val meals = mutableListOf(Meal(null, group.members().first(), recipe(1)))
        group.recordVisit(1, Order(group, RESTAURANT_ID, group.id(), 1, false, meals))
        return group
    }

    private fun capturedOtherSeats(regulars: MutableList<CustomerGroup>): Pair<List<CustomerGroup>, Int> {
        GlobalClock.advanceEvening()
        reservations.openEvening(GlobalClock.getEvening(), regulars)
        restaurant().prepare(regulars)
        val groups = argumentCaptor<MutableList<CustomerGroup>>()
        val seats = argumentCaptor<Int>()
        verify(kitchen).planEvening(groups.capture(), seats.capture(), any())
        return groups.firstValue to seats.firstValue
    }

    @Test
    fun aRegularThatHasNeverOrderedIsGuessedAtLikeAFreeTable() {
        val newcomer = regular(1, 4)

        val (planned, otherSeats) = capturedOtherSeats(mutableListOf(newcomer))

        assertEquals(emptyList(), planned, "forum 328: nothing is known about its order yet")
        assertEquals(10, otherSeats, "its 4-seat table counts as guesswork, like the 4 + 2 still free")
    }

    @Test
    fun aKnownRegularIsPlannedForAndItsTableLeavesTheEstimate() {
        val known = withAVisitBehindIt(regular(1, 4))

        val (planned, otherSeats) = capturedOtherSeats(mutableListOf(known))

        assertEquals(listOf(known), planned, "the kitchen cooks its last three visits")
        assertEquals(6, otherSeats, "only the two tables nobody reserved are guessed at")
    }

    @Test
    fun aKnownAndAnUnknownRegularSplitTheEstimate() {
        val known = withAVisitBehindIt(regular(1, 4))
        val newcomer = regular(2, 4)

        val (planned, otherSeats) = capturedOtherSeats(mutableListOf(known, newcomer))

        assertEquals(listOf(known), planned)
        assertEquals(6, otherSeats, "the newcomer's 4 seats plus the 2 still free")
    }

    @Test
    fun withNobodyReservingEverySeatIsGuessedAt() {
        val (planned, otherSeats) = capturedOtherSeats(mutableListOf())

        assertEquals(emptyList(), planned)
        assertEquals(10, otherSeats, "all three tables are free")
    }
}
