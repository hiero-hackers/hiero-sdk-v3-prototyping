package org.hiero.mirrornode.internal;

import java.time.Duration;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.concurrent.CompletionStage;
import org.hiero.common.Page;
import org.hiero.ledger.AccountId;
import org.hiero.ledger.TransactionId;
import org.hiero.mirrornode.common.NftTransfer;
import org.hiero.mirrornode.common.NftTransferParties;
import org.hiero.mirrornode.common.StakingRewardTransfer;
import org.hiero.mirrornode.common.TokenTransfer;
import org.hiero.mirrornode.common.Transfer;
import org.hiero.mirrornode.internal.Json.JsonObject;
import org.hiero.mirrornode.transaction.BalanceModification;
import org.hiero.mirrornode.transaction.BasicTransactionType;
import org.hiero.mirrornode.transaction.TransactionInfo;
import org.hiero.mirrornode.transaction.TransactionRepository;
import org.hiero.mirrornode.transaction.TransactionResult;
import org.hiero.mirrornode.transaction.TransactionType;
import org.jspecify.annotations.Nullable;

/// The `TransactionRepository` of the SDK, on the endpoint `/transactions` of the Mirror Node REST API.
///
/// A transaction type the SDK does not know is returned as an `UnknownTransactionType` with the received name. An NFT
/// transfer without sender (a mint) or without receiver (a burn) has no parties.
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
    public CompletionStage<Page<TransactionInfo>> findByAccount(final AccountId accountId) {
        Objects.requireNonNull(accountId, "accountId must not be null");
        return list("/transactions?account.id=" + accountId);
    }

    @Override
    public CompletionStage<Page<TransactionInfo>> findByAccountAndType(final AccountId accountId,
            final TransactionType type) {
        Objects.requireNonNull(accountId, "accountId must not be null");
        Objects.requireNonNull(type, "type must not be null");
        return list("/transactions?account.id=" + accountId + "&transactiontype=" + type.protocolName());
    }

    @Override
    public CompletionStage<Page<TransactionInfo>> findByAccountAndResult(final AccountId accountId,
            final TransactionResult result) {
        Objects.requireNonNull(accountId, "accountId must not be null");
        Objects.requireNonNull(result, "result must not be null");
        return list("/transactions?account.id=" + accountId + "&result=" + result.name().toLowerCase(Locale.ROOT));
    }

    @Override
    public CompletionStage<Page<TransactionInfo>> findByAccountAndModification(final AccountId accountId,
            final BalanceModification modification) {
        Objects.requireNonNull(accountId, "accountId must not be null");
        Objects.requireNonNull(modification, "modification must not be null");
        return list("/transactions?account.id=" + accountId + "&type="
                + modification.name().toLowerCase(Locale.ROOT));
    }

    @Override
    public CompletionStage<@Nullable TransactionInfo> findById(final TransactionId transactionId) {
        Objects.requireNonNull(transactionId, "transactionId must not be null");
        final int nonce = transactionId.nonce() == null ? 0 : transactionId.nonce();
        // the endpoint lists the transaction and its child transactions; the one with the nonce of the id is meant
        return rest.getAndMap("/transactions/" + Values.mirrorTransactionId(transactionId) + "?nonce=" + nonce,
                json -> {
                    final List<JsonObject> transactions = json.objects("transactions");
                    for (final JsonObject transaction : transactions) {
                        final Long transactionNonce = transaction.optLong("nonce");
                        if ((transactionNonce == null ? 0 : transactionNonce) == nonce
                                && !transaction.bool("scheduled", false)) {
                            return transactionInfo(transaction);
                        }
                    }
                    return transactions.isEmpty() ? null : transactionInfo(transactions.getFirst());
                });
    }

    private CompletionStage<Page<TransactionInfo>> list(final String path) {
        return rest.page(path, "transactions", DefaultTransactionRepository::transactionInfo);
    }

    private static TransactionInfo transactionInfo(final JsonObject json) {
        final Long nonce = json.optLong("nonce");
        final int nonceValue = nonce == null ? 0 : Math.toIntExact(nonce);
        final Long maxFee = json.optLong("max_fee");
        final Long validDuration = json.optLong("valid_duration_seconds");
        return new TransactionInfo(
                Values.transactionId(json.string("transaction_id"), nonceValue),
                Values.base64(json.optString("transaction_hash")),
                json.longValue("charged_tx_fee"),
                Values.requiredTimestamp(json, "consensus_timestamp"),
                json.optString("entity_id"),
                maxFee == null ? 0L : maxFee,
                Values.base64(json.optString("memo_base64")),
                transactionType(json.string("name")),
                nonceValue,
                json.optString("node"),
                Values.timestamp(json.optString("parent_consensus_timestamp")),
                "SUCCESS".equals(json.optString("result")) ? TransactionResult.SUCCESS : TransactionResult.FAIL,
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
                        Values.account(transfer, "account"), transfer.longValue("amount"))).toList());
    }

    private static NftTransfer nftTransfer(final JsonObject json) {
        final AccountId sender = Values.optAccount(json.optString("sender_account_id"));
        final AccountId receiver = Values.optAccount(json.optString("receiver_account_id"));
        final NftTransferParties parties = sender == null || receiver == null ? null
                : new NftTransferParties(sender, receiver, Values.address(json, "token_id"));
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
