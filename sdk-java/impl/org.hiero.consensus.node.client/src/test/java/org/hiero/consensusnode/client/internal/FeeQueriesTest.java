package org.hiero.consensusnode.client.internal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.hederahashgraph.api.proto.java.CurrentAndNextFeeSchedule;
import com.hederahashgraph.api.proto.java.FeeComponents;
import com.hederahashgraph.api.proto.java.FeeData;
import com.hederahashgraph.api.proto.java.FeeSchedule;
import com.hederahashgraph.api.proto.java.HederaFunctionality;
import com.hederahashgraph.api.proto.java.SubType;
import com.hederahashgraph.api.proto.java.TimestampSeconds;
import com.hederahashgraph.api.proto.java.TransactionFeeSchedule;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Set;
import org.hiero.consensusnode.client.AttemptOutcome;
import org.hiero.consensusnode.queries.fees.BasicFeeSubType;
import org.hiero.consensusnode.queries.fees.FeeSchedules;
import org.hiero.consensusnode.requestTypes.BasicRequestType;
import org.hiero.consensusnode.status.BasicTransactionStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

/// Tests of the decoding of the fee schedule file (rules fee-schedule-decoding and fee-schedule-unknown-codes of
/// `consensusnode.queries.fees`). The values are arbitrary; the tests check how they are carried over.
@DisplayName("Fee schedule decoding")
class FeeQueriesTest {

    private static final long EXPIRY = 1_645_714_800L;

    private static FeeComponents components(final long constant) {
        return FeeComponents.newBuilder().setMin(1).setMax(2).setConstant(constant).setBpt(4).setVpt(5).setRbh(6)
                .setSbh(7).setGas(8).setTv(9).setBpr(10).setSbpr(11).build();
    }

    private static FeeData data(final int subType, final long constant) {
        return FeeData.newBuilder().setNodedata(components(constant)).setNetworkdata(components(constant))
                .setServicedata(components(constant)).setSubTypeValue(subType).build();
    }

    private static FeeSchedule schedule(final TransactionFeeSchedule... entries) {
        return FeeSchedule.newBuilder().addAllTransactionFeeSchedule(List.of(entries))
                .setExpiryTime(TimestampSeconds.newBuilder().setSeconds(EXPIRY)).build();
    }

    private static byte[] file(final FeeSchedule current, final FeeSchedule next) {
        return CurrentAndNextFeeSchedule.newBuilder().setCurrentFeeSchedule(current).setNextFeeSchedule(next).build()
                .toByteArray();
    }

    @Test
    @DisplayName("the prices of a known request type and sub type are carried over")
    @Tag("testId:impl/fees/the-prices-of-a-known-request-type-and-sub-type-are-carried-over")
    void thePricesOfAKnownRequestTypeAndSubTypeAreCarriedOver() {
        // given
        final TransactionFeeSchedule transfer = TransactionFeeSchedule.newBuilder()
                .setHederaFunctionality(HederaFunctionality.CryptoTransfer)
                .addFees(data(SubType.DEFAULT_VALUE, 3)).build();

        // when
        final FeeSchedules schedules = FeeQueries.decode(file(schedule(transfer), schedule()));

        // then
        final var entry = schedules.currentSchedule().transactionFeeSchedules().iterator().next();
        assertEquals(BasicRequestType.CRYPTO_TRANSFER, entry.requestType());
        final var data = entry.fees().iterator().next();
        assertEquals(BasicFeeSubType.DEFAULT, data.subType());
        assertEquals(new org.hiero.consensusnode.queries.fees.FeeComponents(1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11),
                data.nodeData());
        assertEquals(ZonedDateTime.parse("2022-02-24T15:00:00Z"), schedules.currentSchedule().expirationTime());
        assertEquals(Set.of(), schedules.nextSchedule().transactionFeeSchedules());
    }

    @Test
    @DisplayName("entries of an unknown request type or sub type are left out")
    @Tag("testId:impl/fees/entries-of-an-unknown-request-type-or-sub-type-are-left-out")
    void entriesOfAnUnknownRequestTypeOrSubTypeAreLeftOut() {
        // given
        final TransactionFeeSchedule unknownType = TransactionFeeSchedule.newBuilder()
                .setHederaFunctionalityValue(9999).addFees(data(SubType.DEFAULT_VALUE, 3)).build();
        final TransactionFeeSchedule mint = TransactionFeeSchedule.newBuilder()
                .setHederaFunctionality(HederaFunctionality.TokenMint)
                .addFees(data(SubType.TOKEN_FUNGIBLE_COMMON_VALUE, 3)).addFees(data(9999, 4)).build();

        // when
        final FeeSchedules schedules = FeeQueries.decode(file(schedule(unknownType, mint), schedule()));

        // then
        assertEquals(1, schedules.currentSchedule().transactionFeeSchedules().size());
        final var entry = schedules.currentSchedule().transactionFeeSchedules().iterator().next();
        assertEquals(BasicRequestType.TOKEN_MINT, entry.requestType());
        assertEquals(1, entry.fees().size());
        assertEquals(BasicFeeSubType.TOKEN_FUNGIBLE_COMMON, entry.fees().iterator().next().subType());
    }

    @Test
    @DisplayName("the deprecated fee data is the only entry when fees is empty")
    @Tag("testId:impl/fees/the-deprecated-fee-data-is-the-only-entry-when-fees-is-empty")
    @SuppressWarnings("deprecation")
    void theDeprecatedFeeDataIsTheOnlyEntryWhenFeesIsEmpty() {
        // given
        final TransactionFeeSchedule legacy = TransactionFeeSchedule.newBuilder()
                .setHederaFunctionality(HederaFunctionality.CryptoCreate)
                .setFeeData(data(SubType.DEFAULT_VALUE, 3)).build();

        // when
        final FeeSchedules schedules = FeeQueries.decode(file(schedule(legacy), schedule()));

        // then
        final var entry = schedules.currentSchedule().transactionFeeSchedules().iterator().next();
        assertEquals(1, entry.fees().size());
        assertEquals(3, entry.fees().iterator().next().serviceData().constantFee());
    }

    @Test
    @DisplayName("a file that cannot be decoded or lacks a schedule is malformed")
    @Tag("testId:impl/fees/a-file-that-cannot-be-decoded-or-lacks-a-schedule-is-malformed")
    void aFileThatCannotBeDecodedOrLacksAScheduleIsMalformed() {
        assertThrows(MalformedAnswerException.class, () -> FeeQueries.decode(new byte[] {(byte) 0xff, 1, 2}));
        assertThrows(MalformedAnswerException.class, () -> FeeQueries.decode(CurrentAndNextFeeSchedule.newBuilder()
                .setCurrentFeeSchedule(schedule()).build().toByteArray()));
    }

    @Test
    @DisplayName("a malformed answer is rejected")
    @Tag("testId:impl/fees/a-malformed-answer-is-rejected")
    void aMalformedAnswerIsRejected() {
        // when
        final Execution.Answer<FeeSchedules> answer = Execution.Answer.precheck(BasicTransactionStatus.OK, false,
                () -> FeeQueries.decode(new byte[] {(byte) 0xff, 1, 2}));

        // then
        assertEquals(AttemptOutcome.REJECTED, answer.outcome());
        assertNull(answer.value());
    }
}
