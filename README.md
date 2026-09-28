# Saar la Carte

A discrete-event restaurant simulation, written as the group project for the Software
Engineering Lab (SoPra) at Saarland University, 2026.

## What it simulates

The simulation runs a set of restaurants over a number of evenings, each divided into 24 ticks.
Every tick, each restaurant works through the same steps: seating arriving customers, taking
their orders, cooking, serving, eating, escorting people out, and collecting ratings.

Around that core loop sit the parts that make it interesting:

- **Customers** come as regulars with a fixed restaurant and a visiting rhythm, as events booked
  three evenings in advance, or as casual walk-ins. Casual groups can also order delivery.
- **A browsing service** lets customers choose a restaurant by type, rating, free seats and
  available drivers — and the ratings they leave afterwards feed back into later choices.
- **The kitchen** plans and buys ingredients before each evening, assigns dishes to cooks by
  type, and tracks best-before dates in the pantry.
- **Delivery drivers** carry complete orders out to customers and drive back, with customers
  giving up if the food takes too long.
- **Incidents** disrupt an evening: staff joining or leaving, ingredients becoming unavailable,
  recipes and packaging sizes changing.

Everything is driven by three JSON configuration files (food, restaurants, scenario), validated
against JSON schemas, and the whole run is written out as a log.

## Built with

Kotlin, Gradle, JUnit, detekt, JaCoCo.

## Running it

```
./gradlew jar
java -jar libs/selab.jar \
  --food food.json --restaurants restaurants.json --scenario scenario.json \
  --maxTicks 96 --logLevel INFO
```

`--maxTicks` is 24 per evening. `--logLevel` is `DEBUG`, `INFO` or `IMPORTANT`, and `--out <file>`
writes the log to a file instead of standard output.

## Tests

```
./gradlew build          # compiles, runs detekt and the unit tests
./gradlew systemtestExec # runs the system tests against the built jar
```

