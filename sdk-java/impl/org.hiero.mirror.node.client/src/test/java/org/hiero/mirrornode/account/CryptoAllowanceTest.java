package org.hiero.mirrornode.account;

import static org.hiero.sdk.testing.Testing.assertValue;
import static org.junit.jupiter.api.Assertions.*;

import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import org.hiero.ledger.AccountId;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/// Tests of `CryptoAllowance`, generated from the specs.
@DisplayName("CryptoAllowance")
class CryptoAllowanceTest {

    @Test
    @DisplayName("creates a CryptoAllowance and returns the values")
    void shouldCreateAndReturnTheValues() {
        final AccountId owner = ownerValue();
        final AccountId spender = spenderValue();
        final long amount = amountValue();
        final long amountGranted = amountGrantedValue();
        final ZonedDateTime timestamp = timestampValue();
        final CryptoAllowance subject = new CryptoAllowance(owner, spender, amount, amountGranted, timestamp);
        assertValue(owner, subject.owner());
        assertValue(spender, subject.spender());
        assertValue(amount, subject.amount());
        assertValue(amountGranted, subject.amountGranted());
        assertValue(timestamp, subject.timestamp());
    }

    @Test
    @DisplayName("the builder builds the values of the constructor")
    void shouldBuildTheValuesOfTheConstructor() {
        final AccountId owner = ownerValue();
        final AccountId spender = spenderValue();
        final long amount = amountValue();
        final long amountGranted = amountGrantedValue();
        final ZonedDateTime timestamp = timestampValue();
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
    void shouldRejectAMissingRequiredValue() {
        assertThrows(NullPointerException.class, () -> CryptoAllowance.builder().build());
    }

    @Test
    @DisplayName("rejects null for owner")
    void shouldRejectNullOwner() {
        assertThrows(NullPointerException.class, () -> new CryptoAllowance((AccountId) null, spenderValue(), amountValue(), amountGrantedValue(), timestampValue()));
    }

    @Test
    @DisplayName("rejects null for spender")
    void shouldRejectNullSpender() {
        assertThrows(NullPointerException.class, () -> new CryptoAllowance(ownerValue(), (AccountId) null, amountValue(), amountGrantedValue(), timestampValue()));
    }

    @Test
    @DisplayName("amount: accepts the minimum (-9223372036854775808)")
    void shouldAcceptAmountAtMinimum() {
        final long value = -9223372036854775808L;
        final CryptoAllowance subject = new CryptoAllowance(ownerValue(), spenderValue(), value, amountGrantedValue(), timestampValue());
        assertValue(value, subject.amount());
    }

    @Test
    @DisplayName("amount: accepts the maximum (9223372036854775807)")
    void shouldAcceptAmountAtMaximum() {
        final long value = 9223372036854775807L;
        final CryptoAllowance subject = new CryptoAllowance(ownerValue(), spenderValue(), value, amountGrantedValue(), timestampValue());
        assertValue(value, subject.amount());
    }

    @Test
    @DisplayName("amountGranted: accepts the minimum (-9223372036854775808)")
    void shouldAcceptAmountGrantedAtMinimum() {
        final long value = -9223372036854775808L;
        final CryptoAllowance subject = new CryptoAllowance(ownerValue(), spenderValue(), amountValue(), value, timestampValue());
        assertValue(value, subject.amountGranted());
    }

    @Test
    @DisplayName("amountGranted: accepts the maximum (9223372036854775807)")
    void shouldAcceptAmountGrantedAtMaximum() {
        final long value = 9223372036854775807L;
        final CryptoAllowance subject = new CryptoAllowance(ownerValue(), spenderValue(), amountValue(), value, timestampValue());
        assertValue(value, subject.amountGranted());
    }

    @Test
    @DisplayName("rejects null for timestamp")
    void shouldRejectNullTimestamp() {
        assertThrows(NullPointerException.class, () -> new CryptoAllowance(ownerValue(), spenderValue(), amountValue(), amountGrantedValue(), (ZonedDateTime) null));
    }

    @Test
    @DisplayName("equal values give equal objects and hash codes")
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

    private static ZonedDateTime timestampValue() {
        return ZonedDateTime.of(2024, 1, 1, 12, 0, 0, 0, ZoneOffset.UTC);
    }

    private static CryptoAllowance create() {
        return new CryptoAllowance(ownerValue(), spenderValue(), amountValue(), amountGrantedValue(), timestampValue());
    }
}
