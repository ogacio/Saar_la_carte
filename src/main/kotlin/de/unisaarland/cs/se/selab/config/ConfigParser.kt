package de.unisaarland.cs.se.selab.config

/**
 * Template for parsing one configuration file (F01).
 *
 * [parse] runs three steps: the file must match its JSON schema, then its entities are read
 * into [model], then the rules that need the whole file are checked. Each step runs only if
 * the previous one succeeded, because `&&` stops at the first `false`.
 */
abstract class ConfigParser(
    protected val model: ParsedModel,
    protected val validator: Validator = Validator(),
) {
    /** The JSON schema file this parser's configuration must match. */
    protected abstract val schemaPath: String

    /** Reads the entities of the file at [path] into [model]; false if one of them is invalid. */
    protected abstract fun readEntities(path: String): Boolean

    /** Checks the rules that need all entities of the file at once; false if one is violated. */
    protected abstract fun validateFileScope(): Boolean

    /** Validates, reads and checks the file at [path]; true only if all three steps succeed. */
    fun parse(path: String): Boolean =
        validator.validateFile(path, schemaPath) && readEntities(path) && validateFileScope()
}
