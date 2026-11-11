# CLPR Transactions API

This namespace defines the transactions of the Cross Ledger Protocol (CLPR) service of the consensus node: establishing
and closing Channels, registering and deregistering Connectors, submitting bundles of messages from a peer ledger and
updating the CLPR limits of the ledger.

## Description

### Channels

A **Channel** connects this ledger with one peer ledger. Both ledgers know it by the same 32-byte Channel ID. A Channel
is established in two phases, so that nobody who watches the first phase can claim the same Channel ID first:

1. **Commit** — `ClprRegisterChannelTransaction` registers only an ownership commitment,
   `keccak256(channelId || publicKey)`. It reveals neither the Channel ID nor the key. Compute the commitment with
   `ClprRegisterChannelTransaction.computeOwnershipCommitment(channelId, publicKey)`. Submit the same commitment to both
   ledgers.
2. **Reveal** — `ClprCompleteChannelTransaction` reveals the Channel ID and the public key, proves the ownership with a
   signature, names the verifier contract that authenticates the peer ledger, and passes the proofs of the peer's
   configuration and endpoint manifest (read them with the queries of `consensusnode.clpr.queries` on the peer ledger).
   The network checks the commitment and the signature, lets the verifier check the proofs, and activates the Channel.
   Do the same on the peer ledger with the proofs of this ledger.

Create the ownership proof with `ClprChannelOwnershipProof.create(channelId, privateKey)`, using the key pair of the
commitment. The SDK encodes the public key, signs and records the signature scheme; the private key is not kept.

`ClprCloseChannelTransaction` closes a Channel: an `ACTIVE` or `PAUSED` Channel enters `CLOSING`, or `DRAINED` if all
its outbound messages are acknowledged; a `DRAINED` Channel becomes `CLOSED`. With the ownership commitment it also
removes a registration that was never completed.

### Connectors

A **Connector** authorizes and pays for the execution of the messages it sends over a Channel; it is identified by its
Channel ID together with its 32-byte Connector ID. It is registered in the same two phases:

1. **Commit** — `ClprRegisterConnectorTransaction` registers the commitment `keccak256(connectorId || publicKey)`, where
   `connectorId = keccak256(channelId || publicKey || salt)`. Compute both with
   `ClprRegisterConnectorTransaction.computeConnectorId(channelId, publicKey, salt)` and
   `ClprRegisterConnectorTransaction.computeOwnershipCommitment(connectorId, publicKey)`. The salt is any 32 bytes; all
   zeros is fine for the only Connector of a key on a Channel.
2. **Reveal** — `ClprCompleteConnectorTransaction` reveals the Connector with its ownership proof
   (`ClprConnectorOwnershipProof.create(channelId, salt, privateKey)`), names its authorization contract and admin
   authority, and locks the stake the payer funds. The Channel must exist.

`ClprDeregisterConnectorTransaction` removes a Connector once none of its messages is in flight and returns the locked
stake to the given recipient.

### Bundles and the ledger configuration

`ClprSubmitBundleTransaction` delivers a bundle of messages of a peer ledger, together with the proof that
authenticates them. The payload is opaque to the SDK; only the verifier contract of the Channel interprets it. A
bundle that makes no progress fails with `CLPR_NO_PROGRESS`, and the payer is charged a penalty.

`ClprUpdateLedgerConfigurationTransaction` replaces the `ClprThrottles` of this ledger. Read the current values with
`ClprLedgerConfigurationQuery`, create new throttles with all seven values and submit them.

### Signing requirements

| Transaction                                | Signers required                                                                    |
|--------------------------------------------|-------------------------------------------------------------------------------------|
| `ClprRegisterChannelTransaction`           | the *payer*                                                                         |
| `ClprCompleteChannelTransaction`           | the *payer*; the ownership is proven by the ownership proof, not by a signature    |
| `ClprCloseChannelTransaction`              | the *payer*, which must be the network's treasury or system admin account            |
| `ClprSubmitBundleTransaction`              | the *payer*                                                                         |
| `ClprRegisterConnectorTransaction`         | the *payer*                                                                         |
| `ClprCompleteConnectorTransaction`         | the *payer*, which funds the locked stake                                           |
| `ClprDeregisterConnectorTransaction`       | the *payer*; **and** the Connector's `adminAuthority`; **and** the `stakeRecipient` |
| `ClprUpdateLedgerConfigurationTransaction` | the *payer*, which must be the network's treasury or system admin account            |

A payer without the privilege of the network administrator gets `AUTHORIZATION_FAILED`, not the CLPR status
`CLPR_AUTHORIZATION_FAILED`, which has an unrelated meaning. A missing signature of the Connector's admin or of the
stake recipient fails with `INVALID_SIGNATURE`.

None of the CLPR transactions can be scheduled.

## Design Notes

