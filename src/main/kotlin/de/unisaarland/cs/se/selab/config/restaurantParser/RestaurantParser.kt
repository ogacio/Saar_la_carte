package de.unisaarland.cs.se.selab.config.restaurantParser
import de.unisaarland.cs.se.selab.config.ConfigParser
import de.unisaarland.cs.se.selab.config.ParsedModel
import de.unisaarland.cs.se.selab.kitchen.CookType
import de.unisaarland.cs.se.selab.sharedPackage.Recipe
import de.unisaarland.cs.se.selab.sharedPackage.RestaurantType
import de.unisaarland.cs.se.selab.foh.Table
import de.unisaarland.cs.se.selab.kitchen.Kitchen
import de.unisaarland.cs.se.selab.sharedPackage.Order
import de.unisaarland.cs.se.selab.sharedPackage.RestaurantData
import de.unisaarland.cs.se.selab.sharedPackage.TableType
import de.unisaarland.cs.se.selab.simulation.Restaurant
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json
import java.io.File
import kotlin.collections.isNotEmpty
import kotlin.collections.map
import kotlin.collections.set

class RestaurantParser(model : ParsedModel) : ConfigParser(model) {

    protected override fun readEntities(path:String) : Boolean {

        var returnValue = true
        val text = File(path).readText()
        val fileDto: RestaurantFileDto = try {
            Json.decodeFromString(text)
        } catch (expected: SerializationException) {
            return false
        }
        for (restaurantDto in fileDto.restaurants) {
            val restaurant = serialiseRestaurant(restaurantDto)
            if (restaurant == null || !model.registerRestaurant(restaurant)) {
                returnValue = false
                break
            }
        }
        return returnValue
    }

    protected override fun validateFileScope() : Boolean {

        var returnValue = true
        if (!checkAtLeastOneRestaurant()) returnValue = false
        val presentTypes = model.allRestaurants().map{ it.type }.toSet()
        for (type in presentTypes) {
            if (!checkBasicDishCoverage(model.basicDishesFor(type))) {
                returnValue = false
                break
            }
        }
        return returnValue
    }

// helper function for readEntities

    private fun serialiseRestaurant(rjd:RestaurantJsonDto) : Restaurant? {
        try {
            val type = RestaurantType.valueOf(rjd.type)
            if (checkAtLeastOneOfEach(rjd)) {

                val foh = createFoh(rjd)
                val kitchen = createKitchen(rjd)
                val pantry = createPantry(rjd)
                val menu = createMenu(rjd)
                val clock = createClock(rjd)
                val data = createData(rjd)

                if (foh != null && kitchen != null && pantry != null && menu != null && clock != null && data != null) {
                    return Restaurant(
                        rjd.id, rjd.name, type, rjd.openingTickStart, rjd.openingTickEnd,
                        rjd.event, rjd.positiveRatings, rjd.negativeRatings,
                        foh, kitchen, pantry, menu, clock, data
                    )
                }
            }
        } catch (expected: IllegalArgumentException) { }
        return null
    }

/**
    creates foh, kitchen, pantry, menu, clock, data
 */

    private fun createData (rjd:RestaurantJsonDto) : RestaurantData? {
        var returnValue = true

        val tables = serialiseTables(rjd)
        val recipes = resolveRecipes(rjd)
        if (recipes != null && tables != null) {
            if (!(checkUniqueDishNames(recipes) && checkUniqueTableIds(tables)
                        && checkOpeningHours(rjd.openingTickStart, rjd.openingTickEnd))
            ) returnValue = false

            if (returnValue) {
                try {
                    val type = RestaurantType.valueOf(rjd.type)

                    val seats: MutableMap<TableType, Int> = mutableMapOf<TableType, Int>()
                    var totalSeats = 0

                    for (t in tables) {
                        if (seats.containsKey( t.type )) {
                            seats.replace( t.type, seats[ t.type ]!! + t.size)
                        }
                        else {
                            seats[ t.type ] = t.size
                        }
                        totalSeats += t.size
                    }

                    return RestaurantData(
                        rjd.id, type, rjd.openingTickStart, rjd.openingTickEnd,
                        recipes, seats, rjd.deliveryDrivers, rjd.event,
                        totalSeats,  mutableMapOf<Int, Int>()
                    )
                }
                catch (expected: IllegalArgumentException) { }
            }
        }
        return null
    }

    private fun createKitchen(rjd:RestaurantJsonDto) : Kitchen? {
        val roaster = serialiseCookCounts(rjd)
        if(roaster != null)  {
            return Kitchen(roaster, pantry, mutableListOf<Order>(), reservationBook, clock)
        }
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

// helper functions for serialiseRestaurant

    private fun checkUniqueDishNames(recipes:MutableList<Recipe>) : Boolean {
        for(recipe in recipes) {
            var count = 0
            val name : String = recipe.dishName
            for (r in recipes) {
                if (name == r.dishName) count++
            }
            if(count > 1) return false
        }
        return true
    }

    private fun checkUniqueTableIds(tables:MutableList<Table>) : Boolean {
        for(table in tables) {
            var count = 0
            val tableId : Int = table.id
            for (t in tables) {
                if (tableId == t.id) count++
            }
            if(count > 1) return false
        }
        return true
    }

    private fun checkOpeningHours(openingTick: Int,closingTick: Int) : Boolean {
        return openingTick < closingTick
    }

    private fun checkAtLeastOneOfEach(rjd: RestaurantJsonDto) : Boolean {

        return rjd.recipes.isNotEmpty() && rjd.kitchenStaff.isNotEmpty() && rjd.waitstaff > 0 && rjd.tables.isNotEmpty()
    }

//helper functions for validateFileScope
private fun checkBasicDishCoverage(dishes:Set<String>) : Boolean {
    return dishes.isNotEmpty()
}

    private fun checkAtLeastOneRestaurant() : Boolean {
        return model.allRestaurants().isNotEmpty()
    }
}
