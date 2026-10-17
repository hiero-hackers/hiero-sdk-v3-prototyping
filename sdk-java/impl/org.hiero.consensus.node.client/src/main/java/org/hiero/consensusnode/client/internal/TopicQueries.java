package org.hiero.consensusnode.client.internal;

import com.hederahashgraph.api.proto.java.AccountID;
import com.hederahashgraph.api.proto.java.ConsensusGetTopicInfoQuery;
import com.hederahashgraph.api.proto.java.ConsensusGetTopicInfoResponse;
import com.hederahashgraph.api.proto.java.ConsensusTopicInfo;
import com.hederahashgraph.api.proto.java.Query;
import org.hiero.consensusnode.queries.topics.TopicInfo;
import org.hiero.consensusnode.queries.topics.TopicInfoQuery;
import org.hiero.ledger.AccountId;
import org.jspecify.annotations.Nullable;

/// The query types of topics.
public final class TopicQueries {

    /// `TopicInfoQuery`: `ConsensusService.getTopicInfo`.
    public static final QueryKind<TopicInfoQuery, TopicInfo> TOPIC_INFO = new QueryKind<>(
            "proto.ConsensusService", "getTopicInfo",
            (query, header) -> Query.newBuilder().setConsensusGetTopicInfo(ConsensusGetTopicInfoQuery.newBuilder()
                    .setHeader(header).setTopicID(Protobuf.toTopicId(query.topicId()))).build(),
            response -> response.getConsensusGetTopicInfo().getHeader(),
            (query, response) -> topicInfo(query, response.getConsensusGetTopicInfo()));

    private TopicQueries() {
    }

    private static TopicInfo topicInfo(final TopicInfoQuery query, final ConsensusGetTopicInfoResponse response) {
        final ConsensusTopicInfo info = response.getTopicInfo();
        return new TopicInfo(response.hasTopicID() ? Protobuf.fromProto(response.getTopicID()) : query.topicId(),
                info.getMemo().isEmpty() ? null : info.getMemo(),
                info.getRunningHash().toByteArray(),
                info.getSequenceNumber(),
                Protobuf.fromProto(info.getExpirationTime()),
                info.hasAdminKey() ? Protobuf.fromProto(info.getAdminKey()) : null,
                info.hasSubmitKey() ? Protobuf.fromProto(info.getSubmitKey()) : null,
                info.hasAutoRenewPeriod() ? Protobuf.fromProto(info.getAutoRenewPeriod()) : null,
                info.hasAutoRenewAccount() ? account(info.getAutoRenewAccount()) : null);
    }

    /// An account of a topic; the account `0.0.0` (the "no account" sentinel) is no account.
    private static @Nullable AccountId account(final AccountID account) {
        return account.getAccountCase() == AccountID.AccountCase.ACCOUNTNUM && account.getAccountNum() == 0
                ? null : Protobuf.fromProto(account);
    }
}
