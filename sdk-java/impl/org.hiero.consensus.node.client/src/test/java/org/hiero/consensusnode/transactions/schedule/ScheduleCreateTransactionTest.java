package org.hiero.consensusnode.transactions.schedule;

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
import org.hiero.consensusnode.transactions.Transaction;
import org.hiero.consensusnode.transactions.accounts.AccountAllowanceApproveTransaction;
import org.hiero.consensusnode.transactions.accounts.HbarAllowance;
import org.hiero.consensusnode.transactions.accounts.NftAllowance;
import org.hiero.consensusnode.transactions.accounts.NftCollectionGrant;
import org.hiero.consensusnode.transactions.accounts.TokenAllowance;
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

/// Tests of `ScheduleCreateTransaction`, generated from the specs.
@DisplayName("ScheduleCreateTransaction")
class ScheduleCreateTransactionTest {

    @Test
    @DisplayName("creates a ScheduleCreateTransaction and returns the values")
    void shouldCreateAndReturnTheValues() {
        final Transaction<?, ?> scheduledTransaction = scheduledTransactionValue();
        final @Nullable Authority adminKey = adminKeyValue();
        final @Nullable AccountId payerAccountId = payerAccountIdValue();
        final @Nullable String scheduleMemo = scheduleMemoValue();
        final @Nullable ZonedDateTime expirationTime = expirationTimeValue();
        final boolean waitForExpiry = waitForExpiryValue();
        final ScheduleCreateTransaction subject = new ScheduleCreateTransaction(scheduledTransaction, adminKey, payerAccountId, scheduleMemo, expirationTime, waitForExpiry);
        assertValue(scheduledTransaction, subject.scheduledTransaction());
        assertValue(adminKey, subject.adminKey());
        assertValue(payerAccountId, subject.payerAccountId());
        assertValue(scheduleMemo, subject.scheduleMemo());
        assertValue(expirationTime, subject.expirationTime());
        assertValue(waitForExpiry, subject.waitForExpiry());
        assertNull(subject.maxTransactionFee());
        assertNull(subject.validDuration());
        assertNull(subject.memo());
    }

    @Test
    @DisplayName("creates a ScheduleCreateTransaction with the default values")
    void shouldCreateWithTheDefaultValues() {
        final ScheduleCreateTransaction subject = new ScheduleCreateTransaction(scheduledTransactionValue(), adminKeyValue(), payerAccountIdValue(), scheduleMemoValue(), expirationTimeValue());
        assertValue(false, subject.waitForExpiry());
    }

    @Test
    @DisplayName("the builder builds the values of the constructor")
    void shouldBuildTheValuesOfTheConstructor() {
        final Transaction<?, ?> scheduledTransaction = scheduledTransactionValue();
        final @Nullable Authority adminKey = adminKeyValue();
        final @Nullable AccountId payerAccountId = payerAccountIdValue();
        final @Nullable String scheduleMemo = scheduleMemoValue();
        final @Nullable ZonedDateTime expirationTime = expirationTimeValue();
        final boolean waitForExpiry = waitForExpiryValue();
        final ScheduleCreateTransaction subject = ScheduleCreateTransaction.builder()
                .scheduledTransaction(scheduledTransaction)
                .adminKey(adminKey)
                .payerAccountId(payerAccountId)
                .scheduleMemo(scheduleMemo)
                .expirationTime(expirationTime)
                .waitForExpiry(waitForExpiry)
                .build();
        assertValue(scheduledTransaction, subject.scheduledTransaction());
        assertValue(adminKey, subject.adminKey());
        assertValue(payerAccountId, subject.payerAccountId());
        assertValue(scheduleMemo, subject.scheduleMemo());
        assertValue(expirationTime, subject.expirationTime());
        assertValue(waitForExpiry, subject.waitForExpiry());
    }

    @Test
    @DisplayName("a value that is not set is null or its default value")
    void shouldBuildWithoutTheOptionalValues() {
        final ScheduleCreateTransaction subject = ScheduleCreateTransaction.builder()
                .scheduledTransaction(scheduledTransactionValue())
                .build();
        assertNull(subject.adminKey());
        assertNull(subject.payerAccountId());
        assertNull(subject.scheduleMemo());
        assertNull(subject.expirationTime());
        assertValue(false, subject.waitForExpiry());
    }

