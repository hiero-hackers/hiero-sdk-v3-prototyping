# ADR-0016: System files are read by typed queries without a file id

**Status:** Accepted
**Date:** 2026-11-19

## Context

### System files have a fixed number and a known format

The consensus node keeps part of its configuration in system files: the address book (`.102`), the fee schedule
(`.111`) and the exchange rates (`.112`), each in the shard and realm of the network [1]. Their content is a protobuf
message: `CurrentAndNextFeeSchedule` for the fee schedule, `ExchangeRateSet` for the exchange rates [2]. A generic
`FileContentsQuery` returns these files only as bytes [3].

### How the V2 SDKs do it

In the Java SDK the caller builds the file id (`FileId.getFeeScheduleFileIdFor(shard, realm)`,
`getExchangeRatesFileIdFor`, `getAddressBookFileIdFor`), runs a `FileContentsQuery` and decodes the bytes itself
(`FeeSchedules.fromBytes`, `ExchangeRates.fromBytes`) [4]. So the caller has to know three things: the number of the
file, the shard and realm of the network, and which type decodes it. Decoding the fee schedule converts every
`HederaFunctionality` with `RequestType.valueOf`, which throws `IllegalStateException` for a value it does not handle
[5]: a fee schedule that names one request type the SDK does not know cannot be read at all.

### Forces

- A file id that depends on the network should not be built by the caller: V3 supports other shards and realms, and
  the query knows the network only when it is sent [6].
- The result should be a typed value, not bytes plus a decoder the caller has to find.
- A typed query must not be able to read a file of another format.
- `submit` has no error of its own beyond those of `Submittable` [7].
- A newer network can add request types and fee sub types before the SDK knows them [8].

## Decision

**Every system file whose content every user needs gets a typed `PaidQuery` of its own** (`ExchangeRatesQuery`,
`FeeScheduleQuery`) that reads the file and returns the decoded content (`ExchangeRates`, `FeeSchedules`) [6][8]:

1. **No `fileId`.** The query has no attribute for the file. A behavior rule fixes the number and takes shard and realm
   from the account of the consensus node the query is sent to (`rule:exchange-rates-file`, `rule:fee-schedule-file`).
2. **No subtype of `FileContentsQuery`.** It extends `PaidQuery` directly. A subtype would inherit the result type
   `FileContents` (generics are invariant) and a settable `fileId` that could point to a file of another format [6].
3. **Decoding is specified.** A rule names the protobuf message and maps every field (`rule:exchange-rates-decoding`,
   `rule:fee-schedule-decoding`).
4. **A decoding failure is `REJECTED`.** The attempt ends with `AttemptOutcome.REJECTED`, the retry policy gives up and
   `submit` fails with `attempts-exhausted-error`. No new error is added [6][7].
5. **Unknown codes are dropped, not rejected.** An entry whose code the SDK cannot name is left out and the rest of the
   file is returned (`rule:fee-schedule-unknown-codes`) [8]. This differs from the status codes, where an unknown code
   fails the request, because the status decides how the request goes on.
6. **Namespace by audience and size.** Small typed queries live in `consensusnode.queries.systemFiles`. A file that
   brings a model of its own gets a namespace of its own, e.g. the fee schedule in `consensusnode.queries.fees`
   (six types). A file whose audience is operators goes to the admin client, e.g. the address book with
   `NodeAddressBookQuery` [6][8][9].

**Rejected:**

- **Keep the V2 approach** (a file id helper plus `fromBytes`). It leaves the file number, the shard and realm, and the
  matching decoder to the caller [4].
- **A subtype of `FileContentsQuery`.** It would return the wrong result type and expose a `fileId` (see 2).
- **One generic system file query** (e.g. `SystemFileQuery<$$T>` with a descriptor per file). With two files today, a
  generic mechanism would cost more than it saves. This is the revisit condition below.
- **A dedicated error for undecodable content.** `REJECTED` already describes an answer that could not be used [7].

**Not decided here:** whether `FileContentsQuery` keeps the factories `forExchangeRates()` / `forFeeSchedule()`. They
are the current state and are still open in the specs [10].

## Consequences

### Positive

- Reading a system file is one call with a typed result: `new FeeScheduleQuery().submit(client)`.
- The file id is right on every shard and realm without any work by the caller.
- Every SDK decodes the same way: the rules are testable and the Java implementation follows them [11].
- A fee schedule stays readable when the network knows more request types than the SDK [8][11].

### Negative

- Every new system file needs a new query type, a result type and two rules.
- Dropping unknown entries hides data: a caller cannot see the prices of a request type the SDK does not know [8].
- Shard and realm come from the node, so the implementation needs the node (or the network of the client) when it
  builds the request. The Java implementation takes them from the first node of the client [11].
- If every attempt fails to decode, the error is `attempts-exhausted-error` with the precheck status `OK` of the last
  answer, which says little about the cause [11].

### Follow-ups

- Decide the factories on `FileContentsQuery` [10].
- The Java implementation still lacks `ExchangeRatesQuery` (`metalang check`); it can reuse the mechanism of
  `FeeScheduleQuery` [11].
- **Revisit when**
  - so many system files get typed queries that a generic mechanism is cheaper;
  - users need the prices of request types the SDK does not know (keep unknown codes instead of dropping them) [8];
  - a fee estimation API replaces the fee schedule types or uses them differently.

---

**References:**
1. `spec/consensus-node-client/queries-system-files.md:37-40` (system files `.102`, `.111`, `.112`, `.123`).
2. `tooling/protobuf/consensus-node/services/basic_types.proto:2139` (`CurrentAndNextFeeSchedule`),
   `tooling/protobuf/consensus-node/services/exchange_rate.proto:73` (`ExchangeRateSet`).
3. `spec/consensus-node-client/queries-files.md` (`FileContentsQuery`, result `FileContents` with `contents: bytes`).
4. hiero-sdk-java `f917e67b3` (2026-08-17), local clone read on 2026-11-19:
   `sdk/src/main/java/com/hedera/hashgraph/sdk/FileId.java:93,103,113` (`get…FileIdFor(shard, realm)`),
   `FeeSchedules.java:53` and `ExchangeRates.java:44` (`fromBytes`).
5. hiero-sdk-java `f917e67b3`: `TransactionFeeSchedule.java:43` (`RequestType.valueOf(...)`), `RequestType.java:555,656`
   (`default -> throw new IllegalStateException("(BUG) unhandled HederaFunctionality")`).
6. `spec/consensus-node-client/queries-system-files.md:23-30` (rules), `:41-54` (design notes: audience, no subtype,
   no `fileId`, `REJECTED`).
7. `spec/consensus-node-client/client.md:249` (`Submittable.submit` with `@@throws(attempts-exhausted-error,
   unknown-status-error)`, `AttemptOutcome.REJECTED`); ADR-0014.
8. `spec/consensus-node-client/queries-fee.md:33-49` (rules), `:57-68` (design notes), `:264` (open question on
   unknown entries).
9. `spec/consensus-node-admin-client/queries-network.md:10,30` (`NodeAddressBookQuery`).
10. `spec/consensus-node-client/queries-files.md:55,59` (factories); open question
    `spec/consensus-node-client/queries-system-files.md:135`.
11. `sdk-java/impl/org.hiero.consensus.node.client/src/main/java/org/hiero/consensusnode/client/internal/FeeQueries.java:35,59-65,73,98`;
    `…/client/internal/Execution.java:92` (`MalformedAnswerException` → `REJECTED`); output of `metalang check
    --project=sdk-java/impl` on 2026-11-19 (5 differences, all exchange rates).
