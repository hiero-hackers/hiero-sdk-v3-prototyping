package org.hiero.mirrornode.transaction;

import java.time.Duration;
import java.time.ZonedDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import org.hiero.authority.Authority;
import org.hiero.ledger.AccountId;
import org.hiero.ledger.Address;
import org.hiero.ledger.TransactionId;
import org.hiero.mirrornode.common.NftTransfer;
import org.hiero.mirrornode.common.StakingRewardTransfer;
import org.hiero.mirrornode.common.TokenTransfer;
import org.hiero.mirrornode.common.Transfer;
import org.jspecify.annotations.Nullable;

/// @param batchKey the key of the atomic batch of the transaction
/// @param entityId the entity the transaction created or changed
/// @param highVolume whether high-volume throttles and pricing were used
/// @param highVolumePricingMultiplier the fee multiplier of high-volume pricing, times 1000
/// @param node the node the transaction was submitted to
/// @param result the response code, e.g. "SUCCESS"
/// @param assessedCustomFees empty for a transaction of a list
public record TransactionInfo(
        TransactionId transactionId,
        byte[] transactionHash,
        @Nullable Authority batchKey,
        byte @Nullable [] bytes,
        long chargedTxFee,
        ZonedDateTime consensusTimestamp,
        @Nullable Address entityId,
        @Nullable Boolean highVolume,
        @Nullable Long highVolumePricingMultiplier,
        List<CustomFeeLimit> maxCustomFees,
        long maxFee,
        byte[] memo,
        TransactionType name,
        int nonce,
        @Nullable AccountId node,
        @Nullable ZonedDateTime parentConsensusTimestamp,
        String result,
        boolean scheduled,
        Duration validDuration,
        ZonedDateTime validStartTimestamp,
        List<Transfer> transfers,
        List<TokenTransfer> tokenTransfers,
        List<NftTransfer> nftTransfers,
        List<StakingRewardTransfer> stakingRewardTransfers,
        List<AssessedCustomFee> assessedCustomFees) {

    /// Creates a new `TransactionInfo`.
    ///
    /// @throws NullPointerException if a required value is `null`
    public TransactionInfo {
        Objects.requireNonNull(transactionId, "transactionId must not be null");
        Objects.requireNonNull(transactionHash, "transactionHash must not be null");
        transactionHash = transactionHash.clone();
        bytes = bytes == null ? null : bytes.clone();
        Objects.requireNonNull(consensusTimestamp, "consensusTimestamp must not be null");
        Objects.requireNonNull(maxCustomFees, "maxCustomFees must not be null");
        maxCustomFees = List.copyOf(maxCustomFees);
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
        Objects.requireNonNull(assessedCustomFees, "assessedCustomFees must not be null");
        assessedCustomFees = List.copyOf(assessedCustomFees);
    }

    /// Creates a new `TransactionInfo` with the default value of `maxCustomFees`, `transfers`, `tokenTransfers`, `nftTransfers`, `stakingRewardTransfers`, `assessedCustomFees`.
    public TransactionInfo(
            final TransactionId transactionId,
            final byte[] transactionHash,
            final @Nullable Authority batchKey,
            final byte @Nullable [] bytes,
            final long chargedTxFee,
            final ZonedDateTime consensusTimestamp,
            final @Nullable Address entityId,
            final @Nullable Boolean highVolume,
            final @Nullable Long highVolumePricingMultiplier,
            final long maxFee,
            final byte[] memo,
            final TransactionType name,
            final int nonce,
            final @Nullable AccountId node,
            final @Nullable ZonedDateTime parentConsensusTimestamp,
            final String result,
            final boolean scheduled,
            final Duration validDuration,
            final ZonedDateTime validStartTimestamp) {
        this(transactionId, transactionHash, batchKey, bytes, chargedTxFee, consensusTimestamp, entityId, highVolume, highVolumePricingMultiplier, List.of(), maxFee, memo, name, nonce, node, parentConsensusTimestamp, result, scheduled, validDuration, validStartTimestamp, List.of(), List.of(), List.of(), List.of(), List.of());
    }

    /// Returns a copy of the `transactionHash` array.
    @Override
    public byte[] transactionHash() {
        return transactionHash.clone();
    }

    /// Returns a copy of the `bytes` array.
    @Override
    public byte @Nullable [] bytes() {
        return bytes == null ? null : bytes.clone();
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
            && Objects.equals(batchKey, other.batchKey)
            && Arrays.equals(bytes, other.bytes)
            && chargedTxFee == other.chargedTxFee
            && Objects.equals(consensusTimestamp, other.consensusTimestamp)
            && Objects.equals(entityId, other.entityId)
            && Objects.equals(highVolume, other.highVolume)
            && Objects.equals(highVolumePricingMultiplier, other.highVolumePricingMultiplier)
            && Objects.equals(maxCustomFees, other.maxCustomFees)
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
            && Objects.equals(stakingRewardTransfers, other.stakingRewardTransfers)
            && Objects.equals(assessedCustomFees, other.assessedCustomFees);
    }

    @Override
    public int hashCode() {
        return Objects.hash(transactionId, Arrays.hashCode(transactionHash), batchKey, Arrays.hashCode(bytes), chargedTxFee, consensusTimestamp, entityId, highVolume, highVolumePricingMultiplier, maxCustomFees, maxFee, Arrays.hashCode(memo), name, nonce, node, parentConsensusTimestamp, result, scheduled, validDuration, validStartTimestamp, transfers, tokenTransfers, nftTransfers, stakingRewardTransfers, assessedCustomFees);
    }

    @Override
    public String toString() {
        return "TransactionInfo[transactionId=" + transactionId + ", transactionHash=" + "byte[" + transactionHash.length + "]" + ", batchKey=" + batchKey + ", bytes=" + (bytes == null ? "null" : "byte[" + bytes.length + "]") + ", chargedTxFee=" + chargedTxFee + ", consensusTimestamp=" + consensusTimestamp + ", entityId=" + entityId + ", highVolume=" + highVolume + ", highVolumePricingMultiplier=" + highVolumePricingMultiplier + ", maxCustomFees=" + maxCustomFees + ", maxFee=" + maxFee + ", memo=" + "byte[" + memo.length + "]" + ", name=" + name + ", nonce=" + nonce + ", node=" + node + ", parentConsensusTimestamp=" + parentConsensusTimestamp + ", result=" + result + ", scheduled=" + scheduled + ", validDuration=" + validDuration + ", validStartTimestamp=" + validStartTimestamp + ", transfers=" + transfers + ", tokenTransfers=" + tokenTransfers + ", nftTransfers=" + nftTransfers + ", stakingRewardTransfers=" + stakingRewardTransfers + ", assessedCustomFees=" + assessedCustomFees + "]";
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
        private @Nullable Authority batchKey;
        private byte @Nullable [] bytes;
        private @Nullable Long chargedTxFee;
        private @Nullable ZonedDateTime consensusTimestamp;
        private @Nullable Address entityId;
        private @Nullable Boolean highVolume;
        private @Nullable Long highVolumePricingMultiplier;
        private List<CustomFeeLimit> maxCustomFees = List.of();
        private @Nullable Long maxFee;
        private byte @Nullable [] memo;
        private @Nullable TransactionType name;
        private @Nullable Integer nonce;
        private @Nullable AccountId node;
        private @Nullable ZonedDateTime parentConsensusTimestamp;
        private @Nullable String result;
        private @Nullable Boolean scheduled;
        private @Nullable Duration validDuration;
        private @Nullable ZonedDateTime validStartTimestamp;
        private List<Transfer> transfers = List.of();
        private List<TokenTransfer> tokenTransfers = List.of();
        private List<NftTransfer> nftTransfers = List.of();
        private List<StakingRewardTransfer> stakingRewardTransfers = List.of();
        private List<AssessedCustomFee> assessedCustomFees = List.of();

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

        /// Sets the `batchKey`.
        ///
        /// the key of the atomic batch of the transaction
        ///
        /// @param batchKey the value
        /// @return this builder
        public Builder batchKey(final @Nullable Authority batchKey) {
            this.batchKey = batchKey;
            return this;
        }

        /// Sets the `bytes`.
        ///
        /// @param bytes the value
        /// @return this builder
        public Builder bytes(final byte @Nullable [] bytes) {
            this.bytes = bytes;
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
        /// the entity the transaction created or changed
        ///
        /// @param entityId the value
        /// @return this builder
        public Builder entityId(final @Nullable Address entityId) {
            this.entityId = entityId;
            return this;
        }

        /// Sets the `highVolume`.
        ///
        /// whether high-volume throttles and pricing were used
        ///
        /// @param highVolume the value
        /// @return this builder
        public Builder highVolume(final @Nullable Boolean highVolume) {
            this.highVolume = highVolume;
            return this;
        }

        /// Sets the `highVolumePricingMultiplier`.
        ///
        /// the fee multiplier of high-volume pricing, times 1000
        ///
        /// @param highVolumePricingMultiplier the value
        /// @return this builder
        public Builder highVolumePricingMultiplier(final @Nullable Long highVolumePricingMultiplier) {
            this.highVolumePricingMultiplier = highVolumePricingMultiplier;
            return this;
        }

        /// Sets the `maxCustomFees`.
        ///
        /// @param maxCustomFees the value
        /// @return this builder
        public Builder maxCustomFees(final List<CustomFeeLimit> maxCustomFees) {
            this.maxCustomFees = maxCustomFees;
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
        /// the node the transaction was submitted to
        ///
        /// @param node the value
        /// @return this builder
        public Builder node(final @Nullable AccountId node) {
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
        /// the response code, e.g. "SUCCESS"
        ///
        /// @param result the value
        /// @return this builder
        public Builder result(final String result) {
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

        /// Sets the `assessedCustomFees`.
        ///
        /// empty for a transaction of a list
        ///
        /// @param assessedCustomFees the value
        /// @return this builder
        public Builder assessedCustomFees(final List<AssessedCustomFee> assessedCustomFees) {
            this.assessedCustomFees = assessedCustomFees;
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
                    batchKey,
                    bytes,
                    Objects.requireNonNull(chargedTxFee, "chargedTxFee must not be null"),
                    Objects.requireNonNull(consensusTimestamp, "consensusTimestamp must not be null"),
                    entityId,
                    highVolume,
                    highVolumePricingMultiplier,
                    maxCustomFees,
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
                    transfers,
                    tokenTransfers,
                    nftTransfers,
                    stakingRewardTransfers,
                    assessedCustomFees);
        }
    }
}
