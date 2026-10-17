package org.hiero.consensusnode.queries.tokens;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.ZonedDateTime;
import java.util.Arrays;
import java.util.Objects;
import org.hiero.authority.Authority;
import org.hiero.ledger.AccountId;
import org.hiero.ledger.Address;
import org.hiero.token.TokenSupplyType;
import org.hiero.token.TokenType;
import org.jspecify.annotations.Nullable;

/// Full token metadata snapshot. Returned by `TokenInfoQuery`. Covers both fungible tokens and NFT
/// collections; `type` distinguishes them. A deleted token is returned with
/// `deleted = true` rather than failing the query.
///
/// @param type FUNGIBLE_COMMON | NON_FUNGIBLE_UNIQUE
/// @param decimals smallest-unit precision; 0 for NON_FUNGIBLE_UNIQUE
/// @param totalSupply current total supply, in the smallest indivisible unit
/// @param treasuryAccountId account holding the supply pool
/// @param supplyType INFINITE | FINITE
/// @param maxSupply maximum supply enforced by the network; absent when supplyType is INFINITE
/// @param tokenMemo short human-readable label
/// @param metadata opaque token-level metadata (e.g. IPFS CID, HTTPS URL, JSON manifest)
/// @param autoRenewAccount account paying for auto-renewal; if absent, the token itself pays
/// @param deleted true if the token has been deleted (other fields reflect its last state)
/// @param adminAuthority controls update / delete; absent → immutable token
/// @param supplyAuthority controls mint / burn; absent → fixed supply
/// @param kycAuthority controls KYC grant / revoke; absent → KYC disabled (cannot be added later)
/// @param freezeAuthority controls freeze / unfreeze; absent → no freeze (cannot be added later)
/// @param wipeAuthority controls wipe; absent → wipe disabled (cannot be added later)
/// @param pauseAuthority controls pause / unpause; absent → unpausable (cannot be added later)
/// @param feeScheduleAuthority controls custom-fee schedule updates; absent → fees immutable
/// @param metadataAuthority controls per-serial NFT metadata updates (HIP-657); absent → metadata immutable
/// @param defaultFreezeStatus true → newly associated accounts start frozen; absent if the token has no freezeAuthority
/// @param defaultKycStatus true → newly associated accounts start KYC-granted; absent if the token has no kycAuthority
/// @param paused true → token is currently paused; absent if the token has no pauseAuthority
public record TokenInfo(
        Address tokenId,
        TokenType type,
        String name,
        String symbol,
        int decimals,
        long totalSupply,
        AccountId treasuryAccountId,
        TokenSupplyType supplyType,
        @Nullable Long maxSupply,
        @Nullable String tokenMemo,
        byte[] metadata,
        ZonedDateTime expirationTime,
        @Nullable Duration autoRenewPeriod,
        @Nullable AccountId autoRenewAccount,
        boolean deleted,
        @Nullable Authority adminAuthority,
        @Nullable Authority supplyAuthority,
        @Nullable Authority kycAuthority,
        @Nullable Authority freezeAuthority,
        @Nullable Authority wipeAuthority,
        @Nullable Authority pauseAuthority,
        @Nullable Authority feeScheduleAuthority,
        @Nullable Authority metadataAuthority,
        @Nullable Boolean defaultFreezeStatus,
        @Nullable Boolean defaultKycStatus,
        @Nullable Boolean paused) {

    /// Creates a new `TokenInfo`.
    ///
    /// @throws NullPointerException if a required value is `null`
    /// @throws IllegalArgumentException if a value violates its constraints
    public TokenInfo {
        Objects.requireNonNull(tokenId, "tokenId must not be null");
        Objects.requireNonNull(type, "type must not be null");
        Objects.requireNonNull(name, "name must not be null");
        if (name.getBytes(StandardCharsets.UTF_8).length > 100) {
            throw new IllegalArgumentException("name must be at most 100 bytes long in UTF-8");
        }
        Objects.requireNonNull(symbol, "symbol must not be null");
        if (symbol.getBytes(StandardCharsets.UTF_8).length > 100) {
            throw new IllegalArgumentException("symbol must be at most 100 bytes long in UTF-8");
        }
        Objects.requireNonNull(treasuryAccountId, "treasuryAccountId must not be null");
        Objects.requireNonNull(supplyType, "supplyType must not be null");
        Objects.requireNonNull(metadata, "metadata must not be null");
        metadata = metadata.clone();
        Objects.requireNonNull(expirationTime, "expirationTime must not be null");
    }

    /// Creates a new `TokenInfo` with the default value of `decimals`, `metadata`, `deleted`.
    public TokenInfo(
            final Address tokenId,
            final TokenType type,
            final String name,
            final String symbol,
            final long totalSupply,
            final AccountId treasuryAccountId,
            final TokenSupplyType supplyType,
            final @Nullable Long maxSupply,
            final @Nullable String tokenMemo,
            final ZonedDateTime expirationTime,
            final @Nullable Duration autoRenewPeriod,
            final @Nullable AccountId autoRenewAccount,
            final @Nullable Authority adminAuthority,
            final @Nullable Authority supplyAuthority,
            final @Nullable Authority kycAuthority,
            final @Nullable Authority freezeAuthority,
            final @Nullable Authority wipeAuthority,
            final @Nullable Authority pauseAuthority,
            final @Nullable Authority feeScheduleAuthority,
            final @Nullable Authority metadataAuthority,
            final @Nullable Boolean defaultFreezeStatus,
            final @Nullable Boolean defaultKycStatus,
            final @Nullable Boolean paused) {
        this(tokenId, type, name, symbol, 0, totalSupply, treasuryAccountId, supplyType, maxSupply, tokenMemo, new byte[0], expirationTime, autoRenewPeriod, autoRenewAccount, false, adminAuthority, supplyAuthority, kycAuthority, freezeAuthority, wipeAuthority, pauseAuthority, feeScheduleAuthority, metadataAuthority, defaultFreezeStatus, defaultKycStatus, paused);
    }

    /// opaque token-level metadata (e.g. IPFS CID, HTTPS URL, JSON manifest)
    ///
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
            && decimals == other.decimals
            && totalSupply == other.totalSupply
            && Objects.equals(treasuryAccountId, other.treasuryAccountId)
            && Objects.equals(supplyType, other.supplyType)
            && Objects.equals(maxSupply, other.maxSupply)
            && Objects.equals(tokenMemo, other.tokenMemo)
            && Arrays.equals(metadata, other.metadata)
            && Objects.equals(expirationTime, other.expirationTime)
            && Objects.equals(autoRenewPeriod, other.autoRenewPeriod)
            && Objects.equals(autoRenewAccount, other.autoRenewAccount)
            && deleted == other.deleted
            && Objects.equals(adminAuthority, other.adminAuthority)
            && Objects.equals(supplyAuthority, other.supplyAuthority)
            && Objects.equals(kycAuthority, other.kycAuthority)
            && Objects.equals(freezeAuthority, other.freezeAuthority)
            && Objects.equals(wipeAuthority, other.wipeAuthority)
            && Objects.equals(pauseAuthority, other.pauseAuthority)
            && Objects.equals(feeScheduleAuthority, other.feeScheduleAuthority)
            && Objects.equals(metadataAuthority, other.metadataAuthority)
            && Objects.equals(defaultFreezeStatus, other.defaultFreezeStatus)
            && Objects.equals(defaultKycStatus, other.defaultKycStatus)
            && Objects.equals(paused, other.paused);
    }

    @Override
    public int hashCode() {
        return Objects.hash(tokenId, type, name, symbol, decimals, totalSupply, treasuryAccountId, supplyType, maxSupply, tokenMemo, Arrays.hashCode(metadata), expirationTime, autoRenewPeriod, autoRenewAccount, deleted, adminAuthority, supplyAuthority, kycAuthority, freezeAuthority, wipeAuthority, pauseAuthority, feeScheduleAuthority, metadataAuthority, defaultFreezeStatus, defaultKycStatus, paused);
    }

    @Override
    public String toString() {
        return "TokenInfo[tokenId=" + tokenId + ", type=" + type + ", name=" + name + ", symbol=" + symbol + ", decimals=" + decimals + ", totalSupply=" + totalSupply + ", treasuryAccountId=" + treasuryAccountId + ", supplyType=" + supplyType + ", maxSupply=" + maxSupply + ", tokenMemo=" + tokenMemo + ", metadata=" + "byte[" + metadata.length + "]" + ", expirationTime=" + expirationTime + ", autoRenewPeriod=" + autoRenewPeriod + ", autoRenewAccount=" + autoRenewAccount + ", deleted=" + deleted + ", adminAuthority=" + adminAuthority + ", supplyAuthority=" + supplyAuthority + ", kycAuthority=" + kycAuthority + ", freezeAuthority=" + freezeAuthority + ", wipeAuthority=" + wipeAuthority + ", pauseAuthority=" + pauseAuthority + ", feeScheduleAuthority=" + feeScheduleAuthority + ", metadataAuthority=" + metadataAuthority + ", defaultFreezeStatus=" + defaultFreezeStatus + ", defaultKycStatus=" + defaultKycStatus + ", paused=" + paused + "]";
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
        private int decimals = 0;
        private @Nullable Long totalSupply;
        private @Nullable AccountId treasuryAccountId;
        private @Nullable TokenSupplyType supplyType;
        private @Nullable Long maxSupply;
        private @Nullable String tokenMemo;
        private byte[] metadata = new byte[0];
        private @Nullable ZonedDateTime expirationTime;
        private @Nullable Duration autoRenewPeriod;
        private @Nullable AccountId autoRenewAccount;
        private boolean deleted = false;
        private @Nullable Authority adminAuthority;
        private @Nullable Authority supplyAuthority;
        private @Nullable Authority kycAuthority;
        private @Nullable Authority freezeAuthority;
        private @Nullable Authority wipeAuthority;
        private @Nullable Authority pauseAuthority;
        private @Nullable Authority feeScheduleAuthority;
        private @Nullable Authority metadataAuthority;
        private @Nullable Boolean defaultFreezeStatus;
        private @Nullable Boolean defaultKycStatus;
        private @Nullable Boolean paused;

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
        /// FUNGIBLE_COMMON | NON_FUNGIBLE_UNIQUE
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

        /// Sets the `decimals`.
        ///
        /// smallest-unit precision; 0 for NON_FUNGIBLE_UNIQUE
        ///
        /// @param decimals the value
        /// @return this builder
        public Builder decimals(final int decimals) {
            this.decimals = decimals;
            return this;
        }

        /// Sets the `totalSupply`.
        ///
        /// current total supply, in the smallest indivisible unit
        ///
        /// @param totalSupply the value
        /// @return this builder
        public Builder totalSupply(final long totalSupply) {
            this.totalSupply = totalSupply;
            return this;
        }

        /// Sets the `treasuryAccountId`.
        ///
        /// account holding the supply pool
        ///
        /// @param treasuryAccountId the value
        /// @return this builder
        public Builder treasuryAccountId(final AccountId treasuryAccountId) {
            this.treasuryAccountId = treasuryAccountId;
            return this;
        }

        /// Sets the `supplyType`.
        ///
        /// INFINITE | FINITE
        ///
        /// @param supplyType the value
        /// @return this builder
        public Builder supplyType(final TokenSupplyType supplyType) {
            this.supplyType = supplyType;
            return this;
        }

        /// Sets the `maxSupply`.
        ///
        /// maximum supply enforced by the network; absent when supplyType is INFINITE
        ///
        /// @param maxSupply the value
        /// @return this builder
        public Builder maxSupply(final @Nullable Long maxSupply) {
            this.maxSupply = maxSupply;
            return this;
        }

        /// Sets the `tokenMemo`.
        ///
        /// short human-readable label
        ///
        /// @param tokenMemo the value
        /// @return this builder
        public Builder tokenMemo(final @Nullable String tokenMemo) {
            this.tokenMemo = tokenMemo;
            return this;
        }

        /// Sets the `metadata`.
        ///
        /// opaque token-level metadata (e.g. IPFS CID, HTTPS URL, JSON manifest)
        ///
        /// @param metadata the value
        /// @return this builder
        public Builder metadata(final byte[] metadata) {
            this.metadata = metadata;
            return this;
        }

        /// Sets the `expirationTime`.
        ///
        /// @param expirationTime the value
        /// @return this builder
        public Builder expirationTime(final ZonedDateTime expirationTime) {
            this.expirationTime = expirationTime;
            return this;
        }

        /// Sets the `autoRenewPeriod`.
        ///
        /// @param autoRenewPeriod the value
        /// @return this builder
        public Builder autoRenewPeriod(final @Nullable Duration autoRenewPeriod) {
            this.autoRenewPeriod = autoRenewPeriod;
            return this;
        }

        /// Sets the `autoRenewAccount`.
        ///
        /// account paying for auto-renewal; if absent, the token itself pays
        ///
        /// @param autoRenewAccount the value
        /// @return this builder
        public Builder autoRenewAccount(final @Nullable AccountId autoRenewAccount) {
            this.autoRenewAccount = autoRenewAccount;
            return this;
        }

        /// Sets the `deleted`.
        ///
        /// true if the token has been deleted (other fields reflect its last state)
        ///
        /// @param deleted the value
        /// @return this builder
        public Builder deleted(final boolean deleted) {
            this.deleted = deleted;
            return this;
        }

        /// Sets the `adminAuthority`.
        ///
        /// controls update / delete; absent → immutable token
        ///
        /// @param adminAuthority the value
        /// @return this builder
        public Builder adminAuthority(final @Nullable Authority adminAuthority) {
            this.adminAuthority = adminAuthority;
            return this;
        }

        /// Sets the `supplyAuthority`.
        ///
        /// controls mint / burn; absent → fixed supply
        ///
        /// @param supplyAuthority the value
        /// @return this builder
        public Builder supplyAuthority(final @Nullable Authority supplyAuthority) {
            this.supplyAuthority = supplyAuthority;
            return this;
        }

        /// Sets the `kycAuthority`.
        ///
        /// controls KYC grant / revoke; absent → KYC disabled (cannot be added later)
        ///
        /// @param kycAuthority the value
        /// @return this builder
        public Builder kycAuthority(final @Nullable Authority kycAuthority) {
            this.kycAuthority = kycAuthority;
            return this;
        }

        /// Sets the `freezeAuthority`.
        ///
        /// controls freeze / unfreeze; absent → no freeze (cannot be added later)
        ///
        /// @param freezeAuthority the value
        /// @return this builder
        public Builder freezeAuthority(final @Nullable Authority freezeAuthority) {
            this.freezeAuthority = freezeAuthority;
            return this;
        }

        /// Sets the `wipeAuthority`.
        ///
        /// controls wipe; absent → wipe disabled (cannot be added later)
        ///
        /// @param wipeAuthority the value
        /// @return this builder
        public Builder wipeAuthority(final @Nullable Authority wipeAuthority) {
            this.wipeAuthority = wipeAuthority;
            return this;
        }

        /// Sets the `pauseAuthority`.
        ///
        /// controls pause / unpause; absent → unpausable (cannot be added later)
        ///
        /// @param pauseAuthority the value
        /// @return this builder
        public Builder pauseAuthority(final @Nullable Authority pauseAuthority) {
            this.pauseAuthority = pauseAuthority;
            return this;
        }

        /// Sets the `feeScheduleAuthority`.
        ///
        /// controls custom-fee schedule updates; absent → fees immutable
        ///
        /// @param feeScheduleAuthority the value
        /// @return this builder
        public Builder feeScheduleAuthority(final @Nullable Authority feeScheduleAuthority) {
            this.feeScheduleAuthority = feeScheduleAuthority;
            return this;
        }

        /// Sets the `metadataAuthority`.
        ///
        /// controls per-serial NFT metadata updates (HIP-657); absent → metadata immutable
        ///
        /// @param metadataAuthority the value
        /// @return this builder
        public Builder metadataAuthority(final @Nullable Authority metadataAuthority) {
            this.metadataAuthority = metadataAuthority;
            return this;
        }

        /// Sets the `defaultFreezeStatus`.
        ///
        /// true → newly associated accounts start frozen; absent if the token has no freezeAuthority
        ///
        /// @param defaultFreezeStatus the value
        /// @return this builder
        public Builder defaultFreezeStatus(final @Nullable Boolean defaultFreezeStatus) {
            this.defaultFreezeStatus = defaultFreezeStatus;
            return this;
        }

        /// Sets the `defaultKycStatus`.
        ///
        /// true → newly associated accounts start KYC-granted; absent if the token has no kycAuthority
        ///
        /// @param defaultKycStatus the value
        /// @return this builder
        public Builder defaultKycStatus(final @Nullable Boolean defaultKycStatus) {
            this.defaultKycStatus = defaultKycStatus;
            return this;
        }

        /// Sets the `paused`.
        ///
        /// true → token is currently paused; absent if the token has no pauseAuthority
        ///
        /// @param paused the value
        /// @return this builder
        public Builder paused(final @Nullable Boolean paused) {
            this.paused = paused;
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
                    decimals,
                    Objects.requireNonNull(totalSupply, "totalSupply must not be null"),
                    Objects.requireNonNull(treasuryAccountId, "treasuryAccountId must not be null"),
                    Objects.requireNonNull(supplyType, "supplyType must not be null"),
                    maxSupply,
                    tokenMemo,
                    metadata,
                    Objects.requireNonNull(expirationTime, "expirationTime must not be null"),
                    autoRenewPeriod,
                    autoRenewAccount,
                    deleted,
                    adminAuthority,
                    supplyAuthority,
                    kycAuthority,
                    freezeAuthority,
                    wipeAuthority,
                    pauseAuthority,
                    feeScheduleAuthority,
                    metadataAuthority,
                    defaultFreezeStatus,
                    defaultKycStatus,
                    paused);
        }
    }
}
