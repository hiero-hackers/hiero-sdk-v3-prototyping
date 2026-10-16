package org.hiero.consensusnode.transactions.tokens;

import static org.hiero.sdk.testing.Testing.*;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.Set;
import org.hiero.authority.Authority;
import org.hiero.authority.PublicKeyAuthority;
import org.hiero.consensusnode.client.Account;
import org.hiero.consensusnode.transactions.PackedTransaction;
import org.hiero.keys.ByteImportEncoding;
import org.hiero.keys.KeyAlgorithm;
import org.hiero.keys.KeyFormat;
import org.hiero.keys.KeysFactory;
import org.hiero.keys.PrivateKey;
import org.hiero.keys.PublicKey;
import org.hiero.ledger.AccountId;
import org.hiero.ledger.Address;
import org.hiero.token.TokenSupplyType;
import org.hiero.token.TokenType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/// Tests of the spec `transactions-tokens`, generated from the `tests` blocks of its `## Testing` section.
@DisplayName("transactions-tokens")
class TransactionsTokensSpecTest {

    /// The eight token keys of the token tests of the Java SDK (TokenCreateTransactionTest, TokenUpdateTransactionTest), in
    /// the order admin, KYC, freeze, wipe, supply, fee schedule, pause and metadata: the public keys of the Ed25519 PKCS#8
    /// keys ending in 11 to 18.
    private static List<Authority> JAVA_SDK_TOKEN_AUTHORITIES() {
        return List.of(new PublicKeyAuthority(KeysFactory.createPrivateKey(KeyFormat.PKCS8_WITH_DER, hex("302e020100300506032b657004220420" + "db484b828e64b2d8f12ce3c0a0e93a0b8cce7af1bb8f39c97732394482538e11")).createPublicKey()), new PublicKeyAuthority(KeysFactory.createPrivateKey(KeyFormat.PKCS8_WITH_DER, hex("302e020100300506032b657004220420" + "db484b828e64b2d8f12ce3c0a0e93a0b8cce7af1bb8f39c97732394482538e12")).createPublicKey()), new PublicKeyAuthority(KeysFactory.createPrivateKey(KeyFormat.PKCS8_WITH_DER, hex("302e020100300506032b657004220420" + "db484b828e64b2d8f12ce3c0a0e93a0b8cce7af1bb8f39c97732394482538e13")).createPublicKey()), new PublicKeyAuthority(KeysFactory.createPrivateKey(KeyFormat.PKCS8_WITH_DER, hex("302e020100300506032b657004220420" + "db484b828e64b2d8f12ce3c0a0e93a0b8cce7af1bb8f39c97732394482538e14")).createPublicKey()), new PublicKeyAuthority(KeysFactory.createPrivateKey(KeyFormat.PKCS8_WITH_DER, hex("302e020100300506032b657004220420" + "db484b828e64b2d8f12ce3c0a0e93a0b8cce7af1bb8f39c97732394482538e15")).createPublicKey()), new PublicKeyAuthority(KeysFactory.createPrivateKey(KeyFormat.PKCS8_WITH_DER, hex("302e020100300506032b657004220420" + "db484b828e64b2d8f12ce3c0a0e93a0b8cce7af1bb8f39c97732394482538e16")).createPublicKey()), new PublicKeyAuthority(KeysFactory.createPrivateKey(KeyFormat.PKCS8_WITH_DER, hex("302e020100300506032b657004220420" + "db484b828e64b2d8f12ce3c0a0e93a0b8cce7af1bb8f39c97732394482538e17")).createPublicKey()), new PublicKeyAuthority(KeysFactory.createPrivateKey(KeyFormat.PKCS8_WITH_DER, hex("302e020100300506032b657004220420" + "db484b828e64b2d8f12ce3c0a0e93a0b8cce7af1bb8f39c97732394482538e18")).createPublicKey()));
    }

    /// The expiration time of the token tests of the Java SDK (TokenCreateTransactionTest.spawnTestTransactionFungible and
    /// spawnTestTransactionNft: validStart = Instant.ofEpochSecond(1554158542)) and the Rust SDK
    /// (token_create_transaction.rs and token_update_transaction.rs: EXPIRATION_TIME = VALID_START, 1554158542 seconds),
    /// whole seconds.
    private static ZonedDateTime SDK_TEST_EXPIRATION_TIME() {
        return toZonedDateTime("2019-04-01T22:42:22Z");
    }

