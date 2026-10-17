package org.hiero.consensusnode.admin.nodes;

import static org.hiero.sdk.testing.Testing.*;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.List;
import org.hiero.authority.Authority;
import org.hiero.authority.PublicKeyAuthority;
import org.hiero.keys.ByteImportEncoding;
import org.hiero.keys.KeyAlgorithm;
import org.hiero.keys.KeysFactory;
import org.hiero.keys.PrivateKey;
import org.hiero.keys.PublicKey;
import org.hiero.ledger.AccountId;
import org.hiero.ledger.IpAddress;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/// Tests of the spec `transactions-nodes`, generated from the `tests` blocks of its `## Testing` section.
@DisplayName("transactions-nodes")
class TransactionsNodesSpecTest {

    /// A gRPC endpoint by domain name, as in the example "Register a new consensus node".
    private static ServiceEndpoint NODE_DOMAIN_ENDPOINT() {
        return new ServiceEndpoint(null, "node7.consensus.example", 50211);
    }

    /// Stand-ins for the DER bytes of a gossip CA certificate and for the SHA-384 hash (48 bytes) of a gRPC certificate;
    /// their content does not matter to the transactions.
    private static byte[] GOSSIP_CA_CERTIFICATE() {
        return utf8("gossip CA certificate");
    }

    private static byte[] GRPC_CERTIFICATE_HASH() {
        return repeat(hex("cd"), 48L);
    }

    /// One endpoint beyond the @@maxSize(10) of the gossip endpoints (11 entries).
    private static List<ServiceEndpoint> ELEVEN_ENDPOINTS() {
        return List.of(defaultServiceEndpoint(), defaultServiceEndpoint(), defaultServiceEndpoint(), defaultServiceEndpoint(), defaultServiceEndpoint(), defaultServiceEndpoint(), defaultServiceEndpoint(), defaultServiceEndpoint(), defaultServiceEndpoint(), defaultServiceEndpoint(), defaultServiceEndpoint());
    }

    /// One endpoint beyond the @@maxSize(8) of the service endpoints (9 entries).
    private static List<ServiceEndpoint> NINE_ENDPOINTS() {
        return List.of(defaultServiceEndpoint(), defaultServiceEndpoint(), defaultServiceEndpoint(), defaultServiceEndpoint(), defaultServiceEndpoint(), defaultServiceEndpoint(), defaultServiceEndpoint(), defaultServiceEndpoint(), defaultServiceEndpoint());
    }

    /// `consensusnode.admin.nodes/an-endpoint-by-ip-address-keeps-the-address-and-the-port`
    ///
    /// Source: the Go (TestEndpointSetAndGetAddress, TestEndpointSetAndGetPort), Python (test_constructor_with_values),
    /// JavaScript ("should create EndPoint with both address and port") and C++ Endpoint unit tests.
    @Test
    @DisplayName("an endpoint by IP address keeps the address and the port")
    void anEndpointByIPAddressKeepsTheAddressAndThePort() {
        // given
        final IpAddress ipAddress = new IpAddress(new byte[] {(byte) 10, (byte) 0, (byte) 0, (byte) 7});

        // when
        final ServiceEndpoint endpoint = new ServiceEndpoint(ipAddress, null, 50211);

        // then
        assertEquals(ipAddress, endpoint.ipAddress());
        assertNull(endpoint.domainName());
        assertEquals(50211, endpoint.port());
    }

    /// `consensusnode.admin.nodes/an-endpoint-by-domain-name-keeps-the-name-and-the-port`
    ///
    /// Source: the Go (TestEndpointSetAndGetDomainName), JavaScript ("should create EndPoint from domain name JSON"),
    /// Python and Java (validateAllowsOnlyDomain) Endpoint unit tests.
    @Test
    @DisplayName("an endpoint by domain name keeps the name and the port")
    void anEndpointByDomainNameKeepsTheNameAndThePort() {
        // when
        final ServiceEndpoint endpoint = new ServiceEndpoint(null, "node7.consensus.example", 50211);

        // then
        assertNull(endpoint.ipAddress());
        assertEquals("node7.consensus.example", endpoint.domainName());
        assertEquals(50211, endpoint.port());
    }

