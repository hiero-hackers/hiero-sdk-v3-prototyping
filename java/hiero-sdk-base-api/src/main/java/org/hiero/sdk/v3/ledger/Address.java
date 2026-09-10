// Source: spec/base/ledger.md (ledger#Address).

package org.hiero.sdk.v3.ledger;

import java.util.Objects;

/** Immutable numeric shard/realm address. */
public final class Address extends BaseAddress {
    /** Creates a numeric address. */
    public Address(long shard, long realm, long num, String checksum) {
        super(shard, realm, checksum, num);
    }

    /** Returns the required numeric selector, narrowed from {@link BaseAddress#num()}. */
    @Override
    public Long num() { return Objects.requireNonNull(super.num()); }

    /** Returns {@code shard.realm.num}. */
    @Override
    public String toString() { return shard() + "." + realm() + "." + num(); }

    @Override
    public boolean equals(Object other) {
        return other instanceof Address value
                && shard() == value.shard() && realm() == value.realm()
                && num().equals(value.num()) && checksum().equals(value.checksum());
    }

    @Override
    public int hashCode() { return Objects.hash(shard(), realm(), num(), checksum()); }
}
