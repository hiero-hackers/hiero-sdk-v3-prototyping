package org.hiero.consensusnode.transactions.accounts;

import static org.hiero.sdk.testing.Testing.*;

import java.util.List;
import org.hiero.authority.Authority;
import org.hiero.authority.PublicKeyAuthority;
import org.hiero.consensusnode.client.Account;
import org.hiero.consensusnode.transactions.PackedTransaction;
import org.hiero.hedera.Hbar;
import org.hiero.hedera.HbarUnit;
import org.hiero.keys.ByteImportEncoding;
import org.hiero.keys.KeyAlgorithm;
import org.hiero.keys.KeysFactory;
import org.hiero.keys.PrivateKey;
import org.hiero.keys.PublicKey;
import org.hiero.ledger.AccountId;
import org.hiero.ledger.Address;
import org.hiero.nativeToken.NativeToken;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/// Tests of the spec `transactions-accounts`, generated from the `tests` blocks of its `## Testing` section.
@DisplayName("transactions-accounts")
class TransactionsAccountsSpecTest {

    /// `consensusnode.transactions.accounts/an-account-create-transaction-leaves-out-what-is-not-given`
    ///
    /// Source: this spec (@@default(false), @@nullable); the Python SDK (test_constructor_default_values of the account
    /// transactions) and the Java SDK (AccountCreateTransactionTest.propertiesTest: no staked node id, declineStakingReward
    /// false).
    @Test
    @DisplayName("an account create transaction leaves out what is not given")
    void anAccountCreateTransactionLeavesOutWhatIsNotGiven() {
        // when
        final AccountCreateTransaction transaction = new AccountCreateTransaction(defaultAuthority(), null, null, false, null, null, null, null, false, null);

        // then
        assertEquals(defaultAuthority(), transaction.authority());
        assertNull(transaction.initialBalance());
        assertNull(transaction.accountMemo());
        assertFalse(transaction.receiverSignatureRequired());
        assertNull(transaction.maxAutomaticTokenAssociations());
        assertNull(transaction.autoRenewPeriod());
        assertNull(transaction.stakedAccountId());
        assertNull(transaction.stakedNodeId());
        assertFalse(transaction.declineStakingReward());
        assertNull(transaction.alias());
    }

    /// `consensusnode.transactions.accounts/an-account-create-transaction-keeps-its-attributes`
    ///
    /// Source: the Java SDK (AccountCreateTransactionTest.propertiesTest: 450 tinybars, "some dumb memo", 100 automatic
    /// associations, staked to 0.0.3, the EVM address alias 0x5c562e90feaf0eebd33ea75d21024f249d451417) and the Swift SDK
    /// (AccountCreateTransactionUnitTests.test_Properties).
    @Test
    @DisplayName("an account create transaction keeps its attributes")
    void anAccountCreateTransactionKeepsItsAttributes() {
        // given
        final NativeToken<?, ?> initialBalance = new Hbar(450L, HbarUnit.TINYBAR);
        final byte[] alias = hex("5c562e90feaf0eebd33ea75d21024f249d451417");

        // when
        final AccountCreateTransaction transaction = new AccountCreateTransaction(defaultAuthority(), initialBalance, "some dumb memo", true, 100, null, AccountId.fromString("0.0.3"), null, true, alias);

        // then
        assertEquals(initialBalance, transaction.initialBalance());
        assertEquals("some dumb memo", transaction.accountMemo());
        assertTrue(transaction.receiverSignatureRequired());
        assertEquals(100, transaction.maxAutomaticTokenAssociations());
        assertEquals(AccountId.fromString("0.0.3"), transaction.stakedAccountId());
        assertNull(transaction.stakedNodeId());
        assertTrue(transaction.declineStakingReward());
        assertEquals(alias, transaction.alias());
    }

