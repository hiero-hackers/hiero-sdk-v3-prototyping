# Batch Transactions API

This namespace specifies the *atomic batch* transaction of the consensus node's util service
(HIP-551): `BatchTransaction`, which submits several independent transactions as a single
all-or-nothing unit.

## Description

A `BatchTransaction` bundles an ordered list of **inner transactions** and submits them as one
atomic unit. Per HIP-551, *"if any inner transaction fails, the entire batch will fail"* and all
state changes are rolled back atomically — it is all-or-nothing for **state effects**. Each inner
transaction keeps its own `TransactionId`, its own (possibly different) payer, and its own
signatures; the batch adds atomicity of state changes on top, it does not merge the transactions
into one.

**Fees are independent of the atomic outcome.** The outer batch and each *processed* inner
transaction are charged separately and may have different payers; an inner transaction that was
processed is charged its regular fee **even if the batch as a whole fails** and its effects are
rolled back. Atomicity therefore governs committed state, not fees.

The number of inner transactions is bounded by a network configuration parameter (currently tied to
the maximum number of preceding child transactions, ~50), so the SDK does not impose a fixed upper
limit — an over-large batch is rejected by the network with a `TransactionStatus`.

### Preparing the inner transactions

Every inner transaction is **fully packed and signed up front**, possibly by different parties on
different machines, and the batch carries the finished, signed `PackedTransaction` payloads. (This
differs from `ScheduleCreateTransaction`, which takes an unsigned `Transaction` builder.)

An inner transaction is prepared through the batch-specific methods on `Transaction`, which mirror
the regular signing methods without a `nodes` parameter:

- `packForBatch(payer, batchKey)` — pack only, no signature;
- `signForBatchWithOperator(client, batchKey)` — the client's operator pays and signs;
- `signForBatch(payer, batchKey)` — one `Account` pays and signs;
- `signForBatch(payerId, signer, batchKey)` — the payer identity is decoupled from a
  `TransactionSigner` (HSM, hardware wallet, paymaster).

Each method produces a **single** `TransactionBody` addressed to no consensus node
(`nodeAccountID = 0.0.0`), because an inner batch transaction is never submitted to a node on its
own. Further signatures (multi-sig, offline) use the regular `PackedTransaction.sign(...)` /
`signableBodies()` methods.

The required `batchKey` is the `Authority` that must sign the *outer* `BatchTransaction` for this
inner transaction to run — the inner author's controlled opt-in to being batched. It is part of the
signed body, so it is fixed when the inner transaction is packed.

The resulting `PackedTransaction` instances are passed to `BatchTransaction.innerTransactions`. The
list is **ordered**: the network executes the inner transactions in list order.

### Signing the batch

Authorizing a batch uses the regular multi-signature flow:

- The `BatchTransaction` is signed by its own payer, **plus** the `batchKey` of every inner
  transaction. These are ordinary `NodeSignature` entries on the outer transaction; the network
  matches each inner transaction's `batchKey` against the signature set.
- The inner transactions are already self-contained (signed by their own required keys when they
  were packed for the batch). The batch does not re-sign them.

### Reading the inner outcomes

Per HIP-551, the batch record does **not** aggregate the inner results: each inner transaction
produces its own record whose `Record.parentConsensusTimestamp` equals the batch's
`consensusTimestamp`, and the receipts of the inner transactions must be queried individually.
`BatchReceipt` therefore carries only the batch transaction's own status (the base `Receipt`
fields). To read an inner transaction's typed receipt, take its `TransactionId` (the
`transactionId` field of the inner `PackedTransaction`) and resolve it through
`Transaction.getResponse(transactionId, transactionType, client)` — the same mechanism used for the
executed inner transaction of a schedule. See the example below.

### Restrictions

Per HIP-551 an inner transaction cannot be a network `freeze`, cannot be another
`BatchTransaction` (batches do not nest), and a batch itself must not be scheduled via
`ScheduleCreateTransaction`. The SDK does not check this; a disallowed inner transaction is rejected
by the network with a `TransactionStatus` on the receipt.

## Design Notes

