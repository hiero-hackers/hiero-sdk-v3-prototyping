// Generated from spec/base/ledger.md (ledger#AccountId); DO NOT EDIT.

package org.hiero.sdk.v3.ledger;

import java.util.Arrays;
import java.util.Objects;
import org.jspecify.annotations.Nullable;

/** Immutable account identifier with exactly one numeric, EVM, or key-alias selector. */
public final class AccountId implements EvmCapableAddress {
    private final long shard; private final long realm; private final @Nullable Long num;
    private final String checksum; private final @Nullable EvmAddress evmAddress; private final byte @Nullable [] alias;
    /** Creates an account identifier and copies its alias. */
    public AccountId(long shard, long realm, @Nullable Long num, String checksum, @Nullable EvmAddress evmAddress, byte @Nullable [] alias) {
        if (shard < 0 || realm < 0 || (num != null && num < 0)) throw new IllegalArgumentException("identifier values must be non-negative");
        var selectors = (num == null ? 0 : 1) + (evmAddress == null ? 0 : 1) + (alias == null ? 0 : 1);
        if (selectors != 1) throw new IllegalArgumentException("exactly one of num, evmAddress, and alias is required");
        this.shard = shard; this.realm = realm; this.num = num; this.checksum = Objects.requireNonNull(checksum, "checksum");
        this.evmAddress = evmAddress; this.alias = alias == null ? null : alias.clone();
    }
    @Override public long shard() { return shard; }
    @Override public long realm() { return realm; }
    @Override public @Nullable Long num() { return num; }
    @Override public String checksum() { return checksum; }
    @Override public @Nullable EvmAddress evmAddress() { return evmAddress; }
    /** Returns a copy of the optional key alias. */ public byte @Nullable [] alias() { return alias == null ? null : alias.clone(); }
    @Override public boolean equals(Object other) { return other instanceof AccountId value && shard == value.shard && realm == value.realm && Objects.equals(num, value.num) && checksum.equals(value.checksum) && Objects.equals(evmAddress, value.evmAddress) && Arrays.equals(alias, value.alias); }
    @Override public int hashCode() { return Objects.hash(shard, realm, num, checksum, evmAddress, Arrays.hashCode(alias)); }
}
