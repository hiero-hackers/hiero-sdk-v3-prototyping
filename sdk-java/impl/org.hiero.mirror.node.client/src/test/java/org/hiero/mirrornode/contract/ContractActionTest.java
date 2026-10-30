package org.hiero.mirrornode.contract;

import static org.hiero.sdk.testing.Testing.assertValue;
import static org.junit.jupiter.api.Assertions.*;

import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import org.hiero.ledger.Address;
import org.hiero.ledger.EvmAddress;
import org.jspecify.annotations.Nullable;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

/// Tests of `ContractAction`, generated from the specs.
@DisplayName("ContractAction")
class ContractActionTest {

    @Test
    @DisplayName("creates a ContractAction and returns the values")
    @Tag("testId:mirrornode.contract.ContractAction/create")
    void shouldCreateAndReturnTheValues() {
        final int callDepth = callDepthValue();
        final ContractCallOperationType callOperationType = callOperationTypeValue();
        final ContractActionType callType = callTypeValue();
        final @Nullable Address caller = callerValue();
        final ContractEntityType callerType = callerTypeValue();
        final EvmAddress from = fromValue();
        final long gas = gasValue();
        final long gasUsed = gasUsedValue();
        final int index = indexValue();
        final @Nullable String input = inputValue();
        final @Nullable Address recipient = recipientValue();
        final @Nullable ContractEntityType recipientType = recipientTypeValue();
        final @Nullable String resultData = resultDataValue();
        final ContractResultDataType resultDataType = resultDataTypeValue();
        final ZonedDateTime timestamp = timestampValue();
        final @Nullable EvmAddress to = toValue();
        final long valueArgument = valueValue();
        final ContractAction subject = new ContractAction(callDepth, callOperationType, callType, caller, callerType, from, gas, gasUsed, index, input, recipient, recipientType, resultData, resultDataType, timestamp, to, valueArgument);
        assertValue(callDepth, subject.callDepth());
        assertValue(callOperationType, subject.callOperationType());
        assertValue(callType, subject.callType());
        assertValue(caller, subject.caller());
        assertValue(callerType, subject.callerType());
        assertValue(from, subject.from());
        assertValue(gas, subject.gas());
        assertValue(gasUsed, subject.gasUsed());
        assertValue(index, subject.index());
        assertValue(input, subject.input());
        assertValue(recipient, subject.recipient());
        assertValue(recipientType, subject.recipientType());
        assertValue(resultData, subject.resultData());
        assertValue(resultDataType, subject.resultDataType());
        assertValue(timestamp, subject.timestamp());
        assertValue(to, subject.to());
        assertValue(valueArgument, subject.value());
    }

    @Test
    @DisplayName("the builder builds the values of the constructor")
    @Tag("testId:mirrornode.contract.ContractAction/builder/builds")
    void shouldBuildTheValuesOfTheConstructor() {
        final int callDepth = callDepthValue();
        final ContractCallOperationType callOperationType = callOperationTypeValue();
        final ContractActionType callType = callTypeValue();
        final @Nullable Address caller = callerValue();
        final ContractEntityType callerType = callerTypeValue();
        final EvmAddress from = fromValue();
        final long gas = gasValue();
        final long gasUsed = gasUsedValue();
        final int index = indexValue();
        final @Nullable String input = inputValue();
        final @Nullable Address recipient = recipientValue();
        final @Nullable ContractEntityType recipientType = recipientTypeValue();
        final @Nullable String resultData = resultDataValue();
        final ContractResultDataType resultDataType = resultDataTypeValue();
        final ZonedDateTime timestamp = timestampValue();
        final @Nullable EvmAddress to = toValue();
        final long valueArgument = valueValue();
        final ContractAction subject = ContractAction.builder()
                .callDepth(callDepth)
                .callOperationType(callOperationType)
                .callType(callType)
                .caller(caller)
                .callerType(callerType)
                .from(from)
                .gas(gas)
                .gasUsed(gasUsed)
                .index(index)
                .input(input)
                .recipient(recipient)
                .recipientType(recipientType)
                .resultData(resultData)
                .resultDataType(resultDataType)
                .timestamp(timestamp)
                .to(to)
                .value(valueArgument)
                .build();
        assertValue(callDepth, subject.callDepth());
        assertValue(callOperationType, subject.callOperationType());
        assertValue(callType, subject.callType());
        assertValue(caller, subject.caller());
        assertValue(callerType, subject.callerType());
        assertValue(from, subject.from());
        assertValue(gas, subject.gas());
        assertValue(gasUsed, subject.gasUsed());
        assertValue(index, subject.index());
        assertValue(input, subject.input());
        assertValue(recipient, subject.recipient());
        assertValue(recipientType, subject.recipientType());
        assertValue(resultData, subject.resultData());
        assertValue(resultDataType, subject.resultDataType());
        assertValue(timestamp, subject.timestamp());
        assertValue(to, subject.to());
        assertValue(valueArgument, subject.value());
    }

