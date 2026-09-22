package de.unisaarland.cs.se.selab.foh

/**
 * Represents the possible operating states of a delivery driver.
 *
 * LOADING is new (forum #6): the driver is reserved for one order and already stores the meals the
 * waitstaff has handed over so far, but does not drive until the order is complete.
 */
enum class DriverState {
    LOADING,
    DELIVERING,
    WAITING,
    RETURNING
}
