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
import org.hiero.consensusnode.transactions.accounts.NftTransfer;
import org.hiero.consensusnode.transactions.accounts.TokenTransfer;
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

/// Tests of `TokenAirdropTransaction`, generated from the specs.
@DisplayName("TokenAirdropTransaction")
class TokenAirdropTransactionTest {

    @Test
    @DisplayName("creates a TokenAirdropTransaction and returns the values")
    @Tag("testId:consensusnode.transactions.tokens.TokenAirdropTransaction/create")
    void shouldCreateAndReturnTheValues() {
        final List<TokenTransfer> tokenTransfers = tokenTransfersValue();
        final List<NftTransfer> nftTransfers = nftTransfersValue();
        final TokenAirdropTransaction subject = new TokenAirdropTransaction(tokenTransfers, nftTransfers);
        assertValue(tokenTransfers, subject.tokenTransfers());
        assertValue(nftTransfers, subject.nftTransfers());
        assertNull(subject.maxTransactionFee());
        assertNull(subject.validDuration());
        assertNull(subject.memo());
    }

    @Test
    @DisplayName("creates a TokenAirdropTransaction with the default values")
    @Tag("testId:consensusnode.transactions.tokens.TokenAirdropTransaction/create-with-defaults")
    void shouldCreateWithTheDefaultValues() {
        final TokenAirdropTransaction subject = new TokenAirdropTransaction();
        assertValue(List.of(), subject.tokenTransfers());
        assertValue(List.of(), subject.nftTransfers());
    }

    @Test
    @DisplayName("the builder builds the values of the constructor")
    @Tag("testId:consensusnode.transactions.tokens.TokenAirdropTransaction/builder/builds")
    void shouldBuildTheValuesOfTheConstructor() {
        final List<TokenTransfer> tokenTransfers = tokenTransfersValue();
        final List<NftTransfer> nftTransfers = nftTransfersValue();
        final TokenAirdropTransaction subject = TokenAirdropTransaction.builder()
                .tokenTransfers(tokenTransfers)
                .nftTransfers(nftTransfers)
                .build();
        assertValue(tokenTransfers, subject.tokenTransfers());
        assertValue(nftTransfers, subject.nftTransfers());
    }

    @Test
    @DisplayName("a value that is not set is null or its default value")
    @Tag("testId:consensusnode.transactions.tokens.TokenAirdropTransaction/builder/optional-values")
    void shouldBuildWithoutTheOptionalValues() {
        final TokenAirdropTransaction subject = TokenAirdropTransaction.builder()
                .build();
        assertValue(List.of(), subject.tokenTransfers());
        assertValue(List.of(), subject.nftTransfers());
    }

    @Test
    @DisplayName("rejects null for tokenTransfers")
    @Tag("testId:consensusnode.transactions.tokens.TokenAirdropTransaction/tokenTransfers/rejects-null")
    void shouldRejectNullTokenTransfers() {
        assertThrows(NullPointerException.class, () -> new TokenAirdropTransaction((List<TokenTransfer>) null, nftTransfersValue()));
    }

    @Test
    @DisplayName("the constructor copies tokenTransfers; the accessor returns an unmodifiable collection")
    @Tag("testId:consensusnode.transactions.tokens.TokenAirdropTransaction/tokenTransfers/copies-in")
    void shouldCopyTokenTransfersIn() {
        final List<TokenTransfer> value = new ArrayList<>(tokenTransfersValue());
        final List<TokenTransfer> expected = List.copyOf(value);
        final TokenAirdropTransaction subject = new TokenAirdropTransaction(value, nftTransfersValue());
        value.clear();
        assertValue(expected, subject.tokenTransfers());
        assertThrows(UnsupportedOperationException.class, () -> Objects.requireNonNull(subject.tokenTransfers()).clear());
    }

    @Test
    @DisplayName("rejects null for nftTransfers")
    @Tag("testId:consensusnode.transactions.tokens.TokenAirdropTransaction/nftTransfers/rejects-null")
    void shouldRejectNullNftTransfers() {
        assertThrows(NullPointerException.class, () -> new TokenAirdropTransaction(tokenTransfersValue(), (List<NftTransfer>) null));
    }

