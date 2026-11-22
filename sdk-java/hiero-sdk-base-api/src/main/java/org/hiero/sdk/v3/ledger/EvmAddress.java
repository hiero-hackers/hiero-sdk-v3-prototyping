package org.hiero.sdk.v3.ledger;

import java.util.Arrays;
import java.util.HexFormat;
import java.util.Objects;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

/**
 * An immutable standalone EVM address containing exactly 20 bytes in network byte order.
 *
 * <p>Construction and access copy the byte array. Every 20-byte value is accepted without network,
 * account, or contract validation. Equality and hashing compare byte contents. Parsing accepts
 * hexadecimal digits in any case; formatting produces the deterministic
 * <a href="https://eips.ethereum.org/EIPS/eip-55">EIP-55</a> checksum casing.
 *
 * @param bytes the raw address bytes, copied on construction and access
 */
public record EvmAddress(byte @NonNull [] bytes) {
    private static final long[] ROUND_CONSTANTS = {
            0x0000000000000001L, 0x0000000000008082L, 0x800000000000808aL, 0x8000000080008000L,
            0x000000000000808bL, 0x0000000080000001L, 0x8000000080008081L, 0x8000000000008009L,
            0x000000000000008aL, 0x0000000000000088L, 0x0000000080008009L, 0x000000008000000aL,
            0x000000008000808bL, 0x800000000000008bL, 0x8000000000008089L, 0x8000000000008003L,
            0x8000000000008002L, 0x8000000000000080L, 0x000000000000800aL, 0x800000008000000aL,
            0x8000000080008081L, 0x8000000000008080L, 0x0000000080000001L, 0x8000000080008008L
    };
    // Keccak-f[1600] rotation offsets, indexed by x + 5*y.
    private static final int[] ROTATION_OFFSETS = {
            0, 1, 62, 28, 27, 36, 44, 6, 55, 20, 3, 10, 43, 25, 39,
            41, 45, 15, 21, 8, 18, 2, 61, 56, 14
    };

    /**
     * Creates an address, preserving all bytes and taking an independent copy.
     *
     * @param bytes exactly 20 raw bytes, including any leading zeros
     * @throws NullPointerException if {@code bytes} is {@code null}
     * @throws IllegalArgumentException if {@code bytes} does not contain exactly 20 bytes
     */
    public EvmAddress(final byte @NonNull [] bytes) {
        Objects.requireNonNull(bytes, "bytes must not be null");
        if (bytes.length != 20) {
            throw new IllegalArgumentException("bytes must contain exactly 20 bytes");
        }
        this.bytes = bytes.clone();
    }

    /**
     * Constructs an address from exactly 20 raw bytes, without normalization or network validation.
     *
     * @param value the required raw address bytes
     * @return an address owning an independent copy of the supplied bytes
     * @throws NullPointerException if {@code value} is {@code null}
     * @throws IllegalArgumentException if {@code value} does not contain exactly 20 bytes
     */
    public static @NonNull EvmAddress fromBytes(final byte @NonNull [] value) {
        return new EvmAddress(Objects.requireNonNull(value, "value must not be null"));
    }

    /**
     * Parses exactly 40 ASCII hexadecimal digits, optionally prefixed by lowercase {@code 0x}.
     *
     * <p>Lowercase, uppercase, and mixed-case digits are accepted without validating input EIP-55
     * casing. Whitespace, {@code 0X}, signs, separators, suffixes, and shortened values are rejected.
     *
     * @param value the required hexadecimal address text
     * @return the address decoded from left to right, including all leading zero bytes
     * @throws NullPointerException if {@code value} is {@code null}
     * @throws IllegalArgumentException if non-null text is outside the accepted grammar
     */
    public static @NonNull EvmAddress fromString(@NonNull final String value) {
        Objects.requireNonNull(value, "value must not be null");
        final int start = value.startsWith("0x") ? 2 : 0;
        if (value.length() - start != 40) {
            throw new IllegalArgumentException("value must contain exactly 40 ASCII hexadecimal digits");
        }
        // HexFormat accepts only ASCII 0-9, a-f and A-F and rejects every decoration.
        return new EvmAddress(HexFormat.of().parseHex(value, start, value.length()));
    }

