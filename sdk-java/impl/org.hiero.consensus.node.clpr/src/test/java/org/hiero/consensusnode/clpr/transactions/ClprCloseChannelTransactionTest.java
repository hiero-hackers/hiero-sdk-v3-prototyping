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
import org.hiero.ledger.IpAddress;
import org.hiero.ledger.MirrorNode;
import org.hiero.ledger.Network;
import org.hiero.ledger.config.NetworkSetting;
import org.hiero.nativeToken.NativeToken;
import org.jspecify.annotations.Nullable;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

/// Tests of `ClprCloseChannelTransaction`, generated from the specs.
@DisplayName("ClprCloseChannelTransaction")
class ClprCloseChannelTransactionTest {

    @Test
    @DisplayName("creates a ClprCloseChannelTransaction and returns the values")
    @Tag("testId:consensusnode.clpr.transactions.ClprCloseChannelTransaction/create")
    @Tag("hip:1535")
    void shouldCreateAndReturnTheValues() {
        final byte[] channelId = channelIdValue();
        final byte @Nullable [] ownershipCommitment = ownershipCommitmentValue();
        final ClprCloseChannelTransaction subject = new ClprCloseChannelTransaction(channelId, ownershipCommitment);
        assertValue(channelId, subject.channelId());
        assertValue(ownershipCommitment, subject.ownershipCommitment());
        assertNull(subject.maxTransactionFee());
        assertNull(subject.validDuration());
        assertNull(subject.memo());
    }

    @Test
    @DisplayName("rejects null for channelId")
    @Tag("testId:consensusnode.clpr.transactions.ClprCloseChannelTransaction/channelId/rejects-null")
    @Tag("hip:1535")
    void shouldRejectNullChannelId() {
        assertThrows(NullPointerException.class, () -> new ClprCloseChannelTransaction((byte[]) null, ownershipCommitmentValue()));
    }

    @Test
    @DisplayName("channelId: accepts the minimum number of elements")
    @Tag("testId:consensusnode.clpr.transactions.ClprCloseChannelTransaction/channelId/accepts-at-minimum-size")
    @Tag("hip:1535")
    void shouldAcceptChannelIdAtMinimumSize() {
        final byte[] value = new byte[] {1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25, 26, 27, 28, 29, 30, 31, 32};
        final ClprCloseChannelTransaction subject = new ClprCloseChannelTransaction(value, ownershipCommitmentValue());
        assertValue(value, subject.channelId());
    }

    @Test
    @DisplayName("channelId: rejects fewer elements than the minimum")
    @Tag("testId:consensusnode.clpr.transactions.ClprCloseChannelTransaction/channelId/rejects-below-minimum-size")
    @Tag("hip:1535")
    void shouldRejectChannelIdBelowMinimumSize() {
        assertThrows(IllegalArgumentException.class, () -> new ClprCloseChannelTransaction(new byte[] {1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25, 26, 27, 28, 29, 30, 31}, ownershipCommitmentValue()));
    }

    @Test
    @DisplayName("channelId: rejects more elements than the maximum")
    @Tag("testId:consensusnode.clpr.transactions.ClprCloseChannelTransaction/channelId/rejects-above-maximum-size")
    @Tag("hip:1535")
    void shouldRejectChannelIdAboveMaximumSize() {
        assertThrows(IllegalArgumentException.class, () -> new ClprCloseChannelTransaction(new byte[] {1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25, 26, 27, 28, 29, 30, 31, 32, 33}, ownershipCommitmentValue()));
    }

    @Test
    @DisplayName("the constructor copies channelId; the accessor returns a copy")
    @Tag("testId:consensusnode.clpr.transactions.ClprCloseChannelTransaction/channelId/copies-in")
    @Tag("hip:1535")
    void shouldCopyChannelIdIn() {
        final byte[] value = channelIdValue();
        final byte[] expected = value.clone();
        final ClprCloseChannelTransaction subject = new ClprCloseChannelTransaction(value, ownershipCommitmentValue());
        value[0] = (byte) (value[0] + 1);
        assertValue(expected, subject.channelId());
        final byte[] returned = subject.channelId();
        assertNotNull(returned);
        returned[0] = (byte) (returned[0] + 1);
        assertValue(expected, subject.channelId());
    }

    @Test
    @DisplayName("ownershipCommitment: accepts the minimum number of elements")
    @Tag("testId:consensusnode.clpr.transactions.ClprCloseChannelTransaction/ownershipCommitment/accepts-at-minimum-size")
    @Tag("hip:1535")
    void shouldAcceptOwnershipCommitmentAtMinimumSize() {
        final byte @Nullable [] value = new byte[] {1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25, 26, 27, 28, 29, 30, 31, 32};
        final ClprCloseChannelTransaction subject = new ClprCloseChannelTransaction(channelIdValue(), value);
        assertValue(value, subject.ownershipCommitment());
    }