    @Test
    @DisplayName("the builder rejects a missing required value")
    void shouldRejectAMissingRequiredValue() {
        assertThrows(NullPointerException.class, () -> ScheduleCreateTransaction.builder().build());
    }

    @Test
    @DisplayName("rejects null for scheduledTransaction")
    void shouldRejectNullScheduledTransaction() {
        assertThrows(NullPointerException.class, () -> new ScheduleCreateTransaction((Transaction<?, ?>) null, adminKeyValue(), payerAccountIdValue(), scheduleMemoValue(), expirationTimeValue(), waitForExpiryValue()));
    }

    @Test
    @DisplayName("setMaxTransactionFee changes maxTransactionFee and returns the object")
    void shouldSetMaxTransactionFee() {
        final ScheduleCreateTransaction subject = create();
        final @Nullable NativeToken<?, ?> value = maxTransactionFeeOtherValue();
        assertSame(subject, subject.setMaxTransactionFee(value));
        assertValue(value, subject.maxTransactionFee());
    }

    @Test
    @DisplayName("setMaxTransactionFee accepts null")
    void shouldSetMaxTransactionFeeToNull() {
        final ScheduleCreateTransaction subject = create();
        subject.setMaxTransactionFee((NativeToken<?, ?>) null);
        assertNull(subject.maxTransactionFee());
    }

    @Test
    @DisplayName("setValidDuration changes validDuration and returns the object")
    void shouldSetValidDuration() {
        final ScheduleCreateTransaction subject = create();
        final @Nullable Duration value = validDurationOtherValue();
        assertSame(subject, subject.setValidDuration(value));
        assertValue(value, subject.validDuration());
    }

    @Test
    @DisplayName("setValidDuration accepts null")
    void shouldSetValidDurationToNull() {
        final ScheduleCreateTransaction subject = create();
        subject.setValidDuration((Duration) null);
        assertNull(subject.validDuration());
    }

    @Test
    @DisplayName("setMemo changes memo and returns the object")
    void shouldSetMemo() {
        final ScheduleCreateTransaction subject = create();
        final @Nullable String value = memoOtherValue();
        assertSame(subject, subject.setMemo(value));
        assertValue(value, subject.memo());
    }

    @Test
    @DisplayName("setMemo accepts null")
    void shouldSetMemoToNull() {
        final ScheduleCreateTransaction subject = create();
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

    private static Transaction<?, ?> scheduledTransactionValue() {
        return new AccountAllowanceApproveTransaction(List.of(new HbarAllowance(new AccountId(0L, 0L, "", 1001L, null, null), new AccountId(0L, 0L, "", 1001L, null, null), new Hbar(1L, HbarUnit.TINYBAR))), List.of(new TokenAllowance(new Address(0L, 0L, "", 1001L), new AccountId(0L, 0L, "", 1001L, null, null), new AccountId(0L, 0L, "", 1001L, null, null), 1L)), List.of(new NftAllowance(new Address(0L, 0L, "", 1001L), new AccountId(0L, 0L, "", 1001L, null, null), new AccountId(0L, 0L, "", 1001L, null, null), new NftCollectionGrant(true), null)));
    }

    private static Authority adminKeyValue() {
        return new PublicKeyAuthority(KeysFactory.createPrivateKey(KeyAlgorithm.ED25519, ByteImportEncoding.HEX, "d3671a1e98bb22f011c0e4bcf5125590e15d8f21a7017309bb558852039bc75c").createPublicKey());
    }

    private static AccountId payerAccountIdValue() {
        return new AccountId(0L, 0L, "", 1001L, null, null);
    }

    private static String scheduleMemoValue() {
        return "value";
    }

    private static ZonedDateTime expirationTimeValue() {
        return ZonedDateTime.of(2024, 1, 1, 12, 0, 0, 0, ZoneOffset.UTC);
    }

    private static boolean waitForExpiryValue() {
        return true;
    }

    private static ScheduleCreateTransaction create() {
        return new ScheduleCreateTransaction(scheduledTransactionValue(), adminKeyValue(), payerAccountIdValue(), scheduleMemoValue(), expirationTimeValue(), waitForExpiryValue());
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
