package org.hiero.consensusnode.client.internal;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.google.protobuf.ByteString;
import com.hederahashgraph.api.proto.java.TransactionReceipt;
import java.time.ZonedDateTime;
import java.util.List;
import org.hiero.consensusnode.queries.receipts.UntypedReceipt;
import org.hiero.consensusnode.status.TransactionStatus;
import org.hiero.consensusnode.transactions.accounts.AccountCreateReceipt;
import org.hiero.consensusnode.transactions.accounts.AccountCreateTransaction;
import org.hiero.consensusnode.transactions.accounts.TransferReceipt;
import org.hiero.consensusnode.transactions.accounts.TransferTransaction;
import org.hiero.ledger.AccountId;
import org.hiero.ledger.Address;
import org.hiero.ledger.TransactionId;
import org.hiero.nativeToken.ExchangeRate;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

/// Tests of the untyped receipts of the receipt query by id and of typing them (ADR-0013). The spec tests of
/// `queries-receipts` cannot be generated yet: `ExchangeRate` has no default instance.
@DisplayName("Receipts")
class ReceiptsTest {

    private static final TransactionId ID = TransactionId.generateTransactionId(new Address(0L, 0L, "", 1001L));
    private static final ExchangeRate RATE = new DefaultExchangeRate(ZonedDateTime.parse("2026-10-19T12:00:00Z"), 12.5);
    private static final AccountId ACCOUNT = new AccountId(0L, 0L, "", 1002L, null, null);

    private static UntypedReceipt untyped(final TransactionStatus status, final AccountId accountId) {
        return new UntypedReceipt(ID, status, RATE, RATE, accountId, null, null, null, null, null, null, null, null,
                null, null, List.of(), null, null, null);
    }

    @Test
    @DisplayName("an untyped receipt with an account id is typed as the receipt of an account creation")
    @Tag("testId:impl/receipts/an-untyped-receipt-with-an-account-id-is-typed-as-the-receipt-of-an-account-creation")
    void anUntypedReceiptWithAnAccountIdIsTypedAsTheReceiptOfAnAccountCreation() {
        // when
        final AccountCreateReceipt receipt = untyped(HapiTransactionStatus.SUCCESS, ACCOUNT)
                .typed(AccountCreateTransaction.class);

        // then
        assertEquals(ACCOUNT, receipt.accountId());
        assertEquals(ID, receipt.transactionId());
        assertEquals(HapiTransactionStatus.SUCCESS, receipt.status());
    }

    @Test
    @DisplayName("an untyped receipt without an account id is not the receipt of an account creation")
    @Tag("testId:impl/receipts/an-untyped-receipt-without-an-account-id-is-not-the-receipt-of-an-account-creation")
    void anUntypedReceiptWithoutAnAccountIdIsNotTheReceiptOfAnAccountCreation() {
        assertThrows(IllegalArgumentException.class,
                () -> untyped(HapiTransactionStatus.SUCCESS, null).typed(AccountCreateTransaction.class));
        // a failed creation has no account either
        assertThrows(IllegalArgumentException.class,
                () -> untyped(HapiTransactionStatus.INSUFFICIENT_PAYER_BALANCE, null)
                        .typed(AccountCreateTransaction.class));
    }

    @Test
    @DisplayName("a receipt without attributes is typed as the receipt of a transfer")
    @Tag("testId:impl/receipts/a-receipt-without-attributes-is-typed-as-the-receipt-of-a-transfer")
    void aReceiptWithoutAttributesIsTypedAsTheReceiptOfATransfer() {
        final TransferReceipt receipt = untyped(HapiTransactionStatus.SUCCESS, null).typed(TransferTransaction.class);

        assertEquals(ID, receipt.transactionId());
    }

    @Test
    @DisplayName("the protocol message is read into an untyped receipt and written back")
    @Tag("testId:impl/receipts/the-protocol-message-is-read-into-an-untyped-receipt-and-written-back")
    void theProtocolMessageIsReadIntoAnUntypedReceiptAndWrittenBack() {
        // given
        final TransactionReceipt proto = TransactionReceipt.newBuilder()
                .setStatusValue(HapiTransactionStatus.SUCCESS.code())
                .setAccountID(Protobuf.toProto(ACCOUNT))
                .setTopicRunningHash(ByteString.copyFrom(new byte[] {1, 2, 3}))
                .setNewTotalSupply(5L)
                .addAllSerialNumbers(List.of(1L, 2L))
                .build();

        // when
        final UntypedReceipt receipt = Receipts.untyped(ID, proto);

        // then
        assertEquals(ACCOUNT, receipt.accountId());
        assertArrayEquals(new byte[] {1, 2, 3}, receipt.topicRunningHash());
        assertEquals(5L, receipt.totalSupply());
        assertEquals(List.of(1L, 2L), receipt.serials());
        assertNull(receipt.tokenId());
        assertNull(receipt.blockNumber());
        assertEquals(proto, Receipts.toProto(receipt));
    }

    @Test
    @DisplayName("a number the protocol reports as 0 is absent")
    @Tag("testId:impl/receipts/a-number-the-protocol-reports-as-0-is-absent")
    void aNumberTheProtocolReportsAs0IsAbsent() {
        final UntypedReceipt receipt = Receipts.untyped(ID, TransactionReceipt.newBuilder()
                .setStatusValue(HapiTransactionStatus.SUCCESS.code()).build());

        assertNull(receipt.totalSupply());
        assertNull(receipt.nodeId());
        assertNull(receipt.topicSequenceNumber());
    }
}