    /// `consensusnode.admin.nodes/endpoints-are-equal-when-address-name-and-port-are-equal`
    ///
    /// Source: the Go (TestEndpointRoundTripProtobuf) and Python (test_serialization_roundtrip) Endpoint unit tests, which
    /// compare endpoints by value; the Equality of the test language.
    @Test
    @DisplayName("endpoints are equal when address, name and port are equal")
    void endpointsAreEqualWhenAddressNameAndPortAreEqual() {
        // given
        final ServiceEndpoint endpoint = defaultServiceEndpoint();

        // when
        final ServiceEndpoint same = new ServiceEndpoint(new IpAddress(new byte[] {(byte) 10, (byte) 0, (byte) 0, (byte) 7}), null, 50211);
        final ServiceEndpoint otherPort = new ServiceEndpoint(new IpAddress(new byte[] {(byte) 10, (byte) 0, (byte) 0, (byte) 7}), null, 50212);

        // then
        assertEquals(endpoint, same);
        assertNotEquals(endpoint, otherPort);
        assertNotEquals(endpoint, NODE_DOMAIN_ENDPOINT());
    }

    /// `consensusnode.admin.nodes/an-endpoint-with-both-an-ip-address-and-a-domain-name-is-rejected`
    ///
    /// Source: the Java (validateThrowsOnIpAndDomain), Go (TestEndpointValidateFailure "BothAddressAndDomain") and Python
    /// (test_validate_no_ip_and_domain_containing_both_ip_and_domain) Endpoint unit tests; `@@oneOf(ipAddress, domainName)`
    /// of this spec.
    @Test
    @DisplayName("an endpoint with both an IP address and a domain name is rejected")
    void anEndpointWithBothAnIPAddressAndADomainNameIsRejected() {
        // when / then
        assertThrows(IllegalArgumentException.class, () -> new ServiceEndpoint(new IpAddress(new byte[] {(byte) 10, (byte) 0, (byte) 0, (byte) 7}), "node7.consensus.example", 50211));
    }

    /// `consensusnode.admin.nodes/an-endpoint-with-neither-an-ip-address-nor-a-domain-name-is-rejected`
    ///
    /// Source: the Go Endpoint unit test (TestEndpointValidateFailure "NeitherAddressNorDomain");
    /// `@@oneOf(ipAddress, domainName)` of this spec.
    @Test
    @DisplayName("an endpoint with neither an IP address nor a domain name is rejected")
    void anEndpointWithNeitherAnIPAddressNorADomainNameIsRejected() {
        // when / then
        assertThrows(IllegalArgumentException.class, () -> new ServiceEndpoint(null, null, 50211));
    }

    /// `consensusnode.admin.nodes/a-node-create-transaction-keeps-all-its-attributes`
    ///
    /// Source: the Java, Go (TestUnitNodeCreateTransactionGet), Python (test_constructor_with_parameters), C++, JavaScript
    /// and Swift NodeCreateTransaction unit tests.
    @Test
    @DisplayName("a node create transaction keeps all its attributes")
    void aNodeCreateTransactionKeepsAllItsAttributes() {
        // given
        final AccountId account = defaultAccountId();
        final Authority adminAuthority = defaultAuthority();

        // when
        final NodeCreateTransaction transaction = new NodeCreateTransaction(account, "consensus-node-7", List.of(defaultServiceEndpoint()), List.of(NODE_DOMAIN_ENDPOINT()), GOSSIP_CA_CERTIFICATE(), GRPC_CERTIFICATE_HASH(), adminAuthority, true, NODE_DOMAIN_ENDPOINT());

        // then
        assertEquals(account, transaction.accountId());
        assertEquals("consensus-node-7", transaction.description());
        assertEquals(List.of(defaultServiceEndpoint()), transaction.gossipEndpoints());
        assertEquals(List.of(NODE_DOMAIN_ENDPOINT()), transaction.serviceEndpoints());
        assertEquals(GOSSIP_CA_CERTIFICATE(), transaction.gossipCaCertificate());
        assertEquals(GRPC_CERTIFICATE_HASH(), transaction.grpcCertificateHash());
        assertEquals(adminAuthority, transaction.adminAuthority());
        assertTrue(transaction.declineReward());
        assertEquals(NODE_DOMAIN_ENDPOINT(), transaction.grpcWebProxyEndpoint());
    }