- **Source.** The API follows the SDK proposal for HIP-1535 (hiero-ledger/sdk-collaboration-hub,
  `proposals/hips/hip-1535.md`) and the protobuf messages of consensus node commit `76ba2903`, the baseline of that
  proposal. The two mirror node queries of the proposal are not part of this module (see Questions & Comments).
- **Immutable transactions instead of mutable builders.** As every transaction of this SDK, the fields are immutable
  and required fields are not nullable. This removes the rules the proposal needs for its mutable builders: there is
  no field whose change has to clear the ownership proof, and no "complete proof" check before freezing.
- **The ownership proof is a value.** The proposal sets the proof with `setOwnershipProof(privateKey)` and reads
  `publicKey`, `signature` and `signatureScheme` through getters only, so that nobody can combine a scheme, a key and a
  signature that do not belong together. Here the proof is one immutable value (`ClprChannelOwnershipProof`,
  `ClprConnectorOwnershipProof`) that carries the identities it signs, the `PublicKey` and the signature, and is created
  by `create(...)` from the private key. The signature scheme of the wire (`ClprSignatureScheme`, ECDSA secp256k1 or
  Ed25519) is the algorithm of the `PublicKey` and is therefore not a field of its own. The public key is encoded as
  the protocol requires — 64 bytes `x || y` for secp256k1 (no `0x04` prefix), 32 bytes for Ed25519 — when the
  transaction is packed. A proof read from a packed transaction (`fromBytes`) is a value like any other and needs no
  private key.
- **Signing the proof.** The Channel proof signs `keccak256(channelId)`, the Connector proof
  `keccak256(connectorId || clprServiceAddress)` with the 20-byte service address `0x…016e`. An ECDSA `PrivateKey.sign`
  of this SDK already hashes its message with Keccak-256, an Ed25519 one signs its message as it is; `create(...)`
  therefore passes the unhashed message to an ECDSA key and the digest to an Ed25519 key, so that both sign the same
  digest and no hash is applied twice.
- **`Authority` and `NativeToken` instead of `Key` and `Hbar`.** The admin key of a Connector is an `Authority`, as
  every key requirement of this SDK; the locked stake is a `NativeToken<ANY, ANY>`, as every amount, encoded in the
  smallest unit of the native token (tinybars on Hedera).
- **What is not exposed.** `ClprUpdateLedgerConfigurationTransaction` takes only the throttles: the chain id, the
  protocol version and the timestamp of a configuration are ignored by the network. `ClprSubmitBundleTransaction` has
  no setters for the deprecated `endpoint_node_id` and `endpoint_signature` of the wire, which the network ignores.
- **Receipts.** The CLPR transactions add nothing to the receipt; each has its own empty receipt type, as the node
  transactions of the admin client.

## API Schema

