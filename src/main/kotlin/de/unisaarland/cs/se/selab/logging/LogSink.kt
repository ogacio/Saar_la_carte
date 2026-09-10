package de.unisaarland.cs.se.selab.logging

import java.io.PrintWriter

/**
 * The single output handle every logger writes through.
 *
 * It holds the configured [LogLevel] and the writer, applies the level filter and flushes after each
 * line so that the log is complete even if the simulation is interrupted.
 */
internal object LogSink {
    private var level: LogLevel = LogLevel.DEBUG
    private var out: PrintWriter = PrintWriter(System.out)

    /**
     * Points the sink at [out] and only lets statements of at least [level] through.
     */
    fun configure(level: LogLevel, out: PrintWriter) {
        this.level = level
        this.out = out
    }

    /**
     * Writes [message] prefixed with its level if [messageLevel] passes the configured filter.
     */
    fun write(messageLevel: LogLevel, message: String) {
        if (messageLevel.ordinal >= level.ordinal) {
            out.println("[$messageLevel] $message")
            out.flush()
        }
    }
}