    /// `consensusnode.admin.nodes/a-node-create-transaction-takes-the-reward-and-has-no-description-and-no-proxy-by-default`
    ///
    /// Source: the Python (test_constructor_default_values) and Go (TestUnitNodeCreateTransactionDeclineReward)
    /// NodeCreateTransaction unit tests; `@@default(false)` of declineReward.
    @Test
    @DisplayName("a node create transaction takes the reward and has no description and no proxy by default")
    void aNodeCreateTransactionTakesTheRewardAndHasNoDescriptionAndNoProxyByDefault() {
        // when
        final NodeCreateTransaction transaction = new NodeCreateTransaction(defaultAccountId(), null, List.of(defaultServiceEndpoint()), List.of(NODE_DOMAIN_ENDPOINT()), GOSSIP_CA_CERTIFICATE(), GRPC_CERTIFICATE_HASH(), defaultAuthority(), false, null);

        // then
        assertFalse(transaction.declineReward());
        assertNull(transaction.description());
        assertNull(transaction.grpcWebProxyEndpoint());
    }

    /// `consensusnode.admin.nodes/a-node-create-transaction-without-gossip-endpoints-is-rejected`
    ///
    /// Source: HAPI node_create.proto (gossip_endpoint: "This list MUST NOT be empty"); `@@minSize(1)` of this spec.
    @Test
    @DisplayName("a node create transaction without gossip endpoints is rejected")
    void aNodeCreateTransactionWithoutGossipEndpointsIsRejected() {
        // when / then
        assertThrows(IllegalArgumentException.class, () -> new NodeCreateTransaction(defaultAccountId(), null, List.of(), List.of(NODE_DOMAIN_ENDPOINT()), GOSSIP_CA_CERTIFICATE(), GRPC_CERTIFICATE_HASH(), defaultAuthority(), false, null));
    }

    /// `consensusnode.admin.nodes/a-node-create-transaction-without-service-endpoints-is-rejected`
    ///
    /// Source: HAPI node_create.proto (service_endpoint: "This list MUST NOT be empty"); `@@minSize(1)` of this spec.
    @Test
    @DisplayName("a node create transaction without service endpoints is rejected")
    void aNodeCreateTransactionWithoutServiceEndpointsIsRejected() {
        // when / then
        assertThrows(IllegalArgumentException.class, () -> new NodeCreateTransaction(defaultAccountId(), null, List.of(defaultServiceEndpoint()), List.of(), GOSSIP_CA_CERTIFICATE(), GRPC_CERTIFICATE_HASH(), defaultAuthority(), false, null));
    }

    /// `consensusnode.admin.nodes/a-node-update-transaction-changes-nothing-but-what-is-set`
    ///
    /// Source: the Python (test_constructor_default_values), Go (TestUnitNodeUpdateTransactionSetNothing) and JavaScript
    /// NodeUpdateTransaction unit tests; the API schema of this spec (fields that are not set are left unchanged, an empty
    /// endpoint list leaves the list unchanged).
    @Test
    @DisplayName("a node update transaction changes nothing but what is set")
    void aNodeUpdateTransactionChangesNothingButWhatIsSet() {
        // when
        final NodeUpdateTransaction transaction = new NodeUpdateTransaction(7L, null, null, List.of(), List.of(), null, null, null, null, null);

        // then
        assertEquals(7L, transaction.nodeId());
        assertNull(transaction.accountId());
        assertNull(transaction.description());
        assertEquals(0L, length(transaction.gossipEndpoints()));
        assertEquals(0L, length(transaction.serviceEndpoints()));
        assertNull(transaction.gossipCaCertificate());
        assertNull(transaction.grpcCertificateHash());
        assertNull(transaction.adminAuthority());
        assertNull(transaction.declineReward());
        assertNull(transaction.grpcWebProxyEndpoint());
    }

