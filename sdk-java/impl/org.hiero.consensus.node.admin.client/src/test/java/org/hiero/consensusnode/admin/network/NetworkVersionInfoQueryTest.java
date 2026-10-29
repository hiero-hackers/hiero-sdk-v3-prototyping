package org.hiero.consensusnode.admin.network;

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
import org.hiero.ledger.ConsensusNode;
import org.hiero.ledger.IpAddress;
import org.hiero.ledger.MirrorNode;
import org.hiero.ledger.Network;
import org.hiero.ledger.config.NetworkSetting;
import org.jspecify.annotations.Nullable;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

/// Tests of `NetworkVersionInfoQuery`, generated from the specs.
@DisplayName("NetworkVersionInfoQuery")
class NetworkVersionInfoQueryTest {

    @Test
    @DisplayName("creates a NetworkVersionInfoQuery and returns the values")
    @Tag("testId:consensusnode.admin.network.NetworkVersionInfoQuery/create")
    void shouldCreateAndReturnTheValues() {
        final NetworkVersionInfoQuery subject = new NetworkVersionInfoQuery();
        assertNull(subject.retryPolicy());
        assertNull(subject.attemptTimeout());
    }

    @Test
    @DisplayName("setRetryPolicy changes retryPolicy and returns the object")
    @Tag("testId:consensusnode.admin.network.NetworkVersionInfoQuery/retryPolicy/set")
    void shouldSetRetryPolicy() {
        final NetworkVersionInfoQuery subject = create();
        final @Nullable RetryPolicy value = retryPolicyOtherValue();
        assertSame(subject, subject.setRetryPolicy(value));
        assertValue(value, subject.retryPolicy());
    }

    @Test
    @DisplayName("setRetryPolicy accepts null")
    @Tag("testId:consensusnode.admin.network.NetworkVersionInfoQuery/retryPolicy/set-null")
    void shouldSetRetryPolicyToNull() {
        final NetworkVersionInfoQuery subject = create();
        subject.setRetryPolicy((RetryPolicy) null);
        assertNull(subject.retryPolicy());
    }

    @Test
    @DisplayName("setAttemptTimeout changes attemptTimeout and returns the object")
    @Tag("testId:consensusnode.admin.network.NetworkVersionInfoQuery/attemptTimeout/set")
    void shouldSetAttemptTimeout() {
        final NetworkVersionInfoQuery subject = create();
        final @Nullable Duration value = attemptTimeoutOtherValue();
        assertSame(subject, subject.setAttemptTimeout(value));
        assertValue(value, subject.attemptTimeout());
    }

    @Test
    @DisplayName("setAttemptTimeout accepts null")
    @Tag("testId:consensusnode.admin.network.NetworkVersionInfoQuery/attemptTimeout/set-null")
    void shouldSetAttemptTimeoutToNull() {
        final NetworkVersionInfoQuery subject = create();
        subject.setAttemptTimeout((Duration) null);
        assertNull(subject.attemptTimeout());
    }

    @Test
    @DisplayName("submit(client) can be called and returns a value")
    @Tag("testId:consensusnode.admin.network.NetworkVersionInfoQuery/submit/call")
    void shouldCallSubmit() {
        assertNotNull(create().submit(clientValue()));
    }

    private static NetworkVersionInfoQuery create() {
        return new NetworkVersionInfoQuery();
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
