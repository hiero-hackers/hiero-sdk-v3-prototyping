package org.hiero.mirrornode.contract;

import static org.hiero.sdk.testing.Testing.assertValue;
import static org.junit.jupiter.api.Assertions.*;

import java.time.Duration;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import org.hiero.authority.Authority;
import org.hiero.authority.PublicKeyAuthority;
import org.hiero.keys.ByteImportEncoding;
import org.hiero.keys.KeyAlgorithm;
import org.hiero.keys.KeysFactory;
import org.hiero.ledger.AccountId;
import org.hiero.ledger.Address;
import org.hiero.ledger.ContractId;
import org.hiero.ledger.EvmAddress;
import org.hiero.mirrornode.common.TimestampRange;
import org.jspecify.annotations.Nullable;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

/// Tests of `Contract`, generated from the specs.
@DisplayName("Contract")
class ContractTest {

    @Test
    @DisplayName("creates a Contract and returns the values")
    @Tag("testId:mirrornode.contract.Contract/create")
    void shouldCreateAndReturnTheValues() {
        final ContractId contractId = contractIdValue();
        final @Nullable Authority adminAuthority = adminAuthorityValue();
        final @Nullable AccountId autoRenewAccount = autoRenewAccountValue();
        final @Nullable Duration autoRenewPeriod = autoRenewPeriodValue();
        final @Nullable ZonedDateTime createdTimestamp = createdTimestampValue();
        final boolean deleted = deletedValue();
        final @Nullable ZonedDateTime expirationTimestamp = expirationTimestampValue();
        final @Nullable Address fileId = fileIdValue();
        final @Nullable EvmAddress evmAddress = evmAddressValue();
        final @Nullable String memo = memoValue();
        final @Nullable Integer maxAutomaticTokenAssociations = maxAutomaticTokenAssociationsValue();
        final @Nullable Long nonce = nonceValue();
        final @Nullable Address obtainerId = obtainerIdValue();
        final @Nullable Boolean permanentRemoval = permanentRemovalValue();
        final @Nullable AccountId proxyAccountId = proxyAccountIdValue();
        final TimestampRange timestamp = timestampValue();
        final @Nullable String bytecode = bytecodeValue();
        final @Nullable String runtimeBytecode = runtimeBytecodeValue();
        final Contract subject = new Contract(contractId, adminAuthority, autoRenewAccount, autoRenewPeriod, createdTimestamp, deleted, expirationTimestamp, fileId, evmAddress, memo, maxAutomaticTokenAssociations, nonce, obtainerId, permanentRemoval, proxyAccountId, timestamp, bytecode, runtimeBytecode);
        assertValue(contractId, subject.contractId());
        assertValue(adminAuthority, subject.adminAuthority());
        assertValue(autoRenewAccount, subject.autoRenewAccount());
        assertValue(autoRenewPeriod, subject.autoRenewPeriod());
        assertValue(createdTimestamp, subject.createdTimestamp());
        assertValue(deleted, subject.deleted());
        assertValue(expirationTimestamp, subject.expirationTimestamp());
        assertValue(fileId, subject.fileId());
        assertValue(evmAddress, subject.evmAddress());
        assertValue(memo, subject.memo());
        assertValue(maxAutomaticTokenAssociations, subject.maxAutomaticTokenAssociations());
        assertValue(nonce, subject.nonce());
        assertValue(obtainerId, subject.obtainerId());
        assertValue(permanentRemoval, subject.permanentRemoval());
        assertValue(proxyAccountId, subject.proxyAccountId());
        assertValue(timestamp, subject.timestamp());
        assertValue(bytecode, subject.bytecode());
        assertValue(runtimeBytecode, subject.runtimeBytecode());
    }

