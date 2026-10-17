package org.hiero.enterprise.service.nft;

import static org.hiero.sdk.testing.Testing.*;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.time.Duration;
import java.time.ZonedDateTime;
import org.hiero.authority.Authority;
import org.hiero.authority.PublicKeyAuthority;
import org.hiero.keys.ByteImportEncoding;
import org.hiero.keys.KeyAlgorithm;
import org.hiero.keys.KeysFactory;
import org.hiero.keys.PrivateKey;
import org.hiero.keys.PublicKey;
import org.hiero.ledger.AccountId;
import org.hiero.token.TokenSupplyType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/// Tests of the spec `service-nft`, generated from the `tests` blocks of its `## Testing` section.
@DisplayName("service-nft")
class ServiceNftSpecTest {

    /// `enterprise.service.nft/nft-type-parameters-with-only-name-and-symbol-have-the-defaults-of-the-spec`
    ///
    /// Source: this spec (@@default and @@nullable of NftTypeCreateParams).
    @Test
    @DisplayName("NFT type parameters with only name and symbol have the defaults of the spec")
    void nFTTypeParametersWithOnlyNameAndSymbolHaveTheDefaultsOfTheSpec() {
        // when
        final NftTypeCreateParams params = defaultNftTypeCreateParams();

        // then
        assertEquals("Nft Tokens", params.name());
        assertEquals("NT", params.symbol());
        assertEquals(TokenSupplyType.INFINITE, params.supplyType());
        assertFalse(params.freezeDefault());
        assertEquals(0L, length(params.metadata()));
        assertNull(params.treasuryAccountId());
        assertNull(params.maxSupply());
        assertNull(params.tokenMemo());
        assertNull(params.expirationTime());
        assertNull(params.autoRenewPeriod());
        assertNull(params.autoRenewAccount());
        assertNull(params.adminAuthority());
        assertNull(params.supplyAuthority());
        assertNull(params.kycAuthority());
        assertNull(params.freezeAuthority());
        assertNull(params.wipeAuthority());
        assertNull(params.pauseAuthority());
        assertNull(params.feeScheduleAuthority());
        assertNull(params.metadataAuthority());
    }

    /// `enterprise.service.nft/nft-type-parameters-keep-the-values-they-are-created-with`
    ///
    /// Source: the TokenCreateTransaction unit tests of the Java (TokenCreateTransactionTest), Go
    /// (token_create_transaction_unit_test.go), C++ (TokenCreateTransactionUnitTests, with NON_FUNGIBLE_UNIQUE and FINITE)
    /// and Python (token_create_transaction_test.py, test_build_transaction_body_non_fungible) SDKs: every value that is
    /// set is read back unchanged. The expiration time and the auto-renew period are those of the Java test
    /// (spawnTestTransactionNft: Instant.ofEpochSecond(1554158542) and Duration.ofHours(10), converted exactly to
    /// 2019-04-01T22:42:22Z and 36000 seconds).
    @Test
    @DisplayName("NFT type parameters keep the values they are created with")
    void nFTTypeParametersKeepTheValuesTheyAreCreatedWith() {
        // given
        final Authority authority = defaultAuthority();
        final AccountId treasury = defaultAccountId();
        final byte[] metadata = utf8("ipfs://bafkreigh2akiscaildc");
        final ZonedDateTime expirationTime = toZonedDateTime("2019-04-01T22:42:22Z");
        final Duration autoRenewPeriod = toSeconds(36000L);

        // when
        final NftTypeCreateParams params = new NftTypeCreateParams("Nft Tokens", "NT", treasury, TokenSupplyType.FINITE, 10L, true, "memo", metadata, expirationTime, autoRenewPeriod, treasury, authority, authority, authority, authority, authority, authority, authority, authority);

        // then
        assertEquals("Nft Tokens", params.name());
        assertEquals("NT", params.symbol());
        assertEquals(treasury, params.treasuryAccountId());
        assertEquals(TokenSupplyType.FINITE, params.supplyType());
        assertEquals(10L, params.maxSupply());
        assertTrue(params.freezeDefault());
        assertEquals("memo", params.tokenMemo());
        assertEquals(metadata, params.metadata());
        assertEquals(expirationTime, params.expirationTime());
        assertEquals(autoRenewPeriod, params.autoRenewPeriod());
        assertEquals(treasury, params.autoRenewAccount());
        assertEquals(authority, params.adminAuthority());
        assertEquals(authority, params.supplyAuthority());
        assertEquals(authority, params.kycAuthority());
        assertEquals(authority, params.freezeAuthority());
        assertEquals(authority, params.wipeAuthority());
        assertEquals(authority, params.pauseAuthority());
        assertEquals(authority, params.feeScheduleAuthority());
        assertEquals(authority, params.metadataAuthority());
    }

