package org.hiero.consensusnode.transactions.files;

import static org.hiero.sdk.testing.Testing.assertValue;
import static org.junit.jupiter.api.Assertions.*;

import java.time.Duration;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
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

/// Tests of `FileUpdateTransaction`, generated from the specs.
@DisplayName("FileUpdateTransaction")
class FileUpdateTransactionTest {

    @Test
    @DisplayName("creates a FileUpdateTransaction and returns the values")
    @Tag("testId:consensusnode.transactions.files.FileUpdateTransaction/create")
    void shouldCreateAndReturnTheValues() {
        final Address fileId = fileIdValue();
        final byte @Nullable [] contents = contentsValue();
        final @Nullable Authority authority = authorityValue();
        final @Nullable ZonedDateTime expirationTime = expirationTimeValue();
        final @Nullable String fileMemo = fileMemoValue();
        final FileUpdateTransaction subject = new FileUpdateTransaction(fileId, contents, authority, expirationTime, fileMemo);
        assertValue(fileId, subject.fileId());
        assertValue(contents, subject.contents());
        assertValue(authority, subject.authority());
        assertValue(expirationTime, subject.expirationTime());
        assertValue(fileMemo, subject.fileMemo());
        assertNull(subject.maxTransactionFee());
        assertNull(subject.validDuration());
        assertNull(subject.memo());
    }

    @Test
    @DisplayName("the builder builds the values of the constructor")
    @Tag("testId:consensusnode.transactions.files.FileUpdateTransaction/builder/builds")
    void shouldBuildTheValuesOfTheConstructor() {
        final Address fileId = fileIdValue();
        final byte @Nullable [] contents = contentsValue();
        final @Nullable Authority authority = authorityValue();
        final @Nullable ZonedDateTime expirationTime = expirationTimeValue();
        final @Nullable String fileMemo = fileMemoValue();
        final FileUpdateTransaction subject = FileUpdateTransaction.builder()
                .fileId(fileId)
                .contents(contents)
                .authority(authority)
                .expirationTime(expirationTime)
                .fileMemo(fileMemo)
                .build();
        assertValue(fileId, subject.fileId());
        assertValue(contents, subject.contents());
        assertValue(authority, subject.authority());
        assertValue(expirationTime, subject.expirationTime());
        assertValue(fileMemo, subject.fileMemo());
    }

    @Test
    @DisplayName("a value that is not set is null or its default value")
    @Tag("testId:consensusnode.transactions.files.FileUpdateTransaction/builder/optional-values")
    void shouldBuildWithoutTheOptionalValues() {
        final FileUpdateTransaction subject = FileUpdateTransaction.builder()
                .fileId(fileIdValue())
                .build();
        assertNull(subject.contents());
        assertNull(subject.authority());
        assertNull(subject.expirationTime());
        assertNull(subject.fileMemo());
    }

    @Test
    @DisplayName("the builder rejects a missing required value")
    @Tag("testId:consensusnode.transactions.files.FileUpdateTransaction/builder/rejects-missing-value")
    void shouldRejectAMissingRequiredValue() {
        assertThrows(NullPointerException.class, () -> FileUpdateTransaction.builder().build());
    }

    @Test
    @DisplayName("rejects null for fileId")
    @Tag("testId:consensusnode.transactions.files.FileUpdateTransaction/fileId/rejects-null")
    void shouldRejectNullFileId() {
        assertThrows(NullPointerException.class, () -> new FileUpdateTransaction((Address) null, contentsValue(), authorityValue(), expirationTimeValue(), fileMemoValue()));
    }

    @Test
    @DisplayName("the constructor copies contents; the accessor returns a copy")
    @Tag("testId:consensusnode.transactions.files.FileUpdateTransaction/contents/copies-in")
    void shouldCopyContentsIn() {
        final byte[] value = contentsValue();
        final byte[] expected = value.clone();
        final FileUpdateTransaction subject = new FileUpdateTransaction(fileIdValue(), value, authorityValue(), expirationTimeValue(), fileMemoValue());
        value[0] = (byte) (value[0] + 1);
        assertValue(expected, subject.contents());
        final byte[] returned = subject.contents();
        assertNotNull(returned);
        returned[0] = (byte) (returned[0] + 1);
        assertValue(expected, subject.contents());
    }

