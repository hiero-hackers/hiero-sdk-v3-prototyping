/// Two ledgers establish a Channel by passing each other proofs of their configuration (see
/// `consensusnode.clpr.transactions`). Only the consensus node can produce these proofs, so they are read with two free
/// queries:
///
/// - **`ClprLedgerConfigurationQuery`** returns the `ClprLedgerConfiguration` of the ledger: the protocol version, the
///   chain id, the address of the CLPR service, the `ClprThrottles`, the initial trust anchor and the
///   `configurationStateProof`.
/// - **`ClprEndpointManifestQuery`** returns the `ClprEndpointManifest` of the ledger: its CLPR endpoints and the
///   `manifestStateProof`.
///
/// Neither query needs a payment or the privilege of the network administrator; both fail with `CLPR_NOT_ENABLED` on a
/// ledger without CLPR.
///
/// Pass the proofs unchanged as `configProof` and `endpointManifestProof` of a `ClprCompleteChannelTransaction` on the
/// peer ledger, if the verifier there accepts their encoding. A proof is taken from a signed snapshot of the ledger
/// state, so it does not necessarily authenticate the live values returned beside it, and the endpoint manifest is not
/// necessarily the newest one. A proof can be **empty** — when there is no usable snapshot yet or the ledger rejected its
/// own proof. This is a successful answer without proof material, not an error: query again later if you need the proof.
/// An empty `manifestStateProof` does not mean that the endpoint list is empty.
package org.hiero.consensusnode.clpr.queries;
