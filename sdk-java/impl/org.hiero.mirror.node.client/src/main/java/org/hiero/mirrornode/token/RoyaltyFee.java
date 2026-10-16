package org.hiero.mirrornode.token;

import org.hiero.ledger.AccountId;
import org.hiero.ledger.Address;
import org.jspecify.annotations.Nullable;

public record RoyaltyFee(
        long numeratorAmount,
        long denominatorAmount,
        long fallbackFeeAmount,
        @Nullable AccountId collectorAccountId,
        @Nullable Address denominatingTokenId) {
}
