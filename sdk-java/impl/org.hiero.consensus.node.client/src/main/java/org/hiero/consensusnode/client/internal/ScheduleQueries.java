package org.hiero.consensusnode.client.internal;

import com.hederahashgraph.api.proto.java.Query;
import com.hederahashgraph.api.proto.java.ScheduleGetInfoQuery;
import java.util.List;
import org.hiero.authority.PublicKeyAuthority;
import org.hiero.consensusnode.queries.schedule.ScheduleInfo;
import org.hiero.consensusnode.queries.schedule.ScheduleInfoQuery;
import org.hiero.keys.PublicKey;

/// The query types of schedules.
public final class ScheduleQueries {

    /// `ScheduleInfoQuery`: `ScheduleService.getScheduleInfo`.
    public static final QueryKind<ScheduleInfoQuery, ScheduleInfo> SCHEDULE_INFO = new QueryKind<>(
            "proto.ScheduleService", "getScheduleInfo",
            (query, header) -> Query.newBuilder().setScheduleGetInfo(ScheduleGetInfoQuery.newBuilder()
                    .setHeader(header).setScheduleID(Protobuf.toScheduleId(query.scheduleId()))).build(),
            response -> response.getScheduleGetInfo().getHeader(),
            (query, response) -> scheduleInfo(response.getScheduleGetInfo().getScheduleInfo()));

    private ScheduleQueries() {
    }

    private static ScheduleInfo scheduleInfo(final com.hederahashgraph.api.proto.java.ScheduleInfo info) {
        // every credited signer is a single key (Ed25519 or ECDSA secp256k1)
        final List<PublicKey> signers = info.getSigners().getKeysList().stream().map(Protobuf::fromProto)
                .filter(PublicKeyAuthority.class::isInstance)
                .map(authority -> ((PublicKeyAuthority) authority).publicKey()).toList();
        return new ScheduleInfo(Protobuf.fromProto(info.getScheduleID()),
                Protobuf.fromProto(info.getCreatorAccountID()),
                Protobuf.fromProto(info.getPayerAccountID()),
                new ScheduledTransaction(info.getScheduledTransactionBody()),
                Protobuf.fromProto(info.getScheduledTransactionID()),
                signers,
                info.hasAdminKey() ? Protobuf.fromProto(info.getAdminKey()) : null,
                info.getMemo().isEmpty() ? null : info.getMemo(),
                Protobuf.fromProto(info.getExpirationTime()),
                info.getWaitForExpiry(),
                info.hasExecutionTime() ? Protobuf.fromProto(info.getExecutionTime()) : null,
                info.hasDeletionTime() ? Protobuf.fromProto(info.getDeletionTime()) : null);
    }
}
