package org.hiero.consensusnode.clpr.transactions;

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

/// Tests of `ClprCompleteChannelTransaction`, generated from the specs.
@DisplayName("ClprCompleteChannelTransaction")
class ClprCompleteChannelTransactionTest {

    @Test
    @DisplayName("creates a ClprCompleteChannelTransaction and returns the values")
    @Tag("testId:consensusnode.clpr.transactions.ClprCompleteChannelTransaction/create")
    @Tag("hip:1535")
    void shouldCreateAndReturnTheValues() {
        final ClprChannelOwnershipProof ownershipProof = ownershipProofValue();
        final ContractId verifierContract = verifierContractValue();
        final byte[] configProof = configProofValue();
        final byte[] endpointManifestProof = endpointManifestProofValue();
        final ClprCompleteChannelTransaction subject = new ClprCompleteChannelTransaction(ownershipProof, verifierContract, configProof, endpointManifestProof);
        assertValue(ownershipProof, subject.ownershipProof());
        assertValue(verifierContract, subject.verifierContract());
        assertValue(configProof, subject.configProof());
        assertValue(endpointManifestProof, subject.endpointManifestProof());
        assertNull(subject.maxTransactionFee());
        assertNull(subject.validDuration());
        assertNull(subject.memo());
    }

    @Test
    @DisplayName("rejects null for ownershipProof")
    @Tag("testId:consensusnode.clpr.transactions.ClprCompleteChannelTransaction/ownershipProof/rejects-null")
    @Tag("hip:1535")
    void shouldRejectNullOwnershipProof() {
        assertThrows(NullPointerException.class, () -> new ClprCompleteChannelTransaction((ClprChannelOwnershipProof) null, verifierContractValue(), configProofValue(), endpointManifestProofValue()));
    }

    @Test
    @DisplayName("rejects null for verifierContract")
    @Tag("testId:consensusnode.clpr.transactions.ClprCompleteChannelTransaction/verifierContract/rejects-null")
    @Tag("hip:1535")
    void shouldRejectNullVerifierContract() {
        assertThrows(NullPointerException.class, () -> new ClprCompleteChannelTransaction(ownershipProofValue(), (ContractId) null, configProofValue(), endpointManifestProofValue()));
    }

    @Test
    @DisplayName("rejects null for configProof")
    @Tag("testId:consensusnode.clpr.transactions.ClprCompleteChannelTransaction/configProof/rejects-null")
    @Tag("hip:1535")
    void shouldRejectNullConfigProof() {
        assertThrows(NullPointerException.class, () -> new ClprCompleteChannelTransaction(ownershipProofValue(), verifierContractValue(), (byte[]) null, endpointManifestProofValue()));
    }

    @Test
    @DisplayName("the constructor copies configProof; the accessor returns a copy")
    @Tag("testId:consensusnode.clpr.transactions.ClprCompleteChannelTransaction/configProof/copies-in")
    @Tag("hip:1535")
    void shouldCopyConfigProofIn() {
        final byte[] value = configProofValue();
        final byte[] expected = value.clone();
        final ClprCompleteChannelTransaction subject = new ClprCompleteChannelTransaction(ownershipProofValue(), verifierContractValue(), value, endpointManifestProofValue());
        value[0] = (byte) (value[0] + 1);
        assertValue(expected, subject.configProof());
        final byte[] returned = subject.configProof();
        assertNotNull(returned);
        returned[0] = (byte) (returned[0] + 1);
        assertValue(expected, subject.configProof());
    }

    @Test
    @DisplayName("rejects null for endpointManifestProof")
    @Tag("testId:consensusnode.clpr.transactions.ClprCompleteChannelTransaction/endpointManifestProof/rejects-null")
    @Tag("hip:1535")
    void shouldRejectNullEndpointManifestProof() {
        assertThrows(NullPointerException.class, () -> new ClprCompleteChannelTransaction(ownershipProofValue(), verifierContractValue(), configProofValue(), (byte[]) null));
    }

    @Test
    @DisplayName("the constructor copies endpointManifestProof; the accessor returns a copy")
    @Tag("testId:consensusnode.clpr.transactions.ClprCompleteChannelTransaction/endpointManifestProof/copies-in")
    @Tag("hip:1535")
    void shouldCopyEndpointManifestProofIn() {
        final byte[] value = endpointManifestProofValue();
        final byte[] expected = value.clone();
        final ClprCompleteChannelTransaction subject = new ClprCompleteChannelTransaction(ownershipProofValue(), verifierContractValue(), configProofValue(), value);
        value[0] = (byte) (value[0] + 1);
        assertValue(expected, subject.endpointManifestProof());
        final byte[] returned = subject.endpointManifestProof();
        assertNotNull(returned);
        returned[0] = (byte) (returned[0] + 1);
        assertValue(expected, subject.endpointManifestProof());
    }

