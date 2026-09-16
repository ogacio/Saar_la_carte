package de.unisaarland.cs.se.selab.simulation

import de.unisaarland.cs.se.selab.foh.Table
import de.unisaarland.cs.se.selab.foh.TableAssignmentService
import de.unisaarland.cs.se.selab.foh.TableStatus
import de.unisaarland.cs.se.selab.sharedPackage.TableType
import de.unisaarland.cs.se.selab.testsupport.Fixtures.event
import de.unisaarland.cs.se.selab.testsupport.Fixtures.regular
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertSame
import kotlin.test.assertTrue

/** F14: evening table reservation. */
class ReservationBookTest {

    private fun bookWith(vararg tables: Table) = ReservationBook(TableAssignmentService(tables.toMutableList()))

    @Test
    fun bookAheadAcceptsUntilTheRestaurantIsFullAndCountsPerEvening() {
        val book = bookWith(Table(1, 4, TableType.COMMON), Table(2, 4, TableType.COMMON))

        assertTrue(book.bookAhead(event(1, 5), evening = 4))
        assertTrue(book.bookAhead(event(2, 3), evening = 4))
        assertFalse(book.bookAhead(event(3, 1), evening = 4))
        assertTrue(book.bookAhead(event(4, 8), evening = 5))
    }

    @Test
    fun expectedForIsEmptyWithoutBookingsAndSortedById() {
        val book = bookWith(Table(1, 10, TableType.COMMON))
        book.bookAhead(event(7, 2), evening = 4)
        book.bookAhead(event(3, 2), evening = 4)

        assertEquals(listOf(3, 7), book.expectedFor(4).map { it.id() })
        assertTrue(book.expectedFor(5).isEmpty())
    }

    @Test
    fun openEveningReservesForEventsBeforeRegulars() {
        val book = bookWith(Table(1, 2, TableType.COMMON))
        val eventGroup = event(9, 2)
        val regularGroup = regular(5, 2)
        book.bookAhead(eventGroup, evening = 4)

        val failed = book.openEvening(4, listOf(regularGroup))

        assertEquals(listOf(regularGroup), failed)
        assertNotNull(book.claim(9))
        assertNull(book.claim(5))
    }

    @Test
    fun openEveningHandlesRegularsInAscendingId() {
        val book = bookWith(Table(1, 2, TableType.COMMON))
        val late = regular(8, 2)
        val early = regular(3, 2)

        val failed = book.openEvening(1, listOf(late, early))

        assertEquals(listOf(late), failed)
        assertNotNull(book.claim(3))
    }

    @Test
    fun openEveningLiftsTheThreeQuarterRule() {
        val book = bookWith(Table(1, 8, TableType.COMMON))

        val failed = book.openEvening(1, listOf(regular(1, 2)))

        assertTrue(failed.isEmpty())
        assertEquals(1, book.claim(1)?.id)
    }

    @Test
    fun openEveningMarksAMergedTableAndItsOriginalsReserved() {
        val first = Table(1, 2, TableType.COMMON)
        val second = Table(2, 2, TableType.COMMON)
        val book = bookWith(first, second)

        book.openEvening(1, listOf(regular(1, 4)))

        assertEquals(TableStatus.RESERVED, first.status)
        assertEquals(TableStatus.RESERVED, second.status)
        val merged = assertNotNull(book.claim(1))
        assertTrue(merged.isMerged)
    }

    @Test
    fun claimReturnsNullWithoutReservation() {
        val book = bookWith(Table(1, 2, TableType.COMMON))

        assertNull(book.claim(42))
    }

    @Test
    fun claimMarksTheTableAndItsOriginalsOccupied() {
        val first = Table(1, 2, TableType.COMMON)
        val second = Table(2, 2, TableType.COMMON)
        val book = bookWith(first, second)
        book.openEvening(1, listOf(regular(1, 4)))

        val table = assertNotNull(book.claim(1))

        assertEquals(TableStatus.OCCUPIED, table.status)
        assertEquals(TableStatus.OCCUPIED, first.status)
        assertEquals(TableStatus.OCCUPIED, second.status)
    }

    @Test
    fun claimKeepsTheReservationUntilClearTonight() {
        val book = bookWith(Table(1, 2, TableType.COMMON))
        book.openEvening(1, listOf(regular(1, 2)))

        val firstClaim = book.claim(1)

        assertSame(firstClaim, book.claim(1))
    }

    @Test
    fun clearTonightFreesTablesAndDropsReservations() {
        val table = Table(1, 2, TableType.COMMON)
        val book = bookWith(table)
        book.openEvening(1, listOf(regular(1, 2)))

        book.clearTonight()

        assertEquals(TableStatus.FREE, table.status)
        assertNull(book.claim(1))
    }
}
