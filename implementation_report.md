# Implementation Report

## Individual Contributions

`feature_assignments.yaml` at the repository root has been resubmitted and updated to reflect
the F04/F06 swap between Stefan Marinkov and Biborka Fancsali (see Adjustments below).

Implementation ownership below is cross-checked against `git log --follow` per source file
(top-committer per file in `src/main`), not just commit messages. Testing ownership is
cross-checked the same way against `src/test` and `src/systemtest`.

| Student | Major contributions (implementation) | Major contributions (testing) |
|---|---|---|
| Ognjen Stojicic (ogacio) | F01 Simulation (`Simulator`, `Main`, `ConfigParser`, `ConfigurationLoader`, `ParsedModel`), F14 Evening Table Reservation, F16 Seating, F18 Ordering, F19 Serving, F21 Escorting, P02 Event Seating, P03 Event Other Actions (resposible for entire services package); owns the entire `foh/visit` state machine (`Visit`, `VisitState`, `SeatedState`, `AwaitingSeatState`, `AwaitingMealState`, `EatingUpState`, `ReadyToLeaveState`, `GoneState`) and `FrontOfTheHouse`, `GlobalClock`, `ReservationBook`, `SubUnits`; substantial co-authorship (by line count) on `WaiterAssignmentService` (F17, Teodor's feature: the EVENT waiter planning, see Adjustments) and smaller changes in `DeliveryDesk` | Tested Biborka's and Constantin's features per assignment (F04, F08-F12; F07, F13, F24, F28-F30), plus F01/F06 parsing: `F01ConfigurationLoaderTest`, `F06IncidentParsingTest`, most of the `foh/visit` unit tests, `F14CancelledReservationTest`, `F21TableResetTest`, `F22RegularFailureSeriesTest`, `F24DeliveryFlowTest`, `F30EndOfEveningTest`, unit tests closing the coverage gaps in his block (`F08IngredientTest`, `F08StockEntryTest`, `F08PantryStockTest`, `F09HistoryTest`,`F11CookReleaseBatchTest`, `F11CookRoasterGuardTest`,`F24ServingHandsOverPartialOrdersTest`, `P02SeatingAndOrderingStepTest`,`P02PrepareSeatEstimateTest`, `F29DeliveryDriverEdgeTest`), plus a large share of the `src/systemtest` suite (delivery, incidents, kitchen, menu, statistics areas), of which most usefull for catching mutants are — `F14AReservedTableIsBlockedForTheWholeEvening`, `F31StaffIncidentAddsTheCookThatUnlocksTheDish`, `F32RecipeIncidentChangesTheProcuredAmount`, `F33PackagingIncidentChangesTheProcuredPackages` and `F22NothingOrderableDoesNotEndTheRegularsVisits` (RIP); and the reference-only probe system tests used to settle open specification questions — `F21ExtraPatienceAfterPartialServe` (how long a partially served group waits), `F11ACookIsBusyUntilTheTickAfterItsBatchFinishes` (a cook is free only in the tick after its batch finishes), `F31RemovingMoreCooksThanExistStopsAtZero` (a staff reduction clamps at zero) and `P05AFailedReservationRatesAtTheOpeningTick` (a failed reservation rates in the restaurant's openingTickStart, not in tick 1) |
| Teodor Vasilev (Teodor.Vasilev) | F02 Logging (`Logger` with every log message of the specification, `LogFormat`, `LogLevel`, `LogSink`); F03 Parse Ingredients & Recipes (`FoodParser`, `FoodDtos`, `Ingredient`, `Recipe`, `RecipeIngredient`, `UnitType`); F15 Table Merging & Tables (`Table`, `TableType`, `TableAssignmentService` with the reservation/seating rules 1-6 and merging/splitting); F17 FOH Staff Management (`Waiter` with tick load and current load, `WaiterAssignmentService`: permanent waiter selection rules 1-4, EVENT orders taken by the waiters who seated those customers, partial SEATING load of a failed EVENT seating); F31-F34 Incidents (`Incident`, `Incidents`: staff, recipe, packaging and unavailability changes); P04 Customer - Events (`EventCustomerGroup`) | Tested Ognjen's (F01, F14, F16, F18-F21, P03) and Constantin's (F07, F13, F24, F28-F30) features and P05. Unit tests: `SeatingServiceTest`, `OrderingServiceTest`, `ServingServiceDeliveryAndEventTest`, `EscortingServiceTest`, `RatingServiceTest`, `FrontOfTheHouseArrivalAndLeavingTest`, `FrontOfTheHouseClosingTest`, `FrontOfTheHouseDeliveryTest`, `DeliveryDeskTest`, `DeliveryDeskStateTest`, `DeliveryDriverTest`, `DeliveryDriverLoadingTest`, `BrowsingServiceTest`, `BrowsingServiceWinnerTest`, `DeliveryServiceTest`, `DeliveryServiceOrderingTest`, `DeliveryServiceDriversTest`, `CustomerRegistryTest`, `GlobalClockTest`, `ReservationBookTest`, `RestaurantTest`, `RestaurantDeliveryOrderTest`, `SimulatorDecisionTest`, `StatisticsTest`, `MenuTest`, `RestaurantDataTest`, `RatingBookUninitializedTest`, shared `testsupport/Fixtures`. Integration tests: `FohVisitIntegrationTest`, `SimulationRunIntegrationTest`, `BrowsingRatingIntegrationTest`, `DeliveryIntegrationTest`, `MenuKitchenIntegrationTest`. System tests: the `LogSkippingSystemTest` base and the first `foh/` suites (`FrontOfHouseTests`, `BrowsingAndDeliveryTests`, `ServingAndClosingTests`, `SimulationFlowTests`), `TableAndWaiterLoadTests`, `CoverageSimulationTests`, `DeliveryDriverTimingTests`, `MutantHuntTests`, and the `survivors/` package (40 tests for the six mutants no earlier run had found) |
| Stefan Marinkov (stma00002) | F05 Parse Customers (`ScenarioParser`, `CustomerGroupJsonDto`, `FoodPreferenceDto`, `ScenarioFileDto`, `CustomerGroupSerialiser`), F06 Parse Incidents (swapped in from Biborka: `IncidentSerialiser`, `IncidentJsonDto`), F22/F23 (`CustomerGroup`, `RegularCustomerGroup`, `CasualCustomerGroup`, `Customer`, `History`, `CustomerStatus`, `GroupType`), F25 (`CustomerRegistry.deciding`), F26 (`FoodPreference`), F27 (waiting/eating/give-up clock logic in the above), P05 (`ratingFor` in the `CustomerGroup` hierarchy); also `Validator` (cross-cutting config validation, used by all parsers) | Tested Ognjen's and Teodor's features per assignment: `OrderingServiceOrderedTest`, `ServingServiceTest`, `TableAssignmentServiceTest`, `WaiterAssignmentServiceTest`, `IncidentsTest`, all `logging/*Test` files, `SimulatorTest`, `RestaurantTickTest`, `TableLifecycleIntegrationTest`; wrote and registered a large share of `src/systemtest` (`SystemTestRegistration` itself, the single-rule `foh/F*` system tests, most `gvalidation/`, `gdelivery/`, `gkitchen/`, `f03/`, `incidents/` system tests), including the session's mutant-hunting tests; surfaced and fixed several bugs while testing (tick log format, delivery rating/eating lifecycle, ingredient packaging calculation, F27 patience-window off-by-one) |
| Biborka Fancsali (fancsaliborka) | F04 Parse Restaurants (swapped in from Stefan: `RestaurantParser`, `RestaurantFileDto`, `RestaurantJsonDto`, `TableDto`, `RestaurantType`), F08 Supplier, F09 Pantry (`Pantry`, `StockEntry`), F10-F12 Cooking (`Cook`, `CookRoaster`, `CookType`, `Kitchen`), P01 Pantry Best-Before/Reservation; also `Meal`, `MealStatus`, `Order`, `Restaurant` | Tested Teodor's F03 (`f03/RecipeValidTests`, `f03/RecipeInvalidTests`) and Stefan's F22/F23/F25-F27, and supported F07/F13: `IncidentSerialiserTest`, `RestaurantParserTest`, `F27DeliveryDriverTest`, `F27VisitTest`, `F25RestaurantDataTest`, `F25EventCustomerGroupTest`, `FoodPreferenceTest`, `F25BrowsingServiceTest`, and P05 `RatingServiceTest`, F15 `TableAssignmentServiceTest` , F17 `WaiterAssignmentServiceTest`, P04 `EventCustomerGroupTest`, system tests across `f03/`, `f04/`, `foh/` (preference/regular-recurrence/table-merge scenarios), `incidents/`, and `utils/ValidationSystemTest`                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                             |
| Constantin Hartmann (op23no5) | F07 Statistics, F13 Menu, F24/F28/F29 (`BrowsingService`, `DeliveryService`, `DeliveryDriver`, `DriverState`, `DeliveryDesk`), `RestaurantData` | Tested Biborka's features (F04, F08-F12, P01): `ConfigParsingIntegrationTest`, `F09PlanningStockBoundaryTest`, `F09RegularHistoryPlanningTest`, `F10F12ReservationAndServingTest`, `F11OverlappingBatchesTest`, `F23RatingDecisionIntegrationTest`, `F26PreferenceOrderingTest`, `F27ServingWaitingBoundaryTest`, `PantrySupplierTest`, `F10QueueBoundaryTest`, `F11StaffBoundaryTest`, `F12DurationBoundaryTest`, `F25DecisionBoundaryTest`; system tests for `f04/`, `f05/`, `f06/` scope-and-boundary areas, kitchen/supplier boundary tests |

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
- **P05, rating logic shared with Ognjen:** P05 (customer rating) was assigned to Stefan Marinkov,
  but he and Ognjen Stojicic split the work along a natural line in the feature: Ognjen wrote
  `RatingService`, the FOH side that decides when a rating happens during the tick and feeds it
  into the restaurant's statistics, while Stefan wrote `ratingFor` and the `Experience`-to-`Rating`
  mapping in the `CustomerGroup` hierarchy (`RegularCustomerGroup`, `CasualCustomerGroup`,
  `EventCustomerGroup`) — the customer's own side of deciding what to rate and why.
  `feature_assignments.yaml` still lists Stefan as sole implementer since this was an informal
  split within one feature, not a reassignment.