    @Test
    @DisplayName("setMaxTransactionFee changes maxTransactionFee and returns the object")
    @Tag("testId:consensusnode.clpr.transactions.ClprCompleteChannelTransaction/maxTransactionFee/set")
    @Tag("hip:1535")
    void shouldSetMaxTransactionFee() {
        final ClprCompleteChannelTransaction subject = create();
        final @Nullable NativeToken<?, ?> value = maxTransactionFeeOtherValue();
        assertSame(subject, subject.setMaxTransactionFee(value));
        assertValue(value, subject.maxTransactionFee());
    }

    @Test
    @DisplayName("setMaxTransactionFee accepts null")
    @Tag("testId:consensusnode.clpr.transactions.ClprCompleteChannelTransaction/maxTransactionFee/set-null")
    @Tag("hip:1535")
    void shouldSetMaxTransactionFeeToNull() {
        final ClprCompleteChannelTransaction subject = create();
        subject.setMaxTransactionFee((NativeToken<?, ?>) null);
        assertNull(subject.maxTransactionFee());
    }

    @Test
    @DisplayName("setValidDuration changes validDuration and returns the object")
    @Tag("testId:consensusnode.clpr.transactions.ClprCompleteChannelTransaction/validDuration/set")
    @Tag("hip:1535")
    void shouldSetValidDuration() {
        final ClprCompleteChannelTransaction subject = create();
        final @Nullable Duration value = validDurationOtherValue();
        assertSame(subject, subject.setValidDuration(value));
        assertValue(value, subject.validDuration());
    }

    @Test
    @DisplayName("setValidDuration accepts null")
    @Tag("testId:consensusnode.clpr.transactions.ClprCompleteChannelTransaction/validDuration/set-null")
    @Tag("hip:1535")
    void shouldSetValidDurationToNull() {
        final ClprCompleteChannelTransaction subject = create();
        subject.setValidDuration((Duration) null);
        assertNull(subject.validDuration());
    }

    @Test
    @DisplayName("setMemo changes memo and returns the object")
    @Tag("testId:consensusnode.clpr.transactions.ClprCompleteChannelTransaction/memo/set")
    @Tag("hip:1535")
    void shouldSetMemo() {
        final ClprCompleteChannelTransaction subject = create();
        final @Nullable String value = memoOtherValue();
        assertSame(subject, subject.setMemo(value));
        assertValue(value, subject.memo());
    }

    @Test
    @DisplayName("setMemo accepts null")
    @Tag("testId:consensusnode.clpr.transactions.ClprCompleteChannelTransaction/memo/set-null")
    @Tag("hip:1535")
    void shouldSetMemoToNull() {
        final ClprCompleteChannelTransaction subject = create();
        subject.setMemo((String) null);
        assertNull(subject.memo());
    }

    @Test
    @DisplayName("setMemo accepts more elements than the maximum")
    @Tag("testId:consensusnode.clpr.transactions.ClprCompleteChannelTransaction/memo/set-accepts-at-maximum-bytes")
    @Tag("hip:1535")
    void shouldSetMemoAtMaximumBytes() {
        final ClprCompleteChannelTransaction subject = create();
        final @Nullable String value = "ääääääääääääääääääääääääääääääääääääääääääääääääää";
        subject.setMemo(value);
        assertValue(value, subject.memo());
    }

    @Test
    @DisplayName("setMemo rejects more elements than the maximum and keeps the value")
    @Tag("testId:consensusnode.clpr.transactions.ClprCompleteChannelTransaction/memo/set-rejects-above-maximum-bytes")
    @Tag("hip:1535")
    void shouldNotSetMemoAboveMaximumBytes() {
        final ClprCompleteChannelTransaction subject = create();
        assertThrows(IllegalArgumentException.class, () -> subject.setMemo("ääääääääääääääääääääääääääääääääääääääääääääääääääa"));
        assertValue(null, subject.memo());
    }

    @Test
    @DisplayName("pack(payer, nodes) can be called and returns a value")
    @Tag("testId:consensusnode.clpr.transactions.ClprCompleteChannelTransaction/pack/call")
    @Tag("hip:1535")
    void shouldCallPack() {
        assertNotNull(create().pack(payerValue(), nodesValue()));
    }

