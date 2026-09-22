package de.unisaarland.cs.se.selab.foh.services

import de.unisaarland.cs.se.selab.foh.visit.Visit
import de.unisaarland.cs.se.selab.logging.Logger
import de.unisaarland.cs.se.selab.sharedPackage.customers.CustomerGroup
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
        eat(visits, emptyList(), sbu)
    }

    /**
     * Runs the eating step for restaurant visits and delivery groups. Delivery Finished Eating
     * belongs to this step, before the FOH Eating Status and before the later rating step.
     * Returns the delivery groups that finished eating in this tick.
     */
    fun eat(
        visits: List<Visit>,
        deliveryGroups: List<CustomerGroup>,
        sbu: SubUnits,
    ): List<CustomerGroup> {
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

        // Delivered meals are stamped with the in-evening tick in DeliveryDriver.deliverAccepted,
        // so the eating deadline has to be measured on the same clock, not on the global one.
        val deliveryTick = GlobalClock.getTickInEvening()
        for (group in deliveryGroups.sortedBy { it.id() }) {
            for (customer in group.members()) {
                if (customer.status() == CustomerStatus.SERVED && customer.isDoneEating(deliveryTick)) {
                    customer.doneEating()
                }
            }
        }
        val finishedDeliveries = deliveryGroups
            .filter { group -> group.members().all { it.status() == CustomerStatus.DONE_EATING } }
            .sortedBy { it.id() }
        for (group in finishedDeliveries) {
            Logger.Delivery.deliveryFinishedEating(sbu.restaurantId, group.id())
        }

        val stillEating = visits.sumOf { visit ->
            visit.customersInside().count { it.status() == CustomerStatus.SERVED }
        }
        val finishedNow = visits.sumOf { it.finishedEatingThisTick }
        Logger.Customer.eatingStatus(sbu.restaurantId, stillEating, finishedNow)
        return finishedDeliveries
    }
}