    /// `consensusnode.transactions.accounts/an-account-create-transaction-survives-tobytes-and-frombytes-staked-to-an-account-or-to-a-node`
    ///
    /// Source: the Java SDK (AccountCreateTransactionTest.shouldBytes and shouldBytes2: staked to an account, staked to node
    /// 4), the Swift SDK (test_ToFromBytes and test_ToFromBytes2) and the Python SDK
    /// (test_serialization_round_trip_staked_node_id and ..._staked_account_id).
    @Test
    @DisplayName("an account create transaction survives toBytes and fromBytes, staked to an account or to a node")
    void anAccountCreateTransactionSurvivesToBytesAndFromBytesStakedToAnAccountOrToANode() {
        // given
        final Account payer = defaultAccount();
        final List<AccountId> nodes = List.of(AccountId.fromString("0.0.3"), AccountId.fromString("0.0.4"));
        final AccountCreateTransaction toAccount = new AccountCreateTransaction(defaultAuthority(), null, "Test Account", false, null, null, AccountId.fromString("0.0.8"), null, false, null);
        final AccountCreateTransaction toNode = new AccountCreateTransaction(defaultAuthority(), null, "Test Account", false, null, null, null, 1L, false, null);
        for (final AccountCreateTransaction transaction : List.of(toAccount, toNode)) {
            // given
            final PackedTransaction<AccountCreateReceipt, AccountCreateTransaction> packed = transaction.pack(payer, nodes);

            // when
            final PackedTransaction<?, ?> restored = PackedTransaction.fromBytes(packed.toBytes());

            // then
            assertEquals(packed.toBytes(), restored.toBytes());
            assertEquals(packed.signableBodies(), restored.signableBodies());
        }
    }

    /// `consensusnode.transactions.accounts/an-account-update-transaction-changes-nothing-but-what-it-names`
    ///
    /// Source: this spec (@@nullable: an update changes only what it names); the Python SDK
    /// (test_constructor_without_parameters of AccountUpdateTransaction).
    @Test
    @DisplayName("an account update transaction changes nothing but what it names")
    void anAccountUpdateTransactionChangesNothingButWhatItNames() {
        // when
        final AccountUpdateTransaction transaction = new AccountUpdateTransaction(defaultAccountId(), null, null, null, null, null, null, null, null, null);

        // then
        assertEquals(defaultAccountId(), transaction.accountId());
        assertNull(transaction.authority());
        assertNull(transaction.accountMemo());
        assertNull(transaction.receiverSignatureRequired());
        assertNull(transaction.maxAutomaticTokenAssociations());
        assertNull(transaction.autoRenewPeriod());
        assertNull(transaction.expirationTime());
        assertNull(transaction.stakedAccountId());
        assertNull(transaction.stakedNodeId());
        assertNull(transaction.declineStakingReward());
    }

    /// `consensusnode.transactions.accounts/an-account-update-transaction-survives-tobytes-and-frombytes`
    ///
    /// Source: the Java SDK (AccountUpdateTransactionTest.shouldBytes) and the Swift SDK
    /// (AccountUpdateTransactionUnitTests.test_ToFromBytes).
    @Test
    @DisplayName("an account update transaction survives toBytes and fromBytes")
    void anAccountUpdateTransactionSurvivesToBytesAndFromBytes() {
        // given
        final PackedTransaction<AccountUpdateReceipt, AccountUpdateTransaction> packed = new AccountUpdateTransaction(defaultAccountId(), defaultAuthority(), "Some memo", true, 100, null, null, null, 4L, true).pack(defaultAccount(), List.of(AccountId.fromString("0.0.3")));

        // when
        final PackedTransaction<?, ?> restored = PackedTransaction.fromBytes(packed.toBytes());

        // then
        assertEquals(packed.toBytes(), restored.toBytes());
        assertEquals(packed.signableBodies(), restored.signableBodies());
    }

    /// `consensusnode.transactions.accounts/an-account-delete-transaction-keeps-both-accounts-and-survives-tobytes-and-frombytes`
    ///
    /// Source: the Python SDK (test_constructor_with_parameters of AccountDeleteTransaction), the Java SDK
    /// (AccountDeleteTransactionTest.shouldBytes), the JavaScript SDK ("should serialize and deserialize correctly") and the
    /// Swift SDK (test_ToFromBytes).
    @Test
    @DisplayName("an account delete transaction keeps both accounts and survives toBytes and fromBytes")
    void anAccountDeleteTransactionKeepsBothAccountsAndSurvivesToBytesAndFromBytes() {
        // given
        final AccountDeleteTransaction transaction = new AccountDeleteTransaction(AccountId.fromString("0.0.1001"), AccountId.fromString("0.0.1002"));

        // when
        final PackedTransaction<AccountDeleteReceipt, AccountDeleteTransaction> packed = transaction.pack(defaultAccount(), List.of(AccountId.fromString("0.0.3")));
        final PackedTransaction<?, ?> restored = PackedTransaction.fromBytes(packed.toBytes());

        // then
        assertEquals(AccountId.fromString("0.0.1001"), transaction.accountId());
        assertEquals(AccountId.fromString("0.0.1002"), transaction.transferAccountId());
        assertEquals(packed.signableBodies(), restored.signableBodies());
    }

