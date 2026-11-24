package org.hiero.consensusnode.queries.receipts;

import static org.hiero.sdk.testing.Testing.*;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.List;
import org.hiero.consensusnode.status.BasicTransactionStatus;
import org.hiero.consensusnode.status.TransactionStatus;
import org.hiero.consensusnode.transactions.accounts.AccountCreateReceipt;
import org.hiero.consensusnode.transactions.accounts.AccountCreateTransaction;
import org.hiero.consensusnode.transactions.contracts.ContractCreateReceipt;
import org.hiero.consensusnode.transactions.contracts.ContractCreateTransaction;
import org.hiero.consensusnode.transactions.contracts.EthereumTransaction;
import org.hiero.consensusnode.transactions.contracts.EthereumTransactionReceipt;
import org.hiero.consensusnode.transactions.files.FileCreateReceipt;
import org.hiero.consensusnode.transactions.files.FileCreateTransaction;
import org.hiero.consensusnode.transactions.files.FileDeleteReceipt;
import org.hiero.consensusnode.transactions.files.FileDeleteTransaction;
import org.hiero.consensusnode.transactions.schedule.ScheduleCreateReceipt;
import org.hiero.consensusnode.transactions.schedule.ScheduleCreateTransaction;
import org.hiero.consensusnode.transactions.schedule.ScheduleSignReceipt;
import org.hiero.consensusnode.transactions.schedule.ScheduleSignTransaction;
import org.hiero.consensusnode.transactions.tokens.TokenCreateReceipt;
import org.hiero.consensusnode.transactions.tokens.TokenCreateTransaction;
import org.hiero.consensusnode.transactions.tokens.TokenMintFungibleTransaction;
import org.hiero.consensusnode.transactions.tokens.TokenMintNftReceipt;
import org.hiero.consensusnode.transactions.tokens.TokenMintNftTransaction;
import org.hiero.consensusnode.transactions.tokens.TokenMintReceipt;
import org.hiero.consensusnode.transactions.topics.TopicCreateReceipt;
import org.hiero.consensusnode.transactions.topics.TopicCreateTransaction;
import org.hiero.hedera.HbarUnit;
import org.hiero.ledger.AccountId;
import org.hiero.ledger.Address;
import org.hiero.ledger.ContractId;
import org.hiero.ledger.TransactionId;
import org.hiero.nativeToken.ExchangeRate;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

/// Tests of the spec `queries-receipts`, generated from the `tests` blocks of its `## Testing` section.
@DisplayName("queries-receipts")
class QueriesReceiptsSpecTest {

    /// `consensusnode.queries.receipts/an-untyped-receipt-with-an-account-id-is-typed-as-the-receipt-of-an-account-creation`
    ///
    /// Source: this spec (UntypedReceipt.typed: the typed receipt takes the attributes the untyped one carries).
    @Test
    @DisplayName("an untyped receipt with an account id is typed as the receipt of an account creation")
    @Tag("testId:consensusnode.queries.receipts/an-untyped-receipt-with-an-account-id-is-typed-as-the-receipt-of-an-account-creation")
    void anUntypedReceiptWithAnAccountIdIsTypedAsTheReceiptOfAnAccountCreation() {
        // given
        final AccountId account = defaultAccountId();
        final UntypedReceipt untyped = new UntypedReceipt(defaultTransactionId(), defaultTransactionStatus(), defaultExchangeRate(), defaultExchangeRate(), account, null, null, null, null, null, null, null, null, null, null, List.of(), null, null, null);

        // when
        final AccountCreateReceipt receipt = untyped.typed(AccountCreateTransaction.class);

        // then
        assertEquals(account, receipt.accountId());
        assertEquals(untyped.transactionId(), receipt.transactionId());
        assertEquals(untyped.status(), receipt.status());
    }

