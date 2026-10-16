package org.hiero.mirrornode.network;

import static org.hiero.sdk.testing.Testing.*;

import java.util.List;
import org.hiero.ledger.AccountId;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/// Tests of the spec `mirror-node-network`, generated from the `tests` blocks of its `## Testing` section.
@DisplayName("mirror-node-network")
class MirrorNodeNetworkSpecTest {

    /// `mirrornode.network/a-service-endpoint-keeps-either-its-ipv4-address-or-its-domain-name-and-its-port`
    ///
    /// Source: the Java SDK (MirrorNodeServiceEndpointTest: IP address 1.2.3.4, domain name "test.mirror.com", port 443);
    /// HIP-1046 (an endpoint is published either with an IP address or with a domain name).
    @Test
    @DisplayName("a service endpoint keeps either its IPv4 address or its domain name and its port")
    void aServiceEndpointKeepsEitherItsIPv4AddressOrItsDomainNameAndItsPort() {
        // when
        final ServiceEndpoint byAddress = new ServiceEndpoint("1.2.3.4", 443, null);
        final ServiceEndpoint byName = new ServiceEndpoint(null, 443, "test.mirror.com");

        // then
        assertEquals("1.2.3.4", byAddress.ipAddressV4());
        assertNull(byAddress.domainName());
        assertEquals(443, byAddress.port());
        assertEquals("test.mirror.com", byName.domainName());
        assertNull(byName.ipAddressV4());
        assertEquals(443, byName.port());
    }

    /// `mirrornode.network/a-network-node-without-service-endpoints-and-admin-key-has-an-empty-endpoint-list-and-no-admin-key`
    ///
    /// Source: this spec (`serviceEndpoints` defaults to an empty list; `adminKey` is null on entries from before HIP-869).
    @Test
    @DisplayName("a network node without service endpoints and admin key has an empty endpoint list and no admin key")
    void aNetworkNodeWithoutServiceEndpointsAndAdminKeyHasAnEmptyEndpointListAndNoAdminKey() {
        // given
        final AccountId account = AccountId.fromString("0.0.3");

        // when
        final NetworkNode node = new NetworkNode(0L, account, null, null, null, List.of(), null, 0L, null);

        // then
        assertEquals(0L, node.nodeId());
        assertEquals(account, node.accountId());
        assertEquals(0L, length(node.serviceEndpoints()));
        assertNull(node.adminKey());
        assertNull(node.timestamp());
    }

    /// `mirrornode.network/a-network-node-keeps-its-service-endpoints-in-order`
    ///
    /// Source: the Java SDK (MirrorNodeServiceEndpointTest); this spec (the address book entry lists its endpoints).
    @Test
    @DisplayName("a network node keeps its service endpoints in order")
    void aNetworkNodeKeepsItsServiceEndpointsInOrder() {
        // given
        final List<ServiceEndpoint> endpoints = List.of(new ServiceEndpoint("1.2.3.4", 443, null), new ServiceEndpoint(null, 443, "test.mirror.com"));

        // when
        final NetworkNode node = new NetworkNode(0L, AccountId.fromString("0.0.3"), null, null, null, endpoints, null, 0L, null);

        // then
        assertEquals(endpoints, node.serviceEndpoints());
    }
}
