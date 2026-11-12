package org.hiero.consensusnode.clpr.queries;

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

/// Tests of `ClprEndpointManifestQuery`, generated from the specs.
@DisplayName("ClprEndpointManifestQuery")
class ClprEndpointManifestQueryTest {

    @Test
    @DisplayName("creates a ClprEndpointManifestQuery and returns the values")
    @Tag("testId:consensusnode.clpr.queries.ClprEndpointManifestQuery/create")
    @Tag("hip:1535")
    void shouldCreateAndReturnTheValues() {
        final ClprEndpointManifestQuery subject = new ClprEndpointManifestQuery();
        assertNull(subject.retryPolicy());
        assertNull(subject.attemptTimeout());
    }

    @Test
    @DisplayName("setRetryPolicy changes retryPolicy and returns the object")
    @Tag("testId:consensusnode.clpr.queries.ClprEndpointManifestQuery/retryPolicy/set")
    @Tag("hip:1535")
    void shouldSetRetryPolicy() {
        final ClprEndpointManifestQuery subject = create();
        final @Nullable RetryPolicy value = retryPolicyOtherValue();
        assertSame(subject, subject.setRetryPolicy(value));
        assertValue(value, subject.retryPolicy());
    }

    @Test
    @DisplayName("setRetryPolicy accepts null")
    @Tag("testId:consensusnode.clpr.queries.ClprEndpointManifestQuery/retryPolicy/set-null")
    @Tag("hip:1535")
    void shouldSetRetryPolicyToNull() {
        final ClprEndpointManifestQuery subject = create();
        subject.setRetryPolicy((RetryPolicy) null);
        assertNull(subject.retryPolicy());
    }

    @Test
    @DisplayName("setAttemptTimeout changes attemptTimeout and returns the object")
    @Tag("testId:consensusnode.clpr.queries.ClprEndpointManifestQuery/attemptTimeout/set")
    @Tag("hip:1535")
    void shouldSetAttemptTimeout() {
        final ClprEndpointManifestQuery subject = create();
        final @Nullable Duration value = attemptTimeoutOtherValue();
        assertSame(subject, subject.setAttemptTimeout(value));
        assertValue(value, subject.attemptTimeout());
    }

    @Test
    @DisplayName("setAttemptTimeout accepts null")
    @Tag("testId:consensusnode.clpr.queries.ClprEndpointManifestQuery/attemptTimeout/set-null")
    @Tag("hip:1535")
    void shouldSetAttemptTimeoutToNull() {
        final ClprEndpointManifestQuery subject = create();
        subject.setAttemptTimeout((Duration) null);
        assertNull(subject.attemptTimeout());
    }

    @Test
    @DisplayName("submit(client) can be called and returns a value")
    @Tag("testId:consensusnode.clpr.queries.ClprEndpointManifestQuery/submit/call")
    @Tag("hip:1535")
    void shouldCallSubmit() {
        assertNotNull(create().submit(clientValue()));
    }

    private static ClprEndpointManifestQuery create() {
        return new ClprEndpointManifestQuery();
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
