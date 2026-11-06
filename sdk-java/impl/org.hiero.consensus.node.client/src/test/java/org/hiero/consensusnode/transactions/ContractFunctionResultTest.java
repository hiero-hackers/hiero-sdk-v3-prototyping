package org.hiero.consensusnode.transactions;

import static org.hiero.sdk.testing.Testing.assertValue;
import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import org.hiero.evm.EvmAddress;
import org.hiero.hedera.Hbar;
import org.hiero.hedera.HbarUnit;
import org.hiero.ledger.AccountId;
import org.hiero.ledger.ContractId;
import org.hiero.nativeToken.NativeToken;
import org.jspecify.annotations.Nullable;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

/// Tests of `ContractFunctionResult`, generated from the specs.
@DisplayName("ContractFunctionResult")
class ContractFunctionResultTest {

    @Test
    @DisplayName("creates a ContractFunctionResult and returns the values")
    @Tag("testId:consensusnode.transactions.ContractFunctionResult/create")
    void shouldCreateAndReturnTheValues() {
        final ContractId contractId = contractIdValue();
        final @Nullable EvmAddress evmAddress = evmAddressValue();
        final byte[] result = resultValue();
        final @Nullable String errorMessage = errorMessageValue();
        final byte[] bloom = bloomValue();
        final long gasUsed = gasUsedValue();
        final long gas = gasValue();
        final NativeToken<?, ?> amount = amountValue();
        final byte[] callData = callDataValue();
        final @Nullable AccountId senderId = senderIdValue();
        final List<ContractLog> logs = logsValue();
        final List<ContractNonce> contractNonces = contractNoncesValue();
        final @Nullable Long signerNonce = signerNonceValue();
        final ContractFunctionResult subject = new ContractFunctionResult(contractId, evmAddress, result, errorMessage, bloom, gasUsed, gas, amount, callData, senderId, logs, contractNonces, signerNonce);
        assertValue(contractId, subject.contractId());
        assertValue(evmAddress, subject.evmAddress());
        assertValue(result, subject.result());
        assertValue(errorMessage, subject.errorMessage());
        assertValue(bloom, subject.bloom());
        assertValue(gasUsed, subject.gasUsed());
        assertValue(gas, subject.gas());
        assertValue(amount, subject.amount());
        assertValue(callData, subject.callData());
        assertValue(senderId, subject.senderId());
        assertValue(logs, subject.logs());
        assertValue(contractNonces, subject.contractNonces());
        assertValue(signerNonce, subject.signerNonce());
    }

    @Test
    @DisplayName("creates a ContractFunctionResult with the default values")
    @Tag("testId:consensusnode.transactions.ContractFunctionResult/create-with-defaults")
    void shouldCreateWithTheDefaultValues() {
        final ContractFunctionResult subject = new ContractFunctionResult(contractIdValue(), evmAddressValue(), resultValue(), errorMessageValue(), bloomValue(), gasUsedValue(), gasValue(), amountValue(), callDataValue(), senderIdValue(), signerNonceValue());
        assertValue(List.of(), subject.logs());
        assertValue(List.of(), subject.contractNonces());
    }