    /// One character beyond the @@maxLength(100) of the token name, symbol and memo.
    private static String TEXT_OF_101_CHARACTERS() {
        return repeat("a", 101L);
    }

    /// One entry beyond the @@maxSize(10) of the NFT mint metadata (11 entries).
    private static List<byte[]> ELEVEN_METADATA_ENTRIES() {
        return List.of(new byte[] {(byte) 1}, new byte[] {(byte) 2}, new byte[] {(byte) 3}, new byte[] {(byte) 4}, new byte[] {(byte) 5}, new byte[] {(byte) 6}, new byte[] {(byte) 7}, new byte[] {(byte) 8}, new byte[] {(byte) 9}, new byte[] {(byte) 10}, new byte[] {(byte) 11});
    }

    /// One serial beyond the @@maxSize(10) of the NFT burn serials (the serials 1 to 11).
    private static List<Long> ELEVEN_SERIALS() {
        return range(1L, 12L);
    }

    /// `consensusnode.transactions.tokens/a-token-create-transaction-without-optional-values-has-the-defaults-of-the-spec`
    ///
    /// Source: the @@default values of TokenCreateTransaction; the Python SDK (TokenParams: decimals 0, initial supply 0,
    /// supply type INFINITE, freeze default false) and HAPI TokenCreateTransactionBody (no key, no maximum supply, no memo
    /// and no auto-renew account unless set).
    @Test
    @DisplayName("a token create transaction without optional values has the defaults of the spec")
    void aTokenCreateTransactionWithoutOptionalValuesHasTheDefaultsOfTheSpec() {
        // when
        final TokenCreateTransaction transaction = new TokenCreateTransaction("test name", "test symbol", TokenType.FUNGIBLE_COMMON, 0, 0L, AccountId.fromString("7.7.7"), TokenSupplyType.INFINITE, null, false, null, new byte[0], null, null, null, null, null, null, null, null, null, null, null);

        // then
        assertEquals(0, transaction.decimals());
        assertEquals(0L, transaction.initialSupply());
        assertEquals(TokenSupplyType.INFINITE, transaction.supplyType());
        assertFalse(transaction.freezeDefault());
        assertEquals(0L, length(transaction.metadata()));
        assertNull(transaction.maxSupply());
        assertNull(transaction.tokenMemo());
        assertNull(transaction.expirationTime());
        assertNull(transaction.autoRenewPeriod());
        assertNull(transaction.autoRenewAccount());
        assertNull(transaction.adminAuthority());
        assertNull(transaction.kycAuthority());
        assertNull(transaction.freezeAuthority());
        assertNull(transaction.wipeAuthority());
        assertNull(transaction.supplyAuthority());
        assertNull(transaction.feeScheduleAuthority());
        assertNull(transaction.pauseAuthority());
        assertNull(transaction.metadataAuthority());
    }

