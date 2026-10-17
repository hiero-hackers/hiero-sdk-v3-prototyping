package org.hiero.consensusnode.transactions.tokens;

import static org.hiero.sdk.testing.Testing.assertValue;
import static org.junit.jupiter.api.Assertions.*;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
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

/// Tests of `TokenMintNftTransaction`, generated from the specs.
@DisplayName("TokenMintNftTransaction")
class TokenMintNftTransactionTest {

    @Test
    @DisplayName("creates a TokenMintNftTransaction and returns the values")
    @Tag("testId:consensusnode.transactions.tokens.TokenMintNftTransaction/create")
    void shouldCreateAndReturnTheValues() {
        final Address tokenId = tokenIdValue();
        final List<byte[]> metadata = metadataValue();
        final TokenMintNftTransaction subject = new TokenMintNftTransaction(tokenId, metadata);
        assertValue(tokenId, subject.tokenId());
        assertValue(metadata, subject.metadata());
        assertNull(subject.maxTransactionFee());
        assertNull(subject.validDuration());
        assertNull(subject.memo());
    }

    @Test
    @DisplayName("rejects null for tokenId")
    @Tag("testId:consensusnode.transactions.tokens.TokenMintNftTransaction/tokenId/rejects-null")
    void shouldRejectNullTokenId() {
        assertThrows(NullPointerException.class, () -> new TokenMintNftTransaction((Address) null, metadataValue()));
    }

    @Test
    @DisplayName("rejects null for metadata")
    @Tag("testId:consensusnode.transactions.tokens.TokenMintNftTransaction/metadata/rejects-null")
    void shouldRejectNullMetadata() {
        assertThrows(NullPointerException.class, () -> new TokenMintNftTransaction(tokenIdValue(), (List<byte[]>) null));
    }

    @Test
    @DisplayName("metadata: accepts the minimum number of elements")
    @Tag("testId:consensusnode.transactions.tokens.TokenMintNftTransaction/metadata/accepts-at-minimum-size")
    void shouldAcceptMetadataAtMinimumSize() {
        final List<byte[]> value = List.of(new byte[] {1, 2, 3});
        final TokenMintNftTransaction subject = new TokenMintNftTransaction(tokenIdValue(), value);
        assertValue(value, subject.metadata());
    }

    @Test
    @DisplayName("metadata: rejects fewer elements than the minimum")
    @Tag("testId:consensusnode.transactions.tokens.TokenMintNftTransaction/metadata/rejects-below-minimum-size")
    void shouldRejectMetadataBelowMinimumSize() {
        assertThrows(IllegalArgumentException.class, () -> new TokenMintNftTransaction(tokenIdValue(), List.of()));
    }

    @Test
    @DisplayName("metadata: accepts the maximum number of elements")
    @Tag("testId:consensusnode.transactions.tokens.TokenMintNftTransaction/metadata/accepts-at-maximum-size")
    void shouldAcceptMetadataAtMaximumSize() {
        final List<byte[]> value = List.of(new byte[] {1, 2, 3}, new byte[] {2, 3, 4}, new byte[] {3, 4, 5}, new byte[] {4, 5, 6}, new byte[] {5, 6, 7}, new byte[] {6, 7, 8}, new byte[] {7, 8, 9}, new byte[] {8, 9, 10}, new byte[] {9, 10, 11}, new byte[] {10, 11, 12});
        final TokenMintNftTransaction subject = new TokenMintNftTransaction(tokenIdValue(), value);
        assertValue(value, subject.metadata());
    }

    @Test
    @DisplayName("metadata: rejects more elements than the maximum")
    @Tag("testId:consensusnode.transactions.tokens.TokenMintNftTransaction/metadata/rejects-above-maximum-size")
    void shouldRejectMetadataAboveMaximumSize() {
        assertThrows(IllegalArgumentException.class, () -> new TokenMintNftTransaction(tokenIdValue(), List.of(new byte[] {1, 2, 3}, new byte[] {2, 3, 4}, new byte[] {3, 4, 5}, new byte[] {4, 5, 6}, new byte[] {5, 6, 7}, new byte[] {6, 7, 8}, new byte[] {7, 8, 9}, new byte[] {8, 9, 10}, new byte[] {9, 10, 11}, new byte[] {10, 11, 12}, new byte[] {11, 12, 13})));
    }

