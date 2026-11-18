package org.hiero.sdk.v3.ledger;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.HexFormat;
import java.util.Objects;
import java.util.regex.Pattern;
import org.jspecify.annotations.Nullable;

/**
 * A standalone EVM address: exactly 20 raw bytes in network byte order.
 *
 * <p>This is an immutable value type. The bytes are copied on construction and on every call to {@link #bytes()}, so
 * neither a supplied array nor a returned array can modify the address. Every 20-byte value is accepted, including all
 * zeros, with no check that a corresponding account or contract exists. This type is independent of
 * {@link BaseAddress}, {@link Network}, shard and realm.
 *
 * <p>Two addresses are equal when their bytes are equal. {@link #toString()} returns the canonical EIP-55 form.
 *
 * @param bytes the 20 raw address bytes; copied, never {@code null}
 */
public record EvmAddress(byte[] bytes) {
    private static final int LENGTH = 20;
    private static final Pattern TEXT = Pattern.compile("(?:0x)?([0-9a-fA-F]{40})");

    /**
     * Creates an address from exactly 20 bytes.
     *
     * @throws NullPointerException if {@code bytes} is {@code null}
     * @throws IllegalArgumentException if {@code bytes} does not contain exactly 20 bytes
     */
    public EvmAddress {
        Objects.requireNonNull(bytes, "bytes");
        if (bytes.length != LENGTH) {
            throw new IllegalArgumentException("EVM address must be exactly 20 bytes but was " + bytes.length);
        }
        bytes = bytes.clone();
    }

    /**
     * Creates an address from exactly 20 raw bytes without truncating, padding, reversing or otherwise normalizing
     * them.
     *
     * @param value the raw bytes; copied
     * @return the address
     * @throws NullPointerException if {@code value} is {@code null}
     * @throws IllegalArgumentException if {@code value} does not contain exactly 20 bytes
     */
    public static EvmAddress fromBytes(final byte[] value) {
        return new EvmAddress(Objects.requireNonNull(value, "value"));
    }

    /**
     * Parses exactly 40 ASCII hexadecimal digits with an optional lowercase {@code 0x} prefix. Lowercase, uppercase
     * and mixed-case digits are all accepted and decoded identically; the EIP-55 casing of the input is not checked.
     * Whitespace, an uppercase {@code 0X} prefix, repeated prefixes, signs, separators, suffixes and shortened values
     * are rejected without normalization.
     *
     * @param value the text to parse
     * @return the address
     * @throws NullPointerException if {@code value} is {@code null}
     * @throws IllegalArgumentException if {@code value} is not in the accepted form
     */
    public static EvmAddress fromString(final String value) {
        final var matcher = TEXT.matcher(Objects.requireNonNull(value, "value"));
        if (!matcher.matches()) {
            throw new IllegalArgumentException("Invalid EVM address format: " + value);
        }
        return new EvmAddress(HexFormat.of().parseHex(matcher.group(1)));
    }

    /**
     * Returns a copy of the address bytes.
     *
     * @return a new 20-byte array; modifying it does not affect this address
     */
    @Override
    public byte[] bytes() {
        return bytes.clone();
    }

    /**
     * Returns the canonical EIP-55 form: lowercase {@code 0x} followed by 40 hexadecimal digits with checksum casing.
     * The result is deterministic and independent of input casing, locale, network and chain id.
     *
     * @return the 42-character canonical string
     */
    @Override
    public String toString() {
        final var digits = HexFormat.of().formatHex(bytes);
        final var hash = Keccak256.hash(digits.getBytes(StandardCharsets.US_ASCII));
        final var out = new StringBuilder(42).append("0x");
        for (int i = 0; i < digits.length(); i++) {
            final char digit = digits.charAt(i);
            final int nibble = (hash[i / 2] >> (i % 2 == 0 ? 4 : 0)) & 0xf;
            out.append(digit >= 'a' && nibble >= 8 ? Character.toUpperCase(digit) : digit);
        }
        return out.toString();
    }

    @Override
    public boolean equals(final @Nullable Object other) {
        return other instanceof EvmAddress that && Arrays.equals(bytes, that.bytes);
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(bytes);
    }
}
