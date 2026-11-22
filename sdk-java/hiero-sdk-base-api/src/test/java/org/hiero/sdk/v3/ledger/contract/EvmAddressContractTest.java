package org.hiero.sdk.v3.ledger.contract;

import java.util.HexFormat;
import java.util.Locale;
import java.util.random.RandomGenerator;
import java.util.random.RandomGeneratorFactory;
import org.hiero.sdk.v3.ledger.EvmAddress;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.ResourceLock;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNullPointerException;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class EvmAddressContractTest {
    private static final String BODY = "fb6916095ca1df60bb79ce92ce3ea74c37c5d359";
    private static final String CANONICAL = "0xfB6916095ca1df60bB79Ce92cE3Ea74c37c5d359";

    @Nested
    class State {
        @ParameterizedTest
        @ValueSource(booleans = {false, true})
        void observesExactOrderedBytesThroughEveryByteConstructionPath(final boolean direct) {
            // GIVEN
            final byte[] input = HexFormat.of().parseHex("000001027f80feff102030405060708090a0b0c0");
            // WHEN
            final EvmAddress address = direct ? new EvmAddress(input) : EvmAddress.fromBytes(input);
            // THEN
            assertThat(address.bytes()).containsExactly(input).isNotSameAs(input);
        }

        @ParameterizedTest
        @ValueSource(booleans = {false, true})
        void sourceMutationCannotChangeAddressStateFormattingOrHash(final boolean direct) {
            // GIVEN
            final byte[] input = HexFormat.of().parseHex(BODY);
            final EvmAddress address = direct ? new EvmAddress(input) : EvmAddress.fromBytes(input);
            final int hash = address.hashCode();
            final String text = address.toString();
            // WHEN
            input[0] = 0;
            input[19] = 0;
            // THEN
            assertThat(address.bytes()).containsExactly(HexFormat.of().parseHex(BODY));
            assertThat(address.toString()).isEqualTo(text).isEqualTo(CANONICAL);
            assertThat(address.hashCode()).isEqualTo(hash);
            assertThat(address).isEqualTo(EvmAddress.fromString(BODY));
        }

        @ParameterizedTest
        @ValueSource(strings = {"constructor", "bytes", "string"})
        void returnedArraysCannotChangeStoredAddress(final String construction) {
            // GIVEN
            final EvmAddress address = switch (construction) {
                case "constructor" -> new EvmAddress(HexFormat.of().parseHex(BODY));
                case "bytes" -> EvmAddress.fromBytes(HexFormat.of().parseHex(BODY));
                case "string" -> EvmAddress.fromString(BODY);
                default -> throw new AssertionError(construction);
            };
            final byte[] first = address.bytes();
            final byte[] second = address.bytes();
            final int hash = address.hashCode();
            // WHEN
            first[0] = 0;
            second[19] = 0;
            // THEN
            assertThat(first).isNotSameAs(second);
            assertThat(address.bytes()).containsExactly(HexFormat.of().parseHex(BODY))
                    .isNotSameAs(first).isNotSameAs(second);
            assertThat(address.toString()).isEqualTo(CANONICAL);
            assertThat(address.hashCode()).isEqualTo(hash);
        }

        @Test
        void allZeroBytesAreValidAndLeadingZerosRemainInText() {
            // GIVEN / WHEN
            final EvmAddress address = EvmAddress.fromBytes(new byte[20]);
            // THEN
            assertThat(address.bytes()).containsExactly(new byte[20]);
            assertThat(address.toString()).isEqualTo("0x0000000000000000000000000000000000000000").hasSize(42);
        }

        @ParameterizedTest
        @ValueSource(ints = {0, 1, 19, 21, 32, 64})
        void rejectsInvalidByteLengthsThroughConstructorAndFactory(final int length) {
            // GIVEN / WHEN / THEN
            assertThatThrownBy(() -> EvmAddress.fromBytes(new byte[length])).isInstanceOf(IllegalArgumentException.class);
            assertThatThrownBy(() -> new EvmAddress(new byte[length])).isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        void rejectsNullBytesThroughConstructorAndFactory() {
            // GIVEN / WHEN / THEN
            assertThatNullPointerException().isThrownBy(() -> EvmAddress.fromBytes(null))
                    .withMessage("value must not be null");
            assertThatNullPointerException().isThrownBy(() -> new EvmAddress(null))
                    .withMessage("bytes must not be null");
        }

        @Test
        void javaEqualityAndHashingUseByteContents() {
            // GIVEN
            final EvmAddress first = EvmAddress.fromString(BODY);
            final EvmAddress second = new EvmAddress(first.bytes());
            final EvmAddress third = EvmAddress.fromString(CANONICAL);
            // WHEN / THEN
            assertThat(first).isEqualTo(first).isEqualTo(second).isEqualTo(third).hasSameHashCodeAs(second);
            assertThat(second).isEqualTo(first).isEqualTo(third).hasSameHashCodeAs(third);
            assertThat(first.equals(null)).isFalse();
            assertThat(first.equals(CANONICAL)).isFalse();
            final byte[] changed = first.bytes();
            changed[19] ^= 1;
            assertThat(first).isNotEqualTo(EvmAddress.fromBytes(changed));
        }
    }

    @Nested
    class Parsing {
        @ParameterizedTest
        @ValueSource(strings = {"fb6916095ca1df60bb79ce92ce3ea74c37c5d359",
                "FB6916095CA1DF60BB79CE92CE3EA74C37C5D359", "Fb6916095ca1df60bb79ce92ce3ea74c37c5d359"})
        void acceptsBothPrefixesAndAllLetterCasesWithoutInputChecksumValidation(final String body) {
            // GIVEN / WHEN: the third spelling is deliberately not canonical EIP-55
            final EvmAddress bare = EvmAddress.fromString(body);
            final EvmAddress prefixed = EvmAddress.fromString("0x" + body);
            // THEN
            assertThat(bare.bytes()).containsExactly(HexFormat.of().parseHex(BODY));
            assertThat(prefixed).isEqualTo(bare);
            assertThat(bare.toString()).isEqualTo(CANONICAL);
            assertThat(prefixed.toString()).isEqualTo(CANONICAL);
        }

        @ParameterizedTest
        @ValueSource(strings = {"", "0x", "0", "0x1", "000000000000000000000000000000000000000",
                "00000000000000000000000000000000000000000", "0x000000000000000000000000000000000000000",
                "0x00000000000000000000000000000000000000000"})
        void rejectsInvalidTextLength(final String text) {
            // GIVEN / WHEN / THEN
            assertThatThrownBy(() -> EvmAddress.fromString(text)).isInstanceOf(IllegalArgumentException.class);
        }

        @ParameterizedTest
        @ValueSource(strings = {"g", "Ｇ", "０", "١", " ", "\t", "\n", "\u0000"})
        void rejectsNonAsciiHexCharactersWithoutNormalization(final String invalid) {
            // GIVEN
            final String body = BODY.substring(0, 19) + invalid + BODY.substring(20);
            // WHEN / THEN
            assertThatThrownBy(() -> EvmAddress.fromString(body)).isInstanceOf(IllegalArgumentException.class);
            assertThatThrownBy(() -> EvmAddress.fromString("0x" + body)).isInstanceOf(IllegalArgumentException.class);
        }

        @ParameterizedTest
        @ValueSource(strings = {" ", "\n", "\t", "0X", "0x0x", "+", "-", "0x+", "0x-"})
        void rejectsUnsupportedPrefixesAndOuterWhitespace(final String prefix) {
            // GIVEN / WHEN / THEN
            assertThatThrownBy(() -> EvmAddress.fromString(prefix + BODY)).isInstanceOf(IllegalArgumentException.class);
        }

        @ParameterizedTest
        @ValueSource(strings = {" ", "\n", "\t", "-abcde", ":", "."})
        void rejectsUnsupportedSuffixes(final String suffix) {
            // GIVEN / WHEN / THEN
            assertThatThrownBy(() -> EvmAddress.fromString("0x" + BODY + suffix))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        void rejectsSeparatorsAndNullInput() {
            // GIVEN / WHEN / THEN
            assertThatThrownBy(() -> EvmAddress.fromString(BODY.substring(0, 20) + ":" + BODY.substring(20)))
                    .isInstanceOf(IllegalArgumentException.class);
            assertThatNullPointerException().isThrownBy(() -> EvmAddress.fromString(null))
                    .withMessage("value must not be null");
        }
    }

    @Nested
    class Formatting {
        // All eight EIP-55 reference vectors, including canonical uppercase and lowercase bodies.
        @ParameterizedTest
        @ValueSource(strings = {"0x52908400098527886E0F7030069857D2E4169EE7", "0x8617E340B3D01FA5F11F306F4090FD50E238070D",
                "0xde709f2102306220921060314715629080e2fb77", "0x27b1fdb04752bbc536007a920d24acb045561c26",
                "0x5aAeb6053F3E94C9b9A09f33669435E7Ef1BeAed", "0xfB6916095ca1df60bB79Ce92cE3Ea74c37c5d359",
                "0xdbF03B407c01E7cD3CBea99509d93f8DDDC8C6FB", "0xD1220A0cf47c7B9Be7A2E6BA89F429762e7b9aDb"})
        void matchesAuthoritativeEip55Vectors(final String canonical) {
            // GIVEN
            final EvmAddress address = EvmAddress.fromString(canonical.toLowerCase(Locale.ROOT));
            final byte[] before = address.bytes();
            // WHEN / THEN
            assertThat(address.toString()).isEqualTo(canonical).hasSize(42);
            assertThat(address.bytes()).containsExactly(before);
            assertThat(EvmAddress.fromBytes(before).toString()).isEqualTo(canonical);
        }

        @ParameterizedTest
        @ValueSource(longs = {0L, 1L, 42L, Long.MAX_VALUE})
        void variedBytesAndLeadingZerosSurviveCanonicalRoundTrips(final long seed) {
            // GIVEN
            final RandomGenerator random = RandomGeneratorFactory.of("L64X128MixRandom").create(seed);
            for (int iteration = 0; iteration < 32; iteration++) {
                final byte[] input = new byte[20];
                random.nextBytes(input);
                input[0] = 0;
                // WHEN
                final EvmAddress original = EvmAddress.fromBytes(input);
                final String canonical = original.toString();
                final EvmAddress parsed = EvmAddress.fromString(canonical);
                // THEN
                assertThat(canonical).startsWith("0x00").hasSize(42);
                assertThat(parsed).isEqualTo(original).hasSameHashCodeAs(original);
                assertThat(parsed.bytes()).containsExactly(input);
                assertThat(parsed.toString()).isEqualTo(canonical);
                assertThat(EvmAddress.fromString(canonical.substring(2).toUpperCase(Locale.ROOT)).toString())
                        .isEqualTo(canonical);
            }
        }

        @Test
        @ResourceLock("java.util.Locale.default")
        void formattingIsIndependentOfDefaultLocale() {
            // GIVEN
            final Locale previous = Locale.getDefault();
            try {
                // WHEN
                Locale.setDefault(Locale.forLanguageTag("tr-TR"));
                // THEN
                assertThat(EvmAddress.fromString(BODY).toString()).isEqualTo(CANONICAL);
            } finally {
                Locale.setDefault(previous);
            }
        }
    }
}
