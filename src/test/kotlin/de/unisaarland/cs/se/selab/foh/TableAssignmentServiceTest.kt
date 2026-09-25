package de.unisaarland.cs.se.selab.foh

import de.unisaarland.cs.se.selab.sharedPackage.TableType
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertSame
import kotlin.test.assertTrue

/** F15: table assignment - the exact-fit/larger/merge cascade, the three-quarter rule and merging. */
class TableAssignmentServiceTest {

    private fun serviceWith(vararg tables: Table) = TableAssignmentService(tables.toMutableList())

    @Test
    fun exactFitWinsOverALargerTableThatAlsoQualifies() {
        val exact = Table(1, 4, TableType.COMMON)
        val larger = Table(2, 6, TableType.COMMON)
        val service = serviceWith(larger, exact)

        val chosen = service.assign(4, TableType.COMMON, liftRule = false)

        assertSame(exact, chosen)
        assertEquals(TableStatus.OCCUPIED, exact.status)
        assertEquals(TableStatus.FREE, larger.status)
    }

    @Test
    fun groupOfThreeVsFourAndSixPicksTheSmallestThatCoversThreeQuarters() {
        val four = Table(1, 4, TableType.COMMON)
        val six = Table(2, 6, TableType.COMMON)
        val service = serviceWith(six, four)

        val chosen = service.assign(3, TableType.COMMON, liftRule = false)

        assertSame(four, chosen)
    }

    @Test
    fun groupOfThreeVsASingleTableOfEightFailsTheThreeQuarterRule() {
        val service = serviceWith(Table(1, 8, TableType.COMMON))

        val chosen = service.assign(3, TableType.COMMON, liftRule = false)

        assertNull(chosen)
    }

    @Test
    fun sameTableSameSizeWithLiftRuleTrueIsSeatedAnyway() {
        val service = serviceWith(Table(1, 8, TableType.COMMON))

        val chosen = service.assign(3, TableType.COMMON, liftRule = true)

        assertNotNull(chosen)
        assertEquals(8, chosen.size)
    }

    @Test
    fun tiesOnSizeGoToTheLowestId() {
        val first = Table(1, 5, TableType.COMMON)
        val second = Table(2, 5, TableType.COMMON)
        val service = serviceWith(second, first)

        val chosen = service.assign(4, TableType.COMMON, liftRule = false)

        assertSame(first, chosen)
    }

    @Test
    fun mergesTwoSmallTablesWhenNoSingleTableFits() {
        val service = serviceWith(Table(1, 3, TableType.COMMON), Table(2, 3, TableType.COMMON))

        val chosen = service.assign(6, TableType.COMMON, liftRule = false)

        assertNotNull(chosen)
        assertTrue(chosen.isMerged)
        assertEquals(6, chosen.size)
    }

    @Test
    fun mergeOvershootDropsTheSmallestTableBackOut() {
        val small = Table(1, 2, TableType.COMMON)
        val medium = Table(2, 3, TableType.COMMON)
        val large = Table(3, 4, TableType.COMMON)
        val service = serviceWith(small, medium, large)

        val chosen = service.assign(6, TableType.COMMON, liftRule = false)

        assertNotNull(chosen)
        assertEquals(setOf(2, 3), chosen.originals().map { it.id }.toSet())
        assertEquals(TableStatus.FREE, small.status)
    }

    @Test
    fun mergedTableCarriesTheLowestOriginalId() {
        val service = serviceWith(Table(5, 3, TableType.COMMON), Table(2, 3, TableType.COMMON))

        val chosen = service.assign(6, TableType.COMMON, liftRule = false)

        assertNotNull(chosen)
        assertEquals(2, chosen.id)
    }

    @Test
    fun mergedTableSizeIsTheSum() {
        val service = serviceWith(Table(1, 3, TableType.COMMON), Table(2, 4, TableType.COMMON))

        val chosen = service.assign(7, TableType.COMMON, liftRule = false)

        assertNotNull(chosen)
        assertEquals(7, chosen.size)
    }

    @Test
    fun barTablesNeverMergeEvenWhenSizesWouldSuffice() {
        val service = serviceWith(Table(1, 3, TableType.BAR), Table(2, 3, TableType.BAR))

        val chosen = service.assign(6, TableType.BAR, liftRule = false)

        assertNull(chosen)
    }

    @Test
    fun barTablesNeverMergeEvenWithLiftRule() {
        val service = serviceWith(Table(1, 3, TableType.BAR), Table(2, 3, TableType.BAR))

        val chosen = service.assign(6, TableType.BAR, liftRule = true)

        assertNull(chosen)
    }

    @Test
    fun mergeThatStillFailsThreeQuartersUnderStrictReturnsNull() {
        val service = serviceWith(Table(1, 4, TableType.COMMON), Table(2, 4, TableType.COMMON))

        val chosen = service.assign(5, TableType.COMMON, liftRule = false)

        assertNull(chosen)
    }

