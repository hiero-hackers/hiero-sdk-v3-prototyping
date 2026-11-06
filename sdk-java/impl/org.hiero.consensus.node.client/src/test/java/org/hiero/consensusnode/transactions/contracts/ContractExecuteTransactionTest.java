package org.hiero.consensusnode.transactions.contracts;

import static org.hiero.sdk.testing.Testing.assertValue;
import static org.junit.jupiter.api.Assertions.*;

import java.time.Duration;
import java.util.List;
import java.util.Set;
import org.hiero.authority.Authority;
import org.hiero.authority.PublicKeyAuthority;
import org.hiero.consensusnode.client.Account;
import org.hiero.consensusnode.client.ClientFactory;
import org.hiero.consensusnode.client.HieroClient;
import org.hiero.consensusnode.client.TransactionSigner;
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

/// Tests of `ContractExecuteTransaction`, generated from the specs.
@DisplayName("ContractExecuteTransaction")
class ContractExecuteTransactionTest {

    @Test
    @DisplayName("creates a ContractExecuteTransaction and returns the values")
    @Tag("testId:consensusnode.transactions.contracts.ContractExecuteTransaction/create")
    void shouldCreateAndReturnTheValues() {
        final ContractId contractId = contractIdValue();
        final long gas = gasValue();
        final @Nullable NativeToken<?, ?> payableAmount = payableAmountValue();
        final byte[] callData = callDataValue();
        final ContractExecuteTransaction subject = new ContractExecuteTransaction(contractId, gas, payableAmount, callData);
        assertValue(contractId, subject.contractId());
        assertValue(gas, subject.gas());
        assertValue(payableAmount, subject.payableAmount());
        assertValue(callData, subject.callData());
        assertNull(subject.maxTransactionFee());
        assertNull(subject.validDuration());
        assertNull(subject.memo());
    }

    @Test
    @DisplayName("creates a ContractExecuteTransaction with the default values")
    @Tag("testId:consensusnode.transactions.contracts.ContractExecuteTransaction/create-with-defaults")
    void shouldCreateWithTheDefaultValues() {
        final ContractExecuteTransaction subject = new ContractExecuteTransaction(contractIdValue(), gasValue(), payableAmountValue());
        assertValue(new byte[0], subject.callData());
    }

    @Test
    @DisplayName("the builder builds the values of the constructor")
    @Tag("testId:consensusnode.transactions.contracts.ContractExecuteTransaction/builder/builds")
    void shouldBuildTheValuesOfTheConstructor() {
        final ContractId contractId = contractIdValue();
        final long gas = gasValue();
        final @Nullable NativeToken<?, ?> payableAmount = payableAmountValue();
        final byte[] callData = callDataValue();
        final ContractExecuteTransaction subject = ContractExecuteTransaction.builder()
                .contractId(contractId)
                .gas(gas)
                .payableAmount(payableAmount)
                .callData(callData)
                .build();
        assertValue(contractId, subject.contractId());
        assertValue(gas, subject.gas());
        assertValue(payableAmount, subject.payableAmount());
        assertValue(callData, subject.callData());
    }

    @Test
    @DisplayName("a value that is not set is null or its default value")
    @Tag("testId:consensusnode.transactions.contracts.ContractExecuteTransaction/builder/optional-values")
    void shouldBuildWithoutTheOptionalValues() {
        final ContractExecuteTransaction subject = ContractExecuteTransaction.builder()
                .contractId(contractIdValue())
                .gas(gasValue())
                .build();
        assertNull(subject.payableAmount());
        assertValue(new byte[0], subject.callData());
    }

    @Test
    @DisplayName("the builder rejects a missing required value")
    @Tag("testId:consensusnode.transactions.contracts.ContractExecuteTransaction/builder/rejects-missing-value")
    void shouldRejectAMissingRequiredValue() {
        assertThrows(NullPointerException.class, () -> ContractExecuteTransaction.builder().build());
    }

    @Test
    @DisplayName("rejects null for contractId")
    @Tag("testId:consensusnode.transactions.contracts.ContractExecuteTransaction/contractId/rejects-null")
    void shouldRejectNullContractId() {
        assertThrows(NullPointerException.class, () -> new ContractExecuteTransaction((ContractId) null, gasValue(), payableAmountValue(), callDataValue()));
    }