```
namespace consensusnode.clpr.transactions
requires {AccountId, ContractId} from ledger
requires {PrivateKey, PublicKey} from keys
requires {Authority} from authority
requires {NativeToken} from nativeToken
requires {Receipt, Transaction} from consensusnode.transactions
requires {ClprThrottles} from consensusnode.clpr

// The proof that the registrant of a Channel owns the key of its ownership commitment: the Channel ID, the public key
// of the commitment and the signature of keccak256(channelId) with the matching private key.
@@hip("1535")
@@finalType
ClprChannelOwnershipProof {
    @@immutable @@minSize(32) @@maxSize(32) channelId: bytes   // the Channel ID, the same on both ledgers
    @@immutable publicKey: PublicKey                           // the public key of the ownership commitment
    @@immutable @@minSize(64) @@maxSize(64) signature: bytes   // the signature of keccak256(channelId): r || s for ECDSA

    // Creates the proof for the Channel with the private key of the ownership commitment. The private key is not kept.
    @@static
    ClprChannelOwnershipProof create(@@minSize(32) @@maxSize(32) channelId: bytes, privateKey: PrivateKey)
}

// The proof that the registrant of a Connector owns the key of its commitment: the Channel ID, the salt and the
// Connector ID derived from them, the public key and the signature of keccak256(connectorId || clprServiceAddress)
// with the matching private key.
@@hip("1535")
@@finalType
ClprConnectorOwnershipProof {
    @@immutable @@minSize(32) @@maxSize(32) channelId: bytes     // the Channel of the Connector
    @@immutable @@minSize(32) @@maxSize(32) salt: bytes          // the salt of the Connector ID
    @@immutable @@minSize(32) @@maxSize(32) connectorId: bytes   // keccak256(channelId || publicKey || salt)
    @@immutable publicKey: PublicKey                             // the public key of the commitment
    @@immutable @@minSize(64) @@maxSize(64) signature: bytes     // the signature of keccak256(connectorId || clprServiceAddress)

    // Creates the proof for the Connector with the private key of the commitment: derives the Connector ID from the
    // Channel ID, the public key and the salt, and signs it. The private key is not kept.
    @@static
    ClprConnectorOwnershipProof create(@@minSize(32) @@maxSize(32) channelId: bytes,
            @@minSize(32) @@maxSize(32) salt: bytes, privateKey: PrivateKey)
}

// Registers the ownership commitment of a new Channel (phase 1 of 2). Anybody may submit it. A commitment that is
// already registered, also by a completed or closed Channel, fails with CLPR_CHANNEL_ALREADY_EXISTS.
@@hip("1535")
@@finalType
ClprRegisterChannelTransaction extends Transaction<ClprRegisterChannelReceipt, ClprRegisterChannelTransaction> {
    @@immutable @@minSize(32) @@maxSize(32) ownershipCommitment: bytes   // keccak256(channelId || publicKey)

    // Computes the ownership commitment keccak256(channelId || publicKey), with the public key in the encoding of the
    // protocol (64 bytes x || y for secp256k1, 32 bytes for Ed25519).
    @@static
    bytes computeOwnershipCommitment(@@minSize(32) @@maxSize(32) channelId: bytes, publicKey: PublicKey)
}

// Receipt of a `ClprRegisterChannelTransaction`.
@@hip("1535")
@@finalType
ClprRegisterChannelReceipt extends Receipt {
}

// Completes a Channel (phase 2 of 2): reveals the Channel ID and the key of the ownership commitment and activates the
// Channel once the verifier contract has accepted the configuration of the peer ledger.
@@hip("1535")
@@finalType
ClprCompleteChannelTransaction extends Transaction<ClprCompleteChannelReceipt, ClprCompleteChannelTransaction> {
    @@immutable ownershipProof: ClprChannelOwnershipProof   // the Channel ID and the proof of the ownership of the commitment
    // The verifier contract that authenticates the peer ledger: a built-in verifier or a deployed contract. It cannot
    // be changed for the lifetime of the Channel.
    @@immutable verifierContract: ContractId
    // The proof of the peer's ledger configuration, passed to the verifier: the configurationStateProof of the peer's
    // ClprLedgerConfiguration, if the verifier accepts its encoding.
    @@immutable configProof: bytes
    // The proof of the peer's initial endpoint manifest: the manifestStateProof of the peer's ClprEndpointManifest, if
    // the verifier accepts its encoding. Empty bytes are not a valid proof of an empty manifest for every verifier.
    @@immutable endpointManifestProof: bytes
}

// Receipt of a `ClprCompleteChannelTransaction`.
@@hip("1535")
@@finalType
ClprCompleteChannelReceipt extends Receipt {
}

// Closes a Channel, or removes the ownership commitment of a Channel that was never completed. Only the network's
// treasury or system admin account may submit it.
@@hip("1535")
@@finalType
ClprCloseChannelTransaction extends Transaction<ClprCloseChannelReceipt, ClprCloseChannelTransaction> {
    @@immutable @@minSize(32) @@maxSize(32) channelId: bytes   // the Channel to close
    // The ownership commitment of a registration that was never completed; used only when no Channel exists for
    // channelId, ignored otherwise.
    @@immutable @@nullable @@minSize(32) @@maxSize(32) ownershipCommitment: bytes
}

// Receipt of a `ClprCloseChannelTransaction`.
@@hip("1535")
@@finalType
ClprCloseChannelReceipt extends Receipt {
}

// Delivers a bundle of messages of the peer ledger of a Channel, with the proof that authenticates them.
@@hip("1535")
@@finalType
ClprSubmitBundleTransaction extends Transaction<ClprSubmitBundleReceipt, ClprSubmitBundleTransaction> {
    @@immutable @@minSize(32) @@maxSize(32) channelId: bytes   // the Channel the bundle belongs to
    @@immutable bundlePayload: bytes                            // the bundle; interpreted only by the verifier contract of the Channel
}

// Receipt of a `ClprSubmitBundleTransaction`.
@@hip("1535")
@@finalType
ClprSubmitBundleReceipt extends Receipt {
}

// Registers the commitment of a new Connector (phase 1 of 2). Anybody may submit it. A commitment that is already
// registered, also by a completed or deregistered Connector, fails with CLPR_CONNECTOR_ALREADY_EXISTS.
@@hip("1535")
@@finalType
ClprRegisterConnectorTransaction extends Transaction<ClprRegisterConnectorReceipt, ClprRegisterConnectorTransaction> {
    @@immutable @@minSize(32) @@maxSize(32) commitment: bytes   // keccak256(connectorId || publicKey)

    // Computes the Connector ID keccak256(channelId || publicKey || salt), with the public key in the encoding of the
    // protocol.
    @@static
    bytes computeConnectorId(@@minSize(32) @@maxSize(32) channelId: bytes, publicKey: PublicKey,
            @@minSize(32) @@maxSize(32) salt: bytes)

    // Computes the commitment keccak256(connectorId || publicKey), with the public key in the encoding of the
    // protocol.
    @@static
    bytes computeOwnershipCommitment(@@minSize(32) @@maxSize(32) connectorId: bytes, publicKey: PublicKey)
}

// Receipt of a `ClprRegisterConnectorTransaction`.
@@hip("1535")
@@finalType
ClprRegisterConnectorReceipt extends Receipt {
}

// Completes a Connector (phase 2 of 2): reveals it, locks its stake and activates it. The Channel must exist; the
// payer funds the locked stake.
@@hip("1535")
@@finalType
ClprCompleteConnectorTransaction extends Transaction<ClprCompleteConnectorReceipt, ClprCompleteConnectorTransaction> {
    @@immutable ownershipProof: ClprConnectorOwnershipProof   // the Channel, the Connector ID and the proof of the ownership
    @@immutable connectorContract: ContractId                 // the deployed contract that authorizes the messages of the Connector
    @@immutable adminAuthority: Authority                     // required to deregister the Connector
    // The stake the network can slash; must reach the network's minimum (CLPR_INSUFFICIENT_STAKE otherwise).
    @@immutable lockedStake: NativeToken<ANY, ANY>
}

// Receipt of a `ClprCompleteConnectorTransaction`.
@@hip("1535")
@@finalType
ClprCompleteConnectorReceipt extends Receipt {
}

// Removes a Connector and returns its locked stake. Fails with CLPR_CONNECTOR_HAS_IN_FLIGHT_MESSAGES while a message
// of the Connector is still in flight. The Connector's adminAuthority and the stake recipient must sign.
@@hip("1535")
@@finalType
ClprDeregisterConnectorTransaction extends Transaction<ClprDeregisterConnectorReceipt, ClprDeregisterConnectorTransaction> {
    @@immutable @@minSize(32) @@maxSize(32) channelId: bytes     // the Channel of the Connector
    @@immutable @@minSize(32) @@maxSize(32) connectorId: bytes   // the Connector to remove
    @@immutable stakeRecipient: AccountId                         // receives the locked stake; must sign
}

// Receipt of a `ClprDeregisterConnectorTransaction`.
@@hip("1535")
@@finalType
ClprDeregisterConnectorReceipt extends Receipt {
}

// Replaces the CLPR limits of this ledger. Only the network's treasury or system admin account may submit it.
@@hip("1535")
@@finalType
ClprUpdateLedgerConfigurationTransaction extends Transaction<ClprUpdateLedgerConfigurationReceipt,
        ClprUpdateLedgerConfigurationTransaction> {
    @@immutable throttles: ClprThrottles   // the new limits; replace all current ones
}

// Receipt of a `ClprUpdateLedgerConfigurationTransaction`.
@@hip("1535")
@@finalType
ClprUpdateLedgerConfigurationReceipt extends Receipt {
}
```

