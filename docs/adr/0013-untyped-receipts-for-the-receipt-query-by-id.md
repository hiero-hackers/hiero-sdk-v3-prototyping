# ADR-0013: Untyped receipts for the receipt query by id; `Response` is a value

**Status:** Accepted
**Date:** 2026-10-19

## Context

### The gap

V3 types every receipt by its transaction: `AccountCreateTransaction` yields an `AccountCreateReceipt`, and the spec
has 50 such receipt subtypes. A receipt was reachable only through `Response.queryReceipt()`, and for a transaction
the caller did not submit through `Transaction.getResponse(transactionId, transactionType, client)` [1]. Three cases
are not covered:

1. **Children.** The network creates child transactions (e.g. those a smart contract call triggers); the caller does
   not know their type. HAPI returns their receipts on request (`include_child_receipts`) [2].
2. **Duplicates.** Further submissions of the same transaction id; HAPI returns them on request
   (`includeDuplicates`) [2].
3. **Id only.** The Hiero TCK reads a receipt with nothing but the transaction id (`getTransactionReceipt`, ten tests
   in `test-transaction-receipt-query.ts`) and expects a generic result (`status`, `accountId`, `serials`,
   `duplicates`, `children`, `exchangeRate`) [3]. The method had no binding, so these tests were skipped.

The protocol's `TransactionReceipt` is one message with every attribute optional and does not name the transaction
type it belongs to [4]; a query that knows only the id cannot produce a typed receipt.

### `Response` could not query anything

`Response<$$Receipt>` held only a `TransactionId`, yet `queryReceipt()` and `queryRecord()` had to reach a consensus
node; the spec recorded this as an open question [1]. The Java implementation bridges it with a process-wide registry
from `TransactionId` to the submitting client, so a `Response` of another process cannot be queried
(`ClientRuntime.queryReceipt`, "was not submitted through a client of this SDK") [5]. `getResponse(...)` existed to
bind a client to a `Response` for the schedule case of ADR-0005 [6].

### What the current SDKs do

All seven current Hiero SDKs (Java, JavaScript, Go, Rust, Python, Swift, C++; commits in [7]) have a
`TransactionReceiptQuery` (Python: `TransactionGetReceiptQuery`) with `transactionId`, `includeDuplicates` and
`includeChildren`, and **one generic `TransactionReceipt`** with every attribute optional plus `duplicates` and
`children`; none has typed receipts [7]. The query is free in every SDK and in HAPI [2][7]. `TransactionResponse` keeps
the transaction id and the node it was sent to, and `getReceipt()` builds the query pinned to that node; Java,
JavaScript, Go, Rust and C++ offer an opt-in failover, off by default; neither HAPI nor the SDKs state why the query
is pinned [7]. Whether a failed status throws depends on a `validateStatus` flag whose default differs between the SDKs
and between query and response [7]. The SDKs poll while the node answers `BUSY`, `UNKNOWN`, `RECEIPT_NOT_FOUND` or
`PLATFORM_NOT_ACTIVE`; Java, Go, Python and C++ also while the receipt's status is `OK` [7].

## Decision

We will add **untyped receipts next to the typed ones** and make **`Response` a plain value**:

- `TransactionReceiptQuery extends FreeQuery<TransactionReceipts>` (`transactionId`, `includeDuplicates`,
  `includeChildren`, `@@nullable nodeAccountId`) in the new namespace `consensusnode.queries.receipts` [8]. It returns
  `TransactionReceipts` with `receipt`, `duplicates` and `children` as `UntypedReceipt`s.
- `UntypedReceipt extends Receipt` holds every attribute of the protocol's receipt, each optional — the shape of the
  current SDKs, but only here. `@@finalMethod $$R typed<$$R extends Receipt>(transactionType: type<Transaction<$$R,
  ANY>>)` turns it into the typed receipt without a network request: the SDK rebuilds the protocol message and hands it
  to the `TransactionSupport` of the type (ADR-0009). The method is not called `as`, which is a reserved word in Rust
  and Python.
- `Response<$$Receipt>` holds `transactionId`, `transactionType` and `@@nullable @@default(null) nodeAccountId`;
  `queryReceipt(client)` and `queryRecord(client)` take the client, as `submit(client)` does everywhere else [1].
  `queryReceipt(client)` is the `TransactionReceiptQuery` sent to `nodeAccountId` (any node when it is null), followed
  by `typed(transactionType)`.
- `Transaction.getResponse(transactionId, transactionType, client)` is removed: without a client, it is the constructor
  `new Response<>(transactionId, transactionType)` (the second constructor follows from `@@default(null)`).
- The query returns the receipt also for a failed transaction; there is no `validateStatus` flag. Whether the typed
  `queryReceipt(client)` throws on a failed status stays the open question in `transactions.md` [1].

Rejected:

- **Typed only** (the query takes a mandatory transaction type): cannot express children, whose type is unknown, nor
  a query by id alone.
- **Generic only**, as in the current SDKs: gives up the typed receipts, which tell a caller at compile time which
  attributes a transaction's receipt has.
