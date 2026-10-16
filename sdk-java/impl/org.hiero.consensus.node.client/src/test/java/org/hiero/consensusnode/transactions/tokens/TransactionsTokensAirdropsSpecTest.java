package org.hiero.consensusnode.transactions.tokens;

import static org.hiero.sdk.testing.Testing.*;

import java.util.List;
import org.hiero.consensusnode.client.Account;
import org.hiero.consensusnode.transactions.PackedTransaction;
import org.hiero.consensusnode.transactions.accounts.NftTransfer;
import org.hiero.consensusnode.transactions.accounts.TokenTransfer;
import org.hiero.keys.ByteImportEncoding;
import org.hiero.keys.KeyAlgorithm;
import org.hiero.keys.KeysFactory;
import org.hiero.keys.PrivateKey;
import org.hiero.ledger.AccountId;
import org.hiero.ledger.Address;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/// Tests of the spec `transactions-tokens-airdrops`, generated from the `tests` blocks of its `## Testing` section.
@DisplayName("transactions-tokens-airdrops")
class TransactionsTokensAirdropsSpecTest {

    /// `consensusnode.transactions.tokens/an-airdrop-transaction-without-transfers-has-no-token-and-no-nft-legs`
    ///
    /// Source: the @@default values of TokenAirdropTransaction; the Python SDK (test_from_proto_without_nft_transfers,
    /// test_from_proto_without_token_transfer) and the Java SDK (TokenAirdropTransactionTest, shouldBytesNoSetters).
    @Test
    @DisplayName("an airdrop transaction without transfers has no token and no NFT legs")
    void anAirdropTransactionWithoutTransfersHasNoTokenAndNoNFTLegs() {
        // when
        final TokenAirdropTransaction airdrop = new TokenAirdropTransaction(List.of(), List.of());

        // then
        assertEquals(0L, length(airdrop.tokenTransfers()));
        assertEquals(0L, length(airdrop.nftTransfers()));
    }

    /// `consensusnode.transactions.tokens/an-airdrop-transaction-keeps-its-fungible-and-nft-legs-in-order`
    ///
    /// Source: the Java SDK (TokenAirdropTransactionTest, spawnTestTransaction: the same legs, among them the expected
    /// decimals 3 and the approved legs) and the Python SDK (test_build_transaction_body_with_approved_transfer,
    /// test_build_transaction_body_with_expected_decimal).
    @Test
    @DisplayName("an airdrop transaction keeps its fungible and NFT legs in order")
    void anAirdropTransactionKeepsItsFungibleAndNFTLegsInOrder() {
        // given
        final Address token5 = Address.fromString("0.0.5");
        final AccountId account5005 = AccountId.fromString("0.0.5005");
        final AccountId account5006 = AccountId.fromString("0.0.5006");
        final AccountId account5007 = AccountId.fromString("0.0.5007");
        final AccountId account5008 = AccountId.fromString("0.0.5008");
        final List<TokenTransfer> tokenTransfers = List.of(new TokenTransfer(token5, account5006, -800L, 3, false), new TokenTransfer(token5, account5007, 400L, 3, false), new TokenTransfer(token5, account5008, 400L, null, false), new TokenTransfer(Address.fromString("0.0.4"), account5006, 123L, null, true));
        final List<NftTransfer> nftTransfers = List.of(new NftTransfer(Address.fromString("0.0.3"), 2L, account5008, account5007, false), new NftTransfer(Address.fromString("0.0.4"), 4L, account5005, account5006, true));

        // when
        final TokenAirdropTransaction airdrop = new TokenAirdropTransaction(tokenTransfers, nftTransfers);

        // then
        assertEquals(tokenTransfers, airdrop.tokenTransfers());
        assertEquals(nftTransfers, airdrop.nftTransfers());
        assertEquals(3, get(airdrop.tokenTransfers(), 0L).expectedDecimals());
        assertNull(get(airdrop.tokenTransfers(), 2L).expectedDecimals());
        assertTrue(get(airdrop.tokenTransfers(), 3L).approved());
        assertFalse(get(airdrop.nftTransfers(), 0L).approved());
        assertTrue(get(airdrop.nftTransfers(), 1L).approved());
    }

