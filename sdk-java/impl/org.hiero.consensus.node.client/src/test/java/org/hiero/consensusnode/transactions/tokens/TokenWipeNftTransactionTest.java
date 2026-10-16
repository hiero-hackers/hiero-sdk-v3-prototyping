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
import org.junit.jupiter.api.Test;

/// Tests of `TokenWipeNftTransaction`, generated from the specs.
@DisplayName("TokenWipeNftTransaction")
class TokenWipeNftTransactionTest {

    @Test
    @DisplayName("creates a TokenWipeNftTransaction and returns the values")
    void shouldCreateAndReturnTheValues() {
        final Address tokenId = tokenIdValue();
        final AccountId accountId = accountIdValue();
        final List<Long> serials = serialsValue();
        final TokenWipeNftTransaction subject = new TokenWipeNftTransaction(tokenId, accountId, serials);
        assertValue(tokenId, subject.tokenId());
        assertValue(accountId, subject.accountId());
        assertValue(serials, subject.serials());
        assertNull(subject.maxTransactionFee());
        assertNull(subject.validDuration());
        assertNull(subject.memo());
    }

    @Test
    @DisplayName("rejects null for tokenId")
    void shouldRejectNullTokenId() {
        assertThrows(NullPointerException.class, () -> new TokenWipeNftTransaction((Address) null, accountIdValue(), serialsValue()));
    }

    @Test
    @DisplayName("rejects null for accountId")
    void shouldRejectNullAccountId() {
        assertThrows(NullPointerException.class, () -> new TokenWipeNftTransaction(tokenIdValue(), (AccountId) null, serialsValue()));
    }

    @Test
    @DisplayName("rejects null for serials")
    void shouldRejectNullSerials() {
        assertThrows(NullPointerException.class, () -> new TokenWipeNftTransaction(tokenIdValue(), accountIdValue(), (List<Long>) null));
    }

    @Test
    @DisplayName("serials: accepts the minimum number of elements")
    void shouldAcceptSerialsAtMinimumSize() {
        final List<Long> value = List.of(1L);
        final TokenWipeNftTransaction subject = new TokenWipeNftTransaction(tokenIdValue(), accountIdValue(), value);
        assertValue(value, subject.serials());
    }

    @Test
    @DisplayName("serials: rejects fewer elements than the minimum")
    void shouldRejectSerialsBelowMinimumSize() {
        assertThrows(IllegalArgumentException.class, () -> new TokenWipeNftTransaction(tokenIdValue(), accountIdValue(), List.of()));
    }

    @Test
    @DisplayName("serials: accepts the maximum number of elements")
    void shouldAcceptSerialsAtMaximumSize() {
        final List<Long> value = List.of(1L, 2L, 3L, 4L, 5L, 6L, 7L, 8L, 9L, 10L);
        final TokenWipeNftTransaction subject = new TokenWipeNftTransaction(tokenIdValue(), accountIdValue(), value);
        assertValue(value, subject.serials());
    }

    @Test
    @DisplayName("serials: rejects more elements than the maximum")
    void shouldRejectSerialsAboveMaximumSize() {
        assertThrows(IllegalArgumentException.class, () -> new TokenWipeNftTransaction(tokenIdValue(), accountIdValue(), List.of(1L, 2L, 3L, 4L, 5L, 6L, 7L, 8L, 9L, 10L, 11L)));
    }

    @Test
    @DisplayName("the constructor copies serials; the accessor returns an unmodifiable collection")
    void shouldCopySerialsIn() {
        final List<Long> value = new ArrayList<>(serialsValue());
        final List<Long> expected = List.copyOf(value);
        final TokenWipeNftTransaction subject = new TokenWipeNftTransaction(tokenIdValue(), accountIdValue(), value);
        value.clear();
        assertValue(expected, subject.serials());
        assertThrows(UnsupportedOperationException.class, () -> Objects.requireNonNull(subject.serials()).clear());
    }