    @Test
    @DisplayName("gas: accepts the minimum (0)")
    @Tag("testId:consensusnode.transactions.contracts.ContractExecuteTransaction/gas/accepts-at-minimum")
    void shouldAcceptGasAtMinimum() {
        final long value = 0L;
        final ContractExecuteTransaction subject = new ContractExecuteTransaction(contractIdValue(), value, payableAmountValue(), callDataValue());
        assertValue(value, subject.gas());
    }

    @Test
    @DisplayName("gas: accepts the maximum (9223372036854775807)")
    @Tag("testId:consensusnode.transactions.contracts.ContractExecuteTransaction/gas/accepts-at-maximum")
    void shouldAcceptGasAtMaximum() {
        final long value = 9223372036854775807L;
        final ContractExecuteTransaction subject = new ContractExecuteTransaction(contractIdValue(), value, payableAmountValue(), callDataValue());
        assertValue(value, subject.gas());
    }

    @Test
    @DisplayName("gas: rejects a value below the minimum (-1)")
    @Tag("testId:consensusnode.transactions.contracts.ContractExecuteTransaction/gas/rejects-below-minimum")
    void shouldRejectGasBelowMinimum() {
        assertThrows(IllegalArgumentException.class, () -> new ContractExecuteTransaction(contractIdValue(), -1L, payableAmountValue(), callDataValue()));
    }

    @Test
    @DisplayName("rejects null for callData")
    @Tag("testId:consensusnode.transactions.contracts.ContractExecuteTransaction/callData/rejects-null")
    void shouldRejectNullCallData() {
        assertThrows(NullPointerException.class, () -> new ContractExecuteTransaction(contractIdValue(), gasValue(), payableAmountValue(), (byte[]) null));
    }

    @Test
    @DisplayName("the constructor copies callData; the accessor returns a copy")
    @Tag("testId:consensusnode.transactions.contracts.ContractExecuteTransaction/callData/copies-in")
    void shouldCopyCallDataIn() {
        final byte[] value = callDataValue();
        final byte[] expected = value.clone();
        final ContractExecuteTransaction subject = new ContractExecuteTransaction(contractIdValue(), gasValue(), payableAmountValue(), value);
        value[0] = (byte) (value[0] + 1);
        assertValue(expected, subject.callData());
        final byte[] returned = subject.callData();
        assertNotNull(returned);
        returned[0] = (byte) (returned[0] + 1);
        assertValue(expected, subject.callData());
    }

    @Test
    @DisplayName("setMaxTransactionFee changes maxTransactionFee and returns the object")
    @Tag("testId:consensusnode.transactions.contracts.ContractExecuteTransaction/maxTransactionFee/set")
    void shouldSetMaxTransactionFee() {
        final ContractExecuteTransaction subject = create();
        final @Nullable NativeToken<?, ?> value = maxTransactionFeeOtherValue();
        assertSame(subject, subject.setMaxTransactionFee(value));
        assertValue(value, subject.maxTransactionFee());
    }

    @Test
    @DisplayName("setMaxTransactionFee accepts null")
    @Tag("testId:consensusnode.transactions.contracts.ContractExecuteTransaction/maxTransactionFee/set-null")
    void shouldSetMaxTransactionFeeToNull() {
        final ContractExecuteTransaction subject = create();
        subject.setMaxTransactionFee((NativeToken<?, ?>) null);
        assertNull(subject.maxTransactionFee());
    }

    @Test
    @DisplayName("setValidDuration changes validDuration and returns the object")
    @Tag("testId:consensusnode.transactions.contracts.ContractExecuteTransaction/validDuration/set")
    void shouldSetValidDuration() {
        final ContractExecuteTransaction subject = create();
        final @Nullable Duration value = validDurationOtherValue();
        assertSame(subject, subject.setValidDuration(value));
        assertValue(value, subject.validDuration());
    }

