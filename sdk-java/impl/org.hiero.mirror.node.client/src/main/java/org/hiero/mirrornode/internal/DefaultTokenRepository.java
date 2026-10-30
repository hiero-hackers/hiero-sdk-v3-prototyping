package org.hiero.mirrornode.internal;

import java.math.BigInteger;
import java.time.Duration;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletionStage;
import org.hiero.common.Page;
import org.hiero.ledger.AccountId;
import org.hiero.ledger.Address;
import org.hiero.mirrornode.common.FixedFee;
import org.hiero.mirrornode.common.Order;
import org.hiero.mirrornode.internal.Json.JsonObject;
import org.hiero.mirrornode.token.Balance;
import org.hiero.mirrornode.token.CustomFee;
import org.hiero.mirrornode.token.FractionalFee;
import org.hiero.mirrornode.token.RoyaltyFee;
import org.hiero.mirrornode.token.Token;
import org.hiero.mirrornode.token.TokenInfo;
import org.hiero.mirrornode.token.TokenPauseStatus;
import org.hiero.mirrornode.token.TokenRepository;
import org.hiero.token.TokenSupplyType;
import org.hiero.token.TokenType;
import org.jspecify.annotations.Nullable;

/// The `TokenRepository` of the SDK, on the endpoints `/tokens`, `/tokens/{id}` and `/tokens/{id}/balances` of the
/// Mirror Node REST API.
public final class DefaultTokenRepository implements TokenRepository {

    private final MirrorNodeRest rest;

    /// Creates the repository on `rest`.
    ///
    /// @param rest the calls against the mirror node
    /// @throws NullPointerException if `rest` is `null`
    public DefaultTokenRepository(final MirrorNodeRest rest) {
        this.rest = Objects.requireNonNull(rest, "rest must not be null");
    }

    @Override
    public CompletionStage<Page<Token>> findAll() {
        return findAll(List.of(), null, null, null, null, List.of(), List.of());
    }

    @Override
    public CompletionStage<Page<Token>> findAll(final List<String> accountId, final @Nullable Integer limit,
            final @Nullable String name, final @Nullable Order order, final @Nullable String publicKey,
            final List<String> tokenId, final List<TokenType> type) {
        Objects.requireNonNull(type, "type must not be null");
        final String path = Query.of("/tokens").addAll("account.id", accountId).add("limit", limit)
                .add("name", name).add("order", order).add("publickey", publicKey).addAll("token.id", tokenId)
                .addAll("type", type.stream().map(TokenType::name).toList()).build();
        return rest.page(path, "tokens", DefaultTokenRepository::token);
    }

    @Override
    public CompletionStage<Page<Token>> findByAccount(final AccountId accountId) {
        Objects.requireNonNull(accountId, "accountId must not be null");
        return findAll(List.of(accountId.toString()), null, null, null, null, List.of(), List.of());
    }

    @Override
    public CompletionStage<@Nullable TokenInfo> findById(final Address tokenId) {
        return findById(tokenId, List.of());
    }

    @Override
    public CompletionStage<@Nullable TokenInfo> findById(final Address tokenId, final List<String> timestamp) {
        Objects.requireNonNull(tokenId, "tokenId must not be null");
        final String path = Query.of("/tokens/" + tokenId).addAll("timestamp", timestamp).build();
        return rest.getAndMap(path, DefaultTokenRepository::tokenInfo);
    }

    @Override
    public CompletionStage<Page<Balance>> getBalances(final Address tokenId) {
        return getBalances(tokenId, List.of(), List.of(), null, null, null, List.of());
    }

    @Override
    public CompletionStage<Page<Balance>> getBalances(final Address tokenId, final List<String> accountBalance,
            final List<String> accountId, final @Nullable String accountPublicKey, final @Nullable Integer limit,
            final @Nullable Order order, final List<String> timestamp) {
        Objects.requireNonNull(tokenId, "tokenId must not be null");
        final String path = Query.of("/tokens/" + tokenId + "/balances").addAll("account.balance", accountBalance)
                .addAll("account.id", accountId).add("account.publickey", accountPublicKey).add("limit", limit)
                .add("order", order).addAll("timestamp", timestamp).build();
        return rest.page(path, "balances", DefaultTokenRepository::balance);
    }

    @Override
    public CompletionStage<Page<Balance>> getBalancesForAccount(final Address tokenId, final AccountId accountId) {
        Objects.requireNonNull(accountId, "accountId must not be null");
        return getBalances(tokenId, List.of(), List.of(accountId.toString()), null, null, null, List.of());
    }

