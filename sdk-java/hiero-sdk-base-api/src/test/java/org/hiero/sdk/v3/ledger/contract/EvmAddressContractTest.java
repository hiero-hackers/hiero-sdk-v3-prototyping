package org.hiero.sdk.v3.ledger.contract;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;
import static org.assertj.core.api.Assertions.assertThatNullPointerException;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.HexFormat;
import java.util.List;
import java.util.Locale;
import java.util.Random;
import java.util.stream.Stream;
import org.hiero.sdk.v3.ledger.EvmAddress;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;

class EvmAddressContractTest {
    private static final String LOWER = "fb6916095ca1df60bb79ce92ce3ea74c37c5d359";
    private static final String CANONICAL = "0xfB6916095ca1df60bB79Ce92cE3Ea74c37c5d359";

    private static byte[] bytes(final int length) {
        final var out = new byte[length];
        for (int i = 0; i < length; i++) {
            out[i] = (byte) (i + 1);
        }
        return out;
    }

    // --- State, ownership

    @Test
    void exposesTheSameOrderedBytes() {
        final var source = bytes(20);

        assertThat(EvmAddress.fromBytes(source).bytes()).containsExactly(source);
        assertThat(new EvmAddress(source).bytes()).containsExactly(source);
    }

    @Test
    void sourceMutationAfterConstructionHasNoEffect() {
        for (final var create : List.<java.util.function.Function<byte[], EvmAddress>>of(
                EvmAddress::fromBytes, EvmAddress::new)) {
            final var source = bytes(20);
            final var address = create.apply(source);
            final var formatted = address.toString();

            source[0] = 99;

            assertThat(address.bytes()).containsExactly(bytes(20));
            assertThat(address.toString()).isEqualTo(formatted);
        }
    }

    @Test
    void returnedBytesAreIndependentCopies() {
        final var address = EvmAddress.fromBytes(bytes(20));
        final var formatted = address.toString();

        final var first = address.bytes();
        first[5] = 99;

        assertThat(address.bytes()).containsExactly(bytes(20)).isNotSameAs(first);
        assertThat(address.toString()).isEqualTo(formatted);
        assertThat(address).isEqualTo(EvmAddress.fromBytes(bytes(20)));
    }

    // --- fromBytes

    @Test
    void retainsLeadingZerosAndExtremeBytesAndAllZeros() {
        final var withZeros = new byte[20];
        withZeros[19] = 1;
        final var extreme = new byte[20];
        java.util.Arrays.fill(extreme, (byte) 0xff);
        extreme[0] = (byte) 0x80;

        assertThat(EvmAddress.fromBytes(withZeros).bytes()).containsExactly(withZeros);
        assertThat(EvmAddress.fromBytes(extreme).bytes()).containsExactly(extreme);
        assertThat(EvmAddress.fromBytes(new byte[20]).bytes()).containsExactly(new byte[20]);
    }

    @ParameterizedTest
    @ValueSource(ints = {0, 1, 19, 21, 32})
    void rejectsWrongByteLengths(final int length) {
        assertThatIllegalArgumentException().isThrownBy(() -> EvmAddress.fromBytes(new byte[length]));
        assertThatIllegalArgumentException().isThrownBy(() -> new EvmAddress(new byte[length]));
    }

    @Test
    void rejectsNullBytes() {
        assertThatNullPointerException().isThrownBy(() -> EvmAddress.fromBytes(null)).withMessage("value");
        assertThatNullPointerException().isThrownBy(() -> new EvmAddress(null)).withMessage("bytes");
    }

    // --- fromString

    @Test
    void parsesPrefixedAndUnprefixedText() {
        final var expected = HexFormat.of().parseHex(LOWER);

        assertThat(EvmAddress.fromString("0x" + LOWER).bytes()).containsExactly(expected);
        assertThat(EvmAddress.fromString(LOWER).bytes()).containsExactly(expected);
    }

    @Test
    void letterCaseDoesNotChangeTheParsedAddress() {
        final var expected = HexFormat.of().parseHex(LOWER);
        final var mixedNotEip55 = "Fb6916095ca1df60bb79ce92ce3ea74c37c5d359";

        for (final var text : List.of(LOWER, LOWER.toUpperCase(Locale.ROOT), CANONICAL, mixedNotEip55)) {
            assertThat(EvmAddress.fromString(text).bytes()).containsExactly(expected);
        }
    }