    @Test
    fun sameMergeSucceedsWhenLiftRuleLiftsTheThreeQuarterLimit() {
        val service = serviceWith(Table(1, 4, TableType.COMMON), Table(2, 4, TableType.COMMON))

        val chosen = service.assign(5, TableType.COMMON, liftRule = true)

        assertNotNull(chosen)
        assertEquals(8, chosen.size)
    }

    @Test
    fun mergeImpossibleWhenTotalStockIsBelowSizeReturnsNullEvenWithLiftRule() {
        val service = serviceWith(Table(1, 2, TableType.COMMON), Table(2, 2, TableType.COMMON))

        val chosen = service.assign(10, TableType.COMMON, liftRule = true)

        assertNull(chosen)
    }

    @Test
    fun takeMarksTheMergedTableAndEveryOriginalOccupied() {
        val first = Table(1, 3, TableType.COMMON)
        val second = Table(2, 3, TableType.COMMON)
        val service = serviceWith(first, second)

        val chosen = service.assign(6, TableType.COMMON, liftRule = false)

        assertNotNull(chosen)
        assertEquals(TableStatus.OCCUPIED, chosen.status)
        assertEquals(TableStatus.OCCUPIED, first.status)
        assertEquals(TableStatus.OCCUPIED, second.status)
    }

    @Test
    fun releaseFreesAMergedTableAndSplitsItBackImmediately() {
        val first = Table(1, 3, TableType.COMMON)
        val second = Table(2, 3, TableType.COMMON)
        val service = serviceWith(first, second)
        val merged = service.assign(6, TableType.COMMON, liftRule = false)
        checkNotNull(merged)

        service.release(merged)

        assertEquals(TableStatus.FREE, first.status)
        assertEquals(TableStatus.FREE, second.status)
        assertEquals(mapOf(TableType.COMMON to 6), service.freeSeats())
    }

    @Test
    fun releaseOfAnUnmergedTableJustFreesIt() {
        val table = Table(1, 4, TableType.COMMON)
        val service = serviceWith(table)
        service.assign(4, TableType.COMMON, liftRule = false)

        service.release(table)

        assertEquals(TableStatus.FREE, table.status)
        assertFalse(table.isMerged)
    }

    @Test
    fun splitAllMergedRestoresOriginalsFreeAndSortedWithNoDuplicates() {
        val first = Table(3, 2, TableType.COMMON)
        val second = Table(1, 2, TableType.COMMON)
        val service = serviceWith(first, second)
        service.assign(4, TableType.COMMON, liftRule = false)

        service.splitAllMerged()

        assertEquals(mapOf(TableType.COMMON to 4), service.freeSeats())
        assertEquals(TableStatus.FREE, first.status)
        assertEquals(TableStatus.FREE, second.status)
    }

    @Test
    fun freeSeatsGroupsByTypeAndCountsOnlyFreeTables() {
        val service = serviceWith(
            Table(1, 4, TableType.COMMON),
            Table(2, 2, TableType.BAR),
            Table(3, 5, TableType.SEPARATED),
        )
        service.assign(4, TableType.COMMON, liftRule = false)

        assertEquals(mapOf(TableType.BAR to 2, TableType.SEPARATED to 5), service.freeSeats())
    }

    @Test
    fun totalSeatsExcludesMergedTablesSoSeatsAreNotDoubleCounted() {
        val service = serviceWith(Table(1, 3, TableType.COMMON), Table(2, 3, TableType.COMMON))

        assertEquals(6, service.totalSeats())
        service.assign(6, TableType.COMMON, liftRule = false)
        assertEquals(6, service.totalSeats())
    }

    @Test
    fun threeQuarterBoundaryExactlyAtSevenFiftyPercentIsCovered() {
        val service = serviceWith(Table(1, 8, TableType.COMMON))

        val chosen = service.assign(6, TableType.COMMON, liftRule = false)

        assertNotNull(chosen)
    }

    @Test
    fun threeQuarterBoundaryOneSeatBelowSevenFiftyPercentFails() {
        val service = serviceWith(Table(1, 8, TableType.COMMON))

        val chosen = service.assign(5, TableType.COMMON, liftRule = false)

        assertNull(chosen)
    }

    @Test
    fun threeQuarterBoundaryOnATableOfTwentyIsCoveredAtExactlyFifteen() {
        val service = serviceWith(Table(1, 20, TableType.COMMON))

        assertNotNull(service.assign(15, TableType.COMMON, liftRule = false))
    }

    @Test
    fun threeQuarterBoundaryOnATableOfTwentyFailsAtFourteen() {
        val service = serviceWith(Table(1, 20, TableType.COMMON))

        assertNull(service.assign(14, TableType.COMMON, liftRule = false))
    }

