package org.hiero.mirrornode.contract;

import static org.hiero.sdk.testing.Testing.assertValue;
import static org.junit.jupiter.api.Assertions.*;

import java.math.BigInteger;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import org.hiero.ledger.ContractId;
import org.hiero.ledger.EvmAddress;
import org.jspecify.annotations.Nullable;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

/// Tests of `ContractResult`, generated from the specs.
@DisplayName("ContractResult")
class ContractResultTest {

    @Test
    @DisplayName("creates a ContractResult and returns the values")
    @Tag("testId:mirrornode.contract.ContractResult/create")
    void shouldCreateAndReturnTheValues() {
        final List<AccessListEntry> accessList = accessListValue();
        final @Nullable EvmAddress address = addressValue();
        final List<AuthorizationListEntry> authorizationList = authorizationListValue();
        final @Nullable BigInteger amount = amountValue();
        final @Nullable Long blockGasUsed = blockGasUsedValue();
        final @Nullable String blockHash = blockHashValue();
        final @Nullable Long blockNumber = blockNumberValue();
        final @Nullable String bloom = bloomValue();
        final @Nullable String callResult = callResultValue();
        final @Nullable String chainId = chainIdValue();
        final @Nullable ContractId contractId = contractIdValue();
        final List<ContractId> createdContractIds = createdContractIdsValue();
        final @Nullable String errorMessage = errorMessageValue();
        final @Nullable String failedInitcode = failedInitcodeValue();
        final @Nullable EvmAddress from = fromValue();
        final @Nullable String functionParameters = functionParametersValue();
        final @Nullable Long gasConsumed = gasConsumedValue();
        final long gasLimit = gasLimitValue();
        final @Nullable String gasPrice = gasPriceValue();
        final @Nullable Long gasUsed = gasUsedValue();
        final String hash = hashValue();
        final @Nullable String maxFeePerGas = maxFeePerGasValue();
        final @Nullable String maxPriorityFeePerGas = maxPriorityFeePerGasValue();
        final @Nullable Long nonce = nonceValue();
        final @Nullable String r = rValue();
        final String result = resultValue();
        final @Nullable String s = sValue();
        final String status = statusValue();
        final ZonedDateTime timestamp = timestampValue();
        final @Nullable EvmAddress to = toValue();
        final @Nullable Long transactionIndex = transactionIndexValue();
        final @Nullable Integer type = typeValue();
        final @Nullable Integer v = vValue();
        final List<ContractLog> logs = logsValue();
        final List<ContractStateChange> stateChanges = stateChangesValue();
        final ContractResult subject = new ContractResult(accessList, address, authorizationList, amount, blockGasUsed, blockHash, blockNumber, bloom, callResult, chainId, contractId, createdContractIds, errorMessage, failedInitcode, from, functionParameters, gasConsumed, gasLimit, gasPrice, gasUsed, hash, maxFeePerGas, maxPriorityFeePerGas, nonce, r, result, s, status, timestamp, to, transactionIndex, type, v, logs, stateChanges);
        assertValue(accessList, subject.accessList());
        assertValue(address, subject.address());
        assertValue(authorizationList, subject.authorizationList());
        assertValue(amount, subject.amount());
        assertValue(blockGasUsed, subject.blockGasUsed());
        assertValue(blockHash, subject.blockHash());
        assertValue(blockNumber, subject.blockNumber());
        assertValue(bloom, subject.bloom());
        assertValue(callResult, subject.callResult());
        assertValue(chainId, subject.chainId());
        assertValue(contractId, subject.contractId());
        assertValue(createdContractIds, subject.createdContractIds());
        assertValue(errorMessage, subject.errorMessage());
        assertValue(failedInitcode, subject.failedInitcode());
        assertValue(from, subject.from());
        assertValue(functionParameters, subject.functionParameters());
        assertValue(gasConsumed, subject.gasConsumed());
        assertValue(gasLimit, subject.gasLimit());
        assertValue(gasPrice, subject.gasPrice());
        assertValue(gasUsed, subject.gasUsed());
        assertValue(hash, subject.hash());
        assertValue(maxFeePerGas, subject.maxFeePerGas());
        assertValue(maxPriorityFeePerGas, subject.maxPriorityFeePerGas());
        assertValue(nonce, subject.nonce());
        assertValue(r, subject.r());
        assertValue(result, subject.result());
        assertValue(s, subject.s());
        assertValue(status, subject.status());
        assertValue(timestamp, subject.timestamp());
        assertValue(to, subject.to());
        assertValue(transactionIndex, subject.transactionIndex());
        assertValue(type, subject.type());
        assertValue(v, subject.v());
        assertValue(logs, subject.logs());
        assertValue(stateChanges, subject.stateChanges());
    }

    @Test
    @DisplayName("creates a ContractResult with the default values")
    @Tag("testId:mirrornode.contract.ContractResult/create-with-defaults")
    void shouldCreateWithTheDefaultValues() {
        final ContractResult subject = new ContractResult(addressValue(), amountValue(), blockGasUsedValue(), blockHashValue(), blockNumberValue(), bloomValue(), callResultValue(), chainIdValue(), contractIdValue(), errorMessageValue(), failedInitcodeValue(), fromValue(), functionParametersValue(), gasConsumedValue(), gasLimitValue(), gasPriceValue(), gasUsedValue(), hashValue(), maxFeePerGasValue(), maxPriorityFeePerGasValue(), nonceValue(), rValue(), resultValue(), sValue(), statusValue(), timestampValue(), toValue(), transactionIndexValue(), typeValue(), vValue());
        assertValue(List.of(), subject.accessList());
        assertValue(List.of(), subject.authorizationList());
        assertValue(List.of(), subject.createdContractIds());
        assertValue(List.of(), subject.logs());
        assertValue(List.of(), subject.stateChanges());
    }

