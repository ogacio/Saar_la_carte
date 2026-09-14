package de.unisaarland.cs.se.selab.logging

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

class LogLevelTest {

    @Test
    fun fromResolvesExactUppercaseSpelling() {
        assertEquals(LogLevel.DEBUG, LogLevel.from("DEBUG"))
        assertEquals(LogLevel.INFO, LogLevel.from("INFO"))
        assertEquals(LogLevel.IMPORTANT, LogLevel.from("IMPORTANT"))
    }

    @Test
    fun fromResolvesCaseInsensitively() {
        assertEquals(LogLevel.DEBUG, LogLevel.from("debug"))
        assertEquals(LogLevel.IMPORTANT, LogLevel.from("ImPoRtAnT"))
    }

    @Test
    fun fromReturnsNullForUnknownSpelling() {
        assertNull(LogLevel.from("VERBOSE"))
        assertNull(LogLevel.from(""))
    }

    @Test
    fun ordinalOrderIsIncreasingImportance() {
        assertTrue(LogLevel.DEBUG.ordinal < LogLevel.INFO.ordinal)
        assertTrue(LogLevel.INFO.ordinal < LogLevel.IMPORTANT.ordinal)
    }
}
