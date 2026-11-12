/// ### Channels
///
/// A **Channel** connects this ledger with one peer ledger. Both ledgers know it by the same 32-byte Channel ID. A Channel
/// is established in two phases, so that nobody who watches the first phase can claim the same Channel ID first:
///
/// 1. **Commit** — `ClprRegisterChannelTransaction` registers only an ownership commitment,
///    `keccak256(channelId || publicKey)`. It reveals neither the Channel ID nor the key. Compute the commitment with
///    `ClprRegisterChannelTransaction.computeOwnershipCommitment(channelId, publicKey)`. Submit the same commitment to both
///    ledgers.
/// 2. **Reveal** — `ClprCompleteChannelTransaction` reveals the Channel ID and the public key, proves the ownership with a
///    signature, names the verifier contract that authenticates the peer ledger, and passes the proofs of the peer's
///    configuration and endpoint manifest (read them with the queries of `consensusnode.clpr.queries` on the peer ledger).
///    The network checks the commitment and the signature, lets the verifier check the proofs, and activates the Channel.
///    Do the same on the peer ledger with the proofs of this ledger.
///
/// Create the ownership proof with `ClprChannelOwnershipProof.create(channelId, privateKey)`, using the key pair of the
/// commitment. The SDK encodes the public key, signs and records the signature scheme; the private key is not kept.
///
/// `ClprCloseChannelTransaction` closes a Channel: an `ACTIVE` or `PAUSED` Channel enters `CLOSING`, or `DRAINED` if all
/// its outbound messages are acknowledged; a `DRAINED` Channel becomes `CLOSED`. With the ownership commitment it also
/// removes a registration that was never completed.
///
/// ### Connectors
///
/// A **Connector** authorizes and pays for the execution of the messages it sends over a Channel; it is identified by its
/// Channel ID together with its 32-byte Connector ID. It is registered in the same two phases:
///
/// 1. **Commit** — `ClprRegisterConnectorTransaction` registers the commitment `keccak256(connectorId || publicKey)`, where
///    `connectorId = keccak256(channelId || publicKey || salt)`. Compute both with
///    `ClprRegisterConnectorTransaction.computeConnectorId(channelId, publicKey, salt)` and
///    `ClprRegisterConnectorTransaction.computeOwnershipCommitment(connectorId, publicKey)`. The salt is any 32 bytes; all
///    zeros is fine for the only Connector of a key on a Channel.
/// 2. **Reveal** — `ClprCompleteConnectorTransaction` reveals the Connector with its ownership proof
///    (`ClprConnectorOwnershipProof.create(channelId, salt, privateKey)`), names its authorization contract and admin
///    authority, and locks the stake the payer funds. The Channel must exist.
///
/// `ClprDeregisterConnectorTransaction` removes a Connector once none of its messages is in flight and returns the locked
/// stake to the given recipient.
///
/// ### Bundles and the ledger configuration
///
/// `ClprSubmitBundleTransaction` delivers a bundle of messages of a peer ledger, together with the proof that
/// authenticates them. The payload is opaque to the SDK; only the verifier contract of the Channel interprets it. A
/// bundle that makes no progress fails with `CLPR_NO_PROGRESS`, and the payer is charged a penalty.
///
/// `ClprUpdateLedgerConfigurationTransaction` replaces the `ClprThrottles` of this ledger. Read the current values with
/// `ClprLedgerConfigurationQuery`, create new throttles with all seven values and submit them.
///
/// ### Signing requirements
///
/// | Transaction                                | Signers required                                                                    |
/// |--------------------------------------------|-------------------------------------------------------------------------------------|
/// | `ClprRegisterChannelTransaction`           | the *payer*                                                                         |
/// | `ClprCompleteChannelTransaction`           | the *payer*; the ownership is proven by the ownership proof, not by a signature    |
/// | `ClprCloseChannelTransaction`              | the *payer*, which must be the network's treasury or system admin account            |
/// | `ClprSubmitBundleTransaction`              | the *payer*                                                                         |
/// | `ClprRegisterConnectorTransaction`         | the *payer*                                                                         |
/// | `ClprCompleteConnectorTransaction`         | the *payer*, which funds the locked stake                                           |
/// | `ClprDeregisterConnectorTransaction`       | the *payer*; **and** the Connector's `adminAuthority`; **and** the `stakeRecipient` |
/// | `ClprUpdateLedgerConfigurationTransaction` | the *payer*, which must be the network's treasury or system admin account            |
///
/// A payer without the privilege of the network administrator gets `AUTHORIZATION_FAILED`, not the CLPR status
/// `CLPR_AUTHORIZATION_FAILED`, which has an unrelated meaning. A missing signature of the Connector's admin or of the
/// stake recipient fails with `INVALID_SIGNATURE`.
///
/// None of the CLPR transactions can be scheduled.
package org.hiero.consensusnode.clpr.transactions;
