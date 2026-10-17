package org.hiero.mirrornode.token;

import static org.hiero.sdk.testing.Testing.*;

import java.math.BigInteger;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.List;
import org.hiero.ledger.AccountId;
import org.hiero.ledger.Address;
import org.hiero.mirrornode.common.FixedFee;
import org.hiero.token.TokenSupplyType;
import org.hiero.token.TokenType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

/// Tests of the spec `mirror-node-token`, generated from the `tests` blocks of its `## Testing` section.
@DisplayName("mirror-node-token")
class MirrorNodeTokenSpecTest {

    /// `mirrornode.token/custom-fees-keep-their-fixed-fractional-and-royalty-fees`
    ///
    /// Source: the Java SDK (CustomFixedFeeTest, CustomFractionalFeeTest and CustomRoyaltyFeeTest: collector 1.2.3, amount
    /// 4, fraction 4/5, fallback fee 6); this spec (CustomFee groups the fees into fixed, fractional and royalty fees).
    @Test
    @DisplayName("custom fees keep their fixed, fractional and royalty fees")
    @Tag("testId:mirrornode.token/custom-fees-keep-their-fixed-fractional-and-royalty-fees")
    void customFeesKeepTheirFixedFractionalAndRoyaltyFees() {
        // given
        final AccountId collector = AccountId.fromString("1.2.3");
        final FixedFee fixed = new FixedFee(4L, collector, null);
        final FractionalFee fractional = new FractionalFee(4L, 5L, collector, null);
        final RoyaltyFee royalty = new RoyaltyFee(4L, 5L, 6L, collector, null);

        // when
        final CustomFee fees = new CustomFee(List.of(fixed), List.of(fractional), List.of(royalty));

        // then
        assertEquals(List.of(fixed), fees.fixedFees());
        assertEquals(List.of(fractional), fees.fractionalFees());
        assertEquals(List.of(royalty), fees.royaltyFees());
        assertEquals(4L, get(fees.fractionalFees(), 0L).numeratorAmount());
        assertEquals(5L, get(fees.fractionalFees(), 0L).denominatorAmount());
        assertEquals(6L, get(fees.royaltyFees(), 0L).fallbackFeeAmount());
        assertNull(get(fees.royaltyFees(), 0L).denominatingTokenId());
    }

    /// `mirrornode.token/custom-fees-without-fees-have-three-empty-lists`
    ///
    /// Source: the API guideline (a collection is never null: a token without custom fees has empty fee lists).
    @Test
    @DisplayName("custom fees without fees have three empty lists")
    @Tag("testId:mirrornode.token/custom-fees-without-fees-have-three-empty-lists")
    void customFeesWithoutFeesHaveThreeEmptyLists() {
        // when
        final CustomFee fees = new CustomFee(List.of(), List.of(), List.of());

        // then
        assertEquals(0L, length(fees.fixedFees()));
        assertEquals(0L, length(fees.fractionalFees()));
        assertEquals(0L, length(fees.royaltyFees()));
    }

    /// `mirrornode.token/a-token-info-keeps-its-created-modified-and-expiry-timestamps`
    ///
    /// Source: the Hiero Enterprise Java test fixture token-info.json (token 0.0.1267, fungible, infinite supply, 10
    /// decimals, initial supply 1000000, treasury 0.0.1245, deleted, created "1706816186.275486248", modified
    /// "1706816444.138081003", expiry 1713816185275486248 ns); millisecond precision.
    @Test
    @DisplayName("a token info keeps its created, modified and expiry timestamps")
    @Tag("testId:mirrornode.token/a-token-info-keeps-its-created-modified-and-expiry-timestamps")
    void aTokenInfoKeepsItsCreatedModifiedAndExpiryTimestamps() {
        // given
        final ZonedDateTime created = toZonedDateTime("2024-02-01T19:36:26.275Z");
        final ZonedDateTime modified = toZonedDateTime("2024-02-01T19:40:44.138Z");
        final ZonedDateTime expiry = toZonedDateTime("2024-04-22T20:03:05.275Z");
        final CustomFee fees = new CustomFee(List.of(), List.of(), List.of());

        // when
        final TokenInfo info = new TokenInfo(Address.fromString("0.0.1267"), TokenType.FUNGIBLE_COMMON, "fungible_name", "fungible", null, 10L, new byte[0], created, modified, expiry, TokenSupplyType.INFINITE, new BigInteger("1000000"), new BigInteger("0"), new BigInteger("0"), AccountId.fromString("0.0.1245"), true, fees);

        // then
        assertEquals(toZonedDateTime("2024-02-01T19:36:26.275Z"), info.createdTimestamp());
        assertEquals(toZonedDateTime("2024-02-01T19:40:44.138Z"), info.modifiedTimestamp());
        assertEquals(toZonedDateTime("2024-04-22T20:03:05.275Z"), info.expiryTimestamp());
        assertEquals(TokenType.FUNGIBLE_COMMON, info.type());
        assertEquals(TokenSupplyType.INFINITE, info.supplyType());
        assertEquals(new BigInteger("1000000"), info.initialSupply());
        assertEquals(AccountId.fromString("0.0.1245"), info.treasuryAccountId());
        assertTrue(info.deleted());
        assertEquals(fees, info.customFees());
    }

    /// `mirrornode.token/a-token-info-without-expiry-has-no-expiry-timestamp-and-no-memo`
    ///
    /// Source: this spec (`expiryTimestamp` and `memo` are nullable).
    @Test
    @DisplayName("a token info without expiry has no expiry timestamp and no memo")
    @Tag("testId:mirrornode.token/a-token-info-without-expiry-has-no-expiry-timestamp-and-no-memo")
    void aTokenInfoWithoutExpiryHasNoExpiryTimestampAndNoMemo() {
        // given
        final ZonedDateTime time = ZonedDateTime.of(2024, 1, 1, 12, 0, 0, 0, ZoneOffset.UTC);

        // when
        final TokenInfo info = new TokenInfo(Address.fromString("0.0.1267"), TokenType.NON_FUNGIBLE_UNIQUE, "nft_name", "nft", null, 0L, new byte[0], time, time, null, TokenSupplyType.FINITE, new BigInteger("0"), new BigInteger("0"), new BigInteger("100"), defaultAccountId(), false, new CustomFee(List.of(), List.of(), List.of()));

        // then
        assertNull(info.expiryTimestamp());
        assertNull(info.memo());
        assertEquals(time, info.createdTimestamp());
        assertEquals(time, info.modifiedTimestamp());
    }

    private static AccountId defaultAccountId() {
        return new AccountId(0L, 0L, "", 1001L, null, null);
    }
}