    /// `consensusnode.transactions.accounts/a-transfer-transaction-without-legs-has-empty-lists`
    ///
    /// Source: this spec (@@default([])); the Python SDK (test_constructor_default_values of TransferTransaction).
    @Test
    @DisplayName("a transfer transaction without legs has empty lists")
    void aTransferTransactionWithoutLegsHasEmptyLists() {
        // when
        final TransferTransaction transaction = new TransferTransaction(List.of(), List.of(), List.of());

        // then
        assertEquals(0L, length(transaction.hbarTransfers()));
        assertEquals(0L, length(transaction.tokenTransfers()));
        assertEquals(0L, length(transaction.nftTransfers()));
    }

    /// `consensusnode.transactions.accounts/a-transfer-leg-is-not-approved-and-has-no-expected-decimals-unless-given`
    ///
    /// Source: this spec (@@default(false) of approved, @@nullable expectedDecimals); the Python SDK (test_add_hbar_transfer,
    /// test_add_token_transfer, test_add_nft_transfer, test_token_transfer_without_expected_decimals).
    @Test
    @DisplayName("a transfer leg is not approved and has no expected decimals unless given")
    void aTransferLegIsNotApprovedAndHasNoExpectedDecimalsUnlessGiven() {
        // when
        final HbarTransfer hbarLeg = new HbarTransfer(defaultAccountId(), new Hbar(-10L, HbarUnit.HBAR), false);
        final TokenTransfer tokenLeg = new TokenTransfer(Address.fromString("0.0.5005"), defaultAccountId(), -100L, null, false);
        final NftTransfer nftLeg = new NftTransfer(Address.fromString("0.0.5005"), 1L, AccountId.fromString("0.0.1001"), AccountId.fromString("0.0.1002"), false);

        // then
        assertFalse(hbarLeg.approved());
        assertFalse(tokenLeg.approved());
        assertNull(tokenLeg.expectedDecimals());
        assertFalse(nftLeg.approved());
    }

    /// `consensusnode.transactions.accounts/a-transfer-transaction-keeps-its-legs-in-their-order`
    ///
    /// Source: this spec (a transfer is a list of legs; nothing merges them); the Python SDK (test_multiple_nft_transfers,
    /// test_nft_transfer_fields_preservation, test_expected_decimals_field_preservation).
    @Test
    @DisplayName("a transfer transaction keeps its legs in their order")
    void aTransferTransactionKeepsItsLegsInTheirOrder() {
        // given
        final Address token = Address.fromString("0.0.5005");
        final AccountId alice = AccountId.fromString("0.0.1001");
        final AccountId bob = AccountId.fromString("0.0.1002");
        final List<HbarTransfer> hbarLegs = List.of(new HbarTransfer(alice, new Hbar(-10L, HbarUnit.HBAR), false), new HbarTransfer(bob, new Hbar(10L, HbarUnit.HBAR), false));
        final List<TokenTransfer> tokenLegs = List.of(new TokenTransfer(token, alice, -100L, 6, true), new TokenTransfer(token, bob, 100L, 6, false));
        final List<NftTransfer> nftLegs = List.of(new NftTransfer(token, 1L, alice, bob, false), new NftTransfer(token, 2L, alice, bob, true));

        // when
        final TransferTransaction transaction = new TransferTransaction(hbarLegs, tokenLegs, nftLegs);

        // then
        assertEquals(hbarLegs, transaction.hbarTransfers());
        assertEquals(tokenLegs, transaction.tokenTransfers());
        assertEquals(6, get(transaction.tokenTransfers(), 0L).expectedDecimals());
        assertTrue(get(transaction.tokenTransfers(), 0L).approved());
        assertEquals(nftLegs, transaction.nftTransfers());
        assertEquals(2L, get(transaction.nftTransfers(), 1L).serial());
    }

