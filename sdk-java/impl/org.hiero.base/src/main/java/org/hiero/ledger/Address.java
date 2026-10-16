package org.hiero.ledger;

import java.util.Objects;
import org.hiero.ledger.internal.EntityIds;
import org.jspecify.annotations.Nullable;

/// Concrete address with a single numeric selector. Used for entities whose HAPI id is purely
/// (shard, realm, num): tokens, topics, files, schedules. NOT used for accounts or contracts —
/// those carry additional selector variants (see ContractId / AccountId). This is also the
/// typed identifier used throughout the SDK wherever a single-num entity appears.
public final class Address extends BaseAddress {

    /// Creates a new `Address`.
    ///
    /// @throws NullPointerException if a required value is `null`
    public Address(final long shard, final long realm, final String checksum, final Long num) {
        super(shard, realm, checksum, num);
        Objects.requireNonNull(num, "num must not be null");
    }

    @Override
    public Long num() {
        return super.num();
    }

    /// Validates the checksum against the given network's checksum scheme.
    @Override
    public boolean validateChecksum(final Network<?> network) {
        return EntityIds.validateChecksum(this, network);
    }

    /// Canonical string form, without checksum. Subtype-specific: "shard.realm.num" for Address /
    /// numeric-form ContractId / numeric-form AccountId, each number in decimal without leading
    /// zeros (HIP-15); "shard.realm.<40 lowercase hex digits>" without "0x" for EVM-form ContractId /
    /// AccountId (HIP-583); "shard.realm.<base32>" for key-alias-form AccountId (HIP-32).
    @Override
    public String toString() {
        return EntityIds.toString(this, null);
    }

    /// toString() with an appended "-<checksum>" suffix when a non-empty checksum is set.
    @Override
    public String toStringWithChecksum() {
        return EntityIds.toStringWithChecksum(this, null);
    }

    /// Parses Address from string format: "shard.realm.num" or "shard.realm.num-checksum"
    /// (optional checksum suffix after the dash), exactly as HIP-15 defines it: decimal numbers
    /// without leading zeros, a checksum of five lowercase letters, no whitespace or other
    /// characters. Throws if the format is invalid, values are negative, or parsing fails. The
    /// checksum is not validated here (see validateChecksum).
    ///
    /// @throws IllegalArgumentException if an illegal format error occurs
    public static Address fromString(final String address) {
        final EntityIds.Parsed parsed = EntityIds.parse(address, false);
        return new Address(parsed.shard(), parsed.realm(), parsed.checksum(), parsed.num());
    }

    @Override
    public boolean equals(final @Nullable Object obj) {
        if (this == obj) {
            return true;
        }
        return obj instanceof Address other
            && shard() == other.shard()
            && realm() == other.realm()
            && Objects.equals(checksum(), other.checksum())
            && Objects.equals(num(), other.num());
    }

    @Override
    public int hashCode() {
        return Objects.hash(shard(), realm(), checksum(), num());
    }
}
