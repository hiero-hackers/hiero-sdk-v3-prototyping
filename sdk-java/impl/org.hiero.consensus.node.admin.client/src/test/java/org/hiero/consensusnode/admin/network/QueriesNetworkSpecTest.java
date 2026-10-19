package org.hiero.consensusnode.admin.network;

import static org.hiero.sdk.testing.Testing.*;

import java.util.List;
import java.util.Map;
import org.hiero.consensusnode.admin.nodes.ServiceEndpoint;
import org.hiero.ledger.AccountId;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

/// Tests of the spec `queries-network`, generated from the `tests` blocks of its `## Testing` section.
@DisplayName("queries-network")
class QueriesNetworkSpecTest {

    /// Versions and their canonical semver text: the known answers of the Rust (semantic_version display tests) and Swift
    /// (SemanticVersionUnitTests) SDKs, the example of `SemanticVersion.toString()` in this spec, and examples of
    /// semver.org (items 9 and 10 of the specification).
    private static Map<String, SemanticVersion> SEMANTIC_VERSION_TEXTS() {
        return Map.of("1.2.3", new SemanticVersion(1, 2, 3, "", ""), "3.1.4-15.92", new SemanticVersion(3, 1, 4, "15.92", ""), "1.41.1+6535asd", new SemanticVersion(1, 41, 1, "", "6535asd"), "0.1.4-0.9a2+sha.25531c", new SemanticVersion(0, 1, 4, "0.9a2", "sha.25531c"), "0.49.1-rc.1+exp.sha.5114f85", new SemanticVersion(0, 49, 1, "rc.1", "exp.sha.5114f85"), "1.0.0-x.7.z.92", new SemanticVersion(1, 0, 0, "x.7.z.92", ""), "1.0.0+20130313144700", new SemanticVersion(1, 0, 0, "", "20130313144700"), "1.0.0-beta+exp.sha.5114f85", new SemanticVersion(1, 0, 0, "beta", "exp.sha.5114f85"));
    }

    /// `consensusnode.admin.network/a-semantic-version-is-written-in-its-canonical-semver-form`
    ///
    /// Source: the Rust (semantic_version display tests) and Swift (test_Basic, test_WithPrerelease, test_WithBuild,
    /// test_WithPrereleaseAndBuild) SDKs; the example of `SemanticVersion.toString()` in this spec; semver.org.
    @Test
    @DisplayName("a semantic version is written in its canonical semver form")
    @Tag("testId:consensusnode.admin.network/a-semantic-version-is-written-in-its-canonical-semver-form")
    void aSemanticVersionIsWrittenInItsCanonicalSemverForm() {
        for (final String text : keys(SEMANTIC_VERSION_TEXTS())) {
            // when
            final String written = get(SEMANTIC_VERSION_TEXTS(), text).toString();

            // then
            assertEquals(text, written);
        }
    }

    /// `consensusnode.admin.network/a-semantic-version-keeps-its-numbers-and-labels`
    ///
    /// Source: the JavaScript ("constructor stores major, minor and patch"), C++ (ConstructWithValues) and Swift
    /// (test_ParseWithPrereleaseAndBuild) SemanticVersion unit tests.
    @Test
    @DisplayName("a semantic version keeps its numbers and labels")
    @Tag("testId:consensusnode.admin.network/a-semantic-version-keeps-its-numbers-and-labels")
    void aSemanticVersionKeepsItsNumbersAndLabels() {
        // when
        final SemanticVersion version = new SemanticVersion(1, 2, 0, "beta.10", "d13fe780");

        // then
        assertEquals(1, version.major());
        assertEquals(2, version.minor());
        assertEquals(0, version.patch());
        assertEquals("beta.10", version.preReleaseLabel());
        assertEquals("d13fe780", version.buildMetadata());
    }

    /// `consensusnode.admin.network/a-release-version-has-no-labels-and-is-written-as-major-minor-patch`
    ///
    /// Source: the Swift SemanticVersion unit tests (test_ParseMajorMinorPatch, test_ParseZeroMinorPatch: a release has no
    /// labels); the API schema of this spec (both labels are empty for a release version).
    @Test
    @DisplayName("a release version has no labels and is written as major.minor.patch")
    @Tag("testId:consensusnode.admin.network/a-release-version-has-no-labels-and-is-written-as-major-minor-patch")
    void aReleaseVersionHasNoLabelsAndIsWrittenAsMajorMinorPatch() {
        // when
        final SemanticVersion version = new SemanticVersion(0, 2, 0, "", "");

        // then
        assertEquals("", version.preReleaseLabel());
        assertEquals("", version.buildMetadata());
        assertEquals("0.2.0", version.toString());
    }

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

    private static NetworkVersionInfo defaultNetworkVersionInfo() {
        return new NetworkVersionInfo(defaultSemanticVersion(), new SemanticVersion(0, 49, 1, "", ""));
    }

    private static SemanticVersion defaultSemanticVersion() {
        return new SemanticVersion(0, 49, 1, "rc.1", "exp.sha.5114f85");
    }

    private static NodeAddress defaultNodeAddress() {
        return new NodeAddress(0L, new AccountId(0L, 0L, "", 3L, null, null), List.of(new ServiceEndpoint(null, "network-node1-svc", 50211)), "node1", new byte[] {(byte) 1, (byte) 2, (byte) 3}, new byte[] {(byte) 4, (byte) 5, (byte) 6}, new byte[] {(byte) 7, (byte) 8, (byte) 9});
    }
}
