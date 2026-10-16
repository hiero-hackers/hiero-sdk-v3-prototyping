package org.hiero.mirrornode.transaction;

import static org.hiero.sdk.testing.Testing.*;

import java.time.Duration;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.List;
import org.hiero.ledger.Address;
import org.hiero.ledger.TransactionId;
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

    /// `mirrornode.transaction/a-transaction-info-keeps-its-timestamps-and-its-valid-duration`
    ///
    /// Source: the Hiero Enterprise Java test fixture transaction-list.json, first transaction (CRYPTOTRANSFER, SUCCESS,
    /// charged fee 140686, max fee 1000000, node 0.0.13, consensus timestamp "1785848389.057282104", valid start
    /// "1785848378.760000732", valid duration 120 seconds, no parent, no entity); millisecond precision.
    @Test
    @DisplayName("a transaction info keeps its timestamps and its valid duration")
    void aTransactionInfoKeepsItsTimestampsAndItsValidDuration() {
        // given
        final ZonedDateTime consensus = toZonedDateTime("2026-08-04T12:59:49.057Z");
        final ZonedDateTime validStart = toZonedDateTime("2026-08-04T12:59:38.760Z");

        // when
        final TransactionInfo info = new TransactionInfo(defaultTransactionId(), new byte[0], 140686L, consensus, null, 1000000L, new byte[0], BasicTransactionType.CRYPTO_TRANSFER, 0, "0.0.13", null, TransactionResult.SUCCESS, false, toSeconds(120L), validStart, List.of(), List.of(), List.of(), List.of());

        // then
        assertEquals(toZonedDateTime("2026-08-04T12:59:49.057Z"), info.consensusTimestamp());
        assertEquals(toZonedDateTime("2026-08-04T12:59:38.760Z"), info.validStartTimestamp());
        assertEquals(toSeconds(120L), info.validDuration());
        assertNull(info.parentConsensusTimestamp());
        assertNull(info.entityId());
        assertEquals(140686L, info.chargedTxFee());
        assertEquals(1000000L, info.maxFee());
        assertEquals("0.0.13", info.node());
        assertEquals("CRYPTOTRANSFER", info.name().protocolName());
        assertEquals(TransactionResult.SUCCESS, info.result());
    }

    /// `mirrornode.transaction/a-child-transaction-info-keeps-the-consensus-timestamp-of-its-parent`
    ///
    /// Source: this spec (`parentConsensusTimestamp` is nullable); the Mirror Node REST API (`parent_consensus_timestamp`
    /// is set for a child transaction, with its `nonce` greater than 0).
    @Test
    @DisplayName("a child transaction info keeps the consensus timestamp of its parent")
    void aChildTransactionInfoKeepsTheConsensusTimestampOfItsParent() {
        // given
        final ZonedDateTime parent = toZonedDateTime("2026-08-04T12:59:49.057Z");
        final ZonedDateTime child = toZonedDateTime("2026-08-04T12:59:49.058Z");

        // when
        final TransactionInfo info = new TransactionInfo(defaultTransactionId(), new byte[0], 0L, child, null, 0L, new byte[0], BasicTransactionType.CRYPTO_TRANSFER, 1, null, parent, TransactionResult.SUCCESS, false, Duration.ofSeconds(1L), ZonedDateTime.of(2024, 1, 1, 12, 0, 0, 0, ZoneOffset.UTC), List.of(), List.of(), List.of(), List.of());

        // then
        assertEquals(parent, info.parentConsensusTimestamp());
        assertEquals(child, info.consensusTimestamp());
        assertEquals(1, info.nonce());
    }

    private static TransactionId defaultTransactionId() {
        return TransactionId.generateTransactionId(defaultAddress());
    }

    private static Address defaultAddress() {
        return new Address(0L, 0L, "", 1001L);
    }
}