    private static Token token(final JsonObject json) {
        return new Token(
                Values.optAddress(json.optString("token_id")),
                json.string("name"),
                json.string("symbol"),
                TokenType.valueOf(json.string("type")),
                json.longValue("decimals"),
                Values.optBase64(json.optString("metadata")),
                Values.authority(json.optObject("admin_key")));
    }

    private static Balance balance(final JsonObject json) {
        return new Balance(Values.account(json, "account"), json.longValue("balance"), json.optLong("decimals"));
    }

    private static TokenInfo tokenInfo(final JsonObject json) {
        final JsonObject fees = json.optObject("custom_fees");
        final Long autoRenewPeriod = json.optLong("auto_renew_period");
        final String pauseStatus = json.optString("pause_status");
        return new TokenInfo(
                Values.address(json, "token_id"),
                TokenType.valueOf(json.string("type")),
                json.string("name"),
                json.string("symbol"),
                json.optString("memo"),
                json.longValue("decimals"),
                Values.optBase64(json.optString("metadata")),
                Values.requiredTimestamp(json, "created_timestamp"),
                Values.requiredTimestamp(json, "modified_timestamp"),
                // the expiry is given in nanoseconds since the epoch, not as seconds.nanoseconds
                Values.epochNanoseconds(json.optLong("expiry_timestamp")),
                TokenSupplyType.valueOf(json.string("supply_type")),
                integer(json, "initial_supply"),
                integer(json, "total_supply"),
                integer(json, "max_supply"),
                Values.account(json, "treasury_account_id"),
                json.optBoolean("deleted"),
                fees == null ? new CustomFee(List.of(), List.of(), List.of(), null) : customFee(fees),
                Values.authority(json.optObject("admin_key")),
                Values.optAccount(json.optString("auto_renew_account")),
                autoRenewPeriod == null ? null : Duration.ofSeconds(autoRenewPeriod),
                Values.authority(json.optObject("fee_schedule_key")),
                json.bool("freeze_default", false),
                Values.authority(json.optObject("freeze_key")),
                Values.authority(json.optObject("kyc_key")),
                Values.authority(json.optObject("metadata_key")),
                Values.authority(json.optObject("pause_key")),
                pauseStatus == null ? TokenPauseStatus.NOT_APPLICABLE : TokenPauseStatus.valueOf(pauseStatus),
                Values.authority(json.optObject("supply_key")),
                Values.authority(json.optObject("wipe_key")));
    }

    private static CustomFee customFee(final JsonObject json) {
        return new CustomFee(
                json.objects("fixed_fees").stream().map(DefaultTokenRepository::fixedFee).toList(),
                json.objects("fractional_fees").stream().map(fee -> {
                    final JsonObject amount = fee.object("amount");
                    final Long minimum = fee.optLong("minimum");
                    return new FractionalFee(amount.longValue("numerator"), amount.longValue("denominator"),
                            Values.optAccount(fee.optString("collector_account_id")),
                            Values.optAddress(fee.optString("denominating_token_id")),
                            fee.bool("all_collectors_are_exempt", false),
                            fee.optLong("maximum"),
                            minimum == null ? 0L : minimum,
                            fee.bool("net_of_transfers", false));
                }).toList(),
                json.objects("royalty_fees").stream().map(fee -> {
                    final JsonObject amount = fee.object("amount");
                    final JsonObject fallback = fee.optObject("fallback_fee");
                    return new RoyaltyFee(amount.longValue("numerator"), amount.longValue("denominator"),
                            fallback == null ? null : fallback.optLong("amount"),
                            Values.optAccount(fee.optString("collector_account_id")),
                            fallback == null ? null : Values.optAddress(fallback.optString("denominating_token_id")),
                            fee.bool("all_collectors_are_exempt", false));
                }).toList(),
                Values.timestamp(json.optString("created_timestamp")));
    }

    /// Maps a fixed fee of the custom fees of a token or a topic.
    ///
    /// @param json the fee
    /// @return the fixed fee
    static FixedFee fixedFee(final JsonObject json) {
        return new FixedFee(json.bool("all_collectors_are_exempt", false), json.longValue("amount"), Values.optAccount(json.optString("collector_account_id")),
                Values.optAddress(json.optString("denominating_token_id")));
    }

    private static BigInteger integer(final JsonObject json, final String name) {
        final BigInteger value = json.optInteger(name);
        return value == null ? BigInteger.ZERO : value;
    }
}
