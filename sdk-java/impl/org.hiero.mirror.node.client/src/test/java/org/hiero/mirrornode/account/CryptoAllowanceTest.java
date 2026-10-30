package org.hiero.mirrornode.account;

import static org.hiero.sdk.testing.Testing.assertValue;
import static org.junit.jupiter.api.Assertions.*;

import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import org.hiero.ledger.AccountId;
import org.hiero.mirrornode.common.TimestampRange;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

/// Tests of `CryptoAllowance`, generated from the specs.
@DisplayName("CryptoAllowance")
class CryptoAllowanceTest {

    @Test
    @DisplayName("creates a CryptoAllowance and returns the values")
    @Tag("testId:mirrornode.account.CryptoAllowance/create")
    void shouldCreateAndReturnTheValues() {
        final AccountId owner = ownerValue();
        final AccountId spender = spenderValue();
        final long amount = amountValue();
        final long amountGranted = amountGrantedValue();
        final TimestampRange timestamp = timestampValue();
        final CryptoAllowance subject = new CryptoAllowance(owner, spender, amount, amountGranted, timestamp);
        assertValue(owner, subject.owner());
        assertValue(spender, subject.spender());
        assertValue(amount, subject.amount());
        assertValue(amountGranted, subject.amountGranted());
        assertValue(timestamp, subject.timestamp());
    }

    @Test
    @DisplayName("the builder builds the values of the constructor")
    @Tag("testId:mirrornode.account.CryptoAllowance/builder/builds")
    void shouldBuildTheValuesOfTheConstructor() {
        final AccountId owner = ownerValue();
        final AccountId spender = spenderValue();
        final long amount = amountValue();
        final long amountGranted = amountGrantedValue();
        final TimestampRange timestamp = timestampValue();
        final CryptoAllowance subject = CryptoAllowance.builder()
                .owner(owner)
                .spender(spender)
                .amount(amount)
                .amountGranted(amountGranted)
                .timestamp(timestamp)
                .build();
        assertValue(owner, subject.owner());
        assertValue(spender, subject.spender());
        assertValue(amount, subject.amount());
        assertValue(amountGranted, subject.amountGranted());
        assertValue(timestamp, subject.timestamp());
    }

    @Test
    @DisplayName("a value that is not set is null or its default value")
    @Tag("testId:mirrornode.account.CryptoAllowance/builder/optional-values")
    void shouldBuildWithoutTheOptionalValues() {
        final CryptoAllowance subject = CryptoAllowance.builder()
                .owner(ownerValue())
                .spender(spenderValue())
                .amount(amountValue())
                .amountGranted(amountGrantedValue())
                .timestamp(timestampValue())
                .build();
    }

    @Test
    @DisplayName("the builder rejects a missing required value")
    @Tag("testId:mirrornode.account.CryptoAllowance/builder/rejects-missing-value")
    void shouldRejectAMissingRequiredValue() {
        assertThrows(NullPointerException.class, () -> CryptoAllowance.builder().build());
    }

    @Test
    @DisplayName("rejects null for owner")
    @Tag("testId:mirrornode.account.CryptoAllowance/owner/rejects-null")
    void shouldRejectNullOwner() {
        assertThrows(NullPointerException.class, () -> new CryptoAllowance((AccountId) null, spenderValue(), amountValue(), amountGrantedValue(), timestampValue()));
    }

    @Test
    @DisplayName("rejects null for spender")
    @Tag("testId:mirrornode.account.CryptoAllowance/spender/rejects-null")
    void shouldRejectNullSpender() {
        assertThrows(NullPointerException.class, () -> new CryptoAllowance(ownerValue(), (AccountId) null, amountValue(), amountGrantedValue(), timestampValue()));
    }

    @Test
    @DisplayName("amount: accepts the minimum (-9223372036854775808)")
    @Tag("testId:mirrornode.account.CryptoAllowance/amount/accepts-at-minimum")
    void shouldAcceptAmountAtMinimum() {
        final long value = -9223372036854775808L;
        final CryptoAllowance subject = new CryptoAllowance(ownerValue(), spenderValue(), value, amountGrantedValue(), timestampValue());
        assertValue(value, subject.amount());
    }

    @Test
    @DisplayName("amount: accepts the maximum (9223372036854775807)")
    @Tag("testId:mirrornode.account.CryptoAllowance/amount/accepts-at-maximum")
    void shouldAcceptAmountAtMaximum() {
        final long value = 9223372036854775807L;
        final CryptoAllowance subject = new CryptoAllowance(ownerValue(), spenderValue(), value, amountGrantedValue(), timestampValue());
        assertValue(value, subject.amount());
    }

    @Test
    @DisplayName("amountGranted: accepts the minimum (-9223372036854775808)")
    @Tag("testId:mirrornode.account.CryptoAllowance/amountGranted/accepts-at-minimum")
    void shouldAcceptAmountGrantedAtMinimum() {
        final long value = -9223372036854775808L;
        final CryptoAllowance subject = new CryptoAllowance(ownerValue(), spenderValue(), amountValue(), value, timestampValue());
        assertValue(value, subject.amountGranted());
    }

    @Test
    @DisplayName("amountGranted: accepts the maximum (9223372036854775807)")
    @Tag("testId:mirrornode.account.CryptoAllowance/amountGranted/accepts-at-maximum")
    void shouldAcceptAmountGrantedAtMaximum() {
        final long value = 9223372036854775807L;
        final CryptoAllowance subject = new CryptoAllowance(ownerValue(), spenderValue(), amountValue(), value, timestampValue());
        assertValue(value, subject.amountGranted());
    }

    @Test
    @DisplayName("rejects null for timestamp")
    @Tag("testId:mirrornode.account.CryptoAllowance/timestamp/rejects-null")
    void shouldRejectNullTimestamp() {
        assertThrows(NullPointerException.class, () -> new CryptoAllowance(ownerValue(), spenderValue(), amountValue(), amountGrantedValue(), (TimestampRange) null));
    }

    @Test
    @DisplayName("equal values give equal objects and hash codes")
    @Tag("testId:mirrornode.account.CryptoAllowance/value-semantics")
    void shouldCompareByValue() {
        final CryptoAllowance subject = create();
        final CryptoAllowance other = create();
        assertEquals(subject, other);
        assertEquals(subject.hashCode(), other.hashCode());
    }

    private static AccountId ownerValue() {
        return new AccountId(0L, 0L, "", 1001L, null, null);
    }

    private static AccountId spenderValue() {
        return new AccountId(0L, 0L, "", 1001L, null, null);
    }

    private static long amountValue() {
        return 1L;
    }

    private static long amountGrantedValue() {
        return 1L;
    }

    private static TimestampRange timestampValue() {
        return new TimestampRange(ZonedDateTime.of(2024, 1, 1, 12, 0, 0, 0, ZoneOffset.UTC), null);
    }

    private static CryptoAllowance create() {
        return new CryptoAllowance(ownerValue(), spenderValue(), amountValue(), amountGrantedValue(), timestampValue());
    }
}
