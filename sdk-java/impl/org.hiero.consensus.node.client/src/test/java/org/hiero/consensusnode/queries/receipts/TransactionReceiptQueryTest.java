package org.hiero.consensusnode.queries.receipts;

import static org.hiero.sdk.testing.Testing.assertValue;
import static org.junit.jupiter.api.Assertions.*;

import java.time.Duration;
import java.util.Set;
import org.hiero.consensusnode.client.Account;
import org.hiero.consensusnode.client.ClientFactory;
import org.hiero.consensusnode.client.ExecutionSettings;
import org.hiero.consensusnode.client.HieroClient;
import org.hiero.consensusnode.client.RetryPolicy;
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
import org.hiero.ledger.TransactionId;
import org.hiero.ledger.config.NetworkSetting;
import org.jspecify.annotations.Nullable;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

/// Tests of `TransactionReceiptQuery`, generated from the specs.
@DisplayName("TransactionReceiptQuery")
class TransactionReceiptQueryTest {

    @Test
    @DisplayName("creates a TransactionReceiptQuery and returns the values")
    @Tag("testId:consensusnode.queries.receipts.TransactionReceiptQuery/create")
    void shouldCreateAndReturnTheValues() {
        final TransactionId transactionId = transactionIdValue();
        final boolean includeDuplicates = includeDuplicatesValue();
        final boolean includeChildren = includeChildrenValue();
        final @Nullable AccountId nodeAccountId = nodeAccountIdValue();
        final TransactionReceiptQuery subject = new TransactionReceiptQuery(transactionId, includeDuplicates, includeChildren, nodeAccountId);
        assertValue(transactionId, subject.transactionId());
        assertValue(includeDuplicates, subject.includeDuplicates());
        assertValue(includeChildren, subject.includeChildren());
        assertValue(nodeAccountId, subject.nodeAccountId());
        assertNull(subject.retryPolicy());
        assertNull(subject.attemptTimeout());
    }

    @Test
    @DisplayName("creates a TransactionReceiptQuery with the default values")
    @Tag("testId:consensusnode.queries.receipts.TransactionReceiptQuery/create-with-defaults")
    void shouldCreateWithTheDefaultValues() {
        final TransactionReceiptQuery subject = new TransactionReceiptQuery(transactionIdValue(), nodeAccountIdValue());
        assertValue(false, subject.includeDuplicates());
        assertValue(false, subject.includeChildren());
    }

    @Test
    @DisplayName("the builder builds the values of the constructor")
    @Tag("testId:consensusnode.queries.receipts.TransactionReceiptQuery/builder/builds")
    void shouldBuildTheValuesOfTheConstructor() {
        final TransactionId transactionId = transactionIdValue();
        final boolean includeDuplicates = includeDuplicatesValue();
        final boolean includeChildren = includeChildrenValue();
        final @Nullable AccountId nodeAccountId = nodeAccountIdValue();
        final TransactionReceiptQuery subject = TransactionReceiptQuery.builder()
                .transactionId(transactionId)
                .includeDuplicates(includeDuplicates)
                .includeChildren(includeChildren)
                .nodeAccountId(nodeAccountId)
                .build();
        assertValue(transactionId, subject.transactionId());
        assertValue(includeDuplicates, subject.includeDuplicates());
        assertValue(includeChildren, subject.includeChildren());
        assertValue(nodeAccountId, subject.nodeAccountId());
    }

    @Test
    @DisplayName("a value that is not set is null or its default value")
    @Tag("testId:consensusnode.queries.receipts.TransactionReceiptQuery/builder/optional-values")
    void shouldBuildWithoutTheOptionalValues() {
        final TransactionReceiptQuery subject = TransactionReceiptQuery.builder()
                .transactionId(transactionIdValue())
                .build();
        assertValue(false, subject.includeDuplicates());
        assertValue(false, subject.includeChildren());
        assertNull(subject.nodeAccountId());
    }

    @Test
    @DisplayName("the builder rejects a missing required value")
    @Tag("testId:consensusnode.queries.receipts.TransactionReceiptQuery/builder/rejects-missing-value")
    void shouldRejectAMissingRequiredValue() {
        assertThrows(NullPointerException.class, () -> TransactionReceiptQuery.builder().build());
    }