    @Test
    @DisplayName("a value that is not set is null or its default value")
    @Tag("testId:mirrornode.contract.ContractAction/builder/optional-values")
    void shouldBuildWithoutTheOptionalValues() {
        final ContractAction subject = ContractAction.builder()
                .callDepth(callDepthValue())
                .callOperationType(callOperationTypeValue())
                .callType(callTypeValue())
                .callerType(callerTypeValue())
                .from(fromValue())
                .gas(gasValue())
                .gasUsed(gasUsedValue())
                .index(indexValue())
                .resultDataType(resultDataTypeValue())
                .timestamp(timestampValue())
                .value(valueValue())
                .build();
        assertNull(subject.caller());
        assertNull(subject.input());
        assertNull(subject.recipient());
        assertNull(subject.recipientType());
        assertNull(subject.resultData());
        assertNull(subject.to());
    }

    @Test
    @DisplayName("the builder rejects a missing required value")
    @Tag("testId:mirrornode.contract.ContractAction/builder/rejects-missing-value")
    void shouldRejectAMissingRequiredValue() {
        assertThrows(NullPointerException.class, () -> ContractAction.builder().build());
    }

    @Test
    @DisplayName("callDepth: accepts the minimum (-2147483648)")
    @Tag("testId:mirrornode.contract.ContractAction/callDepth/accepts-at-minimum")
    void shouldAcceptCallDepthAtMinimum() {
        final int value = Integer.MIN_VALUE;
        final ContractAction subject = new ContractAction(value, callOperationTypeValue(), callTypeValue(), callerValue(), callerTypeValue(), fromValue(), gasValue(), gasUsedValue(), indexValue(), inputValue(), recipientValue(), recipientTypeValue(), resultDataValue(), resultDataTypeValue(), timestampValue(), toValue(), valueValue());
        assertValue(value, subject.callDepth());
    }

    @Test
    @DisplayName("callDepth: accepts the maximum (2147483647)")
    @Tag("testId:mirrornode.contract.ContractAction/callDepth/accepts-at-maximum")
    void shouldAcceptCallDepthAtMaximum() {
        final int value = 2147483647;
        final ContractAction subject = new ContractAction(value, callOperationTypeValue(), callTypeValue(), callerValue(), callerTypeValue(), fromValue(), gasValue(), gasUsedValue(), indexValue(), inputValue(), recipientValue(), recipientTypeValue(), resultDataValue(), resultDataTypeValue(), timestampValue(), toValue(), valueValue());
        assertValue(value, subject.callDepth());
    }

    @Test
    @DisplayName("rejects null for callOperationType")
    @Tag("testId:mirrornode.contract.ContractAction/callOperationType/rejects-null")
    void shouldRejectNullCallOperationType() {
        assertThrows(NullPointerException.class, () -> new ContractAction(callDepthValue(), (ContractCallOperationType) null, callTypeValue(), callerValue(), callerTypeValue(), fromValue(), gasValue(), gasUsedValue(), indexValue(), inputValue(), recipientValue(), recipientTypeValue(), resultDataValue(), resultDataTypeValue(), timestampValue(), toValue(), valueValue()));
    }

    @Test
    @DisplayName("rejects null for callType")
    @Tag("testId:mirrornode.contract.ContractAction/callType/rejects-null")
    void shouldRejectNullCallType() {
        assertThrows(NullPointerException.class, () -> new ContractAction(callDepthValue(), callOperationTypeValue(), (ContractActionType) null, callerValue(), callerTypeValue(), fromValue(), gasValue(), gasUsedValue(), indexValue(), inputValue(), recipientValue(), recipientTypeValue(), resultDataValue(), resultDataTypeValue(), timestampValue(), toValue(), valueValue()));
    }

    @Test
    @DisplayName("rejects null for callerType")
    @Tag("testId:mirrornode.contract.ContractAction/callerType/rejects-null")
    void shouldRejectNullCallerType() {
        assertThrows(NullPointerException.class, () -> new ContractAction(callDepthValue(), callOperationTypeValue(), callTypeValue(), callerValue(), (ContractEntityType) null, fromValue(), gasValue(), gasUsedValue(), indexValue(), inputValue(), recipientValue(), recipientTypeValue(), resultDataValue(), resultDataTypeValue(), timestampValue(), toValue(), valueValue()));
    }

