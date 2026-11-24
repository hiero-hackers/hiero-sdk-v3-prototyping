package org.hiero.sdk.v3.ledger;

import java.util.Objects;
import java.util.regex.Pattern;
import org.jspecify.annotations.Nullable;

/**
 * A numeric address in the form {@code shard.realm.num}, with an optional checksum.
 *
 * <p>Components are unsigned 64-bit values held as {@code long} bit patterns (see {@link BaseAddress}). Two addresses
 * are equal when shard, realm, num and checksum are equal.
 *
 * <p>Checksums use the HIP-15 scheme: five lowercase letters derived from the address digits and the network's
 * identifier bytes ({@link Network#id()}).
 */
public final class Address extends BaseAddress {
    /** The zero address {@code 0.0.0} with an empty checksum. It is a sentinel, not an actual entity id. */
    public static final Address ZERO_ADDRESS = new Address(0, 0, 0, "");

    private static final Pattern FORMAT = Pattern.compile("(\\d+)\\.(\\d+)\\.(\\d+)(?:-([a-z]{5}))?");
    private static final int ALPHABET = 26;
    private static final int P3 = ALPHABET * ALPHABET * ALPHABET;
    private static final int P5 = P3 * ALPHABET * ALPHABET;
    private static final int M = 1_000_003;
    private static final int W = 31;

    private final long num;

    /**
     * Creates an address without a checksum.
     *
     * @param shard the shard number as unsigned 64-bit bits
     * @param realm the realm number as unsigned 64-bit bits
     * @param num the entity number as unsigned 64-bit bits
     */
    public Address(final long shard, final long realm, final long num) {
        this(shard, realm, num, "");
    }

    /**
     * Creates an address.
     *
     * @param shard the shard number as unsigned 64-bit bits
     * @param realm the realm number as unsigned 64-bit bits
     * @param num the entity number as unsigned 64-bit bits
     * @param checksum the checksum, or the empty string when none applies
     * @throws NullPointerException if {@code checksum} is {@code null}
     */
    public Address(final long shard, final long realm, final long num, final String checksum) {
        super(shard, realm, num, checksum);
        this.num = num;
    }

    /**
     * Parses {@code shard.realm.num} or {@code shard.realm.num-checksum}. Components are non-negative decimal digits
     * that fit an unsigned 64-bit value; the checksum is exactly five lowercase letters. No other normalization
     * (whitespace, signs, leading separators) is applied.
     *
     * @param address the string to parse
     * @return the parsed address
     * @throws NullPointerException if {@code address} is {@code null}
     * @throws IllegalArgumentException if the format is invalid or a component exceeds the unsigned 64-bit range
     */
    public static Address fromString(final String address) {
        final var matcher = FORMAT.matcher(Objects.requireNonNull(address, "address"));
        if (!matcher.matches()) {
            throw new IllegalArgumentException("Invalid address format: " + address);
        }
        try {
            final var checksum = matcher.group(4);
            return new Address(
                    Long.parseUnsignedLong(matcher.group(1)),
                    Long.parseUnsignedLong(matcher.group(2)),
                    Long.parseUnsignedLong(matcher.group(3)),
                    checksum == null ? "" : checksum);
        } catch (final NumberFormatException e) {
            throw new IllegalArgumentException("Address component out of range: " + address, e);
        }
    }

    /**
     * Returns the entity number.
     *
     * @return the entity number as unsigned 64-bit bits; never {@code null}
     */
    @Override
    public Long num() {
        return num;
    }

    /**
     * {@inheritDoc}
     *
     * <p>Returns {@code false} when the stored checksum is empty.
     */
    @Override
    public boolean validateChecksum(final Network<?> network) {
        Objects.requireNonNull(network, "network");
        return !checksum().isEmpty() && checksum().equals(expectedChecksum(network.id()));
    }

    @Override
    public String toString() {
        return Long.toUnsignedString(shard()) + "." + Long.toUnsignedString(realm()) + "." + Long.toUnsignedString(num);
    }

    @Override
    public boolean equals(final @Nullable Object other) {
        return other instanceof Address that
                && shard() == that.shard()
                && realm() == that.realm()
                && num == that.num
                && checksum().equals(that.checksum());
    }

    @Override
    public int hashCode() {
        return Objects.hash(shard(), realm(), num, checksum());
    }

    private String expectedChecksum(final byte[] ledgerId) {
        final var text = toString();
        long s = 0;
        long s0 = 0;
        long s1 = 0;
        for (int i = 0; i < text.length(); i++) {
            final int digit = text.charAt(i) == '.' ? 10 : text.charAt(i) - '0';
            s = (W * s + digit) % P3;
            if (i % 2 == 0) {
                s0 += digit;
            } else {
                s1 += digit;
            }
        }
        s0 %= 11;
        s1 %= 11;

        long sh = 0;
        for (final byte b : ledgerId) {
            sh = (W * sh + (b & 0xff)) % P5;
        }
        for (int i = 0; i < 6; i++) {
            sh = (W * sh) % P5;
        }

        long c = ((((text.length() % 5) * 11 + s0) * 11 + s1) * P3 + s + sh) % P5;
        c = (c * M) % P5;

        final var out = new char[5];
        for (int i = 4; i >= 0; i--) {
            out[i] = (char) ('a' + c % ALPHABET);
            c /= ALPHABET;
        }
        return new String(out);
    }
}
