package org.hiero.mirrornode.account;

import java.util.List;
import java.util.concurrent.CompletionStage;
import org.hiero.common.Page;
import org.hiero.ledger.AccountId;
import org.hiero.mirrornode.common.Order;
import org.hiero.mirrornode.transaction.TransactionType;
import org.jspecify.annotations.Nullable;

public interface AccountRepository {

    /// The account, without its transactions.
    /// Maps to GET /api/v1/accounts/{idOrAliasOrEvmAddress}.
    ///
    /// The returned stage completes exceptionally with `MirrorNodeException` if the operation fails.
    CompletionStage<@Nullable AccountInfo> findById(final AccountId accountId);

    /// The account with its most recent transactions, filtered by consensus time and transaction type;
    /// `transactions` false leaves the transactions out.
    /// Maps to GET /api/v1/accounts/{idOrAliasOrEvmAddress}.
    ///
    /// The returned stage completes exceptionally with `MirrorNodeException` if the operation fails.
    CompletionStage<@Nullable AccountBalanceTransactions> findById(final AccountId accountId, final @Nullable Integer limit, final @Nullable Order order, final List<String> timestamp, final @Nullable TransactionType transactionType, final @Nullable Boolean transactions);

    /// Lists account entities known to the mirror node.
    /// Maps to GET /api/v1/accounts.
    ///
    /// The returned stage completes exceptionally with `MirrorNodeException` if the operation fails.
    CompletionStage<Page<AccountInfo>> findAll();

    /// Lists account entities known to the mirror node, filtered by balance, id and public key; `balance` false
    /// leaves the balances out.
    /// Maps to GET /api/v1/accounts.
    ///
    /// The returned stage completes exceptionally with `MirrorNodeException` if the operation fails.
    CompletionStage<Page<AccountInfo>> findAll(final List<String> accountBalance, final List<String> accountId, final @Nullable String accountPublicKey, final @Nullable Boolean balance, final @Nullable Integer limit, final @Nullable Order order);

    /// The hooks of the account.
    /// Maps to GET /api/v1/accounts/{idOrAliasOrEvmAddress}/hooks.
    ///
    /// The returned stage completes exceptionally with `MirrorNodeException` if the operation fails.
    CompletionStage<Page<Hook>> getHooks(final AccountId accountId);

    /// The hooks of the account, filtered by hook id.
    /// Maps to GET /api/v1/accounts/{idOrAliasOrEvmAddress}/hooks.
    ///
    /// The returned stage completes exceptionally with `MirrorNodeException` if the operation fails.
    CompletionStage<Page<Hook>> getHooks(final AccountId accountId, final @Nullable Long hookId, final @Nullable Integer limit, final @Nullable Order order);

    /// The storage slots of a hook of the account.
    /// Maps to GET /api/v1/accounts/{idOrAliasOrEvmAddress}/hooks/{hookId}/storage.
    ///
    /// The returned stage completes exceptionally with `MirrorNodeException` if the operation fails.
    CompletionStage<Page<HookStorage>> getHookStorage(final AccountId accountId, final long hookId);

    /// The storage slots of a hook of the account, filtered by key and consensus time.
    /// Maps to GET /api/v1/accounts/{idOrAliasOrEvmAddress}/hooks/{hookId}/storage.
    ///
    /// The returned stage completes exceptionally with `MirrorNodeException` if the operation fails.
    CompletionStage<Page<HookStorage>> getHookStorage(final AccountId accountId, final long hookId, final List<String> key, final @Nullable Integer limit, final @Nullable Order order, final List<String> timestamp);

    /// Staking-reward payouts received by the account.
    /// Maps to GET /api/v1/accounts/{idOrAliasOrEvmAddress}/rewards.
    ///
    /// The returned stage completes exceptionally with `MirrorNodeException` if the operation fails.
    CompletionStage<Page<StakingReward>> getRewards(final AccountId accountId);

    /// Staking-reward payouts received by the account, filtered by consensus time.
    /// Maps to GET /api/v1/accounts/{idOrAliasOrEvmAddress}/rewards.
    ///
    /// The returned stage completes exceptionally with `MirrorNodeException` if the operation fails.
    CompletionStage<Page<StakingReward>> getRewards(final AccountId accountId, final @Nullable Integer limit, final @Nullable Order order, final List<String> timestamp);

    /// The tokens the account is associated with.
    /// Maps to GET /api/v1/accounts/{idOrAliasOrEvmAddress}/tokens.
    ///
    /// The returned stage completes exceptionally with `MirrorNodeException` if the operation fails.
    CompletionStage<Page<TokenRelationship>> getTokenRelationships(final AccountId accountId);

    /// The tokens the account is associated with, filtered by token id.
    /// Maps to GET /api/v1/accounts/{idOrAliasOrEvmAddress}/tokens.
    ///
    /// The returned stage completes exceptionally with `MirrorNodeException` if the operation fails.
    CompletionStage<Page<TokenRelationship>> getTokenRelationships(final AccountId accountId, final @Nullable Integer limit, final @Nullable Order order, final List<String> tokenId);

