package org.hiero.consensusnode.transactions.accounts;

import static org.hiero.sdk.testing.Testing.assertValue;
import static org.junit.jupiter.api.Assertions.*;

import org.hiero.hedera.Hbar;
import org.hiero.hedera.HbarUnit;
import org.hiero.ledger.AccountId;
import org.hiero.nativeToken.NativeToken;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/// Tests of `HbarTransfer`, generated from the specs.
@DisplayName("HbarTransfer")
class HbarTransferTest {

    @Test
    @DisplayName("creates a HbarTransfer and returns the values")
    void shouldCreateAndReturnTheValues() {
        final AccountId accountId = accountIdValue();
        final NativeToken<?, ?> amount = amountValue();
        final boolean approved = approvedValue();
        final HbarTransfer subject = new HbarTransfer(accountId, amount, approved);
        assertValue(accountId, subject.accountId());
        assertValue(amount, subject.amount());
        assertValue(approved, subject.approved());
    }

    @Test
    @DisplayName("creates a HbarTransfer with the default values")
    void shouldCreateWithTheDefaultValues() {
        final HbarTransfer subject = new HbarTransfer(accountIdValue(), amountValue());
        assertValue(false, subject.approved());
    }

    @Test
    @DisplayName("rejects null for accountId")
    void shouldRejectNullAccountId() {
        assertThrows(NullPointerException.class, () -> new HbarTransfer((AccountId) null, amountValue(), approvedValue()));
    }

    @Test
    @DisplayName("rejects null for amount")
    void shouldRejectNullAmount() {
        assertThrows(NullPointerException.class, () -> new HbarTransfer(accountIdValue(), (NativeToken<?, ?>) null, approvedValue()));
    }

    private static AccountId accountIdValue() {
        return new AccountId(0L, 0L, "", 1001L, null, null);
    }

    private static NativeToken<?, ?> amountValue() {
        return new Hbar(1L, HbarUnit.TINYBAR);
    }

    private static boolean approvedValue() {
        return true;
    }

    private static HbarTransfer create() {
        return new HbarTransfer(accountIdValue(), amountValue(), approvedValue());
    }
}