    @Test
    @DisplayName("the constructor copies metadata; the accessor returns an unmodifiable collection")
    @Tag("testId:consensusnode.transactions.tokens.TokenMintNftTransaction/metadata/copies-in")
    void shouldCopyMetadataIn() {
        final List<byte[]> value = new ArrayList<>(metadataValue());
        final List<byte[]> expected = List.copyOf(value);
        final TokenMintNftTransaction subject = new TokenMintNftTransaction(tokenIdValue(), value);
        value.clear();
        assertValue(expected, subject.metadata());
        assertThrows(UnsupportedOperationException.class, () -> Objects.requireNonNull(subject.metadata()).clear());
    }

    @Test
    @DisplayName("setMaxTransactionFee changes maxTransactionFee and returns the object")
    @Tag("testId:consensusnode.transactions.tokens.TokenMintNftTransaction/maxTransactionFee/set")
    void shouldSetMaxTransactionFee() {
        final TokenMintNftTransaction subject = create();
        final @Nullable NativeToken<?, ?> value = maxTransactionFeeOtherValue();
        assertSame(subject, subject.setMaxTransactionFee(value));
        assertValue(value, subject.maxTransactionFee());
    }

    @Test
    @DisplayName("setMaxTransactionFee accepts null")
    @Tag("testId:consensusnode.transactions.tokens.TokenMintNftTransaction/maxTransactionFee/set-null")
    void shouldSetMaxTransactionFeeToNull() {
        final TokenMintNftTransaction subject = create();
        subject.setMaxTransactionFee((NativeToken<?, ?>) null);
        assertNull(subject.maxTransactionFee());
    }

    @Test
    @DisplayName("setValidDuration changes validDuration and returns the object")
    @Tag("testId:consensusnode.transactions.tokens.TokenMintNftTransaction/validDuration/set")
    void shouldSetValidDuration() {
        final TokenMintNftTransaction subject = create();
        final @Nullable Duration value = validDurationOtherValue();
        assertSame(subject, subject.setValidDuration(value));
        assertValue(value, subject.validDuration());
    }

    @Test
    @DisplayName("setValidDuration accepts null")
    @Tag("testId:consensusnode.transactions.tokens.TokenMintNftTransaction/validDuration/set-null")
    void shouldSetValidDurationToNull() {
        final TokenMintNftTransaction subject = create();
        subject.setValidDuration((Duration) null);
        assertNull(subject.validDuration());
    }

    @Test
    @DisplayName("setMemo changes memo and returns the object")
    @Tag("testId:consensusnode.transactions.tokens.TokenMintNftTransaction/memo/set")
    void shouldSetMemo() {
        final TokenMintNftTransaction subject = create();
        final @Nullable String value = memoOtherValue();
        assertSame(subject, subject.setMemo(value));
        assertValue(value, subject.memo());
    }

    @Test
    @DisplayName("setMemo accepts null")
    @Tag("testId:consensusnode.transactions.tokens.TokenMintNftTransaction/memo/set-null")
    void shouldSetMemoToNull() {
        final TokenMintNftTransaction subject = create();
        subject.setMemo((String) null);
        assertNull(subject.memo());
    }

    @Test
    @DisplayName("setMemo accepts more elements than the maximum")
    @Tag("testId:consensusnode.transactions.tokens.TokenMintNftTransaction/memo/set-accepts-at-maximum-bytes")
    void shouldSetMemoAtMaximumBytes() {
        final TokenMintNftTransaction subject = create();
        final @Nullable String value = "ääääääääääääääääääääääääääääääääääääääääääääääääää";
        subject.setMemo(value);
        assertValue(value, subject.memo());
    }

