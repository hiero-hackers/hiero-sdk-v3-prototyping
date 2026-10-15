# Transactions API

This namespace defines the lifecycle of transactions submitted to the consensus node, from a mutable
builder through a wire-ready, signed payload to an executed response.

## Description

A transaction passes through two clearly separated states, modelled as two distinct types:

1. **`Transaction<$$Receipt, $$Self>`** — a mutable builder. Concrete subtypes such as
   `AccountCreateTransaction` expose service-specific fields; the generic transaction-level fields
   (`maxTransactionFee`, `validDuration`, `memo`) are inherited from `Transaction`. A `Transaction`
   is not yet bound to a payer, target nodes, or a `TransactionId`.

2. **`PackedTransaction<$$Receipt, $$Transaction>`** — a frozen, serializable, wire-ready
   transaction. Packing binds the transaction to a payer, a set of target consensus nodes, and a
   freshly generated `TransactionId`. From this point on the transaction content cannot change;
   only further `NodeSignature` entries can be added (multi-sig).

Signatures are computed over the exact serialized transaction bytes, so a transaction must not
change after it has been signed. `Transaction` is the editable form; `PackedTransaction` is the
form that has been *packed* for the wire, carrying its target nodes, transaction id, and the
signatures collected so far.

### Lifecycle

```
  build              pack              sign               submit
   ──▶ Transaction ──▶ PackedTransaction ──▶ PackedTransaction ──▶ Response
                          (0 signatures)        (n signatures)
```

- **build** — construct a concrete `Transaction` subtype and populate its service-specific fields.
- **pack** — bind to a payer `Account`, a list of target nodes, and generate a `TransactionId`.
  Produces a `PackedTransaction` with an empty `nodeSignatures` list. This is the explicit hand-off
  point between the editable and the immutable world.
- **sign** — append one or more `NodeSignature` entries. Several mechanisms are supported
  (see *Signing mechanisms* below).
- **submit** — hand the packed transaction to one of the target consensus nodes and obtain a
  `Response`. The SDK chooses the node; the caller does not steer it. Actual ledger execution
  happens on the network asynchronously — the `Response` is the submission acknowledgment, not the
  execution result. Use `Response.queryReceipt()` / `Response.queryRecord()` to read the outcome
  once consensus is reached. `submit(client)` and the retry-tuning fields are inherited from
  `Submittable`.

A `PackedTransaction` may be serialized via `toBytes()` at any point, shipped to another process or
machine, re-loaded via the static `fromBytes(...)` factory, and have further signatures appended on
the receiving side. The transaction id, the target nodes, and the already-collected signatures all
survive the round-trip.

### Signing mechanisms

There is one convenience method per common case and one data-oriented option for out-of-process
workflows:

| Caller has | Method |
|---|---|
| A configured `HieroClient` with operator | `Transaction.signWithOperator(client)` (or `signWithOperatorAndSubmit(client)` for fire-and-forget) |
| A single `Account` that pays *and* signs | `Transaction.sign(payer, nodes)` |
| A separate payer address and a `TransactionSigner` (HSM, hardware wallet, paymaster) | `Transaction.sign(payerId, signer, nodes)` |
| An already-packed transaction that needs another in-process signature (multi-sig) | `PackedTransaction.sign(account)` or `PackedTransaction.sign(signer)` |
| Out-of-process signing (async pipeline, multi-party, audit archival, raw HSM bytes) | `PackedTransaction.signableBodies()` + `PackedTransaction.sign(signatures)` |

Signature ordering is irrelevant — `NodeSignature` entries form a set; the consensus node accepts
them in any order. The packed transaction can also be built first via `Transaction.pack(...)`
without any signature and shipped to one or more signers downstream.

Any transaction can also be prepared as an inner transaction of a `BatchTransaction` through
`packForBatch(...)`, `signForBatchWithOperator(...)` and `signForBatch(...)`.

### Why one signing key contributes N signatures

Each `TransactionBody` carries a `nodeAccountID` field naming the consensus node it was prepared
for. The network rejects bodies addressed to a different node (`INVALID_NODE_ACCOUNT`) — a
protocol-level replay protection. When a transaction is packed for N target nodes, N distinct
`TransactionBody` byte sequences are produced, differing only in that field. Consequently each
signing key contributes N signatures, one per `(node, body)` pair. This is encapsulated in the
`NodeSignature` type. `signableBodies()` exposes the matched `NodeBody` payloads for external
signers.

