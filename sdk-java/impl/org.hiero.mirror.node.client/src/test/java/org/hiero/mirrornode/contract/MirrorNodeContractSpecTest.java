package org.hiero.mirrornode.contract;

import static org.hiero.sdk.testing.Testing.*;

import java.time.Duration;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import org.hiero.ledger.ContractId;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/// Tests of the spec `mirror-node-contract`, generated from the `tests` blocks of its `## Testing` section.
@DisplayName("mirror-node-contract")
class MirrorNodeContractSpecTest {

    /// `mirrornode.contract/a-contract-keeps-its-auto-renew-period-and-its-lifecycle-timestamps`
    ///
    /// Source: the Hiero Enterprise Java test fixture contract.json (contract 0.0.10790540, auto-renew period 7776000
    /// seconds, created "1785753223.568147754", expiration "1793529223.568147754", timestamp range from the creation,
    /// memo "0.0.10790537", nonce 1, max. automatic token associations 0); millisecond precision. The fixture has no end
    /// of the timestamp range, which this spec requires, so the expiration time stands in for it.
    @Test
    @DisplayName("a contract keeps its auto-renew period and its lifecycle timestamps")
    void aContractKeepsItsAutoRenewPeriodAndItsLifecycleTimestamps() {
        // given
        final ZonedDateTime created = toZonedDateTime("2026-08-03T10:33:43.568Z");
        final ZonedDateTime expiration = toZonedDateTime("2026-11-01T10:33:43.568Z");

        // when
        final Contract contract = new Contract(ContractId.fromString("0.0.10790540"), null, null, toSeconds(7776000L), created, false, expiration, null, null, "0.0.10790537", 0, 1L, null, false, null, created, expiration, null, null);

        // then
        assertEquals(ContractId.fromString("0.0.10790540"), contract.contractId());
        assertEquals(toSeconds(7776000L), contract.autoRenewPeriod());
        assertEquals(toZonedDateTime("2026-08-03T10:33:43.568Z"), contract.createdTimestamp());
        assertEquals(toZonedDateTime("2026-11-01T10:33:43.568Z"), contract.expirationTimestamp());
        assertEquals(created, contract.fromTimestamp());
        assertEquals(expiration, contract.toTimestamp());
        assertEquals("0.0.10790537", contract.memo());
        assertEquals(1L, contract.nonce());
        assertFalse(contract.deleted());
    }

    /// `mirrornode.contract/a-contract-without-optional-values-has-no-admin-authority-expiration-evm-address-and-bytecode`
    ///
    /// Source: this spec (the optional values of a contract are nullable).
    @Test
    @DisplayName("a contract without optional values has no admin authority, expiration, EVM address and bytecode")
    void aContractWithoutOptionalValuesHasNoAdminAuthorityExpirationEVMAddressAndBytecode() {
        // given
        final ZonedDateTime time = ZonedDateTime.of(2024, 1, 1, 12, 0, 0, 0, ZoneOffset.UTC);

        // when
        final Contract contract = new Contract(defaultContractId(), null, null, Duration.ofSeconds(1L), time, false, null, null, null, null, null, null, null, false, null, time, time, null, null);

        // then
        assertNull(contract.adminAuthority());
        assertNull(contract.autoRenewAccount());
        assertNull(contract.expirationTimestamp());
        assertNull(contract.evmAddress());
        assertNull(contract.memo());
        assertNull(contract.nonce());
        assertNull(contract.obtainerId());
        assertNull(contract.bytecode());
        assertNull(contract.runtimeBytecode());
    }

    private static ContractId defaultContractId() {
        return new ContractId(0L, 0L, "", 1001L, null);
    }
}
