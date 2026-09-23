# Implementation Report

## Individual Contributions

`feature_assignments.yaml` at the repository root has been resubmitted and updated to reflect
the F04/F06 swap between Stefan Marinkov and Biborka Fancsali (see Adjustments below).

Implementation ownership below is cross-checked against `git log --follow` per source file
(top-committer per file in `src/main`), not just commit messages. Testing ownership is
cross-checked the same way against `src/test` and `src/systemtest`.

| Student | Major contributions (implementation) | Major contributions (testing) |
|---|---|---|
| Ognjen Stojicic (ogacio) | F01 Simulation (`Simulator`, `Main`, `ConfigParser`, `ConfigurationLoader`, `ParsedModel`), F14 Evening Table Reservation, F16 Seating, F18 Ordering, F19 Serving, F21 Escorting, P02 Event Seating, P03 Event Other Actions; owns the entire `foh/visit` state machine (`Visit`, `VisitState`, `SeatedState`, `AwaitingSeatState`, `AwaitingMealState`, `EatingUpState`, `ReadyToLeaveState`, `GoneState`) and `FrontOfTheHouse`, `GlobalClock`, `ReservationBook`, `SubUnits`; substantial co-authorship (by line count) on `WaiterAssignmentService` (F17, Teodor's feature: the EVENT waiter planning, see Adjustments) and smaller changes in `DeliveryDesk` | Tested Biborka's and Constantin's features per assignment (F04, F08-F12; F07, F13, F24, F28-F30), plus F01/F06 parsing: `F01ConfigurationLoaderTest`, `F06IncidentParsingTest`, most of the `foh/visit` unit tests, `F14CancelledReservationTest`, `F21TableResetTest`, `F22RegularFailureSeriesTest`, `F24DeliveryFlowTest`, `F30EndOfEveningTest`, plus a large share of the `src/systemtest` suite (delivery, incidents, kitchen, menu, statistics areas) |
| Teodor Vasilev (Teodor.Vasilev) | F02 Logging (`Logger` with every log message of the specification, `LogFormat`, `LogLevel`, `LogSink`); F03 Parse Ingredients & Recipes (`FoodParser`, `FoodDtos`, `Ingredient`, `Recipe`, `RecipeIngredient`, `UnitType`); F15 Table Merging & Tables (`Table`, `TableType`, `TableAssignmentService` with the reservation/seating rules 1-6 and merging/splitting); F17 FOH Staff Management (`Waiter` with tick load and current load, `WaiterAssignmentService`: permanent waiter selection rules 1-4, EVENT orders taken by the waiters who seated those customers, partial SEATING load of a failed EVENT seating); F31-F34 Incidents (`Incident`, `Incidents`: staff, recipe, packaging and unavailability changes); P04 Customer - Events (`EventCustomerGroup`) | Tested Ognjen's (F01, F14, F16, F18-F21, P03) and Constantin's (F07, F13, F24, F28-F30) features and P05. Unit tests: `SeatingServiceTest`, `OrderingServiceTest`, `ServingServiceDeliveryAndEventTest`, `EscortingServiceTest`, `RatingServiceTest`, `FrontOfTheHouseArrivalAndLeavingTest`, `FrontOfTheHouseClosingTest`, `FrontOfTheHouseDeliveryTest`, `DeliveryDeskTest`, `DeliveryDeskStateTest`, `DeliveryDriverTest`, `DeliveryDriverLoadingTest`, `BrowsingServiceTest`, `BrowsingServiceWinnerTest`, `DeliveryServiceTest`, `DeliveryServiceOrderingTest`, `DeliveryServiceDriversTest`, `CustomerRegistryTest`, `GlobalClockTest`, `ReservationBookTest`, `RestaurantTest`, `RestaurantDeliveryOrderTest`, `SimulatorDecisionTest`, `StatisticsTest`, `MenuTest`, `RestaurantDataTest`, `RatingBookUninitializedTest`, shared `testsupport/Fixtures`. Integration tests: `FohVisitIntegrationTest`, `SimulationRunIntegrationTest`, `BrowsingRatingIntegrationTest`, `DeliveryIntegrationTest`, `MenuKitchenIntegrationTest`. System tests: the `LogSkippingSystemTest` base and the first `foh/` suites (`FrontOfHouseTests`, `BrowsingAndDeliveryTests`, `ServingAndClosingTests`, `SimulationFlowTests`), `TableAndWaiterLoadTests`, `CoverageSimulationTests`, `DeliveryDriverTimingTests`, `MutantHuntTests`, and the `survivors/` package (40 tests for the six mutants no earlier run had found) |
| Stefan Marinkov (stma00002) | F05 Parse Customers (`ScenarioParser`, `CustomerGroupJsonDto`, `FoodPreferenceDto`, `ScenarioFileDto`, `CustomerGroupSerialiser`), F06 Parse Incidents (swapped in from Biborka: `IncidentSerialiser`, `IncidentJsonDto`), F22/F23 (`CustomerGroup`, `RegularCustomerGroup`, `CasualCustomerGroup`, `Customer`, `History`, `CustomerStatus`, `GroupType`), F25 (`CustomerRegistry.deciding`), F26 (`FoodPreference`), F27 (waiting/eating/give-up clock logic in the above), P05 (`ratingFor` in the `CustomerGroup` hierarchy); also `Validator` (cross-cutting config validation, used by all parsers) | Tested Ognjen's and Teodor's features per assignment: `OrderingServiceOrderedTest`, `ServingServiceTest`, `TableAssignmentServiceTest`, `WaiterAssignmentServiceTest`, `IncidentsTest`, all `logging/*Test` files, `SimulatorTest`, `RestaurantTickTest`, `TableLifecycleIntegrationTest`; wrote and registered a large share of `src/systemtest` (`SystemTestRegistration` itself, the single-rule `foh/F*` system tests, most `gvalidation/`, `gdelivery/`, `gkitchen/`, `f03/`, `incidents/` system tests), including the session's mutant-hunting tests; surfaced and fixed several bugs while testing (tick log format, delivery rating/eating lifecycle, ingredient packaging calculation, F27 patience-window off-by-one) |
| Biborka Fancsali (fancsaliborka) | F04 Parse Restaurants (swapped in from Stefan: `RestaurantParser`, `RestaurantFileDto`, `RestaurantJsonDto`, `TableDto`, `RestaurantType`), F08 Supplier, F09 Pantry (`Pantry`, `StockEntry`), F10-F12 Cooking (`Cook`, `CookRoaster`, `CookType`, `Kitchen`), P01 Pantry Best-Before/Reservation; also `Meal`, `MealStatus`, `Order`, `Restaurant` | Tested Teodor's F03 (`f03/RecipeValidTests`, `f03/RecipeInvalidTests`) and Stefan's F22/F23/F25-F27, and supported F07/F13: `IncidentSerialiserTest`, `RestaurantParserTest`, `F27DeliveryDriverTest`, `F27VisitTest`, `F25RestaurantDataTest`, `F25EventCustomerGroupTest`, `FoodPreferenceTest`, `F25BrowsingServiceTest`; system tests across `f03/`, `f04/`, `foh/` (preference/regular-recurrence/table-merge scenarios), `incidents/`, and `utils/ValidationSystemTest` |
| Constantin Hartmann (op23no5) | F07 Statistics, F13 Menu, F24/F28/F29 (`BrowsingService`, `DeliveryService`, `DeliveryDriver`, `DriverState`, `DeliveryDesk`), `RestaurantData` | Tested Biborka's features (F04, F08-F12, P01): `ConfigParsingIntegrationTest`, `F10F12ReservationAndServingTest`, `F11OverlappingBatchesTest`, `F23RatingDecisionIntegrationTest`, `F26PreferenceOrderingTest`, `F27ServingWaitingBoundaryTest`, `PantrySupplierTest`, `F10QueueBoundaryTest`, `F11StaffBoundaryTest`, `F12DurationBoundaryTest`, `F25DecisionBoundaryTest`; system tests for `f04/`, `f05/`, `f06/` scope-and-boundary areas, kitchen/supplier boundary tests |

Only major contributions are listed; routine bugfixes, merge-conflict resolutions, and detekt
cleanups are omitted per the reporting guidelines.

---

## Adjustments from the Implementation Plan

- **F04 / F06 swap (from the start of implementation):** In the implementation plan, Stefan
  Marinkov was assigned `F04` (`RestaurantParser`) and Biborka Fancsali was assigned `F06`
  (incident parsing). The two swapped before either feature was started: Biborka's first commit
  (2026-09-10) was the restaurant parser, Stefan's first feature commit (2026-09-11) was the
  scenario parser including incidents. Biborka's kitchen, pantry and supplier model depends on the
  restaurant data, while incident parsing overlaps with Stefan's scenario parsing (F05), since
  incidents are part of the scenario file. Testing moved with the blocks, so no one tests their own
  feature: `F04` is now tested by Constantin Hartmann and Ognjen Stojicic, `F06` by Biborka
  Fancsali and Constantin Hartmann. `feature_assignments.yaml` reflects the swap.
- **F17, EVENT waiter handling shared with Ognjen:** The plan gave all of `F17` to Teodor Vasilev.
  The EVENT part of `WaiterAssignmentService` (planning which waiters seat, serve and escort an
  EVENT group, and in which order) was written by Ognjen Stojicic on 2026-09-16 while wiring the
  FOH services, because P02/P03 call it from his seating, serving and escorting services. Teodor
  kept the permanent waiter selection and the load bookkeeping and later extended the EVENT part
  (2026-09-22: EVENT orders are taken by the waiters who seated those customers, forum 266;
  2026-09-23: a failed EVENT seating keeps the partial SEATING load, forum 156).
- **P03, EVENT ordering/serving/escorting:** Planned for Constantin Hartmann. The EVENT branches of
  ordering, serving and escorting live in the same service classes as F18, F19 and F21, so they
  were implemented there by Ognjen Stojicic. `feature_assignments.yaml` now lists Ognjen as
  implementer of `P03`; following his testing block it is tested by Teodor Vasilev and Stefan
  Marinkov, who both wrote the EVENT ordering/serving tests.
- **F20, driver side:** The plan listed `DeliveryDesk`, `DeliveryDriver` and `DriverState` under
  Ognjen Stojicic. They were implemented by Constantin Hartmann together with the delivery service
  (F24/F29), which drives them; Ognjen implemented the FOH hand-over of meals to the drivers in
  the serving step.
- **Shared types instead of stand-in interfaces:** On the first day Teodor Vasilev briefly used
  stand-in interfaces for classes owned by others. They were removed the same day (2026-09-10) once
  the real classes existed, and shared enums were split into one file per type in the shared
  package so that all members import the same declarations.
- **Teodor's testing schedule:** The plan spread his testing over days 4-18 in the order the
  features would land. In practice the tested features landed together, so on 2026-09-16 he wrote
  the unit, integration and first system tests for all of Ognjen's and Constantin's features in
  one batch. From 2026-09-18 the focus moved to system tests driven by the server results
  (failing reference tests, uncaught mutants). Bugs found in teammates' code were handed to the
  owners as proposed patches instead of being committed by the tester, so each fix stays with its
  implementer.

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

- **2026-09-10:** First complete version of all his planned features: F02 logging package (`Logger` with
  every log message of the specification, `LogFormat`, `LogLevel`, `LogSink`) and all log
  statements routed through `Logger`; F03 shared food model (one file per class) and the food
  configuration stage (`FoodParser`, `FoodDtos`); F15/F17 tables and waitstaff (`Table`,
  `TableAssignmentService`, `Waiter`, `WaiterAssignmentService`, `ActionType` moved into
  `Waiter`); F31-F34 incident package; P04 event customer groups. Removed the day-1 stand-in
  interfaces and split the shared enums into one file per type.
- **2026-09-14:** Import fixes after the package restructuring.
- **2026-09-16:** Updated F03, F31-F34 and P04 to the integrated model (`FoodParser`, `Incidents`,
  `Recipe`, `RecipeIngredient`, `EventCustomerGroup`). First large testing batch as tester of
  Ognjen's and Constantin's features (72 files): unit tests for seating, ordering, escorting,
  ratings, delivery desk/driver, browsing, delivery service, customer registry, clock,
  reservation book, statistics, menu and restaurant data; integration tests
  (`FohVisitIntegrationTest`, `BrowsingRatingIntegrationTest`, `DeliveryIntegrationTest`,
  `MenuKitchenIntegrationTest`); the `LogSkippingSystemTest` base and the first system-test suites
  with fixtures for F01, F07, F13, F14, F16, F18-F21, F28, F30, P03 and P05.
- **2026-09-17:** Small fixes in F03, F15 and F17 (`FoodDtos`, `TableAssignmentService`, `Waiter`);
  getter-based access instead of direct field access.
- **2026-09-18:** `Logger` and `SimulatorTest` aligned with the new tick log format; fixes to the
  waiter's action consumption, `FoodParser` and the recipe duration in ticks; tests for closing,
  delivery desk/driver, delivery ordering, `Restaurant` and a full simulation run
  (`SimulationRunIntegrationTest`); branch-coverage tests for F01, F14, F18-F20, F24, F29, F30.
- **2026-09-21:** `FoodParser`: a basic and a non-basic dish may share a name within one
  restaurant (a stricter per-type check was reverted after it broke valid configurations); P04:
  an event's favourite dish is chosen by the chosen restaurant's type; `DeliveryDriverTest` fix.
- **2026-09-22:** F17: EVENT orders are taken by the waiters who seated those customers
  (forum 266). System tests for the F15/F17/F32 mutants, for the least covered simulation
  features (`CoverageSimulationTests`) and for the "Stonks" mutant (waiter rule 3, current load).
  Unregistered three delivery tests that failed against the reference, which had blocked the
  mutant run. Wrote a list of forum-based fix proposals for teammates' code (delivery eating
  clock, patience window, retry in the last three ticks, EVENT order-taking, `otherSeats`,
  partly loaded drivers); several were applied by their owners on 2026-09-22/23.
