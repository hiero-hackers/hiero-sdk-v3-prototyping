package org.hiero.consensusnode.clpr;

import org.hiero.consensusnode.requestTypes.RequestType;

/// The request types of the CLPR service of the consensus node, with their name and code.
///
/// @see <a href="https://github.com/hiero-ledger/hiero-improvement-proposals/blob/main/HIP/hip-1535.md">HIP-1535</a>
public enum ClprRequestType implements RequestType {

    /// Update the local CLPR ledger configuration.
    CLPR_UPDATE_LEDGER_CONFIGURATION(117),
    /// Register a new CLPR Channel to a peer ledger.
    CLPR_REGISTER_CHANNEL(118),
    /// Complete a PENDING CLPR Channel (reveal phase).
    CLPR_COMPLETE_CHANNEL(119),
    /// Initiate graceful closure of a CLPR Channel.
    CLPR_CLOSE_CHANNEL(120),
    /// Get the current CLPR ledger configuration.
    CLPR_GET_LEDGER_CONFIGURATION(121),
    /// Submit a bundle of cross-ledger messages from a peer sync.
    CLPR_SUBMIT_BUNDLE(122),
    /// Redact a message from the outbound queue before delivery.
    CLPR_REDACT_MESSAGE(123),
    /// Register a Connector on the CLPR Service.
    CLPR_REGISTER_CONNECTOR(124),
    /// Remove a Connector from the CLPR Service.
    CLPR_DEREGISTER_CONNECTOR(125),
    /// Complete a Connector registration (reveal phase).
    CLPR_COMPLETE_CONNECTOR(126),
    /// Self-publish a node's CLPR endpoint into a manifest construction.
    CLPR_ENDPOINT_PUBLICATION(127),
    /// Get the current CLPR endpoint manifest.
    CLPR_GET_ENDPOINT_MANIFEST(128);

    private final int code;

    ClprRequestType(final int code) {
        this.code = code;
    }

    /// Returns the `code` of this value.
    @Override
    public int code() {
        return code;
    }
}
