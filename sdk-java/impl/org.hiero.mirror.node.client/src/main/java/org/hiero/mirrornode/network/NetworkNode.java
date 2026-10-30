package org.hiero.mirrornode.network;

import java.util.List;
import java.util.Objects;
import org.hiero.authority.Authority;
import org.hiero.ledger.AccountId;
import org.hiero.ledger.Address;
import org.hiero.mirrornode.common.TimestampRange;
import org.jspecify.annotations.Nullable;

/// One consensus-node entry in the address book. Returned by NetworkRepository.nodes()
/// (GET /network/nodes).
///
/// @param nodeId stable HIP-869 node id
/// @param accountId fee account that receives this node's share
/// @param adminKey DAB admin key (HIP-869); null on pre-DAB entries
/// @param associatedRegisteredNodes ids of the registered nodes associated with this node
/// @param declineReward whether the node declines staking rewards
/// @param fileId the address-book file the entry comes from
/// @param grpcProxyEndpoint gRPC-web proxy endpoint (HIP-1046)
/// @param maxStake maximum stake (tinybars) counted as consensus weight
/// @param minStake minimum stake (tinybars) for a non-zero consensus weight
/// @param nodeCertHash SHA-384 of the node's TLS certificate (hex)
/// @param publicKey node gossip public key (hex)
/// @param rewardRateStart tinybars earned per whole HBAR in the last staking period
/// @param stake consensus weight at the beginning of the staking period (tinybars)
/// @param stakeNotRewarded stake of the accounts staked to the node that decline rewards
/// @param stakeRewarded stake of the accounts staked to the node that receive rewards
/// @param stakingPeriod the staking period
/// @param timestamp the period this address-book entry is valid for
public record NetworkNode(
        long nodeId,
        AccountId accountId,
        @Nullable Authority adminKey,
        List<Long> associatedRegisteredNodes,
        @Nullable Boolean declineReward,
        @Nullable String description,
        @Nullable Address fileId,
        @Nullable ServiceEndpoint grpcProxyEndpoint,
        @Nullable Long maxStake,
        @Nullable String memo,
        @Nullable Long minStake,
        @Nullable String nodeCertHash,
        @Nullable String publicKey,
        @Nullable Long rewardRateStart,
        List<ServiceEndpoint> serviceEndpoints,
        @Nullable Long stake,
        @Nullable Long stakeNotRewarded,
        @Nullable Long stakeRewarded,
        @Nullable TimestampRange stakingPeriod,
        TimestampRange timestamp) {

    /// Creates a new `NetworkNode`.
    ///
    /// @throws NullPointerException if a required value is `null`
    public NetworkNode {
        Objects.requireNonNull(accountId, "accountId must not be null");
        Objects.requireNonNull(associatedRegisteredNodes, "associatedRegisteredNodes must not be null");
        associatedRegisteredNodes = List.copyOf(associatedRegisteredNodes);
        Objects.requireNonNull(serviceEndpoints, "serviceEndpoints must not be null");
        serviceEndpoints = List.copyOf(serviceEndpoints);
        Objects.requireNonNull(timestamp, "timestamp must not be null");
    }

    /// Creates a new `NetworkNode` with the default value of `associatedRegisteredNodes`, `serviceEndpoints`.
    public NetworkNode(
            final long nodeId,
            final AccountId accountId,
            final @Nullable Authority adminKey,
            final @Nullable Boolean declineReward,
            final @Nullable String description,
            final @Nullable Address fileId,
            final @Nullable ServiceEndpoint grpcProxyEndpoint,
            final @Nullable Long maxStake,
            final @Nullable String memo,
            final @Nullable Long minStake,
            final @Nullable String nodeCertHash,
            final @Nullable String publicKey,
            final @Nullable Long rewardRateStart,
            final @Nullable Long stake,
            final @Nullable Long stakeNotRewarded,
            final @Nullable Long stakeRewarded,
            final @Nullable TimestampRange stakingPeriod,
            final TimestampRange timestamp) {
        this(nodeId, accountId, adminKey, List.of(), declineReward, description, fileId, grpcProxyEndpoint, maxStake, memo, minStake, nodeCertHash, publicKey, rewardRateStart, List.of(), stake, stakeNotRewarded, stakeRewarded, stakingPeriod, timestamp);
    }

    /// Returns a builder for a new `NetworkNode`.
    ///
    /// @return the builder
    public static Builder builder() {
        return new Builder();
    }

    /// The builder of `NetworkNode`.
    ///
    /// `build()` creates the `NetworkNode` of the values set one by one.
    ///
    /// A value that is not set is `null`, or has its default value.
    public static final class Builder {

        private @Nullable Long nodeId;
        private @Nullable AccountId accountId;
        private @Nullable Authority adminKey;
        private List<Long> associatedRegisteredNodes = List.of();
        private @Nullable Boolean declineReward;
        private @Nullable String description;
        private @Nullable Address fileId;
        private @Nullable ServiceEndpoint grpcProxyEndpoint;
        private @Nullable Long maxStake;
        private @Nullable String memo;
        private @Nullable Long minStake;
        private @Nullable String nodeCertHash;
        private @Nullable String publicKey;
        private @Nullable Long rewardRateStart;
        private List<ServiceEndpoint> serviceEndpoints = List.of();
        private @Nullable Long stake;
        private @Nullable Long stakeNotRewarded;
        private @Nullable Long stakeRewarded;
        private @Nullable TimestampRange stakingPeriod;
        private @Nullable TimestampRange timestamp;

        private Builder() {
            // created by NetworkNode.builder()
        }

        /// Sets the `nodeId`.
        ///
        /// stable HIP-869 node id
        ///
        /// @param nodeId the value
        /// @return this builder
        public Builder nodeId(final long nodeId) {
            this.nodeId = nodeId;
            return this;
        }

        /// Sets the `accountId`.
        ///
        /// fee account that receives this node's share
        ///
        /// @param accountId the value
        /// @return this builder
        public Builder accountId(final AccountId accountId) {
            this.accountId = accountId;
            return this;
        }

        /// Sets the `adminKey`.
        ///
        /// DAB admin key (HIP-869); null on pre-DAB entries
        ///
        /// @param adminKey the value
        /// @return this builder
        public Builder adminKey(final @Nullable Authority adminKey) {
            this.adminKey = adminKey;
            return this;
        }

        /// Sets the `associatedRegisteredNodes`.
        ///
        /// ids of the registered nodes associated with this node
        ///
        /// @param associatedRegisteredNodes the value
        /// @return this builder
        public Builder associatedRegisteredNodes(final List<Long> associatedRegisteredNodes) {
            this.associatedRegisteredNodes = associatedRegisteredNodes;
            return this;
        }

        /// Sets the `declineReward`.
        ///
        /// whether the node declines staking rewards
        ///
        /// @param declineReward the value
        /// @return this builder
        public Builder declineReward(final @Nullable Boolean declineReward) {
            this.declineReward = declineReward;
            return this;
        }

        /// Sets the `description`.
        ///
        /// @param description the value
        /// @return this builder
        public Builder description(final @Nullable String description) {
            this.description = description;
            return this;
        }

        /// Sets the `fileId`.
        ///
        /// the address-book file the entry comes from
        ///
        /// @param fileId the value
        /// @return this builder
        public Builder fileId(final @Nullable Address fileId) {
            this.fileId = fileId;
            return this;
        }

        /// Sets the `grpcProxyEndpoint`.
        ///
        /// gRPC-web proxy endpoint (HIP-1046)
        ///
        /// @param grpcProxyEndpoint the value
        /// @return this builder
        public Builder grpcProxyEndpoint(final @Nullable ServiceEndpoint grpcProxyEndpoint) {
            this.grpcProxyEndpoint = grpcProxyEndpoint;
            return this;
        }

        /// Sets the `maxStake`.
        ///
        /// maximum stake (tinybars) counted as consensus weight
        ///
        /// @param maxStake the value
        /// @return this builder
        public Builder maxStake(final @Nullable Long maxStake) {
            this.maxStake = maxStake;
            return this;
        }

        /// Sets the `memo`.
        ///
        /// @param memo the value
        /// @return this builder
        public Builder memo(final @Nullable String memo) {
            this.memo = memo;
            return this;
        }

        /// Sets the `minStake`.
        ///
        /// minimum stake (tinybars) for a non-zero consensus weight
        ///
        /// @param minStake the value
        /// @return this builder
        public Builder minStake(final @Nullable Long minStake) {
            this.minStake = minStake;
            return this;
        }

        /// Sets the `nodeCertHash`.
        ///
        /// SHA-384 of the node's TLS certificate (hex)
        ///
        /// @param nodeCertHash the value
        /// @return this builder
        public Builder nodeCertHash(final @Nullable String nodeCertHash) {
            this.nodeCertHash = nodeCertHash;
            return this;
        }

        /// Sets the `publicKey`.
        ///
        /// node gossip public key (hex)
        ///
        /// @param publicKey the value
        /// @return this builder
        public Builder publicKey(final @Nullable String publicKey) {
            this.publicKey = publicKey;
            return this;
        }

        /// Sets the `rewardRateStart`.
        ///
        /// tinybars earned per whole HBAR in the last staking period
        ///
        /// @param rewardRateStart the value
        /// @return this builder
        public Builder rewardRateStart(final @Nullable Long rewardRateStart) {
            this.rewardRateStart = rewardRateStart;
            return this;
        }

        /// Sets the `serviceEndpoints`.
        ///
        /// @param serviceEndpoints the value
        /// @return this builder
        public Builder serviceEndpoints(final List<ServiceEndpoint> serviceEndpoints) {
            this.serviceEndpoints = serviceEndpoints;
            return this;
        }

        /// Sets the `stake`.
        ///
        /// consensus weight at the beginning of the staking period (tinybars)
        ///
        /// @param stake the value
        /// @return this builder
        public Builder stake(final @Nullable Long stake) {
            this.stake = stake;
            return this;
        }

        /// Sets the `stakeNotRewarded`.
        ///
        /// stake of the accounts staked to the node that decline rewards
        ///
        /// @param stakeNotRewarded the value
        /// @return this builder
        public Builder stakeNotRewarded(final @Nullable Long stakeNotRewarded) {
            this.stakeNotRewarded = stakeNotRewarded;
            return this;
        }

        /// Sets the `stakeRewarded`.
        ///
        /// stake of the accounts staked to the node that receive rewards
        ///
        /// @param stakeRewarded the value
        /// @return this builder
        public Builder stakeRewarded(final @Nullable Long stakeRewarded) {
            this.stakeRewarded = stakeRewarded;
            return this;
        }

        /// Sets the `stakingPeriod`.
        ///
        /// the staking period
        ///
        /// @param stakingPeriod the value
        /// @return this builder
        public Builder stakingPeriod(final @Nullable TimestampRange stakingPeriod) {
            this.stakingPeriod = stakingPeriod;
            return this;
        }

        /// Sets the `timestamp`.
        ///
        /// the period this address-book entry is valid for
        ///
        /// @param timestamp the value
        /// @return this builder
        public Builder timestamp(final TimestampRange timestamp) {
            this.timestamp = timestamp;
            return this;
        }

        /// Creates the `NetworkNode` of the values set.
        ///
        /// @return the new `NetworkNode`
        /// @throws NullPointerException if a required value is not set
        public NetworkNode build() {
            return new NetworkNode(
                    Objects.requireNonNull(nodeId, "nodeId must not be null"),
                    Objects.requireNonNull(accountId, "accountId must not be null"),
                    adminKey,
                    associatedRegisteredNodes,
                    declineReward,
                    description,
                    fileId,
                    grpcProxyEndpoint,
                    maxStake,
                    memo,
                    minStake,
                    nodeCertHash,
                    publicKey,
                    rewardRateStart,
                    serviceEndpoints,
                    stake,
                    stakeNotRewarded,
                    stakeRewarded,
                    stakingPeriod,
                    Objects.requireNonNull(timestamp, "timestamp must not be null"));
        }
    }
}