    /// `consensusnode.transactions.accounts/a-transfer-of-hbar-a-token-and-an-nft-survives-tobytes-and-frombytes`
    ///
    /// Source: the Python SDK (test_combined_transfers_reconstruction) and the Swift SDK
    /// (TransferTransactionUnitTests.test_ToFromBytes).
    @Test
    @DisplayName("a transfer of HBAR, a token and an NFT survives toBytes and fromBytes")
    void aTransferOfHBARATokenAndAnNFTSurvivesToBytesAndFromBytes() {
        // given
        final Address token = Address.fromString("0.0.5005");
        final AccountId alice = AccountId.fromString("0.0.1001");
        final AccountId bob = AccountId.fromString("0.0.1002");
        final PackedTransaction<TransferReceipt, TransferTransaction> packed = new TransferTransaction(List.of(new HbarTransfer(alice, new Hbar(-10L, HbarUnit.HBAR), false), new HbarTransfer(bob, new Hbar(10L, HbarUnit.HBAR), false)), List.of(new TokenTransfer(token, alice, -100L, 6, false), new TokenTransfer(token, bob, 100L, 6, false)), List.of(new NftTransfer(token, 1L, alice, bob, true))).pack(defaultAccount(), List.of(AccountId.fromString("0.0.3"), AccountId.fromString("0.0.4")));

        // when
        final PackedTransaction<?, ?> restored = PackedTransaction.fromBytes(packed.toBytes());

        // then
        assertEquals(packed.toBytes(), restored.toBytes());
        assertEquals(packed.signableBodies(), restored.signableBodies());
    }

    /// `consensusnode.transactions.accounts/an-allowance-approve-transaction-without-allowances-has-empty-lists`
    ///
    /// Source: this spec (@@default([])); the Python SDK (test_account_allowance_transaction_initialization,
    /// test_build_proto_body_empty).
    @Test
    @DisplayName("an allowance approve transaction without allowances has empty lists")
    void anAllowanceApproveTransactionWithoutAllowancesHasEmptyLists() {
        // when
        final AccountAllowanceApproveTransaction transaction = new AccountAllowanceApproveTransaction(List.of(), List.of(), List.of());

        // then
        assertEquals(0L, length(transaction.hbarAllowances()));
        assertEquals(0L, length(transaction.tokenAllowances()));
        assertEquals(0L, length(transaction.nftAllowances()));
    }

    /// `consensusnode.transactions.accounts/an-allowance-approve-transaction-keeps-hbar-token-serial-and-collection-allowances`
    ///
    /// Source: the Python SDK (test_mixed_allowance_types, test_approve_token_nft_allowance_multiple_serials,
    /// test_approve_token_nft_allowance_all_serials, test_zero_amount_allowances) and the Java SDK
    /// (AccountAllowanceApproveTransactionTest.propertiesTest).
    @Test
    @DisplayName("an allowance approve transaction keeps HBAR, token, serial and collection allowances")
    void anAllowanceApproveTransactionKeepsHBARTokenSerialAndCollectionAllowances() {
        // given
        final AccountId owner = AccountId.fromString("0.0.1001");
        final AccountId spender = AccountId.fromString("0.0.1002");
        final Address token = Address.fromString("0.0.5005");
        final HbarAllowance hbarAllowance = new HbarAllowance(owner, spender, new Hbar(3L, HbarUnit.HBAR));
        final HbarAllowance revokedHbarAllowance = new HbarAllowance(owner, spender, new Hbar(0L, HbarUnit.HBAR));
        final TokenAllowance tokenAllowance = new TokenAllowance(token, owner, spender, 6L);
        final NftAllowance serials = new NftAllowance(token, owner, spender, new NftSerialGrant(List.of(123L, 456L)), null);
        final NftAllowance collection = new NftAllowance(Address.fromString("0.0.6006"), owner, spender, new NftCollectionGrant(true), null);

        // when
        final AccountAllowanceApproveTransaction transaction = new AccountAllowanceApproveTransaction(List.of(hbarAllowance, revokedHbarAllowance), List.of(tokenAllowance), List.of(serials, collection));

        // then
        assertEquals(List.of(hbarAllowance, revokedHbarAllowance), transaction.hbarAllowances());
        assertEquals(List.of(tokenAllowance), transaction.tokenAllowances());
        assertEquals(List.of(serials, collection), transaction.nftAllowances());
        assertInstanceOf(NftSerialGrant.class, get(transaction.nftAllowances(), 0L).grant());
        assertInstanceOf(NftCollectionGrant.class, get(transaction.nftAllowances(), 1L).grant());
        assertNull(get(transaction.nftAllowances(), 0L).delegatingSpender());
    }