    @Test
    @DisplayName("the builder builds the values of the constructor")
    @Tag("testId:mirrornode.contract.ContractResult/builder/builds")
    void shouldBuildTheValuesOfTheConstructor() {
        final List<AccessListEntry> accessList = accessListValue();
        final @Nullable EvmAddress address = addressValue();
        final List<AuthorizationListEntry> authorizationList = authorizationListValue();
        final @Nullable BigInteger amount = amountValue();
        final @Nullable Long blockGasUsed = blockGasUsedValue();
        final @Nullable String blockHash = blockHashValue();
        final @Nullable Long blockNumber = blockNumberValue();
        final @Nullable String bloom = bloomValue();
        final @Nullable String callResult = callResultValue();
        final @Nullable String chainId = chainIdValue();
        final @Nullable ContractId contractId = contractIdValue();
        final List<ContractId> createdContractIds = createdContractIdsValue();
        final @Nullable String errorMessage = errorMessageValue();
        final @Nullable String failedInitcode = failedInitcodeValue();
        final @Nullable EvmAddress from = fromValue();
        final @Nullable String functionParameters = functionParametersValue();
        final @Nullable Long gasConsumed = gasConsumedValue();
        final long gasLimit = gasLimitValue();
        final @Nullable String gasPrice = gasPriceValue();
        final @Nullable Long gasUsed = gasUsedValue();
        final String hash = hashValue();
        final @Nullable String maxFeePerGas = maxFeePerGasValue();
        final @Nullable String maxPriorityFeePerGas = maxPriorityFeePerGasValue();
        final @Nullable Long nonce = nonceValue();
        final @Nullable String r = rValue();
        final String result = resultValue();
        final @Nullable String s = sValue();
        final String status = statusValue();
        final ZonedDateTime timestamp = timestampValue();
        final @Nullable EvmAddress to = toValue();
        final @Nullable Long transactionIndex = transactionIndexValue();
        final @Nullable Integer type = typeValue();
        final @Nullable Integer v = vValue();
        final List<ContractLog> logs = logsValue();
        final List<ContractStateChange> stateChanges = stateChangesValue();
        final ContractResult subject = ContractResult.builder()
                .accessList(accessList)
                .address(address)
                .authorizationList(authorizationList)
                .amount(amount)
                .blockGasUsed(blockGasUsed)
                .blockHash(blockHash)
                .blockNumber(blockNumber)
                .bloom(bloom)
                .callResult(callResult)
                .chainId(chainId)
                .contractId(contractId)
                .createdContractIds(createdContractIds)
                .errorMessage(errorMessage)
                .failedInitcode(failedInitcode)
                .from(from)
                .functionParameters(functionParameters)
                .gasConsumed(gasConsumed)
                .gasLimit(gasLimit)
                .gasPrice(gasPrice)
                .gasUsed(gasUsed)
                .hash(hash)
                .maxFeePerGas(maxFeePerGas)
                .maxPriorityFeePerGas(maxPriorityFeePerGas)
                .nonce(nonce)
                .r(r)
                .result(result)
                .s(s)
                .status(status)
                .timestamp(timestamp)
                .to(to)
                .transactionIndex(transactionIndex)
                .type(type)
                .v(v)
                .logs(logs)
                .stateChanges(stateChanges)
                .build();
        assertValue(accessList, subject.accessList());
        assertValue(address, subject.address());
        assertValue(authorizationList, subject.authorizationList());
        assertValue(amount, subject.amount());
        assertValue(blockGasUsed, subject.blockGasUsed());
        assertValue(blockHash, subject.blockHash());
        assertValue(blockNumber, subject.blockNumber());
        assertValue(bloom, subject.bloom());
        assertValue(callResult, subject.callResult());
        assertValue(chainId, subject.chainId());
        assertValue(contractId, subject.contractId());
        assertValue(createdContractIds, subject.createdContractIds());
        assertValue(errorMessage, subject.errorMessage());
        assertValue(failedInitcode, subject.failedInitcode());
        assertValue(from, subject.from());
        assertValue(functionParameters, subject.functionParameters());
        assertValue(gasConsumed, subject.gasConsumed());
        assertValue(gasLimit, subject.gasLimit());
        assertValue(gasPrice, subject.gasPrice());
        assertValue(gasUsed, subject.gasUsed());
        assertValue(hash, subject.hash());
        assertValue(maxFeePerGas, subject.maxFeePerGas());
        assertValue(maxPriorityFeePerGas, subject.maxPriorityFeePerGas());
        assertValue(nonce, subject.nonce());
        assertValue(r, subject.r());
        assertValue(result, subject.result());
        assertValue(s, subject.s());
        assertValue(status, subject.status());
        assertValue(timestamp, subject.timestamp());
        assertValue(to, subject.to());
        assertValue(transactionIndex, subject.transactionIndex());
        assertValue(type, subject.type());
        assertValue(v, subject.v());
        assertValue(logs, subject.logs());
        assertValue(stateChanges, subject.stateChanges());
    }

    @Test
    @DisplayName("a value that is not set is null or its default value")
    @Tag("testId:mirrornode.contract.ContractResult/builder/optional-values")
    void shouldBuildWithoutTheOptionalValues() {
        final ContractResult subject = ContractResult.builder()
                .gasLimit(gasLimitValue())
                .hash(hashValue())
                .result(resultValue())
                .status(statusValue())
                .timestamp(timestampValue())
                .build();
        assertValue(List.of(), subject.accessList());
        assertNull(subject.address());
        assertValue(List.of(), subject.authorizationList());
        assertNull(subject.amount());
        assertNull(subject.blockGasUsed());
        assertNull(subject.blockHash());
        assertNull(subject.blockNumber());
        assertNull(subject.bloom());
        assertNull(subject.callResult());
        assertNull(subject.chainId());
        assertNull(subject.contractId());
        assertValue(List.of(), subject.createdContractIds());
        assertNull(subject.errorMessage());
        assertNull(subject.failedInitcode());
        assertNull(subject.from());
        assertNull(subject.functionParameters());
        assertNull(subject.gasConsumed());
        assertNull(subject.gasPrice());
        assertNull(subject.gasUsed());
        assertNull(subject.maxFeePerGas());
        assertNull(subject.maxPriorityFeePerGas());
        assertNull(subject.nonce());
        assertNull(subject.r());
        assertNull(subject.s());
        assertNull(subject.to());
        assertNull(subject.transactionIndex());
        assertNull(subject.type());
        assertNull(subject.v());
        assertValue(List.of(), subject.logs());
        assertValue(List.of(), subject.stateChanges());
    }

    @Test
    @DisplayName("the builder rejects a missing required value")
    @Tag("testId:mirrornode.contract.ContractResult/builder/rejects-missing-value")
    void shouldRejectAMissingRequiredValue() {
        assertThrows(NullPointerException.class, () -> ContractResult.builder().build());
    }

    @Test
    @DisplayName("rejects null for accessList")
    @Tag("testId:mirrornode.contract.ContractResult/accessList/rejects-null")
    void shouldRejectNullAccessList() {
        assertThrows(NullPointerException.class, () -> new ContractResult((List<AccessListEntry>) null, addressValue(), authorizationListValue(), amountValue(), blockGasUsedValue(), blockHashValue(), blockNumberValue(), bloomValue(), callResultValue(), chainIdValue(), contractIdValue(), createdContractIdsValue(), errorMessageValue(), failedInitcodeValue(), fromValue(), functionParametersValue(), gasConsumedValue(), gasLimitValue(), gasPriceValue(), gasUsedValue(), hashValue(), maxFeePerGasValue(), maxPriorityFeePerGasValue(), nonceValue(), rValue(), resultValue(), sValue(), statusValue(), timestampValue(), toValue(), transactionIndexValue(), typeValue(), vValue(), logsValue(), stateChangesValue()));
    }

