// Generated from spec/base/ledger.md (ledger#ContractId); DO NOT EDIT.

package org.hiero.sdk.v3.ledger;

import java.util.Objects;
import org.jspecify.annotations.Nullable;

/** Immutable contract identifier with exactly one numeric or EVM selector. */
public final class ContractId implements EvmCapableAddress {
    private final long shard; private final long realm; private final @Nullable Long num;
    private final String checksum; private final @Nullable EvmAddress evmAddress;
    /** Creates a contract identifier. */
    public ContractId(long shard, long realm, @Nullable Long num, String checksum, @Nullable EvmAddress evmAddress) {
        if (shard < 0 || realm < 0 || (num != null && num < 0)) throw new IllegalArgumentException("identifier values must be non-negative");
        if ((num == null) == (evmAddress == null)) throw new IllegalArgumentException("exactly one of num and evmAddress is required");
        this.shard = shard; this.realm = realm; this.num = num;
        this.checksum = Objects.requireNonNull(checksum, "checksum"); this.evmAddress = evmAddress;
    }
    @Override public long shard() { return shard; }
    @Override public long realm() { return realm; }
    @Override public @Nullable Long num() { return num; }
    @Override public String checksum() { return checksum; }
    @Override public @Nullable EvmAddress evmAddress() { return evmAddress; }
    @Override public boolean equals(Object other) { return other instanceof ContractId value && shard == value.shard && realm == value.realm && Objects.equals(num, value.num) && checksum.equals(value.checksum) && Objects.equals(evmAddress, value.evmAddress); }
    @Override public int hashCode() { return Objects.hash(shard, realm, num, checksum, evmAddress); }
}