## Default Instances for Testing

```
// The ownership proofs of the default private key for the Channel ID and the salt of the ownership test vectors of the
// SDK proposal for HIP-1535 (the bytes 0 to 31 resp. 32 to 63).
instance ClprChannelOwnershipProof = ClprChannelOwnershipProof.create(channelId: [0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25, 26, 27, 28, 29, 30, 31],
        privateKey: DEFAULT(PrivateKey))

instance ClprConnectorOwnershipProof = ClprConnectorOwnershipProof.create(channelId: [0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25, 26, 27, 28, 29, 30, 31],
        salt: [32, 33, 34, 35, 36, 37, 38, 39, 40, 41, 42, 43, 44, 45, 46, 47, 48, 49, 50, 51, 52, 53, 54, 55, 56, 57, 58, 59, 60, 61, 62, 63], privateKey: DEFAULT(PrivateKey))

// The Channel ID and the salt of the ownership test vectors of the SDK proposal for HIP-1535.
values CHANNEL_ID: bytes = hex(text: "000102030405060708090a0b0c0d0e0f101112131415161718191a1b1c1d1e1f")

values SALT: bytes = hex(text: "202122232425262728292a2b2c2d2e2f303132333435363738393a3b3c3d3e3f")

// The public test keys of the ownership test vectors: the secp256k1 scalar 1 and the Ed25519 seed of RFC 8032, test 1.
values ECDSA_KEY: PrivateKey = keys.createPrivateKey(algorithm: keys.KeyAlgorithm.ECDSA,
        encoding: keys.ByteImportEncoding.HEX, value: "0000000000000000000000000000000000000000000000000000000000000001")

values ED25519_KEY: PrivateKey = keys.createPrivateKey(algorithm: keys.KeyAlgorithm.ED25519,
        encoding: keys.ByteImportEncoding.HEX, value: "9d61b19deffd5a60ba844af492ec2cc44449c5697b326919703bac031cae7f60")

// A Channel ID one byte short of 32 bytes.
values SHORT_CHANNEL_ID: bytes = repeat(value: hex(text: "01"), count: 31)
```

