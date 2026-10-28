# Client API

This section defines the client API.

## Description

The client API is the API that will be used by the SDK to interact with the network.
A client defines a concrete network connection to a specific network with a specific operator account.

### External signing

A `TransactionSigner` signs transactions with a key the SDK does not hold — a cloud KMS (AWS KMS,
Google Cloud KMS, Azure Key Vault), HashiCorp Vault transit, a PKCS#11 HSM, or a hardware wallet.
Signing with such a key is I/O, so `signTransaction` is `@@async`. One key contributes one
signature per target node (see `consensusnode.transactions`), so the SDK calls the signer once per
`NodeBody`.

Every `TransactionSigner` implementation must follow this contract:

- **Input.** `body.bytes` is the serialized `TransactionBody` addressed to `body.node`, exactly as
  returned by `PackedTransaction.signableBodies()`. The signer signs these bytes and nothing else.
- **Digest.** The signer applies the digest of its key algorithm itself: Ed25519 signs the raw body
  bytes; ECDSA secp256k1 signs the Keccak-256 digest of the body bytes. Backends that cannot
  compute Keccak-256 (no cloud KMS offers it) receive the 32-byte digest computed locally by the
  signer.
- **Output encoding.** `NodeSignature.signature` is 64 bytes for both algorithms. For ECDSA
  secp256k1 that is `r || s` (32 bytes each, big-endian) with `s` in the lower half of the curve
  order; DER-encoded or high-`s` signatures returned by a backend must be converted by the signer.
- **Identity.** `NodeSignature.node` equals `body.node`, and `NodeSignature.publicKey` equals the
  signer's `publicKey`.
- **Concurrency.** The SDK may call `signTransaction` concurrently for the bodies of different
  target nodes (`@@threadSafe`).

## API Schema

```
namespace consensusnode.client
requires {AccountId, Network} from ledger
requires {NetworkSetting} from ledger.config
requires {PrivateKey, PublicKey} from keys
requires {NativeTokenUnit} from nativeToken

// Definition of an account that signs and pays for requests
Account {
    @@immutable accountId: AccountId // the account id of the operator
    @@immutable privateKey: PrivateKey // the private key of the operator
}

// A serialized TransactionBody for one target consensus node — the exact bytes a signer
// must sign. Bytes differ per node because each body carries the target node's nodeAccountID.
type NodeBody {
    @@immutable node: AccountId
    @@immutable bytes: bytes
}

type NodeSignature {
      @@immutable node: AccountId       // the consensus node's fee account
      @@immutable publicKey: PublicKey
      @@immutable signature: bytes      // 64 bytes; see "External signing" for the encoding
}

// Signs transactions with a key the SDK does not hold (KMS, HSM, Vault, hardware wallet).
// Implementations must follow the contract in "External signing" above.
abstraction TransactionSigner {

  @@immutable publicKey: PublicKey // the public key matching every signature this signer produces

  // Signs the TransactionBody addressed to body.node. Called once per target node, possibly
  // concurrently for different nodes.
  @@threadSafe
  @@async NodeSignature signTransaction(body: NodeBody)
}

// Common base for anything that is handed off to the consensus node network and
// produces a typed result — both queries (consensusnode.queries.Query) and
// packed transactions (consensusnode.transactions.PackedTransaction) extend this.
// Carries the shared retry-tuning knobs (max attempts, backoff window, per-attempt
// timeout) and the single submit() entry point. The SDK applies these settings
// when selecting a consensus node, retrying transient gRPC failures, and bounding
// the total wait.
abstraction Submittable<$$Result> {

    @@nullable maxAttempts: int32
    @@nullable maxBackoff: int64
    @@nullable minBackoff: int64
    @@nullable attemptTimeout: int64

    // Hand off to the network and return the typed result. Node selection,
    // retry, and any operation-specific protocol details (e.g. cost discovery
    // and payment for PaidQuery) are handled by the SDK transparently.
    @@async $$Result submit(client: HieroClient)
}

// The client API that will be used by the SDK to interact with the network
HieroClient<$$Unit extends NativeTokenUnit> {
    @@immutable operatorAccount: Account // the operator account
    @@immutable network: Network<$$Unit> // the network to connect to
    @@immutable transactionSigner: TransactionSigner // by default the operator account is used, but this allows to use an external signer for transactions
    // TO_BE_DEFINED_IN_FUTURE_VERSIONS
}

// factory methods of `HieroClient` that should be added to the namespace in the best language dependent way

@@static HieroClient<ANY> createClient(networkSettings: NetworkSetting, operatorAccount: Account)
@@static HieroClient<ANY> createClient(networkSettings: NetworkSetting, operatorAccount: Account, transactionSigner: TransactionSigner)
```

