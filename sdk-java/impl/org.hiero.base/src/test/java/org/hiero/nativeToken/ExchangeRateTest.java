package org.hiero.nativeToken;

import static org.hiero.sdk.testing.Testing.assertValue;
import static org.junit.jupiter.api.Assertions.*;

import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import org.hiero.hedera.HbarUnit;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

/// Tests of `ExchangeRate`, generated from the specs.
@DisplayName("ExchangeRate")
class ExchangeRateTest {

    @Test
    @DisplayName("creates an ExchangeRate and returns the values")
    @Tag("testId:nativeToken.ExchangeRate/create")
    void shouldCreateAndReturnTheValues() {
        final int nativeTokenEquivalent = nativeTokenEquivalentValue();
        final NativeTokenUnit nativeTokenUnit = nativeTokenUnitValue();
        final int centEquivalent = centEquivalentValue();
        final ZonedDateTime expirationTime = expirationTimeValue();
        final ExchangeRate subject = new ExchangeRate(nativeTokenEquivalent, nativeTokenUnit, centEquivalent, expirationTime);
        assertValue(nativeTokenEquivalent, subject.nativeTokenEquivalent());
        assertValue(nativeTokenUnit, subject.nativeTokenUnit());
        assertValue(centEquivalent, subject.centEquivalent());
        assertValue(expirationTime, subject.expirationTime());
    }

    @Test
    @DisplayName("nativeTokenEquivalent: accepts the minimum (-2147483648)")
    @Tag("testId:nativeToken.ExchangeRate/nativeTokenEquivalent/accepts-at-minimum")
    void shouldAcceptNativeTokenEquivalentAtMinimum() {
        final int value = Integer.MIN_VALUE;
        final ExchangeRate subject = new ExchangeRate(value, nativeTokenUnitValue(), centEquivalentValue(), expirationTimeValue());
        assertValue(value, subject.nativeTokenEquivalent());
    }

    @Test
    @DisplayName("nativeTokenEquivalent: accepts the maximum (2147483647)")
    @Tag("testId:nativeToken.ExchangeRate/nativeTokenEquivalent/accepts-at-maximum")
    void shouldAcceptNativeTokenEquivalentAtMaximum() {
        final int value = 2147483647;
        final ExchangeRate subject = new ExchangeRate(value, nativeTokenUnitValue(), centEquivalentValue(), expirationTimeValue());
        assertValue(value, subject.nativeTokenEquivalent());
    }

    @Test
    @DisplayName("rejects null for nativeTokenUnit")
    @Tag("testId:nativeToken.ExchangeRate/nativeTokenUnit/rejects-null")
    void shouldRejectNullNativeTokenUnit() {
        assertThrows(NullPointerException.class, () -> new ExchangeRate(nativeTokenEquivalentValue(), (NativeTokenUnit) null, centEquivalentValue(), expirationTimeValue()));
    }

    @Test
    @DisplayName("centEquivalent: accepts the minimum (-2147483648)")
    @Tag("testId:nativeToken.ExchangeRate/centEquivalent/accepts-at-minimum")
    void shouldAcceptCentEquivalentAtMinimum() {
        final int value = Integer.MIN_VALUE;
        final ExchangeRate subject = new ExchangeRate(nativeTokenEquivalentValue(), nativeTokenUnitValue(), value, expirationTimeValue());
        assertValue(value, subject.centEquivalent());
    }

    @Test
    @DisplayName("centEquivalent: accepts the maximum (2147483647)")
    @Tag("testId:nativeToken.ExchangeRate/centEquivalent/accepts-at-maximum")
    void shouldAcceptCentEquivalentAtMaximum() {
        final int value = 2147483647;
        final ExchangeRate subject = new ExchangeRate(nativeTokenEquivalentValue(), nativeTokenUnitValue(), value, expirationTimeValue());
        assertValue(value, subject.centEquivalent());
    }

    @Test
    @DisplayName("rejects null for expirationTime")
    @Tag("testId:nativeToken.ExchangeRate/expirationTime/rejects-null")
    void shouldRejectNullExpirationTime() {
        assertThrows(NullPointerException.class, () -> new ExchangeRate(nativeTokenEquivalentValue(), nativeTokenUnitValue(), centEquivalentValue(), (ZonedDateTime) null));
    }

    @Test
    @DisplayName("isExpiredAt(time) can be called")
    @Tag("testId:nativeToken.ExchangeRate/isExpiredAt/call")
    void shouldCallIsExpiredAt() {
        defaultSubject().isExpiredAt(timeValue());
    }

    private static int nativeTokenEquivalentValue() {
        return 1;
    }

    private static NativeTokenUnit nativeTokenUnitValue() {
        return HbarUnit.TINYBAR;
    }

    private static int centEquivalentValue() {
        return 1;
    }

    private static ZonedDateTime expirationTimeValue() {
        return ZonedDateTime.of(2024, 1, 1, 12, 0, 0, 0, ZoneOffset.UTC);
    }

    private static ExchangeRate create() {
        return new ExchangeRate(nativeTokenEquivalentValue(), nativeTokenUnitValue(), centEquivalentValue(), expirationTimeValue());
    }

    private static ExchangeRate defaultSubject() {
        return new ExchangeRate(30000, HbarUnit.HBAR, 580150, ZonedDateTime.of(2024, 1, 1, 12, 0, 0, 0, ZoneOffset.UTC));
    }

    private static ZonedDateTime timeValue() {
        return ZonedDateTime.of(2024, 1, 1, 12, 0, 0, 0, ZoneOffset.UTC);
    }
}