    /// `consensusnode.admin.nodes/a-node-update-transaction-keeps-the-attributes-it-sets`
    ///
    /// Source: the Java, Go (TestUnitNodeUpdateTransactionGet), Python (test_constructor_with_parameters), C++, JavaScript
    /// and Swift NodeUpdateTransaction unit tests.
    @Test
    @DisplayName("a node update transaction keeps the attributes it sets")
    void aNodeUpdateTransactionKeepsTheAttributesItSets() {
        // given
        final AccountId account = defaultAccountId();
        final Authority adminAuthority = defaultAuthority();

        // when
        final NodeUpdateTransaction transaction = new NodeUpdateTransaction(7L, account, "consensus-node-7", List.of(defaultServiceEndpoint()), List.of(NODE_DOMAIN_ENDPOINT()), GOSSIP_CA_CERTIFICATE(), GRPC_CERTIFICATE_HASH(), adminAuthority, null, NODE_DOMAIN_ENDPOINT());

        // then
        assertEquals(7L, transaction.nodeId());
        assertEquals(account, transaction.accountId());
        assertEquals("consensus-node-7", transaction.description());
        assertEquals(List.of(defaultServiceEndpoint()), transaction.gossipEndpoints());
        assertEquals(List.of(NODE_DOMAIN_ENDPOINT()), transaction.serviceEndpoints());
        assertEquals(GOSSIP_CA_CERTIFICATE(), transaction.gossipCaCertificate());
        assertEquals(GRPC_CERTIFICATE_HASH(), transaction.grpcCertificateHash());
        assertEquals(adminAuthority, transaction.adminAuthority());
        assertEquals(NODE_DOMAIN_ENDPOINT(), transaction.grpcWebProxyEndpoint());
    }

    /// `consensusnode.admin.nodes/a-node-update-transaction-keeps-declinereward-true-and-false`
    ///
    /// Source: the Go NodeUpdateTransaction unit test (TestUnitNodeUpdateTransactionDeclineReward): true and false are both
    /// kept and differ from "not set".
    @Test
    @DisplayName("a node update transaction keeps declineReward true and false")
    void aNodeUpdateTransactionKeepsDeclineRewardTrueAndFalse() {
        for (final boolean declineReward : List.of(true, false)) {
            // when
            final NodeUpdateTransaction transaction = new NodeUpdateTransaction(7L, null, null, List.of(), List.of(), null, null, null, declineReward, null);

            // then
            assertEquals(declineReward, transaction.declineReward());
        }
    }

    /// `consensusnode.admin.nodes/a-node-delete-transaction-keeps-its-node-id`
    ///
    /// Source: the Java, Go, Python (test_constructor_with_parameter), C++, JavaScript and Swift NodeDeleteTransaction unit
    /// tests.
    @Test
    @DisplayName("a node delete transaction keeps its node id")
    void aNodeDeleteTransactionKeepsItsNodeId() {
        // when
        final NodeDeleteTransaction transaction = new NodeDeleteTransaction(7L);

        // then
        assertEquals(7L, transaction.nodeId());
    }

    /// `consensusnode.admin.nodes/a-node-create-transaction-with-more-than-10-gossip-endpoints-is-rejected`
    ///
    /// Source: this spec (@@maxSize(10)); HAPI node_create.proto (gossip_endpoint: "MUST NOT contain more than 10
    /// entries"); the consensus node (nodes.maxGossipEndpoint = 10, GOSSIP_ENDPOINTS_EXCEEDED_LIMIT); the Java, JavaScript,
    /// Go and Python SDKs reject more than 10 gossip endpoints.
    @Test
    @DisplayName("a node create transaction with more than 10 gossip endpoints is rejected")
    void aNodeCreateTransactionWithMoreThan10GossipEndpointsIsRejected() {
        // when / then
        assertThrows(IllegalArgumentException.class, () -> new NodeCreateTransaction(defaultAccountId(), null, ELEVEN_ENDPOINTS(), List.of(NODE_DOMAIN_ENDPOINT()), GOSSIP_CA_CERTIFICATE(), GRPC_CERTIFICATE_HASH(), defaultAuthority(), false, null));
    }

