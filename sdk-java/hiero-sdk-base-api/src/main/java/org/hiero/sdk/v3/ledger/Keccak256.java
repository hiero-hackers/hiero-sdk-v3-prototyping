package org.hiero.sdk.v3.ledger;

/** Minimal Keccak-256 (the pre-NIST padding variant used by EIP-55), kept internal to the ledger package. */
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
    private static final int[] LANES = {
        10, 7, 11, 17, 18, 3, 5, 16, 8, 21, 24, 4, 15, 23, 19, 13, 12, 2, 20, 14, 22, 9, 6, 1
    };

    private Keccak256() {}

    static byte[] hash(final byte[] input) {
        final var state = new long[25];
        int offset = 0;
        while (input.length - offset >= RATE) {
            absorb(state, input, offset, RATE);
            permute(state);
            offset += RATE;
        }
        final var last = new byte[RATE];
        System.arraycopy(input, offset, last, 0, input.length - offset);
        last[input.length - offset] ^= 0x01;
        last[RATE - 1] ^= (byte) 0x80;
        absorb(state, last, 0, RATE);
        permute(state);

        final var out = new byte[32];
        for (int i = 0; i < out.length; i++) {
            out[i] = (byte) (state[i / 8] >>> (8 * (i % 8)));
        }
        return out;
    }

    private static void absorb(final long[] state, final byte[] block, final int offset, final int length) {
        for (int i = 0; i < length; i++) {
            state[i / 8] ^= (block[offset + i] & 0xffL) << (8 * (i % 8));
        }
    }

    private static void permute(final long[] a) {
        final var c = new long[5];
        for (int round = 0; round < 24; round++) {
            for (int x = 0; x < 5; x++) {
                c[x] = a[x] ^ a[x + 5] ^ a[x + 10] ^ a[x + 15] ^ a[x + 20];
            }
            for (int x = 0; x < 5; x++) {
                final long d = c[(x + 4) % 5] ^ Long.rotateLeft(c[(x + 1) % 5], 1);
                for (int y = 0; y < 25; y += 5) {
                    a[y + x] ^= d;
                }
            }
            long current = a[1];
            for (int i = 0; i < 24; i++) {
                final long next = a[LANES[i]];
                a[LANES[i]] = Long.rotateLeft(current, ROTATIONS[i]);
                current = next;
            }
            for (int y = 0; y < 25; y += 5) {
                System.arraycopy(a, y, c, 0, 5);
                for (int x = 0; x < 5; x++) {
                    a[y + x] = c[x] ^ (~c[(x + 1) % 5] & c[(x + 2) % 5]);
                }
            }
            a[0] ^= ROUND_CONSTANTS[round];
        }
    }
}
