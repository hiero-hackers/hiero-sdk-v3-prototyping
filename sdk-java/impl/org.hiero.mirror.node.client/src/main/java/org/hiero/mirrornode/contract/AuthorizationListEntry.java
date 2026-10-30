package org.hiero.mirrornode.contract;

import java.util.Objects;
import org.hiero.ledger.EvmAddress;
import org.jspecify.annotations.Nullable;

/// An authorization of a wrapped EIP-7702 Ethereum transaction: the delegation of an account to the code of an address.
///
/// @param address the address that is delegated to
/// @param chainId the chain id in hex
/// @param r the signature value r in hex
/// @param s the signature value s in hex
/// @param yParity the parity of the y-coordinate of the signature point in hex
public record AuthorizationListEntry(
        EvmAddress address,
        String chainId,
        long nonce,
        String r,
        String s,
        String yParity) {

    /// Creates a new `AuthorizationListEntry`.
    ///
    /// @throws NullPointerException if a required value is `null`
    public AuthorizationListEntry {
        Objects.requireNonNull(address, "address must not be null");
        Objects.requireNonNull(chainId, "chainId must not be null");
        Objects.requireNonNull(r, "r must not be null");
        Objects.requireNonNull(s, "s must not be null");
        Objects.requireNonNull(yParity, "yParity must not be null");
    }

    /// Returns a builder for a new `AuthorizationListEntry`.
    ///
    /// @return the builder
    public static Builder builder() {
        return new Builder();
    }

    /// The builder of `AuthorizationListEntry`.
    ///
    /// `build()` creates the `AuthorizationListEntry` of the values set one by one.
    ///
    /// A value that is not set is `null`, or has its default value.
    public static final class Builder {

        private @Nullable EvmAddress address;
        private @Nullable String chainId;
        private @Nullable Long nonce;
        private @Nullable String r;
        private @Nullable String s;
        private @Nullable String yParity;

        private Builder() {
            // created by AuthorizationListEntry.builder()
        }

        /// Sets the `address`.
        ///
        /// the address that is delegated to
        ///
        /// @param address the value
        /// @return this builder
        public Builder address(final EvmAddress address) {
            this.address = address;
            return this;
        }

        /// Sets the `chainId`.
        ///
        /// the chain id in hex
        ///
        /// @param chainId the value
        /// @return this builder
        public Builder chainId(final String chainId) {
            this.chainId = chainId;
            return this;
        }

        /// Sets the `nonce`.
        ///
        /// @param nonce the value
        /// @return this builder
        public Builder nonce(final long nonce) {
            this.nonce = nonce;
            return this;
        }

        /// Sets the `r`.
        ///
        /// the signature value r in hex
        ///
        /// @param r the value
        /// @return this builder
        public Builder r(final String r) {
            this.r = r;
            return this;
        }

        /// Sets the `s`.
        ///
        /// the signature value s in hex
        ///
        /// @param s the value
        /// @return this builder
        public Builder s(final String s) {
            this.s = s;
            return this;
        }

        /// Sets the `yParity`.
        ///
        /// the parity of the y-coordinate of the signature point in hex
        ///
        /// @param yParity the value
        /// @return this builder
        public Builder yParity(final String yParity) {
            this.yParity = yParity;
            return this;
        }

        /// Creates the `AuthorizationListEntry` of the values set.
        ///
        /// @return the new `AuthorizationListEntry`
        /// @throws NullPointerException if a required value is not set
        public AuthorizationListEntry build() {
            return new AuthorizationListEntry(
                    Objects.requireNonNull(address, "address must not be null"),
                    Objects.requireNonNull(chainId, "chainId must not be null"),
                    Objects.requireNonNull(nonce, "nonce must not be null"),
                    Objects.requireNonNull(r, "r must not be null"),
                    Objects.requireNonNull(s, "s must not be null"),
                    Objects.requireNonNull(yParity, "yParity must not be null"));
        }
    }
}
