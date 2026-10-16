package org.hiero.hedera;

import static org.hiero.sdk.testing.Testing.assertValue;
import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/// Tests of `Hbar`, generated from the specs.
@DisplayName("Hbar")
class HbarTest {

    @Test
    @DisplayName("creates a Hbar and returns the values")
    void shouldCreateAndReturnTheValues() {
        final long amount = amountValue();
        final HbarUnit unit = unitValue();
        final Hbar subject = new Hbar(amount, unit);
        assertValue(amount, subject.amount());
        assertValue(unit, subject.unit());
    }

    @Test
    @DisplayName("amount: accepts the minimum (-9223372036854775808)")
    void shouldAcceptAmountAtMinimum() {
        final long value = -9223372036854775808L;
        final Hbar subject = new Hbar(value, unitValue());
        assertValue(value, subject.amount());
    }

    @Test
    @DisplayName("amount: accepts the maximum (9223372036854775807)")
    void shouldAcceptAmountAtMaximum() {
        final long value = 9223372036854775807L;
        final Hbar subject = new Hbar(value, unitValue());
        assertValue(value, subject.amount());
    }

    @Test
    @DisplayName("rejects null for unit")
    void shouldRejectNullUnit() {
        assertThrows(NullPointerException.class, () -> new Hbar(amountValue(), (HbarUnit) null));
    }

    @Test
    @DisplayName("to(targetUnit) can be called and returns a value (only IllegalArgumentException may occur)")
    void shouldCallTo() throws Exception {
        try {
            assertNotNull(defaultSubject().to(targetUnitValue()));
        } catch (final Exception e) {
            if (!(e instanceof IllegalArgumentException)) {
                throw e;
            }
        }
    }

    @Test
    @DisplayName("toBaseUnits() can be called (only IllegalArgumentException may occur)")
    void shouldCallToBaseUnits() throws Exception {
        try {
            defaultSubject().toBaseUnits();
        } catch (final Exception e) {
            if (!(e instanceof IllegalArgumentException)) {
                throw e;
            }
        }
    }

    @Test
    @DisplayName("toTinybars() can be called (only IllegalArgumentException may occur)")
    void shouldCallToTinybars() throws Exception {
        try {
            defaultSubject().toTinybars();
        } catch (final Exception e) {
            if (!(e instanceof IllegalArgumentException)) {
                throw e;
            }
        }
    }

    @Test
    @DisplayName("equals(other) can be called")
    void shouldCallEquals() {
        defaultSubject().equals(otherValue());
    }

    @Test
    @DisplayName("hashCode() can be called")
    void shouldCallHashCode() {
        defaultSubject().hashCode();
    }

    private static long amountValue() {
        return 1L;
    }

    private static HbarUnit unitValue() {
        return HbarUnit.TINYBAR;
    }

    private static Hbar create() {
        return new Hbar(amountValue(), unitValue());
    }

    private static Hbar defaultSubject() {
        return new Hbar(1L, HbarUnit.HBAR);
    }

    private static HbarUnit targetUnitValue() {
        return HbarUnit.TINYBAR;
    }

    private static Object otherValue() {
        return "value";
    }
}
