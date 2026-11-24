package org.hiero.consensusnode.queries.contracts;

import static org.hiero.sdk.testing.Testing.assertValue;
import static org.junit.jupiter.api.Assertions.*;

import java.time.Duration;
import java.util.Set;
import org.hiero.consensusnode.client.Account;
import org.hiero.consensusnode.client.ClientFactory;
import org.hiero.consensusnode.client.ExecutionSettings;
import org.hiero.consensusnode.client.HieroClient;
import org.hiero.consensusnode.client.RetryPolicy;
import org.hiero.hedera.Hbar;
import org.hiero.hedera.HbarUnit;
import org.hiero.keys.ByteImportEncoding;
import org.hiero.keys.KeyAlgorithm;
import org.hiero.keys.KeysFactory;
import org.hiero.ledger.AccountId;
import org.hiero.ledger.ConsensusNode;
import org.hiero.ledger.ContractId;
import org.hiero.ledger.IpAddress;
import org.hiero.ledger.MirrorNode;
import org.hiero.ledger.Network;
import org.hiero.ledger.config.NetworkSetting;
import org.hiero.nativeToken.NativeToken;
import org.jspecify.annotations.Nullable;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

/// Tests of `ContractCallQuery`, generated from the specs.
@DisplayName("ContractCallQuery")
class ContractCallQueryTest {

    @Test
    @DisplayName("creates a ContractCallQuery and returns the values")
    @Tag("testId:consensusnode.queries.contracts.ContractCallQuery/create")
    void shouldCreateAndReturnTheValues() {
        final ContractId contractId = contractIdValue();
        final long gas = gasValue();
        final byte[] callData = callDataValue();
        final @Nullable AccountId senderId = senderIdValue();
        final ContractCallQuery subject = new ContractCallQuery(contractId, gas, callData, senderId);
        assertValue(contractId, subject.contractId());
        assertValue(gas, subject.gas());
        assertValue(callData, subject.callData());
        assertValue(senderId, subject.senderId());
        assertNull(subject.retryPolicy());
        assertNull(subject.attemptTimeout());
        assertNull(subject.maxQueryPayment());
    }

    @Test
    @DisplayName("creates a ContractCallQuery with the default values")
    @Tag("testId:consensusnode.queries.contracts.ContractCallQuery/create-with-defaults")
    void shouldCreateWithTheDefaultValues() {
        final ContractCallQuery subject = new ContractCallQuery(contractIdValue(), gasValue(), senderIdValue());
        assertValue(new byte[0], subject.callData());
    }

    @Test
    @DisplayName("the builder builds the values of the constructor")
    @Tag("testId:consensusnode.queries.contracts.ContractCallQuery/builder/builds")
    void shouldBuildTheValuesOfTheConstructor() {
        final ContractId contractId = contractIdValue();
        final long gas = gasValue();
        final byte[] callData = callDataValue();
        final @Nullable AccountId senderId = senderIdValue();
        final ContractCallQuery subject = ContractCallQuery.builder()
                .contractId(contractId)
                .gas(gas)
                .callData(callData)
                .senderId(senderId)
                .build();
        assertValue(contractId, subject.contractId());
        assertValue(gas, subject.gas());
        assertValue(callData, subject.callData());
        assertValue(senderId, subject.senderId());
    }

    @Test
    @DisplayName("a value that is not set is null or its default value")
    @Tag("testId:consensusnode.queries.contracts.ContractCallQuery/builder/optional-values")
    void shouldBuildWithoutTheOptionalValues() {
        final ContractCallQuery subject = ContractCallQuery.builder()
                .contractId(contractIdValue())
                .gas(gasValue())
                .build();
        assertValue(new byte[0], subject.callData());
        assertNull(subject.senderId());
    }

    @Test
    @DisplayName("the builder rejects a missing required value")
    @Tag("testId:consensusnode.queries.contracts.ContractCallQuery/builder/rejects-missing-value")
    void shouldRejectAMissingRequiredValue() {
        assertThrows(NullPointerException.class, () -> ContractCallQuery.builder().build());
    }

    @Test
    @DisplayName("rejects null for contractId")
    @Tag("testId:consensusnode.queries.contracts.ContractCallQuery/contractId/rejects-null")
    void shouldRejectNullContractId() {
        assertThrows(NullPointerException.class, () -> new ContractCallQuery((ContractId) null, gasValue(), callDataValue(), senderIdValue()));
    }

    @Test
    @DisplayName("gas: accepts the minimum (0)")
    @Tag("testId:consensusnode.queries.contracts.ContractCallQuery/gas/accepts-at-minimum")
    void shouldAcceptGasAtMinimum() {
        final long value = 0L;
        final ContractCallQuery subject = new ContractCallQuery(contractIdValue(), value, callDataValue(), senderIdValue());
        assertValue(value, subject.gas());
    }

    @Test
    @DisplayName("gas: accepts the maximum (9223372036854775807)")
    @Tag("testId:consensusnode.queries.contracts.ContractCallQuery/gas/accepts-at-maximum")
    void shouldAcceptGasAtMaximum() {
        final long value = 9223372036854775807L;
        final ContractCallQuery subject = new ContractCallQuery(contractIdValue(), value, callDataValue(), senderIdValue());
        assertValue(value, subject.gas());
    }

    @Test
    @DisplayName("gas: rejects a value below the minimum (-1)")
    @Tag("testId:consensusnode.queries.contracts.ContractCallQuery/gas/rejects-below-minimum")
    void shouldRejectGasBelowMinimum() {
        assertThrows(IllegalArgumentException.class, () -> new ContractCallQuery(contractIdValue(), -1L, callDataValue(), senderIdValue()));
    }