    @Test
    @DisplayName("the constructor copies accessList; the accessor returns an unmodifiable collection")
    @Tag("testId:mirrornode.contract.ContractResult/accessList/copies-in")
    void shouldCopyAccessListIn() {
        final List<AccessListEntry> value = new ArrayList<>(accessListValue());
        final List<AccessListEntry> expected = List.copyOf(value);
        final ContractResult subject = new ContractResult(value, addressValue(), authorizationListValue(), amountValue(), blockGasUsedValue(), blockHashValue(), blockNumberValue(), bloomValue(), callResultValue(), chainIdValue(), contractIdValue(), createdContractIdsValue(), errorMessageValue(), failedInitcodeValue(), fromValue(), functionParametersValue(), gasConsumedValue(), gasLimitValue(), gasPriceValue(), gasUsedValue(), hashValue(), maxFeePerGasValue(), maxPriorityFeePerGasValue(), nonceValue(), rValue(), resultValue(), sValue(), statusValue(), timestampValue(), toValue(), transactionIndexValue(), typeValue(), vValue(), logsValue(), stateChangesValue());
        value.clear();
        assertValue(expected, subject.accessList());
        assertThrows(UnsupportedOperationException.class, () -> Objects.requireNonNull(subject.accessList()).clear());
    }

    @Test
    @DisplayName("rejects null for authorizationList")
    @Tag("testId:mirrornode.contract.ContractResult/authorizationList/rejects-null")
    void shouldRejectNullAuthorizationList() {
        assertThrows(NullPointerException.class, () -> new ContractResult(accessListValue(), addressValue(), (List<AuthorizationListEntry>) null, amountValue(), blockGasUsedValue(), blockHashValue(), blockNumberValue(), bloomValue(), callResultValue(), chainIdValue(), contractIdValue(), createdContractIdsValue(), errorMessageValue(), failedInitcodeValue(), fromValue(), functionParametersValue(), gasConsumedValue(), gasLimitValue(), gasPriceValue(), gasUsedValue(), hashValue(), maxFeePerGasValue(), maxPriorityFeePerGasValue(), nonceValue(), rValue(), resultValue(), sValue(), statusValue(), timestampValue(), toValue(), transactionIndexValue(), typeValue(), vValue(), logsValue(), stateChangesValue()));
    }

    @Test
    @DisplayName("the constructor copies authorizationList; the accessor returns an unmodifiable collection")
    @Tag("testId:mirrornode.contract.ContractResult/authorizationList/copies-in")
    void shouldCopyAuthorizationListIn() {
        final List<AuthorizationListEntry> value = new ArrayList<>(authorizationListValue());
        final List<AuthorizationListEntry> expected = List.copyOf(value);
        final ContractResult subject = new ContractResult(accessListValue(), addressValue(), value, amountValue(), blockGasUsedValue(), blockHashValue(), blockNumberValue(), bloomValue(), callResultValue(), chainIdValue(), contractIdValue(), createdContractIdsValue(), errorMessageValue(), failedInitcodeValue(), fromValue(), functionParametersValue(), gasConsumedValue(), gasLimitValue(), gasPriceValue(), gasUsedValue(), hashValue(), maxFeePerGasValue(), maxPriorityFeePerGasValue(), nonceValue(), rValue(), resultValue(), sValue(), statusValue(), timestampValue(), toValue(), transactionIndexValue(), typeValue(), vValue(), logsValue(), stateChangesValue());
        value.clear();
        assertValue(expected, subject.authorizationList());
        assertThrows(UnsupportedOperationException.class, () -> Objects.requireNonNull(subject.authorizationList()).clear());
    }

    @Test
    @DisplayName("amount: accepts the minimum (-57896044618658097711785492504343953926634992332820282019728792003956564819968)")
    @Tag("testId:mirrornode.contract.ContractResult/amount/accepts-at-minimum")
    void shouldAcceptAmountAtMinimum() {
        final @Nullable BigInteger value = new BigInteger("-57896044618658097711785492504343953926634992332820282019728792003956564819968");
        final ContractResult subject = new ContractResult(accessListValue(), addressValue(), authorizationListValue(), value, blockGasUsedValue(), blockHashValue(), blockNumberValue(), bloomValue(), callResultValue(), chainIdValue(), contractIdValue(), createdContractIdsValue(), errorMessageValue(), failedInitcodeValue(), fromValue(), functionParametersValue(), gasConsumedValue(), gasLimitValue(), gasPriceValue(), gasUsedValue(), hashValue(), maxFeePerGasValue(), maxPriorityFeePerGasValue(), nonceValue(), rValue(), resultValue(), sValue(), statusValue(), timestampValue(), toValue(), transactionIndexValue(), typeValue(), vValue(), logsValue(), stateChangesValue());
        assertValue(value, subject.amount());
    }

    @Test
    @DisplayName("amount: accepts the maximum (57896044618658097711785492504343953926634992332820282019728792003956564819967)")
    @Tag("testId:mirrornode.contract.ContractResult/amount/accepts-at-maximum")
    void shouldAcceptAmountAtMaximum() {
        final @Nullable BigInteger value = new BigInteger("57896044618658097711785492504343953926634992332820282019728792003956564819967");
        final ContractResult subject = new ContractResult(accessListValue(), addressValue(), authorizationListValue(), value, blockGasUsedValue(), blockHashValue(), blockNumberValue(), bloomValue(), callResultValue(), chainIdValue(), contractIdValue(), createdContractIdsValue(), errorMessageValue(), failedInitcodeValue(), fromValue(), functionParametersValue(), gasConsumedValue(), gasLimitValue(), gasPriceValue(), gasUsedValue(), hashValue(), maxFeePerGasValue(), maxPriorityFeePerGasValue(), nonceValue(), rValue(), resultValue(), sValue(), statusValue(), timestampValue(), toValue(), transactionIndexValue(), typeValue(), vValue(), logsValue(), stateChangesValue());
        assertValue(value, subject.amount());
    }

    @Test
    @DisplayName("amount: rejects a value below the minimum (-57896044618658097711785492504343953926634992332820282019728792003956564819969)")
    @Tag("testId:mirrornode.contract.ContractResult/amount/rejects-below-minimum")
    void shouldRejectAmountBelowMinimum() {
        assertThrows(IllegalArgumentException.class, () -> new ContractResult(accessListValue(), addressValue(), authorizationListValue(), new BigInteger("-57896044618658097711785492504343953926634992332820282019728792003956564819969"), blockGasUsedValue(), blockHashValue(), blockNumberValue(), bloomValue(), callResultValue(), chainIdValue(), contractIdValue(), createdContractIdsValue(), errorMessageValue(), failedInitcodeValue(), fromValue(), functionParametersValue(), gasConsumedValue(), gasLimitValue(), gasPriceValue(), gasUsedValue(), hashValue(), maxFeePerGasValue(), maxPriorityFeePerGasValue(), nonceValue(), rValue(), resultValue(), sValue(), statusValue(), timestampValue(), toValue(), transactionIndexValue(), typeValue(), vValue(), logsValue(), stateChangesValue()));
    }

