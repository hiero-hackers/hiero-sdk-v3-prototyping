package org.hiero.consensusnode.admin.internal;

import com.google.protobuf.ByteString;
import java.util.List;
import org.hiero.consensusnode.admin.nodes.ServiceEndpoint;
import org.hiero.ledger.IpAddress;
import org.jspecify.annotations.Nullable;

/// Maps a `ServiceEndpoint` to the HAPI `ServiceEndpoint` message and back.
public final class Endpoints {

    private Endpoints() {
    }

    /// Maps an endpoint: the IPv4 address goes to `ipAddressV4`, the domain name to `domain_name`; exactly one of
    /// them is set.
    ///
    /// @param endpoint the endpoint
    /// @return the message
    public static com.hederahashgraph.api.proto.java.ServiceEndpoint toProto(final ServiceEndpoint endpoint) {
        final com.hederahashgraph.api.proto.java.ServiceEndpoint.Builder builder =
                com.hederahashgraph.api.proto.java.ServiceEndpoint.newBuilder().setPort(endpoint.port());
        final IpAddress ipAddress = endpoint.ipAddress();
        if (ipAddress != null) {
            builder.setIpAddressV4(ByteString.copyFrom(ipAddress.bytes()));
        }
        final String domainName = endpoint.domainName();
        if (domainName != null) {
            builder.setDomainName(domainName);
        }
        return builder.build();
    }

    /// Maps a list of endpoints.
    ///
    /// @param endpoints the endpoints
    /// @return the messages, in the same order
    public static List<com.hederahashgraph.api.proto.java.ServiceEndpoint> toProto(
            final List<ServiceEndpoint> endpoints) {
        return endpoints.stream().map(Endpoints::toProto).toList();
    }

    /// Maps an endpoint message read from the network. A domain name wins over an IPv4 address when a message carries
    /// both; a message with neither (an unset endpoint) maps to `null`. The port is kept as it is, also `0`.
    ///
    /// @param endpoint the message
    /// @return the endpoint, or `null` if the message names no address
    public static @Nullable ServiceEndpoint fromProto(final com.hederahashgraph.api.proto.java.ServiceEndpoint endpoint) {
        if (!endpoint.getDomainName().isEmpty()) {
            return new ServiceEndpoint(null, endpoint.getDomainName(), endpoint.getPort());
        }
        if (!endpoint.getIpAddressV4().isEmpty()) {
            return new ServiceEndpoint(IpAddress.fromBytes(endpoint.getIpAddressV4().toByteArray()), null,
                    endpoint.getPort());
        }
        return null;
    }
}