---

## Detailed Timeline

### Ognjen Stojicic

- **2026-09-10:** First version of the simulation skeleton: `ConfigParser` and
  `ConfigurationLoader` as the configuration entry point, `ParsedModel`, `GlobalClock`, the
  `Simulator` loop, and the first version of the ratings model (`RatingBook`, `RatingScore`,
  `Rating`, `Experience`).
- **2026-09-11:** First version of `FrontOfTheHouse` and of the visit state machine (`Visit`,
  `VisitState`).
- **2026-09-14:** `Visit` and all of its states finished — `AwaitingSeatState`, `SeatedState`,
  `AwaitingMealState`, `EatingUpState`, `ReadyToLeaveState`, `GoneState` — together with the
  whole serving phase and `ReservationBook` for the evening table reservations.
- **2026-09-15:** Structure and import cleanup across the packages added so far.
- **2026-09-16:** Eating and done-eating handling in `DiningService`, parser corrections, the
  third version of the front-of-house services (`SeatingService`, `OrderingService`,
  `ServingService`, `EscortingService`, `RatingService` behind `FohServices`), and their wiring
  into the simulator through `SubUnits`.
- **2026-09-17:** First green build. Cleanup for detekt, and the first unit tests for the
  services and the visit state machine.
- **2026-09-18:** Started on the testing block agreed in the implementation plan, testing
  Biborka's and Constantin's features. Fixed the delivery-service bugs that testing surfaced and
  added further unit and integration tests.