    @Test
    @DisplayName("the builder builds the values of the constructor")
    @Tag("testId:mirrornode.contract.Contract/builder/builds")
    void shouldBuildTheValuesOfTheConstructor() {
        final ContractId contractId = contractIdValue();
        final @Nullable Authority adminAuthority = adminAuthorityValue();
        final @Nullable AccountId autoRenewAccount = autoRenewAccountValue();
        final @Nullable Duration autoRenewPeriod = autoRenewPeriodValue();
        final @Nullable ZonedDateTime createdTimestamp = createdTimestampValue();
        final boolean deleted = deletedValue();
        final @Nullable ZonedDateTime expirationTimestamp = expirationTimestampValue();
        final @Nullable Address fileId = fileIdValue();
        final @Nullable EvmAddress evmAddress = evmAddressValue();
        final @Nullable String memo = memoValue();
        final @Nullable Integer maxAutomaticTokenAssociations = maxAutomaticTokenAssociationsValue();
        final @Nullable Long nonce = nonceValue();
        final @Nullable Address obtainerId = obtainerIdValue();
        final @Nullable Boolean permanentRemoval = permanentRemovalValue();
        final @Nullable AccountId proxyAccountId = proxyAccountIdValue();
        final TimestampRange timestamp = timestampValue();
        final @Nullable String bytecode = bytecodeValue();
        final @Nullable String runtimeBytecode = runtimeBytecodeValue();
        final Contract subject = Contract.builder()
                .contractId(contractId)
                .adminAuthority(adminAuthority)
                .autoRenewAccount(autoRenewAccount)
                .autoRenewPeriod(autoRenewPeriod)
                .createdTimestamp(createdTimestamp)
                .deleted(deleted)
                .expirationTimestamp(expirationTimestamp)
                .fileId(fileId)
                .evmAddress(evmAddress)
                .memo(memo)
                .maxAutomaticTokenAssociations(maxAutomaticTokenAssociations)
                .nonce(nonce)
                .obtainerId(obtainerId)
                .permanentRemoval(permanentRemoval)
                .proxyAccountId(proxyAccountId)
                .timestamp(timestamp)
                .bytecode(bytecode)
                .runtimeBytecode(runtimeBytecode)
                .build();
        assertValue(contractId, subject.contractId());
        assertValue(adminAuthority, subject.adminAuthority());
        assertValue(autoRenewAccount, subject.autoRenewAccount());
        assertValue(autoRenewPeriod, subject.autoRenewPeriod());
        assertValue(createdTimestamp, subject.createdTimestamp());
        assertValue(deleted, subject.deleted());
        assertValue(expirationTimestamp, subject.expirationTimestamp());
        assertValue(fileId, subject.fileId());
        assertValue(evmAddress, subject.evmAddress());
        assertValue(memo, subject.memo());
        assertValue(maxAutomaticTokenAssociations, subject.maxAutomaticTokenAssociations());
        assertValue(nonce, subject.nonce());
        assertValue(obtainerId, subject.obtainerId());
        assertValue(permanentRemoval, subject.permanentRemoval());
        assertValue(proxyAccountId, subject.proxyAccountId());
        assertValue(timestamp, subject.timestamp());
        assertValue(bytecode, subject.bytecode());
        assertValue(runtimeBytecode, subject.runtimeBytecode());
    }

    @Test
    @DisplayName("a value that is not set is null or its default value")
    @Tag("testId:mirrornode.contract.Contract/builder/optional-values")
    void shouldBuildWithoutTheOptionalValues() {
        final Contract subject = Contract.builder()
                .contractId(contractIdValue())
                .deleted(deletedValue())
                .timestamp(timestampValue())
                .build();
        assertNull(subject.adminAuthority());
        assertNull(subject.autoRenewAccount());
        assertNull(subject.autoRenewPeriod());
        assertNull(subject.createdTimestamp());
        assertNull(subject.expirationTimestamp());
        assertNull(subject.fileId());
        assertNull(subject.evmAddress());
        assertNull(subject.memo());
        assertNull(subject.maxAutomaticTokenAssociations());
        assertNull(subject.nonce());
        assertNull(subject.obtainerId());
        assertNull(subject.permanentRemoval());
        assertNull(subject.proxyAccountId());
        assertNull(subject.bytecode());
        assertNull(subject.runtimeBytecode());
    }

