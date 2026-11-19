package org.hiero.consensusnode.queries.fees;

import static org.hiero.sdk.testing.Testing.assertValue;
import static org.junit.jupiter.api.Assertions.*;

import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Set;
import org.hiero.consensusnode.requestTypes.BasicRequestType;
import org.hiero.consensusnode.requestTypes.RequestType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

/// Tests of `TransactionFeeSchedule`, generated from the specs.
@DisplayName("TransactionFeeSchedule")
class TransactionFeeScheduleTest {

    @Test
    @DisplayName("creates a TransactionFeeSchedule and returns the values")
    @Tag("testId:consensusnode.queries.fees.TransactionFeeSchedule/create")
    void shouldCreateAndReturnTheValues() {
        final RequestType requestType = requestTypeValue();
        final Set<FeeData> fees = feesValue();
        final TransactionFeeSchedule subject = new TransactionFeeSchedule(requestType, fees);
        assertValue(requestType, subject.requestType());
        assertValue(fees, subject.fees());
    }

    @Test
    @DisplayName("rejects null for requestType")
    @Tag("testId:consensusnode.queries.fees.TransactionFeeSchedule/requestType/rejects-null")
    void shouldRejectNullRequestType() {
        assertThrows(NullPointerException.class, () -> new TransactionFeeSchedule((RequestType) null, feesValue()));
    }

    @Test
    @DisplayName("rejects null for fees")
    @Tag("testId:consensusnode.queries.fees.TransactionFeeSchedule/fees/rejects-null")
    void shouldRejectNullFees() {
        assertThrows(NullPointerException.class, () -> new TransactionFeeSchedule(requestTypeValue(), (Set<FeeData>) null));
    }

    @Test
    @DisplayName("the constructor copies fees; the accessor returns an unmodifiable collection")
    @Tag("testId:consensusnode.queries.fees.TransactionFeeSchedule/fees/copies-in")
    void shouldCopyFeesIn() {
        final Set<FeeData> value = new LinkedHashSet<>(feesValue());
        final Set<FeeData> expected = Set.copyOf(value);
        final TransactionFeeSchedule subject = new TransactionFeeSchedule(requestTypeValue(), value);
        value.clear();
        assertValue(expected, subject.fees());
        assertThrows(UnsupportedOperationException.class, () -> Objects.requireNonNull(subject.fees()).clear());
    }

    private static RequestType requestTypeValue() {
        return BasicRequestType.NONE;
    }

    private static Set<FeeData> feesValue() {
        return Set.of(new FeeData(new FeeComponents(1L, 1L, 1L, 1L, 1L, 1L, 1L, 1L, 1L, 1L, 1L), new FeeComponents(1L, 1L, 1L, 1L, 1L, 1L, 1L, 1L, 1L, 1L, 1L), new FeeComponents(1L, 1L, 1L, 1L, 1L, 1L, 1L, 1L, 1L, 1L, 1L), BasicFeeSubType.DEFAULT));
    }

    private static TransactionFeeSchedule create() {
        return new TransactionFeeSchedule(requestTypeValue(), feesValue());
    }
}
