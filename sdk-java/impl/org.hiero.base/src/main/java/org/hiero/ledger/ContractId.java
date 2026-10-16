package org.hiero.ledger;

import java.util.Objects;
import org.hiero.ledger.internal.EntityIds;
import org.jspecify.annotations.Nullable;

/// Identifier of a smart contract. A contract may be addressed either by its numeric Hiero id
/// (shard, realm, num) or by its 20-byte EVM address (e.g. from CREATE / CREATE2 / EIP-1014).
/// HAPI carries this as `ContractID { shardNum, realmNum, oneof { contractNum, evm_address } }`;
/// this type mirrors that wire shape exactly: exactly one of `num` and `evmAddress` is set.
public final class ContractId extends EvmCapableAddress {

    /// Creates a new `ContractId`.
    public ContractId(
            final long shard,
            final long realm,
            final String checksum,
            final @Nullable Long num,
            final @Nullable EvmAddress evmAddress) {
        super(shard, realm, checksum, num, evmAddress);
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
        return EntityIds.toString(this, evmAddress());
    }

    /// toString() with an appended "-<checksum>" suffix when a non-empty checksum is set.
    @Override
    public String toStringWithChecksum() {
        return EntityIds.toStringWithChecksum(this, evmAddress());
    }

    /// Parses "shard.realm.num" (with optional "-<checksum>" suffix) or "shard.realm.<40 lowercase
    /// hex digits>" (HIP-583: the EVM address without "0x"). Throws if the format is invalid.
    ///
    /// @throws IllegalArgumentException if an illegal format error occurs
    public static ContractId fromString(final String value) {
        final EntityIds.Parsed parsed = EntityIds.parse(value, true);
        return new ContractId(parsed.shard(), parsed.realm(), parsed.checksum(), parsed.num(), parsed.evmAddress());
    }

    /// Builds a ContractId from a 20-byte EVM address.
    public static ContractId fromEvmAddress(final long shard, final long realm, final EvmAddress address) {
        return new ContractId(shard, realm, "", null, address);
    }

    @Override
    public boolean equals(final @Nullable Object obj) {
        if (this == obj) {
            return true;
        }
        return obj instanceof ContractId other
            && shard() == other.shard()
            && realm() == other.realm()
            && Objects.equals(checksum(), other.checksum())
            && Objects.equals(num(), other.num())
            && Objects.equals(evmAddress(), other.evmAddress());
    }

    @Override
    public int hashCode() {
        return Objects.hash(shard(), realm(), checksum(), num(), evmAddress());
    }
}