- **2026-09-19:** Worked through the forum clarifications and adjusted the features affected by
  them.
- **2026-09-20:** Registered a large batch of new system tests and repaired the ones the first
  server runs rejected.
- **2026-09-21:** System tests for the front of house and for incidents, and the first tests
  written specifically to catch mutants.
- **2026-09-22:** Fixed the delivery eating clock, which made delivery groups finish eating
  instantly from the second evening on, and continued with mutant-targeted tests.
- **2026-09-23:** Serving fixes; system tests targeting multiple mutants and previously
  failing tests.
- **2026-09-24** Targeting last couple of mutants and writing some unit tests for the coverage.

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

No commits on the weekends (2026-09-12/13, 2026-09-19/20) — that work just shows up under the
next weekday instead of being listed separately.

- **2026-09-10:** Repo/environment setup, no feature code yet.
- **2026-09-11:** F05/F06 scenario parser (`ScenarioParser`, `CustomerGroupSerialiser`,
  `CustomerGroupJsonDto`, `ScenarioFileDto`, `FoodPreferenceDto`, `IncidentSerialiser`,
  `IncidentJsonDto`); F22/F23 (`CustomerGroup`, `RegularCustomerGroup`, `CasualCustomerGroup`,
  `History`); F26 (`FoodPreference`); F27 (waiting/eating/give-up clocks); P04 event customer
  group; shared base classes/enums.
- **2026-09-14:** Unit tests for F02 (`logging/*Test`) and F03 (`FoodParserTest`); F22/F25 fixes;
  adapted his code to teammates' interface changes.
- **2026-09-16:** `Validator` (config validation shared by all three parsers); fixes to F05,
  F22/F23/F25/F26, F27; merge-conflict resolution (AI-assisted).
- **2026-09-17:** Testing-block work on Ognjen's and Teodor's features: `SimulatorTest` (F01),
  `TableAssignmentServiceTest` (F15), `WaiterAssignmentServiceTest` (F17),
  `OrderingServiceOrderedTest` (F18), `ServingServiceTest` (F19), `DeliveryDriverTest` (F20),
  `IncidentsTest` (F31-F34), `EventCustomerGroupTest` (P04), `TableLifecycleIntegrationTest`
  (F15/F16/F17/F19/F21); fixed stale `Order`/`RestaurantData` constructor references, switched to
  getters over direct field access, cleared the detekt fallout; more merge conflicts.