    @Test
    @DisplayName("ownershipCommitment: rejects fewer elements than the minimum")
    @Tag("testId:consensusnode.clpr.transactions.ClprCloseChannelTransaction/ownershipCommitment/rejects-below-minimum-size")
    @Tag("hip:1535")
    void shouldRejectOwnershipCommitmentBelowMinimumSize() {
        assertThrows(IllegalArgumentException.class, () -> new ClprCloseChannelTransaction(channelIdValue(), new byte[] {1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25, 26, 27, 28, 29, 30, 31}));
    }

    @Test
    @DisplayName("ownershipCommitment: rejects more elements than the maximum")
    @Tag("testId:consensusnode.clpr.transactions.ClprCloseChannelTransaction/ownershipCommitment/rejects-above-maximum-size")
    @Tag("hip:1535")
    void shouldRejectOwnershipCommitmentAboveMaximumSize() {
        assertThrows(IllegalArgumentException.class, () -> new ClprCloseChannelTransaction(channelIdValue(), new byte[] {1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25, 26, 27, 28, 29, 30, 31, 32, 33}));
    }

    @Test
    @DisplayName("the constructor copies ownershipCommitment; the accessor returns a copy")
    @Tag("testId:consensusnode.clpr.transactions.ClprCloseChannelTransaction/ownershipCommitment/copies-in")
    @Tag("hip:1535")
    void shouldCopyOwnershipCommitmentIn() {
        final byte[] value = ownershipCommitmentValue();
        final byte[] expected = value.clone();
        final ClprCloseChannelTransaction subject = new ClprCloseChannelTransaction(channelIdValue(), value);
        value[0] = (byte) (value[0] + 1);
        assertValue(expected, subject.ownershipCommitment());
        final byte[] returned = subject.ownershipCommitment();
        assertNotNull(returned);
        returned[0] = (byte) (returned[0] + 1);
        assertValue(expected, subject.ownershipCommitment());
    }

    @Test
    @DisplayName("setMaxTransactionFee changes maxTransactionFee and returns the object")
    @Tag("testId:consensusnode.clpr.transactions.ClprCloseChannelTransaction/maxTransactionFee/set")
    @Tag("hip:1535")
    void shouldSetMaxTransactionFee() {
        final ClprCloseChannelTransaction subject = create();
        final @Nullable NativeToken<?, ?> value = maxTransactionFeeOtherValue();
        assertSame(subject, subject.setMaxTransactionFee(value));
        assertValue(value, subject.maxTransactionFee());
    }

    @Test
    @DisplayName("setMaxTransactionFee accepts null")
    @Tag("testId:consensusnode.clpr.transactions.ClprCloseChannelTransaction/maxTransactionFee/set-null")
    @Tag("hip:1535")
    void shouldSetMaxTransactionFeeToNull() {
        final ClprCloseChannelTransaction subject = create();
        subject.setMaxTransactionFee((NativeToken<?, ?>) null);
        assertNull(subject.maxTransactionFee());
    }

    @Test
    @DisplayName("setValidDuration changes validDuration and returns the object")
    @Tag("testId:consensusnode.clpr.transactions.ClprCloseChannelTransaction/validDuration/set")
    @Tag("hip:1535")
    void shouldSetValidDuration() {
        final ClprCloseChannelTransaction subject = create();
        final @Nullable Duration value = validDurationOtherValue();
        assertSame(subject, subject.setValidDuration(value));
        assertValue(value, subject.validDuration());
    }

    @Test
    @DisplayName("setValidDuration accepts null")
    @Tag("testId:consensusnode.clpr.transactions.ClprCloseChannelTransaction/validDuration/set-null")
    @Tag("hip:1535")
    void shouldSetValidDurationToNull() {
        final ClprCloseChannelTransaction subject = create();
        subject.setValidDuration((Duration) null);
        assertNull(subject.validDuration());
    }

    @Test
    @DisplayName("setMemo changes memo and returns the object")
    @Tag("testId:consensusnode.clpr.transactions.ClprCloseChannelTransaction/memo/set")
    @Tag("hip:1535")
    void shouldSetMemo() {
        final ClprCloseChannelTransaction subject = create();
        final @Nullable String value = memoOtherValue();
        assertSame(subject, subject.setMemo(value));
        assertValue(value, subject.memo());
    }

    @Test
    @DisplayName("setMemo accepts null")
    @Tag("testId:consensusnode.clpr.transactions.ClprCloseChannelTransaction/memo/set-null")
    @Tag("hip:1535")
    void shouldSetMemoToNull() {
        final ClprCloseChannelTransaction subject = create();
        subject.setMemo((String) null);
        assertNull(subject.memo());
    }

