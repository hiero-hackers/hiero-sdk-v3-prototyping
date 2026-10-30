package org.hiero.mirrornode.network;

import java.util.List;
import java.util.Objects;
import org.jspecify.annotations.Nullable;

/// An endpoint of a registered node. At least one of domainName / ipAddress is set.
///
/// @param blockNodeApis the APIs of a block node; empty for other types
/// @param generalServiceDescription the description of a general service
public record RegisteredServiceEndpoint(
        List<RegisteredBlockNodeApi> blockNodeApis,
        @Nullable String domainName,
        @Nullable String generalServiceDescription,
        @Nullable String ipAddress,
        int port,
        boolean requiresTls,
        RegisteredNodeType type) {

    /// Creates a new `RegisteredServiceEndpoint`.
    ///
    /// @throws NullPointerException if a required value is `null`
    /// @throws IllegalArgumentException if a value violates its constraints
    public RegisteredServiceEndpoint {
        Objects.requireNonNull(blockNodeApis, "blockNodeApis must not be null");
        blockNodeApis = List.copyOf(blockNodeApis);
        if (port < 0) {
            throw new IllegalArgumentException("port must be at least 0");
        }
        if (port > 65535) {
            throw new IllegalArgumentException("port must be at most 65535");
        }
        Objects.requireNonNull(type, "type must not be null");
    }

    /// Creates a new `RegisteredServiceEndpoint` with the default value of `blockNodeApis`.
    public RegisteredServiceEndpoint(
            final @Nullable String domainName,
            final @Nullable String generalServiceDescription,
            final @Nullable String ipAddress,
            final int port,
            final boolean requiresTls,
            final RegisteredNodeType type) {
        this(List.of(), domainName, generalServiceDescription, ipAddress, port, requiresTls, type);
    }

    /// Returns a builder for a new `RegisteredServiceEndpoint`.
    ///
    /// @return the builder
    public static Builder builder() {
        return new Builder();
    }

    /// The builder of `RegisteredServiceEndpoint`.
    ///
    /// `build()` creates the `RegisteredServiceEndpoint` of the values set one by one.
    ///
    /// A value that is not set is `null`, or has its default value.
    public static final class Builder {

        private List<RegisteredBlockNodeApi> blockNodeApis = List.of();
        private @Nullable String domainName;
        private @Nullable String generalServiceDescription;
        private @Nullable String ipAddress;
        private @Nullable Integer port;
        private @Nullable Boolean requiresTls;
        private @Nullable RegisteredNodeType type;

        private Builder() {
            // created by RegisteredServiceEndpoint.builder()
        }

        /// Sets the `blockNodeApis`.
        ///
        /// the APIs of a block node; empty for other types
        ///
        /// @param blockNodeApis the value
        /// @return this builder
        public Builder blockNodeApis(final List<RegisteredBlockNodeApi> blockNodeApis) {
            this.blockNodeApis = blockNodeApis;
            return this;
        }

        /// Sets the `domainName`.
        ///
        /// @param domainName the value
        /// @return this builder
        public Builder domainName(final @Nullable String domainName) {
            this.domainName = domainName;
            return this;
        }

        /// Sets the `generalServiceDescription`.
        ///
        /// the description of a general service
        ///
        /// @param generalServiceDescription the value
        /// @return this builder
        public Builder generalServiceDescription(final @Nullable String generalServiceDescription) {
            this.generalServiceDescription = generalServiceDescription;
            return this;
        }

        /// Sets the `ipAddress`.
        ///
        /// @param ipAddress the value
        /// @return this builder
        public Builder ipAddress(final @Nullable String ipAddress) {
            this.ipAddress = ipAddress;
            return this;
        }

        /// Sets the `port`.
        ///
        /// @param port the value
        /// @return this builder
        public Builder port(final int port) {
            this.port = port;
            return this;
        }

        /// Sets the `requiresTls`.
        ///
        /// @param requiresTls the value
        /// @return this builder
        public Builder requiresTls(final boolean requiresTls) {
            this.requiresTls = requiresTls;
            return this;
        }

        /// Sets the `type`.
        ///
        /// @param type the value
        /// @return this builder
        public Builder type(final RegisteredNodeType type) {
            this.type = type;
            return this;
        }

        /// Creates the `RegisteredServiceEndpoint` of the values set.
        ///
        /// @return the new `RegisteredServiceEndpoint`
        /// @throws NullPointerException if a required value is not set
        /// @throws IllegalArgumentException if a value violates its constraints
        public RegisteredServiceEndpoint build() {
            return new RegisteredServiceEndpoint(
                    blockNodeApis,
                    domainName,
                    generalServiceDescription,
                    ipAddress,
                    Objects.requireNonNull(port, "port must not be null"),
                    Objects.requireNonNull(requiresTls, "requiresTls must not be null"),
                    Objects.requireNonNull(type, "type must not be null"));
        }
    }
}
