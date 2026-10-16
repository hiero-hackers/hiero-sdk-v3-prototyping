package org.hiero.consensusnode.transactions.topics;

import java.time.Duration;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletionStage;
import org.hiero.authority.Authority;
import org.hiero.consensusnode.client.Account;
import org.hiero.consensusnode.client.HieroClient;
import org.hiero.consensusnode.client.TransactionSigner;
import org.hiero.consensusnode.transactions.PackedTransaction;
import org.hiero.consensusnode.transactions.Response;
import org.hiero.consensusnode.transactions.Transaction;
import org.hiero.ledger.AccountId;
import org.hiero.ledger.Address;
import org.hiero.nativeToken.NativeToken;
import org.jspecify.annotations.Nullable;

/// Updates one or more of a topic's mutable fields. Every nullable field is "leave unchanged"
/// when null. Requires the topic's current adminAuthority to sign; an immutable topic (no adminAuthority)
/// cannot be updated.
public final class TopicUpdateTransaction extends Transaction<TopicUpdateReceipt, TopicUpdateTransaction> {

    private final Address topicId;
    private final @Nullable Authority adminAuthority;
    private final @Nullable Authority submitAuthority;
    private final @Nullable String topicMemo;
    private final @Nullable ZonedDateTime expirationTime;
    private final @Nullable Duration autoRenewPeriod;
    private final @Nullable AccountId autoRenewAccount;

    /// Creates a new `TopicUpdateTransaction`.
    ///
    /// @throws NullPointerException if a required value is `null`
    public TopicUpdateTransaction(
            final Address topicId,
            final @Nullable Authority adminAuthority,
            final @Nullable Authority submitAuthority,
            final @Nullable String topicMemo,
            final @Nullable ZonedDateTime expirationTime,
            final @Nullable Duration autoRenewPeriod,
            final @Nullable AccountId autoRenewAccount) {
        super();
        Objects.requireNonNull(topicId, "topicId must not be null");
        this.topicId = topicId;
        this.adminAuthority = adminAuthority;
        this.submitAuthority = submitAuthority;
        this.topicMemo = topicMemo;
        this.expirationTime = expirationTime;
        this.autoRenewPeriod = autoRenewPeriod;
        this.autoRenewAccount = autoRenewAccount;
    }

    /// Returns the `topicId`.
    public Address topicId() {
        return topicId;
    }

    /// when set, replaces the admin key; the new key must also sign
    public @Nullable Authority adminAuthority() {
        return adminAuthority;
    }

    /// when set, replaces the submit key
    public @Nullable Authority submitAuthority() {
        return submitAuthority;
    }

    /// Returns the `topicMemo`.
    public @Nullable String topicMemo() {
        return topicMemo;
    }

    /// Returns the `expirationTime`.
    public @Nullable ZonedDateTime expirationTime() {
        return expirationTime;
    }

    /// Returns the `autoRenewPeriod`.
    public @Nullable Duration autoRenewPeriod() {
        return autoRenewPeriod;
    }

    /// when set, the new account must also sign
    public @Nullable AccountId autoRenewAccount() {
        return autoRenewAccount;
    }

    @Override
    public PackedTransaction<TopicUpdateReceipt, TopicUpdateTransaction> pack(final Account payer, final List<AccountId> nodes) {
        throw new UnsupportedOperationException("Not implemented yet: TopicUpdateTransaction.pack");
    }

    @Override
    public PackedTransaction<TopicUpdateReceipt, TopicUpdateTransaction> signWithOperator(final HieroClient<?> client) {
        throw new UnsupportedOperationException("Not implemented yet: TopicUpdateTransaction.signWithOperator");
    }

    @Override
    public PackedTransaction<TopicUpdateReceipt, TopicUpdateTransaction> sign(final Account payer, final List<AccountId> nodes) {
        throw new UnsupportedOperationException("Not implemented yet: TopicUpdateTransaction.sign");
    }

    @Override
    public PackedTransaction<TopicUpdateReceipt, TopicUpdateTransaction> sign(final AccountId payerId, final TransactionSigner signer, final List<AccountId> nodes) {
        throw new UnsupportedOperationException("Not implemented yet: TopicUpdateTransaction.sign");
    }

    @Override
    public CompletionStage<Response<TopicUpdateReceipt>> signWithOperatorAndSubmit(final HieroClient<?> client) {
        throw new UnsupportedOperationException("Not implemented yet: TopicUpdateTransaction.signWithOperatorAndSubmit");
    }

    /// Packs this transaction as the *inner* transaction of a BatchTransaction (HIP-551), without
    /// signing it. Unlike pack(payer, nodes), this produces a single TransactionBody addressed to no
    /// consensus node (nodeAccountID = 0.0.0) instead of one body per target node — an inner batch
    /// transaction is never submitted to a node on its own; it is embedded into
    /// BatchTransaction.innerTransactions and executed by the network as part of the batch. The
    /// returned PackedTransaction still carries its own TransactionId (payer + validStart); further
    /// signatures use PackedTransaction.sign(...) / signableBodies().
    ///
    /// batchKey names the Authority that must sign the *outer* BatchTransaction for this inner
    /// transaction to execute — the inner author's controlled opt-in to being batched. Whether a
    /// transaction type may be batched at all is decided by the network.
    @Override
    public PackedTransaction<TopicUpdateReceipt, TopicUpdateTransaction> packForBatch(final Account payer, final Authority batchKey) {
        throw new UnsupportedOperationException("Not implemented yet: TopicUpdateTransaction.packForBatch");
    }

