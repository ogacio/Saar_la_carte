package de.unisaarland.cs.se.selab.sharedPackage.customers
import de.unisaarland.cs.se.selab.kitchen.CookType
import de.unisaarland.cs.se.selab.sharedPackage.Ingredient
import de.unisaarland.cs.se.selab.sharedPackage.Recipe
import de.unisaarland.cs.se.selab.sharedPackage.RecipeIngredient
import de.unisaarland.cs.se.selab.sharedPackage.UnitType
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

/* F26: FoodPreference: which dishes a subgroup accepts, ranks, and favours. */

class FoodPreferenceTest {

    private fun ingredient(name: String) = Ingredient(name, UnitType.G, 100, 3)

    private fun recipe(id: Int, dishName: String, ingredientNames: List<String>, amount: Int = 1) = Recipe(
        id = id,
        dishName = dishName,
        minuteDuration = 10,
        cookTypes = setOf(CookType.EXEC),
        ingredients = ingredientNames.map { RecipeIngredient(ingredient(it), amount) }.toMutableList(),
        basicDishFor = null,
    )

    private fun preference(
        size: Int = 1,
        excluded: Set<Ingredient> = emptySet(),
        preferred: Set<Ingredient> = emptySet(),
        favourites: List<String> = emptyList(),
    ) = FoodPreference(size, excluded, preferred, favourites)

    /** A recipe using none of the excluded ingredients is accepted. */
    @Test
    fun acceptsARecipeWithoutAnyExcludedIngredient() {
        val chicken = ingredient("chicken")
        val pref = preference(excluded = setOf(chicken))
        val riceOnly = recipe(1, "rice bowl", listOf("rice"))

        assertTrue(pref.accepts(riceOnly))
    }

    /** A recipe using even one excluded ingredient is rejected.
     @Test
     fun rejectsARecipeContainingAnExcludedIngredient() {
     val chicken = ingredient("chicken")
     val pref = preference(excluded = setOf(chicken))
     val chickenRice = recipe(1, "chicken rice", listOf("chicken", "rice"))

     assertFalse(pref.accepts(chickenRice))
     }
     */

    /** With no excluded ingredients at all, every recipe is accepted. */
    @Test
    fun acceptsEverythingWhenNothingIsExcluded() {
        val pref = preference()
        val anyDish = recipe(1, "anything", listOf("chicken", "rice", "garlic"))

        assertTrue(pref.accepts(anyDish))
    }

    /** Rank counts distinct preferred ingredients present, not their amounts.
     @Test
     fun rankCountsPreferredIngredientsByKindNotAmount() {
     val rice = ingredient("rice")
     val garlic = ingredient("garlic")
     val pref = preference(preferred = setOf(rice, garlic))
     val dish = recipe(1, "dish", listOf("rice", "garlic", "onion"))

     assertEquals(2, pref.rank(dish))
     }
     */

    /** A recipe using none of the preferred ingredients ranks zero. */
    @Test
    fun rankIsZeroWhenNoPreferredIngredientIsPresent() {
        val pref = preference(preferred = setOf(ingredient("rice")))
        val dish = recipe(1, "dish", listOf("beef", "pasta"))

        assertEquals(0, pref.rank(dish))
    }

    /** The first listed favourite dish that is actually on the menu is returned. */
    @Test
    fun firstFavouriteReturnsTheFirstMatchInDeclaredOrder() {
        val pref = preference(favourites = listOf("beef pasta", "chicken rice"))
        val menu = listOf(
            recipe(1, "chicken rice", listOf("chicken")),
            recipe(2, "beef pasta", listOf("beef")),
        )

        val favourite = pref.firstFavourite(menu)

        assertEquals("beef pasta", favourite?.getDishName())
    }

    /** If the first-declared favourite is not on the menu, the next one in order is used. */
    @Test
    fun firstFavouriteSkipsNamesNotOnTheMenu() {
        val pref = preference(favourites = listOf("not on menu", "chicken rice"))
        val menu = listOf(recipe(1, "chicken rice", listOf("chicken")))

        val favourite = pref.firstFavourite(menu)

        assertEquals("chicken rice", favourite?.getDishName())
    }

    /** If none of the favourite names are on the menu, null is returned. */
    @Test
    fun firstFavouriteReturnsNullWhenNoneAreOnTheMenu() {
        val pref = preference(favourites = listOf("not on menu", "also not on menu"))
        val menu = listOf(recipe(1, "chicken rice", listOf("chicken")))

        assertNull(pref.firstFavourite(menu))
    }

    /** With no declared favourites at all, firstFavourite is null regardless of the menu. */
    @Test
    fun firstFavouriteIsNullWithNoDeclaredFavourites() {
        val pref = preference(favourites = emptyList())
        val menu = listOf(recipe(1, "chicken rice", listOf("chicken")))

        assertNull(pref.firstFavourite(menu))
    }

    /** Plain accessors return exactly what was constructed. */
    @Test
    fun accessorsReturnConstructedValues() {
        val chicken = ingredient("chicken")
        val rice = ingredient("rice")
        val pref = FoodPreference(3, setOf(chicken), setOf(rice), listOf("chicken rice"))

        assertEquals(3, pref.size())
        assertEquals(setOf(chicken), pref.excluded())
        assertEquals(setOf(rice), pref.preferred())
        assertEquals(listOf("chicken rice"), pref.favouriteDishNames())
    }
}
