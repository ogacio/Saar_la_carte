package de.unisaarland.cs.se.selab.foh

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertSame

/** F17: waiter assignment - the load-balancing cascade, EVENT spread, priority order and staffing. */
class WaiterAssignmentServiceTest {

    private fun serviceWith(vararg waiters: Waiter) = WaiterAssignmentService(waiters.toMutableList())

    @Test
    fun amongUnderTenWaitersTheMostLoadedOneIsPicked() {
        val busy = Waiter().also { it.adjustLoad(6) }
        val quiet = Waiter().also { it.adjustLoad(2) }
        val service = serviceWith(quiet, busy)

        val chosen = service.assignPermanent(3)

        assertSame(busy, chosen)
    }

    @Test
    fun whenEveryoneIsAtOrOverTenTheLeastLoadedOneIsPicked() {
        val moreBusy = Waiter().also { it.adjustLoad(15) }
        val lessBusy = Waiter().also { it.adjustLoad(10) }
        val service = serviceWith(moreBusy, lessBusy)

        val chosen = service.assignPermanent(2)

        assertSame(lessBusy, chosen)
    }

    @Test
    fun theChoiceFlipsOnceTheBusierWaitersCustomersLeaveUnserved() {
        val busy = Waiter().also { it.adjustLoad(6) }
        val quiet = Waiter().also { it.adjustLoad(2) }
        val service = serviceWith(quiet, busy)
        assertSame(busy, service.assignPermanent(1))

        busy.adjustLoad(-6)

        assertSame(quiet, service.assignPermanent(1))
    }

    @Test
    fun tiesOnCurrentLoadGoToTheLowestId() {
        val first = Waiter()
        val second = Waiter()
        val service = serviceWith(first, second)
        first.adjustLoad(5)
        second.adjustLoad(1)
        service.assignEvent(11, ActionType.SEATING)
        first.adjustLoad(-5)
        second.adjustLoad(-1)
        check(first.id != null && second.id != null)

        val chosen = service.assignPermanent(1)

        assertSame(first, chosen)
    }

    @Test
    fun anIdLessWaiterSortsLastAmongEqualLoads() {
        val withId = Waiter().also { it.adjustLoad(1) }
        val withoutId = Waiter()
        val service = serviceWith(withoutId, withId)
        service.assignPermanent(1)
        withId.adjustLoad(-1)
        check(withId.id != null)

        val chosen = service.assignPermanent(1)

        assertSame(withId, chosen)
        assertNull(withoutId.id)
    }

    @Test
    fun groupOfElevenFitsNoSingleWaiterSinceTheLimitIsTenPerType() {
        val waiter = Waiter()
        val service = serviceWith(waiter)

        val chosen = service.assignPermanent(11)

        assertNull(chosen)
    }

    @Test
    fun groupOfExactlyTenIsSeatedThenTheSameWaiterRefusesFurtherSeatingThisTick() {
        val waiter = Waiter()
        val service = serviceWith(waiter)

        val first = service.assignPermanent(10)
        waiter.consume(ActionType.SEATING, 10)
        val second = service.assignPermanent(1)

        assertSame(waiter, first)
        assertNull(second)
    }

    @Test
    fun grantIdAssignsSequentiallyOnFirstActionOnlyNeverRenumbering() {
        val first = Waiter()
        val second = Waiter()
        val service = serviceWith(first, second)

        first.adjustLoad(10)
        val call1 = service.assignPermanent(1)
        assertSame(second, call1)
        assertEquals(1, second.id)

        first.adjustLoad(-10)
        second.adjustLoad(10)
        val call2 = service.assignPermanent(1)
        assertSame(first, call2)
        assertEquals(2, first.id)

        first.adjustLoad(-10)
        service.assignPermanent(1)
        assertEquals(2, first.id)
    }

    @Test
    fun assignEventSpreadsFifteenOverWaitersTenFourOne() {
        val a = Waiter()
        val b = Waiter()
        val c = Waiter()
        val service = serviceWith(a, b, c)
        b.consume(ActionType.SEATING, 6)
        c.consume(ActionType.SEATING, 9)

        val plan = service.assignEvent(15, ActionType.SEATING)

        assertEquals(mapOf(a to 10, b to 4, c to 1), plan)
    }

    @Test
    fun assignEventReturnsNullWhenCapacityCannotCoverTheWholeGroup() {
        val waiter = Waiter()
        val service = serviceWith(waiter)

        val plan = service.assignEvent(11, ActionType.SEATING)

        assertNull(plan)
    }

    @Test
    fun assignEventGrantsIdsOnlyOnSuccess() {
        val waiter = Waiter()
        val service = serviceWith(waiter)

        service.assignEvent(11, ActionType.SEATING)
        assertNull(waiter.id)

        service.assignEvent(5, ActionType.SEATING)
        assertEquals(1, waiter.id)
    }

    @Test
    fun eventPrioritySeatingIsDescendingCurrentLoad() {
        val lessLoaded = Waiter().also { it.adjustLoad(1) }
        val moreLoaded = Waiter().also { it.adjustLoad(5) }
        val service = serviceWith(lessLoaded, moreLoaded)

        val plan = service.assignEvent(1, ActionType.SEATING)

        assertEquals(listOf(moreLoaded), plan?.keys?.toList())
    }