    /// `consensusnode.queries.receipts/an-untyped-receipt-without-an-account-id-is-not-the-receipt-of-an-account-creation`
    ///
    /// Source: this spec (UntypedReceipt.typed fails when the receipt lacks an attribute the typed receipt requires).
    @Test
    @DisplayName("an untyped receipt without an account id is not the receipt of an account creation")
    @Tag("testId:consensusnode.queries.receipts/an-untyped-receipt-without-an-account-id-is-not-the-receipt-of-an-account-creation")
    void anUntypedReceiptWithoutAnAccountIdIsNotTheReceiptOfAnAccountCreation() {
        // when / then
        assertThrows(IllegalArgumentException.class, () -> defaultUntypedReceipt().typed(AccountCreateTransaction.class));
    }

    /// `consensusnode.queries.receipts/an-untyped-receipt-is-typed-with-the-id-of-the-entity-it-created`
    ///
    /// Source: this spec (UntypedReceipt.typed: the typed receipt of a creation takes the id the untyped one carries); the
    /// Java SDK (TransactionReceiptTest: the receipt of each creation carries the id of the new entity).
    @Test
    @DisplayName("an untyped receipt is typed with the id of the entity it created")
    @Tag("testId:consensusnode.queries.receipts/an-untyped-receipt-is-typed-with-the-id-of-the-entity-it-created")
    void anUntypedReceiptIsTypedWithTheIdOfTheEntityItCreated() {
        // given
        final Address entity = Address.fromString("0.0.5005");
        final ContractId contract = ContractId.fromString("0.0.5006");
        final UntypedReceipt untyped = new UntypedReceipt(defaultTransactionId(), defaultTransactionStatus(), defaultExchangeRate(), defaultExchangeRate(), null, entity, contract, entity, null, null, null, entity, null, null, null, List.of(), null, null, null);

        // when
        final FileCreateReceipt file = untyped.typed(FileCreateTransaction.class);
        final TopicCreateReceipt topic = untyped.typed(TopicCreateTransaction.class);
        final TokenCreateReceipt token = untyped.typed(TokenCreateTransaction.class);
        final ContractCreateReceipt created = untyped.typed(ContractCreateTransaction.class);
        final EthereumTransactionReceipt ethereum = untyped.typed(EthereumTransaction.class);

        // then
        assertEquals(entity, file.fileId());
        assertEquals(entity, topic.topicId());
        assertEquals(entity, token.tokenId());
        assertEquals(contract, created.contractId());
        assertEquals(contract, ethereum.contractId());
        assertEquals(untyped.status(), token.status());
    }

    /// `consensusnode.queries.receipts/an-untyped-receipt-is-typed-with-the-schedule-the-supply-and-the-serials`
    ///
    /// Source: this spec (UntypedReceipt.typed: a schedule creation carries the schedule and the id of the scheduled
    /// transaction, a mint the new total supply and the serial numbers of new NFTs).
    @Test
    @DisplayName("an untyped receipt is typed with the schedule, the supply and the serials")
    @Tag("testId:consensusnode.queries.receipts/an-untyped-receipt-is-typed-with-the-schedule-the-supply-and-the-serials")
    void anUntypedReceiptIsTypedWithTheScheduleTheSupplyAndTheSerials() {
        // given
        final Address schedule = Address.fromString("0.0.5007");
        final TransactionId scheduled = TransactionId.fromString("0.0.1001@1554158542.000000000");
        final UntypedReceipt untyped = new UntypedReceipt(defaultTransactionId(), defaultTransactionStatus(), defaultExchangeRate(), defaultExchangeRate(), null, null, null, null, null, null, null, null, 3L, schedule, scheduled, List.of(1L, 2L, 3L), null, null, null);

        // when
        final ScheduleCreateReceipt created = untyped.typed(ScheduleCreateTransaction.class);
        final ScheduleSignReceipt signed = untyped.typed(ScheduleSignTransaction.class);
        final TokenMintReceipt fungible = untyped.typed(TokenMintFungibleTransaction.class);
        final TokenMintNftReceipt nft = untyped.typed(TokenMintNftTransaction.class);

        // then
        assertEquals(schedule, created.scheduleId());
        assertEquals(scheduled, created.scheduledTransactionId());
        assertEquals(scheduled, signed.scheduledTransactionId());
        assertEquals(3L, fungible.totalSupply());
        assertEquals(3L, nft.totalSupply());
        assertEquals(List.of(1L, 2L, 3L), nft.serials());
    }

