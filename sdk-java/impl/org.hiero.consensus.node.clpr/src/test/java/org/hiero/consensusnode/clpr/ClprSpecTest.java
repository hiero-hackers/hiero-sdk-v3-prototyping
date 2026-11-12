package org.hiero.consensusnode.clpr;

import static org.hiero.sdk.testing.Testing.*;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.hiero.consensusnode.status.TransactionStatus;
import org.hiero.consensusnode.transactions.spi.SpiFactory;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

/// Tests of the spec `clpr`, generated from the `tests` blocks of its `## Testing` section.
@DisplayName("clpr")
class ClprSpecTest {

    /// `consensusnode.clpr/throttles-keep-all-seven-limits`
    ///
    /// Source: the SDK proposal for HIP-1535 (Test Plan 30: a throttle value keeps all seven fields); HAPI
    /// clpr_ledger_configuration.proto (ClprThrottles).
    @Test
    @DisplayName("throttles keep all seven limits")
    @Tag("testId:consensusnode.clpr/throttles-keep-all-seven-limits")
    @Tag("hip:1535")
    void throttlesKeepAllSevenLimits() {
        // when
        final ClprThrottles throttles = new ClprThrottles(10L, 6144L, 1000000L, 100L, 71680L, 4L, 8L);

        // then
        assertEquals(10L, throttles.maxMessagesPerBundle());
        assertEquals(6144L, throttles.maxMessagePayloadBytes());
        assertEquals(1000000L, throttles.maxGasPerMessage());
        assertEquals(100L, throttles.maxQueueDepth());
        assertEquals(71680L, throttles.maxSyncBytes());
        assertEquals(4L, throttles.maxLocalEndpoints());
        assertEquals(8L, throttles.maxPeerEndpoints());
    }

    /// `consensusnode.clpr/throttles-with-a-limit-of-zero-are-rejected`
    ///
    /// Source: the SDK proposal for HIP-1535 (ClprUpdateLedgerConfigurationTransaction: five limits must be strictly
    /// positive, 0 fails with INVALID_CLPR_CONFIGURATION; Test Plan 30: the factory rejects an invalid limit locally);
    /// `@@min(1)` of this spec.
    @Test
    @DisplayName("throttles with a limit of zero are rejected")
    @Tag("testId:consensusnode.clpr/throttles-with-a-limit-of-zero-are-rejected")
    @Tag("hip:1535")
    void throttlesWithALimitOfZeroAreRejected() {
        // when / then
        assertThrows(IllegalArgumentException.class, () -> new ClprThrottles(0L, 6144L, 1000000L, 100L, 71680L, 0L, 0L));
        assertThrows(IllegalArgumentException.class, () -> new ClprThrottles(10L, 0L, 1000000L, 100L, 71680L, 0L, 0L));
        assertThrows(IllegalArgumentException.class, () -> new ClprThrottles(10L, 6144L, 0L, 100L, 71680L, 0L, 0L));
        assertThrows(IllegalArgumentException.class, () -> new ClprThrottles(10L, 6144L, 1000000L, 0L, 71680L, 0L, 0L));
        assertThrows(IllegalArgumentException.class, () -> new ClprThrottles(10L, 6144L, 1000000L, 100L, 0L, 0L, 0L));
    }

