package org.hiero.hedera;

import static org.hiero.sdk.testing.Testing.assertValue;
import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/// Tests of `HbarUnit`, generated from the specs.
@DisplayName("HbarUnit")
class HbarUnitTest {

    @Test
    @DisplayName("the constants have the attribute values of the specs")
    void shouldProvideTheAttributesOfTheConstants() {
        assertValue("tℏ", HbarUnit.TINYBAR.symbol());
        assertValue(1L, HbarUnit.TINYBAR.baseUnitFactor());
        assertValue("μℏ", HbarUnit.MICROBAR.symbol());
        assertValue(100L, HbarUnit.MICROBAR.baseUnitFactor());
        assertValue("mℏ", HbarUnit.MILLIBAR.symbol());
        assertValue(100_000L, HbarUnit.MILLIBAR.baseUnitFactor());
        assertValue("ℏ", HbarUnit.HBAR.symbol());
        assertValue(100_000_000L, HbarUnit.HBAR.baseUnitFactor());
        assertValue("kℏ", HbarUnit.KILOBAR.symbol());
        assertValue(100_000_000_000L, HbarUnit.KILOBAR.baseUnitFactor());
        assertValue("Mℏ", HbarUnit.MEGABAR.symbol());
        assertValue(100_000_000_000_000L, HbarUnit.MEGABAR.baseUnitFactor());
        assertValue("Gℏ", HbarUnit.GIGABAR.symbol());
        assertValue(100_000_000_000_000_000L, HbarUnit.GIGABAR.baseUnitFactor());
    }
}
