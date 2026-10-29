# ADR-0014: Retry and node selection are specified and exchangeable public API

**Status:** Accepted
**Date:** 2026-10-29

## Context

### V3 promised retries without defining them

`Submittable` carried four nullable settings (`maxAttempts`, `minBackoff`, `maxBackoff`, `attemptTimeout`), the last
three as `int64` without a unit, and its documentation promised "retrying transient gRPC failures" without saying
which failures those are [1]. The client had no defaults for them [2]. The Java implementation does not retry at all
yet; a short outage of the local network (Solo restarting its port-forward) therefore failed whole TCK files [3]. Before
retries can be implemented, it has to be defined what happens and how it is configured.

### The current SDKs disagree, and none can be extended

The seven current Hiero SDKs share a pattern - 10 attempts, a request backoff from 250 ms to 8 s, 10 s per attempt,
2 minutes per request, nodes avoided from 8 s up to 1 h [4] - but react differently to the same answer of the
network [4]:

- `BUSY`: the same node again in Java and Go, the next node in JavaScript, Rust, Swift, C++ and Python.
- gRPC `DEADLINE_EXCEEDED`: aborts the request in Java, is retried in JavaScript, Go, Python and Swift, is permanent
  in Rust, and makes a C++ transaction submit look successful (read from the code, not observed).
- `PLATFORM_TRANSACTION_NOT_CREATED` is not retried in Rust and Swift; `THROTTLED` is retried by none.
- Queries never fail over in Go.

Several settings have no effect: the client backoff in Go, the client `maxBackoff` in JavaScript, `set_min_backoff`
in Rust (it writes `max_backoff`), the node backoff and `maxNodesPerTransaction` in C++ [4]. No SDK offers a strategy
interface; Java and C++ offer request and response listeners, the others a logger at most [4]. No HIP specifies the
behaviour: HIP-1299 asks clients to retry on other nodes [5], HIP-1383 (deferred) proposes a retry hint sent by the
network [6].

### Namespaces must stay acyclic

The Go generator emits one package per namespace [7], and Go forbids import cycles [8]. The statuses
(`TransactionStatus`) lived in `consensusnode.transactions`, which imports `consensusnode.client` [9]; a client that
classifies answers by status would close a cycle.

## Decision

We make the execution of a request a specified, exchangeable part of the public API of the consensus node
client [10]:

1. **A normative classification.** Every attempt ends in an `AttemptOutcome` (`SUCCESS`, `NODE_UNAVAILABLE`,
   `NODE_TIMEOUT`, `NODE_BUSY`, `THROTTLED`, `WRONG_NODE`, `PENDING`, `REJECTED`). Which answer has which outcome is
   specified as a table and is the same in every SDK. The raw answer is an `AttemptResult`: a `NodeAnswer` with the
   status, or a `TransportFailure` with a transport-neutral `TransportFailureKind` (`CONNECTION`, `TIMEOUT`,
   `OVERLOADED`, `INTERRUPTED`, `PROTOCOL`); the mapping of gRPC codes is specified, but no gRPC code is API.
2. **Exchangeable decisions.** A `RetryPolicy` decides after every failed attempt (`RetryOnSameNode`,
   `RetryOnOtherNode` with a delay, or `GiveUp`) from a `RetryContext` (request, attempts, elapsed time). A
   `NodeSelector` picks the node and keeps the health of the nodes across requests. An `AttemptListener` observes
   every attempt. All three are abstractions a caller can implement.
3. **Configuration.** `ExecutionSettings` (retry policy, node selector, attempt timeout, listeners) belong to the
   `HieroClient`; a request (`Submittable`) can override `retryPolicy` and `attemptTimeout`. All times are
   `duration`. When the policy gives up, `submit` fails with `attempts-exhausted-error`.
4. **The default is documentation, not API.** Only `ExecutionSettings.createDefault()` and
   `ExecutionSettings.createDefault(maxAttempts, attemptTimeout)` exist; their documentation describes the default
   behaviour. `RetryPolicy` and `NodeSelector` have no factories of their own.
5. **Execution means of a language.** `ExecutionSettings` invites a language that has its own means of running
   asynchronous work to add an attribute for it with a default (in Java an `Executor` [11]); the meta-language gets
   no type for it.
6. **`TransactionStatus` and `BasicTransactionStatus` move to the new bottom namespace `consensusnode.status`** [12].

Rejected:

- **Configuration values only** (as in the current SDKs): keeps the classification hard-coded and differing per SDK,
  and gives no way to use another strategy.
- **An overridable classification:** a custom policy would see different outcomes in different setups, and the SDKs
  would drift again; a policy that needs more can read the raw `AttemptResult`.
- **gRPC codes in the API:** would tie the abstraction to one transport.
- **An overall request timeout setting:** with the attempt count, the attempt timeout and a total time, which bound
  applies depends on the failure; in the current SDKs the 2 minutes cut off a request that times out on every
  attempt (about 140 s) and never matter for one that fails fast. The default is bounded by `maxAttempts` times
  `attemptTimeout` plus the waits; a policy that needs a total time uses `RetryContext.elapsed`.
- **Configurable factories per part** (`RetryPolicy.createDefault(maxAttempts)`, `NodeSelector.createDefault()`):
  more API for the same behaviour; one documented entry point is enough.
