# Currency Exchange Analyzer

A Java command-line application for analysing real-time foreign exchange rates, detecting currency arbitrage opportunities, and finding the best conversion path between currencies.

The project applies graph algorithms to currency exchange data fetched from a live exchange-rate API.

## Features

### Real-Time Exchange Rates

The application retrieves current exchange-rate data from ExchangeRate-API.

USD is used as the base currency and the application calculates cross-rates between all supported currencies.

Current currencies:

* USD
* EUR
* JPY
* GBP
* AUD
* CAD
* CHF
* CNY
* NZD
* SGD

## Arbitrage Detection

The application searches for currency arbitrage opportunities.

An arbitrage opportunity exists when a sequence of currency exchanges returns more money than the starting amount.

For example:

```text
USD → EUR → GBP → USD
```

If the product of the exchange rates is greater than `1`, the cycle represents a potential arbitrage opportunity.

The application:

* Converts exchange rates using negative logarithms
* Builds a weighted currency graph
* Uses the Bellman-Ford algorithm
* Detects negative-weight cycles
* Reconstructs the arbitrage path
* Calculates the potential percentage profit

## Best Conversion Path

Users can select a source and target currency.

The application then searches for the conversion path with the highest resulting exchange rate.

For example:

```text
NZD → USD
```

The best path might be:

```text
NZD → AUD → USD
```

rather than converting directly from NZD to USD.

The program compares the optimal path against the direct exchange rate and reports any improvement.

## How It Works

Currencies are represented as vertices in a directed graph.

Exchange rates represent edges between currencies.

```text
USD ─────→ EUR
 ↑          │
 │          ↓
GBP ←───── AUD
```

Each exchange rate `r` is transformed using:

```text
weight = -log(r)
```

This transforms multiplication of exchange rates into addition of graph weights.

An arbitrage opportunity occurs when:

```text
r1 × r2 × r3 > 1
```

After applying the logarithmic transformation, this becomes:

```text
-log(r1) + -log(r2) + -log(r3) < 0
```

Arbitrage detection therefore becomes a negative-cycle detection problem.

Bellman-Ford is used because it supports graphs containing negative edge weights and detects negative cycles.

## Algorithms

### Bellman-Ford

Bellman-Ford is used for two main tasks:

1. Detecting arbitrage cycles
2. Finding the highest-value conversion path

The algorithm repeatedly relaxes every edge in the currency graph.

If another relaxation remains possible after `V - 1` iterations, the graph contains a negative cycle.

In this application, a negative cycle corresponds to an arbitrage opportunity.

### Negative Log Transformation

Currency conversion normally involves multiplying exchange rates.

Graph shortest-path algorithms work with addition.

The application converts each rate using:

```java
-Math.log(exchangeRate)
```

This lets the program treat the highest-product exchange path as a shortest-path problem.

## Exchange Rate Matrix

The program generates a complete exchange-rate matrix.

Example:

```text
FROM\TO  USD         EUR         GBP         NZD
USD      1.000000    0.850000    0.730000    1.700000
EUR      1.176471    1.000000    0.858824    2.000000
GBP      1.369863    1.164384    1.000000    2.328767
NZD      0.588235    0.500000    0.429412    1.000000
```

Each cell represents the rate required to convert from the row currency to the column currency.

## Cross-Rate Calculation

The application requests USD-based exchange rates from the API.

Cross-rates are then calculated using:

```te
```
