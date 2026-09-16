package de.unisaarland.cs.se.selab.simulation

import de.unisaarland.cs.se.selab.kicthen.Kitchen
import de.unisaarland.cs.se.selab.sharedPackage.Menu
import de.unisaarland.cs.se.selab.sharedPackage.Pantry

class SubUnits(
    val restaurantId: Int,
    val menu: Menu,
    val pantry: Pantry,
    val kitchen: Kitchen,
)