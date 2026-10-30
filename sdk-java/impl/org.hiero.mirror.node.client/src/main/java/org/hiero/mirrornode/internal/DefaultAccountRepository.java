package org.hiero.mirrornode.internal;

import java.time.Duration;
import java.util.HexFormat;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.concurrent.CompletionStage;
import org.hiero.common.Page;
import org.hiero.ledger.AccountId;
import org.hiero.mirrornode.account.AccountBalance;
import org.hiero.mirrornode.account.AccountBalanceTransactions;
import org.hiero.mirrornode.account.AccountInfo;
import org.hiero.mirrornode.account.AccountRepository;
import org.hiero.mirrornode.account.CryptoAllowance;
import org.hiero.mirrornode.account.Hook;
import org.hiero.mirrornode.account.HookExtensionPoint;
import org.hiero.mirrornode.account.HookStorage;
import org.hiero.mirrornode.account.HookType;
import org.hiero.mirrornode.account.NftAllowance;
import org.hiero.mirrornode.account.StakingReward;
import org.hiero.mirrornode.account.TokenAirdrop;
import org.hiero.mirrornode.account.TokenAllowance;
import org.hiero.mirrornode.account.TokenBalance;
import org.hiero.mirrornode.account.TokenFreezeStatus;
import org.hiero.mirrornode.account.TokenKycStatus;
import org.hiero.mirrornode.account.TokenRelationship;
import org.hiero.mirrornode.common.Order;
import org.hiero.mirrornode.internal.Json.JsonObject;
import org.hiero.mirrornode.transaction.TransactionType;
import org.jspecify.annotations.Nullable;

/// The `AccountRepository` of the SDK, on the endpoints `/accounts` and `/balances` of the Mirror Node REST API.
public final class DefaultAccountRepository implements AccountRepository {

    private final MirrorNodeRest rest;

    /// Creates the repository on `rest`.
    ///
    /// @param rest the calls against the mirror node
    /// @throws NullPointerException if `rest` is `null`
    public DefaultAccountRepository(final MirrorNodeRest rest) {
        this.rest = Objects.requireNonNull(rest, "rest must not be null");
    }

    @Override
    public CompletionStage<@Nullable AccountInfo> findById(final AccountId accountId) {
        Objects.requireNonNull(accountId, "accountId must not be null");
        return rest.getAndMap("/accounts/" + accountId + "?transactions=false", DefaultAccountRepository::accountInfo);
    }

    @Override
    public CompletionStage<@Nullable AccountBalanceTransactions> findById(final AccountId accountId,
            final @Nullable Integer limit, final @Nullable Order order, final List<String> timestamp,
            final @Nullable TransactionType transactionType, final @Nullable Boolean transactions) {
        Objects.requireNonNull(accountId, "accountId must not be null");
        final String path = Query.of("/accounts/" + accountId).add("limit", limit).add("order", order)
                .addAll("timestamp", timestamp)
                // the REST API expects the protocol name, not the name of the Java constant
                .add("transactiontype", transactionType == null ? null : transactionType.protocolName())
                .add("transactions", transactions).build();
        return rest.getAndMap(path, json -> new AccountBalanceTransactions(accountInfo(json),
                json.objects("transactions").stream().map(DefaultTransactionRepository::transactionInfo).toList()));
    }

    @Override
    public CompletionStage<Page<AccountInfo>> findAll() {
        // keeps sending transactions=false, which the endpoint does not declare; the overload with all options sends
        // only the declared parameters
        return rest.page("/accounts?transactions=false", "accounts", DefaultAccountRepository::accountInfo);
    }

    @Override
    public CompletionStage<Page<AccountInfo>> findAll(final List<String> accountBalance, final List<String> accountId,
            final @Nullable String accountPublicKey, final @Nullable Boolean balance, final @Nullable Integer limit,
            final @Nullable Order order) {
        final String path = Query.of("/accounts").addAll("account.balance", accountBalance)
                .addAll("account.id", accountId).add("account.publickey", accountPublicKey).add("balance", balance)
                .add("limit", limit).add("order", order).build();
        return rest.page(path, "accounts", DefaultAccountRepository::accountInfo);
    }