    @Test
    @DisplayName("setMemo rejects more elements than the maximum and keeps the value")
    @Tag("testId:consensusnode.transactions.tokens.TokenMintNftTransaction/memo/set-rejects-above-maximum-bytes")
    void shouldNotSetMemoAboveMaximumBytes() {
        final TokenMintNftTransaction subject = create();
        assertThrows(IllegalArgumentException.class, () -> subject.setMemo("ääääääääääääääääääääääääääääääääääääääääääääääääääa"));
        assertValue(null, subject.memo());
    }

    @Test
    @DisplayName("pack(payer, nodes) can be called and returns a value")
    @Tag("testId:consensusnode.transactions.tokens.TokenMintNftTransaction/pack/call")
    void shouldCallPack() {
        assertNotNull(create().pack(payerValue(), nodesValue()));
    }

    @Test
    @DisplayName("signWithOperator(client) can be called and returns a value")
    @Tag("testId:consensusnode.transactions.tokens.TokenMintNftTransaction/signWithOperator/call")
    void shouldCallSignWithOperator() {
        assertNotNull(create().signWithOperator(clientValue()));
    }

    @Test
    @DisplayName("sign(payer, nodes) can be called and returns a value")
    @Tag("testId:consensusnode.transactions.tokens.TokenMintNftTransaction/sign/call")
    void shouldCallSign() {
        assertNotNull(create().sign(payerValue(), nodesValue()));
    }

    @Test
    @DisplayName("sign(payerId, signer, nodes) can be called and returns a value")
    @Tag("testId:consensusnode.transactions.tokens.TokenMintNftTransaction/sign/call-2")
    void shouldCallSign2() {
        assertNotNull(create().sign(payerIdValue(), signerValue(), nodesValue()));
    }

    @Test
    @DisplayName("signWithOperatorAndSubmit(client) can be called and returns a value")
    @Tag("testId:consensusnode.transactions.tokens.TokenMintNftTransaction/signWithOperatorAndSubmit/call")
    void shouldCallSignWithOperatorAndSubmit() {
        assertNotNull(create().signWithOperatorAndSubmit(clientValue()));
    }

    @Test
    @DisplayName("packForBatch(payer, batchKey) can be called and returns a value")
    @Tag("testId:consensusnode.transactions.tokens.TokenMintNftTransaction/packForBatch/call")
    void shouldCallPackForBatch() {
        assertNotNull(create().packForBatch(payerValue(), batchKeyValue()));
    }

    @Test
    @DisplayName("signForBatchWithOperator(client, batchKey) can be called and returns a value")
    @Tag("testId:consensusnode.transactions.tokens.TokenMintNftTransaction/signForBatchWithOperator/call")
    void shouldCallSignForBatchWithOperator() {
        assertNotNull(create().signForBatchWithOperator(clientValue(), batchKeyValue()));
    }

    @Test
    @DisplayName("signForBatch(payer, batchKey) can be called and returns a value")
    @Tag("testId:consensusnode.transactions.tokens.TokenMintNftTransaction/signForBatch/call")
    void shouldCallSignForBatch() {
        assertNotNull(create().signForBatch(payerValue(), batchKeyValue()));
    }

    @Test
    @DisplayName("signForBatch(payerId, signer, batchKey) can be called and returns a value")
    @Tag("testId:consensusnode.transactions.tokens.TokenMintNftTransaction/signForBatch/call-2")
    void shouldCallSignForBatch2() {
        assertNotNull(create().signForBatch(payerIdValue(), signerValue(), batchKeyValue()));
    }

    private static Address tokenIdValue() {
        return new Address(0L, 0L, "", 1001L);
    }

    private static List<byte[]> metadataValue() {
        return List.of(new byte[] {1, 2, 3});
    }

    private static TokenMintNftTransaction create() {
        return new TokenMintNftTransaction(tokenIdValue(), metadataValue());
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
