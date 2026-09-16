package de.unisaarland.cs.se.selab.sharedPackage

import de.unisaarland.cs.se.selab.kitchen.Kitchen
import de.unisaarland.cs.se.selab.testsupport.Fixtures.recipe
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/** F13: the menu only offers what the pantry covers and the kitchen can cook. */
class MenuTest {

    private val pantry = mock<Pantry>()
    private val kitchen = mock<Kitchen>()

    @Test
    fun nothingIsOrderableBeforeTheFirstRefresh() {
        val menu = Menu(mutableListOf(recipe(1)), pantry, kitchen)

        assertTrue(menu.getOrderables().isEmpty())
    }

    @Test
    fun onlyRecipesCoveredByPantryAndKitchenAreOrderable() {
        val both = recipe(1)
        val onlyPantry = recipe(2)
        val onlyKitchen = recipe(3)
        val neither = recipe(4)
        whenever(pantry.canCover(both)).thenReturn(true)
        whenever(kitchen.canCook(both)).thenReturn(true)
        whenever(pantry.canCover(onlyPantry)).thenReturn(true)
        whenever(kitchen.canCook(onlyPantry)).thenReturn(false)
        whenever(pantry.canCover(onlyKitchen)).thenReturn(false)
        whenever(pantry.canCover(neither)).thenReturn(false)
        val menu = Menu(mutableListOf(both, onlyPantry, onlyKitchen, neither), pantry, kitchen)

        menu.refresh()

        assertEquals(listOf(both), menu.getOrderables())
        verify(kitchen, never()).canCook(onlyKitchen)
    }

    @Test
    fun refreshReplacesThePreviousResult() {
        val dish = recipe(1)
        whenever(pantry.canCover(dish)).thenReturn(true, false)
        whenever(kitchen.canCook(dish)).thenReturn(true)
        val menu = Menu(mutableListOf(dish), pantry, kitchen)

        menu.refresh()
        assertEquals(listOf(dish), menu.getOrderables())
        menu.refresh()

        assertTrue(menu.getOrderables().isEmpty())
    }

    @Test
    fun refreshTwiceDoesNotListARecipeTwice() {
        val dish = recipe(1)
        whenever(pantry.canCover(dish)).thenReturn(true)
        whenever(kitchen.canCook(dish)).thenReturn(true)
        val menu = Menu(mutableListOf(dish), pantry, kitchen)

        menu.refresh()
        menu.refresh()

        assertEquals(listOf(dish), menu.getOrderables())
    }

    @Test
    fun restaurantWithoutRecipesHasAnEmptyMenu() {
        val menu = Menu(mutableListOf(), pantry, kitchen)

        menu.refresh()

        assertTrue(menu.getOrderables().isEmpty())
    }
}
