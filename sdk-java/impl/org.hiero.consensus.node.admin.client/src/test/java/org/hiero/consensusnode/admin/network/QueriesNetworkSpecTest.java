package org.hiero.consensusnode.admin.network;

import static org.hiero.sdk.testing.Testing.*;

import java.util.List;
import org.hiero.common.SemanticVersion;
import org.hiero.consensusnode.admin.nodes.ServiceEndpoint;
import org.hiero.hedera.Hbar;
import org.hiero.hedera.HbarUnit;
import org.hiero.ledger.AccountId;
import org.hiero.nativeToken.NativeToken;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

/// Tests of the spec `queries-network`, generated from the `tests` blocks of its `## Testing` section.
@DisplayName("queries-network")
class QueriesNetworkSpecTest {

    /// `consensusnode.admin.network/versions-and-version-infos-are-equal-when-all-their-parts-are-equal`
    ///
    /// Source: the C++ NetworkVersionInfo unit tests (EqualityIdenticallyConstructed, InequalityDifferentProtobufVersion,
    /// InequalityDifferentServicesVersion); the Equality of the test language.
    @Test
    @DisplayName("versions and version infos are equal when all their parts are equal")
    @Tag("testId:consensusnode.admin.network/versions-and-version-infos-are-equal-when-all-their-parts-are-equal")
    void versionsAndVersionInfosAreEqualWhenAllTheirPartsAreEqual() {
        // given
        final SemanticVersion release = new SemanticVersion(0, 49, 1, "", "");
        final NetworkVersionInfo info = defaultNetworkVersionInfo();

        // when
        final NetworkVersionInfo same = new NetworkVersionInfo(defaultSemanticVersion(), release);
        final NetworkVersionInfo otherHapi = new NetworkVersionInfo(release, release);
        final NetworkVersionInfo otherServices = new NetworkVersionInfo(defaultSemanticVersion(), defaultSemanticVersion());

        // then
        assertNotEquals(release, defaultSemanticVersion());
        assertEquals(info, same);
        assertNotEquals(info, otherHapi);
        assertNotEquals(info, otherServices);
    }

    /// `consensusnode.admin.network/a-network-version-info-keeps-the-hapi-and-the-services-version`
    ///
    /// Source: the JavaScript ("constructor stores both versions"), C++ (ConstructWithValues) and Java (shouldSerialize)
    /// NetworkVersionInfo unit tests.
    @Test
    @DisplayName("a network version info keeps the HAPI and the services version")
    @Tag("testId:consensusnode.admin.network/a-network-version-info-keeps-the-hapi-and-the-services-version")
    void aNetworkVersionInfoKeepsTheHAPIAndTheServicesVersion() {
        // given
        final SemanticVersion hapi = new SemanticVersion(0, 50, 0, "", "");
        final SemanticVersion services = new SemanticVersion(0, 50, 3, "", "");

        // when
        final NetworkVersionInfo info = new NetworkVersionInfo(hapi, services);

        // then
        assertEquals(hapi, info.hapiVersion());
        assertEquals(services, info.servicesVersion());
    }

    /// `consensusnode.admin.network/a-node-address-keeps-all-its-attributes`
    ///
    /// Source: the C++ (GetSetNodeId, GetSetAccountId, GetSetCertHash, GetSetEndpoints, GetSetDescription, GetSetPublicKey)
    /// and Python (test_init) NodeAddress unit tests.
    @Test
    @DisplayName("a node address keeps all its attributes")
    @Tag("testId:consensusnode.admin.network/a-node-address-keeps-all-its-attributes")
    void aNodeAddressKeepsAllItsAttributes() {
        // given
        final AccountId account = new AccountId(0L, 0L, "", 3L, null, null);
        final List<ServiceEndpoint> endpoints = List.of(new ServiceEndpoint(null, "network-node1-svc", 50211));
        final byte[] certificate = utf8("gossip CA certificate");
        final byte[] hash = repeat(hex("cd"), 48L);
        final byte[] rsaKey = utf8("RSA public key");

        // when
        final NodeAddress address = new NodeAddress(0L, account, endpoints, "node1", certificate, hash, rsaKey);

        // then
        assertEquals(0L, address.nodeId());
        assertEquals(account, address.accountId());
        assertEquals(endpoints, address.serviceEndpoints());
        assertEquals("node1", address.description());
        assertEquals(certificate, address.gossipCaCertificate());
        assertEquals(hash, address.grpcCertificateHash());
        assertEquals(rsaKey, address.rsaPublicKey());
    }