    /// `consensusnode.queries.receipts/an-untyped-receipt-is-typed-only-when-nothing-required-is-missing`
    ///
    /// Source: this spec (UntypedReceipt.typed fails when the receipt lacks an attribute the typed receipt requires, and
    /// succeeds when nothing is missing - also for a transaction type the receipt does not belong to, and for an optional
    /// attribute such as the contract of an Ethereum transaction).
    @Test
    @DisplayName("an untyped receipt is typed only when nothing required is missing")
    @Tag("testId:consensusnode.queries.receipts/an-untyped-receipt-is-typed-only-when-nothing-required-is-missing")
    void anUntypedReceiptIsTypedOnlyWhenNothingRequiredIsMissing() {
        // given
        final UntypedReceipt untyped = defaultUntypedReceipt();

        // when / then
        assertThrows(IllegalArgumentException.class, () -> untyped.typed(FileCreateTransaction.class));
        assertThrows(IllegalArgumentException.class, () -> untyped.typed(TokenCreateTransaction.class));
        assertThrows(IllegalArgumentException.class, () -> untyped.typed(ScheduleCreateTransaction.class));
        assertThrows(IllegalArgumentException.class, () -> untyped.typed(TokenMintFungibleTransaction.class));

        // when
        final FileDeleteReceipt deleted = untyped.typed(FileDeleteTransaction.class);
        final EthereumTransactionReceipt ethereum = untyped.typed(EthereumTransaction.class);

        // then
        assertEquals(untyped.transactionId(), deleted.transactionId());
        assertEquals(untyped.status(), deleted.status());
        assertNull(ethereum.contractId());
    }

    /// `consensusnode.queries.receipts/a-receipt-query-asks-only-for-the-receipt-unless-it-names-duplicates-children-or-a-node`
    ///
    /// Source: this spec (@@default(false) of includeDuplicates and includeChildren, @@nullable nodeAccountId: any node of
    /// the client); the Java SDK (TransactionReceiptQueryTest: setIncludeDuplicates, setIncludeChildren) and the Python SDK
    /// (transaction_receipt_query_test.py, test_include_children_and_duplicates).
    @Test
    @DisplayName("a receipt query asks only for the receipt unless it names duplicates, children or a node")
    @Tag("testId:consensusnode.queries.receipts/a-receipt-query-asks-only-for-the-receipt-unless-it-names-duplicates-children-or-a-node")
    void aReceiptQueryAsksOnlyForTheReceiptUnlessItNamesDuplicatesChildrenOrANode() {
        // given
        final TransactionId id = TransactionId.fromString("0.0.1001@1554158542.000000000");
        final AccountId node = AccountId.fromString("0.0.3");

        // when
        final TransactionReceiptQuery plain = new TransactionReceiptQuery(id, false, false, null);
        final TransactionReceiptQuery all = new TransactionReceiptQuery(id, true, true, node);

        // then
        assertEquals(id, plain.transactionId());
        assertFalse(plain.includeDuplicates());
        assertFalse(plain.includeChildren());
        assertNull(plain.nodeAccountId());
        assertTrue(all.includeDuplicates());
        assertTrue(all.includeChildren());
        assertEquals(node, all.nodeAccountId());
    }

    private static AccountId defaultAccountId() {
        return new AccountId(0L, 0L, "", 1001L, null, null);
    }

    private static TransactionId defaultTransactionId() {
        return TransactionId.generateTransactionId(defaultAddress());
    }

    private static Address defaultAddress() {
        return new Address(0L, 0L, "", 1001L);
    }

    private static TransactionStatus defaultTransactionStatus() {
        return BasicTransactionStatus.OK;
    }

    private static ExchangeRate defaultExchangeRate() {
        return new ExchangeRate(30000, HbarUnit.HBAR, 580150, ZonedDateTime.of(2024, 1, 1, 12, 0, 0, 0, ZoneOffset.UTC));
    }

    private static UntypedReceipt defaultUntypedReceipt() {
        return new UntypedReceipt(defaultTransactionId(), defaultTransactionStatus(), defaultExchangeRate(), defaultExchangeRate(), null, null, null, null, null, null, null, null, null, null, null, List.of(), null, null, null);
    }
}
