package org.hiero.mirrornode.common;

import java.util.Objects;
import org.hiero.ledger.AccountId;

public record StakingRewardTransfer(AccountId account, long amount) {

    /// Creates a new `StakingRewardTransfer`.
    ///
    /// @throws NullPointerException if a required value is `null`
    public StakingRewardTransfer {
        Objects.requireNonNull(account, "account must not be null");
    }
}
