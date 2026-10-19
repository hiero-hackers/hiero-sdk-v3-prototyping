package org.hiero.consensusnode.queries.receipts;

import static org.hiero.sdk.testing.Testing.assertValue;
import static org.junit.jupiter.api.Assertions.*;

import java.util.Set;
import org.hiero.consensusnode.client.Account;
import org.hiero.consensusnode.client.ClientFactory;
import org.hiero.consensusnode.client.HieroClient;
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
        assertNull(subject.maxAttempts());
        assertNull(subject.maxBackoff());
        assertNull(subject.minBackoff());
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
    @DisplayName("setMaxAttempts changes maxAttempts and returns the object")
    @Tag("testId:consensusnode.queries.receipts.TransactionReceiptQuery/maxAttempts/set")
    void shouldSetMaxAttempts() {
        final TransactionReceiptQuery subject = create();
        final @Nullable Integer value = maxAttemptsOtherValue();
        assertSame(subject, subject.setMaxAttempts(value));
        assertValue(value, subject.maxAttempts());
    }

    @Test
    @DisplayName("setMaxAttempts accepts null")
    @Tag("testId:consensusnode.queries.receipts.TransactionReceiptQuery/maxAttempts/set-null")
    void shouldSetMaxAttemptsToNull() {
        final TransactionReceiptQuery subject = create();
        subject.setMaxAttempts((Integer) null);
        assertNull(subject.maxAttempts());
    }

    @Test
    @DisplayName("setMaxAttempts accepts the minimum (-2147483648)")
    @Tag("testId:consensusnode.queries.receipts.TransactionReceiptQuery/maxAttempts/set-accepts-at-minimum")
    void shouldSetMaxAttemptsAtMinimum() {
        final TransactionReceiptQuery subject = create();
        final @Nullable Integer value = Integer.MIN_VALUE;
        subject.setMaxAttempts(value);
        assertValue(value, subject.maxAttempts());
    }

    @Test
    @DisplayName("setMaxAttempts accepts the maximum (2147483647)")
    @Tag("testId:consensusnode.queries.receipts.TransactionReceiptQuery/maxAttempts/set-accepts-at-maximum")
    void shouldSetMaxAttemptsAtMaximum() {
        final TransactionReceiptQuery subject = create();
        final @Nullable Integer value = 2147483647;
        subject.setMaxAttempts(value);
        assertValue(value, subject.maxAttempts());
    }

    @Test
    @DisplayName("setMaxBackoff changes maxBackoff and returns the object")
    @Tag("testId:consensusnode.queries.receipts.TransactionReceiptQuery/maxBackoff/set")
    void shouldSetMaxBackoff() {
        final TransactionReceiptQuery subject = create();
        final @Nullable Long value = maxBackoffOtherValue();
        assertSame(subject, subject.setMaxBackoff(value));
        assertValue(value, subject.maxBackoff());
    }

    @Test
    @DisplayName("setMaxBackoff accepts null")
    @Tag("testId:consensusnode.queries.receipts.TransactionReceiptQuery/maxBackoff/set-null")
    void shouldSetMaxBackoffToNull() {
        final TransactionReceiptQuery subject = create();
        subject.setMaxBackoff((Long) null);
        assertNull(subject.maxBackoff());
    }

    @Test
    @DisplayName("setMaxBackoff accepts the minimum (-9223372036854775808)")
    @Tag("testId:consensusnode.queries.receipts.TransactionReceiptQuery/maxBackoff/set-accepts-at-minimum")
    void shouldSetMaxBackoffAtMinimum() {
        final TransactionReceiptQuery subject = create();
        final @Nullable Long value = -9223372036854775808L;
        subject.setMaxBackoff(value);
        assertValue(value, subject.maxBackoff());
    }

    @Test
    @DisplayName("setMaxBackoff accepts the maximum (9223372036854775807)")
    @Tag("testId:consensusnode.queries.receipts.TransactionReceiptQuery/maxBackoff/set-accepts-at-maximum")
    void shouldSetMaxBackoffAtMaximum() {
        final TransactionReceiptQuery subject = create();
        final @Nullable Long value = 9223372036854775807L;
        subject.setMaxBackoff(value);
        assertValue(value, subject.maxBackoff());
    }

    @Test
    @DisplayName("setMinBackoff changes minBackoff and returns the object")
    @Tag("testId:consensusnode.queries.receipts.TransactionReceiptQuery/minBackoff/set")
    void shouldSetMinBackoff() {
        final TransactionReceiptQuery subject = create();
        final @Nullable Long value = minBackoffOtherValue();
        assertSame(subject, subject.setMinBackoff(value));
        assertValue(value, subject.minBackoff());
    }

    @Test
    @DisplayName("setMinBackoff accepts null")
    @Tag("testId:consensusnode.queries.receipts.TransactionReceiptQuery/minBackoff/set-null")
    void shouldSetMinBackoffToNull() {
        final TransactionReceiptQuery subject = create();
        subject.setMinBackoff((Long) null);
        assertNull(subject.minBackoff());
    }

    @Test
    @DisplayName("setMinBackoff accepts the minimum (-9223372036854775808)")
    @Tag("testId:consensusnode.queries.receipts.TransactionReceiptQuery/minBackoff/set-accepts-at-minimum")
    void shouldSetMinBackoffAtMinimum() {
        final TransactionReceiptQuery subject = create();
        final @Nullable Long value = -9223372036854775808L;
        subject.setMinBackoff(value);
        assertValue(value, subject.minBackoff());
    }

    @Test
    @DisplayName("setMinBackoff accepts the maximum (9223372036854775807)")
    @Tag("testId:consensusnode.queries.receipts.TransactionReceiptQuery/minBackoff/set-accepts-at-maximum")
    void shouldSetMinBackoffAtMaximum() {
        final TransactionReceiptQuery subject = create();
        final @Nullable Long value = 9223372036854775807L;
        subject.setMinBackoff(value);
        assertValue(value, subject.minBackoff());
    }

    @Test
    @DisplayName("setAttemptTimeout changes attemptTimeout and returns the object")
    @Tag("testId:consensusnode.queries.receipts.TransactionReceiptQuery/attemptTimeout/set")
    void shouldSetAttemptTimeout() {
        final TransactionReceiptQuery subject = create();
        final @Nullable Long value = attemptTimeoutOtherValue();
        assertSame(subject, subject.setAttemptTimeout(value));
        assertValue(value, subject.attemptTimeout());
    }

    @Test
    @DisplayName("setAttemptTimeout accepts null")
    @Tag("testId:consensusnode.queries.receipts.TransactionReceiptQuery/attemptTimeout/set-null")
    void shouldSetAttemptTimeoutToNull() {
        final TransactionReceiptQuery subject = create();
        subject.setAttemptTimeout((Long) null);
        assertNull(subject.attemptTimeout());
    }

    @Test
    @DisplayName("setAttemptTimeout accepts the minimum (-9223372036854775808)")
    @Tag("testId:consensusnode.queries.receipts.TransactionReceiptQuery/attemptTimeout/set-accepts-at-minimum")
    void shouldSetAttemptTimeoutAtMinimum() {
        final TransactionReceiptQuery subject = create();
        final @Nullable Long value = -9223372036854775808L;
        subject.setAttemptTimeout(value);
        assertValue(value, subject.attemptTimeout());
    }

    @Test
    @DisplayName("setAttemptTimeout accepts the maximum (9223372036854775807)")
    @Tag("testId:consensusnode.queries.receipts.TransactionReceiptQuery/attemptTimeout/set-accepts-at-maximum")
    void shouldSetAttemptTimeoutAtMaximum() {
        final TransactionReceiptQuery subject = create();
        final @Nullable Long value = 9223372036854775807L;
        subject.setAttemptTimeout(value);
        assertValue(value, subject.attemptTimeout());
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

    private static int maxAttemptsOtherValue() {
        return 2;
    }

    private static long maxBackoffOtherValue() {
        return 2L;
    }

    private static long minBackoffOtherValue() {
        return 2L;
    }

    private static long attemptTimeoutOtherValue() {
        return 2L;
    }

    private static HieroClient<?> clientValue() {
        return ClientFactory.createClient(new NetworkSetting(new Network<>(new byte[] {(byte) 3}, "local", HbarUnit.HBAR), Set.of(new ConsensusNode(new IpAddress(new byte[] {(byte) 127, (byte) 0, (byte) 0, (byte) 1}), 35211, new AccountId(0L, 0L, "", 3L, null, null))), Set.of(new MirrorNode("http://127.0.0.1:38081"))), new Account(new AccountId(0L, 0L, "", 1001L, null, null), KeysFactory.createPrivateKey(KeyAlgorithm.ED25519, ByteImportEncoding.HEX, "d3671a1e98bb22f011c0e4bcf5125590e15d8f21a7017309bb558852039bc75c")));
    }
}