    @Test
    @DisplayName("setMaxTransactionFee changes maxTransactionFee and returns the object")
    void shouldSetMaxTransactionFee() {
        final TokenWipeNftTransaction subject = create();
        final @Nullable NativeToken<?, ?> value = maxTransactionFeeOtherValue();
        assertSame(subject, subject.setMaxTransactionFee(value));
        assertValue(value, subject.maxTransactionFee());
    }

    @Test
    @DisplayName("setMaxTransactionFee accepts null")
    void shouldSetMaxTransactionFeeToNull() {
        final TokenWipeNftTransaction subject = create();
        subject.setMaxTransactionFee((NativeToken<?, ?>) null);
        assertNull(subject.maxTransactionFee());
    }

    @Test
    @DisplayName("setValidDuration changes validDuration and returns the object")
    void shouldSetValidDuration() {
        final TokenWipeNftTransaction subject = create();
        final @Nullable Duration value = validDurationOtherValue();
        assertSame(subject, subject.setValidDuration(value));
        assertValue(value, subject.validDuration());
    }

    @Test
    @DisplayName("setValidDuration accepts null")
    void shouldSetValidDurationToNull() {
        final TokenWipeNftTransaction subject = create();
        subject.setValidDuration((Duration) null);
        assertNull(subject.validDuration());
    }

    @Test
    @DisplayName("setMemo changes memo and returns the object")
    void shouldSetMemo() {
        final TokenWipeNftTransaction subject = create();
        final @Nullable String value = memoOtherValue();
        assertSame(subject, subject.setMemo(value));
        assertValue(value, subject.memo());
    }

    @Test
    @DisplayName("setMemo accepts null")
    void shouldSetMemoToNull() {
        final TokenWipeNftTransaction subject = create();
        subject.setMemo((String) null);
        assertNull(subject.memo());
    }

    @Test
    @DisplayName("pack(payer, nodes) can be called and returns a value")
    void shouldCallPack() {
        assertNotNull(create().pack(payerValue(), nodesValue()));
    }

    @Test
    @DisplayName("signWithOperator(client) can be called and returns a value")
    void shouldCallSignWithOperator() {
        assertNotNull(create().signWithOperator(clientValue()));
    }

    @Test
    @DisplayName("sign(payer, nodes) can be called and returns a value")
    void shouldCallSign() {
        assertNotNull(create().sign(payerValue(), nodesValue()));
    }

    @Test
    @DisplayName("sign(payerId, signer, nodes) can be called and returns a value")
    void shouldCallSign2() {
        assertNotNull(create().sign(payerIdValue(), signerValue(), nodesValue()));
    }

    @Test
    @DisplayName("signWithOperatorAndSubmit(client) can be called and returns a value")
    void shouldCallSignWithOperatorAndSubmit() {
        assertNotNull(create().signWithOperatorAndSubmit(clientValue()));
    }

    @Test
    @DisplayName("packForBatch(payer, batchKey) can be called and returns a value")
    void shouldCallPackForBatch() {
        assertNotNull(create().packForBatch(payerValue(), batchKeyValue()));
    }

    @Test
    @DisplayName("signForBatchWithOperator(client, batchKey) can be called and returns a value")
    void shouldCallSignForBatchWithOperator() {
        assertNotNull(create().signForBatchWithOperator(clientValue(), batchKeyValue()));
    }

    @Test
    @DisplayName("signForBatch(payer, batchKey) can be called and returns a value")
    void shouldCallSignForBatch() {
        assertNotNull(create().signForBatch(payerValue(), batchKeyValue()));
    }

    @Test
    @DisplayName("signForBatch(payerId, signer, batchKey) can be called and returns a value")
    void shouldCallSignForBatch2() {
        assertNotNull(create().signForBatch(payerIdValue(), signerValue(), batchKeyValue()));
    }

    private static Address tokenIdValue() {
        return new Address(0L, 0L, "", 1001L);
    }

    private static AccountId accountIdValue() {
        return new AccountId(0L, 0L, "", 1001L, null, null);
    }

    private static List<Long> serialsValue() {
        return List.of(1L);
    }

    private static TokenWipeNftTransaction create() {
        return new TokenWipeNftTransaction(tokenIdValue(), accountIdValue(), serialsValue());
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