- **Packed inner transactions vs. schedule builders.** This is the key difference from
  `ScheduleCreate` (see [`transactions-schedule.md`](transactions-schedule.md)). A schedule
  *captures* an inner transaction as a plain `Transaction<ANY, ANY>` builder and never packs or signs
  it — the network materializes it later. A batch is the opposite, so inner transactions are modelled
  as a list of `PackedTransaction`, not as builders. The batch-specific entry points are specified on
  `Transaction` in [`transactions.md`](transactions.md).
- **`batchKey` as a required parameter.** Because `batchKey` is a `TransactionBody` field, it must be
  fixed **before** signing; setting it afterwards would invalidate the inner signatures, the same
  invariant that drives the `Transaction` → `PackedTransaction` split. Passing it as a parameter
  (rather than as a free-standing build-phase field) makes two illegal states unrepresentable instead
  of network-rejected: an inner transaction can never be packed *without* a `batchKey`, and a
  normally-submitted transaction can never carry one. This is deliberately *not* the same as the
  batchability check — `batchKey` is an SDK-owned, knowable invariant of the batch-pack entry point,
  whereas which transaction *types* may be batched is network policy the SDK cannot know.
- **Signing model — no new concept.** A `batchKey` signature *is* a normal signature on the outer
  `BatchTransaction` — there is no separate "batch signature" payload, and the per-node body fan-out
  of the outer transaction is the standard model from [`transactions.md`](transactions.md). This
  mirrors schedules.
- **Why inner outcomes cannot be fetched in bulk.** Per-inner queries are the *only*
  protocol-supported read path; the inner outcomes cannot be fetched in bulk through HAPI's
  child-records query either. `TransactionGetRecordQuery.include_child_records` returns only
  node-synthesized children that share the parent's `TransactionId` (disambiguated by a `nonce`,
  e.g. HTS precompile calls inside a contract call). Batch inner transactions instead keep their own
  independent, user-supplied `TransactionId` (often a different payer), so they are not
  nonce-children of the batch and are not returned by a record query against the batch's id. They
  are therefore addressed individually by their own ids, and only *correlated* as a group after the
  fact via `parentConsensusTimestamp` (see [`transactions.md`](transactions.md)) in the record stream
  / mirror node.
- **No client-side batchability check.** As with schedulable transactions, the SDK does **not**
  enforce the HIP-551 restrictions: because the consensus node is service-oriented and supports custom
  services and transaction types (see [`transactions-spi.md`](transactions-spi.md)), the SDK cannot
  know the batchable set.

## API Schema

```
namespace consensusnode.transactions.batch
requires {Receipt, Transaction, PackedTransaction} from consensusnode.transactions

// Submits a list of independent inner transactions as a single atomic unit (HIP-551): all inner
// transactions execute, or none does. Each inner transaction must be packed via
// Transaction.packForBatch(...) or one of the signForBatch(...) methods, and be signed by its own
// required keys. This BatchTransaction must additionally be signed by every inner transaction's
// batchKey (ordinary signatures on this outer transaction).
@@finalType
BatchTransaction extends Transaction<BatchReceipt, BatchTransaction> {
    // The inner transactions to execute atomically, in execution order. Each is an already-packed,
    // already-signed PackedTransaction produced by Transaction.packForBatch(...) or
    // signForBatch(...). Must not be empty.
    @@immutable @@minSize(1) innerTransactions: list<PackedTransaction<ANY, ANY>>
}

// The batch transaction's own receipt. Carries only the base Receipt fields (status, exchange
// rates); each inner transaction's typed receipt is read separately via
// Transaction.getResponse(innerTransactionId, innerType, client).
@@finalType
BatchReceipt extends Receipt {
}
```

## Examples

### 1. Two parties batch two transfers atomically

Alice and Bob each authorize their own transfer, but want them to settle together — neither runs
unless both do. Each packs their own inner transaction for batch, sets the other-agreed `batchKey`,
and signs it. A coordinator collects the two signed inner transactions, builds the
`BatchTransaction`, and signs the outer transaction with both batch keys.

