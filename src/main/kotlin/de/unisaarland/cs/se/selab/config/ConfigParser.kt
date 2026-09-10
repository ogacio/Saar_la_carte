package de.unisaarland.cs.se.selab.config

/**
 * Abstract class for parsing configuration files.
 */

abstract class ConfigParser(
    protected val model: ParsedModel,
    protected val validator: Validator = Validator(),
) {

    protected val schemaPath: String

    // Two abstract methods that subclasses must implement to handle
    // specific parsing logic for different configuration files.
    protected abstract fun readEntities(path: String): Boolean
    protected abstract fun validateFileScope(): Boolean

    public fun parse(path: String): Boolean =
        /**
         * If the schema validation fails, the subsequent steps (reading entities and validating file scope)
         * will not be executed due to short-circuit evaluation of the logical AND operator.
         */
        validator.validateFile(path, schemaPath) &&
            readEntities(path) &&
            validateFileScope()
}
