// Generated from spec/base/ledger.md (ledger#Address); DO NOT EDIT.

package org.hiero.sdk.v3.ledger;

import java.util.Objects;

/** Immutable numeric shard/realm address. */
public final class Address implements BaseAddress {
    private final long shard;
    private final long realm;
    private final long num;
    private final String checksum;
    /** Creates a numeric address. */
    public Address(long shard, long realm, long num, String checksum) {
        if (shard < 0 || realm < 0 || num < 0) throw new IllegalArgumentException("address values must be non-negative");
        this.shard = shard; this.realm = realm; this.num = num;
        this.checksum = Objects.requireNonNull(checksum, "checksum");
    }
    @Override public long shard() { return shard; }
    @Override public long realm() { return realm; }
    @Override public String checksum() { return checksum; }
    @Override public Long num() { return num; }
    @Override public boolean equals(Object other) { return other instanceof Address value && shard == value.shard && realm == value.realm && num == value.num && checksum.equals(value.checksum); }
    @Override public int hashCode() { return Objects.hash(shard, realm, num, checksum); }
}