## Examples

The following example shows how to create a `HieroClient` instance:

```
AccountId accountId = ...;
PrivateKey privateKey = ...;
Account operatorAccount = new Account(accountId, privateKey);

NetworkSetting networkSettings = ...;

HieroClient client = HieroClient.createClient(networkSettings, operatorAccount);
```

The following example signs a transfer with a key held in a cloud KMS. For an ECDSA secp256k1 key,
the `TransactionSigner` implementation computes `keccak256(body.bytes)` locally, has the KMS sign
that digest, and converts the DER signature it gets back into the 64-byte low-`s` form (see
_External signing_):

```
HieroClient client = ...;
TransactionSigner kmsSigner = ...;           // wraps an ECDSA secp256k1 key in a cloud KMS
AccountId treasury = ...;                    // an account whose key is kmsSigner.publicKey
AccountId recipient = ...;
list<AccountId> nodes = ...;                 // two target nodes

PackedTransaction<...> packed = new TransferTransaction()
    .hbarTransfers([
        new HbarTransfer(treasury, NativeToken.of(-10, HBAR_UNIT)),
        new HbarTransfer(recipient, NativeToken.of(+10, HBAR_UNIT)),
    ])
    .sign(treasury, kmsSigner, nodes);       // two KMS calls: one per target node

packed.submit(client);
```

## Testing

Tests run against a local [solo](https://solo.hiero.org) network and are described as
language-agnostic Given / When / Then scenarios. See
[`guidelines/testing-guideline.md`](../../guidelines/testing-guideline.md) for the test
platform, the solo lifecycle, and the shared _"`HieroClient` connected to a solo network with a
funded operator account"_ fixture referenced below.

The scenarios use a _test signer_: a `TransactionSigner` over an in-memory key that follows the
_External signing_ contract and records every `NodeBody` it is asked to sign. It stands in for a
KMS or HSM, which the solo platform does not provide.

### `client/external-signer-signs-as-payer`

- **Given** a `HieroClient` connected to a solo network with a funded operator account, a test
  signer over a new Ed25519 key, and an account created by the operator with the test signer's
  `publicKey` as its key and a balance of 1 HBAR.
- **When** a `TransferTransaction` moving 1 tinybar from that account to the operator's account id
  is signed with `sign(accountId, testSigner, nodes)` for two target nodes and submitted.
- **Then** the receipt status is `SUCCESS`, the test signer was called exactly once per target
  node, and the recorded bodies equal the `signableBodies()` of the packed transaction.

### `client/external-ecdsa-signer-signs-as-payer`

- **Given** the same setup as `client/external-signer-signs-as-payer`, but with a test signer over
  a new ECDSA secp256k1 key.
- **When** the same transfer is signed with `sign(accountId, testSigner, nodes)` and submitted.
- **Then** the receipt status is `SUCCESS`, and every `NodeSignature` produced by the test signer
  is 64 bytes and verifies with `publicKey.verify(body.bytes, signature)` against its node's body.

## Questions & Comments

- The `TransactionSigner` contract above was validated with a
  lib that implements this API on top of the V2 JavaScript SDK, with AWS KMS and HashiCorp Vault
  signers (ported from [hiero-remote-signer](https://github.com/Jexsie/hiero-remote-signer)). Open
  points it surfaced:
  - **`Account` still requires a `PrivateKey`.** An operator whose key is in a KMS has no
    `PrivateKey`, so callers need both an `Account` and a separate `transactionSigner`. The package
    models `Account { accountId, signer: TransactionSigner }`, where an in-memory key is one
    `TransactionSigner` implementation. That removes `HieroClient.transactionSigner`, the
    three-argument `createClient`, and the `sign(payerId, signer, nodes)` /
    `signForBatch(payerId, signer, batchKey)` tiers.
  - **Signing cost scales with the number of target nodes.** A remote signer makes one billed,
    rate-limited, audit-logged backend call per target node and key. Should `HieroClient` own node
    selection (callers of `pack(payer, nodes)` currently have no API to obtain `nodes`), and should
    `TransactionSigner` offer an optional batch method such as
    `@@async list<NodeSignature> signTransactions(bodies: list<NodeBody>)`?
  - **Lifecycle.** A PKCS#11 signer holds a session that must be closed. Should
    `TransactionSigner` be closeable, or is that left to implementations?
  - **Wire format.** Out-of-process signing ships `NodeBody` values to a signing service and
    `NodeSignature` values back, but neither has a defined serialization (unlike
    `PackedTransaction.toBytes()`), so every pipeline invents its own.