    @Test
    @DisplayName("the builder rejects a missing required value")
    @Tag("testId:mirrornode.contract.Contract/builder/rejects-missing-value")
    void shouldRejectAMissingRequiredValue() {
        assertThrows(NullPointerException.class, () -> Contract.builder().build());
    }

    @Test
    @DisplayName("rejects null for contractId")
    @Tag("testId:mirrornode.contract.Contract/contractId/rejects-null")
    void shouldRejectNullContractId() {
        assertThrows(NullPointerException.class, () -> new Contract((ContractId) null, adminAuthorityValue(), autoRenewAccountValue(), autoRenewPeriodValue(), createdTimestampValue(), deletedValue(), expirationTimestampValue(), fileIdValue(), evmAddressValue(), memoValue(), maxAutomaticTokenAssociationsValue(), nonceValue(), obtainerIdValue(), permanentRemovalValue(), proxyAccountIdValue(), timestampValue(), bytecodeValue(), runtimeBytecodeValue()));
    }

    @Test
    @DisplayName("maxAutomaticTokenAssociations: accepts the minimum (-2147483648)")
    @Tag("testId:mirrornode.contract.Contract/maxAutomaticTokenAssociations/accepts-at-minimum")
    void shouldAcceptMaxAutomaticTokenAssociationsAtMinimum() {
        final @Nullable Integer value = Integer.MIN_VALUE;
        final Contract subject = new Contract(contractIdValue(), adminAuthorityValue(), autoRenewAccountValue(), autoRenewPeriodValue(), createdTimestampValue(), deletedValue(), expirationTimestampValue(), fileIdValue(), evmAddressValue(), memoValue(), value, nonceValue(), obtainerIdValue(), permanentRemovalValue(), proxyAccountIdValue(), timestampValue(), bytecodeValue(), runtimeBytecodeValue());
        assertValue(value, subject.maxAutomaticTokenAssociations());
    }

    @Test
    @DisplayName("maxAutomaticTokenAssociations: accepts the maximum (2147483647)")
    @Tag("testId:mirrornode.contract.Contract/maxAutomaticTokenAssociations/accepts-at-maximum")
    void shouldAcceptMaxAutomaticTokenAssociationsAtMaximum() {
        final @Nullable Integer value = 2147483647;
        final Contract subject = new Contract(contractIdValue(), adminAuthorityValue(), autoRenewAccountValue(), autoRenewPeriodValue(), createdTimestampValue(), deletedValue(), expirationTimestampValue(), fileIdValue(), evmAddressValue(), memoValue(), value, nonceValue(), obtainerIdValue(), permanentRemovalValue(), proxyAccountIdValue(), timestampValue(), bytecodeValue(), runtimeBytecodeValue());
        assertValue(value, subject.maxAutomaticTokenAssociations());
    }

    @Test
    @DisplayName("nonce: accepts the minimum (-9223372036854775808)")
    @Tag("testId:mirrornode.contract.Contract/nonce/accepts-at-minimum")
    void shouldAcceptNonceAtMinimum() {
        final @Nullable Long value = -9223372036854775808L;
        final Contract subject = new Contract(contractIdValue(), adminAuthorityValue(), autoRenewAccountValue(), autoRenewPeriodValue(), createdTimestampValue(), deletedValue(), expirationTimestampValue(), fileIdValue(), evmAddressValue(), memoValue(), maxAutomaticTokenAssociationsValue(), value, obtainerIdValue(), permanentRemovalValue(), proxyAccountIdValue(), timestampValue(), bytecodeValue(), runtimeBytecodeValue());
        assertValue(value, subject.nonce());
    }