## Examples

### Establish a Channel

```
HieroClient client     = ...;   // ledger A
PrivateKey  registrant = ...;   // the key pair of the ownership commitment
bytes       channelId  = ...;   // 32 bytes, the same on both ledgers

bytes commitment = ClprRegisterChannelTransaction.computeOwnershipCommitment(channelId, registrant.createPublicKey());
new ClprRegisterChannelTransaction(commitment)
    .signWithOperatorAndSubmit(client)
    .queryReceipt(client);
// ... the same on ledger B

// the proofs of ledger B, read with the CLPR queries on ledger B
ClprLedgerConfiguration peerConfiguration = new ClprLedgerConfigurationQuery().submit(clientB).value;
ClprEndpointManifest    peerManifest      = new ClprEndpointManifestQuery().submit(clientB).value;

new ClprCompleteChannelTransaction(
        ClprChannelOwnershipProof.create(channelId, registrant),
        verifierContract,
        peerConfiguration.configurationStateProof,
        peerManifest.manifestStateProof)
    .signWithOperatorAndSubmit(client)
    .queryReceipt(client);
// ... the same on ledger B with the proofs of ledger A
```

### Register a Connector

```
bytes connectorId = ClprRegisterConnectorTransaction.computeConnectorId(channelId, key.createPublicKey(), salt);
bytes commitment  = ClprRegisterConnectorTransaction.computeOwnershipCommitment(connectorId, key.createPublicKey());
new ClprRegisterConnectorTransaction(commitment)
    .signWithOperatorAndSubmit(client)
    .queryReceipt(client);

new ClprCompleteConnectorTransaction(
        ClprConnectorOwnershipProof.create(channelId, salt, key),
        connectorContract,
        Authority.of(adminKey),
        NativeToken.of(100, HBAR_UNIT))
    .signWithOperatorAndSubmit(client)
    .queryReceipt(client);
```

## Testing

Offline tests of the ownership commitments and proofs and of the attributes the transactions keep, written as given /
when / then. The expected bytes are the ownership test vectors of the SDK proposal for HIP-1535. Sending the
transactions needs a network with CLPR enabled and is not tested here. The comment above each test names its source.

