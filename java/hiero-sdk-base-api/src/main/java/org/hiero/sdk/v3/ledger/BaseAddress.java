// Source: spec/base/ledger.md (ledger#BaseAddress).

package org.hiero.sdk.v3.ledger;

import java.util.Objects;
import org.jspecify.annotations.Nullable;

/**
 * Immutable state shared by identifiers in shard/realm space.
 * Checksum interpretation is deliberately delegated to {@link LedgerOperations}.
 */
public abstract sealed class BaseAddress permits Address, EvmCapableAddress {
    private final long shard;
    private final long realm;
    private final String checksum;
    private final @Nullable Long num;

    /** Creates validated address state. */
    protected BaseAddress(long shard, long realm, String checksum, @Nullable Long num) {
        if (shard < 0 || realm < 0 || (num != null && num < 0)) {
            throw new IllegalArgumentException("address values must be non-negative");
        }
        this.shard = shard;
        this.realm = realm;
        this.checksum = Objects.requireNonNull(checksum, "checksum");
        this.num = num;
    }

    /** Returns the non-negative shard. */
    public final long shard() { return shard; }

    /** Returns the non-negative realm. */
    public final long realm() { return realm; }

    /** Returns the checksum, or an empty string when none is present. */
    public final String checksum() { return checksum; }

    /** Returns the optional numeric selector. */
    public @Nullable Long num() { return num; }

    /** Returns the canonical identifier followed by its checksum when present. */
    public final String toStringWithChecksum() {
        return checksum.isEmpty() ? toString() : toString() + "-" + checksum;
    }

    /** Returns the canonical field-derived identifier representation. */
    @Override
    public abstract String toString();
}
