package org.hiero.consensusnode.transactions.tokens;

import static org.hiero.sdk.testing.Testing.*;

import java.util.List;
import org.hiero.consensusnode.client.Account;
import org.hiero.consensusnode.transactions.PackedTransaction;
import org.hiero.keys.ByteImportEncoding;
import org.hiero.keys.KeyAlgorithm;
import org.hiero.keys.KeysFactory;
import org.hiero.keys.PrivateKey;
import org.hiero.ledger.AccountId;
import org.hiero.ledger.Address;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/// Tests of the spec `transactions-tokens-management`, generated from the `tests` blocks of its `## Testing` section.
@DisplayName("transactions-tokens-management")
class TransactionsTokensManagementSpecTest {

    /// `consensusnode.transactions.tokens/a-fungible-wipe-transaction-keeps-the-token-the-account-and-the-amount`
    ///
    /// Source: the Java SDK (TokenWipeTransactionTest: token 4.2.0, account 0.6.9, amount 4) and the JavaScript SDK
    /// (TokenWipeTransaction, "should handle large bigint amounts": 9000000000000000000).
    @Test
    @DisplayName("a fungible wipe transaction keeps the token, the account and the amount")
    void aFungibleWipeTransactionKeepsTheTokenTheAccountAndTheAmount() {
        // given
        final Address token = Address.fromString("4.2.0");
        final AccountId account = AccountId.fromString("0.6.9");

        // when
        final TokenWipeFungibleTransaction wipe = new TokenWipeFungibleTransaction(token, account, 4L);
        final TokenWipeFungibleTransaction largeWipe = new TokenWipeFungibleTransaction(token, account, 9000000000000000000L);

        // then
        assertEquals(token, wipe.tokenId());
        assertEquals(account, wipe.accountId());
        assertEquals(4L, wipe.amount());
        assertEquals(9000000000000000000L, largeWipe.amount());
    }

    /// `consensusnode.transactions.tokens/an-nft-wipe-transaction-keeps-its-serials-in-order`
    ///
    /// Source: the Java SDK (TokenWipeTransactionTest: serials 8, 9 and 10) and the Python SDK
    /// (test_constructor_with_serial_numbers).
    @Test
    @DisplayName("an NFT wipe transaction keeps its serials in order")
    void anNFTWipeTransactionKeepsItsSerialsInOrder() {
        // when
        final TokenWipeNftTransaction wipe = new TokenWipeNftTransaction(Address.fromString("4.2.0"), AccountId.fromString("0.6.9"), List.of(8L, 9L, 10L));

        // then
        assertEquals(Address.fromString("4.2.0"), wipe.tokenId());
        assertEquals(AccountId.fromString("0.6.9"), wipe.accountId());
        assertEquals(List.of(8L, 9L, 10L), wipe.serials());
    }

    /// `consensusnode.transactions.tokens/freeze-unfreeze-grant-kyc-and-revoke-kyc-transactions-keep-the-token-and-the-account`
    ///
    /// Source: the Java SDK (TokenFreezeTransactionTest and TokenUnfreezeTransactionTest: token 6.5.4, account 0.0.222;
    /// TokenGrantKycTransactionTest and TokenRevokeKycTransactionTest: token 4.2.0, account 6.9.0).
    @Test
    @DisplayName("freeze, unfreeze, grant KYC and revoke KYC transactions keep the token and the account")
    void freezeUnfreezeGrantKYCAndRevokeKYCTransactionsKeepTheTokenAndTheAccount() {
        // given
        final Address frozenToken = Address.fromString("6.5.4");
        final AccountId frozenAccount = AccountId.fromString("0.0.222");
        final Address kycToken = Address.fromString("4.2.0");
        final AccountId kycAccount = AccountId.fromString("6.9.0");

        // when
        final TokenFreezeTransaction freeze = new TokenFreezeTransaction(frozenToken, frozenAccount);
        final TokenUnfreezeTransaction unfreeze = new TokenUnfreezeTransaction(frozenToken, frozenAccount);
        final TokenGrantKycTransaction grantKyc = new TokenGrantKycTransaction(kycToken, kycAccount);
        final TokenRevokeKycTransaction revokeKyc = new TokenRevokeKycTransaction(kycToken, kycAccount);

        // then
        assertEquals(frozenToken, freeze.tokenId());
        assertEquals(frozenAccount, freeze.accountId());
        assertEquals(frozenToken, unfreeze.tokenId());
        assertEquals(frozenAccount, unfreeze.accountId());
        assertEquals(kycToken, grantKyc.tokenId());
        assertEquals(kycAccount, grantKyc.accountId());
        assertEquals(kycToken, revokeKyc.tokenId());
        assertEquals(kycAccount, revokeKyc.accountId());
    }

