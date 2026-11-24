package org.hiero.consensusnode.queries.contracts;

import static org.hiero.sdk.testing.Testing.*;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.math.BigInteger;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.List;
import org.hiero.evm.AbiBool;
import org.hiero.evm.AbiFunction;
import org.hiero.evm.AbiUint;
import org.hiero.evm.EvmAddress;
import org.hiero.hedera.Hbar;
import org.hiero.hedera.HbarUnit;
import org.hiero.ledger.AccountId;
import org.hiero.ledger.ContractId;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

/// Tests of the spec `queries-contracts`, generated from the `tests` blocks of its `## Testing` section.
@DisplayName("queries-contracts")
class QueriesContractsSpecTest {

    /// The contract and the sender of the contract query tests of the Java SDK (ContractCallQueryTest,
    /// ContractInfoQueryTest, ContractByteCodeQueryTest).
    private static ContractId JAVA_SDK_QUERIED_CONTRACT() {
        return ContractId.fromString("0.0.5005");
    }

    private static AccountId JAVA_SDK_SENDER() {
        return AccountId.fromString("1.2.3");
    }

    /// An EVM address of a contract. The Java SDK's ContractInfoTest uses "3", which is no EVM address; this is the address
    /// of the EvmAddress tests of the namespace evm.
    private static EvmAddress CONTRACT_EVM_ADDRESS() {
        return EvmAddress.fromString("0x1234567890abcdef1234567890abcdef12345678");
    }

    /// `consensusnode.queries.contracts/a-contract-call-query-keeps-its-attributes`
    ///
    /// Source: the Java SDK (ContractCallQueryTest.shouldSerialize and setFunctionParameters: contract 0.0.5005, gas 1541,
    /// sender 1.2.3).
    @Test
    @DisplayName("a contract call query keeps its attributes")
    @Tag("testId:consensusnode.queries.contracts/a-contract-call-query-keeps-its-attributes")
    void aContractCallQueryKeepsItsAttributes() {
        // given
        final byte[] callData = hex("182b0b");

        // when
        final ContractCallQuery query = new ContractCallQuery(JAVA_SDK_QUERIED_CONTRACT(), 1541L, callData, JAVA_SDK_SENDER());

        // then
        assertEquals(JAVA_SDK_QUERIED_CONTRACT(), query.contractId());
        assertEquals(1541L, query.gas());
        assertEquals(callData, query.callData());
        assertEquals(JAVA_SDK_SENDER(), query.senderId());
        assertNull(query.maxQueryPayment());
    }

    /// `consensusnode.queries.contracts/a-contract-call-query-without-call-data-and-sender-has-empty-call-data-and-no-sender`
    ///
    /// Source: this spec (@@default([]) of callData, @@nullable of senderId: the operator account is the sender).
    @Test
    @DisplayName("a contract call query without call data and sender has empty call data and no sender")
    @Tag("testId:consensusnode.queries.contracts/a-contract-call-query-without-call-data-and-sender-has-empty-call-data-and-no-sender")
    void aContractCallQueryWithoutCallDataAndSenderHasEmptyCallDataAndNoSender() {
        // when
        final ContractCallQuery query = new ContractCallQuery(JAVA_SDK_QUERIED_CONTRACT(), 0L, new byte[0], null);

        // then
        assertEquals(0L, length(query.callData()));
        assertNull(query.senderId());
    }

    /// `consensusnode.queries.contracts/the-call-data-of-a-contract-call-query-is-the-selector-and-the-abi-encoded-arguments`
    ///
    /// Source: the Solidity documentation, "Contract ABI Specification", section "Examples" (baz(uint32,bool) with 69 and
    /// true).
    @Test
    @DisplayName("the call data of a contract call query is the selector and the ABI-encoded arguments")
    @Tag("testId:consensusnode.queries.contracts/the-call-data-of-a-contract-call-query-is-the-selector-and-the-abi-encoded-arguments")
    void theCallDataOfAContractCallQueryIsTheSelectorAndTheABIEncodedArguments() {
        // given
        final AbiFunction baz = AbiFunction.parse("baz(uint32,bool) returns (bool)");

        // when
        final ContractCallQuery query = new ContractCallQuery(JAVA_SDK_QUERIED_CONTRACT(), 1541L, baz.encodeCall(List.of(new AbiUint(new BigInteger("69"), 32), new AbiBool(true))), null);

        // then
        assertEquals(hex(("cdcd77c0" + "0000000000000000000000000000000000000000000000000000000000000045") + "0000000000000000000000000000000000000000000000000000000000000001"), query.callData());
    }

    /// `consensusnode.queries.contracts/a-contract-call-query-rejects-negative-gas`
    ///
    /// Source: this spec (@@min(0) of gas, as for ContractExecuteTransaction).
    @Test
    @DisplayName("a contract call query rejects negative gas")
    @Tag("testId:consensusnode.queries.contracts/a-contract-call-query-rejects-negative-gas")
    void aContractCallQueryRejectsNegativeGas() {
        // when / then
        assertThrows(IllegalArgumentException.class, () -> new ContractCallQuery(JAVA_SDK_QUERIED_CONTRACT(), -1L, new byte[0], null));
    }

