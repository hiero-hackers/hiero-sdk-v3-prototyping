package org.hiero.consensusnode.queries.fees;

import static org.hiero.sdk.testing.Testing.*;

import java.util.Set;
import org.hiero.consensusnode.requestTypes.BasicRequestType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

/// Tests of the spec `queries-fee`, generated from the `tests` blocks of its `## Testing` section.
@DisplayName("queries-fee")
class QueriesFeeSpecTest {

    /// `consensusnode.queries.fees/the-basic-fee-sub-types-have-the-codes-of-the-protocol`
    ///
    /// Source: SubType of the HAPI (basic_types.proto).
    @Test
    @DisplayName("the basic fee sub types have the codes of the protocol")
    @Tag("testId:consensusnode.queries.fees/the-basic-fee-sub-types-have-the-codes-of-the-protocol")
    void theBasicFeeSubTypesHaveTheCodesOfTheProtocol() {
        // when / then
        assertEquals(0, BasicFeeSubType.DEFAULT.code());
        assertEquals(1, BasicFeeSubType.TOKEN_FUNGIBLE_COMMON.code());
        assertEquals(2, BasicFeeSubType.TOKEN_NON_FUNGIBLE_UNIQUE.code());
        assertEquals(8, BasicFeeSubType.CRYPTO_TRANSFER_WITH_HOOKS.code());
    }

    /// `consensusnode.queries.fees/a-fee-schedule-keeps-its-prices`
    ///
    /// Source: this spec (a fee schedule keeps its prices per request type and sub type).
    @Test
    @DisplayName("a fee schedule keeps its prices")
    @Tag("testId:consensusnode.queries.fees/a-fee-schedule-keeps-its-prices")
    void aFeeScheduleKeepsItsPrices() {
        // given
        final FeeComponents components = new FeeComponents(0L, 100L, 1L, 2L, 3L, 4L, 5L, 6L, 7L, 8L, 9L);
        final FeeData data = new FeeData(components, components, components, BasicFeeSubType.DEFAULT);

        // when
        final TransactionFeeSchedule entry = new TransactionFeeSchedule(BasicRequestType.CRYPTO_TRANSFER, Set.of(data));

        // then
        assertEquals(1, entry.requestType().code());
        assertEquals(0, data.subType().code());
        assertEquals(1L, data.nodeData().constantFee());
        assertEquals(9L, data.serviceData().responseDiskByte());
    }

    /// `consensusnode.queries.fees/fee-schedules-keep-the-current-and-the-next-schedule`
    ///
    /// Source: this spec (FeeSchedules keeps the current and the next schedule).
    @Test
    @DisplayName("fee schedules keep the current and the next schedule")
    @Tag("testId:consensusnode.queries.fees/fee-schedules-keep-the-current-and-the-next-schedule")
    void feeSchedulesKeepTheCurrentAndTheNextSchedule() {
        // given
        final FeeSchedule current = new FeeSchedule(Set.of(), toZonedDateTime("2022-02-24T15:00:00Z"));
        final FeeSchedule next = new FeeSchedule(Set.of(), toZonedDateTime("2022-02-24T16:00:00Z"));

        // when
        final FeeSchedules schedules = new FeeSchedules(current, next);

        // then
        assertEquals(current, schedules.currentSchedule());
        assertEquals(next, schedules.nextSchedule());
        assertNotEquals(schedules.currentSchedule(), schedules.nextSchedule());
    }

    /// `consensusnode.queries.fees/a-new-fee-schedule-query-has-no-payment-limit`
    ///
    /// Source: this spec (a system file query has no file id to set; a new query has no payment limit).
    @Test
    @DisplayName("a new fee schedule query has no payment limit")
    @Tag("testId:consensusnode.queries.fees/a-new-fee-schedule-query-has-no-payment-limit")
    void aNewFeeScheduleQueryHasNoPaymentLimit() {
        // when
        final FeeScheduleQuery query = new FeeScheduleQuery();

        // then
        assertNull(query.maxQueryPayment());
    }
}
