package de.unisaarland.cs.se.selab.systemtest.selab26.utils

private const val DEBUG = "DEBUG"

/**
 * A validation test: [food], [restaurants] and [scenario] are parsed in this order; the file named
 * [checked] ("food", "restaurants" or "scenario") is the one under test and must be valid or not.
 */

abstract class ValidationSystemTest(
    private val checked: String,
    private val valid: Boolean,
) : ExampleSystemTestExtension() {
    override val logLevel = DEBUG
    override val maxTicks = 0

    override suspend fun run() {
        val files = listOf("food" to food, "restaurants" to restaurants, "scenario" to scenario)
        for ((kind, path) in files) {
            val file = path.substringAfterLast('/')
            if (kind != checked) {
                assertNextLine("[INFO] Initialization Info: $file successfully parsed and validated.")
                continue
            }
            val line = if (valid) {
                "[INFO] Initialization Info: $file successfully parsed and validated."
            } else {
                "[IMPORTANT] Initialization Info: $file is invalid."
            }
            assertNextLine(line)
            return
        }
    }
}