    @Override
    public CompletionStage<Page<Hook>> getHooks(final AccountId accountId) {
        return getHooks(accountId, null, null, null);
    }

    @Override
    public CompletionStage<Page<Hook>> getHooks(final AccountId accountId, final @Nullable Long hookId,
            final @Nullable Integer limit, final @Nullable Order order) {
        Objects.requireNonNull(accountId, "accountId must not be null");
        final String path = Query.of("/accounts/" + accountId + "/hooks").add("hook.id", hookId).add("limit", limit)
                .add("order", order).build();
        return rest.page(path, "hooks", json -> new Hook(
                Values.authority(json.optObject("admin_key")),
                Values.address(json, "contract_id"),
                Values.timestamp(json.optString("created_timestamp")),
                json.bool("deleted", false),
                HookExtensionPoint.valueOf(json.string("extension_point")),
                json.longValue("hook_id"),
                Values.address(json, "owner_id"),
                Values.optRange(json, "timestamp_range"),
                HookType.valueOf(json.string("type"))));
    }

    @Override
    public CompletionStage<Page<HookStorage>> getHookStorage(final AccountId accountId, final long hookId) {
        return getHookStorage(accountId, hookId, List.of(), null, null, List.of());
    }

    @Override
    public CompletionStage<Page<HookStorage>> getHookStorage(final AccountId accountId, final long hookId,
            final List<String> key, final @Nullable Integer limit, final @Nullable Order order,
            final List<String> timestamp) {
        Objects.requireNonNull(accountId, "accountId must not be null");
        final String path = Query.of("/accounts/" + accountId + "/hooks/" + hookId + "/storage").addAll("key", key)
                .add("limit", limit).add("order", order).addAll("timestamp", timestamp).build();
        return rest.page(path, "storage", json -> new HookStorage(hex(json.string("key")),
                json.optString("value") == null ? null : hex(json.string("value")),
                Values.requiredTimestamp(json, "timestamp")));
    }

    @Override
    public CompletionStage<Page<StakingReward>> getRewards(final AccountId accountId) {
        return getRewards(accountId, null, null, List.of());
    }

    @Override
    public CompletionStage<Page<StakingReward>> getRewards(final AccountId accountId, final @Nullable Integer limit,
            final @Nullable Order order, final List<String> timestamp) {
        Objects.requireNonNull(accountId, "accountId must not be null");
        final String path = Query.of("/accounts/" + accountId + "/rewards").add("limit", limit).add("order", order)
                .addAll("timestamp", timestamp).build();
        return rest.page(path, "rewards", json -> new StakingReward(
                Values.account(json, "account_id"), json.longValue("amount"),
                Values.requiredTimestamp(json, "timestamp")));
    }

    @Override
    public CompletionStage<Page<TokenRelationship>> getTokenRelationships(final AccountId accountId) {
        return getTokenRelationships(accountId, null, null, List.of());
    }

    @Override
    public CompletionStage<Page<TokenRelationship>> getTokenRelationships(final AccountId accountId,
            final @Nullable Integer limit, final @Nullable Order order, final List<String> tokenId) {
        Objects.requireNonNull(accountId, "accountId must not be null");
        final String path = Query.of("/accounts/" + accountId + "/tokens").add("limit", limit).add("order", order)
                .addAll("token.id", tokenId).build();
        return rest.page(path, "tokens", json -> {
            final String freezeStatus = json.optString("freeze_status");
            return new TokenRelationship(
                    json.optBoolean("automatic_association"),
                    json.longValue("balance"),
                    Values.timestamp(json.optString("created_timestamp")),
                    json.optLong("decimals"),
                    freezeStatus == null ? null : TokenFreezeStatus.valueOf(freezeStatus),
                    TokenKycStatus.valueOf(json.string("kyc_status")),
                    Values.address(json, "token_id"));
        });
    }

