package de.unisaarland.cs.se.selab.foh

import de.unisaarland.cs.se.selab.sharedPackage.TableType

/**
 * Whether a table is free, held by a reservation or currently occupied.
 */
enum class TableStatus {
    FREE,
    RESERVED,
    OCCUPIED,
}

/**
 * One table of the front of the house.
 *
 * A merged table is a table in its own right: it carries the lowest id of the tables it was built
 * from, the sum of their sizes, and keeps them in [mergedFromTables] until it is split again.
 */
class Table(
    val id: Int,
    val size: Int,
    val type: TableType,
    val mergedFromTables: MutableList<Table>? = null,
) {
    /** Whether the table is free, reserved or occupied. */
    var status: TableStatus = TableStatus.FREE

    /** Whether this table was built by merging smaller tables. */
    val isMerged: Boolean get() = mergedFromTables != null

    /**
     * The original tables this table consists of: itself if it was never merged.
     */
    fun originals(): List<Table> = mergedFromTables ?: listOf(this)
}