    @Test
    @DisplayName("the builder builds the values of the constructor")
    @Tag("testId:consensusnode.transactions.ContractFunctionResult/builder/builds")
    void shouldBuildTheValuesOfTheConstructor() {
        final ContractId contractId = contractIdValue();
        final @Nullable EvmAddress evmAddress = evmAddressValue();
        final byte[] result = resultValue();
        final @Nullable String errorMessage = errorMessageValue();
        final byte[] bloom = bloomValue();
        final long gasUsed = gasUsedValue();
        final long gas = gasValue();
        final NativeToken<?, ?> amount = amountValue();
        final byte[] callData = callDataValue();
        final @Nullable AccountId senderId = senderIdValue();
        final List<ContractLog> logs = logsValue();
        final List<ContractNonce> contractNonces = contractNoncesValue();
        final @Nullable Long signerNonce = signerNonceValue();
        final ContractFunctionResult subject = ContractFunctionResult.builder()
                .contractId(contractId)
                .evmAddress(evmAddress)
                .result(result)
                .errorMessage(errorMessage)
                .bloom(bloom)
                .gasUsed(gasUsed)
                .gas(gas)
                .amount(amount)
                .callData(callData)
                .senderId(senderId)
                .logs(logs)
                .contractNonces(contractNonces)
                .signerNonce(signerNonce)
                .build();
        assertValue(contractId, subject.contractId());
        assertValue(evmAddress, subject.evmAddress());
        assertValue(result, subject.result());
        assertValue(errorMessage, subject.errorMessage());
        assertValue(bloom, subject.bloom());
        assertValue(gasUsed, subject.gasUsed());
        assertValue(gas, subject.gas());
        assertValue(amount, subject.amount());
        assertValue(callData, subject.callData());
        assertValue(senderId, subject.senderId());
        assertValue(logs, subject.logs());
        assertValue(contractNonces, subject.contractNonces());
        assertValue(signerNonce, subject.signerNonce());
    }

    @Test
    @DisplayName("a value that is not set is null or its default value")
    @Tag("testId:consensusnode.transactions.ContractFunctionResult/builder/optional-values")
    void shouldBuildWithoutTheOptionalValues() {
        final ContractFunctionResult subject = ContractFunctionResult.builder()
                .contractId(contractIdValue())
                .result(resultValue())
                .bloom(bloomValue())
                .gasUsed(gasUsedValue())
                .gas(gasValue())
                .amount(amountValue())
                .callData(callDataValue())
                .build();
        assertNull(subject.evmAddress());
        assertNull(subject.errorMessage());
        assertNull(subject.senderId());
        assertValue(List.of(), subject.logs());
        assertValue(List.of(), subject.contractNonces());
        assertNull(subject.signerNonce());
    }

    @Test
    @DisplayName("the builder rejects a missing required value")
    @Tag("testId:consensusnode.transactions.ContractFunctionResult/builder/rejects-missing-value")
    void shouldRejectAMissingRequiredValue() {
        assertThrows(NullPointerException.class, () -> ContractFunctionResult.builder().build());
    }

    @Test
    @DisplayName("rejects null for contractId")
    @Tag("testId:consensusnode.transactions.ContractFunctionResult/contractId/rejects-null")
    void shouldRejectNullContractId() {
        assertThrows(NullPointerException.class, () -> new ContractFunctionResult((ContractId) null, evmAddressValue(), resultValue(), errorMessageValue(), bloomValue(), gasUsedValue(), gasValue(), amountValue(), callDataValue(), senderIdValue(), logsValue(), contractNoncesValue(), signerNonceValue()));
    }

    @Test
    @DisplayName("rejects null for result")
    @Tag("testId:consensusnode.transactions.ContractFunctionResult/result/rejects-null")
    void shouldRejectNullResult() {
        assertThrows(NullPointerException.class, () -> new ContractFunctionResult(contractIdValue(), evmAddressValue(), (byte[]) null, errorMessageValue(), bloomValue(), gasUsedValue(), gasValue(), amountValue(), callDataValue(), senderIdValue(), logsValue(), contractNoncesValue(), signerNonceValue()));
    }

    @Test
    @DisplayName("the constructor copies result; the accessor returns a copy")
    @Tag("testId:consensusnode.transactions.ContractFunctionResult/result/copies-in")
    void shouldCopyResultIn() {
        final byte[] value = resultValue();
        final byte[] expected = value.clone();
        final ContractFunctionResult subject = new ContractFunctionResult(contractIdValue(), evmAddressValue(), value, errorMessageValue(), bloomValue(), gasUsedValue(), gasValue(), amountValue(), callDataValue(), senderIdValue(), logsValue(), contractNoncesValue(), signerNonceValue());
        value[0] = (byte) (value[0] + 1);
        assertValue(expected, subject.result());
        final byte[] returned = subject.result();
        assertNotNull(returned);
        returned[0] = (byte) (returned[0] + 1);
        assertValue(expected, subject.result());
    }

