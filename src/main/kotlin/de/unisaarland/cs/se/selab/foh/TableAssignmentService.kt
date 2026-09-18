package de.unisaarland.cs.se.selab.foh

import de.unisaarland.cs.se.selab.sharedPackage.TableType

/**
 * Assigns tables to customer groups and keeps track of which ones are taken.
 *
 * The assignment follows the four criteria of the specification in order: an exact fit, a larger
 * table the group fills to at least three quarters, and otherwise a merge of smaller tables. For
 * REGULAR and EVENT groups the three quarter rule is lifted in a second pass, which is what
 * [liftRule] switches on. Among equally suitable tables the smallest and then the lowest id wins.
 */
class TableAssignmentService(
    private val tables: MutableList<Table>,
) {
    /**
     * Assigns a table for a group of [size] customers that wants to sit on a table of [type].
     *
     * Returns the assigned table, already marked as taken, or null if the group cannot be seated.
     */
    fun assign(size: Int, type: TableType, liftRule: Boolean): Table? {
        val pool = freePool(type)
        val chosen = exactFit(size, pool)
            ?: larger(size, pool, strict = true)
            ?: merged(size, pool, type, strict = true)
            ?: if (liftRule) {
                larger(size, pool, strict = false) ?: merged(size, pool, type, strict = false)
            } else {
                null
            }
        chosen?.let { take(it) }
        return chosen
    }

    /**
     * Marks [table] as free again. A merged table is split back into its original tables right away,
     * because the tables of a group that has left are restored immediately.
     */
    fun release(table: Table) {
        table.status = TableStatus.FREE
        table.originals().forEach { it.status = TableStatus.FREE }
        if (table.isMerged) {
            split(table)
            tables.sortBy { it.id }
        }
    }

    /**
     * The number of free seats per table type, counted over the tables that are currently free.
     */
    fun freeSeats(): Map<TableType, Int> = tables
        .filter { it.status == TableStatus.FREE }
        .groupBy { it.type }
        .mapValues { entry -> entry.value.sumOf { it.size } }

    /**
     * The number of seats the front of the house has in total. A merge replaces its originals in the
     * table list by one table of their summed size, so summing the list counts every seat once, whether
     * tables are currently merged or not.
     */
    fun totalSeats(): Int = tables.sumOf { it.size }

    /**
     * Dissolves every merged table back into the tables it was built from.
     */
    fun splitAllMerged() {
        tables.filter { it.isMerged }.forEach { split(it) }
        tables.sortBy { it.id }
    }

    /**
     * Replaces the merged [table] by the free original tables it was built from.
     */
    private fun split(table: Table) {
        tables.remove(table)
        for (original in table.originals()) {
            original.status = TableStatus.FREE
            if (!tables.contains(original)) {
                tables.add(original)
            }
        }
    }

    /**
     * Merges [tables] into one table that carries their lowest id and the sum of their sizes.
     */
    private fun merge(parts: List<Table>, type: TableType): Table {
        val originals = parts.flatMap { it.originals() }.sortedBy { it.id }.toMutableList()
        val mergedTable = Table(originals.first().id, originals.sumOf { it.size }, type, originals)
        parts.forEach { tables.remove(it) }
        tables.add(mergedTable)
        tables.sortBy { it.id }
        return mergedTable
    }

    /**
     * The free tables of [type], ordered by ascending size and then ascending id.
     */
    private fun freePool(type: TableType): List<Table> = tables
        .filter { it.type == type && it.status == TableStatus.FREE }
        .sortedWith(compareBy({ it.size }, { it.id }))

    /**
     * The smallest free table of exactly [size] seats.
     */
    private fun exactFit(size: Int, pool: List<Table>): Table? = pool.firstOrNull { it.size == size }

    /**
     * The smallest free table larger than [size]; under [strict] the group must fill three quarters.
     */
    private fun larger(size: Int, pool: List<Table>, strict: Boolean): Table? = pool
        .filter { it.size > size }
        .firstOrNull { !strict || coversThreeQuarters(size, it.size) }

    /**
     * A merge of the smaller free tables of [type] that seats [size] customers.
     */
    private fun merged(size: Int, pool: List<Table>, type: TableType, strict: Boolean): Table? {
        if (type == TableType.BAR) {
            return null
        }
        val smaller = pool.filter { it.size < size }
        val parts = mutableListOf<Table>()
        var mergedSize = 0
        for (table in smaller) {
            parts.add(table)
            mergedSize += table.size
            if (mergedSize >= size) {
                break
            }
        }
        if (mergedSize < size) {
            return null
        }
        while (parts.size > 1 && mergedSize - parts.first().size >= size) {
            mergedSize -= parts.removeAt(0).size
        }
        if (strict && !coversThreeQuarters(size, mergedSize)) {
            return null
        }
        return merge(parts, type)
    }

    /**
     * Whether a group of [size] customers fills at least three quarters of [tableSize] seats.
     */
    private fun coversThreeQuarters(size: Int, tableSize: Int): Boolean =
        size * QUARTERS >= tableSize * OCCUPIED_QUARTERS

    /**
     * Marks [table] and everything it was merged from as occupied.
     */
    private fun take(table: Table) {
        table.status = TableStatus.OCCUPIED
        table.originals().forEach { it.status = TableStatus.OCCUPIED }
    }

    /** The three quarter rule of the table assignment. */
    private companion object {
        const val QUARTERS = 4
        const val OCCUPIED_QUARTERS = 3
    }
}
