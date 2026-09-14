package de.unisaarland.cs.se.selab.logging

import kotlin.test.Test
import kotlin.test.assertEquals

class LogFormatTest {

    @Test
    fun idsSortsUnsortedInputAscending() {
        assertEquals("3,5,7,8", LogFormat.ids(listOf(8, 3, 7, 5)))
    }

    @Test
    fun idsKeepsDuplicates() {
        assertEquals("1,1,2", LogFormat.ids(listOf(2, 1, 1)))
    }

    @Test
    fun idsOfEmptyCollectionIsEmptyString() {
        assertEquals("", LogFormat.ids(emptyList()))
    }

    @Test
    fun idsOfSingleElement() {
        assertEquals("4", LogFormat.ids(listOf(4)))
    }

    @Test
    fun mappingOrdersByKeyRegardlessOfInputOrder() {
        val entries = linkedMapOf("C" to 7, "A" to 3, "B" to 5)
        assertEquals("A:3,B:5,C:7", LogFormat.mapping(entries))
    }

    @Test
    fun mappingOfEmptyMapIsEmptyString() {
        assertEquals("", LogFormat.mapping(emptyMap()))
    }

    @Test
    fun mappingOfSingleEntry() {
        assertEquals("Pasta:2", LogFormat.mapping(mapOf("Pasta" to 2)))
    }

    @Test
    fun restaurantTagFormat() {
        assertEquals("(R 5)", LogFormat.restaurantTag(5))
    }
}