### TransactionId generation

The `TransactionId` (payer + `validStart` timestamp + `scheduled` flag + `nonce`) is generated
during `pack(...)`. The fields are set as follows:

- `payer` — the address of the `Account` passed to `pack(...)`.
- `validStart` — "now minus a small drift offset", set by the SDK.
- `scheduled` — always `false`; the consensus node sets `true` when materializing a
  `ScheduleCreate`.
- `nonce` — always `0`; the consensus node uses non-zero values only for child transactions
  spawned by smart-contract execution.

The `TransactionId` cannot be set by the caller.

## Design Notes

- **Why two types.** Signing is a function over **exact byte content**: a signature is computed
  over the serialized `TransactionBody`, which includes the target node's `nodeAccountID`. Allowing
  the body to mutate after signing would silently invalidate every previously collected signature and
  break round-trips through serialization. Modelling the two states as two types makes this constraint
  visible in the type system rather than relying on documentation or runtime checks. See ADR-0001 for
  the full rationale, including why this is preferred over a single mutable type or a `freeze()` step
  on `Transaction`.
- `submit(client)` is inherited from `Submittable` (defined in `consensusnode.client`), which also
  carries the shared retry-tuning fields.
- Concrete transaction subtypes are defined under sub-namespaces such as
  `consensusnode.transactions.accounts`. The `BatchTransaction` container is specified in
  [`transactions-batch.md`](transactions-batch.md).
- **`TransactionId` is never user-settable** through this API. Custom validity windows or
  deterministic ids for testing must be modelled by a different mechanism if needed in the future.
- **`Record.parentConsensusTimestamp` keying.** HAPI keys the child-to-parent link on the consensus
  timestamp rather than a parent `TransactionId` because the consensus timestamp is the canonical,
  always-unique key of the record stream (children sit at nanosecond offsets adjacent to the parent),
  and not every child has an independent id — node-synthesized children share the parent's payer +
  validStart and are disambiguated only by a nonce. Batch inner transactions are specified in
  `consensusnode.transactions.batch`.
- **`getResponse(...)` resolution.** The SDK resolves the matching `TransactionSupport`
  (`consensusnode.transactions.spi`) for the given transaction type to parse the proto receipt/record
  into the typed `$$Receipt`.
- **`packForBatch(...)` and `batchKey`.** `batchKey` is a `TransactionBody` field that must be fixed
  before signing, so it is taken as a required parameter of the batch-pack entry points rather than a
  free-standing build-phase field: this makes "an inner transaction always has a batchKey" and "a
  non-batch transaction never has one" structural guarantees (illegal states unrepresentable) instead
  of preconditions the network has to reject. Which transaction *types* may be batched remains a
  network-side policy the SDK cannot know; see [`transactions-batch.md`](transactions-batch.md).
  `signForBatchWithOperator(client, batchKey)` is the V3 equivalent of v2's `batchify(client, batchKey)`.

## API Schema