- **2026-09-18:** Fixed cooking duration/cooked-meal return, ingredient packaging calculation,
  delivery rating/eating lifecycle, swapped tick/evening log args; wired the delivery service
  into the simulator; wrote cooked-meals statistics.
- **2026-09-21:** Fixed a delivery-order tick-timing bug (forum); unregistered a few system tests
  invalid against the reference.
- **2026-09-22:** Added system tests killing 16 previously-uncaught mutants; fixed an F27
  patience-window off-by-one (forum 335/340); more fixture tests for delivery/kitchen/validation
  gaps.
- **2026-09-23:** More mutant-hunting system tests; fixes for event favourite validation (forum
  352), last-3-ticks retry refusal and delivery-ordering interleaving (forum 345), EVENT
  order-taking (forum 266); coverage tests; unregistered four more reference-invalid tests; filled
  in this implementation report and resubmitted `feature_assignments.yaml`.
- **2026-09-24:** Coordinated with Ognjen and Teodor on the remaining uncaught mutants to avoid
  duplicating effort; more system tests.

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

- **2026-09-10:** Started `BrowsingService` and the restaurant-selection data model; added early
  `RestaurantData`, restaurant/table types, `RatingBook`, and `GlobalClock` groundwork.
- **2026-09-11:** Continued `BrowsingService` and `RestaurantData`; introduced `Statistics` and
  `Menu`; adapted parsers, kitchen, and shared model classes during the package restructuring and
  resolved the resulting integration conflicts.
- **2026-09-13:** Added `DriverState` and the initial `DeliveryService`; moved `BrowsingService`
  into the simulation package and connected the first delivery-selection flow.
- **2026-09-14:** Implemented and iterated on `DeliveryDriver` and `DeliveryDesk`; integrated
  delivery state with orders, customer groups, the simulator, statistics, and rating data; began
  the restaurant-closing behaviour.
- **2026-09-16:** Implemented `DeliveryService.choose`, added the delivery-desk accessors, and
  adapted configuration, menu, order, customer, restaurant, and simulator code to the combined
  delivery flow; resolved merge conflicts with the evolving FOH implementation.
- **2026-09-17:** Fixed Detekt findings across delivery, browsing, menu, restaurant data, and
  statistics; reconciled the delivery code with changes in restaurant parsing, reservations,
  restaurant lifecycle, and supplier code.
- **2026-09-18:** Added unit tests for `RestaurantParser` and `CustomerGroupSerialiser` and updated
  delivery integration for the changed constructor/API shape.
- **2026-09-20:** Added `IncidentSerialiserTest`, `ConfigParsingIntegrationTest`,
  `PantrySupplierTest`, `PantryTest`, and `SupplierTest`; followed up with the required fixture and
  argument-type corrections.
- **2026-09-21:** Extended the driver flow and its ordering integration; aligned delivery timing
  with `GlobalClock`; revised `DeliveryDriver`, `Kitchen`, and incident parsing; corrected the
  `SupplierTest` duration boundary; added the `ParsedModel` uniqueness check and restaurant
  acceptance state used by browsing.
- **2026-09-22:** Added and registered boundary-focused system tests and fixtures for F04-F06,
  F08, and F09. Integrated the delivery lifecycle patches across `DeliveryDesk`, `DeliveryDriver`,
  `DriverState`, FOH serving/dining, customer state, and `DeliveryService`, including a delivery
  regression test.
- **2026-09-23:** Added unit, integration, and registered system tests for F10-F12 and
  F22/F23/F25-F27, including cooking boundaries, customer failure series, rating decisions, food
  preferences, and waiting behaviour; added the associated configurations and cleaned test names.
  Also fixed the cross-evening delivery-driver ID collision.
- **2026-09-24:** Focused F09 unit and integration tests for partial-stock package boundaries,
  aggregated ingredient demand, menu changes, and procurement based on the last three recorded
  visits; verified the complete build, Detekt checks, and JaCoCo coverage.

---

## Usage of Generative AI

**Ognjen Stojicic:**
Ognjen used Claude Code (Anthropic, Claude Opus model) in the implementation phase, for the following tasks:
- Writing unit, integration and system tests for his testing assignments and for uncaught mutants.
- Debugging and code correctenss

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
- Resolving merge conflicts when pulling teammates' pushes into his own in-progress work
  (2026-09-16, 17, 18, 21, 22 and 23), after teammates had pushed interface changes to shared
  code (constructors, field access, customer-group shapes) that his own work touched. Every
  resolved conflict was reviewed before committing.

**Biborka Fancsali:**
I used Claude (Sonnet 5) in the implementation for:
- checking my code's correctness, and debugging it
- writing unit, integration and system tests

**Constantin Hartmann:**
ChatGPT was used for:
- checking, debugging, and proposing code changes
- generating unit, integration, and system tests using Codex

We are aware of the potential dangers of using these tools and take full responsibility for any
code, documents and other content produced during the group phase.