    @Override
    public CompletionStage<Page<TokenAirdrop>> getOutstandingAirdrops(final AccountId accountId) {
        return getOutstandingAirdrops(accountId, null, null, List.of(), List.of(), List.of());
    }

    @Override
    public CompletionStage<Page<TokenAirdrop>> getOutstandingAirdrops(final AccountId accountId,
            final @Nullable Integer limit, final @Nullable Order order, final List<String> receiverId,
            final List<String> serialNumber, final List<String> tokenId) {
        Objects.requireNonNull(accountId, "accountId must not be null");
        final String path = Query.of("/accounts/" + accountId + "/airdrops/outstanding").add("limit", limit)
                .add("order", order).addAll("receiver.id", receiverId).addAll("serialnumber", serialNumber)
                .addAll("token.id", tokenId).build();
        return rest.page(path, "airdrops", DefaultAccountRepository::tokenAirdrop);
    }

    @Override
    public CompletionStage<Page<TokenAirdrop>> getPendingAirdrops(final AccountId accountId) {
        return getPendingAirdrops(accountId, null, null, List.of(), List.of(), List.of());
    }

    @Override
    public CompletionStage<Page<TokenAirdrop>> getPendingAirdrops(final AccountId accountId,
            final @Nullable Integer limit, final @Nullable Order order, final List<String> senderId,
            final List<String> serialNumber, final List<String> tokenId) {
        Objects.requireNonNull(accountId, "accountId must not be null");
        final String path = Query.of("/accounts/" + accountId + "/airdrops/pending").add("limit", limit)
                .add("order", order).addAll("sender.id", senderId).addAll("serialnumber", serialNumber)
                .addAll("token.id", tokenId).build();
        return rest.page(path, "airdrops", DefaultAccountRepository::tokenAirdrop);
    }

    @Override
    public CompletionStage<Page<CryptoAllowance>> getCryptoAllowances(final AccountId accountId) {
        return getCryptoAllowances(accountId, null, null, List.of());
    }

    @Override
    public CompletionStage<Page<CryptoAllowance>> getCryptoAllowances(final AccountId accountId,
            final @Nullable Integer limit, final @Nullable Order order, final List<String> spenderId) {
        Objects.requireNonNull(accountId, "accountId must not be null");
        final String path = Query.of("/accounts/" + accountId + "/allowances/crypto").add("limit", limit)
                .add("order", order).addAll("spender.id", spenderId).build();
        return rest.page(path, "allowances", json -> new CryptoAllowance(
                Values.account(json, "owner"), Values.account(json, "spender"), json.longValue("amount"),
                json.longValue("amount_granted"), Values.range(json, "timestamp")));
    }

    @Override
    public CompletionStage<Page<TokenAllowance>> getTokenAllowances(final AccountId accountId) {
        return getTokenAllowances(accountId, null, null, List.of(), List.of());
    }

    @Override
    public CompletionStage<Page<TokenAllowance>> getTokenAllowances(final AccountId accountId,
            final @Nullable Integer limit, final @Nullable Order order, final List<String> spenderId,
            final List<String> tokenId) {
        Objects.requireNonNull(accountId, "accountId must not be null");
        final String path = Query.of("/accounts/" + accountId + "/allowances/tokens").add("limit", limit)
                .add("order", order).addAll("spender.id", spenderId).addAll("token.id", tokenId).build();
        return rest.page(path, "allowances", json -> new TokenAllowance(
                Values.account(json, "owner"), Values.account(json, "spender"), Values.address(json, "token_id"),
                json.longValue("amount"), json.longValue("amount_granted"), Values.range(json, "timestamp")));
    }

    @Override
    public CompletionStage<Page<NftAllowance>> getNftAllowances(final AccountId accountId) {
        return getNftAllowances(accountId, null, null, List.of(), List.of(), null);
    }

