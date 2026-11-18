package org.hiero.sdk.v3.ledger.contract;

import java.util.HexFormat;
import java.util.Objects;
import org.hiero.sdk.v3.ledger.Address;
import org.hiero.sdk.v3.ledger.BaseAddress;
import org.hiero.sdk.v3.ledger.Network;
import org.hiero.sdk.v3.nativeToken.NativeTokenUnit;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNullPointerException;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class AddressContractTest {
    @Nested
    class State {
        @Test
        void observesImmutableStateThroughConcreteAndBaseTypes() {
            // GIVEN
            final Address address = new Address(-1L, Long.MIN_VALUE, 123L, "vfmkw");
            final BaseAddress base = address;
            // WHEN / THEN
            assertThat(address.shard()).isEqualTo(-1L);
            assertThat(base.realm()).isEqualTo(Long.MIN_VALUE);
            assertThat(address.num()).isEqualTo(123L);
            assertThat(base.num()).isEqualTo(123L);
            assertThat(base.checksum()).isEqualTo("vfmkw");
            assertThat(base.toString()).isEqualTo("18446744073709551615.9223372036854775808.123");
            assertThat(base.toStringWithChecksum()).isEqualTo(base + "-vfmkw");
        }

        @Test
        void basePermitsAbsentNumberAndLeavesSelectorFormattingToSubclass() {
            // GIVEN
            final BaseAddress base = new OtherSelector("abcde");
            // WHEN / THEN
            assertThat(base.num()).isNull();
            assertThat(base.shard()).isZero();
            assertThat(base.realm()).isZero();
            assertThat(base.toStringWithChecksum()).isEqualTo("test-selector-abcde");
            assertThat(new OtherSelector("").toStringWithChecksum()).isEqualTo("test-selector");
        }

        @Test
        void rejectsNullChecksumsAtBothConstructionBoundaries() {
            // GIVEN / WHEN / THEN
            assertThatNullPointerException().isThrownBy(() -> new Address(0L, 0L, 1L, null))
                    .withMessage("checksum must not be null");
            assertThatNullPointerException().isThrownBy(() -> new OtherSelector(null))
                    .withMessage("checksum must not be null");
        }

        @ParameterizedTest
        @ValueSource(strings = {"a", "abcdef", "ABCDE", "abCde", "abcd1", "ébcde", "abcde\n"})
        void constructorRejectsMalformedNonemptyChecksums(final String checksum) {
            // GIVEN / WHEN / THEN
            assertThatThrownBy(() -> new Address(0L, 0L, 1L, checksum)).isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        void zeroAddressIsImmediatelyUsable() {
            // GIVEN / WHEN
            final Address zero = Address.ZERO_ADDRESS;
            // THEN
            assertThat(zero.shard()).isZero();
            assertThat(zero.realm()).isZero();
            assertThat(zero.num()).isNotNull().isZero();
            assertThat(zero.checksum()).isEmpty();
            assertThat(zero.toString()).isEqualTo("0.0.0");
            assertThat(zero.toStringWithChecksum()).isEqualTo("0.0.0");
        }

        @Test
        void javaValueEqualityIncludesEveryComponentAndStoredChecksum() {
            // GIVEN
            final Address first = new Address(1L, 2L, -1L, "abcde");
            final Address second = Address.fromString("1.2.18446744073709551615-abcde");
            final Address third = new Address(1L, 2L, -1L, "abcde");
            // WHEN / THEN
            assertThat(first).isEqualTo(first).isEqualTo(second).isEqualTo(third).hasSameHashCodeAs(second);
            assertThat(second).isEqualTo(first).isEqualTo(third).hasSameHashCodeAs(third);
            assertThat(first.equals(null)).isFalse();
            assertThat(first.equals(first.toStringWithChecksum())).isFalse();
            assertThat(first).isNotEqualTo(new Address(0L, 2L, -1L, "abcde"));
            assertThat(first).isNotEqualTo(new Address(1L, 0L, -1L, "abcde"));
            assertThat(first).isNotEqualTo(new Address(1L, 2L, 0L, "abcde"));
            assertThat(first).isNotEqualTo(new Address(1L, 2L, -1L, ""));
            assertThat(first).isNotEqualTo(new Address(1L, 2L, -1L, "fghij"));
            assertThat(first.toString()).isEqualTo("1.2.18446744073709551615");
        }
    }

    @Nested
    class Parsing {
        @ParameterizedTest
        @ValueSource(strings = {"0.0.0", "1.2.3", "0.0.123-vfmkw", "12.345.6789-aoyyt",
                "0.0.123-abcde", "18446744073709551615.0.1-zzzzz"})
        void parsesBothFormsAndPreservesChecksumWithoutNetworkValidation(final String text) {
            // GIVEN / WHEN
            final Address address = Address.fromString(text);
            final String canonical = text.contains("-") ? text.substring(0, text.indexOf('-')) : text;
            final String[] components = canonical.split("\\.");
            // THEN
            assertThat(address.shard()).isEqualTo(Long.parseUnsignedLong(components[0]));
            assertThat(address.realm()).isEqualTo(Long.parseUnsignedLong(components[1]));
            assertThat(address.num()).isEqualTo(Long.parseUnsignedLong(components[2]));
            assertThat(address.checksum()).isEqualTo(text.contains("-") ? text.substring(text.indexOf('-') + 1) : "");
            assertThat(address.toString()).isEqualTo(canonical);
            assertThat(address.toStringWithChecksum()).isEqualTo(text);
            assertThat(Address.fromString(address.toStringWithChecksum())).isEqualTo(address);
        }

        @ParameterizedTest
        @CsvSource({"0,0", "9223372036854775807,9223372036854775807",
                "9223372036854775808,-9223372036854775808", "18446744073709551615,-1"})
        void preservesUnsignedBoundariesAcrossAllComponents(final String decimal, final long bits) {
            // GIVEN
            final String text = decimal + "." + decimal + "." + decimal;
            // WHEN
            final Address parsed = Address.fromString(text + "-abcde");
            final Address constructed = new Address(bits, bits, bits, "abcde");
            // THEN
            assertThat(parsed).isEqualTo(constructed);
            assertThat(parsed.shard()).isEqualTo(bits);
            assertThat(parsed.realm()).isEqualTo(bits);
            assertThat(parsed.num()).isEqualTo(bits);
            assertThat(parsed.toString()).isEqualTo(text);
            assertThat(parsed.toStringWithChecksum()).isEqualTo(text + "-abcde");
            assertThat(Address.fromString(text).toStringWithChecksum()).isEqualTo(text);
        }

        @ParameterizedTest
        @ValueSource(strings = {"", "0", "0.0", "0.0.1.2", ".0.1", "0..1", "0.0.", "a.0.1", "0.b.1", "0.0.c",
                "-1.0.1", "0.-1.1", "0.0.-1", "+1.0.1", "0.+1.1", "0.0.+1", "00.0.1", "0.00.1", "0.0.01",
                " 0.0.1", "0.0.1 ", "0.0.1\n", "0.\t0.1", "０.0.1", "0.0.١", "0.0.1-", "0.0.1-abc",
                "0.0.1-abcdef", "0.0.1-ABCDE", "0.0.1-abCde", "0.0.1-abcd1", "0.0.1-abcde-x", "0.0.1--abcde",
                "0.0.1#abcde", "0.0.1-abcde\n", "18446744073709551616.0.1", "0.18446744073709551616.1",
                "0.0.18446744073709551616", "0.0.99999999999999999999999999999999999999"})
        void rejectsInvalidFormatsNegativeDecimalsAndOverflow(final String text) {
            // GIVEN / WHEN / THEN
            assertThatThrownBy(() -> Address.fromString(text)).isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        void distinguishesNullInputFromMalformedText() {
            // GIVEN / WHEN / THEN
            assertThatNullPointerException().isThrownBy(() -> Address.fromString(null))
                    .withMessage("address must not be null");
        }
    }

    @Nested
    class Checksums {
        // All published HIP-15 vectors, including multi-byte ledger IDs with unsigned bytes.
        @ParameterizedTest
        @CsvSource({"00,0.0.1-dfkxr", "00,0.0.4-cjcuq", "00,0.0.5-ktach", "00,0.0.6-tcxjy",
                "00,0.0.12-uuuup", "00,0.0.123-vfmkw", "00,0.0.1234567890-zbhlt", "00,12.345.6789-aoyyt",
                "00,1.23.456-adpbr", "a1ff01,0.0.1-xzlgq", "a1ff01,0.0.4-xdddp", "a1ff01,0.0.5-fnalg",
                "a1ff01,0.0.6-nwxsx", "a1ff01,0.0.12-povdo", "a1ff01,0.0.123-pzmtv",
                "a1ff01,0.0.1234567890-tvhus", "a1ff01,12.345.6789-vizhs", "a1ff01,1.23.456-uxpkq"})
        void validatesPublishedHip15VectorsThroughBaseApi(final String ledgerId, final String text) {
            // GIVEN
            final BaseAddress address = Address.fromString(text);
            final Network<TestUnit> network = new Network<>(HexFormat.of().parseHex(ledgerId), null, TestUnit.BASE);
            // WHEN / THEN
            assertThat(address.validateChecksum(network)).as(text + " on " + ledgerId).isTrue();
            assertThat(address.toStringWithChecksum()).isEqualTo(text);
        }

        @Test
        void rejectsMismatchesAndUsesLedgerIdRatherThanNetworkNameOrUnit() {
            // GIVEN
            final BaseAddress address = Address.fromString("0.0.123-vfmkw");
            final NativeTokenUnit otherUnit = new NativeTokenUnit() {
                @Override public String symbol() { throw new AssertionError("Unit must not be inspected"); }
                @Override public long baseUnitFactor() { throw new AssertionError("Unit must not be inspected"); }
            };
            // WHEN / THEN
            assertThat(address.validateChecksum(new Network<>(new byte[] {0}, "anything", otherUnit))).isTrue();
            assertThat(address.validateChecksum(new Network<>(new byte[] {1}, null, TestUnit.BASE))).isFalse();
            assertThat(Address.fromString("0.0.123-abcde")
                    .validateChecksum(new Network<>(new byte[] {0}, null, TestUnit.BASE))).isFalse();
        }

        @Test
        void absentChecksumIsFalseButStillRequiresNetwork() {
            // GIVEN
            final BaseAddress withoutChecksum = Address.fromString("0.0.123");
            // WHEN / THEN
            assertThat(withoutChecksum.validateChecksum(new Network<>(new byte[0], null, TestUnit.BASE))).isFalse();
            assertThat(Address.ZERO_ADDRESS.validateChecksum(new Network<>(new byte[] {0}, null, TestUnit.BASE)))
                    .isFalse();
            assertThatNullPointerException().isThrownBy(() -> withoutChecksum.validateChecksum(null))
                    .withMessage("network must not be null");
            assertThatNullPointerException().isThrownBy(() -> Address.fromString("0.0.123-vfmkw").validateChecksum(null))
                    .withMessage("network must not be null");
        }
    }

    private static final class OtherSelector extends BaseAddress {
        private OtherSelector(final String checksum) {
            super(0L, 0L, null, checksum);
        }

        @Override
        public boolean validateChecksum(final Network<?> network) {
            Objects.requireNonNull(network, "network must not be null");
            return false;
        }

        @Override
        public String toString() {
            return "test-selector";
        }
    }

    private enum TestUnit implements NativeTokenUnit {
        BASE;
        @Override public String symbol() { return "base"; }
        @Override public long baseUnitFactor() { return 1L; }
    }
}
