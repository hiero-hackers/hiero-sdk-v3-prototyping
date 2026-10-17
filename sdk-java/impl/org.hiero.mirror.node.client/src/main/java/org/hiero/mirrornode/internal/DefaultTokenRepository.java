package org.hiero.mirrornode.internal;

import java.math.BigInteger;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletionStage;
import org.hiero.common.Page;
import org.hiero.ledger.AccountId;
import org.hiero.ledger.Address;
import org.hiero.mirrornode.common.FixedFee;
import org.hiero.mirrornode.internal.Json.JsonObject;
import org.hiero.mirrornode.token.Balance;
import org.hiero.mirrornode.token.CustomFee;
import org.hiero.mirrornode.token.FractionalFee;
import org.hiero.mirrornode.token.RoyaltyFee;
import org.hiero.mirrornode.token.Token;
import org.hiero.mirrornode.token.TokenInfo;
import org.hiero.mirrornode.token.TokenRepository;
import org.hiero.token.TokenSupplyType;
import org.hiero.token.TokenType;
import org.jspecify.annotations.Nullable;

/// The `TokenRepository` of the SDK, on the endpoints `/tokens` and `/tokens/{id}/balances` of the Mirror Node REST
/// API.
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
    public CompletionStage<Page<Token>> findByAccount(final AccountId accountId) {
        Objects.requireNonNull(accountId, "accountId must not be null");
        return rest.page("/tokens?account.id=" + accountId, "tokens", json -> new Token(
                Values.optAddress(json.optString("token_id")), json.string("name"), json.string("symbol"),
                TokenType.valueOf(json.string("type")), json.longValue("decimals"),
                Values.base64(json.optString("metadata"))));
    }

    @Override
    public CompletionStage<@Nullable TokenInfo> findById(final Address tokenId) {
        Objects.requireNonNull(tokenId, "tokenId must not be null");
        return rest.getAndMap("/tokens/" + tokenId, DefaultTokenRepository::tokenInfo);
    }

    @Override
    public CompletionStage<Page<Balance>> getBalances(final Address tokenId) {
        Objects.requireNonNull(tokenId, "tokenId must not be null");
        return rest.page("/tokens/" + tokenId + "/balances", "balances", DefaultTokenRepository::balance);
    }

    @Override
    public CompletionStage<Page<Balance>> getBalancesForAccount(final Address tokenId, final AccountId accountId) {
        Objects.requireNonNull(tokenId, "tokenId must not be null");
        Objects.requireNonNull(accountId, "accountId must not be null");
        return rest.page("/tokens/" + tokenId + "/balances?account.id=" + accountId, "balances",
                DefaultTokenRepository::balance);
    }

    private static Balance balance(final JsonObject json) {
        final Long decimals = json.optLong("decimals");
        return new Balance(Values.account(json, "account"), json.longValue("balance"),
                decimals == null ? 0L : decimals);
    }

    private static TokenInfo tokenInfo(final JsonObject json) {
        final JsonObject fees = json.optObject("custom_fees");
        return new TokenInfo(
                Values.address(json, "token_id"),
                TokenType.valueOf(json.string("type")),
                json.string("name"),
                json.string("symbol"),
                json.optString("memo"),
                json.longValue("decimals"),
                Values.base64(json.optString("metadata")),
                Values.requiredTimestamp(json, "created_timestamp"),
                Values.requiredTimestamp(json, "modified_timestamp"),
                // the expiry is given in nanoseconds since the epoch, not as seconds.nanoseconds
                Values.epochNanoseconds(json.optLong("expiry_timestamp")),
                TokenSupplyType.valueOf(json.string("supply_type")),
                integer(json, "initial_supply"),
                integer(json, "total_supply"),
                integer(json, "max_supply"),
                Values.account(json, "treasury_account_id"),
                json.bool("deleted", false),
                fees == null ? new CustomFee(List.of(), List.of(), List.of()) : customFee(fees));
    }

    private static CustomFee customFee(final JsonObject json) {
        return new CustomFee(
                json.objects("fixed_fees").stream().map(DefaultTokenRepository::fixedFee).toList(),
                json.objects("fractional_fees").stream().map(fee -> {
                    final JsonObject amount = fee.object("amount");
                    return new FractionalFee(amount.longValue("numerator"), amount.longValue("denominator"),
                            Values.optAccount(fee.optString("collector_account_id")),
                            Values.optAddress(fee.optString("denominating_token_id")));
                }).toList(),
                json.objects("royalty_fees").stream().map(fee -> {
                    final JsonObject amount = fee.object("amount");
                    final JsonObject fallback = fee.optObject("fallback_fee");
                    final Long fallbackAmount = fallback == null ? null : fallback.optLong("amount");
                    return new RoyaltyFee(amount.longValue("numerator"), amount.longValue("denominator"),
                            fallbackAmount == null ? 0L : fallbackAmount,
                            Values.optAccount(fee.optString("collector_account_id")),
                            fallback == null ? null : Values.optAddress(fallback.optString("denominating_token_id")));
                }).toList());
    }

    /// Maps a fixed fee of the custom fees of a token or a topic.
    ///
    /// @param json the fee
    /// @return the fixed fee
    static FixedFee fixedFee(final JsonObject json) {
        return new FixedFee(json.longValue("amount"), Values.optAccount(json.optString("collector_account_id")),
                Values.optAddress(json.optString("denominating_token_id")));
    }

    private static BigInteger integer(final JsonObject json, final String name) {
        final BigInteger value = json.optInteger(name);
        return value == null ? BigInteger.ZERO : value;
    }
}
