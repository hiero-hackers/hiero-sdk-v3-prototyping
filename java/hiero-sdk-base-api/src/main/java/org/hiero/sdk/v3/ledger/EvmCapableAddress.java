// Source: spec/base/ledger.md (ledger#EvmCapableAddress).

package org.hiero.sdk.v3.ledger;

import org.jspecify.annotations.Nullable;

/** Immutable state shared by numeric or EVM-capable identifiers. */
public abstract sealed class EvmCapableAddress extends BaseAddress permits ContractId, AccountId {
    private final @Nullable EvmAddress evmAddress;

    /** Creates validated shared state. */
    protected EvmCapableAddress(
            long shard, long realm, String checksum, @Nullable Long num, @Nullable EvmAddress evmAddress) {
        super(shard, realm, checksum, num);
        this.evmAddress = evmAddress;
    }

    /** Returns the optional EVM selector. */
    public final @Nullable EvmAddress evmAddress() { return evmAddress; }
}
