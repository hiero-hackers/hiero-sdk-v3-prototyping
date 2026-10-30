package org.hiero.mirrornode.network;

import java.util.List;
import java.util.concurrent.CompletionStage;
import org.hiero.common.Page;
import org.hiero.mirrornode.common.Order;
import org.hiero.mirrornode.internal.MirrorNodeRest;
import org.jspecify.annotations.Nullable;

public class NetworkRepository {

    /// Creates a new `NetworkRepository`.
    ///
    /// A repository created with this constructor is bound to no mirror node: its queries fail with
    /// `MirrorNodeException`. `NetworkFactory.createRepository` creates a repository for a mirror node.
    public NetworkRepository() {
    }

    /// The current and the next exchange rate. Maps to GET /api/v1/network/exchangerate.
    ///
    /// The returned stage completes exceptionally with `MirrorNodeException` if the operation fails.
    public CompletionStage<@Nullable ExchangeRates> exchangeRates() {
        return MirrorNodeRest.unbound("NetworkRepository");
    }

    /// The exchange rates at the given consensus time. Maps to GET /api/v1/network/exchangerate.
    ///
    /// The returned stage completes exceptionally with `MirrorNodeException` if the operation fails.
    public CompletionStage<@Nullable ExchangeRates> exchangeRates(final List<String> timestamp) {
        return MirrorNodeRest.unbound("NetworkRepository");
    }

    /// The gas fee schedule. Maps to GET /api/v1/network/fees.
    ///
    /// The returned stage completes exceptionally with `MirrorNodeException` if the operation fails.
    public CompletionStage<NetworkFees> fees() {
        return MirrorNodeRest.unbound("NetworkRepository");
    }

    /// The gas fee schedule at the given consensus time. Maps to GET /api/v1/network/fees.
    ///
    /// The returned stage completes exceptionally with `MirrorNodeException` if the operation fails.
    public CompletionStage<NetworkFees> fees(final @Nullable Order order, final List<String> timestamp) {
        return MirrorNodeRest.unbound("NetworkRepository");
    }

    /// The staking parameters. Maps to GET /api/v1/network/stake.
    ///
    /// The returned stage completes exceptionally with `MirrorNodeException` if the operation fails.
    public CompletionStage<@Nullable NetworkStake> stake() {
        return MirrorNodeRest.unbound("NetworkRepository");
    }

    /// The HBAR supply. Maps to GET /api/v1/network/supply.
    ///
    /// The returned stage completes exceptionally with `MirrorNodeException` if the operation fails.
    public CompletionStage<@Nullable NetworkSupplies> supplies() {
        return MirrorNodeRest.unbound("NetworkRepository");
    }

    /// The HBAR supply at the given consensus time. Maps to GET /api/v1/network/supply.
    ///
    /// The returned stage completes exceptionally with `MirrorNodeException` if the operation fails.
    public CompletionStage<@Nullable NetworkSupplies> supplies(final List<String> timestamp) {
        return MirrorNodeRest.unbound("NetworkRepository");
    }

    /// The network's consensus-node list (address book).
    /// Maps to GET /api/v1/network/nodes.
    ///
    /// The returned stage completes exceptionally with `MirrorNodeException` if the operation fails.
    public CompletionStage<Page<NetworkNode>> nodes() {
        return MirrorNodeRest.unbound("NetworkRepository");
    }

    /// The network's consensus-node list (address book), filtered by the address-book file and the node id.
    /// Maps to GET /api/v1/network/nodes.
    ///
    /// The returned stage completes exceptionally with `MirrorNodeException` if the operation fails.
    public CompletionStage<Page<NetworkNode>> nodes(final List<String> fileId, final @Nullable Integer limit, final List<String> nodeId, final @Nullable Order order) {
        return MirrorNodeRest.unbound("NetworkRepository");
    }

    /// The registered nodes of the network. Maps to GET /api/v1/network/registered-nodes.
    ///
    /// The returned stage completes exceptionally with `MirrorNodeException` if the operation fails.
    public CompletionStage<Page<RegisteredNode>> registeredNodes() {
        return MirrorNodeRest.unbound("NetworkRepository");
    }

    /// The registered nodes of the network, filtered by id and type. Maps to GET /api/v1/network/registered-nodes.
    ///
    /// The returned stage completes exceptionally with `MirrorNodeException` if the operation fails.
    public CompletionStage<Page<RegisteredNode>> registeredNodes(final @Nullable Integer limit, final @Nullable Order order, final List<String> registeredNodeId, final @Nullable RegisteredNodeType type) {
        return MirrorNodeRest.unbound("NetworkRepository");
    }

    /// Estimate the fee for a candidate transaction (HIP-1313). The transaction is passed as its
    /// protobuf-encoded bytes. Maps to POST /api/v1/network/fees.
    ///
    /// The returned stage completes exceptionally with `MirrorNodeException` if the operation fails.
    public CompletionStage<FeeEstimate> estimateFees(final byte[] transaction) {
        return MirrorNodeRest.unbound("NetworkRepository");
    }

    /// Estimate the fee for a candidate transaction (HIP-1313) in the given mode, for the given high-volume throttle
    /// utilization (in basis points). Maps to POST /api/v1/network/fees.
    ///
    /// The returned stage completes exceptionally with `MirrorNodeException` if the operation fails.
    public CompletionStage<FeeEstimate> estimateFees(final byte[] transaction, final @Nullable FeeEstimateMode mode, final @Nullable Integer highVolumeThrottle) {
        return MirrorNodeRest.unbound("NetworkRepository");
    }
}
