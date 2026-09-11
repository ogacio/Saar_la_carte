package de.unisaarland.cs.se.selab.sharedPackage.customers

/** The stage of one customer's visit, from ordering to leaving (specification, Section 2.2). */
enum class CustomerStatus {
    ORDERED,
    SERVED,
    DONE_EATING,
    LEFT,
}
