package org.hiero.sdk.v3.ledger;

import java.util.Objects;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

/**
 * Immutable state shared by entity addresses in a ledger's shard and realm space.
 *
 * <p>Unsigned components use all 64 bits of Java {@code long} values. Negative storage patterns
 * represent unsigned values above {@link Long#MAX_VALUE}. Subclasses define their selector's
 * canonical representation and checksum validation; a numeric selector may be absent here.
 */
public abstract class BaseAddress {
    private final long shard;
    private final long realm;
    private final @Nullable Long num;
    private final @NonNull String checksum;

    /**
     * Creates immutable address state without assigning semantics to a nonnumeric selector.
     *
     * @param shard the unsigned shard bits
     * @param realm the unsigned realm bits
     * @param num the unsigned entity-number bits, or {@code null} for a nonnumeric selector
     * @param checksum the checksum, or an empty string when absent
     * @throws NullPointerException if {@code checksum} is {@code null}
     */
    protected BaseAddress(final long shard, final long realm, @Nullable final Long num,
            @NonNull final String checksum) {
        this.shard = shard;
        this.realm = realm;
        this.num = num;
        this.checksum = Objects.requireNonNull(checksum, "checksum must not be null");
    }

    /** @return the immutable unsigned shard bits */
    public final long shard() {
        return shard;
    }

    /** @return the immutable unsigned realm bits */
    public final long realm() {
        return realm;
    }

    /** @return the immutable unsigned entity-number bits, or {@code null} when absent */
    public @Nullable Long num() {
        return num;
    }

    /** @return the immutable checksum, or an empty string when absent */
    public final @NonNull String checksum() {
        return checksum;
    }

    /**
     * Compares the stored checksum with the expected checksum for the supplied network.
     *
     * <p>Implementations must reject a null network, perform no network I/O, and return {@code false}
     * when the checksum is absent. Subclasses supply the checksum scheme for their selector.
     *
     * @param network the required target network
     * @return whether a stored checksum matches the target network
     * @throws NullPointerException if {@code network} is {@code null}
     */
    public abstract boolean validateChecksum(@NonNull Network<?> network);

    /** @return the canonical address representation without a checksum suffix */
    @Override
    public abstract @NonNull String toString();

    /** @return the canonical address with {@code -<checksum>} appended when the checksum is nonempty */
    public final @NonNull String toStringWithChecksum() {
        return checksum.isEmpty() ? toString() : toString() + "-" + checksum;
    }
}