    @Test
    @DisplayName("rejects null for from")
    @Tag("testId:mirrornode.contract.ContractAction/from/rejects-null")
    void shouldRejectNullFrom() {
        assertThrows(NullPointerException.class, () -> new ContractAction(callDepthValue(), callOperationTypeValue(), callTypeValue(), callerValue(), callerTypeValue(), (EvmAddress) null, gasValue(), gasUsedValue(), indexValue(), inputValue(), recipientValue(), recipientTypeValue(), resultDataValue(), resultDataTypeValue(), timestampValue(), toValue(), valueValue()));
    }

    @Test
    @DisplayName("gas: accepts the minimum (-9223372036854775808)")
    @Tag("testId:mirrornode.contract.ContractAction/gas/accepts-at-minimum")
    void shouldAcceptGasAtMinimum() {
        final long value = -9223372036854775808L;
        final ContractAction subject = new ContractAction(callDepthValue(), callOperationTypeValue(), callTypeValue(), callerValue(), callerTypeValue(), fromValue(), value, gasUsedValue(), indexValue(), inputValue(), recipientValue(), recipientTypeValue(), resultDataValue(), resultDataTypeValue(), timestampValue(), toValue(), valueValue());
        assertValue(value, subject.gas());
    }

    @Test
    @DisplayName("gas: accepts the maximum (9223372036854775807)")
    @Tag("testId:mirrornode.contract.ContractAction/gas/accepts-at-maximum")
    void shouldAcceptGasAtMaximum() {
        final long value = 9223372036854775807L;
        final ContractAction subject = new ContractAction(callDepthValue(), callOperationTypeValue(), callTypeValue(), callerValue(), callerTypeValue(), fromValue(), value, gasUsedValue(), indexValue(), inputValue(), recipientValue(), recipientTypeValue(), resultDataValue(), resultDataTypeValue(), timestampValue(), toValue(), valueValue());
        assertValue(value, subject.gas());
    }

    @Test
    @DisplayName("gasUsed: accepts the minimum (-9223372036854775808)")
    @Tag("testId:mirrornode.contract.ContractAction/gasUsed/accepts-at-minimum")
    void shouldAcceptGasUsedAtMinimum() {
        final long value = -9223372036854775808L;
        final ContractAction subject = new ContractAction(callDepthValue(), callOperationTypeValue(), callTypeValue(), callerValue(), callerTypeValue(), fromValue(), gasValue(), value, indexValue(), inputValue(), recipientValue(), recipientTypeValue(), resultDataValue(), resultDataTypeValue(), timestampValue(), toValue(), valueValue());
        assertValue(value, subject.gasUsed());
    }

    @Test
    @DisplayName("gasUsed: accepts the maximum (9223372036854775807)")
    @Tag("testId:mirrornode.contract.ContractAction/gasUsed/accepts-at-maximum")
    void shouldAcceptGasUsedAtMaximum() {
        final long value = 9223372036854775807L;
        final ContractAction subject = new ContractAction(callDepthValue(), callOperationTypeValue(), callTypeValue(), callerValue(), callerTypeValue(), fromValue(), gasValue(), value, indexValue(), inputValue(), recipientValue(), recipientTypeValue(), resultDataValue(), resultDataTypeValue(), timestampValue(), toValue(), valueValue());
        assertValue(value, subject.gasUsed());
    }

    @Test
    @DisplayName("index: accepts the minimum (-2147483648)")
    @Tag("testId:mirrornode.contract.ContractAction/index/accepts-at-minimum")
    void shouldAcceptIndexAtMinimum() {
        final int value = Integer.MIN_VALUE;
        final ContractAction subject = new ContractAction(callDepthValue(), callOperationTypeValue(), callTypeValue(), callerValue(), callerTypeValue(), fromValue(), gasValue(), gasUsedValue(), value, inputValue(), recipientValue(), recipientTypeValue(), resultDataValue(), resultDataTypeValue(), timestampValue(), toValue(), valueValue());
        assertValue(value, subject.index());
    }

    @Test
    @DisplayName("index: accepts the maximum (2147483647)")
    @Tag("testId:mirrornode.contract.ContractAction/index/accepts-at-maximum")
    void shouldAcceptIndexAtMaximum() {
        final int value = 2147483647;
        final ContractAction subject = new ContractAction(callDepthValue(), callOperationTypeValue(), callTypeValue(), callerValue(), callerTypeValue(), fromValue(), gasValue(), gasUsedValue(), value, inputValue(), recipientValue(), recipientTypeValue(), resultDataValue(), resultDataTypeValue(), timestampValue(), toValue(), valueValue());
        assertValue(value, subject.index());
    }