    /// `consensusnode.transactions.tokens/a-fungible-pending-airdrop-has-no-serial-and-an-nft-pending-airdrop-has-its-serial`
    ///
    /// Source: PendingAirdrop ("serial is null for a fungible airdrop and set for a specific NFT"); the Java SDK
    /// (PendingAirdropIdTest, testConstructorWithTokenId and testConstructorWithNftId: the same accounts and tokens), the Go
    /// SDK (TestPendingAirdropId_FromProtobuf) and the Python SDK (test_pending_airdrop_id_constructor).
    @Test
    @DisplayName("a fungible pending airdrop has no serial and an NFT pending airdrop has its serial")
    void aFungiblePendingAirdropHasNoSerialAndAnNFTPendingAirdropHasItsSerial() {
        // given
        final AccountId sender = AccountId.fromString("0.0.1001");
        final AccountId receiver = AccountId.fromString("0.0.1002");

        // when
        final PendingAirdrop fungible = new PendingAirdrop(sender, receiver, Address.fromString("0.0.1003"), null);
        final PendingAirdrop nft = new PendingAirdrop(sender, receiver, Address.fromString("0.0.1004"), 1L);

        // then
        assertEquals(sender, fungible.senderId());
        assertEquals(receiver, fungible.receiverId());
        assertEquals(Address.fromString("0.0.1003"), fungible.tokenId());
        assertNull(fungible.serial());
        assertEquals(Address.fromString("0.0.1004"), nft.tokenId());
        assertEquals(1L, nft.serial());
    }

    /// `consensusnode.transactions.tokens/pending-airdrops-are-equal-exactly-when-sender-receiver-token-and-serial-are-equal`
    ///
    /// Source: "Pending-airdrop identity" in this spec ((senderId, receiverId, token), the NFT by token and serial); the C++
    /// SDK (PendingAirdropIdUnitTests, OperatorEqualsFt, OperatorEqualsNft and OperatorNotEqualsDifferentSender / Receiver /
    /// Token: the same accounts and tokens).
    @Test
    @DisplayName("pending airdrops are equal exactly when sender, receiver, token and serial are equal")
    void pendingAirdropsAreEqualExactlyWhenSenderReceiverTokenAndSerialAreEqual() {
        // given
        final AccountId sender = AccountId.fromString("1.2.3");
        final AccountId receiver = AccountId.fromString("4.5.6");
        final Address token = Address.fromString("7.8.9");

        // when
        final PendingAirdrop fungible = new PendingAirdrop(sender, receiver, token, null);
        final PendingAirdrop nft = new PendingAirdrop(sender, receiver, token, 10L);

        // then
        assertEquals(new PendingAirdrop(sender, receiver, token, null), fungible);
        assertEquals(new PendingAirdrop(sender, receiver, token, 10L), nft);
        assertNotEquals(new PendingAirdrop(AccountId.fromString("10.2.3"), receiver, token, null), fungible);
        assertNotEquals(new PendingAirdrop(sender, AccountId.fromString("4.5.10"), token, null), fungible);
        assertNotEquals(new PendingAirdrop(sender, receiver, Address.fromString("7.8.10"), null), fungible);
        assertNotEquals(fungible, nft);
    }

    /// `consensusnode.transactions.tokens/claim-and-cancel-airdrop-transactions-keep-their-pending-airdrops-in-order`
    ///
    /// Source: the Python SDK (token_airdrop_claim_test, test_order_preserved_across_batched_adds: the same pending
    /// airdrops) and the Java SDK (TokenClaimAirdropTransactionTest and TokenCancelAirdropTransactionTest,
    /// testGetAndSetPendingAirdropIds).
    @Test
    @DisplayName("claim and cancel airdrop transactions keep their pending airdrops in order")
    void claimAndCancelAirdropTransactionsKeepTheirPendingAirdropsInOrder() {
        // given
        final AccountId sender = AccountId.fromString("0.0.9031");
        final AccountId receiver = AccountId.fromString("0.0.9032");
        final List<PendingAirdrop> pendingAirdrops = List.of(new PendingAirdrop(sender, receiver, Address.fromString("0.0.9030"), null), new PendingAirdrop(sender, receiver, Address.fromString("0.0.9035"), 1L), new PendingAirdrop(sender, receiver, Address.fromString("0.0.9031"), null), new PendingAirdrop(sender, receiver, Address.fromString("0.0.9035"), 2L));

        // when
        final TokenClaimAirdropTransaction claim = new TokenClaimAirdropTransaction(pendingAirdrops);
        final TokenCancelAirdropTransaction cancel = new TokenCancelAirdropTransaction(pendingAirdrops);

        // then
        assertEquals(pendingAirdrops, claim.pendingAirdrops());
        assertEquals(pendingAirdrops, cancel.pendingAirdrops());
    }

