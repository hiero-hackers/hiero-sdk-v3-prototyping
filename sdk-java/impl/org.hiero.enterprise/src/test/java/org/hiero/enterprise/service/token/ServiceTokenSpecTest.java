package org.hiero.enterprise.service.token;

import static org.hiero.sdk.testing.Testing.*;

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

/// Tests of the spec `service-token`, generated from the `tests` blocks of its `## Testing` section.
@DisplayName("service-token")
class ServiceTokenSpecTest {

    /// `enterprise.service.token/token-parameters-with-only-name-and-symbol-have-the-defaults-of-the-spec`
    ///
    /// Source: this spec (@@default and @@nullable of TokenCreateParams); the Python SDK
    /// (token_create_transaction_test.py, test_overwrite_defaults: decimals and initial supply are 0, no expiration time).
    @Test
    @DisplayName("token parameters with only name and symbol have the defaults of the spec")
    void tokenParametersWithOnlyNameAndSymbolHaveTheDefaultsOfTheSpec() {
        // when
        final TokenCreateParams params = defaultTokenCreateParams();

        // then
        assertEquals("Token Name", params.name());
        assertEquals("TKN", params.symbol());
        assertEquals(0, params.decimals());
        assertEquals(0L, params.initialSupply());
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

    /// `enterprise.service.token/token-parameters-keep-the-values-they-are-created-with`
    ///
    /// Source: the TokenCreateTransaction unit tests of the Java (TokenCreateTransactionTest), Go
    /// (token_create_transaction_unit_test.go), C++ (TokenCreateTransactionUnitTests), Swift
    /// (TokenCreateTransactionUnitTests) and Python (token_create_transaction_test.py) SDKs: every value that is set is
    /// read back unchanged.
    @Test
    @DisplayName("token parameters keep the values they are created with")
    void tokenParametersKeepTheValuesTheyAreCreatedWith() {
        // given
        final Authority authority = defaultAuthority();
        final AccountId treasury = defaultAccountId();
        final byte[] metadata = utf8("ipfs://bafkreigh2akiscaildc");

        // when
        final TokenCreateParams params = new TokenCreateParams("USD Coin", "USDC", treasury, 6, 1_000_000L, TokenSupplyType.FINITE, 10_000_000L, true, "memo", metadata, null, null, treasury, authority, authority, authority, authority, authority, authority, authority, authority);

        // then
        assertEquals("USD Coin", params.name());
        assertEquals("USDC", params.symbol());
        assertEquals(treasury, params.treasuryAccountId());
        assertEquals(6, params.decimals());
        assertEquals(1_000_000L, params.initialSupply());
        assertEquals(TokenSupplyType.FINITE, params.supplyType());
        assertEquals(10_000_000L, params.maxSupply());
        assertTrue(params.freezeDefault());
        assertEquals("memo", params.tokenMemo());
        assertEquals(metadata, params.metadata());
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

    /// `enterprise.service.token/a-name-a-symbol-and-a-memo-of-100-characters-are-accepted`
    ///
    /// Source: this spec (@@maxLength(100) of name, symbol and tokenMemo); HAPI limits them to 100 bytes, which ASCII text
    /// of 100 characters is.
    @Test
    @DisplayName("a name, a symbol and a memo of 100 characters are accepted")
    void aNameASymbolAndAMemoOf100CharactersAreAccepted() {
        // given
        final String text = repeat("a", 100L);

        // when
        final TokenCreateParams params = new TokenCreateParams(text, text, null, 0, 0L, TokenSupplyType.INFINITE, null, false, text, new byte[0], null, null, null, null, null, null, null, null, null, null, null);

        // then
        assertEquals(100L, length(params.name()));
        assertEquals(100L, length(params.symbol()));
        assertEquals(text, params.tokenMemo());
    }

    /// `enterprise.service.token/a-finite-token-accepts-a-max-supply-of-1`
    ///
    /// Source: this spec (@@min(1) of maxSupply, which applies to a FINITE supply type).
    @Test
    @DisplayName("a finite token accepts a max supply of 1")
    void aFiniteTokenAcceptsAMaxSupplyOf1() {
        // when
        final TokenCreateParams params = new TokenCreateParams("Token Name", "TKN", null, 0, 0L, TokenSupplyType.FINITE, 1L, false, null, new byte[0], null, null, null, null, null, null, null, null, null, null, null);

        // then
        assertEquals(TokenSupplyType.FINITE, params.supplyType());
        assertEquals(1L, params.maxSupply());
    }

    private static TokenCreateParams defaultTokenCreateParams() {
        return new TokenCreateParams("Token Name", "TKN", null, 0, 0L, TokenSupplyType.INFINITE, null, false, null, new byte[0], null, null, null, null, null, null, null, null, null, null, null);
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
