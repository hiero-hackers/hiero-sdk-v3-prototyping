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
import org.hiero.ledger.Address;
import org.hiero.ledger.ConsensusNode;
import org.hiero.ledger.IpAddress;
import org.hiero.ledger.MirrorNode;
import org.hiero.ledger.Network;
import org.hiero.ledger.config.NetworkSetting;
import org.hiero.nativeToken.NativeToken;
import org.jspecify.annotations.Nullable;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

/// Tests of `EthereumTransaction`, generated from the specs.
@DisplayName("EthereumTransaction")
class EthereumTransactionTest {

    @Test
    @DisplayName("creates an EthereumTransaction and returns the values")
    @Tag("testId:consensusnode.transactions.contracts.EthereumTransaction/create")
    void shouldCreateAndReturnTheValues() {
        final byte[] ethereumData = ethereumDataValue();
        final @Nullable Address callDataFileId = callDataFileIdValue();
        final @Nullable NativeToken<?, ?> maxGasAllowance = maxGasAllowanceValue();
        final EthereumTransaction subject = new EthereumTransaction(ethereumData, callDataFileId, maxGasAllowance);
        assertValue(ethereumData, subject.ethereumData());
        assertValue(callDataFileId, subject.callDataFileId());
        assertValue(maxGasAllowance, subject.maxGasAllowance());
        assertNull(subject.maxTransactionFee());
        assertNull(subject.validDuration());
        assertNull(subject.memo());
    }

    @Test
    @DisplayName("the builder builds the values of the constructor")
    @Tag("testId:consensusnode.transactions.contracts.EthereumTransaction/builder/builds")
    void shouldBuildTheValuesOfTheConstructor() {
        final byte[] ethereumData = ethereumDataValue();
        final @Nullable Address callDataFileId = callDataFileIdValue();
        final @Nullable NativeToken<?, ?> maxGasAllowance = maxGasAllowanceValue();
        final EthereumTransaction subject = EthereumTransaction.builder()
                .ethereumData(ethereumData)
                .callDataFileId(callDataFileId)
                .maxGasAllowance(maxGasAllowance)
                .build();
        assertValue(ethereumData, subject.ethereumData());
        assertValue(callDataFileId, subject.callDataFileId());
        assertValue(maxGasAllowance, subject.maxGasAllowance());
    }

    @Test
    @DisplayName("a value that is not set is null or its default value")
    @Tag("testId:consensusnode.transactions.contracts.EthereumTransaction/builder/optional-values")
    void shouldBuildWithoutTheOptionalValues() {
        final EthereumTransaction subject = EthereumTransaction.builder()
                .ethereumData(ethereumDataValue())
                .build();
        assertNull(subject.callDataFileId());
        assertNull(subject.maxGasAllowance());
    }

    @Test
    @DisplayName("the builder rejects a missing required value")
    @Tag("testId:consensusnode.transactions.contracts.EthereumTransaction/builder/rejects-missing-value")
    void shouldRejectAMissingRequiredValue() {
        assertThrows(NullPointerException.class, () -> EthereumTransaction.builder().build());
    }

    @Test
    @DisplayName("rejects null for ethereumData")
    @Tag("testId:consensusnode.transactions.contracts.EthereumTransaction/ethereumData/rejects-null")
    void shouldRejectNullEthereumData() {
        assertThrows(NullPointerException.class, () -> new EthereumTransaction((byte[]) null, callDataFileIdValue(), maxGasAllowanceValue()));
    }

    @Test
    @DisplayName("ethereumData: accepts the minimum number of elements")
    @Tag("testId:consensusnode.transactions.contracts.EthereumTransaction/ethereumData/accepts-at-minimum-size")
    void shouldAcceptEthereumDataAtMinimumSize() {
        final byte[] value = new byte[] {1};
        final EthereumTransaction subject = new EthereumTransaction(value, callDataFileIdValue(), maxGasAllowanceValue());
        assertValue(value, subject.ethereumData());
    }

