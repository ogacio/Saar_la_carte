package de.unisaarland.cs.se.selab.logging

/**
 * The shared formatting rules for the variable parts of a log statement (specification, 2.3.1).
 */
object LogFormat {
    /**
     * Formats [values] as an ascending, comma separated list, for example `3,5,7,8`.
     */
    fun ids(values: Collection<Int>): String = values.sorted().joinToString(",")

    /**
     * Formats [entries] as comma separated `key:value` pairs in ascending alphabetic order of the
     * key, for example `A:3,B:5,C:7`.
     */
    fun mapping(entries: Map<String, Int>): String =
        entries.entries.sortedBy { it.key }.joinToString(",") { "${it.key}:${it.value}" }

    /**
     * The `(R $id)` tag that marks a statement as belonging to restaurant [restaurantId].
     */
    fun restaurantTag(restaurantId: Int): String = "(R $restaurantId)"
}