```
namespace consensusnode.transactions
requires {AccountId, TransactionId} from ledger
requires {NativeToken, ExchangeRate} from nativeToken
requires {Authority} from authority
requires {Account, HieroClient, NodeSignature, Submittable, TransactionSigner} from consensusnode.client

// The status of a transaction, identified by a numeric code. The set of statuses is open: custom
// services on the consensus node can define their own transaction types and status codes.
abstraction TransactionStatus {
  @@immutable code:int32 // the numeric status code reported by the consensus node
}

// The status codes used by the built-in services of the consensus node. The codes are the HAPI
// ResponseCodeEnum values.
enum BasicTransactionStatus(code: int32) extends TransactionStatus {
    OK(0)
    INVALID_TRANSACTION(1)
    PAYER_ACCOUNT_NOT_FOUND(2)
    // not complete yet: further status codes are still to be added here

    GRPC_WEB_PROXY_NOT_SUPPORTED(399)
}

// A serialized TransactionBody for one target consensus node — the exact bytes an external signer
// must sign. Bytes differ per node because each body carries the target node's nodeAccountID.
type NodeBody {
    @@immutable node: AccountId
    @@immutable bytes: bytes
}

// Base type of all transactions: a mutable builder that is packed and signed into a PackedTransaction
// before it is submitted. The first type parameter is the receipt type; the second is the concrete
// transaction type itself (e.g. AccountCreateTransaction extends
// Transaction<AccountCreateReceipt, AccountCreateTransaction>), so that pack and sign return a
// PackedTransaction that knows the concrete transaction type.
abstraction Transaction<$$Receipt extends Receipt, $$Self extends Transaction<$$Receipt, $$Self>> {
  
  @@nullable maxTransactionFee: NativeToken<ANY, ANY>
  @@nullable validDuration: seconds
  @@nullable memo: string

  PackedTransaction<$$Receipt, $$Self> pack(payer: Account, nodes: list<AccountId>)  
    
  PackedTransaction<$$Receipt, $$Self> signWithOperator(client: HieroClient<ANY>)
  
  PackedTransaction<$$Receipt, $$Self> sign(payer: Account, nodes: list<AccountId>)
  
  PackedTransaction<$$Receipt, $$Self> sign(payerId: AccountId, signer: TransactionSigner, nodes: list<AccountId>)
  
  @@async Response<$$Receipt> signWithOperatorAndSubmit(client: HieroClient<ANY>)

  // Packs this transaction as the *inner* transaction of a BatchTransaction (HIP-551), without
  // signing it. Unlike pack(payer, nodes), this produces a single TransactionBody addressed to no
  // consensus node (nodeAccountID = 0.0.0) instead of one body per target node — an inner batch
  // transaction is never submitted to a node on its own; it is embedded into
  // BatchTransaction.innerTransactions and executed by the network as part of the batch. The
  // returned PackedTransaction still carries its own TransactionId (payer + validStart); further
  // signatures use PackedTransaction.sign(...) / signableBodies().
  //
  // batchKey names the Authority that must sign the *outer* BatchTransaction for this inner
  // transaction to execute — the inner author's controlled opt-in to being batched. Whether a
  // transaction type may be batched at all is decided by the network.
  PackedTransaction<$$Receipt, $$Self> packForBatch(payer: Account, batchKey: Authority)

  // The batch counterparts of the non-batch signing tiers, each mirroring its sign(...) sibling
  // but without a nodes parameter (an inner batch transaction has a single body, nodeAccountID =
  // 0.0.0) and without an ...AndSubmit form (it is never submitted on its own). Each takes the
  // required batchKey, exactly as packForBatch.

  // Packs this transaction for a batch with the client's operator as payer and adds the operator's
  // signature. The batch counterpart of signWithOperator(client).
  PackedTransaction<$$Receipt, $$Self> signForBatchWithOperator(client: HieroClient<ANY>, batchKey: Authority)

  // Packs this transaction for a batch with the given Account as payer and adds its signature. The
  // batch counterpart of sign(payer, nodes).
  PackedTransaction<$$Receipt, $$Self> signForBatch(payer: Account, batchKey: Authority)

  // Packs this transaction for a batch with payerId as payer and signs it with the given
  // TransactionSigner (HSM, hardware wallet, paymaster). The batch counterpart of
  // sign(payerId, signer, nodes).
  PackedTransaction<$$Receipt, $$Self> signForBatch(payerId: AccountId, signer: TransactionSigner, batchKey: Authority)

}

// A packed, wire-ready transaction bound to a payer, a set of target nodes and a TransactionId. Its
// content cannot change anymore; signatures can be added. Submitting it yields a Response.
// Retry-tuning fields (maxAttempts, maxBackoff, minBackoff, attemptTimeout) and the
// submit(client) method are inherited from Submittable.
abstraction PackedTransaction<$$Receipt extends Receipt, $$Transaction extends Transaction<$$Receipt, $$Transaction>>
        extends Submittable<Response<$$Receipt>> {

  @@immutable transactionId: TransactionId
  @@immutable nodeSignatures: list<NodeSignature> 

  // Adds the signature of the account's private key for every target node. A key that has already signed
  // this transaction is not added again.
  PackedTransaction<$$Receipt, $$Transaction> sign(account: Account)

  // Adds the signatures of the signer for every target node. A key that has already signed this transaction is
  // not added again.
  PackedTransaction<$$Receipt, $$Transaction> sign(signer: TransactionSigner)

  // Returns the serialized TransactionBody bytes for every target node. Used by out-of-process
  // signing flows (raw HSMs, async signing pipelines, multi-party coordination, audit archival)
  // that cannot be wrapped behind a synchronous TransactionSigner. The returned list has one
  // NodeBody per target node, in the order in which the nodes were given.
  list<NodeBody> signableBodies()

  // Attaches externally-produced NodeSignatures to this PackedTransaction and returns a new
  // PackedTransaction containing them. The provided list must contain one signature per node
  // returned by signableBodies() for the same PublicKey; otherwise submit() will fail with
  // INVALID_SIGNATURE on the chosen node.
  // Throws if a signature references a node that is not a target node of this transaction, or if
  // signatures for any target node are missing.
  PackedTransaction<$$Receipt, $$Transaction> sign(signatures: list<NodeSignature>)

  bytes toBytes()

  // Loads a PackedTransaction from its serialized form (see toBytes()). The concrete transaction type is only known
  // at runtime, so the result is typed with wildcards; callers check the concrete type themselves.
  @@static PackedTransaction<ANY, ANY> fromBytes(bytes: bytes)
}

Response<$$Receipt extends Receipt> {
  @@immutable transactionId: TransactionId // the id of the transaction

  @@async $$Receipt queryReceipt()          // query for the receipt of the transaction
  @@async Record<$$Receipt> queryRecord()   // query for the record of the transaction
}

abstraction Receipt {
  @@immutable transactionId: TransactionId     // the id of the transaction
  @@immutable status: TransactionStatus        // the status of the transaction
  @@immutable exchangeRate: ExchangeRate     // the exchange rate at the time of the transaction
  @@immutable nextExchangeRate: ExchangeRate // the next exchange rate
}

Record<$$Receipt extends Receipt> {
  @@immutable transactionId: TransactionId          // the id of the transaction
  @@immutable consensusTimestamp: zonedDateTime      // the consensus time of the transaction
  @@immutable receipt: $$Receipt                     // the typed receipt of the transaction

  // For a child/triggered transaction — a batch inner transaction, a scheduled transaction, or a
  // contract-spawned child — the consensusTimestamp of the parent that spawned it; absent for an
  // ordinary top-level transaction. This is the link from a child record back to its parent.
  @@immutable @@nullable parentConsensusTimestamp: zonedDateTime
}

// Reconstructs a client-bound Response for a transaction that was submitted elsewhere — e.g. the
// inner transaction of a schedule (identified by ScheduleCreateReceipt.scheduledTransactionId),
// which executes on the network without the caller ever holding a Response for it.
//
// `transactionType` is the concrete transaction type (e.g. TransferTransaction); it determines the
// receipt type, so the returned Response is typed. This call makes no network request — querying
// happens lazily through the returned Response's queryReceipt() / queryRecord(), exactly as for a
// Response from submit().
@@static Response<$$Receipt> getResponse<$$Receipt extends Receipt>(transactionId: TransactionId,
        transactionType: type<Transaction<$$Receipt, ANY>>, client: HieroClient<ANY>)
```

