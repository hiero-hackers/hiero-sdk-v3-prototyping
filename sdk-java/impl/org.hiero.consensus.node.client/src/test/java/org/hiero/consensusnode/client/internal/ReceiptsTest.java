package org.hiero.consensusnode.client.internal;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.google.protobuf.ByteString;
import com.hederahashgraph.api.proto.java.ExchangeRateSet;
import com.hederahashgraph.api.proto.java.TimestampSeconds;
import com.hederahashgraph.api.proto.java.TransactionReceipt;
import java.time.ZonedDateTime;
import java.util.List;
import org.hiero.consensusnode.queries.receipts.UntypedReceipt;
import org.hiero.consensusnode.status.BasicTransactionStatus;
import org.hiero.consensusnode.status.TransactionStatus;
import org.hiero.consensusnode.transactions.accounts.AccountCreateReceipt;
import org.hiero.consensusnode.transactions.accounts.AccountCreateTransaction;
import org.hiero.consensusnode.transactions.accounts.TransferReceipt;
import org.hiero.consensusnode.transactions.accounts.TransferTransaction;
import org.hiero.hedera.HbarUnit;
import org.hiero.ledger.AccountId;
import org.hiero.ledger.Address;
import org.hiero.ledger.TransactionId;
import org.hiero.nativeToken.ExchangeRate;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

/// Tests of the untyped receipts of the receipt query by id and of typing them (ADR-0013).
@DisplayName("Receipts")
class ReceiptsTest {

    private static final TransactionId ID = TransactionId.generateTransactionId(new Address(0L, 0L, "", 1001L));
    private static final ExchangeRate RATE = new ExchangeRate(30000, HbarUnit.HBAR, 375000,
            ZonedDateTime.parse("2026-10-19T12:00:00Z"));
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
        final AccountCreateReceipt receipt = untyped(BasicTransactionStatus.SUCCESS, ACCOUNT)
                .typed(AccountCreateTransaction.class);

        // then
        assertEquals(ACCOUNT, receipt.accountId());
        assertEquals(ID, receipt.transactionId());
        assertEquals(BasicTransactionStatus.SUCCESS, receipt.status());
    }

    @Test
    @DisplayName("an untyped receipt without an account id is not the receipt of an account creation")
    @Tag("testId:impl/receipts/an-untyped-receipt-without-an-account-id-is-not-the-receipt-of-an-account-creation")
    void anUntypedReceiptWithoutAnAccountIdIsNotTheReceiptOfAnAccountCreation() {
        assertThrows(IllegalArgumentException.class,
                () -> untyped(BasicTransactionStatus.SUCCESS, null).typed(AccountCreateTransaction.class));
        // a failed creation has no account either
        assertThrows(IllegalArgumentException.class,
                () -> untyped(BasicTransactionStatus.INSUFFICIENT_PAYER_BALANCE, null)
                        .typed(AccountCreateTransaction.class));
    }

    @Test
    @DisplayName("a receipt without attributes is typed as the receipt of a transfer")
    @Tag("testId:impl/receipts/a-receipt-without-attributes-is-typed-as-the-receipt-of-a-transfer")
    void aReceiptWithoutAttributesIsTypedAsTheReceiptOfATransfer() {
        final TransferReceipt receipt = untyped(BasicTransactionStatus.SUCCESS, null).typed(TransferTransaction.class);

        assertEquals(ID, receipt.transactionId());
    }

    @Test
    @DisplayName("the protocol message is read into an untyped receipt and written back")
    @Tag("testId:impl/receipts/the-protocol-message-is-read-into-an-untyped-receipt-and-written-back")
    void theProtocolMessageIsReadIntoAnUntypedReceiptAndWrittenBack() {
        // given
        final TransactionReceipt proto = TransactionReceipt.newBuilder()
                .setStatusValue(BasicTransactionStatus.SUCCESS.code())
                .setAccountID(Protobuf.toProto(ACCOUNT))
                .setTopicRunningHash(ByteString.copyFrom(new byte[] {1, 2, 3}))
                .setNewTotalSupply(5L)
                .addAllSerialNumbers(List.of(1L, 2L))
                .build();

        // when
        final UntypedReceipt receipt = Receipts.untyped(ID, proto, HbarUnit.HBAR);

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
    @DisplayName("the exchange rates of a receipt are quoted in the native token unit of the network")
    @Tag("testId:impl/receipts/the-exchange-rates-of-a-receipt-are-quoted-in-the-native-token-unit-of-the-network")
    @Tag("rule:nativeToken/exchange-rate-unit")
    void theExchangeRatesOfAReceiptAreQuotedInTheNativeTokenUnitOfTheNetwork() {
        // given: the current and the next rate of the Java SDK's ExchangeRatesTest
        final TransactionReceipt proto = TransactionReceipt.newBuilder()
                .setStatusValue(BasicTransactionStatus.SUCCESS.code())
                .setExchangeRate(ExchangeRateSet.newBuilder()
                        .setCurrentRate(com.hederahashgraph.api.proto.java.ExchangeRate.newBuilder()
                                .setHbarEquiv(30000).setCentEquiv(580150)
                                .setExpirationTime(TimestampSeconds.newBuilder().setSeconds(1645714800L)))
                        .setNextRate(com.hederahashgraph.api.proto.java.ExchangeRate.newBuilder()
                                .setHbarEquiv(30000).setCentEquiv(587660)
                                .setExpirationTime(TimestampSeconds.newBuilder().setSeconds(1645718400L))))
                .build();

        // when
        final UntypedReceipt receipt = Receipts.untyped(ID, proto, HbarUnit.HBAR);

        // then
        assertEquals(new ExchangeRate(30000, HbarUnit.HBAR, 580150, ZonedDateTime.parse("2022-02-24T15:00:00Z")),
                receipt.exchangeRate());
        assertEquals(new ExchangeRate(30000, HbarUnit.HBAR, 587660, ZonedDateTime.parse("2022-02-24T16:00:00Z")),
                receipt.nextExchangeRate());
    }

    @Test
    @DisplayName("a number the protocol reports as 0 is absent")
    @Tag("testId:impl/receipts/a-number-the-protocol-reports-as-0-is-absent")
    void aNumberTheProtocolReportsAs0IsAbsent() {
        final UntypedReceipt receipt = Receipts.untyped(ID, TransactionReceipt.newBuilder()
                .setStatusValue(BasicTransactionStatus.SUCCESS.code()).build(), HbarUnit.HBAR);

        assertNull(receipt.totalSupply());
        assertNull(receipt.nodeId());
        assertNull(receipt.topicSequenceNumber());
    }
}
