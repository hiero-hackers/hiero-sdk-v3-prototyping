package org.hiero.mirrornode.transaction;

import static org.hiero.sdk.testing.Testing.assertValue;
import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/// Tests of `BasicTransactionType`, generated from the specs.
@DisplayName("BasicTransactionType")
class BasicTransactionTypeTest {

    @Test
    @DisplayName("the constants have the attribute values of the specs")
    void shouldProvideTheAttributesOfTheConstants() {
        assertValue("CRYPTOCREATEACCOUNT", BasicTransactionType.ACCOUNT_CREATE.protocolName());
        assertValue("CRYPTODELETE", BasicTransactionType.ACCOUNT_DELETE.protocolName());
        assertValue("CRYPTOUPDATEACCOUNT", BasicTransactionType.ACCOUNT_UPDATE.protocolName());
        assertValue("CRYPTOTRANSFER", BasicTransactionType.CRYPTO_TRANSFER.protocolName());
        assertValue("CONSENSUSCREATETOPIC", BasicTransactionType.TOPIC_CREATE.protocolName());
        assertValue("CONSENSUSSUBMITMESSAGE", BasicTransactionType.TOPIC_MESSAGE_SUBMIT.protocolName());
        assertValue("TOKENCREATION", BasicTransactionType.TOKEN_CREATE.protocolName());
        assertValue("TOKENMINT", BasicTransactionType.TOKEN_MINT.protocolName());
        assertValue("TOKENBURN", BasicTransactionType.TOKEN_BURN.protocolName());
        assertValue("CONTRACTCREATEINSTANCE", BasicTransactionType.CONTRACT_CREATE.protocolName());
        assertValue("CONTRACTCALL", BasicTransactionType.CONTRACT_CALL.protocolName());
        assertValue("ETHEREUMTRANSACTION", BasicTransactionType.ETHEREUM.protocolName());
    }
}
