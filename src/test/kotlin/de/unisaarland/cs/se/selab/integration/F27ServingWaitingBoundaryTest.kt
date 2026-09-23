package de.unisaarland.cs.se.selab.integration

import de.unisaarland.cs.se.selab.foh.DeliveryDesk
import de.unisaarland.cs.se.selab.foh.Table
import de.unisaarland.cs.se.selab.foh.Waiter
import de.unisaarland.cs.se.selab.foh.WaiterAssignmentService
import de.unisaarland.cs.se.selab.foh.services.DiningService
import de.unisaarland.cs.se.selab.foh.services.ServingService
import de.unisaarland.cs.se.selab.foh.visit.Visit
import de.unisaarland.cs.se.selab.sharedPackage.Menu
import de.unisaarland.cs.se.selab.sharedPackage.Pantry
import de.unisaarland.cs.se.selab.sharedPackage.Recipe
import de.unisaarland.cs.se.selab.sharedPackage.RestaurantType
import de.unisaarland.cs.se.selab.sharedPackage.TableType
import de.unisaarland.cs.se.selab.sharedPackage.customers.CustomerStatus
import de.unisaarland.cs.se.selab.simulation.GlobalClock
import de.unisaarland.cs.se.selab.simulation.SubUnits
import de.unisaarland.cs.se.selab.testsupport.KitchenBoundaryFixtures.dish
import de.unisaarland.cs.se.selab.testsupport.KitchenBoundaryFixtures.kitchen
import de.unisaarland.cs.se.selab.testsupport.KitchenBoundaryFixtures.order
import de.unisaarland.cs.se.selab.testsupport.KitchenBoundaryFixtures.startEvening
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/** F27: real cooking and serving run before the eating step checks the waiting deadline. */
class F27ServingWaitingBoundaryTest {
    @Test
    fun foodCookedInTheFifthWaitingTickIsServedBeforeTheGroupTimesOut() {
        val fixture = flow(dish(1, minutes = 30), blocker = dish(0, minutes = 20, basic = true))
        repeat(4) {
            fixture.tick()
            assertEquals(CustomerStatus.ORDERED, fixture.visit.group.members().single().status())
        }
        fixture.tick()
        assertEquals(CustomerStatus.SERVED, fixture.visit.group.members().single().status())
        assertEquals(requireNotNull(fixture.visit.orderedTick) + 4, fixture.visit.group.members().single().servedTick())
        assertFalse(fixture.visit.failedAttempt)
        fixture.tick()
        assertEquals(CustomerStatus.SERVED, fixture.visit.group.members().single().status())
        fixture.tick()
        assertEquals(CustomerStatus.DONE_EATING, fixture.visit.group.members().single().status())
    }

    @Test
    fun eatingAndTimeoutFinishInTheSameTickWithoutServingTheDepartedMemberLater() {
        val fixture = flow(dish(1, minutes = 30, basic = true), dish(2), dish(3, minutes = 40))
        repeat(5) { fixture.tick() }
        val members = fixture.visit.group.members()
        assertEquals(
            listOf(CustomerStatus.SERVED, CustomerStatus.SERVED, CustomerStatus.ORDERED),
            members.map { it.status() },
        )
        fixture.tick()
        assertEquals(CustomerStatus.ORDERED, members[2].status(), "Sixth waiting tick still belongs to the extension")
        assertEquals(CustomerStatus.SERVED, members[0].status(), "One full eating tick is insufficient")
        fixture.tick()
        assertEquals(
            listOf(CustomerStatus.DONE_EATING, CustomerStatus.DONE_EATING, CustomerStatus.LEFT),
            members.map { it.status() },
        )
        assertEquals(1, fixture.visit.leftUnservedThisTick)
        assertEquals(2, fixture.visit.finishedEatingThisTick)
        assertFalse(fixture.visit.failedAttempt)
        fixture.tick()
        assertTrue(fixture.visit.cookedMeals().isEmpty(), "Late meals must not become servable for departed customers")
        assertEquals(null, members[2].servedTick())
    }

    private fun flow(vararg recipes: Recipe, blocker: Recipe? = null): Flow {
        startEvening()
        val pantry = Pantry(restaurantId = 1)
        val kitchen = kitchen(pantry)
        val sbu = SubUnits(1, Menu(recipes.toMutableList(), pantry, kitchen), pantry, kitchen)
        val waitstaff = WaiterAssignmentService(mutableListOf(Waiter()))
        val blockingOrder = blocker?.let { order(it) }
        val order = order(*recipes)
        val visit = Visit(order.getCustomerGroup())
        val waiter = requireNotNull(waitstaff.assignPermanent(recipes.size))
        visit.seated(Table(1, recipes.size, TableType.COMMON), listOf(waiter), GlobalClock.currentTick)
        visit.ordered(order, GlobalClock.currentTick)
        if (blockingOrder != null) kitchen.enqueue(blockingOrder)
        kitchen.enqueue(order)
        return Flow(visit, sbu, waitstaff)
    }

    private class Flow(val visit: Visit, private val sbu: SubUnits, private val waitstaff: WaiterAssignmentService) {
        private val serving = ServingService(waitstaff, DeliveryDesk(mutableListOf(), 1), RestaurantType.ASIAN)
        private val dining = DiningService()

        fun tick() {
            waitstaff.beginTick()
            sbu.kitchen.cook()
            serving.serve(listOf(visit), sbu)
            dining.eat(listOf(visit), sbu)
            GlobalClock.advanceTick()
        }
    }
}
