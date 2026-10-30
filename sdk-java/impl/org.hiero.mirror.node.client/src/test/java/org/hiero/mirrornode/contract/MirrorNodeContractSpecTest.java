package org.hiero.mirrornode.contract;

import static org.hiero.sdk.testing.Testing.*;

import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.List;
import org.hiero.ledger.ContractId;
import org.hiero.ledger.EvmAddress;
import org.hiero.mirrornode.common.TimestampRange;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

/// Tests of the spec `mirror-node-contract`, generated from the `tests` blocks of its `## Testing` section.
@DisplayName("mirror-node-contract")
class MirrorNodeContractSpecTest {

    /// `mirrornode.contract/a-contract-keeps-its-auto-renew-period-and-its-lifecycle-timestamps`
    ///
    /// Source: the Hiero Enterprise Java test fixture contract.json (contract 0.0.10790540, auto-renew period 7776000
    /// seconds, created "1785753223.568147754", expiration "1793529223.568147754", timestamp range from the creation without
    /// an end, memo "0.0.10790537", nonce 1, max. automatic token associations 0); millisecond precision.
    @Test
    @DisplayName("a contract keeps its auto-renew period and its lifecycle timestamps")
    @Tag("testId:mirrornode.contract/a-contract-keeps-its-auto-renew-period-and-its-lifecycle-timestamps")
    void aContractKeepsItsAutoRenewPeriodAndItsLifecycleTimestamps() {
        // given
        final ZonedDateTime created = toZonedDateTime("2026-08-03T10:33:43.568Z");
        final ZonedDateTime expiration = toZonedDateTime("2026-11-01T10:33:43.568Z");

        // when
        final Contract contract = new Contract(ContractId.fromString("0.0.10790540"), null, null, toSeconds(7776000L), created, false, expiration, null, null, "0.0.10790537", 0, 1L, null, false, null, new TimestampRange(created, null), null, null);

        // then
        assertEquals(ContractId.fromString("0.0.10790540"), contract.contractId());
        assertEquals(toSeconds(7776000L), contract.autoRenewPeriod());
        assertEquals(toZonedDateTime("2026-08-03T10:33:43.568Z"), contract.createdTimestamp());
        assertEquals(toZonedDateTime("2026-11-01T10:33:43.568Z"), contract.expirationTimestamp());
        assertEquals(created, contract.timestamp().from());
        assertNull(contract.timestamp().to());
        assertEquals("0.0.10790537", contract.memo());
        assertEquals(1L, contract.nonce());
        assertFalse(contract.deleted());
    }

    /// `mirrornode.contract/a-contract-without-optional-values-has-no-admin-authority-expiration-evm-address-and-bytecode`
    ///
    /// Source: this spec (the optional values of a contract are nullable).
    @Test
    @DisplayName("a contract without optional values has no admin authority, expiration, EVM address and bytecode")
    @Tag("testId:mirrornode.contract/a-contract-without-optional-values-has-no-admin-authority-expiration-evm-address-and-bytecode")
    void aContractWithoutOptionalValuesHasNoAdminAuthorityExpirationEVMAddressAndBytecode() {
        // when
        final Contract contract = new Contract(defaultContractId(), null, null, null, null, false, null, null, null, null, null, null, null, null, null, new TimestampRange(ZonedDateTime.of(2024, 1, 1, 12, 0, 0, 0, ZoneOffset.UTC), null), null, null);

        // then
        assertNull(contract.adminAuthority());
        assertNull(contract.autoRenewAccount());
        assertNull(contract.autoRenewPeriod());
        assertNull(contract.createdTimestamp());
        assertNull(contract.expirationTimestamp());
        assertNull(contract.fileId());
        assertNull(contract.evmAddress());
        assertNull(contract.memo());
        assertNull(contract.nonce());
        assertNull(contract.obtainerId());
        assertNull(contract.permanentRemoval());
        assertNull(contract.proxyAccountId());
        assertNull(contract.bytecode());
        assertNull(contract.runtimeBytecode());
    }

