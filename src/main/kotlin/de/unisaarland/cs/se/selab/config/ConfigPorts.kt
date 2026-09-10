package de.unisaarland.cs.se.selab.config

import de.unisaarland.cs.se.selab.shared.Ingredient
import de.unisaarland.cs.se.selab.shared.Recipe

/**
 * One stage of the configuration: validate the file, read its entities, validate the file scope.
 *
 * Implemented here so that [FoodParser] can be written and tested before the shared abstract
 * `ConfigParser` template of the configuration package exists; the template will implement this
 * interface and [FoodParser] then only keeps its two `readEntities` / `validateFileScope` steps.
 */
interface ConfigStage {
    /**
     * Parses and validates the configuration file at [path] and reports whether it is valid.
     */
    fun parse(path: String): Boolean
}

/**
 * Validates a configuration file against its JSON schema.
 *
 * The `Validator` of the configuration package implements this.
 */
interface FileValidator {
    /**
     * Whether the file at [path] satisfies the schema at [schemaPath].
     */
    fun validateFile(path: String, schemaPath: String): Boolean
}

/**
 * The part of the parsed model the food stage writes into and reads back.
 *
 * `ParsedModel` implements this; the food stage only ever needs these four operations.
 */
interface FoodRegistry {
    /**
     * Registers [ingredient] and reports whether its name was still free.
     */
    fun registerIngredient(ingredient: Ingredient): Boolean

    /**
     * Registers [recipe] and reports whether its id was still free.
     */
    fun registerRecipe(recipe: Recipe): Boolean

    /**
     * The already registered ingredient called [name], or null if there is none.
     */
    fun ingredient(name: String): Ingredient?

    /**
     * Every recipe registered so far.
     */
    fun allRecipes(): List<Recipe>
}
