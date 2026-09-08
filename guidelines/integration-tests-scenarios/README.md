# Integration test scenarios

This catalog defines stable, namespace-prefixed Given/When/Then scenarios for
SDK conformance. It specifies externally observable behavior without prescribing
a programming language, SDK implementation, test framework, or assertion
library. Each SDK may map these scenarios to its own test infrastructure and
report results by scenario ID. Unless stated otherwise, each scenario assumes a
Solo network and a funded operator account.

This catalog is not exhaustive. It was extracted from the Hedera Java SDK on
2026-08-24.

## Scenario files

Scenarios are separated by the public capability being tested. Each file is
independently consumable, while scenario IDs remain unique across the catalog.

| Tested capability | Required | Deferred | Total |
| --- | ---: | ---: | ---: |
| [Clients / Core](clients-core.md) | 12 | 0 | 12 |
| [Queries / Accounts](queries-accounts.md) | 18 | 3 | 21 |
| [Queries / Contracts](queries-contracts.md) | 230 | 3 | 233 |
| [Queries / Files](queries-files.md) | 10 | 2 | 12 |
| [Queries / Network](queries-network.md) | 1 | 0 | 1 |
| [Queries / Nodes](queries-nodes.md) | 1 | 0 | 1 |
| [Queries / Tokens](queries-tokens.md) | 10 | 2 | 12 |
| [Queries / Topics](queries-topics.md) | 4 | 4 | 8 |
| [Queries / Transactions](queries-transactions.md) | 7 | 16 | 23 |
| [Transactions / Accounts](transactions-accounts.md) | 36 | 13 | 49 |
| [Transactions / Contracts](transactions-contracts.md) | 31 | 13 | 44 |
| [Transactions / Core](transactions-core.md) | 18 | 11 | 29 |
| [Transactions / Files](transactions-files.md) | 13 | 0 | 13 |
| [Transactions / Hooks](transactions-hooks.md) | 0 | 4 | 4 |
| [Transactions / Nodes](transactions-nodes.md) | 29 | 2 | 31 |
| [Transactions / Schedules](transactions-schedules.md) | 7 | 10 | 17 |
| [Transactions / System](transactions-system.md) | 3 | 1 | 4 |
| [Transactions / Tokens](transactions-tokens.md) | 180 | 2 | 182 |
| [Transactions / Topics](transactions-topics.md) | 19 | 0 | 19 |
| [Transactions / Transfers](transactions-transfers.md) | 0 | 6 | 6 |
| **Total** | **629** | **92** | **721** |

## Conformance notes

- **Required** means a conforming SDK must implement and execute the scenario
  when the stated environment capabilities are available.
- **Deferred** means the scenario remains part of the catalog but is temporarily
  excluded from the active conformance set for the stated reason.
- A scenario is satisfied only when its Given/When/Then behavior is exercised
  against a compatible network and the expected observable outcome is verified.
- Implementations may use any programming language, test framework, fixture
  model, or assertion library.
