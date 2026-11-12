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

/// Tests of `ClprSubmitBundleTransaction`, generated from the specs.
@DisplayName("ClprSubmitBundleTransaction")
class ClprSubmitBundleTransactionTest {

    @Test
    @DisplayName("creates a ClprSubmitBundleTransaction and returns the values")
    @Tag("testId:consensusnode.clpr.transactions.ClprSubmitBundleTransaction/create")
    @Tag("hip:1535")
    void shouldCreateAndReturnTheValues() {
        final byte[] channelId = channelIdValue();
        final byte[] bundlePayload = bundlePayloadValue();
        final ClprSubmitBundleTransaction subject = new ClprSubmitBundleTransaction(channelId, bundlePayload);
        assertValue(channelId, subject.channelId());
        assertValue(bundlePayload, subject.bundlePayload());
        assertNull(subject.maxTransactionFee());
        assertNull(subject.validDuration());
        assertNull(subject.memo());
    }

    @Test
    @DisplayName("rejects null for channelId")
    @Tag("testId:consensusnode.clpr.transactions.ClprSubmitBundleTransaction/channelId/rejects-null")
    @Tag("hip:1535")
    void shouldRejectNullChannelId() {
        assertThrows(NullPointerException.class, () -> new ClprSubmitBundleTransaction((byte[]) null, bundlePayloadValue()));
    }

    @Test
    @DisplayName("channelId: accepts the minimum number of elements")
    @Tag("testId:consensusnode.clpr.transactions.ClprSubmitBundleTransaction/channelId/accepts-at-minimum-size")
    @Tag("hip:1535")
    void shouldAcceptChannelIdAtMinimumSize() {
        final byte[] value = new byte[] {1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25, 26, 27, 28, 29, 30, 31, 32};
        final ClprSubmitBundleTransaction subject = new ClprSubmitBundleTransaction(value, bundlePayloadValue());
        assertValue(value, subject.channelId());
    }

    @Test
    @DisplayName("channelId: rejects fewer elements than the minimum")
    @Tag("testId:consensusnode.clpr.transactions.ClprSubmitBundleTransaction/channelId/rejects-below-minimum-size")
    @Tag("hip:1535")
    void shouldRejectChannelIdBelowMinimumSize() {
        assertThrows(IllegalArgumentException.class, () -> new ClprSubmitBundleTransaction(new byte[] {1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25, 26, 27, 28, 29, 30, 31}, bundlePayloadValue()));
    }

    @Test
    @DisplayName("channelId: rejects more elements than the maximum")
    @Tag("testId:consensusnode.clpr.transactions.ClprSubmitBundleTransaction/channelId/rejects-above-maximum-size")
    @Tag("hip:1535")
    void shouldRejectChannelIdAboveMaximumSize() {
        assertThrows(IllegalArgumentException.class, () -> new ClprSubmitBundleTransaction(new byte[] {1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25, 26, 27, 28, 29, 30, 31, 32, 33}, bundlePayloadValue()));
    }

    @Test
    @DisplayName("the constructor copies channelId; the accessor returns a copy")
    @Tag("testId:consensusnode.clpr.transactions.ClprSubmitBundleTransaction/channelId/copies-in")
    @Tag("hip:1535")
    void shouldCopyChannelIdIn() {
        final byte[] value = channelIdValue();
        final byte[] expected = value.clone();
        final ClprSubmitBundleTransaction subject = new ClprSubmitBundleTransaction(value, bundlePayloadValue());
        value[0] = (byte) (value[0] + 1);
        assertValue(expected, subject.channelId());
        final byte[] returned = subject.channelId();
        assertNotNull(returned);
        returned[0] = (byte) (returned[0] + 1);
        assertValue(expected, subject.channelId());
    }

    @Test
    @DisplayName("rejects null for bundlePayload")
    @Tag("testId:consensusnode.clpr.transactions.ClprSubmitBundleTransaction/bundlePayload/rejects-null")
    @Tag("hip:1535")
    void shouldRejectNullBundlePayload() {
        assertThrows(NullPointerException.class, () -> new ClprSubmitBundleTransaction(channelIdValue(), (byte[]) null));
    }