    /// `consensusnode.transactions.tokens/a-token-reject-transaction-without-owner-leaves-the-owner-unset`
    ///
    /// Source: TokenRejectTransaction.owner ("defaults to the payer when unset"); HAPI TokenRejectTransactionBody (an unset
    /// owner is the payer) and the JavaScript SDK (TokenReference, "initializes fungibleToken and nft to null").
    @Test
    @DisplayName("a token reject transaction without owner leaves the owner unset")
    void aTokenRejectTransactionWithoutOwnerLeavesTheOwnerUnset() {
        // when
        final TokenRejectTransaction reject = new TokenRejectTransaction(null, List.of(new TokenReference(Address.fromString("1.2.3"), null)));

        // then
        assertNull(reject.owner());
        assertNull(get(reject.rejections(), 0L).serial());
    }

    /// `consensusnode.transactions.tokens/a-token-reject-transaction-keeps-the-owner-and-its-fungible-and-nft-references-in-order`
    ///
    /// Source: the Java SDK (TokenRejectTransactionTest: owner 0.6.9, the tokens 1.2.3, 4.5.6 and 7.8.9 and the NFTs
    /// 4.5.6/2 and 7.8.9/3) and the Python SDK (test_constructor_with_parameters).
    @Test
    @DisplayName("a token reject transaction keeps the owner and its fungible and NFT references in order")
    void aTokenRejectTransactionKeepsTheOwnerAndItsFungibleAndNFTReferencesInOrder() {
        // given
        final List<TokenReference> rejections = List.of(new TokenReference(Address.fromString("1.2.3"), null), new TokenReference(Address.fromString("4.5.6"), null), new TokenReference(Address.fromString("7.8.9"), null), new TokenReference(Address.fromString("4.5.6"), 2L), new TokenReference(Address.fromString("7.8.9"), 3L));

        // when
        final TokenRejectTransaction reject = new TokenRejectTransaction(AccountId.fromString("0.6.9"), rejections);

        // then
        assertEquals(AccountId.fromString("0.6.9"), reject.owner());
        assertEquals(rejections, reject.rejections());
    }

    /// `consensusnode.transactions.tokens/every-airdrop-transaction-survives-tobytes-and-frombytes-unchanged`
    ///
    /// Source: the Java SDK (shouldBytes / shouldBytesNoSetters of the TokenAirdrop, TokenClaimAirdrop, TokenCancelAirdrop
    /// and TokenReject tests: fromBytes(toBytes()) gives the same transaction), the Swift SDK (testToFromBytes) and the
    /// JavaScript SDK (TokenAirdropTransaction, "from | toBytes").
    @Test
    @DisplayName("every airdrop transaction survives toBytes and fromBytes unchanged")
    void everyAirdropTransactionSurvivesToBytesAndFromBytesUnchanged() {
        // given
        final Account payer = defaultAccount();
        final List<AccountId> nodes = List.of(AccountId.fromString("0.0.3"), AccountId.fromString("0.0.4"));
        final Address token = Address.fromString("0.0.5");
        final AccountId sender = AccountId.fromString("0.0.5006");
        final AccountId receiver = AccountId.fromString("0.0.5008");
        final List<PendingAirdrop> pendingAirdrops = List.of(new PendingAirdrop(sender, receiver, token, null), new PendingAirdrop(sender, receiver, Address.fromString("0.0.3"), 2L));
        final List<PackedTransaction<?, ?>> transactions = List.of(new TokenAirdropTransaction(List.of(new TokenTransfer(token, sender, -400L, null, false), new TokenTransfer(token, receiver, 400L, null, false)), List.of(new NftTransfer(Address.fromString("0.0.3"), 2L, sender, receiver, false))).sign(payer, nodes), new TokenClaimAirdropTransaction(pendingAirdrops).sign(payer, nodes), new TokenCancelAirdropTransaction(pendingAirdrops).sign(payer, nodes), new TokenRejectTransaction(sender, List.of(new TokenReference(token, null), new TokenReference(Address.fromString("0.0.3"), 2L))).sign(payer, nodes));
        for (final PackedTransaction<?, ?> transaction : transactions) {
            // when
            final PackedTransaction<?, ?> restored = PackedTransaction.fromBytes(transaction.toBytes());

            // then
            assertEquals(transaction.toBytes(), restored.toBytes());
            assertEquals(transaction.transactionId(), restored.transactionId());
            assertEquals(transaction.signableBodies(), restored.signableBodies());
            assertEquals(2L, length(restored.nodeSignatures()));
        }
    }

    private static Account defaultAccount() {
        return new Account(defaultAccountId(), defaultPrivateKey());
    }

    private static AccountId defaultAccountId() {
        return new AccountId(0L, 0L, "", 1001L, null, null);
    }

    private static PrivateKey defaultPrivateKey() {
        return KeysFactory.createPrivateKey(KeyAlgorithm.ED25519, ByteImportEncoding.HEX, "d3671a1e98bb22f011c0e4bcf5125590e15d8f21a7017309bb558852039bc75c");
    }
}
