package de.unisaarland.cs.se.selab.kitchen
import de.unisaarland.cs.se.selab.logging.Logger.Kitchen.mealCooked
import de.unisaarland.cs.se.selab.sharedPackage.Ingredient
import de.unisaarland.cs.se.selab.sharedPackage.Meal
import de.unisaarland.cs.se.selab.sharedPackage.MealStatus
import de.unisaarland.cs.se.selab.sharedPackage.Menu
import de.unisaarland.cs.se.selab.sharedPackage.Order
import de.unisaarland.cs.se.selab.sharedPackage.Pantry
import de.unisaarland.cs.se.selab.sharedPackage.Recipe
import de.unisaarland.cs.se.selab.sharedPackage.RestaurantType
import de.unisaarland.cs.se.selab.sharedPackage.customers.CustomerGroup
import de.unisaarland.cs.se.selab.simulation.GlobalClock
import de.unisaarland.cs.se.selab.simulation.ReservationBook
import de.unisaarland.cs.se.selab.simulation.Supplier.resupply

private const val SEATS_PER_ESTIMATE = 10

/**
 * the kitchen, responsible for calling every function that happens inside the package
 */
class Kitchen(
    val roaster: CookRoaster,
    val pantry: Pantry,
    val queue: MutableList<Order>,
    val reservationBook: ReservationBook,
    val restaurantType: RestaurantType
) {
    /**
     * called by foh, puts the order into the queue
     */
    fun enqueue(o: Order) {
        queue.add(o)
    }

    /**
     * called by the restaurant, at the end of the ordering service
     * responsible for 1.updating the queue with the cooked meals 2. starting the cooking from the queue
     * 1. -> calling finished() on roaster
     * 2. -> making MutableList<Meal> from the queue with the same meals inside,
     * then start calling roaster.startCooking on all and reserve ingredients for all
     */
    fun cook(): Int {
        val finishedCount = handleFinishedMeals()

        val mealsToCookByRecipe = groupQueuedMealsByRecipe()

        val sortedEntries = mealsToCookByRecipe.entries.sortedWith(
            compareBy({ !it.key.isBasicFor(restaurantType) }, { it.key.getId() })
        )
        for (entry in sortedEntries) {
            roaster.startCooking(entry.value)
        }

        return finishedCount
    }

    private fun handleFinishedMeals(): Int {
        val ordersById = queue.associateBy { it.getId() }
        val cookedByCook = roaster.finished()
        var count = 0
        for ((cook, meals) in cookedByCook) {
            for (m in meals) {
                pantry.deleteFromReserved(m.recipe)
                val order = ordersById[m.orderId]
                if (order != null) {
                    mealCooked(
                        pantry.getRestaurantId(),
                        requireNotNull(cook.getId()),
                        1,
                        m.recipe.getDishName(),
                        order.ticksSince()
                    )
                }
                count++
            }
        }
        return count
    }

    private fun groupQueuedMealsByRecipe(): MutableMap<Recipe, MutableList<Meal>> {
        val mealsToCookByRecipe: MutableMap<Recipe, MutableList<Meal>> = mutableMapOf()
        for (o in queue) {
            val filtered = o.getMeals().filter {
                it.status == MealStatus.QUEUED && roaster.hasEligibleAndFree(it.recipe)
            }
            for (m in filtered) {
                if (mealsToCookByRecipe.containsKey(m.recipe)) {
                    mealsToCookByRecipe.getValue(m.recipe).add(m)
                } else {
                    mealsToCookByRecipe[m.recipe] = mutableListOf(m)
                }
            }
        }
        return mealsToCookByRecipe
    }

/**
* called by Restaurant -> prepare, gets the supplies from Supplier into the Pantry
*/
    fun planEvening(regulars: MutableList<CustomerGroup>, otherSeats: Int, menu: Menu) {
        pantry.checkDateAndCleanOut()
        val expected = getExpectedRecipes(regulars, otherSeats, menu)
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

    // get the expected recipes of the regulars, events and casuals
    private fun getExpectedRecipes(regulars: MutableList<CustomerGroup>, otherSeats: Int, menu: Menu):
        MutableMap<Recipe, Int> {
        // get the expected recipes of the regulars, events
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
        return expected
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
