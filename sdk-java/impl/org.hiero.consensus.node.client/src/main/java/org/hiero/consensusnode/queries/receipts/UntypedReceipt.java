package org.hiero.consensusnode.queries.receipts;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import org.hiero.consensusnode.client.internal.Receipts;
import org.hiero.consensusnode.transactions.Receipt;
import org.hiero.consensusnode.transactions.Transaction;
import org.hiero.consensusnode.status.TransactionStatus;
import org.hiero.ledger.AccountId;
import org.hiero.ledger.Address;
import org.hiero.ledger.ContractId;
import org.hiero.ledger.TransactionId;
import org.hiero.nativeToken.ExchangeRate;
import org.jspecify.annotations.Nullable;

/// A receipt whose transaction type is not known: every attribute the network reports for a transaction, each of them
/// optional. Use `typed(transactionType)` to get the receipt of a known transaction type.
public final class UntypedReceipt extends Receipt {

    private final @Nullable AccountId accountId;
    private final @Nullable Address fileId;
    private final @Nullable ContractId contractId;
    private final @Nullable Address topicId;
    private final @Nullable Long topicSequenceNumber;
    private final byte @Nullable [] topicRunningHash;
    private final @Nullable Long topicRunningHashVersion;
    private final @Nullable Address tokenId;
    private final @Nullable Long totalSupply;
    private final @Nullable Address scheduleId;
    private final @Nullable TransactionId scheduledTransactionId;
    private final List<Long> serials;
    private final @Nullable Long nodeId;
    private final @Nullable Long registeredNodeId;
    private final @Nullable Long blockNumber;

    /// Creates a new `UntypedReceipt`.
    ///
    /// @throws NullPointerException if a required value is `null`
    public UntypedReceipt(
            final TransactionId transactionId,
            final TransactionStatus status,
            final ExchangeRate exchangeRate,
            final ExchangeRate nextExchangeRate,
            final @Nullable AccountId accountId,
            final @Nullable Address fileId,
            final @Nullable ContractId contractId,
            final @Nullable Address topicId,
            final @Nullable Long topicSequenceNumber,
            final byte @Nullable [] topicRunningHash,
            final @Nullable Long topicRunningHashVersion,
            final @Nullable Address tokenId,
            final @Nullable Long totalSupply,
            final @Nullable Address scheduleId,
            final @Nullable TransactionId scheduledTransactionId,
            final List<Long> serials,
            final @Nullable Long nodeId,
            final @Nullable Long registeredNodeId,
            final @Nullable Long blockNumber) {
        super(transactionId, status, exchangeRate, nextExchangeRate);
        this.accountId = accountId;
        this.fileId = fileId;
        this.contractId = contractId;
        this.topicId = topicId;
        this.topicSequenceNumber = topicSequenceNumber;
        this.topicRunningHash = topicRunningHash == null ? null : topicRunningHash.clone();
        this.topicRunningHashVersion = topicRunningHashVersion;
        this.tokenId = tokenId;
        this.totalSupply = totalSupply;
        this.scheduleId = scheduleId;
        this.scheduledTransactionId = scheduledTransactionId;
        Objects.requireNonNull(serials, "serials must not be null");
        this.serials = List.copyOf(serials);
        this.nodeId = nodeId;
        this.registeredNodeId = registeredNodeId;
        this.blockNumber = blockNumber;
    }

    /// Creates a new `UntypedReceipt` with the default value of `serials`.
    public UntypedReceipt(
            final TransactionId transactionId,
            final TransactionStatus status,
            final ExchangeRate exchangeRate,
            final ExchangeRate nextExchangeRate,
            final @Nullable AccountId accountId,
            final @Nullable Address fileId,
            final @Nullable ContractId contractId,
            final @Nullable Address topicId,
            final @Nullable Long topicSequenceNumber,
            final byte @Nullable [] topicRunningHash,
            final @Nullable Long topicRunningHashVersion,
            final @Nullable Address tokenId,
            final @Nullable Long totalSupply,
            final @Nullable Address scheduleId,
            final @Nullable TransactionId scheduledTransactionId,
            final @Nullable Long nodeId,
            final @Nullable Long registeredNodeId,
            final @Nullable Long blockNumber) {
        this(transactionId, status, exchangeRate, nextExchangeRate, accountId, fileId, contractId, topicId, topicSequenceNumber, topicRunningHash, topicRunningHashVersion, tokenId, totalSupply, scheduleId, scheduledTransactionId, List.of(), nodeId, registeredNodeId, blockNumber);
    }

