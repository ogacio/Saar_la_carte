package de.unisaarland.cs.se.selab.shared_data

/** One customer inside a group, following its subgroup's food preference if it has one. */
data class Customer(val preference: FoodPreference?)