    /// `consensusnode.transactions.tokens/pause-and-unpause-transactions-keep-the-token`
    ///
    /// Source: the Java SDK (TokenPauseTransactionTest and TokenUnpauseTransactionTest: token 4.2.0) and the Python SDK
    /// (test__from_proto_restores_token_id).
    @Test
    @DisplayName("pause and unpause transactions keep the token")
    void pauseAndUnpauseTransactionsKeepTheToken() {
        // when
        final TokenPauseTransaction pause = new TokenPauseTransaction(Address.fromString("4.2.0"));
        final TokenUnpauseTransaction unpause = new TokenUnpauseTransaction(Address.fromString("4.2.0"));

        // then
        assertEquals(Address.fromString("4.2.0"), pause.tokenId());
        assertEquals(Address.fromString("4.2.0"), unpause.tokenId());
    }

    /// `consensusnode.transactions.tokens/an-nft-metadata-update-without-metadata-leaves-the-metadata-unchanged`
    ///
    /// Source: TokenUpdateNftsTransaction.metadata ("null → leave unchanged"); the JavaScript SDK
    /// (TokenUpdateNftsTransaction, "should _metadata equal to null").
    @Test
    @DisplayName("an NFT metadata update without metadata leaves the metadata unchanged")
    void anNFTMetadataUpdateWithoutMetadataLeavesTheMetadataUnchanged() {
        // when
        final TokenUpdateNftsTransaction update = new TokenUpdateNftsTransaction(Address.fromString("4.2.0"), List.of(8L, 9L, 10L), null);

        // then
        assertNull(update.metadata());
    }

    /// `consensusnode.transactions.tokens/an-nft-metadata-update-keeps-the-token-the-serials-and-the-metadata`
    ///
    /// Source: the Java SDK (TokenUpdateNftsTransactionTest: token 4.2.0, serials 8, 9 and 10, metadata 1, 2, 3, 4, 5) and
    /// the Python SDK (test_constructor_with_parameters).
    @Test
    @DisplayName("an NFT metadata update keeps the token, the serials and the metadata")
    void anNFTMetadataUpdateKeepsTheTokenTheSerialsAndTheMetadata() {
        // when
        final TokenUpdateNftsTransaction update = new TokenUpdateNftsTransaction(Address.fromString("4.2.0"), List.of(8L, 9L, 10L), new byte[] {(byte) 1, (byte) 2, (byte) 3, (byte) 4, (byte) 5});

        // then
        assertEquals(Address.fromString("4.2.0"), update.tokenId());
        assertEquals(List.of(8L, 9L, 10L), update.serials());
        assertEquals(hex("0102030405"), update.metadata());
    }

    /// `consensusnode.transactions.tokens/every-token-management-transaction-survives-tobytes-and-frombytes-unchanged`
    ///
    /// Source: the Java SDK (shouldBytes / shouldBytesFungible / shouldBytesNft of the TokenWipe, TokenFreeze,
    /// TokenUnfreeze, TokenGrantKyc, TokenRevokeKyc, TokenPause, TokenUnpause and TokenUpdateNfts tests:
    /// fromBytes(toBytes()) gives the same transaction); the JavaScript SDK (TokenWipeTransaction, "should round trip from
    /// bytes") and the to / from bytes tests of the Go SDK.
    @Test
    @DisplayName("every token management transaction survives toBytes and fromBytes unchanged")
    void everyTokenManagementTransactionSurvivesToBytesAndFromBytesUnchanged() {
        // given
        final Account payer = defaultAccount();
        final List<AccountId> nodes = List.of(AccountId.fromString("0.0.3"), AccountId.fromString("0.0.4"));
        final Address token = Address.fromString("4.2.0");
        final AccountId account = AccountId.fromString("0.6.9");
        final List<PackedTransaction<?, ?>> transactions = List.of(new TokenWipeFungibleTransaction(token, account, 4L).sign(payer, nodes), new TokenWipeNftTransaction(token, account, List.of(8L, 9L, 10L)).sign(payer, nodes), new TokenFreezeTransaction(token, account).sign(payer, nodes), new TokenUnfreezeTransaction(token, account).sign(payer, nodes), new TokenGrantKycTransaction(token, account).sign(payer, nodes), new TokenRevokeKycTransaction(token, account).sign(payer, nodes), new TokenPauseTransaction(token).sign(payer, nodes), new TokenUnpauseTransaction(token).sign(payer, nodes), new TokenUpdateNftsTransaction(token, List.of(8L, 9L, 10L), new byte[] {(byte) 1, (byte) 2, (byte) 3, (byte) 4, (byte) 5}).sign(payer, nodes));
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