    /// the account created
    public @Nullable AccountId accountId() {
        return accountId;
    }

    /// the file created
    public @Nullable Address fileId() {
        return fileId;
    }

    /// the smart contract created
    public @Nullable ContractId contractId() {
        return contractId;
    }

    /// the topic created
    public @Nullable Address topicId() {
        return topicId;
    }

    /// the sequence number of a submitted topic message
    public @Nullable Long topicSequenceNumber() {
        return topicSequenceNumber;
    }

    /// the running hash of a topic after a submitted message
    public byte @Nullable [] topicRunningHash() {
        return topicRunningHash == null ? null : topicRunningHash.clone();
    }

    /// the version of the running hash algorithm
    public @Nullable Long topicRunningHashVersion() {
        return topicRunningHashVersion;
    }

    /// the token created
    public @Nullable Address tokenId() {
        return tokenId;
    }

    /// the total supply of a token after a mint, burn or wipe
    public @Nullable Long totalSupply() {
        return totalSupply;
    }

    /// the schedule created
    public @Nullable Address scheduleId() {
        return scheduleId;
    }

    /// the id of the transaction a schedule executes
    public @Nullable TransactionId scheduledTransactionId() {
        return scheduledTransactionId;
    }

    /// the serial numbers of the NFTs minted
    public List<Long> serials() {
        return serials;
    }

    /// the consensus node created
    public @Nullable Long nodeId() {
        return nodeId;
    }

    /// the registered node created
    public @Nullable Long registeredNodeId() {
        return registeredNodeId;
    }

    /// the number of the block the transaction is in
    public @Nullable Long blockNumber() {
        return blockNumber;
    }

    /// The receipt typed for the given transaction type, e.g. an `AccountCreateReceipt` for
    /// `AccountCreateTransaction`. Makes no network request. Fails when the receipt lacks an attribute the typed
    /// receipt requires, e.g. an `accountId` for an `AccountCreateReceipt`; a receipt can be typed for a transaction
    /// type it does not belong to when nothing is missing.
    ///
    /// @throws IllegalArgumentException if an invalid argument error occurs
    public <R extends Receipt> R typed(final Class<? extends Transaction<R, ?>> transactionType) {
        Objects.requireNonNull(transactionType, "transactionType must not be null");
        return Receipts.typed(this, transactionType);
    }

    /// Returns a builder for a new `UntypedReceipt`.
    ///
    /// @return the builder
    public static Builder builder() {
        return new Builder();
    }

    /// The builder of `UntypedReceipt`.
    ///
    /// `build()` creates the `UntypedReceipt` of the values set one by one.
    ///
    /// A value that is not set is `null`, or has its default value.
    public static final class Builder {

        private @Nullable TransactionId transactionId;
        private @Nullable TransactionStatus status;
        private @Nullable ExchangeRate exchangeRate;
        private @Nullable ExchangeRate nextExchangeRate;
        private @Nullable AccountId accountId;
        private @Nullable Address fileId;
        private @Nullable ContractId contractId;
        private @Nullable Address topicId;
        private @Nullable Long topicSequenceNumber;
        private byte @Nullable [] topicRunningHash;
        private @Nullable Long topicRunningHashVersion;
        private @Nullable Address tokenId;
        private @Nullable Long totalSupply;
        private @Nullable Address scheduleId;
        private @Nullable TransactionId scheduledTransactionId;
        private List<Long> serials = List.of();
        private @Nullable Long nodeId;
        private @Nullable Long registeredNodeId;
        private @Nullable Long blockNumber;

        private Builder() {
            // created by UntypedReceipt.builder()
        }

        /// Sets the `transactionId`.
        ///
        /// the id of the transaction
        ///
        /// @param transactionId the value
        /// @return this builder
        public Builder transactionId(final TransactionId transactionId) {
            this.transactionId = transactionId;
            return this;
        }

        /// Sets the `status`.
        ///
        /// the status of the transaction
        ///
        /// @param status the value
        /// @return this builder
        public Builder status(final TransactionStatus status) {
            this.status = status;
            return this;
        }

