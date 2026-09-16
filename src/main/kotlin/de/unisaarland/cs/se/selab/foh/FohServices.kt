package de.unisaarland.cs.se.selab.foh

import de.unisaarland.cs.se.selab.foh.services.DiningService
import de.unisaarland.cs.se.selab.foh.services.EscortingService
import de.unisaarland.cs.se.selab.foh.services.OrderingService
import de.unisaarland.cs.se.selab.foh.services.RatingService
import de.unisaarland.cs.se.selab.foh.services.SeatingService
import de.unisaarland.cs.se.selab.foh.services.ServingService

/**
 * The six services of one restaurant's front of house, one per tick step it runs. Bundled into one
 * object so [FrontOfTheHouse] keeps a short constructor.
 */
data class FohServices(
    val seating: SeatingService,
    val ordering: OrderingService,
    val serving: ServingService,
    val dining: DiningService,
    val escorting: EscortingService,
    val rating: RatingService,
)