```
HieroClient client = ...;          // coordinator pays the batch fee
Authority aliceKey = ...;
Authority bobKey = ...;

// Alice prepares her inner transfer (on her machine):
PackedTransaction<TransferReceipt, ...> innerA = new TransferTransaction()
    .hbarTransfers([ new HbarTransfer(alice, NativeToken.of(-10, HBAR_UNIT)),
                     new HbarTransfer(bob,   NativeToken.of(+10, HBAR_UNIT)) ])
    .signForBatch(aliceAccount, aliceKey);  // pack for batch (single body, node 0.0.0) + Alice's
                                            // signature; aliceKey must sign the outer batch

// Bob prepares his inner transfer (on his machine):
PackedTransaction<TransferReceipt, ...> innerB = new TransferTransaction()
    .hbarTransfers([ new HbarTransfer(bob,   NativeToken.of(-3, HBAR_UNIT)),
                     new HbarTransfer(alice, NativeToken.of(+3, HBAR_UNIT)) ])
    .signForBatch(bobAccount, bobKey);

// Coordinator assembles and submits the batch, signing with both batch keys:
Response<BatchReceipt> response = new BatchTransaction()
    .innerTransactions([ innerA, innerB ])
    .signWithOperator(client)      // outer payer signature
    .sign(aliceAccount)            // batchKey signature for innerA
    .sign(bobAccount)              // batchKey signature for innerB
    .submit(client);

response.queryReceipt();           // batch status; both transfers ran, or neither did
```

> `signForBatch(payer, batchKey)` mirrors the non-batch `sign(payer, nodes)` tier (without `nodes`).
> The full set on `Transaction` is `packForBatch(payer, batchKey)`, `signForBatchWithOperator(client,
> batchKey)`, `signForBatch(payer, batchKey)`, and the most general `signForBatch(payerId, signer,
> batchKey)`; any additional signatures use the inherited `PackedTransaction.sign(...)` flow.

### 2. Single operator batches several of its own transactions

The common case: one operator submits several of its own transactions atomically. The operator is
the payer and the `batchKey` of every inner transaction, so it signs the outer batch once.

```
HieroClient client = ...;          // operator pays and is the batchKey
Authority operatorKey = ...;       // the operator's Authority

PackedTransaction<...> t1 = new TopicMessageSubmitTransaction()
    .topicId(topicId).message(msg1)
    .signForBatchWithOperator(client, operatorKey);   // v2 batchify(client, batchKey) equivalent

PackedTransaction<...> t2 = new TopicMessageSubmitTransaction()
    .topicId(topicId).message(msg2)
    .signForBatchWithOperator(client, operatorKey);

new BatchTransaction()
    .innerTransactions([ t1, t2 ])
    .signWithOperatorAndSubmit(client);  // one operator signature covers payer + both batchKeys
```

### 3. Read an inner transaction's receipt after the batch executes

The caller holds each inner `TransactionId` from the `PackedTransaction` it packed, and reconstructs
a typed `Response` for it — exactly as for the inner transaction of a schedule.

```
TransactionId innerAId = innerA.transactionId;

Response<TransferReceipt> innerResponse =
    Transaction.getResponse(innerAId, TransferTransaction, client);

TransferReceipt innerReceipt = innerResponse.queryReceipt();
```

## Questions & Comments

- **Open: can the batch key be a composite (`AuthorityList`/threshold) `Authority`, or only a single
  primitive key?** HIP-551 states *"Only Ed25519 and ECDSA(secp256k1) keys and hence signatures are
  currently supported"* for the batch key, and its prose only ever discusses a *single* key per inner
  transaction (*"the top-level `AtomicBatchTransactionBody` must be signed by all batchKey keys"*).
  It does **not** state whether a `KeyList`/`ThresholdKey` is accepted as a batch key. The V3 type
  does not restrict this — `batchKey` is an `Authority`, so a caller *can* construct
  `Authority.of(alice, bob)` (n-of-n) or `Authority.of(2, a, b, c)` (m-of-n) — but whether the network
  honours a composite batch key is unverified and a network-side concern; until confirmed, callers
  should assume a single Ed25519/ECDSA(secp256k1) key is the safe choice. (The Ed25519/ECDSA-only
  signature restriction is itself a network-side constraint, not modelled in the type.)

- **Execution order is modelled as list order but not explicitly guaranteed by HIP-551.** The
  `AtomicBatchTransactionBody.transactions` field is a `repeated Transaction` (order-preserving), and
  inner transactions execute as ordered preceding child transactions, so `list<PackedTransaction>`
  with list-order execution is the faithful model. The HIP does not, however, spell out an ordering
  guarantee in prose — if a strict guarantee is later confirmed (or denied), this note can be
  resolved.