        /// Sets the `exchangeRate`.
        ///
        /// the exchange rate at the time of the transaction
        ///
        /// @param exchangeRate the value
        /// @return this builder
        public Builder exchangeRate(final ExchangeRate exchangeRate) {
            this.exchangeRate = exchangeRate;
            return this;
        }

        /// Sets the `nextExchangeRate`.
        ///
        /// the next exchange rate
        ///
        /// @param nextExchangeRate the value
        /// @return this builder
        public Builder nextExchangeRate(final ExchangeRate nextExchangeRate) {
            this.nextExchangeRate = nextExchangeRate;
            return this;
        }

        /// Sets the `accountId`.
        ///
        /// the account created
        ///
        /// @param accountId the value
        /// @return this builder
        public Builder accountId(final @Nullable AccountId accountId) {
            this.accountId = accountId;
            return this;
        }

        /// Sets the `fileId`.
        ///
        /// the file created
        ///
        /// @param fileId the value
        /// @return this builder
        public Builder fileId(final @Nullable Address fileId) {
            this.fileId = fileId;
            return this;
        }

        /// Sets the `contractId`.
        ///
        /// the smart contract created
        ///
        /// @param contractId the value
        /// @return this builder
        public Builder contractId(final @Nullable ContractId contractId) {
            this.contractId = contractId;
            return this;
        }

        /// Sets the `topicId`.
        ///
        /// the topic created
        ///
        /// @param topicId the value
        /// @return this builder
        public Builder topicId(final @Nullable Address topicId) {
            this.topicId = topicId;
            return this;
        }

        /// Sets the `topicSequenceNumber`.
        ///
        /// the sequence number of a submitted topic message
        ///
        /// @param topicSequenceNumber the value
        /// @return this builder
        public Builder topicSequenceNumber(final @Nullable Long topicSequenceNumber) {
            this.topicSequenceNumber = topicSequenceNumber;
            return this;
        }

        /// Sets the `topicRunningHash`.
        ///
        /// the running hash of a topic after a submitted message
        ///
        /// @param topicRunningHash the value
        /// @return this builder
        public Builder topicRunningHash(final byte @Nullable [] topicRunningHash) {
            this.topicRunningHash = topicRunningHash;
            return this;
        }

        /// Sets the `topicRunningHashVersion`.
        ///
        /// the version of the running hash algorithm
        ///
        /// @param topicRunningHashVersion the value
        /// @return this builder
        public Builder topicRunningHashVersion(final @Nullable Long topicRunningHashVersion) {
            this.topicRunningHashVersion = topicRunningHashVersion;
            return this;
        }

        /// Sets the `tokenId`.
        ///
        /// the token created
        ///
        /// @param tokenId the value
        /// @return this builder
        public Builder tokenId(final @Nullable Address tokenId) {
            this.tokenId = tokenId;
            return this;
        }

        /// Sets the `totalSupply`.
        ///
        /// the total supply of a token after a mint, burn or wipe
        ///
        /// @param totalSupply the value
        /// @return this builder
        public Builder totalSupply(final @Nullable Long totalSupply) {
            this.totalSupply = totalSupply;
            return this;
        }

        /// Sets the `scheduleId`.
        ///
        /// the schedule created
        ///
        /// @param scheduleId the value
        /// @return this builder
        public Builder scheduleId(final @Nullable Address scheduleId) {
            this.scheduleId = scheduleId;
            return this;
        }

        /// Sets the `scheduledTransactionId`.
        ///
        /// the id of the transaction a schedule executes
        ///
        /// @param scheduledTransactionId the value
        /// @return this builder
        public Builder scheduledTransactionId(final @Nullable TransactionId scheduledTransactionId) {
            this.scheduledTransactionId = scheduledTransactionId;
            return this;
        }

        /// Sets the `serials`.
        ///
        /// the serial numbers of the NFTs minted
        ///
        /// @param serials the value
        /// @return this builder
        public Builder serials(final List<Long> serials) {
            this.serials = serials;
            return this;
        }

        /// Sets the `nodeId`.
        ///
        /// the consensus node created
        ///
        /// @param nodeId the value
        /// @return this builder
        public Builder nodeId(final @Nullable Long nodeId) {
            this.nodeId = nodeId;
            return this;
        }