    @Test
    @DisplayName("fileMemo: accepts more elements than the maximum")
    @Tag("testId:consensusnode.transactions.files.FileUpdateTransaction/fileMemo/accepts-at-maximum-bytes")
    void shouldAcceptFileMemoAtMaximumBytes() {
        final @Nullable String value = "ääääääääääääääääääääääääääääääääääääääääääääääääää";
        final FileUpdateTransaction subject = new FileUpdateTransaction(fileIdValue(), contentsValue(), authorityValue(), expirationTimeValue(), value);
        assertValue(value, subject.fileMemo());
    }

    @Test
    @DisplayName("fileMemo: rejects more elements than the maximum")
    @Tag("testId:consensusnode.transactions.files.FileUpdateTransaction/fileMemo/rejects-above-maximum-bytes")
    void shouldRejectFileMemoAboveMaximumBytes() {
        assertThrows(IllegalArgumentException.class, () -> new FileUpdateTransaction(fileIdValue(), contentsValue(), authorityValue(), expirationTimeValue(), "ääääääääääääääääääääääääääääääääääääääääääääääääääa"));
    }

    @Test
    @DisplayName("setMaxTransactionFee changes maxTransactionFee and returns the object")
    @Tag("testId:consensusnode.transactions.files.FileUpdateTransaction/maxTransactionFee/set")
    void shouldSetMaxTransactionFee() {
        final FileUpdateTransaction subject = create();
        final @Nullable NativeToken<?, ?> value = maxTransactionFeeOtherValue();
        assertSame(subject, subject.setMaxTransactionFee(value));
        assertValue(value, subject.maxTransactionFee());
    }

    @Test
    @DisplayName("setMaxTransactionFee accepts null")
    @Tag("testId:consensusnode.transactions.files.FileUpdateTransaction/maxTransactionFee/set-null")
    void shouldSetMaxTransactionFeeToNull() {
        final FileUpdateTransaction subject = create();
        subject.setMaxTransactionFee((NativeToken<?, ?>) null);
        assertNull(subject.maxTransactionFee());
    }

    @Test
    @DisplayName("setValidDuration changes validDuration and returns the object")
    @Tag("testId:consensusnode.transactions.files.FileUpdateTransaction/validDuration/set")
    void shouldSetValidDuration() {
        final FileUpdateTransaction subject = create();
        final @Nullable Duration value = validDurationOtherValue();
        assertSame(subject, subject.setValidDuration(value));
        assertValue(value, subject.validDuration());
    }

    @Test
    @DisplayName("setValidDuration accepts null")
    @Tag("testId:consensusnode.transactions.files.FileUpdateTransaction/validDuration/set-null")
    void shouldSetValidDurationToNull() {
        final FileUpdateTransaction subject = create();
        subject.setValidDuration((Duration) null);
        assertNull(subject.validDuration());
    }

    @Test
    @DisplayName("setMemo changes memo and returns the object")
    @Tag("testId:consensusnode.transactions.files.FileUpdateTransaction/memo/set")
    void shouldSetMemo() {
        final FileUpdateTransaction subject = create();
        final @Nullable String value = memoOtherValue();
        assertSame(subject, subject.setMemo(value));
        assertValue(value, subject.memo());
    }

    @Test
    @DisplayName("setMemo accepts null")
    @Tag("testId:consensusnode.transactions.files.FileUpdateTransaction/memo/set-null")
    void shouldSetMemoToNull() {
        final FileUpdateTransaction subject = create();
        subject.setMemo((String) null);
        assertNull(subject.memo());
    }

    @Test
    @DisplayName("setMemo accepts more elements than the maximum")
    @Tag("testId:consensusnode.transactions.files.FileUpdateTransaction/memo/set-accepts-at-maximum-bytes")
    void shouldSetMemoAtMaximumBytes() {
        final FileUpdateTransaction subject = create();
        final @Nullable String value = "ääääääääääääääääääääääääääääääääääääääääääääääääää";
        subject.setMemo(value);
        assertValue(value, subject.memo());
    }

