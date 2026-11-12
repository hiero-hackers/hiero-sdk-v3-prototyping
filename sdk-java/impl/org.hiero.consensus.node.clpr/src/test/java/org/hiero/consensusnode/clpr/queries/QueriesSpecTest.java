package org.hiero.consensusnode.clpr.queries;

import static org.hiero.sdk.testing.Testing.*;

import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.List;
import org.hiero.consensusnode.clpr.ClprThrottles;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

/// Tests of the spec `queries`, generated from the `tests` blocks of its `## Testing` section.
@DisplayName("queries")
class QueriesSpecTest {

    /// `consensusnode.clpr.queries/an-endpoint-manifest-keeps-its-endpoints-and-an-empty-proof`
    ///
    /// Source: the SDK proposal for HIP-1535 (Test Plan 20: an empty proof is preserved as a successful read); HAPI
    /// clpr_get_endpoint_manifest.proto.
    @Test
    @DisplayName("an endpoint manifest keeps its endpoints and an empty proof")
    @Tag("testId:consensusnode.clpr.queries/an-endpoint-manifest-keeps-its-endpoints-and-an-empty-proof")
    @Tag("hip:1535")
    void anEndpointManifestKeepsItsEndpointsAndAnEmptyProof() {
        // given
        final ClprEndpoint endpoint = defaultClprEndpoint();

        // when
        final ClprEndpointManifest manifest = new ClprEndpointManifest(3L, hex("000000000000000000000000000000000000016e"), List.of(endpoint), new byte[0]);

        // then
        assertEquals(3L, manifest.version());
        assertEquals(List.of(endpoint), manifest.endpoints());
        assertEquals(0L, length(manifest.manifestStateProof()));
    }

    /// `consensusnode.clpr.queries/a-ledger-configuration-keeps-its-proof-byte-for-byte`
    ///
    /// Source: the SDK proposal for HIP-1535 (Test Plan 19: a proof is preserved byte for byte); HAPI
    /// clpr_get_ledger_configuration.proto.
    @Test
    @DisplayName("a ledger configuration keeps its proof byte for byte")
    @Tag("testId:consensusnode.clpr.queries/a-ledger-configuration-keeps-its-proof-byte-for-byte")
    @Tag("hip:1535")
    void aLedgerConfigurationKeepsItsProofByteForByte() {
        // given
        final byte[] proof = hex("00ff10ef");
        final ClprThrottles throttles = defaultClprThrottles();

        // when
        final ClprLedgerConfiguration configuration = new ClprLedgerConfiguration(1L, "eip155:1", hex("000000000000000000000000000000000000016e"), ZonedDateTime.of(2024, 1, 1, 12, 0, 0, 0, ZoneOffset.UTC), throttles, utf8("trust anchor"), utf8("trust anchor id"), proof);

        // then
        assertEquals("eip155:1", configuration.chainId());
        assertEquals(throttles, configuration.throttles());
        assertEquals(proof, configuration.configurationStateProof());
    }

    /// `consensusnode.clpr.queries/the-clpr-queries-start-without-settings`
    ///
    /// Source: this spec (both queries are free queries without input fields; a new query has no execution settings of
    /// its own).
    @Test
    @DisplayName("the CLPR queries start without settings")
    @Tag("testId:consensusnode.clpr.queries/the-clpr-queries-start-without-settings")
    @Tag("hip:1535")
    void theCLPRQueriesStartWithoutSettings() {
        // when
        final ClprLedgerConfigurationQuery configuration = new ClprLedgerConfigurationQuery();
        final ClprEndpointManifestQuery manifest = new ClprEndpointManifestQuery();

        // then
        assertNull(configuration.retryPolicy());
        assertNull(manifest.attemptTimeout());
    }

    private static ClprEndpoint defaultClprEndpoint() {
        return new ClprEndpoint(new ClprServiceEndpoint("127.0.0.1", 50211), new byte[] {(byte) 1, (byte) 2, (byte) 3}, new byte[] {(byte) 4, (byte) 5, (byte) 6});
    }

    private static ClprThrottles defaultClprThrottles() {
        return new ClprThrottles(10L, 6144L, 1000000L, 100L, 71680L, 0L, 0L);
    }
}
