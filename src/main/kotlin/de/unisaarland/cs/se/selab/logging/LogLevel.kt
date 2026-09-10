package de.unisaarland.cs.se.selab.logging

/**
 * The level of detail a log statement belongs to (specification, Section 2.3.1).
 *
 * The order of the constants is the order of increasing importance: a statement is written whenever
 * its own level is at least the configured one, so IMPORTANT is always written and DEBUG only when
 * DEBUG was requested.
 */
enum class LogLevel {
    DEBUG,
    INFO,
    IMPORTANT,
    ;

    /** Factory for the command line spellings of the log levels. */
    companion object {
        /**
         * Resolves the command line spelling [raw] of a log level, or returns null if it is unknown.
         */
        fun from(raw: String): LogLevel? = entries.firstOrNull { it.name == raw.uppercase() }
    }
}