    @Test
    @DisplayName("amount: rejects a value above the maximum (57896044618658097711785492504343953926634992332820282019728792003956564819968)")
    @Tag("testId:mirrornode.contract.ContractResult/amount/rejects-above-maximum")
    void shouldRejectAmountAboveMaximum() {
        assertThrows(IllegalArgumentException.class, () -> new ContractResult(accessListValue(), addressValue(), authorizationListValue(), new BigInteger("57896044618658097711785492504343953926634992332820282019728792003956564819968"), blockGasUsedValue(), blockHashValue(), blockNumberValue(), bloomValue(), callResultValue(), chainIdValue(), contractIdValue(), createdContractIdsValue(), errorMessageValue(), failedInitcodeValue(), fromValue(), functionParametersValue(), gasConsumedValue(), gasLimitValue(), gasPriceValue(), gasUsedValue(), hashValue(), maxFeePerGasValue(), maxPriorityFeePerGasValue(), nonceValue(), rValue(), resultValue(), sValue(), statusValue(), timestampValue(), toValue(), transactionIndexValue(), typeValue(), vValue(), logsValue(), stateChangesValue()));
    }

    @Test
    @DisplayName("blockGasUsed: accepts the minimum (-9223372036854775808)")
    @Tag("testId:mirrornode.contract.ContractResult/blockGasUsed/accepts-at-minimum")
    void shouldAcceptBlockGasUsedAtMinimum() {
        final @Nullable Long value = -9223372036854775808L;
        final ContractResult subject = new ContractResult(accessListValue(), addressValue(), authorizationListValue(), amountValue(), value, blockHashValue(), blockNumberValue(), bloomValue(), callResultValue(), chainIdValue(), contractIdValue(), createdContractIdsValue(), errorMessageValue(), failedInitcodeValue(), fromValue(), functionParametersValue(), gasConsumedValue(), gasLimitValue(), gasPriceValue(), gasUsedValue(), hashValue(), maxFeePerGasValue(), maxPriorityFeePerGasValue(), nonceValue(), rValue(), resultValue(), sValue(), statusValue(), timestampValue(), toValue(), transactionIndexValue(), typeValue(), vValue(), logsValue(), stateChangesValue());
        assertValue(value, subject.blockGasUsed());
    }

    @Test
    @DisplayName("blockGasUsed: accepts the maximum (9223372036854775807)")
    @Tag("testId:mirrornode.contract.ContractResult/blockGasUsed/accepts-at-maximum")
    void shouldAcceptBlockGasUsedAtMaximum() {
        final @Nullable Long value = 9223372036854775807L;
        final ContractResult subject = new ContractResult(accessListValue(), addressValue(), authorizationListValue(), amountValue(), value, blockHashValue(), blockNumberValue(), bloomValue(), callResultValue(), chainIdValue(), contractIdValue(), createdContractIdsValue(), errorMessageValue(), failedInitcodeValue(), fromValue(), functionParametersValue(), gasConsumedValue(), gasLimitValue(), gasPriceValue(), gasUsedValue(), hashValue(), maxFeePerGasValue(), maxPriorityFeePerGasValue(), nonceValue(), rValue(), resultValue(), sValue(), statusValue(), timestampValue(), toValue(), transactionIndexValue(), typeValue(), vValue(), logsValue(), stateChangesValue());
        assertValue(value, subject.blockGasUsed());
    }

    @Test
    @DisplayName("blockNumber: accepts the minimum (-9223372036854775808)")
    @Tag("testId:mirrornode.contract.ContractResult/blockNumber/accepts-at-minimum")
    void shouldAcceptBlockNumberAtMinimum() {
        final @Nullable Long value = -9223372036854775808L;
        final ContractResult subject = new ContractResult(accessListValue(), addressValue(), authorizationListValue(), amountValue(), blockGasUsedValue(), blockHashValue(), value, bloomValue(), callResultValue(), chainIdValue(), contractIdValue(), createdContractIdsValue(), errorMessageValue(), failedInitcodeValue(), fromValue(), functionParametersValue(), gasConsumedValue(), gasLimitValue(), gasPriceValue(), gasUsedValue(), hashValue(), maxFeePerGasValue(), maxPriorityFeePerGasValue(), nonceValue(), rValue(), resultValue(), sValue(), statusValue(), timestampValue(), toValue(), transactionIndexValue(), typeValue(), vValue(), logsValue(), stateChangesValue());
        assertValue(value, subject.blockNumber());
    }

    @Test
    @DisplayName("blockNumber: accepts the maximum (9223372036854775807)")
    @Tag("testId:mirrornode.contract.ContractResult/blockNumber/accepts-at-maximum")
    void shouldAcceptBlockNumberAtMaximum() {
        final @Nullable Long value = 9223372036854775807L;
        final ContractResult subject = new ContractResult(accessListValue(), addressValue(), authorizationListValue(), amountValue(), blockGasUsedValue(), blockHashValue(), value, bloomValue(), callResultValue(), chainIdValue(), contractIdValue(), createdContractIdsValue(), errorMessageValue(), failedInitcodeValue(), fromValue(), functionParametersValue(), gasConsumedValue(), gasLimitValue(), gasPriceValue(), gasUsedValue(), hashValue(), maxFeePerGasValue(), maxPriorityFeePerGasValue(), nonceValue(), rValue(), resultValue(), sValue(), statusValue(), timestampValue(), toValue(), transactionIndexValue(), typeValue(), vValue(), logsValue(), stateChangesValue());
        assertValue(value, subject.blockNumber());
    }

    @Test
    @DisplayName("rejects null for createdContractIds")
    @Tag("testId:mirrornode.contract.ContractResult/createdContractIds/rejects-null")
    void shouldRejectNullCreatedContractIds() {
        assertThrows(NullPointerException.class, () -> new ContractResult(accessListValue(), addressValue(), authorizationListValue(), amountValue(), blockGasUsedValue(), blockHashValue(), blockNumberValue(), bloomValue(), callResultValue(), chainIdValue(), contractIdValue(), (List<ContractId>) null, errorMessageValue(), failedInitcodeValue(), fromValue(), functionParametersValue(), gasConsumedValue(), gasLimitValue(), gasPriceValue(), gasUsedValue(), hashValue(), maxFeePerGasValue(), maxPriorityFeePerGasValue(), nonceValue(), rValue(), resultValue(), sValue(), statusValue(), timestampValue(), toValue(), transactionIndexValue(), typeValue(), vValue(), logsValue(), stateChangesValue()));
    }

    @Test
    @DisplayName("the constructor copies createdContractIds; the accessor returns an unmodifiable collection")
    @Tag("testId:mirrornode.contract.ContractResult/createdContractIds/copies-in")
    void shouldCopyCreatedContractIdsIn() {
        final List<ContractId> value = new ArrayList<>(createdContractIdsValue());
        final List<ContractId> expected = List.copyOf(value);
        final ContractResult subject = new ContractResult(accessListValue(), addressValue(), authorizationListValue(), amountValue(), blockGasUsedValue(), blockHashValue(), blockNumberValue(), bloomValue(), callResultValue(), chainIdValue(), contractIdValue(), value, errorMessageValue(), failedInitcodeValue(), fromValue(), functionParametersValue(), gasConsumedValue(), gasLimitValue(), gasPriceValue(), gasUsedValue(), hashValue(), maxFeePerGasValue(), maxPriorityFeePerGasValue(), nonceValue(), rValue(), resultValue(), sValue(), statusValue(), timestampValue(), toValue(), transactionIndexValue(), typeValue(), vValue(), logsValue(), stateChangesValue());
        value.clear();
        assertValue(expected, subject.createdContractIds());
        assertThrows(UnsupportedOperationException.class, () -> Objects.requireNonNull(subject.createdContractIds()).clear());
    }