        /// Sets the `registeredNodeId`.
        ///
        /// the registered node created
        ///
        /// @param registeredNodeId the value
        /// @return this builder
        public Builder registeredNodeId(final @Nullable Long registeredNodeId) {
            this.registeredNodeId = registeredNodeId;
            return this;
        }

        /// Sets the `blockNumber`.
        ///
        /// the number of the block the transaction is in
        ///
        /// @param blockNumber the value
        /// @return this builder
        public Builder blockNumber(final @Nullable Long blockNumber) {
            this.blockNumber = blockNumber;
            return this;
        }

        /// Creates the `UntypedReceipt` of the values set.
        ///
        /// @return the new `UntypedReceipt`
        /// @throws NullPointerException if a required value is not set
        public UntypedReceipt build() {
            return new UntypedReceipt(
                    Objects.requireNonNull(transactionId, "transactionId must not be null"),
                    Objects.requireNonNull(status, "status must not be null"),
                    Objects.requireNonNull(exchangeRate, "exchangeRate must not be null"),
                    Objects.requireNonNull(nextExchangeRate, "nextExchangeRate must not be null"),
                    accountId,
                    fileId,
                    contractId,
                    topicId,
                    topicSequenceNumber,
                    topicRunningHash,
                    topicRunningHashVersion,
                    tokenId,
                    totalSupply,
                    scheduleId,
                    scheduledTransactionId,
                    serials,
                    nodeId,
                    registeredNodeId,
                    blockNumber);
        }
    }

    @Override
    public boolean equals(final @Nullable Object obj) {
        if (this == obj) {
            return true;
        }
        return obj instanceof UntypedReceipt other
            && Objects.equals(transactionId(), other.transactionId())
            && Objects.equals(status(), other.status())
            && Objects.equals(exchangeRate(), other.exchangeRate())
            && Objects.equals(nextExchangeRate(), other.nextExchangeRate())
            && Objects.equals(accountId(), other.accountId())
            && Objects.equals(fileId(), other.fileId())
            && Objects.equals(contractId(), other.contractId())
            && Objects.equals(topicId(), other.topicId())
            && Objects.equals(topicSequenceNumber(), other.topicSequenceNumber())
            && Arrays.equals(topicRunningHash(), other.topicRunningHash())
            && Objects.equals(topicRunningHashVersion(), other.topicRunningHashVersion())
            && Objects.equals(tokenId(), other.tokenId())
            && Objects.equals(totalSupply(), other.totalSupply())
            && Objects.equals(scheduleId(), other.scheduleId())
            && Objects.equals(scheduledTransactionId(), other.scheduledTransactionId())
            && Objects.equals(serials(), other.serials())
            && Objects.equals(nodeId(), other.nodeId())
            && Objects.equals(registeredNodeId(), other.registeredNodeId())
            && Objects.equals(blockNumber(), other.blockNumber());
    }

    @Override
    public int hashCode() {
        return Objects.hash(transactionId(), status(), exchangeRate(), nextExchangeRate(), accountId(), fileId(), contractId(), topicId(), topicSequenceNumber(), Arrays.hashCode(topicRunningHash()), topicRunningHashVersion(), tokenId(), totalSupply(), scheduleId(), scheduledTransactionId(), serials(), nodeId(), registeredNodeId(), blockNumber());
    }

    @Override
    public String toString() {
        return "UntypedReceipt[transactionId=" + transactionId()
                + ", status=" + status()
                + ", exchangeRate=" + exchangeRate()
                + ", nextExchangeRate=" + nextExchangeRate()
                + ", accountId=" + accountId()
                + ", fileId=" + fileId()
                + ", contractId=" + contractId()
                + ", topicId=" + topicId()
                + ", topicSequenceNumber=" + topicSequenceNumber()
                + ", topicRunningHash=" + (topicRunningHash() == null ? "null" : "byte[" + topicRunningHash().length + "]")
                + ", topicRunningHashVersion=" + topicRunningHashVersion()
                + ", tokenId=" + tokenId()
                + ", totalSupply=" + totalSupply()
                + ", scheduleId=" + scheduleId()
                + ", scheduledTransactionId=" + scheduledTransactionId()
                + ", serials=" + serials()
                + ", nodeId=" + nodeId()
                + ", registeredNodeId=" + registeredNodeId()
                + ", blockNumber=" + blockNumber()
                + "]";
    }
}
