package org.hiero.consensusnode.client;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Set;
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
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

/// Tests of `ClientFactory`, generated from the specs.
@DisplayName("ClientFactory")
class ClientFactoryTest {

    @Test
    @DisplayName("createClient(networkSettings, operatorAccount) can be called and returns a value")
    @Tag("testId:consensusnode.client/createClient/call")
    void shouldCallCreateClient() {
        assertNotNull(ClientFactory.createClient(networkSettingsValue(), operatorAccountValue()));
    }

    @Test
    @DisplayName("createClient rejects null for networkSettings")
    @Tag("testId:consensusnode.client/createClient/networkSettings/rejects-null")
    void shouldRejectNullNetworkSettingsInCreateClient() {
        assertThrows(NullPointerException.class, () -> ClientFactory.createClient((NetworkSetting) null, operatorAccountValue()));
    }

    @Test
    @DisplayName("createClient rejects null for operatorAccount")
    @Tag("testId:consensusnode.client/createClient/operatorAccount/rejects-null")
    void shouldRejectNullOperatorAccountInCreateClient() {
        assertThrows(NullPointerException.class, () -> ClientFactory.createClient(networkSettingsValue(), (Account) null));
    }

    @Test
    @DisplayName("createClient(networkSettings, operatorAccount, transactionSigner) can be called and returns a value")
    @Tag("testId:consensusnode.client/createClient/call-2")
    void shouldCallCreateClient2() {
        assertNotNull(ClientFactory.createClient(networkSettingsValue(), operatorAccountValue(), transactionSignerValue()));
    }

    @Test
    @DisplayName("createClient rejects null for networkSettings")
    @Tag("testId:consensusnode.client/createClient/networkSettings/rejects-null-2")
    void shouldRejectNullNetworkSettingsInCreateClient2() {
        assertThrows(NullPointerException.class, () -> ClientFactory.createClient((NetworkSetting) null, operatorAccountValue(), transactionSignerValue()));
    }

    @Test
    @DisplayName("createClient rejects null for operatorAccount")
    @Tag("testId:consensusnode.client/createClient/operatorAccount/rejects-null-2")
    void shouldRejectNullOperatorAccountInCreateClient2() {
        assertThrows(NullPointerException.class, () -> ClientFactory.createClient(networkSettingsValue(), (Account) null, transactionSignerValue()));
    }

    @Test
    @DisplayName("createClient rejects null for transactionSigner")
    @Tag("testId:consensusnode.client/createClient/transactionSigner/rejects-null")
    void shouldRejectNullTransactionSignerInCreateClient() {
        assertThrows(NullPointerException.class, () -> ClientFactory.createClient(networkSettingsValue(), operatorAccountValue(), (TransactionSigner) null));
    }

    @Test
    @DisplayName("createClient(networkSettings, operatorAccount, transactionSigner, executionSettings) can be called and returns a value")
    @Tag("testId:consensusnode.client/createClient/call-3")
    void shouldCallCreateClient3() {
        assertNotNull(ClientFactory.createClient(networkSettingsValue(), operatorAccountValue(), transactionSignerValue(), executionSettingsValue()));
    }

    @Test
    @DisplayName("createClient rejects null for networkSettings")
    @Tag("testId:consensusnode.client/createClient/networkSettings/rejects-null-3")
    void shouldRejectNullNetworkSettingsInCreateClient3() {
        assertThrows(NullPointerException.class, () -> ClientFactory.createClient((NetworkSetting) null, operatorAccountValue(), transactionSignerValue(), executionSettingsValue()));
    }

    @Test
    @DisplayName("createClient rejects null for operatorAccount")
    @Tag("testId:consensusnode.client/createClient/operatorAccount/rejects-null-3")
    void shouldRejectNullOperatorAccountInCreateClient3() {
        assertThrows(NullPointerException.class, () -> ClientFactory.createClient(networkSettingsValue(), (Account) null, transactionSignerValue(), executionSettingsValue()));
    }

    @Test
    @DisplayName("createClient rejects null for transactionSigner")
    @Tag("testId:consensusnode.client/createClient/transactionSigner/rejects-null-2")
    void shouldRejectNullTransactionSignerInCreateClient2() {
        assertThrows(NullPointerException.class, () -> ClientFactory.createClient(networkSettingsValue(), operatorAccountValue(), (TransactionSigner) null, executionSettingsValue()));
    }

    @Test
    @DisplayName("createClient rejects null for executionSettings")
    @Tag("testId:consensusnode.client/createClient/executionSettings/rejects-null")
    void shouldRejectNullExecutionSettingsInCreateClient() {
        assertThrows(NullPointerException.class, () -> ClientFactory.createClient(networkSettingsValue(), operatorAccountValue(), transactionSignerValue(), (ExecutionSettings) null));
    }

    private static NetworkSetting networkSettingsValue() {
        return new NetworkSetting(new Network<>(new byte[] {(byte) 3}, "local", HbarUnit.HBAR), Set.of(new ConsensusNode(new IpAddress(new byte[] {(byte) 127, (byte) 0, (byte) 0, (byte) 1}), 35211, new AccountId(0L, 0L, "", 3L, null, null))), Set.of(new MirrorNode("http://127.0.0.1:38081")));
    }

    private static Account operatorAccountValue() {
        return new Account(new AccountId(0L, 0L, "", 1001L, null, null), KeysFactory.createPrivateKey(KeyAlgorithm.ED25519, ByteImportEncoding.HEX, "d3671a1e98bb22f011c0e4bcf5125590e15d8f21a7017309bb558852039bc75c"));
    }

    private static TransactionSigner transactionSignerValue() {
        return ClientFactory.createClient(new NetworkSetting(new Network<>(new byte[] {(byte) 3}, "local", HbarUnit.HBAR), Set.of(new ConsensusNode(new IpAddress(new byte[] {(byte) 127, (byte) 0, (byte) 0, (byte) 1}), 35211, new AccountId(0L, 0L, "", 3L, null, null))), Set.of(new MirrorNode("http://127.0.0.1:38081"))), new Account(new AccountId(0L, 0L, "", 1001L, null, null), KeysFactory.createPrivateKey(KeyAlgorithm.ED25519, ByteImportEncoding.HEX, "d3671a1e98bb22f011c0e4bcf5125590e15d8f21a7017309bb558852039bc75c"))).transactionSigner();
    }

    private static ExecutionSettings executionSettingsValue() {
        return ExecutionSettings.createDefault();
    }
}