    @Test
    @DisplayName("gasConsumed: accepts the minimum (-9223372036854775808)")
    @Tag("testId:mirrornode.contract.ContractResult/gasConsumed/accepts-at-minimum")
    void shouldAcceptGasConsumedAtMinimum() {
        final @Nullable Long value = -9223372036854775808L;
        final ContractResult subject = new ContractResult(accessListValue(), addressValue(), authorizationListValue(), amountValue(), blockGasUsedValue(), blockHashValue(), blockNumberValue(), bloomValue(), callResultValue(), chainIdValue(), contractIdValue(), createdContractIdsValue(), errorMessageValue(), failedInitcodeValue(), fromValue(), functionParametersValue(), value, gasLimitValue(), gasPriceValue(), gasUsedValue(), hashValue(), maxFeePerGasValue(), maxPriorityFeePerGasValue(), nonceValue(), rValue(), resultValue(), sValue(), statusValue(), timestampValue(), toValue(), transactionIndexValue(), typeValue(), vValue(), logsValue(), stateChangesValue());
        assertValue(value, subject.gasConsumed());
    }

    @Test
    @DisplayName("gasConsumed: accepts the maximum (9223372036854775807)")
    @Tag("testId:mirrornode.contract.ContractResult/gasConsumed/accepts-at-maximum")
    void shouldAcceptGasConsumedAtMaximum() {
        final @Nullable Long value = 9223372036854775807L;
        final ContractResult subject = new ContractResult(accessListValue(), addressValue(), authorizationListValue(), amountValue(), blockGasUsedValue(), blockHashValue(), blockNumberValue(), bloomValue(), callResultValue(), chainIdValue(), contractIdValue(), createdContractIdsValue(), errorMessageValue(), failedInitcodeValue(), fromValue(), functionParametersValue(), value, gasLimitValue(), gasPriceValue(), gasUsedValue(), hashValue(), maxFeePerGasValue(), maxPriorityFeePerGasValue(), nonceValue(), rValue(), resultValue(), sValue(), statusValue(), timestampValue(), toValue(), transactionIndexValue(), typeValue(), vValue(), logsValue(), stateChangesValue());
        assertValue(value, subject.gasConsumed());
    }

    @Test
    @DisplayName("gasLimit: accepts the minimum (-9223372036854775808)")
    @Tag("testId:mirrornode.contract.ContractResult/gasLimit/accepts-at-minimum")
    void shouldAcceptGasLimitAtMinimum() {
        final long value = -9223372036854775808L;
        final ContractResult subject = new ContractResult(accessListValue(), addressValue(), authorizationListValue(), amountValue(), blockGasUsedValue(), blockHashValue(), blockNumberValue(), bloomValue(), callResultValue(), chainIdValue(), contractIdValue(), createdContractIdsValue(), errorMessageValue(), failedInitcodeValue(), fromValue(), functionParametersValue(), gasConsumedValue(), value, gasPriceValue(), gasUsedValue(), hashValue(), maxFeePerGasValue(), maxPriorityFeePerGasValue(), nonceValue(), rValue(), resultValue(), sValue(), statusValue(), timestampValue(), toValue(), transactionIndexValue(), typeValue(), vValue(), logsValue(), stateChangesValue());
        assertValue(value, subject.gasLimit());
    }

    @Test
    @DisplayName("gasLimit: accepts the maximum (9223372036854775807)")
    @Tag("testId:mirrornode.contract.ContractResult/gasLimit/accepts-at-maximum")
    void shouldAcceptGasLimitAtMaximum() {
        final long value = 9223372036854775807L;
        final ContractResult subject = new ContractResult(accessListValue(), addressValue(), authorizationListValue(), amountValue(), blockGasUsedValue(), blockHashValue(), blockNumberValue(), bloomValue(), callResultValue(), chainIdValue(), contractIdValue(), createdContractIdsValue(), errorMessageValue(), failedInitcodeValue(), fromValue(), functionParametersValue(), gasConsumedValue(), value, gasPriceValue(), gasUsedValue(), hashValue(), maxFeePerGasValue(), maxPriorityFeePerGasValue(), nonceValue(), rValue(), resultValue(), sValue(), statusValue(), timestampValue(), toValue(), transactionIndexValue(), typeValue(), vValue(), logsValue(), stateChangesValue());
        assertValue(value, subject.gasLimit());
    }

    @Test
    @DisplayName("gasUsed: accepts the minimum (-9223372036854775808)")
    @Tag("testId:mirrornode.contract.ContractResult/gasUsed/accepts-at-minimum")
    void shouldAcceptGasUsedAtMinimum() {
        final @Nullable Long value = -9223372036854775808L;
        final ContractResult subject = new ContractResult(accessListValue(), addressValue(), authorizationListValue(), amountValue(), blockGasUsedValue(), blockHashValue(), blockNumberValue(), bloomValue(), callResultValue(), chainIdValue(), contractIdValue(), createdContractIdsValue(), errorMessageValue(), failedInitcodeValue(), fromValue(), functionParametersValue(), gasConsumedValue(), gasLimitValue(), gasPriceValue(), value, hashValue(), maxFeePerGasValue(), maxPriorityFeePerGasValue(), nonceValue(), rValue(), resultValue(), sValue(), statusValue(), timestampValue(), toValue(), transactionIndexValue(), typeValue(), vValue(), logsValue(), stateChangesValue());
        assertValue(value, subject.gasUsed());
    }

    @Test
    @DisplayName("gasUsed: accepts the maximum (9223372036854775807)")
    @Tag("testId:mirrornode.contract.ContractResult/gasUsed/accepts-at-maximum")
    void shouldAcceptGasUsedAtMaximum() {
        final @Nullable Long value = 9223372036854775807L;
        final ContractResult subject = new ContractResult(accessListValue(), addressValue(), authorizationListValue(), amountValue(), blockGasUsedValue(), blockHashValue(), blockNumberValue(), bloomValue(), callResultValue(), chainIdValue(), contractIdValue(), createdContractIdsValue(), errorMessageValue(), failedInitcodeValue(), fromValue(), functionParametersValue(), gasConsumedValue(), gasLimitValue(), gasPriceValue(), value, hashValue(), maxFeePerGasValue(), maxPriorityFeePerGasValue(), nonceValue(), rValue(), resultValue(), sValue(), statusValue(), timestampValue(), toValue(), transactionIndexValue(), typeValue(), vValue(), logsValue(), stateChangesValue());
        assertValue(value, subject.gasUsed());
    }

