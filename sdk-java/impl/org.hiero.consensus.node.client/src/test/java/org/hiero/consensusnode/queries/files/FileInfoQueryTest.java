package org.hiero.consensusnode.queries.files;

import static org.hiero.sdk.testing.Testing.assertValue;
import static org.junit.jupiter.api.Assertions.*;

import java.util.Set;
import org.hiero.consensusnode.client.Account;
import org.hiero.consensusnode.client.ClientFactory;
import org.hiero.consensusnode.client.HieroClient;
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

/// Tests of `FileInfoQuery`, generated from the specs.
@DisplayName("FileInfoQuery")
class FileInfoQueryTest {

    @Test
    @DisplayName("creates a FileInfoQuery and returns the values")
    @Tag("testId:consensusnode.queries.files.FileInfoQuery/create")
    void shouldCreateAndReturnTheValues() {
        final Address fileId = fileIdValue();
        final FileInfoQuery subject = new FileInfoQuery(fileId);
        assertValue(fileId, subject.fileId());
        assertNull(subject.maxAttempts());
        assertNull(subject.maxBackoff());
        assertNull(subject.minBackoff());
        assertNull(subject.attemptTimeout());
        assertNull(subject.maxQueryPayment());
    }

    @Test
    @DisplayName("rejects null for fileId")
    @Tag("testId:consensusnode.queries.files.FileInfoQuery/fileId/rejects-null")
    void shouldRejectNullFileId() {
        assertThrows(NullPointerException.class, () -> new FileInfoQuery((Address) null));
    }

    @Test
    @DisplayName("setMaxAttempts changes maxAttempts and returns the object")
    @Tag("testId:consensusnode.queries.files.FileInfoQuery/maxAttempts/set")
    void shouldSetMaxAttempts() {
        final FileInfoQuery subject = create();
        final @Nullable Integer value = maxAttemptsOtherValue();
        assertSame(subject, subject.setMaxAttempts(value));
        assertValue(value, subject.maxAttempts());
    }

    @Test
    @DisplayName("setMaxAttempts accepts null")
    @Tag("testId:consensusnode.queries.files.FileInfoQuery/maxAttempts/set-null")
    void shouldSetMaxAttemptsToNull() {
        final FileInfoQuery subject = create();
        subject.setMaxAttempts((Integer) null);
        assertNull(subject.maxAttempts());
    }

    @Test
    @DisplayName("setMaxAttempts accepts the minimum (-2147483648)")
    @Tag("testId:consensusnode.queries.files.FileInfoQuery/maxAttempts/set-accepts-at-minimum")
    void shouldSetMaxAttemptsAtMinimum() {
        final FileInfoQuery subject = create();
        final @Nullable Integer value = Integer.MIN_VALUE;
        subject.setMaxAttempts(value);
        assertValue(value, subject.maxAttempts());
    }

    @Test
    @DisplayName("setMaxAttempts accepts the maximum (2147483647)")
    @Tag("testId:consensusnode.queries.files.FileInfoQuery/maxAttempts/set-accepts-at-maximum")
    void shouldSetMaxAttemptsAtMaximum() {
        final FileInfoQuery subject = create();
        final @Nullable Integer value = 2147483647;
        subject.setMaxAttempts(value);
        assertValue(value, subject.maxAttempts());
    }

    @Test
    @DisplayName("setMaxBackoff changes maxBackoff and returns the object")
    @Tag("testId:consensusnode.queries.files.FileInfoQuery/maxBackoff/set")
    void shouldSetMaxBackoff() {
        final FileInfoQuery subject = create();
        final @Nullable Long value = maxBackoffOtherValue();
        assertSame(subject, subject.setMaxBackoff(value));
        assertValue(value, subject.maxBackoff());
    }

    @Test
    @DisplayName("setMaxBackoff accepts null")
    @Tag("testId:consensusnode.queries.files.FileInfoQuery/maxBackoff/set-null")
    void shouldSetMaxBackoffToNull() {
        final FileInfoQuery subject = create();
        subject.setMaxBackoff((Long) null);
        assertNull(subject.maxBackoff());
    }

    @Test
    @DisplayName("setMaxBackoff accepts the minimum (-9223372036854775808)")
    @Tag("testId:consensusnode.queries.files.FileInfoQuery/maxBackoff/set-accepts-at-minimum")
    void shouldSetMaxBackoffAtMinimum() {
        final FileInfoQuery subject = create();
        final @Nullable Long value = -9223372036854775808L;
        subject.setMaxBackoff(value);
        assertValue(value, subject.maxBackoff());
    }

    @Test
    @DisplayName("setMaxBackoff accepts the maximum (9223372036854775807)")
    @Tag("testId:consensusnode.queries.files.FileInfoQuery/maxBackoff/set-accepts-at-maximum")
    void shouldSetMaxBackoffAtMaximum() {
        final FileInfoQuery subject = create();
        final @Nullable Long value = 9223372036854775807L;
        subject.setMaxBackoff(value);
        assertValue(value, subject.maxBackoff());
    }

    @Test
    @DisplayName("setMinBackoff changes minBackoff and returns the object")
    @Tag("testId:consensusnode.queries.files.FileInfoQuery/minBackoff/set")
    void shouldSetMinBackoff() {
        final FileInfoQuery subject = create();
        final @Nullable Long value = minBackoffOtherValue();
        assertSame(subject, subject.setMinBackoff(value));
        assertValue(value, subject.minBackoff());
    }

