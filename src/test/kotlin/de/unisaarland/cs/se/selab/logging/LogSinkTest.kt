package de.unisaarland.cs.se.selab.logging

import java.io.PrintWriter
import java.io.StringWriter
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class LogSinkTest {

    private fun capture(): Pair<StringWriter, PrintWriter> {
        val buffer = StringWriter()
        return buffer to PrintWriter(buffer)
    }

    @Test
    fun debugLevelLetsEverythingThrough() {
        val (buffer, writer) = capture()
        LogSink.configure(LogLevel.DEBUG, writer)

        LogSink.write(LogLevel.DEBUG, "debug message")
        LogSink.write(LogLevel.INFO, "info message")
        LogSink.write(LogLevel.IMPORTANT, "important message")

        val lines = buffer.toString().lines().filter { it.isNotEmpty() }
        assertEquals(3, lines.size)
    }

    @Test
    fun infoLevelSuppressesDebugOnly() {
        val (buffer, writer) = capture()
        LogSink.configure(LogLevel.INFO, writer)

        LogSink.write(LogLevel.DEBUG, "debug message")
        LogSink.write(LogLevel.INFO, "info message")
        LogSink.write(LogLevel.IMPORTANT, "important message")

        val lines = buffer.toString().lines().filter { it.isNotEmpty() }
        assertEquals(2, lines.size)
        assertTrue(lines.none { it.contains("debug message") })
    }

    @Test
    fun importantLevelSuppressesDebugAndInfo() {
        val (buffer, writer) = capture()
        LogSink.configure(LogLevel.IMPORTANT, writer)

        LogSink.write(LogLevel.DEBUG, "debug message")
        LogSink.write(LogLevel.INFO, "info message")
        LogSink.write(LogLevel.IMPORTANT, "important message")

        val lines = buffer.toString().lines().filter { it.isNotEmpty() }
        assertEquals(1, lines.size)
        assertTrue(lines.single().contains("important message"))
    }

    @Test
    fun importantMessageAlwaysWritten() {
        val (buffer, writer) = capture()
        LogSink.configure(LogLevel.IMPORTANT, writer)

        LogSink.write(LogLevel.IMPORTANT, "always visible")

        assertTrue(buffer.toString().contains("always visible"))
    }

    @Test
    fun messageBelowConfiguredLevelIsSuppressed() {
        val (buffer, writer) = capture()
        LogSink.configure(LogLevel.INFO, writer)

        LogSink.write(LogLevel.DEBUG, "should not appear")

        assertFalse(buffer.toString().contains("should not appear"))
    }

    @Test
    fun writtenMessageHasLevelPrefix() {
        val (buffer, writer) = capture()
        LogSink.configure(LogLevel.DEBUG, writer)

        LogSink.write(LogLevel.INFO, "hello world")

        assertEquals("[INFO] hello world", buffer.toString().trim())
    }

    @Test
    fun writtenMessageIsFlushedWithoutExplicitClose() {
        val (buffer, writer) = capture()
        LogSink.configure(LogLevel.DEBUG, writer)

        LogSink.write(LogLevel.DEBUG, "flushed already")

        assertTrue(buffer.toString().contains("flushed already"))
    }

    @Test
    fun reconfigureRedirectsFutureWritesToNewWriter() {
        val (firstBuffer, firstWriter) = capture()
        val (secondBuffer, secondWriter) = capture()
        LogSink.configure(LogLevel.DEBUG, firstWriter)
        LogSink.write(LogLevel.DEBUG, "goes to first")

        LogSink.configure(LogLevel.DEBUG, secondWriter)
        LogSink.write(LogLevel.DEBUG, "goes to second")

        assertTrue(firstBuffer.toString().contains("goes to first"))
        assertFalse(firstBuffer.toString().contains("goes to second"))
        assertTrue(secondBuffer.toString().contains("goes to second"))
        assertFalse(secondBuffer.toString().contains("goes to first"))
    }
}