    @Test
    @DisplayName("rejects null for hash")
    @Tag("testId:mirrornode.contract.ContractResult/hash/rejects-null")
    void shouldRejectNullHash() {
        assertThrows(NullPointerException.class, () -> new ContractResult(accessListValue(), addressValue(), authorizationListValue(), amountValue(), blockGasUsedValue(), blockHashValue(), blockNumberValue(), bloomValue(), callResultValue(), chainIdValue(), contractIdValue(), createdContractIdsValue(), errorMessageValue(), failedInitcodeValue(), fromValue(), functionParametersValue(), gasConsumedValue(), gasLimitValue(), gasPriceValue(), gasUsedValue(), (String) null, maxFeePerGasValue(), maxPriorityFeePerGasValue(), nonceValue(), rValue(), resultValue(), sValue(), statusValue(), timestampValue(), toValue(), transactionIndexValue(), typeValue(), vValue(), logsValue(), stateChangesValue()));
    }

    @Test
    @DisplayName("nonce: accepts the minimum (-9223372036854775808)")
    @Tag("testId:mirrornode.contract.ContractResult/nonce/accepts-at-minimum")
    void shouldAcceptNonceAtMinimum() {
        final @Nullable Long value = -9223372036854775808L;
        final ContractResult subject = new ContractResult(accessListValue(), addressValue(), authorizationListValue(), amountValue(), blockGasUsedValue(), blockHashValue(), blockNumberValue(), bloomValue(), callResultValue(), chainIdValue(), contractIdValue(), createdContractIdsValue(), errorMessageValue(), failedInitcodeValue(), fromValue(), functionParametersValue(), gasConsumedValue(), gasLimitValue(), gasPriceValue(), gasUsedValue(), hashValue(), maxFeePerGasValue(), maxPriorityFeePerGasValue(), value, rValue(), resultValue(), sValue(), statusValue(), timestampValue(), toValue(), transactionIndexValue(), typeValue(), vValue(), logsValue(), stateChangesValue());
        assertValue(value, subject.nonce());
    }

    @Test
    @DisplayName("nonce: accepts the maximum (9223372036854775807)")
    @Tag("testId:mirrornode.contract.ContractResult/nonce/accepts-at-maximum")
    void shouldAcceptNonceAtMaximum() {
        final @Nullable Long value = 9223372036854775807L;
        final ContractResult subject = new ContractResult(accessListValue(), addressValue(), authorizationListValue(), amountValue(), blockGasUsedValue(), blockHashValue(), blockNumberValue(), bloomValue(), callResultValue(), chainIdValue(), contractIdValue(), createdContractIdsValue(), errorMessageValue(), failedInitcodeValue(), fromValue(), functionParametersValue(), gasConsumedValue(), gasLimitValue(), gasPriceValue(), gasUsedValue(), hashValue(), maxFeePerGasValue(), maxPriorityFeePerGasValue(), value, rValue(), resultValue(), sValue(), statusValue(), timestampValue(), toValue(), transactionIndexValue(), typeValue(), vValue(), logsValue(), stateChangesValue());
        assertValue(value, subject.nonce());
    }

    @Test
    @DisplayName("rejects null for result")
    @Tag("testId:mirrornode.contract.ContractResult/result/rejects-null")
    void shouldRejectNullResult() {
        assertThrows(NullPointerException.class, () -> new ContractResult(accessListValue(), addressValue(), authorizationListValue(), amountValue(), blockGasUsedValue(), blockHashValue(), blockNumberValue(), bloomValue(), callResultValue(), chainIdValue(), contractIdValue(), createdContractIdsValue(), errorMessageValue(), failedInitcodeValue(), fromValue(), functionParametersValue(), gasConsumedValue(), gasLimitValue(), gasPriceValue(), gasUsedValue(), hashValue(), maxFeePerGasValue(), maxPriorityFeePerGasValue(), nonceValue(), rValue(), (String) null, sValue(), statusValue(), timestampValue(), toValue(), transactionIndexValue(), typeValue(), vValue(), logsValue(), stateChangesValue()));
    }

    @Test
    @DisplayName("rejects null for status")
    @Tag("testId:mirrornode.contract.ContractResult/status/rejects-null")
    void shouldRejectNullStatus() {
        assertThrows(NullPointerException.class, () -> new ContractResult(accessListValue(), addressValue(), authorizationListValue(), amountValue(), blockGasUsedValue(), blockHashValue(), blockNumberValue(), bloomValue(), callResultValue(), chainIdValue(), contractIdValue(), createdContractIdsValue(), errorMessageValue(), failedInitcodeValue(), fromValue(), functionParametersValue(), gasConsumedValue(), gasLimitValue(), gasPriceValue(), gasUsedValue(), hashValue(), maxFeePerGasValue(), maxPriorityFeePerGasValue(), nonceValue(), rValue(), resultValue(), sValue(), (String) null, timestampValue(), toValue(), transactionIndexValue(), typeValue(), vValue(), logsValue(), stateChangesValue()));
    }

    @Test
    @DisplayName("rejects null for timestamp")
    @Tag("testId:mirrornode.contract.ContractResult/timestamp/rejects-null")
    void shouldRejectNullTimestamp() {
        assertThrows(NullPointerException.class, () -> new ContractResult(accessListValue(), addressValue(), authorizationListValue(), amountValue(), blockGasUsedValue(), blockHashValue(), blockNumberValue(), bloomValue(), callResultValue(), chainIdValue(), contractIdValue(), createdContractIdsValue(), errorMessageValue(), failedInitcodeValue(), fromValue(), functionParametersValue(), gasConsumedValue(), gasLimitValue(), gasPriceValue(), gasUsedValue(), hashValue(), maxFeePerGasValue(), maxPriorityFeePerGasValue(), nonceValue(), rValue(), resultValue(), sValue(), statusValue(), (ZonedDateTime) null, toValue(), transactionIndexValue(), typeValue(), vValue(), logsValue(), stateChangesValue()));
    }

    @Test
    @DisplayName("transactionIndex: accepts the minimum (-9223372036854775808)")
    @Tag("testId:mirrornode.contract.ContractResult/transactionIndex/accepts-at-minimum")
    void shouldAcceptTransactionIndexAtMinimum() {
        final @Nullable Long value = -9223372036854775808L;
        final ContractResult subject = new ContractResult(accessListValue(), addressValue(), authorizationListValue(), amountValue(), blockGasUsedValue(), blockHashValue(), blockNumberValue(), bloomValue(), callResultValue(), chainIdValue(), contractIdValue(), createdContractIdsValue(), errorMessageValue(), failedInitcodeValue(), fromValue(), functionParametersValue(), gasConsumedValue(), gasLimitValue(), gasPriceValue(), gasUsedValue(), hashValue(), maxFeePerGasValue(), maxPriorityFeePerGasValue(), nonceValue(), rValue(), resultValue(), sValue(), statusValue(), timestampValue(), toValue(), value, typeValue(), vValue(), logsValue(), stateChangesValue());
        assertValue(value, subject.transactionIndex());
    }

