package org.hiero.consensusnode.client;

import static org.hiero.sdk.testing.Testing.assertValue;
import static org.junit.jupiter.api.Assertions.*;

import org.hiero.hedera.HbarUnit;
import org.hiero.keys.ByteImportEncoding;
import org.hiero.keys.KeyAlgorithm;
import org.hiero.keys.KeysFactory;
import org.hiero.ledger.AccountId;
import org.hiero.ledger.Network;
import org.hiero.nativeToken.NativeTokenUnit;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

/// Tests of `HieroClient`, generated from the specs.
@DisplayName("HieroClient")
class HieroClientTest {

    @Test
    @DisplayName("creates a HieroClient and returns the values")
    @Tag("testId:consensusnode.client.HieroClient/create")
    void shouldCreateAndReturnTheValues() {
        final Account operatorAccount = operatorAccountValue();
        final Network<NativeTokenUnit> network = networkValue();
        final ExecutionSettings executionSettings = executionSettingsValue();
        final HieroClient<NativeTokenUnit> subject = new HieroClient<>(operatorAccount, network, executionSettings);
        assertValue(operatorAccount, subject.operatorAccount());
        assertValue(network, subject.network());
        assertValue(executionSettings, subject.executionSettings());
    }

    @Test
    @DisplayName("rejects null for operatorAccount")
    @Tag("testId:consensusnode.client.HieroClient/operatorAccount/rejects-null")
    void shouldRejectNullOperatorAccount() {
        assertThrows(NullPointerException.class, () -> new HieroClient<>((Account) null, networkValue(), executionSettingsValue()));
    }

    @Test
    @DisplayName("rejects null for network")
    @Tag("testId:consensusnode.client.HieroClient/network/rejects-null")
    void shouldRejectNullNetwork() {
        assertThrows(NullPointerException.class, () -> new HieroClient<>(operatorAccountValue(), (Network<NativeTokenUnit>) null, executionSettingsValue()));
    }

    @Test
    @DisplayName("rejects null for executionSettings")
    @Tag("testId:consensusnode.client.HieroClient/executionSettings/rejects-null")
    void shouldRejectNullExecutionSettings() {
        assertThrows(NullPointerException.class, () -> new HieroClient<>(operatorAccountValue(), networkValue(), (ExecutionSettings) null));
    }

    private static Account operatorAccountValue() {
        return Account.of(new AccountId(0L, 0L, "", 1001L, null, null), KeysFactory.createPrivateKey(KeyAlgorithm.ED25519, ByteImportEncoding.HEX, "d3671a1e98bb22f011c0e4bcf5125590e15d8f21a7017309bb558852039bc75c"));
    }

    private static Network<NativeTokenUnit> networkValue() {
        return new Network<>(new byte[] {1, 2, 3}, null, HbarUnit.TINYBAR);
    }

    private static ExecutionSettings executionSettingsValue() {
        return ExecutionSettings.createDefault();
    }

    private static HieroClient<NativeTokenUnit> create() {
        return new HieroClient<>(operatorAccountValue(), networkValue(), executionSettingsValue());
    }

    private static HieroClient<NativeTokenUnit> defaultSubject() {
        return new HieroClient<>(Account.of(new AccountId(0L, 0L, "", 1001L, null, null), KeysFactory.createPrivateKey(KeyAlgorithm.ED25519, ByteImportEncoding.HEX, "d3671a1e98bb22f011c0e4bcf5125590e15d8f21a7017309bb558852039bc75c")), new Network<>(new byte[] {1, 2, 3}, null, HbarUnit.TINYBAR), ExecutionSettings.createDefault());
    }
}