    @Test
    @DisplayName("setValidDuration accepts null")
    @Tag("testId:consensusnode.transactions.contracts.ContractExecuteTransaction/validDuration/set-null")
    void shouldSetValidDurationToNull() {
        final ContractExecuteTransaction subject = create();
        subject.setValidDuration((Duration) null);
        assertNull(subject.validDuration());
    }

    @Test
    @DisplayName("setMemo changes memo and returns the object")
    @Tag("testId:consensusnode.transactions.contracts.ContractExecuteTransaction/memo/set")
    void shouldSetMemo() {
        final ContractExecuteTransaction subject = create();
        final @Nullable String value = memoOtherValue();
        assertSame(subject, subject.setMemo(value));
        assertValue(value, subject.memo());
    }

    @Test
    @DisplayName("setMemo accepts null")
    @Tag("testId:consensusnode.transactions.contracts.ContractExecuteTransaction/memo/set-null")
    void shouldSetMemoToNull() {
        final ContractExecuteTransaction subject = create();
        subject.setMemo((String) null);
        assertNull(subject.memo());
    }

    @Test
    @DisplayName("setMemo accepts more elements than the maximum")
    @Tag("testId:consensusnode.transactions.contracts.ContractExecuteTransaction/memo/set-accepts-at-maximum-bytes")
    void shouldSetMemoAtMaximumBytes() {
        final ContractExecuteTransaction subject = create();
        final @Nullable String value = "ääääääääääääääääääääääääääääääääääääääääääääääääää";
        subject.setMemo(value);
        assertValue(value, subject.memo());
    }

    @Test
    @DisplayName("setMemo rejects more elements than the maximum and keeps the value")
    @Tag("testId:consensusnode.transactions.contracts.ContractExecuteTransaction/memo/set-rejects-above-maximum-bytes")
    void shouldNotSetMemoAboveMaximumBytes() {
        final ContractExecuteTransaction subject = create();
        assertThrows(IllegalArgumentException.class, () -> subject.setMemo("ääääääääääääääääääääääääääääääääääääääääääääääääääa"));
        assertValue(null, subject.memo());
    }

    @Test
    @DisplayName("pack(payer, nodes) can be called and returns a value")
    @Tag("testId:consensusnode.transactions.contracts.ContractExecuteTransaction/pack/call")
    void shouldCallPack() {
        assertNotNull(create().pack(payerValue(), nodesValue()));
    }

    @Test
    @DisplayName("signWithOperator(client) can be called and returns a value")
    @Tag("testId:consensusnode.transactions.contracts.ContractExecuteTransaction/signWithOperator/call")
    void shouldCallSignWithOperator() {
        assertNotNull(create().signWithOperator(clientValue()));
    }

    @Test
    @DisplayName("sign(payer, nodes) can be called and returns a value")
    @Tag("testId:consensusnode.transactions.contracts.ContractExecuteTransaction/sign/call")
    void shouldCallSign() {
        assertNotNull(create().sign(payerValue(), nodesValue()));
    }

    @Test
    @DisplayName("sign(payerId, signer, nodes) can be called and returns a value")
    @Tag("testId:consensusnode.transactions.contracts.ContractExecuteTransaction/sign/call-2")
    void shouldCallSign2() {
        assertNotNull(create().sign(payerIdValue(), signerValue(), nodesValue()));
    }

    @Test
    @DisplayName("signWithOperatorAndSubmit(client) can be called and returns a value")
    @Tag("testId:consensusnode.transactions.contracts.ContractExecuteTransaction/signWithOperatorAndSubmit/call")
    void shouldCallSignWithOperatorAndSubmit() {
        assertNotNull(create().signWithOperatorAndSubmit(clientValue()));
    }

    @Test
    @DisplayName("packForBatch(payer, batchKey) can be called and returns a value")
    @Tag("testId:consensusnode.transactions.contracts.ContractExecuteTransaction/packForBatch/call")
    void shouldCallPackForBatch() {
        assertNotNull(create().packForBatch(payerValue(), batchKeyValue()));
    }

    @Test
    @DisplayName("signForBatchWithOperator(client, batchKey) can be called and returns a value")
    @Tag("testId:consensusnode.transactions.contracts.ContractExecuteTransaction/signForBatchWithOperator/call")
    void shouldCallSignForBatchWithOperator() {
        assertNotNull(create().signForBatchWithOperator(clientValue(), batchKeyValue()));
    }