- **2026-09-23:** Unit tests for the F24, F28, F29 and P05 coverage gaps. F17: a failed EVENT
  seating keeps the partial SEATING load (forums 156 and 362); the matching call in
  `SeatingService` was handed to Ognjen as a proposed change. System tests for F20/F24/F29
  (driving distance, kitchen status, give-up, returning driver) and for the six mutants still
  uncaught on `d4ef9e0`. Analysed why six mutants had survived every run and wrote 40 system
  tests for them (`survivors/`), each checked line by line against our jar and registered as
  reference-only probes first; this also exposed the missing `SeatingService` call above.

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
Teodor used Claude Code (Anthropic, Claude Opus models) in the implementation phase, for the
following tasks:
- Reading the course forum and the specification adjustments, including silently edited posts,
  and listing the changes that affect his features and the features he tests.
- Writing unit, integration and system tests for his testing assignments (Ognjen's and
  Constantin's features, P05) and for uncaught mutants, including generating the JSON fixtures
  and the expected log traces, which he checked against the specification and the forum.
- Analysing the server results (reference, component, full and mutant runs) to find failing or
  non-catching tests and to plan new ones.
- Debugging failing tests and simulation behaviour by reading the log output of our jar.
- Drafting fixes for his own features and fix proposals for teammates' code, which the owners
  reviewed and applied themselves.
- Running the project tooling (`./gradlew build`, `detekt*`, `test`, `systemtestExec`) and fixing
  the problems these runs surfaced.
He reviewed every change before committing it and is responsible for all code and tests he
committed.

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
