// Source: spec/base/ledger.md (ledger#ContractId).

package org.hiero.sdk.v3.ledger;

import java.util.Objects;
import org.jspecify.annotations.Nullable;

/** Immutable contract identifier with exactly one numeric or EVM selector. */
public final class ContractId extends EvmCapableAddress {
    /** Creates a contract identifier. */
    public ContractId(
            long shard, long realm, @Nullable Long num, String checksum, @Nullable EvmAddress evmAddress) {
        super(shard, realm, checksum, num, evmAddress);
        if ((num == null) == (evmAddress == null)) {
            throw new IllegalArgumentException("exactly one of num and evmAddress is required");
        }
    }

    /** Builds an EVM-form identifier without parsing or provider selection. */
    public static ContractId fromEvmAddress(long shard, long realm, EvmAddress address) {
        return new ContractId(shard, realm, null, "", Objects.requireNonNull(address, "address"));
    }

    /** Returns the canonical numeric or EVM-field representation. */
    @Override
    public String toString() {
        Object selector = num() == null ? evmAddress() : num();
        return shard() + "." + realm() + "." + selector;
    }

    @Override
    public boolean equals(Object other) {
        return other instanceof ContractId value
                && shard() == value.shard() && realm() == value.realm()
                && Objects.equals(num(), value.num()) && checksum().equals(value.checksum())
                && Objects.equals(evmAddress(), value.evmAddress());
    }

    @Override
    public int hashCode() { return Objects.hash(shard(), realm(), num(), checksum(), evmAddress()); }
}