    /// `consensusnode.clpr/clpr-statuses-carry-the-codes-of-the-protocol`
    ///
    /// Source: HAPI response_code.proto of consensus node v0.78.2 (the CLPR values and their numbers); the SDK
    /// proposal for HIP-1535 (Response-code coverage: name and number of every value are preserved). CLPR_EMPTY_BUNDLE (569)
    /// is deprecated and therefore not asserted.
    @Test
    @DisplayName("CLPR statuses carry the codes of the protocol")
    @Tag("testId:consensusnode.clpr/clpr-statuses-carry-the-codes-of-the-protocol")
    @Tag("hip:1535")
    void cLPRStatusesCarryTheCodesOfTheProtocol() {
        // when / then
        assertEquals(537, ClprTransactionStatus.CLPR_NOT_ENABLED.code());
        assertEquals(538, ClprTransactionStatus.INVALID_CLPR_CONFIGURATION.code());
        assertEquals(539, ClprTransactionStatus.CLPR_CHAIN_ID_IMMUTABLE.code());
        assertEquals(540, ClprTransactionStatus.CLPR_TOO_MANY_SEED_ENDPOINTS.code());
        assertEquals(541, ClprTransactionStatus.CLPR_INVALID_SEED_ENDPOINT.code());
        assertEquals(542, ClprTransactionStatus.CLPR_CHANNEL_ALREADY_EXISTS.code());
        assertEquals(543, ClprTransactionStatus.CLPR_INVALID_SIGNATURE.code());
        assertEquals(544, ClprTransactionStatus.CLPR_INVALID_VERIFIER_CONTRACT.code());
        assertEquals(545, ClprTransactionStatus.CLPR_VERIFIER_CONFIG_FAILED.code());
        assertEquals(546, ClprTransactionStatus.CLPR_COMMITMENT_MISMATCH.code());
        assertEquals(547, ClprTransactionStatus.CLPR_CHANNEL_NOT_FOUND.code());
        assertEquals(548, ClprTransactionStatus.CLPR_INVALID_CHANNEL_STATUS.code());
        assertEquals(549, ClprTransactionStatus.CLPR_MESSAGE_NOT_FOUND.code());
        assertEquals(550, ClprTransactionStatus.CLPR_MESSAGE_ALREADY_ACKNOWLEDGED.code());
        assertEquals(551, ClprTransactionStatus.CLPR_INVALID_ENDPOINT_SIGNATURE.code());
        assertEquals(552, ClprTransactionStatus.CLPR_BUNDLE_VERIFICATION_FAILED.code());
        assertEquals(553, ClprTransactionStatus.CLPR_RUNNING_HASH_MISMATCH.code());
        assertEquals(554, ClprTransactionStatus.CLPR_QUEUE_FULL.code());
        assertEquals(555, ClprTransactionStatus.CLPR_CONNECTOR_NOT_FOUND.code());
        assertEquals(556, ClprTransactionStatus.CLPR_CONNECTOR_ALREADY_EXISTS.code());
        assertEquals(557, ClprTransactionStatus.CLPR_CONNECTOR_UNAUTHORIZED.code());
        assertEquals(558, ClprTransactionStatus.CLPR_INVALID_CONNECTOR_ADDRESS.code());
        assertEquals(559, ClprTransactionStatus.CLPR_INVALID_CONNECTOR_CONTRACT.code());
        assertEquals(560, ClprTransactionStatus.CLPR_INSUFFICIENT_STAKE.code());
        assertEquals(561, ClprTransactionStatus.CLPR_PAYLOAD_TOO_LARGE.code());
        assertEquals(562, ClprTransactionStatus.CLPR_AUTHORIZATION_FAILED.code());
        assertEquals(563, ClprTransactionStatus.CLPR_MESSAGE_ALREADY_REDACTED.code());
        assertEquals(564, ClprTransactionStatus.CLPR_QUEUE_QUOTA_EXCEEDED.code());
        assertEquals(565, ClprTransactionStatus.CLPR_INSUFFICIENT_CONNECTOR_BALANCE.code());
        assertEquals(566, ClprTransactionStatus.CLPR_INTERNAL_STATE_CORRUPTION.code());
        assertEquals(567, ClprTransactionStatus.CLPR_CONNECTOR_HAS_IN_FLIGHT_MESSAGES.code());
        assertEquals(568, ClprTransactionStatus.CLPR_BUNDLE_DECODE_FAILED.code());
        assertEquals(570, ClprTransactionStatus.CLPR_RATE_LIMITED.code());
        assertEquals(571, ClprTransactionStatus.CLPR_NO_PROGRESS.code());
        assertEquals(572, ClprTransactionStatus.CLPR_MESSAGE_NOT_REDACTABLE.code());
    }

    /// `consensusnode.clpr/a-clpr-code-is-its-clpr-status`
    ///
    /// Source: this spec (the module registers the CLPR codes as a TransactionStatusSupport); transactions-spi.md
    /// (getTransactionStatus returns the status of a registered support).
    @Test
    @DisplayName("a CLPR code is its CLPR status")
    @Tag("testId:consensusnode.clpr/a-clpr-code-is-its-clpr-status")
    @Tag("hip:1535")
    void aCLPRCodeIsItsCLPRStatus() {
        // given
        final TransactionStatus notEnabled = ClprTransactionStatus.CLPR_NOT_ENABLED;
        final TransactionStatus noProgress = ClprTransactionStatus.CLPR_NO_PROGRESS;

        // when / then
        assertEquals(notEnabled, SpiFactory.getTransactionStatus(537));
        assertEquals(noProgress, SpiFactory.getTransactionStatus(571));
    }
}
