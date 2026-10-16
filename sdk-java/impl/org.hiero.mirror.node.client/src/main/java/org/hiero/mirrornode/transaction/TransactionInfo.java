package org.hiero.mirrornode.transaction;

import java.time.Duration;
import java.time.ZonedDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import org.hiero.ledger.TransactionId;
import org.hiero.mirrornode.common.NftTransfer;
import org.hiero.mirrornode.common.StakingRewardTransfer;
import org.hiero.mirrornode.common.TokenTransfer;
import org.hiero.mirrornode.common.Transfer;
import org.jspecify.annotations.Nullable;

public record TransactionInfo(
        TransactionId transactionId,
        byte[] transactionHash,
        long chargedTxFee,
        ZonedDateTime consensusTimestamp,
        @Nullable String entityId,
        long maxFee,
        byte[] memo,
        TransactionType name,
        int nonce,
        @Nullable String node,
        @Nullable ZonedDateTime parentConsensusTimestamp,
        TransactionResult result,
        boolean scheduled,
        Duration validDuration,
        ZonedDateTime validStartTimestamp,
        List<Transfer> transfers,
        List<TokenTransfer> tokenTransfers,
        List<NftTransfer> nftTransfers,
        List<StakingRewardTransfer> stakingRewardTransfers) {

    /// Creates a new `TransactionInfo`.
    ///
    /// @throws NullPointerException if a required value is `null`
    public TransactionInfo {
        Objects.requireNonNull(transactionId, "transactionId must not be null");
        Objects.requireNonNull(transactionHash, "transactionHash must not be null");
        transactionHash = transactionHash.clone();
        Objects.requireNonNull(consensusTimestamp, "consensusTimestamp must not be null");
        Objects.requireNonNull(memo, "memo must not be null");
        memo = memo.clone();
        Objects.requireNonNull(name, "name must not be null");
        Objects.requireNonNull(result, "result must not be null");
        Objects.requireNonNull(validDuration, "validDuration must not be null");
        Objects.requireNonNull(validStartTimestamp, "validStartTimestamp must not be null");
        Objects.requireNonNull(transfers, "transfers must not be null");
        transfers = List.copyOf(transfers);
        Objects.requireNonNull(tokenTransfers, "tokenTransfers must not be null");
        tokenTransfers = List.copyOf(tokenTransfers);
        Objects.requireNonNull(nftTransfers, "nftTransfers must not be null");
        nftTransfers = List.copyOf(nftTransfers);
        Objects.requireNonNull(stakingRewardTransfers, "stakingRewardTransfers must not be null");
        stakingRewardTransfers = List.copyOf(stakingRewardTransfers);
    }

    /// Returns a copy of the `transactionHash` array.
    @Override
    public byte[] transactionHash() {
        return transactionHash.clone();
    }

    /// Returns a copy of the `memo` array.
    @Override
    public byte[] memo() {
        return memo.clone();
    }

    @Override
    public boolean equals(final @Nullable Object obj) {
        return obj instanceof TransactionInfo other
            && Objects.equals(transactionId, other.transactionId)
            && Arrays.equals(transactionHash, other.transactionHash)
            && chargedTxFee == other.chargedTxFee
            && Objects.equals(consensusTimestamp, other.consensusTimestamp)
            && Objects.equals(entityId, other.entityId)
            && maxFee == other.maxFee
            && Arrays.equals(memo, other.memo)
            && Objects.equals(name, other.name)
            && nonce == other.nonce
            && Objects.equals(node, other.node)
            && Objects.equals(parentConsensusTimestamp, other.parentConsensusTimestamp)
            && Objects.equals(result, other.result)
            && scheduled == other.scheduled
            && Objects.equals(validDuration, other.validDuration)
            && Objects.equals(validStartTimestamp, other.validStartTimestamp)
            && Objects.equals(transfers, other.transfers)
            && Objects.equals(tokenTransfers, other.tokenTransfers)
            && Objects.equals(nftTransfers, other.nftTransfers)
            && Objects.equals(stakingRewardTransfers, other.stakingRewardTransfers);
    }

    @Override
    public int hashCode() {
        return Objects.hash(transactionId, Arrays.hashCode(transactionHash), chargedTxFee, consensusTimestamp, entityId, maxFee, Arrays.hashCode(memo), name, nonce, node, parentConsensusTimestamp, result, scheduled, validDuration, validStartTimestamp, transfers, tokenTransfers, nftTransfers, stakingRewardTransfers);
    }

    @Override
    public String toString() {
        return "TransactionInfo[transactionId=" + transactionId + ", transactionHash=" + "byte[" + transactionHash.length + "]" + ", chargedTxFee=" + chargedTxFee + ", consensusTimestamp=" + consensusTimestamp + ", entityId=" + entityId + ", maxFee=" + maxFee + ", memo=" + "byte[" + memo.length + "]" + ", name=" + name + ", nonce=" + nonce + ", node=" + node + ", parentConsensusTimestamp=" + parentConsensusTimestamp + ", result=" + result + ", scheduled=" + scheduled + ", validDuration=" + validDuration + ", validStartTimestamp=" + validStartTimestamp + ", transfers=" + transfers + ", tokenTransfers=" + tokenTransfers + ", nftTransfers=" + nftTransfers + ", stakingRewardTransfers=" + stakingRewardTransfers + "]";
    }

    /// Returns a builder for a new `TransactionInfo`.
    ///
    /// @return the builder
    public static Builder builder() {
        return new Builder();
    }

    /// The builder of `TransactionInfo`.
    ///
    /// `build()` creates the `TransactionInfo` of the values set one by one.
    ///
    /// A value that is not set is `null`, or has its default value.
    public static final class Builder {

        private @Nullable TransactionId transactionId;
        private byte @Nullable [] transactionHash;
        private @Nullable Long chargedTxFee;
        private @Nullable ZonedDateTime consensusTimestamp;
        private @Nullable String entityId;
        private @Nullable Long maxFee;
        private byte @Nullable [] memo;
        private @Nullable TransactionType name;
        private @Nullable Integer nonce;
        private @Nullable String node;
        private @Nullable ZonedDateTime parentConsensusTimestamp;
        private @Nullable TransactionResult result;
        private @Nullable Boolean scheduled;
        private @Nullable Duration validDuration;
        private @Nullable ZonedDateTime validStartTimestamp;
        private @Nullable List<Transfer> transfers;
        private @Nullable List<TokenTransfer> tokenTransfers;
        private @Nullable List<NftTransfer> nftTransfers;
        private @Nullable List<StakingRewardTransfer> stakingRewardTransfers;

        private Builder() {
            // created by TransactionInfo.builder()
        }

        /// Sets the `transactionId`.
        ///
        /// @param transactionId the value
        /// @return this builder
        public Builder transactionId(final TransactionId transactionId) {
            this.transactionId = transactionId;
            return this;
        }

        /// Sets the `transactionHash`.
        ///
        /// @param transactionHash the value
        /// @return this builder
        public Builder transactionHash(final byte[] transactionHash) {
            this.transactionHash = transactionHash;
            return this;
        }

        /// Sets the `chargedTxFee`.
        ///
        /// @param chargedTxFee the value
        /// @return this builder
        public Builder chargedTxFee(final long chargedTxFee) {
            this.chargedTxFee = chargedTxFee;
            return this;
        }

        /// Sets the `consensusTimestamp`.
        ///
        /// @param consensusTimestamp the value
        /// @return this builder
        public Builder consensusTimestamp(final ZonedDateTime consensusTimestamp) {
            this.consensusTimestamp = consensusTimestamp;
            return this;
        }

        /// Sets the `entityId`.
        ///
        /// @param entityId the value
        /// @return this builder
        public Builder entityId(final @Nullable String entityId) {
            this.entityId = entityId;
            return this;
        }

        /// Sets the `maxFee`.
        ///
        /// @param maxFee the value
        /// @return this builder
        public Builder maxFee(final long maxFee) {
            this.maxFee = maxFee;
            return this;
        }

        /// Sets the `memo`.
        ///
        /// @param memo the value
        /// @return this builder
        public Builder memo(final byte[] memo) {
            this.memo = memo;
            return this;
        }

        /// Sets the `name`.
        ///
        /// @param name the value
        /// @return this builder
        public Builder name(final TransactionType name) {
            this.name = name;
            return this;
        }

        /// Sets the `nonce`.
        ///
        /// @param nonce the value
        /// @return this builder
        public Builder nonce(final int nonce) {
            this.nonce = nonce;
            return this;
        }

        /// Sets the `node`.
        ///
        /// @param node the value
        /// @return this builder
        public Builder node(final @Nullable String node) {
            this.node = node;
            return this;
        }

        /// Sets the `parentConsensusTimestamp`.
        ///
        /// @param parentConsensusTimestamp the value
        /// @return this builder
        public Builder parentConsensusTimestamp(final @Nullable ZonedDateTime parentConsensusTimestamp) {
            this.parentConsensusTimestamp = parentConsensusTimestamp;
            return this;
        }

        /// Sets the `result`.
        ///
        /// @param result the value
        /// @return this builder
        public Builder result(final TransactionResult result) {
            this.result = result;
            return this;
        }

        /// Sets the `scheduled`.
        ///
        /// @param scheduled the value
        /// @return this builder
        public Builder scheduled(final boolean scheduled) {
            this.scheduled = scheduled;
            return this;
        }

        /// Sets the `validDuration`.
        ///
        /// @param validDuration the value
        /// @return this builder
        public Builder validDuration(final Duration validDuration) {
            this.validDuration = validDuration;
            return this;
        }

        /// Sets the `validStartTimestamp`.
        ///
        /// @param validStartTimestamp the value
        /// @return this builder
        public Builder validStartTimestamp(final ZonedDateTime validStartTimestamp) {
            this.validStartTimestamp = validStartTimestamp;
            return this;
        }

        /// Sets the `transfers`.
        ///
        /// @param transfers the value
        /// @return this builder
        public Builder transfers(final List<Transfer> transfers) {
            this.transfers = transfers;
            return this;
        }

        /// Sets the `tokenTransfers`.
        ///
        /// @param tokenTransfers the value
        /// @return this builder
        public Builder tokenTransfers(final List<TokenTransfer> tokenTransfers) {
            this.tokenTransfers = tokenTransfers;
            return this;
        }

        /// Sets the `nftTransfers`.
        ///
        /// @param nftTransfers the value
        /// @return this builder
        public Builder nftTransfers(final List<NftTransfer> nftTransfers) {
            this.nftTransfers = nftTransfers;
            return this;
        }

        /// Sets the `stakingRewardTransfers`.
        ///
        /// @param stakingRewardTransfers the value
        /// @return this builder
        public Builder stakingRewardTransfers(final List<StakingRewardTransfer> stakingRewardTransfers) {
            this.stakingRewardTransfers = stakingRewardTransfers;
            return this;
        }

        /// Creates the `TransactionInfo` of the values set.
        ///
        /// @return the new `TransactionInfo`
        /// @throws NullPointerException if a required value is not set
        public TransactionInfo build() {
            return new TransactionInfo(
                    Objects.requireNonNull(transactionId, "transactionId must not be null"),
                    Objects.requireNonNull(transactionHash, "transactionHash must not be null"),
                    Objects.requireNonNull(chargedTxFee, "chargedTxFee must not be null"),
                    Objects.requireNonNull(consensusTimestamp, "consensusTimestamp must not be null"),
                    entityId,
                    Objects.requireNonNull(maxFee, "maxFee must not be null"),
                    Objects.requireNonNull(memo, "memo must not be null"),
                    Objects.requireNonNull(name, "name must not be null"),
                    Objects.requireNonNull(nonce, "nonce must not be null"),
                    node,
                    parentConsensusTimestamp,
                    Objects.requireNonNull(result, "result must not be null"),
                    Objects.requireNonNull(scheduled, "scheduled must not be null"),
                    Objects.requireNonNull(validDuration, "validDuration must not be null"),
                    Objects.requireNonNull(validStartTimestamp, "validStartTimestamp must not be null"),
                    Objects.requireNonNull(transfers, "transfers must not be null"),
                    Objects.requireNonNull(tokenTransfers, "tokenTransfers must not be null"),
                    Objects.requireNonNull(nftTransfers, "nftTransfers must not be null"),
                    Objects.requireNonNull(stakingRewardTransfers, "stakingRewardTransfers must not be null"));
        }
    }
}