## Examples

The examples below use a hypothetical `AccountCreateTransaction` (a concrete subtype of
`Transaction`, defined under `consensusnode.transactions.accounts`) to illustrate each flow. The
patterns apply to any concrete transaction type.

### 1. Operator does everything

The most common path. The `HieroClient` provides the operator account, the target nodes, and the
network routing.

```
HieroClient client = ...;

Response<AccountCreateReceipt> response = new AccountCreateTransaction()
    .initialBalance(...)
    .authority(...)
    .signWithOperatorAndSubmit(client);

AccountCreateReceipt receipt = response.queryReceipt();
AccountId newAccountId = receipt.accountId;
```

### 2. Distinct payer, single signature

A non-operator `Account` pays for and signs the transaction. The caller supplies the target nodes
explicitly.

```
HieroClient client = ...;
Account payer = ...;            // not necessarily the operator
list<AccountId> nodes = client.ledger.networkSetting().getConsensusNodes()
                            .map(n -> n.address);

PackedTransaction<...> packed = new AccountCreateTransaction()
    .authority(...)
    .sign(payer, nodes);

Response<AccountCreateReceipt> response = packed.submit(client);
```

### 3. Paymaster pattern (sponsor pays, user signs the operation)

The sponsor's `Account` is the payer; the user's `TransactionSigner` (an HSM or hardware wallet)
adds the operation signature. The sponsor signs after the fact on the resulting
`PackedTransaction` — because the protocol requires the payer to sign as well.

```
Account sponsor = ...;
TransactionSigner userSigner = ...;     // wraps HSM / Ledger / etc.
list<AccountId> nodes = ...;

PackedTransaction<...> packed = new TransferTransaction()
    .addHbarTransfer(...)
    .sign(sponsor.accountId, userSigner, nodes)     // user's operation signature
    .sign(sponsor);                                  // sponsor's payer signature

packed.submit(client);
```

