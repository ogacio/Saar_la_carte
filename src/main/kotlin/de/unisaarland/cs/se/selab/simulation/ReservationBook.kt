package de.unisaarland.cs.se.selab.simulation

import de.unisaarland.cs.se.selab.foh.Table
import de.unisaarland.cs.se.selab.foh.TableAssignmentService
import de.unisaarland.cs.se.selab.foh.TableStatus
import de.unisaarland.cs.se.selab.sharedPackage.customers.CustomerGroup
import de.unisaarland.cs.se.selab.sharedPackage.customers.GroupType

/**
 * The table reservations of one restaurant (spec, "Front of house", F14).
 *
 * EVENT groups book three evenings ahead ([bookAhead]); in the evening itself
 * [openEvening] reserves one table for each EVENT and then each REGULAR group. A reserved
 * table "will only be used by the group for which they were reserved, even after the group
 * has left the restaurant", so it is freed only by [clearTonight].
 */
class ReservationBook(
    private val tables: TableAssignmentService,
) {
    /** Tonight's reservations: group id to its reserved (possibly merged) table. */
    private val tonight: MutableMap<Int, Table> = mutableMapOf()

    /** EVENT groups booked ahead, per evening. */
    private val upcoming: MutableMap<Int, MutableList<CustomerGroup>> = mutableMapOf()

    /**
     * An EVENT [group] books this restaurant for [evening]; the booking is noted here.
     *
     * Whether the restaurant still has seats for that evening is decided by the browsing service
     * before the group books ("for EVENTS whether there are still available seats based on the group
     * sizes of prior EVENT reservations"), so this book does not check the seats a second time.
     */
    fun bookAhead(group: CustomerGroup, evening: Int): Boolean {
        upcoming.getOrPut(evening) { mutableListOf() } += group
        return true
    }

    /** The EVENT groups booked for [evening], in ascending id; the kitchen plans for them. */
    fun expectedFor(evening: Int): List<CustomerGroup> = upcoming[evening].orEmpty().sortedBy { it.id() }

    /** groupId -> the table it holds tonight. Valid only after openEvening. */
    fun tablesTonight(): Map<Int, Table> = tonight

    /** The groups that actually hold a table tonight, events first then regulars, by id. */
    fun reservedGroupsTonight(evening: Int, regulars: List<CustomerGroup>): List<CustomerGroup> =
        (expectedFor(evening) + regulars.sortedBy { it.id() }).filter { it.id() in tonight }

    /**
     * Preparation of [evening]: reserves one table for each EVENT and then each REGULAR group,
     * each type in ascending id. The table rules are applied with the three-quarter rule lifted
     * (rules 5 and 6), because "the manager always tries to find a table for those groups".
     * Returns the groups that got no table; the FOH sends them away.
     */
    fun openEvening(evening: Int, regulars: List<CustomerGroup>): List<CustomerGroup> {
        val failed = mutableListOf<CustomerGroup>()
        for (group in expectedFor(evening) + regulars.sortedBy { it.id() }) {
            val table = tables.assign(group.groupSize(), group.tableType(), liftRule = true)
            if (table == null) {
                failed += group
                continue
            }
            markReserved(table)
            tonight[group.id()] = table
        }
        return failed
    }

    /**
     * The table reserved tonight for group [groupId], now taken by the arriving group; null if the
     * group has no reservation (a CASUAL group). Used by SeatingService. The table stays in the book
     * until [clearTonight].
     */
    fun claim(groupId: Int): Table? {
        val table = tonight[groupId] ?: return null
        table.status = TableStatus.OCCUPIED
        table.originals().forEach { it.status = TableStatus.OCCUPIED }
        return table
    }

    /** Drops tonight's reservations and frees their tables ("reservation from this evening are discarded"). */
    fun clearTonight() {
        tonight.values.forEach { tables.release(it) }
        tonight.clear()
    }

    /**
     * Forgets the event bookings of evening once it is over, so the book only holds future evenings
     * and [getEventSeatsBooked] never reports an evening that has already been played.
     */
    fun dropBookings() {
        upcoming.remove(GlobalClock.getEvening())
    }

    /** Marks [table] and every table it was merged from as reserved. */
    private fun markReserved(table: Table) {
        table.status = TableStatus.RESERVED
        table.originals().forEach { it.status = TableStatus.RESERVED }
    }

    /** returns mapping: evening to booked event seats **/
    fun getEventSeatsBooked(): Map<Int, Int> {
        val temporary = mutableMapOf<Int, Int>()
        for ((i, reservations) in upcoming) {
            val number = reservations
                .filter { it.groupType() == GroupType.EVENT }
                .sumOf { it.groupSize() }
            if (number > 0) {
                temporary[i] = number
            }
        }
        return temporary.toMap()
    }
}
