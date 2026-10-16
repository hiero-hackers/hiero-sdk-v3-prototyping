package org.hiero.mirrornode.token;

import java.math.BigInteger;
import java.time.ZonedDateTime;
import java.util.Arrays;
import java.util.Objects;
import org.hiero.ledger.AccountId;
import org.hiero.ledger.Address;
import org.hiero.token.TokenSupplyType;
import org.hiero.token.TokenType;
import org.jspecify.annotations.Nullable;

public record TokenInfo(
        Address tokenId,
        TokenType type,
        String name,
        String symbol,
        @Nullable String memo,
        long decimals,
        byte[] metadata,
        ZonedDateTime createdTimestamp,
        ZonedDateTime modifiedTimestamp,
        @Nullable ZonedDateTime expiryTimestamp,
        TokenSupplyType supplyType,
        BigInteger initialSupply,
        BigInteger totalSupply,
        BigInteger maxSupply,
        AccountId treasuryAccountId,
        boolean deleted,
        CustomFee customFees) {

    private static final BigInteger INITIAL_SUPPLY_MIN = new BigInteger("-57896044618658097711785492504343953926634992332820282019728792003956564819968");
    private static final BigInteger INITIAL_SUPPLY_MAX = new BigInteger("57896044618658097711785492504343953926634992332820282019728792003956564819967");
    private static final BigInteger TOTAL_SUPPLY_MIN = new BigInteger("-57896044618658097711785492504343953926634992332820282019728792003956564819968");
    private static final BigInteger TOTAL_SUPPLY_MAX = new BigInteger("57896044618658097711785492504343953926634992332820282019728792003956564819967");
    private static final BigInteger MAX_SUPPLY_MIN = new BigInteger("-57896044618658097711785492504343953926634992332820282019728792003956564819968");
    private static final BigInteger MAX_SUPPLY_MAX = new BigInteger("57896044618658097711785492504343953926634992332820282019728792003956564819967");

    /// Creates a new `TokenInfo`.
    ///
    /// @throws NullPointerException if a required value is `null`
    /// @throws IllegalArgumentException if a value violates its constraints
    public TokenInfo {
        Objects.requireNonNull(tokenId, "tokenId must not be null");
        Objects.requireNonNull(type, "type must not be null");
        Objects.requireNonNull(name, "name must not be null");
        Objects.requireNonNull(symbol, "symbol must not be null");
        Objects.requireNonNull(metadata, "metadata must not be null");
        metadata = metadata.clone();
        Objects.requireNonNull(createdTimestamp, "createdTimestamp must not be null");
        Objects.requireNonNull(modifiedTimestamp, "modifiedTimestamp must not be null");
        Objects.requireNonNull(supplyType, "supplyType must not be null");
        Objects.requireNonNull(initialSupply, "initialSupply must not be null");
        if (initialSupply.compareTo(INITIAL_SUPPLY_MIN) < 0 || initialSupply.compareTo(INITIAL_SUPPLY_MAX) > 0) {
            throw new IllegalArgumentException("initialSupply must be between -57896044618658097711785492504343953926634992332820282019728792003956564819968 and 57896044618658097711785492504343953926634992332820282019728792003956564819967");
        }
        Objects.requireNonNull(totalSupply, "totalSupply must not be null");
        if (totalSupply.compareTo(TOTAL_SUPPLY_MIN) < 0 || totalSupply.compareTo(TOTAL_SUPPLY_MAX) > 0) {
            throw new IllegalArgumentException("totalSupply must be between -57896044618658097711785492504343953926634992332820282019728792003956564819968 and 57896044618658097711785492504343953926634992332820282019728792003956564819967");
        }
        Objects.requireNonNull(maxSupply, "maxSupply must not be null");
        if (maxSupply.compareTo(MAX_SUPPLY_MIN) < 0 || maxSupply.compareTo(MAX_SUPPLY_MAX) > 0) {
            throw new IllegalArgumentException("maxSupply must be between -57896044618658097711785492504343953926634992332820282019728792003956564819968 and 57896044618658097711785492504343953926634992332820282019728792003956564819967");
        }
        Objects.requireNonNull(treasuryAccountId, "treasuryAccountId must not be null");
        Objects.requireNonNull(customFees, "customFees must not be null");
    }

    /// Returns a copy of the `metadata` array.
    @Override
    public byte[] metadata() {
        return metadata.clone();
    }

    @Override
    public boolean equals(final @Nullable Object obj) {
        return obj instanceof TokenInfo other
            && Objects.equals(tokenId, other.tokenId)
            && Objects.equals(type, other.type)
            && Objects.equals(name, other.name)
            && Objects.equals(symbol, other.symbol)
            && Objects.equals(memo, other.memo)
            && decimals == other.decimals
            && Arrays.equals(metadata, other.metadata)
            && Objects.equals(createdTimestamp, other.createdTimestamp)
            && Objects.equals(modifiedTimestamp, other.modifiedTimestamp)
            && Objects.equals(expiryTimestamp, other.expiryTimestamp)
            && Objects.equals(supplyType, other.supplyType)
            && Objects.equals(initialSupply, other.initialSupply)
            && Objects.equals(totalSupply, other.totalSupply)
            && Objects.equals(maxSupply, other.maxSupply)
            && Objects.equals(treasuryAccountId, other.treasuryAccountId)
            && deleted == other.deleted
            && Objects.equals(customFees, other.customFees);
    }

    @Override
    public int hashCode() {
        return Objects.hash(tokenId, type, name, symbol, memo, decimals, Arrays.hashCode(metadata), createdTimestamp, modifiedTimestamp, expiryTimestamp, supplyType, initialSupply, totalSupply, maxSupply, treasuryAccountId, deleted, customFees);
    }

    @Override
    public String toString() {
        return "TokenInfo[tokenId=" + tokenId + ", type=" + type + ", name=" + name + ", symbol=" + symbol + ", memo=" + memo + ", decimals=" + decimals + ", metadata=" + "byte[" + metadata.length + "]" + ", createdTimestamp=" + createdTimestamp + ", modifiedTimestamp=" + modifiedTimestamp + ", expiryTimestamp=" + expiryTimestamp + ", supplyType=" + supplyType + ", initialSupply=" + initialSupply + ", totalSupply=" + totalSupply + ", maxSupply=" + maxSupply + ", treasuryAccountId=" + treasuryAccountId + ", deleted=" + deleted + ", customFees=" + customFees + "]";
    }

    /// Returns a builder for a new `TokenInfo`.
    ///
    /// @return the builder
    public static Builder builder() {
        return new Builder();
    }

    /// The builder of `TokenInfo`.
    ///
    /// `build()` creates the `TokenInfo` of the values set one by one.
    ///
    /// A value that is not set is `null`, or has its default value.
    public static final class Builder {

        private @Nullable Address tokenId;
        private @Nullable TokenType type;
        private @Nullable String name;
        private @Nullable String symbol;
        private @Nullable String memo;
        private @Nullable Long decimals;
        private byte @Nullable [] metadata;
        private @Nullable ZonedDateTime createdTimestamp;
        private @Nullable ZonedDateTime modifiedTimestamp;
        private @Nullable ZonedDateTime expiryTimestamp;
        private @Nullable TokenSupplyType supplyType;
        private @Nullable BigInteger initialSupply;
        private @Nullable BigInteger totalSupply;
        private @Nullable BigInteger maxSupply;
        private @Nullable AccountId treasuryAccountId;
        private @Nullable Boolean deleted;
        private @Nullable CustomFee customFees;

        private Builder() {
            // created by TokenInfo.builder()
        }

        /// Sets the `tokenId`.
        ///
        /// @param tokenId the value
        /// @return this builder
        public Builder tokenId(final Address tokenId) {
            this.tokenId = tokenId;
            return this;
        }

        /// Sets the `type`.
        ///
        /// @param type the value
        /// @return this builder
        public Builder type(final TokenType type) {
            this.type = type;
            return this;
        }

        /// Sets the `name`.
        ///
        /// @param name the value
        /// @return this builder
        public Builder name(final String name) {
            this.name = name;
            return this;
        }

        /// Sets the `symbol`.
        ///
        /// @param symbol the value
        /// @return this builder
        public Builder symbol(final String symbol) {
            this.symbol = symbol;
            return this;
        }

        /// Sets the `memo`.
        ///
        /// @param memo the value
        /// @return this builder
        public Builder memo(final @Nullable String memo) {
            this.memo = memo;
            return this;
        }

        /// Sets the `decimals`.
        ///
        /// @param decimals the value
        /// @return this builder
        public Builder decimals(final long decimals) {
            this.decimals = decimals;
            return this;
        }

        /// Sets the `metadata`.
        ///
        /// @param metadata the value
        /// @return this builder
        public Builder metadata(final byte[] metadata) {
            this.metadata = metadata;
            return this;
        }

        /// Sets the `createdTimestamp`.
        ///
        /// @param createdTimestamp the value
        /// @return this builder
        public Builder createdTimestamp(final ZonedDateTime createdTimestamp) {
            this.createdTimestamp = createdTimestamp;
            return this;
        }

        /// Sets the `modifiedTimestamp`.
        ///
        /// @param modifiedTimestamp the value
        /// @return this builder
        public Builder modifiedTimestamp(final ZonedDateTime modifiedTimestamp) {
            this.modifiedTimestamp = modifiedTimestamp;
            return this;
        }

        /// Sets the `expiryTimestamp`.
        ///
        /// @param expiryTimestamp the value
        /// @return this builder
        public Builder expiryTimestamp(final @Nullable ZonedDateTime expiryTimestamp) {
            this.expiryTimestamp = expiryTimestamp;
            return this;
        }

        /// Sets the `supplyType`.
        ///
        /// @param supplyType the value
        /// @return this builder
        public Builder supplyType(final TokenSupplyType supplyType) {
            this.supplyType = supplyType;
            return this;
        }

        /// Sets the `initialSupply`.
        ///
        /// @param initialSupply the value
        /// @return this builder
        public Builder initialSupply(final BigInteger initialSupply) {
            this.initialSupply = initialSupply;
            return this;
        }

        /// Sets the `totalSupply`.
        ///
        /// @param totalSupply the value
        /// @return this builder
        public Builder totalSupply(final BigInteger totalSupply) {
            this.totalSupply = totalSupply;
            return this;
        }

        /// Sets the `maxSupply`.
        ///
        /// @param maxSupply the value
        /// @return this builder
        public Builder maxSupply(final BigInteger maxSupply) {
            this.maxSupply = maxSupply;
            return this;
        }

        /// Sets the `treasuryAccountId`.
        ///
        /// @param treasuryAccountId the value
        /// @return this builder
        public Builder treasuryAccountId(final AccountId treasuryAccountId) {
            this.treasuryAccountId = treasuryAccountId;
            return this;
        }

        /// Sets the `deleted`.
        ///
        /// @param deleted the value
        /// @return this builder
        public Builder deleted(final boolean deleted) {
            this.deleted = deleted;
            return this;
        }

        /// Sets the `customFees`.
        ///
        /// @param customFees the value
        /// @return this builder
        public Builder customFees(final CustomFee customFees) {
            this.customFees = customFees;
            return this;
        }

        /// Creates the `TokenInfo` of the values set.
        ///
        /// @return the new `TokenInfo`
        /// @throws NullPointerException if a required value is not set
        /// @throws IllegalArgumentException if a value violates its constraints
        public TokenInfo build() {
            return new TokenInfo(
                    Objects.requireNonNull(tokenId, "tokenId must not be null"),
                    Objects.requireNonNull(type, "type must not be null"),
                    Objects.requireNonNull(name, "name must not be null"),
                    Objects.requireNonNull(symbol, "symbol must not be null"),
                    memo,
                    Objects.requireNonNull(decimals, "decimals must not be null"),
                    Objects.requireNonNull(metadata, "metadata must not be null"),
                    Objects.requireNonNull(createdTimestamp, "createdTimestamp must not be null"),
                    Objects.requireNonNull(modifiedTimestamp, "modifiedTimestamp must not be null"),
                    expiryTimestamp,
                    Objects.requireNonNull(supplyType, "supplyType must not be null"),
                    Objects.requireNonNull(initialSupply, "initialSupply must not be null"),
                    Objects.requireNonNull(totalSupply, "totalSupply must not be null"),
                    Objects.requireNonNull(maxSupply, "maxSupply must not be null"),
                    Objects.requireNonNull(treasuryAccountId, "treasuryAccountId must not be null"),
                    Objects.requireNonNull(deleted, "deleted must not be null"),
                    Objects.requireNonNull(customFees, "customFees must not be null"));
        }
    }
}