    /// `consensusnode.transactions.tokens/a-token-create-transaction-for-a-fungible-token-keeps-its-values-and-each-key-in-its-own-field`
    ///
    /// Source: the Java SDK (TokenCreateTransactionTest, constructTokenCreateTransactionFungibleFromTransactionBodyProtobuf:
    /// the same values, an auto-renew period of Duration.ofHours(10) = 36000 seconds; the expiration time of
    /// spawnTestTransactionFungible, Instant.ofEpochSecond(1554158542) = 2019-04-01T22:42:22Z, because the protobuf test uses
    /// Instant.now()); the Rust SDK (token_create_transaction.rs make_transaction: an expiration time and an auto-renew
    /// period together); the getter / setter tests of the Python, Go and Swift SDKs.
    @Test
    @DisplayName("a token create transaction for a fungible token keeps its values and each key in its own field")
    void aTokenCreateTransactionForAFungibleTokenKeepsItsValuesAndEachKeyInItsOwnField() {
        // given
        final List<Authority> authorities = JAVA_SDK_TOKEN_AUTHORITIES();

        // when
        final TokenCreateTransaction transaction = new TokenCreateTransaction("test name", "test symbol", TokenType.FUNGIBLE_COMMON, 3, 30L, AccountId.fromString("7.7.7"), TokenSupplyType.INFINITE, null, true, "test memo", new byte[] {(byte) 1, (byte) 2, (byte) 3, (byte) 4, (byte) 5}, SDK_TEST_EXPIRATION_TIME(), toSeconds(36000L), AccountId.fromString("8.8.8"), get(authorities, 0L), get(authorities, 4L), get(authorities, 1L), get(authorities, 2L), get(authorities, 3L), get(authorities, 6L), get(authorities, 5L), get(authorities, 7L));

        // then
        assertEquals("test name", transaction.name());
        assertEquals("test symbol", transaction.symbol());
        assertEquals(TokenType.FUNGIBLE_COMMON, transaction.tokenType());
        assertEquals(3, transaction.decimals());
        assertEquals(30L, transaction.initialSupply());
        assertEquals(AccountId.fromString("7.7.7"), transaction.treasuryAccountId());
        assertTrue(transaction.freezeDefault());
        assertEquals("test memo", transaction.tokenMemo());
        assertEquals(hex("0102030405"), transaction.metadata());
        assertEquals(SDK_TEST_EXPIRATION_TIME(), transaction.expirationTime());
        assertEquals(toSeconds(36000L), transaction.autoRenewPeriod());
        assertEquals(AccountId.fromString("8.8.8"), transaction.autoRenewAccount());
        assertEquals(get(authorities, 0L), transaction.adminAuthority());
        assertEquals(get(authorities, 1L), transaction.kycAuthority());
        assertEquals(get(authorities, 2L), transaction.freezeAuthority());
        assertEquals(get(authorities, 3L), transaction.wipeAuthority());
        assertEquals(get(authorities, 4L), transaction.supplyAuthority());
        assertEquals(get(authorities, 5L), transaction.feeScheduleAuthority());
        assertEquals(get(authorities, 6L), transaction.pauseAuthority());
        assertEquals(get(authorities, 7L), transaction.metadataAuthority());
    }

    /// `consensusnode.transactions.tokens/a-token-create-transaction-for-a-finite-nft-collection-keeps-its-supply-type-and-maximum-supply`
    ///
    /// Source: the Java SDK (TokenCreateTransactionTest, constructTokenCreateTransactionNftFromTransactionBodyProtobuf:
    /// NON_FUNGIBLE_UNIQUE, FINITE, maximum supply 500); the NFT variants of the Python and Swift SDKs.
    @Test
    @DisplayName("a token create transaction for a finite NFT collection keeps its supply type and maximum supply")
    void aTokenCreateTransactionForAFiniteNFTCollectionKeepsItsSupplyTypeAndMaximumSupply() {
        // when
        final TokenCreateTransaction transaction = new TokenCreateTransaction("test name", "test symbol", TokenType.NON_FUNGIBLE_UNIQUE, 0, 0L, AccountId.fromString("7.7.7"), TokenSupplyType.FINITE, 500L, false, null, new byte[0], null, null, null, null, null, null, null, null, null, null, null);

        // then
        assertEquals(TokenType.NON_FUNGIBLE_UNIQUE, transaction.tokenType());
        assertEquals(TokenSupplyType.FINITE, transaction.supplyType());
        assertEquals(500L, transaction.maxSupply());
        assertEquals(0, transaction.decimals());
        assertEquals(0L, transaction.initialSupply());
    }

    /// `consensusnode.transactions.tokens/a-token-update-transaction-with-only-a-token-id-changes-nothing`
    ///
    /// Source: TokenUpdateTransaction ("every nullable field is leave unchanged when null"); HAPI TokenUpdateTransactionBody
    /// (an unset field is not changed); the Python SDK (test_build_transaction_body: only the fields that are set).
    @Test
    @DisplayName("a token update transaction with only a token id changes nothing")
    void aTokenUpdateTransactionWithOnlyATokenIdChangesNothing() {
        // when
        final TokenUpdateTransaction transaction = new TokenUpdateTransaction(Address.fromString("4.2.0"), null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null);

        // then
        assertEquals(Address.fromString("4.2.0"), transaction.tokenId());
        assertNull(transaction.name());
        assertNull(transaction.symbol());
        assertNull(transaction.treasuryAccountId());
        assertNull(transaction.tokenMemo());
        assertNull(transaction.metadata());
        assertNull(transaction.expirationTime());
        assertNull(transaction.autoRenewPeriod());
        assertNull(transaction.autoRenewAccount());
        assertNull(transaction.adminAuthority());
        assertNull(transaction.kycAuthority());
        assertNull(transaction.freezeAuthority());
        assertNull(transaction.wipeAuthority());
        assertNull(transaction.supplyAuthority());
        assertNull(transaction.feeScheduleAuthority());
        assertNull(transaction.pauseAuthority());
        assertNull(transaction.metadataAuthority());
    }

