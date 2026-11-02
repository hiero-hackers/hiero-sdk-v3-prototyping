# Mirror Node REST API: query conditions that behave unexpectedly

**To:** the Hiero Mirror Node team
**From:** the Hiero SDK V3 project (typed query conditions for the Mirror Node client)
**Date:** 2026-11-02

## Summary

We are designing typed query conditions (`account.id=gte:0.0.5`, `timestamp=lt:...`) for the Mirror Node client of the
Hiero SDK V3. To learn what the server really does, we sent about 5,000 read-only requests to the public **mainnet**
and **testnet** Mirror Nodes (both **v0.165.1**, 2026-11-02). Every query parameter with conditions got 23
combinations of operators plus boundary values. The conspicuous results were checked by hand and traced in the source
at tag [`v0.165.1`](https://github.com/hiero-ledger/hiero-mirror-node/tree/v0.165.1) (`6a7a3f5`).

For each item below we would like to know:

1. Is it a bug that will be fixed, or is it intended?
2. Should we open a GitHub issue for it?

Items 1-4 look like bugs, items 5-8 are questions about the intended semantics, item 9 lists differences between
the OpenAPI definition and the server, and item 10 is a question about the hook endpoints.

## Likely bugs

### 1. Airdrops: only one of several `eq` values is applied, `ne` is ignored

**Endpoints:** `/api/v1/accounts/{id}/airdrops/outstanding` (`receiver.id`, `token.id`, `serialnumber`) and
`/api/v1/accounts/{id}/airdrops/pending` (`sender.id`, `token.id`, `serialnumber`).

**Observed** (mainnet, sender `0.0.10086013`, whose outstanding airdrops go to both receivers below):

| Request (`/api/v1/accounts/0.0.10086013/airdrops/outstanding?...`) | Result |
|---|---|
| `receiver.id=eq:0.0.1081216` | 1 airdrop, to `0.0.1081216` |
| `receiver.id=eq:0.0.1081216&receiver.id=eq:0.0.1142` | 1 airdrop, to `0.0.1142` only |
| `receiver.id=eq:0.0.1142&receiver.id=eq:0.0.1081216` | the same: to `0.0.1142` only |
| `receiver.id=ne:0.0.1081216` | 200; the airdrop to `0.0.1081216` is still included |

The order of the values in the URL does not matter: in every case we tried, the lexicographically last value was
the one applied. This suggests that something in front of the service normalizes the query string. The other values
are dropped without an error. `ne` returns 200 and has no effect.

**Cause:** [`TokenAirdropsController.processRequest`](https://github.com/hiero-ledger/hiero-mirror-node/blob/v0.165.1/rest-java/src/main/java/org/hiero/mirror/restjava/controller/TokenAirdropsController.java#L89)
builds a `Bound` without validation.
[`Bound`](https://github.com/hiero-ledger/hiero-mirror-node/blob/v0.165.1/rest-java/src/main/java/org/hiero/mirror/restjava/service/Bound.java#L46)
overwrites `lower` for every `eq`/`gt`/`gte` and only counts `ne`.
[`NftAllowanceServiceImpl`](https://github.com/hiero-ledger/hiero-mirror-node/blob/v0.165.1/rest-java/src/main/java/org/hiero/mirror/restjava/service/NftAllowanceServiceImpl.java#L57)
uses the same class but calls `verifyUnsupported(NE)`, `verifySingleOccurrence()` and `verifyEqualOrRange()`.

**Expected:** a 400, as `/accounts/{id}/allowances/nfts` returns, or IN semantics for repeated `eq`. Either way, `ne`
should not be accepted silently. The OpenAPI pattern of these parameters allows `ne` (except for `serialnumber`).

### 2. `node.id` and `registerednode.id`: `ne` is accepted and ignored

**Endpoints:** `/api/v1/network/nodes` (`node.id`) and `/api/v1/network/registered-nodes` (`registerednode.id`).

**Observed:** `GET /api/v1/network/nodes?node.id=ne:0&limit=3` returns nodes `0, 1, 4` (mainnet).

**Cause:** the loops in
[`NetworkServiceImpl.getNetworkNodes`](https://github.com/hiero-ledger/hiero-mirror-node/blob/v0.165.1/rest-java/src/main/java/org/hiero/mirror/restjava/service/NetworkServiceImpl.java#L100)
and [`resolveRegisteredNodeIdBounds`](https://github.com/hiero-ledger/hiero-mirror-node/blob/v0.165.1/rest-java/src/main/java/org/hiero/mirror/restjava/service/NetworkServiceImpl.java#L154)
have no branch for `ne`, so the condition is silently dropped.

**Expected:** 400. The OpenAPI pattern of both parameters does not allow `ne`.

### 3. `/contracts/results/logs`: a contradictory or empty `timestamp` range returns unfiltered results

**Observed** (mainnet): `GET /api/v1/contracts/results/logs?timestamp=gt:<newest log>&timestamp=lt:<an old log>`
returns the newest logs instead of nothing. An empty range (`gt:x&lt:x`) behaves the same way. On every other
endpoint, both return an empty list or a 400.

**Cause:** [`optimizeTimestampFilters`](https://github.com/hiero-ledger/hiero-mirror-node/blob/v0.165.1/rest/controllers/contractController.js#L393)
returns `{filters: []}` when `range.isEmpty()`, which removes every timestamp condition. It is only used without a
contract id, so `/contracts/{id}/results/logs` is not affected.

**Expected:** an empty result, or a 400.

### 4. 503 instead of 400 for integers out of range

**Observed:** `index` (`/api/v1/contracts/{id}/results/logs`, `/api/v1/contracts/results/{id}/actions`) and
`transaction.index` (`/api/v1/contracts/results`) answer **`503 Service unavailable`** for `9223372036854775807`,
`9223372036854775808`, a 20-digit value and a decimal value. `serialnumber`, `sequencenumber` and `block.number`
reject the same values with 400.

**Expected:** 400 "Invalid parameter".

## Questions on the intended semantics

### 5. Repeated `eq` means IN for some parameters and AND for others

**Observed:** `account.id=eq:A&account.id=eq:B` returns A and B on `/accounts`, `/balances` and `/transactions`, but
nothing on `/schedules`, `/tokens/{id}/balances` and `/tokens/{id}/nfts`. The same AND applies to `account.balance`,
`schedule.id`, `timestamp` on `/transactions`, `/accounts/{id}` and `/topics/{id}/messages`, `sequencenumber`,
`block.number` on `/blocks` and `serialnumber` on `/tokens/{id}/nfts`. Even the same parameter name differs: `timestamp`
is IN on `/contracts/results` and AND on `/transactions`.

**Cause:** [`parseParams`](https://github.com/hiero-ledger/hiero-mirror-node/blob/v0.165.1/rest/utils.js#L567) builds an
`IN (...)` only when the caller passes `allowMultiple`. Otherwise every `eq` becomes its own `and` clause.

**Question:** two different values combined with AND can never match, yet the request is accepted with 200. Is a 400
intended here, or IN semantics everywhere? The OpenAPI definition does not say which parameters are IN.

### 6. `/transactions` `account.id=ne:X` cannot exclude an account

**Observed:** with `account.id=ne:X`, account X is still involved in 28 of 50 returned transactions, against 29 of 50
without the condition.

**Cause:** [`transactions.js`](https://github.com/hiero-ledger/hiero-mirror-node/blob/v0.165.1/rest/transactions.js#L390)
applies `ne` per transfer row (`ctl.entity_id != X`). It therefore matches every transaction that has a transfer to
any other account, which is nearly all of them.

**Question:** is this the intended meaning? It cannot be used to exclude the transactions of an account.

### 7. Lenient parsing only in `rest-java`

**Observed:** the airdrop endpoints, `/accounts/{id}/allowances/nfts` and `/network/nodes` (`file.id`) accept an
upper-case operator (`EQ:0.0.5`), an empty value (`receiver.id=`) and a leading space. All endpoints served by `rest`
reject these with 400. The `rest-java` endpoints also reject an EVM address, which `rest` accepts for account
parameters.

**Cause:** [`RangeOperator.of`](https://github.com/hiero-ledger/hiero-mirror-node/blob/v0.165.1/rest-java/src/main/java/org/hiero/mirror/restjava/common/RangeOperator.java#L42)
upper-cases the operator.

**Question:** which behaviour is the contract? As endpoints move from `rest` to `rest-java`, their accepted input
changes.

### 8. Several `account.balance` values do not finish on mainnet

**Observed:** `GET /api/v1/accounts?account.balance=eq:X&account.balance=eq:Y` returns 502/503 or times out after 30 s
on mainnet. One value or a range answers in about 0.7 s. Because of item 5, such a request can only ever return an
empty list.

**Question:** should repeated `eq` on `account.balance` be rejected up front?

## OpenAPI definition vs. server

### 9. Differences between `openapi.yml` (v0.164.0) and the server (v0.165.1)

| Parameter | OpenAPI definition | Server |
|---|---|---|
| `/tokens` `account.id` | operators allowed | only a bare value; every operator returns 400 |
| `/balances` `account.id` | no operators | all six operators work |
| `node.id`, `registerednode.id` | no `ne` | `ne` accepted and ignored (item 2) |
| `sequencenumber` | plain integer | `eq`, `gt`, `gte`, `lt`, `lte` |
| `transaction.index` | plain integer | `eq` |
| `serialnumber` | the pattern allows `0` | NFT endpoints reject `0`; on the airdrop endpoints `0` matches the fungible airdrops |
| `limit` | at most 100 | larger values are accepted and silently capped at 100 |
| every parameter with conditions | repeatable | `/accounts/{id}/nfts`, `/accounts/{id}/allowances/*` and `/contracts/.../logs` allow at most one `eq`, one lower and one upper bound; `transaction.hash` and `file.id` allow only one value |

**Question:** should we report these as documentation issues?

## Question on the hook endpoints

### 10. When will the hook endpoints be enabled?

`/api/v1/accounts/{id}/hooks` and `/api/v1/accounts/{id}/hooks/{hookId}/storage` answer 404 "Not found" on both public
networks, the same as an unknown path. The `rest-java` chart has
[`routes.hooks: false`](https://github.com/hiero-ledger/hiero-mirror-node/blob/v0.165.1/charts/hedera-mirror-rest-java/values.yaml#L190)
by default. When will they be enabled on testnet and mainnet? We could not measure how their conditions (`key`,
`timestamp`) behave.

## How to reproduce

The probe script and the raw results of both networks are in the Hiero SDK V3 prototyping repository:

- `tooling/openapi/probe-mirror-node-conditions.py`: run it with
  `python3 -I probe-mirror-node-conditions.py [--base <mirror node>/api/v1] --markdown <file>`. It is read-only and
  uses only the Python standard library.
- `docs/mirror-node-query-conditions-results-mainnet.md` and `docs/mirror-node-query-conditions-results-testnet.md`:
  the raw results.
- `docs/mirror-node-query-conditions.md`: the full analysis.