    @Test
    @DisplayName("the constructor copies nftTransfers; the accessor returns an unmodifiable collection")
    @Tag("testId:consensusnode.transactions.tokens.TokenAirdropTransaction/nftTransfers/copies-in")
    void shouldCopyNftTransfersIn() {
        final List<NftTransfer> value = new ArrayList<>(nftTransfersValue());
        final List<NftTransfer> expected = List.copyOf(value);
        final TokenAirdropTransaction subject = new TokenAirdropTransaction(tokenTransfersValue(), value);
        value.clear();
        assertValue(expected, subject.nftTransfers());
        assertThrows(UnsupportedOperationException.class, () -> Objects.requireNonNull(subject.nftTransfers()).clear());
    }

    @Test
    @DisplayName("setMaxTransactionFee changes maxTransactionFee and returns the object")
    @Tag("testId:consensusnode.transactions.tokens.TokenAirdropTransaction/maxTransactionFee/set")
    void shouldSetMaxTransactionFee() {
        final TokenAirdropTransaction subject = create();
        final @Nullable NativeToken<?, ?> value = maxTransactionFeeOtherValue();
        assertSame(subject, subject.setMaxTransactionFee(value));
        assertValue(value, subject.maxTransactionFee());
    }

    @Test
    @DisplayName("setMaxTransactionFee accepts null")
    @Tag("testId:consensusnode.transactions.tokens.TokenAirdropTransaction/maxTransactionFee/set-null")
    void shouldSetMaxTransactionFeeToNull() {
        final TokenAirdropTransaction subject = create();
        subject.setMaxTransactionFee((NativeToken<?, ?>) null);
        assertNull(subject.maxTransactionFee());
    }

    @Test
    @DisplayName("setValidDuration changes validDuration and returns the object")
    @Tag("testId:consensusnode.transactions.tokens.TokenAirdropTransaction/validDuration/set")
    void shouldSetValidDuration() {
        final TokenAirdropTransaction subject = create();
        final @Nullable Duration value = validDurationOtherValue();
        assertSame(subject, subject.setValidDuration(value));
        assertValue(value, subject.validDuration());
    }

    @Test
    @DisplayName("setValidDuration accepts null")
    @Tag("testId:consensusnode.transactions.tokens.TokenAirdropTransaction/validDuration/set-null")
    void shouldSetValidDurationToNull() {
        final TokenAirdropTransaction subject = create();
        subject.setValidDuration((Duration) null);
        assertNull(subject.validDuration());
    }

    @Test
    @DisplayName("setMemo changes memo and returns the object")
    @Tag("testId:consensusnode.transactions.tokens.TokenAirdropTransaction/memo/set")
    void shouldSetMemo() {
        final TokenAirdropTransaction subject = create();
        final @Nullable String value = memoOtherValue();
        assertSame(subject, subject.setMemo(value));
        assertValue(value, subject.memo());
    }

    @Test
    @DisplayName("setMemo accepts null")
    @Tag("testId:consensusnode.transactions.tokens.TokenAirdropTransaction/memo/set-null")
    void shouldSetMemoToNull() {
        final TokenAirdropTransaction subject = create();
        subject.setMemo((String) null);
        assertNull(subject.memo());
    }

    @Test
    @DisplayName("setMemo accepts more elements than the maximum")
    @Tag("testId:consensusnode.transactions.tokens.TokenAirdropTransaction/memo/set-accepts-at-maximum-bytes")
    void shouldSetMemoAtMaximumBytes() {
        final TokenAirdropTransaction subject = create();
        final @Nullable String value = "ääääääääääääääääääääääääääääääääääääääääääääääääää";
        subject.setMemo(value);
        assertValue(value, subject.memo());
    }