    @Test
    @DisplayName("rejects null for resultDataType")
    @Tag("testId:mirrornode.contract.ContractAction/resultDataType/rejects-null")
    void shouldRejectNullResultDataType() {
        assertThrows(NullPointerException.class, () -> new ContractAction(callDepthValue(), callOperationTypeValue(), callTypeValue(), callerValue(), callerTypeValue(), fromValue(), gasValue(), gasUsedValue(), indexValue(), inputValue(), recipientValue(), recipientTypeValue(), resultDataValue(), (ContractResultDataType) null, timestampValue(), toValue(), valueValue()));
    }

    @Test
    @DisplayName("rejects null for timestamp")
    @Tag("testId:mirrornode.contract.ContractAction/timestamp/rejects-null")
    void shouldRejectNullTimestamp() {
        assertThrows(NullPointerException.class, () -> new ContractAction(callDepthValue(), callOperationTypeValue(), callTypeValue(), callerValue(), callerTypeValue(), fromValue(), gasValue(), gasUsedValue(), indexValue(), inputValue(), recipientValue(), recipientTypeValue(), resultDataValue(), resultDataTypeValue(), (ZonedDateTime) null, toValue(), valueValue()));
    }

    @Test
    @DisplayName("value: accepts the minimum (-9223372036854775808)")
    @Tag("testId:mirrornode.contract.ContractAction/value/accepts-at-minimum")
    void shouldAcceptValueAtMinimum() {
        final long value = -9223372036854775808L;
        final ContractAction subject = new ContractAction(callDepthValue(), callOperationTypeValue(), callTypeValue(), callerValue(), callerTypeValue(), fromValue(), gasValue(), gasUsedValue(), indexValue(), inputValue(), recipientValue(), recipientTypeValue(), resultDataValue(), resultDataTypeValue(), timestampValue(), toValue(), value);
        assertValue(value, subject.value());
    }

    @Test
    @DisplayName("value: accepts the maximum (9223372036854775807)")
    @Tag("testId:mirrornode.contract.ContractAction/value/accepts-at-maximum")
    void shouldAcceptValueAtMaximum() {
        final long value = 9223372036854775807L;
        final ContractAction subject = new ContractAction(callDepthValue(), callOperationTypeValue(), callTypeValue(), callerValue(), callerTypeValue(), fromValue(), gasValue(), gasUsedValue(), indexValue(), inputValue(), recipientValue(), recipientTypeValue(), resultDataValue(), resultDataTypeValue(), timestampValue(), toValue(), value);
        assertValue(value, subject.value());
    }

    @Test
    @DisplayName("equal values give equal objects and hash codes")
    @Tag("testId:mirrornode.contract.ContractAction/value-semantics")
    void shouldCompareByValue() {
        final ContractAction subject = create();
        final ContractAction other = create();
        assertEquals(subject, other);
        assertEquals(subject.hashCode(), other.hashCode());
    }

    private static int callDepthValue() {
        return 1;
    }

    private static ContractCallOperationType callOperationTypeValue() {
        return ContractCallOperationType.CALL;
    }

    private static ContractActionType callTypeValue() {
        return ContractActionType.NO_ACTION;
    }

    private static Address callerValue() {
        return new Address(0L, 0L, "", 1001L);
    }

    private static ContractEntityType callerTypeValue() {
        return ContractEntityType.ACCOUNT;
    }

    private static EvmAddress fromValue() {
        return new EvmAddress(new byte[] {1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20});
    }

    private static long gasValue() {
        return 1L;
    }

    private static long gasUsedValue() {
        return 1L;
    }

    private static int indexValue() {
        return 1;
    }

    private static String inputValue() {
        return "value";
    }

    private static Address recipientValue() {
        return new Address(0L, 0L, "", 1001L);
    }

    private static ContractEntityType recipientTypeValue() {
        return ContractEntityType.ACCOUNT;
    }

    private static String resultDataValue() {
        return "value";
    }

    private static ContractResultDataType resultDataTypeValue() {
        return ContractResultDataType.OUTPUT;
    }

    private static ZonedDateTime timestampValue() {
        return ZonedDateTime.of(2024, 1, 1, 12, 0, 0, 0, ZoneOffset.UTC);
    }

    private static EvmAddress toValue() {
        return new EvmAddress(new byte[] {1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20});
    }

    private static long valueValue() {
        return 1L;
    }

    private static ContractAction create() {
        return new ContractAction(callDepthValue(), callOperationTypeValue(), callTypeValue(), callerValue(), callerTypeValue(), fromValue(), gasValue(), gasUsedValue(), indexValue(), inputValue(), recipientValue(), recipientTypeValue(), resultDataValue(), resultDataTypeValue(), timestampValue(), toValue(), valueValue());
    }
}