### 4. Multi-sig collected across processes

A treasury account guarded by a `KeyList` is co-signed by three independent custodians. The
`PackedTransaction` is built once, serialized, and shipped to each custodian for signing.

```
// Coordinator builds and packs (no signatures yet):
PackedTransaction<...> packed = new TransferTransaction()
    .addHbarTransfer(treasury, -amount)
    .addHbarTransfer(recipient, +amount)
    .pack(treasuryAccount, nodes);

bytes payload = packed.toBytes();
// payload travels to custodian A (e.g. via signed HTTPS):

// Custodian A:
PackedTransaction<...> received = PackedTransaction.fromBytes(payload);
bytes signedA = received.sign(custodianA).toBytes();
// signedA travels back; coordinator forwards to custodian B, etc.

// Coordinator finalises:
PackedTransaction<...> finalTx = PackedTransaction.fromBytes(signedC);
finalTx.submit(client);
```

### 5. Out-of-process / async signing (server-side pipeline)

An online server packs and persists the transaction, dispatches the signable bodies to a remote
signing service via a queue, and resumes once signatures return — possibly minutes later, possibly
on a different worker.

```
// Web request handler:
PackedTransaction<...> packed = new ContractCallTransaction()
    .contractId(...)
    .functionParameters(...)
    .pack(userAccount, nodes);

db.store(jobId, packed.toBytes());
queue.publish(SigningJob(jobId, userPublicKey, packed.signableBodies()));
return Accepted(jobId);

// Later, on a worker, after signatures arrive via webhook:
PackedTransaction<...> resumed = PackedTransaction.fromBytes(db.load(jobId));
list<NodeSignature> signatures = ...;      // produced by remote signer
PackedTransaction<...> signed = resumed.sign(signatures);
signed.submit(client);
```

## Testing

Offline tests of packing and signing, written as given / when / then. They use the smallest transaction type,
`PrngTransaction`, and the default account (an Ed25519 key) as payer; nothing is submitted. The behaviour is the
one of the HAPI protocol — one body per target node, signatures over the body bytes — and is tested the same way by
the current SDKs (see `docs/sdk-unit-test-analysis.md`); the comment above each test names where.

