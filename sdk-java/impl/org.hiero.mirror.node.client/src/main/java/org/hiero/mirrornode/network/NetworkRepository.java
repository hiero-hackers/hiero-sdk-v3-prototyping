package org.hiero.mirrornode.network;

import java.util.List;
import java.util.concurrent.CompletionStage;
import org.hiero.common.Page;
import org.hiero.mirrornode.internal.MirrorNodeRest;
import org.jspecify.annotations.Nullable;

public class NetworkRepository {

    /// Creates a new `NetworkRepository`.
    ///
    /// A repository created with this constructor is bound to no mirror node: its queries fail with
    /// `MirrorNodeException`. `NetworkFactory.createRepository` creates a repository for a mirror node.
    public NetworkRepository() {
    }

    /// The returned stage completes exceptionally with `MirrorNodeException` if the operation fails.
    public CompletionStage<@Nullable ExchangeRates> exchangeRates() {
        return MirrorNodeRest.unbound("NetworkRepository");
    }

    /// The returned stage completes exceptionally with `MirrorNodeException` if the operation fails.
    public CompletionStage<List<NetworkFee>> fees() {
        return MirrorNodeRest.unbound("NetworkRepository");
    }

    /// The returned stage completes exceptionally with `MirrorNodeException` if the operation fails.
    public CompletionStage<@Nullable NetworkStake> stake() {
        return MirrorNodeRest.unbound("NetworkRepository");
    }

    /// The returned stage completes exceptionally with `MirrorNodeException` if the operation fails.
    public CompletionStage<@Nullable NetworkSupplies> supplies() {
        return MirrorNodeRest.unbound("NetworkRepository");
    }

    /// The network's consensus-node list (address book).
    /// Maps to GET /api/v1/network/nodes.
    ///
    /// The returned stage completes exceptionally with `MirrorNodeException` if the operation fails.
    public CompletionStage<Page<NetworkNode>> nodes() {
        return MirrorNodeRest.unbound("NetworkRepository");
    }

    /// Estimate the fee for a candidate transaction (HIP-1313). The transaction is passed as its
    /// protobuf-encoded bytes. Maps to POST /api/v1/network/fees.
    ///
    /// The returned stage completes exceptionally with `MirrorNodeException` if the operation fails.
    public CompletionStage<FeeEstimate> estimateFees(final byte[] transaction) {
        return MirrorNodeRest.unbound("NetworkRepository");
    }
}