    @Override
    public CompletionStage<Page<NftAllowance>> getNftAllowances(final AccountId account, final @Nullable Integer limit,
            final @Nullable Order order, final List<String> accountId, final List<String> tokenId,
            final @Nullable Boolean owner) {
        Objects.requireNonNull(account, "account must not be null");
        final String path = Query.of("/accounts/" + account + "/allowances/nfts").add("limit", limit)
                .add("order", order).addAll("account.id", accountId).addAll("token.id", tokenId).add("owner", owner)
                .build();
        return rest.page(path, "allowances", json -> new NftAllowance(
                Values.account(json, "owner"), Values.account(json, "spender"), Values.address(json, "token_id"),
                json.bool("approved_for_all", false), Values.range(json, "timestamp")));
    }

    @Override
    public CompletionStage<Page<AccountBalance>> getBalances() {
        return getBalances(null, List.of(), null, null, null, List.of());
    }

    @Override
    public CompletionStage<Page<AccountBalance>> getBalances(final @Nullable AccountId accountId,
            final List<String> accountBalance, final @Nullable String accountPublicKey, final @Nullable Integer limit,
            final @Nullable Order order, final List<String> timestamp) {
        final String path = Query.of("/balances").add("account.id", accountId).addAll("account.balance", accountBalance)
                .add("account.publickey", accountPublicKey).add("limit", limit).add("order", order)
                .addAll("timestamp", timestamp).build();
        return rest.page(path, "balances", json -> new AccountBalance(Values.account(json, "account"),
                json.longValue("balance"), tokenBalances(json.objects("tokens"))));
    }

    private static AccountInfo accountInfo(final JsonObject json) {
        final JsonObject balance = json.optObject("balance");
        final Long balanceValue = balance == null ? null : balance.optLong("balance");
        final Long autoRenewPeriod = json.optLong("auto_renew_period");
        final Long maxAssociations = json.optLong("max_automatic_token_associations");
        return new AccountInfo(
                Values.account(json, "account"),
                Values.optEvmAddress(json.optString("evm_address")),
                balanceValue == null ? 0L : balanceValue,
                balance == null ? null : Values.timestamp(balance.optString("timestamp")),
                balance == null ? List.of() : tokenBalances(balance.objects("tokens")),
                orZero(json.optLong("ethereum_nonce")),
                orZero(json.optLong("pending_reward")),
                Values.optBase32(json.optString("alias")),
                Values.authority(json.optObject("key")),
                json.optString("memo"),
                Values.timestamp(json.optString("created_timestamp")),
                Values.timestamp(json.optString("expiry_timestamp")),
                autoRenewPeriod == null ? null : Duration.ofSeconds(autoRenewPeriod),
                maxAssociations == null ? null : Math.toIntExact(maxAssociations),
                json.optBoolean("receiver_sig_required"),
                json.bool("decline_reward", false),
                json.bool("deleted", false),
                Values.optAccount(json.optString("staked_account_id")),
                json.optLong("staked_node_id"),
                Values.timestamp(json.optString("stake_period_start")),
                Values.optEvmAddress(json.optString("delegation_address")));
    }

    private static List<TokenBalance> tokenBalances(final List<JsonObject> tokens) {
        return tokens.stream()
                .map(token -> new TokenBalance(Values.address(token, "token_id"), token.longValue("balance")))
                .toList();
    }

    private static TokenAirdrop tokenAirdrop(final JsonObject json) {
        // exactly one of amount (fungible) and serial number (NFT) is set; the REST API also returns an amount for
        // an NFT airdrop
        final Long serialNumber = json.optLong("serial_number");
        return new TokenAirdrop(Values.account(json, "sender_id"), Values.account(json, "receiver_id"),
                Values.address(json, "token_id"), serialNumber == null ? json.optLong("amount") : null, serialNumber,
                Values.range(json, "timestamp"));
    }

    /// Decodes hex text with an optional `0x` prefix (the form of hook storage keys and values).
    private static byte[] hex(final String text) {
        final String digits = text.startsWith("0x") || text.startsWith("0X") ? text.substring(2) : text;
        return HexFormat.of().parseHex(digits.toLowerCase(Locale.ROOT));
    }

    private static long orZero(final @Nullable Long value) {
        return value == null ? 0L : value;
    }
}