```tests
// Source: HAPI (one TransactionBody per target node, each with its nodeAccountID); the Java, Go, JavaScript and
// Python SDKs.
test "packing gives one body per target node, in the order of the nodes" {
    // given
    let payer = DEFAULT(Account)
    let node3 = AccountId.fromString(value: "0.0.3")
    let node4 = AccountId.fromString(value: "0.0.4")

    // when
    let packed = consensusnode.transactions.utility.PrngTransaction{}.pack(payer: payer, nodes: [node3, node4])

    // then
    let bodies = packed.signableBodies()
    assertEquals(expected: 2, actual: length(value: bodies))
    assertEquals(expected: node3, actual: bodies[0].node)
    assertEquals(expected: node4, actual: bodies[1].node)
    assertNotEquals(unexpected: bodies[0].bytes, actual: bodies[1].bytes, message: "each body names its node")
    assertEquals(expected: payer.accountId, actual: packed.transactionId.accountId)
    assertEquals(expected: 0, actual: length(value: packed.nodeSignatures))
}

// Source: HAPI (a signature covers the bodyBytes of its SignedTransaction); the Java, Python, C++ and Go SDKs.
test "signing adds one signature per node that verifies over the body of that node" {
    // given
    let payer = DEFAULT(Account)
    let nodes = [AccountId.fromString(value: "0.0.3"), AccountId.fromString(value: "0.0.4")]

    // when
    let packed = consensusnode.transactions.utility.PrngTransaction{}.sign(payer: payer, nodes: nodes)

    // then
    assertEquals(expected: 2, actual: length(value: packed.nodeSignatures))
    for signature in packed.nodeSignatures {
        assertEquals(expected: payer.privateKey.createPublicKey(), actual: signature.publicKey)
        assertEquals(expected: 64, actual: length(value: signature.signature))
        for body in packed.signableBodies() {
            if body.node == signature.node {
                assertTrue(condition: signature.publicKey.verify(message: body.bytes, signature: signature.signature))
            }
        }
    }
}

// Source: the Python, Java and Go SDKs (a key in a SignatureMap only once).
test "signing again with the same key adds no signature" {
    // given
    let payer = DEFAULT(Account)
    let packed = consensusnode.transactions.utility.PrngTransaction{}
            .sign(payer: payer, nodes: [AccountId.fromString(value: "0.0.3")])

    // when
    let again = packed.sign(account: payer)

    // then
    assertEquals(expected: 1, actual: length(value: again.nodeSignatures))
}

// Source: the JavaScript and Python SDKs (Ed25519 and ECDSA signatures on one transaction). The second key is the
// secp256k1 key of the tests of keys.md.
test "an Ed25519 and a secp256k1 key both sign every body" {
    // given
    let payer = DEFAULT(Account)
    let second = Account{accountId: AccountId.fromString(value: "0.0.1002"),
            privateKey: keys.createPrivateKey(algorithm: keys.KeyAlgorithm.ECDSA, encoding: keys.ByteImportEncoding.HEX,
                    value: "8776c6b831a1b61ac10dac0304a2843de4716f54b1919bb91a2685d0fe3f3048")}
    let packed = consensusnode.transactions.utility.PrngTransaction{}
            .sign(payer: payer, nodes: [AccountId.fromString(value: "0.0.3"), AccountId.fromString(value: "0.0.4")])

    // when
    let signed = packed.sign(account: second)

    // then
    assertEquals(expected: 4, actual: length(value: signed.nodeSignatures))
    for signature in signed.nodeSignatures {
        for body in signed.signableBodies() {
            if body.node == signature.node {
                assertTrue(condition: signature.publicKey.verify(message: body.bytes, signature: signature.signature))
            }
        }
    }
}

// Source: the Rust, Python, JavaScript, Go and Swift SDKs (the body bytes stay byte-exact, otherwise the signatures
// break).
test "a signed transaction survives toBytes and fromBytes unchanged" {
    // given
    let packed = consensusnode.transactions.utility.PrngTransaction{}
            .sign(payer: DEFAULT(Account), nodes: [AccountId.fromString(value: "0.0.3"), AccountId.fromString(value: "0.0.4")])
    let bytes = packed.toBytes()

    // when
    let restored = PackedTransaction.fromBytes(bytes: bytes)

    // then
    assertEquals(expected: bytes, actual: restored.toBytes())
    assertEquals(expected: packed.transactionId.accountId, actual: restored.transactionId.accountId)
    assertEquals(expected: packed.signableBodies(), actual: restored.signableBodies())
    assertEquals(expected: length(value: packed.nodeSignatures), actual: length(value: restored.nodeSignatures))
}

// Source: the multi-party flow of the Python SDK: a transaction is packed by one party and signed by another.
test "a restored transaction can be signed by another party" {
    // given
    let payer = DEFAULT(Account)
    let bytes = consensusnode.transactions.utility.PrngTransaction{}
            .sign(payer: payer, nodes: [AccountId.fromString(value: "0.0.3")]).toBytes()
    let other = Account{accountId: AccountId.fromString(value: "0.0.1002"),
            privateKey: keys.createPrivateKey(algorithm: keys.KeyAlgorithm.ECDSA, encoding: keys.ByteImportEncoding.HEX,
                    value: "8776c6b831a1b61ac10dac0304a2843de4716f54b1919bb91a2685d0fe3f3048")}

    // when
    let signed = PackedTransaction.fromBytes(bytes: bytes).sign(account: other)

    // then
    assertEquals(expected: 2, actual: length(value: signed.nodeSignatures))
    for signature in signed.nodeSignatures {
        assertTrue(condition: signature.publicKey.verify(message: signed.signableBodies()[0].bytes,
                signature: signature.signature))
    }
}

// Source: the out-of-process signing of the Rust SDK and the JavaScript SDK (sign the body bytes, attach the
// signatures).
test "signatures made outside the SDK are attached to their bodies" {
    // given
    let payer = DEFAULT(Account)
    let packed = consensusnode.transactions.utility.PrngTransaction{}
            .pack(payer: payer, nodes: [AccountId.fromString(value: "0.0.3")])
    let body = packed.signableBodies()[0]
    let external = NodeSignature{node: body.node, publicKey: payer.privateKey.createPublicKey(),
            signature: payer.privateKey.sign(message: body.bytes)}

    // when
    let signed = packed.sign(signatures: [external])

    // then
    assertEquals(expected: [external], actual: signed.nodeSignatures)
    assertEquals(expected: packed.transactionId.accountId, actual: signed.transactionId.accountId)
}

// Source: HIP-551 (an inner batch transaction is addressed to node 0.0.0); the Python SDK.
test "a transaction packed for a batch has one body for node 0.0.0" {
    // given
    let payer = DEFAULT(Account)
    let batchKey = authority.PublicKeyAuthority{publicKey: payer.privateKey.createPublicKey()}

    // when
    let packed = consensusnode.transactions.utility.PrngTransaction{}.packForBatch(payer: payer, batchKey: batchKey)

    // then
    assertEquals(expected: 1, actual: length(value: packed.signableBodies()))
    assertEquals(expected: AccountId.fromString(value: "0.0.0"), actual: packed.signableBodies()[0].node)
    assertEquals(expected: payer.accountId, actual: packed.transactionId.accountId)
}

// Source: a fixture of the JavaScript SDK (a TransactionList with one body for node 0.0.3, payer 0.0.1234, valid
// start 4891363200 = 2125-01-01, memo "Transaction with validStart in 2125"); a regression test for valid starts
// beyond 32-bit seconds.
test "a transaction of another SDK is read with a valid start in 2125" {
    // given
    let bytes = hex(text: "0acc012ac9010a5f0a0d0a0608808fb19c12120318d209120218031880c2d72f2202087832235472616e73616374"
            + "696f6e20776974682076616c6964537461727420696e2032313235721c0a1a0a0b0a0318d20910ffa7d6b9070a0b0a0318ae2c1080"
            + "a8d6b90712660a640a205afd59dfc4401b63272c050535457495841ba2d414891cb8609549d3f08ef6671a40a82b1257b5476a98c6"
            + "5fe73e802150e9526eed77c11b377da642893e0004db096c258636a47327d986084a5044409220d68dde2ea34b1b1b097b39129d83d302")

    // when
    let transaction = PackedTransaction.fromBytes(bytes: bytes)

    // then
    assertEquals(expected: AccountId.fromString(value: "0.0.1234"), actual: transaction.transactionId.accountId)
    assertTrue(condition: startsWith(value: transaction.transactionId.toString(), prefix: "0.0.1234@4891363200."))
    assertEquals(expected: 1, actual: length(value: transaction.signableBodies()))
    assertEquals(expected: AccountId.fromString(value: "0.0.3"), actual: transaction.signableBodies()[0].node)
    assertEquals(expected: 1, actual: length(value: transaction.nodeSignatures))
}

// Source: ResponseCodeEnum of the HAPI (response_code.proto).
test "the basic transaction statuses have the codes of the protocol" {
    assertEquals(expected: 0, actual: BasicTransactionStatus.OK.code)
    assertEquals(expected: 1, actual: BasicTransactionStatus.INVALID_TRANSACTION.code)
    assertEquals(expected: 2, actual: BasicTransactionStatus.PAYER_ACCOUNT_NOT_FOUND.code)
    assertEquals(expected: 399, actual: BasicTransactionStatus.GRPC_WEB_PROXY_NOT_SUPPORTED.code)
}
```

