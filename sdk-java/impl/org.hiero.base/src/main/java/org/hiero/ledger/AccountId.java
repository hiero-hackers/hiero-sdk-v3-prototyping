package org.hiero.ledger;

import java.util.Arrays;
import java.util.Objects;
import org.hiero.ledger.internal.EntityIds;
import org.jspecify.annotations.Nullable;

/// Identifier of an account. An account may be addressed either by its numeric Hiero id
/// (shard, realm, num), by a 20-byte EVM-address alias (HIP-583), or by a HIP-32 public-key
/// alias (a serialised public key used to derive the account before an explicit account
/// number is assigned).
///
/// HAPI carries this as `AccountID { shardNum, realmNum, oneof { accountNum, alias: bytes } }`
/// where the bytes-alias is opaque on the wire: a 20-byte payload is interpreted as an EVM
/// address (HIP-583), a longer payload is interpreted as a serialised public key (HIP-32). V3
/// splits that single HAPI alias slot into two typed fields (`evmAddress`, inherited from
/// EvmCapableAddress, and `alias`) so the type system tells the caller which alias form they
/// hold — no length-checking by callers. Exactly one of `num`, `evmAddress` and `alias` is set.
public final class AccountId extends EvmCapableAddress {

    private final byte @Nullable [] alias;

    /// Creates a new `AccountId`.
    ///
    /// @throws IllegalArgumentException if a value violates its constraints
    public AccountId(
            final long shard,
            final long realm,
            final String checksum,
            final @Nullable Long num,
            final @Nullable EvmAddress evmAddress,
            final byte @Nullable [] alias) {
        super(shard, realm, checksum, num, evmAddress);
        this.alias = alias == null ? null : alias.clone();
        if ((num != null ? 1 : 0) + (evmAddress != null ? 1 : 0) + (alias != null ? 1 : 0) != 1) {
            throw new IllegalArgumentException("exactly one of num, evmAddress, alias must be set");
        }
    }

    /// serialised public-key alias (HIP-32); absent when addressed by Hiero number or by EVM address
    public byte @Nullable [] alias() {
        return alias == null ? null : alias.clone();
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

    /// Parses "shard.realm.num" (with optional "-<checksum>" suffix), "shard.realm.<40 lowercase hex
    /// digits>" (HIP-583: the EVM address without "0x"), or "shard.realm.<base32 key alias>"
    /// (HIP-32). Throws if the format is invalid.
    ///
    /// @throws IllegalArgumentException if an illegal format error occurs
    public static AccountId fromString(final String value) {
        final EntityIds.Parsed parsed = EntityIds.parse(value, true);
        return new AccountId(parsed.shard(), parsed.realm(), parsed.checksum(), parsed.num(), parsed.evmAddress(), null);
    }

    /// Builds an AccountId from a 20-byte EVM address (HIP-583 auto-create form).
    public static AccountId fromEvmAddress(final long shard, final long realm, final EvmAddress address) {
        return new AccountId(shard, realm, "", null, address, null);
    }

    /// Returns a builder for a new `AccountId`.
    ///
    /// @return the builder
    public static Builder builder() {
        return new Builder();
    }

    /// The builder of `AccountId`.
    ///
    /// `build()` creates the `AccountId` of the values set one by one.
    ///
    /// A value that is not set is `null`, or has its default value.
    public static final class Builder {

        private @Nullable Long shard;
        private @Nullable Long realm;
        private @Nullable String checksum;
        private @Nullable Long num;
        private @Nullable EvmAddress evmAddress;
        private byte @Nullable [] alias;

        private Builder() {
            // created by AccountId.builder()
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

        /// Sets the `alias`.
        ///
        /// serialised public-key alias (HIP-32); absent when addressed by Hiero number or by EVM address
        ///
        /// @param alias the value
        /// @return this builder
        public Builder alias(final byte @Nullable [] alias) {
            this.alias = alias;
            return this;
        }

        /// Creates the `AccountId` of the values set.
        ///
        /// @return the new `AccountId`
        /// @throws NullPointerException if a required value is not set
        /// @throws IllegalArgumentException if a value violates its constraints
        public AccountId build() {
            return new AccountId(
                    Objects.requireNonNull(shard, "shard must not be null"),
                    Objects.requireNonNull(realm, "realm must not be null"),
                    Objects.requireNonNull(checksum, "checksum must not be null"),
                    num,
                    evmAddress,
                    alias);
        }
    }

    @Override
    public boolean equals(final @Nullable Object obj) {
        if (this == obj) {
            return true;
        }
        return obj instanceof AccountId other
            && shard() == other.shard()
            && realm() == other.realm()
            && Objects.equals(checksum(), other.checksum())
            && Objects.equals(num(), other.num())
            && Objects.equals(evmAddress(), other.evmAddress())
            && Arrays.equals(alias(), other.alias());
    }

    @Override
    public int hashCode() {
        return Objects.hash(shard(), realm(), checksum(), num(), evmAddress(), Arrays.hashCode(alias()));
    }
}
