package org.hiero.mirrornode.internal;

import java.math.BigInteger;
import java.time.Duration;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletionStage;
import org.hiero.common.Page;
import org.hiero.mirrornode.common.Order;
import org.hiero.mirrornode.internal.Json.JsonObject;
import org.hiero.mirrornode.network.ExchangeRate;
import org.hiero.mirrornode.network.ExchangeRates;
import org.hiero.mirrornode.network.FeeEstimate;
import org.hiero.mirrornode.network.FeeEstimateComponent;
import org.hiero.mirrornode.network.FeeEstimateMode;
import org.hiero.mirrornode.network.FeeEstimateNetwork;
import org.hiero.mirrornode.network.FeeExtra;
import org.hiero.mirrornode.network.NetworkFee;
import org.hiero.mirrornode.network.NetworkFees;
import org.hiero.mirrornode.network.NetworkNode;
import org.hiero.mirrornode.network.NetworkRepository;
import org.hiero.mirrornode.network.NetworkStake;
import org.hiero.mirrornode.network.NetworkSupplies;
import org.hiero.mirrornode.network.RegisteredBlockNodeApi;
import org.hiero.mirrornode.network.RegisteredNode;
import org.hiero.mirrornode.network.RegisteredNodeType;
import org.hiero.mirrornode.network.RegisteredServiceEndpoint;
import org.hiero.mirrornode.network.ServiceEndpoint;
import org.jspecify.annotations.Nullable;

/// The `NetworkRepository` of the SDK, on the endpoints `/network/...` of the Mirror Node REST API.
public final class DefaultNetworkRepository extends NetworkRepository {

    private final MirrorNodeRest rest;

    /// Creates the repository on `rest`.
    ///
    /// @param rest the calls against the mirror node
    /// @throws NullPointerException if `rest` is `null`
    public DefaultNetworkRepository(final MirrorNodeRest rest) {
        this.rest = Objects.requireNonNull(rest, "rest must not be null");
    }

    @Override
    public CompletionStage<@Nullable ExchangeRates> exchangeRates() {
        return exchangeRates(List.of());
    }

    @Override
    public CompletionStage<@Nullable ExchangeRates> exchangeRates(final List<String> timestamp) {
        final String path = Query.of("/network/exchangerate").addAll("timestamp", timestamp).build();
        return rest.getAndMap(path, json -> new ExchangeRates(exchangeRate(json.object("current_rate")),
                exchangeRate(json.object("next_rate")), Values.timestamp(json.optString("timestamp"))));
    }

    @Override
    public CompletionStage<NetworkFees> fees() {
        return fees(null, List.of());
    }

    @Override
    public CompletionStage<NetworkFees> fees(final @Nullable Order order, final List<String> timestamp) {
        final String path = Query.of("/network/fees").add("order", order).addAll("timestamp", timestamp).build();
        return rest.getAndMap(path, json -> new NetworkFees(json.objects("fees").stream()
                        .map(fee -> new NetworkFee(fee.longValue("gas"), fee.string("transaction_type")))
                        .toList(), Values.timestamp(json.optString("timestamp"))))
                .thenApply(fees -> fees == null ? new NetworkFees(List.of(), null) : fees);
    }

    @Override
    public CompletionStage<@Nullable NetworkStake> stake() {
        return rest.getAndMap("/network/stake", json -> new NetworkStake(
                json.longValue("max_stake_rewarded"),
                json.longValue("max_staking_reward_rate_per_hbar"),
                json.longValue("max_total_reward"),
                json.doubleValue("node_reward_fee_fraction"),
                json.longValue("reserved_staking_rewards"),
                json.longValue("reward_balance_threshold"),
                json.longValue("stake_total"),
                Values.range(json, "staking_period"),
                // the REST API gives the length of a staking period in minutes
                Duration.ofMinutes(json.longValue("staking_period_duration")),
                json.longValue("staking_periods_stored"),
                json.doubleValue("staking_reward_fee_fraction"),
                json.longValue("staking_reward_rate"),
                json.longValue("staking_start_threshold"),
                json.longValue("unreserved_staking_reward_balance")));
    }

    @Override
    public CompletionStage<@Nullable NetworkSupplies> supplies() {
        return supplies(List.of());
    }

    @Override
    public CompletionStage<@Nullable NetworkSupplies> supplies(final List<String> timestamp) {
        final String path = Query.of("/network/supply").addAll("timestamp", timestamp).build();
        return rest.getAndMap(path, json -> new NetworkSupplies(integer(json, "released_supply"),
                integer(json, "total_supply"), Values.timestamp(json.optString("timestamp"))));
    }

    @Override
    public CompletionStage<Page<NetworkNode>> nodes() {
        return nodes(List.of(), null, List.of(), null);
    }

    @Override
    public CompletionStage<Page<NetworkNode>> nodes(final List<String> fileId, final @Nullable Integer limit,
            final List<String> nodeId, final @Nullable Order order) {
        final String path = Query.of("/network/nodes").addAll("file.id", fileId).add("limit", limit)
                .addAll("node.id", nodeId).add("order", order).build();
        return rest.page(path, "nodes", DefaultNetworkRepository::networkNode);
    }

    @Override
    public CompletionStage<Page<RegisteredNode>> registeredNodes() {
        return registeredNodes(null, null, List.of(), null);
    }