    @Test
    @DisplayName("setMinBackoff accepts null")
    @Tag("testId:consensusnode.queries.files.FileInfoQuery/minBackoff/set-null")
    void shouldSetMinBackoffToNull() {
        final FileInfoQuery subject = create();
        subject.setMinBackoff((Long) null);
        assertNull(subject.minBackoff());
    }

    @Test
    @DisplayName("setMinBackoff accepts the minimum (-9223372036854775808)")
    @Tag("testId:consensusnode.queries.files.FileInfoQuery/minBackoff/set-accepts-at-minimum")
    void shouldSetMinBackoffAtMinimum() {
        final FileInfoQuery subject = create();
        final @Nullable Long value = -9223372036854775808L;
        subject.setMinBackoff(value);
        assertValue(value, subject.minBackoff());
    }

    @Test
    @DisplayName("setMinBackoff accepts the maximum (9223372036854775807)")
    @Tag("testId:consensusnode.queries.files.FileInfoQuery/minBackoff/set-accepts-at-maximum")
    void shouldSetMinBackoffAtMaximum() {
        final FileInfoQuery subject = create();
        final @Nullable Long value = 9223372036854775807L;
        subject.setMinBackoff(value);
        assertValue(value, subject.minBackoff());
    }

    @Test
    @DisplayName("setAttemptTimeout changes attemptTimeout and returns the object")
    @Tag("testId:consensusnode.queries.files.FileInfoQuery/attemptTimeout/set")
    void shouldSetAttemptTimeout() {
        final FileInfoQuery subject = create();
        final @Nullable Long value = attemptTimeoutOtherValue();
        assertSame(subject, subject.setAttemptTimeout(value));
        assertValue(value, subject.attemptTimeout());
    }

    @Test
    @DisplayName("setAttemptTimeout accepts null")
    @Tag("testId:consensusnode.queries.files.FileInfoQuery/attemptTimeout/set-null")
    void shouldSetAttemptTimeoutToNull() {
        final FileInfoQuery subject = create();
        subject.setAttemptTimeout((Long) null);
        assertNull(subject.attemptTimeout());
    }

    @Test
    @DisplayName("setAttemptTimeout accepts the minimum (-9223372036854775808)")
    @Tag("testId:consensusnode.queries.files.FileInfoQuery/attemptTimeout/set-accepts-at-minimum")
    void shouldSetAttemptTimeoutAtMinimum() {
        final FileInfoQuery subject = create();
        final @Nullable Long value = -9223372036854775808L;
        subject.setAttemptTimeout(value);
        assertValue(value, subject.attemptTimeout());
    }

    @Test
    @DisplayName("setAttemptTimeout accepts the maximum (9223372036854775807)")
    @Tag("testId:consensusnode.queries.files.FileInfoQuery/attemptTimeout/set-accepts-at-maximum")
    void shouldSetAttemptTimeoutAtMaximum() {
        final FileInfoQuery subject = create();
        final @Nullable Long value = 9223372036854775807L;
        subject.setAttemptTimeout(value);
        assertValue(value, subject.attemptTimeout());
    }

    @Test
    @DisplayName("setMaxQueryPayment changes maxQueryPayment and returns the object")
    @Tag("testId:consensusnode.queries.files.FileInfoQuery/maxQueryPayment/set")
    void shouldSetMaxQueryPayment() {
        final FileInfoQuery subject = create();
        final @Nullable NativeToken<?, ?> value = maxQueryPaymentOtherValue();
        assertSame(subject, subject.setMaxQueryPayment(value));
        assertValue(value, subject.maxQueryPayment());
    }

    @Test
    @DisplayName("setMaxQueryPayment accepts null")
    @Tag("testId:consensusnode.queries.files.FileInfoQuery/maxQueryPayment/set-null")
    void shouldSetMaxQueryPaymentToNull() {
        final FileInfoQuery subject = create();
        subject.setMaxQueryPayment((NativeToken<?, ?>) null);
        assertNull(subject.maxQueryPayment());
    }

    @Test
    @DisplayName("submit(client) can be called and returns a value")
    @Tag("testId:consensusnode.queries.files.FileInfoQuery/submit/call")
    void shouldCallSubmit() {
        assertNotNull(create().submit(clientValue()));
    }

    @Test
    @DisplayName("getCost(client) can be called and returns a value")
    @Tag("testId:consensusnode.queries.files.FileInfoQuery/getCost/call")
    void shouldCallGetCost() {
        assertNotNull(create().getCost(clientValue()));
    }

    private static Address fileIdValue() {
        return new Address(0L, 0L, "", 1001L);
    }

    private static FileInfoQuery create() {
        return new FileInfoQuery(fileIdValue());
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

    private static NativeToken<?, ?> maxQueryPaymentOtherValue() {
        return new Hbar(2L, HbarUnit.TINYBAR);
    }

    private static HieroClient<?> clientValue() {
        return ClientFactory.createClient(new NetworkSetting(new Network<>(new byte[] {(byte) 3}, "local", HbarUnit.HBAR), Set.of(new ConsensusNode(new IpAddress(new byte[] {(byte) 127, (byte) 0, (byte) 0, (byte) 1}), 35211, new AccountId(0L, 0L, "", 3L, null, null))), Set.of(new MirrorNode("http://127.0.0.1:38081"))), new Account(new AccountId(0L, 0L, "", 1001L, null, null), KeysFactory.createPrivateKey(KeyAlgorithm.ED25519, ByteImportEncoding.HEX, "d3671a1e98bb22f011c0e4bcf5125590e15d8f21a7017309bb558852039bc75c")));
    }
}
