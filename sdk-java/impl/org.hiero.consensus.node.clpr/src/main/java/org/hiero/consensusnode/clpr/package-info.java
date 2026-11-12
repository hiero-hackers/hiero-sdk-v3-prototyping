/// **CLPR** ("Clipper", HIP-1535) connects a Hiero ledger with other ledgers. Two ledgers exchange messages over a
/// **Channel**; a **Connector** authorizes and pays for the execution of the messages it sends over a Channel. The CLPR
/// service of the consensus node keeps the Channels and Connectors of its ledger, receives the messages of the peer ledgers
/// in bundles and publishes the configuration of its ledger, so that a peer ledger can verify it.
///
/// `ClprThrottles` are the capacity limits a ledger enforces on its CLPR service: the size of a message and of a bundle,
/// the depth of the outbound queue of a Channel and the number of endpoints. A ledger reports them in its
/// `ClprLedgerConfiguration`; the network administrator changes them with a `ClprUpdateLedgerConfigurationTransaction`,
/// which always replaces all seven values. Five of them must be greater than zero; a `maxLocalEndpoints` or
/// `maxPeerEndpoints` of `0` means "no limit". `maxSyncBytes` must be at least `maxMessagePayloadBytes + 65,536` bytes, so
/// that a bundle with one message of the maximum size still has room for its proof; the network rejects a configuration
/// that violates it with `INVALID_CLPR_CONFIGURATION`.
///
/// `ClprTransactionStatus` names the status codes of the CLPR service. A code that is defined by the protocol but that the
/// service does not return today is listed as well, so that every status of the network can be read. The module registers
/// them with the client as a `TransactionStatusSupport`; without this module, an answer with a CLPR code fails with
/// `unknown-status-error`.
package org.hiero.consensusnode.clpr;
