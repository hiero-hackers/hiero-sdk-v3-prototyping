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
        // when / then
        assertEquals(0, BasicTransactionStatus.OK.code());
        assertEquals(1, BasicTransactionStatus.INVALID_TRANSACTION.code());
        assertEquals(2, BasicTransactionStatus.PAYER_ACCOUNT_NOT_FOUND.code());
        assertEquals(399, BasicTransactionStatus.GRPC_WEB_PROXY_NOT_SUPPORTED.code());
        assertEquals(535, BasicTransactionStatus.MAX_REGISTERED_NODES_EXCEEDED.code());
    }

    /// `consensusnode.status/the-statuses-of-the-default-execution-have-the-codes-of-the-protocol`
    ///
    /// Source: ResponseCodeEnum of the HAPI (response_code.proto): the statuses that "The outcome of an attempt" in
    /// client.md names, and the status of a successful transaction.
    @Test
    @DisplayName("the statuses of the default execution have the codes of the protocol")
    @Tag("testId:consensusnode.status/the-statuses-of-the-default-execution-have-the-codes-of-the-protocol")
    void theStatusesOfTheDefaultExecutionHaveTheCodesOfTheProtocol() {
        // when / then
        assertEquals(22, BasicTransactionStatus.SUCCESS.code());
        assertEquals(3, BasicTransactionStatus.INVALID_NODE_ACCOUNT.code());
        assertEquals(11, BasicTransactionStatus.DUPLICATE_TRANSACTION.code());
        assertEquals(12, BasicTransactionStatus.BUSY.code());
        assertEquals(18, BasicTransactionStatus.RECEIPT_NOT_FOUND.code());
        assertEquals(19, BasicTransactionStatus.RECORD_NOT_FOUND.code());
        assertEquals(21, BasicTransactionStatus.UNKNOWN.code());
        assertEquals(67, BasicTransactionStatus.PLATFORM_NOT_ACTIVE.code());
        assertEquals(69, BasicTransactionStatus.PLATFORM_TRANSACTION_NOT_CREATED.code());
    }
}
