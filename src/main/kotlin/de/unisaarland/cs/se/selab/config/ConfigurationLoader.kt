package de.unisaarland.cs.se.selab.config

class ConfigurationLoader(
    private val foodPath: String,
    private val restaurantsPath: String,
    private val scenarioPath: String,
    private val maxTicks: Int
) {

    private val model = ParsedModel()

    private fun runStage(parser: ConfigParser, path: String): Boolean {
        val valid = parser.parse(path)
        val fileName = File(path).name
        if (valid) Logger.configParsed(fileName) else Logger.configInvalid(fileName)
        return valid
    }

    // Simulator builder
    private fun buildSimulator(): Simulator {
        val maxTicks = maxTicks
        val restaurants = model.allRestaurants()
        val customers = CustomerRegistry(model.allCustomerGroups())
        val incidents = model.allIncidents()
        val browsingService = BrowsingService()
        val deliveryService = DeliveryService()
        val ratingBook = RatingBook()
        val statistics = Statistics()

        return Simulator(
            maxTicks,
            restaurants,
            customers,
            incidents,
            browsingService,
            deliveryService,
            ratingBook,
            statistics,
        )
    }

    /**
     * Parses the three files in the order the spec requires and returns the
     * simulation, or null as soon as one file is invalid.
     */
    public fun load(): Simulator? {
        if (!runStage(FoodParser(model), foodPath)) return null
        if (!runStage(RestaurantParser(model), restaurantsPath)) return null
        if (!runStage(ScenarioParser(model), scenarioPath)) return null

        return buildSimulator()
    }
}
