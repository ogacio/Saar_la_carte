# Handoff: F09 (Cooking — Order Queue planning) unit coverage

**From:** Stefan, checking co-tester contribution balance (Constantin + Ognjen co-test F09; the
target is roughly half the work each, not "some coverage exists").
**Why this exists:** `F09KitchenPlanEveningTest.kt` is the only test file for `Kitchen.planEvening`
and it is **100% Ognjen's by line-blame** (182 lines, 0 from Constantin). This isn't a case of
"the feature has no coverage" — it's well covered — but Constantin has zero share of it.

## What's already there (don't duplicate)

Ognjen's 5 tests: pantry-already-full needs nothing, 10-vs-11-seats casual estimate rounding,
EVENT groups booked for tonight raise the expectation, expired stock is cleaned before the need is
calculated, every menu dish is planned for.

## The real gap found

None of Ognjen's tests exercise:
- **Partial stock**: the pantry has *some* of an ingredient already, not zero and not enough —
  `planEvening`'s `needed[ingredient] = concreteAmount - inPantry` subtraction is only exercised at
  the two extremes (0% covered, 100%+ covered), never a real partial gap.
- **Multiple regulars with overlapping recipe demand**: `getExpectedRecipes` sums `expectedDishes`
  across every regular in the list into one map — nothing currently proves two regulars wanting the
  *same* dish add together correctly rather than one overwriting the other.
- **The buy-only-the-gap contract end-to-end through `resupply`**: `Ingredient.packagesFor` rounds
  the deficit up to whole packages before buying — worth confirming `planEvening` buys exactly
  `packagesFor(deficit) * packagingVolume` on top of what's already there, not the full requirement
  a second time.

## What's here

`KitchenPlanEveningPartialStockUnitTestDraft.kt` — same style/helpers as the existing
`F09KitchenPlanEveningTest.kt` (same `kitchen()`/`dish()`/`rice()` helper shapes, so it reads as a
natural continuation of that file rather than a new pattern). Three tests:

1. `planEveningBuysOnlyTheMissingAmountWhenPantryIsPartiallyStocked` — pantry starts with a partial
   amount (20g of a 50g-per-portion dish), asserts the final total is the existing stock plus
   exactly the packaged deficit, not the full requirement bought again.
2. `aRegularsRecordedVisitHistoryRaisesTheNextEveningsExpectation` — the gap
   `F09KitchenPlanEveningTest.kt`'s own trailing comment flags explicitly ("NOT COVERED HERE...
   better placed in an integration test"): a REGULAR group's `expectedDishes` reads
   `history().getLastThree()`, built via `recordVisit(evening, order)`. This test records one real
   visit via a constructed `Order`/`Meal`, then confirms `planEvening` buys for that dish based on
   the group's actual history rather than the group being invisible to the estimate.

**Both tests compiled and run against `testing` HEAD `f28ba0c` — pass** (the second one needed a
fix on the first attempt: the expected amount is rounded up to a whole package, not the raw
50g portion — `packagesFor(50)` with a 100g packaging volume buys 100g).

## Not done for you

- Doesn't touch the EVENT-booking or expired-stock paths — those are Ognjen's and already solid.
- Doesn't attempt "two regulars wanting the same dish sum correctly" — that turned out to be a
  smaller, less distinctive gap than the visit-history one once the file's own comment was found;
  left out to avoid diluting effort across too many thin tests.
