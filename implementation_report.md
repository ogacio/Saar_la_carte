# Implementation Report

## Individual Contributions

`feature_assignments.yaml` at the repository root has been resubmitted and updated to reflect
the F04/F06 swap between Stefan Marinkov and Biborka Fancsali (see Adjustments below).

Implementation ownership below is cross-checked against `git log --follow` per source file
(top-committer per file in `src/main`), not just commit messages. Testing ownership is
cross-checked the same way against `src/test` and `src/systemtest`.

| Student | Major contributions (implementation) | Major contributions (testing) |
|---|---|---|
| Ognjen Stojicic (ogacio) | F01 Simulation (`Simulator`, `Main`, `ConfigParser`, `ConfigurationLoader`, `ParsedModel`), F14 Evening Table Reservation, F16 Seating, F18 Ordering, F19 Serving, F21 Escorting, P02 Event Seating, P03 Event Other Actions; owns the entire `foh/visit` state machine (`Visit`, `VisitState`, `SeatedState`, `AwaitingSeatState`, `AwaitingMealState`, `EatingUpState`, `ReadyToLeaveState`, `GoneState`) and `FrontOfTheHouse`, `GlobalClock`, `ReservationBook`, `SubUnits`; substantial later co-authorship (by line count) on `WaiterAssignmentService` (F17, originally Teodor's) and `DeliveryDesk` (F20, originally Constantin's) | Tested Teodor's features (F02, F03, F15, F17, F31-F34, P04): `F01ConfigurationLoaderTest`, `F06IncidentParsingTest`, most of the `foh/visit` unit tests, `F14CancelledReservationTest`, `F21TableResetTest`, `F22RegularFailureSeriesTest`, `F24DeliveryFlowTest`, `F30EndOfEveningTest`, `FohVisitIntegrationTest`, plus a large share of the `src/systemtest` suite (delivery, incidents, kitchen, menu, statistics areas) |
| Teodor Vasilev (Teodor.Vasilev) | F02 Logging (`Logger`, `LogFormat`, `LogLevel`, `LogSink`), F03 Parse Ingredients & Recipes (`FoodParser`, `FoodDtos`, `Recipe`, `RecipeIngredient`, `Ingredient`, `UnitType`, `TableType`), F15/F17 (`Table`, `TableAssignmentService`, `Waiter`, `WaiterAssignmentService`), F31-F34 Incidents (`Incident`, `Incidents`) | Tested Ognjen's features (F01, F14, F16, F18-F21): `EscortingServiceTest`, `OrderingServiceTest`, `RatingServiceTest`, `SeatingServiceTest`, `ServingServiceDeliveryAndEventTest`, `BrowsingRatingIntegrationTest`, `SimulationRunIntegrationTest`, `CustomerRegistryTest`, `DeliveryServiceOrderingTest`/`DeliveryServiceTest`, `GlobalClockTest`, `ReservationBookTest`, `RestaurantTest`, `StatisticsTest`; system tests for F15/F17/F32 mutants, driver timing, coverage, simulation-flow and table/waiter-load areas |
| Stefan Marinkov (stma00002) | F05 Parse Customers (`ScenarioParser`, `CustomerGroupJsonDto`, `FoodPreferenceDto`, `ScenarioFileDto`, `CustomerGroupSerialiser`), F06 Parse Incidents (swapped in from Biborka: `IncidentSerialiser`, `IncidentJsonDto`), F22/F23 (`CustomerGroup`, `RegularCustomerGroup`, `CasualCustomerGroup`, `EventCustomerGroup`, `Customer`, `History`, `CustomerStatus`, `GroupType`), F25 (`CustomerRegistry.deciding`), F26 (`FoodPreference`), F27 (waiting/eating/give-up clock logic in the above), P05 (`ratingFor` in the `CustomerGroup` hierarchy); also `Validator` (cross-cutting config validation, used by all parsers) | Tested Ognjen's and Teodor's features per assignment: `OrderingServiceOrderedTest`, `ServingServiceTest`, `TableAssignmentServiceTest`, `WaiterAssignmentServiceTest`, `IncidentsTest`, all `logging/*Test` files, `SimulatorTest`, `RestaurantTickTest`, `TableLifecycleIntegrationTest`, `DeliveryIntegrationTest`, `MenuKitchenIntegrationTest`; wrote and registered the bulk of `src/systemtest` (`SystemTestRegistration` itself, most `foh/`, `gvalidation/`, `gdelivery/`, `gkitchen/`, `f03/`, `incidents/` system tests), including the session's mutant-hunting tests; surfaced and fixed several bugs while testing (tick log format, delivery rating/eating lifecycle, ingredient packaging calculation, F27 patience-window off-by-one) |
| Biborka Fancsali (fancsaliborka) | F04 Parse Restaurants (swapped in from Stefan: `RestaurantParser`, `RestaurantFileDto`, `RestaurantJsonDto`, `TableDto`, `RestaurantType`), F08 Supplier, F09 Pantry (`Pantry`, `StockEntry`), F10-F12 Cooking (`Cook`, `CookRoaster`, `CookType`, `Kitchen`), P01 Pantry Best-Before/Reservation; also `Meal`, `MealStatus`, `Order`, `Restaurant` | Tested F22/F23/F25-F27 (Stefan's features) and supported F07/F13: `IncidentSerialiserTest`, `RestaurantParserTest`, `F27DeliveryDriverTest`, `F27VisitTest`, `F25RestaurantDataTest`, `F25EventCustomerGroupTest`, `FoodPreferenceTest`, `F25BrowsingServiceTest`; system tests across `f03/`, `f04/`, `foh/` (preference/regular-recurrence/table-merge scenarios), `incidents/`, and `utils/ValidationSystemTest` |
| Constantin Hartmann (op23no5) | F07 Statistics, F13 Menu, F24/F28/F29 (`BrowsingService`, `DeliveryService`, `DeliveryDriver`, `DriverState`, `DeliveryDesk`), `RestaurantData` | Tested Biborka's features (F04, F08-F12, P01): `ConfigParsingIntegrationTest`, `F10F12ReservationAndServingTest`, `F11OverlappingBatchesTest`, `F23RatingDecisionIntegrationTest`, `F26PreferenceOrderingTest`, `F27ServingWaitingBoundaryTest`, `PantrySupplierTest`, `F10QueueBoundaryTest`, `F11StaffBoundaryTest`, `F12DurationBoundaryTest`, `F25DecisionBoundaryTest`; system tests for `f04/`, `f05/`, `f06/` scope-and-boundary areas, kitchen/supplier boundary tests |

Only major contributions are listed; routine bugfixes, merge-conflict resolutions, and detekt
cleanups are omitted per the reporting guidelines.

---

## Adjustments from the Implementation Plan

- **F04 / F06 swap (2026-09-20):** In the original implementation plan, Stefan Marinkov was
  assigned `F04` (`RestaurantParser`) and Biborka Fancsali was assigned `F06` (`IncidentSerialiser`
  / incident parsing). Partway through implementation the two swapped: Biborka took over `F04`
  (`RestaurantParser`, kitchen/pantry/supplier groundwork) and Stefan took over `F06` (incident
  parsing/validation). The swap was made because Biborka had already begun deep work on the
  restaurant/kitchen data model while Stefan's incident-parsing work overlapped more naturally
  with his customer-side scenario parsing (F05). `feature_assignments.yaml` reflects the swap;
  testing responsibilities for both features were unaffected (still assigned to the same
  non-implementer pair).

---

## Detailed Timeline

### Ognjen Stojicic

- **2026-09-10:** `ConfigParser`/`ConfigurationLoader`, `GlobalClock` and ratings v1, `Simulator`
  skeleton and package, `main` + `ParsedModel` v1.
- **2026-09-11:** FOH and Visit skeleton v1.
- **2026-09-14:** Visit and visit states implemented, entire serving phase.
- **2026-09-15:** Import fixes.
- **2026-09-16:** Customer-done-eating handling, detekt fixes, parser fixes, services v3,
  simulator wiring.
- **2026-09-17:** First green build; getters/detekt cleanup; basic services/visit tests.
- **2026-09-18:** Black-box testing of other members' features, delivery service fixes,
  `FohFlowTest`, more self-authored unit/integration tests.
- **2026-09-19:** Forum-driven adjustments.
- **2026-09-20:** Large batch of new system tests registered; own-test fixes.
- **2026-09-21:** System tests for FOH and incidents; mutant-targeting test improvements.
- **2026-09-22:** Delivery eating-clock fix; further mutant-targeting tests and fixes.
- **2026-09-23:** Serving fixes; system tests targeting multiple mutants and previously
  failing tests.

### Teodor Vasilev

- **2026-09-10:** Logging package (F02) with all log statements routed through `Logger`; shared
  food model and food configuration stage (F03); tables and waitstaff skeleton (F15/F17);
  incident package (F31-F34); event customer groups (P04); enum restructuring.
- **2026-09-14:** Import fixes.
- **2026-09-16:** Test additions; updates to own feature areas.
- **2026-09-17:** Getter-based access cleanup; small fixes.
- **2026-09-18:** Aligned `SimulatorTest` with new tick log format; food parser fix; branch
  coverage raised for F01/F14/F18-F20/F24/F29/F30; logger/waiter-consume updates.
- **2026-09-21:** Basic/non-basic dish uniqueness fixes for `FoodParser` (including a revert
  after a regression).
- **2026-09-22:** F17 event orders kept with seating waiters; system tests for F15/F17/F32
  mutants and the "Stonks" mutant; system tests for the least-covered simulation features.
- **2026-09-23:** F17 partial-seating-load fix for failed EVENT seating; system tests for
  F20/F24/F29 (driving distance, kitchen status, give-up, returning driver); system tests for
  the six mutants still uncaught as of `d4ef9e0`; unit tests for F24/F28/F29/P05 coverage gaps.

### Stefan Marinkov

- **2026-09-11:** F05/F06 scenario parser; F22/F23/F26/F27 customer group logic; P04 event
  customer group encapsulation; shared base classes/enums skeleton.
- **2026-09-14:** F02/F03 tests; F22/F25 fixes; adapting code to teammates' interface changes.
- **2026-09-16:** Validator additions; fixes to F05, F22/F23/F25/F26, F27.
- **2026-09-17:** detekt and system-test fixes; unit/integration tests across F01, F15-F21,
  F31-F34, P04 (his testing-block assignment); fixed stale constructor/field-access references
  surfaced by those tests.
- **2026-09-18:** Delivery-desk/cook/pantry bugfixes surfaced during testing (cooking duration,
  ingredient packaging calculation, delivery rating/eating lifecycle, tick log format); wiring
  the delivery service into the simulator; statistics for cooked meals.
- **2026-09-21:** Delivery order tick-timing fix (forum-reported); unregistering tests found to
  be invalid against the reference.
- **2026-09-22:** Added system tests killing 16 previously-uncaught mutants; fixed an F27
  patience-window off-by-one (forum 335/340) and its fixture; additional fixture tests for
  delivery/kitchen/validation gap areas.
- **2026-09-23:** F04/F06 swap work continuation (incident parsing as F06 owner); further
  mutant-targeting system tests; forum-driven fixes (event favourite validation, last-3-ticks
  retry refusal, delivery ordering interleaving, EVENT order-taking wiring); coverage tests;
  unregistering tests that failed against the real reference.

### Biborka Fancsali

- **2026-09-10:** Shared data model and initial restaurant parser groundwork.
- **2026-09-11:** `RestaurantParser` implementation (multiple iterations), Pantry/Supplier
  skeletons, Kitchen package improvements, `Order` creation.
- **2026-09-14:** `Cook` fully implemented (`CookRoaster` pending review); Supplier/Pantry done,
  `reduceBestUntil` added to `Ingredient`.
- **2026-09-15:** `CookRoaster` fully implemented.
- **2026-09-16:** `createData` added to `RestaurantParser`; `Restaurant` class added; getters
  added across classes; `RestaurantParser` re-implementation; conflict resolution.
- **2026-09-17:** Multiple rounds of detekt fixes; documentation added to kitchen/restaurant
  parser/pantry/supplier packages; `Kitchen` implemented including `planEvening`; helper
  functions added; `RestaurantParser` and `Kitchen` double-checked and corrected.
- **2026-09-18:** Cook/supplier error fixes; loggers added to kitchen package; meal-handling
  fixes.
- **2026-09-20:** Kitchen and pantry updates.
- **2026-09-21:** Delivery drivers initialised in `RestaurantParser`; `expectedDishes` update in
  `Kitchen`; F03 recipe-parsing tests; `planEvening`/order-processing fixes; recipe validation
  and unique-restaurant-id checks; free-seat count fix for kitchen; tests for regular customer
  groups (F22), F23/F26.
- **2026-09-22:** F27 tests; supplier "mark unavailable" fix for repeated incidents; cleaner
  free-seat counting; aborted meals excluded from the cooked-meals logger.
- **2026-09-23:** F25 tests, additional F27 tests, further system tests and test fixes.

### Constantin Hartmann

- **2026-09-10:** `BrowsingService` start and data model.
- **2026-09-11:** `BrowsingService`/`ResData` continuation; `Statistics` and start of `Menu`.
- **2026-09-13:** `DeliveryService` groundwork.
- **2026-09-14:** `DeliveryDriver` and `DeliveryDesk`; closing-behaviour groundwork.
- **2026-09-16:** `DeliveryService.choose`; getters; conflict resolution.
- **2026-09-17:** detekt fixes.
- **2026-09-18:** Tests for `RestaurantParser` and `CustomerGroupSerialiser`.
- **2026-09-20:** `PantrySupplierTest` fixes; Pantry/Supplier integration tests.
- **2026-09-21:** Driver flow additions; clock-change handling; `SupplierTest` fix
  (`minDuration`); `ParsedModel` check; `acceptsCustomers` boolean addition.
- **2026-09-22:** Delivery patches; system tests for F04-F06, F08, F09.
- **2026-09-23:** Delivery-ID changes; system tests for F10-F12, F22/F23/F25-F27.

---

## Usage of Generative AI

**Ognjen Stojicic:**
*(to be filled in by Ognjen)*

**Teodor Vasilev:**
*(to be filled in by Teodor)*

**Stefan Marinkov:**
Stefan used Claude Code (Anthropic, Claude Sonnet/Opus models) in the implementation phase, for
the following tasks:
- Scanning the course forum for relevant clarifications and extracting findings applicable to
  his features and to shared simulation behaviour.
- Writing unit, integration, and system tests for his testing-block assignments (Ognjen's and
  Teodor's features) and for mutant-killing system tests.
- Debugging failing tests and unexpected simulation behaviour.
- Static analysis of the code, including reviewing code for correctness issues and detekt
  compliance.
- Manual analysis of simulation log output to verify expected behaviour and locate bugs.
- Automatically running project tooling (`./gradlew test`, `./gradlew build`, `./gradlew
  detekt*`, `./gradlew systemtestExec`, etc.) and fixing the problems these runs surfaced.

**Biborka Fancsali:**
*(to be filled in by Biborka)*

**Constantin Hartmann:**
*(to be filled in by Constantin)*

In case of option 2, add additional sentences in which you provide more details on which tools
you used for which specific tasks and to which extent.

We are aware of the potential dangers of using these tools and take full responsibility for any
code, documents and other content produced during the group phase.