    @Test
    @DisplayName("rejects null for bloom")
    @Tag("testId:consensusnode.transactions.ContractFunctionResult/bloom/rejects-null")
    void shouldRejectNullBloom() {
        assertThrows(NullPointerException.class, () -> new ContractFunctionResult(contractIdValue(), evmAddressValue(), resultValue(), errorMessageValue(), (byte[]) null, gasUsedValue(), gasValue(), amountValue(), callDataValue(), senderIdValue(), logsValue(), contractNoncesValue(), signerNonceValue()));
    }

    @Test
    @DisplayName("the constructor copies bloom; the accessor returns a copy")
    @Tag("testId:consensusnode.transactions.ContractFunctionResult/bloom/copies-in")
    void shouldCopyBloomIn() {
        final byte[] value = bloomValue();
        final byte[] expected = value.clone();
        final ContractFunctionResult subject = new ContractFunctionResult(contractIdValue(), evmAddressValue(), resultValue(), errorMessageValue(), value, gasUsedValue(), gasValue(), amountValue(), callDataValue(), senderIdValue(), logsValue(), contractNoncesValue(), signerNonceValue());
        value[0] = (byte) (value[0] + 1);
        assertValue(expected, subject.bloom());
        final byte[] returned = subject.bloom();
        assertNotNull(returned);
        returned[0] = (byte) (returned[0] + 1);
        assertValue(expected, subject.bloom());
    }

    @Test
    @DisplayName("gasUsed: accepts the minimum (0)")
    @Tag("testId:consensusnode.transactions.ContractFunctionResult/gasUsed/accepts-at-minimum")
    void shouldAcceptGasUsedAtMinimum() {
        final long value = 0L;
        final ContractFunctionResult subject = new ContractFunctionResult(contractIdValue(), evmAddressValue(), resultValue(), errorMessageValue(), bloomValue(), value, gasValue(), amountValue(), callDataValue(), senderIdValue(), logsValue(), contractNoncesValue(), signerNonceValue());
        assertValue(value, subject.gasUsed());
    }

    @Test
    @DisplayName("gasUsed: accepts the maximum (18446744073709551615)")
    @Tag("testId:consensusnode.transactions.ContractFunctionResult/gasUsed/accepts-at-maximum")
    void shouldAcceptGasUsedAtMaximum() {
        final long value = Long.parseUnsignedLong("18446744073709551615");
        final ContractFunctionResult subject = new ContractFunctionResult(contractIdValue(), evmAddressValue(), resultValue(), errorMessageValue(), bloomValue(), value, gasValue(), amountValue(), callDataValue(), senderIdValue(), logsValue(), contractNoncesValue(), signerNonceValue());
        assertValue(value, subject.gasUsed());
    }

    @Test
    @DisplayName("gas: accepts the minimum (-9223372036854775808)")
    @Tag("testId:consensusnode.transactions.ContractFunctionResult/gas/accepts-at-minimum")
    void shouldAcceptGasAtMinimum() {
        final long value = -9223372036854775808L;
        final ContractFunctionResult subject = new ContractFunctionResult(contractIdValue(), evmAddressValue(), resultValue(), errorMessageValue(), bloomValue(), gasUsedValue(), value, amountValue(), callDataValue(), senderIdValue(), logsValue(), contractNoncesValue(), signerNonceValue());
        assertValue(value, subject.gas());
    }

    @Test
    @DisplayName("gas: accepts the maximum (9223372036854775807)")
    @Tag("testId:consensusnode.transactions.ContractFunctionResult/gas/accepts-at-maximum")
    void shouldAcceptGasAtMaximum() {
        final long value = 9223372036854775807L;
        final ContractFunctionResult subject = new ContractFunctionResult(contractIdValue(), evmAddressValue(), resultValue(), errorMessageValue(), bloomValue(), gasUsedValue(), value, amountValue(), callDataValue(), senderIdValue(), logsValue(), contractNoncesValue(), signerNonceValue());
        assertValue(value, subject.gas());
    }