    @Test
    @DisplayName("rejects null for transactionId")
    @Tag("testId:consensusnode.queries.receipts.TransactionReceiptQuery/transactionId/rejects-null")
    void shouldRejectNullTransactionId() {
        assertThrows(NullPointerException.class, () -> new TransactionReceiptQuery((TransactionId) null, includeDuplicatesValue(), includeChildrenValue(), nodeAccountIdValue()));
    }

    @Test
    @DisplayName("setRetryPolicy changes retryPolicy and returns the object")
    @Tag("testId:consensusnode.queries.receipts.TransactionReceiptQuery/retryPolicy/set")
    void shouldSetRetryPolicy() {
        final TransactionReceiptQuery subject = create();
        final @Nullable RetryPolicy value = retryPolicyOtherValue();
        assertSame(subject, subject.setRetryPolicy(value));
        assertValue(value, subject.retryPolicy());
    }

    @Test
    @DisplayName("setRetryPolicy accepts null")
    @Tag("testId:consensusnode.queries.receipts.TransactionReceiptQuery/retryPolicy/set-null")
    void shouldSetRetryPolicyToNull() {
        final TransactionReceiptQuery subject = create();
        subject.setRetryPolicy((RetryPolicy) null);
        assertNull(subject.retryPolicy());
    }

    @Test
    @DisplayName("setAttemptTimeout changes attemptTimeout and returns the object")
    @Tag("testId:consensusnode.queries.receipts.TransactionReceiptQuery/attemptTimeout/set")
    void shouldSetAttemptTimeout() {
        final TransactionReceiptQuery subject = create();
        final @Nullable Duration value = attemptTimeoutOtherValue();
        assertSame(subject, subject.setAttemptTimeout(value));
        assertValue(value, subject.attemptTimeout());
    }

    @Test
    @DisplayName("setAttemptTimeout accepts null")
    @Tag("testId:consensusnode.queries.receipts.TransactionReceiptQuery/attemptTimeout/set-null")
    void shouldSetAttemptTimeoutToNull() {
        final TransactionReceiptQuery subject = create();
        subject.setAttemptTimeout((Duration) null);
        assertNull(subject.attemptTimeout());
    }

    @Test
    @DisplayName("submit(client) can be called and returns a value")
    @Tag("testId:consensusnode.queries.receipts.TransactionReceiptQuery/submit/call")
    void shouldCallSubmit() {
        assertNotNull(create().submit(clientValue()));
    }

    private static TransactionId transactionIdValue() {
        return TransactionId.generateTransactionId(new Address(0L, 0L, "", 1001L));
    }

    private static boolean includeDuplicatesValue() {
        return true;
    }

    private static boolean includeChildrenValue() {
        return true;
    }

    private static AccountId nodeAccountIdValue() {
        return new AccountId(0L, 0L, "", 1001L, null, null);
    }

    private static TransactionReceiptQuery create() {
        return new TransactionReceiptQuery(transactionIdValue(), includeDuplicatesValue(), includeChildrenValue(), nodeAccountIdValue());
    }

    private static RetryPolicy retryPolicyOtherValue() {
        return ExecutionSettings.createDefault().retryPolicy();
    }

    private static Duration attemptTimeoutOtherValue() {
        return Duration.ofSeconds(2L);
    }

    private static HieroClient<?> clientValue() {
        return ClientFactory.createClient(new NetworkSetting(new Network<>(new byte[] {(byte) 3}, "local", HbarUnit.HBAR), Set.of(new ConsensusNode(new IpAddress(new byte[] {(byte) 127, (byte) 0, (byte) 0, (byte) 1}), 35211, new AccountId(0L, 0L, "", 3L, null, null))), Set.of(new MirrorNode("http://127.0.0.1:38081"))), new Account(new AccountId(0L, 0L, "", 1001L, null, null), KeysFactory.createPrivateKey(KeyAlgorithm.ED25519, ByteImportEncoding.HEX, "d3671a1e98bb22f011c0e4bcf5125590e15d8f21a7017309bb558852039bc75c")));
    }
}
