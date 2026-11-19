# ADR-0017: Request types are an open type registered through the SPI

**Status:** Accepted
**Date:** 2026-11-19

## Context

### What a request type is

Every transaction and query of the consensus node has a request type, the protocol enum `HederaFunctionality` [1].
The network reports data per request type, for example the prices of its fee schedule [2]. The fee schedule query of
ADR-0016 therefore needs a type for it.

### The protocol grows faster than an enum

`HederaFunctionality` is shared by every service, the optional ones included: the CLPR service has the values 117 to
128 [1]. The Java SDK (`f917e67b3`) models it as a closed enum without these values and throws for every value it does
not handle [3]. The consensus node is service-oriented, and custom services bring their own transaction types [4].

### The precedent: status codes

The status codes have the same shape: one protocol enum, extended by optional and custom services. V3 models them as an
open `TransactionStatus` with `BasicTransactionStatus`. Every service names its codes with a `TransactionStatusSupport`
in the SPI. The CLPR module registers `ClprTransactionStatus`, and two supports that name the same code make
`createClient` fail [5][6].

The first draft of the fee schedule spec defined `RequestType` inside `consensusnode.queries.fees`, with all 111 values
in one enum, the CLPR values included. In review it was moved to the client and registered through the SPI.

## Decision

**Request types are modeled like the status codes:**

1. `RequestType` (an abstraction with `code: int32`) and `BasicRequestType` (every value of `HederaFunctionality`
   except the CLPR ones) live in a namespace of their own, `consensusnode.requestTypes`, at the bottom of the
   consensus node client, so that the SPI and every user can import them without a cycle [7].
2. A service names its request types with a **`RequestTypeSupport`** in `consensusnode.transactions.spi`.
   `getAllRequestTypeSupports()` returns them, and `getRequestType(code)` returns the request type of a code or fails
   with `unknown-request-type-error` [8].
3. The SDK registers the support of `BasicRequestType`. The CLPR module registers `ClprRequestType` (117–128) [9].
4. The supports are loaded with the first client. Two supports that name the same code make `createClient` fail with
   `request-type-conflict-error` [10].
5. A user of the codes decides what an unknown code means. The fee schedule leaves its entry out (ADR-0016).
6. In Java a support is declared twice: `provides` in `module-info.java` for the module path, and `@AutoService` for
   the generated `META-INF/services` on the class path [11].

**Rejected:**

- **A closed enum** (V2, and the first draft): a module or a custom service could not add request types, and the CLPR
  values would sit in the client although the CLPR API is a module of its own [3][9].
- **An open type in the fee namespace without a support:** the request type is a concept of the protocol, not of the
  fees, and the SDK could not name the request types of a module [7].
- **Rejecting a conflict lazily, on first use:** this would differ from the status codes and only surface in the
  middle of a request.

## Consequences

### Positive

- Status codes and request types are extended in the same way. A module or a custom service adds both with one
  pattern.
- `BasicRequestType` stays a mechanical copy of the protocol without the values of optional services [7].
- A conflict surfaces when the client is created, before any request [10].

### Negative

- Two more error ids (`unknown-request-type-error`, `request-type-conflict-error`), and `createClient` declares one
  more error [8][10].
- `BasicRequestType` has to be taken over again whenever the protobuf definitions are updated [7].
- A custom service has to register its request types separately from its `TransactionSupport` [12].

### Follow-ups

- Open: should a `TransactionSupport` name its request type, so that no separate support is needed (queries have no
  support) [12]? And should `Transaction` or `PackedTransaction` expose it [13]?
- Open: should `BasicRequestType` name the internal request types that no client sends [12]?

---

**References:**
1. `tooling/protobuf/consensus-node/services/basic_types.proto:1341` (`enum HederaFunctionality`), `:1885`
   (`ClprUpdateLedgerConfiguration = 117`), `:1941` (`ClprGetEndpointManifest = 128`).
2. `spec/consensus-node-client/queries-fee.md:36-49` (rules: decoding, request types, unknown codes).
3. hiero-sdk-java `f917e67b3` (2026-08-17), local clone read on 2026-11-19:
   `sdk/src/main/java/com/hedera/hashgraph/sdk/RequestType.java:555-656` (`valueOf` ends with
   `MIGRATION_ROOT_HASH_VOTE`, `default -> throw new IllegalStateException`).
4. `spec/consensus-node-client/transactions-spi.md` (Description: custom services and their transaction types).
5. `spec/consensus-node-client/status.md:33` (`abstraction TransactionStatus`), Design Notes;
   `spec/consensus-node-client/transactions-spi.md:39-50` (status supports, unknown and conflicting codes).
6. `spec/consensus-node-clpr/clpr.md` (`ClprTransactionStatus`, Design Notes).
7. `spec/consensus-node-client/request-types.md:19-28` (Design Notes: own namespace, every code of the protocol).
8. `spec/consensus-node-client/transactions-spi.md:147-159` (`RequestTypeSupport`, `getAllRequestTypeSupports`,
   `getRequestType`), `:51` (Design Note).
9. `spec/consensus-node-clpr/clpr.md:42` (Design Note), `:159` (`enum ClprRequestType`).
10. `spec/consensus-node-client/client.md:390` (`createClient` with `request-type-conflict-error`);
    `sdk-java/impl/org.hiero.consensus.node.client/src/main/java/org/hiero/consensusnode/client/ClientFactory.java:44-45`.
11. `sdk-java/impl/org.hiero.consensus.node.client/src/main/java/module-info.java:68` (`uses`),
    `sdk-java/impl/org.hiero.consensus.node.clpr/src/main/java/module-info.java:37` (`provides`),
    `…/clpr/internal/ClprSupports.java:57` (`@AutoService(RequestTypeSupport.class)`),
    `…/client/internal/RequestTypes.java:91` (`ServiceLoader`); `guidelines/api-best-practices-java.md:2485-2500`.
12. `spec/consensus-node-client/request-types.md:286-291` (Questions & Comments).
13. `spec/consensus-node-client/transactions.md:880` (Questions & Comments).