    @Test
    @DisplayName("rejects null for amount")
    @Tag("testId:consensusnode.transactions.ContractFunctionResult/amount/rejects-null")
    void shouldRejectNullAmount() {
        assertThrows(NullPointerException.class, () -> new ContractFunctionResult(contractIdValue(), evmAddressValue(), resultValue(), errorMessageValue(), bloomValue(), gasUsedValue(), gasValue(), (NativeToken<?, ?>) null, callDataValue(), senderIdValue(), logsValue(), contractNoncesValue(), signerNonceValue()));
    }

    @Test
    @DisplayName("rejects null for callData")
    @Tag("testId:consensusnode.transactions.ContractFunctionResult/callData/rejects-null")
    void shouldRejectNullCallData() {
        assertThrows(NullPointerException.class, () -> new ContractFunctionResult(contractIdValue(), evmAddressValue(), resultValue(), errorMessageValue(), bloomValue(), gasUsedValue(), gasValue(), amountValue(), (byte[]) null, senderIdValue(), logsValue(), contractNoncesValue(), signerNonceValue()));
    }

    @Test
    @DisplayName("the constructor copies callData; the accessor returns a copy")
    @Tag("testId:consensusnode.transactions.ContractFunctionResult/callData/copies-in")
    void shouldCopyCallDataIn() {
        final byte[] value = callDataValue();
        final byte[] expected = value.clone();
        final ContractFunctionResult subject = new ContractFunctionResult(contractIdValue(), evmAddressValue(), resultValue(), errorMessageValue(), bloomValue(), gasUsedValue(), gasValue(), amountValue(), value, senderIdValue(), logsValue(), contractNoncesValue(), signerNonceValue());
        value[0] = (byte) (value[0] + 1);
        assertValue(expected, subject.callData());
        final byte[] returned = subject.callData();
        assertNotNull(returned);
        returned[0] = (byte) (returned[0] + 1);
        assertValue(expected, subject.callData());
    }

    @Test
    @DisplayName("rejects null for logs")
    @Tag("testId:consensusnode.transactions.ContractFunctionResult/logs/rejects-null")
    void shouldRejectNullLogs() {
        assertThrows(NullPointerException.class, () -> new ContractFunctionResult(contractIdValue(), evmAddressValue(), resultValue(), errorMessageValue(), bloomValue(), gasUsedValue(), gasValue(), amountValue(), callDataValue(), senderIdValue(), (List<ContractLog>) null, contractNoncesValue(), signerNonceValue()));
    }

    @Test
    @DisplayName("the constructor copies logs; the accessor returns an unmodifiable collection")
    @Tag("testId:consensusnode.transactions.ContractFunctionResult/logs/copies-in")
    void shouldCopyLogsIn() {
        final List<ContractLog> value = new ArrayList<>(logsValue());
        final List<ContractLog> expected = List.copyOf(value);
        final ContractFunctionResult subject = new ContractFunctionResult(contractIdValue(), evmAddressValue(), resultValue(), errorMessageValue(), bloomValue(), gasUsedValue(), gasValue(), amountValue(), callDataValue(), senderIdValue(), value, contractNoncesValue(), signerNonceValue());
        value.clear();
        assertValue(expected, subject.logs());
        assertThrows(UnsupportedOperationException.class, () -> Objects.requireNonNull(subject.logs()).clear());
    }

    @Test
    @DisplayName("rejects null for contractNonces")
    @Tag("testId:consensusnode.transactions.ContractFunctionResult/contractNonces/rejects-null")
    void shouldRejectNullContractNonces() {
        assertThrows(NullPointerException.class, () -> new ContractFunctionResult(contractIdValue(), evmAddressValue(), resultValue(), errorMessageValue(), bloomValue(), gasUsedValue(), gasValue(), amountValue(), callDataValue(), senderIdValue(), logsValue(), (List<ContractNonce>) null, signerNonceValue()));
    }

