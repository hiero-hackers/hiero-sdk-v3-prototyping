package org.hiero.mirrornode.token;

import org.hiero.ledger.AccountId;
import org.hiero.ledger.Address;
import org.jspecify.annotations.Nullable;

public record FractionalFee(
        long numeratorAmount,
        long denominatorAmount,
        @Nullable AccountId collectorAccountId,
        @Nullable Address denominatingTokenId) {
}
