package org.hiero.consensusnode.queries.tokens;

import static org.hiero.sdk.testing.Testing.*;

import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.List;
import org.hiero.ledger.AccountId;
import org.hiero.ledger.Address;
import org.hiero.token.TokenSupplyType;
import org.hiero.token.TokenType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/// Tests of the spec `queries-tokens`, generated from the `tests` blocks of its `## Testing` section.
@DisplayName("queries-tokens")
class QueriesTokensSpecTest {

    /// `consensusnode.queries.tokens/a-token-info-query-keeps-the-id-of-its-token`
    ///
    /// Source: the Java SDK (TokenInfoQueryTest.getSetTokenId), the Python SDK (token_info_query_test.py,
    /// test_constructor and test_get_method) and the Swift SDK (TokenInfoQueryUnitTests, the get / set test).
    @Test
    @DisplayName("a token info query keeps the id of its token")
    void aTokenInfoQueryKeepsTheIdOfItsToken() {
        // given
        final Address token = defaultAddress();

        // when
        final TokenInfoQuery query = new TokenInfoQuery(token);

        // then
        assertEquals(token, query.tokenId());
    }

    /// `consensusnode.queries.tokens/an-nft-info-query-keeps-its-token-id-and-serial-and-serial-1-is-accepted`
    ///
    /// Source: the Java SDK (TokenNftInfoQueryTest.propertiesTest), the Python SDK (token_nft_info_query_test.py,
    /// test_constructor) and the JavaScript SDK (TokenNftInfoQuery); this spec (serials start at 1).
    @Test
    @DisplayName("an NFT info query keeps its token id and serial, and serial 1 is accepted")
    void anNFTInfoQueryKeepsItsTokenIdAndSerialAndSerial1IsAccepted() {
        // given
        final Address token = defaultAddress();
        for (final long serial : List.of(1L, 101L)) {
            // when
            final TokenNftInfoQuery query = new TokenNftInfoQuery(token, serial);

            // then
            assertEquals(token, query.tokenId());
            assertEquals(serial, query.serial());
        }
    }

    /// `consensusnode.queries.tokens/a-token-info-has-these-defaults`
    ///
    /// Source: this spec (the defaults of TokenInfo; a key that was never granted is absent, and so is its status); the
    /// Python SDK (token_info_test.py, test_token_info_initialization) and the Java SDK (TokenInfoTest).
    @Test
    @DisplayName("a token info has these defaults")
    void aTokenInfoHasTheseDefaults() {
        // when
        final TokenInfo info = new TokenInfo(defaultAddress(), TokenType.FUNGIBLE_COMMON, "ffff", "F", 0, 1000L, defaultAccountId(), TokenSupplyType.INFINITE, null, null, new byte[0], ZonedDateTime.of(2024, 1, 1, 12, 0, 0, 0, ZoneOffset.UTC), null, null, false, null, null, null, null, null, null, null, null, null, null, null);

        // then
        assertEquals(0, info.decimals());
        assertEquals(0L, length(info.metadata()));
        assertFalse(info.deleted());
        assertNull(info.maxSupply());
        assertNull(info.tokenMemo());
        assertNull(info.autoRenewPeriod());
        assertNull(info.autoRenewAccount());
        assertNull(info.adminAuthority());
        assertNull(info.supplyAuthority());
        assertNull(info.kycAuthority());
        assertNull(info.freezeAuthority());
        assertNull(info.wipeAuthority());
        assertNull(info.pauseAuthority());
        assertNull(info.feeScheduleAuthority());
        assertNull(info.metadataAuthority());
        assertNull(info.defaultFreezeStatus());
        assertNull(info.defaultKycStatus());
        assertNull(info.paused());
    }

    /// `consensusnode.queries.tokens/a-token-info-accepts-a-name-and-a-symbol-of-100-characters`
    ///
    /// Source: this spec (name and symbol have at most 100 characters).
    @Test
    @DisplayName("a token info accepts a name and a symbol of 100 characters")
    void aTokenInfoAcceptsANameAndASymbolOf100Characters() {
        // given
        final String longest = repeat("a", 100L);

        // when
        final TokenInfo info = new TokenInfo(defaultAddress(), TokenType.NON_FUNGIBLE_UNIQUE, longest, longest, 0, 0L, defaultAccountId(), TokenSupplyType.INFINITE, null, null, new byte[0], ZonedDateTime.of(2024, 1, 1, 12, 0, 0, 0, ZoneOffset.UTC), null, null, false, null, null, null, null, null, null, null, null, null, null, null);

        // then
        assertEquals(longest, info.name());
        assertEquals(longest, info.symbol());
    }

    /// `consensusnode.queries.tokens/an-nft-info-without-allowance-has-no-spender-and-empty-metadata`
    ///
    /// Source: this spec (the defaults of TokenNftInfo); the Python SDK (token_nft_info_test.py,
    /// test_from_proto_without_spender_id) and the Java SDK (TokenNftInfoTest).
    @Test
    @DisplayName("an NFT info without allowance has no spender and empty metadata")
    void anNFTInfoWithoutAllowanceHasNoSpenderAndEmptyMetadata() {
        // when
        final TokenNftInfo info = new TokenNftInfo(defaultAddress(), 1L, defaultAccountId(), ZonedDateTime.of(2024, 1, 1, 12, 0, 0, 0, ZoneOffset.UTC), new byte[0], null);

        // then
        assertEquals(1L, info.serial());
        assertEquals(0L, length(info.metadata()));
        assertNull(info.spenderId());
    }

    private static Address defaultAddress() {
        return new Address(0L, 0L, "", 1001L);
    }

    private static AccountId defaultAccountId() {
        return new AccountId(0L, 0L, "", 1001L, null, null);
    }
}
