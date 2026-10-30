package org.hiero.mirrornode.token;

import java.util.Arrays;
import java.util.Objects;
import org.hiero.authority.Authority;
import org.hiero.ledger.Address;
import org.hiero.token.TokenType;
import org.jspecify.annotations.Nullable;

/// A token as listed by the token list.
public record Token(
        @Nullable Address tokenId,
        String name,
        String symbol,
        TokenType type,
        long decimals,
        byte @Nullable [] metadata,
        @Nullable Authority adminKey) {

    /// Creates a new `Token`.
    ///
    /// @throws NullPointerException if a required value is `null`
    public Token {
        Objects.requireNonNull(name, "name must not be null");
        Objects.requireNonNull(symbol, "symbol must not be null");
        Objects.requireNonNull(type, "type must not be null");
        metadata = metadata == null ? null : metadata.clone();
    }

    /// Returns a copy of the `metadata` array.
    @Override
    public byte @Nullable [] metadata() {
        return metadata == null ? null : metadata.clone();
    }

    @Override
    public boolean equals(final @Nullable Object obj) {
        return obj instanceof Token other
            && Objects.equals(tokenId, other.tokenId)
            && Objects.equals(name, other.name)
            && Objects.equals(symbol, other.symbol)
            && Objects.equals(type, other.type)
            && decimals == other.decimals
            && Arrays.equals(metadata, other.metadata)
            && Objects.equals(adminKey, other.adminKey);
    }

    @Override
    public int hashCode() {
        return Objects.hash(tokenId, name, symbol, type, decimals, Arrays.hashCode(metadata), adminKey);
    }

    @Override
    public String toString() {
        return "Token[tokenId=" + tokenId + ", name=" + name + ", symbol=" + symbol + ", type=" + type + ", decimals=" + decimals + ", metadata=" + (metadata == null ? "null" : "byte[" + metadata.length + "]") + ", adminKey=" + adminKey + "]";
    }

    /// Returns a builder for a new `Token`.
    ///
    /// @return the builder
    public static Builder builder() {
        return new Builder();
    }

    /// The builder of `Token`.
    ///
    /// `build()` creates the `Token` of the values set one by one.
    ///
    /// A value that is not set is `null`, or has its default value.
    public static final class Builder {

        private @Nullable Address tokenId;
        private @Nullable String name;
        private @Nullable String symbol;
        private @Nullable TokenType type;
        private @Nullable Long decimals;
        private byte @Nullable [] metadata;
        private @Nullable Authority adminKey;

        private Builder() {
            // created by Token.builder()
        }

        /// Sets the `tokenId`.
        ///
        /// @param tokenId the value
        /// @return this builder
        public Builder tokenId(final @Nullable Address tokenId) {
            this.tokenId = tokenId;
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

        /// Sets the `type`.
        ///
        /// @param type the value
        /// @return this builder
        public Builder type(final TokenType type) {
            this.type = type;
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
        public Builder metadata(final byte @Nullable [] metadata) {
            this.metadata = metadata;
            return this;
        }

        /// Sets the `adminKey`.
        ///
        /// @param adminKey the value
        /// @return this builder
        public Builder adminKey(final @Nullable Authority adminKey) {
            this.adminKey = adminKey;
            return this;
        }

        /// Creates the `Token` of the values set.
        ///
        /// @return the new `Token`
        /// @throws NullPointerException if a required value is not set
        public Token build() {
            return new Token(
                    tokenId,
                    Objects.requireNonNull(name, "name must not be null"),
                    Objects.requireNonNull(symbol, "symbol must not be null"),
                    Objects.requireNonNull(type, "type must not be null"),
                    Objects.requireNonNull(decimals, "decimals must not be null"),
                    metadata,
                    adminKey);
        }
    }
}
