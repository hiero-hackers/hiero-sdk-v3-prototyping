package org.hiero.mirrornode.internal;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.concurrent.CompletionStage;
import org.hiero.common.Page;
import org.hiero.ledger.AccountId;
import org.hiero.ledger.TransactionId;
import org.hiero.mirrornode.common.NftTransfer;
import org.hiero.mirrornode.common.NftTransferParties;
import org.hiero.mirrornode.common.Order;
import org.hiero.mirrornode.common.StakingRewardTransfer;
import org.hiero.mirrornode.common.TokenTransfer;
import org.hiero.mirrornode.common.Transfer;
import org.hiero.mirrornode.internal.Json.JsonObject;
import org.hiero.mirrornode.transaction.AssessedCustomFee;
import org.hiero.mirrornode.transaction.BalanceModification;
import org.hiero.mirrornode.transaction.BasicTransactionType;
import org.hiero.mirrornode.transaction.CustomFeeLimit;
import org.hiero.mirrornode.transaction.TransactionInfo;
import org.hiero.mirrornode.transaction.TransactionRepository;
import org.hiero.mirrornode.transaction.TransactionResult;
import org.hiero.mirrornode.transaction.TransactionType;
import org.jspecify.annotations.Nullable;

/// The `TransactionRepository` of the SDK, on the endpoints `/transactions` and `/transactions/{transactionId}` of the
/// Mirror Node REST API.
///
/// A transaction type the SDK does not know is returned as an `UnknownTransactionType` with the received name. An NFT
/// transfer without sender (a mint) or without receiver (a burn) has parties without sender resp. receiver.
public final class DefaultTransactionRepository implements TransactionRepository {

    private final MirrorNodeRest rest;

    /// Creates the repository on `rest`.
    ///
    /// @param rest the calls against the mirror node
    /// @throws NullPointerException if `rest` is `null`
    public DefaultTransactionRepository(final MirrorNodeRest rest) {
        this.rest = Objects.requireNonNull(rest, "rest must not be null");
    }

    @Override
    public CompletionStage<Page<TransactionInfo>> findAll() {
        return findAll(List.of(), null, null, List.of(), null, null, null);
    }

    @Override
    public CompletionStage<Page<TransactionInfo>> findAll(final List<String> accountId, final @Nullable Integer limit,
            final @Nullable Order order, final List<String> timestamp, final @Nullable TransactionType transactionType,
            final @Nullable TransactionResult result, final @Nullable BalanceModification type) {
        final String path = Query.of("/transactions").addAll("account.id", accountId).add("limit", limit)
                .add("order", order).addAll("timestamp", timestamp)
                .add("transactiontype", transactionType == null ? null : transactionType.protocolName())
                .add("result", result == null ? null : result.name().toLowerCase(Locale.ROOT))
                .add("type", type == null ? null : type.name().toLowerCase(Locale.ROOT)).build();
        return list(path);
    }

    @Override
    public CompletionStage<Page<TransactionInfo>> findByAccount(final AccountId accountId) {
        Objects.requireNonNull(accountId, "accountId must not be null");
        return findAll(List.of(accountId.toString()), null, null, List.of(), null, null, null);
    }

    @Override
    public CompletionStage<Page<TransactionInfo>> findByAccountAndType(final AccountId accountId,
            final TransactionType type) {
        Objects.requireNonNull(accountId, "accountId must not be null");
        Objects.requireNonNull(type, "type must not be null");
        return findAll(List.of(accountId.toString()), null, null, List.of(), type, null, null);
    }

    @Override
    public CompletionStage<Page<TransactionInfo>> findByAccountAndResult(final AccountId accountId,
            final TransactionResult result) {
        Objects.requireNonNull(accountId, "accountId must not be null");
        Objects.requireNonNull(result, "result must not be null");
        return findAll(List.of(accountId.toString()), null, null, List.of(), null, result, null);
    }

    @Override
    public CompletionStage<Page<TransactionInfo>> findByAccountAndModification(final AccountId accountId,
            final BalanceModification modification) {
        Objects.requireNonNull(accountId, "accountId must not be null");
        Objects.requireNonNull(modification, "modification must not be null");
        return findAll(List.of(accountId.toString()), null, null, List.of(), null, null, modification);
    }

    @Override
    public CompletionStage<@Nullable TransactionInfo> findById(final TransactionId transactionId) {
        Objects.requireNonNull(transactionId, "transactionId must not be null");
        final int nonce = transactionId.nonce() == null ? 0 : transactionId.nonce();
        // the endpoint lists the transaction and its child transactions; the one with the nonce of the id is meant
        return findAllById(transactionId, nonce, null).thenApply(transactions -> {
            for (final TransactionInfo transaction : transactions) {
                if (transaction.nonce() == nonce && !transaction.scheduled()) {
                    return transaction;
                }
            }
            return transactions.isEmpty() ? null : transactions.getFirst();
        });
    }

