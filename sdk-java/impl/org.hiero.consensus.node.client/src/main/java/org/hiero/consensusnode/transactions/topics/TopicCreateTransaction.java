package org.hiero.consensusnode.transactions.topics;

import org.hiero.consensusnode.client.internal.TopicKinds;
import org.hiero.consensusnode.client.internal.Packing;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.List;
import java.util.concurrent.CompletionStage;
import org.hiero.authority.Authority;
import org.hiero.consensusnode.client.Account;
import org.hiero.consensusnode.client.HieroClient;
import org.hiero.consensusnode.client.TransactionSigner;
import org.hiero.consensusnode.transactions.PackedTransaction;
import org.hiero.consensusnode.transactions.Response;
import org.hiero.consensusnode.transactions.Transaction;
import org.hiero.ledger.AccountId;
import org.hiero.nativeToken.NativeToken;
import org.jspecify.annotations.Nullable;

/// Creates a new topic. Both keys are optional:
/// - adminAuthority unset  → topic is immutable (no update / delete possible)
/// - submitAuthority unset → topic is public (anyone may submit messages)
public final class TopicCreateTransaction extends Transaction<TopicCreateReceipt, TopicCreateTransaction> {

    private final @Nullable Authority adminAuthority;
    private final @Nullable Authority submitAuthority;
    private final @Nullable String topicMemo;
    private final @Nullable Duration autoRenewPeriod;
    private final @Nullable AccountId autoRenewAccount;

    /// Creates a new `TopicCreateTransaction`.
    ///
    /// @throws IllegalArgumentException if a value violates its constraints
    public TopicCreateTransaction(
            final @Nullable Authority adminAuthority,
            final @Nullable Authority submitAuthority,
            final @Nullable String topicMemo,
            final @Nullable Duration autoRenewPeriod,
            final @Nullable AccountId autoRenewAccount) {
        super();
        this.adminAuthority = adminAuthority;
        this.submitAuthority = submitAuthority;
        if (topicMemo != null && topicMemo.getBytes(StandardCharsets.UTF_8).length > 100) {
            throw new IllegalArgumentException("topicMemo must be at most 100 bytes long in UTF-8");
        }
        this.topicMemo = topicMemo;
        this.autoRenewPeriod = autoRenewPeriod;
        this.autoRenewAccount = autoRenewAccount;
    }

    /// controls update / delete; unset → immutable topic; must sign the create transaction when set (anti-spoofing)
    public @Nullable Authority adminAuthority() {
        return adminAuthority;
    }

    /// controls message submission; unset → public topic; does NOT need to sign creation
    public @Nullable Authority submitAuthority() {
        return submitAuthority;
    }

    /// short human-readable label
    public @Nullable String topicMemo() {
        return topicMemo;
    }

    /// protocol default applies when unset
    public @Nullable Duration autoRenewPeriod() {
        return autoRenewPeriod;
    }

    /// pays auto-renewal; must sign the create transaction if set
    public @Nullable AccountId autoRenewAccount() {
        return autoRenewAccount;
    }

    @Override
    public PackedTransaction<TopicCreateReceipt, TopicCreateTransaction> pack(final Account payer, final List<AccountId> nodes) {
        return new Packing<>(TopicKinds.TOPIC_CREATE).pack(this, payer, nodes);
    }

    @Override
    public PackedTransaction<TopicCreateReceipt, TopicCreateTransaction> signWithOperator(final HieroClient<?> client) {
        return new Packing<>(TopicKinds.TOPIC_CREATE).signWithOperator(this, client);
    }

    @Override
    public PackedTransaction<TopicCreateReceipt, TopicCreateTransaction> sign(final Account payer, final List<AccountId> nodes) {
        return new Packing<>(TopicKinds.TOPIC_CREATE).sign(this, payer, nodes);
    }

    @Override
    public PackedTransaction<TopicCreateReceipt, TopicCreateTransaction> sign(final AccountId payerId, final TransactionSigner signer, final List<AccountId> nodes) {
        return new Packing<>(TopicKinds.TOPIC_CREATE).sign(this, payerId, signer, nodes);
    }