    @Test
    @DisplayName("signForBatch(payer, batchKey) can be called and returns a value")
    @Tag("testId:consensusnode.transactions.contracts.ContractExecuteTransaction/signForBatch/call")
    void shouldCallSignForBatch() {
        assertNotNull(create().signForBatch(payerValue(), batchKeyValue()));
    }

    @Test
    @DisplayName("signForBatch(payerId, signer, batchKey) can be called and returns a value")
    @Tag("testId:consensusnode.transactions.contracts.ContractExecuteTransaction/signForBatch/call-2")
    void shouldCallSignForBatch2() {
        assertNotNull(create().signForBatch(payerIdValue(), signerValue(), batchKeyValue()));
    }

    private static ContractId contractIdValue() {
        return new ContractId(0L, 0L, "", 1001L, null);
    }

    private static long gasValue() {
        return 1L;
    }

    private static NativeToken<?, ?> payableAmountValue() {
        return new Hbar(1L, HbarUnit.TINYBAR);
    }

    private static byte[] callDataValue() {
        return new byte[] {1, 2, 3};
    }

    private static ContractExecuteTransaction create() {
        return new ContractExecuteTransaction(contractIdValue(), gasValue(), payableAmountValue(), callDataValue());
    }

    private static NativeToken<?, ?> maxTransactionFeeOtherValue() {
        return new Hbar(2L, HbarUnit.TINYBAR);
    }

    private static Duration validDurationOtherValue() {
        return Duration.ofSeconds(2L);
    }

    private static String memoOtherValue() {
        return "value1";
    }

    private static Account payerValue() {
        return new Account(new AccountId(0L, 0L, "", 1001L, null, null), KeysFactory.createPrivateKey(KeyAlgorithm.ED25519, ByteImportEncoding.HEX, "d3671a1e98bb22f011c0e4bcf5125590e15d8f21a7017309bb558852039bc75c"));
    }

    private static List<AccountId> nodesValue() {
        return List.of(new AccountId(0L, 0L, "", 1001L, null, null));
    }

    private static HieroClient<?> clientValue() {
        return ClientFactory.createClient(new NetworkSetting(new Network<>(new byte[] {(byte) 3}, "local", HbarUnit.HBAR), Set.of(new ConsensusNode(new IpAddress(new byte[] {(byte) 127, (byte) 0, (byte) 0, (byte) 1}), 35211, new AccountId(0L, 0L, "", 3L, null, null))), Set.of(new MirrorNode("http://127.0.0.1:38081"))), new Account(new AccountId(0L, 0L, "", 1001L, null, null), KeysFactory.createPrivateKey(KeyAlgorithm.ED25519, ByteImportEncoding.HEX, "d3671a1e98bb22f011c0e4bcf5125590e15d8f21a7017309bb558852039bc75c")));
    }

    private static AccountId payerIdValue() {
        return new AccountId(0L, 0L, "", 1001L, null, null);
    }

    private static TransactionSigner signerValue() {
        return ClientFactory.createClient(new NetworkSetting(new Network<>(new byte[] {(byte) 3}, "local", HbarUnit.HBAR), Set.of(new ConsensusNode(new IpAddress(new byte[] {(byte) 127, (byte) 0, (byte) 0, (byte) 1}), 35211, new AccountId(0L, 0L, "", 3L, null, null))), Set.of(new MirrorNode("http://127.0.0.1:38081"))), new Account(new AccountId(0L, 0L, "", 1001L, null, null), KeysFactory.createPrivateKey(KeyAlgorithm.ED25519, ByteImportEncoding.HEX, "d3671a1e98bb22f011c0e4bcf5125590e15d8f21a7017309bb558852039bc75c"))).transactionSigner();
    }

    private static Authority batchKeyValue() {
        return new PublicKeyAuthority(KeysFactory.createPrivateKey(KeyAlgorithm.ED25519, ByteImportEncoding.HEX, "d3671a1e98bb22f011c0e4bcf5125590e15d8f21a7017309bb558852039bc75c").createPublicKey());
    }
}