    @Test
    @DisplayName("the constructor copies bundlePayload; the accessor returns a copy")
    @Tag("testId:consensusnode.clpr.transactions.ClprSubmitBundleTransaction/bundlePayload/copies-in")
    @Tag("hip:1535")
    void shouldCopyBundlePayloadIn() {
        final byte[] value = bundlePayloadValue();
        final byte[] expected = value.clone();
        final ClprSubmitBundleTransaction subject = new ClprSubmitBundleTransaction(channelIdValue(), value);
        value[0] = (byte) (value[0] + 1);
        assertValue(expected, subject.bundlePayload());
        final byte[] returned = subject.bundlePayload();
        assertNotNull(returned);
        returned[0] = (byte) (returned[0] + 1);
        assertValue(expected, subject.bundlePayload());
    }

    @Test
    @DisplayName("setMaxTransactionFee changes maxTransactionFee and returns the object")
    @Tag("testId:consensusnode.clpr.transactions.ClprSubmitBundleTransaction/maxTransactionFee/set")
    @Tag("hip:1535")
    void shouldSetMaxTransactionFee() {
        final ClprSubmitBundleTransaction subject = create();
        final @Nullable NativeToken<?, ?> value = maxTransactionFeeOtherValue();
        assertSame(subject, subject.setMaxTransactionFee(value));
        assertValue(value, subject.maxTransactionFee());
    }

    @Test
    @DisplayName("setMaxTransactionFee accepts null")
    @Tag("testId:consensusnode.clpr.transactions.ClprSubmitBundleTransaction/maxTransactionFee/set-null")
    @Tag("hip:1535")
    void shouldSetMaxTransactionFeeToNull() {
        final ClprSubmitBundleTransaction subject = create();
        subject.setMaxTransactionFee((NativeToken<?, ?>) null);
        assertNull(subject.maxTransactionFee());
    }

    @Test
    @DisplayName("setValidDuration changes validDuration and returns the object")
    @Tag("testId:consensusnode.clpr.transactions.ClprSubmitBundleTransaction/validDuration/set")
    @Tag("hip:1535")
    void shouldSetValidDuration() {
        final ClprSubmitBundleTransaction subject = create();
        final @Nullable Duration value = validDurationOtherValue();
        assertSame(subject, subject.setValidDuration(value));
        assertValue(value, subject.validDuration());
    }

    @Test
    @DisplayName("setValidDuration accepts null")
    @Tag("testId:consensusnode.clpr.transactions.ClprSubmitBundleTransaction/validDuration/set-null")
    @Tag("hip:1535")
    void shouldSetValidDurationToNull() {
        final ClprSubmitBundleTransaction subject = create();
        subject.setValidDuration((Duration) null);
        assertNull(subject.validDuration());
    }

    @Test
    @DisplayName("setMemo changes memo and returns the object")
    @Tag("testId:consensusnode.clpr.transactions.ClprSubmitBundleTransaction/memo/set")
    @Tag("hip:1535")
    void shouldSetMemo() {
        final ClprSubmitBundleTransaction subject = create();
        final @Nullable String value = memoOtherValue();
        assertSame(subject, subject.setMemo(value));
        assertValue(value, subject.memo());
    }

    @Test
    @DisplayName("setMemo accepts null")
    @Tag("testId:consensusnode.clpr.transactions.ClprSubmitBundleTransaction/memo/set-null")
    @Tag("hip:1535")
    void shouldSetMemoToNull() {
        final ClprSubmitBundleTransaction subject = create();
        subject.setMemo((String) null);
        assertNull(subject.memo());
    }

    @Test
    @DisplayName("setMemo accepts more elements than the maximum")
    @Tag("testId:consensusnode.clpr.transactions.ClprSubmitBundleTransaction/memo/set-accepts-at-maximum-bytes")
    @Tag("hip:1535")
    void shouldSetMemoAtMaximumBytes() {
        final ClprSubmitBundleTransaction subject = create();
        final @Nullable String value = "ääääääääääääääääääääääääääääääääääääääääääääääääää";
        subject.setMemo(value);
        assertValue(value, subject.memo());
    }

