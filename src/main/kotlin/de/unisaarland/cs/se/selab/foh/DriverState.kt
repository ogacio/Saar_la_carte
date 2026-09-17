package de.unisaarland.cs.se.selab.foh

/**
 * Represents the possible operating states of a delivery driver,
 * distinguishing between waiting for an order, actively delivering
 * an order, and returning to the restaurant after a delivery.
 */
enum class DriverState {
    DELIVERING,
    WAITING,
    RETURNING
}
