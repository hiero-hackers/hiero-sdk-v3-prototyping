package org.hiero.consensusnode.client.internal;

import com.google.protobuf.InvalidProtocolBufferException;
import com.hederahashgraph.api.proto.java.CurrentAndNextFeeSchedule;
import com.hederahashgraph.api.proto.java.FileGetContentsQuery;
import com.hederahashgraph.api.proto.java.Query;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.hiero.consensusnode.client.HieroClient;
import org.hiero.consensusnode.queries.fees.BasicFeeSubType;
import org.hiero.consensusnode.queries.fees.FeeComponents;
import org.hiero.consensusnode.queries.fees.FeeData;
import org.hiero.consensusnode.queries.fees.FeeSchedule;
import org.hiero.consensusnode.queries.fees.FeeSchedules;
import org.hiero.consensusnode.queries.fees.FeeSubType;
import org.hiero.consensusnode.queries.fees.TransactionFeeSchedule;
import org.hiero.consensusnode.requestTypes.RequestType;
import org.hiero.ledger.Address;

/// The query of the fee schedule file: `FileService.getFileContent` for the file `<shard>.<realm>.111`, whose contents
/// are decoded as `CurrentAndNextFeeSchedule`.
public final class FeeQueries {

    /// rule fee-schedule-file: the number of the fee schedule file in the shard and realm of the network.
    private static final long FEE_SCHEDULE_FILE = 111L;

    private static final Map<Integer, FeeSubType> SUB_TYPES = Arrays.stream(BasicFeeSubType.values())
            .collect(Collectors.toUnmodifiableMap(BasicFeeSubType::code, Function.identity()));

    /// `FeeScheduleQuery`: `FileService.getFileContent` of the fee schedule file. The query value is the id of the
    /// file.
    public static final QueryKind<Address, FeeSchedules> FEE_SCHEDULE = new QueryKind<>(
            "proto.FileService", "getFileContent",
            (fileId, header) -> Query.newBuilder().setFileGetContents(FileGetContentsQuery.newBuilder()
                    .setHeader(header).setFileID(Protobuf.toFileId(fileId))).build(),
            response -> response.getFileGetContents().getHeader(),
            (fileId, response) -> decode(response.getFileGetContents().getFileContents().getContents()
                    .toByteArray()));

    private FeeQueries() {
    }

    /// rule fee-schedule-file: the fee schedule file in the shard and realm of the network of the client.
    ///
    /// @param client the client
    /// @return the id of the fee schedule file
    public static Address feeScheduleFile(final HieroClient<?> client) {
        return SystemFileQueries.systemFile(client, FEE_SCHEDULE_FILE);
    }

    /// rule fee-schedule-decoding: the contents of the fee schedule file as `FeeSchedules`.
    ///
    /// @param contents the contents of the file
    /// @return the schedules
    /// @throws MalformedAnswerException if the contents cannot be decoded or lack one of the two schedules
    static FeeSchedules decode(final byte[] contents) {
        final CurrentAndNextFeeSchedule schedules;
        try {
            schedules = CurrentAndNextFeeSchedule.parseFrom(contents);
        } catch (final InvalidProtocolBufferException invalid) {
            throw new MalformedAnswerException("The fee schedule file cannot be decoded", invalid);
        }
        if (!schedules.hasCurrentFeeSchedule() || !schedules.hasNextFeeSchedule()) {
            throw new MalformedAnswerException("The fee schedule file lacks the current or the next schedule", null);
        }
        try {
            return new FeeSchedules(schedule(schedules.getCurrentFeeSchedule()),
                    schedule(schedules.getNextFeeSchedule()));
        } catch (final IllegalArgumentException invalid) {
            // a negative price violates @@min(0) of FeeComponents
            throw new MalformedAnswerException("The fee schedule file holds an invalid value", invalid);
        }
    }

    private static FeeSchedule schedule(final com.hederahashgraph.api.proto.java.FeeSchedule schedule) {
        final Set<TransactionFeeSchedule> entries = new LinkedHashSet<>();
        for (final com.hederahashgraph.api.proto.java.TransactionFeeSchedule entry
                : schedule.getTransactionFeeScheduleList()) {
            // rule fee-schedule-request-types / fee-schedule-unknown-codes: an entry of an unknown request type is
            // left out
            final Optional<RequestType> requestType = RequestTypes.find(entry.getHederaFunctionalityValue());
            requestType.ifPresent(type -> entries.add(new TransactionFeeSchedule(type, fees(entry))));
        }
        return new FeeSchedule(entries, Instant.ofEpochSecond(schedule.getExpiryTime().getSeconds())
                .atZone(ZoneOffset.UTC));
    }

    // rule fee-schedule-decoding reads the deprecated feeData of an older file
    @SuppressWarnings("deprecation")
    private static Set<FeeData> fees(final com.hederahashgraph.api.proto.java.TransactionFeeSchedule entry) {
        // the deprecated feeData is the only entry if fees is empty
        final List<com.hederahashgraph.api.proto.java.FeeData> fees = entry.getFeesCount() == 0 && entry.hasFeeData()
                ? List.of(entry.getFeeData()) : entry.getFeesList();
        final Set<FeeData> result = new LinkedHashSet<>();
        for (final com.hederahashgraph.api.proto.java.FeeData data : fees) {
            // rule fee-schedule-unknown-codes: a price of an unknown sub type is left out
            final FeeSubType subType = SUB_TYPES.get(data.getSubTypeValue());
            if (subType != null) {
                result.add(new FeeData(components(data.getNodedata()), components(data.getNetworkdata()),
                        components(data.getServicedata()), subType));
            }
        }
        return result;
    }

    // a missing message is the default instance: every price 0
    private static FeeComponents components(final com.hederahashgraph.api.proto.java.FeeComponents components) {
        return new FeeComponents(components.getMin(), components.getMax(), components.getConstant(),
                components.getBpt(), components.getVpt(), components.getRbh(), components.getSbh(),
                components.getGas(), components.getTv(), components.getBpr(), components.getSbpr());
    }
}
