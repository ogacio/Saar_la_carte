package de.unisaarland.cs.se.selab.config.restaurantParser
import de.unisaarland.cs.se.selab.kicthen.CookType
import de.unisaarland.cs.se.selab.shared_data.Recipe
import de.unisaarland.cs.se.selab.shared_data.RestaurantType
import de.unisaarland.cs.se.selab.foh.Table
import de.unisaarland.cs.se.selab.shared_data.TableType
import de.unisaarland.cs.se.selab.simulation.Restaurant
import kotlin.collections.set

class RestaurantParser : ConfigParser()
{
    protected override fun readEntities(path:String) : Boolean {

    }

    protected override fun validateFileScope() : Boolean {

    }

// helper function for readEntities

    private fun serialiseRestaurant(rjd:RestaurantJsonDto) : Restaurant? {
        try {
            val cooks = serialiseCookCounts(rjd)
            val tables = serialiseTables(rjd)
            val recipes = resolveRecipes(rjd)
            if(cooks != null && recipes != null && tables != null) {
                var restaurant = Restaurant(
                    rjd.id, rjd.name, rjd.type, rjd.openingTickStart, rjd.openingTickEnd,
                    rjd.deliveryDrivers, rjd.event, rjd.positiveRatings, rjd.negativeRatings, recipes,
                    cooks, rjd.waitstaff, tables
                )
                return restaurant
            }
        } catch (expected: IllegalArgumentException) {
            return null
        }
        return null
    }

    private fun resolveRecipes(rjd:RestaurantJsonDto) : MutableList<Recipe>? {
        val idDto : MutableList<Int> = rjd.recipes
        val recipes = mutableListOf<Recipe>()
        for (recipeId in idDto) {
            val recipe : Recipe? = model.recipe(recipeId)
            if (recipe != null) { recipes.add(recipe) }
            else { return null }
        }
        return recipes
    }

    private fun serialiseCookCounts(rjd:RestaurantJsonDto) : Map<CookType, Int>? {
        val cooksDto : Map<String, Int> = rjd.kitchenStaff
        val cooks = mutableMapOf<CookType, Int>()
        for ((key, value) in cooksDto) {
            try {
                if (key == "EXEC" && value != 0 && value != 1) { return null }
                cooks[CookType.valueOf(key)] = value
            } catch (expected: IllegalArgumentException) {
                return null
            }
        }
        return cooks
    }

    private fun serialiseTables(rjd:RestaurantJsonDto) : MutableList<Table>? {
        val tablesDto: MutableList<TableDto> = rjd.tables
        val tables = mutableListOf<Table>()

        for((id, type, size) in tablesDto) {
            try {
                val currentTable = Table(id, size, TableType.valueOf(type))
                tables.add(currentTable)
            } catch (expected: IllegalArgumentException) {
                return null
            }
        }

        return tables
    }

// helper functions for validateFileScope

    private fun checkUniqueDishNames(recipes:MutableList<Recipe>) : Boolean {
        for(recipe in recipes) {
            val name : String = recipe.dishName
            for (r in recipes) {
                if (name == r.dishName) return false
            }
        }
        return true
    }

    private fun checkUniqueTableIds(tables:MutableList<Table>) : Boolean {
        for(table in tables) {
            val tableId : Int = table.id
            for (t in tables) {
                if (tableId == t.id) return false
            }
        }
        return true
    }

    private fun checkOpeningHours(openingTick: Int,closingTick: Int) : Boolean {
        return openingTick < closingTick
    }

    private fun checkAtLeastOneOfEach(rjd: RestaurantJsonDto) : Boolean {

        return rjd.recipes.isNotEmpty() && rjd.kitchenStaff.isNotEmpty() && rjd.waitstaff > 0 && rjd.tables.isNotEmpty()
    }

    private fun checkBasicDishCoverage(type: RestaurantType, dishes:Set<String>) : Boolean {
        val recipes : MutableCollection<Recipe> = model.recipesById.values
        for(dish in dishes) {
            for (recipe in recipes) {
                if (recipe.dishName == dish && recipe.basicDishFor == type) return true
            }
        }
        return false
    }

    private fun checkAtLeastOneRestaurant() : Boolean {
        return restaurants != null
    }
}
