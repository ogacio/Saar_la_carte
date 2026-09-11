package de.unisaarland.cs.se.selab.foh


class SeatingService(
    private val tables: TableAssignmentService,
    private val waitstaff: WaiterAssignmentService,
    private val reservations: ReservationBook,
) {

    public fun seat(visit: Visit, sbu: SubUnits): Boolean {
        // TODO
    }

    
}