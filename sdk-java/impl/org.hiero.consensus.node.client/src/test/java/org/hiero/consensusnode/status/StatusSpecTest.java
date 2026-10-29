package org.hiero.consensusnode.status;

import static org.hiero.sdk.testing.Testing.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

/// Tests of the spec `status`, generated from the `tests` blocks of its `## Testing` section.
@DisplayName("status")
class StatusSpecTest {

    /// `consensusnode.status/the-basic-transaction-statuses-have-the-codes-of-the-protocol`
    ///
    /// Source: ResponseCodeEnum of the HAPI (response_code.proto).
    @Test
    @DisplayName("the basic transaction statuses have the codes of the protocol")
    @Tag("testId:consensusnode.status/the-basic-transaction-statuses-have-the-codes-of-the-protocol")
    void theBasicTransactionStatusesHaveTheCodesOfTheProtocol() {
        assertEquals(0, BasicTransactionStatus.OK.code());
        assertEquals(1, BasicTransactionStatus.INVALID_TRANSACTION.code());
        assertEquals(2, BasicTransactionStatus.PAYER_ACCOUNT_NOT_FOUND.code());
        assertEquals(399, BasicTransactionStatus.GRPC_WEB_PROXY_NOT_SUPPORTED.code());
    }
}