    @Test
    @DisplayName("setMemo rejects more elements than the maximum and keeps the value")
    @Tag("testId:consensusnode.transactions.files.FileUpdateTransaction/memo/set-rejects-above-maximum-bytes")
    void shouldNotSetMemoAboveMaximumBytes() {
        final FileUpdateTransaction subject = create();
        assertThrows(IllegalArgumentException.class, () -> subject.setMemo("ääääääääääääääääääääääääääääääääääääääääääääääääääa"));
        assertValue(null, subject.memo());
    }

    @Test
    @DisplayName("pack(payer, nodes) can be called and returns a value")
    @Tag("testId:consensusnode.transactions.files.FileUpdateTransaction/pack/call")
    void shouldCallPack() {
        assertNotNull(create().pack(payerValue(), nodesValue()));
    }

    @Test
    @DisplayName("signWithOperator(client) can be called and returns a value")
    @Tag("testId:consensusnode.transactions.files.FileUpdateTransaction/signWithOperator/call")
    void shouldCallSignWithOperator() {
        assertNotNull(create().signWithOperator(clientValue()));
    }

    @Test
    @DisplayName("sign(payer, nodes) can be called and returns a value")
    @Tag("testId:consensusnode.transactions.files.FileUpdateTransaction/sign/call")
    void shouldCallSign() {
        assertNotNull(create().sign(payerValue(), nodesValue()));
    }

    @Test
    @DisplayName("sign(payerId, signer, nodes) can be called and returns a value")
    @Tag("testId:consensusnode.transactions.files.FileUpdateTransaction/sign/call-2")
    void shouldCallSign2() {
        assertNotNull(create().sign(payerIdValue(), signerValue(), nodesValue()));
    }

    @Test
    @DisplayName("signWithOperatorAndSubmit(client) can be called and returns a value")
    @Tag("testId:consensusnode.transactions.files.FileUpdateTransaction/signWithOperatorAndSubmit/call")
    void shouldCallSignWithOperatorAndSubmit() {
        assertNotNull(create().signWithOperatorAndSubmit(clientValue()));
    }

    @Test
    @DisplayName("packForBatch(payer, batchKey) can be called and returns a value")
    @Tag("testId:consensusnode.transactions.files.FileUpdateTransaction/packForBatch/call")
    void shouldCallPackForBatch() {
        assertNotNull(create().packForBatch(payerValue(), batchKeyValue()));
    }

    @Test
    @DisplayName("signForBatchWithOperator(client, batchKey) can be called and returns a value")
    @Tag("testId:consensusnode.transactions.files.FileUpdateTransaction/signForBatchWithOperator/call")
    void shouldCallSignForBatchWithOperator() {
        assertNotNull(create().signForBatchWithOperator(clientValue(), batchKeyValue()));
    }

    @Test
    @DisplayName("signForBatch(payer, batchKey) can be called and returns a value")
    @Tag("testId:consensusnode.transactions.files.FileUpdateTransaction/signForBatch/call")
    void shouldCallSignForBatch() {
        assertNotNull(create().signForBatch(payerValue(), batchKeyValue()));
    }

    @Test
    @DisplayName("signForBatch(payerId, signer, batchKey) can be called and returns a value")
    @Tag("testId:consensusnode.transactions.files.FileUpdateTransaction/signForBatch/call-2")
    void shouldCallSignForBatch2() {
        assertNotNull(create().signForBatch(payerIdValue(), signerValue(), batchKeyValue()));
    }

    private static Address fileIdValue() {
        return new Address(0L, 0L, "", 1001L);
    }

    private static byte[] contentsValue() {
        return new byte[] {1, 2, 3};
    }

    private static Authority authorityValue() {
        return new PublicKeyAuthority(KeysFactory.createPrivateKey(KeyAlgorithm.ED25519, ByteImportEncoding.HEX, "d3671a1e98bb22f011c0e4bcf5125590e15d8f21a7017309bb558852039bc75c").createPublicKey());
    }

    private static ZonedDateTime expirationTimeValue() {
        return ZonedDateTime.of(2024, 1, 1, 12, 0, 0, 0, ZoneOffset.UTC);
    }

    private static String fileMemoValue() {
        return "value";
    }

    private static FileUpdateTransaction create() {
        return new FileUpdateTransaction(fileIdValue(), contentsValue(), authorityValue(), expirationTimeValue(), fileMemoValue());
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