- **An execution type in the meta-language:** JavaScript, Go and Python have nothing to configure there, and Rust's
  runtime is not a value [11].
- **Keeping `TransactionStatus` and passing the status as `int32`, or a `RequestKind` instead of the request:**
  avoids the move, but loses the status type or the ability of a policy to tell transaction types apart.

## Consequences

### Positive

- The behaviour of retries and failover is the same in every SDK and testable against the spec, including the cases
  where the current SDKs disagree.
- A caller can replace the strategy or the node selection and observe every attempt, which no current SDK allows.
- The unit question of the old settings is gone; every time is a `duration`.
- A retry hint of the network (HIP-1383) can be added to `RetryContext` without changing a signature.

### Negative

- The public API grows by 15 types in `consensusnode.client` and a new namespace [10][12]; every SDK has to implement
  the classification table exactly.
- Moving the statuses changed the imports of two spec files, the TCK converter and 104 files of `sdk-java/impl` [12].
- `sdk-java/impl` does not provide the new API yet: `metalang check` reports the missing types and members.
- An error id cannot carry attributes, so `attempts-exhausted-error` cannot declare the attempts or the status a caller
  needs [10].
- The default values are taken over from the current SDKs, which copied them from the Java SDK; none is derived from
  measured answer times (assumption, see References [4]).
- `metalang check` compares record components exactly and will report a language-specific attribute such as the Java
  `executor` [11].

### Follow-ups

- Decide the open default questions in `client.md`: jitter, retrying `THROTTLED`, and whether `DUPLICATE_TRANSACTION`
  after a timed-out attempt of the same request counts as `SUCCESS`.
- Name the statuses the classification uses (`BUSY`, `THROTTLED`, `RECEIPT_NOT_FOUND`, ...) in
  `BasicTransactionStatus`.
- Implement the API in `sdk-java/impl`, then rerun the TCK.
- Measure the answer times of consensus nodes (Solo, testnet) and revisit the default values.
- Revisit when HIP-1383 is accepted, and when the client gets access to its nodes and address book.

---

**References:**
1. `spec/consensus-node-client/client.md` before this change (`git show HEAD:spec/consensus-node-client/client.md`):
   `Submittable` `:50-64`, the open question on the unit of the settings (Questions & Comments).
2. `docs/missing-features.md:496-497` (request timeout, gRPC deadline, backoff and attempts only per transaction).
3. `docs/tck-local-checks.md:11-14` (TCK run disturbed by Solo restarting its port-forward).
4. Local clones of the current SDKs, read 2026-10-28, revisions: hiero-sdk-java `f917e67b3`, hiero-sdk-js `83dd15c`,
   hiero-sdk-go `eb2a9bd`, hiero-sdk-rust `9ae3b3f`, hiero-sdk-python `56c884a`, hiero-sdk-swift `f8e0a10`,
   hiero-sdk-cpp `f1537cc`. Among others: Java `Client.java:47-54` (defaults), `Executable.java:439-441, 461-485,
   914-928, 954-963`; JavaScript `Executable.js:88, 436-452, 1035-1046`, `ManagedNetwork.js:176-179`; Go
   `executable.go:298-304, 333-336`, `query.go:43-46, 98-100`, `client.go:470-521`; Rust `client/mod.rs:606-611,
   697-704`, `execute.rs:382-428, 500-552`; Python `executable.py:377-391, 406`, `transaction.py:137-148`; Swift
   `Execute.swift:542-598`, `ExponentialBackoff.swift:55-58`; C++ `Executable.cc:226-232, 257-293, 501-502`,
   `BaseNetwork.cc:98-103, 144-160`. Java and C++ listeners: Java `Executable.java` `setRequestListener`/
   `setResponseListener`, C++ `include/Executable.h:149-220`. The values `Client.java:47-54`, Rust `set_min_backoff`,
   JavaScript `_maxBackoff` and the Go query backoff were checked by hand.
5. hiero-improvement-proposals `41ac253`, `HIP/hip-1299.md` (status Final), `:130-134`.
6. hiero-improvement-proposals `41ac253`, `HIP/hip-1383.md` (status Deferred), `RetryType` `:199-232`.
7. `CLAUDE.md`, repository structure, `sdk-go/` (one package per namespace).
8. The Go Programming Language Specification, Import declarations: "It is illegal for a package to import itself,
   directly or indirectly" — https://go.dev/ref/spec#Import_declarations
9. `spec/consensus-node-client/transactions.md` before this change: `requires ... from consensusnode.client` and
   `abstraction TransactionStatus` (`:160`).
10. `spec/consensus-node-client/client.md`: *How a request is executed* (`:14`), Design Notes (`:41`), schema from
    `Submittable` (`:116`) to `ExecutionSettings` (`:232-257`), Questions & Comments (`:412-430`).
11. `guidelines/api-best-practices-java.md`, *Where the asynchronous work runs*; `sdk-java/impl/org.hiero.consensus.node.client/src/main/java/org/hiero/consensusnode/client/internal/ClientRuntime.java:47`
    (today's executor of `impl`).
12. `spec/consensus-node-client/status.md` (new); `spec/consensus-node-client/transactions.md`,
    `transactions-spi.md`; `tooling/metalang/metalang-core/src/main/java/org/hiero/sdk/v3/metalang/tck/Converter.java`;
    `sdk-java/impl/.../consensusnode/status/`.
