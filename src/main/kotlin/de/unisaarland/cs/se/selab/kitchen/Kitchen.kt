package de.unisaarland.cs.se.selab.kitchen
import de.unisaarland.cs.se.selab.sharedPackage.Ingredient
import de.unisaarland.cs.se.selab.sharedPackage.Meal
import de.unisaarland.cs.se.selab.sharedPackage.MealStatus
import de.unisaarland.cs.se.selab.sharedPackage.Menu
import de.unisaarland.cs.se.selab.sharedPackage.Order
import de.unisaarland.cs.se.selab.sharedPackage.Pantry
import de.unisaarland.cs.se.selab.sharedPackage.Recipe
import de.unisaarland.cs.se.selab.sharedPackage.customers.CustomerGroup
import de.unisaarland.cs.se.selab.simulation.GlobalClock
import de.unisaarland.cs.se.selab.simulation.ReservationBook
import de.unisaarland.cs.se.selab.simulation.Supplier.resupply

/**
 * the kitchen, responsible for calling every function that happens inside the package
 */
class Kitchen(
    val roaster: CookRoaster,
    val pantry: Pantry,
    var queue: MutableList<Order>,
    val reservationBook: ReservationBook
) {
    private val SEATS_PER_ESTIMATE = 10

    /**
     * called by foh, puts the order into the queue
     */
    fun enqueue(o: Order) {
        queue.add(o)
    }

    /**
     * called by foh, at the end of the ordering service
     * responsible for 1.updating the queue with the cooked meals 2. starting the cooking from the queue
     * 1. -> calling finished() on roaster
     * 2. -> making MutableList<Meal> from the queue with the same meals inside,
     * then start calling roaster.startCooking on all and reserve ingredients for all
     */
    fun cook() {
        val cooked = roaster.finished()
        for (m in cooked) {
            pantry.deleteFromReserved(m.recipe)
        }

        val mealsToCookByRecipe: MutableMap<Recipe, MutableList<Meal>> =
            mutableMapOf<Recipe, MutableList<Meal>>()
        for (o in queue) {
            for (m in o.meals) {
                if (m.status == MealStatus.QUEUED && roaster.hasEligible(m.recipe)) {
                    if (mealsToCookByRecipe.containsKey(m.recipe)) {
                        mealsToCookByRecipe.getValue(m.recipe).add(m)
                    } else { mealsToCookByRecipe[m.recipe] = mutableListOf(m) }
                }
            }
        }
        for ((recipe, mealList) in mealsToCookByRecipe) {
            roaster.startCooking(mealList)
        }
    }

/**
* called by Restaurant -> prepare, gets the supplies from Supplier into the Pantry
*/
    fun planEvening(regulars: MutableList<CustomerGroup>, otherSeats: Int, menu: Menu) {
        pantry.checkDateAndCleanOut()
        // get the expected recipes of the regulars and events
        val reservers = regulars.toMutableList()
        reservers.addAll(reservationBook.expectedFor(GlobalClock.getEvening()))
        val expected: MutableMap<Recipe, Int> = mutableMapOf()
        for (r in reservers) {
            val groupDishes = r.expectedDishes(menu.getRecipes())
            for ((recipe, amount) in groupDishes) {
                if (expected.containsKey(recipe)) {
                    expected[recipe] = expected.getValue(recipe) + amount
                } else {
                    expected[recipe] = amount
                }
            }
        }
        // get the expected recipes of the casuals
        for (recipe in menu.getRecipes()) {
            if (expected.containsKey(recipe)) {
                expected.replace(
                    recipe,
                    expected.getValue(recipe) + (otherSeats + SEATS_PER_ESTIMATE - 1) / SEATS_PER_ESTIMATE
                )
            } else {
                expected[recipe] = (otherSeats + SEATS_PER_ESTIMATE - 1) / SEATS_PER_ESTIMATE
            }
        }
        // builds a map how much exactly we will need from each ingredient
        val totalRequired: MutableMap<Ingredient, Int> = mutableMapOf()
        for ((recipe, count) in expected) {
            for (ingredient in recipe.ingredients) {
                val current = ingredient.ingredient
                if (totalRequired.containsKey(current)) {
                    totalRequired[current] = totalRequired.getValue(current) + count * ingredient.amount
                } else {
                    totalRequired[current] = count * ingredient.amount
                }
            }
        }
        // get needed ingredients by comparing pantry with totalRequired
        val needed: MutableMap<Ingredient, Int> = mutableMapOf()
        for ((ingredient, concreteAmount) in totalRequired) {
            val inPantry = pantry.getTotalIngredients(ingredient)
            if (concreteAmount > inPantry) {
                needed[ingredient] = concreteAmount - inPantry
            }
        }
        // calls supplier
        resupply(pantry, needed)
    }

    /**
     * returns if a given recipe can cook or not
     */
    fun canCook(r: Recipe): Boolean {
        return roaster.hasEligible(r)
    }

    /**
     * called by incident, changes the cooks
     */
    fun changeStaff(type: CookType, delta: Int) {
        roaster.changeStaff(type, delta)
    }

    /**
     * called at the end of the evening by Restaurant -> closeEvening(), resets everything
     */
    fun closeEvening() {
        roaster.resetEvening()
        queue.clear()
    }
}