    /// `consensusnode.transactions.tokens/a-token-update-transaction-keeps-each-rotated-key-in-its-own-field`
    ///
    /// Source: the Java SDK (TokenUpdateTransactionTest, constructTokenUpdateTransactionFromTransactionBodyProtobuf: the
    /// same keys and values, an auto-renew period of Duration.ofHours(10) = 36000 seconds); the Rust SDK
    /// (token_update_transaction.rs: TEST_EXPIRATION_TIME = VALID_START = 2019-04-01T22:42:22Z, because the Java test uses
    /// Instant.now()); the getter / setter tests of the Python SDK (test_set_methods, test_single_key_fields).
    @Test
    @DisplayName("a token update transaction keeps each rotated key in its own field")
    void aTokenUpdateTransactionKeepsEachRotatedKeyInItsOwnField() {
        // given
        final List<Authority> authorities = JAVA_SDK_TOKEN_AUTHORITIES();

        // when
        final TokenUpdateTransaction transaction = new TokenUpdateTransaction(Address.fromString("4.2.0"), "test name", "test symbol", AccountId.fromString("7.7.7"), "test memo", new byte[] {(byte) 1, (byte) 2, (byte) 3, (byte) 4, (byte) 5}, SDK_TEST_EXPIRATION_TIME(), toSeconds(36000L), AccountId.fromString("8.8.8"), get(authorities, 0L), get(authorities, 4L), get(authorities, 1L), get(authorities, 2L), get(authorities, 3L), get(authorities, 6L), get(authorities, 5L), get(authorities, 7L));

        // then
        assertEquals("test name", transaction.name());
        assertEquals("test symbol", transaction.symbol());
        assertEquals(AccountId.fromString("7.7.7"), transaction.treasuryAccountId());
        assertEquals("test memo", transaction.tokenMemo());
        assertEquals(hex("0102030405"), transaction.metadata());
        assertEquals(SDK_TEST_EXPIRATION_TIME(), transaction.expirationTime());
        assertEquals(toSeconds(36000L), transaction.autoRenewPeriod());
        assertEquals(AccountId.fromString("8.8.8"), transaction.autoRenewAccount());
        assertEquals(get(authorities, 0L), transaction.adminAuthority());
        assertEquals(get(authorities, 1L), transaction.kycAuthority());
        assertEquals(get(authorities, 2L), transaction.freezeAuthority());
        assertEquals(get(authorities, 3L), transaction.wipeAuthority());
        assertEquals(get(authorities, 4L), transaction.supplyAuthority());
        assertEquals(get(authorities, 5L), transaction.feeScheduleAuthority());
        assertEquals(get(authorities, 6L), transaction.pauseAuthority());
        assertEquals(get(authorities, 7L), transaction.metadataAuthority());
    }

    /// `consensusnode.transactions.tokens/token-associate-and-dissociate-transactions-keep-the-account-and-the-set-of-tokens-in-any-order`
    ///
    /// Source: TokenAssociateTransaction.tokens ("order is irrelevant — hence a set"); the Java SDK
    /// (TokenAssociateTransactionTest and TokenDissociateTransactionTest: the same account and token ids).
    @Test
    @DisplayName("token associate and dissociate transactions keep the account and the set of tokens in any order")
    void tokenAssociateAndDissociateTransactionsKeepTheAccountAndTheSetOfTokensInAnyOrder() {
        // given
        final Address first = Address.fromString("4.5.6");
        final Address second = Address.fromString("7.8.9");
        final Address third = Address.fromString("10.11.12");
        final Set<Address> reordered = Set.of(third, first, second);

        // when
        final TokenAssociateTransaction associate = new TokenAssociateTransaction(AccountId.fromString("1.2.3"), Set.of(first, second, third));
        final TokenDissociateTransaction dissociate = new TokenDissociateTransaction(AccountId.fromString("1.2.3"), Set.of(first, second, third));

        // then
        assertEquals(AccountId.fromString("1.2.3"), associate.accountId());
        assertEquals(reordered, associate.tokens());
        assertEquals(AccountId.fromString("1.2.3"), dissociate.accountId());
        assertEquals(reordered, dissociate.tokens());
    }