    /// Packs this transaction for a batch with the client's operator as payer and adds the operator's
    /// signature. The batch counterpart of signWithOperator(client).
    @Override
    public PackedTransaction<TopicUpdateReceipt, TopicUpdateTransaction> signForBatchWithOperator(final HieroClient<?> client, final Authority batchKey) {
        throw new UnsupportedOperationException("Not implemented yet: TopicUpdateTransaction.signForBatchWithOperator");
    }

    /// Packs this transaction for a batch with the given Account as payer and adds its signature. The
    /// batch counterpart of sign(payer, nodes).
    @Override
    public PackedTransaction<TopicUpdateReceipt, TopicUpdateTransaction> signForBatch(final Account payer, final Authority batchKey) {
        throw new UnsupportedOperationException("Not implemented yet: TopicUpdateTransaction.signForBatch");
    }

    /// Packs this transaction for a batch with payerId as payer and signs it with the given
    /// TransactionSigner (HSM, hardware wallet, paymaster). The batch counterpart of
    /// sign(payerId, signer, nodes).
    @Override
    public PackedTransaction<TopicUpdateReceipt, TopicUpdateTransaction> signForBatch(final AccountId payerId, final TransactionSigner signer, final Authority batchKey) {
        throw new UnsupportedOperationException("Not implemented yet: TopicUpdateTransaction.signForBatch");
    }

    /// Returns a builder for a new `TopicUpdateTransaction`.
    ///
    /// @return the builder
    public static Builder builder() {
        return new Builder();
    }

    /// The builder of `TopicUpdateTransaction`.
    ///
    /// `build()` creates the `TopicUpdateTransaction` of the values set one by one.
    ///
    /// A value that is not set is `null`, or has its default value.
    public static final class Builder {

        private @Nullable Address topicId;
        private @Nullable Authority adminAuthority;
        private @Nullable Authority submitAuthority;
        private @Nullable String topicMemo;
        private @Nullable ZonedDateTime expirationTime;
        private @Nullable Duration autoRenewPeriod;
        private @Nullable AccountId autoRenewAccount;

        private Builder() {
            // created by TopicUpdateTransaction.builder()
        }

        /// Sets the `topicId`.
        ///
        /// @param topicId the value
        /// @return this builder
        public Builder topicId(final Address topicId) {
            this.topicId = topicId;
            return this;
        }

        /// Sets the `adminAuthority`.
        ///
        /// when set, replaces the admin key; the new key must also sign
        ///
        /// @param adminAuthority the value
        /// @return this builder
        public Builder adminAuthority(final @Nullable Authority adminAuthority) {
            this.adminAuthority = adminAuthority;
            return this;
        }

        /// Sets the `submitAuthority`.
        ///
        /// when set, replaces the submit key
        ///
        /// @param submitAuthority the value
        /// @return this builder
        public Builder submitAuthority(final @Nullable Authority submitAuthority) {
            this.submitAuthority = submitAuthority;
            return this;
        }

        /// Sets the `topicMemo`.
        ///
        /// @param topicMemo the value
        /// @return this builder
        public Builder topicMemo(final @Nullable String topicMemo) {
            this.topicMemo = topicMemo;
            return this;
        }

        /// Sets the `expirationTime`.
        ///
        /// @param expirationTime the value
        /// @return this builder
        public Builder expirationTime(final @Nullable ZonedDateTime expirationTime) {
            this.expirationTime = expirationTime;
            return this;
        }

        /// Sets the `autoRenewPeriod`.
        ///
        /// @param autoRenewPeriod the value
        /// @return this builder
        public Builder autoRenewPeriod(final @Nullable Duration autoRenewPeriod) {
            this.autoRenewPeriod = autoRenewPeriod;
            return this;
        }

        /// Sets the `autoRenewAccount`.
        ///
        /// when set, the new account must also sign
        ///
        /// @param autoRenewAccount the value
        /// @return this builder
        public Builder autoRenewAccount(final @Nullable AccountId autoRenewAccount) {
            this.autoRenewAccount = autoRenewAccount;
            return this;
        }

        /// Creates the `TopicUpdateTransaction` of the values set.
        ///
        /// @return the new `TopicUpdateTransaction`
        /// @throws NullPointerException if a required value is not set
        public TopicUpdateTransaction build() {
            return new TopicUpdateTransaction(
                    Objects.requireNonNull(topicId, "topicId must not be null"),
                    adminAuthority,
                    submitAuthority,
                    topicMemo,
                    expirationTime,
                    autoRenewPeriod,
                    autoRenewAccount);
        }
    }

    @Override
    public String toString() {
        return "TopicUpdateTransaction[maxTransactionFee=" + maxTransactionFee()
                + ", validDuration=" + validDuration()
                + ", memo=" + memo()
                + ", topicId=" + topicId()
                + ", adminAuthority=" + adminAuthority()
                + ", submitAuthority=" + submitAuthority()
                + ", topicMemo=" + topicMemo()
                + ", expirationTime=" + expirationTime()
                + ", autoRenewPeriod=" + autoRenewPeriod()
                + ", autoRenewAccount=" + autoRenewAccount()
                + "]";
    }
}