    @Test
    @DisplayName("ethereumData: rejects fewer elements than the minimum")
    @Tag("testId:consensusnode.transactions.contracts.EthereumTransaction/ethereumData/rejects-below-minimum-size")
    void shouldRejectEthereumDataBelowMinimumSize() {
        assertThrows(IllegalArgumentException.class, () -> new EthereumTransaction(new byte[0], callDataFileIdValue(), maxGasAllowanceValue()));
    }

    @Test
    @DisplayName("the constructor copies ethereumData; the accessor returns a copy")
    @Tag("testId:consensusnode.transactions.contracts.EthereumTransaction/ethereumData/copies-in")
    void shouldCopyEthereumDataIn() {
        final byte[] value = ethereumDataValue();
        final byte[] expected = value.clone();
        final EthereumTransaction subject = new EthereumTransaction(value, callDataFileIdValue(), maxGasAllowanceValue());
        value[0] = (byte) (value[0] + 1);
        assertValue(expected, subject.ethereumData());
        final byte[] returned = subject.ethereumData();
        assertNotNull(returned);
        returned[0] = (byte) (returned[0] + 1);
        assertValue(expected, subject.ethereumData());
    }

    @Test
    @DisplayName("setMaxTransactionFee changes maxTransactionFee and returns the object")
    @Tag("testId:consensusnode.transactions.contracts.EthereumTransaction/maxTransactionFee/set")
    void shouldSetMaxTransactionFee() {
        final EthereumTransaction subject = create();
        final @Nullable NativeToken<?, ?> value = maxTransactionFeeOtherValue();
        assertSame(subject, subject.setMaxTransactionFee(value));
        assertValue(value, subject.maxTransactionFee());
    }

    @Test
    @DisplayName("setMaxTransactionFee accepts null")
    @Tag("testId:consensusnode.transactions.contracts.EthereumTransaction/maxTransactionFee/set-null")
    void shouldSetMaxTransactionFeeToNull() {
        final EthereumTransaction subject = create();
        subject.setMaxTransactionFee((NativeToken<?, ?>) null);
        assertNull(subject.maxTransactionFee());
    }

    @Test
    @DisplayName("setValidDuration changes validDuration and returns the object")
    @Tag("testId:consensusnode.transactions.contracts.EthereumTransaction/validDuration/set")
    void shouldSetValidDuration() {
        final EthereumTransaction subject = create();
        final @Nullable Duration value = validDurationOtherValue();
        assertSame(subject, subject.setValidDuration(value));
        assertValue(value, subject.validDuration());
    }

    @Test
    @DisplayName("setValidDuration accepts null")
    @Tag("testId:consensusnode.transactions.contracts.EthereumTransaction/validDuration/set-null")
    void shouldSetValidDurationToNull() {
        final EthereumTransaction subject = create();
        subject.setValidDuration((Duration) null);
        assertNull(subject.validDuration());
    }

    @Test
    @DisplayName("setMemo changes memo and returns the object")
    @Tag("testId:consensusnode.transactions.contracts.EthereumTransaction/memo/set")
    void shouldSetMemo() {
        final EthereumTransaction subject = create();
        final @Nullable String value = memoOtherValue();
        assertSame(subject, subject.setMemo(value));
        assertValue(value, subject.memo());
    }

    @Test
    @DisplayName("setMemo accepts null")
    @Tag("testId:consensusnode.transactions.contracts.EthereumTransaction/memo/set-null")
    void shouldSetMemoToNull() {
        final EthereumTransaction subject = create();
        subject.setMemo((String) null);
        assertNull(subject.memo());
    }

    @Test
    @DisplayName("setMemo accepts more elements than the maximum")
    @Tag("testId:consensusnode.transactions.contracts.EthereumTransaction/memo/set-accepts-at-maximum-bytes")
    void shouldSetMemoAtMaximumBytes() {
        final EthereumTransaction subject = create();
        final @Nullable String value = "ääääääääääääääääääääääääääääääääääääääääääääääääää";
        subject.setMemo(value);
        assertValue(value, subject.memo());
    }

