package org.hiero.mirrornode.internal;

import java.time.Duration;
import java.util.Objects;
import java.util.concurrent.CompletionStage;
import org.hiero.common.Page;
import org.hiero.ledger.AccountId;
import org.hiero.mirrornode.account.AccountBalance;
import org.hiero.mirrornode.account.AccountInfo;
import org.hiero.mirrornode.account.AccountRepository;
import org.hiero.mirrornode.account.CryptoAllowance;
import org.hiero.mirrornode.account.NftAllowance;
import org.hiero.mirrornode.account.StakingReward;
import org.hiero.mirrornode.account.TokenAirdrop;
import org.hiero.mirrornode.account.TokenAllowance;
import org.hiero.mirrornode.account.TokenBalance;
import org.hiero.mirrornode.internal.Json.JsonObject;
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
    public CompletionStage<Page<AccountInfo>> findAll() {
        return rest.page("/accounts?transactions=false", "accounts", DefaultAccountRepository::accountInfo);
    }

    @Override
    public CompletionStage<Page<StakingReward>> getRewards(final AccountId accountId) {
        Objects.requireNonNull(accountId, "accountId must not be null");
        return rest.page("/accounts/" + accountId + "/rewards", "rewards", json -> new StakingReward(
                Values.account(json, "account_id"), json.longValue("amount"),
                Values.requiredTimestamp(json, "timestamp")));
    }

    @Override
    public CompletionStage<Page<CryptoAllowance>> getCryptoAllowances(final AccountId accountId) {
        Objects.requireNonNull(accountId, "accountId must not be null");
        return rest.page("/accounts/" + accountId + "/allowances/crypto", "allowances", json -> new CryptoAllowance(
                Values.account(json, "owner"), Values.account(json, "spender"), json.longValue("amount"),
                json.longValue("amount_granted"), Values.rangeFrom(json, "timestamp")));
    }

    @Override
    public CompletionStage<Page<TokenAllowance>> getTokenAllowances(final AccountId accountId) {
        Objects.requireNonNull(accountId, "accountId must not be null");
        return rest.page("/accounts/" + accountId + "/allowances/tokens", "allowances", json -> new TokenAllowance(
                Values.account(json, "owner"), Values.account(json, "spender"), Values.address(json, "token_id"),
                json.longValue("amount"), json.longValue("amount_granted"), Values.rangeFrom(json, "timestamp")));
    }

    @Override
    public CompletionStage<Page<NftAllowance>> getNftAllowances(final AccountId accountId) {
        Objects.requireNonNull(accountId, "accountId must not be null");
        return rest.page("/accounts/" + accountId + "/allowances/nfts", "allowances", json -> new NftAllowance(
                Values.account(json, "owner"), Values.account(json, "spender"), Values.address(json, "token_id"),
                json.bool("approved_for_all", false), Values.rangeFrom(json, "timestamp")));
    }

    @Override
    public CompletionStage<Page<TokenAirdrop>> getOutstandingAirdrops(final AccountId accountId) {
        Objects.requireNonNull(accountId, "accountId must not be null");
        return rest.page("/accounts/" + accountId + "/airdrops/outstanding", "airdrops",
                DefaultAccountRepository::tokenAirdrop);
    }

    @Override
    public CompletionStage<Page<TokenAirdrop>> getPendingAirdrops(final AccountId accountId) {
        Objects.requireNonNull(accountId, "accountId must not be null");
        return rest.page("/accounts/" + accountId + "/airdrops/pending", "airdrops",
                DefaultAccountRepository::tokenAirdrop);
    }

    @Override
    public CompletionStage<Page<AccountBalance>> getBalances() {
        return rest.page("/balances", "balances", json -> new AccountBalance(Values.account(json, "account"),
                json.longValue("balance"), json.objects("tokens").stream()
                        .map(token -> new TokenBalance(Values.address(token, "token_id"), token.longValue("balance")))
                        .toList()));
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
                Values.timestamp(json.optString("stake_period_start")));
    }

    private static TokenAirdrop tokenAirdrop(final JsonObject json) {
        // exactly one of amount (fungible) and serial number (NFT) is set; the REST API also returns an amount for
        // an NFT airdrop
        final Long serialNumber = json.optLong("serial_number");
        return new TokenAirdrop(Values.account(json, "sender_id"), Values.account(json, "receiver_id"),
                Values.address(json, "token_id"), serialNumber == null ? json.optLong("amount") : null, serialNumber,
                Values.rangeFrom(json, "timestamp"));
    }

    private static long orZero(final @Nullable Long value) {
        return value == null ? 0L : value;
    }
}
