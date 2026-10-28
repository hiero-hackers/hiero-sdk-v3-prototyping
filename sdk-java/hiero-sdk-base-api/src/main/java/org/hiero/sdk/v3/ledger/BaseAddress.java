package org.hiero.sdk.v3.ledger;

import java.util.Objects;
import org.jspecify.annotations.Nullable;

/**
 * Abstract base for every entity identifier that lives in the (shard, realm) space of a Hiero ledger.
 *
 * <p>The {@code uint64} components are stored as Java {@code long} bit patterns: values above
 * {@link Long#MAX_VALUE} appear as negative Java values and must be interpreted with the {@code Long.*Unsigned}
 * methods. Instances are immutable.
 */
public abstract class BaseAddress {
    private final long shard;
    private final long realm;
    private final @Nullable Long num;
    private final String checksum;

    /**
     * Creates the shared address state.
     *
     * @param shard the shard number, as unsigned 64-bit bits
     * @param realm the realm number, as unsigned 64-bit bits
     * @param num the entity number as unsigned 64-bit bits, or {@code null} for address kinds without a numeric
     *         selector
     * @param checksum the protocol checksum, or the empty string when no checksum applies
     * @throws NullPointerException if {@code checksum} is {@code null}
     */
    protected BaseAddress(final long shard, final long realm, final @Nullable Long num, final String checksum) {
        this.shard = shard;
        this.realm = realm;
        this.num = num;
        this.checksum = Objects.requireNonNull(checksum, "checksum");
    }

    /**
     * Returns the shard number.
     *
     * @return the shard number as unsigned 64-bit bits
     */
    public final long shard() {
        return shard;
    }

    /**
     * Returns the realm number.
     *
     * @return the realm number as unsigned 64-bit bits
     */
    public final long realm() {
        return realm;
    }

    /**
     * Returns the checksum.
     *
     * @return the checksum, or the empty string when no checksum applies
     */
    public final String checksum() {
        return checksum;
    }

    /**
     * Returns the entity number, if this kind of address has one.
     *
     * @return the entity number as unsigned 64-bit bits, or {@code null}
     */
    public @Nullable Long num() {
        return num;
    }

    /**
     * Validates the stored checksum against the checksum scheme of the given network. No network I/O is performed.
     *
     * @param network the network whose checksum is expected
     * @return {@code true} if the stored checksum matches the one expected for {@code network}
     * @throws NullPointerException if {@code network} is {@code null}
     */
    public abstract boolean validateChecksum(Network<?> network);

    /**
     * Returns the canonical string form, without a checksum.
     *
     * @return the canonical string form
     */
    @Override
    public abstract String toString();

    /**
     * Returns {@link #toString()} followed by {@code -<checksum>} when the stored checksum is not empty.
     *
     * @return the canonical string form, with the checksum suffix when one is stored
     */
    public String toStringWithChecksum() {
        return checksum.isEmpty() ? toString() : toString() + "-" + checksum;
    }
}