    /**
     * Returns an independent copy of the address bytes.
     *
     * @return a fresh copy of the 20 raw address bytes in network byte order
     */
    @Override
    public byte @NonNull [] bytes() {
        return bytes.clone();
    }

    /**
     * Formats the address as lowercase {@code 0x} and 40 EIP-55-cased hexadecimal digits.
     *
     * <p>Casing uses Keccak-256 over the lowercase ASCII hexadecimal body without a prefix. Output
     * preserves all leading zeros and is independent of input casing, locale, network, and chain ID.
     *
     * @return the canonical 42-character address
     */
    @Override
    public @NonNull String toString() {
        final String lower = HexFormat.of().formatHex(bytes);
        final byte[] hash = checksumHash(lower);
        final char[] body = lower.toCharArray();
        for (int index = 0; index < body.length; index++) {
            final int nibble = (Byte.toUnsignedInt(hash[index / 2]) >>> (index % 2 == 0 ? 4 : 0)) & 0x0f;
            if (body[index] >= 'a' && body[index] <= 'f' && nibble >= 8) {
                body[index] = (char) (body[index] - ('a' - 'A'));
            }
        }
        return "0x" + new String(body);
    }

    /**
     * Compares the complete 20-byte address contents.
     *
     * @param other the object to compare, or {@code null}
     * @return whether both objects contain the same ordered bytes
     */
    @Override
    public boolean equals(@Nullable final Object other) {
        return this == other || other instanceof EvmAddress address && Arrays.equals(bytes, address.bytes);
    }

    /** @return the byte-content hash, consistent with {@link #equals(Object)} */
    @Override
    public int hashCode() {
        return Arrays.hashCode(bytes);
    }

    // Keccak-256 specialized to EIP-55's 40 ASCII bytes, which fit in one 136-byte rate block.
    // Permutation and constants: https://keccak.team/keccak_specs_summary.html
    private static byte[] checksumHash(final String lower) {
        final long[] state = new long[25];
        for (int index = 0; index < lower.length(); index++) {
            state[index / 8] ^= (long) lower.charAt(index) << (8 * (index % 8));
        }
        // Original Keccak pad10*1 (suffix 0x01), not SHA3-256's domain-separating 0x06.
        state[5] ^= 0x01L;
        state[16] ^= 0x8000000000000000L;
        final long[] columns = new long[5];
        final long[] rotated = new long[25];
        for (final long roundConstant : ROUND_CONSTANTS) {
            // Theta: mix the neighboring column parities into every lane.
            for (int x = 0; x < 5; x++) {
                columns[x] = state[x] ^ state[x + 5] ^ state[x + 10] ^ state[x + 15] ^ state[x + 20];
            }
            for (int x = 0; x < 5; x++) {
                final long delta = columns[(x + 4) % 5] ^ Long.rotateLeft(columns[(x + 1) % 5], 1);
                for (int y = 0; y < 5; y++) {
                    state[x + 5 * y] ^= delta;
                }
            }
            // Rho/Pi: rotate each lane and permute its coordinates.
            for (int x = 0; x < 5; x++) {
                for (int y = 0; y < 5; y++) {
                    rotated[y + 5 * ((2 * x + 3 * y) % 5)] = Long.rotateLeft(
                            state[x + 5 * y], ROTATION_OFFSETS[x + 5 * y]);
                }
            }
            // Chi and Iota: nonlinear row mixing followed by the round constant.
            for (int x = 0; x < 5; x++) {
                for (int y = 0; y < 5; y++) {
                    state[x + 5 * y] = rotated[x + 5 * y]
                            ^ (~rotated[(x + 1) % 5 + 5 * y] & rotated[(x + 2) % 5 + 5 * y]);
                }
            }
            state[0] ^= roundConstant;
        }
        final byte[] hash = new byte[32];
        for (int index = 0; index < hash.length; index++) {
            hash[index] = (byte) (state[index / 8] >>> (8 * (index % 8)));
        }
        return hash;
    }
}
