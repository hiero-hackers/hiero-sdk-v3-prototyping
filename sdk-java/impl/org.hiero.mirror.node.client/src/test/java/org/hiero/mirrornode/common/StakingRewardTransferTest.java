package org.hiero.mirrornode.common;

import static org.hiero.sdk.testing.Testing.assertValue;
import static org.junit.jupiter.api.Assertions.*;

import org.hiero.ledger.AccountId;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

/// Tests of `StakingRewardTransfer`, generated from the specs.
@DisplayName("StakingRewardTransfer")
class StakingRewardTransferTest {

    @Test
    @DisplayName("creates a StakingRewardTransfer and returns the values")
    @Tag("testId:mirrornode.common.StakingRewardTransfer/create")
    void shouldCreateAndReturnTheValues() {
        final AccountId account = accountValue();
        final long amount = amountValue();
        final StakingRewardTransfer subject = new StakingRewardTransfer(account, amount);
        assertValue(account, subject.account());
        assertValue(amount, subject.amount());
    }

    @Test
    @DisplayName("rejects null for account")
    @Tag("testId:mirrornode.common.StakingRewardTransfer/account/rejects-null")
    void shouldRejectNullAccount() {
        assertThrows(NullPointerException.class, () -> new StakingRewardTransfer((AccountId) null, amountValue()));
    }

    @Test
    @DisplayName("amount: accepts the minimum (-9223372036854775808)")
    @Tag("testId:mirrornode.common.StakingRewardTransfer/amount/accepts-at-minimum")
    void shouldAcceptAmountAtMinimum() {
        final long value = -9223372036854775808L;
        final StakingRewardTransfer subject = new StakingRewardTransfer(accountValue(), value);
        assertValue(value, subject.amount());
    }

    @Test
    @DisplayName("amount: accepts the maximum (9223372036854775807)")
    @Tag("testId:mirrornode.common.StakingRewardTransfer/amount/accepts-at-maximum")
    void shouldAcceptAmountAtMaximum() {
        final long value = 9223372036854775807L;
        final StakingRewardTransfer subject = new StakingRewardTransfer(accountValue(), value);
        assertValue(value, subject.amount());
    }

    @Test
    @DisplayName("equal values give equal objects and hash codes")
    @Tag("testId:mirrornode.common.StakingRewardTransfer/value-semantics")
    void shouldCompareByValue() {
        final StakingRewardTransfer subject = create();
        final StakingRewardTransfer other = create();
        assertEquals(subject, other);
        assertEquals(subject.hashCode(), other.hashCode());
    }

    private static AccountId accountValue() {
        return new AccountId(0L, 0L, "", 1001L, null, null);
    }

    private static long amountValue() {
        return 1L;
    }

    private static StakingRewardTransfer create() {
        return new StakingRewardTransfer(accountValue(), amountValue());
    }
}
