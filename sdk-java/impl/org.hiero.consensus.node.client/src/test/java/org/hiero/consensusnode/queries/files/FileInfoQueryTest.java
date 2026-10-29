package org.hiero.consensusnode.queries.files;

import static org.hiero.sdk.testing.Testing.assertValue;
import static org.junit.jupiter.api.Assertions.*;

import java.time.Duration;
import java.util.Set;
import org.hiero.consensusnode.client.Account;
import org.hiero.consensusnode.client.ClientFactory;
import org.hiero.consensusnode.client.ExecutionSettings;
import org.hiero.consensusnode.client.HieroClient;
import org.hiero.consensusnode.client.RetryPolicy;
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
        assertNull(subject.retryPolicy());
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
    @DisplayName("setRetryPolicy changes retryPolicy and returns the object")
    @Tag("testId:consensusnode.queries.files.FileInfoQuery/retryPolicy/set")
    void shouldSetRetryPolicy() {
        final FileInfoQuery subject = create();
        final @Nullable RetryPolicy value = retryPolicyOtherValue();
        assertSame(subject, subject.setRetryPolicy(value));
        assertValue(value, subject.retryPolicy());
    }

    @Test
    @DisplayName("setRetryPolicy accepts null")
    @Tag("testId:consensusnode.queries.files.FileInfoQuery/retryPolicy/set-null")
    void shouldSetRetryPolicyToNull() {
        final FileInfoQuery subject = create();
        subject.setRetryPolicy((RetryPolicy) null);
        assertNull(subject.retryPolicy());
    }

    @Test
    @DisplayName("setAttemptTimeout changes attemptTimeout and returns the object")
    @Tag("testId:consensusnode.queries.files.FileInfoQuery/attemptTimeout/set")
    void shouldSetAttemptTimeout() {
        final FileInfoQuery subject = create();
        final @Nullable Duration value = attemptTimeoutOtherValue();
        assertSame(subject, subject.setAttemptTimeout(value));
        assertValue(value, subject.attemptTimeout());
    }

    @Test
    @DisplayName("setAttemptTimeout accepts null")
    @Tag("testId:consensusnode.queries.files.FileInfoQuery/attemptTimeout/set-null")
    void shouldSetAttemptTimeoutToNull() {
        final FileInfoQuery subject = create();
        subject.setAttemptTimeout((Duration) null);
        assertNull(subject.attemptTimeout());
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

    private static RetryPolicy retryPolicyOtherValue() {
        return ExecutionSettings.createDefault().retryPolicy();
    }

    private static Duration attemptTimeoutOtherValue() {
        return Duration.ofSeconds(2L);
    }

    private static NativeToken<?, ?> maxQueryPaymentOtherValue() {
        return new Hbar(2L, HbarUnit.TINYBAR);
    }

    private static HieroClient<?> clientValue() {
        return ClientFactory.createClient(new NetworkSetting(new Network<>(new byte[] {(byte) 3}, "local", HbarUnit.HBAR), Set.of(new ConsensusNode(new IpAddress(new byte[] {(byte) 127, (byte) 0, (byte) 0, (byte) 1}), 35211, new AccountId(0L, 0L, "", 3L, null, null))), Set.of(new MirrorNode("http://127.0.0.1:38081"))), new Account(new AccountId(0L, 0L, "", 1001L, null, null), KeysFactory.createPrivateKey(KeyAlgorithm.ED25519, ByteImportEncoding.HEX, "d3671a1e98bb22f011c0e4bcf5125590e15d8f21a7017309bb558852039bc75c")));
    }
}