    @Override
    public CompletionStage<Page<RegisteredNode>> registeredNodes(final @Nullable Integer limit,
            final @Nullable Order order, final List<String> registeredNodeId, final @Nullable RegisteredNodeType type) {
        final String path = Query.of("/network/registered-nodes").add("limit", limit).add("order", order)
                .addAll("registerednode.id", registeredNodeId).add("type", type).build();
        return rest.page(path, "registered_nodes", DefaultNetworkRepository::registeredNode);
    }

    @Override
    public CompletionStage<FeeEstimate> estimateFees(final byte[] transaction) {
        return estimateFees(transaction, null, null);
    }

    @Override
    public CompletionStage<FeeEstimate> estimateFees(final byte[] transaction, final @Nullable FeeEstimateMode mode,
            final @Nullable Integer highVolumeThrottle) {
        Objects.requireNonNull(transaction, "transaction must not be null");
        final String path = Query.of("/network/fees").add("mode", mode)
                .add("high_volume_throttle", highVolumeThrottle).build();
        return rest.post(path, transaction, "application/protobuf").thenApply(json -> {
            final JsonObject network = json.object("network");
            return new FeeEstimate(json.longValue("high_volume_multiplier"),
                    new FeeEstimateNetwork(Math.toIntExact(network.longValue("multiplier")),
                            network.longValue("subtotal")),
                    feeComponent(json.object("node")), feeComponent(json.object("service")),
                    json.longValue("total"));
        });
    }

    private static FeeEstimateComponent feeComponent(final JsonObject json) {
        return new FeeEstimateComponent(json.longValue("base"), json.objects("extras").stream()
                .map(extra -> new FeeExtra(extra.longValue("charged"), extra.longValue("count"),
                        extra.longValue("fee_per_unit"), extra.longValue("included"), extra.string("name"),
                        extra.longValue("subtotal")))
                .toList());
    }

    private static ExchangeRate exchangeRate(final JsonObject json) {
        return new ExchangeRate(Math.toIntExact(json.longValue("cent_equivalent")),
                Math.toIntExact(json.longValue("hbar_equivalent")),
                Values.epochSeconds(json.longValue("expiration_time")));
    }

    private static NetworkNode networkNode(final JsonObject json) {
        final JsonObject proxy = json.optObject("grpc_proxy_endpoint");
        return new NetworkNode(
                json.longValue("node_id"),
                Values.account(json, "node_account_id"),
                Values.authority(json.optObject("admin_key")),
                longs(json, "associated_registered_nodes"),
                json.optBoolean("decline_reward"),
                json.optString("description"),
                Values.optAddress(json.optString("file_id")),
                proxy == null ? null : serviceEndpoint(proxy),
                json.optLong("max_stake"),
                json.optString("memo"),
                json.optLong("min_stake"),
                json.optString("node_cert_hash"),
                json.optString("public_key"),
                json.optLong("reward_rate_start"),
                json.objects("service_endpoints").stream().map(DefaultNetworkRepository::serviceEndpoint).toList(),
                json.optLong("stake"),
                json.optLong("stake_not_rewarded"),
                json.optLong("stake_rewarded"),
                Values.optRange(json, "staking_period"),
                Values.range(json, "timestamp"));
    }

    private static RegisteredNode registeredNode(final JsonObject json) {
        return new RegisteredNode(
                json.longValue("registered_node_id"),
                Values.authority(json.optObject("admin_key")),
                Values.timestamp(json.optString("created_timestamp")),
                json.optString("description"),
                json.objects("service_endpoints").stream()
                        .map(DefaultNetworkRepository::registeredServiceEndpoint).toList(),
                Values.range(json, "timestamp"));
    }

    private static RegisteredServiceEndpoint registeredServiceEndpoint(final JsonObject json) {
        final JsonObject blockNode = json.optObject("block_node");
        final JsonObject generalService = json.optObject("general_service");
        return new RegisteredServiceEndpoint(
                blockNode == null ? List.of() : strings(blockNode, "endpoint_apis").stream()
                        .map(RegisteredBlockNodeApi::valueOf).toList(),
                json.optString("domain_name"),
                generalService == null ? null : generalService.optString("description"),
                json.optString("ip_address"),
                Math.toIntExact(json.longValue("port")),
                json.bool("requires_tls", false),
                RegisteredNodeType.valueOf(json.string("type")));
    }

    private static ServiceEndpoint serviceEndpoint(final JsonObject json) {
        return new ServiceEndpoint(emptyToNull(json.optString("ip_address_v4")),
                Math.toIntExact(json.longValue("port")), emptyToNull(json.optString("domain_name")));
    }

    private static List<Long> longs(final JsonObject json, final String name) {
        return json.opt(name) instanceof List<?> values
                ? values.stream().map(value -> ((Number) value).longValue()).toList()
                : List.of();
    }

    private static List<String> strings(final JsonObject json, final String name) {
        return json.opt(name) instanceof List<?> values ? values.stream().map(String::valueOf).toList() : List.of();
    }

    private static @Nullable String emptyToNull(final @Nullable String value) {
        return value == null || value.isEmpty() ? null : value;
    }

    private static BigInteger integer(final JsonObject json, final String name) {
        final BigInteger value = json.optInteger(name);
        if (value == null) {
            throw new IllegalArgumentException("member '" + name + "' is missing");
        }
        return value;
    }
}
