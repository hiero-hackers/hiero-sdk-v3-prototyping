package org.hiero.sdk.v3.ledger;

import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

/**
 * An immutable numeric entity address with three unsigned 64-bit components and an optional checksum.
 *
 * <p>Parsing and formatting use canonical unsigned decimal text. Checksum validation uses
 * <a href="https://github.com/hiero-ledger/hiero-improvement-proposals/blob/main/HIP/hip-15.md">HIP-15</a>
 * and the target network's exact ledger ID bytes without network I/O. Parsing checks syntax only;
 * callers validate a supplied checksum separately against their network.
 *
 * <p>Java value equality compares all components and the stored checksum. It does not establish
 * entity existence or equate checksummed and unchecksummed addresses.
 */
public final class Address extends BaseAddress {
    private static final Pattern ADDRESS_PATTERN = Pattern.compile(
            "(0|[1-9][0-9]{0,19})\\.(0|[1-9][0-9]{0,19})\\.(0|[1-9][0-9]{0,19})(?:-([a-z]{5}))?");
    private static final int CHECKSUM_MODULUS = 26 * 26 * 26 * 26 * 26;
    private static final int ADDRESS_MODULUS = 26 * 26 * 26;

    /** The {@code 0.0.0} clear-sentinel with no checksum; do not use it as an actual entity ID. */
    public static final @NonNull Address ZERO_ADDRESS = new Address(0L, 0L, 0L, "");

    /**
     * Creates an address from unsigned component bits and a syntactically valid checksum.
     *
     * <p>All {@code long} bit patterns are accepted. The primitive number parameter cannot represent
     * an absent selector. Construction does not validate the checksum against a network.
     *
     * @param shard the unsigned shard bits
     * @param realm the unsigned realm bits
     * @param num the required unsigned entity-number bits
     * @param checksum an empty string or exactly five lowercase ASCII letters
     * @throws NullPointerException if {@code checksum} is {@code null}
     * @throws IllegalArgumentException if a nonempty checksum has invalid syntax
     */
    public Address(final long shard, final long realm, final long num, @NonNull final String checksum) {
        super(shard, realm, num, checksum);
        if (!checksum.isEmpty() && !checksum.matches("[a-z]{5}")) {
            throw new IllegalArgumentException("checksum must be empty or five lowercase ASCII letters");
        }
    }

    /** @return the required immutable unsigned entity-number bits */
    @Override
    public @NonNull Long num() {
        return Objects.requireNonNull(super.num());
    }

    /**
     * Parses canonical {@code shard.realm.num} text, optionally followed by {@code -<checksum>}.
     *
     * <p>Components must be ASCII decimal integers from zero through {@code 18446744073709551615},
     * without leading zeros, signs, or whitespace. A suffix must contain exactly five lowercase
     * ASCII letters. Its network-specific value is retained without validation or normalization.
     *
     * @param address the required address text
     * @return the populated numeric address
     * @throws NullPointerException if {@code address} is {@code null}
     * @throws IllegalArgumentException if the syntax is invalid or a component exceeds unsigned 64-bit range
     */
    public static @NonNull Address fromString(@NonNull final String address) {
        Objects.requireNonNull(address, "address must not be null");
        final Matcher matcher = ADDRESS_PATTERN.matcher(address);
        if (!matcher.matches()) {
            throw new IllegalArgumentException("Invalid numeric address format");
        }
        try {
            final String suffix = matcher.group(4);
            return new Address(Long.parseUnsignedLong(matcher.group(1)), Long.parseUnsignedLong(matcher.group(2)),
                    Long.parseUnsignedLong(matcher.group(3)), suffix == null ? "" : suffix);
        } catch (final NumberFormatException exception) {
            throw new IllegalArgumentException("Address component exceeds unsigned 64-bit range", exception);
        }
    }

    /**
     * Validates a stored HIP-15 checksum using the supplied network's ledger ID bytes.
     *
     * @param network the required target network, with any native-token unit type
     * @return {@code true} when the nonempty stored checksum matches; {@code false} when absent or mismatched
     * @throws NullPointerException if {@code network} is {@code null}, even if the checksum is absent
     */
    @Override
    public boolean validateChecksum(@NonNull final Network<?> network) {
        Objects.requireNonNull(network, "network must not be null");
        return !checksum().isEmpty() && checksum().equals(expectedChecksum(toString(), network.id()));
    }

    /** @return canonical unsigned decimal {@code shard.realm.num}, without the checksum */
    @Override
    public @NonNull String toString() {
        return Long.toUnsignedString(shard()) + "." + Long.toUnsignedString(realm()) + "."
                + Long.toUnsignedString(num());
    }

    /**
     * Compares unsigned components and the stored checksum.
     *
     * @param other the object to compare, or {@code null}
     * @return whether every stored component and checksum is equal
     */
    @Override
    public boolean equals(@Nullable final Object other) {
        return this == other || other instanceof Address address
                && shard() == address.shard() && realm() == address.realm()
                && num().equals(address.num()) && checksum().equals(address.checksum());
    }

    /** @return the value hash of the unsigned components and stored checksum */
    @Override
    public int hashCode() {
        return Objects.hash(shard(), realm(), num(), checksum());
    }

    private static String expectedChecksum(final String address, final byte[] ledgerId) {
        int evenSum = 0;
        int oddSum = 0;
        int addressHash = 0;
        for (int index = 0; index < address.length(); index++) {
            final char character = address.charAt(index);
            final int digit = character == '.' ? 10 : character - '0';
            if (index % 2 == 0) {
                evenSum = (evenSum + digit) % 11;
            } else {
                oddSum = (oddSum + digit) % 11;
            }
            addressHash = (addressHash * 31 + digit) % ADDRESS_MODULUS;
        }
        int ledgerHash = 0;
        for (final byte value : ledgerId) {
            ledgerHash = (ledgerHash * 31 + Byte.toUnsignedInt(value)) % CHECKSUM_MODULUS;
        }
        // HIP-15 appends six zero bytes to the ledger ID before hashing.
        for (int index = 0; index < 6; index++) {
            ledgerHash = ledgerHash * 31 % CHECKSUM_MODULUS;
        }
        final int combined = (((address.length() % 5 * 11 + evenSum) * 11 + oddSum) * ADDRESS_MODULUS
                + addressHash + ledgerHash) % CHECKSUM_MODULUS;
        long permuted = combined * 1_000_003L % CHECKSUM_MODULUS;
        final char[] checksum = new char[5];
        for (int index = checksum.length - 1; index >= 0; index--) {
            checksum[index] = (char) ('a' + permuted % 26);
            permuted /= 26;
        }
        return new String(checksum);
    }
}
