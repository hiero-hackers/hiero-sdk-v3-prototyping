package org.hiero.mirrornode.network;

import java.util.Objects;
import org.jspecify.annotations.Nullable;

/// A reachable gRPC endpoint of a consensus node (GET /network/nodes). One of ipAddressV4 /
/// domainName is populated (HIP-1046 added domain names).
///
/// @param ipAddressV4 dotted-quad IPv4, when published
/// @param domainName DNS name, when published (HIP-1046)
public record ServiceEndpoint(@Nullable String ipAddressV4, int port, @Nullable String domainName) {

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

        private @Nullable String ipAddressV4;
        private @Nullable Integer port;
        private @Nullable String domainName;

        private Builder() {
            // created by ServiceEndpoint.builder()
        }

        /// Sets the `ipAddressV4`.
        ///
        /// dotted-quad IPv4, when published
        ///
        /// @param ipAddressV4 the value
        /// @return this builder
        public Builder ipAddressV4(final @Nullable String ipAddressV4) {
            this.ipAddressV4 = ipAddressV4;
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

        /// Sets the `domainName`.
        ///
        /// DNS name, when published (HIP-1046)
        ///
        /// @param domainName the value
        /// @return this builder
        public Builder domainName(final @Nullable String domainName) {
            this.domainName = domainName;
            return this;
        }

        /// Creates the `ServiceEndpoint` of the values set.
        ///
        /// @return the new `ServiceEndpoint`
        /// @throws NullPointerException if a required value is not set
        public ServiceEndpoint build() {
            return new ServiceEndpoint(
                    ipAddressV4,
                    Objects.requireNonNull(port, "port must not be null"),
                    domainName);
        }
    }
}
