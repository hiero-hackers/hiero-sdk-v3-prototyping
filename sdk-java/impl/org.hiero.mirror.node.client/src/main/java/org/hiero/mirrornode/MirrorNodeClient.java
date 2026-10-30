package org.hiero.mirrornode;

import java.util.Objects;
import org.hiero.mirrornode.account.AccountRepository;
import org.hiero.mirrornode.block.BlockRepository;
import org.hiero.mirrornode.contract.ContractRepository;
import org.hiero.mirrornode.http.MirrorNodeHttpClient;
import org.hiero.mirrornode.network.NetworkRepository;
import org.hiero.mirrornode.nft.NftRepository;
import org.hiero.mirrornode.schedule.ScheduleRepository;
import org.hiero.mirrornode.token.TokenRepository;
import org.hiero.mirrornode.topic.TopicRepository;
import org.hiero.mirrornode.transaction.TransactionRepository;
import org.jspecify.annotations.Nullable;

public record MirrorNodeClient(
        AccountRepository accounts,
        BlockRepository blocks,
        ContractRepository contracts,
        NetworkRepository network,
        NftRepository nft,
        ScheduleRepository schedules,
        TokenRepository token,
        TopicRepository topic,
        TransactionRepository transaction,
        MirrorNodeHttpClient mirrorNodeHttpClient) {

    /// Creates a new `MirrorNodeClient`.
    ///
    /// @throws NullPointerException if a required value is `null`
    public MirrorNodeClient {
        Objects.requireNonNull(accounts, "accounts must not be null");
        Objects.requireNonNull(blocks, "blocks must not be null");
        Objects.requireNonNull(contracts, "contracts must not be null");
        Objects.requireNonNull(network, "network must not be null");
        Objects.requireNonNull(nft, "nft must not be null");
        Objects.requireNonNull(schedules, "schedules must not be null");
        Objects.requireNonNull(token, "token must not be null");
        Objects.requireNonNull(topic, "topic must not be null");
        Objects.requireNonNull(transaction, "transaction must not be null");
        Objects.requireNonNull(mirrorNodeHttpClient, "mirrorNodeHttpClient must not be null");
    }

    /// Returns a builder for a new `MirrorNodeClient`.
    ///
    /// @return the builder
    public static Builder builder() {
        return new Builder();
    }

    /// The builder of `MirrorNodeClient`.
    ///
    /// `build()` creates the `MirrorNodeClient` of the values set one by one.
    ///
    /// A value that is not set is `null`, or has its default value.
    public static final class Builder {

        private @Nullable AccountRepository accounts;
        private @Nullable BlockRepository blocks;
        private @Nullable ContractRepository contracts;
        private @Nullable NetworkRepository network;
        private @Nullable NftRepository nft;
        private @Nullable ScheduleRepository schedules;
        private @Nullable TokenRepository token;
        private @Nullable TopicRepository topic;
        private @Nullable TransactionRepository transaction;
        private @Nullable MirrorNodeHttpClient mirrorNodeHttpClient;

        private Builder() {
            // created by MirrorNodeClient.builder()
        }

        /// Sets the `accounts`.
        ///
        /// @param accounts the value
        /// @return this builder
        public Builder accounts(final AccountRepository accounts) {
            this.accounts = accounts;
            return this;
        }

        /// Sets the `blocks`.
        ///
        /// @param blocks the value
        /// @return this builder
        public Builder blocks(final BlockRepository blocks) {
            this.blocks = blocks;
            return this;
        }

        /// Sets the `contracts`.
        ///
        /// @param contracts the value
        /// @return this builder
        public Builder contracts(final ContractRepository contracts) {
            this.contracts = contracts;
            return this;
        }

        /// Sets the `network`.
        ///
        /// @param network the value
        /// @return this builder
        public Builder network(final NetworkRepository network) {
            this.network = network;
            return this;
        }

        /// Sets the `nft`.
        ///
        /// @param nft the value
        /// @return this builder
        public Builder nft(final NftRepository nft) {
            this.nft = nft;
            return this;
        }

        /// Sets the `schedules`.
        ///
        /// @param schedules the value
        /// @return this builder
        public Builder schedules(final ScheduleRepository schedules) {
            this.schedules = schedules;
            return this;
        }

        /// Sets the `token`.
        ///
        /// @param token the value
        /// @return this builder
        public Builder token(final TokenRepository token) {
            this.token = token;
            return this;
        }

        /// Sets the `topic`.
        ///
        /// @param topic the value
        /// @return this builder
        public Builder topic(final TopicRepository topic) {
            this.topic = topic;
            return this;
        }

        /// Sets the `transaction`.
        ///
        /// @param transaction the value
        /// @return this builder
        public Builder transaction(final TransactionRepository transaction) {
            this.transaction = transaction;
            return this;
        }

        /// Sets the `mirrorNodeHttpClient`.
        ///
        /// @param mirrorNodeHttpClient the value
        /// @return this builder
        public Builder mirrorNodeHttpClient(final MirrorNodeHttpClient mirrorNodeHttpClient) {
            this.mirrorNodeHttpClient = mirrorNodeHttpClient;
            return this;
        }

        /// Creates the `MirrorNodeClient` of the values set.
        ///
        /// @return the new `MirrorNodeClient`
        /// @throws NullPointerException if a required value is not set
        public MirrorNodeClient build() {
            return new MirrorNodeClient(
                    Objects.requireNonNull(accounts, "accounts must not be null"),
                    Objects.requireNonNull(blocks, "blocks must not be null"),
                    Objects.requireNonNull(contracts, "contracts must not be null"),
                    Objects.requireNonNull(network, "network must not be null"),
                    Objects.requireNonNull(nft, "nft must not be null"),
                    Objects.requireNonNull(schedules, "schedules must not be null"),
                    Objects.requireNonNull(token, "token must not be null"),
                    Objects.requireNonNull(topic, "topic must not be null"),
                    Objects.requireNonNull(transaction, "transaction must not be null"),
                    Objects.requireNonNull(mirrorNodeHttpClient, "mirrorNodeHttpClient must not be null"));
        }
    }
}