    @Test
    @DisplayName("signWithOperator(client) can be called and returns a value")
    @Tag("testId:consensusnode.clpr.transactions.ClprCompleteChannelTransaction/signWithOperator/call")
    @Tag("hip:1535")
    void shouldCallSignWithOperator() {
        assertNotNull(create().signWithOperator(clientValue()));
    }

    @Test
    @DisplayName("sign(payer, nodes) can be called and returns a value")
    @Tag("testId:consensusnode.clpr.transactions.ClprCompleteChannelTransaction/sign/call")
    @Tag("hip:1535")
    void shouldCallSign() {
        assertNotNull(create().sign(payerValue(), nodesValue()));
    }

    @Test
    @DisplayName("sign(payerId, signer, nodes) can be called and returns a value")
    @Tag("testId:consensusnode.clpr.transactions.ClprCompleteChannelTransaction/sign/call-2")
    @Tag("hip:1535")
    void shouldCallSign2() {
        assertNotNull(create().sign(payerIdValue(), signerValue(), nodesValue()));
    }

    @Test
    @DisplayName("signWithOperatorAndSubmit(client) can be called and returns a value")
    @Tag("testId:consensusnode.clpr.transactions.ClprCompleteChannelTransaction/signWithOperatorAndSubmit/call")
    @Tag("hip:1535")
    void shouldCallSignWithOperatorAndSubmit() {
        assertNotNull(create().signWithOperatorAndSubmit(clientValue()));
    }

    @Test
    @DisplayName("packForBatch(payer, batchKey) can be called and returns a value")
    @Tag("testId:consensusnode.clpr.transactions.ClprCompleteChannelTransaction/packForBatch/call")
    @Tag("hip:1535")
    void shouldCallPackForBatch() {
        assertNotNull(create().packForBatch(payerValue(), batchKeyValue()));
    }

    @Test
    @DisplayName("signForBatchWithOperator(client, batchKey) can be called and returns a value")
    @Tag("testId:consensusnode.clpr.transactions.ClprCompleteChannelTransaction/signForBatchWithOperator/call")
    @Tag("hip:1535")
    void shouldCallSignForBatchWithOperator() {
        assertNotNull(create().signForBatchWithOperator(clientValue(), batchKeyValue()));
    }

    @Test
    @DisplayName("signForBatch(payer, batchKey) can be called and returns a value")
    @Tag("testId:consensusnode.clpr.transactions.ClprCompleteChannelTransaction/signForBatch/call")
    @Tag("hip:1535")
    void shouldCallSignForBatch() {
        assertNotNull(create().signForBatch(payerValue(), batchKeyValue()));
    }

    @Test
    @DisplayName("signForBatch(payerId, signer, batchKey) can be called and returns a value")
    @Tag("testId:consensusnode.clpr.transactions.ClprCompleteChannelTransaction/signForBatch/call-2")
    @Tag("hip:1535")
    void shouldCallSignForBatch2() {
        assertNotNull(create().signForBatch(payerIdValue(), signerValue(), batchKeyValue()));
    }

    private static ClprChannelOwnershipProof ownershipProofValue() {
        return ClprChannelOwnershipProof.create(new byte[] {(byte) 0, (byte) 1, (byte) 2, (byte) 3, (byte) 4, (byte) 5, (byte) 6, (byte) 7, (byte) 8, (byte) 9, (byte) 10, (byte) 11, (byte) 12, (byte) 13, (byte) 14, (byte) 15, (byte) 16, (byte) 17, (byte) 18, (byte) 19, (byte) 20, (byte) 21, (byte) 22, (byte) 23, (byte) 24, (byte) 25, (byte) 26, (byte) 27, (byte) 28, (byte) 29, (byte) 30, (byte) 31}, KeysFactory.createPrivateKey(KeyAlgorithm.ED25519, ByteImportEncoding.HEX, "d3671a1e98bb22f011c0e4bcf5125590e15d8f21a7017309bb558852039bc75c"));
    }

    private static ContractId verifierContractValue() {
        return new ContractId(0L, 0L, "", 1001L, null);
    }

    private static byte[] configProofValue() {
        return new byte[] {1, 2, 3};
    }

    private static byte[] endpointManifestProofValue() {
        return new byte[] {1, 2, 3};
    }

    private static ClprCompleteChannelTransaction create() {
        return new ClprCompleteChannelTransaction(ownershipProofValue(), verifierContractValue(), configProofValue(), endpointManifestProofValue());
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
