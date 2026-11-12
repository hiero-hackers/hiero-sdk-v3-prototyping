package org.hiero.consensusnode.clpr.internal;

import com.hederahashgraph.api.proto.java.ClprGetEndpointManifestQuery;
import com.hederahashgraph.api.proto.java.ClprGetEndpointManifestResponse;
import com.hederahashgraph.api.proto.java.ClprGetLedgerConfigurationQuery;
import com.hederahashgraph.api.proto.java.ClprGetLedgerConfigurationResponse;
import com.hederahashgraph.api.proto.java.Query;
import org.hiero.consensusnode.client.internal.Protobuf;
import org.hiero.consensusnode.client.internal.QueryKind;
import org.hiero.consensusnode.clpr.ClprThrottles;
import org.hiero.consensusnode.clpr.queries.ClprEndpoint;
import org.hiero.consensusnode.clpr.queries.ClprEndpointManifest;
import org.hiero.consensusnode.clpr.queries.ClprEndpointManifestQuery;
import org.hiero.consensusnode.clpr.queries.ClprLedgerConfiguration;
import org.hiero.consensusnode.clpr.queries.ClprLedgerConfigurationQuery;
import org.hiero.consensusnode.clpr.queries.ClprServiceEndpoint;

/// The query types of the CLPR service (`proto.ClprService`).
public final class ClprQueries {

    /// `ClprLedgerConfigurationQuery`: `ClprService.getLedgerConfiguration`.
    public static final QueryKind<ClprLedgerConfigurationQuery, ClprLedgerConfiguration> LEDGER_CONFIGURATION =
            new QueryKind<>("proto.ClprService", "getLedgerConfiguration",
                    (query, header) -> Query.newBuilder().setClprGetLedgerConfiguration(
                            ClprGetLedgerConfigurationQuery.newBuilder().setHeader(header)).build(),
                    response -> response.getClprGetLedgerConfiguration().getHeader(),
                    (query, response) -> ledgerConfiguration(response.getClprGetLedgerConfiguration()));

    /// `ClprEndpointManifestQuery`: `ClprService.getEndpointManifest`.
    public static final QueryKind<ClprEndpointManifestQuery, ClprEndpointManifest> ENDPOINT_MANIFEST =
            new QueryKind<>("proto.ClprService", "getEndpointManifest",
                    (query, header) -> Query.newBuilder().setClprGetEndpointManifest(
                            ClprGetEndpointManifestQuery.newBuilder().setHeader(header)).build(),
                    response -> response.getClprGetEndpointManifest().getHeader(),
                    (query, response) -> endpointManifest(response.getClprGetEndpointManifest()));

    private ClprQueries() {
    }

    private static ClprLedgerConfiguration ledgerConfiguration(final ClprGetLedgerConfigurationResponse response) {
        final com.hederahashgraph.api.proto.java.ClprLedgerConfiguration configuration = response.getConfiguration();
        return new ClprLedgerConfiguration(Integer.toUnsignedLong(configuration.getProtocolVersion()),
                configuration.getChainId(), configuration.getServiceAddress().toByteArray(),
                Protobuf.fromProto(configuration.getTimestamp()), throttles(configuration.getThrottles()),
                configuration.getInitialTrustAnchor().toByteArray(),
                configuration.getInitialTrustAnchorId().toByteArray(),
                response.getConfigurationStateProof().toByteArray());
    }

    private static ClprThrottles throttles(final com.hederahashgraph.api.proto.java.ClprThrottles throttles) {
        return new ClprThrottles(Integer.toUnsignedLong(throttles.getMaxMessagesPerBundle()),
                Integer.toUnsignedLong(throttles.getMaxMessagePayloadBytes()), throttles.getMaxGasPerMessage(),
                Integer.toUnsignedLong(throttles.getMaxQueueDepth()), throttles.getMaxSyncBytes(),
                Integer.toUnsignedLong(throttles.getMaxLocalEndpoints()),
                Integer.toUnsignedLong(throttles.getMaxPeerEndpoints()));
    }

    private static ClprEndpointManifest endpointManifest(final ClprGetEndpointManifestResponse response) {
        final com.hederahashgraph.api.proto.java.ClprEndpointManifest manifest = response.getManifest();
        return new ClprEndpointManifest(manifest.getVersion(), manifest.getServiceAddress().toByteArray(),
                manifest.getEndpointsList().stream().map(ClprQueries::endpoint).toList(),
                response.getManifestStateProof().toByteArray());
    }

    private static ClprEndpoint endpoint(final com.hederahashgraph.api.proto.java.ClprEndpoint endpoint) {
        return new ClprEndpoint(new ClprServiceEndpoint(endpoint.getServiceEndpoint().getIpAddress(),
                endpoint.getServiceEndpoint().getPort()), endpoint.getTlsCertificate().toByteArray(),
                endpoint.getAccountId().toByteArray());
    }
}