    @Test
    @DisplayName("setMemo accepts more elements than the maximum")
    @Tag("testId:consensusnode.clpr.transactions.ClprCloseChannelTransaction/memo/set-accepts-at-maximum-bytes")
    @Tag("hip:1535")
    void shouldSetMemoAtMaximumBytes() {
        final ClprCloseChannelTransaction subject = create();
        final @Nullable String value = "ääääääääääääääääääääääääääääääääääääääääääääääääää";
        subject.setMemo(value);
        assertValue(value, subject.memo());
    }

    @Test
    @DisplayName("setMemo rejects more elements than the maximum and keeps the value")
    @Tag("testId:consensusnode.clpr.transactions.ClprCloseChannelTransaction/memo/set-rejects-above-maximum-bytes")
    @Tag("hip:1535")
    void shouldNotSetMemoAboveMaximumBytes() {
        final ClprCloseChannelTransaction subject = create();
        assertThrows(IllegalArgumentException.class, () -> subject.setMemo("ääääääääääääääääääääääääääääääääääääääääääääääääääa"));
        assertValue(null, subject.memo());
    }

    @Test
    @DisplayName("pack(payer, nodes) can be called and returns a value")
    @Tag("testId:consensusnode.clpr.transactions.ClprCloseChannelTransaction/pack/call")
    @Tag("hip:1535")
    void shouldCallPack() {
        assertNotNull(create().pack(payerValue(), nodesValue()));
    }

    @Test
    @DisplayName("signWithOperator(client) can be called and returns a value")
    @Tag("testId:consensusnode.clpr.transactions.ClprCloseChannelTransaction/signWithOperator/call")
    @Tag("hip:1535")
    void shouldCallSignWithOperator() {
        assertNotNull(create().signWithOperator(clientValue()));
    }

    @Test
    @DisplayName("sign(payer, nodes) can be called and returns a value")
    @Tag("testId:consensusnode.clpr.transactions.ClprCloseChannelTransaction/sign/call")
    @Tag("hip:1535")
    void shouldCallSign() {
        assertNotNull(create().sign(payerValue(), nodesValue()));
    }

    @Test
    @DisplayName("sign(payerId, signer, nodes) can be called and returns a value")
    @Tag("testId:consensusnode.clpr.transactions.ClprCloseChannelTransaction/sign/call-2")
    @Tag("hip:1535")
    void shouldCallSign2() {
        assertNotNull(create().sign(payerIdValue(), signerValue(), nodesValue()));
    }

    @Test
    @DisplayName("signWithOperatorAndSubmit(client) can be called and returns a value")
    @Tag("testId:consensusnode.clpr.transactions.ClprCloseChannelTransaction/signWithOperatorAndSubmit/call")
    @Tag("hip:1535")
    void shouldCallSignWithOperatorAndSubmit() {
        assertNotNull(create().signWithOperatorAndSubmit(clientValue()));
    }

    @Test
    @DisplayName("packForBatch(payer, batchKey) can be called and returns a value")
    @Tag("testId:consensusnode.clpr.transactions.ClprCloseChannelTransaction/packForBatch/call")
    @Tag("hip:1535")
    void shouldCallPackForBatch() {
        assertNotNull(create().packForBatch(payerValue(), batchKeyValue()));
    }

    @Test
    @DisplayName("signForBatchWithOperator(client, batchKey) can be called and returns a value")
    @Tag("testId:consensusnode.clpr.transactions.ClprCloseChannelTransaction/signForBatchWithOperator/call")
    @Tag("hip:1535")
    void shouldCallSignForBatchWithOperator() {
        assertNotNull(create().signForBatchWithOperator(clientValue(), batchKeyValue()));
    }

    @Test
    @DisplayName("signForBatch(payer, batchKey) can be called and returns a value")
    @Tag("testId:consensusnode.clpr.transactions.ClprCloseChannelTransaction/signForBatch/call")
    @Tag("hip:1535")
    void shouldCallSignForBatch() {
        assertNotNull(create().signForBatch(payerValue(), batchKeyValue()));
    }

    @Test
    @DisplayName("signForBatch(payerId, signer, batchKey) can be called and returns a value")
    @Tag("testId:consensusnode.clpr.transactions.ClprCloseChannelTransaction/signForBatch/call-2")
    @Tag("hip:1535")
    void shouldCallSignForBatch2() {
        assertNotNull(create().signForBatch(payerIdValue(), signerValue(), batchKeyValue()));
    }

    private static byte[] channelIdValue() {
        return new byte[] {1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25, 26, 27, 28, 29, 30, 31, 32};
    }

    private static byte[] ownershipCommitmentValue() {
        return new byte[] {1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25, 26, 27, 28, 29, 30, 31, 32};
    }

    private static ClprCloseChannelTransaction create() {
        return new ClprCloseChannelTransaction(channelIdValue(), ownershipCommitmentValue());
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