    @Test
    @DisplayName("setMemo rejects more elements than the maximum and keeps the value")
    @Tag("testId:consensusnode.transactions.contracts.EthereumTransaction/memo/set-rejects-above-maximum-bytes")
    void shouldNotSetMemoAboveMaximumBytes() {
        final EthereumTransaction subject = create();
        assertThrows(IllegalArgumentException.class, () -> subject.setMemo("ääääääääääääääääääääääääääääääääääääääääääääääääääa"));
        assertValue(null, subject.memo());
    }

    @Test
    @DisplayName("pack(payer, nodes) can be called and returns a value")
    @Tag("testId:consensusnode.transactions.contracts.EthereumTransaction/pack/call")
    void shouldCallPack() {
        assertNotNull(create().pack(payerValue(), nodesValue()));
    }

    @Test
    @DisplayName("signWithOperator(client) can be called and returns a value")
    @Tag("testId:consensusnode.transactions.contracts.EthereumTransaction/signWithOperator/call")
    void shouldCallSignWithOperator() {
        assertNotNull(create().signWithOperator(clientValue()));
    }

    @Test
    @DisplayName("sign(payer, nodes) can be called and returns a value")
    @Tag("testId:consensusnode.transactions.contracts.EthereumTransaction/sign/call")
    void shouldCallSign() {
        assertNotNull(create().sign(payerValue(), nodesValue()));
    }

    @Test
    @DisplayName("sign(payerId, signer, nodes) can be called and returns a value")
    @Tag("testId:consensusnode.transactions.contracts.EthereumTransaction/sign/call-2")
    void shouldCallSign2() {
        assertNotNull(create().sign(payerIdValue(), signerValue(), nodesValue()));
    }

    @Test
    @DisplayName("signWithOperatorAndSubmit(client) can be called and returns a value")
    @Tag("testId:consensusnode.transactions.contracts.EthereumTransaction/signWithOperatorAndSubmit/call")
    void shouldCallSignWithOperatorAndSubmit() {
        assertNotNull(create().signWithOperatorAndSubmit(clientValue()));
    }

    @Test
    @DisplayName("packForBatch(payer, batchKey) can be called and returns a value")
    @Tag("testId:consensusnode.transactions.contracts.EthereumTransaction/packForBatch/call")
    void shouldCallPackForBatch() {
        assertNotNull(create().packForBatch(payerValue(), batchKeyValue()));
    }

    @Test
    @DisplayName("signForBatchWithOperator(client, batchKey) can be called and returns a value")
    @Tag("testId:consensusnode.transactions.contracts.EthereumTransaction/signForBatchWithOperator/call")
    void shouldCallSignForBatchWithOperator() {
        assertNotNull(create().signForBatchWithOperator(clientValue(), batchKeyValue()));
    }

    @Test
    @DisplayName("signForBatch(payer, batchKey) can be called and returns a value")
    @Tag("testId:consensusnode.transactions.contracts.EthereumTransaction/signForBatch/call")
    void shouldCallSignForBatch() {
        assertNotNull(create().signForBatch(payerValue(), batchKeyValue()));
    }

    @Test
    @DisplayName("signForBatch(payerId, signer, batchKey) can be called and returns a value")
    @Tag("testId:consensusnode.transactions.contracts.EthereumTransaction/signForBatch/call-2")
    void shouldCallSignForBatch2() {
        assertNotNull(create().signForBatch(payerIdValue(), signerValue(), batchKeyValue()));
    }

    private static byte[] ethereumDataValue() {
        return new byte[] {1, 2, 3};
    }

    private static Address callDataFileIdValue() {
        return new Address(0L, 0L, "", 1001L);
    }

    private static NativeToken<?, ?> maxGasAllowanceValue() {
        return new Hbar(1L, HbarUnit.TINYBAR);
    }

    private static EthereumTransaction create() {
        return new EthereumTransaction(ethereumDataValue(), callDataFileIdValue(), maxGasAllowanceValue());
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
