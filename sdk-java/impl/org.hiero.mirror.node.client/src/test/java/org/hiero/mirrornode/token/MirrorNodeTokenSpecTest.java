package org.hiero.mirrornode.token;

import static org.hiero.sdk.testing.Testing.*;

import java.util.List;
import org.hiero.ledger.AccountId;
import org.hiero.mirrornode.common.FixedFee;
import org.junit.jupiter.api.DisplayName;
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
    void customFeesWithoutFeesHaveThreeEmptyLists() {
        // when
        final CustomFee fees = new CustomFee(List.of(), List.of(), List.of());

        // then
        assertEquals(0L, length(fees.fixedFees()));
        assertEquals(0L, length(fees.fractionalFees()));
        assertEquals(0L, length(fees.royaltyFees()));
    }
}