```tests
// Source: the ownership test vectors of the SDK proposal for HIP-1535 (channelCommitment of both vectors).
@@testId("the-channel-commitment-is-the-keccak-256-hash-of-the-channel-id-and-the-public-key")
@@hip("1535")
test "the Channel commitment is the Keccak-256 hash of the Channel ID and the public key" {
    // when
    let ecdsa = ClprRegisterChannelTransaction.computeOwnershipCommitment(channelId: CHANNEL_ID,
            publicKey: ECDSA_KEY.createPublicKey())
    let ed25519 = ClprRegisterChannelTransaction.computeOwnershipCommitment(channelId: CHANNEL_ID,
            publicKey: ED25519_KEY.createPublicKey())

    // then
    assertEquals(expected: hex(text: "f476ca287a171dd8aa94165fb27fadee1fc61f051840382aed326e1fd41d41b3"), actual: ecdsa)
    assertEquals(expected: hex(text: "4816bb408545af511a22953026b40500890c7fa0a44c83682e251b114a0b6496"), actual: ed25519)
}

// Source: the ownership test vectors of the SDK proposal for HIP-1535 (connectorId and connectorCommitment of both
// vectors).
@@testId("the-connector-id-and-commitment-are-the-keccak-256-hashes-of-the-protocol")
@@hip("1535")
test "the Connector ID and commitment are the Keccak-256 hashes of the protocol" {
    // when
    let ecdsaId = ClprRegisterConnectorTransaction.computeConnectorId(channelId: CHANNEL_ID,
            publicKey: ECDSA_KEY.createPublicKey(), salt: SALT)
    let ed25519Id = ClprRegisterConnectorTransaction.computeConnectorId(channelId: CHANNEL_ID,
            publicKey: ED25519_KEY.createPublicKey(), salt: SALT)

    // then
    assertEquals(expected: hex(text: "532a8331032028874f4d7fa1953636f8b076876c493606f93a86d8880058b4a6"), actual: ecdsaId)
    assertEquals(expected: hex(text: "488d7b7fdcc061f1de9fe98666c1337f0fc90275889f2450d59e1e6e837e74b1"), actual: ed25519Id)
    assertEquals(expected: hex(text: "647e3f78cfdbceb3714e6c4d043f60e3169cabae3d90be168aeb58796ed6533d"),
            actual: ClprRegisterConnectorTransaction.computeOwnershipCommitment(connectorId: ecdsaId,
                    publicKey: ECDSA_KEY.createPublicKey()))
    assertEquals(expected: hex(text: "a36b6836b720019a9fb1846df6bb4b76d13a1f276da0ebc2371b4d0eb8e30618"),
            actual: ClprRegisterConnectorTransaction.computeOwnershipCommitment(connectorId: ed25519Id,
                    publicKey: ED25519_KEY.createPublicKey()))
}

// Source: the ownership test vectors of the SDK proposal for HIP-1535 (channelSignature of both vectors; the ECDSA
// reference uses RFC 6979 and a low s, as `PrivateKey.sign` of `keys`).
@@testId("a-channel-ownership-proof-signs-the-keccak-256-hash-of-the-channel-id")
@@hip("1535")
test "a Channel ownership proof signs the Keccak-256 hash of the Channel ID" {
    // when
    let ecdsa = ClprChannelOwnershipProof.create(channelId: CHANNEL_ID, privateKey: ECDSA_KEY)
    let ed25519 = ClprChannelOwnershipProof.create(channelId: CHANNEL_ID, privateKey: ED25519_KEY)

    // then
    assertEquals(expected: CHANNEL_ID, actual: ecdsa.channelId)
    assertEquals(expected: ECDSA_KEY.createPublicKey(), actual: ecdsa.publicKey)
    assertEquals(expected: hex(text: "cd52448f226a599c8d61d568a6186d50bb263a2559e08c1c8389233e632d2a58"
            + "39232b91d7b8974b862ff5540bae836c08792936ebd708682413e0c8e2884086"), actual: ecdsa.signature)
    assertEquals(expected: ED25519_KEY.createPublicKey(), actual: ed25519.publicKey)
    assertEquals(expected: hex(text: "8ef8546b7a464125e9bda37b52203c8a8f1c59c51deae85c6178632b40501412"
            + "26d6018ed52fcf8afd2a4c141a04109220eb8c7cd9d923a06a2fff23155cab0f"), actual: ed25519.signature)
}

// Source: the ownership test vectors of the SDK proposal for HIP-1535 (connectorId and connectorSignature of both
// vectors).
@@testId("a-connector-ownership-proof-derives-the-connector-id-and-signs-it-with-the-service-address")
@@hip("1535")
test "a Connector ownership proof derives the Connector ID and signs it with the service address" {
    // when
    let ecdsa = ClprConnectorOwnershipProof.create(channelId: CHANNEL_ID, salt: SALT, privateKey: ECDSA_KEY)
    let ed25519 = ClprConnectorOwnershipProof.create(channelId: CHANNEL_ID, salt: SALT, privateKey: ED25519_KEY)

    // then
    assertEquals(expected: CHANNEL_ID, actual: ecdsa.channelId)
    assertEquals(expected: SALT, actual: ecdsa.salt)
    assertEquals(expected: hex(text: "532a8331032028874f4d7fa1953636f8b076876c493606f93a86d8880058b4a6"),
            actual: ecdsa.connectorId)
    assertEquals(expected: hex(text: "4b133aadf1d8a8c8bc6509842e245e01f71639e6e09fa2ed8ddf315faa38926f"
            + "29e87b0cf850345f9cf74e82de367c7e31c96dfde6cb4603afb42750a551197d"), actual: ecdsa.signature)
    assertEquals(expected: hex(text: "488d7b7fdcc061f1de9fe98666c1337f0fc90275889f2450d59e1e6e837e74b1"),
            actual: ed25519.connectorId)
    assertEquals(expected: hex(text: "7374599d239cc8300e8f8f99057656a0c4b3e42f7fbaa99ab8cd3907da4d9fbe"
            + "1ef28f12b7af9cd3f126fa513e3d6661b24fe790c1bd8a80275e8eaf69f1c70f"), actual: ed25519.signature)
}

// Source: the SDK proposal for HIP-1535 (the helpers reject invalid ID lengths locally); `@@minSize(32) @@maxSize(32)`
// of this spec.
@@testId("a-channel-id-that-is-not-32-bytes-long-is-rejected")
@@hip("1535")
test "a Channel ID that is not 32 bytes long is rejected" {
    // when / then
    @@expectThrows(invalid-argument-error)
    ClprRegisterChannelTransaction.computeOwnershipCommitment(channelId: SHORT_CHANNEL_ID,
            publicKey: ECDSA_KEY.createPublicKey())
    @@expectThrows(invalid-argument-error)
    ClprChannelOwnershipProof.create(channelId: SHORT_CHANNEL_ID, privateKey: ECDSA_KEY)
    @@expectThrows(invalid-argument-error)
    ClprCloseChannelTransaction{channelId: SHORT_CHANNEL_ID}
    @@expectThrows(invalid-argument-error)
    ClprSubmitBundleTransaction{channelId: SHORT_CHANNEL_ID, bundlePayload: utf8(text: "bundle")}
}

// Source: the SDK proposal for HIP-1535 (the transactions of the Channel lifecycle and their fields); HAPI
// clpr_complete_channel.proto.
@@testId("a-complete-channel-transaction-keeps-its-proof-verifier-and-proofs")
@@hip("1535")
test "a complete Channel transaction keeps its proof, verifier and proofs" {
    // given
    let proof = DEFAULT(ClprChannelOwnershipProof)
    let verifier = ContractId.fromString(value: "0.0.366")

    // when
    let transaction = ClprCompleteChannelTransaction{ownershipProof: proof, verifierContract: verifier,
            configProof: utf8(text: "config proof"), endpointManifestProof: utf8(text: "manifest proof")}

    // then
    assertEquals(expected: proof, actual: transaction.ownershipProof)
    assertEquals(expected: verifier, actual: transaction.verifierContract)
    assertEquals(expected: utf8(text: "config proof"), actual: transaction.configProof)
    assertEquals(expected: utf8(text: "manifest proof"), actual: transaction.endpointManifestProof)
}

// Source: the SDK proposal for HIP-1535 (ClprCloseChannelTransaction: the ownership commitment is only needed for a
// registration that was never completed); `@@nullable` of this spec.
@@testId("a-close-channel-transaction-needs-the-commitment-only-for-an-incomplete-registration")
@@hip("1535")
test "a close Channel transaction needs the commitment only for an incomplete registration" {
    // given
    let commitment = ClprRegisterChannelTransaction.computeOwnershipCommitment(channelId: CHANNEL_ID,
            publicKey: ECDSA_KEY.createPublicKey())

    // when
    let close = ClprCloseChannelTransaction{channelId: CHANNEL_ID}
    let sweep = ClprCloseChannelTransaction{channelId: CHANNEL_ID, ownershipCommitment: commitment}

    // then
    assertEquals(expected: CHANNEL_ID, actual: close.channelId)
    assertNull(actual: close.ownershipCommitment)
    assertEquals(expected: commitment, actual: sweep.ownershipCommitment)
}

// Source: the SDK proposal for HIP-1535 (the transactions of the Connector lifecycle and their fields); HAPI
// clpr_complete_connector.proto and clpr_deregister_connector.proto.
@@testId("the-connector-transactions-keep-their-attributes")
@@hip("1535")
test "the Connector transactions keep their attributes" {
    // given
    let proof = DEFAULT(ClprConnectorOwnershipProof)
    let contract = ContractId.fromString(value: "0.0.5007")
    let adminAuthority = DEFAULT(Authority)
    let stake: nativeToken.NativeToken<ANY, ANY> = DEFAULT(hedera.Hbar)
    let recipient = DEFAULT(AccountId)

    // when
    let complete = ClprCompleteConnectorTransaction{ownershipProof: proof, connectorContract: contract,
            adminAuthority: adminAuthority, lockedStake: stake}
    let deregister = ClprDeregisterConnectorTransaction{channelId: CHANNEL_ID, connectorId: proof.connectorId,
            stakeRecipient: recipient}

    // then
    assertEquals(expected: proof, actual: complete.ownershipProof)
    assertEquals(expected: contract, actual: complete.connectorContract)
    assertEquals(expected: adminAuthority, actual: complete.adminAuthority)
    assertEquals(expected: stake, actual: complete.lockedStake)
    assertEquals(expected: CHANNEL_ID, actual: deregister.channelId)
    assertEquals(expected: proof.connectorId, actual: deregister.connectorId)
    assertEquals(expected: recipient, actual: deregister.stakeRecipient)
}

// Source: the SDK proposal for HIP-1535 (ClprUpdateLedgerConfigurationTransaction replaces the entire throttles object).
@@testId("an-update-ledger-configuration-transaction-keeps-the-throttles")
@@hip("1535")
test "an update ledger configuration transaction keeps the throttles" {
    // given
    let throttles = DEFAULT(consensusnode.clpr.ClprThrottles)

    // when
    let transaction = ClprUpdateLedgerConfigurationTransaction{throttles: throttles}

    // then
    assertEquals(expected: throttles, actual: transaction.throttles)
}

// Source: this spec (a packed transaction survives toBytes and fromBytes, as every transaction of the SDK); the SDK
// proposal for HIP-1535 (Test Plan 24: a decoded completion transaction keeps its proof without a private key when it is
// re-serialized unchanged).
@@testId("every-clpr-transaction-survives-tobytes-and-frombytes")
@@hip("1535")
test "every CLPR transaction survives toBytes and fromBytes" {
    // given
    let payer = DEFAULT(consensusnode.client.Account)
    let nodes = [AccountId.fromString(value: "0.0.5005"), AccountId.fromString(value: "0.0.5006")]
    let channelProof = ClprChannelOwnershipProof.create(channelId: CHANNEL_ID, privateKey: ED25519_KEY)
    let connectorProof = ClprConnectorOwnershipProof.create(channelId: CHANNEL_ID, salt: SALT, privateKey: ECDSA_KEY)
    let stake: nativeToken.NativeToken<ANY, ANY> = DEFAULT(hedera.Hbar)
    let transactions: list<Transaction<ANY, ANY>> = [
            ClprRegisterChannelTransaction{ownershipCommitment: ClprRegisterChannelTransaction.computeOwnershipCommitment(
                    channelId: CHANNEL_ID, publicKey: ED25519_KEY.createPublicKey())},
            ClprCompleteChannelTransaction{ownershipProof: channelProof,
                    verifierContract: ContractId.fromString(value: "0.0.366"),
                    configProof: utf8(text: "config proof"), endpointManifestProof: utf8(text: "manifest proof")},
            ClprCloseChannelTransaction{channelId: CHANNEL_ID},
            ClprSubmitBundleTransaction{channelId: CHANNEL_ID, bundlePayload: utf8(text: "bundle")},
            ClprRegisterConnectorTransaction{commitment: connectorProof.connectorId},
            ClprCompleteConnectorTransaction{ownershipProof: connectorProof,
                    connectorContract: ContractId.fromString(value: "0.0.5007"), adminAuthority: DEFAULT(Authority),
                    lockedStake: stake},
            ClprDeregisterConnectorTransaction{channelId: CHANNEL_ID, connectorId: connectorProof.connectorId,
                    stakeRecipient: DEFAULT(AccountId)},
            ClprUpdateLedgerConfigurationTransaction{throttles: DEFAULT(consensusnode.clpr.ClprThrottles)}]

    for transaction in transactions {
        // given
        let packed = transaction.pack(payer: payer, nodes: nodes)

        // when
        let restored = consensusnode.transactions.PackedTransaction.fromBytes(bytes: packed.toBytes())

        // then
        assertEquals(expected: packed.toBytes(), actual: restored.toBytes())
        assertEquals(expected: packed.signableBodies(), actual: restored.signableBodies())
    }
}
```

