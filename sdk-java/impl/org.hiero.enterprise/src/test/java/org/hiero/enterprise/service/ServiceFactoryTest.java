package org.hiero.enterprise.service;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Set;
import org.hiero.consensusnode.client.Account;
import org.hiero.consensusnode.client.ClientFactory;
import org.hiero.consensusnode.client.TransactionSigner;
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
import org.junit.jupiter.api.Test;

/// Tests of `ServiceFactory`, generated from the specs.
@DisplayName("ServiceFactory")
class ServiceFactoryTest {

    @Test
    @DisplayName("createSession(networkSettings, operatorAccount) can be called and returns a value")
    void shouldCallCreateSession() {
        assertNotNull(ServiceFactory.createSession(networkSettingsValue(), operatorAccountValue()));
    }

    @Test
    @DisplayName("createSession rejects null for networkSettings")
    void shouldRejectNullNetworkSettingsInCreateSession() {
        assertThrows(NullPointerException.class, () -> ServiceFactory.createSession((NetworkSetting) null, operatorAccountValue()));
    }

    @Test
    @DisplayName("createSession rejects null for operatorAccount")
    void shouldRejectNullOperatorAccountInCreateSession() {
        assertThrows(NullPointerException.class, () -> ServiceFactory.createSession(networkSettingsValue(), (Account) null));
    }

    @Test
    @DisplayName("createSession(networkSettings, operatorAccount, transactionSigner) can be called and returns a value")
    void shouldCallCreateSession2() {
        assertNotNull(ServiceFactory.createSession(networkSettingsValue(), operatorAccountValue(), transactionSignerValue()));
    }

    @Test
    @DisplayName("createSession rejects null for networkSettings")
    void shouldRejectNullNetworkSettingsInCreateSession2() {
        assertThrows(NullPointerException.class, () -> ServiceFactory.createSession((NetworkSetting) null, operatorAccountValue(), transactionSignerValue()));
    }

    @Test
    @DisplayName("createSession rejects null for operatorAccount")
    void shouldRejectNullOperatorAccountInCreateSession2() {
        assertThrows(NullPointerException.class, () -> ServiceFactory.createSession(networkSettingsValue(), (Account) null, transactionSignerValue()));
    }

    @Test
    @DisplayName("createSession rejects null for transactionSigner")
    void shouldRejectNullTransactionSignerInCreateSession() {
        assertThrows(NullPointerException.class, () -> ServiceFactory.createSession(networkSettingsValue(), operatorAccountValue(), (TransactionSigner) null));
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
}
