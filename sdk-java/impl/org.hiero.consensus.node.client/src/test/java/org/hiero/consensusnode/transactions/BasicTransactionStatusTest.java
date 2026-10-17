package org.hiero.consensusnode.transactions;

import static org.hiero.sdk.testing.Testing.assertValue;
import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

/// Tests of `BasicTransactionStatus`, generated from the specs.
@DisplayName("BasicTransactionStatus")
class BasicTransactionStatusTest {

    @Test
    @DisplayName("the constants have the attribute values of the specs")
    @Tag("testId:consensusnode.transactions.BasicTransactionStatus/constants")
    void shouldProvideTheAttributesOfTheConstants() {
        assertValue(0, BasicTransactionStatus.OK.code());
        assertValue(1, BasicTransactionStatus.INVALID_TRANSACTION.code());
        assertValue(2, BasicTransactionStatus.PAYER_ACCOUNT_NOT_FOUND.code());
        assertValue(399, BasicTransactionStatus.GRPC_WEB_PROXY_NOT_SUPPORTED.code());
    }
}