    /// `consensusnode.admin.nodes/a-node-create-transaction-with-more-than-8-service-endpoints-is-rejected`
    ///
    /// Source: this spec (@@maxSize(8)); HAPI node_create.proto (service_endpoint: "MUST NOT contain more than 8
    /// entries"); the consensus node (nodes.maxServiceEndpoint = 8, SERVICE_ENDPOINTS_EXCEEDED_LIMIT); the Java,
    /// JavaScript, Go and Python SDKs reject more than 8 service endpoints.
    @Test
    @DisplayName("a node create transaction with more than 8 service endpoints is rejected")
    void aNodeCreateTransactionWithMoreThan8ServiceEndpointsIsRejected() {
        // when / then
        assertThrows(IllegalArgumentException.class, () -> new NodeCreateTransaction(defaultAccountId(), null, List.of(defaultServiceEndpoint()), NINE_ENDPOINTS(), GOSSIP_CA_CERTIFICATE(), GRPC_CERTIFICATE_HASH(), defaultAuthority(), false, null));
    }

    /// `consensusnode.admin.nodes/a-node-update-transaction-with-too-many-endpoints-is-rejected`
    ///
    /// Source: this spec (@@maxSize(10) and @@maxSize(8)); the Java SDK (NodeUpdateTransaction.setGossipEndpoints and
    /// setServiceEndpoints reject more than 10 resp. 8 entries).
    @Test
    @DisplayName("a node update transaction with too many endpoints is rejected")
    void aNodeUpdateTransactionWithTooManyEndpointsIsRejected() {
        // when / then
        assertThrows(IllegalArgumentException.class, () -> new NodeUpdateTransaction(7L, null, null, ELEVEN_ENDPOINTS(), List.of(), null, null, null, null, null));
        assertThrows(IllegalArgumentException.class, () -> new NodeUpdateTransaction(7L, null, null, List.of(), NINE_ENDPOINTS(), null, null, null, null, null));
    }

    /// `consensusnode.admin.nodes/a-node-create-transaction-with-a-description-of-more-than-100-utf-8-bytes-is-rejected`
    ///
    /// Source: this spec (@@maxBytes(100) of description); the consensus node (nodes.nodeMaxDescriptionUtf8Bytes = 100,
    /// INVALID_NODE_DESCRIPTION); the Java, Go and Python SDKs check the UTF-8 length: 34 characters "€" are 102 bytes.
    @Test
    @DisplayName("a node create transaction with a description of more than 100 UTF-8 bytes is rejected")
    void aNodeCreateTransactionWithADescriptionOfMoreThan100UTF8BytesIsRejected() {
        // when / then
        assertThrows(IllegalArgumentException.class, () -> new NodeCreateTransaction(defaultAccountId(), repeat("€", 34L), List.of(defaultServiceEndpoint()), List.of(NODE_DOMAIN_ENDPOINT()), GOSSIP_CA_CERTIFICATE(), GRPC_CERTIFICATE_HASH(), defaultAuthority(), false, null));
    }

    private static ServiceEndpoint defaultServiceEndpoint() {
        return new ServiceEndpoint(new IpAddress(new byte[] {(byte) 10, (byte) 0, (byte) 0, (byte) 7}), null, 50211);
    }

    private static AccountId defaultAccountId() {
        return new AccountId(0L, 0L, "", 1001L, null, null);
    }

    private static Authority defaultAuthority() {
        return new PublicKeyAuthority(defaultPublicKey());
    }

    private static PublicKey defaultPublicKey() {
        return defaultPrivateKey().createPublicKey();
    }

    private static PrivateKey defaultPrivateKey() {
        return KeysFactory.createPrivateKey(KeyAlgorithm.ED25519, ByteImportEncoding.HEX, "d3671a1e98bb22f011c0e4bcf5125590e15d8f21a7017309bb558852039bc75c");
    }
}