## Questions & Comments

- **The CLPR transactions cannot be packed with the pinned protobuf definitions.** `tooling/protobuf` is pinned to
  consensus node v0.77.2, which has no CLPR service. Generating the API works; an implementation needs the protobuf
  definitions of a release with CLPR (the proposal names commit `a6b24529` for the bindings). Until then
  `sdk-java/impl` has no CLPR module, and `metalang check --project=sdk-java/impl` reports it as missing.
- **Scheduling.** The proposal asks to reject `schedule()` locally for all eight transactions, because none of their
  bodies is part of `SchedulableTransactionBody`. The Design Notes of
  [`transactions-schedule.md`](../consensus-node-client/transactions-schedule.md) say that the SDK does no
  schedulability check, while the Java implementation does check it. How does a transaction type of a module like
  this one declare that it cannot be scheduled?
- **The ownership proof needs the private key in the process.** `create(...)` takes a `PrivateKey`; a key in an HSM
  or a hardware wallet cannot create the proof. `TransactionSigner` signs transaction bodies, not arbitrary digests.
  The proposal defers external signing of the proofs as follow-up scope; a signer that signs a digest would allow it.
- **The proof is not checked against the transaction.** A `ClprChannelOwnershipProof` built with its constructor (or
  read from bytes) can carry a signature that does not verify. The network rejects it with `CLPR_INVALID_SIGNATURE`.
  Should the constructor verify the signature, or is a value without the check the right shape for proofs read from
  the wire?
- **The amount of the locked stake is not validated.** The proposal rejects negative values, fractions of a tinybar
  and amounts that the wire (`uint64` tinybars) cannot represent before serialization. `NativeToken` has no annotation
  for that; where should the check of a non-negative, whole amount of the smallest unit live (the same holds for the
  other amounts of the SDK)?
- **Who may submit a bundle?** The protobuf comment describes that only a registered endpoint may submit a bundle; the
  pinned handler and the mirror node design allow any payer. The proposal asks the consensus node team to confirm the
  intended contract; the description above follows the current behavior.
- **A bundle that cannot be decoded.** The protocol says `CLPR_BUNDLE_DECODE_FAILED` pauses the Channel; according
  to the proposal the handler rolls the pause back. Which state the Channel ends in is open on the consensus node side.
- **Closing a Channel with the commitment.** `ownershipCommitment` of `ClprCloseChannelTransaction` is only used when
  no Channel exists for `channelId`. Two separate transaction types (close / remove a registration) would make the
  rule a type; the protocol has one body for both, so they are kept together for now.
