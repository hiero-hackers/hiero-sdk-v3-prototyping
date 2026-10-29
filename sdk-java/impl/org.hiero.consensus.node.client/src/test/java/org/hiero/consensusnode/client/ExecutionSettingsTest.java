package org.hiero.consensusnode.client;

import static org.hiero.sdk.testing.Testing.assertValue;
import static org.junit.jupiter.api.Assertions.*;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import org.hiero.authority.AuthorityFactory;
import org.hiero.consensusnode.transactions.spi.SpiFactory;
import org.hiero.http.HttpFactory;
import org.hiero.keys.KeysFactory;
import org.hiero.ledger.config.ConfigFactory;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

/// Tests of `ExecutionSettings`, generated from the specs.
@DisplayName("ExecutionSettings")
class ExecutionSettingsTest {

    @Test
    @DisplayName("creates an ExecutionSettings and returns the values")
    @Tag("testId:consensusnode.client.ExecutionSettings/create")
    void shouldCreateAndReturnTheValues() {
        final RetryPolicy retryPolicy = retryPolicyValue();
        final NodeSelector nodeSelector = nodeSelectorValue();
        final Duration attemptTimeout = attemptTimeoutValue();
        final List<AttemptListener> listeners = listenersValue();
        final ExecutionSettings subject = new ExecutionSettings(retryPolicy, nodeSelector, attemptTimeout, listeners);
        assertValue(retryPolicy, subject.retryPolicy());
        assertValue(nodeSelector, subject.nodeSelector());
        assertValue(attemptTimeout, subject.attemptTimeout());
        assertValue(listeners, subject.listeners());
    }

    @Test
    @DisplayName("creates an ExecutionSettings with the default values")
    @Tag("testId:consensusnode.client.ExecutionSettings/create-with-defaults")
    void shouldCreateWithTheDefaultValues() {
        final ExecutionSettings subject = new ExecutionSettings(retryPolicyValue(), nodeSelectorValue(), attemptTimeoutValue());
        assertValue(List.of(), subject.listeners());
    }

    @Test
    @DisplayName("rejects null for retryPolicy")
    @Tag("testId:consensusnode.client.ExecutionSettings/retryPolicy/rejects-null")
    void shouldRejectNullRetryPolicy() {
        assertThrows(NullPointerException.class, () -> new ExecutionSettings((RetryPolicy) null, nodeSelectorValue(), attemptTimeoutValue(), listenersValue()));
    }

    @Test
    @DisplayName("rejects null for nodeSelector")
    @Tag("testId:consensusnode.client.ExecutionSettings/nodeSelector/rejects-null")
    void shouldRejectNullNodeSelector() {
        assertThrows(NullPointerException.class, () -> new ExecutionSettings(retryPolicyValue(), (NodeSelector) null, attemptTimeoutValue(), listenersValue()));
    }

    @Test
    @DisplayName("rejects null for attemptTimeout")
    @Tag("testId:consensusnode.client.ExecutionSettings/attemptTimeout/rejects-null")
    void shouldRejectNullAttemptTimeout() {
        assertThrows(NullPointerException.class, () -> new ExecutionSettings(retryPolicyValue(), nodeSelectorValue(), (Duration) null, listenersValue()));
    }

    @Test
    @DisplayName("rejects null for listeners")
    @Tag("testId:consensusnode.client.ExecutionSettings/listeners/rejects-null")
    void shouldRejectNullListeners() {
        assertThrows(NullPointerException.class, () -> new ExecutionSettings(retryPolicyValue(), nodeSelectorValue(), attemptTimeoutValue(), (List<AttemptListener>) null));
    }

    @Test
    @DisplayName("the constructor copies listeners; the accessor returns an unmodifiable collection")
    @Tag("testId:consensusnode.client.ExecutionSettings/listeners/copies-in")
    void shouldCopyListenersIn() {
        final List<AttemptListener> value = new ArrayList<>(listenersValue());
        final List<AttemptListener> expected = List.copyOf(value);
        final ExecutionSettings subject = new ExecutionSettings(retryPolicyValue(), nodeSelectorValue(), attemptTimeoutValue(), value);
        value.clear();
        assertValue(expected, subject.listeners());
        assertThrows(UnsupportedOperationException.class, () -> Objects.requireNonNull(subject.listeners()).clear());
    }

    @Test
    @DisplayName("createDefault() can be called and returns a value")
    @Tag("testId:consensusnode.client.ExecutionSettings/createDefault/call")
    void shouldCallCreateDefault() {
        assertNotNull(ExecutionSettings.createDefault());
    }

    @Test
    @DisplayName("createDefault(maxAttempts, attemptTimeout) can be called and returns a value")
    @Tag("testId:consensusnode.client.ExecutionSettings/createDefault/call-2")
    void shouldCallCreateDefault2() {
        assertNotNull(ExecutionSettings.createDefault(maxAttemptsValue(), attemptTimeoutValue2()));
    }

    private static RetryPolicy retryPolicyValue() {
        return ExecutionSettings.createDefault().retryPolicy();
    }

    private static NodeSelector nodeSelectorValue() {
        return ExecutionSettings.createDefault().nodeSelector();
    }

    private static Duration attemptTimeoutValue() {
        return Duration.ofSeconds(1L);
    }

    private static List<AttemptListener> listenersValue() {
        return List.of(new AttemptListener() {
            public void attemptCompleted(final Submittable<?> request, final Attempt attempt) {
                throw new UnsupportedOperationException("test double");
            }
        });
    }

    private static ExecutionSettings create() {
        return new ExecutionSettings(retryPolicyValue(), nodeSelectorValue(), attemptTimeoutValue(), listenersValue());
    }

    private static ExecutionSettings defaultSubject() {
        return ExecutionSettings.createDefault();
    }

    private static int maxAttemptsValue() {
        return 1;
    }

    private static Duration attemptTimeoutValue2() {
        return Duration.ofMillis(1L);
    }
}
