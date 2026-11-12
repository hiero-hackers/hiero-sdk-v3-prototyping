package org.hiero.consensusnode.clpr;

import org.hiero.consensusnode.status.TransactionStatus;

/// The status codes of the CLPR service of the consensus node, with their name and code.
///
/// @see <a href="https://github.com/hiero-ledger/hiero-improvement-proposals/blob/main/HIP/hip-1535.md">HIP-1535</a>
public enum ClprTransactionStatus implements TransactionStatus {

    /// The CLPR service is not enabled on this ledger.
    CLPR_NOT_ENABLED(537),
    /// A CLPR configuration field has an invalid value.
    INVALID_CLPR_CONFIGURATION(538),
    /// The chain_id in a CLPR ledger configuration is immutable (set at genesis from configuration properties).
    CLPR_CHAIN_ID_IMMUTABLE(539),
    /// Too many endpoints were specified in the CLPR configuration.
    CLPR_TOO_MANY_SEED_ENDPOINTS(540),
    /// An endpoint in the CLPR configuration is missing required fields (service_endpoint, tls_certificate, or
    /// ecdsa_signing_key).
    CLPR_INVALID_SEED_ENDPOINT(541),
    /// A CLPR Channel with the given channel_id already exists, or (on registerChannel) the ownership commitment is
    /// already registered, either pending reveal or bound to an existing Channel in any status, including CLOSED.
    CLPR_CHANNEL_ALREADY_EXISTS(542),
    /// The signature on the CLPR channel completion is invalid.
    CLPR_INVALID_SIGNATURE(543),
    /// The verifier contract account does not exist or is not a smart contract.
    CLPR_INVALID_VERIFIER_CONTRACT(544),
    /// The verifier contract's verifyConfig call failed or returned invalid data.
    CLPR_VERIFIER_CONFIG_FAILED(545),
    /// The keccak256(channel_id || public_key) does not match any stored ownership commitment.
    CLPR_COMMITMENT_MISMATCH(546),
    /// The specified channel_id does not correspond to any known CLPR channel.
    CLPR_CHANNEL_NOT_FOUND(547),
    /// The channel's current status does not allow the requested transition (e.g., attempting to close an
    /// already-closing or closed channel).
    CLPR_INVALID_CHANNEL_STATUS(548),
    /// The specified message ID does not exist in the queue.
    CLPR_MESSAGE_NOT_FOUND(549),
    /// The message has already been acknowledged by the peer and cannot be redacted.
    CLPR_MESSAGE_ALREADY_ACKNOWLEDGED(550),
    /// The endpoint signature is invalid or the signer is not a registered endpoint.
    CLPR_INVALID_ENDPOINT_SIGNATURE(551),
    /// The bundle proof verification failed.
    CLPR_BUNDLE_VERIFICATION_FAILED(552),
    /// The running hash in the bundle does not match the expected value.
    CLPR_RUNNING_HASH_MISMATCH(553),
    /// The message queue has reached the maximum depth for this Channel.
    CLPR_QUEUE_FULL(554),
    /// The specified Connector does not exist.
    CLPR_CONNECTOR_NOT_FOUND(555),
    /// A Connector with the same (channel_id, connector_id) already exists, or (on registerConnector) the commitment is
    /// already registered, either pending reveal or already used to create a Connector, including one that has since
    /// been deregistered.
    CLPR_CONNECTOR_ALREADY_EXISTS(556),
    /// The caller is not the admin of the specified Connector.
    CLPR_CONNECTOR_UNAUTHORIZED(557),
    /// The connector_id is missing or empty.
    CLPR_INVALID_CONNECTOR_ADDRESS(558),
    /// The connector_contract does not refer to a deployed smart contract.
    CLPR_INVALID_CONNECTOR_CONTRACT(559),
    /// The locked_stake amount is below the minimum required by ClprConfig.
    CLPR_INSUFFICIENT_STAKE(560),
    /// The message payload exceeds the destination ledger's max_message_payload_bytes.
    CLPR_PAYLOAD_TOO_LARGE(561),
    /// The Connector's authorization contract rejected the message. Not the same as AUTHORIZATION_FAILED, which a
    /// payer without the privilege of the network administrator receives.
    CLPR_AUTHORIZATION_FAILED(562),
    /// The message has already been redacted (payload already removed).
    CLPR_MESSAGE_ALREADY_REDACTED(563),
    /// The Connector has exceeded its per-Channel queue quota.
    CLPR_QUEUE_QUOTA_EXCEEDED(564),
    /// The Connector's balance is insufficient to cover the queue fee (including any congestion escalation).
    CLPR_INSUFFICIENT_CONNECTOR_BALANCE(565),
    /// Internal CLPR state is corrupt (e.g., an outbound message ID was assigned but the message does not exist in the
    /// queue).
    CLPR_INTERNAL_STATE_CORRUPTION(566),
    /// Cannot deregister a Connector that still has unresolved outbound in-flight messages.
    CLPR_CONNECTOR_HAS_IN_FLIGHT_MESSAGES(567),
    /// A per-message protobuf decode failed inside a verifier-accepted bundle.
    CLPR_BUNDLE_DECODE_FAILED(568),
    /// The submitted bundle carries no application messages, no trust anchor advance, and no ack progress.
    ///
    /// @deprecated Deprecated by the protocol; use CLPR_NO_PROGRESS instead.
    @Deprecated
    CLPR_EMPTY_BUNDLE(569),
    /// The submitting endpoint has exceeded its fair share of the per-ledger max_bundles_per_sec throttle.
    CLPR_RATE_LIMITED(570),
    /// A submitted bundle satisfies none of the Bundle Progress Criteria.
    CLPR_NO_PROGRESS(571),
    /// Attempted to redact a message slot whose payload is not a Data Message; only Data Messages may be redacted.
    CLPR_MESSAGE_NOT_REDACTABLE(572);

    private final int code;

    ClprTransactionStatus(final int code) {
        this.code = code;
    }

    /// Returns the `code` of this value.
    @Override
    public int code() {
        return code;
    }
}