    /// Pending airdrops the account has SENT that are not yet claimed (HIP-904).
    /// Maps to GET /api/v1/accounts/{idOrAliasOrEvmAddress}/airdrops/outstanding.
    ///
    /// The returned stage completes exceptionally with `MirrorNodeException` if the operation fails.
    CompletionStage<Page<TokenAirdrop>> getOutstandingAirdrops(final AccountId accountId);

    /// Pending airdrops the account has SENT that are not yet claimed (HIP-904), filtered by receiver, serial number
    /// and token.
    /// Maps to GET /api/v1/accounts/{idOrAliasOrEvmAddress}/airdrops/outstanding.
    ///
    /// The returned stage completes exceptionally with `MirrorNodeException` if the operation fails.
    CompletionStage<Page<TokenAirdrop>> getOutstandingAirdrops(final AccountId accountId, final @Nullable Integer limit, final @Nullable Order order, final List<String> receiverId, final List<String> serialNumber, final List<String> tokenId);

    /// Pending airdrops the account is to RECEIVE but has not yet claimed (HIP-904).
    /// Maps to GET /api/v1/accounts/{idOrAliasOrEvmAddress}/airdrops/pending.
    ///
    /// The returned stage completes exceptionally with `MirrorNodeException` if the operation fails.
    CompletionStage<Page<TokenAirdrop>> getPendingAirdrops(final AccountId accountId);

    /// Pending airdrops the account is to RECEIVE but has not yet claimed (HIP-904), filtered by sender, serial number
    /// and token.
    /// Maps to GET /api/v1/accounts/{idOrAliasOrEvmAddress}/airdrops/pending.
    ///
    /// The returned stage completes exceptionally with `MirrorNodeException` if the operation fails.
    CompletionStage<Page<TokenAirdrop>> getPendingAirdrops(final AccountId accountId, final @Nullable Integer limit, final @Nullable Order order, final List<String> senderId, final List<String> serialNumber, final List<String> tokenId);

    /// HBAR allowances the account has granted to spenders.
    /// Maps to GET /api/v1/accounts/{idOrAliasOrEvmAddress}/allowances/crypto.
    ///
    /// The returned stage completes exceptionally with `MirrorNodeException` if the operation fails.
    CompletionStage<Page<CryptoAllowance>> getCryptoAllowances(final AccountId accountId);

    /// HBAR allowances the account has granted, filtered by spender.
    /// Maps to GET /api/v1/accounts/{idOrAliasOrEvmAddress}/allowances/crypto.
    ///
    /// The returned stage completes exceptionally with `MirrorNodeException` if the operation fails.
    CompletionStage<Page<CryptoAllowance>> getCryptoAllowances(final AccountId accountId, final @Nullable Integer limit, final @Nullable Order order, final List<String> spenderId);

    /// Fungible-token allowances the account has granted.
    /// Maps to GET /api/v1/accounts/{idOrAliasOrEvmAddress}/allowances/tokens.
    ///
    /// The returned stage completes exceptionally with `MirrorNodeException` if the operation fails.
    CompletionStage<Page<TokenAllowance>> getTokenAllowances(final AccountId accountId);

    /// Fungible-token allowances the account has granted, filtered by spender and token.
    /// Maps to GET /api/v1/accounts/{idOrAliasOrEvmAddress}/allowances/tokens.
    ///
    /// The returned stage completes exceptionally with `MirrorNodeException` if the operation fails.
    CompletionStage<Page<TokenAllowance>> getTokenAllowances(final AccountId accountId, final @Nullable Integer limit, final @Nullable Order order, final List<String> spenderId, final List<String> tokenId);

    /// NFT approved-for-all allowances the account has granted.
    /// Maps to GET /api/v1/accounts/{idOrAliasOrEvmAddress}/allowances/nfts.
    ///
    /// The returned stage completes exceptionally with `MirrorNodeException` if the operation fails.
    CompletionStage<Page<NftAllowance>> getNftAllowances(final AccountId accountId);

    /// NFT approved-for-all allowances, filtered by the other account and the token. With `owner` true or null the
    /// account is the owner and `accountId` filters the spenders; with `owner` false the account is the spender and
    /// `accountId` filters the owners.
    /// Maps to GET /api/v1/accounts/{idOrAliasOrEvmAddress}/allowances/nfts.
    ///
    /// The returned stage completes exceptionally with `MirrorNodeException` if the operation fails.
    CompletionStage<Page<NftAllowance>> getNftAllowances(final AccountId account, final @Nullable Integer limit, final @Nullable Order order, final List<String> accountId, final List<String> tokenId, final @Nullable Boolean owner);

    /// Account balances across the network (latest).
    /// Maps to the top-level GET /api/v1/balances — NOT /accounts/{id}/balances.
    ///
    /// The returned stage completes exceptionally with `MirrorNodeException` if the operation fails.
    CompletionStage<Page<AccountBalance>> getBalances();

    /// Account balances across the network, filtered by account, balance and public key, at the given consensus time.
    /// Maps to the top-level GET /api/v1/balances — NOT /accounts/{id}/balances.
    ///
    /// The returned stage completes exceptionally with `MirrorNodeException` if the operation fails.
    CompletionStage<Page<AccountBalance>> getBalances(final @Nullable AccountId accountId, final List<String> accountBalance, final @Nullable String accountPublicKey, final @Nullable Integer limit, final @Nullable Order order, final List<String> timestamp);
}
