package org.hiero.sdk.v3.ledger.contract;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;
import static org.assertj.core.api.Assertions.assertThatNullPointerException;

import org.hiero.sdk.v3.ledger.Address;
import org.hiero.sdk.v3.ledger.BaseAddress;
import org.hiero.sdk.v3.ledger.Network;
import org.hiero.sdk.v3.nativeToken.NativeTokenUnit;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class AddressContractTest {
    private static final String MAX_UINT64 = "18446744073709551615";

    enum Unit implements NativeTokenUnit {
        BASE;

        @Override
        public String symbol() {
            return "b";
        }

        @Override
        public long baseUnitFactor() {
            return 1;
        }
    }

    enum OtherUnit implements NativeTokenUnit {
        OTHER;

        @Override
        public String symbol() {
            return "o";
        }

        @Override
        public long baseUnitFactor() {
            return 2;
        }
    }

    // HIP-15 checksum vectors for 0.0.123 on ledger ids 0 (mainnet), 1 (testnet) and 2 (previewnet).
    private static final Network<Unit> MAINNET = new Network<>(new byte[] {0}, "mainnet", Unit.BASE);
    private static final Network<OtherUnit> TESTNET = new Network<>(new byte[] {1}, "testnet", OtherUnit.OTHER);
    private static final Network<Unit> PREVIEWNET = new Network<>(new byte[] {2}, "previewnet", Unit.BASE);

    // --- Address state is observed

    @Test
    void exposesStateThroughAddressAndBaseReferences() {
        final BaseAddress base = new Address(1, 2, 3, "abcde");
        final Address address = (Address) base;

        assertThat(base.shard()).isEqualTo(1);
        assertThat(base.realm()).isEqualTo(2);
        assertThat(base.num()).isEqualTo(3L);
        assertThat(base.checksum()).isEqualTo("abcde");
        assertThat(address.num()).isEqualTo(3L);
    }

    @Test
    void baseAllowsAbsentNumWhileEveryAddressConstructionHasOne() {
        final BaseAddress noNum = new BaseAddress(0, 0, null, "") {
            @Override
            public boolean validateChecksum(final Network<?> network) {
                return false;
            }

            @Override
            public String toString() {
                return "0.0";
            }
        };

        assertThat(noNum.num()).isNull();
        assertThat(new Address(0, 0, 0).num()).isNotNull();
        assertThat(Address.fromString("1.2.3").num()).isNotNull();
    }

    @Test
    void rejectsNullChecksum() {
        assertThatNullPointerException()
                .isThrownBy(() -> new Address(0, 0, 0, null))
                .withMessage("checksum");
    }

    // --- Address is parsed from string

    @Test
    void parsesBothForms() {
        final var plain = Address.fromString("1.2.3");
        final var withChecksum = Address.fromString("1.2.3-abcde");

        assertThat(plain).isEqualTo(new Address(1, 2, 3));
        assertThat(plain.checksum()).isEmpty();
        assertThat(withChecksum).isEqualTo(new Address(1, 2, 3, "abcde"));
        assertThat(withChecksum.checksum()).isEqualTo("abcde");
    }

    @Test
    void roundTripsThroughFormatting() {
        final var address = Address.fromString("0.0.123-vfmkw");

        assertThat(address.toString()).isEqualTo("0.0.123");
        assertThat(address.toStringWithChecksum()).isEqualTo("0.0.123-vfmkw");
        assertThat(Address.fromString(address.toStringWithChecksum())).isEqualTo(address);
        assertThat(new Address(1, 2, 3).toStringWithChecksum()).isEqualTo("1.2.3");
    }

    // --- Invalid address parsing fails

    @ParameterizedTest
    @ValueSource(strings = {
        "", "1", "1.2", "1.2.3.4", "1..3", ".1.2", "1.2.", "a.2.3", "1.b.3", "1.2.c", "-1.2.3", "1.-2.3", "1.2.-3",
        "+1.2.3", " 1.2.3", "1.2.3 ", "1.2.3-", "1.2.3-abcd", "1.2.3-abcdef", "1.2.3-ABCDE", "1.2.3-ab1de",
        "1.2.3-abcde-fghij", "18446744073709551616.0.0", "0.18446744073709551616.0", "0.0.18446744073709551616",
        "0.0.99999999999999999999999"
    })
    void rejectsInvalidInput(final String input) {
        assertThatIllegalArgumentException().isThrownBy(() -> Address.fromString(input));
    }

    @Test
    void rejectsNullInputWithNullPointerException() {
        assertThatNullPointerException().isThrownBy(() -> Address.fromString(null)).withMessage("address");
    }

    // --- Checksum is validated

    @Test
    void validatesMatchingChecksumsPerNetwork() {
        assertThat(Address.fromString("0.0.123-vfmkw").validateChecksum(MAINNET)).isTrue();
        assertThat(Address.fromString("0.0.123-esxsf").validateChecksum(TESTNET)).isTrue();
        assertThat(Address.fromString("0.0.123-ogizo").validateChecksum(PREVIEWNET)).isTrue();
    }

    @Test
    void rejectsMismatchingChecksums() {
        assertThat(Address.fromString("0.0.123-vfmkw").validateChecksum(TESTNET)).isFalse();
        assertThat(Address.fromString("0.0.123-esxsf").validateChecksum(MAINNET)).isFalse();
        assertThat(Address.fromString("0.0.124-vfmkw").validateChecksum(MAINNET)).isFalse();
    }

    @Test
    void worksThroughBaseReferenceAndAnyUnitType() {
        final BaseAddress base = Address.fromString("0.0.123-esxsf");

        assertThat(base.validateChecksum(TESTNET)).isTrue();
        assertThat(base.validateChecksum(MAINNET)).isFalse();
    }

    @Test
    void emptyStoredChecksumNeverValidates() {
        assertThat(Address.fromString("0.0.123").validateChecksum(MAINNET)).isFalse();
    }

    @Test
    void requiresANetwork() {
        assertThatNullPointerException()
                .isThrownBy(() -> new Address(0, 0, 1, "abcde").validateChecksum(null))
                .withMessage("network");
    }

    // --- Sentinel address is used

    @Test
    void zeroAddressIsAValidZeroSentinel() {
        final var zero = Address.ZERO_ADDRESS;

        assertThat(zero.shard()).isZero();
        assertThat(zero.realm()).isZero();
        assertThat(zero.num()).isEqualTo(0L);
        assertThat(zero.checksum()).isEmpty();
        assertThat(zero.toString()).isEqualTo("0.0.0");
        assertThat(zero).isEqualTo(Address.fromString("0.0.0"));
    }

    // --- Unsigned boundaries

    @Test
    void handlesUnsignedBoundaries() {
        for (final var value : new String[] {"0", String.valueOf(Long.MAX_VALUE), "9223372036854775808", MAX_UINT64}) {
            final var address = Address.fromString(value + "." + value + "." + value);

            assertThat(address.toString()).isEqualTo(value + "." + value + "." + value);
            assertThat(Long.toUnsignedString(address.shard())).isEqualTo(value);
            assertThat(Long.toUnsignedString(address.num())).isEqualTo(value);
        }
        assertThat(Address.fromString("0.0." + MAX_UINT64).num()).isEqualTo(-1L);
        assertThat(Address.fromString("0.0." + MAX_UINT64 + "-abcde").toStringWithChecksum())
                .isEqualTo("0.0." + MAX_UINT64 + "-abcde");
    }

    @Test
    void equalityIncludesChecksum() {
        assertThat(new Address(1, 2, 3, "abcde")).isNotEqualTo(new Address(1, 2, 3));
        assertThat(new Address(1, 2, 3)).hasSameHashCodeAs(new Address(1, 2, 3));
    }
}