## Questions & Comments

- **`Response` cannot query anything.** `Response<$$Receipt>` holds nothing but a `TransactionId`, yet
  `queryReceipt()` and `queryRecord()` have to reach the consensus node. The type carries neither the
  client it was created from nor the node the transaction went to, so an implementation cannot answer
  these calls from the object alone.

  Found while implementing `createAccount` against the TCK; the spike works around it with a
  process-wide registry from `TransactionId` to the submitting client, which also means a `Response`
  cannot be deserialized in another process. Options: give `Response` a reference to its client, or move
  the queries to the client (`client.queryReceipt(transactionId)`).

- **`TransactionId.generateTransactionId(payer: Address)` takes the wrong type.** A `TransactionId`
  holds an `AccountId` and the payer of a transaction is an account, but the factory takes an `Address`.
  `Address` is the numeric-only form, so an account addressed by EVM address or key alias cannot be a
  payer through this method, and an implementation has to convert.

- **`BasicTransactionStatus` has no `SUCCESS`.** The enum defines `OK(0)`, `INVALID_TRANSACTION(1)`,
  `PAYER_ACCOUNT_NOT_FOUND(2)` and `GRPC_WEB_PROXY_NOT_SUPPORTED(399)`, but the status a successful
  transaction reports in HAPI is `SUCCESS` with code 22. Every caller that wants to know whether a
  transaction succeeded has to compare against a hard-coded 22 — the TCK runtime does exactly that.
  `TransactionStatus` is deliberately open (an `int32` code, for custom services), so the question is
  which statuses the spec itself should name.