    @Test
    @DisplayName("rejects null for callData")
    @Tag("testId:consensusnode.queries.contracts.ContractCallQuery/callData/rejects-null")
    void shouldRejectNullCallData() {
        assertThrows(NullPointerException.class, () -> new ContractCallQuery(contractIdValue(), gasValue(), (byte[]) null, senderIdValue()));
    }

    @Test
    @DisplayName("the constructor copies callData; the accessor returns a copy")
    @Tag("testId:consensusnode.queries.contracts.ContractCallQuery/callData/copies-in")
    void shouldCopyCallDataIn() {
        final byte[] value = callDataValue();
        final byte[] expected = value.clone();
        final ContractCallQuery subject = new ContractCallQuery(contractIdValue(), gasValue(), value, senderIdValue());
        value[0] = (byte) (value[0] + 1);
        assertValue(expected, subject.callData());
        final byte[] returned = subject.callData();
        assertNotNull(returned);
        returned[0] = (byte) (returned[0] + 1);
        assertValue(expected, subject.callData());
    }

    @Test
    @DisplayName("setRetryPolicy changes retryPolicy and returns the object")
    @Tag("testId:consensusnode.queries.contracts.ContractCallQuery/retryPolicy/set")
    void shouldSetRetryPolicy() {
        final ContractCallQuery subject = create();
        final @Nullable RetryPolicy value = retryPolicyOtherValue();
        assertSame(subject, subject.setRetryPolicy(value));
        assertValue(value, subject.retryPolicy());
    }

    @Test
    @DisplayName("setRetryPolicy accepts null")
    @Tag("testId:consensusnode.queries.contracts.ContractCallQuery/retryPolicy/set-null")
    void shouldSetRetryPolicyToNull() {
        final ContractCallQuery subject = create();
        subject.setRetryPolicy((RetryPolicy) null);
        assertNull(subject.retryPolicy());
    }

    @Test
    @DisplayName("setAttemptTimeout changes attemptTimeout and returns the object")
    @Tag("testId:consensusnode.queries.contracts.ContractCallQuery/attemptTimeout/set")
    void shouldSetAttemptTimeout() {
        final ContractCallQuery subject = create();
        final @Nullable Duration value = attemptTimeoutOtherValue();
        assertSame(subject, subject.setAttemptTimeout(value));
        assertValue(value, subject.attemptTimeout());
    }

    @Test
    @DisplayName("setAttemptTimeout accepts null")
    @Tag("testId:consensusnode.queries.contracts.ContractCallQuery/attemptTimeout/set-null")
    void shouldSetAttemptTimeoutToNull() {
        final ContractCallQuery subject = create();
        subject.setAttemptTimeout((Duration) null);
        assertNull(subject.attemptTimeout());
    }

    @Test
    @DisplayName("setMaxQueryPayment changes maxQueryPayment and returns the object")
    @Tag("testId:consensusnode.queries.contracts.ContractCallQuery/maxQueryPayment/set")
    void shouldSetMaxQueryPayment() {
        final ContractCallQuery subject = create();
        final @Nullable NativeToken<?, ?> value = maxQueryPaymentOtherValue();
        assertSame(subject, subject.setMaxQueryPayment(value));
        assertValue(value, subject.maxQueryPayment());
    }

    @Test
    @DisplayName("setMaxQueryPayment accepts null")
    @Tag("testId:consensusnode.queries.contracts.ContractCallQuery/maxQueryPayment/set-null")
    void shouldSetMaxQueryPaymentToNull() {
        final ContractCallQuery subject = create();
        subject.setMaxQueryPayment((NativeToken<?, ?>) null);
        assertNull(subject.maxQueryPayment());
    }

    @Test
    @DisplayName("submit(client) can be called and returns a value")
    @Tag("testId:consensusnode.queries.contracts.ContractCallQuery/submit/call")
    void shouldCallSubmit() {
        assertNotNull(create().submit(clientValue()));
    }

    @Test
    @DisplayName("getCost(client) can be called and returns a value")
    @Tag("testId:consensusnode.queries.contracts.ContractCallQuery/getCost/call")
    void shouldCallGetCost() {
        assertNotNull(create().getCost(clientValue()));
    }

    private static ContractId contractIdValue() {
        return new ContractId(0L, 0L, "", 1001L, null);
    }

    private static long gasValue() {
        return 1L;
    }

    private static byte[] callDataValue() {
        return new byte[] {1, 2, 3};
    }

    private static AccountId senderIdValue() {
        return new AccountId(0L, 0L, "", 1001L, null, null);
    }

    private static ContractCallQuery create() {
        return new ContractCallQuery(contractIdValue(), gasValue(), callDataValue(), senderIdValue());
    }

    private static RetryPolicy retryPolicyOtherValue() {
        return ExecutionSettings.createDefault().retryPolicy();
    }

    private static Duration attemptTimeoutOtherValue() {
        return Duration.ofSeconds(2L);
    }

    private static NativeToken<?, ?> maxQueryPaymentOtherValue() {
        return new Hbar(2L, HbarUnit.TINYBAR);
    }

    private static HieroClient<?> clientValue() {
        return ClientFactory.createClient(new NetworkSetting(new Network<>(new byte[] {(byte) 3}, "local", HbarUnit.HBAR), Set.of(new ConsensusNode(new IpAddress(new byte[] {(byte) 127, (byte) 0, (byte) 0, (byte) 1}), 35211, new AccountId(0L, 0L, "", 3L, null, null))), Set.of(new MirrorNode("http://127.0.0.1:38081"))), Account.of(new AccountId(0L, 0L, "", 1001L, null, null), KeysFactory.createPrivateKey(KeyAlgorithm.ED25519, ByteImportEncoding.HEX, "d3671a1e98bb22f011c0e4bcf5125590e15d8f21a7017309bb558852039bc75c")));
    }
}