    /// `consensusnode.transactions.tokens/fungible-mint-and-burn-transactions-keep-amounts-beyond-the-53-bits-of-a-javascript-number`
    ///
    /// Source: the JavaScript SDK (TokenMintTransaction and TokenBurnTransaction, "should handle large bigint amounts":
    /// 9000000000000000000); the Java SDK (TokenMintTransactionTest, TokenBurnTransactionTest: token 4.2.0).
    @Test
    @DisplayName("fungible mint and burn transactions keep amounts beyond the 53 bits of a JavaScript number")
    void fungibleMintAndBurnTransactionsKeepAmountsBeyondThe53BitsOfAJavaScriptNumber() {
        // given
        final Address token = Address.fromString("4.2.0");

        // when
        final TokenMintFungibleTransaction mint = new TokenMintFungibleTransaction(token, 9000000000000000000L);
        final TokenBurnFungibleTransaction burn = new TokenBurnFungibleTransaction(token, 9000000000000000000L);

        // then
        assertEquals(token, mint.tokenId());
        assertEquals(9000000000000000000L, mint.amount());
        assertEquals(token, burn.tokenId());
        assertEquals(9000000000000000000L, burn.amount());
    }

    /// `consensusnode.transactions.tokens/an-nft-mint-transaction-keeps-one-metadata-entry-per-new-serial-in-order`
    ///
    /// Source: TokenMintNftTransaction ("one metadata blob per new serial"); the Java SDK (TokenMintTransactionTest,
    /// getSetMetadata: [1, 2, 3, 4, 5]) and the Python SDK (test_build_transaction_body_nft: several entries in order).
    @Test
    @DisplayName("an NFT mint transaction keeps one metadata entry per new serial in order")
    void anNFTMintTransactionKeepsOneMetadataEntryPerNewSerialInOrder() {
        // given
        final List<byte[]> metadata = List.of(hex("0102030405"), utf8("ipfs://QmHash1"), utf8("ipfs://QmHash2"));

        // when
        final TokenMintNftTransaction mint = new TokenMintNftTransaction(Address.fromString("4.2.0"), metadata);

        // then
        assertEquals(3L, length(mint.metadata()));
        assertEquals(metadata, mint.metadata());
    }

    /// `consensusnode.transactions.tokens/an-nft-burn-transaction-keeps-its-serials-in-order`
    ///
    /// Source: the Java SDK (TokenBurnTransactionTest: serial 420) and the Python SDK (token_burn_transaction_test).
    @Test
    @DisplayName("an NFT burn transaction keeps its serials in order")
    void anNFTBurnTransactionKeepsItsSerialsInOrder() {
        // when
        final TokenBurnNftTransaction burn = new TokenBurnNftTransaction(Address.fromString("4.2.0"), List.of(420L, 1L, 2L));

        // then
        assertEquals(Address.fromString("4.2.0"), burn.tokenId());
        assertEquals(List.of(420L, 1L, 2L), burn.serials());
    }

    /// `consensusnode.transactions.tokens/a-token-create-transaction-rejects-a-name-longer-than-100-characters`
    ///
    /// Source: this spec (@@maxLength(100)); the network limits a token name to 100 UTF-8 bytes (TOKEN_NAME_TOO_LONG).
    @Test
    @DisplayName("a token create transaction rejects a name longer than 100 characters")
    void aTokenCreateTransactionRejectsANameLongerThan100Characters() {
        // when / then
        assertThrows(IllegalArgumentException.class, () -> new TokenCreateTransaction(TEXT_OF_101_CHARACTERS(), "test symbol", TokenType.FUNGIBLE_COMMON, 0, 0L, AccountId.fromString("7.7.7"), TokenSupplyType.INFINITE, null, false, null, new byte[0], null, null, null, null, null, null, null, null, null, null, null));
    }