- **`Response` holds the client**: keeps `queryReceipt()` without a parameter, but binds the value to a live client
  with network connections, so it cannot be stored or used in another process, and a static factory taking a client
  is still needed for the schedule case.
- **No node in `Response`**, the query always going to any node: simpler, but differs from every current SDK, and no
  measurement shows that any node answers equally well (Solo has one node).

## Consequences

### Positive
- The receipt of every transaction is readable by id, including duplicates and children; the TCK method
  `getTransactionReceipt` becomes bindable.
- `Response` needs no registry and can be stored and used in another process; the open question "`Response` cannot
  query anything" of `transactions.md` is resolved.
- The API has one static factory less; reading a schedule's or a batch's inner transaction uses the same constructor.
- `typed(...)` works for custom transaction types too, through the SPI that already creates typed receipts.

### Negative
- Two kinds of receipts: callers of the query handle `UntypedReceipt`, whose attributes are all optional.
- `typed(...)` cannot always detect a wrong transaction type: a receipt converts to every type whose receipt requires
  no attribute it lacks (e.g. `TransferReceipt`).
- `queryReceipt` and `queryRecord` need the client as an argument at every call; every caller of the former
  `queryReceipt()` changes.
- Breaking for `sdk-java/impl`, the TCK runtime and the enterprise layer, which call `queryReceipt()` today and rely on
  the registry; they must be carried over.
- Pinning to `nodeAccountId` is kept without a documented reason.

### Follow-ups
- Carry the change over to `sdk-java/impl` (`Response`, `ClientRuntime`, the new types, `typed` via
  `TransactionSupport`) and to the TCK runtime; bind `getTransactionReceipt`, and return `transactionId` from the
  transaction bindings, which the TCK tests read.
- `impl` polls only while the node answers `BUSY`, `UNKNOWN`, `RECEIPT_NOT_FOUND`, `PLATFORM_NOT_ACTIVE` or the
  receipt's status is not final; check whether it must also wait while the receipt's status is `OK`, as four SDKs do
  [5][7].
- Open questions recorded in `queries-receipts.md`: the polled statuses, a failover after a pinned node failed, and
  whether any node can answer (measure against a network with more than one node).
- Revisit if HAPI adds the transaction type to the receipt: then the query could return typed receipts directly.

---

**References:**
1. `spec/consensus-node-client/transactions.md` — before this change: `Response` with only `transactionId` and
   `getResponse(...)` (schema), the open question "`Response` cannot query anything" (Questions & Comments; git
   history), and the open question on the status of a rejected transaction (Questions & Comments).
2. `tooling/protobuf/consensus-node/services/transaction_get_receipt.proto:39-53` (retention of about three minutes,
   free, `UNKNOWN`), `:95` (`includeDuplicates`), `:105` (`include_child_receipts`), `:135-184` (response with
   duplicate and child receipts).
3. hiero-sdk-tck revision `a3ea777`, `src/tests/crypto-service/test-transaction-receipt-query.ts` (tests #1–#11);
   `docs/test-specifications/crypto-service/TransactionReceiptQuery.md` (parameters and output).
4. `tooling/protobuf/consensus-node/services/transaction_receipt.proto:35-217` (`TransactionReceipt`, all fields).
5. `sdk-java/impl/org.hiero.consensus.node.client/src/main/java/org/hiero/consensusnode/client/internal/ClientRuntime.java`
   (`queryReceipt`, `awaitReceipt`, `isPending`).
6. `docs/adr/0005-schedule-service-reuses-transaction-model.md:143-148`.
7. Local clones of the current SDKs, read on 2026-10-19: hiero-sdk-java `f917e67b3`, hiero-sdk-js `83dd15c`,
   hiero-sdk-go `eb2a9bd`, hiero-sdk-rust `9ae3b3f`, hiero-sdk-python `56c884a`, hiero-sdk-swift `f8e0a10`,
   hiero-sdk-cpp `f1537cc`. Among others: Java `TransactionReceiptQuery.java:23,75,99,105-107,175-205`,
   `TransactionReceipt.java:22-144,297-301`, `TransactionResponse.java:140,194-213`, `Client.java:85,1322-1333`;
   JavaScript `TransactionReceiptQuery.js:51-59,164,209-252`, `TransactionResponse.js:188-215`; Go
   `transaction_receipt_query.go:28,229-242`, `transaction_response.go:121-135`; Rust
   `transaction_receipt_query.rs:30-37,93,137-168`; Python `transaction_get_receipt_query.py:27-45,195-215`; Swift
   `TransactionReceiptQuery.swift:51,88,102-118`, `TransactionReceipt.swift:191-192`; C++
   `TransactionReceiptQuery.h:146`, `TransactionReceiptQuery.cc:74-96`, `TransactionResponse.cc:50-60`.
8. `spec/consensus-node-client/queries-receipts.md` (new) and `spec/consensus-node-client/transactions.md`
   (`Response`, *Reading the outcome*, Design Notes).
