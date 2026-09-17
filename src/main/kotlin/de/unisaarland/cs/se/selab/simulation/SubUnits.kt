package de.unisaarland.cs.se.selab.simulation

import de.unisaarland.cs.se.selab.kitchen.Kitchen
import de.unisaarland.cs.se.selab.sharedPackage.Menu
import de.unisaarland.cs.se.selab.sharedPackage.Pantry

/**
 * The parts of one restaurant that the front-of-house services need: its id, menu, pantry and kitchen.
 * Every service receives it as `sbu`, so the services do not depend on the whole [Restaurant].
 */
data class SubUnits(
    val restaurantId: Int,
    val menu: Menu,
    val pantry: Pantry,
    val kitchen: Kitchen,
)