    /// `consensusnode.transactions.tokens/a-token-create-transaction-rejects-a-symbol-longer-than-100-characters`
    ///
    /// Source: this spec (@@maxLength(100)); the network limits a token symbol to 100 UTF-8 bytes (TOKEN_SYMBOL_TOO_LONG).
    @Test
    @DisplayName("a token create transaction rejects a symbol longer than 100 characters")
    void aTokenCreateTransactionRejectsASymbolLongerThan100Characters() {
        // when / then
        assertThrows(IllegalArgumentException.class, () -> new TokenCreateTransaction("test name", TEXT_OF_101_CHARACTERS(), TokenType.FUNGIBLE_COMMON, 0, 0L, AccountId.fromString("7.7.7"), TokenSupplyType.INFINITE, null, false, null, new byte[0], null, null, null, null, null, null, null, null, null, null, null));
    }

    /// `consensusnode.transactions.tokens/a-token-create-transaction-rejects-a-memo-longer-than-100-characters`
    ///
    /// Source: this spec (@@maxLength(100)); the network limits a memo to 100 UTF-8 bytes (MEMO_TOO_LONG).
    @Test
    @DisplayName("a token create transaction rejects a memo longer than 100 characters")
    void aTokenCreateTransactionRejectsAMemoLongerThan100Characters() {
        // when / then
        assertThrows(IllegalArgumentException.class, () -> new TokenCreateTransaction("test name", "test symbol", TokenType.FUNGIBLE_COMMON, 0, 0L, AccountId.fromString("7.7.7"), TokenSupplyType.INFINITE, null, false, TEXT_OF_101_CHARACTERS(), new byte[0], null, null, null, null, null, null, null, null, null, null, null));
    }

    /// `consensusnode.transactions.tokens/a-token-create-transaction-rejects-a-maximum-supply-of-0`
    ///
    /// Source: this spec (@@min(1) of maxSupply); the network rejects a finite token without a positive maximum supply
    /// (INVALID_TOKEN_MAX_SUPPLY).
    @Test
    @DisplayName("a token create transaction rejects a maximum supply of 0")
    void aTokenCreateTransactionRejectsAMaximumSupplyOf0() {
        // when / then
        assertThrows(IllegalArgumentException.class, () -> new TokenCreateTransaction("test name", "test symbol", TokenType.NON_FUNGIBLE_UNIQUE, 0, 0L, AccountId.fromString("7.7.7"), TokenSupplyType.FINITE, 0L, false, null, new byte[0], null, null, null, null, null, null, null, null, null, null, null));
    }

    /// `consensusnode.transactions.tokens/a-token-update-transaction-rejects-a-name-longer-than-100-characters`
    ///
    /// Source: this spec (@@maxLength(100)); the network limits a token name to 100 UTF-8 bytes (TOKEN_NAME_TOO_LONG).
    @Test
    @DisplayName("a token update transaction rejects a name longer than 100 characters")
    void aTokenUpdateTransactionRejectsANameLongerThan100Characters() {
        // when / then
        assertThrows(IllegalArgumentException.class, () -> new TokenUpdateTransaction(Address.fromString("4.2.0"), TEXT_OF_101_CHARACTERS(), null, null, null, null, null, null, null, null, null, null, null, null, null, null, null));
    }

    /// `consensusnode.transactions.tokens/a-token-update-transaction-rejects-a-symbol-longer-than-100-characters`
    ///
    /// Source: this spec (@@maxLength(100)); the network limits a token symbol to 100 UTF-8 bytes (TOKEN_SYMBOL_TOO_LONG).
    @Test
    @DisplayName("a token update transaction rejects a symbol longer than 100 characters")
    void aTokenUpdateTransactionRejectsASymbolLongerThan100Characters() {
        // when / then
        assertThrows(IllegalArgumentException.class, () -> new TokenUpdateTransaction(Address.fromString("4.2.0"), null, TEXT_OF_101_CHARACTERS(), null, null, null, null, null, null, null, null, null, null, null, null, null, null));
    }