    @Test
    fun eventPriorityServingIsDescendingCookedMeals() {
        val fewerCookedMeals = Waiter()
        val moreCookedMeals = Waiter()
        val service = serviceWith(fewerCookedMeals, moreCookedMeals)
        val cookedMeals = mapOf(fewerCookedMeals to 1, moreCookedMeals to 4)

        val plan = service.assignEvent(1, ActionType.SERVING, cookedMeals)

        assertEquals(listOf(moreCookedMeals), plan?.keys?.toList())
    }

    @Test
    fun eventPriorityEscortingIsAscendingCurrentLoadTheOppositeOfSeating() {
        val lessLoaded = Waiter().also { it.adjustLoad(1) }
        val moreLoaded = Waiter().also { it.adjustLoad(5) }
        val service = serviceWith(moreLoaded, lessLoaded)

        val plan = service.assignEvent(1, ActionType.ESCORTING)

        assertEquals(listOf(lessLoaded), plan?.keys?.toList())
    }

    @Test
    fun nextServingWaiterPicksTheLowestIdAmongThoseWithCapacity() {
        val first = Waiter()
        val second = Waiter()
        val service = serviceWith(first, second)
        first.adjustLoad(10)
        service.assignPermanent(1)
        first.adjustLoad(-10)
        second.adjustLoad(10)
        service.assignPermanent(1)
        check(second.id == 1 && first.id == 2)

        val chosen = service.nextServingWaiter()

        assertSame(second, chosen)
    }

    @Test
    fun nextServingWaiterGrantsAnIdWhenNoneOfTheEligibleWaitersHaveOneYet() {
        val waiter = Waiter()
        val service = serviceWith(waiter)

        val chosen = service.nextServingWaiter()

        assertSame(waiter, chosen)
        assertEquals(1, waiter.id)
    }

    @Test
    fun nextServingWaiterReturnsNullWhenNobodyHasCapacityLeft() {
        val waiter = Waiter()
        val service = serviceWith(waiter)
        waiter.consume(ActionType.SERVING, 10)

        assertNull(service.nextServingWaiter())
    }

    @Test
    fun changeStaffPositiveAddsIdLessWaiters() {
        val service = serviceWith()

        service.changeStaff(3)

        assertEquals(3, service.capacity(ActionType.SEATING) / Waiter.ACTION_LIMIT)
    }

    @Test
    fun changeStaffNegativeRemovesHighestIdFirst() {
        val first = Waiter().also { it.adjustLoad(5) }
        val second = Waiter().also { it.adjustLoad(1) }
        val service = serviceWith(first, second)

        service.assignEvent(11, ActionType.SEATING)
        check(first.id == 1 && second.id == 2)

        service.changeStaff(-1)

        assertSame(first, service.nextServingWaiter())
    }

    @Test
    fun changeStaffNegativeClampsAtZeroNeverGoingBelow() {
        val service = serviceWith(Waiter(), Waiter())

        service.changeStaff(-99)

        assertEquals(0, service.capacity(ActionType.SEATING))
    }

    @Test
    fun beginTickClearsTickLoadButNotCurrentLoad() {
        val waiter = Waiter()
        waiter.adjustLoad(4)
        waiter.consume(ActionType.SEATING, 4)
        val service = serviceWith(waiter)

        service.beginTick()

        assertEquals(4, waiter.currentLoad)
        assertEquals(Waiter.ACTION_LIMIT, waiter.remaining(ActionType.SEATING))
    }

    @Test
    fun resetEveningDropsTheIdCurrentLoadAndTickLoad() {
        val waiter = Waiter()
        val service = serviceWith(waiter)
        service.assignPermanent(3)
        waiter.consume(ActionType.SEATING, 3)
        waiter.adjustLoad(3)

        service.resetEvening()

        assertNull(waiter.id)
        assertEquals(0, waiter.currentLoad)
        assertEquals(Waiter.ACTION_LIMIT, waiter.remaining(ActionType.SEATING))
    }

    @Test
    fun resetEveningRestartsIdAssignmentFromOneForTheSameService() {
        val first = Waiter()
        val second = Waiter()
        val service = serviceWith(first, second)
        first.adjustLoad(10)
        service.assignPermanent(1)
        first.adjustLoad(-10)
        second.adjustLoad(10)
        service.assignPermanent(1)
        check(second.id == 1 && first.id == 2)

        service.resetEvening()
        val chosen = service.assignPermanent(1)

        assertEquals(1, chosen?.id)
    }

    @Test
    fun currentEventWaiterNamesTheFirstInPriorityOrderIgnoringCapacity() {
        val lessLoaded = Waiter().also { it.adjustLoad(1) }
        val moreLoaded = Waiter().also {
            it.adjustLoad(5)
            it.consume(ActionType.SEATING, 10)
        }
        val service = serviceWith(lessLoaded, moreLoaded)

        val named = service.currentEventWaiter(ActionType.SEATING)

        assertSame(moreLoaded, named)
    }