- **`setRegenerateTransactionId(boolean)` is intentionally NOT part of V3.** In v2 the SDK
  silently regenerates a `TransactionId` (and retries) when the network rejects a transaction
  with `TRANSACTION_EXPIRED` or `INVALID_TRANSACTION_DURATION`. This is **incompatible with the
  V3 transaction model** and will not be re-introduced.

  The fundamental problem: every `NodeSignature` on a `PackedTransaction` signs the serialized
  `TransactionBody` bytes, and the body carries the `TransactionId`. Regenerating the id
  therefore changes the body bytes, which silently invalidates every signature already
  collected. In V3, signing is modelled around exactly this property — the
  `Transaction` → `PackedTransaction` split (ADR-0001) exists so that the byte-stable signing
  surface is visible in the type system, and the offline / multi-party / HSM flows documented
  above (`signableBodies()`, `sign(list<NodeSignature>)`, `fromBytes(...)`) all assume the
  packed bytes do not mutate.

  A "regenerate and retry" toggle on top of those flows would either:
  - silently drop the previously collected signatures (data loss the user did not consent to —
    the custodians who already signed never agreed to a different `TransactionId`), or
  - go behind the type-system guarantee that `PackedTransaction.toBytes()` is the canonical
    payload (every consumer who persisted a `toBytes()` snapshot would now diverge from the
    submitted one).

  Neither is acceptable. The V3 contract for `TRANSACTION_EXPIRED` is therefore explicit:
  callers re-build (`Transaction.pack(...)`) and re-collect signatures. A future high-level
  helper at the `enterprise.service.*` layer **may** wrap "pack + sign with operator + submit;
  on TRANSACTION_EXPIRED repeat the whole pack+sign cycle" for the single-signer case where
  the loss-of-signatures problem is moot — but that lives above this layer, not as a flag on
  `Transaction`. Tracked in
  [`missing-features.md`](../../docs/missing-features.md) section 1.9.

- **`packForBatch(...)` and the `signForBatch(...)` tiers live on the base `Transaction` abstraction
  (not on a `BatchTransaction` subtype) because *any* transaction may be an inner member of a batch
  (HIP-551).** `packForBatch(...)` is a distinct pack entry point rather than an overload of
  `pack(payer, nodes)` because an inner batch transaction produces a *single* body addressed to no
  node (`nodeAccountID = 0.0.0`), so the per-node body fan-out and the `nodes` parameter do not apply.
  The `batchKey` (the `Authority` that must sign the outer batch) is a `TransactionBody` field, so —
  exactly like the `setRegenerateTransactionId` reasoning above — it must be fixed before signing.
  It is taken as a **required parameter** of these batch-pack methods rather than modelled as a
  free-standing build-phase field: doing so makes "an inner transaction always carries a `batchKey`"
  and "a non-batch transaction never carries one" structural guarantees of the type system rather
  than preconditions the network must reject. An earlier draft modelled `batchKey` as a `@@nullable`
  field on `Transaction` with a `missing-batch-key-error` thrown by `packForBatch`; folding it into
  the parameter removes both the nullable field and the error. The `BatchTransaction` container
  itself is specified in [`transactions-batch.md`](transactions-batch.md).

- **Errors of `PackedTransaction.sign(signatures)` are only documented in prose.** The method
  comment used to list `@@throws(unknown-node-error)` (a signature references a node not in the
  target nodes) and `@@throws(incomplete-signatures-error)` (signatures for a target node are
  missing) inside the comment, but the declaration does not carry these `@@throws` annotations.
  Should they be added to the schema?

- **How does a caller learn the status of a rejected transaction?** Neither `submit(client)` (the node
  rejects the transaction in precheck) nor `Response.queryReceipt()` (the transaction reached consensus
  but failed) declares a `@@throws` with an error that carries the `TransactionStatus`. V2 throws
  `PrecheckStatusException` / `ReceiptStatusException` with the status, and callers need it to tell e.g.
  `INSUFFICIENT_PAYER_BALANCE` from `INVALID_SIGNATURE`; the TCK server
  ([`tck-binding.md`](../../docs/tck-binding.md)) must answer such requests with the status name. Does
  `queryReceipt()` return the receipt of a failed transaction (status other than `SUCCESS`) and only
  `submit` throw, or do both throw an error with a `status` attribute? Until `BasicTransactionStatus`
  lists `SUCCESS`, the TCK server compares the receipt status with the HAPI code 22.
