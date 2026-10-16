package org.hiero.consensusnode.admin.nodes;

import java.util.Objects;
import org.hiero.ledger.IpAddress;
import org.jspecify.annotations.Nullable;

/// One reachable endpoint on a consensus node. Exactly one of ipAddress or domainName must
/// be set; the network rejects an endpoint with both (or neither) as INVALID_ENDPOINT.
///
/// @param ipAddress IPv4 address; absent when domainName is used
/// @param domainName DNS name; absent when ipAddress is used
/// @param port listening port (0 is invalid)
public record ServiceEndpoint(@Nullable IpAddress ipAddress, @Nullable String domainName, int port) {

    /// Creates a new `ServiceEndpoint`.
    ///
    /// @throws IllegalArgumentException if a value violates its constraints
    public ServiceEndpoint {
        if (port < 0 || port > 65535) {
            throw new IllegalArgumentException("port must be between 0 and 65535");
        }
        if ((ipAddress != null ? 1 : 0) + (domainName != null ? 1 : 0) != 1) {
            throw new IllegalArgumentException("exactly one of ipAddress, domainName must be set");
        }
    }

    /// Returns a builder for a new `ServiceEndpoint`.
    ///
    /// @return the builder
    public static Builder builder() {
        return new Builder();
    }

    /// The builder of `ServiceEndpoint`.
    ///
    /// `build()` creates the `ServiceEndpoint` of the values set one by one.
    ///
    /// A value that is not set is `null`, or has its default value.
    public static final class Builder {

        private @Nullable IpAddress ipAddress;
        private @Nullable String domainName;
        private @Nullable Integer port;

        private Builder() {
            // created by ServiceEndpoint.builder()
        }

        /// Sets the `ipAddress`.
        ///
        /// IPv4 address; absent when domainName is used
        ///
        /// @param ipAddress the value
        /// @return this builder
        public Builder ipAddress(final @Nullable IpAddress ipAddress) {
            this.ipAddress = ipAddress;
            return this;
        }

        /// Sets the `domainName`.
        ///
        /// DNS name; absent when ipAddress is used
        ///
        /// @param domainName the value
        /// @return this builder
        public Builder domainName(final @Nullable String domainName) {
            this.domainName = domainName;
            return this;
        }

        /// Sets the `port`.
        ///
        /// listening port (0 is invalid)
        ///
        /// @param port the value
        /// @return this builder
        public Builder port(final int port) {
            this.port = port;
            return this;
        }

        /// Creates the `ServiceEndpoint` of the values set.
        ///
        /// @return the new `ServiceEndpoint`
        /// @throws NullPointerException if a required value is not set
        /// @throws IllegalArgumentException if a value violates its constraints
        public ServiceEndpoint build() {
            return new ServiceEndpoint(
                    ipAddress,
                    domainName,
                    Objects.requireNonNull(port, "port must not be null"));
        }
    }
}