    @Override
    public CompletionStage<Response<TopicCreateReceipt>> signWithOperatorAndSubmit(final HieroClient<?> client) {
        return new Packing<>(TopicKinds.TOPIC_CREATE).signWithOperatorAndSubmit(this, client);
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
    public PackedTransaction<TopicCreateReceipt, TopicCreateTransaction> packForBatch(final Account payer, final Authority batchKey) {
        return new Packing<>(TopicKinds.TOPIC_CREATE).packForBatch(this, payer, batchKey);
    }

    /// Packs this transaction for a batch with the client's operator as payer and adds the operator's
    /// signature. The batch counterpart of signWithOperator(client).
    @Override
    public PackedTransaction<TopicCreateReceipt, TopicCreateTransaction> signForBatchWithOperator(final HieroClient<?> client, final Authority batchKey) {
        return new Packing<>(TopicKinds.TOPIC_CREATE).signForBatchWithOperator(this, client, batchKey);
    }

    /// Packs this transaction for a batch with the given Account as payer and adds its signature. The
    /// batch counterpart of sign(payer, nodes).
    @Override
    public PackedTransaction<TopicCreateReceipt, TopicCreateTransaction> signForBatch(final Account payer, final Authority batchKey) {
        return new Packing<>(TopicKinds.TOPIC_CREATE).signForBatch(this, payer, batchKey);
    }

    /// Packs this transaction for a batch with payerId as payer and signs it with the given
    /// TransactionSigner (HSM, hardware wallet, paymaster). The batch counterpart of
    /// sign(payerId, signer, nodes).
    @Override
    public PackedTransaction<TopicCreateReceipt, TopicCreateTransaction> signForBatch(final AccountId payerId, final TransactionSigner signer, final Authority batchKey) {
        return new Packing<>(TopicKinds.TOPIC_CREATE).signForBatch(this, payerId, signer, batchKey);
    }

    /// Returns a builder for a new `TopicCreateTransaction`.
    ///
    /// @return the builder
    public static Builder builder() {
        return new Builder();
    }

    /// The builder of `TopicCreateTransaction`.
    ///
    /// `build()` creates the `TopicCreateTransaction` of the values set one by one.
    ///
    /// A value that is not set is `null`, or has its default value.
    public static final class Builder {

        private @Nullable Authority adminAuthority;
        private @Nullable Authority submitAuthority;
        private @Nullable String topicMemo;
        private @Nullable Duration autoRenewPeriod;
        private @Nullable AccountId autoRenewAccount;

        private Builder() {
            // created by TopicCreateTransaction.builder()
        }

        /// Sets the `adminAuthority`.
        ///
        /// controls update / delete; unset → immutable topic; must sign the create transaction when set (anti-spoofing)
        ///
        /// @param adminAuthority the value
        /// @return this builder
        public Builder adminAuthority(final @Nullable Authority adminAuthority) {
            this.adminAuthority = adminAuthority;
            return this;
        }

        /// Sets the `submitAuthority`.
        ///
        /// controls message submission; unset → public topic; does NOT need to sign creation
        ///
        /// @param submitAuthority the value
        /// @return this builder
        public Builder submitAuthority(final @Nullable Authority submitAuthority) {
            this.submitAuthority = submitAuthority;
            return this;
        }

        /// Sets the `topicMemo`.
        ///
        /// short human-readable label
        ///
        /// @param topicMemo the value
        /// @return this builder
        public Builder topicMemo(final @Nullable String topicMemo) {
            this.topicMemo = topicMemo;
            return this;
        }

        /// Sets the `autoRenewPeriod`.
        ///
        /// protocol default applies when unset
        ///
        /// @param autoRenewPeriod the value
        /// @return this builder
        public Builder autoRenewPeriod(final @Nullable Duration autoRenewPeriod) {
            this.autoRenewPeriod = autoRenewPeriod;
            return this;
        }

        /// Sets the `autoRenewAccount`.
        ///
        /// pays auto-renewal; must sign the create transaction if set
        ///
        /// @param autoRenewAccount the value
        /// @return this builder
        public Builder autoRenewAccount(final @Nullable AccountId autoRenewAccount) {
            this.autoRenewAccount = autoRenewAccount;
            return this;
        }

        /// Creates the `TopicCreateTransaction` of the values set.
        ///
        /// @return the new `TopicCreateTransaction`
        /// @throws IllegalArgumentException if a value violates its constraints
        public TopicCreateTransaction build() {
            return new TopicCreateTransaction(
                    adminAuthority,
                    submitAuthority,
                    topicMemo,
                    autoRenewPeriod,
                    autoRenewAccount);
        }
    }

    @Override
    public String toString() {
        return "TopicCreateTransaction[maxTransactionFee=" + maxTransactionFee()
                + ", validDuration=" + validDuration()
                + ", memo=" + memo()
                + ", adminAuthority=" + adminAuthority()
                + ", submitAuthority=" + submitAuthority()
                + ", topicMemo=" + topicMemo()
                + ", autoRenewPeriod=" + autoRenewPeriod()
                + ", autoRenewAccount=" + autoRenewAccount()
                + "]";
    }
}