    /// `consensusnode.queries.contracts/the-info-and-bytecode-queries-keep-the-id-of-their-contract`
    ///
    /// Source: the Java SDK (ContractInfoQueryTest.shouldSerialize and ContractByteCodeQueryTest.shouldSerialize: contract
    /// 0.0.5005).
    @Test
    @DisplayName("the info and bytecode queries keep the id of their contract")
    @Tag("testId:consensusnode.queries.contracts/the-info-and-bytecode-queries-keep-the-id-of-their-contract")
    void theInfoAndBytecodeQueriesKeepTheIdOfTheirContract() {
        // when
        final ContractInfoQuery info = new ContractInfoQuery(JAVA_SDK_QUERIED_CONTRACT());
        final ContractBytecodeQuery bytecode = new ContractBytecodeQuery(JAVA_SDK_QUERIED_CONTRACT());

        // then
        assertEquals(JAVA_SDK_QUERIED_CONTRACT(), info.contractId());
        assertEquals(JAVA_SDK_QUERIED_CONTRACT(), bytecode.contractId());
    }

    /// `consensusnode.queries.contracts/a-contract-info-keeps-its-attributes`
    ///
    /// Source: the Java SDK (ContractInfoTest, the contract info of its fixture: contract 0.0.1, account 0.0.2, expiration
    /// time Instant.ofEpochMilli(4) = 1970-01-01T00:00:00.004Z, auto renew period Duration.ofDays(5) = 432000 seconds,
    /// storage 6, memo "7"). Millisecond precision.
    @Test
    @DisplayName("a contract info keeps its attributes")
    @Tag("testId:consensusnode.queries.contracts/a-contract-info-keeps-its-attributes")
    void aContractInfoKeepsItsAttributes() {
        // given
        final ZonedDateTime expirationTime = toZonedDateTime("1970-01-01T00:00:00.004Z");

        // when
        final ContractInfo info = new ContractInfo(ContractId.fromString("0.0.1"), AccountId.fromString("0.0.2"), CONTRACT_EVM_ADDRESS(), null, expirationTime, toSeconds(432000L), null, 6L, "7", defaultHbar(), false, 0, null, null, false);

        // then
        assertEquals(ContractId.fromString("0.0.1"), info.contractId());
        assertEquals(AccountId.fromString("0.0.2"), info.accountId());
        assertEquals(CONTRACT_EVM_ADDRESS(), info.evmAddress());
        assertEquals(toZonedDateTime("1970-01-01T00:00:00.004Z"), info.expirationTime());
        assertEquals(toSeconds(432000L), info.autoRenewPeriod());
        assertEquals(6L, info.storage());
        assertEquals("7", info.contractMemo());
    }

    /// `consensusnode.queries.contracts/a-contract-info-has-these-defaults`
    ///
    /// Source: this spec (the defaults of ContractInfo).
    @Test
    @DisplayName("a contract info has these defaults")
    @Tag("testId:consensusnode.queries.contracts/a-contract-info-has-these-defaults")
    void aContractInfoHasTheseDefaults() {
        // when
        final ContractInfo info = new ContractInfo(defaultContractId(), defaultAccountId(), CONTRACT_EVM_ADDRESS(), null, ZonedDateTime.of(2024, 1, 1, 12, 0, 0, 0, ZoneOffset.UTC), null, null, 0L, "", defaultHbar(), false, 0, null, null, false);

        // then
        assertNull(info.adminAuthority());
        assertNull(info.autoRenewPeriod());
        assertNull(info.autoRenewAccount());
        assertEquals("", info.contractMemo());
        assertFalse(info.deleted());
        assertEquals(0, info.maxAutomaticTokenAssociations());
        assertNull(info.stakedAccountId());
        assertNull(info.stakedNodeId());
        assertFalse(info.declineStakingReward());
    }

    /// `consensusnode.queries.contracts/a-contract-info-rejects-being-staked-to-an-account-and-to-a-node`
    ///
    /// Source: this spec (@@oneOrNoneOf(stakedAccountId, stakedNodeId)); HAPI basic_types.proto (StakingInfo.staked_id is a
    /// oneof).
    @Test
    @DisplayName("a contract info rejects being staked to an account and to a node")
    @Tag("testId:consensusnode.queries.contracts/a-contract-info-rejects-being-staked-to-an-account-and-to-a-node")
    void aContractInfoRejectsBeingStakedToAnAccountAndToANode() {
        // when / then
        assertThrows(IllegalArgumentException.class, () -> new ContractInfo(defaultContractId(), defaultAccountId(), CONTRACT_EVM_ADDRESS(), null, ZonedDateTime.of(2024, 1, 1, 12, 0, 0, 0, ZoneOffset.UTC), null, null, 0L, "", defaultHbar(), false, 0, AccountId.fromString("0.0.3"), 4L, false));
    }

    private static Hbar defaultHbar() {
        return new Hbar(1L, HbarUnit.HBAR);
    }

    private static ContractId defaultContractId() {
        return new ContractId(0L, 0L, "", 1001L, null);
    }

    private static AccountId defaultAccountId() {
        return new AccountId(0L, 0L, "", 1001L, null, null);
    }
}
