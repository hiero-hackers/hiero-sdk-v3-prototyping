package org.hiero.consensusnode.client;

import static org.hiero.sdk.testing.Testing.assertValue;
import static org.junit.jupiter.api.Assertions.*;

import java.util.Set;
import org.hiero.authority.AuthorityFactory;
import org.hiero.consensusnode.transactions.spi.SpiFactory;
import org.hiero.hedera.HbarUnit;
import org.hiero.http.HttpFactory;
import org.hiero.keys.ByteImportEncoding;
import org.hiero.keys.KeyAlgorithm;
import org.hiero.keys.KeysFactory;
import org.hiero.ledger.AccountId;
import org.hiero.ledger.ConsensusNode;
import org.hiero.ledger.IpAddress;
import org.hiero.ledger.MirrorNode;
import org.hiero.ledger.Network;
import org.hiero.ledger.config.ConfigFactory;
import org.hiero.ledger.config.NetworkSetting;
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
        final TransactionSigner transactionSigner = transactionSignerValue();
        final HieroClient<NativeTokenUnit> subject = new HieroClient<>(operatorAccount, network, transactionSigner);
        assertValue(operatorAccount, subject.operatorAccount());
        assertValue(network, subject.network());
        assertValue(transactionSigner, subject.transactionSigner());
    }

    @Test
    @DisplayName("rejects null for operatorAccount")
    @Tag("testId:consensusnode.client.HieroClient/operatorAccount/rejects-null")
    void shouldRejectNullOperatorAccount() {
        assertThrows(NullPointerException.class, () -> new HieroClient<>((Account) null, networkValue(), transactionSignerValue()));
    }

    @Test
    @DisplayName("rejects null for network")
    @Tag("testId:consensusnode.client.HieroClient/network/rejects-null")
    void shouldRejectNullNetwork() {
        assertThrows(NullPointerException.class, () -> new HieroClient<>(operatorAccountValue(), (Network<NativeTokenUnit>) null, transactionSignerValue()));
    }

    @Test
    @DisplayName("rejects null for transactionSigner")
    @Tag("testId:consensusnode.client.HieroClient/transactionSigner/rejects-null")
    void shouldRejectNullTransactionSigner() {
        assertThrows(NullPointerException.class, () -> new HieroClient<>(operatorAccountValue(), networkValue(), (TransactionSigner) null));
    }

    private static Account operatorAccountValue() {
        return new Account(new AccountId(0L, 0L, "", 1001L, null, null), KeysFactory.createPrivateKey(KeyAlgorithm.ED25519, ByteImportEncoding.HEX, "d3671a1e98bb22f011c0e4bcf5125590e15d8f21a7017309bb558852039bc75c"));
    }

    private static Network<NativeTokenUnit> networkValue() {
        return new Network<>(new byte[] {1, 2, 3}, null, HbarUnit.TINYBAR);
    }

    private static TransactionSigner transactionSignerValue() {
        return ClientFactory.createClient(new NetworkSetting(new Network<>(new byte[] {(byte) 3}, "local", HbarUnit.HBAR), Set.of(new ConsensusNode(new IpAddress(new byte[] {(byte) 127, (byte) 0, (byte) 0, (byte) 1}), 35211, new AccountId(0L, 0L, "", 3L, null, null))), Set.of(new MirrorNode("http://127.0.0.1:38081"))), new Account(new AccountId(0L, 0L, "", 1001L, null, null), KeysFactory.createPrivateKey(KeyAlgorithm.ED25519, ByteImportEncoding.HEX, "d3671a1e98bb22f011c0e4bcf5125590e15d8f21a7017309bb558852039bc75c"))).transactionSigner();
    }

    private static HieroClient<NativeTokenUnit> create() {
        return new HieroClient<>(operatorAccountValue(), networkValue(), transactionSignerValue());
    }
}
