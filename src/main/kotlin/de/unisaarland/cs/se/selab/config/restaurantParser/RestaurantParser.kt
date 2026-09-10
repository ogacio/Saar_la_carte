package de.unisaarland.cs.se.selab.config.restaurantParser
import de.unisaarland.cs.se.selab.kicthen.CookType
import de.unisaarland.cs.se.selab.shared_data.Recipe
import de.unisaarland.cs.se.selab.shared_data.RestaurantType
import de.unisaarland.cs.se.selab.foh.Table

class RestaurantParser //extend ConfigParser
{
    @Override
    protected fun readEntries(path:String) : Boolean {
        return true
    }

    @Override
    protected fun validateFileScope() : Boolean {

    }

    private fun serialiseRestaurant(restaurantDto:RestaurantJsonDto) : Restaurant? {

    }

    private fun resolveRecipes(restaurantDto:RestaurantJsonDto):MutableList<Recipe>? {

    }

    private fun serialiseCookCounts(restaurantDto:RestaurantJsonDto):Map<CookType, Int>? {

    }

    private fun serialiseTables(restaurantDto:RestaurantJsonDto):MutableList<Table>? {

    }

    private fun checkUniqueDishNames(recipes:MutableList<Recipe>):Boolean {

    }

    private fun checkUniqueTableIds(tables:MutableList<Table>):Boolean {

    }

    private fun checkOpeningHours(openingTick: Int,closingTick: Int):Boolean {

    }

    private fun checkAtLeastOneOfEach(restaurantDto : RestaurantJsonDto):Boolean {

    }

    private fun checkBasicDishCoverage(type : RestaurantType, dishes:Set<String>):Boolean {

    }

    private fun checkAtLeastOneRestaurant():Boolean {

    }
}
