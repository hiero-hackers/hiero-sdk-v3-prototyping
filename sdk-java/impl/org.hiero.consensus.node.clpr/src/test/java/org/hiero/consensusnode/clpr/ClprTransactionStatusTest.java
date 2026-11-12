package org.hiero.consensusnode.clpr;

import static org.hiero.sdk.testing.Testing.assertValue;
import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

/// Tests of `ClprTransactionStatus`, generated from the specs.
@SuppressWarnings("deprecation")
@DisplayName("ClprTransactionStatus")
class ClprTransactionStatusTest {

    @Test
    @DisplayName("the constants have the attribute values of the specs")
    @Tag("testId:consensusnode.clpr.ClprTransactionStatus/constants")
    @Tag("hip:1535")
    void shouldProvideTheAttributesOfTheConstants() {
        assertValue(537, ClprTransactionStatus.CLPR_NOT_ENABLED.code());
        assertValue(538, ClprTransactionStatus.INVALID_CLPR_CONFIGURATION.code());
        assertValue(539, ClprTransactionStatus.CLPR_CHAIN_ID_IMMUTABLE.code());
        assertValue(540, ClprTransactionStatus.CLPR_TOO_MANY_SEED_ENDPOINTS.code());
        assertValue(541, ClprTransactionStatus.CLPR_INVALID_SEED_ENDPOINT.code());
        assertValue(542, ClprTransactionStatus.CLPR_CHANNEL_ALREADY_EXISTS.code());
        assertValue(543, ClprTransactionStatus.CLPR_INVALID_SIGNATURE.code());
        assertValue(544, ClprTransactionStatus.CLPR_INVALID_VERIFIER_CONTRACT.code());
        assertValue(545, ClprTransactionStatus.CLPR_VERIFIER_CONFIG_FAILED.code());
        assertValue(546, ClprTransactionStatus.CLPR_COMMITMENT_MISMATCH.code());
        assertValue(547, ClprTransactionStatus.CLPR_CHANNEL_NOT_FOUND.code());
        assertValue(548, ClprTransactionStatus.CLPR_INVALID_CHANNEL_STATUS.code());
        assertValue(549, ClprTransactionStatus.CLPR_MESSAGE_NOT_FOUND.code());
        assertValue(550, ClprTransactionStatus.CLPR_MESSAGE_ALREADY_ACKNOWLEDGED.code());
        assertValue(551, ClprTransactionStatus.CLPR_INVALID_ENDPOINT_SIGNATURE.code());
        assertValue(552, ClprTransactionStatus.CLPR_BUNDLE_VERIFICATION_FAILED.code());
        assertValue(553, ClprTransactionStatus.CLPR_RUNNING_HASH_MISMATCH.code());
        assertValue(554, ClprTransactionStatus.CLPR_QUEUE_FULL.code());
        assertValue(555, ClprTransactionStatus.CLPR_CONNECTOR_NOT_FOUND.code());
        assertValue(556, ClprTransactionStatus.CLPR_CONNECTOR_ALREADY_EXISTS.code());
        assertValue(557, ClprTransactionStatus.CLPR_CONNECTOR_UNAUTHORIZED.code());
        assertValue(558, ClprTransactionStatus.CLPR_INVALID_CONNECTOR_ADDRESS.code());
        assertValue(559, ClprTransactionStatus.CLPR_INVALID_CONNECTOR_CONTRACT.code());
        assertValue(560, ClprTransactionStatus.CLPR_INSUFFICIENT_STAKE.code());
        assertValue(561, ClprTransactionStatus.CLPR_PAYLOAD_TOO_LARGE.code());
        assertValue(562, ClprTransactionStatus.CLPR_AUTHORIZATION_FAILED.code());
        assertValue(563, ClprTransactionStatus.CLPR_MESSAGE_ALREADY_REDACTED.code());
        assertValue(564, ClprTransactionStatus.CLPR_QUEUE_QUOTA_EXCEEDED.code());
        assertValue(565, ClprTransactionStatus.CLPR_INSUFFICIENT_CONNECTOR_BALANCE.code());
        assertValue(566, ClprTransactionStatus.CLPR_INTERNAL_STATE_CORRUPTION.code());
        assertValue(567, ClprTransactionStatus.CLPR_CONNECTOR_HAS_IN_FLIGHT_MESSAGES.code());
        assertValue(568, ClprTransactionStatus.CLPR_BUNDLE_DECODE_FAILED.code());
        assertValue(569, ClprTransactionStatus.CLPR_EMPTY_BUNDLE.code());
        assertValue(570, ClprTransactionStatus.CLPR_RATE_LIMITED.code());
        assertValue(571, ClprTransactionStatus.CLPR_NO_PROGRESS.code());
        assertValue(572, ClprTransactionStatus.CLPR_MESSAGE_NOT_REDACTABLE.code());
    }
}