    /// `consensusnode.admin.network/a-node-address-has-no-endpoints-and-no-description-by-default`
    ///
    /// Source: the Python NodeAddress unit test (test_empty_addresses); `@@default([])` of serviceEndpoints and
    /// `@@default("")` of description.
    @Test
    @DisplayName("a node address has no endpoints and no description by default")
    @Tag("testId:consensusnode.admin.network/a-node-address-has-no-endpoints-and-no-description-by-default")
    void aNodeAddressHasNoEndpointsAndNoDescriptionByDefault() {
        // when
        final NodeAddress address = new NodeAddress(0L, new AccountId(0L, 0L, "", 3L, null, null), List.of(), "", utf8("gossip CA certificate"), repeat(hex("cd"), 48L), utf8("RSA public key"));

        // then
        assertEquals(0L, length(address.serviceEndpoints()));
        assertEquals("", address.description());
    }

    /// `consensusnode.admin.network/an-address-book-keeps-its-nodes-in-order`
    ///
    /// Source: the Java AddressBookResponse test of the TCK and the C++ AddressBookQuery unit tests (an address book is a
    /// list of node addresses).
    @Test
    @DisplayName("an address book keeps its nodes in order")
    @Tag("testId:consensusnode.admin.network/an-address-book-keeps-its-nodes-in-order")
    void anAddressBookKeepsItsNodesInOrder() {
        // given
        final NodeAddress node0 = defaultNodeAddress();
        final NodeAddress node1 = new NodeAddress(1L, new AccountId(0L, 0L, "", 4L, null, null), List.of(), "", utf8("gossip CA certificate"), repeat(hex("cd"), 48L), utf8("RSA public key"));

        // when
        final NodeAddressBook book = new NodeAddressBook(List.of(node0, node1));

        // then
        assertEquals(2L, length(book.nodes()));
        assertEquals(node0, get(book.nodes(), 0L));
        assertEquals(node1, get(book.nodes(), 1L));
    }

    /// `consensusnode.admin.network/the-version-and-address-book-queries-start-without-settings-and-keep-a-payment-limit`
    ///
    /// Source: this spec (NetworkVersionInfoQuery is a free query, NodeAddressBookQuery a paid one; a new query has no
    /// payment limit and no execution settings of its own); the Java SDK (NetworkVersionInfoQuery and AddressBookQuery
    /// have no attributes to set besides the ones of every query).
    @Test
    @DisplayName("the version and address book queries start without settings and keep a payment limit")
    @Tag("testId:consensusnode.admin.network/the-version-and-address-book-queries-start-without-settings-and-keep-a-payment-limit")
    void theVersionAndAddressBookQueriesStartWithoutSettingsAndKeepAPaymentLimit() {
        // given
        final NativeToken<?, ?> limit = defaultHbar();

        // when
        final NetworkVersionInfoQuery version = new NetworkVersionInfoQuery();
        final NodeAddressBookQuery addressBook = new NodeAddressBookQuery();
        final NodeAddressBookQuery limited = new NodeAddressBookQuery().setMaxQueryPayment(limit);

        // then
        assertNull(version.retryPolicy());
        assertNull(version.attemptTimeout());
        assertNull(addressBook.maxQueryPayment());
        assertNull(addressBook.retryPolicy());
        assertEquals(limit, limited.maxQueryPayment());
    }

    private static NetworkVersionInfo defaultNetworkVersionInfo() {
        return new NetworkVersionInfo(defaultSemanticVersion(), new SemanticVersion(0, 49, 1, "", ""));
    }

    private static SemanticVersion defaultSemanticVersion() {
        return new SemanticVersion(0, 49, 1, "rc.1", "exp.sha.5114f85");
    }

    private static NodeAddress defaultNodeAddress() {
        return new NodeAddress(0L, new AccountId(0L, 0L, "", 3L, null, null), List.of(new ServiceEndpoint(null, "network-node1-svc", 50211)), "node1", new byte[] {(byte) 1, (byte) 2, (byte) 3}, new byte[] {(byte) 4, (byte) 5, (byte) 6}, new byte[] {(byte) 7, (byte) 8, (byte) 9});
    }

    private static Hbar defaultHbar() {
        return new Hbar(1L, HbarUnit.HBAR);
    }
}
