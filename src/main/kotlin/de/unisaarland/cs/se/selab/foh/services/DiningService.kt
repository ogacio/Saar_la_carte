package de.unisaarland.cs.se.selab.foh.services

import de.unisaarland.cs.se.selab.foh.visit.Visit
import de.unisaarland.cs.se.selab.logging.Logger
import de.unisaarland.cs.se.selab.sharedPackage.customers.CustomerStatus
import de.unisaarland.cs.se.selab.simulation.GlobalClock
import de.unisaarland.cs.se.selab.simulation.SubUnits

/**
 * Step 5, eating (spec, "Eating").
 *
 * Tells every visit that the eating step of this tick has come round; the waiting and
 * eating states react to it. Then it writes the step's log lines in the spec's order:
 * first every group whose unserved customers left, then every group with customers who
 * finished eating, then the summary.
 */
class DiningService {

    /** Runs the eating step for all [visits], which arrive in group order. */
    fun eat(visits: List<Visit>, sbu: SubUnits) {
        val tick = GlobalClock.currentTick
        visits.forEach { it.advance(tick) }

        for (visit in visits) {
            val table = visit.table ?: continue
            if (visit.leftUnservedThisTick > 0) {
                Logger.Customer.noEating(sbu.restaurantId, visit.leftUnservedThisTick, visit.group.id(), table.id)
            }
        }
        for (visit in visits) {
            val table = visit.table ?: continue
            if (visit.finishedEatingThisTick > 0) {
                Logger.Customer.finishedEating(
                    sbu.restaurantId,
                    visit.finishedEatingThisTick,
                    visit.group.id(),
                    table.id,
                )
            }
        }

        val stillEating = visits.sumOf { visit ->
            visit.customersInside().count { it.status() == CustomerStatus.SERVED }
        }
        val finishedNow = visits.sumOf { it.finishedEatingThisTick }
        Logger.Customer.eatingStatus(sbu.restaurantId, stillEating, finishedNow)
    }
}
