package de.unisaarland.cs.se.selab.config

import de.unisaarland.cs.se.selab.kitchen.CookType
import de.unisaarland.cs.se.selab.sharedPackage.Ingredient
import de.unisaarland.cs.se.selab.sharedPackage.Recipe
import de.unisaarland.cs.se.selab.sharedPackage.RecipeIngredient
import de.unisaarland.cs.se.selab.sharedPackage.RestaurantType
import de.unisaarland.cs.se.selab.sharedPackage.UnitType
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json
import java.io.File
import java.io.IOException

/**
 * The first configuration stage: it turns the food file into [Ingredient]s and [Recipe]s and
 * registers them in the model.
 *
 * The stage keeps the order `validateFile` then `readEntities` then `validateFileScope`, so every
 * constraint that can be decided on a single entity fails before the whole-file constraints run.
 */
class FoodParser(
    private val model: FoodRegistry,
    private val validator: FileValidator,
) : ConfigParser {
    private val schemaPath = SCHEMA_PATH
    private val json = Json { ignoreUnknownKeys = false }
    private var ingredientCount = 0

    override fun parse(path: String): Boolean =
        validator.validateFile(path, schemaPath) && readEntities(path) && validateFileScope()

    /**
     * Reads the food file at [path] and registers everything it contains.
     */
    private fun readEntities(path: String): Boolean {
        val file = decode(path) ?: return false
        return readIngredients(file.ingredients) && readRecipes(file.recipes)
    }

    /**
     * Turns [dtos] into ingredients and registers them, failing on the first invalid entry.
     */
    private fun readIngredients(dtos: List<IngredientDto>): Boolean {
        for (dto in dtos) {
            val ingredient = serialiseIngredient(dto) ?: return false
            if (!model.registerIngredient(ingredient)) {
                return false
            }
            ingredientCount++
        }
        return true
    }

    /**
     * Turns [dtos] into recipes and registers them, failing on the first invalid entry.
     */
    private fun readRecipes(dtos: List<RecipeDto>): Boolean {
        for (dto in dtos) {
            val recipe = serialiseRecipe(dto) ?: return false
            if (!model.registerRecipe(recipe)) {
                return false
            }
        }
        return true
    }

    /**
     * Reads the file at [path] into its root DTO, or returns null if it cannot be decoded.
     */
    private fun decode(path: String): FoodFileDto? = try {
        json.decodeFromString<FoodFileDto>(File(path).readText())
    } catch (_: SerializationException) {
        null
    } catch (_: IllegalArgumentException) {
        null
    } catch (_: IOException) {
        null
    }

    /**
     * Turns [dto] into an [Ingredient], or returns null if one of its values is out of range.
     */
    private fun serialiseIngredient(dto: IngredientDto): Ingredient? {
        val unit = UnitType.from(dto.unit) ?: return null
        if (dto.packagingVolume <= 0 || dto.bestBefore <= 0) {
            return null
        }
        return Ingredient(dto.name, unit, dto.packagingVolume, dto.bestBefore)
    }

    /**
     * Turns [dto] into a [Recipe], or returns null if it is inconsistent with the file so far.
     */
    private fun serialiseRecipe(dto: RecipeDto): Recipe? {
        if (dto.id < 0 || dto.duration < MIN_DURATION || dto.duration > MAX_DURATION) {
            return null
        }
        val cookTypes = serialiseCookTypes(dto.cookType) ?: return null
        val ingredients = serialiseRecipeIngredients(dto) ?: return null
        val basicDishFor = dto.basicDishFor?.let { raw -> RestaurantType.entries.firstOrNull { it.name == raw } }
        if (dto.basicDishFor != null && basicDishFor == null) {
            return null
        }
        return Recipe(dto.id, dto.dishName, dto.duration, cookTypes, ingredients, basicDishFor)
    }

    /**
     * Resolves the cook type names in [raw], or returns null if one is unknown or the list is empty.
     */
    private fun serialiseCookTypes(raw: List<String>): Set<CookType>? {
        val resolved = raw.mapNotNull { name -> CookType.entries.firstOrNull { it.name == name } }
        return if (resolved.size == raw.size && resolved.isNotEmpty()) resolved.toSet() else null
    }

    /**
     * Resolves the ingredient references of [dto] against the already registered ingredients.
     */
    private fun serialiseRecipeIngredients(dto: RecipeDto): MutableList<RecipeIngredient>? {
        if (dto.ingredients.isEmpty()) {
            return null
        }
        val resolved = mutableListOf<RecipeIngredient>()
        for (entry in dto.ingredients) {
            val ingredient = model.ingredient(entry.name) ?: return null
            if (entry.amount <= 0) {
                return null
            }
            if (entry.unit != null && UnitType.from(entry.unit) != ingredient.unit) {
                return null
            }
            resolved.add(RecipeIngredient(ingredient, entry.amount))
        }
        return resolved
    }

    /**
     * Checks the constraints that can only be decided once the whole food file has been read.
     */
    private fun validateFileScope(): Boolean {
        val recipes = model.allRecipes()
        return ingredientCount > 0 && recipes.isNotEmpty() && checkBasicDishUniqueness(recipes)
    }

    /**
     * Whether there is exactly one default recipe per basic dish name.
     */
    private fun checkBasicDishUniqueness(recipes: List<Recipe>): Boolean {
        val basicNames = recipes.filter { it.basicDishFor != null }.map { it.dishName }
        return basicNames.size == basicNames.toSet().size
    }

    /** File scope constants of the food stage. */
    private companion object {
        const val SCHEMA_PATH = "/schema/food.schema"
        const val MIN_DURATION = 2
        const val MAX_DURATION = 40
    }
}
