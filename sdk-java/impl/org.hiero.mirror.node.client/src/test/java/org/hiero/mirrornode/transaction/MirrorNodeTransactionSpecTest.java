package org.hiero.mirrornode.transaction;

import static org.hiero.sdk.testing.Testing.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/// Tests of the spec `mirror-node-transaction`, generated from the `tests` blocks of its `## Testing` section.
@DisplayName("mirror-node-transaction")
class MirrorNodeTransactionSpecTest {

    /// `mirrornode.transaction/the-basic-transaction-types-carry-the-names-of-the-mirror-node-rest-api`
    ///
    /// Source: this spec (BasicTransactionType, whose protocol names are those of the `TransactionTypes` enum of the Mirror
    /// Node REST OpenAPI spec).
    @Test
    @DisplayName("the basic transaction types carry the names of the Mirror Node REST API")
    void theBasicTransactionTypesCarryTheNamesOfTheMirrorNodeRESTAPI() {
        // when / then
        assertEquals("CRYPTOCREATEACCOUNT", BasicTransactionType.ACCOUNT_CREATE.protocolName());
        assertEquals("CRYPTODELETE", BasicTransactionType.ACCOUNT_DELETE.protocolName());
        assertEquals("CRYPTOUPDATEACCOUNT", BasicTransactionType.ACCOUNT_UPDATE.protocolName());
        assertEquals("CRYPTOTRANSFER", BasicTransactionType.CRYPTO_TRANSFER.protocolName());
        assertEquals("CONSENSUSCREATETOPIC", BasicTransactionType.TOPIC_CREATE.protocolName());
        assertEquals("CONSENSUSSUBMITMESSAGE", BasicTransactionType.TOPIC_MESSAGE_SUBMIT.protocolName());
        assertEquals("TOKENCREATION", BasicTransactionType.TOKEN_CREATE.protocolName());
        assertEquals("TOKENMINT", BasicTransactionType.TOKEN_MINT.protocolName());
        assertEquals("TOKENBURN", BasicTransactionType.TOKEN_BURN.protocolName());
        assertEquals("CONTRACTCREATEINSTANCE", BasicTransactionType.CONTRACT_CREATE.protocolName());
        assertEquals("CONTRACTCALL", BasicTransactionType.CONTRACT_CALL.protocolName());
        assertEquals("ETHEREUMTRANSACTION", BasicTransactionType.ETHEREUM.protocolName());
    }

    /// `mirrornode.transaction/a-basic-transaction-type-is-a-transaction-type-with-its-protocol-name`
    ///
    /// Source: this spec (a BasicTransactionType is a TransactionType and can be used as a filter).
    @Test
    @DisplayName("a basic transaction type is a transaction type with its protocol name")
    void aBasicTransactionTypeIsATransactionTypeWithItsProtocolName() {
        // when
        final TransactionType type = BasicTransactionType.CRYPTO_TRANSFER;

        // then
        assertInstanceOf(BasicTransactionType.class, type);
        assertEquals("CRYPTOTRANSFER", type.protocolName());
    }
}
