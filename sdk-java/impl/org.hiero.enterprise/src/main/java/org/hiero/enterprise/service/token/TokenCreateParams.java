package org.hiero.enterprise.service.token;

import java.time.Duration;
import java.time.ZonedDateTime;
import java.util.Arrays;
import java.util.Objects;
import org.hiero.authority.Authority;
import org.hiero.ledger.AccountId;
import org.hiero.token.TokenSupplyType;
import org.jspecify.annotations.Nullable;

/// The parameters of a new fungible token: everything that can be set when it is created. Name and symbol are
/// required.
///
/// @param treasuryAccountId The account that receives the initial supply and every mint; the operator account if absent.
/// @param decimals The number of decimal places of the smallest unit (e.g. 6 for a currency like USDC).
/// @param initialSupply The amount minted to the treasury when the token is created, in the smallest unit.
/// @param supplyType INFINITE or FINITE; cannot be changed later.
/// @param maxSupply The highest total supply; only for a FINITE supply type.
/// @param freezeDefault When true, newly associated accounts start frozen; requires a freeze authority.
/// @param metadata Opaque metadata of the token (e.g. an IPFS CID, a URL or a JSON manifest).
/// @param expirationTime When the token expires; the network default if absent.
/// @param autoRenewPeriod The renewal period applied when the expiration approaches; the network default if absent.
/// @param autoRenewAccount The account that pays for the renewal.
/// @param adminAuthority Allows to update and delete the token; without it the token cannot be changed.
/// @param supplyAuthority Allows to mint and burn; without it the supply is fixed after creation.
/// @param kycAuthority Allows to grant and revoke KYC; without it KYC is disabled.
/// @param freezeAuthority Allows to freeze and unfreeze accounts; without it accounts cannot be frozen.
/// @param wipeAuthority Allows to wipe the balances of accounts other than the treasury; without it wiping is disabled.
/// @param pauseAuthority Allows to pause and unpause the token; without it the token cannot be paused.
/// @param feeScheduleAuthority Allows to update the custom fee schedule; without it the fees cannot be changed.
/// @param metadataAuthority Allows to update the metadata of single tokens (HIP-657); without it that metadata cannot be changed.
public record TokenCreateParams(
        String name,
        String symbol,
        @Nullable AccountId treasuryAccountId,
        int decimals,
        long initialSupply,
        TokenSupplyType supplyType,
        @Nullable Long maxSupply,
        boolean freezeDefault,
        @Nullable String tokenMemo,
        byte[] metadata,
        @Nullable ZonedDateTime expirationTime,
        @Nullable Duration autoRenewPeriod,
        @Nullable AccountId autoRenewAccount,
        @Nullable Authority adminAuthority,
        @Nullable Authority supplyAuthority,
        @Nullable Authority kycAuthority,
        @Nullable Authority freezeAuthority,
        @Nullable Authority wipeAuthority,
        @Nullable Authority pauseAuthority,
        @Nullable Authority feeScheduleAuthority,
        @Nullable Authority metadataAuthority) {

    /// Creates a new `TokenCreateParams`.
    ///
    /// @throws NullPointerException if a required value is `null`
    /// @throws IllegalArgumentException if a value violates its constraints
    public TokenCreateParams {
        Objects.requireNonNull(name, "name must not be null");
        if (name.length() > 100) {
            throw new IllegalArgumentException("name must be at most 100 characters long");
        }
        Objects.requireNonNull(symbol, "symbol must not be null");
        if (symbol.length() > 100) {
            throw new IllegalArgumentException("symbol must be at most 100 characters long");
        }
        Objects.requireNonNull(supplyType, "supplyType must not be null");
        if (maxSupply != null && maxSupply < 1L) {
            throw new IllegalArgumentException("maxSupply must be at least 1");
        }
        if (tokenMemo != null && tokenMemo.length() > 100) {
            throw new IllegalArgumentException("tokenMemo must be at most 100 characters long");
        }
        Objects.requireNonNull(metadata, "metadata must not be null");
        metadata = metadata.clone();
        if (metadata.length > 100) {
            throw new IllegalArgumentException("metadata must contain at most 100 element(s)");
        }
    }

    /// Creates a new `TokenCreateParams` with the default value of `decimals`, `initialSupply`, `supplyType`, `freezeDefault`, `metadata`.
    public TokenCreateParams(
            final String name,
            final String symbol,
            final @Nullable AccountId treasuryAccountId,
            final @Nullable Long maxSupply,
            final @Nullable String tokenMemo,
            final @Nullable ZonedDateTime expirationTime,
            final @Nullable Duration autoRenewPeriod,
            final @Nullable AccountId autoRenewAccount,
            final @Nullable Authority adminAuthority,
            final @Nullable Authority supplyAuthority,
            final @Nullable Authority kycAuthority,
            final @Nullable Authority freezeAuthority,
            final @Nullable Authority wipeAuthority,
            final @Nullable Authority pauseAuthority,
            final @Nullable Authority feeScheduleAuthority,
            final @Nullable Authority metadataAuthority) {
        this(name, symbol, treasuryAccountId, 0, 0L, TokenSupplyType.INFINITE, maxSupply, false, tokenMemo, new byte[0], expirationTime, autoRenewPeriod, autoRenewAccount, adminAuthority, supplyAuthority, kycAuthority, freezeAuthority, wipeAuthority, pauseAuthority, feeScheduleAuthority, metadataAuthority);
    }

    /// Opaque metadata of the token (e.g. an IPFS CID, a URL or a JSON manifest).
    ///
    /// Returns a copy of the `metadata` array.
    @Override
    public byte[] metadata() {
        return metadata.clone();
    }

    @Override
    public boolean equals(final @Nullable Object obj) {
        return obj instanceof TokenCreateParams other
            && Objects.equals(name, other.name)
            && Objects.equals(symbol, other.symbol)
            && Objects.equals(treasuryAccountId, other.treasuryAccountId)
            && decimals == other.decimals
            && initialSupply == other.initialSupply
            && Objects.equals(supplyType, other.supplyType)
            && Objects.equals(maxSupply, other.maxSupply)
            && freezeDefault == other.freezeDefault
            && Objects.equals(tokenMemo, other.tokenMemo)
            && Arrays.equals(metadata, other.metadata)
            && Objects.equals(expirationTime, other.expirationTime)
            && Objects.equals(autoRenewPeriod, other.autoRenewPeriod)
            && Objects.equals(autoRenewAccount, other.autoRenewAccount)
            && Objects.equals(adminAuthority, other.adminAuthority)
            && Objects.equals(supplyAuthority, other.supplyAuthority)
            && Objects.equals(kycAuthority, other.kycAuthority)
            && Objects.equals(freezeAuthority, other.freezeAuthority)
            && Objects.equals(wipeAuthority, other.wipeAuthority)
            && Objects.equals(pauseAuthority, other.pauseAuthority)
            && Objects.equals(feeScheduleAuthority, other.feeScheduleAuthority)
            && Objects.equals(metadataAuthority, other.metadataAuthority);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, symbol, treasuryAccountId, decimals, initialSupply, supplyType, maxSupply, freezeDefault, tokenMemo, Arrays.hashCode(metadata), expirationTime, autoRenewPeriod, autoRenewAccount, adminAuthority, supplyAuthority, kycAuthority, freezeAuthority, wipeAuthority, pauseAuthority, feeScheduleAuthority, metadataAuthority);
    }

    @Override
    public String toString() {
        return "TokenCreateParams[name=" + name + ", symbol=" + symbol + ", treasuryAccountId=" + treasuryAccountId + ", decimals=" + decimals + ", initialSupply=" + initialSupply + ", supplyType=" + supplyType + ", maxSupply=" + maxSupply + ", freezeDefault=" + freezeDefault + ", tokenMemo=" + tokenMemo + ", metadata=" + "byte[" + metadata.length + "]" + ", expirationTime=" + expirationTime + ", autoRenewPeriod=" + autoRenewPeriod + ", autoRenewAccount=" + autoRenewAccount + ", adminAuthority=" + adminAuthority + ", supplyAuthority=" + supplyAuthority + ", kycAuthority=" + kycAuthority + ", freezeAuthority=" + freezeAuthority + ", wipeAuthority=" + wipeAuthority + ", pauseAuthority=" + pauseAuthority + ", feeScheduleAuthority=" + feeScheduleAuthority + ", metadataAuthority=" + metadataAuthority + "]";
    }

    /// Returns a builder for a new `TokenCreateParams`.
    ///
    /// @return the builder
    public static Builder builder() {
        return new Builder();
    }

    /// The builder of `TokenCreateParams`.
    ///
    /// `build()` creates the `TokenCreateParams` of the values set one by one.
    ///
    /// A value that is not set is `null`, or has its default value.
    public static final class Builder {

        private @Nullable String name;
        private @Nullable String symbol;
        private @Nullable AccountId treasuryAccountId;
        private int decimals = 0;
        private long initialSupply = 0L;
        private TokenSupplyType supplyType = TokenSupplyType.INFINITE;
        private @Nullable Long maxSupply;
        private boolean freezeDefault = false;
        private @Nullable String tokenMemo;
        private byte[] metadata = new byte[0];
        private @Nullable ZonedDateTime expirationTime;
        private @Nullable Duration autoRenewPeriod;
        private @Nullable AccountId autoRenewAccount;
        private @Nullable Authority adminAuthority;
        private @Nullable Authority supplyAuthority;
        private @Nullable Authority kycAuthority;
        private @Nullable Authority freezeAuthority;
        private @Nullable Authority wipeAuthority;
        private @Nullable Authority pauseAuthority;
        private @Nullable Authority feeScheduleAuthority;
        private @Nullable Authority metadataAuthority;

        private Builder() {
            // created by TokenCreateParams.builder()
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

        /// Sets the `treasuryAccountId`.
        ///
        /// The account that receives the initial supply and every mint; the operator account if absent.
        ///
        /// @param treasuryAccountId the value
        /// @return this builder
        public Builder treasuryAccountId(final @Nullable AccountId treasuryAccountId) {
            this.treasuryAccountId = treasuryAccountId;
            return this;
        }

        /// Sets the `decimals`.
        ///
        /// The number of decimal places of the smallest unit (e.g. 6 for a currency like USDC).
        ///
        /// @param decimals the value
        /// @return this builder
        public Builder decimals(final int decimals) {
            this.decimals = decimals;
            return this;
        }

        /// Sets the `initialSupply`.
        ///
        /// The amount minted to the treasury when the token is created, in the smallest unit.
        ///
        /// @param initialSupply the value
        /// @return this builder
        public Builder initialSupply(final long initialSupply) {
            this.initialSupply = initialSupply;
            return this;
        }

        /// Sets the `supplyType`.
        ///
        /// INFINITE or FINITE; cannot be changed later.
        ///
        /// @param supplyType the value
        /// @return this builder
        public Builder supplyType(final TokenSupplyType supplyType) {
            this.supplyType = supplyType;
            return this;
        }

        /// Sets the `maxSupply`.
        ///
        /// The highest total supply; only for a FINITE supply type.
        ///
        /// @param maxSupply the value
        /// @return this builder
        public Builder maxSupply(final @Nullable Long maxSupply) {
            this.maxSupply = maxSupply;
            return this;
        }

        /// Sets the `freezeDefault`.
        ///
        /// When true, newly associated accounts start frozen; requires a freeze authority.
        ///
        /// @param freezeDefault the value
        /// @return this builder
        public Builder freezeDefault(final boolean freezeDefault) {
            this.freezeDefault = freezeDefault;
            return this;
        }

        /// Sets the `tokenMemo`.
        ///
        /// @param tokenMemo the value
        /// @return this builder
        public Builder tokenMemo(final @Nullable String tokenMemo) {
            this.tokenMemo = tokenMemo;
            return this;
        }

        /// Sets the `metadata`.
        ///
        /// Opaque metadata of the token (e.g. an IPFS CID, a URL or a JSON manifest).
        ///
        /// @param metadata the value
        /// @return this builder
        public Builder metadata(final byte[] metadata) {
            this.metadata = metadata;
            return this;
        }

        /// Sets the `expirationTime`.
        ///
        /// When the token expires; the network default if absent.
        ///
        /// @param expirationTime the value
        /// @return this builder
        public Builder expirationTime(final @Nullable ZonedDateTime expirationTime) {
            this.expirationTime = expirationTime;
            return this;
        }

        /// Sets the `autoRenewPeriod`.
        ///
        /// The renewal period applied when the expiration approaches; the network default if absent.
        ///
        /// @param autoRenewPeriod the value
        /// @return this builder
        public Builder autoRenewPeriod(final @Nullable Duration autoRenewPeriod) {
            this.autoRenewPeriod = autoRenewPeriod;
            return this;
        }

        /// Sets the `autoRenewAccount`.
        ///
        /// The account that pays for the renewal.
        ///
        /// @param autoRenewAccount the value
        /// @return this builder
        public Builder autoRenewAccount(final @Nullable AccountId autoRenewAccount) {
            this.autoRenewAccount = autoRenewAccount;
            return this;
        }

        /// Sets the `adminAuthority`.
        ///
        /// Allows to update and delete the token; without it the token cannot be changed.
        ///
        /// @param adminAuthority the value
        /// @return this builder
        public Builder adminAuthority(final @Nullable Authority adminAuthority) {
            this.adminAuthority = adminAuthority;
            return this;
        }

        /// Sets the `supplyAuthority`.
        ///
        /// Allows to mint and burn; without it the supply is fixed after creation.
        ///
        /// @param supplyAuthority the value
        /// @return this builder
        public Builder supplyAuthority(final @Nullable Authority supplyAuthority) {
            this.supplyAuthority = supplyAuthority;
            return this;
        }

        /// Sets the `kycAuthority`.
        ///
        /// Allows to grant and revoke KYC; without it KYC is disabled.
        ///
        /// @param kycAuthority the value
        /// @return this builder
        public Builder kycAuthority(final @Nullable Authority kycAuthority) {
            this.kycAuthority = kycAuthority;
            return this;
        }

        /// Sets the `freezeAuthority`.
        ///
        /// Allows to freeze and unfreeze accounts; without it accounts cannot be frozen.
        ///
        /// @param freezeAuthority the value
        /// @return this builder
        public Builder freezeAuthority(final @Nullable Authority freezeAuthority) {
            this.freezeAuthority = freezeAuthority;
            return this;
        }

        /// Sets the `wipeAuthority`.
        ///
        /// Allows to wipe the balances of accounts other than the treasury; without it wiping is disabled.
        ///
        /// @param wipeAuthority the value
        /// @return this builder
        public Builder wipeAuthority(final @Nullable Authority wipeAuthority) {
            this.wipeAuthority = wipeAuthority;
            return this;
        }

        /// Sets the `pauseAuthority`.
        ///
        /// Allows to pause and unpause the token; without it the token cannot be paused.
        ///
        /// @param pauseAuthority the value
        /// @return this builder
        public Builder pauseAuthority(final @Nullable Authority pauseAuthority) {
            this.pauseAuthority = pauseAuthority;
            return this;
        }

        /// Sets the `feeScheduleAuthority`.
        ///
        /// Allows to update the custom fee schedule; without it the fees cannot be changed.
        ///
        /// @param feeScheduleAuthority the value
        /// @return this builder
        public Builder feeScheduleAuthority(final @Nullable Authority feeScheduleAuthority) {
            this.feeScheduleAuthority = feeScheduleAuthority;
            return this;
        }

        /// Sets the `metadataAuthority`.
        ///
        /// Allows to update the metadata of single tokens (HIP-657); without it that metadata cannot be changed.
        ///
        /// @param metadataAuthority the value
        /// @return this builder
        public Builder metadataAuthority(final @Nullable Authority metadataAuthority) {
            this.metadataAuthority = metadataAuthority;
            return this;
        }

        /// Creates the `TokenCreateParams` of the values set.
        ///
        /// @return the new `TokenCreateParams`
        /// @throws NullPointerException if a required value is not set
        /// @throws IllegalArgumentException if a value violates its constraints
        public TokenCreateParams build() {
            return new TokenCreateParams(
                    Objects.requireNonNull(name, "name must not be null"),
                    Objects.requireNonNull(symbol, "symbol must not be null"),
                    treasuryAccountId,
                    decimals,
                    initialSupply,
                    supplyType,
                    maxSupply,
                    freezeDefault,
                    tokenMemo,
                    metadata,
                    expirationTime,
                    autoRenewPeriod,
                    autoRenewAccount,
                    adminAuthority,
                    supplyAuthority,
                    kycAuthority,
                    freezeAuthority,
                    wipeAuthority,
                    pauseAuthority,
                    feeScheduleAuthority,
                    metadataAuthority);
        }
    }
}
