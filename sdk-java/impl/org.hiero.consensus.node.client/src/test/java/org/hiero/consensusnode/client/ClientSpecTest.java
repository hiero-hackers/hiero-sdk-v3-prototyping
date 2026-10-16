package org.hiero.consensusnode.client;

import static org.hiero.sdk.testing.Testing.*;

import java.util.Set;
import org.hiero.hedera.HbarUnit;
import org.hiero.keys.ByteImportEncoding;
import org.hiero.keys.KeyAlgorithm;
import org.hiero.keys.KeysFactory;
import org.hiero.keys.PrivateKey;
import org.hiero.ledger.AccountId;
import org.hiero.ledger.ConsensusNode;
import org.hiero.ledger.IpAddress;
import org.hiero.ledger.MirrorNode;
import org.hiero.ledger.Network;
import org.hiero.ledger.config.NetworkSetting;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/// Tests of the spec `client`, generated from the `tests` blocks of its `## Testing` section.
@DisplayName("client")
class ClientSpecTest {

    /// `consensusnode.client/a-client-keeps-the-operator-account-it-was-created-with`
    ///
    /// Source: the Go SDK (TestUnitClientFromConfigWithOperator), the Python SDK (client_test.py,
    /// test_for_testnet_then_set_operator) and the C++ SDK (ClientUnitTests): a client keeps its operator.
    @Test
    @DisplayName("a client keeps the operator account it was created with")
    void aClientKeepsTheOperatorAccountItWasCreatedWith() {
        // given
        final Account operator = defaultAccount();

        // when
        final HieroClient<?> client = ClientFactory.createClient(defaultNetworkSetting(), operator);

        // then
        assertEquals(operator, client.operatorAccount());
        assertEquals(operator.accountId(), client.operatorAccount().accountId());
        assertEquals(operator.privateKey(), client.operatorAccount().privateKey());
    }

    /// `consensusnode.client/a-client-is-bound-to-the-network-of-its-network-setting`
    ///
    /// Source: the Java SDK (ClientTest.forMainnet, forNameReturnsCorrectNetwork), the Go SDK (TestUnitClientFromConfig)
    /// and the Python SDK (client_test.py, test_factory_basic_setup): a client is bound to the network it was created for.
    @Test
    @DisplayName("a client is bound to the network of its network setting")
    void aClientIsBoundToTheNetworkOfItsNetworkSetting() {
        // given
        final NetworkSetting setting = defaultNetworkSetting();

        // when
        final HieroClient<?> client = ClientFactory.createClient(setting, defaultAccount());

        // then
        assertEquals(setting.network(), client.network());
    }

    /// `consensusnode.client/the-default-signer-signs-with-the-private-key-of-the-operator-account`
    ///
    /// Source: this spec (by default the operator account's private key signs); the Java and JavaScript SDKs
    /// (Client.setOperator signs with the operator's private key) and the Go SDK (the operator's key signs).
    @Test
    @DisplayName("the default signer signs with the private key of the operator account")
    void theDefaultSignerSignsWithThePrivateKeyOfTheOperatorAccount() {
        // given
        final Account operator = defaultAccount();
        final HieroClient<?> client = ClientFactory.createClient(defaultNetworkSetting(), operator);
        final AccountId node = AccountId.fromString("0.0.3");
        final byte[] transactionBytes = utf8("transaction body");

        // when
        final NodeSignature signature = client.transactionSigner().signTransaction(transactionBytes, node);

        // then
        assertEquals(node, signature.node());
        assertEquals(operator.privateKey().createPublicKey(), signature.publicKey());
        assertTrue(signature.publicKey().verify(transactionBytes, signature.signature()));
        assertFalse(signature.publicKey().verify(utf8("another body"), signature.signature()));
    }

    /// `consensusnode.client/a-client-created-with-a-signer-signs-with-that-signer`
    ///
    /// Source: the Java and JavaScript SDKs (Client.setOperatorWith: an operator whose transactions are signed by a
    /// given signer instead of its private key). The second key is the secp256k1 key of the tests of keys.md.
    @Test
    @DisplayName("a client created with a signer signs with that signer")
    void aClientCreatedWithASignerSignsWithThatSigner() {
        // given
        final PrivateKey signerKey = KeysFactory.createPrivateKey(KeyAlgorithm.ECDSA, ByteImportEncoding.HEX, "8776c6b831a1b61ac10dac0304a2843de4716f54b1919bb91a2685d0fe3f3048");
        final Account signerAccount = new Account(AccountId.fromString("0.0.1002"), signerKey);
        final TransactionSigner signer = ClientFactory.createClient(defaultNetworkSetting(), signerAccount).transactionSigner();
        final byte[] transactionBytes = utf8("transaction body");

        // when
        final HieroClient<?> client = ClientFactory.createClient(defaultNetworkSetting(), defaultAccount(), signer);
        final NodeSignature signature = client.transactionSigner().signTransaction(transactionBytes, AccountId.fromString("0.0.3"));

        // then
        assertEquals(defaultAccount(), client.operatorAccount());
        assertEquals(signerKey.createPublicKey(), signature.publicKey());
        assertTrue(signature.publicKey().verify(transactionBytes, signature.signature()));
    }

    private static Account defaultAccount() {
        return new Account(defaultAccountId(), defaultPrivateKey());
    }

    private static AccountId defaultAccountId() {
        return new AccountId(0L, 0L, "", 1001L, null, null);
    }

    private static PrivateKey defaultPrivateKey() {
        return KeysFactory.createPrivateKey(KeyAlgorithm.ED25519, ByteImportEncoding.HEX, "d3671a1e98bb22f011c0e4bcf5125590e15d8f21a7017309bb558852039bc75c");
    }

    private static NetworkSetting defaultNetworkSetting() {
        return new NetworkSetting(defaultNetwork(), Set.of(defaultConsensusNode()), Set.of(defaultMirrorNode()));
    }

    private static Network<?> defaultNetwork() {
        return new Network<>(new byte[] {(byte) 3}, "local", HbarUnit.HBAR);
    }

    private static ConsensusNode defaultConsensusNode() {
        return new ConsensusNode(defaultIpAddress(), 35211, new AccountId(0L, 0L, "", 3L, null, null));
    }

    private static IpAddress defaultIpAddress() {
        return new IpAddress(new byte[] {(byte) 127, (byte) 0, (byte) 0, (byte) 1});
    }

    private static MirrorNode defaultMirrorNode() {
        return new MirrorNode("http://127.0.0.1:38081");
    }
}
