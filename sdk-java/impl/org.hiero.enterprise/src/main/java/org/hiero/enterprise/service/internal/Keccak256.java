package org.hiero.enterprise.service.internal;

import java.util.Arrays;
import java.util.Objects;

/// Keccak-256 as used by Ethereum and the EVM (the original Keccak padding, not the one of SHA3-256).
final class Keccak256 {

    private static final int RATE = 136;

    private static final long[] ROUND_CONSTANTS = {
        0x0000000000000001L, 0x0000000000008082L, 0x800000000000808aL, 0x8000000080008000L,
        0x000000000000808bL, 0x0000000080000001L, 0x8000000080008081L, 0x8000000000008009L,
        0x000000000000008aL, 0x0000000000000088L, 0x0000000080008009L, 0x000000008000000aL,
        0x000000008000808bL, 0x800000000000008bL, 0x8000000000008089L, 0x8000000000008003L,
        0x8000000000008002L, 0x8000000000000080L, 0x000000000000800aL, 0x800000008000000aL,
        0x8000000080008081L, 0x8000000000008080L, 0x0000000080000001L, 0x8000000080008008L
    };

    private static final int[] ROTATIONS = {
        1, 3, 6, 10, 15, 21, 28, 36, 45, 55, 2, 14, 27, 41, 56, 8, 25, 43, 62, 18, 39, 61, 20, 44
    };

    private static final int[] PERMUTATION = {
        10, 7, 11, 17, 18, 3, 5, 16, 8, 21, 24, 4, 15, 23, 19, 13, 12, 2, 20, 14, 22, 9, 6, 1
    };

    private Keccak256() {
    }

    /// The Keccak-256 hash of the input.
    ///
    /// @param input the input
    /// @return the 32 bytes of the hash
    static byte[] hash(final byte[] input) {
        Objects.requireNonNull(input, "input must not be null");
        final int blocks = input.length / RATE + 1;
        final byte[] padded = Arrays.copyOf(input, blocks * RATE);
        padded[input.length] ^= 0x01;
        padded[padded.length - 1] ^= (byte) 0x80;
        final long[] state = new long[25];
        for (int block = 0; block < blocks; block++) {
            for (int lane = 0; lane < RATE / 8; lane++) {
                state[lane] ^= littleEndian(padded, block * RATE + lane * 8);
            }
            permute(state);
        }
        final byte[] hash = new byte[32];
        for (int i = 0; i < hash.length; i++) {
            hash[i] = (byte) (state[i / 8] >>> (8 * (i % 8)));
        }
        return hash;
    }

    private static long littleEndian(final byte[] bytes, final int offset) {
        long value = 0;
        for (int i = 7; i >= 0; i--) {
            value = (value << 8) | (bytes[offset + i] & 0xFFL);
        }
        return value;
    }

    private static void permute(final long[] state) {
        final long[] columns = new long[5];
        for (final long roundConstant : ROUND_CONSTANTS) {
            // theta
            for (int x = 0; x < 5; x++) {
                columns[x] = state[x] ^ state[x + 5] ^ state[x + 10] ^ state[x + 15] ^ state[x + 20];
            }
            for (int x = 0; x < 5; x++) {
                final long t = columns[(x + 4) % 5] ^ Long.rotateLeft(columns[(x + 1) % 5], 1);
                for (int y = 0; y < 25; y += 5) {
                    state[y + x] ^= t;
                }
            }
            // rho and pi
            long current = state[1];
            for (int i = 0; i < 24; i++) {
                final int target = PERMUTATION[i];
                final long next = state[target];
                state[target] = Long.rotateLeft(current, ROTATIONS[i]);
                current = next;
            }
            // chi
            for (int y = 0; y < 25; y += 5) {
                for (int x = 0; x < 5; x++) {
                    columns[x] = state[y + x];
                }
                for (int x = 0; x < 5; x++) {
                    state[y + x] ^= ~columns[(x + 1) % 5] & columns[(x + 2) % 5];
                }
            }
            // iota
            state[0] ^= roundConstant;
        }
    }
}
