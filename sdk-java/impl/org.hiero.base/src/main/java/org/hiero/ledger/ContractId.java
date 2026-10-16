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
    ///
    /// @throws IllegalArgumentException if a value violates its constraints
    public ContractId(
            final long shard,
            final long realm,
            final String checksum,
            final @Nullable Long num,
            final @Nullable EvmAddress evmAddress) {
        super(shard, realm, checksum, num, evmAddress);
        if ((num != null ? 1 : 0) + (evmAddress != null ? 1 : 0) != 1) {
            throw new IllegalArgumentException("exactly one of num, evmAddress must be set");
        }
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

    /// Returns a builder for a new `ContractId`.
    ///
    /// @return the builder
    public static Builder builder() {
        return new Builder();
    }

    /// The builder of `ContractId`.
    ///
    /// `build()` creates the `ContractId` of the values set one by one.
    ///
    /// A value that is not set is `null`, or has its default value.
    public static final class Builder {

        private @Nullable Long shard;
        private @Nullable Long realm;
        private @Nullable String checksum;
        private @Nullable Long num;
        private @Nullable EvmAddress evmAddress;

        private Builder() {
            // created by ContractId.builder()
        }

        /// Sets the `shard`.
        ///
        /// shard number
        ///
        /// @param shard the value
        /// @return this builder
        public Builder shard(final long shard) {
            this.shard = shard;
            return this;
        }

        /// Sets the `realm`.
        ///
        /// realm number
        ///
        /// @param realm the value
        /// @return this builder
        public Builder realm(final long realm) {
            this.realm = realm;
            return this;
        }

        /// Sets the `checksum`.
        ///
        /// protocol-side checksum over the (shard, realm, selector) form; empty string when no checksum applies (e.g. an EVM-address-only ContractId / AccountId before materialisation)
        ///
        /// @param checksum the value
        /// @return this builder
        public Builder checksum(final String checksum) {
            this.checksum = checksum;
            return this;
        }

        /// Sets the `num`.
        ///
        /// Hiero entity number. May be absent here because a ContractId / AccountId can be addressed by EVM address or alias without a num; always set on `Address`.
        ///
        /// @param num the value
        /// @return this builder
        public Builder num(final @Nullable Long num) {
            this.num = num;
            return this;
        }

        /// Sets the `evmAddress`.
        ///
        /// 20-byte EVM-side address; absent when addressed by Hiero number only
        ///
        /// @param evmAddress the value
        /// @return this builder
        public Builder evmAddress(final @Nullable EvmAddress evmAddress) {
            this.evmAddress = evmAddress;
            return this;
        }

        /// Creates the `ContractId` of the values set.
        ///
        /// @return the new `ContractId`
        /// @throws NullPointerException if a required value is not set
        /// @throws IllegalArgumentException if a value violates its constraints
        public ContractId build() {
            return new ContractId(
                    Objects.requireNonNull(shard, "shard must not be null"),
                    Objects.requireNonNull(realm, "realm must not be null"),
                    Objects.requireNonNull(checksum, "checksum must not be null"),
                    num,
                    evmAddress);
        }
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