    @Test
    @DisplayName("transactionIndex: accepts the maximum (9223372036854775807)")
    @Tag("testId:mirrornode.contract.ContractResult/transactionIndex/accepts-at-maximum")
    void shouldAcceptTransactionIndexAtMaximum() {
        final @Nullable Long value = 9223372036854775807L;
        final ContractResult subject = new ContractResult(accessListValue(), addressValue(), authorizationListValue(), amountValue(), blockGasUsedValue(), blockHashValue(), blockNumberValue(), bloomValue(), callResultValue(), chainIdValue(), contractIdValue(), createdContractIdsValue(), errorMessageValue(), failedInitcodeValue(), fromValue(), functionParametersValue(), gasConsumedValue(), gasLimitValue(), gasPriceValue(), gasUsedValue(), hashValue(), maxFeePerGasValue(), maxPriorityFeePerGasValue(), nonceValue(), rValue(), resultValue(), sValue(), statusValue(), timestampValue(), toValue(), value, typeValue(), vValue(), logsValue(), stateChangesValue());
        assertValue(value, subject.transactionIndex());
    }

    @Test
    @DisplayName("type: accepts the minimum (-2147483648)")
    @Tag("testId:mirrornode.contract.ContractResult/type/accepts-at-minimum")
    void shouldAcceptTypeAtMinimum() {
        final @Nullable Integer value = Integer.MIN_VALUE;
        final ContractResult subject = new ContractResult(accessListValue(), addressValue(), authorizationListValue(), amountValue(), blockGasUsedValue(), blockHashValue(), blockNumberValue(), bloomValue(), callResultValue(), chainIdValue(), contractIdValue(), createdContractIdsValue(), errorMessageValue(), failedInitcodeValue(), fromValue(), functionParametersValue(), gasConsumedValue(), gasLimitValue(), gasPriceValue(), gasUsedValue(), hashValue(), maxFeePerGasValue(), maxPriorityFeePerGasValue(), nonceValue(), rValue(), resultValue(), sValue(), statusValue(), timestampValue(), toValue(), transactionIndexValue(), value, vValue(), logsValue(), stateChangesValue());
        assertValue(value, subject.type());
    }

    @Test
    @DisplayName("type: accepts the maximum (2147483647)")
    @Tag("testId:mirrornode.contract.ContractResult/type/accepts-at-maximum")
    void shouldAcceptTypeAtMaximum() {
        final @Nullable Integer value = 2147483647;
        final ContractResult subject = new ContractResult(accessListValue(), addressValue(), authorizationListValue(), amountValue(), blockGasUsedValue(), blockHashValue(), blockNumberValue(), bloomValue(), callResultValue(), chainIdValue(), contractIdValue(), createdContractIdsValue(), errorMessageValue(), failedInitcodeValue(), fromValue(), functionParametersValue(), gasConsumedValue(), gasLimitValue(), gasPriceValue(), gasUsedValue(), hashValue(), maxFeePerGasValue(), maxPriorityFeePerGasValue(), nonceValue(), rValue(), resultValue(), sValue(), statusValue(), timestampValue(), toValue(), transactionIndexValue(), value, vValue(), logsValue(), stateChangesValue());
        assertValue(value, subject.type());
    }

    @Test
    @DisplayName("v: accepts the minimum (-2147483648)")
    @Tag("testId:mirrornode.contract.ContractResult/v/accepts-at-minimum")
    void shouldAcceptVAtMinimum() {
        final @Nullable Integer value = Integer.MIN_VALUE;
        final ContractResult subject = new ContractResult(accessListValue(), addressValue(), authorizationListValue(), amountValue(), blockGasUsedValue(), blockHashValue(), blockNumberValue(), bloomValue(), callResultValue(), chainIdValue(), contractIdValue(), createdContractIdsValue(), errorMessageValue(), failedInitcodeValue(), fromValue(), functionParametersValue(), gasConsumedValue(), gasLimitValue(), gasPriceValue(), gasUsedValue(), hashValue(), maxFeePerGasValue(), maxPriorityFeePerGasValue(), nonceValue(), rValue(), resultValue(), sValue(), statusValue(), timestampValue(), toValue(), transactionIndexValue(), typeValue(), value, logsValue(), stateChangesValue());
        assertValue(value, subject.v());
    }

    @Test
    @DisplayName("v: accepts the maximum (2147483647)")
    @Tag("testId:mirrornode.contract.ContractResult/v/accepts-at-maximum")
    void shouldAcceptVAtMaximum() {
        final @Nullable Integer value = 2147483647;
        final ContractResult subject = new ContractResult(accessListValue(), addressValue(), authorizationListValue(), amountValue(), blockGasUsedValue(), blockHashValue(), blockNumberValue(), bloomValue(), callResultValue(), chainIdValue(), contractIdValue(), createdContractIdsValue(), errorMessageValue(), failedInitcodeValue(), fromValue(), functionParametersValue(), gasConsumedValue(), gasLimitValue(), gasPriceValue(), gasUsedValue(), hashValue(), maxFeePerGasValue(), maxPriorityFeePerGasValue(), nonceValue(), rValue(), resultValue(), sValue(), statusValue(), timestampValue(), toValue(), transactionIndexValue(), typeValue(), value, logsValue(), stateChangesValue());
        assertValue(value, subject.v());
    }

    @Test
    @DisplayName("rejects null for logs")
    @Tag("testId:mirrornode.contract.ContractResult/logs/rejects-null")
    void shouldRejectNullLogs() {
        assertThrows(NullPointerException.class, () -> new ContractResult(accessListValue(), addressValue(), authorizationListValue(), amountValue(), blockGasUsedValue(), blockHashValue(), blockNumberValue(), bloomValue(), callResultValue(), chainIdValue(), contractIdValue(), createdContractIdsValue(), errorMessageValue(), failedInitcodeValue(), fromValue(), functionParametersValue(), gasConsumedValue(), gasLimitValue(), gasPriceValue(), gasUsedValue(), hashValue(), maxFeePerGasValue(), maxPriorityFeePerGasValue(), nonceValue(), rValue(), resultValue(), sValue(), statusValue(), timestampValue(), toValue(), transactionIndexValue(), typeValue(), vValue(), (List<ContractLog>) null, stateChangesValue()));
    }