    /// `mirrornode.contract/a-contract-state-keeps-its-slot-and-its-value`
    ///
    /// Source: the examples of the schemas ContractState (slot, value), EvmAddress (address) and EntityId (contract id
    /// "0.0.2") in the mirror node OpenAPI definition.
    @Test
    @DisplayName("a contract state keeps its slot and its value")
    @Tag("testId:mirrornode.contract/a-contract-state-keeps-its-slot-and-its-value")
    void aContractStateKeepsItsSlotAndItsValue() {
        // when
        final ContractState state = new ContractState(EvmAddress.fromString("0x0000000000000000000000000000000000001f41"), ContractId.fromString("0.0.2"), ZonedDateTime.of(2024, 1, 1, 12, 0, 0, 0, ZoneOffset.UTC), "0x00000000000000000000000000000000000000000000000000000000000000fa", "0x8c5be1e5ebec7d5bd14f71427d1e84f3dd0314c0f7b2291e5b200ac8c7c3b925");

        // then
        assertEquals("0x00000000000000000000000000000000000000000000000000000000000000fa", state.slot());
        assertEquals("0x8c5be1e5ebec7d5bd14f71427d1e84f3dd0314c0f7b2291e5b200ac8c7c3b925", state.value());
        assertEquals(ContractId.fromString("0.0.2"), state.contractId());
    }

    /// `mirrornode.contract/a-contract-log-of-a-result-has-no-block-transaction-and-timestamp`
    ///
    /// Source: this spec (the attributes that only the logs of the log queries have are null in the logs of a result;
    /// the topics default to an empty list).
    @Test
    @DisplayName("a contract log of a result has no block, transaction and timestamp")
    @Tag("testId:mirrornode.contract/a-contract-log-of-a-result-has-no-block-transaction-and-timestamp")
    void aContractLogOfAResultHasNoBlockTransactionAndTimestamp() {
        // when
        final ContractLog log = new ContractLog(EvmAddress.fromString("0x0000000000000000000000000000000000001f41"), null, null, null, 0, List.of(), null, null, null, null, null, null);

        // then
        assertEquals(0, log.index());
        assertEquals(0L, length(log.topics()));
        assertNull(log.blockHash());
        assertNull(log.blockNumber());
        assertNull(log.timestamp());
        assertNull(log.transactionHash());
        assertNull(log.transactionIndex());
    }

    /// `mirrornode.contract/a-contract-result-of-a-list-has-no-logs-and-no-state-changes`
    ///
    /// Source: this spec (logs and state changes are only filled by the lookups of a single result; lists default to
    /// empty); the example of ContractResult in the mirror node OpenAPI definition (gas limit 100000, result SUCCESS,
    /// status 0x1).
    @Test
    @DisplayName("a contract result of a list has no logs and no state changes")
    @Tag("testId:mirrornode.contract/a-contract-result-of-a-list-has-no-logs-and-no-state-changes")
    void aContractResultOfAListHasNoLogsAndNoStateChanges() {
        // when
        final ContractResult result = new ContractResult(List.of(), null, List.of(), null, null, null, null, null, null, null, null, List.of(), null, null, null, null, null, 100000L, null, null, "0xfebbaa29c513d124a6377246ea3506ad917d740c21a88f61a1c55ba338fc2bb1", null, null, null, null, "SUCCESS", null, "0x1", ZonedDateTime.of(2024, 1, 1, 12, 0, 0, 0, ZoneOffset.UTC), null, null, null, null, List.of(), List.of());

        // then
        assertEquals(100000L, result.gasLimit());
        assertEquals("SUCCESS", result.result());
        assertEquals(0L, length(result.logs()));
        assertEquals(0L, length(result.stateChanges()));
        assertEquals(0L, length(result.createdContractIds()));
        assertEquals(0L, length(result.accessList()));
        assertNull(result.amount());
        assertNull(result.contractId());
    }

    private static ContractId defaultContractId() {
        return new ContractId(0L, 0L, "", 1001L, null);
    }
}