    @Test
    @DisplayName("setMemo rejects more elements than the maximum and keeps the value")
    @Tag("testId:consensusnode.clpr.transactions.ClprSubmitBundleTransaction/memo/set-rejects-above-maximum-bytes")
    @Tag("hip:1535")
    void shouldNotSetMemoAboveMaximumBytes() {
        final ClprSubmitBundleTransaction subject = create();
        assertThrows(IllegalArgumentException.class, () -> subject.setMemo("ääääääääääääääääääääääääääääääääääääääääääääääääääa"));
        assertValue(null, subject.memo());
    }

    @Test
    @DisplayName("pack(payer, nodes) can be called and returns a value")
    @Tag("testId:consensusnode.clpr.transactions.ClprSubmitBundleTransaction/pack/call")
    @Tag("hip:1535")
    void shouldCallPack() {
        assertNotNull(create().pack(payerValue(), nodesValue()));
    }

    @Test
    @DisplayName("signWithOperator(client) can be called and returns a value")
    @Tag("testId:consensusnode.clpr.transactions.ClprSubmitBundleTransaction/signWithOperator/call")
    @Tag("hip:1535")
    void shouldCallSignWithOperator() {
        assertNotNull(create().signWithOperator(clientValue()));
    }

    @Test
    @DisplayName("sign(payer, nodes) can be called and returns a value")
    @Tag("testId:consensusnode.clpr.transactions.ClprSubmitBundleTransaction/sign/call")
    @Tag("hip:1535")
    void shouldCallSign() {
        assertNotNull(create().sign(payerValue(), nodesValue()));
    }

    @Test
    @DisplayName("sign(payerId, signer, nodes) can be called and returns a value")
    @Tag("testId:consensusnode.clpr.transactions.ClprSubmitBundleTransaction/sign/call-2")
    @Tag("hip:1535")
    void shouldCallSign2() {
        assertNotNull(create().sign(payerIdValue(), signerValue(), nodesValue()));
    }

    @Test
    @DisplayName("signWithOperatorAndSubmit(client) can be called and returns a value")
    @Tag("testId:consensusnode.clpr.transactions.ClprSubmitBundleTransaction/signWithOperatorAndSubmit/call")
    @Tag("hip:1535")
    void shouldCallSignWithOperatorAndSubmit() {
        assertNotNull(create().signWithOperatorAndSubmit(clientValue()));
    }

    @Test
    @DisplayName("packForBatch(payer, batchKey) can be called and returns a value")
    @Tag("testId:consensusnode.clpr.transactions.ClprSubmitBundleTransaction/packForBatch/call")
    @Tag("hip:1535")
    void shouldCallPackForBatch() {
        assertNotNull(create().packForBatch(payerValue(), batchKeyValue()));
    }

    @Test
    @DisplayName("signForBatchWithOperator(client, batchKey) can be called and returns a value")
    @Tag("testId:consensusnode.clpr.transactions.ClprSubmitBundleTransaction/signForBatchWithOperator/call")
    @Tag("hip:1535")
    void shouldCallSignForBatchWithOperator() {
        assertNotNull(create().signForBatchWithOperator(clientValue(), batchKeyValue()));
    }

    @Test
    @DisplayName("signForBatch(payer, batchKey) can be called and returns a value")
    @Tag("testId:consensusnode.clpr.transactions.ClprSubmitBundleTransaction/signForBatch/call")
    @Tag("hip:1535")
    void shouldCallSignForBatch() {
        assertNotNull(create().signForBatch(payerValue(), batchKeyValue()));
    }

    @Test
    @DisplayName("signForBatch(payerId, signer, batchKey) can be called and returns a value")
    @Tag("testId:consensusnode.clpr.transactions.ClprSubmitBundleTransaction/signForBatch/call-2")
    @Tag("hip:1535")
    void shouldCallSignForBatch2() {
        assertNotNull(create().signForBatch(payerIdValue(), signerValue(), batchKeyValue()));
    }

    private static byte[] channelIdValue() {
        return new byte[] {1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25, 26, 27, 28, 29, 30, 31, 32};
    }

    private static byte[] bundlePayloadValue() {
        return new byte[] {1, 2, 3};
    }

    private static ClprSubmitBundleTransaction create() {
        return new ClprSubmitBundleTransaction(channelIdValue(), bundlePayloadValue());
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