    @Test
    @DisplayName("nonce: accepts the maximum (9223372036854775807)")
    @Tag("testId:mirrornode.contract.Contract/nonce/accepts-at-maximum")
    void shouldAcceptNonceAtMaximum() {
        final @Nullable Long value = 9223372036854775807L;
        final Contract subject = new Contract(contractIdValue(), adminAuthorityValue(), autoRenewAccountValue(), autoRenewPeriodValue(), createdTimestampValue(), deletedValue(), expirationTimestampValue(), fileIdValue(), evmAddressValue(), memoValue(), maxAutomaticTokenAssociationsValue(), value, obtainerIdValue(), permanentRemovalValue(), proxyAccountIdValue(), timestampValue(), bytecodeValue(), runtimeBytecodeValue());
        assertValue(value, subject.nonce());
    }

    @Test
    @DisplayName("rejects null for timestamp")
    @Tag("testId:mirrornode.contract.Contract/timestamp/rejects-null")
    void shouldRejectNullTimestamp() {
        assertThrows(NullPointerException.class, () -> new Contract(contractIdValue(), adminAuthorityValue(), autoRenewAccountValue(), autoRenewPeriodValue(), createdTimestampValue(), deletedValue(), expirationTimestampValue(), fileIdValue(), evmAddressValue(), memoValue(), maxAutomaticTokenAssociationsValue(), nonceValue(), obtainerIdValue(), permanentRemovalValue(), proxyAccountIdValue(), (TimestampRange) null, bytecodeValue(), runtimeBytecodeValue()));
    }

    private static ContractId contractIdValue() {
        return new ContractId(0L, 0L, "", 1001L, null);
    }

    private static Authority adminAuthorityValue() {
        return new PublicKeyAuthority(KeysFactory.createPrivateKey(KeyAlgorithm.ED25519, ByteImportEncoding.HEX, "d3671a1e98bb22f011c0e4bcf5125590e15d8f21a7017309bb558852039bc75c").createPublicKey());
    }

    private static AccountId autoRenewAccountValue() {
        return new AccountId(0L, 0L, "", 1001L, null, null);
    }

    private static Duration autoRenewPeriodValue() {
        return Duration.ofSeconds(1L);
    }

    private static ZonedDateTime createdTimestampValue() {
        return ZonedDateTime.of(2024, 1, 1, 12, 0, 0, 0, ZoneOffset.UTC);
    }

    private static boolean deletedValue() {
        return true;
    }

    private static ZonedDateTime expirationTimestampValue() {
        return ZonedDateTime.of(2024, 1, 1, 12, 0, 0, 0, ZoneOffset.UTC);
    }

    private static Address fileIdValue() {
        return new Address(0L, 0L, "", 1001L);
    }

    private static EvmAddress evmAddressValue() {
        return new EvmAddress(new byte[] {1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20});
    }

    private static String memoValue() {
        return "value";
    }

    private static int maxAutomaticTokenAssociationsValue() {
        return 1;
    }

    private static long nonceValue() {
        return 1L;
    }

    private static Address obtainerIdValue() {
        return new Address(0L, 0L, "", 1001L);
    }

    private static boolean permanentRemovalValue() {
        return true;
    }

    private static AccountId proxyAccountIdValue() {
        return new AccountId(0L, 0L, "", 1001L, null, null);
    }

    private static TimestampRange timestampValue() {
        return new TimestampRange(ZonedDateTime.of(2024, 1, 1, 12, 0, 0, 0, ZoneOffset.UTC), null);
    }

    private static String bytecodeValue() {
        return "value";
    }

    private static String runtimeBytecodeValue() {
        return "value";
    }

    private static Contract create() {
        return new Contract(contractIdValue(), adminAuthorityValue(), autoRenewAccountValue(), autoRenewPeriodValue(), createdTimestampValue(), deletedValue(), expirationTimestampValue(), fileIdValue(), evmAddressValue(), memoValue(), maxAutomaticTokenAssociationsValue(), nonceValue(), obtainerIdValue(), permanentRemovalValue(), proxyAccountIdValue(), timestampValue(), bytecodeValue(), runtimeBytecodeValue());
    }
}