    /// `enterprise.service.nft/an-nft-type-accepts-a-name-a-symbol-and-a-memo-of-100-characters`
    ///
    /// Source: this spec (@@maxBytes(100) of name, symbol and tokenMemo); HAPI limits them to 100 bytes, which ASCII text
    /// of 100 characters is.
    @Test
    @DisplayName("an NFT type accepts a name, a symbol and a memo of 100 characters")
    void anNFTTypeAcceptsANameASymbolAndAMemoOf100Characters() {
        // given
        final String text = repeat("a", 100L);

        // when
        final NftTypeCreateParams params = new NftTypeCreateParams(text, text, null, TokenSupplyType.INFINITE, null, false, text, new byte[0], null, null, null, null, null, null, null, null, null, null, null);

        // then
        assertEquals(100L, length(params.name()));
        assertEquals(100L, length(params.symbol()));
        assertEquals(text, params.tokenMemo());
    }

    /// `enterprise.service.nft/a-finite-nft-type-accepts-a-max-supply-of-1`
    ///
    /// Source: this spec (@@min(1) of maxSupply, which applies to a FINITE supply type).
    @Test
    @DisplayName("a finite NFT type accepts a max supply of 1")
    void aFiniteNFTTypeAcceptsAMaxSupplyOf1() {
        // when
        final NftTypeCreateParams params = new NftTypeCreateParams("Nft Tokens", "NT", null, TokenSupplyType.FINITE, 1L, false, null, new byte[0], null, null, null, null, null, null, null, null, null, null, null);

        // then
        assertEquals(TokenSupplyType.FINITE, params.supplyType());
        assertEquals(1L, params.maxSupply());
    }

    /// `enterprise.service.nft/nft-type-parameters-reject-a-name-of-101-characters`
    ///
    /// Source: this spec (@@maxBytes(100) of name); HAPI limits a token name to 100 bytes.
    @Test
    @DisplayName("NFT type parameters reject a name of 101 characters")
    void nFTTypeParametersRejectANameOf101Characters() {
        // when / then
        assertThrows(IllegalArgumentException.class, () -> new NftTypeCreateParams(repeat("a", 101L), "NT", null, TokenSupplyType.INFINITE, null, false, null, new byte[0], null, null, null, null, null, null, null, null, null, null, null));
    }

    /// `enterprise.service.nft/nft-type-parameters-reject-a-symbol-of-101-characters`
    ///
    /// Source: this spec (@@maxBytes(100) of symbol); HAPI limits a token symbol to 100 bytes.
    @Test
    @DisplayName("NFT type parameters reject a symbol of 101 characters")
    void nFTTypeParametersRejectASymbolOf101Characters() {
        // when / then
        assertThrows(IllegalArgumentException.class, () -> new NftTypeCreateParams("Nft Tokens", repeat("a", 101L), null, TokenSupplyType.INFINITE, null, false, null, new byte[0], null, null, null, null, null, null, null, null, null, null, null));
    }

    /// `enterprise.service.nft/nft-type-parameters-reject-a-memo-of-101-characters`
    ///
    /// Source: this spec (@@maxBytes(100) of tokenMemo); HAPI limits a memo to 100 bytes.
    @Test
    @DisplayName("NFT type parameters reject a memo of 101 characters")
    void nFTTypeParametersRejectAMemoOf101Characters() {
        // when / then
        assertThrows(IllegalArgumentException.class, () -> new NftTypeCreateParams("Nft Tokens", "NT", null, TokenSupplyType.INFINITE, null, false, repeat("a", 101L), new byte[0], null, null, null, null, null, null, null, null, null, null, null));
    }

    /// `enterprise.service.nft/a-finite-nft-type-rejects-a-max-supply-of-0`
    ///
    /// Source: this spec (@@min(1) of maxSupply).
    @Test
    @DisplayName("a finite NFT type rejects a max supply of 0")
    void aFiniteNFTTypeRejectsAMaxSupplyOf0() {
        // when / then
        assertThrows(IllegalArgumentException.class, () -> new NftTypeCreateParams("Nft Tokens", "NT", null, TokenSupplyType.FINITE, 0L, false, null, new byte[0], null, null, null, null, null, null, null, null, null, null, null));
    }

    /// `enterprise.service.nft/nft-type-parameters-reject-metadata-longer-than-100-bytes`
    ///
    /// Source: this spec (@@maxSize(100) of metadata); HIP-646 and the consensus node (tokens.maxMetadataBytes = 100).
    @Test
    @DisplayName("NFT type parameters reject metadata longer than 100 bytes")
    void nFTTypeParametersRejectMetadataLongerThan100Bytes() {
        // when / then
        assertThrows(IllegalArgumentException.class, () -> new NftTypeCreateParams("Nft Tokens", "NT", null, TokenSupplyType.INFINITE, null, false, null, repeat(hex("78"), 101L), null, null, null, null, null, null, null, null, null, null, null));
    }

    private static NftTypeCreateParams defaultNftTypeCreateParams() {
        return new NftTypeCreateParams("Nft Tokens", "NT", null, TokenSupplyType.INFINITE, null, false, null, new byte[0], null, null, null, null, null, null, null, null, null, null, null);
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

    private static AccountId defaultAccountId() {
        return new AccountId(0L, 0L, "", 1001L, null, null);
    }
}