    @Test
    @DisplayName("the constructor copies contractNonces; the accessor returns an unmodifiable collection")
    @Tag("testId:consensusnode.transactions.ContractFunctionResult/contractNonces/copies-in")
    void shouldCopyContractNoncesIn() {
        final List<ContractNonce> value = new ArrayList<>(contractNoncesValue());
        final List<ContractNonce> expected = List.copyOf(value);
        final ContractFunctionResult subject = new ContractFunctionResult(contractIdValue(), evmAddressValue(), resultValue(), errorMessageValue(), bloomValue(), gasUsedValue(), gasValue(), amountValue(), callDataValue(), senderIdValue(), logsValue(), value, signerNonceValue());
        value.clear();
        assertValue(expected, subject.contractNonces());
        assertThrows(UnsupportedOperationException.class, () -> Objects.requireNonNull(subject.contractNonces()).clear());
    }

    @Test
    @DisplayName("signerNonce: accepts the minimum (-9223372036854775808)")
    @Tag("testId:consensusnode.transactions.ContractFunctionResult/signerNonce/accepts-at-minimum")
    void shouldAcceptSignerNonceAtMinimum() {
        final @Nullable Long value = -9223372036854775808L;
        final ContractFunctionResult subject = new ContractFunctionResult(contractIdValue(), evmAddressValue(), resultValue(), errorMessageValue(), bloomValue(), gasUsedValue(), gasValue(), amountValue(), callDataValue(), senderIdValue(), logsValue(), contractNoncesValue(), value);
        assertValue(value, subject.signerNonce());
    }

    @Test
    @DisplayName("signerNonce: accepts the maximum (9223372036854775807)")
    @Tag("testId:consensusnode.transactions.ContractFunctionResult/signerNonce/accepts-at-maximum")
    void shouldAcceptSignerNonceAtMaximum() {
        final @Nullable Long value = 9223372036854775807L;
        final ContractFunctionResult subject = new ContractFunctionResult(contractIdValue(), evmAddressValue(), resultValue(), errorMessageValue(), bloomValue(), gasUsedValue(), gasValue(), amountValue(), callDataValue(), senderIdValue(), logsValue(), contractNoncesValue(), value);
        assertValue(value, subject.signerNonce());
    }

    private static ContractId contractIdValue() {
        return new ContractId(0L, 0L, "", 1001L, null);
    }

    private static EvmAddress evmAddressValue() {
        return new EvmAddress(new byte[] {1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20});
    }

    private static byte[] resultValue() {
        return new byte[] {1, 2, 3};
    }

    private static String errorMessageValue() {
        return "value";
    }

    private static byte[] bloomValue() {
        return new byte[] {1, 2, 3};
    }

    private static long gasUsedValue() {
        return 1L;
    }

    private static long gasValue() {
        return 1L;
    }

    private static NativeToken<?, ?> amountValue() {
        return new Hbar(1L, HbarUnit.TINYBAR);
    }

    private static byte[] callDataValue() {
        return new byte[] {1, 2, 3};
    }

    private static AccountId senderIdValue() {
        return new AccountId(0L, 0L, "", 1001L, null, null);
    }

    private static List<ContractLog> logsValue() {
        return List.of(new ContractLog(new ContractId(0L, 0L, "", 1001L, null), new byte[] {1, 2, 3}, List.of(new byte[] {1, 2, 3}), new byte[] {1, 2, 3}));
    }

    private static List<ContractNonce> contractNoncesValue() {
        return List.of(new ContractNonce(new ContractId(0L, 0L, "", 1001L, null), 1L));
    }

    private static long signerNonceValue() {
        return 1L;
    }

    private static ContractFunctionResult create() {
        return new ContractFunctionResult(contractIdValue(), evmAddressValue(), resultValue(), errorMessageValue(), bloomValue(), gasUsedValue(), gasValue(), amountValue(), callDataValue(), senderIdValue(), logsValue(), contractNoncesValue(), signerNonceValue());
    }
}