    @Test
    fun threeQuarterBoundaryJustAboveSeventyFivePercentIsCovered() {
        val service = serviceWith(Table(1, 10, TableType.COMMON))

        assertNotNull(service.assign(8, TableType.COMMON, liftRule = false))
    }

    @Test
    fun threeQuarterBoundaryJustBelowSeventyFivePercentFails() {
        val service = serviceWith(Table(1, 10, TableType.COMMON))

        assertNull(service.assign(7, TableType.COMMON, liftRule = false))
    }

    @Test
    fun threeQuarterBoundaryOnATableOfFourIsCoveredAtExactlyThree() {
        val service = serviceWith(Table(1, 4, TableType.COMMON))

        assertNotNull(service.assign(3, TableType.COMMON, liftRule = false))
    }

    @Test
    fun threeQuarterBoundaryOnATableOfFourFailsAtTwo() {
        val service = serviceWith(Table(1, 4, TableType.COMMON))

        assertNull(service.assign(2, TableType.COMMON, liftRule = false))
    }

    @Test
    fun originalsOfAnUnmergedTableIsJustItself() {
        val table = Table(1, 4, TableType.COMMON)

        assertEquals(listOf(table), table.originals())
    }

    @Test
    fun isMergedIsFalseForAPlainTable() {
        assertFalse(Table(1, 4, TableType.COMMON).isMerged)
    }

    @Test
    fun ignoresOccupiedAndReservedTables() {
        val occupied = Table(1, 4, TableType.COMMON).apply { status = TableStatus.OCCUPIED }
        val reserved = Table(2, 4, TableType.COMMON).apply { status = TableStatus.RESERVED }
        val free = Table(3, 4, TableType.COMMON)
        val service = serviceWith(occupied, reserved, free)

        val chosen = service.assign(4, TableType.COMMON, liftRule = false)

        assertSame(free, chosen) // Rule 1: All occupied or reserved tables are ignored[cite: 1].
    }

    @Test
    fun multipleExactFitsPicksLowestId() {
        val exactHighId = Table(5, 4, TableType.COMMON)
        val exactLowId = Table(2, 4, TableType.COMMON)
        val service = serviceWith(exactHighId, exactLowId)

        val chosen = service.assign(4, TableType.COMMON, liftRule = false)

        assertSame(exactLowId, chosen) // Tie-breaker: lowest id is chosen first[cite: 1].
    }

    @Test
    fun largerTableSelectionPrioritizesSmallestSizeThenLowestId() {
        // Group size 5. Needs at least 5.
        // 3/4 rule allows up to size 6 (5 >= 6 * 0.75 = 4.5).
        val large = Table(1, 8, TableType.COMMON) // Too large (fails 3/4 rule)
        val perfectSizeHighId = Table(4, 6, TableType.COMMON)
        val perfectSizeLowId = Table(2, 6, TableType.COMMON)
        val slightlyLarger = Table(3, 7, TableType.COMMON)

        val service = serviceWith(large, perfectSizeHighId, perfectSizeLowId, slightlyLarger)

        val chosen = service.assign(5, TableType.COMMON, liftRule = false)

        // Rule: smallest size (6 over 7), then lowest ID (2 over 4)[cite: 1].
        assertSame(perfectSizeLowId, chosen)
    }

    @Test
    fun filtersStrictlyByRequestedTableType() {
        val commonTable = Table(1, 4, TableType.COMMON)
        val separatedTable = Table(2, 4, TableType.SEPARATED)
        val service = serviceWith(commonTable, separatedTable)

        val chosen = service.assign(4, TableType.SEPARATED, liftRule = false)

        assertSame(separatedTable, chosen)
    }

    @Test
    fun complexMergeIterativelyDropsSmallestTablesInAscendingIdOrder() {
        // Group size: 7
        val t1 = Table(1, 3, TableType.COMMON)
        val t2 = Table(2, 3, TableType.COMMON)
        val t3 = Table(3, 5, TableType.COMMON)
        val service = serviceWith(t1, t2, t3)

        // Algorithm:
        // 1. Sort smallest to largest size, then ID: t1(3), t2(3), t3(5)[cite: 1].
        // 2. Add until size >= 7: t1 + t2 + t3 = 11.
        // 3. Size is above 7. Iteratively remove smallest in ascending ID order[cite: 1].
        // 4. Remove t1(3) -> total is 8. Still >= 7.
        // 5. Try removing next smallest, t2(3) -> total would be 5. 5 < 7, so stop dropping[cite: 1].
        // Result should be t2(3) + t3(5) = size 8.
        val chosen = service.assign(7, TableType.COMMON, liftRule = false)

        assertNotNull(chosen)
        assertEquals(8, chosen.size)
        assertEquals(setOf(2, 3), chosen.originals().map { it.id }.toSet())
        assertEquals(2, chosen.id) // Merged table operates on lowest id of original tables[cite: 1].
        assertEquals(TableStatus.FREE, t1.status)
    }
}