    static Stream<String> malformedText() {
        final var a40 = "a".repeat(40);
        return Stream.of(
                "", "0x", "0x" + "a".repeat(39), "0x" + "a".repeat(41), "a".repeat(39), "a".repeat(41),
                "0x" + "g" + "a".repeat(39), "0x" + "\uff10" + "a".repeat(39),
                "0x" + "a".repeat(20) + " " + "a".repeat(19), " 0x" + a40, "0x" + a40 + " ", "0x" + a40 + "\n",
                "0X" + a40, "0x0x" + a40, "+0x" + a40, "-" + a40, "0x" + "a".repeat(20) + "_" + "a".repeat(19),
                "0x" + "a".repeat(20) + ":" + "a".repeat(20), "0x" + a40 + "-abcde", "0.0.123");
    }

    @ParameterizedTest
    @MethodSource("malformedText")
    void rejectsMalformedText(final String text) {
        assertThatIllegalArgumentException().isThrownBy(() -> EvmAddress.fromString(text));
    }

    @Test
    void rejectsNullTextWithDescriptiveNullPointerException() {
        assertThatThrownBy(() -> EvmAddress.fromString(null))
                .isInstanceOf(NullPointerException.class)
                .hasMessage("value");
    }

    // --- toString

    @Test
    void formatsTheEip55ReferenceVector() {
        assertThat(EvmAddress.fromString("0xfb6916095ca1df60bb79ce92ce3ea74c37c5d359").toString())
                .isEqualTo(CANONICAL);
    }

    @ParameterizedTest
    @ValueSource(strings = {
        // EIP-55 reference vectors: all caps, all lower, mixed
        "0x52908400098527886E0F7030069857D2E4169EE7", "0x8617E340B3D01FA5F11F306F4090FD50E238070D",
        "0xde709f2102306220921060314715629080e2fb77", "0x27b1fdb04752bbc536007a920d24acb045561c26",
        "0x5aAeb6053F3E94C9b9A09f33669435E7Ef1BeAed", "0xfB6916095ca1df60bB79Ce92cE3Ea74c37c5d359",
        "0xdbF03B407c01E7cD3CBea99509d93f8DDDC8C6FB", "0xD1220A0cf47c7B9Be7A2E6BA89F429762e7b9aDb"
    })
    void formatsEveryReferenceVectorCanonically(final String canonical) {
        assertThat(EvmAddress.fromString(canonical.toLowerCase(Locale.ROOT)).toString()).isEqualTo(canonical);
        assertThat(EvmAddress.fromString(canonical).toString()).isEqualTo(canonical);
    }

    @Test
    void preservesLeadingZerosInFormattedText() {
        final var zero = EvmAddress.fromBytes(new byte[20]).toString();

        assertThat(zero).isEqualTo("0x0000000000000000000000000000000000000000").hasSize(42);
    }

    @Test
    void formattingIsLocaleIndependent() {
        final var original = Locale.getDefault();
        try {
            Locale.setDefault(Locale.forLanguageTag("tr-TR"));
            assertThat(EvmAddress.fromString(LOWER).toString()).isEqualTo(CANONICAL);
        } finally {
            Locale.setDefault(original);
        }
    }

    // --- Round trips

    @Test
    void bytesSurviveATextRoundTrip() {
        final var random = new Random(42);
        for (int i = 0; i < 200; i++) {
            final var raw = new byte[20];
            random.nextBytes(raw);
            if (i % 3 == 0) {
                raw[0] = 0;
                raw[1] = 0;
            }
            final var text = EvmAddress.fromBytes(raw).toString();
            final var parsed = EvmAddress.fromString(text);

            assertThat(parsed.bytes()).containsExactly(raw);
            assertThat(parsed.toString()).isEqualTo(text);
        }
    }

    @Test
    void acceptedSpellingsNormalizeToOneCanonicalForm() {
        for (final var text : List.of(LOWER, "0x" + LOWER, LOWER.toUpperCase(Locale.ROOT), "0x" + CANONICAL.substring(2))) {
            final var formatted = EvmAddress.fromString(text).toString();

            assertThat(formatted).isEqualTo(CANONICAL);
            assertThat(EvmAddress.fromString(formatted).toString()).isEqualTo(CANONICAL);
        }
    }

    // --- Value semantics

    @Test
    void equalityAndHashingUseByteContents() {
        final var a = EvmAddress.fromBytes(bytes(20));
        final var b = EvmAddress.fromBytes(bytes(20));
        final var other = bytes(20);
        other[19] = 0;

        assertThat(a).isEqualTo(b).hasSameHashCodeAs(b);
        assertThat(a).isNotEqualTo(EvmAddress.fromBytes(other));
    }
}
