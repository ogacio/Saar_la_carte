package de.unisaarland.cs.se.selab.integration

import de.unisaarland.cs.se.selab.foh.services.RatingService
import de.unisaarland.cs.se.selab.sharedPackage.RestaurantData
import de.unisaarland.cs.se.selab.sharedPackage.RestaurantType
import de.unisaarland.cs.se.selab.sharedPackage.TableType
import de.unisaarland.cs.se.selab.sharedPackage.customers.GroupType
import de.unisaarland.cs.se.selab.simulation.BrowsingService
import de.unisaarland.cs.se.selab.simulation.CustomerRegistry
import de.unisaarland.cs.se.selab.simulation.GlobalClock
import de.unisaarland.cs.se.selab.simulation.SubUnits
import de.unisaarland.cs.se.selab.simulation.ratings.RatingBook
import de.unisaarland.cs.se.selab.simulation.ratings.RatingLikelihood
import de.unisaarland.cs.se.selab.testsupport.Fixtures.captureLog
import de.unisaarland.cs.se.selab.testsupport.Fixtures.casual
import de.unisaarland.cs.se.selab.testsupport.Fixtures.event
import de.unisaarland.cs.se.selab.testsupport.Fixtures.recipe
import org.mockito.kotlin.mock
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

/**
 * Integration of the restaurant decision (F24, F28) with the rating book and the rating service (P05):
 * the registry decides who browses, browsing books capacity, and ratings left by one group change the
 * decision of the next.
 */
class BrowsingRatingIntegrationTest {

    private val ratings = RatingBook
    private val ratingService = RatingService()

    private fun restaurant(id: Int, seats: Int = 10, hostsEvents: Boolean = false) = RestaurantData(
        id,
        RestaurantType.EUROPEAN,
        1,
        24,
        mutableListOf(recipe(1)),
        mutableMapOf(TableType.COMMON to seats),
        0,
        hostsEvents,
        seats,
        mutableMapOf(),
    ).also { ratings.initializeRatings(id, 0, 0) }

    /** Lets every group the registry names for [evening]/[tick] browse, and returns their choices by group id. */
    private fun decide(registry: CustomerRegistry, browsing: BrowsingService, evening: Int, tick: Int) =
        registry.deciding(evening, tick).associate { group ->
            group.id() to if (group.groupType() == GroupType.EVENT) {
                browsing.chooseForEvent(group, group.getEventEvening())
            } else {
                browsing.choose(group)
            }
        }

    @BeforeTest
    fun startOfEvening() {
        GlobalClock.advanceEvening()
        repeat(3) { GlobalClock.advanceTick() }
        captureLog()
    }

    @Test
    fun groupsDecideInRegistryOrderAndTheSecondSeesTheFirstsBooking() {
        val small = restaurant(1, seats = 4)
        val browsing = BrowsingService(mutableListOf(small, restaurant(2, seats = 4)), ratings)
        val first = casual(1, 4, evenings = listOf(2), visitingTick = 3)
        val second = casual(2, 4, evenings = listOf(2), visitingTick = 3)
        val registry = CustomerRegistry(mutableListOf(second, first))

        val choices = decide(registry, browsing, evening = 2, tick = 3)

        assertEquals(mapOf(1 to 1, 2 to 2), choices)
        assertEquals(0, small.getFreeSeats()[TableType.COMMON])
    }

    @Test
    fun negativeRatingFromAFailedVisitMovesTheNextGroupToAnotherRestaurant() {
        val browsing = BrowsingService(mutableListOf(restaurant(1), restaurant(2)), ratings)
        val sbuOne = SubUnits(1, mock(), mock(), mock())

        assertEquals(1, browsing.choose(casual(1, 2)))
        ratingService.rateFailedReservation(casual(1, 2, likelihood = RatingLikelihood.ALWAYS), sbuOne)

        assertEquals(2, browsing.choose(casual(2, 2)))
    }

    @Test
    fun groupThatNeverRatesDoesNotChangeTheRanking() {
        val browsing = BrowsingService(mutableListOf(restaurant(1), restaurant(2)), ratings)
        val sbuOne = SubUnits(1, mock(), mock(), mock())

        ratingService.rateFailedReservation(casual(1, 2, likelihood = RatingLikelihood.NEVER), sbuOne)

        assertEquals(1, browsing.choose(casual(2, 2)))
    }

    @Test
    fun eventBookingUsesUpEventSeatsForOtherEventsOfThatEvening() {
        val host = restaurant(1, seats = 6, hostsEvents = true)
        val browsing = BrowsingService(mutableListOf(host), ratings)
        val registry = CustomerRegistry(mutableListOf(event(1, 4, eventEvening = 5), event(2, 4, eventEvening = 5)))

        val choices = decide(registry, browsing, evening = 2, tick = 1)

        assertEquals(1, choices[1])
        assertNull(choices[2])
    }
}