    /// `consensusnode.transactions.tokens/a-token-update-transaction-rejects-a-memo-longer-than-100-characters`
    ///
    /// Source: this spec (@@maxLength(100)); the network limits a memo to 100 UTF-8 bytes (MEMO_TOO_LONG).
    @Test
    @DisplayName("a token update transaction rejects a memo longer than 100 characters")
    void aTokenUpdateTransactionRejectsAMemoLongerThan100Characters() {
        // when / then
        assertThrows(IllegalArgumentException.class, () -> new TokenUpdateTransaction(Address.fromString("4.2.0"), null, null, null, TEXT_OF_101_CHARACTERS(), null, null, null, null, null, null, null, null, null, null, null, null));
    }

    /// `consensusnode.transactions.tokens/a-fungible-mint-transaction-rejects-an-amount-of-0`
    ///
    /// Source: this spec (@@min(1) of amount).
    @Test
    @DisplayName("a fungible mint transaction rejects an amount of 0")
    void aFungibleMintTransactionRejectsAnAmountOf0() {
        // when / then
        assertThrows(IllegalArgumentException.class, () -> new TokenMintFungibleTransaction(Address.fromString("4.2.0"), 0L));
    }

    /// `consensusnode.transactions.tokens/an-nft-mint-transaction-rejects-an-empty-list-of-metadata`
    ///
    /// Source: this spec (@@minSize(1): one metadata entry per new serial).
    @Test
    @DisplayName("an NFT mint transaction rejects an empty list of metadata")
    void anNFTMintTransactionRejectsAnEmptyListOfMetadata() {
        // when / then
        assertThrows(IllegalArgumentException.class, () -> new TokenMintNftTransaction(Address.fromString("4.2.0"), List.of()));
    }

    /// `consensusnode.transactions.tokens/an-nft-mint-transaction-rejects-more-than-10-metadata-entries`
    ///
    /// Source: this spec (@@maxSize(10): the network caps a mint at 10 NFTs, TOKEN_MAX_BATCH_SIZE_REACHED).
    @Test
    @DisplayName("an NFT mint transaction rejects more than 10 metadata entries")
    void anNFTMintTransactionRejectsMoreThan10MetadataEntries() {
        // when / then
        assertThrows(IllegalArgumentException.class, () -> new TokenMintNftTransaction(Address.fromString("4.2.0"), ELEVEN_METADATA_ENTRIES()));
    }

    /// `consensusnode.transactions.tokens/a-fungible-burn-transaction-rejects-an-amount-of-0`
    ///
    /// Source: this spec (@@min(1) of amount).
    @Test
    @DisplayName("a fungible burn transaction rejects an amount of 0")
    void aFungibleBurnTransactionRejectsAnAmountOf0() {
        // when / then
        assertThrows(IllegalArgumentException.class, () -> new TokenBurnFungibleTransaction(Address.fromString("4.2.0"), 0L));
    }

    /// `consensusnode.transactions.tokens/an-nft-burn-transaction-rejects-an-empty-list-of-serials`
    ///
    /// Source: this spec (@@minSize(1)).
    @Test
    @DisplayName("an NFT burn transaction rejects an empty list of serials")
    void anNFTBurnTransactionRejectsAnEmptyListOfSerials() {
        // when / then
        assertThrows(IllegalArgumentException.class, () -> new TokenBurnNftTransaction(Address.fromString("4.2.0"), List.of()));
    }

    /// `consensusnode.transactions.tokens/an-nft-burn-transaction-rejects-more-than-10-serials`
    ///
    /// Source: this spec (@@maxSize(10): the network caps a burn at 10 NFTs, TOKEN_MAX_BATCH_SIZE_REACHED).
    @Test
    @DisplayName("an NFT burn transaction rejects more than 10 serials")
    void anNFTBurnTransactionRejectsMoreThan10Serials() {
        // when / then
        assertThrows(IllegalArgumentException.class, () -> new TokenBurnNftTransaction(Address.fromString("4.2.0"), ELEVEN_SERIALS()));
    }