    /// `consensusnode.transactions.accounts/an-nft-allowance-keeps-its-delegating-spender`
    ///
    /// Source: HIP-336 (a spender with a collection grant delegates serials onward); this spec (delegatingSpender).
    @Test
    @DisplayName("an NFT allowance keeps its delegating spender")
    void anNFTAllowanceKeepsItsDelegatingSpender() {
        // given
        final AccountId delegatingSpender = AccountId.fromString("0.0.1003");

        // when
        final NftAllowance allowance = new NftAllowance(Address.fromString("0.0.5005"), AccountId.fromString("0.0.1001"), AccountId.fromString("0.0.1002"), new NftSerialGrant(List.of(1L)), delegatingSpender);

        // then
        assertEquals(delegatingSpender, allowance.delegatingSpender());
    }

    /// `consensusnode.transactions.accounts/an-allowance-approve-transaction-survives-tobytes-and-frombytes`
    ///
    /// Source: the Java SDK (AccountAllowanceApproveTransactionTest.shouldBytes), the JavaScript SDK ("should round trip
    /// from bytes and maintain order") and the Swift SDK (test_ToFromBytes).
    @Test
    @DisplayName("an allowance approve transaction survives toBytes and fromBytes")
    void anAllowanceApproveTransactionSurvivesToBytesAndFromBytes() {
        // given
        final AccountId owner = AccountId.fromString("0.0.1001");
        final AccountId spender = AccountId.fromString("0.0.1002");
        final PackedTransaction<AccountAllowanceApproveReceipt, AccountAllowanceApproveTransaction> packed = new AccountAllowanceApproveTransaction(List.of(new HbarAllowance(owner, spender, new Hbar(3L, HbarUnit.HBAR))), List.of(new TokenAllowance(Address.fromString("0.0.5005"), owner, spender, 6L)), List.of(new NftAllowance(Address.fromString("0.0.6006"), owner, spender, new NftSerialGrant(List.of(123L, 456L)), null), new NftAllowance(Address.fromString("0.0.7007"), owner, spender, new NftCollectionGrant(false), null))).pack(defaultAccount(), List.of(AccountId.fromString("0.0.3")));

        // when
        final PackedTransaction<?, ?> restored = PackedTransaction.fromBytes(packed.toBytes());

        // then
        assertEquals(packed.toBytes(), restored.toBytes());
        assertEquals(packed.signableBodies(), restored.signableBodies());
    }

    /// `consensusnode.transactions.accounts/an-allowance-delete-transaction-keeps-its-deletions-and-survives-tobytes-and-frombytes`
    ///
    /// Source: the Java SDK (AccountAllowanceDeleteTransactionTest.shouldBytes), the JavaScript SDK ("[from|to]Bytes"),
    /// the Swift SDK (test_ToFromBytes) and the C++ SDK (DeleteNftAllowances).
    @Test
    @DisplayName("an allowance delete transaction keeps its deletions and survives toBytes and fromBytes")
    void anAllowanceDeleteTransactionKeepsItsDeletionsAndSurvivesToBytesAndFromBytes() {
        // given
        final NftAllowanceDeletion deletion = new NftAllowanceDeletion(Address.fromString("0.0.5005"), AccountId.fromString("0.0.1001"), List.of(1L, 2L, 3L));
        final AccountAllowanceDeleteTransaction transaction = new AccountAllowanceDeleteTransaction(List.of(deletion));

        // when
        final PackedTransaction<AccountAllowanceDeleteReceipt, AccountAllowanceDeleteTransaction> packed = transaction.pack(defaultAccount(), List.of(AccountId.fromString("0.0.3")));
        final PackedTransaction<?, ?> restored = PackedTransaction.fromBytes(packed.toBytes());

        // then
        assertEquals(List.of(deletion), transaction.nftAllowanceDeletions());
        assertEquals(List.of(1L, 2L, 3L), get(transaction.nftAllowanceDeletions(), 0L).serials());
        assertEquals(packed.signableBodies(), restored.signableBodies());
    }

    private static Authority defaultAuthority() {
        return new PublicKeyAuthority(defaultPublicKey());
    }

    private static PublicKey defaultPublicKey() {
        return defaultPrivateKey().createPublicKey();
    }

    private static PrivateKey defaultPrivateKey() {
        return KeysFactory.createPrivateKey(KeyAlgorithm.ED25519, ByteImportEncoding.HEX, "d3671a1e98bb22f011c0e4bcf5125590e15d8f21a7017309bb558852039bc75c");
    }

    private static Account defaultAccount() {
        return new Account(defaultAccountId(), defaultPrivateKey());
    }

    private static AccountId defaultAccountId() {
        return new AccountId(0L, 0L, "", 1001L, null, null);
    }
}