    @Test
    @DisplayName("setMemo rejects more elements than the maximum and keeps the value")
    @Tag("testId:consensusnode.transactions.tokens.TokenAirdropTransaction/memo/set-rejects-above-maximum-bytes")
    void shouldNotSetMemoAboveMaximumBytes() {
        final TokenAirdropTransaction subject = create();
        assertThrows(IllegalArgumentException.class, () -> subject.setMemo("ääääääääääääääääääääääääääääääääääääääääääääääääääa"));
        assertValue(null, subject.memo());
    }

    @Test
    @DisplayName("pack(payer, nodes) can be called and returns a value")
    @Tag("testId:consensusnode.transactions.tokens.TokenAirdropTransaction/pack/call")
    void shouldCallPack() {
        assertNotNull(create().pack(payerValue(), nodesValue()));
    }

    @Test
    @DisplayName("signWithOperator(client) can be called and returns a value")
    @Tag("testId:consensusnode.transactions.tokens.TokenAirdropTransaction/signWithOperator/call")
    void shouldCallSignWithOperator() {
        assertNotNull(create().signWithOperator(clientValue()));
    }

    @Test
    @DisplayName("sign(payer, nodes) can be called and returns a value")
    @Tag("testId:consensusnode.transactions.tokens.TokenAirdropTransaction/sign/call")
    void shouldCallSign() {
        assertNotNull(create().sign(payerValue(), nodesValue()));
    }

    @Test
    @DisplayName("sign(payerId, signer, nodes) can be called and returns a value")
    @Tag("testId:consensusnode.transactions.tokens.TokenAirdropTransaction/sign/call-2")
    void shouldCallSign2() {
        assertNotNull(create().sign(payerIdValue(), signerValue(), nodesValue()));
    }

    @Test
    @DisplayName("signWithOperatorAndSubmit(client) can be called and returns a value")
    @Tag("testId:consensusnode.transactions.tokens.TokenAirdropTransaction/signWithOperatorAndSubmit/call")
    void shouldCallSignWithOperatorAndSubmit() {
        assertNotNull(create().signWithOperatorAndSubmit(clientValue()));
    }

    @Test
    @DisplayName("packForBatch(payer, batchKey) can be called and returns a value")
    @Tag("testId:consensusnode.transactions.tokens.TokenAirdropTransaction/packForBatch/call")
    void shouldCallPackForBatch() {
        assertNotNull(create().packForBatch(payerValue(), batchKeyValue()));
    }

    @Test
    @DisplayName("signForBatchWithOperator(client, batchKey) can be called and returns a value")
    @Tag("testId:consensusnode.transactions.tokens.TokenAirdropTransaction/signForBatchWithOperator/call")
    void shouldCallSignForBatchWithOperator() {
        assertNotNull(create().signForBatchWithOperator(clientValue(), batchKeyValue()));
    }

    @Test
    @DisplayName("signForBatch(payer, batchKey) can be called and returns a value")
    @Tag("testId:consensusnode.transactions.tokens.TokenAirdropTransaction/signForBatch/call")
    void shouldCallSignForBatch() {
        assertNotNull(create().signForBatch(payerValue(), batchKeyValue()));
    }

    @Test
    @DisplayName("signForBatch(payerId, signer, batchKey) can be called and returns a value")
    @Tag("testId:consensusnode.transactions.tokens.TokenAirdropTransaction/signForBatch/call-2")
    void shouldCallSignForBatch2() {
        assertNotNull(create().signForBatch(payerIdValue(), signerValue(), batchKeyValue()));
    }

    private static List<TokenTransfer> tokenTransfersValue() {
        return List.of(new TokenTransfer(new Address(0L, 0L, "", 1001L), new AccountId(0L, 0L, "", 1001L, null, null), 1L, null, true));
    }

    private static List<NftTransfer> nftTransfersValue() {
        return List.of(new NftTransfer(new Address(0L, 0L, "", 1001L), 1L, new AccountId(0L, 0L, "", 1001L, null, null), new AccountId(0L, 0L, "", 1001L, null, null), true));
    }

    private static TokenAirdropTransaction create() {
        return new TokenAirdropTransaction(tokenTransfersValue(), nftTransfersValue());
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