    /// `consensusnode.transactions.tokens/every-token-lifecycle-transaction-survives-tobytes-and-frombytes-unchanged`
    ///
    /// Source: the Java SDK (shouldBytes / shouldBytesFungible / shouldBytesNft of the TokenCreate, TokenUpdate,
    /// TokenDelete, TokenAssociate, TokenDissociate, TokenMint and TokenBurn tests: fromBytes(toBytes()) gives the same
    /// transaction; TokenCreate with the expiration time validStart = Instant.ofEpochSecond(1554158542) = 2019-04-01T22:42:22Z,
    /// which clears its auto-renew period there, TokenUpdate with the auto-renew period Duration.ofHours(10) = 36000
    /// seconds); the Rust SDK (token_update_transaction.rs to_from_bytes: the expiration time VALID_START); the JavaScript
    /// SDK ("should round trip from bytes") and the to / from bytes tests of the Go SDK.
    @Test
    @DisplayName("every token lifecycle transaction survives toBytes and fromBytes unchanged")
    void everyTokenLifecycleTransactionSurvivesToBytesAndFromBytesUnchanged() {
        // given
        final Account payer = defaultAccount();
        final List<AccountId> nodes = List.of(AccountId.fromString("0.0.3"), AccountId.fromString("0.0.4"));
        final Address token = Address.fromString("4.2.0");
        final AccountId account = AccountId.fromString("0.6.9");
        final List<PackedTransaction<?, ?>> transactions = List.of(new TokenCreateTransaction("test name", "test symbol", TokenType.FUNGIBLE_COMMON, 3, 30L, account, TokenSupplyType.INFINITE, null, false, null, new byte[0], SDK_TEST_EXPIRATION_TIME(), null, null, defaultAuthority(), null, null, null, null, null, null, null).sign(payer, nodes), new TokenUpdateTransaction(token, "test name", null, null, null, null, SDK_TEST_EXPIRATION_TIME(), toSeconds(36000L), null, null, null, null, null, null, null, null, null).sign(payer, nodes), new TokenDeleteTransaction(token).sign(payer, nodes), new TokenAssociateTransaction(account, Set.of(token)).sign(payer, nodes), new TokenDissociateTransaction(account, Set.of(token)).sign(payer, nodes), new TokenMintFungibleTransaction(token, 10L).sign(payer, nodes), new TokenMintNftTransaction(token, List.of(new byte[] {(byte) 1, (byte) 2, (byte) 3, (byte) 4, (byte) 5})).sign(payer, nodes), new TokenBurnFungibleTransaction(token, 69L).sign(payer, nodes), new TokenBurnNftTransaction(token, List.of(420L)).sign(payer, nodes));
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

    /// `consensusnode.transactions.tokens/a-token-create-transaction-rejects-metadata-longer-than-100-bytes`
    ///
    /// Source: this spec (@@maxSize(100) of metadata); HIP-646 and the consensus node (tokens.maxMetadataBytes = 100,
    /// METADATA_TOO_LONG); the Python SDK (TokenCreateTransaction.set_metadata rejects more than 100 bytes).
    @Test
    @DisplayName("a token create transaction rejects metadata longer than 100 bytes")
    void aTokenCreateTransactionRejectsMetadataLongerThan100Bytes() {
        // when / then
        assertThrows(IllegalArgumentException.class, () -> new TokenCreateTransaction("test name", "test symbol", TokenType.FUNGIBLE_COMMON, 0, 0L, AccountId.fromString("7.7.7"), TokenSupplyType.INFINITE, null, false, null, repeat(hex("78"), 101L), null, null, null, null, null, null, null, null, null, null, null));
    }

    /// `consensusnode.transactions.tokens/a-token-update-transaction-rejects-metadata-longer-than-100-bytes`
    ///
    /// Source: this spec (@@maxSize(100) of metadata); HIP-646 and the consensus node (tokens.maxMetadataBytes = 100).
    @Test
    @DisplayName("a token update transaction rejects metadata longer than 100 bytes")
    void aTokenUpdateTransactionRejectsMetadataLongerThan100Bytes() {
        // when / then
        assertThrows(IllegalArgumentException.class, () -> new TokenUpdateTransaction(Address.fromString("4.2.0"), null, null, null, null, repeat(hex("78"), 101L), null, null, null, null, null, null, null, null, null, null, null));
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

    private static Authority defaultAuthority() {
        return new PublicKeyAuthority(defaultPublicKey());
    }

    private static PublicKey defaultPublicKey() {
        return defaultPrivateKey().createPublicKey();
    }
}