    @Override
    public CompletionStage<List<TransactionInfo>> findAllById(final TransactionId transactionId) {
        return findAllById(transactionId, null, null);
    }

    @Override
    public CompletionStage<List<TransactionInfo>> findAllById(final TransactionId transactionId,
            final @Nullable Integer nonce, final @Nullable Boolean scheduled) {
        Objects.requireNonNull(transactionId, "transactionId must not be null");
        final String path = Query.of("/transactions/" + Values.mirrorTransactionId(transactionId))
                .add("nonce", nonce).add("scheduled", scheduled).build();
        return rest.getAndMap(path, json -> json.objects("transactions").stream()
                        .map(DefaultTransactionRepository::transactionInfo).toList())
                .thenApply(transactions -> transactions == null ? List.<TransactionInfo>of() : transactions);
    }

    private CompletionStage<Page<TransactionInfo>> list(final String path) {
        return rest.page(path, "transactions", DefaultTransactionRepository::transactionInfo);
    }

    static TransactionInfo transactionInfo(final JsonObject json) {
        final Long nonce = json.optLong("nonce");
        final int nonceValue = nonce == null ? 0 : Math.toIntExact(nonce);
        final Long maxFee = json.optLong("max_fee");
        final Long validDuration = json.optLong("valid_duration_seconds");
        return new TransactionInfo(
                Values.transactionId(json.string("transaction_id"), nonceValue),
                Values.base64(json.optString("transaction_hash")),
                Values.authority(json.optObject("batch_key")),
                Values.optBase64(json.optString("bytes")),
                json.longValue("charged_tx_fee"),
                Values.requiredTimestamp(json, "consensus_timestamp"),
                Values.optAddress(json.optString("entity_id")),
                json.optBoolean("high_volume"),
                json.optLong("high_volume_pricing_multiplier"),
                json.objects("max_custom_fees").stream().map(limit -> new CustomFeeLimit(
                        Values.optAccount(limit.optString("account_id")), limit.longValue("amount"),
                        Values.optAddress(limit.optString("denominating_token_id")))).toList(),
                maxFee == null ? 0L : maxFee,
                Values.base64(json.optString("memo_base64")),
                transactionType(json.string("name")),
                nonceValue,
                Values.optAccount(json.optString("node")),
                Values.timestamp(json.optString("parent_consensus_timestamp")),
                json.string("result"),
                json.bool("scheduled", false),
                Duration.ofSeconds(validDuration == null ? 0L : validDuration),
                Values.requiredTimestamp(json, "valid_start_timestamp"),
                json.objects("transfers").stream().map(transfer -> new Transfer(Values.account(transfer, "account"),
                        transfer.longValue("amount"), transfer.bool("is_approval", false))).toList(),
                json.objects("token_transfers").stream().map(transfer -> new TokenTransfer(
                        Values.address(transfer, "token_id"), Values.account(transfer, "account"),
                        transfer.longValue("amount"), transfer.bool("is_approval", false))).toList(),
                json.objects("nft_transfers").stream().map(DefaultTransactionRepository::nftTransfer).toList(),
                json.objects("staking_reward_transfers").stream().map(transfer -> new StakingRewardTransfer(
                        Values.account(transfer, "account"), transfer.longValue("amount"))).toList(),
                json.objects("assessed_custom_fees").stream().map(DefaultTransactionRepository::assessedCustomFee)
                        .toList());
    }

    private static AssessedCustomFee assessedCustomFee(final JsonObject json) {
        final List<AccountId> payers = new ArrayList<>();
        if (json.opt("effective_payer_account_ids") instanceof List<?> ids) {
            for (final Object id : ids) {
                if (id instanceof String text) {
                    payers.add(AccountId.fromString(text));
                }
            }
        }
        return new AssessedCustomFee(json.longValue("amount"),
                Values.optAccount(json.optString("collector_account_id")), payers,
                Values.optAddress(json.optString("token_id")));
    }

    private static NftTransfer nftTransfer(final JsonObject json) {
        final NftTransferParties parties = new NftTransferParties(
                Values.optAccount(json.optString("sender_account_id")),
                Values.optAccount(json.optString("receiver_account_id")), Values.address(json, "token_id"));
        return new NftTransfer(json.bool("is_approval", false), parties, json.longValue("serial_number"));
    }

    private static TransactionType transactionType(final String name) {
        for (final BasicTransactionType type : BasicTransactionType.values()) {
            if (type.protocolName().equals(name)) {
                return type;
            }
        }
        return new UnknownTransactionType(name);
    }

    /// A transaction type the SDK does not know, with the name the Mirror Node REST API uses for it.
    ///
    /// @param protocolName the REST API wire value
    public record UnknownTransactionType(String protocolName) implements TransactionType {

        /// Creates the type of `protocolName`.
        ///
        /// @throws NullPointerException if `protocolName` is `null`
        public UnknownTransactionType {
            Objects.requireNonNull(protocolName, "protocolName must not be null");
        }
    }
}
