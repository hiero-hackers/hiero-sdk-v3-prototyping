package org.hiero.mirrornode.internal;

import java.math.BigInteger;
import java.time.Duration;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletionStage;
import org.hiero.common.Page;
import org.hiero.mirrornode.internal.Json.JsonObject;
import org.hiero.mirrornode.network.ExchangeRate;
import org.hiero.mirrornode.network.ExchangeRates;
import org.hiero.mirrornode.network.FeeEstimate;
import org.hiero.mirrornode.network.NetworkFee;
import org.hiero.mirrornode.network.NetworkNode;
import org.hiero.mirrornode.network.NetworkRepository;
import org.hiero.mirrornode.network.NetworkStake;
import org.hiero.mirrornode.network.NetworkSupplies;
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
        return rest.getAndMap("/network/exchangerate", json -> new ExchangeRates(
                exchangeRate(json.object("current_rate")), exchangeRate(json.object("next_rate"))));
    }

    @Override
    public CompletionStage<List<NetworkFee>> fees() {
        return rest.getAndMap("/network/fees", json -> json.objects("fees").stream()
                        .map(fee -> new NetworkFee(fee.longValue("gas"), fee.string("transaction_type")))
                        .toList())
                .thenApply(fees -> fees == null ? List.of() : fees);
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
        return rest.getAndMap("/network/supply", json -> new NetworkSupplies(
                integer(json, "released_supply"), integer(json, "total_supply")));
    }

    @Override
    public CompletionStage<Page<NetworkNode>> nodes() {
        return rest.page("/network/nodes", "nodes", DefaultNetworkRepository::networkNode);
    }

    @Override
    public CompletionStage<FeeEstimate> estimateFees(final byte[] transaction) {
        Objects.requireNonNull(transaction, "transaction must not be null");
        return rest.post("/network/fees", transaction, "application/protobuf")
                .thenApply(json -> new FeeEstimate(json.longValue("total")));
    }

    private static ExchangeRate exchangeRate(final JsonObject json) {
        return new ExchangeRate(Math.toIntExact(json.longValue("cent_equivalent")),
                Math.toIntExact(json.longValue("hbar_equivalent")),
                Values.epochSeconds(json.longValue("expiration_time")));
    }

    private static NetworkNode networkNode(final JsonObject json) {
        final Long stake = json.optLong("stake");
        final JsonObject range = json.optObject("timestamp");
        return new NetworkNode(
                json.longValue("node_id"),
                Values.account(json, "node_account_id"),
                json.optString("description"),
                json.optString("public_key"),
                json.optString("node_cert_hash"),
                json.objects("service_endpoints").stream().map(DefaultNetworkRepository::serviceEndpoint).toList(),
                Values.authority(json.optObject("admin_key")),
                stake == null ? 0L : stake,
                range == null ? null : Values.timestamp(range.optString("from")));
    }

    private static ServiceEndpoint serviceEndpoint(final JsonObject json) {
        return new ServiceEndpoint(emptyToNull(json.optString("ip_address_v4")),
                Math.toIntExact(json.longValue("port")), emptyToNull(json.optString("domain_name")));
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