    @Test
    @DisplayName("the constructor copies logs; the accessor returns an unmodifiable collection")
    @Tag("testId:mirrornode.contract.ContractResult/logs/copies-in")
    void shouldCopyLogsIn() {
        final List<ContractLog> value = new ArrayList<>(logsValue());
        final List<ContractLog> expected = List.copyOf(value);
        final ContractResult subject = new ContractResult(accessListValue(), addressValue(), authorizationListValue(), amountValue(), blockGasUsedValue(), blockHashValue(), blockNumberValue(), bloomValue(), callResultValue(), chainIdValue(), contractIdValue(), createdContractIdsValue(), errorMessageValue(), failedInitcodeValue(), fromValue(), functionParametersValue(), gasConsumedValue(), gasLimitValue(), gasPriceValue(), gasUsedValue(), hashValue(), maxFeePerGasValue(), maxPriorityFeePerGasValue(), nonceValue(), rValue(), resultValue(), sValue(), statusValue(), timestampValue(), toValue(), transactionIndexValue(), typeValue(), vValue(), value, stateChangesValue());
        value.clear();
        assertValue(expected, subject.logs());
        assertThrows(UnsupportedOperationException.class, () -> Objects.requireNonNull(subject.logs()).clear());
    }

    @Test
    @DisplayName("rejects null for stateChanges")
    @Tag("testId:mirrornode.contract.ContractResult/stateChanges/rejects-null")
    void shouldRejectNullStateChanges() {
        assertThrows(NullPointerException.class, () -> new ContractResult(accessListValue(), addressValue(), authorizationListValue(), amountValue(), blockGasUsedValue(), blockHashValue(), blockNumberValue(), bloomValue(), callResultValue(), chainIdValue(), contractIdValue(), createdContractIdsValue(), errorMessageValue(), failedInitcodeValue(), fromValue(), functionParametersValue(), gasConsumedValue(), gasLimitValue(), gasPriceValue(), gasUsedValue(), hashValue(), maxFeePerGasValue(), maxPriorityFeePerGasValue(), nonceValue(), rValue(), resultValue(), sValue(), statusValue(), timestampValue(), toValue(), transactionIndexValue(), typeValue(), vValue(), logsValue(), (List<ContractStateChange>) null));
    }

    @Test
    @DisplayName("the constructor copies stateChanges; the accessor returns an unmodifiable collection")
    @Tag("testId:mirrornode.contract.ContractResult/stateChanges/copies-in")
    void shouldCopyStateChangesIn() {
        final List<ContractStateChange> value = new ArrayList<>(stateChangesValue());
        final List<ContractStateChange> expected = List.copyOf(value);
        final ContractResult subject = new ContractResult(accessListValue(), addressValue(), authorizationListValue(), amountValue(), blockGasUsedValue(), blockHashValue(), blockNumberValue(), bloomValue(), callResultValue(), chainIdValue(), contractIdValue(), createdContractIdsValue(), errorMessageValue(), failedInitcodeValue(), fromValue(), functionParametersValue(), gasConsumedValue(), gasLimitValue(), gasPriceValue(), gasUsedValue(), hashValue(), maxFeePerGasValue(), maxPriorityFeePerGasValue(), nonceValue(), rValue(), resultValue(), sValue(), statusValue(), timestampValue(), toValue(), transactionIndexValue(), typeValue(), vValue(), logsValue(), value);
        value.clear();
        assertValue(expected, subject.stateChanges());
        assertThrows(UnsupportedOperationException.class, () -> Objects.requireNonNull(subject.stateChanges()).clear());
    }

    @Test
    @DisplayName("equal values give equal objects and hash codes")
    @Tag("testId:mirrornode.contract.ContractResult/value-semantics")
    void shouldCompareByValue() {
        final ContractResult subject = create();
        final ContractResult other = create();
        assertEquals(subject, other);
        assertEquals(subject.hashCode(), other.hashCode());
    }

    private static List<AccessListEntry> accessListValue() {
        return List.of(new AccessListEntry(new EvmAddress(new byte[] {1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20}), List.of("value")));
    }

    private static EvmAddress addressValue() {
        return new EvmAddress(new byte[] {1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20});
    }

    private static List<AuthorizationListEntry> authorizationListValue() {
        return List.of(new AuthorizationListEntry(new EvmAddress(new byte[] {1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20}), "value", 1L, "value", "value", "value"));
    }

    private static BigInteger amountValue() {
        return new BigInteger("1");
    }

    private static long blockGasUsedValue() {
        return 1L;
    }

    private static String blockHashValue() {
        return "value";
    }

    private static long blockNumberValue() {
        return 1L;
    }

    private static String bloomValue() {
        return "value";
    }

    private static String callResultValue() {
        return "value";
    }

    private static String chainIdValue() {
        return "value";
    }

    private static ContractId contractIdValue() {
        return new ContractId(0L, 0L, "", 1001L, null);
    }

    private static List<ContractId> createdContractIdsValue() {
        return List.of(new ContractId(0L, 0L, "", 1001L, null));
    }

    private static String errorMessageValue() {
        return "value";
    }

    private static String failedInitcodeValue() {
        return "value";
    }

    private static EvmAddress fromValue() {
        return new EvmAddress(new byte[] {1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20});
    }

    private static String functionParametersValue() {
        return "value";
    }

    private static long gasConsumedValue() {
        return 1L;
    }

    private static long gasLimitValue() {
        return 1L;
    }

    private static String gasPriceValue() {
        return "value";
    }

    private static long gasUsedValue() {
        return 1L;
    }

    private static String hashValue() {
        return "value";
    }

    private static String maxFeePerGasValue() {
        return "value";
    }

    private static String maxPriorityFeePerGasValue() {
        return "value";
    }

    private static long nonceValue() {
        return 1L;
    }

    private static String rValue() {
        return "value";
    }

    private static String resultValue() {
        return "value";
    }

    private static String sValue() {
        return "value";
    }

    private static String statusValue() {
        return "value";
    }

    private static ZonedDateTime timestampValue() {
        return ZonedDateTime.of(2024, 1, 1, 12, 0, 0, 0, ZoneOffset.UTC);
    }

    private static EvmAddress toValue() {
        return new EvmAddress(new byte[] {1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20});
    }

    private static long transactionIndexValue() {
        return 1L;
    }

    private static int typeValue() {
        return 1;
    }

    private static int vValue() {
        return 1;
    }

    private static List<ContractLog> logsValue() {
        return List.of(new ContractLog(new EvmAddress(new byte[] {1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20}), null, null, null, 1, List.of("value"), null, null, null, null, null, null));
    }

    private static List<ContractStateChange> stateChangesValue() {
        return List.of(new ContractStateChange(new EvmAddress(new byte[] {1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20}), new ContractId(0L, 0L, "", 1001L, null), "value", "value", null));
    }

    private static ContractResult create() {
        return new ContractResult(accessListValue(), addressValue(), authorizationListValue(), amountValue(), blockGasUsedValue(), blockHashValue(), blockNumberValue(), bloomValue(), callResultValue(), chainIdValue(), contractIdValue(), createdContractIdsValue(), errorMessageValue(), failedInitcodeValue(), fromValue(), functionParametersValue(), gasConsumedValue(), gasLimitValue(), gasPriceValue(), gasUsedValue(), hashValue(), maxFeePerGasValue(), maxPriorityFeePerGasValue(), nonceValue(), rValue(), resultValue(), sValue(), statusValue(), timestampValue(), toValue(), transactionIndexValue(), typeValue(), vValue(), logsValue(), stateChangesValue());
    }
}
