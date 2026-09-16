package de.unisaarland.cs.se.selab.simulation

import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * F01: the simulation clock. [GlobalClock] is a singleton without a reset, so the tests only assert
 * how the values change, never their absolute value.
 */
class GlobalClockTest {

    @Test
    fun tickAdvancesTheGlobalAndTheEveningTick() {
        val tick = GlobalClock.currentTick
        val tickInEvening = GlobalClock.getTickInEvening()

        GlobalClock.advanceTick()

        assertEquals(tick + 1, GlobalClock.currentTick)
        assertEquals(tickInEvening + 1, GlobalClock.getTickInEvening())
    }

    @Test
    fun eveningAdvancesAndRestartsTheEveningTickButNotTheGlobalTick() {
        GlobalClock.advanceTick()
        val tick = GlobalClock.currentTick
        val evening = GlobalClock.getEvening()

        GlobalClock.advanceEvening()

        assertEquals(evening + 1, GlobalClock.getEvening())
        assertEquals(0, GlobalClock.getTickInEvening())
        assertEquals(tick, GlobalClock.currentTick)
    }
}