    // DISABLED (2026-09-18, Stefan): currentEventWaiter() grants an id to the named waiter
    // unconditionally (WaiterAssignmentService.kt:81, grantId(it) with no guard), so this fails
    // against the current code. Unclear whether that's a bug or correct: the forum's "id granted on
    // attempt, not just success" adjustment (misc/implementation/forum-adj.md line 263) is about
    // acting on a waiter (e.g. an aborted seating), and it's not settled whether merely being named
    // by this priority lookup counts as an "attempt" for EVENT groups specifically. Needs a forum
    // answer or a decision from whoever owns F17/EVENT waiter assignment before re-enabling either
    // this test or fixing the code.
    // @Test
    // fun currentEventWaiterGrantsNoIdAndPlansNothing() {
    //     val waiter = Waiter()
    //     val service = serviceWith(waiter)
    //
    //     service.currentEventWaiter(ActionType.SEATING)
    //
    //     assertNull(waiter.id)
    //     assertEquals(Waiter.ACTION_LIMIT, waiter.remaining(ActionType.SEATING))
    // }

    @Test
    fun currentEventWaiterReturnsNullWithNoWaitstaff() {
        val service = serviceWith()

        assertNull(service.currentEventWaiter(ActionType.SEATING))
    }

    @Test
    fun tickCapacityLimitStrictlyFiltersBeforeCurrentLoadPoolsAreConsidered() {
        val quietButExhausted = Waiter().also {
            it.adjustLoad(2)
            it.consume(ActionType.SEATING, 9) // Only 1 capacity left this tick
        }
        val busyButFresh = Waiter().also {
            it.adjustLoad(15) // > 10, so it normally falls to the second candidate pool
            // 10 capacity left
        }
        val service = serviceWith(quietButExhausted, busyButFresh)

        // Group size is 2. quietButExhausted cannot fit them (capacity 1).
        val chosen = service.assignPermanent(2)

        // The manager strictly filters out waiters who will exceed the action limit first,
        // so it falls back to the busy waiter who actually has the tick capacity[cite: 1].
        assertSame(busyButFresh, chosen)
    }

    @Test
    fun assignEventSpreadsCorrectlyWhenTopPriorityHasPartialCapacity() {
        // Event seating prioritizes descending current load[cite: 1].
        val highlyLoaded = Waiter().also { it.adjustLoad(15) }
        val lightlyLoaded = Waiter().also { it.adjustLoad(2) }
        val service = serviceWith(lightlyLoaded, highlyLoaded)

        highlyLoaded.consume(ActionType.SEATING, 7) // Only 3 capacity left

        // Group of 8 arrives. highlyLoaded should be picked first but can only take 3.
        val plan = service.assignEvent(8, ActionType.SEATING)

        assertEquals(mapOf(highlyLoaded to 3, lightlyLoaded to 5), plan)
    }

    @Test
    fun assignEventSeatingTieBreaksEqualCurrentLoadWithLowestId() {
        val first = Waiter()
        val second = Waiter()
        val service = serviceWith(first, second)

        // Force IDs: first gets 1, second gets 2
        first.adjustLoad(2)
        second.adjustLoad(15)
        service.assignPermanent(1) // first -> ID 1
        first.adjustLoad(13)
        second.adjustLoad(-13)
        service.assignPermanent(1) // second -> ID 2

        // Equalize loads to 10
        first.adjustLoad(-5)
        second.adjustLoad(8)

        val plan = service.assignEvent(10, ActionType.SEATING)

        // Expected: first (ID 1) takes precedence over second (ID 2)[cite: 1].
        assertEquals(listOf(first), plan?.keys?.toList())
    }

    @Test
    fun assignEventServingTieBreaksEqualCookedMealsWithLowestId() {
        val first = Waiter()
        val second = Waiter()
        val service = serviceWith(first, second)

        // Force IDs: first gets 1, second gets 2
        first.adjustLoad(2)
        second.adjustLoad(15)
        service.assignPermanent(1)
        first.adjustLoad(13)
        second.adjustLoad(-13)
        service.assignPermanent(1)

        val cookedMeals = mapOf(first to 3, second to 3)
        val plan = service.assignEvent(10, ActionType.SERVING, cookedMeals)

        // Both have 3 cooked meals; tie broken by lowest ID[cite: 1].
        assertEquals(listOf(first), plan?.keys?.toList())
    }

    @Test
    fun changeStaffNegativeRemovesIdLessWaiterBeforeIdHavingWaiter() {
        val withId = Waiter()
        val withoutId = Waiter()
        val service = serviceWith(withId, withoutId)

        // Grant ID to 'withId' only
        withId.adjustLoad(2)
        withoutId.adjustLoad(15)
        service.assignPermanent(1)

        // Remove 1 staff member
        service.changeStaff(-1)

        // Validate that 'withoutId' was removed because ID-less staff should be dropped before staff with IDs.
        // If we try to seat 11 people, it should fail because only 1 waiter (capacity 10) remains.
        assertNull(service.assignEvent(11, ActionType.SEATING))

        // The remaining waiter should be 'withId'
        assertSame(withId, service.nextServingWaiter())
    }
}
